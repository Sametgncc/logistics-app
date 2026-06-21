package com.logistics.backend.service.business;

import com.logistics.backend.entity.business.DriverLocation;
import com.logistics.backend.entity.business.Load;
import com.logistics.backend.entity.business.LoadMatch;
import com.logistics.backend.entity.business.RoutePlan;
import com.logistics.backend.entity.enums.LoadStatus;
import com.logistics.backend.entity.enums.MatchStatus;
import com.logistics.backend.entity.enums.RoutePlanStatus;
import com.logistics.backend.exception.BadRequestException;
import com.logistics.backend.exception.ResourceNotFoundException;
import com.logistics.backend.payload.request.UpdateDriverLocationRequest;
import com.logistics.backend.payload.response.DriverLocationResponse;
import com.logistics.backend.payload.response.LoadTrackingResponse;
import com.logistics.backend.repository.business.DriverLocationRepository;
import com.logistics.backend.repository.business.LoadMatchRepository;
import com.logistics.backend.repository.business.LoadRepository;
import com.logistics.backend.repository.business.RoutePlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DriverTrackingService {

    private static final Duration MAX_FUTURE_LOCATION_TIME = Duration.ofMinutes(5);
    private static final Duration STALE_LOCATION_AFTER = Duration.ofMinutes(2);

    private final RoutePlanRepository routePlanRepository;
    private final LoadRepository loadRepository;
    private final LoadMatchRepository loadMatchRepository;
    private final DriverLocationRepository driverLocationRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @Transactional
    public DriverLocationResponse recordLocation(
            Long routePlanId,
            Long driverId,
            UpdateDriverLocationRequest request
    ) {
        RoutePlan routePlan = routePlanRepository.findById(routePlanId)
                .orElseThrow(() -> new ResourceNotFoundException("Route plan not found with id: " + routePlanId));

        if (!routePlan.getDriver().getId().equals(driverId)) {
            throw new AccessDeniedException("Only the route plan's driver can share its location");
        }

        if (routePlan.getStatus() != RoutePlanStatus.IN_PROGRESS) {
            throw new BadRequestException("Location can only be shared for an IN_PROGRESS route plan");
        }

        Instant now = Instant.now();
        if (request.getCapturedAt().isAfter(now.plus(MAX_FUTURE_LOCATION_TIME))) {
            throw new BadRequestException("Location capture time cannot be in the future");
        }

        List<LoadMatch> activeMatches = activeMatchesForRoutePlan(routePlanId);
        if (activeMatches.isEmpty()) {
            throw new BadRequestException("Location can only be shared while carrying an active load");
        }

        DriverLocation savedLocation = driverLocationRepository.save(DriverLocation.builder()
                .routePlan(routePlan)
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .accuracyMeters(request.getAccuracyMeters())
                .speedKph(request.getSpeedKph())
                .headingDegrees(request.getHeadingDegrees())
                .capturedAt(request.getCapturedAt())
                .receivedAt(now)
                .build());

        DriverLocationResponse response = toDriverLocationResponse(savedLocation);
        activeMatches.forEach(match -> publishLocationToCustomer(match.getLoad(), response));
        return response;
    }

    @Transactional(readOnly = true)
    public LoadTrackingResponse getLatestLocationForLoad(Long loadId, Long customerId) {
        Load load = loadRepository.findById(loadId)
                .orElseThrow(() -> new ResourceNotFoundException("Load not found with id: " + loadId));

        if (!load.getCustomer().getId().equals(customerId)) {
            throw new AccessDeniedException("Only the load owner can view live tracking");
        }

        if (load.getStatus() != LoadStatus.IN_TRANSIT) {
            throw new BadRequestException("Live tracking is only available while the load is IN_TRANSIT");
        }

        LoadMatch activeMatch = loadMatchRepository.findByLoadIdAndStatus(loadId, MatchStatus.ACCEPTED)
                .stream()
                .filter(match -> match.getRoutePlan().getStatus() == RoutePlanStatus.IN_PROGRESS)
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("No active route plan found for this load"));

        DriverLocation location = driverLocationRepository
                .findTopByRoutePlanIdOrderByCapturedAtDesc(activeMatch.getRoutePlan().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Driver has not shared a location yet"));

        return toLoadTrackingResponse(loadId, toDriverLocationResponse(location));
    }

    private List<LoadMatch> activeMatchesForRoutePlan(Long routePlanId) {
        return loadMatchRepository.findByRoutePlanId(routePlanId)
                .stream()
                .filter(match -> match.getStatus() == MatchStatus.ACCEPTED)
                .filter(match -> match.getLoad().getStatus() == LoadStatus.IN_TRANSIT)
                .toList();
    }

    private void publishLocationToCustomer(Load load, DriverLocationResponse location) {
        messagingTemplate.convertAndSendToUser(
                String.valueOf(load.getCustomer().getId()),
                "/queue/tracking",
                toLoadTrackingResponse(load.getId(), location)
        );
    }

    private DriverLocationResponse toDriverLocationResponse(DriverLocation location) {
        return DriverLocationResponse.builder()
                .routePlanId(location.getRoutePlan().getId())
                .latitude(location.getLatitude())
                .longitude(location.getLongitude())
                .accuracyMeters(location.getAccuracyMeters())
                .speedKph(location.getSpeedKph())
                .headingDegrees(location.getHeadingDegrees())
                .capturedAt(location.getCapturedAt())
                .receivedAt(location.getReceivedAt())
                .stale(location.getCapturedAt().isBefore(Instant.now().minus(STALE_LOCATION_AFTER)))
                .build();
    }

    private LoadTrackingResponse toLoadTrackingResponse(Long loadId, DriverLocationResponse location) {
        return LoadTrackingResponse.builder()
                .loadId(loadId)
                .routePlanId(location.getRoutePlanId())
                .latitude(location.getLatitude())
                .longitude(location.getLongitude())
                .accuracyMeters(location.getAccuracyMeters())
                .speedKph(location.getSpeedKph())
                .headingDegrees(location.getHeadingDegrees())
                .capturedAt(location.getCapturedAt())
                .receivedAt(location.getReceivedAt())
                .stale(location.isStale())
                .build();
    }
}

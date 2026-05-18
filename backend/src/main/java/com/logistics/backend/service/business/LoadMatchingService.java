package com.logistics.backend.service.business;

import com.logistics.backend.entity.business.Load;
import com.logistics.backend.entity.business.LoadMatch;
import com.logistics.backend.entity.business.RoutePlan;
import com.logistics.backend.entity.enums.LoadStatus;
import com.logistics.backend.entity.enums.MatchStatus;
import com.logistics.backend.entity.enums.NotificationType;
import com.logistics.backend.entity.enums.RoutePlanStatus;
import com.logistics.backend.exception.BadRequestException;
import com.logistics.backend.exception.ResourceNotFoundException;
import com.logistics.backend.mapper.LoadMatchMapper;
import com.logistics.backend.payload.response.LoadMatchResponse;
import com.logistics.backend.payload.response.MatchDebugResponse;
import com.logistics.backend.repository.business.LoadMatchRepository;
import com.logistics.backend.repository.business.LoadRepository;
import com.logistics.backend.repository.business.RoutePlanRepository;
import com.logistics.backend.service.user.NotificationService;
import com.logistics.backend.util.GeoUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LoadMatchingService {


    private static final double MAX_PICKUP_DISTANCE_FROM_ROUTE_ORIGIN_KM = 600.0;
    private static final double MAX_DELIVERY_DISTANCE_FROM_ROUTE_DESTINATION_KM = 80.0;
    private static final double MAX_TOTAL_DEVIATION_KM = 700.0;

    private final LoadRepository loadRepository;
    private final RoutePlanRepository routePlanRepository;
    private final LoadMatchRepository loadMatchRepository;
    private final LoadMatchMapper loadMatchMapper;
    private final NotificationService notificationService;

    public List<LoadMatchResponse> findSuitableRoutePlansForLoad(Long loadId) {
        Load load = loadRepository.findById(loadId)
                .orElseThrow(() -> new ResourceNotFoundException("Load not found with id: " + loadId));

        validateLoadIsOpen(load);

        List<RoutePlan> openRoutePlans = routePlanRepository.findByStatus(RoutePlanStatus.OPEN);

        return openRoutePlans.stream()
                .filter(routePlan -> isRoutePlanSuitableForLoad(routePlan, load))
                .map(routePlan -> createMatchIfNotExists(load, routePlan))
                .map(loadMatchMapper::toResponse)
                .toList();
    }

    public List<LoadMatchResponse> findSuitableLoadsForRoutePlan(Long routePlanId) {
        RoutePlan routePlan = routePlanRepository.findById(routePlanId)
                .orElseThrow(() -> new ResourceNotFoundException("Route plan not found with id: " + routePlanId));

        validateRoutePlanIsOpen(routePlan);

        List<Load> openLoads = loadRepository.findByStatus(LoadStatus.OPEN);

        return openLoads.stream()
                .filter(load -> isRoutePlanSuitableForLoad(routePlan, load))
                .map(load -> createMatchIfNotExists(load, routePlan))
                .map(loadMatchMapper::toResponse)
                .toList();
    }

    public List<LoadMatchResponse> previewSuitableLoadsForRoutePlan(Long routePlanId) {
        RoutePlan routePlan = routePlanRepository.findById(routePlanId)
                .orElseThrow(() -> new ResourceNotFoundException("Route plan not found with id: " + routePlanId));

        validateRoutePlanIsOpen(routePlan);

        List<Load> openLoads = loadRepository.findByStatus(LoadStatus.OPEN);

        return openLoads.stream()
                .filter(load -> isRoutePlanSuitableForLoad(routePlan, load))
                .map(load -> buildMatch(load, routePlan))
                .map(loadMatchMapper::toResponse)
                .toList();
    }

    public List<LoadMatchResponse> getMatchesByLoadId(Long loadId) {
        loadRepository.findById(loadId)
                .orElseThrow(() -> new ResourceNotFoundException("Load not found with id: " + loadId));

        return loadMatchRepository.findByLoadId(loadId)
                .stream()
                .map(loadMatchMapper::toResponse)
                .toList();
    }

    public List<LoadMatchResponse> getMatchesByRoutePlanId(Long routePlanId) {
        routePlanRepository.findById(routePlanId)
                .orElseThrow(() -> new ResourceNotFoundException("Route plan not found with id: " + routePlanId));

        return loadMatchRepository.findByRoutePlanId(routePlanId)
                .stream()
                .map(loadMatchMapper::toResponse)
                .toList();
    }

    public LoadMatchResponse acceptMatch(Long matchId) {
        LoadMatch match = loadMatchRepository.findById(matchId)
                .orElseThrow(() -> new ResourceNotFoundException("Match not found with id: " + matchId));

        if (match.getStatus() != MatchStatus.PENDING) {
            throw new BadRequestException("Only PENDING matches can be accepted");
        }
        match.setStatus(MatchStatus.ACCEPTED);
       // match.getLoad().setStatus(LoadStatus.MATCHED);

        return loadMatchMapper.toResponse(loadMatchRepository.save(match));
    }

    public LoadMatchResponse rejectMatch(Long matchId) {
        LoadMatch match = loadMatchRepository.findById(matchId)
                .orElseThrow(() -> new ResourceNotFoundException("Match not found with id: " + matchId));

        if (match.getStatus() != MatchStatus.PENDING) {
            throw new BadRequestException("Only PENDING matches can be rejected");
        }

        match.setStatus(MatchStatus.REJECTED);

        return loadMatchMapper.toResponse(loadMatchRepository.save(match));
    }

    private LoadMatch createMatchIfNotExists(Load load, RoutePlan routePlan) {
        return loadMatchRepository.findByLoadIdAndRoutePlanId(load.getId(), routePlan.getId())
                .orElseGet(() -> {
                    LoadMatch match = buildMatch(load, routePlan);
                    LoadMatch savedMatch = loadMatchRepository.save(match);
                    notifyMatchCreated(savedMatch);
                    return savedMatch;
                });
    }

    private LoadMatch buildMatch(Load load, RoutePlan routePlan) {
        BigDecimal score = calculateMatchScore(load, routePlan);

        double directDistance = calculateDirectRouteDistance(routePlan);
        double routeWithLoadDistance = calculateRouteWithLoadDistance(routePlan, load);
        double deviation = calculateDeviationKm(routePlan, load);

        return LoadMatch.builder()
                .load(load)
                .routePlan(routePlan)
                .status(MatchStatus.PENDING)
                .matchScore(score)
                .note(buildMatchNote(load, routePlan))
                .createdAt(LocalDateTime.now())
                .directRouteDistanceKm(BigDecimal.valueOf(directDistance))
                .routeWithLoadDistanceKm(BigDecimal.valueOf(routeWithLoadDistance))
                .deviationKm(BigDecimal.valueOf(deviation))
                .build();
    }

    private boolean isRoutePlanSuitableForLoad(RoutePlan routePlan, Load load) {
        return isRoutePlanOpen(routePlan)
                && hasEnoughCapacity(routePlan, load)
                && hasLocationCompatibility(routePlan, load)
                && hasTimeCompatibility(routePlan, load)
                && hasAcceptableDeviation(routePlan, load);
    }

    private boolean isRoutePlanOpen(RoutePlan routePlan) {
        return routePlan.getStatus() == RoutePlanStatus.OPEN;
    }

    private boolean hasEnoughCapacity(RoutePlan routePlan, Load load) {
        return routePlan.getAvailableCapacityKg().compareTo(load.getWeightKg()) >= 0
                && routePlan.getAvailableCapacityVolumeM3().compareTo(load.getVolumeM3()) >= 0;
    }

    private boolean hasCityCompatibility(RoutePlan routePlan, Load load) {
        boolean exactMatch =
                routePlan.getOriginCity().equalsIgnoreCase(load.getPickupCity())
                        && routePlan.getDestinationCity().equalsIgnoreCase(load.getDeliveryCity());

        boolean sameDestination =
                routePlan.getDestinationCity().equalsIgnoreCase(load.getDeliveryCity());

        return exactMatch || sameDestination;
    }

    private boolean hasLocationCompatibility(RoutePlan routePlan, Load load) {
        return hasCityCompatibility(routePlan, load)
                || hasPickupAndDeliveryNearRouteEndpoints(routePlan, load)
                || hasAcceptableDeviation(routePlan, load);
    }

    private boolean hasTimeCompatibility(RoutePlan routePlan, Load load) {
        if (routePlan.getDepartureTime() == null || load.getPickupTime() == null) {
            return false;
        }

        if (routePlan.getDepartureTime().isAfter(load.getPickupTime())) {
            return false;
        }

        if (load.getDeliveryDeadline() != null &&
                routePlan.getEstimatedArrivalTime() != null &&
                routePlan.getEstimatedArrivalTime().isAfter(load.getDeliveryDeadline())) {
            return false;
        }

        return true;
    }

    private BigDecimal calculateMatchScore(Load load, RoutePlan routePlan) {
        int score = 0;

        if (routePlan.getDestinationCity().equalsIgnoreCase(load.getDeliveryCity())) {
            score += 30;
        }

        if (routePlan.getOriginCity().equalsIgnoreCase(load.getPickupCity())) {
            score += 25;
        }

        if (hasEnoughCapacity(routePlan, load)) {
            score += 20;
        }

        double deviationKm = calculateDeviationKm(routePlan, load);
        if (deviationKm <= 50) {
            score += 25;
        } else if (deviationKm <= 150) {
            score += 20;
        } else if (deviationKm <= 300) {
            score += 15;
        } else if (deviationKm <= 500) {
            score += 10;
        } else {
            score += 5;
        }

        return BigDecimal.valueOf(score);
    }

    private void validateLoadIsOpen(Load load) {
        if (load.getStatus() != LoadStatus.OPEN) {
            throw new BadRequestException("Only OPEN loads can be matched");
        }
    }

    private void validateRoutePlanIsOpen(RoutePlan routePlan) {
        if (routePlan.getStatus() != RoutePlanStatus.OPEN) {
            throw new BadRequestException("Only OPEN route plans can be matched");
        }
    }




    private String buildMatchNote(Load load, RoutePlan routePlan) {
        double directDistance = calculateDirectRouteDistance(routePlan);
        double routeWithLoadDistance = calculateRouteWithLoadDistance(routePlan, load);
        double deviation = calculateDeviationKm(routePlan, load);

        return String.format(
                "Direct route: %.2f km, route with load: %.2f km, deviation: %.2f km",
                directDistance,
                routeWithLoadDistance,
                deviation
        );
    }




    private boolean hasPickupAndDeliveryNearRouteEndpoints(RoutePlan routePlan, Load load) {
        double pickupDistanceFromRouteOrigin = GeoUtils.calculateDistance(
                routePlan.getOriginLat().doubleValue(),
                routePlan.getOriginLng().doubleValue(),
                load.getPickupLat().doubleValue(),
                load.getPickupLng().doubleValue()
        );

        double deliveryDistanceFromRouteDestination = GeoUtils.calculateDistance(
                routePlan.getDestinationLat().doubleValue(),
                routePlan.getDestinationLng().doubleValue(),
                load.getDeliveryLat().doubleValue(),
                load.getDeliveryLng().doubleValue()
        );

        return pickupDistanceFromRouteOrigin <= MAX_PICKUP_DISTANCE_FROM_ROUTE_ORIGIN_KM
                && deliveryDistanceFromRouteDestination <= MAX_DELIVERY_DISTANCE_FROM_ROUTE_DESTINATION_KM;
    }

    private double calculateDistanceKm(
            BigDecimal lat1,
            BigDecimal lng1,
            BigDecimal lat2,
            BigDecimal lng2
    ) {
        return GeoUtils.calculateDistance(
                lat1.doubleValue(),
                lng1.doubleValue(),
                lat2.doubleValue(),
                lng2.doubleValue()
        );
    }

    private double calculateDirectRouteDistance(RoutePlan routePlan) {
        return calculateDistanceKm(
                routePlan.getOriginLat(),
                routePlan.getOriginLng(),
                routePlan.getDestinationLat(),
                routePlan.getDestinationLng()
        );
    }

    private double calculateRouteWithLoadDistance(RoutePlan routePlan, Load load) {
        double originToPickup = calculateDistanceKm(
                routePlan.getOriginLat(),
                routePlan.getOriginLng(),
                load.getPickupLat(),
                load.getPickupLng()
        );

        double pickupToDelivery = calculateDistanceKm(
                load.getPickupLat(),
                load.getPickupLng(),
                load.getDeliveryLat(),
                load.getDeliveryLng()
        );

        double deliveryToDestination = calculateDistanceKm(
                load.getDeliveryLat(),
                load.getDeliveryLng(),
                routePlan.getDestinationLat(),
                routePlan.getDestinationLng()
        );

        return originToPickup + pickupToDelivery + deliveryToDestination;
    }

    private double calculateDeviationKm(RoutePlan routePlan, Load load) {
        double directRouteDistance = calculateDirectRouteDistance(routePlan);
        double routeWithLoadDistance = calculateRouteWithLoadDistance(routePlan, load);

        return routeWithLoadDistance - directRouteDistance;
    }

    private boolean hasAcceptableDeviation(RoutePlan routePlan, Load load) {
        double deviationKm = calculateDeviationKm(routePlan, load);
        return deviationKm <= MAX_TOTAL_DEVIATION_KM;
    }




    private MatchDebugResponse buildDebugResponse(Load load, RoutePlan routePlan) {
        boolean routePlanOpen = isRoutePlanOpen(routePlan);
        boolean enoughCapacity = hasEnoughCapacity(routePlan, load);
        boolean cityCompatible = hasLocationCompatibility(routePlan, load);
        boolean timeCompatible = hasTimeCompatibility(routePlan, load);
        boolean acceptableDeviation = hasAcceptableDeviation(routePlan, load);

        double directDistance = calculateDirectRouteDistance(routePlan);
        double routeWithLoadDistance = calculateRouteWithLoadDistance(routePlan, load);
        double deviation = calculateDeviationKm(routePlan, load);

        boolean suitable = routePlanOpen
                && enoughCapacity
                && cityCompatible
                && timeCompatible
                && acceptableDeviation;

        return MatchDebugResponse.builder()
                .loadId(load.getId())
                .routePlanId(routePlan.getId())
                .routePlanOpen(routePlanOpen)
                .enoughCapacity(enoughCapacity)
                .cityCompatible(cityCompatible)
                .timeCompatible(timeCompatible)
                .acceptableDeviation(acceptableDeviation)
                .directRouteDistanceKm(directDistance)
                .routeWithLoadDistanceKm(routeWithLoadDistance)
                .deviationKm(deviation)
                .suitable(suitable)
                .reason(buildDebugReason(routePlanOpen, enoughCapacity, cityCompatible, timeCompatible, acceptableDeviation))
                .build();
    }

    private String buildDebugReason(
            boolean routePlanOpen,
            boolean enoughCapacity,
            boolean cityCompatible,
            boolean timeCompatible,
            boolean acceptableDeviation
    ) {
        if (!routePlanOpen) {
            return "Route plan is not OPEN";
        }

        if (!enoughCapacity) {
            return "Route plan capacity is not enough";
        }

        if (!cityCompatible) {
            return "Location compatibility failed";
        }

        if (!timeCompatible) {
            return "Time compatibility failed";
        }

        if (!acceptableDeviation) {
            return "Route deviation is too high";
        }

        return "Route plan is suitable";
    }

    private void notifyMatchCreated(LoadMatch match) {
        notificationService.createNotification(
                match.getLoad().getCustomer().getId(),
                NotificationType.LOAD_MATCH_CREATED,
                "New route match",
                "A suitable route was found for your load.",
                "LOAD_MATCH",
                match.getId()
        );

        notificationService.createNotification(
                match.getRoutePlan().getDriver().getId(),
                NotificationType.LOAD_MATCH_CREATED,
                "New suitable load",
                "A suitable load was found for your route plan.",
                "LOAD_MATCH",
                match.getId()
        );
    }

    public List<MatchDebugResponse> debugSuitableRoutePlansForLoad(Long loadId) {
        Load load = loadRepository.findById(loadId)
                .orElseThrow(() -> new ResourceNotFoundException("Load not found with id: " + loadId));

        List<RoutePlan> routePlans = routePlanRepository.findAll();

        return routePlans.stream()
                .map(routePlan -> buildDebugResponse(load, routePlan))
                .toList();
    }
}

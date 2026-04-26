package com.logistics.backend.service.business;

import com.logistics.backend.entity.business.Load;
import com.logistics.backend.entity.business.LoadMatch;
import com.logistics.backend.entity.business.RoutePlan;
import com.logistics.backend.entity.enums.LoadStatus;
import com.logistics.backend.entity.enums.MatchStatus;
import com.logistics.backend.entity.enums.RoutePlanStatus;
import com.logistics.backend.exception.BadRequestException;
import com.logistics.backend.exception.ResourceNotFoundException;
import com.logistics.backend.mapper.LoadMatchMapper;
import com.logistics.backend.payload.response.LoadMatchResponse;
import com.logistics.backend.repository.business.LoadMatchRepository;
import com.logistics.backend.repository.business.LoadRepository;
import com.logistics.backend.repository.business.RoutePlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LoadMatchingService {

    private final LoadRepository loadRepository;
    private final RoutePlanRepository routePlanRepository;
    private final LoadMatchRepository loadMatchRepository;
    private final LoadMatchMapper loadMatchMapper;

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
        match.getLoad().setStatus(LoadStatus.MATCHED);

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
                    BigDecimal score = calculateMatchScore(load, routePlan);

                    LoadMatch match = LoadMatch.builder()
                            .load(load)
                            .routePlan(routePlan)
                            .status(MatchStatus.PENDING)
                            .matchScore(score)
                            .note("Suitable route plan found for this load")
                            .createdAt(LocalDateTime.now())
                            .build();

                    return loadMatchRepository.save(match);
                });
    }

    private boolean isRoutePlanSuitableForLoad(RoutePlan routePlan, Load load) {
        return isRoutePlanOpen(routePlan)
                && hasEnoughCapacity(routePlan, load)
                && hasCityCompatibility(routePlan, load)
                && hasTimeCompatibility(routePlan, load);
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

        if (routePlan.getOriginCity().equalsIgnoreCase(load.getPickupCity())) {
            score += 40;
        }

        if (routePlan.getDestinationCity().equalsIgnoreCase(load.getDeliveryCity())) {
            score += 40;
        }

        if (hasEnoughCapacity(routePlan, load)) {
            score += 20;
        }

        return BigDecimal.valueOf(score);
    }

    private void validateLoadIsOpen(Load load) {
        if (load.getStatus() != LoadStatus.OPEN) {
            throw new BadRequestException("Only OPEN loads can be matched");
        }
    }
}
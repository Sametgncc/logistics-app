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
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LoadMatchingService {

    // Maksimum Sapma Limiti: Sürücünün ana rotasından en fazla ne kadar çıkabileceği (km)
    private static final double MAX_TOTAL_DEVIATION_KM = 75.0;

    // Yakınlık Bonusu Puanlama Sınırı (km)
    private static final double BONUS_DISTANCE_THRESHOLD_KM = 50.0;

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

    @Transactional
    public LoadMatchResponse acceptMatch(Long matchId) {
        LoadMatch match = loadMatchRepository.findById(matchId)
                .orElseThrow(() -> new ResourceNotFoundException("Match not found with id: " + matchId));

        if (match.getStatus() != MatchStatus.PENDING) {
            throw new BadRequestException("Only PENDING matches can be accepted");
        }
        match.setStatus(MatchStatus.ACCEPTED);

        Load load = match.getLoad();
        load.setStatus(LoadStatus.MATCHED);

        RoutePlan routePlan = match.getRoutePlan();
        routePlan.setAvailableCapacityKg(routePlan.getAvailableCapacityKg().subtract(load.getWeightKg()));
        routePlan.setAvailableCapacityVolumeM3(routePlan.getAvailableCapacityVolumeM3().subtract(load.getVolumeM3()));

        if (routePlan.getAvailableCapacityKg().compareTo(BigDecimal.ZERO) <= 0 ||
                routePlan.getAvailableCapacityVolumeM3().compareTo(BigDecimal.ZERO) <= 0) {
            routePlan.setStatus(RoutePlanStatus.FULL);
        }

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
                .note(buildMatchNote(load, routePlan, directDistance, routeWithLoadDistance, deviation))
                .createdAt(LocalDateTime.now())
                .directRouteDistanceKm(BigDecimal.valueOf(directDistance))
                .routeWithLoadDistanceKm(BigDecimal.valueOf(routeWithLoadDistance))
                .deviationKm(BigDecimal.valueOf(deviation))
                .build();
    }

    private boolean isRoutePlanSuitableForLoad(RoutePlan routePlan, Load load) {
        boolean open = isRoutePlanOpen(routePlan);
        boolean cap = hasEnoughCapacity(routePlan, load);
        boolean time = hasTimeCompatibility(routePlan, load);
        boolean dev = hasAcceptableDeviation(routePlan, load);

        if (!open || !cap || !time || !dev) {
            System.out.println("Eşleşme başarısız! LoadID: " + load.getId() + " RouteID: " + routePlan.getId());
            System.out.println("Detaylar -> Open: " + open + ", Capacity: " + cap + ", Time: " + time + ", Deviation: " + dev);
        }

        return open && cap && time && dev;
    }

    private boolean isRoutePlanOpen(RoutePlan routePlan) {
        return routePlan.getStatus() == RoutePlanStatus.OPEN;
    }

    private boolean hasEnoughCapacity(RoutePlan routePlan, Load load) {
        return routePlan.getAvailableCapacityKg().compareTo(load.getWeightKg()) >= 0
                && routePlan.getAvailableCapacityVolumeM3().compareTo(load.getVolumeM3()) >= 0;
    }

    private boolean hasAcceptableDeviation(RoutePlan routePlan, Load load) {
        double deviationKm = calculateDeviationKm(routePlan, load);
        return deviationKm <= MAX_TOTAL_DEVIATION_KM;
    }

    private boolean hasTimeCompatibility(RoutePlan routePlan, Load load) {
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

        // 1. Kapasite yeterliliği puanı
        if (hasEnoughCapacity(routePlan, load)) {
            score += 20;
        }

        // 2. Yükün teslimat noktası ile sürücünün hedefi birbirine yakın mı? (Şehir ismi yerine koordinat kontrolü)
        double deliveryToDestinationDistance = calculateDistanceKm(
                load.getDeliveryLat(), load.getDeliveryLng(),
                routePlan.getDestinationLat(), routePlan.getDestinationLng()
        );
        if (deliveryToDestinationDistance <= BONUS_DISTANCE_THRESHOLD_KM) {
            score += 30;
        }

        // 3. Yükün alınacağı nokta ile sürücünün başlangıcı birbirine yakın mı?
        double originToPickupDistance = calculateDistanceKm(
                routePlan.getOriginLat(), routePlan.getOriginLng(),
                load.getPickupLat(), load.getPickupLng()
        );
        if (originToPickupDistance <= BONUS_DISTANCE_THRESHOLD_KM) {
            score += 25;
        }

        // 4. Genel Sapma Puanı (Ne kadar az sapma, o kadar yüksek puan)
        double deviationKm = calculateDeviationKm(routePlan, load);
        if (deviationKm <= 10) {
            score += 25;
        } else if (deviationKm <= 30) {
            score += 20;
        } else if (deviationKm <= 50) {
            score += 15;
        } else if (deviationKm <= 75) {
            score += 10;
        } else {
            score += 0;
        }

        // Toplam skoru 100 üzerinden sınırlandır
        return BigDecimal.valueOf(Math.min(score, 100));
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

    private String buildMatchNote(Load load, RoutePlan routePlan, double direct, double routeWithLoad, double deviation) {
        return String.format(
                "Direct route: %.2f km, route with load: %.2f km, deviation: %.2f km",
                direct, routeWithLoad, deviation
        );
    }

    private double calculateDistanceKm(BigDecimal lat1, BigDecimal lng1, BigDecimal lat2, BigDecimal lng2) {
        return GeoUtils.calculateDistance(
                lat1.doubleValue(), lng1.doubleValue(),
                lat2.doubleValue(), lng2.doubleValue()
        );
    }

    private double calculateDirectRouteDistance(RoutePlan routePlan) {
        return calculateDistanceKm(
                routePlan.getOriginLat(), routePlan.getOriginLng(),
                routePlan.getDestinationLat(), routePlan.getDestinationLng()
        );
    }

    private double calculateRouteWithLoadDistance(RoutePlan routePlan, Load load) {
        double originToPickup = calculateDistanceKm(
                routePlan.getOriginLat(), routePlan.getOriginLng(),
                load.getPickupLat(), load.getPickupLng()
        );

        double pickupToDelivery = calculateDistanceKm(
                load.getPickupLat(), load.getPickupLng(),
                load.getDeliveryLat(), load.getDeliveryLng()
        );

        double deliveryToDestination = calculateDistanceKm(
                load.getDeliveryLat(), load.getDeliveryLng(),
                routePlan.getDestinationLat(), routePlan.getDestinationLng()
        );

        return originToPickup + pickupToDelivery + deliveryToDestination;
    }

    private double calculateDeviationKm(RoutePlan routePlan, Load load) {
        double directRouteDistance = calculateDirectRouteDistance(routePlan);
        double routeWithLoadDistance = calculateRouteWithLoadDistance(routePlan, load);
        return routeWithLoadDistance - directRouteDistance;
    }

    private MatchDebugResponse buildDebugResponse(Load load, RoutePlan routePlan) {
        boolean routePlanOpen = isRoutePlanOpen(routePlan);
        boolean enoughCapacity = hasEnoughCapacity(routePlan, load);
        boolean timeCompatible = hasTimeCompatibility(routePlan, load);
        boolean acceptableDeviation = hasAcceptableDeviation(routePlan, load);

        double directDistance = calculateDirectRouteDistance(routePlan);
        double routeWithLoadDistance = calculateRouteWithLoadDistance(routePlan, load);
        double deviation = calculateDeviationKm(routePlan, load);

        boolean suitable = routePlanOpen
                && enoughCapacity
                && timeCompatible
                && acceptableDeviation;

        return MatchDebugResponse.builder()
                .loadId(load.getId())
                .routePlanId(routePlan.getId())
                .routePlanOpen(routePlanOpen)
                .enoughCapacity(enoughCapacity)
                .cityCompatible(acceptableDeviation) // Artık lokasyon uyumluluğunu sapma belirliyor
                .timeCompatible(timeCompatible)
                .acceptableDeviation(acceptableDeviation)
                .directRouteDistanceKm(directDistance)
                .routeWithLoadDistanceKm(routeWithLoadDistance)
                .deviationKm(deviation)
                .suitable(suitable)
                .reason(buildDebugReason(routePlanOpen, enoughCapacity, timeCompatible, acceptableDeviation))
                .build();
    }

    private String buildDebugReason(boolean routePlanOpen, boolean enoughCapacity, boolean timeCompatible, boolean acceptableDeviation) {
        if (!routePlanOpen) return "Route plan is not OPEN";
        if (!enoughCapacity) return "Route plan capacity is not enough";
        if (!timeCompatible) return "Time compatibility failed";
        if (!acceptableDeviation) return "Route deviation is too high (>" + MAX_TOTAL_DEVIATION_KM + " km)";
        return "Route plan is suitable";
    }

    private void notifyMatchCreated(LoadMatch match) {
        notificationService.createNotification(
                match.getLoad().getCustomer().getId(),
                NotificationType.LOAD_MATCH_CREATED,
                "Yeni rota eşleşmesi",
                "Yükünüz için uygun bir araç bulundu.",
                "LOAD_MATCH",
                match.getId()
        );

        notificationService.createNotification(
                match.getRoutePlan().getDriver().getId(),
                NotificationType.LOAD_MATCH_CREATED,
                "Yeni uygun yük",
                "Rotanızın üzerinde yeni bir yük bulundu.",
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
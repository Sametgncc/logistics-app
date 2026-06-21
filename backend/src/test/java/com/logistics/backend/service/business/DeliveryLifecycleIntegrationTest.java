package com.logistics.backend.service.business;

import com.logistics.backend.entity.business.Load;
import com.logistics.backend.entity.business.LoadMatch;
import com.logistics.backend.entity.business.RoutePlan;
import com.logistics.backend.entity.business.TransportOffer;
import com.logistics.backend.entity.business.Vehicle;
import com.logistics.backend.entity.enums.LoadStatus;
import com.logistics.backend.entity.enums.MatchStatus;
import com.logistics.backend.entity.enums.OfferDirection;
import com.logistics.backend.entity.enums.OfferStatus;
import com.logistics.backend.entity.enums.RoutePlanStatus;
import com.logistics.backend.entity.enums.VehicleType;
import com.logistics.backend.entity.user.User;
import com.logistics.backend.exception.BadRequestException;
import com.logistics.backend.repository.business.LoadMatchRepository;
import com.logistics.backend.repository.business.DriverLocationRepository;
import com.logistics.backend.repository.business.LoadRepository;
import com.logistics.backend.repository.business.RoutePlanRepository;
import com.logistics.backend.repository.business.TransportOfferRepository;
import com.logistics.backend.repository.business.VehicleRepository;
import com.logistics.backend.repository.user.NotificationRepository;
import com.logistics.backend.repository.user.UserRepository;
import com.logistics.backend.payload.request.UpdateDriverLocationRequest;
import com.logistics.backend.payload.response.DriverLocationResponse;
import com.logistics.backend.payload.response.LoadMatchResponse;
import com.logistics.backend.payload.response.LoadTrackingResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
class DeliveryLifecycleIntegrationTest {

    @Autowired
    private LoadMatchingService loadMatchingService;

    @Autowired
    private TransportOfferService transportOfferService;

    @Autowired
    private DriverTrackingService driverTrackingService;

    @Autowired
    private LoadMatchRepository loadMatchRepository;

    @Autowired
    private DriverLocationRepository driverLocationRepository;

    @Autowired
    private TransportOfferRepository transportOfferRepository;

    @Autowired
    private LoadRepository loadRepository;

    @Autowired
    private RoutePlanRepository routePlanRepository;

    @Autowired
    private VehicleRepository vehicleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NotificationRepository notificationRepository;

    @AfterEach
    void cleanUp() {
        notificationRepository.deleteAll();
        transportOfferRepository.deleteAll();
        driverLocationRepository.deleteAll();
        loadMatchRepository.deleteAll();
        routePlanRepository.deleteAll();
        vehicleRepository.deleteAll();
        loadRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void driverCanStartAndCompleteOwnAcceptedDelivery() {
        DeliveryFixture fixture = createFixture();

        LoadMatchResponse started = loadMatchingService.startTransit(
                fixture.match().getId(), fixture.driver().getId());

        assertEquals(LoadStatus.IN_TRANSIT, started.getLoadStatus());
        assertEquals(RoutePlanStatus.IN_PROGRESS, started.getRoutePlanStatus());
        assertEquals(LoadStatus.IN_TRANSIT, loadRepository.findById(fixture.load().getId()).orElseThrow().getStatus());

        LoadMatchResponse completed = loadMatchingService.completeDelivery(
                fixture.match().getId(), fixture.driver().getId());

        assertEquals(MatchStatus.COMPLETED, completed.getStatus());
        assertEquals(LoadStatus.DELIVERED, completed.getLoadStatus());
        assertEquals(RoutePlanStatus.COMPLETED, completed.getRoutePlanStatus());
        assertEquals(RoutePlanStatus.COMPLETED,
                routePlanRepository.findById(fixture.routePlan().getId()).orElseThrow().getStatus());
    }

    @Test
    void anotherDriverCannotUpdateDelivery() {
        DeliveryFixture fixture = createFixture();
        User anotherDriver = userRepository.save(user("another-driver@example.com"));

        assertThrows(BadRequestException.class, () ->
                loadMatchingService.startTransit(fixture.match().getId(), anotherDriver.getId()));
    }

    @Test
    void acceptingOfferAcceptsExistingMatchAndHidesItFromMatchingLists() {
        User customer = userRepository.save(user("offer-customer@example.com"));
        User driver = userRepository.save(user("offer-driver@example.com"));
        Vehicle vehicle = vehicleRepository.save(vehicle(driver, "34-OFFER-01"));
        LocalDateTime departure = LocalDateTime.now().plusDays(1);

        RoutePlan routePlan = routePlanRepository.save(routePlan(driver, vehicle, departure));
        Load load = loadRepository.save(load(customer, departure));
        LoadMatch match = loadMatchRepository.save(LoadMatch.builder()
                .load(load)
                .routePlan(routePlan)
                .status(MatchStatus.PENDING)
                .matchScore(BigDecimal.valueOf(90))
                .createdAt(LocalDateTime.now())
                .build());
        TransportOffer offer = transportOfferRepository.save(TransportOffer.builder()
                .load(load)
                .routePlan(routePlan)
                .driver(driver)
                .customer(customer)
                .offeredBy(driver)
                .direction(OfferDirection.DRIVER_TO_CUSTOMER)
                .price(BigDecimal.valueOf(1_000))
                .status(OfferStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build());

        transportOfferService.acceptOffer(offer.getId());

        assertEquals(MatchStatus.ACCEPTED,
                loadMatchRepository.findById(match.getId()).orElseThrow().getStatus());
        assertEquals(match.getId(), loadMatchingService.getAcceptedMatchForLoad(load.getId(), driver.getId()).getId());
        assertTrue(loadMatchingService.getMatchesByLoadId(load.getId()).isEmpty());
        assertTrue(loadMatchingService.getMatchesByRoutePlanId(routePlan.getId()).isEmpty());
    }

    @Test
    void customerCanReadLatestLocationOnlyWhileOwnLoadIsInTransit() {
        DeliveryFixture fixture = createFixture();
        loadMatchingService.startTransit(fixture.match().getId(), fixture.driver().getId());

        DriverLocationResponse savedLocation = driverTrackingService.recordLocation(
                fixture.routePlan().getId(),
                fixture.driver().getId(),
                UpdateDriverLocationRequest.builder()
                        .latitude(BigDecimal.valueOf(40.9876543))
                        .longitude(BigDecimal.valueOf(29.1234567))
                        .accuracyMeters(BigDecimal.valueOf(8))
                        .speedKph(BigDecimal.valueOf(65))
                        .headingDegrees(BigDecimal.valueOf(135))
                        .capturedAt(Instant.now())
                        .build()
        );

        LoadTrackingResponse tracking = driverTrackingService.getLatestLocationForLoad(
                fixture.load().getId(),
                fixture.load().getCustomer().getId()
        );

        assertEquals(fixture.routePlan().getId(), savedLocation.getRoutePlanId());
        assertEquals(fixture.load().getId(), tracking.getLoadId());
        assertEquals(BigDecimal.valueOf(40.9876543), tracking.getLatitude());
        assertEquals(BigDecimal.valueOf(29.1234567), tracking.getLongitude());
        assertEquals(0, BigDecimal.valueOf(65).compareTo(tracking.getSpeedKph()));

        User anotherCustomer = userRepository.save(user("tracking-outsider@example.com"));
        assertThrows(AccessDeniedException.class, () -> driverTrackingService.getLatestLocationForLoad(
                fixture.load().getId(), anotherCustomer.getId()));
    }

    private DeliveryFixture createFixture() {
        User customer = userRepository.save(user("customer@example.com"));
        User driver = userRepository.save(user("driver@example.com"));

        Vehicle vehicle = vehicleRepository.save(vehicle(driver, "34-TEST-01"));

        LocalDateTime now = LocalDateTime.now().plusDays(1);
        RoutePlan routePlan = routePlanRepository.save(routePlan(driver, vehicle, now));

        Load load = loadRepository.save(load(customer, now));
        load.setStatus(LoadStatus.MATCHED);
        loadRepository.save(load);

        LoadMatch match = loadMatchRepository.save(LoadMatch.builder()
                .load(load)
                .routePlan(routePlan)
                .status(MatchStatus.ACCEPTED)
                .matchScore(BigDecimal.valueOf(90))
                .createdAt(LocalDateTime.now())
                .build());

        return new DeliveryFixture(driver, routePlan, load, match);
    }

    private Vehicle vehicle(User driver, String plateNumber) {
        return Vehicle.builder()
                .owner(driver)
                .plateNumber(plateNumber)
                .vehicleType(VehicleType.TRUCK)
                .capacityKg(BigDecimal.valueOf(1_000))
                .capacityVolumeM3(BigDecimal.valueOf(100))
                .active(true)
                .build();
    }

    private RoutePlan routePlan(User driver, Vehicle vehicle, LocalDateTime departure) {
        return RoutePlan.builder()
                .driver(driver)
                .vehicle(vehicle)
                .originCity("Istanbul")
                .originLat(BigDecimal.valueOf(41.0082))
                .originLng(BigDecimal.valueOf(28.9784))
                .destinationCity("Ankara")
                .destinationLat(BigDecimal.valueOf(39.9334))
                .destinationLng(BigDecimal.valueOf(32.8597))
                .departureTime(departure)
                .estimatedArrivalTime(departure.plusHours(6))
                .availableCapacityKg(BigDecimal.valueOf(500))
                .availableCapacityVolumeM3(BigDecimal.valueOf(50))
                .status(RoutePlanStatus.OPEN)
                .build();
    }

    private Load load(User customer, LocalDateTime pickupTime) {
        return Load.builder()
                .customer(customer)
                .pickupCity("Istanbul")
                .pickupLat(BigDecimal.valueOf(41.0082))
                .pickupLng(BigDecimal.valueOf(28.9784))
                .deliveryCity("Ankara")
                .deliveryLat(BigDecimal.valueOf(39.9334))
                .deliveryLng(BigDecimal.valueOf(32.8597))
                .weightKg(BigDecimal.valueOf(100))
                .volumeM3(BigDecimal.valueOf(10))
                .pickupTime(pickupTime)
                .status(LoadStatus.OPEN)
                .createdAt(LocalDateTime.now())
                .build();
    }

    private User user(String email) {
        return User.builder()
                .name("Test")
                .lastName("User")
                .email(email)
                .enabled(true)
                .build();
    }

    private record DeliveryFixture(User driver, RoutePlan routePlan, Load load, LoadMatch match) {
    }
}

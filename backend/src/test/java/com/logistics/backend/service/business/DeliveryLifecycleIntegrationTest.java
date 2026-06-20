package com.logistics.backend.service.business;

import com.logistics.backend.entity.business.Load;
import com.logistics.backend.entity.business.LoadMatch;
import com.logistics.backend.entity.business.RoutePlan;
import com.logistics.backend.entity.business.Vehicle;
import com.logistics.backend.entity.enums.LoadStatus;
import com.logistics.backend.entity.enums.MatchStatus;
import com.logistics.backend.entity.enums.RoutePlanStatus;
import com.logistics.backend.entity.enums.VehicleType;
import com.logistics.backend.entity.user.User;
import com.logistics.backend.exception.BadRequestException;
import com.logistics.backend.repository.business.LoadMatchRepository;
import com.logistics.backend.repository.business.LoadRepository;
import com.logistics.backend.repository.business.RoutePlanRepository;
import com.logistics.backend.repository.business.VehicleRepository;
import com.logistics.backend.repository.user.UserRepository;
import com.logistics.backend.payload.response.LoadMatchResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ActiveProfiles("test")
class DeliveryLifecycleIntegrationTest {

    @Autowired
    private LoadMatchingService loadMatchingService;

    @Autowired
    private LoadMatchRepository loadMatchRepository;

    @Autowired
    private LoadRepository loadRepository;

    @Autowired
    private RoutePlanRepository routePlanRepository;

    @Autowired
    private VehicleRepository vehicleRepository;

    @Autowired
    private UserRepository userRepository;

    @AfterEach
    void cleanUp() {
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

    private DeliveryFixture createFixture() {
        User customer = userRepository.save(user("customer@example.com"));
        User driver = userRepository.save(user("driver@example.com"));

        Vehicle vehicle = vehicleRepository.save(Vehicle.builder()
                .owner(driver)
                .plateNumber("34-TEST-01")
                .vehicleType(VehicleType.TRUCK)
                .capacityKg(BigDecimal.valueOf(1_000))
                .capacityVolumeM3(BigDecimal.valueOf(100))
                .active(true)
                .build());

        LocalDateTime now = LocalDateTime.now().plusDays(1);
        RoutePlan routePlan = routePlanRepository.save(RoutePlan.builder()
                .driver(driver)
                .vehicle(vehicle)
                .originCity("Istanbul")
                .originLat(BigDecimal.valueOf(41.0082))
                .originLng(BigDecimal.valueOf(28.9784))
                .destinationCity("Ankara")
                .destinationLat(BigDecimal.valueOf(39.9334))
                .destinationLng(BigDecimal.valueOf(32.8597))
                .departureTime(now)
                .estimatedArrivalTime(now.plusHours(6))
                .availableCapacityKg(BigDecimal.valueOf(500))
                .availableCapacityVolumeM3(BigDecimal.valueOf(50))
                .status(RoutePlanStatus.OPEN)
                .build());

        Load load = loadRepository.save(Load.builder()
                .customer(customer)
                .pickupCity("Istanbul")
                .pickupLat(BigDecimal.valueOf(41.0082))
                .pickupLng(BigDecimal.valueOf(28.9784))
                .deliveryCity("Ankara")
                .deliveryLat(BigDecimal.valueOf(39.9334))
                .deliveryLng(BigDecimal.valueOf(32.8597))
                .weightKg(BigDecimal.valueOf(100))
                .volumeM3(BigDecimal.valueOf(10))
                .pickupTime(now)
                .status(LoadStatus.MATCHED)
                .createdAt(LocalDateTime.now())
                .build());

        LoadMatch match = loadMatchRepository.save(LoadMatch.builder()
                .load(load)
                .routePlan(routePlan)
                .status(MatchStatus.ACCEPTED)
                .matchScore(BigDecimal.valueOf(90))
                .createdAt(LocalDateTime.now())
                .build());

        return new DeliveryFixture(driver, routePlan, load, match);
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

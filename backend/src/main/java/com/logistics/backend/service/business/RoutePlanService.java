package com.logistics.backend.service.business;

import com.logistics.backend.entity.business.RoutePlan;
import com.logistics.backend.entity.business.Vehicle;
import com.logistics.backend.entity.enums.Role;
import com.logistics.backend.entity.enums.RoutePlanStatus;
import com.logistics.backend.entity.user.User;
import com.logistics.backend.exception.BadRequestException;
import com.logistics.backend.exception.ResourceNotFoundException;
import com.logistics.backend.mapper.RoutePlanMapper;
import com.logistics.backend.payload.request.CreateRoutePlanRequest;
import com.logistics.backend.payload.request.UpdateRoutePlanRequest;
import com.logistics.backend.payload.response.RoutePlanResponse;
import com.logistics.backend.repository.business.RoutePlanRepository;
import com.logistics.backend.repository.business.VehicleRepository;
import com.logistics.backend.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RoutePlanService {

    private final RoutePlanRepository routePlanRepository;
    private final UserRepository userRepository;
    private final VehicleRepository vehicleRepository;
    private final RoutePlanMapper routePlanMapper;

    public RoutePlanResponse createRoutePlan(CreateRoutePlanRequest request) {
        User driver = userRepository.findById(request.getDriverId())
                .orElseThrow(() -> new ResourceNotFoundException("Driver not found with id: " + request.getDriverId()));

        Vehicle vehicle = vehicleRepository.findById(request.getVehicleId())
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found with id: " + request.getVehicleId()));

        validateDriver(driver);
        validateVehicleOwnership(driver, vehicle);
        validateVehicleIsActive(vehicle);
        validateTimes(request.getDepartureTime(), request.getEstimatedArrivalTime());
        validateCapacities(request.getAvailableCapacityKg(), request.getAvailableCapacityVolumeM3());

        RoutePlan routePlan = RoutePlan.builder()
                .driver(driver)
                .vehicle(vehicle)
                .originCity(request.getOriginCity())
                .originLat(request.getOriginLat())
                .originLng(request.getOriginLng())
                .destinationCity(request.getDestinationCity())
                .destinationLat(request.getDestinationLat())
                .destinationLng(request.getDestinationLng())
                .departureTime(request.getDepartureTime())
                .estimatedArrivalTime(request.getEstimatedArrivalTime())
                .availableCapacityKg(request.getAvailableCapacityKg())
                .availableCapacityVolumeM3(request.getAvailableCapacityVolumeM3())
                .status(RoutePlanStatus.OPEN)
                .notes(request.getNotes())
                .build();

        RoutePlan savedRoutePlan = routePlanRepository.save(routePlan);
        return routePlanMapper.toResponse(savedRoutePlan);
    }

    public List<RoutePlanResponse> getMyRoutePlans(Long driverId) {
        userRepository.findById(driverId)
                .orElseThrow(() -> new ResourceNotFoundException("Driver not found with id: " + driverId));

        return routePlanRepository.findByDriverId(driverId)
                .stream()
                .map(routePlanMapper::toResponse)
                .toList();
    }

    public RoutePlanResponse getRoutePlanById(Long id) {
        RoutePlan routePlan = routePlanRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Route plan not found with id: " + id));

        return routePlanMapper.toResponse(routePlan);
    }

    public List<RoutePlanResponse> getOpenRoutePlans() {
        return routePlanRepository.findByStatus(RoutePlanStatus.OPEN)
                .stream()
                .map(routePlanMapper::toResponse)
                .toList();
    }

    public RoutePlanResponse updateRoutePlan(Long id, UpdateRoutePlanRequest request) {
        RoutePlan routePlan = routePlanRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Route plan not found with id: " + id));

        if (routePlan.getStatus() == RoutePlanStatus.CANCELLED ||
                routePlan.getStatus() == RoutePlanStatus.COMPLETED) {
            throw new BadRequestException("Cancelled or completed route plans cannot be updated");
        }

        validateTimes(request.getDepartureTime(), request.getEstimatedArrivalTime());
        validateCapacities(request.getAvailableCapacityKg(), request.getAvailableCapacityVolumeM3());

        routePlan.setOriginCity(request.getOriginCity());
        routePlan.setOriginLat(request.getOriginLat());
        routePlan.setOriginLng(request.getOriginLng());
        routePlan.setDestinationCity(request.getDestinationCity());
        routePlan.setDestinationLat(request.getDestinationLat());
        routePlan.setDestinationLng(request.getDestinationLng());
        routePlan.setDepartureTime(request.getDepartureTime());
        routePlan.setEstimatedArrivalTime(request.getEstimatedArrivalTime());
        routePlan.setAvailableCapacityKg(request.getAvailableCapacityKg());
        routePlan.setAvailableCapacityVolumeM3(request.getAvailableCapacityVolumeM3());
        routePlan.setNotes(request.getNotes());

        RoutePlan updatedRoutePlan = routePlanRepository.save(routePlan);
        return routePlanMapper.toResponse(updatedRoutePlan);
    }

    public RoutePlanResponse cancelRoutePlan(Long id) {
        RoutePlan routePlan = routePlanRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Route plan not found with id: " + id));

        if (routePlan.getStatus() == RoutePlanStatus.COMPLETED) {
            throw new BadRequestException("Completed route plan cannot be cancelled");
        }

        if (routePlan.getStatus() == RoutePlanStatus.CANCELLED) {
            throw new BadRequestException("Route plan is already cancelled");
        }

        routePlan.setStatus(RoutePlanStatus.CANCELLED);

        RoutePlan cancelledRoutePlan = routePlanRepository.save(routePlan);
        return routePlanMapper.toResponse(cancelledRoutePlan);
    }

    private void validateDriver(User driver) {
        if (driver.getRole() == null || driver.getRole().getRole() == null) {
            throw new BadRequestException("User role information is missing");
        }

        if (driver.getRole().getRole() != Role.DRIVER) {
            throw new BadRequestException("Only DRIVER users can create route plans");
        }

        if (Boolean.FALSE.equals(driver.getEnabled())) {
            throw new BadRequestException("Driver account is disabled");
        }

        if (Boolean.FALSE.equals(driver.getDriverApproved())) {
            throw new BadRequestException("Driver is not approved yet");
        }
    }

    private void validateVehicleOwnership(User driver, Vehicle vehicle) {
        if (vehicle.getOwner() == null || vehicle.getOwner().getId() == null) {
            throw new BadRequestException("Vehicle owner information is missing");
        }

        if (!vehicle.getOwner().getId().equals(driver.getId())) {
            throw new BadRequestException("Vehicle does not belong to this driver");
        }
    }

    private void validateVehicleIsActive(Vehicle vehicle) {
        if (Boolean.FALSE.equals(vehicle.getActive())) {
            throw new BadRequestException("Vehicle is not active");
        }
    }

    private void validateTimes(LocalDateTime departureTime, LocalDateTime estimatedArrivalTime) {
        if (departureTime == null) {
            throw new BadRequestException("Departure time cannot be null");
        }

        if (departureTime.isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Departure time cannot be in the past");
        }

        if (estimatedArrivalTime != null && !estimatedArrivalTime.isAfter(departureTime)) {
            throw new BadRequestException("Estimated arrival time must be after departure time");
        }
    }

    private void validateCapacities(BigDecimal availableCapacityKg, BigDecimal availableCapacityVolumeM3) {
        if (availableCapacityKg == null || availableCapacityKg.signum() <= 0) {
            throw new BadRequestException("Capacity kg must be greater than zero");
        }

        if (availableCapacityVolumeM3 == null || availableCapacityVolumeM3.signum() <= 0) {
            throw new BadRequestException("Capacity volume must be greater than zero");
        }
    }
}
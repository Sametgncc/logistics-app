package com.logistics.backend.service.business;

import com.logistics.backend.entity.business.Vehicle;
import com.logistics.backend.entity.enums.Role;
import com.logistics.backend.entity.user.User;
import com.logistics.backend.exception.BadRequestException;
import com.logistics.backend.exception.ResourceNotFoundException;
import com.logistics.backend.mapper.VehicleMapper;
import com.logistics.backend.payload.request.CreateVehicleRequest;
import com.logistics.backend.payload.request.UpdateVehicleRequest;
import com.logistics.backend.payload.response.VehicleResponse;
import com.logistics.backend.repository.business.VehicleRepository;
import com.logistics.backend.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class VehicleService {

    private final VehicleRepository vehicleRepository;
    private final UserRepository userRepository;
    private final VehicleMapper vehicleMapper;

    public VehicleResponse createVehicle(CreateVehicleRequest request) {
        User owner = userRepository.findById(request.getOwnerId())
                .orElseThrow(() -> new ResourceNotFoundException("Owner not found with id: " + request.getOwnerId()));

        validateOwnerIsDriver(owner);
        validatePlateNumberIsUnique(request.getPlateNumber());

        Vehicle vehicle = Vehicle.builder()
                .owner(owner)
                .plateNumber(normalizePlateNumber(request.getPlateNumber()))
                .vehicleType(request.getVehicleType())
                .capacityKg(request.getCapacityKg())
                .capacityVolumeM3(request.getCapacityVolumeM3())
                .active(true)
                .build();

        return vehicleMapper.toResponse(vehicleRepository.save(vehicle));
    }

    public List<VehicleResponse> getVehiclesByOwnerId(Long ownerId) {
        userRepository.findById(ownerId)
                .orElseThrow(() -> new ResourceNotFoundException("Owner not found with id: " + ownerId));

        return vehicleRepository.findByOwnerId(ownerId)
                .stream()
                .map(vehicleMapper::toResponse)
                .toList();
    }

    public VehicleResponse getVehicleById(Long id) {
        Vehicle vehicle = findVehicleById(id);
        return vehicleMapper.toResponse(vehicle);
    }

    public List<VehicleResponse> getAllVehicles() {
        return vehicleRepository.findAll()
                .stream()
                .map(vehicleMapper::toResponse)
                .toList();
    }

    public VehicleResponse updateVehicle(Long id, UpdateVehicleRequest request) {
        Vehicle vehicle = findVehicleById(id);

        String normalizedPlate = normalizePlateNumber(request.getPlateNumber());

        if (!vehicle.getPlateNumber().equalsIgnoreCase(normalizedPlate)) {
            validatePlateNumberIsUnique(normalizedPlate);
        }

        vehicle.setPlateNumber(normalizedPlate);
        vehicle.setVehicleType(request.getVehicleType());
        vehicle.setCapacityKg(request.getCapacityKg());
        vehicle.setCapacityVolumeM3(request.getCapacityVolumeM3());
        vehicle.setActive(request.getActive());

        return vehicleMapper.toResponse(vehicleRepository.save(vehicle));
    }

    public VehicleResponse deactivateVehicle(Long id) {
        Vehicle vehicle = findVehicleById(id);

        if (Boolean.FALSE.equals(vehicle.getActive())) {
            throw new BadRequestException("Vehicle is already inactive");
        }

        vehicle.setActive(false);

        return vehicleMapper.toResponse(vehicleRepository.save(vehicle));
    }

    public VehicleResponse activateVehicle(Long id) {
        Vehicle vehicle = findVehicleById(id);

        if (Boolean.TRUE.equals(vehicle.getActive())) {
            throw new BadRequestException("Vehicle is already active");
        }

        vehicle.setActive(true);

        return vehicleMapper.toResponse(vehicleRepository.save(vehicle));
    }

    private Vehicle findVehicleById(Long id) {
        return vehicleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found with id: " + id));
    }

    private void validateOwnerIsDriver(User owner) {
        if (owner.getRole() == null || owner.getRole().getRole() == null) {
            throw new BadRequestException("Owner role information is missing");
        }

        if (Boolean.FALSE.equals(owner.getDriverApproved())) {
            throw new BadRequestException("Driver is not approved to add a vehicle");
        }

        if (Boolean.FALSE.equals(owner.getEnabled())) {
            throw new BadRequestException("Driver account is disabled");
        }
    }

    private void validatePlateNumberIsUnique(String plateNumber) {
        String normalizedPlate = normalizePlateNumber(plateNumber);

        if (vehicleRepository.existsByPlateNumberIgnoreCase(normalizedPlate)) {
            throw new BadRequestException("Vehicle with plate number already exists: " + normalizedPlate);
        }
    }

    private String normalizePlateNumber(String plateNumber) {
        if (plateNumber == null) {
            throw new BadRequestException("Plate number cannot be null");
        }

        return plateNumber
                .trim()
                .replaceAll("\\s+", "")
                .toUpperCase();
    }
}
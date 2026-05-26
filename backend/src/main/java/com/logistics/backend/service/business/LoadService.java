package com.logistics.backend.service.business;

import com.logistics.backend.entity.business.Load;
import com.logistics.backend.entity.enums.LoadStatus;
import com.logistics.backend.entity.enums.Role;
import com.logistics.backend.entity.user.User;
import com.logistics.backend.exception.BadRequestException;
import com.logistics.backend.exception.ResourceNotFoundException;
import com.logistics.backend.mapper.LoadMapper;
import com.logistics.backend.payload.request.CreateLoadRequest;
import com.logistics.backend.payload.request.UpdateLoadRequest;
import com.logistics.backend.payload.response.LoadResponse;
import com.logistics.backend.repository.business.LoadRepository;
import com.logistics.backend.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LoadService {

    private final LoadRepository loadRepository;
    private final UserRepository userRepository;
    private final LoadMapper loadMapper;

    private final LoadMatchingService loadMatchingService;

    public LoadResponse createLoad(CreateLoadRequest request) {
        User customer = userRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + request.getCustomerId()));

        validateCustomer(customer);
        validateCities(request.getPickupCity(), request.getDeliveryCity());
        validateCoordinates(request.getPickupLat(), request.getPickupLng(), request.getDeliveryLat(), request.getDeliveryLng());
        validateCapacity(request.getWeightKg(), request.getVolumeM3());
        validateTimes(request.getPickupTime(), request.getDeliveryDeadline());

        Load load = Load.builder()
                .customer(customer)
                .pickupCity(request.getPickupCity())
                .pickupLat(request.getPickupLat())
                .pickupLng(request.getPickupLng())
                .deliveryCity(request.getDeliveryCity())
                .deliveryLat(request.getDeliveryLat())
                .deliveryLng(request.getDeliveryLng())
                .weightKg(request.getWeightKg())
                .volumeM3(request.getVolumeM3())
                .pickupTime(request.getPickupTime())
                .deliveryDeadline(request.getDeliveryDeadline())
                .description(request.getDescription())
                .status(LoadStatus.OPEN)
                .createdAt(LocalDateTime.now())
                .build();

        Load savedLoad = loadRepository.save(load);
        loadMatchingService.findSuitableRoutePlansForLoad(savedLoad.getId());

        return loadMapper.toResponse(savedLoad);
    }

    public List<LoadResponse> getMyLoads(Long customerId) {
        userRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + customerId));

        return loadRepository.findByCustomerId(customerId)
                .stream()
                .map(loadMapper::toResponse)
                .toList();
    }

    public LoadResponse getLoadById(Long id) {
        Load load = loadRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Load not found with id: " + id));

        return loadMapper.toResponse(load);
    }

    public List<LoadResponse> getOpenLoads() {
        return loadRepository.findByStatus(LoadStatus.OPEN)
                .stream()
                .map(loadMapper::toResponse)
                .toList();
    }

    public LoadResponse updateLoad(Long id, UpdateLoadRequest request) {
        Load load = loadRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Load not found with id: " + id));

        if (load.getStatus() != LoadStatus.OPEN) {
            throw new BadRequestException("Only OPEN loads can be updated");
        }

        validateCities(request.getPickupCity(), request.getDeliveryCity());
        validateCoordinates(request.getPickupLat(), request.getPickupLng(), request.getDeliveryLat(), request.getDeliveryLng());
        validateCapacity(request.getWeightKg(), request.getVolumeM3());
        validateTimes(request.getPickupTime(), request.getDeliveryDeadline());

        load.setPickupCity(request.getPickupCity());
        load.setPickupLat(request.getPickupLat());
        load.setPickupLng(request.getPickupLng());
        load.setDeliveryCity(request.getDeliveryCity());
        load.setDeliveryLat(request.getDeliveryLat());
        load.setDeliveryLng(request.getDeliveryLng());
        load.setWeightKg(request.getWeightKg());
        load.setVolumeM3(request.getVolumeM3());
        load.setPickupTime(request.getPickupTime());
        load.setDeliveryDeadline(request.getDeliveryDeadline());
        load.setDescription(request.getDescription());

        return loadMapper.toResponse(loadRepository.save(load));
    }

    public LoadResponse cancelLoad(Long id) {
        Load load = loadRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Load not found with id: " + id));

        if (load.getStatus() == LoadStatus.DELIVERED) {
            throw new BadRequestException("Delivered load cannot be cancelled");
        }

        if (load.getStatus() == LoadStatus.CANCELLED) {
            throw new BadRequestException("Load is already cancelled");
        }

        load.setStatus(LoadStatus.CANCELLED);

        return loadMapper.toResponse(loadRepository.save(load));
    }

    private void validateCustomer(User customer) {
        if (customer.getRole() == null || customer.getRole().getRole() == null) {
            throw new BadRequestException("User role information is missing");
        }

        if (customer.getRole().getRole() != Role.CUSTOMER) {
            throw new BadRequestException("Only CUSTOMER users can create load postings");
        }

        if (Boolean.FALSE.equals(customer.getEnabled())) {
            throw new BadRequestException("Customer account is disabled");
        }
    }

    private void validateCities(String pickupCity, String deliveryCity) {
        if (pickupCity == null || pickupCity.isBlank()) {
            throw new BadRequestException("Pickup city cannot be blank");
        }

        if (deliveryCity == null || deliveryCity.isBlank()) {
            throw new BadRequestException("Delivery city cannot be blank");
        }

        if (pickupCity.equalsIgnoreCase(deliveryCity)) {
            throw new BadRequestException("Pickup city and delivery city cannot be the same");
        }
    }

    private void validateCoordinates(
            BigDecimal pickupLat,
            BigDecimal pickupLng,
            BigDecimal deliveryLat,
            BigDecimal deliveryLng
    ) {
        validateLatitude(pickupLat, "Pickup latitude");
        validateLongitude(pickupLng, "Pickup longitude");
        validateLatitude(deliveryLat, "Delivery latitude");
        validateLongitude(deliveryLng, "Delivery longitude");
    }

    private void validateLatitude(BigDecimal latitude, String fieldName) {
        if (latitude == null) {
            throw new BadRequestException(fieldName + " cannot be null");
        }

        if (latitude.compareTo(BigDecimal.valueOf(-90)) < 0 ||
                latitude.compareTo(BigDecimal.valueOf(90)) > 0) {
            throw new BadRequestException(fieldName + " must be between -90 and 90");
        }
    }

    private void validateLongitude(BigDecimal longitude, String fieldName) {
        if (longitude == null) {
            throw new BadRequestException(fieldName + " cannot be null");
        }

        if (longitude.compareTo(BigDecimal.valueOf(-180)) < 0 ||
                longitude.compareTo(BigDecimal.valueOf(180)) > 0) {
            throw new BadRequestException(fieldName + " must be between -180 and 180");
        }
    }

    private void validateCapacity(BigDecimal weightKg, BigDecimal volumeM3) {
        if (weightKg == null || weightKg.signum() <= 0) {
            throw new BadRequestException("Weight must be greater than zero");
        }

        if (volumeM3 == null || volumeM3.signum() <= 0) {
            throw new BadRequestException("Volume must be greater than zero");
        }
    }

    private void validateTimes(LocalDateTime pickupTime, LocalDateTime deliveryDeadline) {
        if (pickupTime == null) {
            throw new BadRequestException("Pickup time cannot be null");
        }

        if (pickupTime.isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Pickup time cannot be in the past");
        }

        if (deliveryDeadline != null && !deliveryDeadline.isAfter(pickupTime)) {
            throw new BadRequestException("Delivery deadline must be after pickup time");
        }
    }
}
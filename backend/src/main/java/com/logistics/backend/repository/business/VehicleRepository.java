package com.logistics.backend.repository.business;

import com.logistics.backend.entity.business.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VehicleRepository extends JpaRepository<Vehicle, Long> {

    List<Vehicle> findByOwnerId(Long ownerId);

    boolean existsByPlateNumberIgnoreCase(String plateNumber);
}
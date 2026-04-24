package com.logistics.backend.repository.business;

import com.logistics.backend.entity.business.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VehicleRepository extends JpaRepository<Vehicle, Long> {
}
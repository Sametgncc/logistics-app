package com.logistics.backend.repository.business;

import com.logistics.backend.entity.business.DriverLocation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DriverLocationRepository extends JpaRepository<DriverLocation, Long> {

    Optional<DriverLocation> findTopByRoutePlanIdOrderByCapturedAtDesc(Long routePlanId);
}

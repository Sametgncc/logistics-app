package com.logistics.backend.repository.business;

import com.logistics.backend.entity.business.RoutePlan;
import com.logistics.backend.entity.enums.RoutePlanStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RoutePlanRepository extends JpaRepository<RoutePlan, Long> {

    List<RoutePlan> findByDriverId(Long driverId);

    List<RoutePlan> findByStatus(RoutePlanStatus status);

    long countByStatus(RoutePlanStatus status);

    List<RoutePlan> findByDriverIdAndStatus(Long driverId, RoutePlanStatus status);
}

package com.logistics.backend.repository.business;

import com.logistics.backend.entity.business.Load;
import com.logistics.backend.entity.enums.LoadStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LoadRepository extends JpaRepository<Load, Long> {

    List<Load> findByCustomerId(Long customerId);

    List<Load> findByStatus(LoadStatus status);

    long countByStatus(LoadStatus status);

    List<Load> findByPickupCityIgnoreCaseAndDeliveryCityIgnoreCase(
            String pickupCity,
            String deliveryCity
    );
}

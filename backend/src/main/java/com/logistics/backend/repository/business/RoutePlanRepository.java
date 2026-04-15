package com.logistics.backend.repository.business;

import com.logistics.backend.entity.business.RoutePlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoutePlanRepository extends JpaRepository<RoutePlan, Long> {


}

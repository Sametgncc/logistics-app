package com.logistics.backend.service.business;

import com.logistics.backend.entity.business.RoutePlan;
import com.logistics.backend.mapper.RoutePlanMapper;
import com.logistics.backend.repository.business.RoutePlanRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class RoutePlanService {

    private final RoutePlanRepository routePlanRepository;
    private final RoutePlanMapper routePlanMapper;

    @Autowired
    public RoutePlanService(RoutePlanRepository routePlanRepository, RoutePlanMapper routePlanMapper) {
        this.routePlanRepository = routePlanRepository;
        this.routePlanMapper = routePlanMapper;
    }

    public List<RoutePlan> getAllRoutePlans() {
        return routePlanRepository.findAll();
    }

    public Optional<RoutePlan> getRoutePlanById(Long id) {
        return routePlanRepository.findById(id);
    }

    public RoutePlan createRoutePlan(RoutePlan routePlan) {
        return routePlanRepository.save(routePlan);
    }

    public RoutePlan updateRoutePlan(Long id, RoutePlan updatedRoutePlan) {
        return routePlanRepository.findById(id).map(routePlan -> {
            routePlanMapper.updateRoutePlanFromDto(routePlan, updatedRoutePlan);
            return routePlanRepository.save(routePlan);
        }).orElseThrow(() -> new RuntimeException("RoutePlan not found"));
    }

    public void deleteRoutePlan(Long id) {
        routePlanRepository.deleteById(id);
    }
}

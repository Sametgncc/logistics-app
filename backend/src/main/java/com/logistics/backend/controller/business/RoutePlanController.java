package com.logistics.backend.controller.business;

import com.logistics.backend.entity.business.RoutePlan;
import com.logistics.backend.service.business.RoutePlanService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/route-plans")
public class RoutePlanController {

    private final RoutePlanService routePlanService;

    @Autowired
    public RoutePlanController(RoutePlanService routePlanService) {
        this.routePlanService = routePlanService;
    }

    @GetMapping
    public ResponseEntity<List<RoutePlan>> getAllRoutePlans() {
        return ResponseEntity.ok(routePlanService.getAllRoutePlans());
    }

    @GetMapping("/{id}")
    public ResponseEntity<RoutePlan> getRoutePlanById(@PathVariable Long id) {
        return routePlanService.getRoutePlanById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<RoutePlan> createRoutePlan(@RequestBody RoutePlan routePlan) {
        return ResponseEntity.ok(routePlanService.createRoutePlan(routePlan));
    }

    @PutMapping("/{id}")
    public ResponseEntity<RoutePlan> updateRoutePlan(@PathVariable Long id, @RequestBody RoutePlan routePlan) {
        return ResponseEntity.ok(routePlanService.updateRoutePlan(id, routePlan));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRoutePlan(@PathVariable Long id) {
        routePlanService.deleteRoutePlan(id);
        return ResponseEntity.noContent().build();
    }
}

package com.logistics.backend.controller.business;

import com.logistics.backend.payload.response.LoadMatchResponse;
import com.logistics.backend.payload.response.MatchDebugResponse;
import com.logistics.backend.service.business.LoadMatchingService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/load-matches")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class LoadMatchController {

    private final LoadMatchingService loadMatchingService;

    @PostMapping("/loads/{loadId}/find-route-plans")
    public List<LoadMatchResponse> findSuitableRoutePlansForLoad(@PathVariable Long loadId) {
        return loadMatchingService.findSuitableRoutePlansForLoad(loadId);
    }

    @PostMapping("/route-plans/{routePlanId}/find-loads")
    public List<LoadMatchResponse> findSuitableLoadsForRoutePlan(@PathVariable Long routePlanId) {
        return loadMatchingService.findSuitableLoadsForRoutePlan(routePlanId);
    }

    @GetMapping("/route-plans/{routePlanId}/suitable-loads/preview")
    public List<LoadMatchResponse> previewSuitableLoadsForRoutePlan(@PathVariable Long routePlanId) {
        return loadMatchingService.previewSuitableLoadsForRoutePlan(routePlanId);
    }

    @GetMapping("/loads/{loadId}")
    public List<LoadMatchResponse> getMatchesByLoadId(@PathVariable Long loadId) {
        return loadMatchingService.getMatchesByLoadId(loadId);
    }

    @GetMapping("/route-plans/{routePlanId}")
    public List<LoadMatchResponse> getMatchesByRoutePlanId(@PathVariable Long routePlanId) {
        return loadMatchingService.getMatchesByRoutePlanId(routePlanId);
    }

    @PatchMapping("/{matchId}/accept")
    public LoadMatchResponse acceptMatch(@PathVariable Long matchId) {
        return loadMatchingService.acceptMatch(matchId);
    }

    @PatchMapping("/{matchId}/reject")
    public LoadMatchResponse rejectMatch(@PathVariable Long matchId) {
        return loadMatchingService.rejectMatch(matchId);
    }

    @GetMapping("/loads/{loadId}/debug")
    public List<MatchDebugResponse> debugSuitableRoutePlansForLoad(@PathVariable Long loadId) {
        return loadMatchingService.debugSuitableRoutePlansForLoad(loadId);
    }
}

package com.logistics.backend.repository.business;

import com.logistics.backend.entity.business.LoadMatch;
import com.logistics.backend.entity.enums.MatchStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LoadMatchRepository extends JpaRepository<LoadMatch, Long> {

    List<LoadMatch> findByLoadId(Long loadId);

    List<LoadMatch> findByRoutePlanId(Long routePlanId);

    List<LoadMatch> findByStatus(MatchStatus status);

    boolean existsByLoadIdAndStatus(Long loadId, MatchStatus status);

    boolean existsByLoadIdAndRoutePlanId(Long loadId, Long routePlanId);

    Optional<LoadMatch> findByLoadIdAndRoutePlanId(Long loadId, Long routePlanId);
}

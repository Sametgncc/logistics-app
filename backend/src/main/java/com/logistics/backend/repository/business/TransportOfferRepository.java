package com.logistics.backend.repository.business;

import com.logistics.backend.entity.business.TransportOffer;
import com.logistics.backend.entity.enums.OfferStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TransportOfferRepository extends JpaRepository<TransportOffer, Long> {

    List<TransportOffer> findByLoadId(Long loadId);

    List<TransportOffer> findByRoutePlanId(Long routePlanId);

    List<TransportOffer> findByDriverId(Long driverId);

    List<TransportOffer> findByCustomerId(Long customerId);

    List<TransportOffer> findByStatus(OfferStatus status);

    long countByStatus(OfferStatus status);

    boolean existsByLoadIdAndRoutePlanIdAndStatus(
            Long loadId,
            Long routePlanId,
            OfferStatus status
    );

    Optional<TransportOffer> findByLoadIdAndRoutePlanIdAndStatus(
            Long loadId,
            Long routePlanId,
            OfferStatus status
    );

    // Müşteri için kabul edilmiş işlemleri getir
    List<TransportOffer> findByCustomerIdAndStatusOrderByRespondedAtDesc(Long customerId, OfferStatus status);

    // Şoför/Nakliyeci için kabul edilmiş işlemleri getir
    List<TransportOffer> findByDriverIdAndStatusOrderByRespondedAtDesc(Long driverId, OfferStatus status);
}

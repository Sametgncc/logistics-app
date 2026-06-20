package com.logistics.backend.repository.business;

import com.logistics.backend.entity.business.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByIyzicoToken(String iyzicoToken);
    Optional<Payment> findByTransportOfferId(Long transportOfferId);
}

package com.logistics.backend.repository.business;

import com.logistics.backend.entity.enums.DriverDocumentStatus;
import com.logistics.backend.entity.enums.DriverDocumentType;
import com.logistics.backend.entity.user.DriverDocument;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DriverDocumentRepository extends JpaRepository<DriverDocument, Long> {

    List<DriverDocument> findByDriverId(Long driverId);

    List<DriverDocument> findByStatus(DriverDocumentStatus status);

    Optional<DriverDocument> findByDriverIdAndDocumentType(
            Long driverId,
            DriverDocumentType documentType
    );

    boolean existsByDriverIdAndDocumentType(
            Long driverId,
            DriverDocumentType documentType
    );
}
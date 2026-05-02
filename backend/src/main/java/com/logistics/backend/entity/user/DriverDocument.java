package com.logistics.backend.entity.user;

import com.logistics.backend.entity.enums.DriverDocumentStatus;
import com.logistics.backend.entity.enums.DriverDocumentType;
import com.logistics.backend.entity.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "driver_documents")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DriverDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Belgeyi yükleyen sürücü kullanıcı
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "driver_id", nullable = false)
    private User driver;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private DriverDocumentType documentType;

    @Column(nullable = false)
    private String fileUrl;

    @Column(length = 255)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private DriverDocumentStatus status;

    @Column(length = 500)
    private String rejectionReason;

    @Column(nullable = false)
    private LocalDateTime uploadedAt;

    private LocalDateTime reviewedAt;
}
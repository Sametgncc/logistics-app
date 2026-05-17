package com.logistics.backend.service.business;

import com.logistics.backend.entity.enums.DriverDocumentStatus;
import com.logistics.backend.entity.enums.DriverDocumentType;
import com.logistics.backend.entity.enums.Role;
import com.logistics.backend.entity.user.User;
import com.logistics.backend.exception.BadRequestException;
import com.logistics.backend.exception.ResourceNotFoundException;
import com.logistics.backend.payload.response.UserResponse;
import com.logistics.backend.mapper.UserMapper;
import com.logistics.backend.repository.business.DriverDocumentRepository;
import com.logistics.backend.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DriverApprovalService {

    private final UserRepository userRepository;
    private final DriverDocumentRepository driverDocumentRepository;
    private final UserMapper userMapper;


    public UserResponse approveDriver(Long driverId) {
        User driver = findDriver(driverId);

        validateRequiredDocumentsApproved(driverId);

        driver.setDriverApproved(true);

        return userMapper.toUserResponse(userRepository.save(driver));
    }

    public UserResponse rejectDriver(Long driverId) {
        User driver = findDriver(driverId);

        driver.setDriverApproved(false);

        return userMapper.toUserResponse(userRepository.save(driver));
    }

    private User findDriver(Long driverId) {
        User driver = userRepository.findById(driverId)
                .orElseThrow(() -> new ResourceNotFoundException("Driver not found with id: " + driverId));

        if (driver.getRole() == null) {
            throw new BadRequestException("User haven't any role");
        }

        return driver;
    }

    private void validateRequiredDocumentsApproved(Long driverId) {
        boolean allRequiredApproved = Arrays.stream(getRequiredDocumentTypes())
                .allMatch(type -> driverDocumentRepository
                        .findByDriverIdAndDocumentType(driverId, type)
                        .filter(doc -> doc.getStatus() == DriverDocumentStatus.APPROVED)
                        .isPresent());

        if (!allRequiredApproved) {
            throw new BadRequestException("All required driver documents must be approved before approving driver");
        }
    }

    private DriverDocumentType[] getRequiredDocumentTypes() {
        return new DriverDocumentType[]{
                DriverDocumentType.DRIVER_LICENSE,
                DriverDocumentType.SRC_CERTIFICATE,
                DriverDocumentType.PSYCHOTECHNICAL_CERTIFICATE
        };
    }

    //Bekleyenleri listeler
    public List<UserResponse> getPendingDrivers() {
        // Tüm kullanıcıları veya sadece driverApproved = false olanları çekebilirsin.
        // Şimdilik hata almamak için tümünü çekip Java tarafında filtreliyoruz.
        return userRepository.findAll().stream()
                // 1. Şart: Henüz sürücü onayı almamış olanlar (false veya null olanlar)
                .filter(user -> user.getDriverApproved() == null || !user.getDriverApproved())
                // 2. Şart: 3 zorunlu belgesi de APPROVED statüsünde olanlar
                .filter(user -> hasAllDocumentsApproved(user.getId()))
                .map(userMapper::toUserResponse)
                .toList();
    }

    // Üstteki metodun belge kontrolünü yapması için yardımcı metot
    private boolean hasAllDocumentsApproved(Long driverId) {
        return Arrays.stream(getRequiredDocumentTypes())
                .allMatch(type -> driverDocumentRepository
                        .findByDriverIdAndDocumentType(driverId, type)
                        .filter(doc -> doc.getStatus() == DriverDocumentStatus.APPROVED)
                        .isPresent());
    }
}
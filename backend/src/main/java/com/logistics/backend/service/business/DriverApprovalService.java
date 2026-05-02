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

        if (driver.getRole() == null || driver.getRole().getRole() != Role.DRIVER) {
            throw new BadRequestException("User is not a DRIVER");
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
}
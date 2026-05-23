package com.logistics.backend.service.business;

import com.logistics.backend.entity.enums.DriverDocumentStatus;
import com.logistics.backend.entity.enums.LoadStatus;
import com.logistics.backend.entity.enums.OfferStatus;
import com.logistics.backend.entity.enums.Role;
import com.logistics.backend.entity.enums.RoutePlanStatus;
import com.logistics.backend.entity.user.User;
import com.logistics.backend.exception.BadRequestException;
import com.logistics.backend.exception.ResourceNotFoundException;
import com.logistics.backend.mapper.LoadMapper;
import com.logistics.backend.mapper.RoutePlanMapper;
import com.logistics.backend.mapper.TransportOfferMapper;
import com.logistics.backend.mapper.UserMapper;
import com.logistics.backend.payload.response.AdminDashboardResponse;
import com.logistics.backend.payload.response.LoadResponse;
import com.logistics.backend.payload.response.RoutePlanResponse;
import com.logistics.backend.payload.response.TransportOfferResponse;
import com.logistics.backend.payload.response.UserResponse;
import com.logistics.backend.repository.business.DriverDocumentRepository;
import com.logistics.backend.repository.business.LoadRepository;
import com.logistics.backend.repository.business.RoutePlanRepository;
import com.logistics.backend.repository.business.TransportOfferRepository;
import com.logistics.backend.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminPanelService {

    private final UserRepository userRepository;
    private final LoadRepository loadRepository;
    private final RoutePlanRepository routePlanRepository;
    private final TransportOfferRepository transportOfferRepository;
    private final DriverDocumentRepository driverDocumentRepository;
    private final UserMapper userMapper;
    private final LoadMapper loadMapper;
    private final RoutePlanMapper routePlanMapper;
    private final TransportOfferMapper transportOfferMapper;

    public AdminDashboardResponse getDashboard() {
        return AdminDashboardResponse.builder()
                .totalUsers(userRepository.count())
                .totalCustomers(userRepository.countByRole_Role(Role.CUSTOMER))
                .totalDrivers(userRepository.countByRole_Role(Role.DRIVER))
                .approvedDrivers(userRepository.countByDriverApproved(true))
                .pendingDrivers(userRepository.countByRole_Role(Role.DRIVER) - userRepository.countByDriverApproved(true))
                .disabledUsers(userRepository.countByEnabled(false))
                .totalLoads(loadRepository.count())
                .openLoads(loadRepository.countByStatus(LoadStatus.OPEN))
                .matchedLoads(loadRepository.countByStatus(LoadStatus.MATCHED))
                .deliveredLoads(loadRepository.countByStatus(LoadStatus.DELIVERED))
                .cancelledLoads(loadRepository.countByStatus(LoadStatus.CANCELLED))
                .totalRoutePlans(routePlanRepository.count())
                .openRoutePlans(routePlanRepository.countByStatus(RoutePlanStatus.OPEN))
                .fullRoutePlans(routePlanRepository.countByStatus(RoutePlanStatus.FULL))
                .inProgressRoutePlans(routePlanRepository.countByStatus(RoutePlanStatus.IN_PROGRESS))
                .completedRoutePlans(routePlanRepository.countByStatus(RoutePlanStatus.COMPLETED))
                .cancelledRoutePlans(routePlanRepository.countByStatus(RoutePlanStatus.CANCELLED))
                .totalOffers(transportOfferRepository.count())
                .pendingOffers(transportOfferRepository.countByStatus(OfferStatus.PENDING))
                .acceptedOffers(transportOfferRepository.countByStatus(OfferStatus.ACCEPTED))
                .rejectedOffers(transportOfferRepository.countByStatus(OfferStatus.REJECTED))
                .cancelledOffers(transportOfferRepository.countByStatus(OfferStatus.CANCELLED))
                .counteredOffers(transportOfferRepository.countByStatus(OfferStatus.COUNTERED))
                .pendingDriverDocuments(driverDocumentRepository.countByStatus(DriverDocumentStatus.PENDING))
                .build();
    }

    public List<UserResponse> getUsers(String role) {
        if (role == null || role.isBlank()) {
            return userRepository.findAll()
                    .stream()
                    .map(userMapper::toUserResponse)
                    .toList();
        }

        Role parsedRole = parseEnum(Role.class, role, "role");
        return userRepository.findByRole_Role(parsedRole)
                .stream()
                .map(userMapper::toUserResponse)
                .toList();
    }

    public UserResponse deactivateUser(Long userId) {
        User user = findUser(userId);

        if (Boolean.FALSE.equals(user.getEnabled())) {
            throw new BadRequestException("User is already disabled");
        }

        user.setEnabled(false);
        return userMapper.toUserResponse(userRepository.save(user));
    }

    public UserResponse activateUser(Long userId) {
        User user = findUser(userId);

        if (Boolean.TRUE.equals(user.getEnabled())) {
            throw new BadRequestException("User is already active");
        }

        user.setEnabled(true);
        return userMapper.toUserResponse(userRepository.save(user));
    }

    public List<LoadResponse> getLoads(String status) {
        if (status == null || status.isBlank()) {
            return loadRepository.findAll()
                    .stream()
                    .map(loadMapper::toResponse)
                    .toList();
        }

        LoadStatus parsedStatus = parseEnum(LoadStatus.class, status, "status");
        return loadRepository.findByStatus(parsedStatus)
                .stream()
                .map(loadMapper::toResponse)
                .toList();
    }

    public List<RoutePlanResponse> getRoutePlans(String status) {
        if (status == null || status.isBlank()) {
            return routePlanRepository.findAll()
                    .stream()
                    .map(routePlanMapper::toResponse)
                    .toList();
        }

        RoutePlanStatus parsedStatus = parseEnum(RoutePlanStatus.class, status, "status");
        return routePlanRepository.findByStatus(parsedStatus)
                .stream()
                .map(routePlanMapper::toResponse)
                .toList();
    }

    public List<TransportOfferResponse> getTransportOffers(String status) {
        if (status == null || status.isBlank()) {
            return transportOfferRepository.findAll()
                    .stream()
                    .map(transportOfferMapper::toResponse)
                    .toList();
        }

        OfferStatus parsedStatus = parseEnum(OfferStatus.class, status, "status");
        return transportOfferRepository.findByStatus(parsedStatus)
                .stream()
                .map(transportOfferMapper::toResponse)
                .toList();
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
    }

    private <T extends Enum<T>> T parseEnum(Class<T> enumType, String value, String fieldName) {
        try {
            return Enum.valueOf(enumType, value.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException("Invalid " + fieldName + ": " + value);
        }
    }
}

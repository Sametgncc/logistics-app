package com.logistics.backend.service.business;

import com.logistics.backend.entity.business.Load;
import com.logistics.backend.entity.business.RoutePlan;
import com.logistics.backend.entity.business.TransportOffer;
import com.logistics.backend.entity.enums.LoadStatus;
import com.logistics.backend.entity.enums.NotificationType;
import com.logistics.backend.entity.enums.OfferDirection;
import com.logistics.backend.entity.enums.OfferStatus;
import com.logistics.backend.entity.enums.Role;
import com.logistics.backend.entity.enums.RoutePlanStatus;
import com.logistics.backend.entity.user.User;
import com.logistics.backend.exception.BadRequestException;
import com.logistics.backend.exception.ResourceNotFoundException;
import com.logistics.backend.mapper.TransportOfferMapper;
import com.logistics.backend.payload.request.CreateTransportOfferRequest;
import com.logistics.backend.payload.request.CounterTransportOfferRequest;
import com.logistics.backend.payload.response.TransportOfferResponse;
import com.logistics.backend.repository.business.LoadRepository;
import com.logistics.backend.repository.business.RoutePlanRepository;
import com.logistics.backend.repository.business.TransportOfferRepository;
import com.logistics.backend.repository.user.UserRepository;
import com.logistics.backend.service.user.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TransportOfferService {

    private final TransportOfferRepository transportOfferRepository;
    private final LoadRepository loadRepository;
    private final RoutePlanRepository routePlanRepository;
    private final UserRepository userRepository;
    private final TransportOfferMapper transportOfferMapper;
    private final NotificationService notificationService;

    @Transactional
    public TransportOfferResponse createOffer(CreateTransportOfferRequest request, Long offeredById) {
        Load load = loadRepository.findById(request.getLoadId())
                .orElseThrow(() -> new ResourceNotFoundException("Load not found with id: " + request.getLoadId()));

        RoutePlan routePlan = routePlanRepository.findById(request.getRoutePlanId())
                .orElseThrow(() -> new ResourceNotFoundException("Route plan not found with id: " + request.getRoutePlanId()));

        User offeredBy = userRepository.findById(offeredById)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + offeredById));

        validateLoadAndRoutePlan(load, routePlan);
        validateOfferDirection(request.getDirection(), offeredBy, load, routePlan);
        validateNoPendingOfferExists(load.getId(), routePlan.getId());

        TransportOffer offer = TransportOffer.builder()
                .load(load)
                .routePlan(routePlan)
                .driver(routePlan.getDriver())
                .customer(load.getCustomer())
                .offeredBy(offeredBy)
                .direction(request.getDirection())
                .price(request.getPrice())
                .message(request.getMessage())
                .status(OfferStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();

        TransportOffer savedOffer = transportOfferRepository.save(offer);
        notifyOfferRecipient(savedOffer);

        return transportOfferMapper.toResponse(savedOffer);
    }

    @Transactional
    public TransportOfferResponse counterOffer(Long offerId, CounterTransportOfferRequest request, Long offeredById) {
        TransportOffer originalOffer = findOfferById(offerId);

        if (originalOffer.getStatus() != OfferStatus.PENDING) {
            throw new BadRequestException("Only PENDING offers can be countered");
        }

        User offeredBy = userRepository.findById(offeredById)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + offeredById));

        if (originalOffer.getOfferedBy().getId().equals(offeredBy.getId())) {
            throw new BadRequestException("Offer owner cannot counter their own offer");
        }

        if (!isOfferParticipant(originalOffer, offeredBy.getId())) {
            throw new BadRequestException("Only offer participants can create counter offers");
        }

        Load load = originalOffer.getLoad();
        RoutePlan routePlan = originalOffer.getRoutePlan();
        OfferDirection counterDirection = resolveOfferDirection(offeredBy, originalOffer);

        validateLoadAndRoutePlan(load, routePlan);
        validateOfferDirection(counterDirection, offeredBy, load, routePlan);

        originalOffer.setStatus(OfferStatus.COUNTERED);
        originalOffer.setRespondedAt(LocalDateTime.now());
        transportOfferRepository.save(originalOffer);

        TransportOffer counterOffer = TransportOffer.builder()
                .load(load)
                .routePlan(routePlan)
                .driver(originalOffer.getDriver())
                .customer(originalOffer.getCustomer())
                .offeredBy(offeredBy)
                .direction(counterDirection)
                .price(request.getPrice())
                .message(request.getMessage())
                .status(OfferStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();

        TransportOffer savedOffer = transportOfferRepository.save(counterOffer);
        notifyOfferRecipient(savedOffer, NotificationType.TRANSPORT_OFFER_COUNTERED, "Counter offer received", "You received a counter offer.");

        return transportOfferMapper.toResponse(savedOffer);
    }

    public List<TransportOfferResponse> getOffersByLoadId(Long loadId) {
        loadRepository.findById(loadId)
                .orElseThrow(() -> new ResourceNotFoundException("Load not found with id: " + loadId));

        return transportOfferRepository.findByLoadId(loadId)
                .stream()
                .map(transportOfferMapper::toResponse)
                .toList();
    }

    public List<TransportOfferResponse> getOffersByRoutePlanId(Long routePlanId) {
        routePlanRepository.findById(routePlanId)
                .orElseThrow(() -> new ResourceNotFoundException("Route plan not found with id: " + routePlanId));

        return transportOfferRepository.findByRoutePlanId(routePlanId)
                .stream()
                .map(transportOfferMapper::toResponse)
                .toList();
    }

    public List<TransportOfferResponse> getOffersByDriverId(Long driverId) {
        userRepository.findById(driverId)
                .orElseThrow(() -> new ResourceNotFoundException("Driver not found with id: " + driverId));

        return transportOfferRepository.findByDriverId(driverId)
                .stream()
                .map(transportOfferMapper::toResponse)
                .toList();
    }

    public List<TransportOfferResponse> getOffersByCustomerId(Long customerId) {
        userRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + customerId));

        return transportOfferRepository.findByCustomerId(customerId)
                .stream()
                .map(transportOfferMapper::toResponse)
                .toList();
    }

    private void validateLoadAndRoutePlan(Load load, RoutePlan routePlan) {
        if (load.getStatus() != LoadStatus.OPEN) {
            throw new BadRequestException("Only OPEN loads can receive offers");
        }

        if (routePlan.getStatus() != RoutePlanStatus.OPEN) {
            throw new BadRequestException("Only OPEN route plans can be used for offers");
        }

        if (routePlan.getAvailableCapacityKg().compareTo(load.getWeightKg()) < 0 ||
                routePlan.getAvailableCapacityVolumeM3().compareTo(load.getVolumeM3()) < 0) {
            throw new BadRequestException("Route plan capacity is not enough for this load");
        }
    }

    private void validateOfferDirection(
            OfferDirection direction,
            User offeredBy,
            Load load,
            RoutePlan routePlan
    ) {
        if (offeredBy.getRole() == null || offeredBy.getRole().getRole() == null) {
            throw new BadRequestException("Offer owner role information is missing");
        }

        if (direction == OfferDirection.DRIVER_TO_CUSTOMER) {
            // DEĞİŞEN KISIM: Veritabanında rolü DRIVER olanlar VEYA driverApproved onayı almış olanlar bu işlemi yapabilsin
            boolean isDriver = (offeredBy.getRole().getRole() == Role.DRIVER) || Boolean.TRUE.equals(offeredBy.getDriverApproved());

            if (!isDriver) {
                throw new BadRequestException("Only DRIVER can create DRIVER_TO_CUSTOMER offers");
            }

            if (!routePlan.getDriver().getId().equals(offeredBy.getId())) {
                throw new BadRequestException("Driver can only offer with own route plan");
            }
        }

        if (direction == OfferDirection.CUSTOMER_TO_DRIVER) {
            // Not: İleride müşteri tarafında benzer bir sorun yaşarsan burayı da
            // offeredBy.getRole().getRole() != Role.CUSTOMER yerine uygulamanın mantığına göre esnetebilirsin.
            if (offeredBy.getRole().getRole() != Role.CUSTOMER) {
                throw new BadRequestException("Only CUSTOMER can create CUSTOMER_TO_DRIVER offers");
            }

            if (!load.getCustomer().getId().equals(offeredBy.getId())) {
                throw new BadRequestException("Customer can only offer with own load");
            }
        }
    }

    private void validateNoPendingOfferExists(Long loadId, Long routePlanId) {
        boolean exists = transportOfferRepository.existsByLoadIdAndRoutePlanIdAndStatus(
                loadId,
                routePlanId,
                OfferStatus.PENDING
        );

        if (exists) {
            throw new BadRequestException("There is already a pending offer for this load and route plan");
        }
    }

    private boolean isOfferParticipant(TransportOffer offer, Long userId) {
        return offer.getDriver().getId().equals(userId) || offer.getCustomer().getId().equals(userId);
    }

    private OfferDirection resolveOfferDirection(User offeredBy, TransportOffer originalOffer) {
        if (originalOffer.getDriver().getId().equals(offeredBy.getId())) {
            return OfferDirection.DRIVER_TO_CUSTOMER;
        }

        if (originalOffer.getCustomer().getId().equals(offeredBy.getId())) {
            return OfferDirection.CUSTOMER_TO_DRIVER;
        }

        throw new BadRequestException("Only offer participants can create counter offers");
    }

    public TransportOfferResponse acceptOffer(Long offerId) {
        TransportOffer offer = findOfferById(offerId);

        if (offer.getStatus() != OfferStatus.PENDING) {
            throw new BadRequestException("Only PENDING offers can be accepted");
        }

        offer.setStatus(OfferStatus.ACCEPTED);
        offer.setRespondedAt(LocalDateTime.now());

        offer.getLoad().setStatus(LoadStatus.MATCHED);

        RoutePlan routePlan = offer.getRoutePlan();
        routePlan.setAvailableCapacityKg(routePlan.getAvailableCapacityKg().subtract(offer.getLoad().getWeightKg()));
        routePlan.setAvailableCapacityVolumeM3(routePlan.getAvailableCapacityVolumeM3().subtract(offer.getLoad().getVolumeM3()));

        // Eğer kapasite sıfırlandıysa rota planını FULL yap
        if (routePlan.getAvailableCapacityKg().compareTo(BigDecimal.ZERO) <= 0 ||
                routePlan.getAvailableCapacityVolumeM3().compareTo(BigDecimal.ZERO) <= 0) {
            routePlan.setStatus(RoutePlanStatus.FULL);
        }

        rejectOtherPendingOffersForSameLoad(
                offer.getLoad().getId(),
                offer.getId()
        );

        TransportOffer savedOffer = transportOfferRepository.save(offer);
        notifyOfferOwner(savedOffer, NotificationType.TRANSPORT_OFFER_ACCEPTED, "Offer accepted", "Your transport offer was accepted.");

        return transportOfferMapper.toResponse(savedOffer);
    }

    public TransportOfferResponse rejectOffer(Long offerId) {
        TransportOffer offer = findOfferById(offerId);

        if (offer.getStatus() != OfferStatus.PENDING) {
            throw new BadRequestException("Only PENDING offers can be rejected");
        }

        offer.setStatus(OfferStatus.REJECTED);
        offer.setRespondedAt(LocalDateTime.now());

        TransportOffer savedOffer = transportOfferRepository.save(offer);
        notifyOfferOwner(savedOffer, NotificationType.TRANSPORT_OFFER_REJECTED, "Offer rejected", "Your transport offer was rejected.");

        return transportOfferMapper.toResponse(savedOffer);
    }

    public TransportOfferResponse cancelOffer(Long offerId) {
        TransportOffer offer = findOfferById(offerId);

        if (offer.getStatus() != OfferStatus.PENDING) {
            throw new BadRequestException("Only PENDING offers can be cancelled");
        }

        offer.setStatus(OfferStatus.CANCELLED);
        offer.setRespondedAt(LocalDateTime.now());

        TransportOffer savedOffer = transportOfferRepository.save(offer);
        notifyOfferCounterparty(savedOffer, NotificationType.TRANSPORT_OFFER_CANCELLED, "Offer cancelled", "A transport offer was cancelled.");

        return transportOfferMapper.toResponse(savedOffer);
    }

    private TransportOffer findOfferById(Long offerId) {
        return transportOfferRepository.findById(offerId)
                .orElseThrow(() -> new ResourceNotFoundException("Offer not found with id: " + offerId));
    }

    private void rejectOtherPendingOffersForSameLoad(Long loadId, Long acceptedOfferId) {
        List<TransportOffer> pendingOffers = transportOfferRepository.findByLoadId(loadId)
                .stream()
                .filter(offer -> offer.getStatus() == OfferStatus.PENDING)
                .filter(offer -> !offer.getId().equals(acceptedOfferId))
                .toList();

        pendingOffers.forEach(offer -> {
            offer.setStatus(OfferStatus.REJECTED);
            offer.setRespondedAt(LocalDateTime.now());
        });

        transportOfferRepository.saveAll(pendingOffers);
    }

    private void notifyOfferRecipient(TransportOffer offer) {
        notifyOfferRecipient(
                offer,
                NotificationType.TRANSPORT_OFFER_CREATED,
                "New transport offer",
                "You received a new transport offer."
        );
    }

    private void notifyOfferRecipient(
            TransportOffer offer,
            NotificationType type,
            String title,
            String message
    ) {
        User recipient = offer.getDirection() == OfferDirection.CUSTOMER_TO_DRIVER
                ? offer.getDriver()
                : offer.getCustomer();

        notificationService.createNotification(
                recipient.getId(),
                type,
                title,
                message,
                "TRANSPORT_OFFER",
                offer.getId()
        );
    }

    private void notifyOfferOwner(
            TransportOffer offer,
            NotificationType type,
            String title,
            String message
    ) {
        notificationService.createNotification(
                offer.getOfferedBy().getId(),
                type,
                title,
                message,
                "TRANSPORT_OFFER",
                offer.getId()
        );
    }

    private void notifyOfferCounterparty(
            TransportOffer offer,
            NotificationType type,
            String title,
            String message
    ) {
        User counterparty = offer.getOfferedBy().getId().equals(offer.getDriver().getId())
                ? offer.getCustomer()
                : offer.getDriver();

        notificationService.createNotification(
                counterparty.getId(),
                type,
                title,
                message,
                "TRANSPORT_OFFER",
                offer.getId()
        );
    }
}

package com.logistics.backend.mapper;

import com.logistics.backend.entity.business.Load;
import com.logistics.backend.entity.business.LoadMatch;
import com.logistics.backend.entity.business.Payment;
import com.logistics.backend.entity.business.RoutePlan;
import com.logistics.backend.entity.business.TransportOffer;
import com.logistics.backend.payload.response.TransportOfferResponse;
import com.logistics.backend.repository.business.LoadMatchRepository;
import com.logistics.backend.repository.business.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TransportOfferMapper {

    private final LoadMatchRepository loadMatchRepository;
    private final PaymentRepository paymentRepository;

    public TransportOfferResponse toResponse(TransportOffer offer) {
        if (offer == null) {
            return null;
        }

        Load load = offer.getLoad();
        RoutePlan routePlan = offer.getRoutePlan();
        Long loadMatchId = loadMatchRepository.findByLoadIdAndRoutePlanId(load.getId(), routePlan.getId())
                .map(LoadMatch::getId)
                .orElse(null);
        var paymentStatus = paymentRepository.findByTransportOfferId(offer.getId())
                .map(Payment::getStatus)
                .orElse(null);

        return TransportOfferResponse.builder()
                .id(offer.getId())

                .loadId(load.getId())
                .loadMatchId(loadMatchId)
                .pickupCity(load.getPickupCity())
                .deliveryCity(load.getDeliveryCity())
                .loadStatus(load.getStatus())

                .routePlanId(routePlan.getId())
                .originCity(routePlan.getOriginCity())
                .destinationCity(routePlan.getDestinationCity())
                .routePlanStatus(routePlan.getStatus())

                .driverId(offer.getDriver().getId())
                .driverName(offer.getDriver().getName())
                .driverEmail(offer.getDriver().getEmail())

                .customerId(offer.getCustomer().getId())
                .customerName(offer.getCustomer().getName())
                .customerEmail(offer.getCustomer().getEmail())

                .offeredById(offer.getOfferedBy().getId())
                .offeredByEmail(offer.getOfferedBy().getEmail())

                .direction(offer.getDirection())
                .price(offer.getPrice())
                .message(offer.getMessage())
                .status(offer.getStatus())
                .paymentStatus(paymentStatus)
                .createdAt(offer.getCreatedAt())
                .respondedAt(offer.getRespondedAt())
                .build();
    }
}

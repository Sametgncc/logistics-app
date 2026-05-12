package com.logistics.backend.mapper;

import com.logistics.backend.entity.business.Load;
import com.logistics.backend.entity.business.RoutePlan;
import com.logistics.backend.entity.business.TransportOffer;
import com.logistics.backend.payload.response.TransportOfferResponse;
import org.springframework.stereotype.Component;

@Component
public class TransportOfferMapper {

    public TransportOfferResponse toResponse(TransportOffer offer) {
        if (offer == null) {
            return null;
        }

        Load load = offer.getLoad();
        RoutePlan routePlan = offer.getRoutePlan();

        return TransportOfferResponse.builder()
                .id(offer.getId())

                .loadId(load.getId())
                .pickupCity(load.getPickupCity())
                .deliveryCity(load.getDeliveryCity())

                .routePlanId(routePlan.getId())
                .originCity(routePlan.getOriginCity())
                .destinationCity(routePlan.getDestinationCity())

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
                .createdAt(offer.getCreatedAt())
                .respondedAt(offer.getRespondedAt())
                .build();
    }
}
package com.logistics.backend.service.business;

import com.iyzipay.Options;
import com.iyzipay.model.Address;
import com.iyzipay.model.BasketItem;
import com.iyzipay.model.BasketItemType;
import com.iyzipay.model.Buyer;
import com.iyzipay.model.CheckoutForm;
import com.iyzipay.model.CheckoutFormInitialize;
import com.iyzipay.model.Currency;
import com.iyzipay.model.Locale;
import com.iyzipay.model.PaymentGroup;
import com.iyzipay.request.CreateCheckoutFormInitializeRequest;
import com.iyzipay.request.RetrieveCheckoutFormRequest;
import com.logistics.backend.entity.business.Payment;
import com.logistics.backend.entity.business.TransportOffer;
import com.logistics.backend.entity.enums.OfferStatus;
import com.logistics.backend.entity.enums.PaymentStatus;
import com.logistics.backend.entity.user.User;
import com.logistics.backend.exception.BadRequestException;
import com.logistics.backend.exception.ResourceNotFoundException;
import com.logistics.backend.payload.request.CreateCheckoutPaymentRequest;
import com.logistics.backend.payload.response.CheckoutPaymentResponse;
import com.logistics.backend.repository.business.PaymentRepository;
import com.logistics.backend.repository.business.TransportOfferRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final TransportOfferRepository transportOfferRepository;

    @Value("${iyzico.api-key:}")
    private String apiKey;
    @Value("${iyzico.secret-key:}")
    private String secretKey;
    @Value("${iyzico.base-url:https://sandbox-api.iyzipay.com}")
    private String baseUrl;
    @Value("${iyzico.callback-url:http://localhost:8080/payments/iyzico/callback}")
    private String callbackUrl;
    @Value("${iyzico.sandbox:true}")
    private boolean sandbox;

    @Transactional
    public CheckoutPaymentResponse startCheckout(CreateCheckoutPaymentRequest request, Long customerId) {
        requireConfigured();
        Long offerId = request.getOfferId();
        TransportOffer offer = transportOfferRepository.findById(offerId)
                .orElseThrow(() -> new ResourceNotFoundException("Offer not found with id: " + offerId));

        if (!offer.getCustomer().getId().equals(customerId)) {
            throw new BadRequestException("Only the customer of this offer can start its payment");
        }
        if (offer.getStatus() != OfferStatus.ACCEPTED) {
            throw new BadRequestException("Only ACCEPTED offers can be paid");
        }
        paymentRepository.findByTransportOfferId(offerId).ifPresent(payment -> {
            throw new BadRequestException("A payment session already exists for this offer");
        });

        User buyer = offer.getCustomer();
        validateBuyerProfile(buyer);
        String conversationId = "offer-" + offer.getId() + "-" + UUID.randomUUID();

        CheckoutFormInitialize response = CheckoutFormInitialize.create(
                buildInitializeRequest(offer, buyer, conversationId), options());
        if (!"success".equalsIgnoreCase(response.getStatus()) || response.getToken() == null) {
            throw new BadRequestException("iyzico checkout form could not be started: " + response.getErrorMessage());
        }

        Payment payment = Payment.builder()
                .transportOffer(offer)
                .amount(offer.getPrice())
                .currency(Currency.TRY.name())
                .status(PaymentStatus.PENDING)
                .iyzicoToken(response.getToken())
                .conversationId(conversationId)
                .createdAt(LocalDateTime.now())
                .build();

        return toResponse(paymentRepository.save(payment), response.getPaymentPageUrl());
    }

    @Transactional
    public CheckoutPaymentResponse verifyCheckout(String token) {
        Payment payment = paymentRepository.findByIyzicoToken(token)
                .orElseThrow(() -> new ResourceNotFoundException("Payment session not found"));

        RetrieveCheckoutFormRequest request = new RetrieveCheckoutFormRequest();
        request.setLocale(Locale.TR.getValue());
        request.setConversationId(payment.getConversationId());
        request.setToken(token);
        CheckoutForm result = CheckoutForm.retrieve(request, options());

        if (!"success".equalsIgnoreCase(result.getStatus())) {
            payment.setStatus(PaymentStatus.FAILED);
            payment.setFailureReason(result.getErrorMessage());
        } else if ("SUCCESS".equalsIgnoreCase(result.getPaymentStatus())) {
            payment.setStatus(PaymentStatus.PAID);
            payment.setIyzicoPaymentId(result.getPaymentId());
            payment.setCompletedAt(LocalDateTime.now());
        } else {
            payment.setStatus(PaymentStatus.FAILED);
            payment.setFailureReason(result.getErrorMessage());
        }

        return toResponse(paymentRepository.save(payment), null);
    }

    private CreateCheckoutFormInitializeRequest buildInitializeRequest(TransportOffer offer, User buyer, String conversationId) {
        CreateCheckoutFormInitializeRequest request = new CreateCheckoutFormInitializeRequest();
        request.setLocale(Locale.TR.getValue());
        request.setConversationId(conversationId);
        request.setPrice(offer.getPrice());
        request.setPaidPrice(offer.getPrice());
        request.setCurrency(Currency.TRY.name());
        request.setEnabledInstallments(List.of(1));
        request.setBasketId("offer-" + offer.getId());
        request.setPaymentGroup(PaymentGroup.PRODUCT.name());
        request.setCallbackUrl(callbackUrl);
        request.setBuyer(toBuyer(buyer));
        request.setShippingAddress(toAddress(buyer));
        request.setBillingAddress(toAddress(buyer));
        request.setBasketItems(List.of(toBasketItem(offer)));
        return request;
    }

    private Buyer toBuyer(User user) {
        Buyer buyer = new Buyer();
        buyer.setId(String.valueOf(user.getId()));
        buyer.setName(user.getName());
        buyer.setSurname(user.getLastName());
        buyer.setGsmNumber(user.getPhone());
        buyer.setEmail(user.getEmail());
        buyer.setRegistrationAddress(user.getAddress());
        buyer.setIp("127.0.0.1");
        // Sandbox accepts the documented test identity number. Real identity data must be collected before live use.
        buyer.setIdentityNumber(sandbox ? "11111111111" : null);
        return buyer;
    }

    private Address toAddress(User user) {
        Address address = new Address();
        address.setContactName(user.getName() + " " + user.getLastName());
        address.setCity("Istanbul");
        address.setCountry("Turkey");
        address.setAddress(user.getAddress());
        return address;
    }

    private BasketItem toBasketItem(TransportOffer offer) {
        BasketItem item = new BasketItem();
        item.setId("offer-" + offer.getId());
        item.setName("Nakliye hizmeti #" + offer.getId());
        item.setCategory1("Lojistik");
        item.setItemType(BasketItemType.VIRTUAL.name());
        item.setPrice(offer.getPrice());
        return item;
    }

    private Options options() {
        Options options = new Options();
        options.setApiKey(apiKey);
        options.setSecretKey(secretKey);
        options.setBaseUrl(baseUrl);
        return options;
    }

    private void requireConfigured() {
        if (apiKey.isBlank() || secretKey.isBlank()) {
            throw new BadRequestException("iyzico sandbox credentials are not configured");
        }
    }

    private void validateBuyerProfile(User buyer) {
        if (isBlank(buyer.getName()) || isBlank(buyer.getLastName()) || isBlank(buyer.getPhone()) || isBlank(buyer.getAddress())) {
            throw new BadRequestException("Complete name, surname, phone and address before starting a payment");
        }
        if (!sandbox) {
            throw new BadRequestException("Live iyzico payments require identity verification before they can be enabled");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private CheckoutPaymentResponse toResponse(Payment payment, String paymentPageUrl) {
        return CheckoutPaymentResponse.builder()
                .paymentId(payment.getId())
                .status(payment.getStatus())
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .paymentPageUrl(paymentPageUrl)
                .build();
    }
}

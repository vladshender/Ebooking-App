package com.example.ebooking.service.payment;

import com.example.ebooking.dto.booking.BookingResponseDto;
import com.example.ebooking.dto.payment.CreatePaymentSessionDto;
import com.example.ebooking.dto.payment.PaymentResponseDto;
import com.example.ebooking.dto.payment.PaymentWithoutSessionDto;
import com.example.ebooking.exception.EntityNotFoundException;
import com.example.ebooking.exception.PaymentStatusException;
import com.example.ebooking.mapper.BookingMapper;
import com.example.ebooking.mapper.PaymentMapper;
import com.example.ebooking.model.Booking;
import com.example.ebooking.model.Payment;
import com.example.ebooking.repository.booking.BookingRepository;
import com.example.ebooking.repository.payment.PaymentRepository;
import com.example.ebooking.service.notification.NotificationService;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import jakarta.annotation.PostConstruct;
import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;

@Service
@RequiredArgsConstructor
public class StripePaymentService implements PaymentService {
    public static final String SUCCESS_URL = "http://localhost:8080/api/payments/success";
    public static final String CANCEL_URL = "http://localhost:8080/api/payments/cancel";
    public static final long DEFAULT_QUANTITY = 1L;
    public static final String DEFAULT_CURRENCY = "usd";
    public static final BigDecimal CENTS_AMOUNT = BigDecimal.valueOf(100);
    public static final String SESSION_PLACEHOLDER = "{CHECKOUT_SESSION_ID}";
    public static final String SESSION_REQUEST_PARAM = "sessionId";
    public static final Payment.PaymentStatus PAID = Payment.PaymentStatus.PAID;
    public static final Payment.PaymentStatus PENDING = Payment.PaymentStatus.PENDING;
    public static final Payment.PaymentStatus EXPIRED = Payment.PaymentStatus.EXPIRED;

    @Value("${stripe.secretKey}")
    private String stripeSecretKey;

    private final PaymentRepository paymentRepository;
    private final BookingRepository bookingRepository;
    private final BookingMapper bookingMapper;
    private final PaymentMapper paymentMapper;
    private final NotificationService notificationService;

    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeSecretKey;
    }

    @Override
    public List<PaymentResponseDto> getPaymentsForUser(Long userId,
                                                       Pageable pageable) {
        Page<Payment> paymentFromDB = paymentRepository.findByBookingUserId(userId,
                pageable);

        if (paymentFromDB.getContent().isEmpty()) {
            throw new EntityNotFoundException("Payments not found by user id: "
                    + userId);
        }

        return paymentMapper.toDtoList(paymentFromDB.getContent());
    }

    @Override
    public List<PaymentResponseDto> getPaymentsForAdmin(Pageable pageable) {
        return paymentMapper.toDtoList(paymentRepository.findAll(pageable)
                .getContent());
    }

    @Override
    public CreatePaymentSessionDto createPaymentSession(Long bookingId)
            throws StripeException {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new EntityNotFoundException("Payment not found by id: "
                        + bookingId));

        BigDecimal totalAmount = calculateTotalAmount(booking);

        String cancelUrl = UriComponentsBuilder.fromHttpUrl(CANCEL_URL)
                .queryParam(SESSION_REQUEST_PARAM, SESSION_PLACEHOLDER)
                .build(false)
                .toUriString();

        String successUrl = UriComponentsBuilder.fromHttpUrl(SUCCESS_URL)
                .queryParam(SESSION_REQUEST_PARAM, SESSION_PLACEHOLDER)
                .build(false)
                .toUriString();

        SessionCreateParams params = createSessionParams(totalAmount, cancelUrl,
                successUrl);
        Session session = Session.create(params);

        Payment payment = new Payment();
        payment.setBooking(booking);
        payment.setSessionId(session.getId());
        payment.setSessionUrl(session.getUrl());
        payment.setExpiredTime(session.getExpiresAt());
        payment.setAmount(totalAmount);
        payment.setStatus(PENDING);
        paymentRepository.save(payment);

        return paymentMapper.toPaymentResponseDto(paymentRepository.save(payment));
    }

    @Override
    @Transactional
    public PaymentWithoutSessionDto processSuccessfulPayment(String sessionId)
            throws StripeException {
        Payment payment = findPaymentBySessionId(sessionId);
        payment.setStatus(PAID);
        paymentRepository.updateStatus(payment.getId(), PAID);

        Booking booking = payment.getBooking();
        bookingRepository.updateStatus(booking.getId(), Booking.Status.CONFIRMED);

        notificationService.sendPaymentSuccessMessage(payment);
        return paymentMapper.toPaymentWithoutSessionDto(payment);
    }

    @Override
    public BookingResponseDto processCancelPayment(String sessionId) {
        Payment payment = findPaymentBySessionId(sessionId);
        Booking booking = payment.getBooking();
        return bookingMapper.toDto(booking);
    }

    @Scheduled(cron = "0 1 * * * *")
    @Override
    public void checkExpiredPayments() throws StripeException {
        Long currentTime = System.currentTimeMillis() / 1000;

        paymentRepository.updateExpiredPayments(
                currentTime,
                Payment.PaymentStatus.EXPIRED,
                Payment.PaymentStatus.PENDING);
    }

    @Override
    public CreatePaymentSessionDto renewPaymentSession(Long paymentId)
            throws StripeException {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new EntityNotFoundException("Payment not found"));

        if (payment.getStatus() != Payment.PaymentStatus.EXPIRED) {
            throw new PaymentStatusException("Payment is not expired.");
        }

        return createPaymentSession(payment.getBooking().getId());
    }

    @Override
    public boolean existsByBookingUserIdAndStatus(Long userId) {
        return paymentRepository.existsByBookingUserIdAndStatus(userId, PENDING);
    }

    private SessionCreateParams.LineItem.PriceData createPriceData(BigDecimal totalAmount) {
        return SessionCreateParams.LineItem.PriceData.builder()
                .setCurrency(DEFAULT_CURRENCY)
                .setUnitAmount(totalAmount.multiply(CENTS_AMOUNT).longValue())
                .setProductData(SessionCreateParams.LineItem.PriceData.ProductData.builder()
                        .setName("Booking Payment")
                        .build())
                .build();
    }

    private SessionCreateParams.LineItem createLineItem(BigDecimal totalAmount) {
        return SessionCreateParams.LineItem.builder()
                .setPriceData(createPriceData(totalAmount))
                .setQuantity(DEFAULT_QUANTITY)
                .build();
    }

    private SessionCreateParams createSessionParams(BigDecimal totalAmount,
                                                    String cancelUrl,
                                                    String successUrl) {
        return SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setCancelUrl(cancelUrl)
                .setSuccessUrl(successUrl)
                .addLineItem(createLineItem(totalAmount))
                .build();
    }

    private Payment findPaymentBySessionId(String sessionId) {
        return paymentRepository.findBySessionId(sessionId)
                .orElseThrow(() -> new EntityNotFoundException("Payment not found by session id: "
                        + sessionId));
    }

    private BigDecimal calculateTotalAmount(Booking booking) {
        long days = ChronoUnit.DAYS.between(booking.getCheckInDate().toLocalDate(),
                booking.getCheckOutDate().toLocalDate());
        BigDecimal size = booking.getAccommodation().getDailyRate();
        return size.multiply(BigDecimal.valueOf(days));
    }
}

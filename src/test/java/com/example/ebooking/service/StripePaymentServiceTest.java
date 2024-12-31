package com.example.ebooking.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mockStatic;

import com.example.ebooking.dto.booking.BookingResponseDto;
import com.example.ebooking.dto.payment.CreatePaymentSessionDto;
import com.example.ebooking.dto.payment.PaymentResponseDto;
import com.example.ebooking.dto.payment.PaymentWithoutSessionDto;
import com.example.ebooking.exception.EntityNotFoundException;
import com.example.ebooking.exception.PaymentStatusException;
import com.example.ebooking.mapper.BookingMapper;
import com.example.ebooking.mapper.PaymentMapper;
import com.example.ebooking.model.Accommodation;
import com.example.ebooking.model.Booking;
import com.example.ebooking.model.Payment;
import com.example.ebooking.repository.booking.BookingRepository;
import com.example.ebooking.repository.payment.PaymentRepository;
import com.example.ebooking.service.notification.NotificationService;
import com.example.ebooking.service.payment.StripePaymentService;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
public class StripePaymentServiceTest {
    @InjectMocks
    private StripePaymentService paymentService;

    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private BookingMapper bookingMapper;
    @Mock
    private PaymentMapper paymentMapper;
    @Mock
    private NotificationService notificationService;
    @Mock
    private Session session;

    @Test
    @DisplayName("Returns all payments by user")
    void getPaymentsForUser_withValidUserId_returnPayments() {
        Payment payment = new Payment();
        payment.setId(1L);
        payment.setStatus(Payment.PaymentStatus.PENDING);

        PaymentResponseDto responseDto = new PaymentResponseDto();
        responseDto.setId(payment.getId());
        responseDto.setStatus(payment.getStatus().toString());

        List<PaymentResponseDto> expected = List.of(responseDto);

        Pageable pageable = PageRequest.of(0, 10);
        List<Payment> payments = List.of(payment);
        Page<Payment> paymentPage = new PageImpl<>(payments, pageable, payments.size());

        Mockito.when(paymentRepository.findByBookingUserId(1L, pageable))
                .thenReturn(paymentPage);
        Mockito.when(paymentMapper.toDtoList(payments)).thenReturn(expected);

        List<PaymentResponseDto> actual = paymentService.getPaymentsForUser(1L,
                pageable);

        assertEquals(expected.size(), actual.size());
    }

    @Test
    @DisplayName("Returns payments with not exist user id")
    void getPaymentsForUser_withNotExistUserId_throwException() {
        Long userId = 1L;

        Pageable pageable = PageRequest.of(0, 10);
        List<Payment> payments = List.of();
        Page<Payment> paymentPage = new PageImpl<>(payments, pageable, payments.size());

        Mockito.when(paymentRepository.findByBookingUserId(1L, pageable))
                .thenReturn(paymentPage);

        Mockito.when(paymentRepository.findByBookingUserId(userId, pageable))
                .thenReturn(paymentPage);

        assertThatThrownBy(() -> paymentService.getPaymentsForUser(userId, pageable))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Payments not found by user id: "
                        + userId);
    }

    @Test
    @DisplayName("Return all payment for admin")
    void getPaymentsForAdmin_withValidId_returnAllPayment() {
        Long userId = 1L;

        Payment firstPayment = new Payment();
        firstPayment.setId(1L);
        firstPayment.setStatus(Payment.PaymentStatus.PENDING);

        Payment secondPayment = new Payment();
        secondPayment.setId(2L);
        secondPayment.setStatus(Payment.PaymentStatus.PENDING);

        PaymentResponseDto firstDto = new PaymentResponseDto();
        firstDto.setId(firstDto.getId());
        firstDto.setStatus(firstPayment.getStatus().toString());

        PaymentResponseDto secondDto = new PaymentResponseDto();
        secondDto.setId(secondDto.getId());
        secondDto.setStatus(secondDto.getStatus());

        List<PaymentResponseDto> expected = List.of(firstDto, secondDto);

        Pageable pageable = PageRequest.of(0, 10);
        List<Payment> paymnets = List.of(firstPayment, secondPayment);
        Page<Payment> paymentPage = new PageImpl<>(paymnets, pageable, paymnets.size());

        Mockito.when(paymentRepository.findAll(pageable))
                .thenReturn(paymentPage);
        Mockito.when(paymentMapper.toDtoList(paymnets)).thenReturn(expected);

        List<PaymentResponseDto> actual = paymentService.getPaymentsForAdmin(pageable);

        assertEquals(expected.size(), actual.size());
    }

    @Test
    @DisplayName("Create session with not exist booking id")
    void createPaymentSession_withNotExistId_throwException() {
        Long bookingId = 1L;

        Mockito.when(bookingRepository.findById(bookingId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> paymentService.createPaymentSession(bookingId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Payment not found by id: "
                        + bookingId);
    }

    @Test
    @DisplayName("Create session with valid id")
    void createPaymentSessionDto_withValidId_returnDto() throws StripeException {
        Long bookingId = 1L;

        Booking booking = new Booking();
        booking.setId(bookingId);
        booking.setAccommodation(new Accommodation());
        booking.getAccommodation().setDailyRate(BigDecimal.valueOf(100));
        booking.setCheckInDate(LocalDateTime.of(2025, 2, 23, 14, 0, 0));
        booking.setCheckOutDate(LocalDateTime.of(2025, 2, 24, 14, 0, 0));

        String sessionUrl = "https://stripe.com/session/123";
        CreatePaymentSessionDto createPaymentSessionDto = new CreatePaymentSessionDto();
        createPaymentSessionDto.setSessionUrl(sessionUrl);

        String sessionId = "session_123";
        Mockito.when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));
        Mockito.when(session.getId()).thenReturn(sessionId);
        Mockito.when(session.getUrl()).thenReturn(sessionUrl);
        Mockito.when(session.getExpiresAt()).thenReturn(System.currentTimeMillis() / 1000 + 3600);

        MockedStatic<Session> sessionMock = mockStatic(Session.class);
        sessionMock.when(() -> Session.create(any(SessionCreateParams.class)))
                .thenReturn(session);

        Payment payment = new Payment();
        Mockito.when(paymentRepository.save(any(Payment.class))).thenReturn(payment);
        Mockito.when(paymentMapper.toPaymentResponseDto(any(Payment.class)))
                .thenReturn(createPaymentSessionDto);

        CreatePaymentSessionDto expected = paymentService.createPaymentSession(bookingId);

        assertEquals(expected.getSessionUrl(), expected.getSessionUrl());

        sessionMock.close();
    }

    @Test
    @DisplayName("Processing a successful payment with a valid id")
    void processSuccessfulPayment_withValidSessionId_returnDto() throws StripeException {
        Booking booking = new Booking();
        booking.setId(1L);
        booking.setStatus(Booking.Status.PENDING);

        Payment payment = new Payment();
        payment.setId(1L);
        payment.setSessionId("session_id_111");
        payment.setStatus(Payment.PaymentStatus.PAID);
        payment.setBooking(booking);

        PaymentWithoutSessionDto expected = new PaymentWithoutSessionDto(
                booking.getId(),
                Payment.PaymentStatus.PAID,
                BigDecimal.valueOf(100));

        Mockito.when(paymentRepository.findBySessionId(anyString()))
                .thenReturn(Optional.of(payment));
        Mockito.doNothing().when(paymentRepository).updateStatus(payment.getId(),
                Payment.PaymentStatus.PAID);
        Mockito.doNothing().when(bookingRepository).updateStatus(booking.getId(),
                Booking.Status.CONFIRMED);
        Mockito.when(paymentMapper.toPaymentWithoutSessionDto(payment)).thenReturn(expected);

        PaymentWithoutSessionDto actual = paymentService.processSuccessfulPayment("session_id_111");

        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Processing a canceled payment with a valid id")
    void processCancelPayment_withValidSessionId_returnDto() {
        Payment payment = new Payment();
        payment.setId(1L);
        payment.setStatus(Payment.PaymentStatus.PAID);

        Booking booking = new Booking();
        booking.setId(1L);
        booking.setStatus(Booking.Status.PENDING);
        booking.setAccommodation(new Accommodation());
        booking.getAccommodation().setId(1L);
        payment.setBooking(booking);

        BookingResponseDto expected = new BookingResponseDto();
        expected.setId(1L);
        expected.setStatus(Booking.Status.PENDING.toString());
        expected.setAccommodationId(1L);

        String sessionId = "session_id_111";

        Mockito.when(paymentRepository.findBySessionId(sessionId)).thenReturn(Optional.of(payment));
        Mockito.when(bookingMapper.toDto(booking)).thenReturn(expected);

        BookingResponseDto actual = paymentService.processCancelPayment(sessionId);

        assertEquals(expected, actual);
    }

    @Test
    @DisplayName("Renew session with not expired status")
    void renewPaymentSession_withStatusNotExpired_throwException() {
        Long paymentId = 1L;

        Payment payment = new Payment();
        payment.setId(paymentId);
        payment.setStatus(Payment.PaymentStatus.PENDING);

        Mockito.when(paymentRepository.findById(paymentId)).thenReturn(Optional.of(payment));

        assertThatThrownBy(() -> paymentService.renewPaymentSession(paymentId))
                .isInstanceOf(PaymentStatusException.class)
                .hasMessageContaining("Payment is not expired.");
    }
}

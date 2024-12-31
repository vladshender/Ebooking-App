package com.example.ebooking.controller;

import com.example.ebooking.dto.booking.BookingResponseDto;
import com.example.ebooking.dto.payment.CreatePaymentSessionDto;
import com.example.ebooking.dto.payment.PaymentResponseDto;
import com.example.ebooking.dto.payment.PaymentWithoutSessionDto;
import com.example.ebooking.model.User;
import com.example.ebooking.service.payment.StripePaymentService;
import com.stripe.exception.StripeException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Payment management", description = "Endpoints for payment")
@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentController {
    public static final String USER_ROLES = "hasAuthority('ROLE_USER') "
            + "or hasAuthority('ROLE_GOLD_USER') "
            + "or hasAuthority('ROLE_PRIVILEGED_USER')";

    private final StripePaymentService paymentService;

    @Operation(summary = "Get all payment for user",
            description = "Get all payment for authentication user")
    @GetMapping("/user")
    @PreAuthorize(USER_ROLES)
    public List<PaymentResponseDto> getAllPaymentsForAuthUser(Authentication authentication,
                                                              Pageable pageable) {
        User user = (User) authentication.getPrincipal();
        return paymentService.getPaymentsForUser(user.getId(), pageable);
    }

    @Operation(summary = "Get all payment for admin",
            description = "Get all payment for admin")
    @GetMapping("/all")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public List<PaymentResponseDto> getAllPaymentsForAdmin(Pageable pageable) {
        return paymentService.getPaymentsForAdmin(pageable);
    }

    @Operation(summary = "Create session by booking id",
            description = "Create session by booking id")
    @PostMapping("/session")
    @PreAuthorize(USER_ROLES)
    public CreatePaymentSessionDto createPaymentSession(@RequestParam Long bookingId)
            throws StripeException {
        return paymentService.createPaymentSession(bookingId);
    }

    @GetMapping("/success")
    @PreAuthorize(USER_ROLES)
    public PaymentWithoutSessionDto handleSuccessPayment(@RequestParam String sessionId)
            throws StripeException {
        return paymentService.processSuccessfulPayment(sessionId);
    }

    @GetMapping("/cancel")
    @PreAuthorize(USER_ROLES)
    public BookingResponseDto handleCancelledPayment(@RequestParam String sessionId) {
        return paymentService.processCancelPayment(sessionId);
    }

    @Operation(summary = "Renew session by payment id",
            description = "Renew session by payment id")
    @PreAuthorize(USER_ROLES)
    @PostMapping("/{paymentId}/renew")
    public CreatePaymentSessionDto renewPaymentSession(@PathVariable Long paymentId)
            throws StripeException {
        return paymentService.renewPaymentSession(paymentId);
    }
}

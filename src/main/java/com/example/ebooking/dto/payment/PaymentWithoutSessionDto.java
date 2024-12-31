package com.example.ebooking.dto.payment;

import com.example.ebooking.model.Payment;
import java.math.BigDecimal;

public record PaymentWithoutSessionDto(Long bookingId,
                                       Payment.PaymentStatus status,
                                       BigDecimal amount) {
}

package com.example.ebooking.dto.payment;

import com.example.ebooking.model.Payment;
import java.math.BigDecimal;
import lombok.Data;

@Data
public class CreatePaymentSessionDto {
    private String sessionUrl;
    private BigDecimal amount;
    private Payment.PaymentStatus status;
}

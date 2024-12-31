package com.example.ebooking.dto.payment;

import java.math.BigDecimal;
import lombok.Data;

@Data
public class PaymentResponseDto {
    private Long id;
    private Long bookingId;
    private String sessionId;
    private String sessionUrl;
    private BigDecimal amount;
    private String status;
}

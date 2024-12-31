package com.example.ebooking.dto.booking;

import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class UpdateBookingStatusRequestDto {
    @Pattern(regexp = "PENDING|CONFIRMED|CANCELED|EXPIRED",
            message = "Type must be one of PENDING, CONFIRMED, CANCELED, EXPIRED")
    private String status;
}

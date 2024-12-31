package com.example.ebooking.dto.booking;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class BookingResponseDto {
    private Long id;
    private LocalDateTime checkInDate;
    private LocalDateTime checkOutDate;
    private Long accommodationId;
    private String userName;
    private String status;
}

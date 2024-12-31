package com.example.ebooking.dto.booking;

import com.example.ebooking.validation.checkinbeforecheckout.ValidCheckInAndCheckOut;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import lombok.Data;

@Data
@ValidCheckInAndCheckOut
public class BookingRequestDto {
    @NotNull
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy HH:mm:ss")
    private LocalDateTime checkInDate;

    @NotNull
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy HH:mm:ss")
    private LocalDateTime checkOutDate;

    @NotNull
    private Long accommodationId;
}

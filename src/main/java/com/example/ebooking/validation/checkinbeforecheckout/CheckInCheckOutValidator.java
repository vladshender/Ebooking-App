package com.example.ebooking.validation.checkinbeforecheckout;

import com.example.ebooking.dto.booking.BookingRequestDto;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.time.LocalDateTime;

public class CheckInCheckOutValidator implements ConstraintValidator<ValidCheckInAndCheckOut,
        BookingRequestDto> {

    @Override
    public boolean isValid(BookingRequestDto bookingRequestDto,
                           ConstraintValidatorContext context) {
        LocalDateTime checkInDate = bookingRequestDto.getCheckInDate();
        LocalDateTime checkOutDate = bookingRequestDto.getCheckOutDate();
        LocalDateTime now = LocalDateTime.now();

        if (checkInDate.isBefore(now)) {
            return false;
        }

        if (checkInDate.isAfter(checkOutDate)) {
            return false;
        }

        return true;
    }
}

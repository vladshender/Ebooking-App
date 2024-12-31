package com.example.ebooking.service.booking;

import com.example.ebooking.dto.booking.BookingFilterParameters;
import com.example.ebooking.dto.booking.BookingRequestDto;
import com.example.ebooking.dto.booking.BookingResponseDto;
import com.example.ebooking.dto.booking.UpdateBookingStatusRequestDto;
import com.example.ebooking.model.User;
import java.util.List;
import org.springframework.data.domain.Pageable;

public interface BookingService {
    BookingResponseDto save(User user, BookingRequestDto requestDto);

    List<BookingResponseDto> getAllBookingsByUser(User user, Pageable pageable);

    BookingResponseDto getBookingByIdForUser(User user, Long id);

    BookingResponseDto updateBookingByIdForAuthUser(User user,
                                                    BookingRequestDto requestDto,
                                                    Long id);

    void canceledById(User user, Long id);

    void deleteById(User user, Long id);

    List<BookingResponseDto> getAllBookingByUserIdAndStatus(
            BookingFilterParameters parameters, Pageable pageable);

    BookingResponseDto updateStatusById(UpdateBookingStatusRequestDto requestDto,
                                        Long id);

    void checkHourlyExpiredBookings();
}

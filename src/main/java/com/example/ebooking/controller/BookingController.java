package com.example.ebooking.controller;

import com.example.ebooking.dto.booking.BookingFilterParameters;
import com.example.ebooking.dto.booking.BookingRequestDto;
import com.example.ebooking.dto.booking.BookingResponseDto;
import com.example.ebooking.dto.booking.UpdateBookingStatusRequestDto;
import com.example.ebooking.model.User;
import com.example.ebooking.service.booking.BookingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Booking management", description = "Endpoints for booking")
@RestController
@RequiredArgsConstructor
@RequestMapping("/bookings")
public class BookingController {
    public static final String USER_ROLES = "hasAuthority('ROLE_USER') "
            + "or hasAuthority('ROLE_GOLD_USER') "
            + "or hasAuthority('ROLE_PRIVILEGED_USER')";

    private final BookingService bookingService;

    @Operation(summary = "Create new booking",
            description = "Create new booking for authentication user")
    @PreAuthorize(USER_ROLES)
    @PostMapping
    public BookingResponseDto save(
            Authentication authentication,
            @RequestBody @Valid BookingRequestDto requestDto) {
        User user = (User) authentication.getPrincipal();
        return bookingService.save(user, requestDto);
    }

    @Operation(summary = "Get all booking by user",
            description = "Get all booking by user for authentication user")
    @PreAuthorize(USER_ROLES)
    @GetMapping("/my")
    public List<BookingResponseDto> getAllBookingByAuthUser(
            Authentication authentication,
            Pageable pageable
    ) {
        User user = (User) authentication.getPrincipal();
        return bookingService.getAllBookingsByUser(user, pageable);
    }

    @Operation(summary = "Get booking by id for user",
            description = "Get booking by id for authentication user")
    @PreAuthorize(USER_ROLES)
    @GetMapping("/{id}")
    public BookingResponseDto getBookingByIdForAuthUser(Authentication authentication,
                                                        @PathVariable Long id) {
        User user = (User) authentication.getPrincipal();
        return bookingService.getBookingByIdForUser(user, id);
    }

    @Operation(summary = "Update booking by id for user",
            description = "Update booking by id for authentication user")
    @PreAuthorize(USER_ROLES)
    @PutMapping("/{id}")
    public BookingResponseDto updateBookingByIdForAuthUser(
            Authentication authentication,
            @RequestBody @Valid BookingRequestDto requestDto,
            @PathVariable Long id
    ) {
        User user = (User) authentication.getPrincipal();
        return bookingService.updateBookingByIdForAuthUser(user, requestDto, id);
    }

    @Operation(summary = "Canceled booking by id for user",
            description = "Canceled booking by id for authentication user")
    @PreAuthorize(USER_ROLES)
    @PutMapping("/{id}/cancel")
    @ResponseStatus(HttpStatus.OK)
    public void canceledBookingByIdForAuthUser(Authentication authentication,
                                               @PathVariable Long id) {
        User user = (User) authentication.getPrincipal();
        bookingService.canceledById(user, id);
    }

    @Operation(summary = "Delete booking by id for user",
            description = "Delete booking by id for authentication user")
    @PreAuthorize(USER_ROLES)
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteBookingByIdForAuthUser(Authentication authentication,
                                             @PathVariable Long id) {
        User user = (User) authentication.getPrincipal();
        bookingService.deleteById(user, id);
    }

    @Operation(summary = "Find booking by status or user id",
            description = "Find booking by status or user id for admin")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @GetMapping
    public List<BookingResponseDto> findBookingByUserIdAndStatus(
            BookingFilterParameters parameters,
            Pageable pageable
    ) {
        return bookingService.getAllBookingByUserIdAndStatus(parameters, pageable);
    }

    @Operation(summary = "Update booking`s status by id",
            description = "Update booking`s status by id for admin")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PutMapping("/status/{id}")
    public BookingResponseDto updateStatusById(
            @RequestBody @Valid UpdateBookingStatusRequestDto requestDto,
            @PathVariable Long id) {
        return bookingService.updateStatusById(requestDto, id);
    }
}

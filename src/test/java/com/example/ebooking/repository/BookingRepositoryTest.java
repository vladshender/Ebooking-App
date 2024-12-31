package com.example.ebooking.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.example.ebooking.exception.EntityNotFoundException;
import com.example.ebooking.model.Booking;
import com.example.ebooking.repository.booking.BookingRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class BookingRepositoryTest {
    @Autowired
    private BookingRepository bookingRepository;

    @Test
    @DisplayName("Updates status for bookings by list ids")
    @Sql(scripts = {
            "classpath:scripts/repository/booking/add-accommodation.sql",
            "classpath:scripts/repository/booking/add-three-booking.sql"},
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = {
            "classpath:scripts/repository/booking/delete-accommodation.sql",
            "classpath:scripts/repository/booking/delete-three-booking.sql"},
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void updateStatusForExpiredBooking_withValidId_updateFieldStatus() {
        Set<Long> bookingIds = Set.of(1L, 2L, 3L);
        Booking.Status status = Booking.Status.EXPIRED;
        bookingRepository.updateStatusForExpiredBooking(bookingIds, status);
        List<Booking> bookingList = bookingRepository.findAll();

        assertEquals(bookingList.get(0).getStatus(), status);
        assertEquals(bookingList.get(1).getStatus(), status);
        assertEquals(bookingList.get(2).getStatus(), status);
    }

    @Test
    @DisplayName("Updates status for booking by booking id")
    @Sql(scripts = {"classpath:scripts/repository/booking/add-accommodation.sql",
            "classpath:scripts/repository/booking/add-three-booking.sql"},
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = {
            "classpath:scripts/repository/booking/delete-accommodation.sql",
            "classpath:scripts/repository/booking/delete-three-booking.sql"},
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void updateStatus_withValidId_updateFieldStatus() {
        Long bookingId = 1L;
        Booking.Status status = Booking.Status.CONFIRMED;
        bookingRepository.updateStatus(bookingId, status);
        Booking bookingFromDB = bookingRepository.findById(bookingId)
                .orElseThrow(
                        () -> new EntityNotFoundException("Booking not found by id: "
                                + bookingId)
                );
        assertEquals(bookingFromDB.getStatus(), status);
    }

    @Test
    @DisplayName("Returns a bookings with a check-out time and no status specified")
    @Sql(scripts = {"classpath:scripts/repository/booking/add-accommodation.sql",
            "classpath:scripts/repository/booking/add-three-booking.sql"},
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = {
            "classpath:scripts/repository/booking/delete-accommodation.sql",
            "classpath:scripts/repository/booking/delete-three-booking.sql"},
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void findByCheckOutDateAndStatusNot_withValidDateAndStatus_returnBookings() {
        LocalDateTime date = LocalDateTime.of(2025, 2, 23, 14, 0, 0);
        Booking.Status status = Booking.Status.CANCELED;
        List<Booking> bookingList = bookingRepository.findByCheckOutDateAndStatusNot(date, status);
        for (Booking booking : bookingList) {
            assertEquals(booking.getCheckOutDate(), date);
            assertNotEquals(booking.getStatus(), status);
        }
    }

    @Test
    @DisplayName("Returns a bookings with a check-out time and no status specified")
    @Sql(scripts = {"classpath:scripts/repository/booking/add-accommodation.sql",
            "classpath:scripts/repository/booking/add-three-booking-status-canceled.sql"},
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = {
            "classpath:scripts/repository/booking/delete-accommodation.sql",
            "classpath:scripts/repository/booking/delete-three-booking.sql"},
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void findByCheckOutDateAndStatusNot_withValidDateAndStatus_returnEmptyList() {
        LocalDateTime date = LocalDateTime.of(2025, 2, 23, 14, 0, 0);
        Booking.Status status = Booking.Status.CANCELED;
        List<Booking> bookingList = bookingRepository.findByCheckOutDateAndStatusNot(date, status);
        assertTrue(bookingList.isEmpty());
    }
}

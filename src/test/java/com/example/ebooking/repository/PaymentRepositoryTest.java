package com.example.ebooking.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.example.ebooking.exception.EntityNotFoundException;
import com.example.ebooking.model.Payment;
import com.example.ebooking.repository.payment.PaymentRepository;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class PaymentRepositoryTest {
    @Autowired
    private PaymentRepository paymentRepository;

    @Test
    @DisplayName("Updates status for payment by payment id")
    @Sql(scripts = {"classpath:scripts/repository/payment/add-accommodation.sql",
                    "classpath:scripts/repository/payment/add-booking.sql",
                    "classpath:scripts/repository/payment/add-payment.sql"},
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = {"classpath:scripts/repository/payment/delete-accommodation.sql",
                    "classpath:scripts/repository/payment/delete-booking.sql",
                    "classpath:scripts/repository/payment/delete-payment.sql"},
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void updateStatus_withValidId_updateFieldStatus() {
        Long paymentId = 1L;
        Payment.PaymentStatus status = Payment.PaymentStatus.PAID;
        paymentRepository.updateStatus(paymentId, status);
        Payment paymentFromDB = paymentRepository.findById(paymentId)
                .orElseThrow(
                        () -> new EntityNotFoundException("Payment not found by id:"
                                + paymentId)
                );
        assertEquals(paymentFromDB.getStatus(), status);
    }

    @Test
    @DisplayName("Returns true because the payment with user and status exists")
    @Sql(scripts = {"classpath:scripts/repository/payment/add-accommodation.sql",
            "classpath:scripts/repository/payment/add-booking.sql",
            "classpath:scripts/repository/payment/add-payment.sql"},
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = {"classpath:scripts/repository/payment/delete-accommodation.sql",
            "classpath:scripts/repository/payment/delete-booking.sql",
            "classpath:scripts/repository/payment/delete-payment.sql"},
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void existsByBookingUserIdAndStatus_withValidUserIdAndStatus_returnTrue() {
        Long userId = 1L;
        Payment.PaymentStatus status = Payment.PaymentStatus.PENDING;
        boolean isExist = paymentRepository.existsByBookingUserIdAndStatus(userId, status);
        assertTrue(isExist);
    }

    @Test
    @DisplayName("Returns false because the payment with user and status not exists")
    @Sql(scripts = {"classpath:scripts/repository/payment/add-accommodation.sql",
            "classpath:scripts/repository/payment/add-booking.sql",
            "classpath:scripts/repository/payment/add-payment.sql"},
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = {"classpath:scripts/repository/payment/delete-accommodation.sql",
            "classpath:scripts/repository/payment/delete-booking.sql",
            "classpath:scripts/repository/payment/delete-payment.sql"},
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void existsByBookingUserIdAndStatus_withValidUserIdAndStatus_returnFalse() {
        Long userId = 1L;
        Payment.PaymentStatus status = Payment.PaymentStatus.EXPIRED;
        boolean isExist = paymentRepository.existsByBookingUserIdAndStatus(userId, status);
        assertFalse(isExist);
    }

    @Test
    @DisplayName("update status for session timed out payments")
    @Sql(scripts = {"classpath:scripts/repository/payment/add-accommodation.sql",
            "classpath:scripts/repository/payment/add-booking.sql",
            "classpath:scripts/repository/payment/add-two-payment.sql"},
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(scripts = {"classpath:scripts/repository/payment/delete-accommodation.sql",
            "classpath:scripts/repository/payment/delete-booking.sql",
            "classpath:scripts/repository/payment/delete-payment.sql"},
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    void updateExpiredPayments_withValidInputData_updateFieldInDB() {
        LocalDateTime date = LocalDateTime.of(2025, 01, 22, 14, 01, 0);
        Long time = date.toEpochSecond(ZoneOffset.UTC);
        Payment.PaymentStatus status = Payment.PaymentStatus.EXPIRED;
        Payment.PaymentStatus pendingStatus = Payment.PaymentStatus.PENDING;

        Integer beforeSize = paymentRepository.findAllByStatus(status).size();
        paymentRepository.updateExpiredPayments(time, status, pendingStatus);
        Integer afterSize = paymentRepository.findAllByStatus(status).size();

        assertTrue(beforeSize < afterSize);
    }
}

package com.example.ebooking.repository.payment;

import com.example.ebooking.model.Payment;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findBySessionId(String sessionId);

    List<Payment> findAllByStatus(Payment.PaymentStatus status);

    @Transactional
    @Modifying
    @Query("UPDATE Payment p SET p.status = :status WHERE p.id IN :paymentId")
    void updateStatus(@Param("paymentId") Long paymentId,
                              @Param("status") Payment.PaymentStatus status);

    @Modifying
    @Query("UPDATE Payment p SET p.status = :status WHERE p.status = :pendingStatus "
            + "AND p.expiredTime < :currentTime")
    void updateExpiredPayments(@Param("currentTime") Long currentTime,
                               @Param("status") Payment.PaymentStatus status,
                               @Param("pendingStatus") Payment.PaymentStatus pendingStatus);

    Page<Payment> findByBookingUserId(Long userId, Pageable pageable);

    boolean existsByBookingUserIdAndStatus(Long userId,
                                           Payment.PaymentStatus status);
}

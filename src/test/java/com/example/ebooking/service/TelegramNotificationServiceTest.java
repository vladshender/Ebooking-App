package com.example.ebooking.service;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.example.ebooking.bot.NotificationTelegramBot;
import com.example.ebooking.model.Accommodation;
import com.example.ebooking.model.Booking;
import com.example.ebooking.model.Payment;
import com.example.ebooking.model.User;
import com.example.ebooking.service.notification.TelegramNotificationService;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class TelegramNotificationServiceTest {
    @InjectMocks
    private TelegramNotificationService notificationService;

    @Mock
    private NotificationTelegramBot telegramBot;

    @Test
    @DisplayName("Send notification when booking created")
    void sendBookingCreateMessage_withValidInputData_sendMessage() {
        Accommodation accommodation = new Accommodation();
        accommodation.setId(1L);
        accommodation.setType(Accommodation.Type.CONDO);

        User user = new User();
        user.setId(1L);
        user.setFirstName("John");
        user.setLastName("Doe");

        Booking booking = new Booking();
        booking.setId(1L);
        booking.setCheckInDate(LocalDateTime.of(2024, 12, 30, 14, 0));
        booking.setCheckOutDate(LocalDateTime.of(2025, 1, 2, 11, 0));
        booking.setStatus(Booking.Status.PENDING);

        notificationService.sendBookingCreateMessage(accommodation, user, booking);

        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
        verify(telegramBot).sendNotification(messageCaptor.capture());

        String message = messageCaptor.getValue();
        assertTrue(message.contains("#BOOKING_CREATED"));
        assertTrue(message.contains("John Doe"));
        assertTrue(message.contains("CONDO"));
        assertTrue(message.contains("PENDING"));
    }

    @Test
    @DisplayName("Send notification when booking canceled")
    void sendBookingCanceledMessage_withValidInputData_sendMessage() {
        Accommodation accommodation = new Accommodation();
        accommodation.setId(1L);
        accommodation.setType(Accommodation.Type.HOUSE);

        User user = new User();
        user.setId(1L);
        user.setFirstName("John");
        user.setLastName("Doe");

        Booking booking = new Booking();
        booking.setId(1L);
        booking.setCheckInDate(LocalDateTime.of(2024, 12, 30, 14, 0));
        booking.setCheckOutDate(LocalDateTime.of(2025, 1, 2, 11, 0));
        booking.setStatus(Booking.Status.CANCELED);

        notificationService.sendBookingCanceledMessage(accommodation, user, booking);

        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
        verify(telegramBot).sendNotification(messageCaptor.capture());

        String message = messageCaptor.getValue();
        assertTrue(message.contains("#BOOKING_CANCELED"));
        assertTrue(message.contains("John Doe"));
        assertTrue(message.contains("HOUSE"));
        assertTrue(message.contains("CANCELED"));
    }

    @Test
    @DisplayName("Send notification when new accommodation created")
    void sendAccommodationCreateMessage_withValidInputData_sendMessage() {
        Accommodation accommodation = new Accommodation();
        accommodation.setId(1L);
        accommodation.setType(Accommodation.Type.HOUSE);
        accommodation.setLocation("Kyiv");
        accommodation.setSize("55m");
        accommodation.setAmenities(Set.of(Accommodation.Amenities.WiFi));
        accommodation.setDailyRate(BigDecimal.valueOf(120));
        accommodation.setAvailability(2);

        notificationService.sendAccommodationCreateMessage(accommodation);

        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
        verify(telegramBot).sendNotification(messageCaptor.capture());

        String message = messageCaptor.getValue();
        assertTrue(message.contains("#ACCOMMODATION_CREATED"));
        assertTrue(message.contains("Kyiv"));
        assertTrue(message.contains("55m"));
        assertTrue(message.contains("120"));
    }

    @Test
    @DisplayName("Send notification when accommodation released")
    void sendAccommodationReleaseMessage_withValidInputData_sendMessage() {
        Accommodation accommodation = new Accommodation();
        accommodation.setId(1L);
        accommodation.setType(Accommodation.Type.APARTMENT);
        accommodation.setSize("55m");
        accommodation.setLocation("Lviv");
        accommodation.setDailyRate(BigDecimal.valueOf(70));
        accommodation.setAvailability(2);

        Map<Accommodation, Long> expiringBookingsByAccommodation
                = new HashMap<Accommodation, Long>();
        List<Integer> amountOfAvailability = List.of(1);
        expiringBookingsByAccommodation.put(accommodation, 1L);

        notificationService.sendAccommodationReleaseMessage(
                expiringBookingsByAccommodation,
                amountOfAvailability
        );

        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
        verify(telegramBot, times(2))
                .sendNotification(messageCaptor.capture());

        List<String> capturedMessages = messageCaptor.getAllValues();
        assertTrue(capturedMessages.get(0).contains("#hourly_check"));

        String secondMessage = capturedMessages.get(1);
        assertTrue(secondMessage.contains("APARTMENT"));
        assertTrue(secondMessage.contains("id:  1"));
        assertTrue(secondMessage.contains("55m"));
        assertTrue(secondMessage.contains("Lviv"));
        assertTrue(secondMessage.contains("2"));
        assertTrue(secondMessage.contains("70"));
    }

    @Test
    @DisplayName("Send notification when payment success")
    void sendPaymentSuccessMessage_withValidInputData_sendMessage() {
        Payment payment = new Payment();
        payment.setId(1L);
        payment.setBooking(new Booking());
        payment.getBooking().setId(2L);
        payment.setStatus(Payment.PaymentStatus.PAID);
        payment.setAmount(BigDecimal.valueOf(150));

        notificationService.sendPaymentSuccessMessage(payment);

        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);
        verify(telegramBot).sendNotification(messageCaptor.capture());

        String message = messageCaptor.getValue();
        assertTrue(message.contains("#PAYMENT_CREATED"));
        assertTrue(message.contains("2"));
        assertTrue(message.contains("PAID"));
        assertTrue(message.contains("150"));
    }
}

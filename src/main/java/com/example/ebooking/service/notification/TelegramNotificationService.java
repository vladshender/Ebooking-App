package com.example.ebooking.service.notification;

import com.example.ebooking.bot.NotificationTelegramBot;
import com.example.ebooking.model.Accommodation;
import com.example.ebooking.model.Booking;
import com.example.ebooking.model.Payment;
import com.example.ebooking.model.User;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TelegramNotificationService implements NotificationService {
    private final NotificationTelegramBot telegramBot;

    private DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public void sendBookingCreateMessage(Accommodation accommodation, User user,
                                         Booking booking) {
        LocalDateTime now = LocalDateTime.now();

        String message = String.format(
                "Type notification: #BOOKING_CREATED%n"
                        + "Creation time: %s%n"
                        + "Booking detail: %n"
                        + "%s"
                        + "Reservation owner: %n"
                        + "           id:  %s%n"
                        + "           name: %s%n",
                now.format(formatter),
                getDetailBooking(booking, accommodation),
                user.getId(),
                getUserName(user)
        );

        telegramBot.sendNotification(message);
    }

    @Override
    public void sendBookingCanceledMessage(Accommodation accommodation, User user,
                                           Booking booking) {
        LocalDateTime now = LocalDateTime.now();

        String message = String.format(
                "Type notification: #BOOKING_CANCELED%n"
                        + "Creation time: %s%n"
                        + "Booking detail: %n"
                        + "%s"
                        + "Reservation owner: %n"
                        + "           id:  %s%n"
                        + "           name: %s%n",
                now.format(formatter),
                getDetailBooking(booking, accommodation),
                user.getId(),
                getUserName(user)
        );
        telegramBot.sendNotification(message);
    }

    @Override
    public void sendAccommodationCreateMessage(Accommodation accommodation) {
        LocalDateTime now = LocalDateTime.now();

        String message = String.format(
                "Type notification: #ACCOMMODATION_CREATED%n"
                        + "Creation time: %s%n"
                        + "Accommodation detail: %n"
                        + "           id:  %s%n"
                        + "           type: %s%n"
                        + "           location: %s%n"
                        + "           size: %s%n"
                        + "           daily rate: %s%n"
                        + "           avalaibility: %s%n",
                now.format(formatter),
                accommodation.getId(),
                accommodation.getType(),
                accommodation.getLocation(),
                accommodation.getSize(),
                accommodation.getDailyRate(),
                accommodation.getAvailability()
        );

        telegramBot.sendNotification(message);
    }

    @Override
    public void sendAccommodationReleaseMessage(
            Map<Accommodation,
            Long> expiringBookingsByAccommodation,
            List<Integer> amountOfAvailability
    ) {
        LocalTime time = LocalTime.now().withNano(0);
        telegramBot.sendNotification("#hourly_check\n "
                + "Bookings will end at " + time.toString());

        Iterator<Map.Entry<Accommodation, Long>> accommodationIterator =
                expiringBookingsByAccommodation.entrySet().iterator();
        Iterator<Integer> availabilityIterator = amountOfAvailability.iterator();

        while (accommodationIterator.hasNext() && availabilityIterator.hasNext()) {
            Map.Entry<Accommodation, Long> entry = accommodationIterator.next();
            Accommodation accommodation = entry.getKey();
            Long bookingsExpiredCount = entry.getValue();
            Integer currentAvailability = availabilityIterator.next();

            String message = String.format(
                            "Accommodation detail: %n"
                            + "           id:  %s%n"
                            + "           type: %s%n"
                            + "           location: %s%n"
                            + "           size: %s%n"
                            + "           daily rate: %s%n"
                            + "           avalaibility: %s%n"
                            + "The number that becomes free at that hour: %s%n"
                            + "Available to book now: %s%n",
                    accommodation.getId(),
                    accommodation.getType(),
                    accommodation.getLocation(),
                    accommodation.getSize(),
                    accommodation.getDailyRate(),
                    accommodation.getAvailability(),
                    bookingsExpiredCount,
                    currentAvailability
            );
            telegramBot.sendNotification(message);
        }
    }

    @Override
    public void sendPaymentSuccessMessage(Payment payment) {
        LocalTime time = LocalTime.now().withNano(0);
        String message = String.format(
                "Type notification: #PAYMENT_CREATED%n"
                + "The payment was successful!%n"
                        + "Payment detail: %n"
                        + "           id:  %s%n"
                        + "           bookingId:  %s%n"
                        + "           status: %s%n"
                        + "           amount: %s%n",
                payment.getId(),
                payment.getBooking().getId(),
                payment.getStatus().toString(),
                payment.getAmount()
        );
        telegramBot.sendNotification(message);
    }

    private String getUserName(User user) {
        return user.getFirstName() + " " + user.getLastName();
    }

    private String getDetailBooking(Booking booking, Accommodation accommodation) {
        String message = String.format("Booking create with id: %s%n"
                + "           Check in date: %s%n"
                + "           Check out date: %s%n"
                + "           Accommodation: %s, %s%n"
                + "           Status: %s%n",
                booking.getId(),
                booking.getCheckInDate().format(formatter),
                booking.getCheckOutDate().format(formatter),
                accommodation.getId(),
                accommodation.getType().toString(),
                booking.getStatus().toString()
        );
        return message;
    }
}

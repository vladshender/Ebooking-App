package com.example.ebooking.service.notification;

import com.example.ebooking.model.Accommodation;
import com.example.ebooking.model.Booking;
import com.example.ebooking.model.Payment;
import com.example.ebooking.model.User;
import java.util.List;
import java.util.Map;

public interface NotificationService {

    void sendBookingCreateMessage(Accommodation accommodation, User user,
                                  Booking booking);

    void sendBookingCanceledMessage(Accommodation accommodation, User user,
                                    Booking booking);

    void sendAccommodationCreateMessage(Accommodation accommodation);

    void sendAccommodationReleaseMessage(Map<Accommodation, Long> expiringBookingsByAccommodation,
                                         List<Integer> amountOfAvailability);

    void sendPaymentSuccessMessage(Payment payment);
}

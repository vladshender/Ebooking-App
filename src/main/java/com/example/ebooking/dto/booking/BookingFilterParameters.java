package com.example.ebooking.dto.booking;

public record BookingFilterParameters(String[] statusArray, String[] userIdArray) {
    public BookingFilterParameters {
        statusArray = statusArray != null ? statusArray : new String[0];
        userIdArray = userIdArray != null ? userIdArray : new String[0];
    }
}

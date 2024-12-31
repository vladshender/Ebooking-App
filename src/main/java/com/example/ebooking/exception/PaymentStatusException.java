package com.example.ebooking.exception;

public class PaymentStatusException extends RuntimeException {
    public PaymentStatusException(String message) {
        super(message);
    }
}

package com.example.ebooking.exception;

public class PendingPaymentException extends RuntimeException {
    public PendingPaymentException(String message) {
        super(message);
    }
}

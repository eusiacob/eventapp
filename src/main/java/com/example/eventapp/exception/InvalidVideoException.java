package com.example.eventapp.exception;

public class InvalidVideoException extends RuntimeException {

    public InvalidVideoException(String message) {
        super(message);
    }
}
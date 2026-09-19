package com.example.eventapp.service;

/** Only notification details; never include the password or reset token. */
public record PasswordResetCompletedEvent(String recipient, String firstName) {
}

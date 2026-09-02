package com.example.eventapp.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendPasswordResetEmail(
            String recipient,
            String resetLink
    ) {

        SimpleMailMessage message =
                new SimpleMailMessage();

        message.setTo(recipient);

        message.setSubject(
                "Resetarea parolei - M-Event"
        );

        message.setText(
                "Salutare,\n\n" +
                        "Ai solicitat resetarea parolei pentru contul tău M-Event.\n\n" +
                        "Pentru a seta o parolă nouă, accesează următorul link:\n\n" +
                        resetLink +
                        "\n\n" +
                        "Linkul este valabil timp de 15 minute.\n\n" +
                        "Dacă nu ai solicitat resetarea parolei, " +
                        "poți ignora acest mesaj.\n\n" +
                        "Echipa M-Event"
        );

        mailSender.send(message);
    }
}
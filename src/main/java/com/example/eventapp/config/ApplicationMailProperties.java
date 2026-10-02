package com.example.eventapp.config;

import lombok.Getter;
import lombok.Setter;
import jakarta.mail.internet.InternetAddress;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.mail")
public class ApplicationMailProperties {

    private String fromAddress = "notificari@m-event.ro";
    private String fromName = "M-Event";
    private String replyToAddress = "contact@m-event.ro";
    private String contactAddress = "contact@m-event.ro";
    private String partnersAddress = "parteneri@m-event.ro";
    private SentCopy sentCopy = new SentCopy();

    @Getter
    @Setter
    public static class SentCopy {
        private boolean enabled = false;
        private String host = "";
        private int port = 993;
        private String username = "";
        private String password = "";
        private String folder = "";
        private int timeoutMs = 5000;
    }

    public String getFormattedFromAddress() {
        try {
            return new InternetAddress(fromAddress, fromName, "UTF-8")
                    .toUnicodeString();
        } catch (java.io.UnsupportedEncodingException exception) {
            throw new IllegalStateException("Adresa expeditorului nu este validă.", exception);
        }
    }
}

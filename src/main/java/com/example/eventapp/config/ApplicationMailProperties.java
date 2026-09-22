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

    public String getFormattedFromAddress() {
        try {
            return new InternetAddress(fromAddress, fromName, "UTF-8")
                    .toUnicodeString();
        } catch (java.io.UnsupportedEncodingException exception) {
            throw new IllegalStateException("Adresa expeditorului nu este validă.", exception);
        }
    }
}

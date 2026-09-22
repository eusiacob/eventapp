package com.example.eventapp.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.mail")
public class ApplicationMailProperties {

    private String fromAddress = "notificari@m-event.ro";
    private String replyToAddress = "contact@m-event.ro";
    private String contactAddress = "contact@m-event.ro";
    private String partnersAddress = "parteneri@m-event.ro";
}

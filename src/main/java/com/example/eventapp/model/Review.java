package com.example.eventapp.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
@Entity
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Min(value = 1, message = "Evaluarea trebuie să fie cel putin 1")
    @Max(value = 5, message = "Evaluarea trebuie să fie cel mult 5")
    private int rating;

    @NotBlank(message = "Recenzia este obligatorie!")
    @Column(length = 1000)
    private String comment;

    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    @ManyToOne
    @JoinColumn(name = "business_id")
    private BusinessProfile businessProfile;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    public Review() {
    }

}
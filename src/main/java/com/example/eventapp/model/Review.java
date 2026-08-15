package com.example.eventapp.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
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

    @NotBlank(message = "Recenzia nu poate fi goală.")
    @Size(max = 1000, message = "Recenzia poate avea maximum 1000 de caractere.")
    @Column(length = 1000)
    private String comment;

    private LocalDateTime createdAt = LocalDateTime.now();

    @Getter
    public enum ReviewStatus {
        PENDING ("În așteptare"),
        APPROVED("Aprobat"),
        REJECTED("Refuzat");

        private final String reviewStatusName;

        ReviewStatus(String reviewStatusName) {
            this.reviewStatusName = reviewStatusName;
        }
    }

    @Column(length = 500)
    private String rejectionReason;

    @Enumerated(EnumType.STRING)
    public ReviewStatus reviewStatus = ReviewStatus.PENDING;

    @ManyToOne
    @JoinColumn(name = "business_id")
    private BusinessProfile businessProfile;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    public Review() {
    }

}
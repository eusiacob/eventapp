package com.example.eventapp.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Entity
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating must be at most 5")
    private int rating;

    @NotBlank(message = "Comment is required")
    @Column(length = 1000)
    private String comment;

    @ManyToOne
    @JoinColumn(name = "business_id")
    private BusinessProfile businessProfile;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    public Review() {
    }

    public Review(Long id, int rating, String comment, BusinessProfile businessProfile, User user) {
        this.id = id;
        this.rating = rating;
        this.comment = comment;
        this.businessProfile = businessProfile;
        this.user = user;
    }

}
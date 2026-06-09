package com.example.eventapp.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BusinessProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Name is required")
    @Size(min = 3, max = 50, message = "Name must be between 3 and 50 characters")
    private String name;

    @Enumerated(EnumType.STRING)
    private BusinessCategory category;

    @NotBlank(message = "Description is required")
    @Size(min = 10, max = 700, message = "Description must be at least 10 characters")
    private String description;

    @NotBlank(message = "City is required")
    private String city;

    @NotBlank(message = "Phone is required")
    @Pattern(regexp = "^[0-9+\\- ]{10}$", message = "Invalid phone number! It should be 07X XXX XXX")
    private String phone;

    @Email(message = "Introdu un mail valid!")
    private String email;

    private String website;

    @OneToMany(mappedBy = "businessProfile", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BusinessUnavailableDate> unavailableDates = new ArrayList<>();

    private String imagePath;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToMany(mappedBy = "favoriteBusinesses")
    Set<User> favorites;

    @OneToMany(mappedBy = "businessProfile", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Review> reviews = new ArrayList<>();

    @OneToMany(mappedBy = "businessProfile", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BusinessImage> galleryImages = new ArrayList<>();

    public int getReviewCount() {
        if (reviews == null) {
            return 0;
        }

        return reviews.size();
    }

    public double getAverageRating() {
        if (reviews == null || reviews.isEmpty()) {
            return 0.0;
        }

        return reviews.stream()
                .mapToInt(Review::getRating)
                .average()
                .orElse(0.0);
    }
}

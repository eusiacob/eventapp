package com.example.eventapp.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BusinessProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, updatable = false)
    private String uuid;

    @Column(unique = true, nullable = false)
    private String slug;

    @NotBlank(message = "Numele este obligatoriu!")
    @Size(min = 3, max = 50, message = "Lungimea trebuie să fie între 3 și 10 caractere.")
    private String name;

    @Enumerated(EnumType.STRING)
    private BusinessCategory category;

    @NotBlank(message = "Descrierea este obligatorie!")
    @Size(min = 10, max = 700, message = "Descrierea trebuie să fie de minim 10 caractere.")
    private String description;

    @NotBlank(message = "Județul este obligatoriu")
    private String city;

    @NotBlank(message = "Introdu numărul de telefon!")
    @Pattern(regexp = "^[0-9+\\- ]{10}$", message = "Număr de telefon invalid! Trebuie să fie de forma 07X XXX XXX")
    private String phone;

    @Email(message = "Introdu un mail valid!")
    private String email;

    private String website;

    @OneToMany(mappedBy = "businessProfile", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BusinessUnavailableDate> unavailableDates = new ArrayList<>();

    private String imagePath;

    private boolean premium;

    @Column(nullable = false)
    private boolean active = true;

    @Getter
    public enum BusinessStatus {
        PENDING ("În așteptare"),
        APPROVED ("Aprobat"),
        REJECTED ("Respins");

        private final String statusDisplayName;

        BusinessStatus (String statusDisplayName) {
            this.statusDisplayName = statusDisplayName;
        }
    }

    @Column(length = 1000)
    private String rejectionReason;

    @Enumerated(EnumType.STRING)
    private BusinessStatus status = BusinessStatus.PENDING;

    private LocalDate createdAt;

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

        return Math.toIntExact(reviews.stream()
                .filter(r -> r.getReviewStatus() == Review.ReviewStatus.APPROVED)
                .count());
    }

    public double getAverageRating() {

        if (reviews == null) {
            return 0;
        }

        return reviews.stream()
                .filter(r -> r.getReviewStatus() == Review.ReviewStatus.APPROVED)
                .mapToInt(Review::getRating)
                .average()
                .orElse(0);
    }

    @PrePersist
    public void generateIdentifiers() {

        if (uuid == null) {
            uuid = UUID.randomUUID()
                    .toString();
        }

        if (slug == null && name != null) {
            slug = name
                    .toLowerCase()
                    .trim()
                    .replaceAll("[^a-z0-9]+", "-")
                    .replaceAll("^-|-$", "");
        }
    }
}

package com.example.eventapp.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.URL;

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

    // Retained for compatibility with existing single-county records.
    private String city;

    @Column(nullable = false)
    private boolean nationwide;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "business_service_counties",
            joinColumns = @JoinColumn(name = "business_profile_id"))
    @OrderColumn(name = "county_order")
    @Column(name = "county", nullable = false, length = 50)
    @org.hibernate.annotations.BatchSize(size = 50)
    @Size(max = 10, message = "Poți selecta maximum 10 județe.")
    private List<String> serviceCounties = new ArrayList<>();

    public List<String> getServiceCounties() {
        if (nationwide) return List.of();
        if (serviceCounties != null && !serviceCounties.isEmpty()) return serviceCounties;
        return city == null || city.isBlank() ? List.of() : List.of(city);
    }

    @Transient
    @AssertTrue(message = "Alege între 1 și 10 județe valide sau opțiunea Toată țara.")
    public boolean isServiceAreaValid() {
        if (nationwide) return true;
        List<String> counties = getServiceCounties();
        return !counties.isEmpty() && counties.size() <= 10
                && counties.stream().allMatch(county -> county != null && RomanianCounties.ALL.contains(county))
                && counties.stream().distinct().count() == counties.size();
    }

    public void normalizeServiceArea() {
        if (!isServiceAreaValid()) {
            throw new IllegalArgumentException("Alege între 1 și 10 județe sau Toată țara.");
        }
        List<String> selected = new ArrayList<>(getServiceCounties());
        if (serviceCounties == null) serviceCounties = new ArrayList<>();
        serviceCounties.clear();
        serviceCounties.addAll(selected);
        city = nationwide ? "Toată țara" : selected.get(0);
    }

    public String getCity() {
        if (nationwide) return "Toată țara";
        List<String> counties = getServiceCounties();
        if (counties.size() <= 2) return String.join(", ", counties);
        return String.join(", ", counties.subList(0, 2)) + " +" + (counties.size() - 2);
    }

    @Pattern(regexp = "^[0-9+\\- ]{10}$", message = "Număr de telefon invalid! Trebuie să fie de forma 07XXXXXXX")
    @Column(nullable = true)
    private String phone;

    @Email(
            regexp = "[a-z0-9._%+-]+@[a-z0-9.-]+\\.[a-z]{2,63}",
            flags = Pattern.Flag.CASE_INSENSITIVE,
            message = "Format incorect! Introdu o adresă de email validă."
    )
    @Column(nullable = true)
    private String email;

    @Column(nullable = false)
    private boolean emailVerified = false;

    @Column
    private java.time.LocalDateTime emailVerifiedAt;

    @URL(
            regexp = "^(http:\\/\\/|https:\\/\\/)?(www.)?([a-zA-Z0-9]+).[a-zA-Z0-9]*.[a-z]{3}.?([a-z]+)?$",
            message = "Format URL incorect. Trebuie să înceapă cu http:// sau https://"
    )
    @Column(nullable = true)
    private String website;

    @Transient
    @AssertTrue(message = "Completează cel puțin o metodă de contact: telefon, email sau website.")
    public boolean isContactMethodProvided() {
        return hasText(phone) || hasText(email) || hasText(website);
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    @OneToMany(mappedBy = "businessProfile", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BusinessUnavailableDate> unavailableDates = new ArrayList<>();

    private String imagePath;

    private boolean premium;

    @Column(nullable = false)
    private boolean active = true;

    @Getter
    public enum BusinessStatus {
        PENDING("În așteptare"),
        APPROVED("Aprobat"),
        REJECTED("Respins");

        private final String statusDisplayName;

        BusinessStatus(String statusDisplayName) {
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

    @OneToMany(mappedBy = "businessProfile",
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    private List<Review> reviews =
            new ArrayList<>();

    @OneToMany(mappedBy = "businessProfile",
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    private List<BusinessImage> galleryImages =
            new ArrayList<>();

    @OneToMany(mappedBy = "businessProfile",
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    private List<BusinessVideo> galleryVideos =
            new ArrayList<>();

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

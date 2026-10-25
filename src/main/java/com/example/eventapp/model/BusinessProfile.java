package com.example.eventapp.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.URL;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
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
    @Size(min = 3, max = 50, message = "Lungimea trebuie să fie între 3 și 50 de caractere.")
    private String name;

    @Enumerated(EnumType.STRING)
    private BusinessCategory category;

    @ElementCollection
    @CollectionTable(name = "business_service_types", joinColumns = @JoinColumn(name = "business_profile_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "service_type", nullable = false, length = 100)
    private Set<BusinessServiceType> serviceTypes = new java.util.LinkedHashSet<>();

    @ElementCollection
    @CollectionTable(name = "business_event_types", joinColumns = @JoinColumn(name = "business_profile_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 50)
    private Set<BusinessEventType> eventTypes = new java.util.LinkedHashSet<>();

    @Size(max = 150, message = "Descrierea altui serviciu poate avea maximum 150 de caractere.")
    @Column(length = 150)
    private String otherServiceDetails;

    @Transient
    @AssertTrue(message = "Selectează doar tipuri de servicii din categoria aleasă.")
    public boolean isServiceTypesValid() {
        return serviceTypes != null && serviceTypes.stream()
                .allMatch(type -> type != null && type.getCategory() == category);
    }

    @Transient
    @AssertTrue(message = "Descrie pe scurt serviciul oferit când alegi Alt serviciu.")
    public boolean isOtherServiceDetailsValid() {
        return serviceTypes == null || serviceTypes.stream()
                .noneMatch(type -> type != null && type.isOther()) || hasText(otherServiceDetails);
    }

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

    @Pattern(regexp = "^[0-9() .\\-]{0,40}$", message = "Numărul poate conține doar cifre, spații și semne uzuale de formatare.")
    @Column(nullable = true)
    private String phone;

    private static final Set<String> SUPPORTED_PHONE_COUNTRY_CODES = Set.of(
            "+40", "+1", "+44", "+33", "+34", "+36", "+39", "+49", "+30", "+31",
            "+32", "+351", "+352", "+353", "+354", "+355", "+356", "+357", "+358",
            "+359", "+370", "+371", "+372", "+373", "+374", "+375", "+376", "+377",
            "+378", "+380", "+381", "+382", "+385", "+386", "+387", "+389", "+420",
            "+421", "+423", "+7", "+90", "+971"
    );

    /** Null means this is an existing record created before country prefixes were supported. */
    @Column(name = "phone_country_code", length = 4)
    private String phoneCountryCode;

    public String getPhoneCountryCode() {
        return phoneCountryCode == null || phoneCountryCode.isBlank() ? "+40" : phoneCountryCode;
    }

    @Transient
    public String getPhoneCountryName() {
        return switch (getPhoneCountryCode()) {
            case "+40" -> "România";
            case "+1" -> "Statele Unite / Canada";
            case "+44" -> "Regatul Unit";
            case "+33" -> "Franța";
            case "+34" -> "Spania";
            case "+36" -> "Ungaria";
            case "+39" -> "Italia";
            case "+49" -> "Germania";
            case "+30" -> "Grecia";
            case "+31" -> "Țările de Jos";
            case "+32" -> "Belgia";
            case "+351" -> "Portugalia";
            case "+352" -> "Luxemburg";
            case "+353" -> "Irlanda";
            case "+354" -> "Islanda";
            case "+355" -> "Albania";
            case "+356" -> "Malta";
            case "+357" -> "Cipru";
            case "+358" -> "Finlanda";
            case "+359" -> "Bulgaria";
            case "+370" -> "Lituania";
            case "+371" -> "Letonia";
            case "+372" -> "Estonia";
            case "+373" -> "Republica Moldova";
            case "+374" -> "Armenia";
            case "+375" -> "Belarus";
            case "+376" -> "Andorra";
            case "+377" -> "Monaco";
            case "+378" -> "San Marino";
            case "+380" -> "Ucraina";
            case "+381" -> "Serbia";
            case "+382" -> "Muntenegru";
            case "+385" -> "Croația";
            case "+386" -> "Slovenia";
            case "+387" -> "Bosnia și Herțegovina";
            case "+389" -> "Macedonia de Nord";
            case "+420" -> "Cehia";
            case "+421" -> "Slovacia";
            case "+423" -> "Liechtenstein";
            case "+7" -> "Rusia / Kazahstan";
            case "+90" -> "Turcia";
            case "+971" -> "Emiratele Arabe Unite";
            default -> "Altă țară";
        };
    }

    @Transient
    public String getPhoneCountryFlag() {
        return switch (getPhoneCountryCode()) {
            case "+40" -> "🇷🇴";
            case "+1" -> "🇺🇸 🇨🇦";
            case "+44" -> "🇬🇧";
            case "+33" -> "🇫🇷";
            case "+34" -> "🇪🇸";
            case "+36" -> "🇭🇺";
            case "+39" -> "🇮🇹";
            case "+49" -> "🇩🇪";
            case "+30" -> "🇬🇷";
            case "+31" -> "🇳🇱";
            case "+32" -> "🇧🇪";
            case "+351" -> "🇵🇹";
            case "+352" -> "🇱🇺";
            case "+353" -> "🇮🇪";
            case "+354" -> "🇮🇸";
            case "+355" -> "🇦🇱";
            case "+356" -> "🇲🇹";
            case "+357" -> "🇨🇾";
            case "+358" -> "🇫🇮";
            case "+359" -> "🇧🇬";
            case "+370" -> "🇱🇹";
            case "+371" -> "🇱🇻";
            case "+372" -> "🇪🇪";
            case "+373" -> "🇲🇩";
            case "+374" -> "🇦🇲";
            case "+375" -> "🇧🇾";
            case "+376" -> "🇦🇩";
            case "+377" -> "🇲🇨";
            case "+378" -> "🇸🇲";
            case "+380" -> "🇺🇦";
            case "+381" -> "🇷🇸";
            case "+382" -> "🇲🇪";
            case "+385" -> "🇭🇷";
            case "+386" -> "🇸🇮";
            case "+387" -> "🇧🇦";
            case "+389" -> "🇲🇰";
            case "+420" -> "🇨🇿";
            case "+421" -> "🇸🇰";
            case "+423" -> "🇱🇮";
            case "+7" -> "🇷🇺 🇰🇿";
            case "+90" -> "🇹🇷";
            case "+971" -> "🇦🇪";
            default -> "🌐";
        };
    }

    @Transient
    @AssertTrue(message = "Alege un prefix de țară valid.")
    public boolean isPhoneCountryCodeValid() {
        return phoneCountryCode == null || phoneCountryCode.isBlank()
                || SUPPORTED_PHONE_COUNTRY_CODES.contains(phoneCountryCode);
    }

    @Transient
    @AssertTrue(message = "Numărul internațional trebuie să aibă între 7 și 15 cifre în total, incluzând prefixul țării.")
    public boolean isPhoneNumberValid() {
        if (phone == null || phone.isBlank()) return true;
        String nationalDigits = phone.replaceAll("\\D", "");
        if (nationalDigits.startsWith("0")) nationalDigits = nationalDigits.substring(1);
        String countryDigits = getPhoneCountryCode().replaceAll("\\D", "");
        int internationalLength = countryDigits.length() + nationalDigits.length();
        return internationalLength >= 7 && internationalLength <= 15;
    }

    /** E.164 value used by tel: and WhatsApp links; legacy phone values are not rewritten. */
    @Transient
    public String getInternationalPhoneNumber() {
        if (phone == null || phone.isBlank()) return "";
        String nationalDigits = phone.replaceAll("\\D", "");
        if (nationalDigits.startsWith("0")) nationalDigits = nationalDigits.substring(1);
        return getPhoneCountryCode() + nationalDigits;
    }

    @Transient
    public String getWhatsappPhoneNumber() {
        return getInternationalPhoneNumber().replaceAll("\\D", "");
    }

    @Transient
    public String getPhoneDisplay() {
        return phone == null || phone.isBlank() ? "" : getPhoneCountryCode() + " "
                + internationalNationalPart();
    }

    private String internationalNationalPart() {
        String digits = phone == null ? "" : phone.replaceAll("\\D", "");
        return digits.startsWith("0") ? digits.substring(1) : digits;
    }

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

    @URL(message = "Format URL Facebook incorect.")
    @Column(nullable = true, length = 2048)
    private String facebookUrl;

    @URL(message = "Format URL Instagram incorect.")
    @Column(nullable = true, length = 2048)
    private String instagramUrl;

    @URL(message = "Format URL TikTok incorect.")
    @Column(nullable = true, length = 2048)
    private String tiktokUrl;

    @URL(message = "Format URL YouTube incorect.")
    @Column(nullable = true, length = 2048)
    private String youtubeUrl;

    @Transient
    @AssertTrue(message = "Completează cel puțin o metodă de contact: telefon sau email.")
    public boolean isContactMethodProvided() {
        return hasText(phone) || hasText(email);
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    @OneToMany(mappedBy = "businessProfile", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<BusinessUnavailableDate> unavailableDates = new ArrayList<>();

    private String imagePath;

    @Transient
    public String getCoverImagePath() {
        return imagePath == null || imagePath.isBlank()
                ? "/images/business-default.svg" : imagePath;
    }

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

    /**
     * Used for sitemap freshness and structured-data maintenance signals.
     * It is updated automatically whenever an existing profile changes.
     */
    @Column
    private LocalDateTime updatedAt;

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

        if (createdAt == null) {
            createdAt = LocalDate.now();
        }
        if (updatedAt == null) {
            updatedAt = LocalDateTime.now();
        }

        if (uuid == null) {
            uuid = UUID.randomUUID()
                    .toString();
        }

        if (slug == null && name != null) {
            slug = java.text.Normalizer.normalize(name, java.text.Normalizer.Form.NFD)
                    .replaceAll("\\p{M}+", "")
                    .toLowerCase(java.util.Locale.ROOT)
                    .trim()
                    .replaceAll("[^a-z0-9]+", "-")
                    .replaceAll("^-|-$", "");
            if (slug.isBlank()) slug = "serviciu";
            slug = slug + "-" + uuid.substring(0, 8);
        }
    }

    @PreUpdate
    public void touchUpdatedAt() {
        updatedAt = LocalDateTime.now();
    }
}

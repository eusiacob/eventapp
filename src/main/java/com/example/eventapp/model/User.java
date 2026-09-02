package com.example.eventapp.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;


@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Introdu prenumele!")
    @Size(min = 3, max = 10, message = "Lungimea trebuie să fie între 3 și 10 caractere.")
    private String firstName;

    @NotBlank(message = "Introdu numele!")
    @Size(min = 3, max = 10, message = "Lungimea trebuie să fie între 3 și 10 caractere.")
    private String lastName;

    @Transient
    private String email;

    /**
     * Email criptat AES-GCM.
     * Va fi folosit ulterior pentru stocarea sigură a emailului.
     */
    @Column(name = "email_encrypted", length = 1000)
    private String emailEncrypted;

    /**
     * Hash determinist al emailului normalizat.
     * Va fi folosit ulterior pentru căutare/autentificare.
     */
    @Column(name = "email_hash", unique = true, length = 64)
    private String emailHash;

    @NotBlank(message = "Introdu parola!")
    @Size(min = 8, message = "Parola trebuie să aibă cel puțin 8 caractere.")
    private String password;

    private LocalDateTime createdAt = LocalDateTime.now();

    @Transient
    private String confirmPassword;


    @Transient
    private String phone;

    /**
     * Telefon criptat AES-GCM.
     */
    @Column(name = "phone_encrypted", length = 1000)
    private String phoneEncrypted;

    /**
     * Hash determinist al telefonului.
     * Va fi folosit dacă vom avea nevoie să căutăm/verificăm numărul.
     */
    @Column(name = "phone_hash", length = 64)
    private String phoneHash;

    @Enumerated(EnumType.STRING)
    private Role role;

    @Column(nullable = false)
    private boolean enabled = true;

    @Column(nullable = false)
    private int failedLoginAttempts = 0;

    @Column
    private LocalDateTime loginBlockedUntil;

    @Column
    private LocalDateTime lastActivityAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccountStatusReason accountStatusReason = AccountStatusReason.NONE;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<BusinessProfile> businessProfiles = new ArrayList<>();

    @ManyToMany
    @JoinTable(name = "favorite_businesses", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "business_id"))
    Set<BusinessProfile> favoriteBusinesses;

    @OneToMany(mappedBy = "user",
            cascade = CascadeType.ALL)
    private List<Subscription>  subscriptions = new ArrayList<>();

}
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

    @Email(regexp = "[a-z0-9._%+-]+@[a-z0-9.-]+\\.[a-z]{2,3}", flags = Pattern.Flag.CASE_INSENSITIVE, message = "Format incorect! Trebuie să fie de forma nume@gmail.com")
    @Column(unique = true)
    private String email;

    @NotBlank(message = "Introdu parola!")
    private String password;

    private LocalDateTime createdAt = LocalDateTime.now();

    @Transient
    private String confirmPassword;

    @NotBlank(message = "Introdu numărul de telefon!")
    @Pattern(regexp = "^[0-9+\\- ]{10}$", message = "Număr de telefon invalid! Trebuie să fie de forma 07X XXX XXX")
    private String phone;

    @Enumerated(EnumType.STRING)
    private Role role;

    @Column(nullable = false)
    private boolean enabled = true;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<BusinessProfile> businessProfiles = new ArrayList<>();

    @ManyToMany
    @JoinTable(name = "favorite_businesses", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "business_id"))
    Set<BusinessProfile> favoriteBusinesses;

    @OneToOne(mappedBy = "user",
            cascade = CascadeType.ALL)
    private Subscription subscription;

}
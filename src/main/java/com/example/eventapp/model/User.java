package com.example.eventapp.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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

    @NotBlank(message = "First name can not be empty!")
    @Size(min = 3, max = 10)
    private String firstName;

    @NotBlank(message = "Last name can not be empty!")
    @Size(min = 3, max = 10)
    private String lastName;

    @Email(regexp = "[a-z0-9._%+-]+@[a-z0-9.-]+\\.[a-z]{2,3}", flags = Pattern.Flag.CASE_INSENSITIVE, message = "Incorrect format! It should be like email@me.com")
    @Column(unique = true)
    private String email;

    @NotBlank(message = "Password can not be empty!")
    private String password;

    @Transient
    private String confirmPassword;

    @NotBlank(message = "Phone is required")
    @Pattern(regexp = "^[0-9+\\- ]{10}$", message = "Invalid phone number! It should be like 07X XXX XXX")
    private String phone;

    @Enumerated(EnumType.STRING)
    private Role role;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<BusinessProfile> businessProfiles = new ArrayList<>();

    @ManyToMany
    @JoinTable(
            name = "favorite_businesses",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "business_id")
    )
    Set<BusinessProfile> favoriteBusinesses;
}
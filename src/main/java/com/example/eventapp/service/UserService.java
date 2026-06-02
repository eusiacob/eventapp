package com.example.eventapp.service;

import com.example.eventapp.model.BusinessProfile;
import com.example.eventapp.model.User;
import com.example.eventapp.repository.BusinessProfileRepository;
import com.example.eventapp.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final BusinessProfileRepository businessProfileRepository;

    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder, BusinessProfileRepository businessProfileRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.businessProfileRepository = businessProfileRepository;
    }

    public boolean emailExists(String email) {
        return userRepository.existsByEmail(email);
    }

    public void registerUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);
    }

    public void addFavorite(Long businessId, String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow();

        BusinessProfile profile =
                businessProfileRepository.findById(businessId)
                        .orElseThrow();

        if (!user.getFavoriteBusinesses().contains(profile)) {

            user.getFavoriteBusinesses().add(profile);

            userRepository.save(user);
        }
    }

    public void removeFavorite(Long businessId, String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow();

        user.getFavoriteBusinesses()
                .removeIf(b -> b.getId().equals(businessId));

        userRepository.save(user);
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }


    //    Toggle favorite heart
    public boolean toggleFavorite(Long businessId, String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        BusinessProfile businessProfile = businessProfileRepository.findById(businessId)
                .orElseThrow(() -> new RuntimeException("Business not found"));

        boolean alreadyFavorite = user.getFavoriteBusinesses()
                .stream()
                .anyMatch(b -> b.getId().equals(businessId));

        if (alreadyFavorite) {
            user.getFavoriteBusinesses()
                    .removeIf(b -> b.getId().equals(businessId));

            userRepository.save(user);

            return false;
        }

        user.getFavoriteBusinesses().add(businessProfile);

        userRepository.save(user);

        return true;
    }
}
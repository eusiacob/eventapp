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
}
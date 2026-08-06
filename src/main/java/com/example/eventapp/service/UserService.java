package com.example.eventapp.service;

import com.example.eventapp.model.BusinessProfile;
import com.example.eventapp.model.Role;
import com.example.eventapp.model.User;
import com.example.eventapp.repository.BusinessProfileRepository;
import com.example.eventapp.repository.ReviewRepository;
import com.example.eventapp.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final BusinessProfileRepository businessProfileRepository;
    private final ReviewRepository reviewRepository;

    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder, ReviewRepository reviewRepository, BusinessProfileRepository businessProfileRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.businessProfileRepository = businessProfileRepository;
        this.reviewRepository = reviewRepository;
    }

    public List<User> findAll() {

        return userRepository.findAll();
    }

    public List<User> findByRole(Role role) {

        return userRepository.findByRole(role);

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

    public User findById(Long id) {

        return userRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

    }

    public long getReviewCount(User user) {
        return reviewRepository.countByUser(user);
    }


    //    Toggle favorite heart
    public boolean toggleFavorite(String businessUuid, String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        BusinessProfile businessProfile = businessProfileRepository.findByUuid(businessUuid)
                .orElseThrow(() -> new RuntimeException("Business not found"));

        boolean alreadyFavorite = user.getFavoriteBusinesses()
                .stream()
                .anyMatch(b -> b.getUuid().equals(businessUuid));

        if (alreadyFavorite) {

            user.getFavoriteBusinesses()
                    .removeIf(b -> b.getUuid().equals(businessUuid));

            userRepository.save(user);

            return false;
        }

        user.getFavoriteBusinesses().add(businessProfile);

        userRepository.save(user);

        return true;
    }

    //Se sterge serviciul din favorite de la toti userii
    public void removeBusinessFromAllFavorites(Long businessId) {
        List<User> users = userRepository.findUsersWhoFavoriteBusiness(businessId);

        for (User user : users) {
            user.getFavoriteBusinesses().removeIf(
                    business -> business.getId().equals(businessId)
            );
        }

        userRepository.saveAll(users);
    }
}
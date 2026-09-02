package com.example.eventapp.service;

import com.example.eventapp.dto.RegisterUserDTO;
import com.example.eventapp.model.AccountStatusReason;
import com.example.eventapp.model.BusinessProfile;
import com.example.eventapp.model.Role;
import com.example.eventapp.model.User;
import com.example.eventapp.repository.BusinessProfileRepository;
import com.example.eventapp.repository.UserRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final BusinessProfileRepository businessProfileRepository;
    private final EncryptionService encryptionService;

    public UserService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            BusinessProfileRepository businessProfileRepository,
            EncryptionService encryptionService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.businessProfileRepository = businessProfileRepository;
        this.encryptionService = encryptionService;
    }

    public List<User> findAll() {

        return userRepository.findAll();
    }

    public List<User> findByRole(Role role) {

        return userRepository.findByRole(role);

    }

    public boolean emailExists(String email) {

        if (email == null || email.isBlank()) {
            return false;
        }

        String normalizedEmail =
                email.trim().toLowerCase();

        String emailHash =
                encryptionService.hash(normalizedEmail);

        return userRepository
                .findByEmailHash(emailHash)
                .isPresent();
    }

    public void registerUser(RegisterUserDTO userDTO) {

        User user = new User();

        user.setFirstName(userDTO.getFirstName().trim());
        user.setLastName(userDTO.getLastName().trim());

        String email =
                userDTO.getEmail()
                        .trim()
                        .toLowerCase();

        String phone =
                userDTO.getPhone()
                        .trim();

        user.setEmail(email);
        user.setPhone(phone);

        user.setEmailHash(
                encryptionService.hash(email)
        );

        user.setEmailEncrypted(
                encryptionService.encrypt(email)
        );

        user.setPhoneHash(
                encryptionService.hash(phone)
        );

        user.setPhoneEncrypted(
                encryptionService.encrypt(phone)
        );

        user.setPassword(
                passwordEncoder.encode(
                        userDTO.getPassword()
                )
        );

        user.setConfirmPassword(null);

        user.setRole(Role.USER);
        user.setEnabled(true);
        user.setLastActivityAt(LocalDateTime.now());
        user.setAccountStatusReason(AccountStatusReason.NONE);

        userRepository.save(user);
    }

    public void addFavorite(Long businessId, String email) {

        User user = findByEmail(email);

        BusinessProfile profile =
                businessProfileRepository.findById(businessId)
                        .orElseThrow();

        if (!user.getFavoriteBusinesses().contains(profile)) {

            user.getFavoriteBusinesses().add(profile);

            userRepository.save(user);
        }
    }

    public void removeFavorite(String businessId, String email) {

        User user = findByEmail(email);

        user.getFavoriteBusinesses()
                .removeIf(b -> b.getUuid().equals(businessId));

        userRepository.save(user);
    }

    public User findByEmail(String email) {

        if (email == null || email.isBlank()) {
            throw new RuntimeException("User not found");
        }

        String emailHash =
                encryptionService.hash(
                        email.trim().toLowerCase()
                );

        return userRepository
                .findByEmailHash(emailHash)
                .orElseThrow(
                        () -> new RuntimeException(
                                "User not found"
                        )
                );
    }

    public User findById(Long id) {

        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        if (user.getEmailEncrypted() != null) {
            user.setEmail(
                    encryptionService.decrypt(
                            user.getEmailEncrypted()
                    )
            );
        }

        return user;
    }

    //    Toggle favorite heart
    public boolean toggleFavorite(String businessUuid, String email) {

        User user = findByEmail(email);

        BusinessProfile businessProfile =
                businessProfileRepository.findByUuid(businessUuid)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Business not found"
                                )
                        );

        boolean alreadyFavorite =
                user.getFavoriteBusinesses()
                        .stream()
                        .anyMatch(
                                b -> b.getUuid()
                                        .equals(businessUuid)
                        );

        if (alreadyFavorite) {

            user.getFavoriteBusinesses()
                    .removeIf(
                            b -> b.getUuid()
                                    .equals(businessUuid)
                    );

            userRepository.save(user);

            return false;
        }

        user.getFavoriteBusinesses()
                .add(businessProfile);

        userRepository.save(user);

        return true;
    }

    public void toggleEnabled(Long userId, String currentAdminEmail) {

        User user = findById(userId);

        User currentAdmin = findByEmail(currentAdminEmail);

        if (user.getId().equals(currentAdmin.getId())) {
            throw new IllegalStateException(
                    "Nu vă puteți suspenda propriul cont."
            );
        }

        user.setEnabled(!user.isEnabled());

        if (user.isEnabled()) {
            user.setAccountStatusReason(AccountStatusReason.NONE);
            user.setLastActivityAt(LocalDateTime.now());

        } else {
            user.setAccountStatusReason(AccountStatusReason.MANUAL);
        }

        userRepository.save(user);

    }

    @Scheduled(cron = "0 0 3 * * *")
    public void disableInactiveUsers() {

        LocalDateTime limit =
                LocalDateTime.now().minusMonths(6);

        List<User> inactiveUsers =
                userRepository.findByEnabledTrueAndLastActivityAtBefore(
                        limit
                );

        for (User user : inactiveUsers) {

            if (user.getRole() == Role.ADMIN) {
                continue;
            }

            user.setEnabled(false);

            user.setAccountStatusReason(AccountStatusReason.INACTIVITY);

        }

        userRepository.saveAll(inactiveUsers);

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
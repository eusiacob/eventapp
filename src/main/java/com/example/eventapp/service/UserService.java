package com.example.eventapp.service;

import com.example.eventapp.dto.RegisterUserDTO;
import com.example.eventapp.dto.LegalAcceptanceDTO;
import com.example.eventapp.model.AccountStatusReason;
import com.example.eventapp.model.BusinessProfile;
import com.example.eventapp.model.Role;
import com.example.eventapp.model.User;
import com.example.eventapp.repository.BusinessProfileRepository;
import com.example.eventapp.repository.PasswordResetTokenRepository;
import com.example.eventapp.repository.ReviewRepository;
import com.example.eventapp.repository.SubscriptionRepository;
import com.example.eventapp.repository.SupportMessageRepository;
import com.example.eventapp.repository.SupportTicketRepository;
import com.example.eventapp.repository.UserRepository;
import com.example.eventapp.repository.UserNotificationRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final BusinessProfileRepository businessProfileRepository;
    private final EncryptionService encryptionService;
    private final ReviewRepository reviewRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final SupportTicketRepository supportTicketRepository;
    private final SupportMessageRepository supportMessageRepository;
    private final UserNotificationRepository userNotificationRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final LegalDocumentService legalDocumentService;

    public UserService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            BusinessProfileRepository businessProfileRepository,
            EncryptionService encryptionService,
            ReviewRepository reviewRepository,
            SubscriptionRepository subscriptionRepository,
            SupportTicketRepository supportTicketRepository,
            SupportMessageRepository supportMessageRepository,
            UserNotificationRepository userNotificationRepository,
            PasswordResetTokenRepository passwordResetTokenRepository,
            LegalDocumentService legalDocumentService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.businessProfileRepository = businessProfileRepository;
        this.encryptionService = encryptionService;
        this.reviewRepository = reviewRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.supportTicketRepository = supportTicketRepository;
        this.supportMessageRepository = supportMessageRepository;
        this.userNotificationRepository = userNotificationRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.legalDocumentService = legalDocumentService;
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
        legalDocumentService.acceptCurrentDocuments(user);

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

        String emailHash = encryptionService.hash(email.trim().toLowerCase());

        return userRepository
                .findByEmailHash(emailHash)
                .orElseThrow(
                        () -> new RuntimeException(
                                "User not found"));
    }

    public User findById(Long id) {

        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        if (user.getEmailEncrypted() != null) {
            user.setEmail(
                    encryptionService.decrypt(
                            user.getEmailEncrypted()));
        }

        return user;
    }

    public String getEmailAddress(User user) {
        if (user == null) {
            throw new IllegalArgumentException("Utilizatorul este obligatoriu.");
        }

        if (user.getEmail() != null && !user.getEmail().isBlank()) {
            return user.getEmail();
        }

        if (user.getEmailEncrypted() != null && !user.getEmailEncrypted().isBlank()) {
            return encryptionService.decrypt(user.getEmailEncrypted());
        }

        throw new IllegalStateException("Emailul utilizatorului nu este disponibil.");
    }

    public String getPhoneNumber(User user) {
        if (user == null) {
            throw new IllegalArgumentException("Utilizatorul este obligatoriu.");
        }

        if (user.getPhone() != null && !user.getPhone().isBlank()) {
            return user.getPhone();
        }

        if (user.getPhoneEncrypted() != null && !user.getPhoneEncrypted().isBlank()) {
            return encryptionService.decrypt(user.getPhoneEncrypted());
        }

        return null;
    }

    public boolean hasCurrentLegalAcceptances(User user) {
        return legalDocumentService.hasCurrentAcceptances(user);
    }

    public void acceptCurrentLegalDocuments(
            User user,
            LegalAcceptanceDTO acceptance
    ) {
        if (!acceptance.isPrivacyAccepted() || !acceptance.isTermsAccepted()) {
            throw new IllegalArgumentException(
                    "Este necesară acceptarea ambelor documente."
            );
        }

        legalDocumentService.acceptCurrentDocuments(user);
        userRepository.save(user);
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

    @Scheduled(cron = "0 15 3 * * *")
    @Transactional
    public void deleteAccountsPastRetentionPeriod() {

        LocalDateTime retentionLimit =
                LocalDateTime.now().minusYears(2);

        List<User> inactiveUsers =
                userRepository.findByLastActivityAtBefore(retentionLimit);

        for (User user : inactiveUsers) {

            if (user.getRole() == Role.ADMIN) {
                continue;
            }

            deleteUserData(user);
        }
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

    public void changeEmail(
            User user,
            String currentPassword,
            String newEmail
    ) {
        validateCurrentPassword(user, currentPassword);

        if (newEmail == null ||
                !newEmail.trim().matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            throw new IllegalArgumentException("Introdu o adresă de email validă.");
        }

        String normalizedEmail = newEmail.trim().toLowerCase();

        if (encryptionService.hash(normalizedEmail).equals(user.getEmailHash())) {
            throw new IllegalArgumentException("Noul email este identic cu cel actual.");
        }

        if (emailExists(normalizedEmail)) {
            throw new IllegalArgumentException("Această adresă de email este deja folosită.");
        }

        user.setEmail(normalizedEmail);
        user.setEmailHash(encryptionService.hash(normalizedEmail));
        user.setEmailEncrypted(encryptionService.encrypt(normalizedEmail));

        userRepository.save(user);
    }

    public void changePassword(
            User user,
            String currentPassword,
            String newPassword,
            String confirmPassword
    ) {
        validateCurrentPassword(user, currentPassword);

        if (newPassword == null || newPassword.length() < 8) {
            throw new IllegalArgumentException("Parola nouă trebuie să aibă cel puțin 8 caractere.");
        }

        if (!newPassword.equals(confirmPassword)) {
            throw new IllegalArgumentException("Parolele noi nu se potrivesc.");
        }

        if (passwordEncoder.matches(newPassword, user.getPassword())) {
            throw new IllegalArgumentException("Alege o parolă diferită de cea actuală.");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Transactional
    public void deleteAccount(
            User user,
            String currentPassword,
            String deleteConfirmation
    ) {
        validateCurrentPassword(user, currentPassword);

        if (!"ȘTERGE".equals(deleteConfirmation)) {
            throw new IllegalArgumentException("Scrie ȘTERGE pentru a confirma ștergerea contului.");
        }

        User managedUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new IllegalStateException("Contul nu mai există."));

        deleteUserData(managedUser);
    }

    private void deleteUserData(User managedUser) {

        List<BusinessProfile> profiles = businessProfileRepository.findByUser(managedUser);
        for (BusinessProfile profile : profiles) {
            removeBusinessFromAllFavorites(profile.getId());
            deleteBusinessUploads(profile);
        }

        if (managedUser.getFavoriteBusinesses() != null) {
            managedUser.getFavoriteBusinesses().clear();
            userRepository.saveAndFlush(managedUser);
        }

        supportMessageRepository.deleteByUser(managedUser);
        List<com.example.eventapp.model.SupportTicket> tickets =
                supportTicketRepository.findAllByUserOrderByUpdatedAtDesc(managedUser);
        if (!tickets.isEmpty()) {
            supportMessageRepository.deleteByTicketIn(tickets);
        }
        supportTicketRepository.deleteByUser(managedUser);
        reviewRepository.deleteByUser(managedUser);
        subscriptionRepository.deleteByUser(managedUser);
        userNotificationRepository.deleteByUser(managedUser);
        passwordResetTokenRepository.deleteByUser(managedUser);

        userRepository.delete(managedUser);
    }

    private void deleteBusinessUploads(BusinessProfile profile) {
        if (profile.getUuid() == null) {
            return;
        }

        try {
            UUID.fromString(profile.getUuid());
        } catch (IllegalArgumentException exception) {
            throw new IllegalStateException("Identificatorul serviciului este invalid.", exception);
        }

        Path uploadsRoot = Paths.get("uploads", "businesses")
                .toAbsolutePath()
                .normalize();

        if (!Files.exists(uploadsRoot)) {
            return;
        }

        try (Stream<Path> paths = Files.walk(uploadsRoot, 2)) {
            List<Path> profileDirectories = paths
                    .filter(Files::isDirectory)
                    .filter(path -> path.startsWith(uploadsRoot))
                    .filter(path -> profile.getUuid().equals(
                            path.getFileName().toString()
                    ))
                    .toList();

            for (Path directory : profileDirectories) {
                deleteDirectoryIfAllowed(directory, uploadsRoot);
            }
        } catch (IOException exception) {
            throw new IllegalStateException(
                    "Fișierele serviciului nu au putut fi identificate.",
                    exception
            );
        }
    }

    private void deleteDirectoryIfAllowed(Path directory, Path uploadsRoot) {
        if (!directory.startsWith(uploadsRoot) || !Files.exists(directory)) {
            return;
        }

        try (Stream<Path> paths = Files.walk(directory)) {
            paths.sorted(Comparator.reverseOrder())
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                        } catch (IOException exception) {
                            throw new UncheckedIOException(exception);
                        }
                    });
        } catch (IOException | UncheckedIOException exception) {
            throw new IllegalStateException(
                    "Fișierele serviciului nu au putut fi șterse.",
                    exception
            );
        }
    }

    private void validateCurrentPassword(User user, String currentPassword) {
        if (currentPassword == null ||
                !passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new IllegalArgumentException("Parola curentă este incorectă.");
        }
    }
}

package com.example.eventapp.service;

import com.example.eventapp.model.User;
import com.example.eventapp.repository.UserRepository;
import org.jspecify.annotations.NonNull;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final EncryptionService encryptionService;

    public CustomUserDetailsService(
            UserRepository userRepository,
            EncryptionService encryptionService
    ) {
        this.userRepository = userRepository;
        this.encryptionService = encryptionService;
    }

    @Override
    @NonNull
    public UserDetails loadUserByUsername(
            @NonNull String email
    ) throws UsernameNotFoundException {

        String normalizedEmail =
                email.trim().toLowerCase();

        String emailHash =
                encryptionService.hash(normalizedEmail);

        User user =
                userRepository
                        .findByEmailHash(emailHash)
                        .orElseThrow(
                                () -> new UsernameNotFoundException(
                                        "User not found"
                                )
                        );

        String decryptedEmail =
                encryptionService.decrypt(
                        user.getEmailEncrypted()
                );

        return org.springframework.security.core.userdetails.User
                .withUsername(decryptedEmail)
                .password(user.getPassword())
                .roles(user.getRole().name())
                .disabled(!user.isEnabled())
                .build();
    }
}
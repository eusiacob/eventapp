package com.example.eventapp.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

@Service
public class EncryptionService {

    private static final String ALGORITHM = "AES/GCM/NoPadding";

    private static final int GCM_TAG_LENGTH = 128;

    private static final int IV_LENGTH = 12;

    private final SecretKeySpec secretKey;

    public EncryptionService(
            @Value("${app.encryption.key}") String key
    ) {

        byte[] keyBytes =
                Base64.getDecoder().decode(key);

        if (keyBytes.length != 32) {
            throw new IllegalArgumentException(
                    "Cheia de criptare trebuie să aibă exact 32 bytes."
            );
        }

        this.secretKey =
                new SecretKeySpec(
                        keyBytes,
                        "AES"
                );
    }

    public String encrypt(String value) {

        if (value == null) {
            return null;
        }

        try {

            byte[] iv =
                    new byte[IV_LENGTH];

            java.security.SecureRandom
                    .getInstanceStrong()
                    .nextBytes(iv);

            Cipher cipher =
                    Cipher.getInstance(ALGORITHM);

            GCMParameterSpec gcmSpec =
                    new GCMParameterSpec(
                            GCM_TAG_LENGTH,
                            iv
                    );

            cipher.init(
                    Cipher.ENCRYPT_MODE,
                    secretKey,
                    gcmSpec
            );

            byte[] encrypted =
                    cipher.doFinal(
                            value.getBytes(
                                    StandardCharsets.UTF_8
                            )
                    );

            byte[] result =
                    new byte[
                            iv.length +
                                    encrypted.length
                            ];

            System.arraycopy(
                    iv,
                    0,
                    result,
                    0,
                    iv.length
            );

            System.arraycopy(
                    encrypted,
                    0,
                    result,
                    iv.length,
                    encrypted.length
            );

            return Base64.getEncoder()
                    .encodeToString(result);

        } catch (Exception e) {

            throw new IllegalStateException(
                    "Nu s-a putut cripta valoarea.",
                    e
            );
        }
    }

    public String decrypt(String encryptedValue) {

        if (encryptedValue == null) {
            return null;
        }

        try {

            byte[] decoded =
                    Base64.getDecoder()
                            .decode(encryptedValue);

            byte[] iv =
                    new byte[IV_LENGTH];

            byte[] encrypted =
                    new byte[
                            decoded.length -
                                    IV_LENGTH
                            ];

            System.arraycopy(
                    decoded,
                    0,
                    iv,
                    0,
                    IV_LENGTH
            );

            System.arraycopy(
                    decoded,
                    IV_LENGTH,
                    encrypted,
                    0,
                    encrypted.length
            );

            Cipher cipher =
                    Cipher.getInstance(ALGORITHM);

            GCMParameterSpec gcmSpec =
                    new GCMParameterSpec(
                            GCM_TAG_LENGTH,
                            iv
                    );

            cipher.init(
                    Cipher.DECRYPT_MODE,
                    secretKey,
                    gcmSpec
            );

            byte[] decrypted =
                    cipher.doFinal(encrypted);

            return new String(
                    decrypted,
                    StandardCharsets.UTF_8
            );

        } catch (Exception e) {

            throw new IllegalStateException(
                    "Nu s-a putut decripta valoarea.",
                    e
            );
        }
    }

    public String hash(String value) {

        if (value == null) {
            return null;
        }

        try {

            MessageDigest digest =
                    MessageDigest.getInstance(
                            "SHA-256"
                    );

            byte[] hash =
                    digest.digest(
                            value.getBytes(
                                    StandardCharsets.UTF_8
                            )
                    );

            StringBuilder hex =
                    new StringBuilder();

            for (byte b : hash) {

                hex.append(
                        String.format(
                                "%02x",
                                b
                        )
                );
            }

            return hex.toString();

        } catch (Exception e) {

            throw new IllegalStateException(
                    "Nu s-a putut calcula hash-ul.",
                    e
            );
        }
    }
}
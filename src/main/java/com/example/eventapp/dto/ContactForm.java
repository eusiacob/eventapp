package com.example.eventapp.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ContactForm {

    @NotBlank(message = "Numele este obligatoriu.")
    @Size(max = 100, message = "Numele poate avea maximum 100 de caractere.")
    @Pattern(
            regexp = "[\\p{L}\\p{M}]+(?:[ '\\u2019-][\\p{L}\\p{M}]+)*",
            message = "Numele poate conține doar litere, spații, cratimă și apostrof."
    )
    private String name;

    @NotBlank(message = "Adresa de email este obligatorie.")
    @Email(message = "Introdu o adresă de email validă.")
    @Size(max = 150, message = "Adresa de email este prea lungă.")
    private String email;

    @NotBlank(message = "Subiectul este obligatoriu.")
    @Size(max = 150, message = "Subiectul poate avea maximum 150 de caractere.")
    @Pattern(
            regexp = "[\\p{L}\\p{M}\\p{N}][\\p{L}\\p{M}\\p{N} .,!?;:'\\\"()–—-]*",
            message = "Subiectul poate conține litere, cifre, spații și punctuație obișnuită; fără slash-uri sau caractere speciale."
    )
    private String subject;

    @NotBlank(message = "Mesajul este obligatoriu.")
    @Size(max = 5000, message = "Mesajul poate avea maximum 5000 de caractere.")
    private String message;

    @jakarta.validation.constraints.AssertTrue(
            message = "Trebuie să accepți Politica de confidențialitate."
    )
    private boolean privacyAccepted;

}

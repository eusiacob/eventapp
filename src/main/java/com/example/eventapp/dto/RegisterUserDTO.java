package com.example.eventapp.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterUserDTO {

    @NotBlank(message = "Introdu prenumele!")
    @Size(
            min = 3,
            max = 10,
            message = "Lungimea trebuie să fie între 3 și 10 caractere."
    )
    private String firstName;

    @NotBlank(message = "Introdu numele!")
    @Size(
            min = 3,
            max = 10,
            message = "Lungimea trebuie să fie între 3 și 10 caractere."
    )
    private String lastName;

    @NotBlank(message = "Introdu emailul!")
    @Email(
            regexp = "[a-z0-9._%+-]+@[a-z0-9.-]+\\.[a-z]{2,3}",
            flags = Pattern.Flag.CASE_INSENSITIVE,
            message = "Format incorect! Trebuie să fie de forma nume@gmail.com"
    )
    private String email;

    @NotBlank(message = "Introdu numărul de telefon!")
    @Pattern(
            regexp = "^[0-9+\\- ]{10}$",
            message = "Număr de telefon invalid! Trebuie să fie de forma 07X XXX XXX"
    )
    private String phone;

    @NotBlank(message = "Introdu parola!")
    private String password;

    @NotBlank(message = "Confirmă parola!")
    private String confirmPassword;
}
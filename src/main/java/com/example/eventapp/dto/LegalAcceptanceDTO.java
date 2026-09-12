package com.example.eventapp.dto;

import jakarta.validation.constraints.AssertTrue;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LegalAcceptanceDTO {

    @AssertTrue(message = "Trebuie să confirmi că ai citit Politica de confidențialitate.")
    private boolean privacyAccepted;

    @AssertTrue(message = "Trebuie să accepți Termenii și condițiile.")
    private boolean termsAccepted;
}

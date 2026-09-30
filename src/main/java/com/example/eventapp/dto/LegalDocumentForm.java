package com.example.eventapp.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class LegalDocumentForm {

    @NotBlank(message = "Conținutul documentului este obligatoriu.")
    private String content;

    @NotBlank(message = "Versiunea documentului este obligatorie.")
    @Size(max = 32, message = "Versiunea poate avea cel mult 32 de caractere.")
    private String version;

    @NotNull(message = "Data actualizării este obligatorie.")
    private LocalDate lastUpdated;
}

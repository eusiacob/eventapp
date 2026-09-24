package com.example.eventapp.model;

import lombok.Getter;

@Getter
public enum BusinessEventType {
    NUNTA("Nuntă"),
    BOTEZ("Botez"),
    CUNUNIE_CIVILA("Cununie civilă"),
    ANIVERSARE("Aniversare"),
    MAJORAT("Majorat"),
    CORPORATE("Eveniment corporate"),
    BAL_BANCHET("Bal / Banchet"),
    FESTIVAL_CONCERT("Festival / Concert"),
    ALTUL("Alt eveniment");

    private final String displayName;

    BusinessEventType(String displayName) {
        this.displayName = displayName;
    }
}


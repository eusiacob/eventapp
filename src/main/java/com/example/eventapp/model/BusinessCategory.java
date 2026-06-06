package com.example.eventapp.model;

import lombok.Getter;

@Getter
public enum BusinessCategory {
    ORGANIZATOR("Organizator eveniment"),
    FOTOGRAF("Fotograf"),
    DJ("DJ"),
    RESTAURANT("Restaurant"),
    DECOR("Decor"),
    CANDY_BAR("Candy Bar"),
    MUZICA_LIVE("Muzică live"),
    TRANSPORT("Transport"),
    ENTERTAINMENT("Entertainment");

    private final String displayName;

    BusinessCategory(String displayName) {
        this.displayName = displayName;
    }

}
package com.example.eventapp.model;

import lombok.Getter;

@Getter
public enum BusinessCategory {

    FOTOGRAF("Fotograf", "bi-camera-fill"),
    VIDEOGRAF("Videograf", "bi-camera-reels-fill"),
    DJ("DJ", "bi-music-note-beamed"),
    MUZICA_LIVE("Muzică live", "bi-mic-fill"),
    BARURI("Baruri pentru evenimente", "bi-shop-window"),
    CATERING("Catering", "bi-egg-fried"),
    FLORIST("Aranjamente florale", "bi-flower1"),
    DECOR("Decor", "bi-balloon-fill"),
    DIVERTISMENT("Divertisment", "bi-magic"),
    LOCATII("Locații", "bi bi-building-fill"),
    ORGANIZATOR("Organizator", "bi-calendar-heart"),
    TRANSPORT("Transport", "bi-car-front-fill"),
    ARTIFICII("Artificii", "bi-stars"),
    ALTELE("Alte servicii", "bi-grid");

    private final String displayName;
    private final String icon;

    BusinessCategory(String displayName, String icon) {
        this.displayName = displayName;
        this.icon = icon;
    }

    public String getCarouselName() {
        return this == BARURI ? "Baruri" : displayName;
    }
}

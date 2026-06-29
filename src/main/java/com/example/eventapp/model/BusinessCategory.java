package com.example.eventapp.model;

import lombok.Getter;

@Getter
public enum BusinessCategory {

    FOTOGRAF("Fotograf", "bi-camera-fill"),
    VIDEOGRAF("Videograf", "bi-camera-reels-fill"),
    DJ("DJ", "bi-music-note-beamed"),
    MUZICA_LIVE("Muzică", "bi-mic-fill"),
    RESTAURANT("Restaurant", "bi-cup-hot-fill"),
    CANDY_BAR("Candy Bar", "bi-cake2-fill"),
    FLORIST("Florist", "bi-flower1"),
    DECOR("Decor", "bi-stars"),
    DIVERTISMENT("Divertisment", "bi-stars"),
    ORGANIZATOR("Organizator", "bi-calendar-heart"),
    MAKEUP("Make-up Artist", "bi-brush-fill"),
    HAIRSTYLIST("Hair Stylist", "bi-scissors"),
    TRANSPORT("Transport", "bi-car-front-fill"),
    ARTIFICII("Artificii", "bi-stars"),
    ALTELE("Altele", "bi-grid");

    private final String displayName;
    private final String icon;

    BusinessCategory(String displayName, String icon) {
        this.displayName = displayName;
        this.icon = icon;
    }
}
package com.example.eventapp.model;

public enum AccountPurpose {
    UNSPECIFIED,
    SEARCH_SERVICES,
    PROMOTE_SERVICES,
    BOTH;

    public boolean includesPromotion() {
        return this == PROMOTE_SERVICES || this == BOTH;
    }
}

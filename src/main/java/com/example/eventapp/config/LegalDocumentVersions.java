package com.example.eventapp.config;

import com.example.eventapp.model.User;

public final class LegalDocumentVersions {

    public static final String PRIVACY_POLICY = "2026-09-13";
    public static final String TERMS_AND_CONDITIONS = "2026-09-13";

    public static boolean hasCurrentAcceptances(User user) {
        return user != null &&
                PRIVACY_POLICY.equals(user.getPrivacyPolicyVersion()) &&
                user.getPrivacyPolicyAcceptedAt() != null &&
                TERMS_AND_CONDITIONS.equals(user.getTermsVersion()) &&
                user.getTermsAcceptedAt() != null;
    }

    private LegalDocumentVersions() {
    }
}

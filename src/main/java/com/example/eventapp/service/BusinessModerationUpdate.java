package com.example.eventapp.service;

import com.example.eventapp.model.BusinessProfile;

public record BusinessModerationUpdate(
        BusinessProfile profile,
        boolean alreadyPendingModeration
) {
}

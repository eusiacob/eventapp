package com.example.eventapp.seo;

import com.example.eventapp.model.BusinessCategory;
import com.example.eventapp.model.RomanianCounties;

import java.text.Normalizer;
import java.util.Locale;
import java.util.Optional;

public final class SeoSlug {
    private SeoSlug() {}

    public static String slugify(String value) {
        if (value == null) return "";
        String normalized = Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "")
                .toLowerCase(Locale.ROOT)
                .replace('ș', 's')
                .replace('ț', 't')
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-|-$", "");
        return normalized;
    }

    public static Optional<BusinessCategory> category(String slug) {
        if (slug == null) return Optional.empty();
        return java.util.Arrays.stream(BusinessCategory.values())
                .filter(category -> slugify(category.getDisplayName()).equals(slug))
                .findFirst();
    }

    public static Optional<String> county(String slug) {
        if (slug == null) return Optional.empty();
        return RomanianCounties.ALL.stream()
                .filter(county -> slugify(county).equals(slug))
                .findFirst();
    }
}

package com.example.eventapp.seo;

import java.util.List;

public record GuideArticle(
        String slug,
        String title,
        String description,
        String intro,
        String categorySlug,
        String categoryLabel,
        List<GuideSection> sections
) {
    public record GuideSection(String heading, String text, List<String> points) {
    }
}

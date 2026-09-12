package com.example.eventapp.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.StringJoiner;

@Getter
@Component
@ConfigurationProperties(prefix = "app.uploads")
public class UploadProperties {

    @Setter
    private String directory = "uploads";
    private String publicPath = "/uploads";

    public void setPublicPath(String publicPath) {
        this.publicPath = normalizePublicPath(publicPath);
    }

    public Path rootPath() {
        return Paths.get(directory)
                .toAbsolutePath()
                .normalize();
    }

    public Path businessesPath() {
        return rootPath()
                .resolve("businesses")
                .normalize();
    }

    public Path businessPath(String category, String uuid, String... extraSegments) {
        Path path = businessesPath()
                .resolve(category)
                .resolve(uuid)
                .normalize();

        for (String segment : extraSegments) {
            path = path.resolve(segment).normalize();
        }

        return path;
    }

    public Path resolvePublicPath(String path) {
        String normalizedPath = stripLeadingSlash(path);
        String normalizedPublicPath = stripLeadingSlash(publicPath);

        if (!normalizedPath.equals(normalizedPublicPath) &&
                !normalizedPath.startsWith(normalizedPublicPath + "/")) {
            throw new IllegalArgumentException("Calea fișierului nu este permisă.");
        }

        String relativeToUploads = normalizedPath
                .substring(normalizedPublicPath.length())
                .replaceFirst("^/", "");

        return rootPath()
                .resolve(relativeToUploads)
                .normalize();
    }

    public String publicBusinessPath(String category, String uuid, String... extraSegments) {
        StringJoiner joiner = new StringJoiner("/");
        joiner.add(publicPath)
                .add("businesses")
                .add(category)
                .add(uuid);

        for (String segment : extraSegments) {
            joiner.add(segment);
        }

        return joiner.toString();
    }

    public String resourceLocation() {
        return rootPath().toUri().toString();
    }

    private String normalizePublicPath(String value) {
        if (value == null || value.isBlank()) {
            return "/uploads";
        }

        String normalized = value.trim();

        if (!normalized.startsWith("/")) {
            normalized = "/" + normalized;
        }

        if (normalized.endsWith("/") && normalized.length() > 1) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }

        return normalized;
    }

    private String stripLeadingSlash(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }

        return value.startsWith("/")
                ? value.substring(1)
                : value;
    }
}

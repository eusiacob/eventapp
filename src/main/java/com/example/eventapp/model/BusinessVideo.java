package com.example.eventapp.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class BusinessVideo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String videoPath;

    @Transient
    public String getThumbnailPath() {
        if (videoPath == null || videoPath.isBlank()) {
            return "";
        }

        int lastDot = videoPath.lastIndexOf('.');

        if (lastDot < 0) {
            return videoPath + ".jpg";
        }

        return videoPath.substring(0, lastDot) + ".jpg";
    }

    @Transient
    public String getVideoContentType() {
        if (videoPath != null &&
                videoPath.toLowerCase().endsWith(".webm")) {
            return "video/webm";
        }

        return "video/mp4";
    }

    @ManyToOne
    @JoinColumn(name = "business_id")
    private BusinessProfile businessProfile;

}

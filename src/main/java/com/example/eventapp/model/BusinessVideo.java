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

    @ManyToOne
    @JoinColumn(name = "business_id")
    private BusinessProfile businessProfile;

}
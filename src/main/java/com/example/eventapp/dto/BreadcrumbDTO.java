package com.example.eventapp.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BreadcrumbDTO {

    private String label;
    private String url;

    public BreadcrumbDTO(String label, String url) {
        this.label = label;
        this.url = url;
    }
}
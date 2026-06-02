package com.example.eventapp.controller;

import com.example.eventapp.model.BusinessProfile;
import com.example.eventapp.service.BusinessProfileService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class HomeController {

    private final BusinessProfileService businessProfileService;

    public HomeController(BusinessProfileService businessProfileService) {
        this.businessProfileService = businessProfileService;
    }

    @GetMapping("/")
    public String home() {
        return "home";
    }

    @GetMapping("/businesses")
    public String businesses(Model model) {

        List<BusinessProfile> profiles =
                businessProfileService.findAll()
                        .stream()
                        .sorted(Comparator.comparing(BusinessProfile::getCategory))
                        .toList();

        Map<String, List<BusinessProfile>> profilesByCategory =
                profiles.stream()
                        .collect(Collectors.groupingBy(
                                BusinessProfile::getCategory,
                                LinkedHashMap::new,
                                Collectors.toList()
                        ));

        model.addAttribute("profilesByCategory", profilesByCategory);

        return "businesses";
    }
}
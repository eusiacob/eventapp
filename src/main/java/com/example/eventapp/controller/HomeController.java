package com.example.eventapp.controller;

import com.example.eventapp.model.BusinessProfile;
import com.example.eventapp.service.BusinessProfileService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class HomeController {

    private final BusinessProfileService businessProfileService;

    public HomeController(BusinessProfileService businessProfileService) {
        this.businessProfileService = businessProfileService;
    }

    @GetMapping("/")
    public String home(@RequestParam(required = false, defaultValue = "") String category,
                       @RequestParam(required = false, defaultValue = "") String city,
                       @RequestParam (required = false, defaultValue = "") String keyword,
                       Model model) {

        model.addAttribute("businesses", businessProfileService.search(category, city, keyword));
        model.addAttribute("keyword", keyword);
        model.addAttribute("category", category);
        model.addAttribute("city", city);

        return "home";
    }

    @GetMapping("/favorites")
    public String favorites() {
        return "favorites";
    }
}
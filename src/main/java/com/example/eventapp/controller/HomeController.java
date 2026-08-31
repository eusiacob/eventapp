package com.example.eventapp.controller;

import com.example.eventapp.dto.BreadcrumbDTO;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home() {
        return "home";
    }

    @GetMapping("/terms")
    public String terms(Model model) {

        model.addAttribute("breadcrumbs", List.of(
                new BreadcrumbDTO("Acasă", "/businesses"),
                new BreadcrumbDTO("Termeni și condiții", null)
        ));

        return "terms";
    }

    @GetMapping("/privacy")
    public String privacy(Model model) {

        model.addAttribute("breadcrumbs", List.of(
                new BreadcrumbDTO("Acasă", "/businesses"),
                new BreadcrumbDTO("Politica de confidențialitate", null)
        ));

        return "privacy";
    }

    @GetMapping("/forgot")
    public String forgot() {
        return "forgot";
    }
}
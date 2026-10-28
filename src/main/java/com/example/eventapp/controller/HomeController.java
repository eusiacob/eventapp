package com.example.eventapp.controller;

import com.example.eventapp.dto.BreadcrumbDTO;
import com.example.eventapp.dto.RegisterUserDTO;
import com.example.eventapp.model.BusinessCategory;
import com.example.eventapp.model.LegalDocumentType;
import com.example.eventapp.service.LegalDocumentService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class HomeController {

    private final LegalDocumentService legalDocumentService;

    public HomeController(LegalDocumentService legalDocumentService) {
        this.legalDocumentService = legalDocumentService;
    }

    @GetMapping("/")
    public String home(
            @RequestParam(required = false) String auth,
            @RequestParam(required = false) String token,
            Model model
    ) {
        model.addAttribute("user", new RegisterUserDTO());
        model.addAttribute("authModalMode", auth == null ? "" : auth);
        if (token != null) {
            model.addAttribute("resetToken", token);
        }
        return "home";
    }

    @GetMapping("/prezentare")
    public String presentation() {
        return "prezentare";
    }

    @GetMapping("/prezentare-generala")
    public String generalPresentation(Model model) {
        model.addAttribute("categories", BusinessCategory.values());
        return "prezentare-generala";
    }

    @GetMapping("/terms")
    public String terms(Model model) {

        model.addAttribute("breadcrumbs", List.of(
                new BreadcrumbDTO("Acasă", "/businesses"),
                new BreadcrumbDTO("Termeni și condiții", null)
        ));
        model.addAttribute(
                "document",
                legalDocumentService.getDocument(LegalDocumentType.TERMS_AND_CONDITIONS)
        );

        return "terms";
    }

    @GetMapping("/privacy")
    public String privacy(Model model) {

        model.addAttribute("breadcrumbs", List.of(
                new BreadcrumbDTO("Acasă", "/businesses"),
                new BreadcrumbDTO("Politica de confidențialitate", null)
        ));
        model.addAttribute(
                "document",
                legalDocumentService.getDocument(LegalDocumentType.PRIVACY_POLICY)
        );

        return "privacy";
    }
}

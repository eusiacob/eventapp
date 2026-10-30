package com.example.eventapp.controller;

import com.example.eventapp.dto.BreadcrumbDTO;
import com.example.eventapp.model.BusinessCategory;
import com.example.eventapp.model.LegalDocumentType;
import com.example.eventapp.service.LegalDocumentService;
import org.springframework.stereotype.Controller;
import org.springframework.util.MultiValueMap;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class PublicPagesController {

    private final LegalDocumentService legalDocumentService;

    public PublicPagesController(LegalDocumentService legalDocumentService) {
        this.legalDocumentService = legalDocumentService;
    }

    @GetMapping("/")
    public String root(@RequestParam MultiValueMap<String, String> query) {
        return AuthPageRedirect.toPage(query.getFirst("auth"), query);
    }

    @GetMapping("/prezentare")
    public String presentation() {
        return "prezentare";
    }

    @GetMapping("/prezentare-generala")
    public String generalPresentation(Model model) {
        model.addAttribute("categories", BusinessCategory.values());
        model.addAttribute("breadcrumbs", List.of(
                new BreadcrumbDTO("Servicii", "/businesses"),
                new BreadcrumbDTO("Prezentare generală", null)
        ));
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

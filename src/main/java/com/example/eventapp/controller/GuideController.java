package com.example.eventapp.controller;

import com.example.eventapp.dto.BreadcrumbDTO;
import com.example.eventapp.seo.GuideArticle;
import com.example.eventapp.seo.GuideArticleCatalog;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Controller
public class GuideController {
    private final GuideArticleCatalog catalog;

    public GuideController(GuideArticleCatalog catalog) {
        this.catalog = catalog;
    }

    @GetMapping("/ghid")
    public String guides(Model model) {
        model.addAttribute("articles", catalog.all());
        model.addAttribute("breadcrumbs", List.of(
                new BreadcrumbDTO("Acasă", "/"),
                new BreadcrumbDTO("Ghid", null)));
        return "guides";
    }

    @GetMapping("/ghid/{slug}")
    public String guide(@PathVariable String slug, Model model) {
        GuideArticle article = catalog.find(slug)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND));
        model.addAttribute("article", article);
        model.addAttribute("breadcrumbs", List.of(
                new BreadcrumbDTO("Acasă", "/"),
                new BreadcrumbDTO("Ghid", "/ghid"),
                new BreadcrumbDTO(article.title(), null)));
        return "guide-article";
    }
}

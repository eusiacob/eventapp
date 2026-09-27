package com.example.eventapp.seo;

import com.example.eventapp.model.BusinessCategory;
import com.example.eventapp.repository.BusinessProfileRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriUtils;
import org.springframework.web.util.HtmlUtils;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@RestController
public class SitemapController {
    private static final int PAGE_SIZE = 5000;
    private static final String XML = "application/xml;charset=UTF-8";
    private final BusinessProfileRepository profiles;
    private final SeoService seo;

    public SitemapController(BusinessProfileRepository profiles, SeoService seo) {
        this.profiles = profiles;
        this.seo = seo;
    }

    @GetMapping(value = "/sitemap.xml", produces = XML)
    public ResponseEntity<String> index() {
        List<String> paths = new ArrayList<>(List.of("/sitemaps/pages.xml"));
        long pages = (profiles.countPublicSlugsForSitemap() + PAGE_SIZE - 1) / PAGE_SIZE;
        for (long page = 0; page < pages; page++) paths.add("/sitemaps/businesses-" + page + ".xml");
        return xml("sitemapindex", "sitemap", paths);
    }

    @GetMapping(value = "/sitemaps/pages.xml", produces = XML)
    public ResponseEntity<String> pages() {
        List<String> paths = new ArrayList<>(List.of("/", "/businesses", "/prezentare", "/prezentare-generala", "/contact", "/terms", "/privacy"));
        for (BusinessCategory category : profiles.findPublicCategoriesForSitemap()) {
            paths.add("/businesses/category/" + category.name());
        }
        return xml("urlset", "url", paths);
    }

    @GetMapping(value = "/sitemaps/businesses-{page}.xml", produces = XML)
    public ResponseEntity<String> businesses(@PathVariable int page) {
        if (page < 0 || (long) page * PAGE_SIZE >= profiles.countPublicSlugsForSitemap()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        List<String> paths = profiles.findPublicSlugsForSitemap(PageRequest.of(page, PAGE_SIZE)).stream()
                .map(slug -> "/business/" + UriUtils.encodePathSegment(slug, StandardCharsets.UTF_8)).toList();
        return xml("urlset", "url", paths);
    }

    private ResponseEntity<String> xml(String root, String item, List<String> paths) {
        StringBuilder xml = new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<")
                .append(root).append(" xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">\n");
        for (String path : paths) {
            xml.append('<').append(item).append("><loc>")
                    .append(HtmlUtils.htmlEscape(seo.absolute(path), "UTF-8"))
                    .append("</loc></").append(item).append(">\n");
        }
        xml.append("</").append(root).append('>');
        // No cache: deactivated or rejected services disappear on the next request.
        return ResponseEntity.ok().header("Cache-Control", "no-cache").body(xml.toString());
    }
}

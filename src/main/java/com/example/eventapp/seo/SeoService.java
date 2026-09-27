package com.example.eventapp.seo;

import com.example.eventapp.model.BusinessCategory;
import com.example.eventapp.model.BusinessProfile;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;
import tools.jackson.databind.json.JsonMapper;

import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class SeoService {
    private final String baseUrl;
    private final JsonMapper json = JsonMapper.builder().build();

    public SeoService(@Value("${app.seo.base-url:https://m-event.ro}") String baseUrl) {
        URI uri = URI.create(baseUrl.strip());
        if (!List.of("http", "https").contains(uri.getScheme()) || uri.getHost() == null
                || uri.getUserInfo() != null || uri.getQuery() != null || uri.getFragment() != null
                || (uri.getPath() != null && !uri.getPath().isEmpty() && !uri.getPath().equals("/"))) {
            throw new IllegalArgumentException("app.seo.base-url trebuie să fie o origine HTTP(S) fără cale sau parametri.");
        }
        this.baseUrl = baseUrl.strip().replaceAll("/+$", "");
    }

    public String absolute(String path) {
        return baseUrl + path;
    }

    public SeoMetadata forView(String view, Map<String, Object> model) {
        String path;
        String title;
        String description;
        String image = absolute("/images/logo_transparent.png");
        String robots = "index, follow, max-image-preview:large";
        List<Object> graph = new ArrayList<>();
        switch (view) {
            case "home" -> {
                path = "/";
                title = "M-Event – Furnizori și locații pentru evenimente în România";
                description = "Descoperă fotografi, DJ, formații, locații, catering și decor pentru nunți, botezuri și alte evenimente. Explorează serviciile pe M-Event.";
                graph.add(Map.of("@type", "WebSite", "@id", absolute("/#website"),
                        "url", absolute("/"), "name", "M-Event", "inLanguage", "ro-RO"));
                graph.add(Map.of("@type", "Organization", "@id", absolute("/#organization"),
                        "name", "M-Event", "url", absolute("/"), "logo", image));
            }
            case "businesses" -> {
                path = "/businesses";
                title = "Servicii pentru nunți, botezuri și alte evenimente speciale | M-Event";
                description = "Explorează categoriile de servicii pentru evenimente: fotografie, muzică, locații, catering, decor și transport. Găsește furnizorii potriviți pe M-Event.";
            }
            case "business-category" -> {
                BusinessCategory category = (BusinessCategory) model.get("selectedCategory");
                Page<?> page = (Page<?>) model.get("businessPage");
                path = "/businesses/category/" + category.name();
                title = category.getDisplayName() + " pentru evenimente în România"
                        + (page.getNumber() > 0 ? " – pagina " + (page.getNumber() + 1) : "") + " | M-Event";
                description = categoryDescription(category);
                UriComponentsBuilder canonical = UriComponentsBuilder.fromPath(path);
                boolean filtered = false;
                for (String key : List.of("keyword", "city", "eventDate", "serviceType", "eventType")) {
                    Object value = model.get(key);
                    if (value != null && !value.toString().isBlank()) {
                        filtered = true;
                        canonical.queryParam(key, value.toString().strip());
                    }
                }
                if (page.getNumber() > 0) canonical.queryParam("page", page.getNumber());
                path = canonical.build().encode().toUriString();
                if (filtered || page.isEmpty()) robots = "noindex, follow";
                graph.add(breadcrumbs(List.of("Acasă", "Servicii", category.getDisplayName()),
                        List.of("/", "/businesses", "/businesses/category/" + category.name())));
            }
            case "business-details" -> {
                BusinessProfile profile = (BusinessProfile) model.get("profile");
                Page<?> reviews = (Page<?>) model.get("reviewPage");
                int size = (int) model.get("reviewSize");
                path = "/business/" + profile.getSlug();
                String serviceUrl = absolute(path);
                if (reviews.getNumber() > 0 || size != 5) {
                    UriComponentsBuilder canonical = UriComponentsBuilder.fromPath(path);
                    if (reviews.getNumber() > 0) canonical.queryParam("reviewPage", reviews.getNumber());
                    if (size != 5) canonical.queryParam("reviewSize", size);
                    path = canonical.build().encode().toUriString();
                    if (size != 5) robots = "noindex, follow";
                }
                title = profile.getName() + " – " + profile.getCategory().getDisplayName()
                        + (!profile.isNationwide() && profile.getServiceCounties().size() == 1
                            ? " în " + profile.getServiceCounties().get(0) : "")
                        + (reviews.getNumber() > 0 ? " – recenzii, pagina " + (reviews.getNumber() + 1) : "") + " | M-Event";
                String publicDescription = profile.getDescription() == null ? "" : profile.getDescription().replaceAll("\\s+", " ").strip();
                description = publicDescription.length() > 165 ? publicDescription.substring(0, 162) + "…" : publicDescription;
                if (description.isBlank()) description = profile.getName() + " – " + profile.getCategory().getDisplayName() + " pe M-Event.";
                String cover = profile.getCoverImagePath();
                if (cover.startsWith("/uploads/businesses/")) image = absolute(cover);
                Map<String, Object> service = new LinkedHashMap<>();
                service.put("@type", "Service");
                service.put("@id", serviceUrl + "#service");
                service.put("name", profile.getName());
                service.put("serviceType", profile.getCategory().getDisplayName());
                service.put("url", serviceUrl);
                service.put("description", publicDescription);
                service.put("areaServed", profile.isNationwide()
                        ? List.of(Map.of("@type", "Country", "name", "România"))
                        : profile.getServiceCounties().stream().map(county -> Map.of("@type", "AdministrativeArea", "name", county)).toList());
                if (cover.startsWith("/uploads/businesses/")) service.put("image", image);
                graph.add(service);
                graph.add(breadcrumbs(List.of("Servicii", profile.getCategory().getDisplayName(), profile.getName()),
                        List.of("/businesses", "/businesses/category/" + profile.getCategory().name(),
                                "/business/" + profile.getSlug())));
            }
            case "prezentare" -> {
                path = "/prezentare";
                title = "Prezintă-ți locația pentru evenimente | M-Event";
                description = "Descoperă cum poți prezenta locația ta pentru evenimente pe M-Event, prin fotografii, videoclipuri și informații utile pentru clienți.";
                image = absolute("/images/prezentare-video-poster.jpg");
            }
            case "prezentare-generala" -> {
                path = "/prezentare-generala";
                title = "Prezintă-ți serviciile pentru evenimente | M-Event";
                description = "Ești fotograf, DJ, decorator sau furnizor de servicii pentru evenimente? Descoperă cum îți poți prezenta oferta și portofoliul pe M-Event.";
            }
            case "contact" -> {
                path = "/contact";
                title = "Contact M-Event – Întrebări și parteneriate";
                description = "Contactează echipa M-Event pentru întrebări despre platformă, asistență și colaborări.";
            }
            case "terms", "privacy" -> {
                path = "/" + view;
                title = (view.equals("terms") ? "Termeni și condiții" : "Politica de confidențialitate") + " | M-Event";
                description = "Informații despre " + (view.equals("terms") ? "utilizarea platformei" : "prelucrarea datelor personale pe") + " M-Event.";
            }
            default -> { return null; }
        }
        String structured = graph.isEmpty() ? null : scriptSafeJson(Map.of("@context", "https://schema.org", "@graph", graph));
        return new SeoMetadata(title, description, absolute(path), image, robots, structured);
    }

    private Map<String, Object> breadcrumbs(List<String> names, List<String> paths) {
        List<Object> items = new ArrayList<>();
        for (int i = 0; i < names.size(); i++) {
            items.add(Map.of("@type", "ListItem", "position", i + 1, "name", names.get(i), "item", absolute(paths.get(i))));
        }
        return Map.of("@type", "BreadcrumbList", "itemListElement", items);
    }

    String scriptSafeJson(Object value) {
        return json.writeValueAsString(value).replace("&", "\\u0026").replace("<", "\\u003c")
                .replace(">", "\\u003e").replace("\u2028", "\\u2028").replace("\u2029", "\\u2029");
    }

    public static String categoryDescription(BusinessCategory category) {
        return switch (category) {
            case FOTOGRAF -> "Descoperă fotografi pentru nunți, botezuri și alte evenimente. Alege județul și tipul de eveniment, apoi explorează profilurile și recenziile.";
            case VIDEOGRAF -> "Caută videografi pentru nunți, botezuri și evenimente speciale. Filtrează serviciile după județ, tipul evenimentului și data dorită.";
            case DJ -> "Descoperă DJ pentru nunți, petreceri și evenimente corporate. Explorează serviciile și filtrează după județ sau data evenimentului.";
            case MUZICA_LIVE -> "Găsește soliști și formații de muzică live pentru evenimentul tău. Explorează furnizorii în funcție de județ și tipul de serviciu.";
            case RESTAURANT -> "Explorează restaurante pentru nunți, botezuri, aniversări și alte evenimente. Filtrează după județ și tipul evenimentului.";
            case CANDY_BAR -> "Descoperă servicii de candy bar pentru nunți, botezuri și petreceri. Caută furnizori în județul dorit și explorează profilurile lor.";
            case CATERING -> "Găsește servicii de catering pentru evenimente private și corporate. Explorează furnizorii și filtrează după județ și tipul evenimentului.";
            case FLORIST -> "Descoperă furnizori de aranjamente florale pentru nunți, botezuri și evenimente. Explorează serviciile disponibile în județul dorit.";
            case DECOR -> "Explorează servicii de decor pentru nunți, botezuri și petreceri. Alege județul, tipul serviciului și evenimentul pe care îl organizezi.";
            case DIVERTISMENT -> "Descoperă servicii de divertisment pentru evenimente și petreceri. Filtrează furnizorii după județ, tipul serviciului și data dorită.";
            case LOCATII -> "Descoperă locații pentru nunți, botezuri, aniversări și evenimente corporate. Explorează profilurile și filtrează după județ și data evenimentului.";
            case ORGANIZATOR -> "Găsește organizatori de nunți și alte evenimente. Explorează serviciile oferite și filtrează furnizorii după județ și tipul evenimentului.";
            case TRANSPORT -> "Explorează servicii de transport pentru nunți și alte evenimente. Alege județul și tipul serviciului pentru a găsi furnizorii potriviți.";
            case ARTIFICII -> "Descoperă servicii de artificii pentru nunți și evenimente speciale. Explorează profilurile furnizorilor din județul dorit.";
            case ALTELE -> "Explorează alte servicii pentru nunți, botezuri și evenimente speciale. Filtrează după județ și tipul evenimentului.";
        };
    }
}

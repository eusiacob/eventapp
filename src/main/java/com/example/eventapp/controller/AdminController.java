package com.example.eventapp.controller;

import com.example.eventapp.dto.BreadcrumbDTO;
import com.example.eventapp.dto.LegalDocumentForm;
import com.example.eventapp.dto.UserAdminDto;
import com.example.eventapp.model.*;
import com.example.eventapp.repository.BusinessProfileRepository;
import com.example.eventapp.repository.ReviewRepository;
import com.example.eventapp.repository.SupportTicketRepository;
import com.example.eventapp.repository.UserRepository;
import com.example.eventapp.service.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Page;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final BusinessProfileRepository businessProfileRepository;
    private final BusinessProfileService businessProfileService;
    private final UserNotificationService userNotificationService;
    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final ReviewService reviewService;
    private final UserService userService;
    private final SubscriptionService subscriptionService;
    private final SupportTicketRepository supportTicketRepository;
    private final EmailService emailService;
    private final LegalDocumentService legalDocumentService;

    @GetMapping
    public String dashboard(Model model) {

        model.addAttribute("totalBusinesses",
                businessProfileRepository.count());
        model.addAttribute("pendingBusinesses",
                businessProfileRepository.countByStatus(BusinessProfile.BusinessStatus.PENDING));
        model.addAttribute("totalReviews",
                reviewRepository.count());
        model.addAttribute("pendingReviews",
                reviewRepository.countByReviewStatus(Review.ReviewStatus.PENDING));
        model.addAttribute("totalUsers",
                userRepository.count());
        model.addAttribute("totalSubscriptions",
                subscriptionService.count());
        model.addAttribute("plans",
                subscriptionService.findAllPlans().size());
        model.addAttribute("tickets",
                supportTicketRepository.findAll().size());

        return "admin/dashboard";
    }

    @GetMapping("/notifications")
    public String notifications(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            Model model
    ) {
        int pageNumber = Math.max(page, 0);
        model.addAttribute("recipients", userService.searchForNotificationRecipients(search));
        model.addAttribute("notificationPage", userNotificationService.getAdminNotificationHistory(
                PageRequest.of(pageNumber, 20, Sort.by(Sort.Direction.DESC, "createdAt"))));
        model.addAttribute("search", search == null ? "" : search);
        model.addAttribute("breadcrumbs", List.of(
                new BreadcrumbDTO("Dashboard", "/admin"),
                new BreadcrumbDTO("Trimite notificări", null)
        ));
        return "admin/notifications";
    }

    @GetMapping("/notifications/recipients")
    @ResponseBody
    public List<NotificationRecipientResponse> notificationRecipients(@RequestParam(required = false) String search) {
        return userService.searchForNotificationRecipients(search).stream()
                .map(user -> new NotificationRecipientResponse(
                        user.getId(), user.getFirstName(), user.getLastName(), user.getEmail()))
                .toList();
    }

    public record NotificationRecipientResponse(Long id, String firstName, String lastName, String email) {
    }

    @PostMapping("/notifications")
    public String sendNotification(
            @RequestParam String title,
            @RequestParam String message,
            @RequestParam(defaultValue = "ALL") String audience,
            @RequestParam(required = false) List<Long> userIds,
            RedirectAttributes redirectAttributes
    ) {
        String cleanTitle = title == null ? "" : title.trim();
        String cleanMessage = message == null ? "" : message.trim();
        if (cleanTitle.isBlank() || cleanMessage.isBlank()
                || cleanTitle.length() > 255 || cleanMessage.length() > 1000) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Titlul este obligatoriu și poate avea maximum 255 de caractere, iar mesajul maximum 1000."
            );
            return "redirect:/admin/notifications";
        }

        List<User> recipients = resolveNotificationRecipients(audience, userIds);
        int notified = userNotificationService.sendAdminNotification(cleanTitle, cleanMessage, recipients);
        redirectAttributes.addFlashAttribute(
                "successMessage", "Notificarea a fost trimisă către " + notified + " utilizatori."
        );
        return "redirect:/admin/notifications";
    }

    private List<User> resolveNotificationRecipients(String audience, List<Long> userIds) {
        Set<User> recipients = new LinkedHashSet<>();
        String normalizedAudience = audience == null ? "ALL" : audience.trim().toUpperCase();

        if ("LIST".equals(normalizedAudience)) {
            if (userIds == null || userIds.isEmpty()) return List.of();
            userIds.stream()
                    .map(userRepository::findById)
                    .flatMap(Optional::stream)
                    .forEach(recipients::add);
        } else {
            userRepository.findAll().stream()
                    .filter(user -> user.isEnabled() && user.getRole() != Role.ADMIN)
                    .filter(user -> switch (normalizedAudience) {
                        case "PROMOTE_SERVICES" -> user.getAccountPurpose() != null
                                && user.getAccountPurpose().includesPromotion();
                        case "SEARCH_SERVICES" -> user.getAccountPurpose() == AccountPurpose.SEARCH_SERVICES;
                        case "BOTH" -> user.getAccountPurpose() == AccountPurpose.BOTH;
                        default -> true;
                    })
                    .forEach(recipients::add);
        }
        return new ArrayList<>(recipients);
    }

    @GetMapping("/legal-documents")
    public String legalDocuments(Model model) {
        model.addAttribute("documents", legalDocumentService.getAllDocuments());
        model.addAttribute("breadcrumbs", List.of(
                new BreadcrumbDTO("Dashboard", "/admin"),
                new BreadcrumbDTO("Documente legale", null)
        ));
        return "admin/legal-documents";
    }

    @GetMapping("/legal-documents/{type}")
    public String editLegalDocument(
            @PathVariable LegalDocumentType type,
            Model model
    ) {
        LegalDocument document = legalDocumentService.getDocument(type);
        LegalDocumentForm form = new LegalDocumentForm();
        form.setContent(document.getContent());
        form.setVersion(document.getVersion());
        form.setLastUpdated(document.getLastUpdated());

        model.addAttribute("documentType", type);
        model.addAttribute("document", form);
        model.addAttribute("breadcrumbs", List.of(
                new BreadcrumbDTO("Dashboard", "/admin"),
                new BreadcrumbDTO("Documente legale", "/admin/legal-documents"),
                new BreadcrumbDTO("Editare document", null)
        ));
        return "admin/legal-document-edit";
    }

    @PostMapping("/legal-documents/{type}")
    public String updateLegalDocument(
            @PathVariable LegalDocumentType type,
            @Valid @ModelAttribute("document") LegalDocumentForm form,
            BindingResult result,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        if (result.hasErrors()) {
            model.addAttribute("documentType", type);
            model.addAttribute("breadcrumbs", List.of(
                    new BreadcrumbDTO("Dashboard", "/admin"),
                    new BreadcrumbDTO("Documente legale", "/admin/legal-documents"),
                    new BreadcrumbDTO("Editare document", null)
            ));
            return "admin/legal-document-edit";
        }

        boolean changed = legalDocumentService.updateDocument(type, form);
        redirectAttributes.addFlashAttribute(
                "successMessage",
                changed
                        ? "Documentul a fost actualizat. Notificările email pentru toți utilizatorii au fost puse în coada de trimitere. O versiune nouă va fi confirmată la următoarea autentificare."
                        : "Nu există modificări. Nu au fost programate notificări email."
        );
        return "redirect:/admin/legal-documents";
    }

    @GetMapping("/businesses")
    public String businesses(
            @RequestParam(required = false) BusinessProfile.BusinessStatus status,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "newest") String sort,
            Model model
    ) {

        int pageSize = validPageSize(size);
        Sort sortOrder = switch (sort) {
            case "oldest" -> Sort.by(Sort.Direction.ASC, "createdAt");
            case "nameAsc" -> Sort.by(Sort.Direction.ASC, "name");
            case "nameDesc" -> Sort.by(Sort.Direction.DESC, "name");
            default -> { sort = "newest"; yield Sort.by(Sort.Direction.DESC, "createdAt"); }
        };
        Page<BusinessProfile> businesses = businessProfileRepository.searchForAdmin(
                status, search, PageRequest.of(Math.max(0, page), pageSize, sortOrder));
        if (businesses.getTotalPages() > 0 && page >= businesses.getTotalPages()) {
            businesses = businessProfileRepository.searchForAdmin(status, search,
                    PageRequest.of(businesses.getTotalPages() - 1, pageSize, sortOrder));
        }

        model.addAttribute("businesses", businesses);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("search", search);
        model.addAttribute("sort", sort);
        model.addAttribute("size", pageSize);
        model.addAttribute("breadcrumbs", List.of(
                new BreadcrumbDTO("Dashboard", "/admin"),
                new BreadcrumbDTO("Servicii", null)
        ));

        return "admin/businesses";
    }

    @GetMapping("/business/{uuid}")
    public Object businessDetails(
            @PathVariable String uuid,
            Model model
    ) {

        BusinessProfile business = businessProfileService.findBySlugOrUuid(uuid);
        if (!uuid.equals(business.getSlug())) {
            return com.example.eventapp.util.BusinessSlugRedirect.to("/admin/business/" + business.getSlug());
        }

        model.addAttribute("business", business);
        model.addAttribute("ownerEmail", userService.getEmailAddress(business.getUser()));
        model.addAttribute("breadcrumbs", List.of(
                new BreadcrumbDTO("Dashboard", "/admin"),
                new BreadcrumbDTO("Servicii", "/admin/businesses"),
                new BreadcrumbDTO("Detalii serviciu", null)
        ));

        return "admin/business-details";
    }

    @PostMapping("/business/{uuid}/approve")
    public String approveBusiness(
            @PathVariable String uuid,
            RedirectAttributes redirectAttributes
    ) {

        BusinessProfile business =
                businessProfileService.findByUuid(uuid);

        business.setStatus(BusinessProfile.BusinessStatus.APPROVED);

        userNotificationService.create(

                business.getUser(),

                "Serviciu aprobat",

                business.getName()
                        + " este acum vizibil pe site.",

                "/business/"
                        + business.getUuid()

        );

        businessProfileService.save(business);

        emailService.sendBusinessApprovedEmail(
                userService.getEmailAddress(business.getUser()),
                business.getUser().getFirstName(),
                business.getName()
        );


        redirectAttributes.addAttribute(
                "success", true
        );


        return "redirect:/admin/business/" + uuid;
    }

    @GetMapping("/users")
    public String adminUsers(
            @RequestParam(required = false) Role role,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "newest") String sort,
            Model model
    ) {

        int pageSize = validPageSize(size);
        Sort sortOrder = switch (sort) {
            case "oldest" -> Sort.by(Sort.Direction.ASC, "createdAt");
            case "nameAsc" -> Sort.by(Sort.Direction.ASC, "name");
            case "nameDesc" -> Sort.by(Sort.Direction.DESC, "name");
            case "emailAsc" -> Sort.by(Sort.Direction.ASC, "email");
            case "emailDesc" -> Sort.by(Sort.Direction.DESC, "email");
            default -> { sort = "newest"; yield Sort.by(Sort.Direction.DESC, "createdAt"); }
        };
        Page<User> userPage = userService.searchForAdmin(role, search,
                PageRequest.of(Math.max(0, page), pageSize, sortOrder));
        if (userPage.getTotalPages() > 0 && page >= userPage.getTotalPages()) {
            userPage = userService.searchForAdmin(role, search,
                    PageRequest.of(userPage.getTotalPages() - 1, pageSize, sortOrder));
        }

        Page<UserAdminDto> userDtos = userPage.map(user -> new UserAdminDto(
                        user,
                        reviewService.countByUser(user)
                ));

        model.addAttribute("users", userDtos);
        model.addAttribute("selectedRole", role);
        model.addAttribute("search", search);
        model.addAttribute("sort", sort);
        model.addAttribute("size", pageSize);
        model.addAttribute("breadcrumbs", List.of(
                new BreadcrumbDTO("Dashboard", "/admin"),
                new BreadcrumbDTO("Utilizatori", null)
        ));

        return "admin/users";
    }

    @GetMapping("/user/{id}")
    public String userDetails(
            @PathVariable Long id,
            Model model
    ) {

        User user = userService.findById(id);

        model.addAttribute("user", user);
        model.addAttribute("subscription", subscriptionService.findActiveSubscription(user));
        model.addAttribute("businesses", user.getBusinessProfiles());
        model.addAttribute("latestReviews", reviewService.findLatestByUser(user));
        model.addAttribute("breadcrumbs", List.of(
                new BreadcrumbDTO("Dashboard", "/admin"),
                new BreadcrumbDTO("Utilizatori", "/admin/users"),
                new BreadcrumbDTO("Detalii utilizator", null)
        ));

        return "admin/user-details";
    }

    @PostMapping("/business/{uuid}/reject")
    public String rejectBusiness(
            @PathVariable String uuid,
            @RequestParam String reason,
            RedirectAttributes redirectAttributes
    ) {

        BusinessProfile business = businessProfileService.findByUuid(uuid);
        business.setStatus(BusinessProfile.BusinessStatus.REJECTED);

        userNotificationService.create(

                business.getUser(),

                "Serviciu respins",

                business.getName()
                        + " a fost respins. Motiv: "
                        + reason,
                "/business/"
                        + business.getUuid()
        );

        business.setRejectionReason(reason);
        businessProfileService.save(business);

        redirectAttributes.addAttribute(
                "rejected", true
        );

        return "redirect:/admin/business/" + uuid;
    }

    @GetMapping("/reviews")
    public String adminReviews(
            @RequestParam(required = false) Review.ReviewStatus status,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "newest") String sort,
            Model model
    ) {

        int pageSize = validPageSize(size);
        Sort sortOrder = switch (sort) {
            case "oldest" -> Sort.by(Sort.Direction.ASC, "createdAt");
            case "ratingDesc" -> Sort.by(Sort.Direction.DESC, "rating");
            case "ratingAsc" -> Sort.by(Sort.Direction.ASC, "rating");
            default -> { sort = "newest"; yield Sort.by(Sort.Direction.DESC, "createdAt"); }
        };
        Page<Review> reviews = reviewRepository.searchForAdmin(status, search,
                PageRequest.of(Math.max(0, page), pageSize, sortOrder));
        if (reviews.getTotalPages() > 0 && page >= reviews.getTotalPages()) {
            reviews = reviewRepository.searchForAdmin(status, search,
                    PageRequest.of(reviews.getTotalPages() - 1, pageSize, sortOrder));
        }

        model.addAttribute("reviews", reviews);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("search", search);
        model.addAttribute("sort", sort);
        model.addAttribute("size", pageSize);
        model.addAttribute("breadcrumbs", List.of(
                new BreadcrumbDTO("Dashboard", "/admin"),
                new BreadcrumbDTO("Recenzii", null)
        ));

        return "admin/reviews";
    }

    private int validPageSize(int size) {
        return switch (size) {
            case 5, 20 -> size;
            default -> 10;
        };
    }

    @GetMapping("review/{id}")
    public String reviewDetails(
            @PathVariable Long id,
            Model model
    ) {

        Review review = reviewService.findById(id);

        model.addAttribute("review", review);
        model.addAttribute("breadcrumbs", List.of(
                new BreadcrumbDTO("Dashboard", "/admin"),
                new BreadcrumbDTO("Recenzii", "/admin/reviews"),
                new BreadcrumbDTO("Detalii recenzie", null)
        ));

        return "admin/review-details";
    }

    @PostMapping("/review/{id}/approve")
    public String approveReview(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes
    ) {

        reviewService.approveReview(id);

        redirectAttributes.addAttribute(
                "reviewApproved",
                true
        );

        return "redirect:/admin/review/" + id;
    }

    @PostMapping("/review/{id}/reject")
    public String rejectReview(
            @PathVariable Long id,
            @RequestParam String reason,
            RedirectAttributes redirectAttributes
    ) {
        try {

            reviewService.rejectReview(id, reason);

            redirectAttributes.addFlashAttribute(
                    "reviewRejected", true);
        } catch (IllegalArgumentException ex) {

            redirectAttributes.addFlashAttribute(
                    "rejectError", ex.getMessage());
        }
        return "redirect:/admin/review/" + id;
    }

    @PostMapping("/user/{id}/toggle-enabled")
    public String toggleUserEnabled(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes
    ) {

        try {

            userService.toggleEnabled(
                    id,
                    userDetails.getUsername()
            );

            redirectAttributes.addFlashAttribute(
                    "successMessage",
                    "Starea contului a fost actualizată."
            );

        } catch (IllegalStateException ex) {

            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    ex.getMessage()
            );

        }

        return "redirect:/admin/user/" + id;
    }

    @GetMapping("/subscriptions")
    public String subscriptions(
            @RequestParam(required = false) String status,
            Model model
    ) {

        List<Subscription> subscriptions;

        if (status == null || status.isBlank()) {

            subscriptions = subscriptionService.findAll();

        } else {

            subscriptions = subscriptionService.findByStatus(
                    Subscription.SubscriptionStatus.valueOf(status)
            );

        }

        model.addAttribute("subscriptions", subscriptions);
        model.addAttribute("plans", subscriptionService.findAllPlans());
        model.addAttribute("currentStatus", status);
        model.addAttribute("breadcrumbs", List.of(
                new BreadcrumbDTO("Dashboard", "/admin"),
                new BreadcrumbDTO("Abonamente", null)
        ));

        return "admin/subscriptions";

    }

    @GetMapping("/plans")
    public String subscriptions(
            Model model
    ) {

        model.addAttribute("plans", subscriptionService.findAllPlans());
        model.addAttribute("breadcrumbs", List.of(
                new BreadcrumbDTO("Dashboard", "/admin"),
                new BreadcrumbDTO("Planuri de abonament", null)
        ));

        return "admin/plans";
    }

    @GetMapping("/subscription-plans/{id}/edit")
    public String editSubscriptionPlan(
            @PathVariable Long id,
            Model model
    ) {

        SubscriptionPlan plan = subscriptionService.findPlanById(id);

        model.addAttribute("plan", plan);

        return "admin/subscription-plan-edit";
    }

    @PostMapping("/subscription-plans/{id}/edit")
    public String updateSubscriptionPlan(
            @PathVariable Long id,
            @RequestParam BigDecimal price,
            @RequestParam(defaultValue = "false") boolean active
    ) {

        subscriptionService.updatePlan(id, price, active);

        return "redirect:/admin/plans";
    }

    // DOAR PENTRU TEST - VA FI FACUT AUTOMAT
    @PostMapping("/user/{id}/subscription/activate")
    public String activateSubscription(
            @PathVariable Long id
    ) {

        Subscription subscription = subscriptionService.findById(id);

        subscriptionService.activateSubscription(subscription);

        return "redirect:/admin/subscriptions";
    }

}

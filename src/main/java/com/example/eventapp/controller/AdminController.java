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
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private static final Logger LOGGER = LoggerFactory.getLogger(AdminController.class);

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
    private final EmailVerificationService emailVerificationService;
    private final LegalDocumentService legalDocumentService;
    private final AdminAuditService adminAuditService;

    private static final List<AuditActionOption> AUDIT_ACTIONS = List.of(
            new AuditActionOption("USER_EMAIL_CORRECTED", "Email corectat"),
            new AuditActionOption("USER_SUSPENDED", "Cont suspendat"),
            new AuditActionOption("USER_REACTIVATED", "Cont reactivat"),
            new AuditActionOption("BUSINESS_APPROVED", "Serviciu aprobat"),
            new AuditActionOption("BUSINESS_REJECTED", "Serviciu respins"),
            new AuditActionOption("REVIEW_APPROVED", "Recenzie aprobată"),
            new AuditActionOption("REVIEW_REJECTED", "Recenzie respinsă"),
            new AuditActionOption("SUPPORT_REPLY_SENT", "Răspuns trimis la suport"),
            new AuditActionOption("SUPPORT_STATUS_CHANGED", "Status suport schimbat"),
            new AuditActionOption("ADMIN_NOTIFICATION_SENT", "Notificare administrativă trimisă"),
            new AuditActionOption("LEGAL_DOCUMENT_UPDATED", "Document legal actualizat")
    );

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
        model.addAttribute("totalAdminAuditEntries", adminAuditService.countEntries());

        return "admin/dashboard";
    }

    @GetMapping("/audit-log")
    public String adminAuditLog(
            @RequestParam(required = false) Long adminId,
            @RequestParam(required = false) Long targetUserId,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "newest") String sort,
            Model model
    ) {
        List<String> actionTypes = AUDIT_ACTIONS.stream().map(AuditActionOption::value).toList();
        if (action != null && !action.isBlank() && !actionTypes.contains(action)) action = null;
        if (from != null && to != null && to.isBefore(from)) {
            model.addAttribute("filterError", "Data finală nu poate preceda data de început.");
            from = null;
            to = null;
        }

        int pageSize = switch (size) {
            case 10, 50, 100 -> size;
            default -> 20;
        };
        Sort order = switch (sort) {
            case "oldest" -> Sort.by(Sort.Direction.ASC, "createdAt");
            default -> {
                sort = "newest";
                yield Sort.by(Sort.Direction.DESC, "createdAt");
            }
        };
        LocalDateTime fromDate = from == null ? null : from.atStartOfDay();
        LocalDateTime toDateExclusive = to == null ? null : to.plusDays(1).atStartOfDay();
        Page<AdminAuditLog> auditPage = adminAuditService.search(
                adminId, targetUserId, action, fromDate, toDateExclusive,
                PageRequest.of(Math.max(0, page), pageSize, order));
        if (auditPage.getTotalPages() > 0 && page >= auditPage.getTotalPages()) {
            auditPage = adminAuditService.search(adminId, targetUserId, action, fromDate, toDateExclusive,
                    PageRequest.of(auditPage.getTotalPages() - 1, pageSize, order));
        }

        List<AuditAdminOption> auditAdmins = adminAuditService.findAdminIds().stream()
                .map(id -> userRepository.findById(id)
                        .map(user -> new AuditAdminOption(id, user.getFirstName() + " " + user.getLastName()))
                        .orElseGet(() -> new AuditAdminOption(id, "Cont admin șters")))
                .toList();

        model.addAttribute("auditPage", auditPage);
        model.addAttribute("adminOptions", auditAdmins);
        model.addAttribute("actionOptions", AUDIT_ACTIONS);
        model.addAttribute("adminId", adminId);
        model.addAttribute("targetUserId", targetUserId);
        model.addAttribute("action", action == null ? "" : action);
        model.addAttribute("from", from == null ? "" : from.toString());
        model.addAttribute("to", to == null ? "" : to.toString());
        model.addAttribute("pageSize", pageSize);
        model.addAttribute("sort", sort);
        model.addAttribute("breadcrumbs", List.of(
                new BreadcrumbDTO("Dashboard", "/admin"),
                new BreadcrumbDTO("Jurnal administrativ", null)
        ));
        return "admin/audit-log";
    }

    public record AuditActionOption(String value, String label) { }
    public record AuditAdminOption(Long id, String label) { }

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
            @AuthenticationPrincipal UserDetails userDetails,
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
        User admin = userService.findByEmail(userDetails.getUsername());
        String auditAudience = audience == null ? "ALL" : audience.trim().toUpperCase(java.util.Locale.ROOT);
        if (auditAudience.length() > 40) auditAudience = "UNKNOWN";
        adminAuditService.record(admin.getId(), null, "ADMIN_NOTIFICATION_SENT",
                "USER_AUDIENCE", auditAudience,
                "Notificare administrativă trimisă către " + notified + " utilizatori.");
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
            @AuthenticationPrincipal UserDetails userDetails,
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
        if (changed) {
            User admin = userService.findByEmail(userDetails.getUsername());
            adminAuditService.record(admin.getId(), null, "LEGAL_DOCUMENT_UPDATED",
                    "LEGAL_DOCUMENT", type.name(), "Document legal actualizat.");
        }
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
            @RequestParam(defaultValue = "5") int size,
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
            @AuthenticationPrincipal UserDetails userDetails,
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
        User admin = userService.findByEmail(userDetails.getUsername());
        adminAuditService.record(admin.getId(), business.getUser().getId(), "BUSINESS_APPROVED",
                "BUSINESS", business.getId().toString(), "Serviciu aprobat.");

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
            @RequestParam(defaultValue = "5") int size,
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
        model.addAttribute("auditEntries", adminAuditService.findForUser(id, PageRequest.of(0, 20)));
        model.addAttribute("breadcrumbs", List.of(
                new BreadcrumbDTO("Dashboard", "/admin"),
                new BreadcrumbDTO("Utilizatori", "/admin/users"),
                new BreadcrumbDTO("Detalii utilizator", null)
        ));

        return "admin/user-details";
    }

    @PostMapping("/user/{id}/correct-email")
    public String correctUnverifiedUserEmail(
            @PathVariable Long id,
            @RequestParam String newEmail,
            @AuthenticationPrincipal UserDetails adminDetails,
            RedirectAttributes redirectAttributes
    ) {
        try {
            User updatedUser = userService.correctUnverifiedEmail(id, newEmail);
            emailVerificationService.requestUserEmailVerification(updatedUser);
            User admin = userService.findByEmail(adminDetails.getUsername());
            LOGGER.info("Admin userId={} corrected unverified email for userId={}", admin.getId(), id);
            adminAuditService.record(admin.getId(), id, "USER_EMAIL_CORRECTED",
                    "USER", id.toString(), "Email corectat pentru un cont neverificat; verificarea reinițiată.");
            redirectAttributes.addFlashAttribute(
                    "successMessage", "Adresa a fost actualizată. A fost solicitat un nou link de verificare.");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/admin/user/" + id;
    }

    @PostMapping("/business/{uuid}/reject")
    public String rejectBusiness(
            @PathVariable String uuid,
            @RequestParam String reason,
            @AuthenticationPrincipal UserDetails userDetails,
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
        User admin = userService.findByEmail(userDetails.getUsername());
        adminAuditService.record(admin.getId(), business.getUser().getId(), "BUSINESS_REJECTED",
                "BUSINESS", business.getId().toString(), "Serviciu respins.");

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
            @RequestParam(defaultValue = "5") int size,
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
            case 5, 10, 20 -> size;
            default -> 5;
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
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes
    ) {

        Review review = reviewService.findById(id);
        reviewService.approveReview(id);
        User admin = userService.findByEmail(userDetails.getUsername());
        adminAuditService.record(admin.getId(), review.getUser().getId(), "REVIEW_APPROVED",
                "REVIEW", id.toString(), "Recenzie aprobată.");

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
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes
    ) {
        try {
            Review review = reviewService.findById(id);
            reviewService.rejectReview(id, reason);
            User admin = userService.findByEmail(userDetails.getUsername());
            adminAuditService.record(admin.getId(), review.getUser().getId(), "REVIEW_REJECTED",
                    "REVIEW", id.toString(), "Recenzie respinsă.");

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
            User targetUser = userService.findById(id);
            userService.toggleEnabled(
                    id,
                    userDetails.getUsername()
            );
            User admin = userService.findByEmail(userDetails.getUsername());
            User refreshedTarget = userService.findById(id);
            String action = refreshedTarget.isEnabled() ? "USER_REACTIVATED" : "USER_SUSPENDED";
            String summary = refreshedTarget.isEnabled() ? "Cont reactivat." : "Cont suspendat.";
            adminAuditService.record(admin.getId(), targetUser.getId(), action,
                    "USER", id.toString(), summary);

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

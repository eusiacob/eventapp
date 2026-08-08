package com.example.eventapp.controller;

import com.example.eventapp.dto.BreadcrumbDTO;
import com.example.eventapp.dto.UserAdminDto;
import com.example.eventapp.model.*;
import com.example.eventapp.repository.BusinessProfileRepository;
import com.example.eventapp.repository.ReviewRepository;
import com.example.eventapp.repository.UserRepository;
import com.example.eventapp.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

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

        return "admin/dashboard";
    }

    @GetMapping("/businesses")
    public String businesses(
            @RequestParam(required = false) BusinessProfile.BusinessStatus status,
            Model model
    ) {

        List<BusinessProfile> businesses;

        if (status == null) {

            businesses =
                    businessProfileRepository
                            .findAllByOrderByCreatedAtDesc();
        } else {

            businesses =
                    businessProfileRepository
                            .findByStatusOrderByCreatedAtDesc(status);
        }

        model.addAttribute("businesses", businesses);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("breadcrumbs", List.of(
                new BreadcrumbDTO("Dashboard", "/admin"),
                new BreadcrumbDTO("Servicii", null)
        ));

        return "admin/businesses";
    }

    @GetMapping("/business/{uuid}")
    public String businessDetails(
            @PathVariable String uuid,
            Model model
    ) {

        BusinessProfile business = businessProfileService.findByUuid(uuid);

        model.addAttribute("business", business);
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


        redirectAttributes.addAttribute(
                "success", true
        );


        return "redirect:/admin/business/" + uuid;
    }

    @GetMapping("/users")
    public String adminUsers(
            @RequestParam(required = false) Role role,
            Model model
    ) {

        List<User> users;

        if (role != null) {
            users = userService.findByRole(role);
        } else {
            users = userService.findAll();
        }

        List<UserAdminDto> userDtos = users.stream()
                .map(user -> new UserAdminDto(
                        user,
                        reviewService.countByUser(user)
                ))
                .toList();

        model.addAttribute("users", userDtos);
        model.addAttribute("selectedRole", role);
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
        model.addAttribute("subscription", subscriptionService.findByUser(user));
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
            Model model
    ) {

        List<Review> reviews;

        if (status != null) {
            reviews = reviewService.findByStatus(status);
        } else {
            reviews = reviewService.findAll();
        }

        model.addAttribute("reviews", reviews);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("breadcrumbs", List.of(
                new BreadcrumbDTO("Dashboard", "/admin"),
                new BreadcrumbDTO("Recenzii", null)
        ));

        return "admin/reviews";
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
                    "reviewRejected",
                    true
            );
        } catch (IllegalArgumentException ex) {

            redirectAttributes.addFlashAttribute(
                    "rejectError",
                    ex.getMessage()
            );
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
        model.addAttribute("currentStatus", status);
        model.addAttribute("breadcrumbs", List.of(
                new BreadcrumbDTO("Dashboard", "/admin"),
                new BreadcrumbDTO("Abonamente", null)
        ));

        return "admin/subscriptions";

    }

    //    DOAR PENTRU TEST - VA FI FACUT AUTOMAT
    @PostMapping("/user/{id}/subscription/activate")
    public String activateSubscription(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes
    ) {

        User user = userService.findById(id);

        subscriptionService.activateSubscription(
                user,
                Subscription.SubscriptionPlan.MONTHLY
        );

        redirectAttributes.addFlashAttribute(
                "successMessage",
                "Abonamentul a fost activat."
        );

        return "redirect:/admin/user/" + id;
    }
}

package com.example.eventapp.controller;

import com.example.eventapp.model.BusinessProfile;
import com.example.eventapp.model.Review;
import com.example.eventapp.repository.BusinessProfileRepository;
import com.example.eventapp.repository.ReviewRepository;
import com.example.eventapp.repository.UserRepository;
import com.example.eventapp.service.BusinessProfileService;
import com.example.eventapp.service.ReviewService;
import com.example.eventapp.service.UserNotificationService;
import lombok.RequiredArgsConstructor;
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
    private final BusinessProfileService  businessProfileService;
    private final UserNotificationService userNotificationService;
    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;
    private final ReviewService reviewService;

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


        model.addAttribute(
                "businesses",
                businesses
        );

        model.addAttribute(
                "selectedStatus",
                status
        );

        return "admin/businesses";
    }

    @GetMapping("/business/{uuid}")
    public String businessDetails(
            @PathVariable String uuid,
            Model model
    ) {

        BusinessProfile business =
                businessProfileService.findByUuid(uuid);

        model.addAttribute("business", business);

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

                "Serviciul "
                        + business.getName()
                        + " a fost aprobat.",

                "/business/"
                        + business.getUuid()

        );

        businessProfileService.save(business);


        redirectAttributes.addAttribute(
                "success", true
        );


        return "redirect:/admin/business/" + uuid;
    }

    @PostMapping("/business/{uuid}/reject")
    public String rejectBusiness(
            @PathVariable String uuid,
            @RequestParam String reason,
            RedirectAttributes redirectAttributes
    ) {


        BusinessProfile business =
                businessProfileService.findByUuid(uuid);


        business.setStatus(
                BusinessProfile.BusinessStatus.REJECTED
        );

        userNotificationService.create(

                business.getUser(),

                "Serviciu respins",

                "Serviciul "
                        + business.getName()
                        + " a fost respins. Motiv: "
                        + reason,
                "/business/"
                        + business.getUuid()
        );

        business.setRejectionReason(
                reason
        );


        businessProfileService.save(
                business
        );


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

        return "admin/reviews";
    }

    @GetMapping("review/{id}")
    public String reviewDetails(
            @PathVariable Long id,
            Model model
    ) {

        Review review = reviewService.findById(id);

        model.addAttribute("review", review);

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

        reviewService.rejectReview(id, reason);

        redirectAttributes.addAttribute(
                "reviewRejected",
                true
        );

        return "redirect:/admin/review/" + id;
    }
}

package com.example.eventapp.controller;

import com.example.eventapp.model.Review;
import com.example.eventapp.service.ReviewService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping("/business/{businessId}/reviews")
    public String addReview(@PathVariable Long businessId,
                            @Valid @ModelAttribute("review") Review review,
                            BindingResult result,
                            @AuthenticationPrincipal UserDetails userDetails,
                            RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            redirectAttributes.addAttribute("reviewError", true);
            return "redirect:/business/" + businessId;
        }

        try {
            reviewService.addReview(businessId, userDetails.getUsername(), review);
        } catch (RuntimeException e) {
            redirectAttributes.addAttribute("alreadyReviewed", true);
            return "redirect:/business/" + businessId;
        }

        return "redirect:/business/" + businessId;
    }

    @GetMapping("/reviews/edit/{id}")
    public String editReviewForm(@PathVariable Long id,
                                 @AuthenticationPrincipal UserDetails userDetails,
                                 Model model) {

        Review review = reviewService.findByIdAndValidateOwner(
                id,
                userDetails.getUsername()
        );

        model.addAttribute("review", review);

        return "review-edit";
    }

    @PostMapping("/reviews/edit/{id}")
    public String updateReview(@PathVariable Long id,
                               @Valid @ModelAttribute("review") Review review,
                               BindingResult result,
                               @AuthenticationPrincipal UserDetails userDetails,
                               Model model,
                               RedirectAttributes redirectAttributes) {

        Review existingReview = reviewService.findByIdAndValidateOwner(
                id,
                userDetails.getUsername()
        );

        Long businessId = existingReview.getBusinessProfile().getId();

        if (result.hasErrors()) {

            review.setId(existingReview.getId());
            review.setBusinessProfile(existingReview.getBusinessProfile());
            review.setUser(existingReview.getUser());
            review.setCreatedAt(existingReview.getCreatedAt());

            model.addAttribute("review", review);

            return "review-edit";
        }

        reviewService.updateReview(
                id,
                userDetails.getUsername(),
                review
        );

        redirectAttributes.addAttribute("reviewUpdated", true);

        return "redirect:/business/" + businessId;
    }

    @PostMapping("/reviews/delete/{id}")
    public String deleteReview(@PathVariable Long id,
                               @AuthenticationPrincipal UserDetails userDetails,
                               RedirectAttributes redirectAttributes) {

        Long businessId = reviewService.deleteReview(
                id,
                userDetails.getUsername()
        );

        redirectAttributes.addAttribute("reviewDeleted", true);

        return "redirect:/business/" + businessId;
    }
}
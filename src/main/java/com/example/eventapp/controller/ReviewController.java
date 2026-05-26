package com.example.eventapp.controller;


import com.example.eventapp.model.Review;
import com.example.eventapp.service.ReviewService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
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
            review.setId(null);

            reviewService.addReview(businessId, userDetails.getUsername(), review);

        } catch (RuntimeException e) {
            e.printStackTrace();
            redirectAttributes.addAttribute("reviewError", true);
            return "redirect:/business/" + businessId;
        }

        return "redirect:/business/" + businessId;
    }
}

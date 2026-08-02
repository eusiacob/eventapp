package com.example.eventapp.controller;

import com.example.eventapp.model.BusinessProfile;
import com.example.eventapp.model.Review;
import com.example.eventapp.repository.BusinessProfileRepository;
import com.example.eventapp.repository.ReviewRepository;
import com.example.eventapp.repository.UserRepository;
import com.example.eventapp.service.BusinessProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final BusinessProfileRepository businessProfileRepository;
    private final BusinessProfileService  businessProfileService;
    private final ReviewRepository reviewRepository;
    private final UserRepository userRepository;

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
    public String pendingBusinesses(Model model) {

        List<BusinessProfile> businesses =
                businessProfileRepository.findByStatusOrderByCreatedAtAsc(
                        BusinessProfile.BusinessStatus.PENDING);

        model.addAttribute("businesses", businesses);

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

        businessProfileService.save(business);


        redirectAttributes.addFlashAttribute(
                "success",
                "Business-ul a fost aprobat."
        );


        return "redirect:/admin/business/" + uuid;
    }
}

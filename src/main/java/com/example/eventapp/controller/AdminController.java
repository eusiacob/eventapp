package com.example.eventapp.controller;

import com.example.eventapp.model.BusinessProfile;
import com.example.eventapp.model.Review;
import com.example.eventapp.repository.BusinessProfileRepository;
import com.example.eventapp.repository.ReviewRepository;
import com.example.eventapp.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final BusinessProfileRepository businessProfileRepository;
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
                businessProfileRepository.findByStatus(
                        BusinessProfile.BusinessStatus.PENDING);

        model.addAttribute("businesses", businesses);

        return "admin/businesses";
    }
}

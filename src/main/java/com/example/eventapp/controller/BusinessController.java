package com.example.eventapp.controller;

import com.example.eventapp.model.BusinessProfile;
import com.example.eventapp.model.Review;
import com.example.eventapp.model.User;
import com.example.eventapp.repository.UserRepository;
import com.example.eventapp.service.BusinessProfileService;
import com.example.eventapp.service.ReviewService;
import com.example.eventapp.service.UserService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
public class BusinessController {

    private final BusinessProfileService service;
    private final UserRepository userRepository;
    private final ReviewService reviewService;
    private final UserService userService;

    public BusinessController(BusinessProfileService service, UserRepository userRepository, ReviewService reviewService, UserService userService) {

        this.service = service;
        this.userRepository = userRepository;
        this.reviewService = reviewService;
        this.userService = userService;
    }

    private boolean isOwner(BusinessProfile profile, UserDetails userDetails) {

        return profile.getUser().getEmail().equals(userDetails.getUsername());
    }

    @GetMapping("/business/create")
    public String showForm(Model model) {
        model.addAttribute("profile", new BusinessProfile());
        return "business-form";
    }

    @PostMapping("/business/create")
    public String createProfile(@Valid @ModelAttribute("profile") BusinessProfile profile, BindingResult result,
                                @RequestParam("imageFile") MultipartFile file,
                                @AuthenticationPrincipal UserDetails userDetails) throws IOException {

        if (result.hasErrors()) {
            return "business-form";
        }

        User user = userRepository.findByEmail(userDetails.getUsername()).orElseThrow();

        if (!file.isEmpty()) {

            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();

            Path uploadPath = Paths.get("uploads/");

            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String uploadDir = "uploads/";

            Path filePath = Paths.get(uploadDir + fileName);

            Files.write(filePath, file.getBytes());

            profile.setImagePath("/images/" + fileName);
        }

        profile.setUser(user);

        service.save(profile);

        return "redirect:/dashboard";
    }

    @GetMapping("/business/{id}")
    public String businessDetails(@PathVariable Long id,
                                  Model model,
                                  @AuthenticationPrincipal UserDetails userDetails) {

        BusinessProfile profile = service.findById(id);

        model.addAttribute("profile", profile);

        model.addAttribute("review", new Review());
        model.addAttribute("reviews", reviewService.getReviewsForBusiness(profile));
        model.addAttribute("averageRating", reviewService.getAverageRating(profile));
        model.addAttribute("reviewCount", reviewService.getReviewCount(profile));

        if (userDetails != null) {
            model.addAttribute("hasReviewed",
                    reviewService.hasUserReviewed(id, userDetails.getUsername()));

            User user = userService.findByEmail(userDetails.getUsername());

            boolean isFavorite = user.getFavoriteBusinesses()
                    .stream()
                    .anyMatch(b -> b.getId().equals(profile.getId()));

            model.addAttribute("isFavorite", isFavorite);
        } else {
            model.addAttribute("hasReviewed", false);
            model.addAttribute("isFavorite", false);
        }

        return "business-details";
    }

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal UserDetails userDetails, Model model) {

        User user = userRepository.findByEmail(userDetails.getUsername()).orElseThrow();

        List<BusinessProfile> profiles = user.getBusinessProfiles();

        model.addAttribute("profiles", profiles);

        return "dashboard";
    }

    @GetMapping("/business/edit/{id}")
    public String editForm(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails, Model model) {

        BusinessProfile profile = service.findById(id);

        if (!isOwner(profile, userDetails)) {
            return "redirect:/";
        }

        model.addAttribute("profile", profile);

        return "business-edit";
    }

    @PostMapping("/business/edit/{id}")
    public String updateProfile(@PathVariable Long id, @Valid @ModelAttribute("profile") BusinessProfile updatedProfile, BindingResult result, @AuthenticationPrincipal UserDetails userDetails) {

        if (result.hasErrors()) {
            return "business-edit";
        }

        BusinessProfile profile = service.findById(id);

        if (!isOwner(profile, userDetails)) {
            return "redirect:/";
        }

        profile.setName(updatedProfile.getName());
        profile.setCategory(updatedProfile.getCategory());
        profile.setCity(updatedProfile.getCity());
        profile.setPhone(updatedProfile.getPhone());
        profile.setDescription(updatedProfile.getDescription());

        service.save(profile);

        return "redirect:/dashboard";
    }

    @PostMapping("/business/delete/{id}")
    public String deleteProfile(@PathVariable Long id, @AuthenticationPrincipal UserDetails userDetails) {

        BusinessProfile profile = service.findById(id);

        if (!isOwner(profile, userDetails)) {
            return "redirect:/";
        }

        service.delete(id);

        return "redirect:/dashboard";
    }

    @GetMapping("/businesses/category/{category}")
    public String businessesByCategory(@PathVariable String category,
                                       @RequestParam(required = false) String keyword,
                                       @RequestParam(required = false) String city,
                                       Model model,
                                       @AuthenticationPrincipal UserDetails userDetails) {

        List<BusinessProfile> profiles =
                service.searchByCategoryNameAndCity(category, keyword, city);

        model.addAttribute("profiles", profiles);
        model.addAttribute("selectedCategory", category);
        model.addAttribute("keyword", keyword);
        model.addAttribute("city", city);
        model.addAttribute("cities", service.getCitiesByCategory(category));

        if (userDetails != null) {
            User user = userService.findByEmail(userDetails.getUsername());

            Set<Long> favoriteIds = user.getFavoriteBusinesses()
                    .stream()
                    .map(BusinessProfile::getId)
                    .collect(Collectors.toSet());

            model.addAttribute("favoriteIds", favoriteIds);
        } else {
            model.addAttribute("favoriteIds", Collections.emptySet());
        }

        return "business-category";
    }

}
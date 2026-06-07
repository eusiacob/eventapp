package com.example.eventapp.controller;

import com.example.eventapp.model.BusinessCategory;
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
    public String showCreateForm(Model model) {
        model.addAttribute("profile", new BusinessProfile());
        model.addAttribute("categories", service.getCategories());

        return "business-form";
    }

    @PostMapping("/business/create")
    public String createProfile(@Valid @ModelAttribute("profile") BusinessProfile profile, BindingResult result,
                                @RequestParam("imageFile") MultipartFile file, Model model,
                                @AuthenticationPrincipal UserDetails userDetails) throws IOException {

        if (result.hasErrors()) {
            model.addAttribute("categories", service.getCategories());
            return "business-form";
        }

        User user = userRepository.findByEmail(userDetails.getUsername()).orElseThrow();

        if (!file.isEmpty()) {

            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();

            Path uploadPath = Paths.get("uploads/images/");

            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String uploadDir = "uploads/images/";

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
    public String editForm(@PathVariable Long id,
                           Model model,
                           @AuthenticationPrincipal UserDetails userDetails) {

        User user = userService.findByEmail(userDetails.getUsername());

        BusinessProfile profile =
                service.findByIdAndValidateOwner(id, user);

        model.addAttribute("profile", profile);
        model.addAttribute("categories", service.getCategories());

        return "business-edit";
    }

    @PostMapping("/business/edit/{id}")
    public String updateBusiness(@PathVariable Long id,
                                 @Valid @ModelAttribute("profile") BusinessProfile profile,
                                 BindingResult result,
                                 @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
                                 @AuthenticationPrincipal UserDetails userDetails,
                                 Model model) throws IOException {

        User user = userService.findByEmail(userDetails.getUsername());

        BusinessProfile existingProfile =
                service.findByIdAndValidateOwner(id, user);

        if (result.hasErrors()) {
            profile.setId(existingProfile.getId());
            profile.setUser(existingProfile.getUser());
            profile.setImagePath(existingProfile.getImagePath());
            profile.setGalleryImages(existingProfile.getGalleryImages());

            model.addAttribute("categories", service.getCategories());

            return "business-edit";
        }

        existingProfile.setName(profile.getName());
        existingProfile.setCategory(profile.getCategory());
        existingProfile.setCity(profile.getCity());
        existingProfile.setPhone(profile.getPhone());
        existingProfile.setDescription(profile.getDescription());

        if (imageFile != null && !imageFile.isEmpty()) {

            Path uploadPath = Paths.get("uploads/images/");

            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            String originalFileName = imageFile.getOriginalFilename();

            assert originalFileName != null;
            String fileName = System.currentTimeMillis()
                    + "_"
                    + originalFileName.replaceAll("\\s+", "_");

            Path filePath = uploadPath.resolve(fileName);

            Files.write(filePath, imageFile.getBytes());

            existingProfile.setImagePath("/images/" + fileName);
        }

        service.save(existingProfile);

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
    public String businessesByCategory(@PathVariable BusinessCategory category,
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
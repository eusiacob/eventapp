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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
public class BusinessController {

    private final BusinessProfileService businessProfileService;
    private final UserRepository userRepository;
    private final ReviewService reviewService;
    private final UserService userService;

    public BusinessController(BusinessProfileService businessProfileService, UserRepository userRepository, ReviewService reviewService, UserService userService) {

        this.businessProfileService = businessProfileService;
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
        model.addAttribute("categories", businessProfileService.getCategories());

        return "business-form";
    }

    @PostMapping("/business/create")
    public String createProfile(@Valid @ModelAttribute("profile") BusinessProfile profile, BindingResult result,
                                @RequestParam("imageFile") MultipartFile file, Model model,
                                @AuthenticationPrincipal UserDetails userDetails, RedirectAttributes redirectAttributes) throws IOException {

        if (result.hasErrors()) {
            model.addAttribute("categories", businessProfileService.getCategories());
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

        businessProfileService.save(profile);

        redirectAttributes.addAttribute("businessCreated", true);

        return "redirect:/business/edit/" + profile.getId();
    }

    @GetMapping("/business/{id}")
    public String businessDetails(@PathVariable Long id,
                                  Model model,
                                  @AuthenticationPrincipal UserDetails userDetails) {

        BusinessProfile profile = businessProfileService.findById(id);

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

        model.addAttribute("businessSaved", new Review());

        return "dashboard";
    }

    @GetMapping("/business/edit/{id}")
    public String editBusinessForm(@PathVariable Long id,
                                   Model model,
                                   @AuthenticationPrincipal UserDetails userDetails) {

        User user = userService.findByEmail(userDetails.getUsername());

        BusinessProfile profile =
                businessProfileService.findByIdAndValidateOwner(id, user);

        List<String> unavailableDateStrings = profile.getUnavailableDates()
                .stream()
                .map(d -> d.getUnavailableDate().toString())
                .toList();

        model.addAttribute("profile", profile);
        model.addAttribute("categories", businessProfileService.getCategories());
        model.addAttribute("unavailableDateStrings", unavailableDateStrings);

        return "business-edit";
    }

    @PostMapping("/business/edit/{id}")
    public String updateBusiness(@PathVariable Long id,
                                 @Valid @ModelAttribute("profile") BusinessProfile profile,
                                 BindingResult result,
                                 @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
                                 @AuthenticationPrincipal UserDetails userDetails,
                                 Model model, RedirectAttributes redirectAttributes) throws IOException {

        User user = userService.findByEmail(userDetails.getUsername());

        BusinessProfile existingProfile =
                businessProfileService.findByIdAndValidateOwner(id, user);

        if (result.hasErrors()) {
            profile.setId(existingProfile.getId());
            profile.setUser(existingProfile.getUser());
            profile.setImagePath(existingProfile.getImagePath());
            profile.setGalleryImages(existingProfile.getGalleryImages());

            model.addAttribute("categories", businessProfileService.getCategories());

            return "business-edit";
        }

        existingProfile.setName(profile.getName());
        existingProfile.setCategory(profile.getCategory());
        existingProfile.setCity(profile.getCity());
        existingProfile.setPhone(profile.getPhone());
        existingProfile.setDescription(profile.getDescription());
        existingProfile.setEmail(profile.getEmail());
        existingProfile.setWebsite(profile.getWebsite());

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

        businessProfileService.save(existingProfile);

        redirectAttributes.addAttribute("businessCreated", true);

        return "redirect:/dashboard";
    }

    @PostMapping("/business/delete/{id}")
    public String deleteBusiness(@PathVariable Long id,
                                 @AuthenticationPrincipal UserDetails userDetails, RedirectAttributes redirectAttributes) {

        User user = userService.findByEmail(userDetails.getUsername());

        BusinessProfile profile =
                businessProfileService.findByIdAndValidateOwner(id, user);

        userService.removeBusinessFromAllFavorites(profile.getId());

        businessProfileService.delete(profile.getId());

        redirectAttributes.addAttribute("businessDeleted", true);

        return "redirect:/dashboard";
    }

    @GetMapping("/businesses/category/{category}")
    public String businessesByCategory(@PathVariable BusinessCategory category,
                                       @RequestParam(required = false) String keyword,
                                       @RequestParam(required = false) String city,
                                       @RequestParam(required = false) LocalDate eventDate,
                                       Model model,
                                       @AuthenticationPrincipal UserDetails userDetails) {

        List<BusinessProfile> profiles =
                businessProfileService.searchAvailableByCategoryNameCityAndDate(
                        category,
                        keyword,
                        city,
                        eventDate
                );

        model.addAttribute("profiles", profiles);
        model.addAttribute("selectedCategory", category);
        model.addAttribute("keyword", keyword);
        model.addAttribute("city", city);
        model.addAttribute("eventDate", eventDate);
        model.addAttribute("cities", businessProfileService.getCitiesByCategory(category));

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
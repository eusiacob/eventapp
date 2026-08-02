package com.example.eventapp.controller;

import com.example.eventapp.model.BusinessCategory;
import com.example.eventapp.model.BusinessProfile;
import com.example.eventapp.model.Review;
import com.example.eventapp.model.User;
import com.example.eventapp.repository.UserRepository;
import com.example.eventapp.service.BusinessImageService;
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
    private final BusinessImageService businessImageService;

    public BusinessController(BusinessProfileService businessProfileService, UserRepository userRepository, ReviewService reviewService, UserService userService, BusinessImageService businessImageService) {

        this.businessProfileService = businessProfileService;
        this.businessImageService = businessImageService;
        this.userRepository = userRepository;
        this.reviewService = reviewService;
        this.userService = userService;
    }

    @GetMapping("/businesses")
    public String businesses(Model model) {

        model.addAttribute("categories", BusinessCategory.values());

        model.addAttribute("premiumBusinesses",
                businessProfileService.getPremiumBusinesses());

        model.addAttribute(
                "mostFavoriteBusinesses",
                businessProfileService.getMostFavoriteBusinesses()
        );

        model.addAttribute(
                "topRatedBusinesses",
                businessProfileService.getTopRatedBusinesses()
        );

        return "businesses";
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

        profile.setUser(user);

        profile.setCreatedAt(LocalDate.now());

        businessProfileService.save(profile);

        if (file != null && !file.isEmpty()) {

            String categoryFolder = profile.getCategory()
                    .name()
                    .toLowerCase();


            Path uploadPath = Paths.get(
                    "uploads",
                    "businesses",
                    categoryFolder,
                    profile.getUuid()
            );


            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }


            String originalFileName =
                    file.getOriginalFilename();


            if (originalFileName != null &&
                    originalFileName.contains(".")) {


                String extension =
                        originalFileName.substring(
                                originalFileName.lastIndexOf(".")
                        );


                String fileName = "cover" + extension;


                Path filePath =
                        uploadPath.resolve(fileName);


                Files.write(
                        filePath,
                        file.getBytes()
                );


                profile.setImagePath(
                        "/uploads/businesses/"
                                + categoryFolder
                                + "/"
                                + profile.getUuid()
                                + "/"
                                + fileName
                );


                businessProfileService.save(profile);
            }
        }

        redirectAttributes.addAttribute("businessCreated", true);

        return "redirect:/business/edit/" + profile.getUuid();
    }

    @GetMapping("/business/{uuid}")
    public String businessDetails(@PathVariable String uuid,
                                  Model model,
                                  @AuthenticationPrincipal UserDetails userDetails) {

        BusinessProfile profile =
                businessProfileService.findByUuid(uuid);

        if (profile.getStatus() != BusinessProfile.BusinessStatus.APPROVED) {

            return "redirect:/business/edit/" + profile.getUuid();
        }

        model.addAttribute("profile", profile);

        model.addAttribute("review", new Review());
        model.addAttribute("reviews", reviewService.getReviewsForBusiness(profile));
        model.addAttribute("averageRating", reviewService.getAverageRating(profile));
        model.addAttribute("reviewCount", reviewService.getReviewCount(profile));

        if (userDetails != null) {

            model.addAttribute(
                    "hasReviewed",
                    reviewService.hasUserReviewed(
                            uuid,
                            userDetails.getUsername()
                    )
            );

            model.addAttribute(
                    "hasPendingReview",
                    reviewService.hasPendingReview(
                            uuid,
                            userDetails.getUsername()
                    )
            );

            User user =
                    userService.findByEmail(
                            userDetails.getUsername()
                    );

            boolean isFavorite = user.getFavoriteBusinesses()
                    .stream()
                    .anyMatch(b -> b.getId().equals(profile.getId()));

            model.addAttribute("isFavorite", isFavorite);

        } else {

            model.addAttribute("hasReviewed", false);
            model.addAttribute("hasPendingReview", false);
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

    @GetMapping("/business/edit/{uuid}")
    public String editBusinessForm(
            @PathVariable String uuid,
            Model model,
            @AuthenticationPrincipal UserDetails userDetails
    ) {

        User user = userService.findByEmail(
                userDetails.getUsername()
        );


        BusinessProfile profile =
                businessProfileService.findByUuidAndValidateOwner(
                        uuid,
                        user
                );


        List<String> unavailableDateStrings =
                profile.getUnavailableDates()
                        .stream()
                        .map(d -> d.getUnavailableDate().toString())
                        .toList();


        long currentImageCount =
                businessImageService.countImagesByBusinessId(
                        profile.getId()
                );


        model.addAttribute(
                "currentImageCount",
                currentImageCount
        );


        model.addAttribute(
                "maxImageCount",
                20
        );


        model.addAttribute(
                "profile",
                profile
        );


        model.addAttribute(
                "categories",
                businessProfileService.getCategories()
        );


        model.addAttribute(
                "unavailableDateStrings",
                unavailableDateStrings
        );


        return "business-edit";
    }

    @PostMapping("/business/edit/{uuid}")
    public String updateBusiness(
            @PathVariable String uuid,
            @Valid @ModelAttribute("profile") BusinessProfile profile,
            BindingResult result,
            @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
            @AuthenticationPrincipal UserDetails userDetails,
            Model model,
            RedirectAttributes redirectAttributes
    ) throws IOException {


        User user = userService.findByEmail(
                userDetails.getUsername()
        );


        BusinessProfile existingProfile =
                businessProfileService.findByUuidAndValidateOwner(
                        uuid,
                        user
                );


        if (result.hasErrors()) {

            profile.setId(existingProfile.getId());
            profile.setUuid(existingProfile.getUuid());
            profile.setUser(existingProfile.getUser());
            profile.setImagePath(existingProfile.getImagePath());
            profile.setGalleryImages(existingProfile.getGalleryImages());

            model.addAttribute(
                    "categories",
                    businessProfileService.getCategories()
            );

            return "business-edit";
        }


        existingProfile.setName(profile.getName());
        existingProfile.setCategory(profile.getCategory());
        existingProfile.setCity(profile.getCity());
        existingProfile.setPhone(profile.getPhone());
        existingProfile.setDescription(profile.getDescription());
        existingProfile.setEmail(profile.getEmail());
        existingProfile.setWebsite(profile.getWebsite());
        existingProfile.setStatus(BusinessProfile.BusinessStatus.PENDING);


        if (imageFile != null && !imageFile.isEmpty()) {


            Path uploadPath = Paths.get(
                    "uploads",
                    "businesses",
                    existingProfile.getUuid()
            );


            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }


            String originalFileName =
                    imageFile.getOriginalFilename();


            if (originalFileName != null &&
                    originalFileName.contains(".")) {


                String extension =
                        originalFileName.substring(
                                originalFileName.lastIndexOf(".")
                        );


                String fileName =
                        "cover" + extension;


                Path filePath =
                        uploadPath.resolve(fileName);


                Files.write(
                        filePath,
                        imageFile.getBytes()
                );


                existingProfile.setImagePath(
                        "/uploads/businesses/"
                                + existingProfile.getUuid()
                                + "/"
                                + fileName
                );
            }
        }


        businessProfileService.save(existingProfile);


        redirectAttributes.addAttribute(
                "businessUpdated",
                true
        );


        return "redirect:/dashboard";
    }

    @PostMapping("/business/delete/{uuid}")
    public String deleteBusiness(
            @PathVariable String uuid,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes
    ) {


        User user = userService.findByEmail(
                userDetails.getUsername()
        );


        BusinessProfile profile =
                businessProfileService.findByUuidAndValidateOwner(
                        uuid,
                        user
                );


        userService.removeBusinessFromAllFavorites(
                profile.getId()
        );


        businessProfileService.delete(
                profile.getId()
        );


        redirectAttributes.addAttribute(
                "businessDeleted",
                true
        );


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
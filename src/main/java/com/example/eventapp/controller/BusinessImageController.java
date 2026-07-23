package com.example.eventapp.controller;

import com.example.eventapp.model.BusinessProfile;
import com.example.eventapp.model.User;
import com.example.eventapp.service.BusinessImageService;
import com.example.eventapp.service.BusinessProfileService;
import com.example.eventapp.service.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.List;

@Controller
public class BusinessImageController {

    private final BusinessImageService businessImageService;
    private final BusinessProfileService businessProfileService;
    private final UserService userService;

    public BusinessImageController(BusinessImageService businessImageService,
                                   BusinessProfileService businessProfileService,
                                   UserService userService) {
        this.businessImageService = businessImageService;
        this.businessProfileService = businessProfileService;
        this.userService = userService;
    }

    @PostMapping("/business/{businessId}/gallery/upload")
    public String uploadGalleryImages(
            @PathVariable Long businessId,
            @RequestParam("images") List<MultipartFile> images,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {

        User user = userService.findByEmail(userDetails.getUsername());

        BusinessProfile businessProfile =
                businessProfileService.findByIdAndValidateOwner(
                        businessId,
                        user
                );

        long existingImages =
                businessImageService.countImagesByBusinessId(businessId);

        if (existingImages + images.size() > 20) {

            redirectAttributes.addFlashAttribute(
                    "galleryError",
                    "Galeria poate conține maximum 20 de imagini."
            );

            return "redirect:/business/edit/" + businessId;
        }

        try {

            businessImageService.uploadImages(
                    businessProfile.getId(),
                    images
            );

            redirectAttributes.addFlashAttribute(
                    "gallerySuccess",
                    "Imaginile au fost încărcate cu succes."
            );

        } catch (IOException e) {

            redirectAttributes.addFlashAttribute(
                    "galleryError",
                    "A apărut o eroare la încărcarea imaginilor."
            );
        }


        return "redirect:/business/edit/" + businessId;
    }

    @PostMapping("/business/gallery/delete/{imageId}")
    public String deleteGalleryImage(@PathVariable Long imageId,
                                     @RequestParam("businessId") Long businessId,
                                     @AuthenticationPrincipal UserDetails userDetails,
                                     RedirectAttributes redirectAttributes) {

        User user = userService.findByEmail(userDetails.getUsername());

        businessProfileService.findByIdAndValidateOwner(businessId, user);

        businessImageService.deleteImage(imageId);

        redirectAttributes.addAttribute("galleryDeleted", true);

        return "redirect:/business/edit/" + businessId;
    }
}
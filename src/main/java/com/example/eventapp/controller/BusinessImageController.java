package com.example.eventapp.controller;

import com.example.eventapp.exception.InvalidVideoException;
import com.example.eventapp.model.BusinessProfile;
import com.example.eventapp.model.Role;
import com.example.eventapp.model.User;
import com.example.eventapp.repository.UserRepository;
import com.example.eventapp.service.*;
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
    private final UserRepository userRepository;
    private final UserNotificationService userNotificationService;
    private final BusinessVideoService businessVideoService;

    public BusinessImageController(BusinessImageService businessImageService,
                                   BusinessProfileService businessProfileService,
                                   BusinessVideoService businessVideoService,
                                   UserService userService, UserRepository userRepository, UserNotificationService userNotificationService) {
        this.businessImageService = businessImageService;
        this.businessProfileService = businessProfileService;
        this.userService = userService;
        this.businessVideoService = businessVideoService;
        this.userRepository = userRepository;
        this.userNotificationService = userNotificationService;
    }

    @PostMapping("/business/{uuid}/gallery/upload")
    public String uploadGalleryImages(
            @PathVariable String uuid,
            @RequestParam("images") List<MultipartFile> images,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {


        User user = userService.findByEmail(
                userDetails.getUsername()
        );


        BusinessProfile businessProfile =
                businessProfileService.findByUuidAndValidateOwner(
                        uuid,
                        user
                );


        long existingImages =
                businessImageService.countImagesByBusinessId(
                        businessProfile.getId()
                );


        if (existingImages + images.size() > 20) {

            redirectAttributes.addFlashAttribute(
                    "galleryError",
                    "Galeria poate conține maximum 15 de imagini."
            );


            return "redirect:/business/edit/" + uuid;
        }

        try {

            businessImageService.uploadImages(businessProfile.getId(), images);

            redirectAttributes.addFlashAttribute("gallerySuccess", "Imaginile au fost încărcate cu succes.");

        } catch (IOException e) {

            redirectAttributes.addFlashAttribute("galleryError", "A apărut o eroare la încărcarea imaginilor.");
        }

        List<User> admins =
                userRepository.findByRole(Role.ADMIN);

        for (User admin : admins) {

            userNotificationService.create(

                    admin,

                    "Galerie actualizată",

                    businessProfile.getName()
                            + " a fost editat și trimis pentru aprobare.",

                    "/admin/business/"
                            + businessProfile.getUuid()
            );

        }

        redirectAttributes.addAttribute("businessUpdated", true);
        redirectAttributes.addAttribute("businessNotApproved", true);

        return "redirect:/business/edit/" + uuid;
    }

    @PostMapping("/business/{uuid}/videos/upload")
    public String uploadVideos(
            @PathVariable String uuid,
            @RequestParam("videos") List<MultipartFile> videos,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes
    ) {

        User user =
                userService.findByEmail(
                        userDetails.getUsername()
                );

        BusinessProfile businessProfile =
                businessProfileService
                        .findByUuidAndValidateOwner(
                                uuid,
                                user
                        );

        long existingVideos =
                businessVideoService
                        .countVideosByBusinessId(
                                businessProfile.getId()
                        );

        if (existingVideos + videos.size() > 5) {

            redirectAttributes.addFlashAttribute(
                    "videoError",
                    "Galeria poate conține maximum 5 videoclipuri."
            );

            return "redirect:/business/edit/" + uuid;
        }

        try {

            businessVideoService.uploadVideos(
                    businessProfile.getId(),
                    videos
            );

            redirectAttributes.addFlashAttribute(
                    "videoSuccess",
                    "Videoclipurile au fost încărcate cu succes."
            );

        } catch (InvalidVideoException e) {

            redirectAttributes.addFlashAttribute(
                    "videoError",
                    e.getMessage()
            );

            return "redirect:/business/edit/" + uuid;

        } catch (IOException e) {

            e.printStackTrace();

            redirectAttributes.addFlashAttribute(
                    "videoError",
                    "A apărut o eroare la încărcarea videoclipurilor."
            );
        }

        return "redirect:/business/edit/" + uuid;
    }

    @PostMapping("/business/gallery/delete/{imageId}")
    public String deleteGalleryImage(
            @PathVariable Long imageId,
            @RequestParam("uuid") String uuid,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {


        User user = userService.findByEmail(
                userDetails.getUsername()
        );


        businessProfileService.findByUuidAndValidateOwner(
                uuid,
                user
        );


        businessImageService.deleteImage(imageId);


        redirectAttributes.addAttribute(
                "galleryDeleted",
                true
        );


        return "redirect:/business/edit/" + uuid;
    }
}
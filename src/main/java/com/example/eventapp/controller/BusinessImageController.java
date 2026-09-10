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

        if (images == null ||
                images.isEmpty() ||
                images.stream().allMatch(MultipartFile::isEmpty)) {

            redirectAttributes.addFlashAttribute(
                    "galleryError",
                    "Selectează cel puțin o fotografie."
            );

            return "redirect:/business/edit/" + uuid
                    + "#galleryUpload";
        }

        if (existingImages + images.size() > 15) {

            redirectAttributes.addFlashAttribute(
                    "galleryError",
                    "Galeria poate conține maximum 15 imagini."
            );

            return "redirect:/business/edit/" + uuid
                    + "#galleryUpload";
        }

        try {

            businessImageService.uploadImages(
                    businessProfile.getId(),
                    images
            );

            notifyAdminsAboutBusinessUpdate(
                    businessProfile
            );

            redirectAttributes.addFlashAttribute(
                    "gallerySuccess",
                    "Imaginile au fost încărcate cu succes."
            );

            redirectAttributes.addAttribute(
                    "businessUpdated",
                    true
            );

            redirectAttributes.addAttribute(
                    "businessNotApproved",
                    true
            );

            return "redirect:/business/edit/" + uuid
                    + "#currentGalleryPhotos";

        } catch (IOException e) {

            redirectAttributes.addFlashAttribute(
                    "galleryError",
                    "A apărut o eroare la încărcarea imaginilor."
            );

            return "redirect:/business/edit/" + uuid
                    + "#galleryUpload";
        }
    }

    @PostMapping("/business/{uuid}/videos/upload")
    public String uploadVideos(
            @PathVariable String uuid,
            @RequestParam("videos") List<MultipartFile> videos,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes
    ) {

        if (videos == null ||
                videos.isEmpty() ||
                videos.stream().allMatch(MultipartFile::isEmpty)) {

            redirectAttributes.addFlashAttribute(
                    "videoError",
                    "Selectează cel puțin un videoclip."
            );

            return "redirect:/business/edit/" + uuid
                    + "#videoUpload";
        }

        User user = userService.findByEmail(
                userDetails.getUsername()
        );

        BusinessProfile businessProfile =
                businessProfileService.findByUuidAndValidateOwner(
                        uuid,
                        user
                );

        long existingVideos =
                businessVideoService.countVideosByBusinessId(
                        businessProfile.getId()
                );

        if (existingVideos + videos.size() > 5) {

            redirectAttributes.addFlashAttribute(
                    "videoError",
                    "Galeria poate conține maximum 5 videoclipuri."
            );

            return "redirect:/business/edit/" + uuid
                    + "#videoUpload";
        }

        try {

            businessVideoService.uploadVideos(
                    businessProfile.getId(),
                    videos
            );

            notifyAdminsAboutBusinessUpdate(
                    businessProfile
            );

            redirectAttributes.addFlashAttribute(
                    "videoSuccess",
                    "Videoclipurile au fost încărcate cu succes."
            );

            redirectAttributes.addAttribute(
                    "businessUpdated",
                    true
            );

            redirectAttributes.addAttribute(
                    "businessNotApproved",
                    true
            );

            return "redirect:/business/edit/" + uuid
                    + "#currentGalleryVideos";

        } catch (InvalidVideoException e) {

            redirectAttributes.addFlashAttribute(
                    "videoError",
                    e.getMessage()
            );

            return "redirect:/business/edit/" + uuid
                    + "#videoUpload";

        } catch (IOException e) {

            redirectAttributes.addFlashAttribute(
                    "videoError",
                    "A apărut o eroare la încărcarea videoclipurilor."
            );

            return "redirect:/business/edit/" + uuid
                    + "#videoUpload";

        } catch (InterruptedException e) {

            Thread.currentThread().interrupt();

            redirectAttributes.addFlashAttribute(
                    "videoError",
                    "Procesarea videoclipului a fost întreruptă."
            );

            return "redirect:/business/edit/" + uuid
                    + "#videoUpload";
        }
    }

    private void notifyAdminsAboutBusinessUpdate(
            BusinessProfile businessProfile
    ) {

        List<User> admins =
                userRepository.findByRole(Role.ADMIN);

        for (User admin : admins) {

            userNotificationService.create(

                    admin,

                    "Galerie actualizată",

                    "Business-ul "
                            + businessProfile.getName()
                            + " a fost modificat și trimis din nou pentru aprobare.",

                    "/admin/business/"
                            + businessProfile.getUuid()
            );
        }
    }

    @PostMapping("/business/gallery/delete/{id}")
    public String deleteImage(
            @PathVariable Long id,
            @RequestParam("uuid") String uuid,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes
    ) {

        User user =
                userService.findByEmail(
                        userDetails.getUsername()
                );

        try {

            BusinessProfile businessProfile =
                    businessImageService.deleteImage(
                            id,
                            user
                    );

            notifyAdminsAboutBusinessUpdate(
                    businessProfile
            );


            redirectAttributes.addFlashAttribute(
                    "imageDeleteSuccess",
                    "Imaginea a fost ștearsă cu succes."
            );


        } catch (IOException e) {

            redirectAttributes.addFlashAttribute(
                    "imageDeleteError",
                    "Imaginea nu a putut fi ștearsă de pe disc."
            );

        } catch (RuntimeException e) {

            redirectAttributes.addFlashAttribute(
                    "imageOtherError",
                    e.getMessage()
            );
        }

        redirectAttributes.addAttribute("businessUpdated", true);
        redirectAttributes.addAttribute("businessNotApproved", true);

        return "redirect:/business/edit/" + uuid + "#currentGalleryPhotos";
    }

    @PostMapping("/business/videos/delete/{id}")
    public String deleteVideo(
            @PathVariable Long id,
            @RequestParam("uuid") String uuid,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes
    ) {

        User user =
                userService.findByEmail(
                        userDetails.getUsername()
                );

        try {

            BusinessProfile businessProfile =
                    businessVideoService.deleteVideo(
                            id,
                            user
                    );

            notifyAdminsAboutBusinessUpdate(
                    businessProfile
            );

            redirectAttributes.addFlashAttribute(
                    "videoDeleteSuccess",
                    "Videoclipul a fost șters cu succes."
            );


        } catch (IOException e) {

            redirectAttributes.addFlashAttribute(
                    "videoDeleteError",
                    "Videoclipul nu a putut fi șters de pe disc."
            );

        } catch (RuntimeException e) {

            redirectAttributes.addFlashAttribute(
                    "videoError",
                    e.getMessage()
            );
        }

        redirectAttributes.addAttribute("businessUpdated", true);
        redirectAttributes.addAttribute("businessNotApproved", true);

        return "redirect:/business/edit/" + uuid + "#currentGalleryVideos";
    }
}
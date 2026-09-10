package com.example.eventapp.controller;

import com.example.eventapp.exception.InvalidVideoException;
import com.example.eventapp.model.BusinessProfile;
import com.example.eventapp.model.Role;
import com.example.eventapp.model.User;
import com.example.eventapp.repository.UserRepository;
import com.example.eventapp.service.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

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
    @ResponseBody
    public ResponseEntity<ActionResponse> uploadGalleryImages(
            @PathVariable String uuid,
            @RequestParam(value = "images", required = false) List<MultipartFile> images,
            @AuthenticationPrincipal UserDetails userDetails) {

        if (images == null ||
                images.isEmpty() ||
                images.stream().allMatch(MultipartFile::isEmpty)) {

            return error(
                    HttpStatus.BAD_REQUEST,
                    "Selectează cel puțin o fotografie."
            );
        }

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

        if (existingImages + images.size() > 15) {

            return error(
                    HttpStatus.BAD_REQUEST,
                    "Galeria poate conține maximum 15 imagini."
            );
        }

        try {

            businessImageService.uploadImages(
                    businessProfile.getId(),
                    images
            );

            notifyAdminsAboutBusinessUpdate(
                    businessProfile
            );

            return success(
                    "Imaginile au fost încărcate cu succes."
            );

        } catch (IOException e) {

            return error(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "A apărut o eroare la încărcarea imaginilor."
            );
        }
    }

    @PostMapping("/business/{uuid}/videos/upload")
    @ResponseBody
    public ResponseEntity<ActionResponse> uploadVideos(
            @PathVariable String uuid,
            @RequestParam(value = "videos", required = false) List<MultipartFile> videos,
            @AuthenticationPrincipal UserDetails userDetails
    ) {

        if (videos == null ||
                videos.isEmpty() ||
                videos.stream().allMatch(MultipartFile::isEmpty)) {

            return error(
                    HttpStatus.BAD_REQUEST,
                    "Selectează cel puțin un videoclip."
            );
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

            return error(
                    HttpStatus.BAD_REQUEST,
                    "Galeria poate conține maximum 5 videoclipuri."
            );
        }

        try {

            businessVideoService.uploadVideos(
                    businessProfile.getId(),
                    videos
            );

            notifyAdminsAboutBusinessUpdate(
                    businessProfile
            );

            return success(
                    "Videoclipurile au fost încărcate cu succes."
            );

        } catch (InvalidVideoException e) {

            return error(
                    HttpStatus.BAD_REQUEST,
                    e.getMessage()
            );

        } catch (IOException e) {

            return error(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "A apărut o eroare la încărcarea videoclipurilor."
            );

        } catch (InterruptedException e) {

            Thread.currentThread().interrupt();

            return error(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Procesarea videoclipului a fost întreruptă."
            );
        }
    }

    private ResponseEntity<ActionResponse> success(String message) {
        return ResponseEntity.ok(
                new ActionResponse(true, message)
        );
    }

    private ResponseEntity<ActionResponse> error(
            HttpStatus status,
            String message
    ) {
        return ResponseEntity.status(status)
                .body(new ActionResponse(false, message));
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
    @ResponseBody
    public ResponseEntity<ActionResponse> deleteImage(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails
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


            return success(
                    "Imaginea a fost ștearsă cu succes."
            );

        } catch (IOException e) {

            return error(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Imaginea nu a putut fi ștearsă de pe disc."
            );

        } catch (RuntimeException e) {

            return error(
                    HttpStatus.BAD_REQUEST,
                    e.getMessage()
            );
        }
    }

    @PostMapping("/business/videos/delete/{id}")
    @ResponseBody
    public ResponseEntity<ActionResponse> deleteVideo(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails
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

            return success(
                    "Videoclipul a fost șters cu succes."
            );

        } catch (IOException e) {

            return error(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Videoclipul nu a putut fi șters de pe disc."
            );

        } catch (RuntimeException e) {

            return error(
                    HttpStatus.BAD_REQUEST,
                    e.getMessage()
            );
        }
    }

    public record ActionResponse(
            boolean success,
            String message
    ) {
    }
}

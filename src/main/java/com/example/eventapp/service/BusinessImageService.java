package com.example.eventapp.service;

import com.example.eventapp.model.BusinessImage;
import com.example.eventapp.model.BusinessProfile;
import com.example.eventapp.model.User;
import com.example.eventapp.repository.BusinessImageRepository;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.UUID;

@Service
public class BusinessImageService {

    private final BusinessImageRepository businessImageRepository;
    private final BusinessProfileService businessProfileService;

    public BusinessImageService(BusinessImageRepository businessImageRepository,
                                BusinessProfileService businessProfileService) {
        this.businessImageRepository = businessImageRepository;
        this.businessProfileService = businessProfileService;
    }

    public void uploadImages(
            Long businessId,
            List<MultipartFile> files
    ) throws IOException {

        BusinessProfile businessProfile =
                businessProfileService.findById(businessId);

        String category =
                businessProfile.getCategory()
                        .name()
                        .toLowerCase();

        Path uploadPath =
                Paths.get(
                        "uploads",
                        "businesses",
                        category,
                        businessProfile.getUuid()
                );

        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        long currentImages =
                businessImageRepository
                        .countByBusinessProfile(businessProfile);

        boolean uploaded = false;

        for (MultipartFile file : files) {

            if (file.isEmpty()) {
                continue;
            }

            String contentType = file.getContentType();

            if (contentType == null ||
                    !contentType.startsWith("image/")) {

                continue;
            }

            if (file.getSize() > 10 * 1024 * 1024) {
                continue;
            }

            if (currentImages >= 15) {
                break;
            }

            String fileName =
                    "gallery_"
                            + UUID.randomUUID()
                            + ".webp";

            currentImages++;

            Path filePath =
                    uploadPath.resolve(fileName);

            ByteArrayOutputStream outputStream =
                    new ByteArrayOutputStream();

            Thumbnails.of(file.getInputStream())
                    .size(1920, 1920)
                    .outputFormat("jpg")
                    .outputQuality(0.82)
                    .toOutputStream(outputStream);

            Files.write(
                    filePath,
                    outputStream.toByteArray()
            );

            BusinessImage image =
                    new BusinessImage();

            image.setImagePath(
                    "/uploads/businesses/"
                            + category
                            + "/"
                            + businessProfile.getUuid()
                            + "/"
                            + fileName
            );

            image.setBusinessProfile(
                    businessProfile
            );

            businessImageRepository.save(image);

            uploaded = true;
        }

        if (uploaded) {

            businessProfile.setStatus(
                    BusinessProfile.BusinessStatus.PENDING
            );

            businessProfileService.save(businessProfile);
        }
    }

    public long countImagesByBusinessId(Long businessId) {

        return businessImageRepository
                .countByBusinessProfileId(businessId);
    }

    public BusinessProfile deleteImage(
            Long imageId,
            User user
    ) throws IOException {

        BusinessImage image =
                businessImageRepository.findById(imageId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Imaginea nu există."
                                ));

        BusinessProfile businessProfile =
                image.getBusinessProfile();

        if (businessProfile == null ||
                businessProfile.getUser() == null ||
                !businessProfile.getUser().getId()
                        .equals(user.getId())) {

            throw new RuntimeException(
                    "Nu ai permisiunea să ștergi această imagine."
            );
        }

        String imagePath =
                image.getImagePath();

        if (imagePath != null) {

            String relativePath =
                    imagePath.startsWith("/")
                            ? imagePath.substring(1)
                            : imagePath;

            Path filePath =
                    Paths.get(relativePath);

            if (Files.exists(filePath)) {
                Files.delete(filePath);
            }
        }

        businessProfile.setStatus(
                BusinessProfile.BusinessStatus.PENDING
        );

        businessProfileService.save(businessProfile);

        businessImageRepository.delete(image);

        return businessProfile;


    }
}
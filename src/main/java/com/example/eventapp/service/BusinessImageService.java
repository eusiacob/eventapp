package com.example.eventapp.service;

import com.example.eventapp.model.BusinessImage;
import com.example.eventapp.model.BusinessProfile;
import com.example.eventapp.repository.BusinessImageRepository;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.*;
import java.util.List;

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

            currentImages++;

            String fileName =
                    "gallery_"
                            + currentImages
                            + ".jpg";

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

            businessProfile.setStatus(
                    BusinessProfile.BusinessStatus.PENDING
            );

            businessImageRepository.save(image);
        }
    }

    public long countImagesByBusinessId(Long businessId) {

        return businessImageRepository
                .countByBusinessProfileId(businessId);
    }

    public void deleteImage(Long imageId) {
        BusinessImage image = businessImageRepository.findById(imageId)
                .orElseThrow(() -> new RuntimeException("Image not found"));

        businessImageRepository.delete(image);
    }
}
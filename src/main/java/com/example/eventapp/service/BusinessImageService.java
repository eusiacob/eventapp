package com.example.eventapp.service;

import com.example.eventapp.model.BusinessImage;
import com.example.eventapp.model.BusinessProfile;
import com.example.eventapp.repository.BusinessImageRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

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

    public List<BusinessImage> findByBusinessProfile(BusinessProfile businessProfile) {
        return businessImageRepository.findByBusinessProfile(businessProfile);
    }

    public void uploadImages(
            Long businessId,
            List<MultipartFile> files
    ) throws IOException {


        BusinessProfile businessProfile =
                businessProfileService.findById(businessId);


        String category = businessProfile.getCategory()
                .name()
                .toLowerCase();


        Path uploadPath = Paths.get(
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


            if (currentImages >= 20) {
                break;
            }


            String originalFileName =
                    file.getOriginalFilename();


            if (originalFileName == null ||
                    !originalFileName.contains(".")) {
                continue;
            }


            String extension =
                    originalFileName.substring(
                            originalFileName.lastIndexOf(".")
                    );


            currentImages++;


            String fileName =
                    "gallery_"
                            + currentImages
                            + extension;


            Path filePath =
                    uploadPath.resolve(fileName);


            Files.write(
                    filePath,
                    file.getBytes()
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
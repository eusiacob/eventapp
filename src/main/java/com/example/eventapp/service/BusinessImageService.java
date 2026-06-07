package com.example.eventapp.service;

import com.example.eventapp.model.BusinessImage;
import com.example.eventapp.model.BusinessProfile;
import com.example.eventapp.repository.BusinessImageRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

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

    public List<BusinessImage> findByBusinessProfile(BusinessProfile businessProfile) {
        return businessImageRepository.findByBusinessProfile(businessProfile);
    }

    public void uploadImages(Long businessId, List<MultipartFile> files) throws IOException {
        BusinessProfile businessProfile = businessProfileService.findById(businessId);

        Path uploadPath = Paths.get("uploads/images/");

        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        for (MultipartFile file : files) {

            if (file.isEmpty()) {
                continue;
            }

            String originalFileName = file.getOriginalFilename();

            if (originalFileName == null || originalFileName.isBlank()) {
                continue;
            }

            String fileName = System.currentTimeMillis()
                    + "_"
                    + originalFileName.replaceAll("\\s+", "_");

            Path filePath = uploadPath.resolve(fileName);

            Files.write(filePath, file.getBytes());

            BusinessImage businessImage = new BusinessImage();
            businessImage.setImagePath("/images/" + fileName);
            businessImage.setBusinessProfile(businessProfile);

            businessImageRepository.save(businessImage);
        }
    }

    public void deleteImage(Long imageId) {
        BusinessImage image = businessImageRepository.findById(imageId)
                .orElseThrow(() -> new RuntimeException("Image not found"));

        businessImageRepository.delete(image);
    }
}
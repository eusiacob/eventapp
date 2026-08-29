package com.example.eventapp.service;

import com.example.eventapp.exception.InvalidVideoException;
import com.example.eventapp.model.BusinessProfile;
import com.example.eventapp.model.BusinessVideo;
import com.example.eventapp.repository.BusinessVideoRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

@Service
public class BusinessVideoService {

    private final BusinessVideoRepository businessVideoRepository;
    private final BusinessProfileService businessProfileService;

    public BusinessVideoService(
            BusinessVideoRepository businessVideoRepository,
            BusinessProfileService businessProfileService
    ) {
        this.businessVideoRepository = businessVideoRepository;
        this.businessProfileService = businessProfileService;
    }

    public void uploadVideos(
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
                        businessProfile.getUuid(),
                        "videos"
                );

        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        long currentVideos =
                businessVideoRepository
                        .countByBusinessProfile(businessProfile);

        for (MultipartFile file : files) {

            if (file.isEmpty()) {
                continue;
            }

            if (file.getSize() > 100 * 1024 * 1024) {

                throw new InvalidVideoException(
                        "Videoclipul \"" +
                                file.getOriginalFilename() +
                                "\" depășește limita de 100 MB."
                );
            }

            if (currentVideos >= 5) {
                break;
            }

            String contentType =
                    file.getContentType();

            if (contentType == null ||
                    !contentType.equals("video/mp4")) {

                throw new InvalidVideoException(
                        "Videoclipul \"" +
                                file.getOriginalFilename() +
                                "\" nu este în format MP4."
                );
            }

            double duration =
                    getVideoDuration(file);

            if (duration > 30) {

                throw new InvalidVideoException(
                        "Videoclipul \"" +
                                file.getOriginalFilename() +
                                "\" depășește limita de 30 de secunde."
                );
            }

            currentVideos++;

            String fileName =
                    "video_"
                            + currentVideos
                            + ".mp4";

            Path filePath =
                    uploadPath.resolve(fileName);

            Files.write(
                    filePath,
                    file.getBytes()
            );

            BusinessVideo video =
                    new BusinessVideo();

            video.setVideoPath(
                    "/uploads/businesses/"
                            + category
                            + "/"
                            + businessProfile.getUuid()
                            + "/videos/"
                            + fileName
            );

            video.setBusinessProfile(
                    businessProfile
            );

            businessVideoRepository.save(video);
        }
    }

    private double getVideoDuration(MultipartFile file)
            throws IOException {

        Path tempFile =
                Files.createTempFile(
                        "video_",
                        ".mp4"
                );

        try {

            Files.copy(
                    file.getInputStream(),
                    tempFile,
                    StandardCopyOption.REPLACE_EXISTING
            );

            ProcessBuilder processBuilder =
                    new ProcessBuilder(
                            "ffprobe",
                            "-v",
                            "error",
                            "-show_entries",
                            "format=duration",
                            "-of",
                            "default=noprint_wrappers=1:nokey=1",
                            tempFile.toString()
                    );

            processBuilder.redirectErrorStream(true);

            Process process =
                    processBuilder.start();

            String output =
                    new String(
                            process.getInputStream()
                                    .readAllBytes()
                    ).trim();

            int exitCode =
                    process.waitFor();

            if (exitCode != 0 || output.isEmpty()) {

                throw new IOException(
                        "FFprobe nu a putut determina durata videoclipului."
                );
            }

            return Double.parseDouble(output);

        } catch (InterruptedException e) {

            Thread.currentThread().interrupt();

            throw new IOException(
                    "Procesarea videoclipului a fost întreruptă.",
                    e
            );

        } finally {

            Files.deleteIfExists(tempFile);
        }
    }

    public long countVideosByBusinessId(
            Long businessId
    ) {

        BusinessProfile businessProfile =
                businessProfileService.findById(businessId);

        return businessVideoRepository
                .countByBusinessProfile(businessProfile);
    }
}
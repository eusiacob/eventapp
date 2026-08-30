package com.example.eventapp.service;

import com.example.eventapp.exception.InvalidVideoException;
import com.example.eventapp.model.BusinessProfile;
import com.example.eventapp.model.BusinessVideo;
import com.example.eventapp.model.User;
import com.example.eventapp.repository.BusinessVideoRepository;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;

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
    ) throws IOException, InterruptedException {

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

        boolean uploaded = false;

        for (MultipartFile file : files) {

            if (file.isEmpty()) {
                continue;
            }

            if (currentVideos >= 5) {
                break;
            }

            /*
             * LIMITA FIȘIERUL ORIGINAL
             */
            String extension = getExtension(file);

            /*
             * FIȘIER TEMPORAR PENTRU INPUT
             */
            Path tempInput =
                    Files.createTempFile(
                            "video_input_",
                            extension
                    );

            /*
             * FIȘIER TEMPORAR PENTRU OUTPUT MP4
             */
            Path tempOutput =
                    Files.createTempFile(
                            "video_output_",
                            ".mp4"
                    );

            try {

                /*
                 * COPIEM UPLOAD-UL ÎN TEMP
                 */
                Files.copy(
                        file.getInputStream(),
                        tempInput,
                        StandardCopyOption.REPLACE_EXISTING
                );

                /*
                 * VERIFICĂM DURATA
                 */
                double duration =
                        getVideoDuration(tempInput);

                if (duration > 30) {

                    throw new InvalidVideoException(
                            "Videoclipul \""
                                    + file.getOriginalFilename()
                                    + "\" depășește limita de 30 de secunde."
                    );
                }

                /*
                 * CONVERSIE MP4 / H.264 / AAC
                 */
                convertToMp4(
                        tempInput,
                        tempOutput
                );

                /*
                 * NUMĂR NOU VIDEO
                 */
                currentVideos++;

                String fileName =
                        "video_"
                                + UUID.randomUUID()
                                + extension;

                Path filePath =
                        uploadPath.resolve(fileName);

                /*
                 * MUTĂM MP4-UL FINAL
                 */
                Files.copy(
                        tempOutput,
                        filePath,
                        StandardCopyOption.REPLACE_EXISTING
                );

                /*
                 * SALVARE ÎN BAZA DE DATE
                 */
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

                uploaded = true;

            } finally {

                /*
                 * ȘTERGEM FIȘIERELE TEMPORARE
                 */
                Files.deleteIfExists(tempInput);
                Files.deleteIfExists(tempOutput);
            }

        }

        if (uploaded) {

            businessProfile.setStatus(
                    BusinessProfile.BusinessStatus.PENDING
            );

            businessProfileService.save(businessProfile);
        }
    }

    private static @NonNull String getExtension(MultipartFile file) {
        if (file.getSize() > 500 * 1024 * 1024) {

            throw new InvalidVideoException(
                    "Videoclipul \""
                            + file.getOriginalFilename()
                            + "\" depășește limita de 500 MB."
            );
        }

        /*
         * FORMATE ACCEPTATE
         */
        String contentType =
                file.getContentType();

        List<String> allowedVideoTypes =
                List.of(
                        "video/mp4",
                        "video/quicktime",
                        "video/webm",
                        "video/x-msvideo"
                );

        if (contentType == null ||
                !allowedVideoTypes.contains(contentType)) {

            throw new InvalidVideoException(
                    "Videoclipul \""
                            + file.getOriginalFilename()
                            + "\" nu are un format acceptat. "
                            + "Sunt acceptate MP4, MOV, WebM și AVI."
            );
        }

        /*
         * EXTENSIA ORIGINALĂ
         */
        String originalFileName =
                file.getOriginalFilename();

        String extension = ".tmp";

        if (originalFileName != null &&
                originalFileName.contains(".")) {

            extension =
                    originalFileName
                            .substring(
                                    originalFileName.lastIndexOf(".")
                            )
                            .toLowerCase();
        }
        return extension;
    }

    private double getVideoDuration(Path videoFile)
            throws IOException, InterruptedException {

        ProcessBuilder processBuilder =
                new ProcessBuilder(
                        "ffprobe",
                        "-v",
                        "error",
                        "-show_entries",
                        "format=duration",
                        "-of",
                        "default=noprint_wrappers=1:nokey=1",
                        videoFile.toString()
                );

        processBuilder.redirectErrorStream(true);

        Process process =
                processBuilder.start();

        String output =
                new String(
                        process.getInputStream().readAllBytes()
                ).trim();

        int exitCode =
                process.waitFor();

        if (exitCode != 0 || output.isEmpty()) {

            throw new IOException(
                    "FFprobe nu a putut determina durata videoclipului."
            );
        }

        try {

            return Double.parseDouble(output);

        } catch (NumberFormatException e) {

            throw new IOException(
                    "Durata videoclipului nu a putut fi interpretată.",
                    e
            );
        }
    }

    private void convertToMp4(
            Path input,
            Path output
    ) throws IOException, InterruptedException {

        ProcessBuilder processBuilder =
                new ProcessBuilder(
                        "ffmpeg",
                        "-y",

                        "-i",
                        input.toString(),

                        "-vf",
                        "scale='min(1920,iw)':-2",

                        "-c:v",
                        "libx264",

                        "-preset",
                        "medium",

                        "-crf",
                        "24",

                        "-c:a",
                        "aac",

                        "-b:a",
                        "128k",

                        "-movflags",
                        "+faststart",

                        output.toString()
                );

        processBuilder.redirectErrorStream(true);

        Process process =
                processBuilder.start();

        String outputMessage =
                new String(
                        process.getInputStream().readAllBytes()
                );

        int exitCode =
                process.waitFor();

        if (exitCode != 0) {

            throw new IOException(
                    "Conversia videoclipului a eșuat: "
                            + outputMessage
            );
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

    public BusinessProfile deleteVideo(
            Long videoId,
            User user
    ) throws IOException {

        BusinessVideo video =
                businessVideoRepository.findById(videoId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Videoclipul nu există."
                                ));

        BusinessProfile businessProfile =
                video.getBusinessProfile();

        if (businessProfile == null ||
                businessProfile.getUser() == null ||
                !businessProfile.getUser().getId()
                        .equals(user.getId())) {

            throw new RuntimeException(
                    "Nu ai permisiunea să ștergi acest videoclip."
            );
        }

        String videoPath = video.getVideoPath();

        if (videoPath != null) {

            String relativePath =
                    videoPath.startsWith("/")
                            ? videoPath.substring(1)
                            : videoPath;

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

        businessVideoRepository.delete(video);

        return businessProfile;
    }
}
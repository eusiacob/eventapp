package com.example.eventapp.service;

import com.example.eventapp.exception.InvalidVideoException;
import com.example.eventapp.model.BusinessProfile;
import com.example.eventapp.model.BusinessVideo;
import com.example.eventapp.model.User;
import com.example.eventapp.repository.BusinessVideoRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

@Service
public class BusinessVideoService {

    private final BusinessVideoRepository businessVideoRepository;
    private final BusinessProfileService businessProfileService;
    private final Semaphore ffmpegSemaphore = new Semaphore(2);

    public BusinessVideoService(
            BusinessVideoRepository businessVideoRepository,
            BusinessProfileService businessProfileService
    ) {
        this.businessVideoRepository = businessVideoRepository;
        this.businessProfileService = businessProfileService;
    }

    public void uploadVideos(Long businessId, List<MultipartFile> files)
            throws IOException, InterruptedException {

        BusinessProfile businessProfile =
                businessProfileService.findById(businessId);

        String category =
                businessProfile.getCategory().name().toLowerCase();

        Path uploadPath = Paths.get(
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
                businessVideoRepository.countByBusinessProfile(businessProfile);

        boolean uploaded = false;

        for (MultipartFile file : files) {

            if (file == null || file.isEmpty()) {
                continue;
            }

            if (currentVideos >= 5) {
                break;
            }

            // 1. Validări de bază
            validateVideo(file);

            // 2. Extensia este folosită DOAR pentru fișierul temporar
            String extension = getExtension(file);

            Path tempInput =
                    Files.createTempFile("video_input_", extension);

            Path tempOutput =
                    Files.createTempFile("video_output_", ".mp4");

            try {

                // 3. Copiem fișierul încărcat în fișierul temporar
                Files.copy(
                        file.getInputStream(),
                        tempInput,
                        StandardCopyOption.REPLACE_EXISTING
                );

                // 4. Verificăm conținutul REAL cu FFprobe
                validateVideoWithFfprobe(tempInput);

                // 5. Verificăm durata
                double duration = getVideoDuration(tempInput);

                if (duration > 30) {
                    throw new InvalidVideoException(
                            "Videoclipul \"" +
                                    file.getOriginalFilename() +
                                    "\" depășește limita de 30 de secunde."
                    );
                }

                // 6. Conversie la MP4
                convertToMp4(tempInput, tempOutput);

                // 7. Fișierul final este ÎNTOTDEAUNA MP4
                String fileName =
                        "video_" + UUID.randomUUID() + ".mp4";

                Path uploadPathAbsolute = uploadPath
                        .toAbsolutePath()
                        .normalize();

                Path filePath = uploadPathAbsolute
                        .resolve(fileName)
                        .normalize();

                if (!filePath.startsWith(uploadPathAbsolute)) {
                    throw new IOException(
                            "Calea fișierului nu este permisă."
                    );
                }

                Files.copy(
                        tempOutput,
                        filePath,
                        StandardCopyOption.REPLACE_EXISTING
                );

                // 8. Salvăm calea în DB
                BusinessVideo video = new BusinessVideo();

                video.setVideoPath(
                        "/uploads/businesses/"
                                + category + "/"
                                + businessProfile.getUuid()
                                + "/videos/"
                                + fileName
                );

                video.setBusinessProfile(businessProfile);

                businessVideoRepository.save(video);

                currentVideos++;
                uploaded = true;

            } finally {

                // Ștergem întotdeauna fișierele temporare
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

    private void validateVideoWithFfprobe(Path videoFile)
            throws IOException, InterruptedException {

        ProcessBuilder processBuilder = new ProcessBuilder(
                "ffprobe",
                "-v", "error",
                "-select_streams", "v:0",
                "-show_entries", "stream=codec_type",
                "-of", "default=noprint_wrappers=1:nokey=1",
                videoFile.toString()
        );

        processBuilder.redirectErrorStream(true);

        Process process = processBuilder.start();

        StringBuilder output = new StringBuilder();

        Thread outputReader = new Thread(() -> {

            try (var reader =
                         new java.io.BufferedReader(
                                 new java.io.InputStreamReader(
                                         process.getInputStream()))) {

                String line;

                while ((line = reader.readLine()) != null) {

                    output.append(line)
                            .append(System.lineSeparator());
                }

            } catch (IOException ignored) {
                // Procesul este gestionat mai jos.
            }
        });

        outputReader.start();

        boolean finished =
                process.waitFor(30, TimeUnit.SECONDS);

        if (!finished) {

            process.destroyForcibly();
            outputReader.interrupt();

            throw new InvalidVideoException(
                    "Fișierul video nu a putut fi verificat în timpul alocat."
            );
        }

        outputReader.join(2000);

        int exitCode =
                process.exitValue();

        String result =
                output.toString().trim();

        if (exitCode != 0 ||
                result.isEmpty() ||
                !result.contains("video")) {

            throw new InvalidVideoException(
                    "Fișierul încărcat nu este un videoclip valid."
            );
        }
    }

    private String getExtension(MultipartFile file) {

        String originalFileName = file.getOriginalFilename();

        if (originalFileName == null ||
                originalFileName.isBlank()) {
            return ".tmp";
        }

        int lastDot =
                originalFileName.lastIndexOf('.');

        if (lastDot < 0 ||
                lastDot == originalFileName.length() - 1) {
            return ".tmp";
        }

        String extension =
                originalFileName
                        .substring(lastDot)
                        .toLowerCase();

        List<String> allowedExtensions = List.of(
                ".mp4",
                ".mov",
                ".webm",
                ".avi"
        );

        if (!allowedExtensions.contains(extension)) {
            return ".tmp";
        }

        return extension;
    }

    private void validateVideo(MultipartFile file) {

        if (file.getSize() > 500 * 1024 * 1024) {

            throw new InvalidVideoException(
                    "Videoclipul \"" +
                            file.getOriginalFilename() +
                            "\" depășește limita de 500 MB."
            );
        }

        String contentType = file.getContentType();

        List<String> allowedVideoTypes = List.of(
                "video/mp4",
                "video/quicktime",
                "video/webm",
                "video/x-msvideo"
        );

        if (contentType == null ||
                !allowedVideoTypes.contains(contentType.toLowerCase())) {

            throw new InvalidVideoException(
                    "Videoclipul \"" +
                            file.getOriginalFilename() +
                            "\" nu are un format acceptat. " +
                            "Sunt acceptate MP4, MOV, WebM și AVI."
            );
        }
    }

    private double getVideoDuration(Path videoFile)
            throws IOException, InterruptedException {

        ProcessBuilder processBuilder = new ProcessBuilder(
                "ffprobe",
                "-v", "error",
                "-show_entries", "format=duration",
                "-of", "default=noprint_wrappers=1:nokey=1",
                videoFile.toString()
        );

        processBuilder.redirectErrorStream(true);

        Process process = processBuilder.start();

        // Citim output-ul într-un thread separat, pentru a evita blocarea
        // procesului dacă bufferul de output se umple.
        StringBuilder output = new StringBuilder();

        Thread outputReader = new Thread(() -> {
            try (var reader = new java.io.BufferedReader(
                    new java.io.InputStreamReader(process.getInputStream()))) {

                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append(System.lineSeparator());
                }

            } catch (IOException ignored) {
                // Procesul este gestionat mai jos.
            }
        });

        outputReader.start();

        boolean finished = process.waitFor(30, TimeUnit.SECONDS);

        if (!finished) {
            process.destroyForcibly();
            outputReader.interrupt();

            throw new IOException(
                    "FFprobe a depășit timpul maxim de execuție de 30 de secunde."
            );
        }

        outputReader.join(2000);

        int exitCode = process.exitValue();

        String outputMessage = output.toString().trim();

        if (exitCode != 0 || outputMessage.isEmpty()) {
            throw new IOException(
                    "FFprobe nu a putut determina durata videoclipului."
            );
        }

        try {
            return Double.parseDouble(outputMessage);
        } catch (NumberFormatException e) {
            throw new IOException(
                    "Durata videoclipului nu a putut fi interpretată.",
                    e
            );
        }
    }

    private void convertToMp4(Path input, Path output)
            throws IOException, InterruptedException {

        ffmpegSemaphore.acquire();

        try {
            ProcessBuilder processBuilder = new ProcessBuilder(
                    "ffmpeg",
                    "-y",
                    "-i", input.toString(),
                    "-vf", "scale='min(1920,iw)':-2",
                    "-c:v", "libx264",
                    "-preset", "medium",
                    "-crf", "24",
                    "-c:a", "aac",
                    "-b:a", "128k",
                    "-movflags", "+faststart",
                    output.toString()
            );

            processBuilder.redirectErrorStream(true);

            Process process = processBuilder.start();

            StringBuilder outputMessage = new StringBuilder();

            Thread outputReader = new Thread(() -> {
                try (var reader = new java.io.BufferedReader(
                        new java.io.InputStreamReader(process.getInputStream()))) {

                    String line;

                    while ((line = reader.readLine()) != null) {
                        outputMessage.append(line)
                                .append(System.lineSeparator());
                    }

                } catch (IOException ignored) {
                    // Procesul este gestionat mai jos.
                }
            });

            outputReader.start();

            boolean finished = process.waitFor(2, TimeUnit.MINUTES);

            if (!finished) {
                process.destroyForcibly();
                outputReader.interrupt();

                throw new IOException(
                        "Conversia video a depășit timpul maxim de 2 minute."
                );
            }

            outputReader.join(2000);

            int exitCode = process.exitValue();

            if (exitCode != 0) {
                throw new IOException(
                        "Conversia video a eșuat: "
                                + outputMessage
                );
            }

        } finally {
            ffmpegSemaphore.release();
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

        if (videoPath != null && !videoPath.isBlank()) {

            String relativePath = videoPath.startsWith("/")
                    ? videoPath.substring(1)
                    : videoPath;

            Path uploadsRoot = Paths.get("uploads")
                    .toAbsolutePath()
                    .normalize();

            Path filePath = Paths.get(relativePath)
                    .toAbsolutePath()
                    .normalize();

            if (!filePath.startsWith(uploadsRoot)) {
                throw new IOException(
                        "Calea fișierului nu este permisă."
                );
            }

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
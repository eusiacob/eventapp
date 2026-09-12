package com.example.eventapp.service;

import com.example.eventapp.model.BusinessProfile;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

@Service
public class BusinessCoverImageService {

    private static final int COVER_WIDTH = 1200;
    private static final int COVER_HEIGHT = 900;
    private static final String COVER_FILE_NAME = "cover.jpg";

    public void validateCoverImage(MultipartFile file) throws IOException {

        if (file == null || file.isEmpty()) {
            return;
        }

        String contentType = file.getContentType();

        if (contentType == null ||
                !(contentType.equals("image/jpeg")
                        || contentType.equals("image/png")
                        || contentType.equals("image/webp"))) {

            throw new IllegalArgumentException(
                    "Formatul imaginii nu este acceptat. Folosește JPG, PNG sau WebP."
            );
        }

        BufferedImage image = ImageIO.read(file.getInputStream());

        if (image == null) {
            throw new IllegalArgumentException(
                    "Fișierul selectat nu este o imagine validă."
            );
        }

        if (image.getWidth() != COVER_WIDTH ||
                image.getHeight() != COVER_HEIGHT) {

            throw new IllegalArgumentException(
                    "Imaginea trebuie să fie decupată înainte de salvare."
            );
        }
    }

    public String saveCoverImage(
            BusinessProfile profile,
            MultipartFile file
    ) throws IOException {

        validateCoverImage(file);

        BufferedImage sourceImage = ImageIO.read(file.getInputStream());
        BufferedImage safeJpegImage = new BufferedImage(
                sourceImage.getWidth(),
                sourceImage.getHeight(),
                BufferedImage.TYPE_INT_RGB
        );

        Graphics2D graphics = safeJpegImage.createGraphics();

        try {
            graphics.setColor(Color.WHITE);
            graphics.fillRect(
                    0,
                    0,
                    safeJpegImage.getWidth(),
                    safeJpegImage.getHeight()
            );
            graphics.drawImage(sourceImage, 0, 0, null);
        } finally {
            graphics.dispose();
        }

        String categoryFolder = profile.getCategory()
                .name()
                .toLowerCase();

        Path uploadPath = Paths.get(
                "uploads",
                "businesses",
                categoryFolder,
                profile.getUuid()
        );

        Files.createDirectories(uploadPath);
        deleteCoverFiles(uploadPath);

        Path filePath = uploadPath.resolve(COVER_FILE_NAME);

        if (!ImageIO.write(safeJpegImage, "jpg", filePath.toFile())) {
            throw new IOException("Nu s-a putut salva imaginea de copertă.");
        }

        return "/uploads/businesses/"
                + categoryFolder
                + "/"
                + profile.getUuid()
                + "/"
                + COVER_FILE_NAME;
    }

    public void deleteCoverImage(String imagePath) throws IOException {

        if (imagePath == null || imagePath.isBlank()) {
            return;
        }

        String relativePath = imagePath.startsWith("/")
                ? imagePath.substring(1)
                : imagePath;

        Path uploadsRoot = Paths.get("uploads", "businesses")
                .toAbsolutePath()
                .normalize();
        Path filePath = Paths.get(relativePath)
                .toAbsolutePath()
                .normalize();

        if (!filePath.startsWith(uploadsRoot)) {
            return;
        }

        Path fileName = filePath.getFileName();

        if (fileName == null ||
                !fileName.toString().startsWith("cover")) {
            return;
        }

        Files.deleteIfExists(filePath);
    }

    private void deleteCoverFiles(Path uploadPath) throws IOException {

        if (!Files.exists(uploadPath)) {
            return;
        }

        try (Stream<Path> paths = Files.list(uploadPath)) {
            paths
                    .filter(Files::isRegularFile)
                    .filter(path -> path.getFileName()
                            .toString()
                            .startsWith("cover"))
                    .forEach(path -> {
                        try {
                            Files.deleteIfExists(path);
                        } catch (IOException e) {
                            throw new IllegalStateException(
                                    "Nu s-a putut șterge vechea copertă.",
                                    e
                            );
                        }
                    });
        } catch (IllegalStateException e) {
            if (e.getCause() instanceof IOException ioException) {
                throw ioException;
            }

            throw e;
        }
    }
}

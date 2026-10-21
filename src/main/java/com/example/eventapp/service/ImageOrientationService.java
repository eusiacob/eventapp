package com.example.eventapp.service;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifIFD0Directory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Iterator;

@Service
public class ImageOrientationService {

    private static final int MAX_IMAGE_DIMENSION = 20_000;
    private static final long MAX_IMAGE_PIXELS = 40_000_000L;

    static void validateDimensions(int width, int height) {
        if (width <= 0 || height <= 0
                || width > MAX_IMAGE_DIMENSION
                || height > MAX_IMAGE_DIMENSION
                || (long) width * height > MAX_IMAGE_PIXELS) {
            throw new IllegalArgumentException(
                    "Imaginea are dimensiuni prea mari. Folosește o imagine de maximum 40 de megapixeli."
            );
        }
    }

    public BufferedImage readWithCorrectOrientation(MultipartFile file)
            throws IOException {

        byte[] imageBytes =
                file.getBytes();

        BufferedImage image;
        try (ImageInputStream input = ImageIO.createImageInputStream(
                new ByteArrayInputStream(imageBytes))) {
            if (input == null) {
                return null;
            }

            Iterator<ImageReader> readers = ImageIO.getImageReaders(input);
            if (!readers.hasNext()) {
                return null;
            }

            ImageReader reader = readers.next();
            try {
                reader.setInput(input, true, false);
                int width = reader.getWidth(0);
                int height = reader.getHeight(0);
                validateDimensions(width, height);
                image = reader.read(0);
            } finally {
                reader.dispose();
            }
        }

        if (image == null) {
            return null;
        }

        return applyOrientation(
                image,
                readOrientation(imageBytes)
        );
    }

    private int readOrientation(byte[] imageBytes) {
        try {
            Metadata metadata =
                    ImageMetadataReader.readMetadata(
                            new ByteArrayInputStream(imageBytes)
                    );

            ExifIFD0Directory directory =
                    metadata.getFirstDirectoryOfType(ExifIFD0Directory.class);

            if (directory == null ||
                    !directory.containsTag(ExifIFD0Directory.TAG_ORIENTATION)) {
                return 1;
            }

            return directory.getInt(ExifIFD0Directory.TAG_ORIENTATION);

        } catch (ImageProcessingException |
                 IOException |
                 com.drew.metadata.MetadataException exception) {

            return 1;
        }
    }

    private BufferedImage applyOrientation(
            BufferedImage image,
            int orientation
    ) {

        return switch (orientation) {
            case 2 -> flipHorizontal(image);
            case 3 -> rotate180(image);
            case 4 -> flipVertical(image);
            case 5 -> rotate90Clockwise(flipHorizontal(image));
            case 6 -> rotate90Clockwise(image);
            case 7 -> rotate90Clockwise(flipVertical(image));
            case 8 -> rotate90CounterClockwise(image);
            default -> image;
        };
    }

    private BufferedImage rotate90Clockwise(BufferedImage image) {
        BufferedImage rotated =
                new BufferedImage(
                        image.getHeight(),
                        image.getWidth(),
                        BufferedImage.TYPE_INT_RGB
                );

        Graphics2D graphics = rotated.createGraphics();

        try {
            applyQualityRendering(graphics);
            graphics.translate(image.getHeight(), 0);
            graphics.rotate(Math.toRadians(90));
            graphics.drawImage(image, 0, 0, null);
        } finally {
            graphics.dispose();
        }

        return rotated;
    }

    private BufferedImage rotate90CounterClockwise(BufferedImage image) {
        BufferedImage rotated =
                new BufferedImage(
                        image.getHeight(),
                        image.getWidth(),
                        BufferedImage.TYPE_INT_RGB
                );

        Graphics2D graphics = rotated.createGraphics();

        try {
            applyQualityRendering(graphics);
            graphics.translate(0, image.getWidth());
            graphics.rotate(Math.toRadians(-90));
            graphics.drawImage(image, 0, 0, null);
        } finally {
            graphics.dispose();
        }

        return rotated;
    }

    private BufferedImage rotate180(BufferedImage image) {
        BufferedImage rotated =
                new BufferedImage(
                        image.getWidth(),
                        image.getHeight(),
                        BufferedImage.TYPE_INT_RGB
                );

        Graphics2D graphics = rotated.createGraphics();

        try {
            applyQualityRendering(graphics);
            graphics.translate(image.getWidth(), image.getHeight());
            graphics.rotate(Math.toRadians(180));
            graphics.drawImage(image, 0, 0, null);
        } finally {
            graphics.dispose();
        }

        return rotated;
    }

    private BufferedImage flipHorizontal(BufferedImage image) {
        BufferedImage flipped =
                new BufferedImage(
                        image.getWidth(),
                        image.getHeight(),
                        BufferedImage.TYPE_INT_RGB
                );

        Graphics2D graphics = flipped.createGraphics();

        try {
            applyQualityRendering(graphics);
            graphics.drawImage(
                    image,
                    image.getWidth(),
                    0,
                    -image.getWidth(),
                    image.getHeight(),
                    null
            );
        } finally {
            graphics.dispose();
        }

        return flipped;
    }

    private BufferedImage flipVertical(BufferedImage image) {
        BufferedImage flipped =
                new BufferedImage(
                        image.getWidth(),
                        image.getHeight(),
                        BufferedImage.TYPE_INT_RGB
                );

        Graphics2D graphics = flipped.createGraphics();

        try {
            applyQualityRendering(graphics);
            graphics.drawImage(
                    image,
                    0,
                    image.getHeight(),
                    image.getWidth(),
                    -image.getHeight(),
                    null
            );
        } finally {
            graphics.dispose();
        }

        return flipped;
    }

    private void applyQualityRendering(Graphics2D graphics) {
        graphics.setRenderingHint(
                RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR
        );
        graphics.setRenderingHint(
                RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_QUALITY
        );
        graphics.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON
        );
    }
}

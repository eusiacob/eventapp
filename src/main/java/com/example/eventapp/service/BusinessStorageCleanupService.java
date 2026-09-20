package com.example.eventapp.service;

import com.example.eventapp.config.UploadProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

@Service
public class BusinessStorageCleanupService {

    private static final Logger LOGGER =
            LoggerFactory.getLogger(BusinessStorageCleanupService.class);

    private final UploadProperties uploadProperties;
    private final ApplicationEventPublisher eventPublisher;

    public BusinessStorageCleanupService(
            UploadProperties uploadProperties,
            ApplicationEventPublisher eventPublisher
    ) {
        this.uploadProperties = uploadProperties;
        this.eventPublisher = eventPublisher;
    }

    public void deleteAfterCommit(String businessUuid) {
        validateUuid(businessUuid);
        eventPublisher.publishEvent(new BusinessStorageDeletionRequested(businessUuid));
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void deleteCommittedBusinessFiles(BusinessStorageDeletionRequested event) {
        try {
            deleteBusinessFiles(event.businessUuid());
        } catch (RuntimeException exception) {
            LOGGER.error(
                    "Fișierele serviciului {} nu au putut fi șterse din storage.",
                    event.businessUuid(),
                    exception
            );
        }
    }

    void deleteBusinessFiles(String businessUuid) {
        validateUuid(businessUuid);

        Path uploadsRoot = uploadProperties.businessesPath();
        if (!Files.exists(uploadsRoot)) {
            return;
        }

        try (Stream<Path> paths = Files.walk(uploadsRoot, 2)) {
            List<Path> profileDirectories = paths
                    .filter(Files::isDirectory)
                    .filter(path -> path.startsWith(uploadsRoot))
                    .filter(path -> businessUuid.equals(path.getFileName().toString()))
                    .toList();

            for (Path directory : profileDirectories) {
                deleteDirectory(directory, uploadsRoot);
            }
        } catch (IOException exception) {
            throw new IllegalStateException(
                    "Fișierele serviciului nu au putut fi identificate.",
                    exception
            );
        }
    }

    private void deleteDirectory(Path directory, Path uploadsRoot) {
        if (!directory.startsWith(uploadsRoot) || !Files.exists(directory)) {
            return;
        }

        try (Stream<Path> paths = Files.walk(directory)) {
            paths.sorted(Comparator.reverseOrder())
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                        } catch (IOException exception) {
                            throw new UncheckedIOException(exception);
                        }
                    });
        } catch (IOException | UncheckedIOException exception) {
            throw new IllegalStateException(
                    "Fișierele serviciului nu au putut fi șterse.",
                    exception
            );
        }
    }

    private void validateUuid(String businessUuid) {
        if (businessUuid == null) {
            throw new IllegalStateException("Identificatorul serviciului lipsește.");
        }

        try {
            UUID.fromString(businessUuid);
        } catch (IllegalArgumentException exception) {
            throw new IllegalStateException(
                    "Identificatorul serviciului este invalid.",
                    exception
            );
        }
    }

    public record BusinessStorageDeletionRequested(String businessUuid) {
    }
}

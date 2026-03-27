package com.example.microquest.service;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

/**
 * Service for storing and resolving uploaded files (GIFs/videos and photo IDs).
 * <p>
 * Files are stored under a configurable root directory (default: {@code uploads/}):
 * <ul>
 *   <li>{@code uploads/gifs/}      — quest submission media (GIF, JPEG, MP4)</li>
 *   <li>{@code uploads/photo-ids/} — user identity documents (JPG, PNG)</li>
 * </ul>
 * Both subdirectories are created at startup via {@link #init()}.
 * </p>
 * <p>
 * All resolve methods validate the result path against the expected subdirectory
 * using {@link Path#startsWith} to prevent directory-traversal attacks.
 * </p>
 */
@Service
public class FileStorageService {

    private final Path storageRoot;

    public FileStorageService(@Value("${storage.location:uploads}") String storageLocation) {
        this.storageRoot = Paths.get(storageLocation).toAbsolutePath().normalize();
    }

    /** Creates the {@code gifs/} and {@code photo-ids/} subdirectories if they do not exist. */
    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(storageRoot.resolve("gifs"));
            Files.createDirectories(storageRoot.resolve("photo-ids"));
        } catch (IOException e) {
            throw new RuntimeException("Could not create upload directories: " + e.getMessage(), e);
        }
    }

    /**
     * Validates and stores a GIF/video/JPEG submission file.
     * The filename is generated as {@code media_{userId}_{timestamp}.{ext}} to
     * avoid collisions.
     */
    public String storeGif(MultipartFile file, Long userId) throws IOException {
        validateMediaFile(file);
        String ext = getExtension(file.getOriginalFilename());
        String filename = "media_" + userId + "_" + System.currentTimeMillis() + "." + ext;
        Path destination = storageRoot.resolve("gifs").resolve(filename);
        Files.copy(file.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);
        return filename;
    }

    /**
     * Validates and stores a photo-ID image during registration.
     * Returns the filename (relative to the {@code photo-ids/} subdirectory)
     * which is then stored on the {@link com.example.microquest.model.UserProfile}.
     */
    public String storePhotoId(MultipartFile file, Long userId) throws IOException {
        validateImageFile(file);
        String ext = getExtension(file.getOriginalFilename());
        String filename = "photoid_" + userId + "_" + System.currentTimeMillis() + "." + ext;
        Path destination = storageRoot.resolve("photo-ids").resolve(filename);
        Files.copy(file.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);
        return filename; // relative filename under photo-ids/
    }

    /**
     * Resolves a photo-ID filename to an absolute {@link Path}.
     * Throws {@link IllegalArgumentException} if the resolved path escapes the
     * {@code photo-ids/} directory (path-traversal guard).
     */
    public Path resolvePhotoId(String filename) {
        Path resolved = storageRoot.resolve("photo-ids").resolve(filename).normalize();
        if (!resolved.startsWith(storageRoot.resolve("photo-ids"))) {
            throw new IllegalArgumentException("Invalid file path");
        }
        return resolved;
    }

    /**
     * Resolves a GIF/video filename to an absolute {@link Path}.
     * Throws {@link IllegalArgumentException} if the resolved path escapes the
     * {@code gifs/} directory (path-traversal guard).
     */
    public Path resolveGif(String filename) {
        Path resolved = storageRoot.resolve("gifs").resolve(filename).normalize();
        if (!resolved.startsWith(storageRoot.resolve("gifs"))) {
            throw new IllegalArgumentException("Invalid file path");
        }
        return resolved;
    }

    /** Deletes a GIF/video file by filename; no-op if the file does not exist. */
    public void deleteGif(String filename) throws IOException {
        Path file = storageRoot.resolve("gifs").resolve(filename).normalize();
        if (!file.startsWith(storageRoot.resolve("gifs"))) {
            throw new IllegalArgumentException("Invalid filename");
        }
        Files.deleteIfExists(file);
    }

    /** Deletes a photo-ID file by filename; no-op if the file does not exist. */
    public void deletePhotoId(String filename) throws IOException {
        Path file = storageRoot.resolve("photo-ids").resolve(filename).normalize();
        if (!file.startsWith(storageRoot.resolve("photo-ids"))) {
            throw new IllegalArgumentException("Invalid filename");
        }
        Files.deleteIfExists(file);
    }

    /** Validates a media upload: must be non-empty, GIF/JPEG/MP4, and under 25 MB. */
    private void validateMediaFile(MultipartFile file) {
        if (file.isEmpty()) throw new IllegalArgumentException("File is empty");
        String name = file.getOriginalFilename();
        if (name == null) throw new IllegalArgumentException("Invalid file name");
        String lower = name.toLowerCase();
        if (!lower.endsWith(".gif") && !lower.endsWith(".jpg") && !lower.endsWith(".jpeg") && !lower.endsWith(".mp4")) {
            throw new IllegalArgumentException("Only GIF, JPEG, or MP4 files are allowed");
        }
        if (file.getSize() > 25L * 1024 * 1024) throw new IllegalArgumentException("File must be under 25 MB");
    }

    /** Validates a photo-ID upload: must be non-empty, JPG/PNG, and under 20 MB. */
    private void validateImageFile(MultipartFile file) {
        if (file.isEmpty()) throw new IllegalArgumentException("File is empty");
        String name = file.getOriginalFilename();
        if (name == null) throw new IllegalArgumentException("Invalid file name");
        String lower = name.toLowerCase();
        if (!lower.endsWith(".jpg") && !lower.endsWith(".jpeg") && !lower.endsWith(".png")) {
            throw new IllegalArgumentException("Photo ID must be a JPG or PNG file");
        }
        if (file.getSize() > 20L * 1024 * 1024) throw new IllegalArgumentException("Photo ID must be under 20 MB");
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) return "bin";
        return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
    }
}

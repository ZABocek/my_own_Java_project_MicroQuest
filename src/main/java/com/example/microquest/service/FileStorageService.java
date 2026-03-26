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

@Service
public class FileStorageService {

    private final Path storageRoot;

    public FileStorageService(@Value("${storage.location:uploads}") String storageLocation) {
        this.storageRoot = Paths.get(storageLocation).toAbsolutePath().normalize();
    }

    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(storageRoot.resolve("gifs"));
            Files.createDirectories(storageRoot.resolve("photo-ids"));
        } catch (IOException e) {
            throw new RuntimeException("Could not create upload directories: " + e.getMessage(), e);
        }
    }

    public String storeGif(MultipartFile file, Long userId) throws IOException {
        validateMediaFile(file);
        String ext = getExtension(file.getOriginalFilename());
        String filename = "media_" + userId + "_" + System.currentTimeMillis() + "." + ext;
        Path destination = storageRoot.resolve("gifs").resolve(filename);
        Files.copy(file.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);
        return filename;
    }

    public String storePhotoId(MultipartFile file, Long userId) throws IOException {
        validateImageFile(file);
        String ext = getExtension(file.getOriginalFilename());
        String filename = "photoid_" + userId + "_" + System.currentTimeMillis() + "." + ext;
        Path destination = storageRoot.resolve("photo-ids").resolve(filename);
        Files.copy(file.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);
        return filename; // relative filename under photo-ids/
    }

    /** Returns the absolute path for a photo-id file (admin access only). */
    public Path resolvePhotoId(String filename) {
        Path resolved = storageRoot.resolve("photo-ids").resolve(filename).normalize();
        if (!resolved.startsWith(storageRoot.resolve("photo-ids"))) {
            throw new IllegalArgumentException("Invalid file path");
        }
        return resolved;
    }

    public Path resolveGif(String filename) {
        Path resolved = storageRoot.resolve("gifs").resolve(filename).normalize();
        if (!resolved.startsWith(storageRoot.resolve("gifs"))) {
            throw new IllegalArgumentException("Invalid file path");
        }
        return resolved;
    }

    public void deleteGif(String filename) throws IOException {
        Path file = storageRoot.resolve("gifs").resolve(filename).normalize();
        if (!file.startsWith(storageRoot.resolve("gifs"))) {
            throw new IllegalArgumentException("Invalid filename");
        }
        Files.deleteIfExists(file);
    }

    public void deletePhotoId(String filename) throws IOException {
        Path file = storageRoot.resolve("photo-ids").resolve(filename).normalize();
        if (!file.startsWith(storageRoot.resolve("photo-ids"))) {
            throw new IllegalArgumentException("Invalid filename");
        }
        Files.deleteIfExists(file);
    }

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

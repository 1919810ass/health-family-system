package com.healthfamily.web.controller;

import com.healthfamily.web.dto.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@Slf4j
public class FileUploadController {

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    @PostMapping("/upload")
    public Result<String> uploadFile(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Result.error(400, "Please select a file to upload");
        }

        try {
            // Create directory based on current date
            String dateStr = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            // Fix: Use absolute path to ensure correct file saving location
            Path rootPath = Paths.get(uploadDir).toAbsolutePath().normalize();
            Path uploadPath = rootPath.resolve(dateStr);
            
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Generate unique filename
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String newFilename = UUID.randomUUID().toString() + extension;

            // Save file using absolute path
            Path filePath = uploadPath.resolve(newFilename);
            file.transferTo(filePath.toFile());

            // Return URL
            // Format: /api/files/{date}/{filename}
            String fileUrl = "/api/files/" + dateStr + "/" + newFilename;
            return Result.success(fileUrl);

        } catch (IOException e) {
            log.error("File upload failed", e);
            return Result.error(500, "File upload failed: " + e.getMessage());
        }
    }

    @GetMapping("/files/{date}/{filename}")
    public ResponseEntity<Resource> getFile(@PathVariable String date, @PathVariable String filename) {
        try {
            Path rootPath = Paths.get(uploadDir).toAbsolutePath().normalize();
            Path filePath = rootPath.resolve(date).resolve(filename);
            File file = filePath.toFile();

            if (file.exists() && file.canRead()) {
                FileSystemResource resource = new FileSystemResource(file);
                
                // Determine content type
                String contentType = Files.probeContentType(filePath);
                if (contentType == null) {
                    contentType = "application/octet-stream";
                }

                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(contentType))
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("Error reading file", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}

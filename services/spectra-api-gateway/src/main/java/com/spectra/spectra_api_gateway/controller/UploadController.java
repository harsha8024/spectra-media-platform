package com.spectra.spectra_api_gateway.controller;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.spectra.spectra_api_gateway.client.TagManagerClient;
import com.spectra.spectra_api_gateway.config.RabbitConfig;
import com.spectra.spectra_api_gateway.dto.CreateImageRequest;
import com.spectra.spectra_api_gateway.dto.ImageMetadataResponse;
import com.spectra.spectra_api_gateway.dto.ImageReceivedEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@RestController
@RequestMapping("/api/images")
@RequiredArgsConstructor
@Slf4j
public class UploadController {

    private final TagManagerClient tagManagerClient;
    private final RabbitTemplate rabbitTemplate;

    @Value("${storage.path:/tmp/spectra/images}")
    private String storagePath;

    @PostConstruct
    public void init() {
        // Create storage directory if it doesn't exist
        File storageDir = new File(storagePath);
        if (!storageDir.exists()) {
            storageDir.mkdirs();
        }
        log.info("Storage path initialized: {}", storagePath);
    }

    @PostMapping("/upload")
    public ResponseEntity<String> uploadImage(
            @RequestHeader("X-User-Id") String userId,
            @RequestParam("file") MultipartFile file) {
        try {
            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body("File cannot be empty");
            }

            // Generate storage paths
            String uniqueKey = userId + "/" + UUID.randomUUID() + "-" + file.getOriginalFilename();
            String thumbnailKey = "thumbnails/" + uniqueKey;

            File targetFile = new File(storagePath, uniqueKey);
            targetFile.getParentFile().mkdirs();

            // Save the file
            file.transferTo(targetFile);
            log.info("File saved to: {}", targetFile.getAbsolutePath());

            // Create metadata record
            CreateImageRequest createRequest = new CreateImageRequest(
                    userId,
                    file.getOriginalFilename(),
                    uniqueKey,
                    thumbnailKey);

            ImageMetadataResponse metadata = tagManagerClient.createImage(createRequest);
            log.info("Created metadata record: {}", metadata.id());

            // Publish event
            ImageReceivedEvent event = new ImageReceivedEvent(
                    metadata.id(),
                    userId,
                    uniqueKey);

            rabbitTemplate.convertAndSend(
                    RabbitConfig.EXCHANGE_NAME,
                    RabbitConfig.ROUTING_KEY,
                    event);

            log.info("Published ImageReceivedEvent for image: {}", metadata.id());
            return ResponseEntity.accepted()
                    .body(metadata.id().toString());
        } catch (Exception e) {
            log.error("Error processing upload", e);
            return ResponseEntity.internalServerError()
                    .body("Error processing upload: " + e.getMessage());
        }
    }

    @GetMapping("/content/{imageId}")
    public ResponseEntity<Resource> getImage(@PathVariable UUID imageId) {
        try {
            ImageMetadataResponse metadata = tagManagerClient.getImage(imageId);
            Path path = Paths.get(storagePath, metadata.storageUrl());
            Resource resource = new FileSystemResource(path);

            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_JPEG)
                    .body(resource);
        } catch (Exception e) {
            log.error("Error fetching image", e);
            return ResponseEntity.notFound().build();
        }
    }
}

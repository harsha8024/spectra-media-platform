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
import com.spectra.spectra_api_gateway.dto.UpdateMetadataRequest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
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
                    metadata.id().toString(),
                    uniqueKey // This is the filePath that the image processor needs
            );

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

            if (!resource.exists()) {
                return ResponseEntity.notFound().build();
            }

            String filename = path.getFileName().toString().toLowerCase();
            MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;
            if (filename.endsWith(".jpg") || filename.endsWith(".jpeg")) {
                mediaType = MediaType.IMAGE_JPEG;
            } else if (filename.endsWith(".png")) {
                mediaType = MediaType.IMAGE_PNG;
            } else if (filename.endsWith(".gif")) {
                mediaType = MediaType.IMAGE_GIF;
            }

            return ResponseEntity.ok()
                    .contentType(mediaType)
                    .body(resource);
        } catch (Exception e) {
            log.error("Error fetching image", e);
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping
    public List<ImageMetadataResponse> getAllImages() {
        try {
            return tagManagerClient.getAllImages();
        } catch (Exception e) {
            log.error("Error fetching all images", e);
            return List.of();
        }
    }

    @PutMapping("/{imageId}/metadata")
    public ResponseEntity<Void> updateImageMetadata(
            @PathVariable UUID imageId,
            @RequestBody UpdateMetadataRequest request) {
        try {
            tagManagerClient.updateImageMetadata(imageId, request);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error updating image metadata for imageId: {}", imageId, e);
            return ResponseEntity.internalServerError().build();
        }
    }
}

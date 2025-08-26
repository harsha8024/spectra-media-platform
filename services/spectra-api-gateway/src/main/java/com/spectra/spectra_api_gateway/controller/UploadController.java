package com.spectra.spectra_api_gateway.controller;

import com.spectra.spectra_api_gateway.client.TagManagerClient;
import com.spectra.spectra_api_gateway.config.RabbitConfig;
import com.spectra.spectra_api_gateway.dto.CreateImageRequest;
import com.spectra.spectra_api_gateway.dto.ImageMetadataResponse;
import com.spectra.spectra_api_gateway.dto.ImageReceivedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UploadController {

    private final TagManagerClient tagManagerClient;
    private final RabbitTemplate rabbitTemplate;

    @PostMapping("/upload")
    public ResponseEntity<String> uploadImage(
            @RequestHeader("X-User-Id") String userId,
            @RequestParam("file") MultipartFile file
    ) {
        // Generate a unique storage key
        String uniqueKey = userId + "/" + UUID.randomUUID() + "-" + file.getOriginalFilename();
        String thumbnailKey = "thumbnails/" + uniqueKey;

        // Create initial metadata record
        CreateImageRequest createRequest = new CreateImageRequest(
            userId,
            file.getOriginalFilename(),
            uniqueKey, // simulated storage URL
            thumbnailKey // simulated thumbnail URL
        );

        ImageMetadataResponse metadata = tagManagerClient.createImage(createRequest);

        // Create and publish event
        ImageReceivedEvent event = new ImageReceivedEvent(
            metadata.id(),
            userId,
            uniqueKey
        );

        rabbitTemplate.convertAndSend(
            RabbitConfig.EXCHANGE_NAME,
            RabbitConfig.ROUTING_KEY,
            event
        );

        return ResponseEntity.accepted()
            .body("Image upload accepted for processing");
    }
}

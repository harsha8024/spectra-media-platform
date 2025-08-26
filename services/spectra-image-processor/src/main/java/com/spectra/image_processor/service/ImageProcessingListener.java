package com.spectra.image_processor.service;

import com.spectra.image_processor.config.RabbitConfig;
import com.spectra.image_processor.dto.ImageProcessedEvent;
import com.spectra.image_processor.dto.ImageReceivedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class ImageProcessingListener {

    private final RabbitTemplate rabbitTemplate;

    @RabbitListener(queues = RabbitConfig.QUEUE_NAME)
    public void processImage(ImageReceivedEvent event) {
        log.info("Received image processing request for imageId: {}", event.imageId());

        // Simulate downloading the image
        log.info("Downloading image from location: {}", event.storageLocation());

        // Simulate thumbnail generation
        log.info("Generating 150x150 thumbnail");
        
        // Generate thumbnail location
        String thumbnailLocation = generateThumbnailLocation(event.storageLocation());
        log.info("Thumbnail will be stored at: {}", thumbnailLocation);

        // Create and publish the processed event
        ImageProcessedEvent processedEvent = new ImageProcessedEvent(
            event.imageId(),
            event.storageLocation(),
            thumbnailLocation
        );

        rabbitTemplate.convertAndSend(
            RabbitConfig.EXCHANGE_NAME,
            RabbitConfig.ROUTING_KEY_PROCESSED,
            processedEvent
        );

        log.info("Published image processed event for imageId: {}", event.imageId());
    }

    private String generateThumbnailLocation(String originalLocation) {
        int lastDotIndex = originalLocation.lastIndexOf('.');
        if (lastDotIndex > 0) {
            return originalLocation.substring(0, lastDotIndex) + "-thumb" + originalLocation.substring(lastDotIndex);
        }
        return originalLocation + "-thumb";
    }
}

package com.spectra.image_processor.service;

import com.spectra.image_processor.config.RabbitConfig;
import com.spectra.image_processor.dto.ImageProcessedEvent;
import com.spectra.image_processor.dto.ImageReceivedEvent;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
@Slf4j
public class ImageProcessingListener {

    private final RabbitTemplate rabbitTemplate;
    private final String storagePath;

    public ImageProcessingListener(RabbitTemplate rabbitTemplate,
            @Value("${storage.path:/tmp/spectra/images}") String storagePath) {
        this.rabbitTemplate = rabbitTemplate;
        this.storagePath = storagePath;
    }

    @RabbitListener(queues = RabbitConfig.QUEUE_NAME)
    public void onImageReceived(ImageReceivedEvent event) {
        log.info("Received image processing request for imageId: {}", event.getImageId());

        try {
            // Use the imageId to find the file since filePath might be null
            String filePath = event.getFilePath();
            if (filePath == null || filePath.isEmpty()) {
                log.warn("FilePath is null or empty for imageId: {}, skipping processing", event.getImageId());
                return;
            }

            Path sourcePath = Paths.get(storagePath, filePath);
            File sourceFile = sourcePath.toFile();

            if (!sourceFile.exists()) {
                log.error("Source file not found at: {}", sourcePath);
                return;
            }

            // Generate thumbnail
            String thumbnailFilename = getThumbnailFilename(sourceFile.getName());
            Path thumbnailPath = sourcePath.getParent().resolve(thumbnailFilename);
            log.info("Generating 150x150 thumbnail");
            Thumbnails.of(sourceFile)
                    .size(150, 150)
                    .toFile(thumbnailPath.toFile());
            log.info("Thumbnail will be stored at: {}", sourcePath.getParent().relativize(thumbnailPath));

            // Publish event that processing is complete
            ImageProcessedEvent processedEvent = new ImageProcessedEvent(
                    event.getImageId(),
                    filePath, // original location
                    sourcePath.getParent().relativize(thumbnailPath).toString().replace('\\', '/') // thumbnail location
            );

            rabbitTemplate.convertAndSend(RabbitConfig.EXCHANGE_NAME, RabbitConfig.ROUTING_KEY_PROCESSED,
                    processedEvent);
            log.info("Published image processed event for imageId: {}", event.getImageId());

        } catch (IOException e) {
            log.error("Error processing image for imageId: {}", event.getImageId(), e);
        }
    }

    private String getThumbnailFilename(String originalFilename) {
        int lastDotIndex = originalFilename.lastIndexOf('.');
        if (lastDotIndex > 0) {
            return originalFilename.substring(0, lastDotIndex) + "-thumb" + originalFilename.substring(lastDotIndex);
        }
        return originalFilename + "-thumb";
    }
}

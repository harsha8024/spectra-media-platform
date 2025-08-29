package com.spectra.image_processor.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ImageProcessedEvent {
    private String imageId;
    private String originalLocation;
    private String thumbnailLocation;
}

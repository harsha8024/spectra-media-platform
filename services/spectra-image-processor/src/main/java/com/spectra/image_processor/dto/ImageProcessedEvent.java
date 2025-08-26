package com.spectra.image_processor.dto;

import java.util.UUID;

public record ImageProcessedEvent(
    UUID imageId,
    String originalLocation,
    String thumbnailLocation
) {}

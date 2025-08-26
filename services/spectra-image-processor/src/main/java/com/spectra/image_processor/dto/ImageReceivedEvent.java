package com.spectra.image_processor.dto;

import java.util.UUID;

public record ImageReceivedEvent(
    UUID imageId,
    String userId,
    String storageLocation
) {}

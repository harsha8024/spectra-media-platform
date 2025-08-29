package com.spectra.spectra_api_gateway.dto;

import java.util.List;
import java.util.UUID;

public record ImageMetadataResponse(
        UUID id,
        String userId,
        String originalFilename,
        String storageUrl,
        String thumbnailUrl,
        List<String> tags,
        List<String> palette,
        String createdAt) {
}

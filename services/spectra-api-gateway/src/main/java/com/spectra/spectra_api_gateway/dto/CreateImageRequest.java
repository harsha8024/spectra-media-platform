package com.spectra.spectra_api_gateway.dto;

public record CreateImageRequest(
    String userId,
    String originalFilename,
    String storageUrl,
    String thumbnailUrl
) {}

package com.spectra.tag.dto;

public record CreateImageRequest(
    String userId,
    String originalFilename,
    String storageUrl,
    String thumbnailUrl
) {}

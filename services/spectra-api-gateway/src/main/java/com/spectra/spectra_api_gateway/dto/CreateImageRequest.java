package com.spectra.spectra_api_gateway.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateImageRequest {
    private String userId;
    private String originalFilename;
    private String storageUrl;
    private String thumbnailUrl;
}

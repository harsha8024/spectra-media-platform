package com.spectra.spectra_api_gateway.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ImageReceivedEvent {
    private String imageId;
    private String filePath;
}

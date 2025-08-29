package com.spectra.spectra_api_gateway.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateMetadataRequest {
    private List<String> tags;
    private List<String> palette;
}
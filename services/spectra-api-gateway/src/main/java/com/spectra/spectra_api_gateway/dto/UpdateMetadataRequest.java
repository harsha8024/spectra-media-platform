package com.spectra.spectra_api_gateway.dto;

import java.util.List;

public class UpdateMetadataRequest {
    private List<String> tags;
    private List<String> palette;

    // Getters and Setters
    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public List<String> getPalette() {
        return palette;
    }

    public void setPalette(List<String> palette) {
        this.palette = palette;
    }
}
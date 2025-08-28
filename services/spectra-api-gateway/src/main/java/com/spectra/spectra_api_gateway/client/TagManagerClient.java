package com.spectra.spectra_api_gateway.client;

import com.spectra.spectra_api_gateway.dto.CreateImageRequest;
import com.spectra.spectra_api_gateway.dto.ImageMetadataResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "tag-manager", url = "${tag.manager.url}")
public interface TagManagerClient {
    @PostMapping("/api/images")
    ImageMetadataResponse createImage(@RequestBody CreateImageRequest request);
    @GetMapping("/api/images/{imageId}")
    ImageMetadataResponse getImage(@PathVariable String imageId);
}

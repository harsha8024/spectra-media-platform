// package com.spectra.spectra_api_gateway.client;

// import com.spectra.spectra_api_gateway.dto.CreateImageRequest;
// import com.spectra.spectra_api_gateway.dto.ImageMetadataResponse;

// import java.util.UUID;

// import org.springframework.cloud.openfeign.FeignClient;
// import org.springframework.web.bind.annotation.GetMapping;
// import org.springframework.web.bind.annotation.PathVariable;
// import org.springframework.web.bind.annotation.PostMapping;
// import org.springframework.web.bind.annotation.RequestBody;

// @FeignClient(name = "tag-manager", url = "${tag.manager.url}")
// public interface TagManagerClient {
//     @PostMapping("/api/images")
//     ImageMetadataResponse createImage(@RequestBody CreateImageRequest request);

//     @GetMapping("/api/images/{imageId}")
//     ImageMetadataResponse getImage(@PathVariable UUID imageId);
// }
package com.spectra.spectra_api_gateway.client;

import com.spectra.spectra_api_gateway.dto.CreateImageRequest;
import com.spectra.spectra_api_gateway.dto.ImageMetadataResponse;
import com.spectra.spectra_api_gateway.dto.UpdateMetadataRequest; // Import the new DTO

import java.util.UUID;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping; // Import PutMapping
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "tag-manager", url = "${tag.manager.url}")
public interface TagManagerClient {
    @PostMapping("/api/images")
    ImageMetadataResponse createImage(@RequestBody CreateImageRequest request);

    @GetMapping("/api/images/{imageId}")
    ImageMetadataResponse getImage(@PathVariable UUID imageId);

    // Add this method to update the metadata
    @PutMapping("/api/images/{imageId}/metadata")
    void updateImageMetadata(@PathVariable UUID imageId, @RequestBody UpdateMetadataRequest request);
}
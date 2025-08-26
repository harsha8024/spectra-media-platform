package com.spectra.tag;

import com.spectra.tag.dto.CreateImageRequest;
import com.spectra.tag.dto.ImageMetadataResponse;
import com.spectra.tag.dto.TagRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/images")
@RequiredArgsConstructor
public class GalleryController {

    private final ImageMetadataRepository repository;

    @PostMapping
    public ImageMetadataResponse createImage(@RequestBody CreateImageRequest request) {
        ImageMetadata image = ImageMetadata.builder()
                .userId(request.userId())
                .originalFilename(request.originalFilename())
                .storageUrl(request.storageUrl())
                .thumbnailUrl(request.thumbnailUrl())
                .tags(new ArrayList<>())
                .palette(new ArrayList<>())
                .build();
        return toResponse(repository.save(image));
    }

    @PostMapping("/{id}/metadata")
    public ResponseEntity<ImageMetadataResponse> addTagsAndPalette(
            @PathVariable UUID id,
            @RequestBody TagRequest request
    ) {
        return repository.findById(id)
                .map(image -> {
                    image.setTags(request.tags());
                    image.setPalette(request.palette());
                    ImageMetadata updated = repository.save(image);
                    return ResponseEntity.ok(toResponse(updated));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public Page<ImageMetadataResponse> getAllImages(Pageable pageable) {
        return repository.findAll(pageable)
                .map(this::toResponse);
    }

    @GetMapping("/search")
    public List<ImageMetadataResponse> searchByTags(@RequestParam List<String> tags) {
        List<ImageMetadata> images = repository.findByTagsIn(tags);
        return images.stream().map(this::toResponse).collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ImageMetadataResponse> getImageMetadata(@PathVariable UUID id) {
        return repository.findById(id)
                .map(image -> ResponseEntity.ok(toResponse(image)))
                .orElse(ResponseEntity.notFound().build());
    }

    private ImageMetadataResponse toResponse(ImageMetadata image) {
        return new ImageMetadataResponse(
                image.getId(),
                image.getUserId(),
                image.getOriginalFilename(),
                image.getStorageUrl(),
                image.getThumbnailUrl(),
                image.getTags(),
                image.getPalette(),
                image.getCreatedAt()
        );
    }
}

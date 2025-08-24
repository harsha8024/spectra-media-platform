package com.spectra.tag;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/tags") // All requests to this controller will start with /api/tags
public class TagController {

    private final ImageTagRepository imageTagRepository;

    // Spring will automatically provide the ImageTagRepository (Dependency Injection)
    public TagController(ImageTagRepository imageTagRepository) {
        this.imageTagRepository = imageTagRepository;
    }

    @GetMapping("/{imageId}") // Handles GET requests to /api/tags/{some-image-id}
    public List<ImageTag> getTagsByImageId(@PathVariable String imageId) {
        return imageTagRepository.findByImageId(imageId);
    }
}

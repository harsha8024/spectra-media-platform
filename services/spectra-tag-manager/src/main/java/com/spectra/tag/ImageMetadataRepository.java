package com.spectra.tag;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface ImageMetadataRepository extends JpaRepository<ImageMetadata, UUID> {
    List<ImageMetadata> findByTagsIn(Collection<String> tags);
}

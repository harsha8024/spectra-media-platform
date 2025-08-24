package com.spectra.tag;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ImageTagRepository extends JpaRepository<ImageTag, Long> {

    // Spring Data JPA automatically creates a query based on the method name
    List<ImageTag> findByImageId(String imageId);
}
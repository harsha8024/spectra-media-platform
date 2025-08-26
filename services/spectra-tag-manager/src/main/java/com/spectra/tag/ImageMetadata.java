package com.spectra.tag;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ImageMetadata {

    @Id
    @GeneratedValue
    private UUID id;

    private String userId;

    private String originalFilename;

    private String storageUrl;

    private String thumbnailUrl;

    @ElementCollection
    private List<String> tags;

    @ElementCollection
    private List<String> palette;

    @CreationTimestamp
    private Timestamp createdAt;
}

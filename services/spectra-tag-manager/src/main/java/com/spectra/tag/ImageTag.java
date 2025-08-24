
package com.spectra.tag;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class ImageTag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String imageId; // To link tags to a specific image
    private String tagName;
    private double confidenceScore;

    // Constructors, Getters, and Setters
}


package com.spectra.tag.dto;

import java.util.List;

public record TagRequest(List<String> tags, List<String> palette) {}

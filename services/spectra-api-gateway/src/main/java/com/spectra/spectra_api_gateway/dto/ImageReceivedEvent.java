package com.spectra.spectra_api_gateway.dto;

import java.util.UUID;

public record ImageReceivedEvent(
    UUID imageId,
    String userId,
    String storageLocation
) {}

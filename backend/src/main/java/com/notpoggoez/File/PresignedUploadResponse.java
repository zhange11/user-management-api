package com.notpoggoez.File;

import java.time.Instant;

public record PresignedUploadResponse(
        String fileId,
        String uploadUrl,
        String key,
        Instant expiresAt
) {
}
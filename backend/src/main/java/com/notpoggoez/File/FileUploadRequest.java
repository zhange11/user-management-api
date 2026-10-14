package com.notpoggoez.File;

public record FileUploadRequest(
        String fileName,
        String contentType
) {
}
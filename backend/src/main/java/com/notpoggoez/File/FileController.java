package com.notpoggoez.File;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/customers/{customerId}/files")
public class FileController {

    private final FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    @PostMapping("/presigned-url")
    public PresignedUploadResponse createPresignedUploadUrl(
            @PathVariable("customerId") Integer customerId,
            @RequestBody FileUploadRequest request) {
        return fileService.createPresignedUploadUrl(customerId, request);
    }

    @GetMapping("/{fileId}/status")
    public FileMetadataStatusResponse getFileStatus(
            @PathVariable("customerId") Integer customerId,
            @PathVariable("fileId") String fileId) {
        return fileService.getFileStatus(customerId, fileId);
    }
}
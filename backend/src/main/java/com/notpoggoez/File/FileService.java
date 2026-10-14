package com.notpoggoez.File;

import com.notpoggoez.Customer.CustomerDao;
import com.notpoggoez.Exception.ResourceNotFoundException;
import com.notpoggoez.S3.S3Buckets;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.time.Duration;
import java.util.UUID;

@Service
public class FileService {

    private static final Duration URL_EXPIRATION = Duration.ofMinutes(15);

    private final S3Presigner s3Presigner;
    private final S3Buckets s3Buckets;
    private final FileMetadataRepository fileMetadataRepository;
    private final CustomerDao customerDao;

    public FileService(S3Presigner s3Presigner,
                        S3Buckets s3Buckets,
                        FileMetadataRepository fileMetadataRepository,
                        @Qualifier("jpa") CustomerDao customerDao) {
        this.s3Presigner = s3Presigner;
        this.s3Buckets = s3Buckets;
        this.fileMetadataRepository = fileMetadataRepository;
        this.customerDao = customerDao;
    }

    public PresignedUploadResponse createPresignedUploadUrl(Integer customerId, FileUploadRequest request) {
        if (!customerDao.existsCustomerByID(customerId)) {
            throw new ResourceNotFoundException(
                    "customer with id [%s] not found".formatted(customerId)
            );
        }

        String fileId = UUID.randomUUID().toString();
        String key = "uploads/%s/%s".formatted(customerId, fileId);

        FileMetadata metadata = new FileMetadata(
                fileId,
                customerId,
                request.fileName(),
                request.contentType(),
                FileStatus.UPLOADING
        );
        fileMetadataRepository.save(metadata);

        PutObjectRequest objectRequest = PutObjectRequest.builder()
                .bucket(s3Buckets.getCustomer())
                .key(key)
                .contentType(request.contentType())
                .build();

        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(URL_EXPIRATION)
                .putObjectRequest(objectRequest)
                .build();

        PresignedPutObjectRequest presignedRequest = s3Presigner.presignPutObject(presignRequest);

        return new PresignedUploadResponse(
                fileId,
                presignedRequest.url().toString(),
                key,
                presignedRequest.expiration()
        );
    }

    public FileMetadataStatusResponse getFileStatus(Integer customerId, String fileId) {
        FileMetadata metadata = fileMetadataRepository.findByFileId(fileId)
                .filter(f -> f.getCustomerId().equals(customerId))
                .orElseThrow(() -> new ResourceNotFoundException(
                        "file with id [%s] not found".formatted(fileId)));

        return new FileMetadataStatusResponse(metadata.getFileId(), metadata.getStatus().name());
    }
}
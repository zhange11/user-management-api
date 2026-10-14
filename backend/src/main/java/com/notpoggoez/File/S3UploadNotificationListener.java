package com.notpoggoez.File;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.DeleteMessageRequest;
import software.amazon.awssdk.services.sqs.model.Message;
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Polls an SQS queue that S3 bucket notifications publish "s3:ObjectCreated:*"
 * events to, and flips the matching FileMetadata row from UPLOADING -> UPLOADED.
 *
 * Requires the S3 bucket (aws.s3.buckets.customer) to have an event notification
 * configured to publish ObjectCreated events directly to the SQS queue referenced
 * by aws.sqs.file-upload-queue-url.
 */
@Component
public class S3UploadNotificationListener {

    private static final Logger log = LoggerFactory.getLogger(S3UploadNotificationListener.class);
    private static final Pattern KEY_PATTERN = Pattern.compile("^uploads/(\\d+)/([\\w-]+)$");

    private final SqsClient sqsClient;
    private final FileMetadataRepository fileMetadataRepository;
    private final ObjectMapper objectMapper;

    @Value("${aws.sqs.file-upload-queue-url:}")
    private String queueUrl;

    public S3UploadNotificationListener(SqsClient sqsClient,
                                         FileMetadataRepository fileMetadataRepository,
                                         ObjectMapper objectMapper) {
        this.sqsClient = sqsClient;
        this.fileMetadataRepository = fileMetadataRepository;
        this.objectMapper = objectMapper;
    }

    @Scheduled(fixedDelay = 5000)
    public void pollForUploadEvents() {
        if (queueUrl == null || queueUrl.isBlank()) {
            return;
        }

        ReceiveMessageRequest receiveRequest = ReceiveMessageRequest.builder()
                .queueUrl(queueUrl)
                .maxNumberOfMessages(10)
                .waitTimeSeconds(5)
                .build();

        List<Message> messages = sqsClient.receiveMessage(receiveRequest).messages();

        for (Message message : messages) {
            try {
                processMessage(message.body());
                sqsClient.deleteMessage(DeleteMessageRequest.builder()
                        .queueUrl(queueUrl)
                        .receiptHandle(message.receiptHandle())
                        .build());
            } catch (Exception e) {
                log.error("failed to process S3 notification message: {}", message.body(), e);
            }
        }
    }

    private void processMessage(String body) throws Exception {
        S3EventNotification notification = objectMapper.readValue(body, S3EventNotification.class);

        if (notification.records() == null) {
            return;
        }

        for (S3EventRecord record : notification.records()) {
            String rawKey = record.s3().object().key();
            String key = URLDecoder.decode(rawKey, StandardCharsets.UTF_8);
            Matcher matcher = KEY_PATTERN.matcher(key);

            if (!matcher.matches()) {
                log.warn("received S3 notification for key that does not match expected pattern: {}", key);
                continue;
            }

            String fileId = matcher.group(2);
            int updated = fileMetadataRepository.updateStatusByFileId(fileId, FileStatus.UPLOADED);
            if (updated == 0) {
                log.warn("no file metadata found for fileId [{}] from key [{}]", fileId, key);
            }
        }
    }
}
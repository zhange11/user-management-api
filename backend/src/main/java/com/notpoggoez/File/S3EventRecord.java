package com.notpoggoez.File;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record S3EventRecord(
        @JsonProperty("s3") S3Details s3
) {
}
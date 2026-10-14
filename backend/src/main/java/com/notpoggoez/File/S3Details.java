package com.notpoggoez.File;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record S3Details(
        @JsonProperty("bucket") S3BucketDetails bucket,
        @JsonProperty("object") S3ObjectDetails object
) {
}
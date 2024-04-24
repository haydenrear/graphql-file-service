package com.hayden.fileservice.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;

@ConfigurationProperties(prefix = "file-sync")
@Component
@Data
public class FileSyncProperties {

    String partitionId;

    String fileSyncEndpointTopicPattern;

    String filePartitionSyncEndpointTopicPattern;

    public Pattern getFileSyncEndpointTopicPattern() {
        return Pattern.compile(fileSyncEndpointTopicPattern);
    }

    public Pattern getFileSyncEndpointTopicPattern(String file) {
        return Pattern.compile(filePartitionSyncEndpointTopicPattern.formatted(file));
    }
}

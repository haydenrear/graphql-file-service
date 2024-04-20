package com.hayden.fileservice.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "files")
@Component
@Data
public class FileProperties {

    int bufferSize = 1024;

}

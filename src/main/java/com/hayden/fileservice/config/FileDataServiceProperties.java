package com.hayden.fileservice.config;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "file-data-service")
@Component
@Data
public class FileDataServiceProperties {

    String host;

}

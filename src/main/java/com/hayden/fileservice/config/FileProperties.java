package com.hayden.fileservice.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "files")
@Component
@Data
public class FileProperties {

    int bufferSize = 1024;

    /**
     * Number of bytes to assign to the header.
     */
    long dataStreamFileHeaderLengthBytes = 2048;

    /**
     * The size of the data in a header operation that automatically forces condense.
     */
    long sizeDataStreamFileOperationForceCondense;

    /**
     * The threshold percentage of the total data in a header operation compared to the total data in the file before
     * for which condense will automatically be triggered.
     */
    float percentageOfFileSizeOperationForceCondense;

}

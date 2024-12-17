package com.hayden.fileservice.config;


import com.hayden.graphql.models.visitor.datafetcher.DataFetcherGraphQlSource;
import com.hayden.utilitymodule.result.error.SingleError;
import com.hayden.utilitymodule.result.Result;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@ConfigurationProperties(prefix = "federated-graphql")
@Component
@Data
public class FederatedGraphQlProperties {

    Path schemaPath;

    public record DataFetcherSource(Path path, String fieldName, String packageName, String mimeType) {}

    Map<String, List<DataFetcherSource>> dataFetcherSources;

    public Collection<Result<DataFetcherGraphQlSource, SingleError>> toSource() {
        return new ArrayList<>();
    }

}

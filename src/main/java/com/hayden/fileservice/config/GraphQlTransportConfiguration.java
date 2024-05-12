package com.hayden.fileservice.config;

import com.hayden.fileservice.data_fetcher.GetFilesRemoteDataFetcher;
import com.hayden.graphql.federated.transport.http.HttpGraphQlTransportBuilder;
import com.hayden.graphql.models.GraphQlTarget;
import com.hayden.graphql.models.federated.service.FederatedGraphQlServiceFetcherItemId;
import com.hayden.graphql.models.visitor.datafed.DataFederationSources;
import com.hayden.graphql.models.visitor.datafed.GraphQlDataFederationModel;
import com.hayden.graphql.models.visitor.datafetcher.DataFetcherGraphQlSource;
import com.hayden.graphql.models.visitor.datafetcher.GraphQlDataFetcherDiscoveryModel;
import com.hayden.graphql.models.visitor.schema.GraphQlFederatedSchemaSource;
import com.hayden.graphql.models.visitor.simpletransport.GraphQlTransportModel;
import com.hayden.utilitymodule.ArrayUtilUtilities;
import com.hayden.utilitymodule.io.FileUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MimeType;

import java.util.*;

@Configuration
@Slf4j
public class GraphQlTransportConfiguration {


    @Bean
    public FederatedGraphQlServiceFetcherItemId getFilesRemoteDataFetcherServiceItemId(FileDataServiceProperties fileDataServiceProperties) {
        return new FederatedGraphQlServiceFetcherItemId(
                getFilesRemoteDataFetcher(),
                FileServiceConstants.DATA_SERVICE_ID,
                fileDataServiceProperties.getHost()
        );
    }

    @Bean
    public MimeType mimeType() {
        return new MimeType("files", "*");
    }

    @Bean
    public String uniqueServiceInstanceId() {
        String uniqueServiceId = UUID.randomUUID().toString();
        log.info("Initializing instance of {} with unique instance ID of {}.", FileServiceConstants.DATA_SERVICE_ID, uniqueServiceId);
        return uniqueServiceId;
    }

    @Bean
    public FederatedGraphQlServiceFetcherItemId.FederatedGraphQlServiceFetcherId getFilesRemoteDataFetcher() {
        return GetFilesRemoteDataFetcher.GET_FILES_REMOTE_FED_ID;
    }

    @Bean
    public GraphQlTransportModel graphQlTransportModel(FederatedGraphQlServiceFetcherItemId getFilesRemoteDataFetcherServiceItemId,
                                                       FileDataServiceProperties fileDataServiceProperties,
                                                       @Value("${server.port:9092}") int port) {
        return new GraphQlTransportModel(getFilesRemoteDataFetcherServiceItemId, HttpGraphQlTransportBuilder.builder()
                .host(fileDataServiceProperties.host)
                .port(port)
                .path("/graphql")
                .queryParams(new LinkedMultiValueMap<>())
                .build()
        );
    }

    @Bean
    public Collection<GraphQlFederatedSchemaSource> schemas(FederatedGraphQlProperties federatedGraphQlProperties) {
        return ArrayUtilUtilities.fromArray(federatedGraphQlProperties.schemaPath.toFile().listFiles())
                .flatMap(f -> FileUtils.readToString(f)
                        .mapError(e -> { throw new RuntimeException("Failed to load schema from file: %s.".formatted(e)); })
                        .stream()
                        .map(s -> new GraphQlFederatedSchemaSource(GraphQlTarget.String, s))
                )
                .toList();
    }

    @Bean
    public Collection<DataFetcherGraphQlSource> dataFetchers(FederatedGraphQlProperties federatedGraphQlProperties) {
        return federatedGraphQlProperties.toSource()
                .stream()
                .flatMap(r -> r
                        .mapError(err -> { throw new RuntimeException("Error parsing data fetchers: %s".formatted(err.getMessage())); })
                        .stream()
                )
                .toList();
    }

    @Bean
    public GraphQlDataFetcherDiscoveryModel graphQlDataFetcherDiscoveryModel(
            FederatedGraphQlServiceFetcherItemId getFilesRemoteDataFetcherServiceItemId,
            Collection<GraphQlFederatedSchemaSource> schemas,
            Collection<DataFetcherGraphQlSource> dataFetchers
    ) {
        return new GraphQlDataFetcherDiscoveryModel(getFilesRemoteDataFetcherServiceItemId, schemas, dataFetchers);
    }

    @Bean
    public Collection<DataFederationSources> federationSource() {
        return new ArrayList<>();
    }

    @Bean
    public GraphQlDataFederationModel getGraphQlDataFederationModel(
            FederatedGraphQlServiceFetcherItemId getFilesRemoteDataFetcherServiceItemId,
            Collection<GraphQlFederatedSchemaSource> schemas,
            Collection<DataFederationSources> federationSource
    ) {
        return new GraphQlDataFederationModel(getFilesRemoteDataFetcherServiceItemId, schemas, federationSource);
    }
}
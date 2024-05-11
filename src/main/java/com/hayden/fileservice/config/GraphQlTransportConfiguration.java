package com.hayden.fileservice.config;

import com.hayden.graphql.federated.transport.http.HttpGraphQlTransportBuilder;
import com.hayden.graphql.models.federated.service.FederatedGraphQlServiceItemId;
import com.hayden.graphql.models.visitor.VisitorModel;
import com.hayden.graphql.models.visitor.simpletransport.GraphQlTransportModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MimeType;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Configuration
@Slf4j
public class GraphQlTransportConfiguration {


    @Bean
    public FederatedGraphQlServiceItemId serviceItemId(FederatedGraphQlServiceItemId.FederatedGraphQlServiceId serviceId,
                                                       FileDataServiceProperties fileDataServiceProperties) {
        return new FederatedGraphQlServiceItemId(
                serviceId,
                FileServiceConstants.DATA_SERVICE_ID,
                fileDataServiceProperties.host
        );
    }

    @Bean
    public MimeType mimeType() {
        return MimeType.valueOf("file/*");
    }

    @Bean
    public String uniqueServiceInstanceId() {
        String uniqueServiceId = UUID.randomUUID().toString();
        log.info("Initializing instance of {} with unique instance ID of {}.", FileServiceConstants.DATA_SERVICE_ID, uniqueServiceId);
        return uniqueServiceId;
    }

    @Bean
    public FederatedGraphQlServiceItemId.FederatedGraphQlServiceId serviceId(MimeType mimeType,
                                                                             String uniqueServiceInstanceId) {
        return new FederatedGraphQlServiceItemId.FederatedGraphQlServiceId(mimeType, uniqueServiceInstanceId, FileServiceConstants.DATA_SERVICE_ID);
    }

    @Bean
    public GraphQlTransportModel graphQlTransportModel(FederatedGraphQlServiceItemId serviceItemId,
                                                       FileDataServiceProperties fileDataServiceProperties) {
        return new GraphQlTransportModel(serviceItemId, HttpGraphQlTransportBuilder.builder()
                .host(fileDataServiceProperties.host)
                .path("/graphql")
                .queryParams(new LinkedMultiValueMap<>())
                .build()
        );
    }

    @Bean
    public List<VisitorModel> visitorModels(FederatedGraphQlServiceItemId itemId) {

        return new ArrayList<>();
    }

}

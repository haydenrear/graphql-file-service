package com.hayden.fileservice.data_fetcher;

import com.hayden.fileservice.codegen.types.FileChangeEvent;
import com.hayden.graphql.models.dataservice.RemoteDataFetcherImpl;
import com.hayden.graphql.models.federated.request.FederatedRequestData;
import com.hayden.graphql.models.federated.request.FederatedRequestDataItem;
import com.hayden.utilitymodule.result.Result;
import graphql.schema.DataFetchingEnvironment;

import java.util.List;

public class GetFilesRemoteDataFetcher extends RemoteDataFetcherImpl<FileChangeEvent> {
    @Override
    public FederatedRequestData toRequestData(DataFetchingEnvironment env) {
//        return new FederatedRequestData(new FederatedRequestDataItem());
        return null;
    }

    @Override
    public <U> Result<FileChangeEvent, RemoteDataFetcherError> from(List<U> fromValue) {
        return null;
    }

    @Override
    public FileChangeEvent get(DataFetchingEnvironment environment) throws Exception {
        return null;
    }
}

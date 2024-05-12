package com.hayden.fileservice.data_fetcher;

import com.hayden.fileservice.codegen.types.FileChangeEvent;
import com.hayden.graphql.models.dataservice.RemoteDataFetcherImpl;
import com.hayden.graphql.models.federated.request.FederatedRequestData;
import com.hayden.graphql.models.federated.request.FederatedRequestDataItem;
import com.hayden.graphql.models.federated.service.FederatedGraphQlServiceFetcherItemId;
import com.hayden.utilitymodule.result.Result;
import graphql.schema.DataFetchingEnvironment;
import lombok.NoArgsConstructor;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.NotNull;
import org.springframework.util.MimeType;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Stream;

@NoArgsConstructor
public class GetFilesRemoteDataFetcher extends RemoteDataFetcherImpl<FileChangeEvent> {

    public static final MimeType FILES_MIME_TYPE = new MimeType("files", "*");

    public static final FederatedGraphQlServiceFetcherItemId.FederatedGraphQlServiceFetcherId GET_FILES_REMOTE_FED_ID = new FederatedGraphQlServiceFetcherItemId.FederatedGraphQlServiceFetcherId(
            FILES_MIME_TYPE,
            GetFilesRemoteDataFetcher.class.getSimpleName(),
            GetFilesRemoteDataFetcher.class.getPackageName()
    );

    private static final @Language("graphql") String FILE_CHANGE_EVENT = """
            subscription ListenToFileChanges($path: String, $fileId: ID, $fileName: String, $fileTypeType: String!, $fileTypeValue: String!) {
              files(fileSearch: { path: $path, fileId: $fileId, fileName: $fileName, fileType: { type: $fileTypeType, value: $fileTypeValue } }) {
                fileId
              }
            }
            
            subscription GetFileMetadata($path: String, $fileId: ID, $fileName: String, $fileTypeType: String!, $fileTypeValue: String!) {
              fileMetadata(fileSearch: { path: $path, fileId: $fileId, fileName: $fileName, fileType: { type: $fileTypeType, value: $fileTypeValue } }) {
                id
                name
                path
                size
                lastModified
                changeType
              }
            }
            
            mutation UpdateFileChangeEvent($input: FileChangeEventInput!) {
                  update(fileChangeEvent: $input) {
                     id,
                     name,
                     path,
                     size,
                     lastModified,
                     changeType
                  }
            }
            """;


    @Override
    public FederatedRequestData toRequestData(DataFetchingEnvironment env) {
        return new FederatedRequestData(
                FederatedRequestDataItem.builder()
                        .dataFetchingEnvironment(env)
                        .requestBody(FILE_CHANGE_EVENT)
                        .operationName(env.getOperationDefinition().getName())
                        .variables(env.getVariables())
                        .extensions(new HashMap<>())
                        .attributes(new HashMap<>())
                        .federatedService(new FederatedGraphQlServiceFetcherItemId.FederatedGraphQlServiceFetcherId(
                                FILES_MIME_TYPE,
                                this.getClass().getSimpleName(),
                                this.getClass().getPackage().getName()
                        ))
                        .build()
        );
    }

    @Override
    public <U> Result<FileChangeEvent, RemoteDataFetcherError> from(List<U> fromValue) {
        var multiple = parseResult(fromValue);
        if (multiple.size() == 1) {
            return multiple.getFirst();
        } else if (!multiple.isEmpty()) {
            return returnMultipleError(multiple);
        } else {
            return Result.err(new RemoteDataFetcherError("No file change event found."));
        }
    }

    @Override
    public FileChangeEvent get(DataFetchingEnvironment environment) throws Exception {
        return environment.getRoot();
    }


    private static <U> @NotNull List<Result<FileChangeEvent, RemoteDataFetcherError>> parseResult(List<U> fromValue) {
        var multiple = fromValue.stream()
                .flatMap(u -> {
                    try {
                        return Stream.of(Result.<FileChangeEvent, RemoteDataFetcherError>ok((FileChangeEvent) u));
                    } catch (ClassCastException e) {
                        return Stream.of(Result.<FileChangeEvent, RemoteDataFetcherError>err(new RemoteDataFetcherError(e)));
                    }
                })
                .toList();
        return multiple;
    }

    private static @NotNull Result<FileChangeEvent, RemoteDataFetcherError> returnMultipleError(
            List<Result<FileChangeEvent, RemoteDataFetcherError>> multiple
    ) {
        Result<FileChangeEvent, RemoteDataFetcherError> res = multiple.getFirst();
        if (res.isError()) {
            assert res.error() != null;
            res.error().addError(Result.Error.fromMessage("Multiple data fetcher file change events found. Returning only one with error."));
            return res;
        } else {
            return Result.from(res.get(), new RemoteDataFetcherError("Multiple file change events found."));
        }
    }
}

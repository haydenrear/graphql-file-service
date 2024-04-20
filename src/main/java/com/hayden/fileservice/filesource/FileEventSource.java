package com.hayden.fileservice.filesource;

import com.hayden.fileservice.codegen.types.FileChangeEvent;
import com.hayden.fileservice.codegen.types.FileChangeEventInput;
import com.hayden.fileservice.codegen.types.FileMetadata;
import com.hayden.fileservice.codegen.types.FileSearch;
import com.hayden.fileservice.filechange.FileChangeService;
import com.hayden.fileservice.filechange.FileChangeSync;
import com.hayden.utilitymodule.result.Result;
import com.netflix.graphql.dgs.DgsMutation;
import graphql.schema.DataFetchingEnvironment;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsSubscription;

@DgsComponent
@RequiredArgsConstructor
public class FileEventSource implements FileEventSourceActions{

    private final FileChangeService fileChangeService;
    private final FileDataSource fileDataSource;

    public Result<Flux<Result<FileMetadata, FileEventSourceActions.FileEventError>>, FileEventSourceActions.FileEventError> files() {
        return null;
    }

    public Result<Flux<Result<FileChangeSync, FileEventSourceActions.FileEventError>>, FileEventSourceActions.FileEventError> subscribeFileChange(String fileId) {
//        try {
//            return Result.fromResult(Flux.from(fileChangeService.subscribe(fileId)).map(Result::fromResult));
//        } catch (IOException e) {
//            return Result.fromError(new FileEventSource.FileEventError(e));
//        }
        return Result.emptyError();
    }

    @DgsMutation
    public FileMetadata update(DataFetchingEnvironment dataFetchingEnvironment) {
        return doUpdate(new FileChangeEventInput())
                .orElse(null);
    }

    @DgsSubscription
    public Flux<FileMetadata> fileMetadata(FileChangeEvent fileEventChange) {
        return Flux.empty();
    }

    @DgsSubscription
    public Flux<FileChangeEvent> files(FileSearch fileSearch) {
        return getFiles(fileSearch)
                .orElse(Flux.empty())
                .flatMap(r -> reactor.core.publisher.Mono.justOrEmpty(r.orElse(null)).flux());
    }

    @Override
    public Result<Flux<Result<FileMetadata, FileEventError>>, FileEventError> getFileMetadata(FileSearch fetchingEnvironment) {
        return null;
    }

    @Override
    public Result<Flux<Result<FileChangeEvent, FileEventError>>, FileEventError> getFiles(FileSearch dataFetchingEnvironment) {
        return Result.fromResult(
                Flux.fromIterable(this.fileDataSource.getData(dataFetchingEnvironment))
                        .map(Result::fromResult)
        );
    }

    @Override
    public Result<FileMetadata, FileEventError> doUpdate(FileChangeEventInput input) {
        return null;
    }
}

package com.hayden.fileservice.graphql;

import com.hayden.fileservice.codegen.types.FileChangeEvent;
import com.hayden.fileservice.codegen.types.FileChangeEventInput;
import com.hayden.fileservice.codegen.types.FileMetadata;
import com.hayden.fileservice.codegen.types.FileSearch;
import com.hayden.fileservice.filechange.FileChangeService;
import com.hayden.fileservice.filesource.FileDataSource;
import com.hayden.utilitymodule.result.Result;
import com.netflix.graphql.dgs.DgsMutation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;

import com.netflix.graphql.dgs.DgsComponent;
import com.netflix.graphql.dgs.DgsSubscription;
import reactor.core.publisher.Mono;

import java.util.function.Consumer;

@DgsComponent
@RequiredArgsConstructor
@Slf4j
public class FileEventSource implements FileEventSourceActions{

    public static final String RETRIEVING_FILE_METADATA = "retrieving file metadata";
    public static final String DOING_UPDATING = "doing updating";
    public static final String SEARCHING_FOR_FILES = "searching for files";
    private final FileChangeService fileChangeService;
    private final FileDataSource fileDataSource;

    @DgsMutation
    public FileMetadata update(FileChangeEventInput dataFetchingEnvironment) {
        return doUpdate(dataFetchingEnvironment)
                .doOnError(mapSearchError(DOING_UPDATING))
                .orElse(null);
    }

    @DgsSubscription
    public Flux<FileMetadata> fileMetadata(FileSearch fileEventChange) {
        return Flux.fromStream(this.getFileMetadata(fileEventChange)
                        .doOnError(mapSearchError(RETRIEVING_FILE_METADATA))
                        .stream()
                )
                .flatMap(r -> Flux.from(r).flatMap(result -> Flux.fromStream(result
                        .doOnError(mapSearchError(RETRIEVING_FILE_METADATA))
                        .stream()
                )));
    }

    @DgsSubscription(field = "files")
    public Flux<FileChangeEvent> ok(FileSearch fileSearch) {
        return Flux.from(
                        getFiles(fileSearch)
                                .doOnError(mapSearchError(SEARCHING_FOR_FILES))
                                .orElse(Flux.empty())
                )
                .flatMap(r -> Mono.justOrEmpty(
                        r.doOnError(mapSearchError(SEARCHING_FOR_FILES))
                        .orElse(null)
                ).flux());
    }

    private static @NotNull Consumer<FileEventError> mapSearchError(String doing) {
        return f -> log.error("Error when {}: {}", doing, f);
    }

    @Override
    public Result<Publisher<Result<FileMetadata, FileEventError>>, FileEventError> getFileMetadata(FileSearch fetchingEnvironment) {
        return Result.ok(fileDataSource.getMetadata(fetchingEnvironment));
    }

    @Override
    public Result<Publisher<Result<FileChangeEvent, FileEventError>>, FileEventError> getFiles(FileSearch dataFetchingEnvironment) {
        return Result.ok(
                Flux.from(this.fileDataSource.getFile(dataFetchingEnvironment))
        );
    }

    @Override
    public Result<FileMetadata, FileEventError> doUpdate(FileChangeEventInput input) {
        return fileDataSource.update(input);
    }
}

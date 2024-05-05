package com.hayden.fileservice.filesource;

import com.hayden.fileservice.codegen.types.*;
import com.hayden.fileservice.filesource.fileoperations.FileOperations;
import com.hayden.fileservice.graphql.FileEventSourceActions;
import com.hayden.utilitymodule.result.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.springframework.stereotype.Component;

import reactor.core.publisher.Flux;

@Component
@RequiredArgsConstructor
@Slf4j
public class FileDataSourceImpl implements FileDataSource {

    private final FileOperations fileOperations;

    @Override
    public Publisher<Result<FileMetadata, FileEventSourceActions.FileEventError>> getMetadata(FileSearch path) {
        return fileOperations.getMetadata(path);
    }

    @Override
    public Flux<Result<FileChangeEvent, FileEventSourceActions.FileEventError>> getFile(FileSearch path) {
        return Flux.from(fileOperations.getFile(path));
    }

    @Override
    public Result<FileMetadata, FileEventSourceActions.FileEventError> update(FileChangeEventInput input) {
        return switch(input.getChangeType()) {
            case DELETED -> fileOperations.deleteFile(input);
            case CREATED -> fileOperations.createFile(input);
            case ADD_CONTENT -> fileOperations.addContent(input);
            case REMOVE_CONTENT -> fileOperations.removeContent(input);
            case EXISTING -> Result.err(new FileEventSourceActions.FileEventError("Cannot update existing."));
        };
    }


}

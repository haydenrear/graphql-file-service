package com.hayden.fileservice.filesource;

import com.hayden.fileservice.codegen.types.*;
import com.hayden.fileservice.graphql.FileEventSourceActions;
import com.hayden.utilitymodule.result.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

import reactor.core.publisher.Flux;

@Component
@RequiredArgsConstructor
@Slf4j
public class FileDataSourceImpl implements FileDataSource {

    private final FileOperations fileOperations;

    @Override
    public List<Result<FileMetadata, FileEventSourceActions.FileEventError>> getMetadata(FileSearch path) {
        return fileOperations.metadata(path);
    }

    @Override
    public Flux<Result<FileChangeEvent, FileEventSourceActions.FileEventError>> getData(FileSearch path) {
        return Flux.from(fileOperations.read(path));
    }

    @Override
    public Result<FileMetadata, FileEventSourceActions.FileEventError> update(FileChangeEventInput input) {
        return fileOperations.doUpdate(input);
    }


}

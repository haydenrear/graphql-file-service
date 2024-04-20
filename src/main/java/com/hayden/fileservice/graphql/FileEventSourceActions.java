package com.hayden.fileservice.graphql;

import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.List;

import com.hayden.fileservice.codegen.types.FileChangeEvent;
import com.hayden.fileservice.codegen.types.FileChangeEventInput;
import com.hayden.fileservice.codegen.types.FileMetadata;
import com.hayden.fileservice.codegen.types.FileSearch;
import com.hayden.utilitymodule.result.Result;
import reactor.core.publisher.Flux;

public interface FileEventSourceActions {

    record FileEventError(List<Result.Error> fileEventErrors) implements Result.AggregateError {
        public FileEventError() {
            this(new ArrayList<>());
        }

        public FileEventError(Throwable throwable) {
            this(Lists.newArrayList(Result.Error.fromE(throwable)));
        }

        public FileEventError(String throwable) {
            this(Lists.newArrayList(Result.Error.fromMessage(throwable)));
        }

        @Override
        public List<Result.Error> errors() {
            return fileEventErrors;
        }
    }

    Result<Flux<Result<FileMetadata, FileEventError>>, FileEventError> getFileMetadata(FileSearch fetchingEnvironment);

    Result<Flux<Result<FileChangeEvent, FileEventError>>, FileEventError> getFiles(FileSearch dataFetchingEnvironment);

    Result<FileMetadata, FileEventError> doUpdate(FileChangeEventInput input);

}

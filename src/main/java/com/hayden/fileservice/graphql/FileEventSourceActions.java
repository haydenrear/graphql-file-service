package com.hayden.fileservice.graphql;

import java.util.HashSet;
import java.util.Set;

import com.google.common.collect.Sets;
import com.hayden.fileservice.codegen.types.FileChangeEvent;
import com.hayden.fileservice.codegen.types.FileChangeEventInput;
import com.hayden.fileservice.codegen.types.FileMetadata;
import com.hayden.fileservice.codegen.types.FileSearch;
import com.hayden.utilitymodule.result.agg.AggregateError;
import com.hayden.utilitymodule.result.agg.AggregateParamError;
import com.hayden.utilitymodule.result.error.SingleError;
import com.hayden.utilitymodule.result.Result;
import org.reactivestreams.Publisher;

public interface FileEventSourceActions {

    record FileEventError(Set<FileEventErrorItem> fileEventErrors) implements AggregateParamError<FileEventErrorItem>, AggregateError<FileEventErrorItem> {
        public FileEventError() {
            this(new HashSet<>());
        }

        public FileEventError(Throwable throwable) {
            this(Sets.newHashSet(new FileEventErrorItem(throwable)));
        }

        public FileEventError(Throwable throwable, String cause) {
            this(Sets.newHashSet(new FileEventErrorItem(SingleError.fromE(throwable, cause))));
        }

        public FileEventError(String throwable) {
            this(Sets.newHashSet(new FileEventErrorItem(SingleError.fromMessage(throwable))));
        }

        @Override
        public Set<FileEventErrorItem> errors() {
            return fileEventErrors;
        }
    }

    record FileEventErrorItem(SingleError fileEventErrors) implements SingleError {

        public FileEventErrorItem(Throwable throwable) {
            this(SingleError.fromE(throwable));
        }

        public FileEventErrorItem(Throwable throwable, String cause) {
            this(SingleError.fromE(throwable, cause));
        }

        public FileEventErrorItem(String throwable) {
            this(SingleError.fromMessage(throwable));
        }

        @Override
        public String getMessage() {
            return fileEventErrors().getMessage();
        }
    }

    Result<Publisher<Result<FileMetadata, FileEventError>>, FileEventError> getFileMetadata(FileSearch fetchingEnvironment);

    Result<Publisher<Result<FileChangeEvent, FileEventError>>, FileEventError> getFiles(FileSearch dataFetchingEnvironment);

    Result<FileMetadata, FileEventError> doUpdate(FileChangeEventInput input);

}

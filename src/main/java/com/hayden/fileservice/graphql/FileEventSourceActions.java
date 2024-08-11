package com.hayden.fileservice.graphql;

import java.util.HashSet;
import java.util.Set;

import com.google.common.collect.Sets;
import com.hayden.fileservice.codegen.types.FileChangeEvent;
import com.hayden.fileservice.codegen.types.FileChangeEventInput;
import com.hayden.fileservice.codegen.types.FileMetadata;
import com.hayden.fileservice.codegen.types.FileSearch;
import com.hayden.utilitymodule.result.error.AggregateError;
import com.hayden.utilitymodule.result.error.ErrorCollect;
import com.hayden.utilitymodule.result.Result;
import org.reactivestreams.Publisher;

public interface FileEventSourceActions {

    record FileEventError(Set<ErrorCollect> fileEventErrors) implements AggregateError {
        public FileEventError() {
            this(new HashSet<>());
        }

        public FileEventError(Throwable throwable) {
            this(Sets.newHashSet(ErrorCollect.fromE(throwable)));
        }

        public FileEventError(Throwable throwable, String cause) {
            this(Sets.newHashSet(ErrorCollect.fromE(throwable, cause)));
        }

        public FileEventError(String throwable) {
            this(Sets.newHashSet(ErrorCollect.fromMessage(throwable)));
        }

        @Override
        public Set<ErrorCollect> errors() {
            return fileEventErrors;
        }
    }

    Result<Publisher<Result<FileMetadata, FileEventError>>, FileEventError> getFileMetadata(FileSearch fetchingEnvironment);

    Result<Publisher<Result<FileChangeEvent, FileEventError>>, FileEventError> getFiles(FileSearch dataFetchingEnvironment);

    Result<FileMetadata, FileEventError> doUpdate(FileChangeEventInput input);

}

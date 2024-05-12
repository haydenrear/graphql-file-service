package com.hayden.fileservice.filesource.fileoperations;

import com.hayden.fileservice.codegen.types.*;
import com.hayden.fileservice.filesource.directoryoperations.DirectoryOperations;
import com.hayden.fileservice.graphql.FileEventSourceActions;
import com.hayden.utilitymodule.result.Result;
import org.jetbrains.annotations.NotNull;
import org.reactivestreams.Publisher;

import java.io.File;
import java.util.function.Function;

public interface FileOperations extends DirectoryOperations {

    Result<FileMetadata, FileEventSourceActions.FileEventError> createFile(FileChangeEventInput input);

    Result<FileMetadata, FileEventSourceActions.FileEventError> deleteFile(FileChangeEventInput input);

    Result<FileMetadata, FileEventSourceActions.FileEventError> addContent(FileChangeEventInput input);

    Result<FileMetadata, FileEventSourceActions.FileEventError> removeContent(FileChangeEventInput input);

    Publisher<Result<FileChangeEvent, FileEventSourceActions.FileEventError>> getFile(FileSearch path);

    default @NotNull Result<FileMetadata, FileEventSourceActions.FileEventError> doOnFile(FileChangeEventInput input,
                                                                                         Function<File, Result<FileMetadata, FileEventSourceActions.FileEventError>> toDoOnFile, String errorMessage) {
        return search(input.getPath()).findAny()
                .map(toDoOnFile)
                .orElse(Result.err(new FileEventSourceActions.FileEventError(errorMessage)));
    }

}

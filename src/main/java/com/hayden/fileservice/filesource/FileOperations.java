package com.hayden.fileservice.filesource;

import com.hayden.fileservice.codegen.types.*;
import com.hayden.fileservice.graphql.FileEventSourceActions;
import com.hayden.utilitymodule.result.Result;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.reactivestreams.Publisher;

import java.io.File;
import java.util.stream.Stream;

public interface FileOperations {

    Result<FileMetadata, FileEventSourceActions.FileEventError> createFile(FileChangeEventInput input);

    Result<FileMetadata, FileEventSourceActions.FileEventError> deleteFile(FileChangeEventInput input);

    Result<FileMetadata, FileEventSourceActions.FileEventError> addContent(FileChangeEventInput input);

    Result<FileMetadata, FileEventSourceActions.FileEventError> removeContent(FileChangeEventInput input);

    Publisher<Result<FileChangeEvent, FileEventSourceActions.FileEventError>> getFile(FileSearch path);

    Publisher<Result<FileMetadata, FileEventSourceActions.FileEventError>> getMetadata(FileSearch path);

    @NotNull Stream<File> search(String path, @Nullable String fileName);

    default @NotNull Stream<File> search(String path) {
        return this.search(path, null);
    }

}

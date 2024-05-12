package com.hayden.fileservice.filesource.directoryoperations;

import com.hayden.fileservice.codegen.types.FileMetadata;
import com.hayden.fileservice.codegen.types.FileSearch;
import com.hayden.fileservice.graphql.FileEventSourceActions;
import com.hayden.utilitymodule.result.Result;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.reactivestreams.Publisher;

import java.io.File;
import java.util.stream.Stream;

public interface DirectoryOperations {
    Publisher<Result<FileMetadata, FileEventSourceActions.FileEventError>> getMetadata(FileSearch path);

    Stream<File> search(String path, @Nullable String fileName);

    default @NotNull Stream<File> search(String path) {
        return this.search(path, null);
    }

}

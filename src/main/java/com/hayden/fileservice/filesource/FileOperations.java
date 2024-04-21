package com.hayden.fileservice.filesource;

import com.hayden.fileservice.codegen.types.*;
import com.hayden.fileservice.graphql.FileEventSourceActions;
import com.hayden.utilitymodule.result.Result;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.reactivestreams.Publisher;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Stream;

public interface FileOperations {

    Result<FileMetadata, FileEventSourceActions.FileEventError> createFile(FileChangeEventInput input);

    Result<FileMetadata, FileEventSourceActions.FileEventError> deleteFile(FileChangeEventInput input);

    Result<FileMetadata, FileEventSourceActions.FileEventError> addContent(FileChangeEventInput input);

    Result<FileMetadata, FileEventSourceActions.FileEventError> removeContent(FileChangeEventInput input);

    Publisher<Result<FileChangeEvent, FileEventSourceActions.FileEventError>> getFile(FileSearch path);

    List<Result<FileMetadata, FileEventSourceActions.FileEventError>> getMetadata(FileSearch path);

    @NotNull Stream<File> filteredFile(String path, @Nullable String fileName);

    default @NotNull Stream<File> filteredFile(String path) {
        return this.filteredFile(path, null);
    }

    static @NotNull Result<FileMetadata, FileEventSourceActions.FileEventError> fileMetadata(File nextFile, FileChangeType input) {
        return Result.fromResult(new FileMetadata(nextFile.getAbsolutePath(), nextFile.getName(), nextFile.getAbsolutePath(), (int) nextFile.length(),
                LocalDate.ofInstant(Instant.ofEpochMilli(nextFile.lastModified()), ZoneId.systemDefault()), input));
    }

    static @NotNull Result<FileMetadata, FileEventSourceActions.FileEventError> fileMetadata(Throwable t) {
        return Result.fromError(new FileEventSourceActions.FileEventError(t));
    }

    static @NotNull Result<FileMetadata, FileEventSourceActions.FileEventError> createFile(FileChangeEventInput input, File nextFile) {
        try(FileOutputStream fos = new FileOutputStream(nextFile))  {
            fos.write(input.getData().getBytes());
        } catch (IOException e) {
            return fileMetadata(e);
        }

        return fileMetadata(nextFile, input.getChangeType());
    }


}

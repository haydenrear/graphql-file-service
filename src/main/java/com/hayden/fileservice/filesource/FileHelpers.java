package com.hayden.fileservice.filesource;

import com.hayden.fileservice.codegen.types.FileChangeEventInput;
import com.hayden.fileservice.codegen.types.FileChangeType;
import com.hayden.fileservice.codegen.types.FileMetadata;
import com.hayden.fileservice.graphql.FileEventSourceActions;
import com.hayden.utilitymodule.result.Result;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

public interface FileHelpers {


    static @NotNull Result<FileMetadata, FileEventSourceActions.FileEventError> fileMetadata(File nextFile, FileChangeType input) {
        return Result.ok(new FileMetadata(nextFile.getAbsolutePath(), nextFile.getName(), nextFile.getAbsolutePath(), (int) nextFile.length(),
                LocalDate.ofInstant(Instant.ofEpochMilli(nextFile.lastModified()), ZoneId.systemDefault()), input));
    }

    static @NotNull Result<FileMetadata, FileEventSourceActions.FileEventError> fileMetadata(Throwable t) {
        return Result.err(new FileEventSourceActions.FileEventError(t));
    }

    static @NotNull Result<FileMetadata, FileEventSourceActions.FileEventError> createFile(FileChangeEventInput input, File nextFile) {
        return createFile(input, nextFile, 0);
    }

    static @NotNull Result<FileMetadata, FileEventSourceActions.FileEventError> createFile(byte[] input, File nextFile, FileChangeType fileChangeType) {
        try(var fos = new RandomAccessFile(nextFile, "rw"))  {
            fos.seek(0);
            fos.write(input);
        } catch (IOException e) {
            return fileMetadata(e);
        }

        return fileMetadata(nextFile, fileChangeType);
    }

    static @NotNull Result<FileMetadata, FileEventSourceActions.FileEventError> createFile(FileChangeEventInput input, File nextFile, long startingOffset) {
        try(var fos = new RandomAccessFile(nextFile, "rw"))  {
            fos.seek(startingOffset);
            fos.write(input.getData().getBytes());
        } catch (IOException e) {
            return fileMetadata(e);
        }

        return fileMetadata(nextFile, input.getChangeType());
    }

    static @NotNull Result<FileMetadata, FileEventSourceActions.FileEventError> deleteFile(FileChangeEventInput input, File nextFile) {
        if (!nextFile.delete()) {
            return Result.err(new FileEventSourceActions.FileEventError("Could not delete %s.".formatted(nextFile)));
        }

        return fileMetadata(nextFile, input.getChangeType());
    }
}

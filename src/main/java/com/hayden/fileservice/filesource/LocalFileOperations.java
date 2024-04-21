package com.hayden.fileservice.filesource;

import com.hayden.fileservice.codegen.types.*;
import com.hayden.fileservice.config.ByteArray;
import com.hayden.fileservice.config.FileProperties;
import com.hayden.fileservice.graphql.FileEventSourceActions;
import com.hayden.fileservice.io.FileStream;
import com.hayden.utilitymodule.result.Result;
import graphql.Assert;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.reactivestreams.Publisher;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Stream;
import reactor.core.publisher.Flux;

@Component
@RequiredArgsConstructor
public class LocalFileOperations implements FileOperations {

    private final FileStream fileStream;

    private final FileProperties fileProperties;

    @Override
    public Result<FileMetadata, FileEventSourceActions.FileEventError> createFile(FileChangeEventInput input) {
        return filteredFile(input.getPath())
                .findAny()
                .map(f -> FileOperations.createFile(input, f))
                .orElse(Result.fromError(new FileEventSourceActions.FileEventError("Could not find ")));
    }

    @Override
    public Result<FileMetadata, FileEventSourceActions.FileEventError> deleteFile(FileChangeEventInput input) {
        return doOnFile(input, nextFile -> {
            if (!nextFile.delete()) {
                return Result.<FileMetadata, FileEventSourceActions.FileEventError>
                        fromError(new FileEventSourceActions.FileEventError("Error deleting file."));
            }

            return FileOperations.fileMetadata(nextFile, input.getChangeType());
        }, "Could not find.");
    }

    private @NotNull Result<FileMetadata, FileEventSourceActions.FileEventError> doOnFile(FileChangeEventInput input, Function<File, Result<FileMetadata, FileEventSourceActions.FileEventError>> toDoOnFile, String errorMessage) {
        return filteredFile(input.getPath()).findAny()
                .map(toDoOnFile)
                .orElse(Result.fromError(new FileEventSourceActions.FileEventError(errorMessage)));
    }

    @Override
    public Result<FileMetadata, FileEventSourceActions.FileEventError> addContent(FileChangeEventInput input) {
        return doOnFile(input, nextFile -> addContent(input, nextFile), "Could not find.");
    }

    @Override
    public Result<FileMetadata, FileEventSourceActions.FileEventError> removeContent(FileChangeEventInput input) {
        return doOnFile(input, nextFile -> removeContent(input, nextFile), "Could not find.");
    }

    @Override
    public Publisher<Result<FileChangeEvent, FileEventSourceActions.FileEventError>> getFile(FileSearch path) {
        return Flux.fromStream(this.filteredFile(path.getPath(), path.getFileName()))
                .flatMap(nextFile -> {
                    AtomicInteger i = new AtomicInteger(0);
                    return Flux.from(fileStream.readFileInChunks(nextFile.getPath(), fileProperties.getBufferSize()))
                            .map(bufRes -> bufRes.map(buf -> new FileChangeEvent(
                                    nextFile.getAbsolutePath(), FileChangeType.EXISTING, i.getAndAdd(fileProperties.getBufferSize()),
                                    new ByteArray(buf.array()), nextFile.getAbsolutePath()
                            )));
                });
    }

    @Override
    public List<Result<FileMetadata, FileEventSourceActions.FileEventError>> getMetadata(FileSearch path) {
        return this.filteredFile(path.getPath(), path.getFileName())
                .map(nextFile -> FileOperations.fileMetadata(nextFile, FileChangeType.EXISTING))
                .toList();

    }


    private static @NotNull Result<FileMetadata, FileEventSourceActions.FileEventError> addContent(FileChangeEventInput input, File nextFile) {
        int offset = input.getOffset();
        try(var file = new RandomAccessFile(nextFile, "rw")) {
            // Move the pointer to the insertion point
            file.seek(offset);

            // Save the data after the insertion point
            byte[] temp = new byte[(int) (file.length() - offset)];
            file.read(temp);

            // Write the new data at the insertion point
            file.seek(offset);
            file.write(input.getData().getBytes());

            // Write back the saved data after the insertion point
            file.write(temp);
        } catch (IOException e) {
            return FileOperations.fileMetadata(e);
        }

        return FileOperations.fileMetadata(nextFile, input.getChangeType());
    }

    private static @NotNull Result<FileMetadata, FileEventSourceActions.FileEventError> removeContent(FileChangeEventInput input, File nextFile) {
        int offset = input.getOffset();
        try (RandomAccessFile file = new RandomAccessFile(nextFile, "rw")) {
            // Move the file pointer to the position
            file.seek(offset);

            // Delete content at the specified position
            byte[] buffer = new byte[1024];
            long remainingBytes = input.getLength();
            while (remainingBytes > 0) {
                int bytesRead = file.read(buffer, 0, (int) Math.min(buffer.length, remainingBytes));
                if (bytesRead == -1) {
                    break;
                }
                file.seek(file.getFilePointer() - bytesRead);
                file.write(new byte[bytesRead]);
                file.seek(file.getFilePointer() + remainingBytes - bytesRead);
                remainingBytes -= bytesRead;
            }
        } catch (IOException e) {
            return FileOperations.fileMetadata(e);
        }

        return FileOperations.fileMetadata(nextFile, input.getChangeType());
    }

    public @NotNull Stream<File> filteredFile(String path, @Nullable String fileName) {
        File file = Paths.get(path).toFile();
        boolean isDirectory = file.isDirectory();
        Assert.assertTrue(!isDirectory || fileName != null);
        return isDirectory
                ? Optional.ofNullable(file.listFiles())
                .stream().flatMap(Arrays::stream)
                .filter(f -> Optional.of(fileName).map(fileNameFilter -> fileNameFilter.equals(f.getName()))
                        .orElse(true))
                : Optional.of(file).stream();
    }
}

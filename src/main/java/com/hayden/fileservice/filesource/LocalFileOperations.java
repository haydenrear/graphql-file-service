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
        return search(input.getPath())
                .findAny()
                .map(f -> FileHelpers.createFile(input, f))
                .orElse(Result.fromError(new FileEventSourceActions.FileEventError("Could not find ")));
    }

    @Override
    public Result<FileMetadata, FileEventSourceActions.FileEventError> deleteFile(FileChangeEventInput input) {
        return doOnFile(input, nextFile -> {
            if (!nextFile.delete()) {
                return Result.<FileMetadata, FileEventSourceActions.FileEventError>
                        fromError(new FileEventSourceActions.FileEventError("Error deleting file."));
            }

            return FileHelpers.fileMetadata(nextFile, input.getChangeType());
        }, "Could not find.");
    }

    @Override
    public Result<FileMetadata, FileEventSourceActions.FileEventError> addContent(FileChangeEventInput input) {
        return doOnFile(input, nextFile -> addContent(input, nextFile), "Could not find.");
    }

    @Override
    public Result<FileMetadata, FileEventSourceActions.FileEventError> removeContent(FileChangeEventInput input) {
        return doOnFile(input, nextFile -> removeContent(input, nextFile, fileProperties.getBufferSize()), "Could not find.");
    }

    @Override
    public Publisher<Result<FileChangeEvent, FileEventSourceActions.FileEventError>> getFile(FileSearch path) {
        return Flux.fromStream(this.search(path.getPath(), path.getFileName()))
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
    public Publisher<Result<FileMetadata, FileEventSourceActions.FileEventError>> getMetadata(FileSearch path) {
        return Flux.fromStream(
                this.search(path.getPath(), path.getFileName())
                        .map(nextFile -> FileHelpers.fileMetadata(nextFile, FileChangeType.EXISTING))
        );
    }

    @Override
    public @NotNull Stream<File> search(String path, @Nullable String fileName) {
        File file = Paths.get(path).toFile();
        return file.isDirectory()
                ? Optional.ofNullable(file.listFiles())
                .stream().flatMap(Arrays::stream)
                .filter(f -> Optional.ofNullable(fileName)
                        .map(fileNameFilter -> fileNameFilter.equals(f.getName()))
                        .orElse(true)
                )
                : Optional.of(file).stream();
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
            return FileHelpers.fileMetadata(e);
        }

        return FileHelpers.fileMetadata(nextFile, input.getChangeType());
    }

    @NotNull Result<FileMetadata, FileEventSourceActions.FileEventError> removeContent(FileChangeEventInput input, File nextFile, int bufferSize) {
        int offset = input.getOffset();
        try (RandomAccessFile file = new RandomAccessFile(nextFile, "rw")) {
            // Move the file pointer to the position
            // the offset + length
            long startKeep = offset + input.getLength();
            long writePointer = offset;
            long amtConsume = Math.min(file.length() - offset, bufferSize);
            long readPointer = startKeep;
            long startLength = file.length();
            while (writePointer <= startLength && readPointer < startLength) {
                file.seek(readPointer);
                byte[] toRead = new byte[(int) amtConsume];
                file.read(toRead, 0, (int) amtConsume);
                file.seek(writePointer);
                file.write(toRead);
                readPointer += amtConsume;
                long length = file.length();
                writePointer += amtConsume;
                amtConsume = Math.min((length - amtConsume) - offset, bufferSize);
            }

            file.setLength(startLength - input.getLength());

        } catch (IOException e) {
            return FileHelpers.fileMetadata(e);
        }

        return FileHelpers.fileMetadata(nextFile, input.getChangeType());
    }

    private @NotNull Result<FileMetadata, FileEventSourceActions.FileEventError> doOnFile(FileChangeEventInput input,
                                                                                          Function<File, Result<FileMetadata, FileEventSourceActions.FileEventError>> toDoOnFile, String errorMessage) {
        return search(input.getPath()).findAny()
                .map(toDoOnFile)
                .orElse(Result.fromError(new FileEventSourceActions.FileEventError(errorMessage)));
    }

}

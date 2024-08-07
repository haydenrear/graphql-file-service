package com.hayden.fileservice.filesource.fileoperations.naive;

import com.hayden.fileservice.codegen.types.*;
import com.hayden.fileservice.config.ByteArray;
import com.hayden.fileservice.config.FileProperties;
import com.hayden.fileservice.filesource.FileHelpers;
import com.hayden.fileservice.filesource.fileoperations.FileOperations;
import com.hayden.fileservice.filesource.directoryoperations.LocalDirectoryOperations;
import com.hayden.fileservice.graphql.FileEventSourceActions;
import com.hayden.fileservice.io.FileStream;
import com.hayden.utilitymodule.result.Result;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.reactivestreams.Publisher;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import reactor.core.publisher.Flux;


@Component
@RequiredArgsConstructor
@Profile("naive")
public class LocalNaiveFileOperations implements FileOperations {

    private final FileStream fileStream;
    private final FileProperties fileProperties;
    private final LocalDirectoryOperations localDirectoryOperations;

    @Override
    public Publisher<Result<FileMetadata, FileEventSourceActions.FileEventError>> getMetadata(FileSearch path) {
        return this.localDirectoryOperations.getMetadata(path);
    }

    @Override
    public Stream<File> search(String path, @Nullable String fileName) {
        return this.localDirectoryOperations.search(path, fileName);
    }

    @Override
    public Result<FileMetadata, FileEventSourceActions.FileEventError> createFile(FileChangeEventInput input) {
        return search(input.getPath())
                .findAny()
                .map(f -> FileHelpers.writeToFile(input, f))
                .orElse(Result.err(new FileEventSourceActions.FileEventError("Could not find ")));
    }

    @Override
    public Result<FileMetadata, FileEventSourceActions.FileEventError> deleteFile(FileChangeEventInput input) {
        return doOnFile(input, nextFile -> {
            if (!nextFile.delete()) {
                return Result.<FileMetadata, FileEventSourceActions.FileEventError>
                        err(new FileEventSourceActions.FileEventError("Error deleting file."));
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

    @NotNull
    public Result<FileMetadata, FileEventSourceActions.FileEventError> removeContent(FileChangeEventInput input, File nextFile, int bufferSize) {
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


}

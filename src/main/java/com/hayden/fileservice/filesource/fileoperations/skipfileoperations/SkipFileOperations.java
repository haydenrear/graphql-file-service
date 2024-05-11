package com.hayden.fileservice.filesource.fileoperations.skipfileoperations;

import com.hayden.fileservice.codegen.types.*;
import com.hayden.fileservice.config.ByteArray;
import com.hayden.fileservice.config.FileProperties;
import com.hayden.fileservice.filesource.FileHelpers;
import com.hayden.fileservice.filesource.fileoperations.FileOperations;
import com.hayden.fileservice.filesource.directoroperations.LocalDirectoryOperations;
import com.hayden.fileservice.filesource.fileoperations.skipfileoperations.datanode.DataNode;
import com.hayden.fileservice.filesource.fileoperations.skipfileoperations.datanode.DataNodeOperations;
import com.hayden.fileservice.graphql.FileEventSourceActions;
import com.hayden.utilitymodule.result.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.reactivestreams.Publisher;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.io.*;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * File is like:
 * * - - - - - - - - - - *
 * | Head descriptor     |  - Start of operations data, end of operations data.
 * * - - - - - - - - - - *
 * | Header Op Indices   | - Pointers to the changes made, each pointer 12 bytes * 4 (see HeaderOpIndices) + a.
 * * - - - - - - - - - - *
 * |    Operations Data |  - Changes made since last compression.
 * * - - - - - - - - - - *
 * There exist no Remove. The indices are of 12 bytes, allowing for max file size
 * of ~ 50-70GB. There exists a null character for removes, so that there does not
 * need to be a shift of the data over, which is the whole purpose of this effort,
 * moving from shifting to pointers.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SkipFileOperations implements FileOperations {

    private final FileProperties fileProperties;
    private final DataNodeOperations dataNodeOperationsDelegate;
    private final LocalDirectoryOperations localDirectoryOperations;


    @Override
    public Result<FileMetadata, FileEventSourceActions.FileEventError> createFile(FileChangeEventInput input) {
        FileHeader.HeaderDescriptor descriptor = new FileHeader.HeaderDescriptor(
                List.of(new DataNode.AddNode(
                        0,
                        input.getLength(),
                        fileProperties.getDataStreamFileHeaderLengthBytes(),
                        fileProperties.getDataStreamFileHeaderLengthBytes() + input.getLength(), true
                )),
                new FileHeader.HeaderDescriptorData(
                        fileProperties.getDataStreamFileHeaderLengthBytes(),
                        fileProperties.getDataStreamFileHeaderLengthBytes() + input.getLength()
                )
        );
        return Result.from(search(input.getPath()).findAny(), (Supplier<FileEventSourceActions.FileEventError>) FileEventSourceActions.FileEventError::new)
                .flatMapResult(file -> HeaderOperationTypes.writeHeader(descriptor, fileProperties)
                        .flatMapResult(f -> HeaderOperationTypes.flushHeader(file, f))
                        .flatMapResult(f -> FileHelpers.createFile(input, file, fileProperties.getDataStreamFileHeaderLengthBytes()))
                );
    }


    @Override
    public Result<FileMetadata, FileEventSourceActions.FileEventError> deleteFile(FileChangeEventInput input) {
        return search(input.getPath()).findAny()
                .map(file -> FileHelpers.deleteFile(input, file))
                .orElse(Result.err(new FileEventSourceActions.FileEventError()));
    }

    @Override
    public Result<FileMetadata, FileEventSourceActions.FileEventError> addContent(FileChangeEventInput input) {
        return doAddRemoveFileOp(input);
    }

    @Override
    public Result<FileMetadata, FileEventSourceActions.FileEventError> removeContent(FileChangeEventInput input) {
        return doAddRemoveFileOp(input);
    }

    private Result<FileMetadata, FileEventSourceActions.FileEventError> doAddRemoveFileOp(FileChangeEventInput input) {
        return getFileAndHeader(input.getPath())
                .flatMapResult(headerOps -> dataNodeOperationsDelegate.doChangeNode(headerOps.getValue(), input)
                        .mapError(e -> log.error("Error when attempting to insert node: {}.", e.errors()))
                        .map(h -> Map.entry(headerOps.getKey(), h))
                        .map(e -> {
                            if (e.getValue().nodeAdded() instanceof DataNode.AddNode addNode) {
                                try (RandomAccessFile randomAccessFile = new RandomAccessFile(e.getKey(), "rw")) {
                                    randomAccessFile.seek(addNode.dataStart());
                                    randomAccessFile.write(input.getData().getBytes());
                                } catch (
                                        IOException ex) {
                                    throw new RuntimeException(ex);
                                }
                            }
                            return e;
                        })
                )
                .flatMapResult(h -> HeaderOperationTypes.writeHeader(h.getValue().headerDescriptor(), fileProperties)
                        .flatMapResult(f -> {
                            var flushed = HeaderOperationTypes.flushHeader(h.getKey(), f);
                            return flushed;
                        })
                        .map(f -> Map.entry(h.getKey(), f))
                )
                .flatMapResult(h -> FileHelpers.fileMetadata(h.getKey(), input.getChangeType()));
    }

    private Result<Map.Entry<File, FileHeader.HeaderDescriptor>, Result.Error> getFileAndHeader(String path) {
        return Result.from(search(path)
                                .flatMap(f -> FileHeader.parseHeader(f, fileProperties)
                                        .mapError(e -> log.error("Error when parsing file: {}.", e.errors()))
                                        .stream()
                                        .map(b -> Map.entry(f, b))
                                )
                                .findAny(),
                        new FileEventSourceActions.FileEventError("Could not find file.")
                )
                .flatMapResult(byteFile -> Result
                        .from(
                                Optional.of(HeaderOperationTypes.getOps(byteFile.getValue())),
                                new FileEventSourceActions.FileEventError("File operations unsuccessful.")
                        )
                        .flatMapResult(h -> h.map(s -> Map.entry(byteFile.getKey(), s)))
                );
    }

    public Publisher<Result<FileChangeEvent, FileEventSourceActions.FileEventError>> getFile(Path path) {
        return getFile(path.toAbsolutePath().toString()) ;
    }

    /**
     * Stream the file data back to the user.
     * @param path
     * @return
     */
    @Override
    public Publisher<Result<FileChangeEvent, FileEventSourceActions.FileEventError>> getFile(FileSearch path) {
        var fileHeader = getFileAndHeader(path.getPath());
        if (fileHeader.isError()) {
            return Flux.just(Result.err(new FileEventSourceActions.FileEventError("Could not find path for %s.".formatted(path))));
        }
        return Flux.fromStream(fileHeader.stream())
                .flatMap(SkipFileOperations::readFile);
    }

    public Publisher<Result<FileChangeEvent, FileEventSourceActions.FileEventError>> getFile(String path) {
        var fileHeader = getFileAndHeader(path);
        if (fileHeader.isError()) {
            return Flux.just(Result.err(new FileEventSourceActions.FileEventError("Could not find path for %s.".formatted(path))));
        }
        return Flux.fromStream(fileHeader.stream())
                .flatMap(SkipFileOperations::readFile);
    }

    private static @NotNull Flux<Result<FileChangeEvent, FileEventSourceActions.FileEventError>> readFile(Map.Entry<File, FileHeader.HeaderDescriptor> fileData) {
        return Flux.using(
                () -> new RandomAccessFile(fileData.getKey(), "r"),
                file -> Flux.fromStream(fileData.getValue().inIndices().stream())
                        .filter(dataNode -> !(dataNode instanceof DataNode.SkipNode))
                        .sort(Comparator.comparing(DataNode::indexStart))
                        .publishOn(Schedulers.boundedElastic())
                        .flatMap(dataNode -> {
                            try {
                                file.seek(dataNode.dataStart());
                                byte[] toRead = new byte[Math.toIntExact(dataNode.length())];
                                file.read(toRead);
                                String s = new String(toRead);
                                return Flux.just(Result.ok(new FileChangeEvent(
                                        fileData.getKey().getName(),
                                        FileChangeType.EXISTING,
                                        Math.toIntExact(dataNode.indexStart()),
                                        new ByteArray(toRead),
                                        fileData.getKey().getPath())
                                ));
                            } catch (
                                    IOException e) {
                                return Flux.just(Result.err(new FileEventSourceActions.FileEventError(e)));
                            }
                        }),
                file -> {
                    try {
                        file.close();
                    } catch (IOException e) {
                        log.error("Error when closing file.", e);
                    }
                }
        );
    }

    @Override
    public Publisher<Result<FileMetadata, FileEventSourceActions.FileEventError>> getMetadata(FileSearch path) {
        return this.localDirectoryOperations.getMetadata(path);
    }

    @Override
    public Stream<File> search(String path, @Nullable String fileName) {
        return this.localDirectoryOperations.search(path, fileName);
    }
}
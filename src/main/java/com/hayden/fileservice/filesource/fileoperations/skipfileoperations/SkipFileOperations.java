package com.hayden.fileservice.filesource.fileoperations.skipfileoperations;

import com.hayden.fileservice.codegen.types.*;
import com.hayden.fileservice.config.FileProperties;
import com.hayden.fileservice.filesource.FileHelpers;
import com.hayden.fileservice.filesource.fileoperations.FileOperations;
import com.hayden.fileservice.filesource.directoroperations.LocalDirectoryOperations;
import com.hayden.fileservice.graphql.FileEventSourceActions;
import com.hayden.utilitymodule.result.Result;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.io.*;
import java.util.*;

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
    private final DataNodeOperations dataNodeOperations;

    @Delegate
    private final LocalDirectoryOperations localDirectoryOperations;

    @Override
    public Result<FileMetadata, FileEventSourceActions.FileEventError> createFile(FileChangeEventInput input) {
        return search(input.getPath()).findAny()
                .map(file -> FileHelpers.createFile(input, file, fileProperties.getDataStreamFileHeaderLengthBytes()))
                .orElse(Result.err(new FileEventSourceActions.FileEventError()));
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
                .map(headerOps -> Map.entry(headerOps.getKey(), dataNodeOperations.insertNode(headerOps.getValue(), input)))
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
                                Optional.ofNullable(HeaderOperationTypes.getOps(byteFile.getValue())),
                                new FileEventSourceActions.FileEventError("File operations unsuccessful.")
                        )
                        .flatMapResult(h -> h.map(s -> Map.entry(byteFile.getKey(), s)))
                );
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
                .flatMap(fileData -> Flux.using(
                        () -> new RandomAccessFile(fileData.getKey(), "r"),
                        file -> Flux.fromStream(fileData.getValue().inIndices().stream())
                                .filter(dataNode -> !(dataNode instanceof DataNode.SkipNode))
                                .sort(Comparator.comparing(DataNode::indexStart))
                                .publishOn(Schedulers.boundedElastic())
                                .flatMap(dataNode -> {
                                    try {
                                        file.seek(dataNode.dataEnd());
                                        byte [] toRead = new byte[Math.toIntExact(dataNode.length())];
                                        file.read(toRead);
                                        return Flux.just(Result.ok(new FileChangeEvent()));
                                    } catch (IOException e) {
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
                ));
    }

}
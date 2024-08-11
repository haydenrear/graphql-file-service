package com.hayden.fileservice.filesource.fileoperations.skipfileoperations;

import com.hayden.fileservice.codegen.types.*;
import com.hayden.fileservice.config.ByteArray;
import com.hayden.fileservice.config.FileProperties;
import com.hayden.fileservice.filesource.FileHelpers;
import com.hayden.fileservice.filesource.fileoperations.CompactableFileOperations;
import com.hayden.fileservice.filesource.fileoperations.FileOperations;
import com.hayden.fileservice.filesource.directoryoperations.LocalDirectoryOperations;
import com.hayden.fileservice.filesource.fileoperations.skipfileoperations.datanode.DataNode;
import com.hayden.fileservice.filesource.fileoperations.skipfileoperations.datanode.DataNodeOperations;
import com.hayden.fileservice.graphql.FileEventSourceActions;
import com.hayden.utilitymodule.RandomUtils;
import com.hayden.utilitymodule.result.error.ErrorCollect;
import com.hayden.utilitymodule.result.Result;
import com.hayden.utilitymodule.result.map.ResultCollectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.reactivestreams.Publisher;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

import java.io.*;
import java.nio.file.*;
import java.util.*;
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
@Profile("!naive")
public class SkipFileOperations implements FileOperations, CompactableFileOperations {

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
        return Result.fromThunkError(search(input.getPath()).findAny(), FileEventSourceActions.FileEventError::new)
                .flatMapResult(file -> HeaderOperationTypes.writeHeader(descriptor, fileProperties)
                        .flatMapResult(f -> HeaderOperationTypes.flushHeader(file, f))
                        .flatMapResult(f -> FileHelpers.writeToFile(input, file, fileProperties.getDataStreamFileHeaderLengthBytes()))
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
                .flatMapResultError(headerOps -> dataNodeOperationsDelegate.doChangeNode(headerOps.getValue(), input)
                        .doOnError(e -> log.error("Error when attempting to insert node: {}.", e.errors()))
                        .map(h -> Map.entry(headerOps.getKey(), h))
                        .map(e -> getFileChangeNodeOperationsResultEntry(input, e))
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

    private static Map.Entry<File, DataNodeOperations.ChangeNodeOperationsResult> getFileChangeNodeOperationsResultEntry(FileChangeEventInput input, Map.Entry<File, DataNodeOperations.ChangeNodeOperationsResult> e) {
        if (e.getValue().nodeAdded() instanceof DataNode.AddNode addNode) {
            try (RandomAccessFile randomAccessFile = new RandomAccessFile(e.getKey(), "rw")) {
                randomAccessFile.seek(addNode.dataStart());
                randomAccessFile.write(input.getData().getBytes());
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
        return e;
    }

    private Result<Map.Entry<File, FileHeader.HeaderDescriptor>, FileEventSourceActions.FileEventError> getFileAndHeader(String path) {
        return Result.from(search(path)
                                .flatMap(f -> FileHeader.parseHeader(f, fileProperties)
                                        .doOnError(e -> log.error("Error when parsing file: {}.", e.errors()))
                                        .stream()
                                        .map(b -> Map.entry(f, b))
                                )
                                .findAny()
                                .map(Result.ResultInner::ok)
                                .orElse(Result.ResultInner.empty()),
                        Result.Error.err(new FileEventSourceActions.FileEventError("Could not find file."))
                )
                .flatMapResultError(byteFile -> Result
                        .from(
                                HeaderOperationTypes.getOps(byteFile.getValue()),
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


    @Override
    public Result<FileCompactifyResponse, FileEventSourceActions.FileEventError> compactify(File file, FileHeader.HeaderDescriptor headerDescriptor) {
        Path archived;

        try {
            archived = Paths.get(file.toPath().getParent().toString(), "%s_%s".formatted(file.getName(), RandomUtils.randomNumberString(6)));
            Files.copy(file.toPath(), archived);
        } catch (IOException e) {
            return Result.err(new FileEventSourceActions.FileEventError(e, "Could not create archive file during compactify."));
        }

        try{
            return compactifyFileAndFlushHeader(file, headerDescriptor, archived);
        } catch (IOException e) {
            return Result.err(new FileEventSourceActions.FileEventError(e));
        }
    }

    @Override
    public Result<FileFlushResponse, FileEventSourceActions.FileEventError> flush(File file, FileHeader.HeaderDescriptor headerDescriptor, FileChangeEvent input) {

        return headerDescriptor.inIndices()
                .stream()
                .flatMap(d -> {
                    if (d instanceof DataNode.AddNode addNode && addNode.change()) {
                        try (RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw")) {
                            randomAccessFile.seek(addNode.dataStart());
                            randomAccessFile.write(input.getData().getBytes());
                        } catch (IOException ex) {
                            return Stream.of(Result.<FileFlushResponse, FileEventSourceActions.FileEventError>err(new FileEventSourceActions.FileEventError(ex)));
                        }
                    }

                    return Stream.of(Result.<FileFlushResponse, FileEventSourceActions.FileEventError>ok(new FileFlushResponse()));
                })
                .collect(ResultCollectors.AggregateResultCollector.toResult(() -> new FileFlushResponse(file), FileEventSourceActions.FileEventError::new));
    }

    private @Nullable Result<FileCompactifyResponse, FileEventSourceActions.FileEventError> compactifyFileAndFlushHeader(File file, FileHeader.HeaderDescriptor headerDescriptor, Path archived) throws IOException {
        var compactifyFileResult = compactifyFile(headerDescriptor, file);
        var updated = compactifyFileResult.nodes;


        Result<FileCompactifyResponse, FileEventSourceActions.FileEventError> result =
                HeaderOperationTypes.writeHeader(new FileHeader.HeaderDescriptor(updated, headerDescriptor.headerDescriptorData()), fileProperties)
                        .flatMapResult(header -> HeaderOperationTypes.flushHeader(file, header))
                        .flatMapResult(f -> Result.ok(new FileCompactifyResponse(file)));

        if (result.isOk() && !archived.toFile().delete()) {
            return Result.all(
                    result,
                    Result.err(new FileEventSourceActions.FileEventError("File successfully written but could not delete archive file."))
            );
        }

        return result;
    }

    record CompactifyFileResult(List<DataNode> nodes, long dataIndex) {}

    private CompactifyFileResult compactifyFile(FileHeader.HeaderDescriptor headerDescriptor, File file) throws IOException {
        try(RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw")) {
            HeaderOperationTypes.writeHeader(headerDescriptor, fileProperties);

            List<DataNode> toSort = new ArrayList<>(headerDescriptor.inIndices());
            toSort.sort(Comparator.comparing(DataNode::indexStart));

            List<DataNode> updated = new ArrayList<>();

            long dataIndex = fileProperties.getDataStreamFileHeaderLengthBytes();

            for (DataNode d : toSort) {
                Assert.isInstanceOf(DataNode.AddNode.class, d, "Skip nodes do not exist in this algorithm!");
                randomAccessFile.seek(d.dataStart());
                byte[] toRead = new byte[Math.toIntExact(d.length())];
                randomAccessFile.read(toRead);
                randomAccessFile.seek(dataIndex);
                randomAccessFile.write(toRead);
                dataIndex += toRead.length;
                updated.add(new DataNode.AddNode(d.indexStart(), d.indexEnd(), dataIndex - toRead.length, dataIndex, true));
            }

            randomAccessFile.setLength(dataIndex);

            return new CompactifyFileResult(updated, dataIndex);
        }
    }

}
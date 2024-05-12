package com.hayden.fileservice.filesource.directoryoperations;

import com.hayden.fileservice.codegen.types.FileChangeType;
import com.hayden.fileservice.codegen.types.FileMetadata;
import com.hayden.fileservice.codegen.types.FileSearch;
import com.hayden.fileservice.filesource.FileHelpers;
import com.hayden.fileservice.graphql.FileEventSourceActions;
import com.hayden.utilitymodule.result.Result;
import jakarta.validation.constraints.NotNull;
import org.jetbrains.annotations.Nullable;
import org.reactivestreams.Publisher;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.io.File;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Stream;

@Component
public class LocalDirectoryOperations implements DirectoryOperations {
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
}

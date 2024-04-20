package com.hayden.fileservice.filesource;

import com.hayden.fileservice.codegen.types.FileChangeEvent;
import com.hayden.fileservice.codegen.types.FileChangeEventInput;
import com.hayden.fileservice.codegen.types.FileMetadata;
import com.hayden.fileservice.codegen.types.FileSearch;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class LocalFileDataSource implements FileDataSource {
    @Override
    public List<FileMetadata> getMetadata(FileSearch path) {
        return List.of();
    }

    @Override
    public List<FileChangeEvent> getData(FileSearch path) {
        return List.of();
    }

    @Override
    public Optional<FileMetadata> update(FileChangeEventInput input) {
        return Optional.empty();
    }
}

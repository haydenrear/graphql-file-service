package com.hayden.fileservice.filesource;

import com.hayden.fileservice.codegen.types.FileChangeEvent;
import com.hayden.fileservice.codegen.types.FileChangeEventInput;
import com.hayden.fileservice.codegen.types.FileMetadata;
import com.hayden.fileservice.codegen.types.FileSearch;

import java.util.List;
import java.util.Optional;

public interface FileDataSource {

    List<FileMetadata> getMetadata(FileSearch path);

    List<FileChangeEvent> getData(FileSearch path);

    Optional<FileMetadata> update(FileChangeEventInput input);

}

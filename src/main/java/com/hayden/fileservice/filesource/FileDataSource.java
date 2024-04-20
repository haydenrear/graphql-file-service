package com.hayden.fileservice.filesource;

import com.hayden.fileservice.codegen.types.FileChangeEvent;
import com.hayden.fileservice.codegen.types.FileChangeEventInput;
import com.hayden.fileservice.codegen.types.FileMetadata;
import com.hayden.fileservice.codegen.types.FileSearch;
import com.hayden.fileservice.graphql.FileEventSourceActions;
import com.hayden.utilitymodule.result.Result;
import org.reactivestreams.Publisher;

import java.util.List;

public interface FileDataSource {

    List<Result<FileMetadata, FileEventSourceActions.FileEventError>> getMetadata(FileSearch path);

    Publisher<Result<FileChangeEvent, FileEventSourceActions.FileEventError>> getData(FileSearch path);

    Result<FileMetadata, FileEventSourceActions.FileEventError> update(FileChangeEventInput input);

}

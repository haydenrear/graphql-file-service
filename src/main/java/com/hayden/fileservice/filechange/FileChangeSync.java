package com.hayden.fileservice.filechange;

import com.hayden.fileservice.codegen.types.FileChangeEvent;
import lombok.experimental.Delegate;

/**
 * For efficiency, all changes to files will be pushed to Kafka so that nodes can keep history without reading from the files themselves.
 */
public sealed interface FileChangeSync permits FileChangeSync.AddContent, FileChangeSync.RemoveContent {

    record AddContent(@Delegate FileChangeEvent fileChangeEvent) implements FileChangeSync {}
    record RemoveContent(@Delegate FileChangeEvent fileChangeEvent) implements FileChangeSync {}
    record AddFile(@Delegate FileChangeEvent fileChangeEvent) {}
    record DeleteFile(@Delegate FileChangeEvent fileChangeEvent) {}

}

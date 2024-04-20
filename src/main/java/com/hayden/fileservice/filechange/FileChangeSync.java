package com.hayden.fileservice.filechange;

public sealed interface FileChangeSync permits FileChangeSync.AddContent, FileChangeSync.RemoveContent {

    record AddContent(long offset, long length) implements FileChangeSync {}
    record RemoveContent(long offset, long length) implements FileChangeSync {}
    record AddFile(String path) {}
    record DeleteFile(String path) {}

}

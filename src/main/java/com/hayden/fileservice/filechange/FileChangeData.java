package com.hayden.fileservice.filechange;

import java.nio.file.Path;

public interface FileChangeData {

    record GetFileData(byte[] fileData, long offset) implements FileChangeData {}
    record FileAddContent(byte[] toAdd, long offset) implements FileChangeData {}
    record FileRemoveContent(long offset, long length) implements FileChangeData {}
    record AddFile(byte[] fileData, long offset, String filePath) implements FileChangeData {}
    record RemoveFile(String filePath) implements FileChangeData {}

}

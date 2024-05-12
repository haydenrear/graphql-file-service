package com.hayden.fileservice.codegen.types;

import java.util.List;

public class FilePathMetadata {
  private String id;

  private List<FileMetadata> enumerateFiles;

  private FileMetadata getFileCurrentState;

  public FilePathMetadata() {
  }

  public FilePathMetadata(String id, List<FileMetadata> enumerateFiles,
      FileMetadata getFileCurrentState) {
    this.id = id;
    this.enumerateFiles = enumerateFiles;
    this.getFileCurrentState = getFileCurrentState;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public List<FileMetadata> getEnumerateFiles() {
    return enumerateFiles;
  }

  public void setEnumerateFiles(List<FileMetadata> enumerateFiles) {
    this.enumerateFiles = enumerateFiles;
  }

  public FileMetadata getGetFileCurrentState() {
    return getFileCurrentState;
  }

  public void setGetFileCurrentState(FileMetadata getFileCurrentState) {
    this.getFileCurrentState = getFileCurrentState;
  }

  @Override
  public String toString() {
    return "FilePathMetadata{" + "id='" + id + "'," +"enumerateFiles='" + enumerateFiles + "'," +"getFileCurrentState='" + getFileCurrentState + "'" +"}";
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FilePathMetadata that = (FilePathMetadata) o;
        return java.util.Objects.equals(id, that.id) &&
                            java.util.Objects.equals(enumerateFiles, that.enumerateFiles) &&
                            java.util.Objects.equals(getFileCurrentState, that.getFileCurrentState);
  }

  @Override
  public int hashCode() {
    return java.util.Objects.hash(id, enumerateFiles, getFileCurrentState);
  }

  public static FilePathMetadata.Builder newBuilder() {
    return new Builder();
  }

  public static class Builder {
    private String id;

    private List<FileMetadata> enumerateFiles;

    private FileMetadata getFileCurrentState;

    public FilePathMetadata build() {
                  FilePathMetadata result = new FilePathMetadata();
                      result.id = this.id;
          result.enumerateFiles = this.enumerateFiles;
          result.getFileCurrentState = this.getFileCurrentState;
                      return result;
    }

    public FilePathMetadata.Builder id(String id) {
      this.id = id;
      return this;
    }

    public FilePathMetadata.Builder enumerateFiles(
        List<FileMetadata> enumerateFiles) {
      this.enumerateFiles = enumerateFiles;
      return this;
    }

    public FilePathMetadata.Builder getFileCurrentState(
        FileMetadata getFileCurrentState) {
      this.getFileCurrentState = getFileCurrentState;
      return this;
    }
  }
}

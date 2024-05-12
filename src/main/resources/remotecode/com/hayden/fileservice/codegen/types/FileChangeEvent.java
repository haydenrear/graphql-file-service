package com.hayden.fileservice.codegen.types;

import com.hayden.fileservice.config.ByteArray;

public class FileChangeEvent {
  private String fileId;

  private FileChangeType changeType;

  private Integer offset;

  private ByteArray data;

  private String path;

  public FileChangeEvent() {
  }

  public FileChangeEvent(String fileId, FileChangeType changeType, Integer offset, ByteArray data,
      String path) {
    this.fileId = fileId;
    this.changeType = changeType;
    this.offset = offset;
    this.data = data;
    this.path = path;
  }

  public String getFileId() {
    return fileId;
  }

  public void setFileId(String fileId) {
    this.fileId = fileId;
  }

  public FileChangeType getChangeType() {
    return changeType;
  }

  public void setChangeType(FileChangeType changeType) {
    this.changeType = changeType;
  }

  public Integer getOffset() {
    return offset;
  }

  public void setOffset(Integer offset) {
    this.offset = offset;
  }

  public ByteArray getData() {
    return data;
  }

  public void setData(ByteArray data) {
    this.data = data;
  }

  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }

  @Override
  public String toString() {
    return "FileChangeEvent{" + "fileId='" + fileId + "'," +"changeType='" + changeType + "'," +"offset='" + offset + "'," +"data='" + data + "'," +"path='" + path + "'" +"}";
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FileChangeEvent that = (FileChangeEvent) o;
        return java.util.Objects.equals(fileId, that.fileId) &&
                            java.util.Objects.equals(changeType, that.changeType) &&
                            java.util.Objects.equals(offset, that.offset) &&
                            java.util.Objects.equals(data, that.data) &&
                            java.util.Objects.equals(path, that.path);
  }

  @Override
  public int hashCode() {
    return java.util.Objects.hash(fileId, changeType, offset, data, path);
  }

  public static FileChangeEvent.Builder newBuilder() {
    return new Builder();
  }

  public static class Builder {
    private String fileId;

    private FileChangeType changeType;

    private Integer offset;

    private ByteArray data;

    private String path;

    public FileChangeEvent build() {
                  FileChangeEvent result = new FileChangeEvent();
                      result.fileId = this.fileId;
          result.changeType = this.changeType;
          result.offset = this.offset;
          result.data = this.data;
          result.path = this.path;
                      return result;
    }

    public FileChangeEvent.Builder fileId(String fileId) {
      this.fileId = fileId;
      return this;
    }

    public FileChangeEvent.Builder changeType(
        FileChangeType changeType) {
      this.changeType = changeType;
      return this;
    }

    public FileChangeEvent.Builder offset(Integer offset) {
      this.offset = offset;
      return this;
    }

    public FileChangeEvent.Builder data(ByteArray data) {
      this.data = data;
      return this;
    }

    public FileChangeEvent.Builder path(String path) {
      this.path = path;
      return this;
    }
  }
}

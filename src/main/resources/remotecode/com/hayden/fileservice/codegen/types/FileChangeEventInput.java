package com.hayden.fileservice.codegen.types;

import com.hayden.fileservice.config.ByteArray;

public class FileChangeEventInput {
  private String fileId;

  private FileChangeType changeType;

  private int offset;

  private ByteArray data;

  private String path;

  private Integer length;

  public FileChangeEventInput() {
  }

  public FileChangeEventInput(String fileId, FileChangeType changeType, int offset, ByteArray data,
      String path, Integer length) {
    this.fileId = fileId;
    this.changeType = changeType;
    this.offset = offset;
    this.data = data;
    this.path = path;
    this.length = length;
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

  public int getOffset() {
    return offset;
  }

  public void setOffset(int offset) {
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

  public Integer getLength() {
    return length;
  }

  public void setLength(Integer length) {
    this.length = length;
  }

  @Override
  public String toString() {
    return "FileChangeEventInput{" + "fileId='" + fileId + "'," +"changeType='" + changeType + "'," +"offset='" + offset + "'," +"data='" + data + "'," +"path='" + path + "'," +"length='" + length + "'" +"}";
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FileChangeEventInput that = (FileChangeEventInput) o;
        return java.util.Objects.equals(fileId, that.fileId) &&
                            java.util.Objects.equals(changeType, that.changeType) &&
                            offset == that.offset &&
                            java.util.Objects.equals(data, that.data) &&
                            java.util.Objects.equals(path, that.path) &&
                            java.util.Objects.equals(length, that.length);
  }

  @Override
  public int hashCode() {
    return java.util.Objects.hash(fileId, changeType, offset, data, path, length);
  }

  public static FileChangeEventInput.Builder newBuilder() {
    return new Builder();
  }

  public static class Builder {
    private String fileId;

    private FileChangeType changeType;

    private int offset;

    private ByteArray data;

    private String path;

    private Integer length;

    public FileChangeEventInput build() {
                  FileChangeEventInput result = new FileChangeEventInput();
                      result.fileId = this.fileId;
          result.changeType = this.changeType;
          result.offset = this.offset;
          result.data = this.data;
          result.path = this.path;
          result.length = this.length;
                      return result;
    }

    public FileChangeEventInput.Builder fileId(String fileId) {
      this.fileId = fileId;
      return this;
    }

    public FileChangeEventInput.Builder changeType(
        FileChangeType changeType) {
      this.changeType = changeType;
      return this;
    }

    public FileChangeEventInput.Builder offset(int offset) {
      this.offset = offset;
      return this;
    }

    public FileChangeEventInput.Builder data(ByteArray data) {
      this.data = data;
      return this;
    }

    public FileChangeEventInput.Builder path(String path) {
      this.path = path;
      return this;
    }

    public FileChangeEventInput.Builder length(
        Integer length) {
      this.length = length;
      return this;
    }
  }
}

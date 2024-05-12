package com.hayden.fileservice.codegen.types;

import java.time.LocalDate;

public class FileMetadata {
  private String id;

  private String name;

  private String path;

  private int size;

  private LocalDate lastModified;

  private FileChangeType changeType;

  public FileMetadata() {
  }

  public FileMetadata(String id, String name, String path, int size, LocalDate lastModified,
      FileChangeType changeType) {
    this.id = id;
    this.name = name;
    this.path = path;
    this.size = size;
    this.lastModified = lastModified;
    this.changeType = changeType;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }

  public int getSize() {
    return size;
  }

  public void setSize(int size) {
    this.size = size;
  }

  public LocalDate getLastModified() {
    return lastModified;
  }

  public void setLastModified(LocalDate lastModified) {
    this.lastModified = lastModified;
  }

  public FileChangeType getChangeType() {
    return changeType;
  }

  public void setChangeType(FileChangeType changeType) {
    this.changeType = changeType;
  }

  @Override
  public String toString() {
    return "FileMetadata{" + "id='" + id + "'," +"name='" + name + "'," +"path='" + path + "'," +"size='" + size + "'," +"lastModified='" + lastModified + "'," +"changeType='" + changeType + "'" +"}";
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FileMetadata that = (FileMetadata) o;
        return java.util.Objects.equals(id, that.id) &&
                            java.util.Objects.equals(name, that.name) &&
                            java.util.Objects.equals(path, that.path) &&
                            size == that.size &&
                            java.util.Objects.equals(lastModified, that.lastModified) &&
                            java.util.Objects.equals(changeType, that.changeType);
  }

  @Override
  public int hashCode() {
    return java.util.Objects.hash(id, name, path, size, lastModified, changeType);
  }

  public static FileMetadata.Builder newBuilder() {
    return new Builder();
  }

  public static class Builder {
    private String id;

    private String name;

    private String path;

    private int size;

    private LocalDate lastModified;

    private FileChangeType changeType;

    public FileMetadata build() {
                  FileMetadata result = new FileMetadata();
                      result.id = this.id;
          result.name = this.name;
          result.path = this.path;
          result.size = this.size;
          result.lastModified = this.lastModified;
          result.changeType = this.changeType;
                      return result;
    }

    public FileMetadata.Builder id(String id) {
      this.id = id;
      return this;
    }

    public FileMetadata.Builder name(String name) {
      this.name = name;
      return this;
    }

    public FileMetadata.Builder path(String path) {
      this.path = path;
      return this;
    }

    public FileMetadata.Builder size(int size) {
      this.size = size;
      return this;
    }

    public FileMetadata.Builder lastModified(
        LocalDate lastModified) {
      this.lastModified = lastModified;
      return this;
    }

    public FileMetadata.Builder changeType(
        FileChangeType changeType) {
      this.changeType = changeType;
      return this;
    }
  }
}

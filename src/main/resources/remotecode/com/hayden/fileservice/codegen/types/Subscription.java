package com.hayden.fileservice.codegen.types;

public class Subscription {
  private FileMetadata fileMetadata;

  private FileChangeEvent files;

  public Subscription() {
  }

  public Subscription(FileMetadata fileMetadata, FileChangeEvent files) {
    this.fileMetadata = fileMetadata;
    this.files = files;
  }

  public FileMetadata getFileMetadata() {
    return fileMetadata;
  }

  public void setFileMetadata(FileMetadata fileMetadata) {
    this.fileMetadata = fileMetadata;
  }

  public FileChangeEvent getFiles() {
    return files;
  }

  public void setFiles(FileChangeEvent files) {
    this.files = files;
  }

  @Override
  public String toString() {
    return "Subscription{" + "fileMetadata='" + fileMetadata + "'," +"files='" + files + "'" +"}";
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Subscription that = (Subscription) o;
        return java.util.Objects.equals(fileMetadata, that.fileMetadata) &&
                            java.util.Objects.equals(files, that.files);
  }

  @Override
  public int hashCode() {
    return java.util.Objects.hash(fileMetadata, files);
  }

  public static Subscription.Builder newBuilder() {
    return new Builder();
  }

  public static class Builder {
    private FileMetadata fileMetadata;

    private FileChangeEvent files;

    public Subscription build() {
                  Subscription result = new Subscription();
                      result.fileMetadata = this.fileMetadata;
          result.files = this.files;
                      return result;
    }

    public Subscription.Builder fileMetadata(
        FileMetadata fileMetadata) {
      this.fileMetadata = fileMetadata;
      return this;
    }

    public Subscription.Builder files(FileChangeEvent files) {
      this.files = files;
      return this;
    }
  }
}

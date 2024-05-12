package com.hayden.fileservice.codegen.types;

public class FileSearch {
  private String fileId;

  private String path;

  private String fileName;

  private MimeType fileType;

  public FileSearch() {
  }

  public FileSearch(String fileId, String path, String fileName, MimeType fileType) {
    this.fileId = fileId;
    this.path = path;
    this.fileName = fileName;
    this.fileType = fileType;
  }

  public String getFileId() {
    return fileId;
  }

  public void setFileId(String fileId) {
    this.fileId = fileId;
  }

  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }

  public String getFileName() {
    return fileName;
  }

  public void setFileName(String fileName) {
    this.fileName = fileName;
  }

  public MimeType getFileType() {
    return fileType;
  }

  public void setFileType(MimeType fileType) {
    this.fileType = fileType;
  }

  @Override
  public String toString() {
    return "FileSearch{" + "fileId='" + fileId + "'," +"path='" + path + "'," +"fileName='" + fileName + "'," +"fileType='" + fileType + "'" +"}";
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FileSearch that = (FileSearch) o;
        return java.util.Objects.equals(fileId, that.fileId) &&
                            java.util.Objects.equals(path, that.path) &&
                            java.util.Objects.equals(fileName, that.fileName) &&
                            java.util.Objects.equals(fileType, that.fileType);
  }

  @Override
  public int hashCode() {
    return java.util.Objects.hash(fileId, path, fileName, fileType);
  }

  public static FileSearch.Builder newBuilder() {
    return new Builder();
  }

  public static class Builder {
    private String fileId;

    private String path;

    private String fileName;

    private MimeType fileType;

    public FileSearch build() {
                  FileSearch result = new FileSearch();
                      result.fileId = this.fileId;
          result.path = this.path;
          result.fileName = this.fileName;
          result.fileType = this.fileType;
                      return result;
    }

    public FileSearch.Builder fileId(String fileId) {
      this.fileId = fileId;
      return this;
    }

    public FileSearch.Builder path(String path) {
      this.path = path;
      return this;
    }

    public FileSearch.Builder fileName(String fileName) {
      this.fileName = fileName;
      return this;
    }

    public FileSearch.Builder fileType(MimeType fileType) {
      this.fileType = fileType;
      return this;
    }
  }
}

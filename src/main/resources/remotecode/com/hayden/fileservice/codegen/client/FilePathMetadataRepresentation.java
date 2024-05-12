package com.hayden.fileservice.codegen.client;

public class FilePathMetadataRepresentation {
  private String id;

  private String __typename = "FilePathMetadata";

  public FilePathMetadataRepresentation() {
  }

  public FilePathMetadataRepresentation(String id, String __typename) {
    this.id = id;
    this.__typename = __typename;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String get__typename() {
    return __typename;
  }

  public void set__typename(String __typename) {
    this.__typename = __typename;
  }

  @Override
  public String toString() {
    return "FilePathMetadataRepresentation{" + "id='" + id + "'," +"__typename='" + __typename + "'" +"}";
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FilePathMetadataRepresentation that = (FilePathMetadataRepresentation) o;
        return java.util.Objects.equals(id, that.id) &&
                            java.util.Objects.equals(__typename, that.__typename);
  }

  @Override
  public int hashCode() {
    return java.util.Objects.hash(id, __typename);
  }

  public static FilePathMetadataRepresentation.Builder newBuilder(
      ) {
    return new Builder();
  }

  public static class Builder {
    private String id;

    private String __typename = "FilePathMetadata";

    public FilePathMetadataRepresentation build() {
                  FilePathMetadataRepresentation result = new FilePathMetadataRepresentation();
                      result.id = this.id;
          result.__typename = this.__typename;
                      return result;
    }

    public FilePathMetadataRepresentation.Builder id(
        String id) {
      this.id = id;
      return this;
    }

    public FilePathMetadataRepresentation.Builder __typename(
        String __typename) {
      this.__typename = __typename;
      return this;
    }
  }
}

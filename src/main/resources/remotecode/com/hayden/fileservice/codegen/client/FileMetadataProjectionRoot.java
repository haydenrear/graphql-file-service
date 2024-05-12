package com.hayden.fileservice.codegen.client;

import com.netflix.graphql.dgs.client.codegen.BaseSubProjectionNode;

public class FileMetadataProjectionRoot<PARENT extends BaseSubProjectionNode<?, ?>, ROOT extends BaseSubProjectionNode<?, ?>> extends BaseSubProjectionNode<PARENT, ROOT> {
  public FileMetadataProjectionRoot() {
    super(null, null, java.util.Optional.of("FileMetadata"));
  }

  public FileChangeTypeProjection<FileMetadataProjectionRoot<PARENT, ROOT>, FileMetadataProjectionRoot<PARENT, ROOT>> changeType(
      ) {
    FileChangeTypeProjection<FileMetadataProjectionRoot<PARENT, ROOT>, FileMetadataProjectionRoot<PARENT, ROOT>> projection = new FileChangeTypeProjection<>(this, this);    
    getFields().put("changeType", projection);
    return projection;
  }

  public FileMetadataProjectionRoot<PARENT, ROOT> id() {
    getFields().put("id", null);
    return this;
  }

  public FileMetadataProjectionRoot<PARENT, ROOT> name() {
    getFields().put("name", null);
    return this;
  }

  public FileMetadataProjectionRoot<PARENT, ROOT> path() {
    getFields().put("path", null);
    return this;
  }

  public FileMetadataProjectionRoot<PARENT, ROOT> size() {
    getFields().put("size", null);
    return this;
  }

  public FileMetadataProjectionRoot<PARENT, ROOT> lastModified() {
    getFields().put("lastModified", null);
    return this;
  }
}

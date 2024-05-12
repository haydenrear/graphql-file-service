package com.hayden.fileservice.codegen.client;

import com.netflix.graphql.dgs.client.codegen.BaseSubProjectionNode;

public class FileMetadataProjection<PARENT extends BaseSubProjectionNode<?, ?>, ROOT extends BaseSubProjectionNode<?, ?>> extends BaseSubProjectionNode<PARENT, ROOT> {
  public FileMetadataProjection(PARENT parent, ROOT root) {
    super(parent, root, java.util.Optional.of("FileMetadata"));
  }

  public FileChangeTypeProjection<FileMetadataProjection<PARENT, ROOT>, ROOT> changeType() {
     FileChangeTypeProjection<FileMetadataProjection<PARENT, ROOT>, ROOT> projection = new FileChangeTypeProjection<>(this, getRoot());
     getFields().put("changeType", projection);
     return projection;
  }

  public FileMetadataProjection<PARENT, ROOT> id() {
    getFields().put("id", null);
    return this;
  }

  public FileMetadataProjection<PARENT, ROOT> name() {
    getFields().put("name", null);
    return this;
  }

  public FileMetadataProjection<PARENT, ROOT> path() {
    getFields().put("path", null);
    return this;
  }

  public FileMetadataProjection<PARENT, ROOT> size() {
    getFields().put("size", null);
    return this;
  }

  public FileMetadataProjection<PARENT, ROOT> lastModified() {
    getFields().put("lastModified", null);
    return this;
  }
}

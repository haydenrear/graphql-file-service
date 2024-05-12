package com.hayden.fileservice.codegen.client;

import com.netflix.graphql.dgs.client.codegen.BaseSubProjectionNode;

public class FilesProjectionRoot<PARENT extends BaseSubProjectionNode<?, ?>, ROOT extends BaseSubProjectionNode<?, ?>> extends BaseSubProjectionNode<PARENT, ROOT> {
  public FilesProjectionRoot() {
    super(null, null, java.util.Optional.of("FileChangeEvent"));
  }

  public FileChangeTypeProjection<FilesProjectionRoot<PARENT, ROOT>, FilesProjectionRoot<PARENT, ROOT>> changeType(
      ) {
    FileChangeTypeProjection<FilesProjectionRoot<PARENT, ROOT>, FilesProjectionRoot<PARENT, ROOT>> projection = new FileChangeTypeProjection<>(this, this);    
    getFields().put("changeType", projection);
    return projection;
  }

  public FilesProjectionRoot<PARENT, ROOT> fileId() {
    getFields().put("fileId", null);
    return this;
  }

  public FilesProjectionRoot<PARENT, ROOT> offset() {
    getFields().put("offset", null);
    return this;
  }

  public FilesProjectionRoot<PARENT, ROOT> data() {
    getFields().put("data", null);
    return this;
  }

  public FilesProjectionRoot<PARENT, ROOT> path() {
    getFields().put("path", null);
    return this;
  }
}

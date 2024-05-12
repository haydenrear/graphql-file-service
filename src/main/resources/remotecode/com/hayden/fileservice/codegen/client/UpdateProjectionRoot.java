package com.hayden.fileservice.codegen.client;

import com.netflix.graphql.dgs.client.codegen.BaseSubProjectionNode;

public class UpdateProjectionRoot<PARENT extends BaseSubProjectionNode<?, ?>, ROOT extends BaseSubProjectionNode<?, ?>> extends BaseSubProjectionNode<PARENT, ROOT> {
  public UpdateProjectionRoot() {
    super(null, null, java.util.Optional.of("FileMetadata"));
  }

  public FileChangeTypeProjection<UpdateProjectionRoot<PARENT, ROOT>, UpdateProjectionRoot<PARENT, ROOT>> changeType(
      ) {
    FileChangeTypeProjection<UpdateProjectionRoot<PARENT, ROOT>, UpdateProjectionRoot<PARENT, ROOT>> projection = new FileChangeTypeProjection<>(this, this);    
    getFields().put("changeType", projection);
    return projection;
  }

  public UpdateProjectionRoot<PARENT, ROOT> id() {
    getFields().put("id", null);
    return this;
  }

  public UpdateProjectionRoot<PARENT, ROOT> name() {
    getFields().put("name", null);
    return this;
  }

  public UpdateProjectionRoot<PARENT, ROOT> path() {
    getFields().put("path", null);
    return this;
  }

  public UpdateProjectionRoot<PARENT, ROOT> size() {
    getFields().put("size", null);
    return this;
  }

  public UpdateProjectionRoot<PARENT, ROOT> lastModified() {
    getFields().put("lastModified", null);
    return this;
  }
}

package com.hayden.fileservice.codegen.client;

import com.netflix.graphql.dgs.client.codegen.BaseSubProjectionNode;

public class EntitiesFileMetadataKeyProjection<PARENT extends BaseSubProjectionNode<?, ?>, ROOT extends BaseSubProjectionNode<?, ?>> extends BaseSubProjectionNode<PARENT, ROOT> {
  public EntitiesFileMetadataKeyProjection(PARENT parent, ROOT root) {
    super(parent, root, java.util.Optional.of("FileMetadata"));
  }

  public FileChangeTypeProjection<EntitiesFileMetadataKeyProjection<PARENT, ROOT>, ROOT> changeType(
      ) {
     FileChangeTypeProjection<EntitiesFileMetadataKeyProjection<PARENT, ROOT>, ROOT> projection = new FileChangeTypeProjection<>(this, getRoot());
     getFields().put("changeType", projection);
     return projection;
  }

  public EntitiesFileMetadataKeyProjection<PARENT, ROOT> id() {
    getFields().put("id", null);
    return this;
  }

  public EntitiesFileMetadataKeyProjection<PARENT, ROOT> name() {
    getFields().put("name", null);
    return this;
  }

  public EntitiesFileMetadataKeyProjection<PARENT, ROOT> path() {
    getFields().put("path", null);
    return this;
  }

  public EntitiesFileMetadataKeyProjection<PARENT, ROOT> size() {
    getFields().put("size", null);
    return this;
  }

  public EntitiesFileMetadataKeyProjection<PARENT, ROOT> lastModified() {
    getFields().put("lastModified", null);
    return this;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("... on FileMetadata {");
    getFields().forEach((k, v) -> {
        builder.append(" ").append(k);
        if(v != null) {
            builder.append(" ").append(v.toString());
        }
    });
    builder.append("}");
     
    return builder.toString();
  }
}

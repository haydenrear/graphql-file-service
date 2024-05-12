package com.hayden.fileservice.codegen.client;

import com.netflix.graphql.dgs.client.codegen.BaseSubProjectionNode;

import java.util.ArrayList;

public class EntitiesFilePathMetadataKeyProjection<PARENT extends BaseSubProjectionNode<?, ?>, ROOT extends BaseSubProjectionNode<?, ?>> extends BaseSubProjectionNode<PARENT, ROOT> {
  public EntitiesFilePathMetadataKeyProjection(PARENT parent, ROOT root) {
    super(parent, root, java.util.Optional.of("FilePathMetadata"));
  }

  public FileMetadataProjection<EntitiesFilePathMetadataKeyProjection<PARENT, ROOT>, ROOT> enumerateFiles(
      ) {
     FileMetadataProjection<EntitiesFilePathMetadataKeyProjection<PARENT, ROOT>, ROOT> projection = new FileMetadataProjection<>(this, getRoot());
     getFields().put("enumerateFiles", projection);
     return projection;
  }

  public FileMetadataProjection<EntitiesFilePathMetadataKeyProjection<PARENT, ROOT>, ROOT> getFileCurrentState(
      ) {
     FileMetadataProjection<EntitiesFilePathMetadataKeyProjection<PARENT, ROOT>, ROOT> projection = new FileMetadataProjection<>(this, getRoot());
     getFields().put("getFileCurrentState", projection);
     return projection;
  }

  public FileMetadataProjection<EntitiesFilePathMetadataKeyProjection<PARENT, ROOT>, ROOT> getFileCurrentState(
      String id) {
    FileMetadataProjection<EntitiesFilePathMetadataKeyProjection<PARENT, ROOT>, ROOT> projection = new FileMetadataProjection<>(this, getRoot());    
    getFields().put("getFileCurrentState", projection);
    getInputArguments().computeIfAbsent("getFileCurrentState", k -> new ArrayList<>());                      
    InputArgument idArg = new InputArgument("id", id);
    getInputArguments().get("getFileCurrentState").add(idArg);
    return projection;
  }

  public EntitiesFilePathMetadataKeyProjection<PARENT, ROOT> id() {
    getFields().put("id", null);
    return this;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("... on FilePathMetadata {");
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

package com.hayden.fileservice.codegen.client;

import com.netflix.graphql.dgs.client.codegen.BaseSubProjectionNode;

public class FileChangeTypeProjection<PARENT extends BaseSubProjectionNode<?, ?>, ROOT extends BaseSubProjectionNode<?, ?>> extends BaseSubProjectionNode<PARENT, ROOT> {
  public FileChangeTypeProjection(PARENT parent, ROOT root) {
    super(parent, root, java.util.Optional.of("FileChangeType"));
  }
}

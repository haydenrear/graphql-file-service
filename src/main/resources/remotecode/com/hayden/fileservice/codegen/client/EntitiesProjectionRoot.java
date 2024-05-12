package com.hayden.fileservice.codegen.client;

import com.netflix.graphql.dgs.client.codegen.BaseSubProjectionNode;

public class EntitiesProjectionRoot<PARENT extends BaseSubProjectionNode<?, ?>, ROOT extends BaseSubProjectionNode<?, ?>> extends BaseSubProjectionNode<PARENT, ROOT> {
  public EntitiesProjectionRoot() {
    super(null, null, java.util.Optional.of("_entities"));
  }

  public EntitiesFilePathMetadataKeyProjection<EntitiesProjectionRoot<PARENT, ROOT>, EntitiesProjectionRoot<PARENT, ROOT>> onFilePathMetadata(
      ) {
     EntitiesFilePathMetadataKeyProjection<EntitiesProjectionRoot<PARENT, ROOT>, EntitiesProjectionRoot<PARENT, ROOT>> fragment = new EntitiesFilePathMetadataKeyProjection(this, this);
     getFragments().add(fragment);
     return fragment;
  }

  public EntitiesFileMetadataKeyProjection<EntitiesProjectionRoot<PARENT, ROOT>, EntitiesProjectionRoot<PARENT, ROOT>> onFileMetadata(
      ) {
     EntitiesFileMetadataKeyProjection<EntitiesProjectionRoot<PARENT, ROOT>, EntitiesProjectionRoot<PARENT, ROOT>> fragment = new EntitiesFileMetadataKeyProjection(this, this);
     getFragments().add(fragment);
     return fragment;
  }
}

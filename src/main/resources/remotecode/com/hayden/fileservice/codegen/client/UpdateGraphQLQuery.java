package com.hayden.fileservice.codegen.client;

import com.hayden.fileservice.codegen.types.FileChangeEventInput;
import com.netflix.graphql.dgs.client.codegen.GraphQLQuery;

import java.util.HashSet;
import java.util.Set;

public class UpdateGraphQLQuery extends GraphQLQuery {
  public UpdateGraphQLQuery(FileChangeEventInput fileChangeEvent, String queryName,
      Set<String> fieldsSet) {
    super("mutation", queryName);
    if (fileChangeEvent != null || fieldsSet.contains("fileChangeEvent")) {
        getInput().put("fileChangeEvent", fileChangeEvent);
    }
  }

  public UpdateGraphQLQuery() {
    super("mutation");
  }

  @Override
  public String getOperationName() {
     return "update";
                    
  }

  public static Builder newRequest() {
    return new Builder();
  }

  public static class Builder {
    private Set<String> fieldsSet = new HashSet<>();

    private FileChangeEventInput fileChangeEvent;

    private String queryName;

    public UpdateGraphQLQuery build() {
      return new UpdateGraphQLQuery(fileChangeEvent, queryName, fieldsSet);
               
    }

    public Builder fileChangeEvent(FileChangeEventInput fileChangeEvent) {
      this.fileChangeEvent = fileChangeEvent;
      this.fieldsSet.add("fileChangeEvent");
      return this;
    }

    public Builder queryName(String queryName) {
      this.queryName = queryName;
      return this;
    }
  }
}

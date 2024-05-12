package com.hayden.fileservice.codegen.client;

import com.hayden.fileservice.codegen.types.FileSearch;
import com.netflix.graphql.dgs.client.codegen.GraphQLQuery;

import java.util.HashSet;
import java.util.Set;

public class FileMetadataGraphQLQuery extends GraphQLQuery {
  public FileMetadataGraphQLQuery(FileSearch fileSearch, String queryName, Set<String> fieldsSet) {
    super("subscription", queryName);
    if (fileSearch != null || fieldsSet.contains("fileSearch")) {
        getInput().put("fileSearch", fileSearch);
    }
  }

  public FileMetadataGraphQLQuery() {
    super("subscription");
  }

  @Override
  public String getOperationName() {
     return "fileMetadata";
                    
  }

  public static Builder newRequest() {
    return new Builder();
  }

  public static class Builder {
    private Set<String> fieldsSet = new HashSet<>();

    private FileSearch fileSearch;

    private String queryName;

    public FileMetadataGraphQLQuery build() {
      return new FileMetadataGraphQLQuery(fileSearch, queryName, fieldsSet);
               
    }

    public Builder fileSearch(FileSearch fileSearch) {
      this.fileSearch = fileSearch;
      this.fieldsSet.add("fileSearch");
      return this;
    }

    public Builder queryName(String queryName) {
      this.queryName = queryName;
      return this;
    }
  }
}

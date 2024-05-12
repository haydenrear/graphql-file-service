package com.hayden.fileservice.codegen;

public class DgsConstants {
  public static final String SUBSCRIPTION_TYPE = "Subscription";

  public static class SUBSCRIPTION {
    public static final String TYPE_NAME = "Subscription";

    public static final String FileMetadata = "fileMetadata";

    public static final String Files = "files";

    public static class FILEMETADATA_INPUT_ARGUMENT {
      public static final String FileSearch = "fileSearch";
    }

    public static class FILES_INPUT_ARGUMENT {
      public static final String FileSearch = "fileSearch";
    }
  }

  public static class FILEPATHMETADATA {
    public static final String TYPE_NAME = "FilePathMetadata";

    public static final String Id = "id";

    public static final String EnumerateFiles = "enumerateFiles";

    public static final String GetFileCurrentState = "getFileCurrentState";

    public static class GETFILECURRENTSTATE_INPUT_ARGUMENT {
      public static final String Id = "id";
    }
  }

  public static class FILEMETADATA {
    public static final String TYPE_NAME = "FileMetadata";

    public static final String Id = "id";

    public static final String Name = "name";

    public static final String Path = "path";

    public static final String Size = "size";

    public static final String LastModified = "lastModified";

    public static final String ChangeType = "changeType";
  }

  public static class FILECHANGEEVENT {
    public static final String TYPE_NAME = "FileChangeEvent";

    public static final String FileId = "fileId";

    public static final String ChangeType = "changeType";

    public static final String Offset = "offset";

    public static final String Data = "data";

    public static final String Path = "path";
  }

  public static class MUTATION {
    public static final String TYPE_NAME = "Mutation";

    public static final String Update = "update";

    public static class UPDATE_INPUT_ARGUMENT {
      public static final String FileChangeEvent = "fileChangeEvent";
    }
  }

  public static class MIMETYPE {
    public static final String TYPE_NAME = "MimeType";

    public static final String Type = "type";

    public static final String Value = "value";
  }

  public static class FILESEARCH {
    public static final String TYPE_NAME = "FileSearch";

    public static final String FileId = "fileId";

    public static final String Path = "path";

    public static final String FileName = "fileName";

    public static final String FileType = "fileType";
  }

  public static class FILECHANGEEVENTINPUT {
    public static final String TYPE_NAME = "FileChangeEventInput";

    public static final String FileId = "fileId";

    public static final String ChangeType = "changeType";

    public static final String Offset = "offset";

    public static final String Data = "data";

    public static final String Path = "path";

    public static final String Length = "length";
  }
}

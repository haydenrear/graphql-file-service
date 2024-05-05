package com.hayden.fileservice.filesource.fileoperations;

import com.hayden.fileservice.filesource.fileoperations.skipfileoperations.FileHeader;
import com.hayden.fileservice.graphql.FileEventSourceActions;
import com.hayden.utilitymodule.result.Result;

import java.io.File;

public interface CompactableFileOperations {

    Result<File, FileEventSourceActions.FileEventError> compactify(File file);

    Result<File, FileEventSourceActions.FileEventError> flush(File file, FileHeader.HeaderDescriptor headerDescriptor);


}

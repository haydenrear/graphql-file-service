package com.hayden.fileservice.filesource.fileoperations.skipfileoperations.datanode;

import com.hayden.fileservice.codegen.types.FileChangeEventInput;
import com.hayden.fileservice.codegen.types.FileChangeType;
import com.hayden.fileservice.filesource.fileoperations.skipfileoperations.FileHeader;
import com.hayden.fileservice.graphql.FileEventSourceActions;
import com.hayden.utilitymodule.result.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataNodeOperationsDelegate implements DataNodeOperations {

    private final AddNodeOperations addNodeOperations;
    private final RemoveNodeOperations removeNodeOperations;

    @Override
    public Result<ChangeNodeOperationsResult, FileEventSourceActions.FileEventError> doChangeNode(FileHeader.HeaderDescriptor inIndices, FileChangeEventInput eventInput) {
        if (eventInput.getChangeType() == FileChangeType.ADD_CONTENT) {
            return addNodeOperations.doChangeNode(inIndices, eventInput);
        } else if (eventInput.getChangeType() == FileChangeType.REMOVE_CONTENT) {
            return removeNodeOperations.doChangeNode(inIndices, eventInput);
        }

        return Result.err(new FileEventSourceActions.FileEventError("Could not find file operations for %s.".formatted(eventInput.getChangeType()))) ;
    }
}

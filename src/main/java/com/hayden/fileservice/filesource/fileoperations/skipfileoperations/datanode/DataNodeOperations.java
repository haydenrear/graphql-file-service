package com.hayden.fileservice.filesource.fileoperations.skipfileoperations.datanode;

import com.hayden.fileservice.codegen.types.FileChangeEventInput;
import com.hayden.fileservice.filesource.fileoperations.skipfileoperations.FileHeader;
import com.hayden.fileservice.graphql.FileEventSourceActions;
import com.hayden.utilitymodule.result.Result;

public interface DataNodeOperations {

    record ChangeNodeOperationsResult(DataNode nodeAdded, FileHeader.HeaderDescriptor headerDescriptor) {}

    static boolean isSecondNodeFullyBeforeFirstNode(DataNode first, DataNode second) {
        return second.indexEnd() < first.indexEnd() && second.indexStart() < first.indexStart()
                && second.indexStart() < first.indexEnd() && second.indexEnd() < first.indexStart();
    }

    static boolean isSecondNodeFullyAfterFirstNode(DataNode first, DataNode second) {
        return isSecondNodeFullyAfterFirstNode(first, second, 0);
    }

    static boolean isFirstOverlappingAllSecond(DataNode first, DataNode second) {
        return second.indexEnd() < first.indexEnd() && second.indexStart() > first.indexStart();
    }

    static boolean isFirstEmptySecondNotEmpty(DataNode first, DataNode second) {
        return first.length() == 0 && second.length() != 0;
    }

    static boolean isSecondNodeFullyAfterFirstNode(DataNode first, DataNode second, int offset) {
        return second.indexEnd() > first.indexEnd() && second.indexStart() > first.indexStart()
                && second.indexStart() > first.indexEnd() && second.indexEnd() > first.indexStart();
    }

    static boolean isFirstNodeFullyWithinSecondNode(DataNode first, DataNode second) {
        return second.indexEnd() > first.indexEnd() && second.indexStart() < first.indexStart();
    }

    static boolean isFirstOverlappingRightSecond(DataNode first, DataNode second) {
        return second.indexEnd() < first.indexEnd() && second.indexStart() < first.indexStart();
    }

    static boolean isFirstOverlappingLeftSecond(DataNode first, DataNode second) {
        return second.indexEnd() > first.indexEnd() && second.indexStart() > first.indexStart();
    }

    static boolean isFirstAllFlushSecond(DataNode first, DataNode second) {
        return second.indexEnd() == first.indexEnd() && second.indexStart() == first.indexStart();
    }

    static boolean isFirstRightFlushOverlappingLeftSecond(DataNode first, DataNode second) {
        return second.indexEnd() == first.indexEnd() && second.indexStart() > first.indexStart();
    }

    static boolean isFirstLeftFlushOverlappingRightSecond(DataNode first, DataNode second) {
        return second.indexEnd() < first.indexEnd() && second.indexStart() == first.indexStart();
    }

    static boolean isFirstRightFlushSecond(DataNode first, DataNode second) {
        return second.indexEnd() == first.indexEnd() && second.indexStart() < first.indexStart();
    }

    static boolean isFirstLeftFlushSecond(DataNode first, DataNode second) {
        return second.indexEnd() > first.indexEnd() && second.indexStart() == first.indexStart();
    }

    static boolean isFirstRightAlterFlushSecond(DataNode first, DataNode second) {
        return first.indexEnd() == second.indexStart() && second.indexStart() > first.indexStart();
    }

    static boolean isFirstLeftAlterFlushSecond(DataNode first, DataNode second) {
        return first.indexStart() == second.indexEnd() && first.indexStart() > second.indexStart();
    }

    Result<ChangeNodeOperationsResult, FileEventSourceActions.FileEventError> doChangeNode(FileHeader.HeaderDescriptor inIndices,
                                                                                            FileChangeEventInput eventInput);

}

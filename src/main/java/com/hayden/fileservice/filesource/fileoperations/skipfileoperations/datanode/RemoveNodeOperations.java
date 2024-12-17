package com.hayden.fileservice.filesource.fileoperations.skipfileoperations.datanode;

import com.google.common.collect.Lists;
import com.hayden.fileservice.codegen.types.FileChangeEventInput;
import com.hayden.fileservice.codegen.types.FileChangeType;
import com.hayden.fileservice.filesource.fileoperations.skipfileoperations.FileHeader;
import com.hayden.fileservice.graphql.FileEventSourceActions;
import com.hayden.utilitymodule.result.Result;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Nullable;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

@Component
@Slf4j
public class RemoveNodeOperations implements DataNodeOperations {

    public Result<ChangeNodeOperationsResult, FileEventSourceActions.FileEventError> doChangeNode(FileHeader.HeaderDescriptor inIndices,
                                                                                                 FileChangeEventInput eventInput) {
        var nodeType = getDataNode(eventInput, inIndices).one().get();
        List<DataNode> existingNodes = new ArrayList<>(inIndices.inIndices());
        var updateNodes = removeNodes(existingNodes, nodeType);
        return Result.ok(new ChangeNodeOperationsResult(nodeType, new FileHeader.HeaderDescriptor(updateNodes, inIndices.headerDescriptorData())));
    }

    private List<DataNode> removeNodes(List<DataNode> existingNodes, DataNode nodeType) {

        var outNodes = new ArrayList<DataNode>();
        List<DataNode> last = null;
        Queue<DataNode> lastItem = new ConcurrentLinkedQueue<>();
        boolean inserted = false;

        for (var v : existingNodes) {
            Pair<Boolean, List<DataNode>> c = splitNode(v, nodeType, last, lastItem, inserted);
            outNodes.addAll(c.getRight());
            last = c.getRight();
            lastItem.add(v);
            inserted  = c.getKey();
        }

        return outNodes;
    }

    public Pair<Boolean, List<DataNode>> splitNode(DataNode toInsertInto, DataNode toInsert,
                                                     List<DataNode> last, Queue<DataNode> lastItem,
                                                     boolean inserted) {
        if (!inserted) {
            return doBeforeRemove(toInsertInto, toInsert);
        } else {
            return doAfterRemove(toInsertInto, toInsert, lastItem.poll(), last.getLast());
        }
    }

    private static void writeNotSupportedCaseLog(DataNode toInsertInto, DataNode toInsert) {
        log.error("Found case not supported: toInsert, {} toInsertInto {}.", toInsert, toInsertInto);
    }

    private static @NotNull Pair<Boolean, List<DataNode>> doBeforeRemove(DataNode toInsertInto, DataNode toInsert) {
        if (DataNodeOperations.isFirstOverlappingAllSecond(toInsert, toInsertInto)) {
            // return none as the whole node is removed.
            return Pair.of(false, Lists.newArrayList());
        }

        if (DataNodeOperations.isSecondNodeFullyBeforeFirstNode(toInsert, toInsertInto)) {
            return Pair.of(false, Lists.newArrayList(toInsertInto));
        }

        if (DataNodeOperations.isSecondNodeFullyAfterFirstNode(toInsert, toInsertInto, Math.toIntExact(toInsert.length()))) {
            return Pair.of(true, Lists.newArrayList(
                    DataNode.DataNodeFactory.fromNode(
                            toInsertInto,
                            toInsertInto.indexStart() - toInsert.length(),
                            toInsertInto.indexEnd() - toInsert.length()
                    )
            ));
        }

        if (DataNodeOperations.isFirstNodeFullyWithinSecondNode(toInsert, toInsertInto)) {
            return Pair.of(true, Lists.newArrayList(
                    DataNode.DataNodeFactory.fromNode(
                            toInsertInto,
                            toInsertInto.indexStart(),
                            toInsert.indexStart(),
                            toInsertInto.dataStart(),
                            toInsertInto.dataStart() + (toInsert.indexStart() - toInsertInto.indexStart())
                    ),
                    DataNode.DataNodeFactory.fromNode(
                            toInsertInto,
                            toInsert.indexStart(),
                            toInsertInto.indexEnd() - toInsert.length(),
                            (toInsertInto.dataStart() + (toInsert.indexStart() - toInsertInto.indexStart())) + toInsert.length(),
                            toInsertInto.dataEnd()
                    )
            ));
        }

        if (DataNodeOperations.isFirstLeftFlushOverlappingRightSecond(toInsert, toInsertInto)) {
            // shift the node by the amount of the insert - only return toInsertInto node, as the toInsert node will
            // be returned once it ends.
            return Pair.of(false, Lists.newArrayList());
        }

        if (DataNodeOperations.isFirstRightFlushOverlappingLeftSecond(toInsert, toInsertInto)) {
            // return the node toInsert along with the node being overlapped shifted.
            return Pair.of(false, Lists.newArrayList());
        }

        if (DataNodeOperations.isFirstLeftFlushSecond(toInsert, toInsertInto)) {
            // return the node with skip removed
            return Pair.of(true, Lists.newArrayList(
                    DataNode.DataNodeFactory.fromNode(
                            toInsertInto,
                            toInsertInto.indexStart(),
                            toInsertInto.indexEnd() - toInsert.length(),
                            toInsertInto.dataStart() + toInsert.length(),
                            toInsertInto.dataEnd()
                    )
            ));
        }

        if (DataNodeOperations.isFirstRightFlushSecond(toInsert, toInsertInto)) {
            // return two new nodes
            return Pair.of(false, Lists.newArrayList(
                    DataNode.DataNodeFactory.fromNode(
                            toInsertInto,
                            toInsertInto.indexStart(),
                            toInsertInto.indexStart() + (toInsertInto.length() - toInsert.length()),
                            toInsertInto.dataStart(),
                            toInsertInto.dataStart() + (toInsertInto.length() - toInsert.length())
                    )
            ));
        }

        if (DataNodeOperations.isFirstRightAlterFlushSecond(toInsert, toInsertInto)) {
            return Pair.of(true, Lists.newArrayList(
                    DataNode.DataNodeFactory.fromNode(
                            toInsertInto,
                            toInsertInto.indexStart() - toInsert.length(),
                            toInsertInto.indexEnd() - toInsert.length()
                    )
            ));
        }

        if (DataNodeOperations.isFirstLeftAlterFlushSecond(toInsert, toInsertInto)) {
            return Pair.of(false, Lists.newArrayList(
                    DataNode.DataNodeFactory.fromNode(
                            toInsertInto,
                            toInsertInto.indexStart(),
                            toInsertInto.indexEnd()
                    )
            ));
        }

        if (DataNodeOperations.isFirstAllFlushSecond(toInsert, toInsertInto)) {
            return Pair.of(false, Lists.newArrayList());
        }

        if (DataNodeOperations.isFirstOverlappingRightSecond(toInsert, toInsertInto)) {
            // return up until the skip node
            return Pair.of(false, Lists.newArrayList(
                    DataNode.DataNodeFactory.fromNode(
                            toInsertInto,
                            toInsertInto.indexStart(),
                            toInsert.indexStart(),
                            toInsertInto.dataStart(),
                            toInsertInto.dataStart() + (toInsert.indexStart() - toInsertInto.indexStart())
                    )
            ));
        }

        if (DataNodeOperations.isFirstOverlappingLeftSecond(toInsert, toInsertInto)) {
            // return after skip node
            long newLength = toInsertInto.indexEnd() - toInsert.indexEnd();
            return Pair.of(true, Lists.newArrayList(
                    DataNode.DataNodeFactory.fromNode(
                            toInsertInto,
                            toInsert.indexStart(),
                            toInsert.indexStart() + newLength,
                            toInsertInto.dataStart() + (toInsert.indexEnd() - toInsertInto.indexStart()),
                            toInsertInto.dataEnd()
                    )
            ));
        }

        writeNotSupportedCaseLog(toInsertInto, toInsert);
        return Pair.of(false, new ArrayList<>());
    }

    private static Pair<Boolean, List<DataNode>> doAfterRemove(DataNode toInsertInto, DataNode toInsert, DataNode lastLast, DataNode last) {
        return Pair.of(true, Lists.newArrayList(
                DataNode.DataNodeFactory.fromNode(
                        toInsertInto,
                        toInsertInto.indexStart() - toInsert.length(),
                        toInsertInto.indexEnd() - toInsert.length()
                )
        ));
    }

    private static Result<DataNode.SkipNode, FileEventSourceActions.FileEventError> getDataNode(FileChangeEventInput eventInput, FileHeader.HeaderDescriptor inIndices) {
        if(eventInput.getChangeType() == FileChangeType.REMOVE_CONTENT) {
            return Result.ok(new DataNode.SkipNode(
                    eventInput.getOffset(),
                    eventInput.getOffset() + eventInput.getLength(),
                    true
                    ));
        }

        return Result.err(new FileEventSourceActions.FileEventError("Did not recognize event type: %s.".formatted(eventInput)));
    }
}

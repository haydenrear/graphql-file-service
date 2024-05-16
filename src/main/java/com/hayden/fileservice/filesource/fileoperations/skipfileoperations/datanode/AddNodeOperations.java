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
public class AddNodeOperations implements DataNodeOperations{


    @Override
    public Result<ChangeNodeOperationsResult, FileEventSourceActions.FileEventError> doChangeNode(FileHeader.HeaderDescriptor inIndices, FileChangeEventInput eventInput) {
        var nodeType = getDataNode(eventInput, inIndices).get();
        List<DataNode> existingNodes = new ArrayList<>(inIndices.inIndices());
        var updateNodes = insertNodes(existingNodes, nodeType);
        return Result.ok(new ChangeNodeOperationsResult(nodeType, new FileHeader.HeaderDescriptor(updateNodes, inIndices.headerDescriptorData())));
    }

    /**
     * Note: flush means up against perfectly or indices being equal, overlapping means index greater than
     * @param existingNodes
     * @param nodeType
     * @return
     */
    private List<DataNode> insertNodes(List<DataNode> existingNodes, DataNode nodeType) {

        var outNodes = new ArrayList<DataNode>();
        List<DataNode> last = null;
        Queue<DataNode> lastItem = new ConcurrentLinkedQueue<>();
        boolean inserted = false;

        for (var v : existingNodes) {
            List<DataNode> c = splitNode(v, nodeType, last, lastItem, inserted);
            outNodes.addAll(c);
            last = c;
            lastItem.add(v);
            if (!inserted) {
                inserted = c.contains(nodeType);
            }
        }

        if (!inserted)
            outNodes.add(nodeType);

        return outNodes;
    }

    public List<DataNode> splitNode(DataNode toInsertInto, DataNode toInsert,
                                    List<DataNode> last, Queue<DataNode> lastItem,
                                    boolean inserted) {
        if (!inserted) {
            return doBeforeInsert(toInsertInto, toInsert);
        } else {
            return doAfterInsert(toInsertInto, toInsert, lastItem.poll(), last.getLast());
        }
    }

    private static void writeNotSupportedCaseLog(DataNode toInsertInto, DataNode toInsert) {
        log.error("Found case not supported: toInsert, {} toInsertInto {}.", toInsert, toInsertInto);
    }

    private static @NotNull ArrayList<DataNode> doBeforeInsert(DataNode toInsertInto, DataNode toInsert) {

        if (DataNodeOperations.isFirstOverlappingAllSecond(toInsert, toInsertInto)) {
            // return just the toInsertInto node shifted, the toInsert node will be returned when there exists
            // a left overlap
            return Lists.newArrayList(
                    DataNode.DataNodeFactory.fromNode(
                            toInsertInto,
                            toInsertInto.indexStart() + toInsert.length(),
                            toInsertInto.indexEnd() + toInsert.length()
                    )
            );
        }

        if (DataNodeOperations.isSecondNodeFullyBeforeFirstNode(toInsert, toInsertInto)) {
            return Lists.newArrayList(toInsertInto);
        }

        if (DataNodeOperations.isSecondNodeFullyAfterFirstNode(toInsert, toInsertInto, Math.toIntExact(toInsert.length()))) {
            return Lists.newArrayList(
                    DataNode.DataNodeFactory.fromNode(
                            toInsertInto,
                            toInsertInto.indexStart() + toInsert.length(),
                            toInsertInto.indexEnd() + toInsert.length()
                    )
            );
        }

        if (DataNodeOperations.isFirstNodeFullyWithinSecondNode(toInsert, toInsertInto)) {
            long toInsertFirst = toInsert.indexStart() - toInsertInto.indexStart();
            long toInsertSecondLength = toInsertInto.length() - toInsertFirst;
            long toInsertEnd = toInsert.indexEnd() + toInsertSecondLength;
            return Lists.newArrayList(
                    DataNode.DataNodeFactory.fromNode(
                            toInsertInto,
                            toInsertInto.indexStart(),
                            toInsert.indexStart(),
                            toInsertInto.dataStart(),
                            toInsertInto.dataStart() + (toInsert.indexStart() - toInsertInto.indexStart())
                    ),
                    toInsert,
                    DataNode.DataNodeFactory.fromNode(
                            toInsertInto,
                            toInsert.indexEnd(),
                            toInsertEnd,
                            toInsertInto.dataStart() + (toInsert.indexStart() - toInsertInto.indexStart()),
                            toInsertInto.dataEnd()
                    )
            );
        }

        if (DataNodeOperations.isFirstLeftFlushOverlappingRightSecond(toInsert, toInsertInto)) {
            // shift the node by the amount of the insert - only return toInsertInto node, as the toInsert node will
            // be returned once it ends.
            return Lists.newArrayList(
                    DataNode.DataNodeFactory.fromNode(
                            toInsertInto,
                            toInsertInto.indexStart() + toInsert.length(),
                            toInsert.indexEnd() + toInsertInto.length()
                    )
            );
        }

        if (DataNodeOperations.isFirstRightFlushOverlappingLeftSecond(toInsert, toInsertInto)) {
            // return the node toInsert along with the node being overlapped shifted.
            return Lists.newArrayList(
                    toInsert,
                    DataNode.DataNodeFactory.fromNode(
                            toInsertInto,
                            toInsertInto.indexStart() + toInsert.length(),
                            toInsert.indexEnd() + toInsert.length()
                    )
            );
        }

        if (DataNodeOperations.isFirstLeftFlushSecond(toInsert, toInsertInto)) {
            // return two new nodes
            long start = toInsertInto.indexStart() + toInsert.length();
            long end = start + toInsertInto.length();
            return Lists.newArrayList(
                    toInsert,
                    DataNode.DataNodeFactory.fromNode(
                            toInsertInto,
                            toInsertInto.indexStart() + toInsert.length(),
                            end
                    )
            );
        }

        if (DataNodeOperations.isFirstRightFlushSecond(toInsert, toInsertInto)) {
            // return two new nodes
            long toInsertFirst = toInsert.indexStart() - toInsertInto.indexStart();
            long toInsertSecondLength = toInsertInto.length() - toInsertFirst;
            long toInsertEnd = toInsert.indexEnd() + toInsertSecondLength;
            return Lists.newArrayList(
                    DataNode.DataNodeFactory.fromNode(
                            toInsertInto,
                            toInsertInto.indexStart(),
                            toInsert.indexStart(),
                            toInsertInto.dataStart(),
                            toInsertInto.dataStart() + (toInsertInto.length() - toInsert.length())
                    ),
                    toInsert,
                    DataNode.DataNodeFactory.fromNode(
                            toInsertInto,
                            toInsert.indexEnd(),
                            toInsertEnd,
                            toInsertInto.dataStart() + (toInsertInto.length() - toInsert.length()),
                            toInsertInto.dataEnd()
                    )
            );
        }

        if (DataNodeOperations.isFirstRightAlterFlushSecond(toInsert, toInsertInto)) {
            return Lists.newArrayList(
                    DataNode.DataNodeFactory.fromNode(
                            toInsertInto,
                            toInsertInto.indexStart(),
                            toInsert.indexStart()
                    )
            );
        }

        if (DataNodeOperations.isFirstLeftAlterFlushSecond(toInsert, toInsertInto)) {
            return Lists.newArrayList(
                    DataNode.DataNodeFactory.fromNode(
                            toInsertInto,
                            toInsertInto.indexStart(),
                            toInsert.indexStart()
                    )
            );
        }

        if (DataNodeOperations.isFirstAllFlushSecond(toInsert, toInsertInto)) {
            long firstPart = toInsert.indexStart() - toInsertInto.indexStart();
            long secondPart = toInsertInto.length() - firstPart;
            long end = toInsert.indexEnd() + secondPart;
            return Lists.newArrayList(
                    toInsert,
                    DataNode.DataNodeFactory.fromNode(
                            toInsertInto,
                            toInsert.indexEnd(),
                            end
                    )
            );
        }

        if (DataNodeOperations.isFirstOverlappingRightSecond(toInsert, toInsertInto)) {
            // return two nodes, the first is before the overlap up to the overlap start. The second is starting at
            // (the overlap start + length of toInsert) and ending at (length of toInsert + length of second part)
            long firstPart = toInsert.indexStart() - toInsertInto.indexStart();
            long secondPart = toInsertInto.length() - firstPart;
            long end = toInsert.indexEnd() + secondPart;
            return Lists.newArrayList(
                    DataNode.DataNodeFactory.fromNode(
                            toInsertInto,
                            toInsertInto.indexStart(),
                            toInsert.indexStart(),
                            toInsertInto.dataStart(),
                            toInsertInto.dataStart() + (toInsert.indexStart() - toInsertInto.indexStart())
                    ),
                    DataNode.DataNodeFactory.fromNode(
                            toInsertInto,
                            toInsert.indexEnd(),
                            end,
                            toInsertInto.dataStart() + (toInsert.indexStart() - toInsertInto.indexStart()),
                            toInsertInto.dataEnd()
                    )
            );
        }

        if (DataNodeOperations.isFirstOverlappingLeftSecond(toInsert, toInsertInto)) {
            // return the node toInsert along with the node shifted to the right.
            return Lists.newArrayList(
                    toInsert,
                    DataNode.DataNodeFactory.fromNode(
                            toInsertInto,
                            toInsertInto.indexStart() + toInsert.length(),
                            toInsertInto.indexEnd() + toInsert.length()
                    )
            );
        }

        writeNotSupportedCaseLog(toInsertInto, toInsert);
        return new ArrayList<>();
    }

    private static @Nullable ArrayList<DataNode> doAfterInsert(DataNode toInsertInto, DataNode toInsert, DataNode lastLast, DataNode last) {
        return Lists.newArrayList(
                DataNode.DataNodeFactory.fromNode(
                        toInsertInto,
                        toInsertInto.indexStart() + toInsert.length(),
                        toInsertInto.indexEnd() + toInsert.length()
                )
        );
    }

    private static Result<DataNode.AddNode, FileEventSourceActions.FileEventError> getDataNode(FileChangeEventInput eventInput, FileHeader.HeaderDescriptor inIndices) {
        if (eventInput.getChangeType() == FileChangeType.ADD_CONTENT){
            return Result.ok(new DataNode.AddNode(
                    eventInput.getOffset(),
                    eventInput.getOffset() + eventInput.getLength(),
                    inIndices.headerDescriptorData().dataEnd(),
                    inIndices.headerDescriptorData().dataEnd() + eventInput.getLength(), true
            ));
        }

        return Result.err(new FileEventSourceActions.FileEventError("Did not recognize event type: %s.".formatted(eventInput)));
    }

}

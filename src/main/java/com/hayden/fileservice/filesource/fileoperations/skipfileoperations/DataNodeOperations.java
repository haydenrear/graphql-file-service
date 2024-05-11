package com.hayden.fileservice.filesource.fileoperations.skipfileoperations;

import com.google.common.collect.Lists;
import com.hayden.fileservice.codegen.types.FileChangeEventInput;
import com.hayden.fileservice.codegen.types.FileChangeType;
import com.hayden.fileservice.graphql.FileEventSourceActions;
import com.hayden.utilitymodule.result.Result;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
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
public class DataNodeOperations {

    public Result<FileHeader.HeaderDescriptor, FileEventSourceActions.FileEventError> insertNode(FileHeader.HeaderDescriptor inIndices,
                                                                                                 FileChangeEventInput eventInput) {
        var nodeType = getDataNode(eventInput, inIndices).get();
        List<DataNode> existingNodes = new ArrayList<>(inIndices.inIndices());
        if (nodeType instanceof DataNode.AddNode) {
            var updateNodes = insertNodes(existingNodes, nodeType);
            return Result.ok(new FileHeader.HeaderDescriptor(updateNodes, inIndices.headerDescriptorData()));
        }
        else if (nodeType instanceof DataNode.SkipNode) {
            var updateNodes = removeNodes(existingNodes, nodeType);
            return Result.ok(new FileHeader.HeaderDescriptor(updateNodes, inIndices.headerDescriptorData()));
        }

        return Result.err(new FileEventSourceActions.FileEventError("Did not recognize node type: %s.".formatted(nodeType)));
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

    private List<DataNode> removeNodes(List<DataNode> existingNodes, DataNode nodeType) {

        var outNodes = new ArrayList<DataNode>();
        List<DataNode> last = null;
        Queue<DataNode> lastItem = new ConcurrentLinkedQueue<>();
        boolean inserted = false;

        for (var v : existingNodes) {
            Pair<Boolean, List<DataNode>> c = splitRemove(v, nodeType, last, lastItem, inserted);
            outNodes.addAll(c.getRight());
            last = c.getRight();
            lastItem.add(v);
            inserted  = c.getKey();
        }

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

    public Pair<Boolean, List<DataNode>> splitRemove(DataNode toInsertInto, DataNode toInsert,
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
        if (isFirstOverlappingAllSecond(toInsert, toInsertInto)) {
            // return none as the whole node is removed.
            return Pair.of(false, Lists.newArrayList());
        }

        if (isSecondNodeFullyBeforeFirstNode(toInsert, toInsertInto)) {
            return Pair.of(false, Lists.newArrayList(toInsertInto));
        }

        if (isSecondNodeFullyAfterFirstNode(toInsert, toInsertInto, Math.toIntExact(toInsert.length()))) {
            return Pair.of(true, Lists.newArrayList(
                    DataNode.DataNodeFactory.fromNode(
                            toInsertInto,
                            toInsertInto.indexStart() - toInsert.length(),
                            toInsertInto.indexEnd() - toInsert.length()
                    )
            ));
        }

        if (isFirstNodeFullyWithinSecondNode(toInsert, toInsertInto)) {
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

        if (isFirstLeftFlushOverlappingRightSecond(toInsert, toInsertInto)) {
            // shift the node by the amount of the insert - only return toInsertInto node, as the toInsert node will
            // be returned once it ends.
            return Pair.of(false, Lists.newArrayList(
                    DataNode.DataNodeFactory.fromNode(
                            toInsertInto,
                            toInsertInto.indexStart() - toInsert.length(),
                            toInsert.indexEnd() - toInsert.length()
                    )
            ));
        }

        if (isFirstRightFlushOverlappingLeftSecond(toInsert, toInsertInto)) {
            // return the node toInsert along with the node being overlapped shifted.
            return Pair.of(true, Lists.newArrayList(
                    DataNode.DataNodeFactory.fromNode(
                            toInsertInto,
                            toInsertInto.indexStart() - toInsert.length(),
                            toInsert.indexEnd() - toInsert.length()
                    )
            ));
        }

        if (isFirstLeftFlushSecond(toInsert, toInsertInto)) {
            // return the node with skip removed
            return Pair.of(true, Lists.newArrayList(
                    DataNode.DataNodeFactory.fromNode(
                            toInsertInto,
                            toInsertInto.indexStart(),
                            toInsertInto.indexEnd() - toInsert.length(),
                            toInsertInto.indexStart() + toInsert.length(),
                            toInsertInto.dataEnd()
                    )
            ));
        }

        if (isFirstRightFlushSecond(toInsert, toInsertInto)) {
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

        if (isFirstRightAlterFlushSecond(toInsert, toInsertInto)) {
            return Pair.of(true, Lists.newArrayList(
                    DataNode.DataNodeFactory.fromNode(
                            toInsertInto,
                            toInsertInto.indexStart() - toInsert.length(),
                            toInsertInto.indexEnd() - toInsert.length()
                    )
            ));
        }

        if (isFirstLeftAlterFlushSecond(toInsert, toInsertInto)) {
            return Pair.of(false, Lists.newArrayList(
                    DataNode.DataNodeFactory.fromNode(
                            toInsertInto,
                            toInsertInto.indexStart(),
                            toInsertInto.indexEnd()
                    )
            ));
        }

        if (isFirstAllFlushSecond(toInsert, toInsertInto)) {
            return Pair.of(false, Lists.newArrayList());
        }

        if (isFirstOverlappingRightSecond(toInsert, toInsertInto)) {
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

        if (isFirstOverlappingLeftSecond(toInsert, toInsertInto)) {
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

    private static @Nullable Pair<Boolean, List<DataNode>> doAfterRemove(DataNode toInsertInto, DataNode toInsert, DataNode lastLast, DataNode last) {
        return Pair.of(true, Lists.newArrayList(
                DataNode.DataNodeFactory.fromNode(
                        toInsertInto,
                        toInsertInto.indexStart() - toInsert.length(),
                        toInsertInto.indexEnd() - toInsert.length()
                )
        ));
    }

    private static @NotNull ArrayList<DataNode> doBeforeInsert(DataNode toInsertInto, DataNode toInsert) {
        if (isFirstOverlappingAllSecond(toInsert, toInsertInto)) {
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

        if (isSecondNodeFullyBeforeFirstNode(toInsert, toInsertInto)) {
            return Lists.newArrayList(toInsertInto);
        }

        if (isSecondNodeFullyAfterFirstNode(toInsert, toInsertInto, Math.toIntExact(toInsert.length()))) {
            return Lists.newArrayList(
                    DataNode.DataNodeFactory.fromNode(
                            toInsertInto,
                            toInsertInto.indexStart() + toInsert.length(),
                            toInsertInto.indexEnd() + toInsert.length()
                    )
            );
        }

        if (isFirstNodeFullyWithinSecondNode(toInsert, toInsertInto)) {
            long toInsertFirst = toInsert.indexStart() - toInsertInto.indexStart();
            long toInsertSecondLength = toInsertInto.length() - toInsertFirst;
            long toInsertEnd = toInsert.indexEnd() + toInsertSecondLength;
            return Lists.newArrayList(
                    DataNode.DataNodeFactory.fromNode(
                            toInsertInto,
                            toInsertInto.indexStart(),
                            toInsert.indexStart()
                    ),
                    toInsert,
                    DataNode.DataNodeFactory.fromNode(
                            toInsertInto,
                            toInsert.indexEnd(),
                            toInsertEnd
                            )
            );
        }

        if (isFirstLeftFlushOverlappingRightSecond(toInsert, toInsertInto)) {
            // shift the node by the amount of the insert - only return toInsertInto node, as the toInsert node will
            // be returned once it ends.
            return Lists.newArrayList(
                    DataNode.DataNodeFactory.fromNode(
                            toInsertInto,
                            toInsertInto.indexStart() + toInsert.length(),
                            toInsert.indexEnd() + toInsert.length()
                    )
            );
        }

        if (isFirstRightFlushOverlappingLeftSecond(toInsert, toInsertInto)) {
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

        if (isFirstLeftFlushSecond(toInsert, toInsertInto)) {
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

        if (isFirstRightFlushSecond(toInsert, toInsertInto)) {
            // return two new nodes
            long toInsertFirst = toInsert.indexStart() - toInsertInto.indexStart();
            long toInsertSecondLength = toInsertInto.length() - toInsertFirst;
            long toInsertEnd = toInsert.indexEnd() + toInsertSecondLength;
            return Lists.newArrayList(
                    DataNode.DataNodeFactory.fromNode(
                            toInsertInto,
                            toInsertInto.indexStart(),
                            toInsert.indexStart()
                    ),
                    toInsert,
                    DataNode.DataNodeFactory.fromNode(
                            toInsertInto,
                            toInsert.indexEnd(),
                            toInsertEnd
                    )
            );
        }

        if (isFirstRightAlterFlushSecond(toInsert, toInsertInto)) {
            return Lists.newArrayList(
                    DataNode.DataNodeFactory.fromNode(
                            toInsertInto,
                            toInsertInto.indexStart(),
                            toInsert.indexStart()
                    )
            );
        }

        if (isFirstLeftAlterFlushSecond(toInsert, toInsertInto)) {
            return Lists.newArrayList(
                    DataNode.DataNodeFactory.fromNode(
                            toInsertInto,
                            toInsertInto.indexStart(),
                            toInsert.indexStart()
                    )
            );
        }

        if (isFirstAllFlushSecond(toInsert, toInsertInto)) {
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

        if (isFirstOverlappingRightSecond(toInsert, toInsertInto)) {
            // return two nodes, the first is before the overlap up to the overlap start. The second is starting at
            // (the overlap start + length of toInsert) and ending at (length of toInsert + length of second part)
            long firstPart = toInsert.indexStart() - toInsertInto.indexStart();
            long secondPart = toInsertInto.length() - firstPart;
            long end = toInsert.indexEnd() + secondPart;
            return Lists.newArrayList(
                    DataNode.DataNodeFactory.fromNode(
                            toInsertInto,
                            toInsertInto.indexStart(),
                            toInsert.indexStart()
                    ),
                    DataNode.DataNodeFactory.fromNode(
                            toInsertInto,
                            toInsert.indexEnd(),
                            end
                    )
            );
        }

        if (isFirstOverlappingLeftSecond(toInsert, toInsertInto)) {
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

    private static boolean isSecondNodeFullyBeforeFirstNode(DataNode first, DataNode second) {
        return second.indexEnd() < first.indexEnd() && second.indexStart() < first.indexStart()
                && second.indexStart() < first.indexEnd() && second.indexEnd() < first.indexStart();
    }

    private static boolean isSecondNodeFullyAfterFirstNode(DataNode first, DataNode second) {
        return isSecondNodeFullyAfterFirstNode(first, second, 0);
    }

    private static boolean isFirstOverlappingAllSecond(DataNode first, DataNode second) {
        return second.indexEnd() < first.indexEnd() && second.indexStart() > first.indexStart();
    }

    private static boolean isSecondNodeFullyAfterFirstNode(DataNode first, DataNode second, int offset) {
        return second.indexEnd() > first.indexEnd() && second.indexStart() > first.indexStart()
                && second.indexStart() > first.indexEnd() && second.indexEnd() > first.indexStart();
    }

    private static boolean isFirstNodeFullyWithinSecondNode(DataNode first, DataNode second) {
        return second.indexEnd() > first.indexEnd() && second.indexStart() < first.indexStart();
    }

    private static boolean isFirstOverlappingRightSecond(DataNode first, DataNode second) {
        return second.indexEnd() < first.indexEnd() && second.indexStart() < first.indexStart();
    }


    private static boolean isFirstOverlappingLeftSecond(DataNode first, DataNode second) {
        return second.indexEnd() > first.indexEnd() && second.indexStart() > first.indexStart();
    }

    private static boolean isFirstAllFlushSecond(DataNode first, DataNode second) {
        return second.indexEnd() == first.indexEnd() && second.indexStart() == first.indexStart();
    }

    private static boolean isFirstRightFlushOverlappingLeftSecond(DataNode first, DataNode second) {
        return second.indexEnd() == first.indexEnd() && second.indexStart() > first.indexStart();
    }

    private static boolean isFirstLeftFlushOverlappingRightSecond(DataNode first, DataNode second) {
        return second.indexEnd() < first.indexEnd() && second.indexStart() == first.indexStart();
    }

    private static boolean isFirstRightFlushSecond(DataNode first, DataNode second) {
        return second.indexEnd() == first.indexEnd() && second.indexStart() < first.indexStart();
    }

    private static boolean isFirstLeftFlushSecond(DataNode first, DataNode second) {
        return second.indexEnd() > first.indexEnd() && second.indexStart() == first.indexStart();
    }

    private static boolean isFirstRightAlterFlushSecond(DataNode first, DataNode second) {
        return first.indexEnd() == second.indexStart() && second.indexStart() > first.indexStart();
    }

    private static boolean isFirstLeftAlterFlushSecond(DataNode first, DataNode second) {
        return first.indexStart() == second.indexEnd() && first.indexStart() > second.indexStart();
    }

    private static Result<DataNode, FileEventSourceActions.FileEventError> getDataNode(FileChangeEventInput eventInput, FileHeader.HeaderDescriptor inIndices) {
        if(eventInput.getChangeType() == FileChangeType.REMOVE_CONTENT) {
            return Result.ok(new DataNode.SkipNode(
                    eventInput.getOffset(),
                    eventInput.getOffset() + eventInput.getLength(),
                    true
                    ));
        } else if (eventInput.getChangeType() == FileChangeType.ADD_CONTENT){
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

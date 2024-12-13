package com.hayden.fileservice.filesource.fileoperations.skipfileoperations.datanode;

import com.hayden.utilitymodule.result.agg.Agg;
import com.hayden.utilitymodule.result.agg.Responses;
import lombok.experimental.UtilityClass;

/**
 * @param indexEnd End of where data should go in content section.
 * @param dataStart Start of where data exists in operations section.
 * @param dataEnd End of where data exists in operations section.
 */
public interface DataNode {
    /**
     * @return indexStart Start of where data should go in content section.
     */
    long indexStart();
    long indexEnd();
    long dataStart();
    long dataEnd();
    boolean change();


    record FileEventHeaderResult(byte[] responses) implements Responses.AggregateResponse {

        @Override
        public void addAgg(Agg aggregateResponse) {
            throw new UnsupportedOperationException("Haven't refactored to mapping collector.");
        }
    }

    default long length() {
        return indexEnd() - indexStart();
    }

    record AddNode(long indexStart, long indexEnd, long dataStart, long dataEnd, boolean change) implements DataNode {
        public AddNode(long indexStart, long indexEnd, boolean change)  {
            this(indexStart, indexEnd, -1, -1, change) ;
        }
        public AddNode(NodeArgs nodeArgs) {
            this(nodeArgs.indexStart, nodeArgs.indexEnd, nodeArgs.dataStart, nodeArgs.dataStart, nodeArgs.change);
        }
    }

    record SkipNode(long indexStart, long indexEnd, long dataStart, long dataEnd, boolean change) implements DataNode {
        public SkipNode(long indexStart, long indexEnd, boolean change)  {
            this(indexStart, indexEnd, -1, -1, change) ;
        }
        public SkipNode(NodeArgs nodeArgs) {
            this(nodeArgs.indexStart, nodeArgs.indexEnd, nodeArgs.dataStart, nodeArgs.dataStart, nodeArgs.change);
        }
    }

    record NodeArgs(long indexStart, long indexEnd, long dataStart, long dataEnd, boolean change) {
    }

    @UtilityClass
    class DataNodeFactory {

        public static DataNode nodeShifted(AddNode currentNode, long length) {
            return new AddNode(currentNode.indexStart + length, currentNode.indexEnd + length,
                    currentNode.dataStart, currentNode.dataEnd, true);
        }

        public static DataNode nodeShifted(SkipNode currentNode, long length) {
            return new SkipNode(currentNode.indexStart + length, currentNode.indexEnd + length, true);
        }

        public static DataNode fromNode(DataNode dataNode, long indexStart, long indexEnd) {
            return switch(dataNode) {
                case AddNode a -> fromNode(a, indexStart, indexEnd);
                case SkipNode s -> fromNode(s, indexStart, indexEnd);
                default ->
                        throw new IllegalStateException("Unexpected value: " + dataNode);
            };
        }

        public static DataNode fromNode(DataNode dataNode, long indexStart, long indexEnd, long dataStart, long dataEnd) {
            return switch(dataNode) {
                case AddNode a -> fromNode(a, indexStart, indexEnd, dataStart, dataEnd);
                case SkipNode s -> fromNode(s, indexStart, indexEnd, dataStart, dataEnd);
                default ->
                        throw new IllegalStateException("Unexpected value: " + dataNode);
            };
        }

        public static DataNode fromNode(AddNode dataNode, long indexStart, long indexEnd) {
            return new AddNode(indexStart, indexEnd, dataNode.dataStart, dataNode.dataEnd, true);
        }

        public static DataNode fromNode(AddNode dataNode, long indexStart, long indexEnd, long dataStart, long dataEnd) {
            return new AddNode(indexStart, indexEnd, dataStart, dataEnd, true);
        }

        public static DataNode fromNode(SkipNode dataNode, long indexStart, long indexEnd, long dataStart, long dataEnd) {
            return new SkipNode(indexStart, indexEnd, true);
        }

    }

}

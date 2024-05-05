package com.hayden.fileservice.filesource.fileoperations.skipfileoperations;

import com.hayden.fileservice.filesource.util.NumberEncoder;
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

    default byte[] toByteArray() {

//        return NumberEncoder.encodeNumber()
        return null;
    }

    default long length() {
        return indexEnd() - indexStart();
    }

    record AddNode(long indexStart, long indexEnd, long dataStart, long dataEnd, boolean change) implements DataNode {
        public AddNode(long indexStart, long indexEnd, boolean change)  {
            this(indexStart, indexEnd, -1, -1, change) ;
        }
    }

    record SkipNode(long indexStart, long indexEnd, long dataStart, long dataEnd, boolean change) implements DataNode {
        public SkipNode(long indexStart, long indexEnd, boolean change)  {
            this(indexStart, indexEnd, -1, -1, change) ;
        }
    }

    @UtilityClass
    public class DataNodeFactory {

        public static DataNode nodeShifted(AddNode currentNode, long length) {
            return new AddNode(currentNode.indexStart + length, currentNode.indexEnd + length,
                    currentNode.dataStart, currentNode.dataEnd, true);
        }

        public static DataNode nodeShifted(SkipNode currentNode, long length) {
            return new SkipNode(currentNode.indexStart + length, currentNode.indexEnd + length, true);
        }

        public static DataNode fromNode(AddNode dataNode, long indexStart, long indexEnd) {
            return new AddNode(indexStart, indexEnd, dataNode.dataStart, dataNode.dataEnd, true);
        }

        public static DataNode fromNode(SkipNode dataNode, long indexStart, long indexEnd) {
            return new SkipNode(indexStart, indexEnd, true);
        }

    }

}

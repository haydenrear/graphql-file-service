package com.hayden.fileservice.filesource.fileoperations.skipfileoperations;

import com.hayden.fileservice.config.FileProperties;
import com.hayden.fileservice.filesource.fileoperations.skipfileoperations.datanode.DataNode;
import com.hayden.fileservice.graphql.FileEventSourceActions;
import com.hayden.utilitymodule.result.Result;

import java.io.*;
import java.util.List;


public interface FileHeader {

    static Result<Void, FileEventSourceActions.FileEventError> flush(File file, HeaderDescriptor headerDescriptor) {
        try(RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw")) {
//            randomAccessFile.write()
            return Result.ok(Void.TYPE.cast(null));
        } catch (IOException e) {
            return Result.err(new FileEventSourceActions.FileEventError(e));
        }
    }

    static Result<byte[], FileEventSourceActions.FileEventError> parseHeader(File file, FileProperties fileProperties) {
        try(FileInputStream fileInputStream = new FileInputStream(file)) {
            byte[] header = new byte[Math.toIntExact(fileProperties.getDataStreamFileHeaderLengthBytes())];
            if (fileInputStream.read(header) == -1) {
                return Result.err(new FileEventSourceActions.FileEventError("File input stream was not able to read full header. Could not read file."));
            }
            return Result.ok(header);
        } catch (IOException e) {
            return Result.err(new FileEventSourceActions.FileEventError(e));
        }
    }

    /**
     * @param dataStart The start of the append after content.
     * @param dataEnd The end of the append after the content.
     */
    record HeaderDescriptorData(long dataStart, long dataEnd) {}

    record HeaderDescriptor(
            List<DataNode> inIndices,
            HeaderDescriptorData headerDescriptorData) {
    }
}

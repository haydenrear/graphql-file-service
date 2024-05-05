package com.hayden.fileservice.filesource.fileoperations.skipfileoperations;

import com.hayden.fileservice.config.FileProperties;
import com.hayden.fileservice.filesource.util.NumberEncoder;
import com.hayden.fileservice.graphql.FileEventSourceActions;
import com.hayden.utilitymodule.result.Result;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

import static org.junit.jupiter.api.Assertions.*;

class HeaderOperationTypesTest {

    @Test
    void writeHeader() {
        Result<DataNode.FileEventHeaderResult, FileEventSourceActions.FileEventError> headerWrite = HeaderOperationTypes.writeHeader(new FileHeader.HeaderDescriptor(
                List.of(
                        new DataNode.AddNode(0, 10, 0, 20, true),
                        new DataNode.AddNode(10, 15, 0, 10, true),
                        new DataNode.AddNode(15, 20, 0, 10, true),
                        new DataNode.AddNode(20, 35, 0, 10, true),
                        new DataNode.AddNode(35, 45, 10, 20, true)
                ),
                new FileHeader.HeaderDescriptorData(100, 200)
        ), new FileProperties());

        assertTrue(headerWrite.isOk());
        byte[] responses = headerWrite.get().responses();
        System.out.println(Arrays.toString(responses));
        System.out.println(Arrays.toString(new String(responses).split(DataStreamFileDelim.SPLIT_HEADER_OPERATION_TY_DELIM.fileDelims)));
        HeaderOperationTypes.getOps(responses);
    }

    @Test
    public void testSplit() {
        byte[] ok = new byte[] {
                1,2,3,4,5,1,2,4,5,1,2,3,4,5,1,2
        };
        List<byte[]> bytes = HeaderOperationTypes.splitByteArrayByByteValue(new byte[]{1, 2}, ok, false);
        System.out.println(bytes);
    }

    @Test
    public void testEncodeDecode() {
        LongStream.range(1, 100000).boxed()
                .forEach(i -> {
                    byte[] en = NumberEncoder.encodeNumber(i);
                    long l = NumberEncoder.decodeNumber(en);
                    assertEquals(i, l);
                });
    }
}
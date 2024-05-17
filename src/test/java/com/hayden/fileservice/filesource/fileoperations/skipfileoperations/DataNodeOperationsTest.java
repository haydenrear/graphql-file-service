package com.hayden.fileservice.filesource.fileoperations.skipfileoperations;

import com.hayden.fileservice.codegen.types.FileChangeEventInput;
import com.hayden.fileservice.codegen.types.FileChangeType;
import com.hayden.fileservice.config.ByteArray;
import com.hayden.fileservice.filesource.fileoperations.skipfileoperations.datanode.*;
import org.assertj.core.util.Lists;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

class DataNodeOperationsTest {

    private final DataNodeOperations dataNodeOperations = new DataNodeOperationsDelegate(
            new AddNodeOperations(),
            new RemoveNodeOperations()
    );

    @Test
    public void testAddContentAfterAll() {
        // Create initial state with some existing nodes
        List<DataNode> existingNodes = new ArrayList<>();
        existingNodes.add(new DataNode.AddNode(0, 10, 90, 100, true));
        existingNodes.add(new DataNode.AddNode(10, 30, 100, 120, true));
        existingNodes.add(new DataNode.AddNode(30, 40, 120, 130, true));
        FileHeader.HeaderDescriptor inIndices = new FileHeader.HeaderDescriptor(existingNodes,
                new FileHeader.HeaderDescriptorData(90, 130));

        // Simulate removing content in the middle of the file
        FileChangeEventInput eventInput = createAdd(40, 40);
        FileHeader.HeaderDescriptor expectedOutput = new FileHeader.HeaderDescriptor(
                Lists.newArrayList(
                        new DataNode.AddNode(0, 10, 90, 100, true),
                        new DataNode.AddNode(10, 30, 100, 120, true),
                        new DataNode.AddNode(30, 40, 120, 130, true),
                        new DataNode.AddNode(40, 80, 130, 170, true)
                ),
                new FileHeader.HeaderDescriptorData(90, 170));
        // Call the method and assert the result
        var actualOutput = dataNodeOperations.doChangeNode(inIndices, eventInput).get();
        assertEqualsValue(expectedOutput.inIndices(), actualOutput.headerDescriptor().inIndices(), true);
    }


    @Test
    public void testInsertBeforeBlank() {
        // Create initial state with some existing nodes
        List<DataNode> existingNodes = new ArrayList<>();
        existingNodes.add(new DataNode.AddNode(0, 0, 2048, 2048, true));
        var inIndices = new FileHeader.HeaderDescriptor(existingNodes,
                new FileHeader.HeaderDescriptorData(2048, 2048));

        // Simulate inserting data before the first existing node
        FileChangeEventInput eventInput = createAdd(0, 5);
        FileHeader.HeaderDescriptor expectedOutput = new FileHeader.HeaderDescriptor(
                List.of(
                        new DataNode.AddNode(0, 5, 2048, 2053, true),
                        new DataNode.AddNode(5, 5, 2048, 2048, true)
                ),
                new FileHeader.HeaderDescriptorData(2048, 2048));

        // Call the method and assert the result
        var actualOutput = dataNodeOperations.doChangeNode(inIndices, eventInput).get().headerDescriptor();
        assertEqualsValue(expectedOutput.inIndices(), actualOutput.inIndices());
    }

    @Test
    public void testInsertBeforeExistingNode() {
        // Create initial state with some existing nodes
        List<DataNode> existingNodes = new ArrayList<>();
        existingNodes.add(new DataNode.AddNode(0, 10, 90, 100, true));
        existingNodes.add(new DataNode.AddNode(10, 20, 100, 110, true));
        existingNodes.add(new DataNode.AddNode(20, 30, 110, 120, true));
        existingNodes.add(new DataNode.AddNode(30, 40, 120, 130, true));
        var inIndices = new FileHeader.HeaderDescriptor(existingNodes,
                new FileHeader.HeaderDescriptorData(90, 130));

        // Simulate inserting data before the first existing node
        FileChangeEventInput eventInput = createAdd(5, 5);
        FileHeader.HeaderDescriptor expectedOutput = new FileHeader.HeaderDescriptor(
                List.of(
                        new DataNode.AddNode(0, 5, 90, 95, true),
                        new DataNode.AddNode(5, 10, 130, 135, true),
                        new DataNode.AddNode(10, 15, 95, 100, true),
                        new DataNode.AddNode(15, 25, 100, 110, true),
                        new DataNode.AddNode(25, 35, 110, 120, true),
                        new DataNode.AddNode(35, 45, 120, 130, true)
                ),
                new FileHeader.HeaderDescriptorData(90, 135));

        // Call the method and assert the result
        var actualOutput = dataNodeOperations.doChangeNode(inIndices, eventInput).get().headerDescriptor();
        assertEqualsValue(expectedOutput.inIndices(), actualOutput.inIndices(), true);
    }

    @Test
    public void testRemoveBefore() {
        // Create initial state with some existing nodes
        List<DataNode> existingNodes = new ArrayList<>();
        existingNodes.add(new DataNode.AddNode(0, 10, 0, 10, true));
        existingNodes.add(new DataNode.AddNode(10, 20, 10, 20, true));
        existingNodes.add(new DataNode.AddNode(20, 30, 20, 30, true));
        existingNodes.add(new DataNode.AddNode(30, 40, 30, 40, true));
        FileHeader.HeaderDescriptor inIndices = new FileHeader.HeaderDescriptor(existingNodes,
                new FileHeader.HeaderDescriptorData(0, 45));

        // Simulate inserting data before the first existing node
        FileChangeEventInput eventInput = createRemove(5, 5);
        FileHeader.HeaderDescriptor expectedOutput = new FileHeader.HeaderDescriptor(
                List.of(
                        new DataNode.AddNode(0, 5, 0, 5, true),
                        new DataNode.AddNode(5, 15, 10, 20, true),
                        new DataNode.AddNode(15, 25, 20, 30, true),
                        new DataNode.AddNode(25, 35, 30, 40, true)
                ),
                new FileHeader.HeaderDescriptorData(0, 45));

        // Call the method and assert the result
        var actualOutput = dataNodeOperations.doChangeNode(inIndices, eventInput).get().headerDescriptor();
        assertEqualsValue(expectedOutput.inIndices(), actualOutput.inIndices());
    }

    private static @NotNull FileChangeEventInput createRemove(int offset, int length) {
        FileChangeEventInput eventInput = new FileChangeEventInput("test", FileChangeType.REMOVE_CONTENT, offset, new ByteArray(new byte[0]), "", length);
        return eventInput;
    }

    private static @NotNull FileChangeEventInput createAdd(int offset, int length) {
        FileChangeEventInput eventInput = new FileChangeEventInput("test", FileChangeType.ADD_CONTENT, offset, new ByteArray(new byte[0]), "", length);
        return eventInput;
    }

    @Test
    public void testInsertAfterExistingNode() {
        // Create initial state with some existing nodes
        List<DataNode> existingNodes = new ArrayList<>();
        existingNodes.add(new DataNode.AddNode(0, 10, 90, 100, true));
        existingNodes.add(new DataNode.AddNode(10, 20, 100, 110, true));
        existingNodes.add(new DataNode.AddNode(20, 30, 110, 120, true));
        existingNodes.add(new DataNode.AddNode(30, 40, 120, 130, true));
        FileHeader.HeaderDescriptor inIndices = new FileHeader.HeaderDescriptor(existingNodes,
                new FileHeader.HeaderDescriptorData(90, 130));

        // Simulate inserting data after the first existing node
        FileChangeEventInput eventInput = createAdd(20, 5);
        FileHeader.HeaderDescriptor expectedOutput = new FileHeader.HeaderDescriptor(
                List.of(
                        new DataNode.AddNode(0, 10, 90, 100, true),
                        new DataNode.AddNode(10, 20, 100, 110, true),
                        new DataNode.AddNode(20, 25, 130, 135, true),
                        new DataNode.AddNode(25, 35, 110, 120, true),
                        new DataNode.AddNode(35, 45, 120, 130, true)
                ),
                new FileHeader.HeaderDescriptorData(90, 135));

        // Call the method and assert the result
        var actualOutput = dataNodeOperations.doChangeNode(inIndices, eventInput).get().headerDescriptor();
        assertEqualsValue(expectedOutput.inIndices(), actualOutput.inIndices(), true);
    }

    @Test
    public void testAddContentAtBeginning() {
        // Create initial state with some existing nodes
        List<DataNode> existingNodes = new ArrayList<>();
        existingNodes.add(new DataNode.AddNode(0, 10, 90, 100, true));
        existingNodes.add(new DataNode.AddNode(10, 20, 100, 110, true));
        existingNodes.add(new DataNode.AddNode(20, 30, 110, 120, true));
        existingNodes.add(new DataNode.AddNode(30, 40, 120, 130, true));
        FileHeader.HeaderDescriptor inIndices = new FileHeader.HeaderDescriptor(existingNodes,
                new FileHeader.HeaderDescriptorData(90, 130));

        // Simulate removing content at the beginning of the file
        FileChangeEventInput eventInput = createAdd(0, 5);
        FileHeader.HeaderDescriptor expectedOutput = new FileHeader.HeaderDescriptor(
                List.of(
                        new DataNode.AddNode(0, 5, 130, 135, true),
                        new DataNode.AddNode(5, 15, 90, 100, true),
                        new DataNode.AddNode(15, 25, 100, 110, true),
                                new DataNode.AddNode(25, 35, 110, 120, true),
                        new DataNode.AddNode(35, 45, 120, 130, true)
                ),
                new FileHeader.HeaderDescriptorData(90, 135));

        // Call the method and assert the result
        var actualOutput = dataNodeOperations.doChangeNode(inIndices, eventInput).get().headerDescriptor();

        assertEqualsValue(expectedOutput.inIndices(), actualOutput.inIndices(), true);
    }

    @Test
    public void testAddContentAtBeginningAll() {
        // Create initial state with some existing nodes
        IntStream.range(2, 100).boxed().filter(i -> i == 10).forEach(i -> {
            List<DataNode> existingNodes = new ArrayList<>();
            existingNodes.add(new DataNode.AddNode(0, 10, 90, 100, true));
            existingNodes.add(new DataNode.AddNode(10, 20, 100, 110, true));
            existingNodes.add(new DataNode.AddNode(20, 30, 110, 120, true));
            existingNodes.add(new DataNode.AddNode(30, 40, 120, 130, true));
            FileHeader.HeaderDescriptor inIndices = new FileHeader.HeaderDescriptor(existingNodes,
                    new FileHeader.HeaderDescriptorData(90, 130));

            // Simulate removing content at the beginning of the file
            FileChangeEventInput eventInput = createAdd(0, i);
            FileHeader.HeaderDescriptor expectedOutput = new FileHeader.HeaderDescriptor(
                    List.of(
                            new DataNode.AddNode(0, i, 130, 130 + i, true),
                            new DataNode.AddNode(i, i + 10, 90, 100, true),
                            new DataNode.AddNode(i + 10, i + 20, 110, 120, true),
                            new DataNode.AddNode( i + 20,  i + 30, 110, 120, true),
                            new DataNode.AddNode(i + 30, i + 40, 120, 130, true)
                    ),
                    new FileHeader.HeaderDescriptorData(90, 130 + i));

            // Call the method and assert the result
            var actualOutput = dataNodeOperations.doChangeNode(inIndices, eventInput).get().headerDescriptor();

            assertEqualsValue(expectedOutput.inIndices(), actualOutput.inIndices(), true);
        });
    }



    @Test
    public void testAddContentInMiddle() {
        // Create initial state with some existing nodes
        List<DataNode> existingNodes = new ArrayList<>();
        existingNodes.add(new DataNode.AddNode(0, 10, 90, 100, true));
        existingNodes.add(new DataNode.AddNode(10, 30, 100, 120, true));
        existingNodes.add(new DataNode.AddNode(30, 40, 120, 130, true));
        FileHeader.HeaderDescriptor inIndices = new FileHeader.HeaderDescriptor(existingNodes,
                new FileHeader.HeaderDescriptorData(90, 130));

        // Simulate removing content in the middle of the file
        FileChangeEventInput eventInput = createAdd(15, 10);
        FileHeader.HeaderDescriptor expectedOutput = new FileHeader.HeaderDescriptor(
                List.of(
                        new DataNode.AddNode(0, 10, 90, 100, true),
                        new DataNode.AddNode(10, 15, 100, 105, true),
                        new DataNode.AddNode(15, 25, 130, 140, true),
                        new DataNode.AddNode(25, 40, 105, 120, true),
                        new DataNode.AddNode(40, 50, 120, 130, true)
                ),
                new FileHeader.HeaderDescriptorData(90, 140));

        // Call the method and assert the result
        FileHeader.HeaderDescriptor actualOutput = dataNodeOperations.doChangeNode(inIndices, eventInput).get().headerDescriptor();
        assertEqualsValue(expectedOutput.inIndices(), actualOutput.inIndices(), true);
    }

    @Test
    public void testAddContentInMiddleBigger() {
        // Create initial state with some existing nodes
        List<DataNode> existingNodes = new ArrayList<>();
        existingNodes.add(new DataNode.AddNode(0, 10, 90, 100, true));
        existingNodes.add(new DataNode.AddNode(10, 30, 100, 120, true));
        existingNodes.add(new DataNode.AddNode(30, 40, 120, 130, true));
        FileHeader.HeaderDescriptor inIndices = new FileHeader.HeaderDescriptor(existingNodes,
                new FileHeader.HeaderDescriptorData(90, 130));

        // Simulate removing content in the middle of the file
        FileChangeEventInput eventInput = createAdd(15, 20);
        FileHeader.HeaderDescriptor expectedOutput = new FileHeader.HeaderDescriptor(
                List.of(
                        new DataNode.AddNode(0, 10, 90, 100, true),
                        new DataNode.AddNode(10, 15, 100, 105, true),
                        new DataNode.AddNode(15, 35, 130, 150, true),
                        new DataNode.AddNode(35, 50, 105, 120, true),
                        new DataNode.AddNode(50, 60, 120, 130, true)
                ),
                new FileHeader.HeaderDescriptorData(90, 150));

        // Call the method and assert the result
        FileHeader.HeaderDescriptor actualOutput = dataNodeOperations.doChangeNode(inIndices, eventInput).get().headerDescriptor();
        assertEqualsValue(expectedOutput.inIndices(), actualOutput.inIndices(), true);
    }

    @Test
    public void testAddContentInMiddleBiggest() {
        // Create initial state with some existing nodes
        IntStream.range(2, 100).forEach(i -> {
            List<DataNode> existingNodes = new ArrayList<>();
            existingNodes.add(new DataNode.AddNode(0, 10, 90, 100, true));
            existingNodes.add(new DataNode.AddNode(10, 30, 100, 120, true));
            existingNodes.add(new DataNode.AddNode(30, 40, 120, 130, true));
            FileHeader.HeaderDescriptor inIndices = new FileHeader.HeaderDescriptor(existingNodes,
                    new FileHeader.HeaderDescriptorData(90, 130));

            // Simulate removing content in the middle of the file
            FileChangeEventInput eventInput = createAdd(15, i);
            FileHeader.HeaderDescriptor expectedOutput = new FileHeader.HeaderDescriptor(
                    Lists.newArrayList(
                    ),
                    new FileHeader.HeaderDescriptorData(0, 30));
            expectedOutput.inIndices().add(new DataNode.AddNode(0, 10, 90, 100, true));
            expectedOutput.inIndices().add(new DataNode.AddNode(10, 15, 100, 105, true));
            expectedOutput.inIndices().add(new DataNode.AddNode(15, 15 + i, 130, 130 + i, true));
            expectedOutput.inIndices().add(new DataNode.AddNode(15 + i, 30 + i, 105, 120, true));
            expectedOutput.inIndices().add(new DataNode.AddNode(30 + i, 40 + i, 120, 130, true));
            // Call the method and assert the result
            FileHeader.HeaderDescriptor actualOutput = dataNodeOperations.doChangeNode(inIndices, eventInput).get().headerDescriptor();
            assertEqualsValue(expectedOutput.inIndices(), actualOutput.inIndices(), true);
        });
    }

    @Test
    public void testRemoveContentRemovalInMiddleBiggest() {
        // Create initial state with some existing nodes
        List<DataNode> existingNodes = new ArrayList<>();
        existingNodes.add(new DataNode.AddNode(0, 10, 0, 10, true));
        existingNodes.add(new DataNode.AddNode(10, 30, 10, 30, true));
        existingNodes.add(new DataNode.AddNode(30, 40, 30, 40, true));
        FileHeader.HeaderDescriptor inIndices = new FileHeader.HeaderDescriptor(existingNodes,
                new FileHeader.HeaderDescriptorData(0, 40));

        // Simulate removing content in the middle of the file
        FileChangeEventInput eventInput = createRemove(15, 5);
        FileHeader.HeaderDescriptor expectedOutput = new FileHeader.HeaderDescriptor(
                Lists.newArrayList(
                        new DataNode.AddNode(0, 10, 0, 10, true),
                        new DataNode.AddNode(10, 15, 10, 15, true),
                        new DataNode.AddNode(15, 25 , 20, 30, true),
                        new DataNode.AddNode(25 , 35 , 30, 40, true)
                ),
                new FileHeader.HeaderDescriptorData(0, 30)
        );
        // Call the method and assert the result
        FileHeader.HeaderDescriptor actualOutput = dataNodeOperations.doChangeNode(inIndices, eventInput).get().headerDescriptor();
        assertEqualsValue(expectedOutput.inIndices(), actualOutput.inIndices());

        existingNodes = new ArrayList<>();
        existingNodes.add(new DataNode.AddNode(0, 10, 0, 10, true));
        existingNodes.add(new DataNode.AddNode(10, 30, 10, 30, true));
        existingNodes.add(new DataNode.AddNode(30, 40, 30, 40, true));
        inIndices = new FileHeader.HeaderDescriptor(existingNodes,
                new FileHeader.HeaderDescriptorData(0, 40));

        // Simulate removing content in the middle of the file
        eventInput = createRemove(3, 5);
        expectedOutput = new FileHeader.HeaderDescriptor(
                Lists.newArrayList(
                        new DataNode.AddNode(0, 3, 0, 3, true),
                        new DataNode.AddNode(3, 5, 8, 10, true),
                        new DataNode.AddNode(5, 25, 10, 30, true),
                        new DataNode.AddNode(25, 35 , 30, 40, true)
                ),
                new FileHeader.HeaderDescriptorData(0, 30));
        // Call the method and assert the result
        actualOutput = dataNodeOperations.doChangeNode(inIndices, eventInput).get().headerDescriptor();
        assertEqualsValue(expectedOutput.inIndices(), actualOutput.inIndices());


        existingNodes = new ArrayList<>();
        existingNodes.add(new DataNode.AddNode(0, 10, 0, 10, true));
        existingNodes.add(new DataNode.AddNode(10, 30, 10, 30, true));
        existingNodes.add(new DataNode.AddNode(30, 40, 30, 40, true));
        inIndices = new FileHeader.HeaderDescriptor(existingNodes,
                new FileHeader.HeaderDescriptorData(0, 40));

        // Simulate removing content in the middle of the file
        eventInput = createRemove(30, 10);
        expectedOutput = new FileHeader.HeaderDescriptor(
                Lists.newArrayList(
                        new DataNode.AddNode(0, 10, 0, 10, true),
                        new DataNode.AddNode(10, 30, 10, 30, true)
                ),
                new FileHeader.HeaderDescriptorData(0, 30));
        // Call the method and assert the result
        actualOutput = dataNodeOperations.doChangeNode(inIndices, eventInput).get().headerDescriptor();
        assertEqualsValue(expectedOutput.inIndices(), actualOutput.inIndices());

        existingNodes = new ArrayList<>();
        existingNodes.add(new DataNode.AddNode(0, 10, 0, 10, true));
        existingNodes.add(new DataNode.AddNode(10, 30, 10, 30, true));
        existingNodes.add(new DataNode.AddNode(30, 40, 30, 40, true));

        // Simulate removing content in the middle of the file
        eventInput = createRemove(30, 4);
        expectedOutput = new FileHeader.HeaderDescriptor(
                Lists.newArrayList(
                        new DataNode.AddNode(0, 10, 0, 10, true),
                        new DataNode.AddNode(10, 30, 10, 30, true),
                        new DataNode.AddNode(30, 36, 34, 40, true)
                ),
                new FileHeader.HeaderDescriptorData(0, 30));
        // Call the method and assert the result
        actualOutput = dataNodeOperations.doChangeNode(inIndices, eventInput).get().headerDescriptor();

        assertEqualsValue(expectedOutput.inIndices(), actualOutput.inIndices());
    }

    @Test
    public void testRemoveContentRemovalInMiddleBigger() {
        // Create initial state with some existing nodes
        List<DataNode> existingNodes = new ArrayList<>();
        existingNodes.add(new DataNode.AddNode(0, 10, 0, 10, true));
        existingNodes.add(new DataNode.AddNode(10, 30, 10, 30, true));
        existingNodes.add(new DataNode.AddNode(30, 40, 30, 40, true));
        FileHeader.HeaderDescriptor inIndices = new FileHeader.HeaderDescriptor(existingNodes,
                new FileHeader.HeaderDescriptorData(0, 40));

        // Simulate removing content in the middle of the file
        FileChangeEventInput eventInput = createRemove(5, 20);
        FileHeader.HeaderDescriptor expectedOutput = new FileHeader.HeaderDescriptor(
                Lists.newArrayList(
                        new DataNode.AddNode(0, 5, 0, 5, true),
                        new DataNode.AddNode(5, 10, 25, 30, true),
                        new DataNode.AddNode(10, 20 , 30, 40, true)
                ),
                new FileHeader.HeaderDescriptorData(0, 30));
        // Call the method and assert the result
        FileHeader.HeaderDescriptor actualOutput = dataNodeOperations.doChangeNode(inIndices, eventInput).get().headerDescriptor();
        assertEqualsValue(expectedOutput.inIndices(), actualOutput.inIndices());
    }

    @Test
    public void testRemoveContentRemovalInMiddleBiggestRemove() {
        // Create initial state with some existing nodes
        List<DataNode> existingNodes = new ArrayList<>();
        existingNodes.add(new DataNode.AddNode(0, 10, 0, 10, true));
        existingNodes.add(new DataNode.AddNode(10, 30, 10, 30, true));
        existingNodes.add(new DataNode.AddNode(30, 40, 30, 40, true));
        FileHeader.HeaderDescriptor inIndices = new FileHeader.HeaderDescriptor(existingNodes,
                new FileHeader.HeaderDescriptorData(0, 40));

        // Simulate removing content in the middle of the file
        FileChangeEventInput eventInput = createRemove(7, 30);
        FileHeader.HeaderDescriptor expectedOutput = new FileHeader.HeaderDescriptor(
                Lists.newArrayList(
                        new DataNode.AddNode(0, 7, 0, 7, true),
                        new DataNode.AddNode(7, 10, 37, 40, true)
                ),
                new FileHeader.HeaderDescriptorData(0, 30));
        // Call the method and assert the result
        FileHeader.HeaderDescriptor actualOutput = dataNodeOperations.doChangeNode(inIndices, eventInput).get().headerDescriptor();
        assertEqualsValue(expectedOutput.inIndices(), actualOutput.inIndices());
    }

    @Test
    public void testRemoveContentRemovalInMiddleSmallerRemove() {
        // Create initial state with some existing nodes
        testRemoveRightFlushNonOverlapLeft();
        testRemoveStartDataNonRightFlush();
        testRemoveMiddleNonRightFlushNonLeftFlushNonOverlap();
        testRemoveLeftFlushOverlapRight();
        testRemoveRightFlushOverlapLeft();
        testRemoveAllOverlap();
        testRemoveRightFlushLeftFlush();
        testRemoveLeftFlushNonOverlapRight();
    }

    private void testRemoveRightFlushLeftFlush() {
        FileHeader.HeaderDescriptor inIndices;
        FileHeader.HeaderDescriptor expectedOutput;
        FileHeader.HeaderDescriptor actualOutput;
        FileChangeEventInput eventInput;
        List<DataNode> existingNodes;
        existingNodes = new ArrayList<DataNode>();
        existingNodes.add(new DataNode.AddNode(0, 10, 0, 10, true));
        existingNodes.add(new DataNode.AddNode(10, 30, 10, 30, true));
        existingNodes.add(new DataNode.AddNode(30, 40, 30, 40, true));
        inIndices = new FileHeader.HeaderDescriptor(existingNodes,
                new FileHeader.HeaderDescriptorData(10, 40));

        // Simulate removing content in the middle of the file
        eventInput = createRemove(10, 20);
        expectedOutput = new FileHeader.HeaderDescriptor(
                Lists.newArrayList(
                        new DataNode.AddNode(0, 10, 0, 10, true),
                        new DataNode.AddNode(10, 20, 30, 40, true)
                ),
                new FileHeader.HeaderDescriptorData(0, 40));
        // Call the method and assert the result
        actualOutput = dataNodeOperations.doChangeNode(inIndices, eventInput).get().headerDescriptor();
        assertEqualsValue(expectedOutput.inIndices(), actualOutput.inIndices());
    }

    private void testRemoveAllOverlap() {
        FileHeader.HeaderDescriptor inIndices;
        FileHeader.HeaderDescriptor expectedOutput;
        FileHeader.HeaderDescriptor actualOutput;
        FileChangeEventInput eventInput;
        List<DataNode> existingNodes;
        existingNodes = new ArrayList<DataNode>();
        existingNodes.add(new DataNode.AddNode(0, 10, 0, 10, true));
        existingNodes.add(new DataNode.AddNode(10, 30, 10, 30, true));
        existingNodes.add(new DataNode.AddNode(30, 40, 30, 40, true));
        inIndices = new FileHeader.HeaderDescriptor(existingNodes,
                new FileHeader.HeaderDescriptorData(10, 40));

        // Simulate removing content in the middle of the file
        eventInput = createRemove(8, 30);
        expectedOutput = new FileHeader.HeaderDescriptor(
                Lists.newArrayList(
                        new DataNode.AddNode(0, 8, 0, 8, true),
                        new DataNode.AddNode(8, 10, 38, 40, true)
                ),
                new FileHeader.HeaderDescriptorData(0, 40));
        // Call the method and assert the result
        actualOutput = dataNodeOperations.doChangeNode(inIndices, eventInput).get().headerDescriptor();
        assertEqualsValue(expectedOutput.inIndices(), actualOutput.inIndices());
    }

    private void testRemoveRightFlushOverlapLeft() {
        List<DataNode> existingNodes;
        FileHeader.HeaderDescriptor inIndices;
        FileHeader.HeaderDescriptor actualOutput;
        FileChangeEventInput eventInput;
        FileHeader.HeaderDescriptor expectedOutput;
        existingNodes = new ArrayList<DataNode>();
        existingNodes.add(new DataNode.AddNode(0, 10, 0, 10, true));
        existingNodes.add(new DataNode.AddNode(10, 30, 10, 30, true));
        existingNodes.add(new DataNode.AddNode(30, 40, 30, 40, true));
        inIndices = new FileHeader.HeaderDescriptor(existingNodes,
                new FileHeader.HeaderDescriptorData(10, 40));

        // Simulate removing content in the middle of the file
        eventInput = createRemove(8, 22);
        expectedOutput = new FileHeader.HeaderDescriptor(
                Lists.newArrayList(
                        new DataNode.AddNode(0, 8, 0, 8, true),
                        new DataNode.AddNode(8, 18, 30, 40, true)
                ),
                new FileHeader.HeaderDescriptorData(0, 40));
        // Call the method and assert the result
        actualOutput = dataNodeOperations.doChangeNode(inIndices, eventInput).get().headerDescriptor();
        assertEqualsValue(expectedOutput.inIndices(), actualOutput.inIndices());
    }

    private void testRemoveLeftFlushOverlapRight() {
        FileChangeEventInput eventInput;
        FileHeader.HeaderDescriptor inIndices;
        FileHeader.HeaderDescriptor actualOutput;
        List<DataNode> existingNodes;
        FileHeader.HeaderDescriptor expectedOutput;
        existingNodes = new ArrayList<DataNode>();
        existingNodes.add(new DataNode.AddNode(0, 10, 0, 10, true));
        existingNodes.add(new DataNode.AddNode(10, 30, 10, 30, true));
        existingNodes.add(new DataNode.AddNode(30, 40, 30, 40, true));
        inIndices = new FileHeader.HeaderDescriptor(existingNodes,
                new FileHeader.HeaderDescriptorData(10, 40));

        // Simulate removing content in the middle of the file
        eventInput = createRemove(10, 22);
        expectedOutput = new FileHeader.HeaderDescriptor(
                Lists.newArrayList(
                        new DataNode.AddNode(0, 10, 0, 10, true),
                        new DataNode.AddNode(10, 18, 32, 40, true)
                ),
                new FileHeader.HeaderDescriptorData(0, 40));
        // Call the method and assert the result
        actualOutput = dataNodeOperations.doChangeNode(inIndices, eventInput).get().headerDescriptor();
        assertEqualsValue(expectedOutput.inIndices(), actualOutput.inIndices());
    }

    private void testRemoveMiddleNonRightFlushNonLeftFlushNonOverlap() {
        List<DataNode> existingNodes;
        FileHeader.HeaderDescriptor inIndices;
        FileHeader.HeaderDescriptor expectedOutput;
        FileChangeEventInput eventInput;
        FileHeader.HeaderDescriptor actualOutput;
        existingNodes = new ArrayList<DataNode>();
        existingNodes.add(new DataNode.AddNode(0, 10, 0, 10, true));
        existingNodes.add(new DataNode.AddNode(10, 30, 10, 30, true));
        existingNodes.add(new DataNode.AddNode(30, 40, 30, 40, true));
        inIndices = new FileHeader.HeaderDescriptor(existingNodes,
                new FileHeader.HeaderDescriptorData(0, 5));

        // Simulate removing content in the middle of the file
        eventInput = createRemove(35, 3);
        expectedOutput = new FileHeader.HeaderDescriptor(
                Lists.newArrayList(
                        new DataNode.AddNode(0, 10, 0, 10, true),
                        new DataNode.AddNode(10, 30, 10, 30, true),
                        new DataNode.AddNode(30, 35, 30, 35, true),
                        new DataNode.AddNode(35, 37, 38, 40, true)
                ),
                new FileHeader.HeaderDescriptorData(0, 30));
        // Call the method and assert the result
        actualOutput = dataNodeOperations.doChangeNode(inIndices, eventInput).get().headerDescriptor();
        assertEqualsValue(expectedOutput.inIndices(), actualOutput.inIndices(), true);
    }

    private void testRemoveStartDataNonRightFlush() {
        List<DataNode> existingNodes;
        FileHeader.HeaderDescriptor expectedOutput;
        FileChangeEventInput eventInput;
        FileHeader.HeaderDescriptor inIndices;
        FileHeader.HeaderDescriptor actualOutput;
        existingNodes = new ArrayList<DataNode>();
        existingNodes.add(new DataNode.AddNode(0, 10, 0, 10, true));
        existingNodes.add(new DataNode.AddNode(10, 30, 10, 30, true));
        existingNodes.add(new DataNode.AddNode(30, 40, 30, 40, true));
        inIndices = new FileHeader.HeaderDescriptor(existingNodes,
                new FileHeader.HeaderDescriptorData(0, 5));

        // Simulate removing content in the middle of the file
        eventInput = createRemove(30, 3);
        expectedOutput = new FileHeader.HeaderDescriptor(
                Lists.newArrayList(
                        new DataNode.AddNode(0, 10, 0, 10, true),
                        new DataNode.AddNode(10, 30, 10, 30, true),
                        new DataNode.AddNode(30, 37, 33, 40, true)
                ),
                new FileHeader.HeaderDescriptorData(0, 30));
        // Call the method and assert the result
        actualOutput = dataNodeOperations.doChangeNode(inIndices, eventInput).get().headerDescriptor();
        assertEqualsValue(expectedOutput.inIndices(), actualOutput.inIndices());
    }

    private void testRemoveLeftFlushNonOverlapRight() {
        List<DataNode> existingNodes = new ArrayList<>();
        existingNodes.add(new DataNode.AddNode(0, 10, 0, 10, true));
        existingNodes.add(new DataNode.AddNode(10, 30, 10, 30, true));
        existingNodes.add(new DataNode.AddNode(30, 40, 30, 40, true));
        FileHeader.HeaderDescriptor inIndices = new FileHeader.HeaderDescriptor(existingNodes,
                new FileHeader.HeaderDescriptorData(0, 40));

        // Simulate removing content in the middle of the file
        FileChangeEventInput eventInput = createRemove(10, 5);
        FileHeader.HeaderDescriptor expectedOutput = new FileHeader.HeaderDescriptor(
                Lists.newArrayList(
                        new DataNode.AddNode(0, 10, 0, 10, true),
                        new DataNode.AddNode(10, 25, 15, 30, true),
                        new DataNode.AddNode(25, 35, 30, 40, true)
                ),
                new FileHeader.HeaderDescriptorData(0, 40));

        // Call the method and assert the result
        FileHeader.HeaderDescriptor actualOutput = dataNodeOperations.doChangeNode(inIndices, eventInput).get().headerDescriptor();
        assertEqualsValue(expectedOutput.inIndices(), actualOutput.inIndices());
    }

    private void testRemoveRightFlushNonOverlapLeft() {
        List<DataNode> existingNodes = new ArrayList<>();
        existingNodes.add(new DataNode.AddNode(0, 10, 0, 10, true));
        existingNodes.add(new DataNode.AddNode(10, 30, 10, 30, true));
        existingNodes.add(new DataNode.AddNode(30, 40, 30, 40, true));
        FileHeader.HeaderDescriptor inIndices = new FileHeader.HeaderDescriptor(existingNodes,
                new FileHeader.HeaderDescriptorData(0, 5));

        // Simulate removing content in the middle of the file
        FileChangeEventInput eventInput = createRemove(7, 3);
        FileHeader.HeaderDescriptor expectedOutput = new FileHeader.HeaderDescriptor(
                Lists.newArrayList(
                        new DataNode.AddNode(0, 7, 0, 7, true),
                        new DataNode.AddNode(7, 27, 10, 30, true),
                        new DataNode.AddNode(27, 37, 30, 40, true)
                ),
                new FileHeader.HeaderDescriptorData(0, 30));

        // Call the method and assert the result
        FileHeader.HeaderDescriptor actualOutput = dataNodeOperations.doChangeNode(inIndices, eventInput).get().headerDescriptor();
        assertEqualsValue(expectedOutput.inIndices(), actualOutput.inIndices());
    }

    @Test
    public void testAddContentInMiddleSmaller() {
        // Create initial state with some existing nodes
        List<DataNode> existingNodes = new ArrayList<>();
        existingNodes.add(new DataNode.AddNode(0, 10, 90, 100, true));
        existingNodes.add(new DataNode.AddNode(10, 20, 100, 110, true));
        existingNodes.add(new DataNode.AddNode(20, 30, 110, 120, true));
        existingNodes.add(new DataNode.AddNode(30, 40, 120, 130, true));
        FileHeader.HeaderDescriptor inIndices = new FileHeader.HeaderDescriptor(existingNodes,
                new FileHeader.HeaderDescriptorData(90, 130));

        // Simulate removing content in the middle of the file
        FileChangeEventInput eventInput = createAdd(15, 5);
        FileHeader.HeaderDescriptor expectedOutput = new FileHeader.HeaderDescriptor(
                List.of(
                        new DataNode.AddNode(0, 10, 90, 100, true),
                        new DataNode.AddNode(10, 15, 100, 105, true),
                        new DataNode.AddNode(15, 20, 130, 135, true),
                        new DataNode.AddNode(20, 25, 105, 110, true),
                        new DataNode.AddNode(25, 35, 110, 120, true),
                        new DataNode.AddNode(35, 45, 120, 130, true)
                ),
                new FileHeader.HeaderDescriptorData(90, 135));

        // Call the method and assert the result
        FileHeader.HeaderDescriptor actualOutput = dataNodeOperations.doChangeNode(inIndices, eventInput).get().headerDescriptor();
        assertEqualsValue(expectedOutput.inIndices(), actualOutput.inIndices(), true);
    }

    @Test
    public void testAddContentInMiddleSmallest() {
        // Create initial state with some existing nodes
        List<DataNode> existingNodes = new ArrayList<>();
        existingNodes.add(new DataNode.AddNode(0, 10, 90, 100, true));
        existingNodes.add(new DataNode.AddNode(10, 30, 100, 130, true));
        existingNodes.add(new DataNode.AddNode(30, 40, 130, 140, true));
        FileHeader.HeaderDescriptor inIndices = new FileHeader.HeaderDescriptor(existingNodes,
                new FileHeader.HeaderDescriptorData(90, 140));

        // Simulate removing content in the middle of the file
        FileChangeEventInput eventInput = createAdd(15, 3);
        FileHeader.HeaderDescriptor expectedOutput = new FileHeader.HeaderDescriptor(
                List.of(
                        new DataNode.AddNode(0, 10, 90, 100, true),
                        new DataNode.AddNode(10, 15, 100, 105, true),
                        new DataNode.AddNode(15, 18, 140, 143, true),
                        new DataNode.AddNode(18, 33, 105, 130, true),
                        new DataNode.AddNode(33, 43, 130, 140, true)
                ),
                new FileHeader.HeaderDescriptorData(90, 143));

        // Call the method and assert the result
        FileHeader.HeaderDescriptor actualOutput = dataNodeOperations.doChangeNode(inIndices, eventInput).get().headerDescriptor();
        assertEqualsValue(expectedOutput.inIndices(), actualOutput.inIndices(), true);
    }

    public void assertEqualsValue(List<DataNode> first, List<DataNode> second) {
        assertEqualsValue(first, second, true);
    }

    public void assertEqualsValue(List<DataNode> first, List<DataNode> second, boolean assertDataIndices) {
        String printIndex = "%s vs %s".formatted(
                first.stream().map(d -> Map.entry(d.indexStart(), d.indexEnd())).toList(),
                second.stream().map(d -> Map.entry(d.indexStart(), d.indexEnd())).toList());
        String printDataIndex = "%s vs %s".formatted(
                first.stream().map(d -> Map.entry(d.dataStart(), d.dataEnd())).toList(),
                second.stream().map(d -> Map.entry(d.dataStart(), d.dataEnd())).toList());

        System.out.println(printIndex);
        System.out.println(printDataIndex);

        assertEquals(first.size(), second.size(), printIndex);
        boolean condition = first.stream().allMatch(d -> second.stream().anyMatch(d1 -> d.indexStart() == d1.indexStart() && d.indexEnd() == d1.indexEnd()));

        assertTrue(condition, printIndex);

        if (assertDataIndices) {
            condition = first.stream().allMatch(d -> second.stream().anyMatch(d1 -> d.dataStart() == d1.dataStart() && d.dataEnd() == d1.dataEnd()));
            assertTrue(condition, printDataIndex);
        }
    }
}
package com.hayden.fileservice.filesource;

import com.hayden.fileservice.codegen.types.FileChangeEventInput;
import com.hayden.fileservice.codegen.types.FileChangeType;
import com.hayden.fileservice.codegen.types.FileSearch;
import com.hayden.fileservice.config.ByteArray;
import com.hayden.fileservice.config.FileProperties;
import com.hayden.fileservice.filesource.directoryoperations.LocalDirectoryOperations;
import com.hayden.fileservice.filesource.fileoperations.naive.LocalNaiveFileOperations;
import com.hayden.fileservice.io.FileStream;
import com.hayden.utilitymodule.io.FileUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.test.StepVerifier;
import reactor.core.publisher.Flux;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest(classes = {LocalNaiveFileOperations.class, FileStream.class, FileProperties.class, LocalDirectoryOperations.class})
@ExtendWith(SpringExtension.class)
@ActiveProfiles("naive")
@EnableConfigurationProperties(FileProperties.class)
class LocalNaiveFileOperationsTest {

    @Autowired
    private LocalNaiveFileOperations localNaiveFileOperations;

    @BeforeAll
    public static void setUp() {
        new File("work/test_dir").mkdirs();
        new File("work/metadata_test_dir").mkdirs();
    }

    @AfterAll
    public static void after() {
        FileUtils.deleteFilesRecursive(new File("work").toPath());
    }

    @Test
    void createFile() {
        localNaiveFileOperations.createFile(
                fileChangeEvent(FileChangeType.CREATED, "out.txt", -1, 0, "hello")
        );

        File testFile = new File("work/out.txt");

        assertThat(testFile.exists()).isTrue();
        assertThat(new String(Assertions.assertDoesNotThrow(() -> Files.readAllBytes(testFile.toPath())))).isEqualTo("hello");
        assertThat(testFile.delete()).isTrue();
    }

    @Test
    void deleteFile() {
        File testFile = new File("work/to_delete.txt");
        Assertions.assertDoesNotThrow(testFile::createNewFile);
        localNaiveFileOperations.deleteFile(fileChangeEvent(FileChangeType.DELETED, "to_delete.txt", -1, 0));
        assertThat(testFile.exists()).isFalse();
    }

    @Test
    void addContent() {
        File testFile = new File("work/to_add_content_to.txt");
        Assertions.assertDoesNotThrow(testFile::createNewFile);
        assertThat(testFile.exists()).isTrue();
        localNaiveFileOperations.addContent(fileChangeEvent(FileChangeType.ADD_CONTENT, "to_add_content_to.txt", -1, 0, "goodbye"));
        assertThat(new String(Assertions.assertDoesNotThrow(() -> Files.readAllBytes(testFile.toPath())))).isEqualTo("goodbye");
    }

    @Test
    void removeContentFromBeginning() {
        File testFile = createTestFile("work/to_remove_content_from_beginning.txt");
        localNaiveFileOperations.removeContent(fileChangeEvent(FileChangeType.ADD_CONTENT, "to_remove_content_from_beginning.txt", 3, 0));
        assertThat(new String(Assertions.assertDoesNotThrow(() -> Files.readAllBytes(testFile.toPath())))).isEqualTo("lo there");
    }

    @Test
    void removeContentFromEnd() {
        File testFile = createTestFile("work/to_remove_content_from_end.txt");
        localNaiveFileOperations.removeContent(fileChangeEvent(FileChangeType.ADD_CONTENT, "to_remove_content_from_end.txt", 3, 8));
        assertThat(new String(Assertions.assertDoesNotThrow(() -> Files.readAllBytes(testFile.toPath())))).isEqualTo("hello th");
    }

    @Test
    void removeContentFromBeginningMultiplePass() {
        File testFile = createTestFile("work/to_remove_content_from_beginning_multiple_pass.txt");
        localNaiveFileOperations.removeContent(fileChangeEvent(FileChangeType.ADD_CONTENT, "to_remove_content_from_beginning_multiple_pass.txt", 3, 0), testFile, 2);
        assertThat(new String(Assertions.assertDoesNotThrow(() -> Files.readAllBytes(testFile.toPath())))).isEqualTo("lo there");
    }

    @Test
    void removeContentFromEndMultiplePass() {
        File testFile = createTestFile("work/to_remove_content_from_end_multiple_pass.txt");
        localNaiveFileOperations.removeContent(fileChangeEvent(FileChangeType.ADD_CONTENT, "to_remove_content_from_end_multiple_pass.txt", 3, 8), testFile, 2);
        assertThat(new String(Assertions.assertDoesNotThrow(() -> Files.readAllBytes(testFile.toPath())))).isEqualTo("hello th");
    }

    @Test
    void removeContentFromMiddleMultiplePass() {
        File testFile = createTestFile("work/to_remove_content_from_middle_multiple_pass.txt");
        localNaiveFileOperations.removeContent(fileChangeEvent(FileChangeType.ADD_CONTENT, "to_remove_content_from_middle_multiple_pass.txt", 3, 4), testFile, 2);
        assertThat(new String(Assertions.assertDoesNotThrow(() -> Files.readAllBytes(testFile.toPath())))).isEqualTo("hellhere");
    }

    @Test
    void removeContentFromMiddle() {
        File testFile = createTestFile("work/to_remove_content_from_middle.txt");
        localNaiveFileOperations.removeContent(fileChangeEvent(FileChangeType.ADD_CONTENT, "to_remove_content_from_middle.txt", 3, 4));
        assertThat(new String(Assertions.assertDoesNotThrow(() -> Files.readAllBytes(testFile.toPath())))).isEqualTo("hellhere");
    }

    @Test
    void getFile() {
        File testFile = createTestFile("work/to_search_for.txt");
        StepVerifier.create(localNaiveFileOperations.getFile(fileSearchEvent("to_search_for.txt", "work")))
                .assertNext(r -> assertThat(new String(Assertions.assertDoesNotThrow(() -> Files.readAllBytes(testFile.toPath())))).isEqualTo("hello there"))
                .expectComplete()
                .verify();

    }

    @Test
    void search() {
        IntStream.range(0, 20).boxed().map("work/test_dir/to_search_%s.txt"::formatted)
                .forEach(LocalNaiveFileOperationsTest::createTestFile);

        List<File> foundSearched = localNaiveFileOperations.search("work/test_dir").toList();
        assertThat(foundSearched).hasSize(20);
    }

    @Test
    void getMetadata() {
        IntStream.range(0, 20).boxed().map("work/metadata_test_dir/to_search_metadata_%s.txt"::formatted)
                .forEach(LocalNaiveFileOperationsTest::createTestFile);

        StepVerifier.create(Flux.from(localNaiveFileOperations.getMetadata(fileSearchEvent("work/metadata_test_dir"))).collectList())
                .assertNext(l -> assertThat(l).hasSize(20))
                .expectComplete()
                .verify();
    }

    private static @NotNull File createTestFile(String pathname) {
        File testFile = new File(pathname);
        Assertions.assertDoesNotThrow(testFile::createNewFile);
        FileUtils.writeToFile("hello there", testFile.toPath());
        assertThat(testFile.exists()).isTrue();
        return testFile;
    }

    private static @NotNull FileSearch fileSearchEvent(String fileName, String path) {
        return FileSearch.newBuilder()
                .fileId("test")
                .fileName(fileName)
                .path(path)
                .build();
    }

    private static @NotNull FileSearch fileSearchEvent(String path) {
        return fileSearchEvent(null, path);
    }

    private static @NotNull FileChangeEventInput fileChangeEvent(FileChangeType changeType, String fileName, int length, int offset) {
        return fileChangeEvent(changeType, fileName, length, offset, null);
    }

    private static @NotNull FileChangeEventInput fileChangeEvent(FileChangeType changeType, String fileName, int length, int offset, @Nullable String hello) {
        return new FileChangeEventInput(
                "id", changeType, offset,
                Optional.ofNullable(hello).map(String::getBytes).map(ByteArray::new).orElse(null),
                Path.of("work", fileName).toFile().getPath(),
                length);
    }


}
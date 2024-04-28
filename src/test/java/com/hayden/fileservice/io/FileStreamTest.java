package com.hayden.fileservice.io;

import com.hayden.fileservice.graphql.FileEventSourceActions;
import com.hayden.utilitymodule.result.Result;
import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import java.io.BufferedOutputStream;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;


class FileStreamTest {

    @SneakyThrows
    @Test
    public void testReadChunks() {
        Path tempFile = Files.createTempFile("test1", "test1");
        try(
                var fos = FileUtils.openOutputStream(tempFile.toFile());
                var bos = new BufferedOutputStream(fos)
        ) {
            byte[] helloBytes = "hello".getBytes();
            for (int i = 0; i < 5000; i++) {
                bos.write(helloBytes[i % helloBytes.length]);
            }
        }
        FileStream fileStream = new FileStream();
        StepVerifier.create(fileStream.readFileInChunks(tempFile.toFile().getAbsolutePath(), 1024))
                .assertNext(r -> equalsByteNums(r, 1024))
                .assertNext(r -> equalsByteNums(r, 1024))
                .assertNext(r -> equalsByteNums(r, 1024))
                .assertNext(r -> equalsByteNums(r, 1024))
                .assertNext(r -> equalsByteNums(r, 5000 - (1024 * 4)))
                .verifyComplete();

    }

    private static void equalsByteNums(Result<ByteBuffer, FileEventSourceActions.FileEventError> r, int expected) {
        assertThat(r.isPresent()).isTrue();
        assertThat(r.get().array().length).isEqualTo(expected);
    }

}
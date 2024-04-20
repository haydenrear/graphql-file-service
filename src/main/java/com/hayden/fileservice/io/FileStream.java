package com.hayden.fileservice.io;

import com.hayden.fileservice.graphql.FileEventSourceActions;
import com.hayden.utilitymodule.result.Result;
import org.reactivestreams.Publisher;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.Paths;

@Component
public class FileStream {

    public Publisher<Result<ByteBuffer, FileEventSourceActions.FileEventError>> readFileInChunks(String filePath, int batchSize) {
        Path path = Paths.get(filePath);
        return Flux.create(sink -> readInChunks(sink, path, batchSize));
    }

    private void readInChunks(FluxSink<Result<ByteBuffer, FileEventSourceActions.FileEventError>> sink, Path path, int batchSize) {
        try {
            try (FileChannel channel = FileChannel.open(path)) {
                ByteBuffer buffer = ByteBuffer.allocate(batchSize);
                int bytesRead;
                while ((bytesRead = channel.read(buffer)) != -1) {
                    buffer.flip();
                    sink.next(Result.fromResult(buffer));
                    buffer = ByteBuffer.allocate(batchSize);
                }
            }
            sink.complete();
        } catch (IOException e) {
            sink.next(Result.fromError(new FileEventSourceActions.FileEventError()));
        }
    }

}

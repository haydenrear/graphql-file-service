package com.hayden.fileservice.filechange;

import com.hayden.fileservice.codegen.types.FileChangeEvent;
import com.hayden.shared.communication.DataClientPublisher;
import com.hayden.shared.communication.DataClientSubscriber;
import com.hayden.shared.communication.MessageResult;
import com.hayden.shared.models.messaging.PartitionEndpoint;
import com.hayden.utilitymodule.reflection.TypeReferenceDelegate;
import lombok.RequiredArgsConstructor;
import org.reactivestreams.Publisher;
import org.springframework.stereotype.Component;

import java.util.regex.Pattern;


@Component
@RequiredArgsConstructor
public class FileChangeService {

//    private final DataClientSubscriber dataClientSubscriber;
//    private final DataClientPublisher dataClientPublisher;

    public record FileSyncEndpoint(Pattern topicMatcher)
            implements PartitionEndpoint.MultiNodePartitionEndpoint {

        @Override
        public String partitionId() {
            return "file-sync";
        }
    }

//    public Publisher<FileChangeSync> subscribe(String fileTopic) {
//        return dataClientSubscriber.subscribe(
//                TypeReferenceDelegate.<FileChangeSync>create(FileChangeSync.class).get(),
//                new FileSyncEndpoint(Pattern.compile(fileTopic))
//        );
//    }
//
//    public Publisher<MessageResult> publish(FileChangeEvent fileChangeEvent) {
//        return switch(fileChangeEvent.getChangeType()) {
//            case CREATED -> {
//                yield dataClientPublisher.process(
//                        new FileChangeSync.AddFile(null),
//                        "",
//                        new FileSyncEndpoint(Pattern.compile(""))
//                );
//            }
//            case DELETED -> {
//                yield dataClientPublisher.process(
//                        new FileChangeSync.DeleteFile(""),
//                        "",
//                        new FileSyncEndpoint(Pattern.compile(""))
//                );
//            }
//            case REMOVE_CONTENT -> {
//                yield dataClientPublisher.process(
//                        new FileChangeSync.RemoveContent(""),
//                        "",
//                        new FileSyncEndpoint(Pattern.compile(""))
//                );
//            }
//            case ADD_CONTENT -> {
//                yield dataClientPublisher.process(
//                        new FileChangeSync.AddContent(""),
//                        "",
//                        new FileSyncEndpoint(Pattern.compile(""))
//                );
//            }
//        };
//    }

}

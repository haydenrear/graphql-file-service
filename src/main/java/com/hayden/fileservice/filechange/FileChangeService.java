package com.hayden.fileservice.filechange;

import com.hayden.fileservice.codegen.types.FileChangeEvent;
import com.hayden.fileservice.config.FileSyncProperties;
import com.hayden.shared.communication.DataClientPublisher;
import com.hayden.shared.communication.DataClientSubscriber;
import com.hayden.shared.communication.MessageResult;
import com.hayden.shared.models.messaging.PartitionEndpoint;
import com.hayden.utilitymodule.reflection.TypeReferenceDelegate;
import jakarta.annotation.Nullable;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.reactivestreams.Publisher;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Distribute information about file changes...
 * should be CdcGraphQlDataFetcher... so then this server provides to the Gateway
 * an implementation of CdcGraphQlDataFetcher, then Gateway compiles it on the fly,
 * the Gateway nodes have access to queue, limiting number of subscribers (rather than
 * have number of listeners equivalent to number of services, as this is bad... too many),
 * then the Gateway nodes abstract away how they listen to queue, and the Gateway fields
 * the sync requests but the FileChangeService publishes the changes that it performs.
 */
@Component
@RequiredArgsConstructor
public class FileChangeService {

//    private final DataClientSubscriber dataClientSubscriber;
//    private final DataClientPublisher dataClientPublisher;
    private final FileSyncProperties fileSyncProperties;

//    public record FileSyncEndpoint(Pattern topicMatcher, String partitionId)
//            implements PartitionEndpoint.MultiNodePartitionEndpoint {
//    }
//
//    public Publisher<FileChangeSync> subscribe(@Nullable String fileTopic) {
//        return reactor.core.publisher.Mono.justOrEmpty(TypeReferenceDelegate.<FileChangeSync>create(FileChangeSync.class))
//                .flatMapMany(trd -> reactor.core.publisher.Flux.from(dataClientSubscriber.subscribe(
//                        trd,
//                        Optional.ofNullable(fileTopic).map(this::filePartitionSyncEndpoint)
//                                .orElse(this.fileSyncEndpoint())
//                )));
//    }
//
//    public Publisher<FileChangeSync> subscribe() {
//        return subscribe(null);
//    }

//    public Publisher<MessageResult> publish(FileChangeEvent fileChangeEvent) {
//        return switch(fileChangeEvent.getChangeType()) {
//            case CREATED -> {
//                yield dataClientPublisher.process(
//                        new FileChangeSync.AddFile(fileChangeEvent),
//                        "",
//                        fileSyncEndpoint()
//                );
//            }
//            case DELETED -> {
//                yield dataClientPublisher.process(
//                        new FileChangeSync.DeleteFile(fileChangeEvent),
//                        "",
//                        fileSyncEndpoint()
//                );
//            }
//            case REMOVE_CONTENT -> {
//                yield dataClientPublisher.process(
//                        new FileChangeSync.RemoveContent(fileChangeEvent),
//                        "",
//                        fileSyncEndpoint()
//                );
//            }
//            case ADD_CONTENT -> {
//                yield dataClientPublisher.process(
//                        new FileChangeSync.AddContent(fileChangeEvent),
//                        "",
//                        fileSyncEndpoint()
//                );
//            }
//            case EXISTING ->
//                    reactor.core.publisher.Flux.empty();
//        };
//    }
//
//    private @NotNull FileSyncEndpoint fileSyncEndpoint() {
//        return new FileSyncEndpoint(fileSyncProperties.getFileSyncEndpointTopicPattern(), fileSyncEndpoint().partitionId());
//    }
//
//    private @NotNull FileSyncEndpoint filePartitionSyncEndpoint(String file) {
//        return new FileSyncEndpoint(fileSyncProperties.getFileSyncEndpointTopicPattern(file), fileSyncEndpoint().partitionId());
//    }

}

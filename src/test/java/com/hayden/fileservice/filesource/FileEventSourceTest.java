package com.hayden.fileservice.filesource;

import com.hayden.fileservice.codegen.types.FileChangeEvent;
import com.hayden.fileservice.codegen.types.FileChangeType;
import com.hayden.fileservice.config.ByteArray;
import com.hayden.fileservice.data_fetcher.GetFilesRemoteDataFetcher;
import com.hayden.shared.communication.DataClientPublisher;
import com.hayden.shared.communication.DataClientSubscriber;
import com.hayden.utilitymodule.result.Result;
import com.netflix.graphql.dgs.autoconfig.DgsAutoConfiguration;
import graphql.ExecutionResult;
import graphql.execution.reactive.SubscriptionPublisher;
import org.intellij.lang.annotations.Language;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import reactor.core.publisher.Flux;

import com.netflix.graphql.dgs.DgsQueryExecutor;

import java.util.Map;

import static com.hayden.shared.assertions.AssertionUtil.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@AutoConfigureMockMvc
@ImportAutoConfiguration(DgsAutoConfiguration.class)
class FileEventSourceTest {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    DgsQueryExecutor queryExecutor;

    @MockBean
    FileDataSource fileDataSource;
    @MockBean
    DataClientSubscriber dataClientSubscriber;
    @MockBean
    DataClientPublisher dataClientPublisher;

    @Test
    void update() {
        when(fileDataSource.getFile(any())).thenReturn(Flux.just(Result.ok(new FileChangeEvent("hello", FileChangeType.ADD_CONTENT, 0, new ByteArray("hello".getBytes()), ""))));

        @Language("graphql") String q = """
        subscription {
            files(fileSearch: {path: "this", fileId: "ok", fileName: "hello", fileType: {type: "text", value: "html"}}) {
                fileId
            }
        }
        """;

        ExecutionResult execute = queryExecutor.execute(GetFilesRemoteDataFetcher.FILE_CHANGE_EVENT, Map.of("path", "this", "fileId", "ok", "fileName", "hello", "fileTypeType", "text", "fileTypeValue", "html"), "ListenToFileChanges");
        assertThat(execute.getErrors().size()).isEqualTo(0);
        assertThat(execute.isDataPresent()).isEqualTo(true);
        String isHello = getDataItem(execute).map(f -> new String(f.getData().getBytes())).blockFirst();
        assertThat(isHello).isEqualTo("hello");
    }

    private static Flux<FileChangeEvent> getDataItem(ExecutionResult execute) {
        return Flux.from(getData(execute).getUpstreamPublisher()).cast(FileChangeEvent.class);
    }

    private static SubscriptionPublisher getData(ExecutionResult execute) {
        return execute.getData();
    }

    @Test
    void fileMetadata() {
    }

    @Test
    void files() {
    }
}
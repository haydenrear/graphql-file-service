package com.hayden.fileservice.filesource.fileoperations;

import com.google.common.collect.Sets;
import com.hayden.fileservice.codegen.types.FileChangeEvent;
import com.hayden.fileservice.filesource.fileoperations.skipfileoperations.FileHeader;
import com.hayden.fileservice.graphql.FileEventSourceActions;
import com.hayden.utilitymodule.result.agg.Agg;
import com.hayden.utilitymodule.result.agg.Responses;
import com.hayden.utilitymodule.result.Result;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

public interface CompactableFileOperations {


    Result<FileCompactifyResponse, FileEventSourceActions.FileEventError> compactify(File file, FileHeader.HeaderDescriptor headerDescriptor);

    Result<FileFlushResponse, FileEventSourceActions.FileEventError> flush(File file, FileHeader.HeaderDescriptor headerDescriptor, FileChangeEvent fileChangeEvent);

    interface CompactableFileOperationsResponse<T> extends Responses.AggregateResponse {

        Set<File> file();

        Set<String> resultMessage();

        @Override
        default void addAgg(Agg aggregateResponse) {
            if (aggregateResponse instanceof CompactableFileOperationsResponse res) {
                file().addAll(res.file());
                resultMessage().addAll(res.resultMessage());
            }
        }
    }

    record FileCompactifyResponse(Set<File> file, Set<String> resultMessage) implements CompactableFileOperationsResponse {

        public FileCompactifyResponse() {
            this(new HashSet<>(), new HashSet<>());
        }

        public FileCompactifyResponse(File file) {
            this(Sets.newHashSet(file), new HashSet<>());
        }

        public FileCompactifyResponse(File file, String message) {
            this(Sets.newHashSet(file), Sets.newHashSet(message));
        }
    }

    record FileFlushResponse(Set<File> file, Set<String> resultMessage) implements CompactableFileOperationsResponse{

        public FileFlushResponse() {
            this(new HashSet<>(), new HashSet<>());
        }

        public FileFlushResponse(File file) {
            this(Sets.newHashSet(file), new HashSet<>());
        }

        public FileFlushResponse(File file, String message) {
            this(Sets.newHashSet(file), Sets.newHashSet(message));
        }

    }


}

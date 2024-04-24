package com.hayden.fileservice.graphql;

import com.hayden.fileservice.graphql.visitor_model.VisitorModelService;
import com.hayden.graphql.models.visitor.VisitorModel;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController("/api/v1/graphql")
@RequiredArgsConstructor
public class GraphQlSourcesController {

    private final VisitorModelService models;

    @GetMapping
    public List<VisitorModel> getSources() {
        return models.models();
    }

}

package com.hayden.fileservice.graphql;

import com.hayden.graphql.federated.visitor_model.ChangeVisitorModel;
import com.hayden.graphql.federated.visitor_model.ChangeVisitorModelService;
import com.hayden.graphql.federated.visitor_model.ChangeVisitorModelServiceImpl;
import com.hayden.graphql.federated.visitor_model.VisitorModelService;
import com.hayden.graphql.models.visitor.model.VisitorModel;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Import;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/graphql")
@RequiredArgsConstructor
// todo:
public class GraphQlSourcesController {

    private final VisitorModelService models;
    private final FileServiceChangeModelContext ctx;

    @PostConstruct
    public void initializeVisitorModelService() {
    }

    @GetMapping
    public List<VisitorModel> getSources() {
        return models.models(ctx).visitorModels();
    }

}

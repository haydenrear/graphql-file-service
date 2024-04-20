package com.hayden.fileservice.graphql;

import com.hayden.graphql.models.visitor.VisitorModel;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController("/api/v1/graphql")
public class GraphQlSourcesController {

    @GetMapping
    public List<VisitorModel> getSources() {
        return new ArrayList<>();
    }

}

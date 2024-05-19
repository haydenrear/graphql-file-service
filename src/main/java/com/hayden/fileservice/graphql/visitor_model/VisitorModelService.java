package com.hayden.fileservice.graphql.visitor_model;

import com.hayden.graphql.models.visitor.model.VisitorModel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class VisitorModelService {

    private final ChangeVisitorModelService visitorModelService;

    public List<VisitorModel> models() {
        return Optional.ofNullable(visitorModelService)
                .map(ChangeVisitorModelService::get)
                .orElse(new ArrayList<>());
    }

}

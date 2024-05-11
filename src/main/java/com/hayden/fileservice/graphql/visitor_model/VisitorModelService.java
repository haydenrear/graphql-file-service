package com.hayden.fileservice.graphql.visitor_model;

import com.hayden.graphql.models.visitor.VisitorModel;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class VisitorModelService {

    @Autowired(required = false)
    private ChangeVisitorModelService visitorModelService;

    public List<VisitorModel> models() {
        return Optional.ofNullable(visitorModelService)
                .map(ChangeVisitorModelService::get)
                .orElse(new ArrayList<>());
    }

}

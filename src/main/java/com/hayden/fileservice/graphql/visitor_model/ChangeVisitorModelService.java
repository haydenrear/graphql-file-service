package com.hayden.fileservice.graphql.visitor_model;

import com.hayden.graphql.models.visitor.model.VisitorModel;

import java.util.List;

/**
 *
 */
public interface ChangeVisitorModelService {

    List<VisitorModel> get();

    void register(ChangeVisitorModel changeVisitorModel);

}

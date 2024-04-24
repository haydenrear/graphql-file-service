package com.hayden.fileservice.graphql.visitor_model;

import com.hayden.graphql.models.visitor.VisitorModel;

import java.util.List;

/**
 *
 */
public interface ChangeVisitorModelService {

    boolean doChange();

    List<VisitorModel> get();

    void register(ChangeVisitorModel changeVisitorModel);

}

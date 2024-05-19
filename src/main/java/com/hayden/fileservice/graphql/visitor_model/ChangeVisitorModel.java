package com.hayden.fileservice.graphql.visitor_model;

import com.hayden.graphql.models.visitor.model.VisitorModel;

public interface ChangeVisitorModel {

    /**
     * return true if matches and if stale.
     * @param model
     * @return
     */
    boolean doAction(VisitorModel model);

    void command(VisitorModel model);

}

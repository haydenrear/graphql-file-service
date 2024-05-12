package com.hayden.fileservice.graphql.visitor_model;

import com.hayden.graphql.models.visitor.VisitorModel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@RequiredArgsConstructor
@Service
public class ChangeVisitorModelServiceImpl implements ChangeVisitorModelService {

    private final List<VisitorModel> visitorModels;

    private final List<ChangeVisitorModel> changeVisitorModels = new ArrayList<>();
    private final AtomicBoolean stale = new AtomicBoolean(false);

    @Override
    public List<VisitorModel> get() {
        doUpdate();
        return visitorModels;
    }

    @Override
    public void register(ChangeVisitorModel changeVisitorModel) {
        changeVisitorModels.add(changeVisitorModel);
        stale.set(true);
    }

    private void doUpdate() {
        if (stale.getAndSet(false))
            changeVisitorModels.forEach(changeVisitorModel -> visitorModels.stream()
                    .filter(changeVisitorModel::doAction)
                    .forEach(changeVisitorModel::command));
    }
}

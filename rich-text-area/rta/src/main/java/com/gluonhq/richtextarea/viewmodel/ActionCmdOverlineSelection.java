package com.gluonhq.richtextarea.viewmodel;

import com.gluonhq.richtextarea.Selection;
import javafx.beans.binding.BooleanBinding;

import java.util.Map;
import java.util.Objects;

public class ActionCmdOverlineSelection implements ActionCmd {

    private final Map<String, String> overlineMap;

    public ActionCmdOverlineSelection(Map<String, String> overlineMap) {
        this.overlineMap = Objects.requireNonNull(overlineMap);
    }

    @Override
    public void apply(RichTextAreaViewModel viewModel) {
        if (viewModel.isEditable() && viewModel.hasSelection()) {
            viewModel.getCommandManager().execute(new OverlineSelectionCmd(overlineMap));
        }
    }

    @Override
    public BooleanBinding getDisabledBinding(RichTextAreaViewModel viewModel) {
        return viewModel.selectionProperty().isEqualTo(Selection.UNDEFINED)
                .or(viewModel.editableProperty().not());
    }
}
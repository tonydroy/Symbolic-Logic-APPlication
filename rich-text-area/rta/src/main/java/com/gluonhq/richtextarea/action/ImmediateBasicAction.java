package com.gluonhq.richtextarea.action;

import com.gluonhq.richtextarea.RichTextArea;
import com.gluonhq.richtextarea.viewmodel.ActionCmd;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.event.ActionEvent;

import java.util.function.Function;

public class ImmediateBasicAction extends BasicAction {

    public ImmediateBasicAction(RichTextArea control, Function<Action, ActionCmd> actionCmdFunction) {
        super(control, actionCmdFunction);
    }

    @Override
    public void execute(ActionEvent event) {
        if (viewModel != null) {
            getActionCmd().apply(viewModel);
        } else {
            control.skinProperty().addListener(new InvalidationListener() {
                @Override
                public void invalidated(Observable observable) {
                    if (control.getSkin() != null) {
                        if (viewModel != null) {
                            getActionCmd().apply(viewModel);
                        }
                        control.skinProperty().removeListener(this);
                    }
                }
            });
        }
    }

}

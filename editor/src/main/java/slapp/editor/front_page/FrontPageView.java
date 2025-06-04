/*
Copyright (c) 2024 Tony Roy

This file is part of the Symbolic Logic APPlication (SLAPP).

SLAPP is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.

SLAPP is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License along with SLAPP.  If not, see
<https://www.gnu.org/licenses/>.
 */

package slapp.editor.front_page;

import com.gluonhq.richtextarea.RichTextArea;
import com.gluonhq.richtextarea.model.Document;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import slapp.editor.decorated_rta.DecoratedRTA;
import slapp.editor.main_window.ControlType;
import slapp.editor.main_window.ExerciseView;
import slapp.editor.main_window.MainWindowView;

/**
 * The view has dummy statement and comment areas, with an animation for its main content
 */

public class FrontPageView implements ExerciseView<Label> {
    private Label exerciseStatement;
    private DecoratedRTA exerciseComment;
    private Node exerciseControl;
    private MainWindowView mainView;
    private FrontPageAnimation frontAnimation = new FrontPageAnimation();
    private VBox exerciseContent;
    private boolean played = false;
    private ChangeListener contentFocusListener;




    public FrontPageView(MainWindowView mainView) {
        this.mainView = mainView;
        exerciseStatement = new Label("");
        exerciseComment = new DecoratedRTA();

        exerciseStatement.setVisible(false);
        exerciseStatement.setManaged(false);
        exerciseComment.getEditor().setVisible(false);
        exerciseComment.getEditor().setManaged(false);

        exerciseContent = frontAnimation.getFrontPageBox();

        /*
        Pane spacerPane = new Pane();
        double centeringWidth = (mainView.getMinStageWidth() - exerciseContent.getPrefWidth()) / 2.0;
        spacerPane.setMinWidth(centeringWidth);
        exerciseControl = spacerPane;

         */

  //      CheckBox checkBox = mainView.getInstructorCheck();
        AnchorPane controlPane = new AnchorPane();    //(checkBox)
  //      AnchorPane.setBottomAnchor(checkBox, 10.0);
  //      AnchorPane.setRightAnchor(checkBox, 5.0);

        double centeringWidth = (mainView.getMinStageWidth() - exerciseContent.getPrefWidth()) / 2.0;
        controlPane.setMinWidth(centeringWidth);
        exerciseControl = controlPane;



        RichTextArea commentRTA = exerciseComment.getEditor();
        commentRTA.getActionFactory().open(new Document("Logo Here")).execute(new ActionEvent());
        commentRTA.setEditable(false);
        commentRTA.setFocusTraversable(false);
        commentRTA.setMouseTransparent(true);
        commentRTA.setPrefHeight(500);

        commentRTA.setStyle("-fx-padding: 5; -fx-background-color: WHITE; -fx-border-width: 2; -fx-border-color: gainsboro; ");

        exerciseComment.getEditToolbar().setFocusTraversable(false);
        exerciseComment.getEditToolbar().setMouseTransparent(true);
        exerciseComment.getParagraphToolbar().setFocusTraversable(false);
        exerciseComment.getParagraphToolbar().setMouseTransparent(true);
        exerciseComment.getKbdSelectorToolbar().setFocusTraversable(false);
        exerciseComment.getKbdSelectorToolbar().setMouseTransparent(true);
        mainView.editorInFocus(exerciseComment, ControlType.STATEMENT);

        contentFocusListener = new ChangeListener() {
            @Override
            public void changed(ObservableValue ob, Object ov, Object nv) {
                exerciseContent.focusedProperty().removeListener(contentFocusListener);
                frontAnimation.playFrontAnimation();
            }
        };
        exerciseContent.focusedProperty().addListener(contentFocusListener);
    }

    @Override
    public DecoratedRTA getExerciseComment() { return exerciseComment; }
    @Override
    public void setExerciseComment(DecoratedRTA exerciseComment) {}
    @Override
    public Label getExerciseStatement() { return exerciseStatement; }
    @Override
    public void setExerciseStatement(Label exerciseStatement) {}
    @Override
    public Node getExerciseStatementNode() { return exerciseStatement; }
    @Override
    public void setStatementPrefHeight(double height) { }
    @Override
    public Node getExerciseContentNode() { return exerciseContent; }
    @Override
    public Node getExerciseControl() { return exerciseControl; }
    @Override
    public Node getRightControl() { return null; }

}

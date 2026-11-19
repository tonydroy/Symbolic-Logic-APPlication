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
import javafx.event.ActionEvent;
import javafx.scene.Node;
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

    // SCORCHED EARTH RULE 2: Kill Switch for asynchronous event interception during teardown
    private boolean isTornDown = false;



    public FrontPageView(MainWindowView mainView) {
        this.mainView = mainView;
        exerciseStatement = new Label("");
        exerciseComment = new DecoratedRTA();

        exerciseStatement.setVisible(false);
        exerciseStatement.setManaged(false);
        exerciseComment.getEditor().setVisible(false);
        exerciseComment.getEditor().setManaged(false);

        exerciseContent = frontAnimation.getFrontPageBox();

        double centeringWidth = (mainView.getMinStageWidth() - exerciseContent.getPrefWidth()) / 2.0;
        AnchorPane controlPane = new AnchorPane();
        controlPane.setMinWidth(centeringWidth - 10);

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

        frontAnimation.playFrontAnimation();
    }

    // SCORCHED EARTH RULE 6: Sever Shared Nodes helper
    private void severFromParent(Node node) {
        try {
            if (node != null && node.getParent() != null) {
                if (node.getParent() instanceof javafx.scene.layout.Pane) ((javafx.scene.layout.Pane) node.getParent()).getChildren().remove(node);
                else if (node.getParent() instanceof javafx.scene.Group) ((javafx.scene.Group) node.getParent()).getChildren().remove(node);
            }
        } catch (Exception e) {}
    }

    // SCORCHED EARTH RULE 1 & 5: Cleanup cascading and isolation
    public void cleanup() {
        // SCORCHED EARTH RULE 2: Engage Kill Switch
        isTornDown = true;

        // SCORCHED EARTH RULE 7: Safe Global State & Null-Poisoning Prevention
        try {
            if (mainView != null) {
                mainView.textFieldInFocus(); // Safely clear toolbars
            }
        } catch (Exception e) {}

        // SCORCHED EARTH RULE 4: The Isolated JavaFX Skin Hammer
        try {
            final RichTextArea commEditor = (exerciseComment != null) ? exerciseComment.getEditor() : null;
            if (commEditor != null) {
                javafx.animation.PauseTransition pt = new javafx.animation.PauseTransition(javafx.util.Duration.millis(1500));
                pt.setOnFinished(ev -> {
                    try {
                        if (commEditor.getDocument() == null || commEditor.getDocument().getText().isEmpty()) {
                            commEditor.getActionFactory().open(new Document(" ")).execute(new ActionEvent());
                        }
                        commEditor.setSkin(null);
                    } catch (Exception ex) {}
                });
                pt.play();
            }
        } catch (Exception e) {}

        // SCORCHED EARTH RULE 6: Disconnect UI Controls
        try {
            if (exerciseStatement != null) {
                severFromParent(exerciseStatement);
            }
        } catch (Exception e) {}

        try {
            if (exerciseControl != null) {
                severFromParent(exerciseControl);
                if (exerciseControl instanceof Pane) ((Pane) exerciseControl).getChildren().clear();
            }
        } catch (Exception e) {}

        try {
            if (exerciseContent != null) {
                severFromParent(exerciseContent);
                exerciseContent.getChildren().clear();
            }
        } catch (Exception e) {}

        // SCORCHED EARTH RULE 1: Cascade cleanup to Animation (via reflection)
        try {
            if (frontAnimation != null) {
                frontAnimation.getClass().getMethod("cleanup").invoke(frontAnimation);
            }
        } catch (Exception e) {}

        // SCORCHED EARTH RULE 1: Final Nullification
        try { this.mainView = null; } catch (Exception e) {}
        try { this.exerciseStatement = null; } catch (Exception e) {}
        try { this.exerciseComment = null; } catch (Exception e) {}
        try { this.exerciseControl = null; } catch (Exception e) {}
        try { this.frontAnimation = null; } catch (Exception e) {}
        try { this.exerciseContent = null; } catch (Exception e) {}
        try { this.contentFocusListener = null; } catch (Exception e) {}
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

    @Override
    public Node getPointsNode() { return null; }

    @Override
    public int getPointsPossible() { return 0; }
}
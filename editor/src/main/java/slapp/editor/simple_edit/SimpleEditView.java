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

package slapp.editor.simple_edit;

import com.gluonhq.richtextarea.RichTextArea;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import slapp.editor.decorated_rta.DecoratedRTA;
import slapp.editor.main_window.ExerciseView;
import slapp.editor.main_window.MainWindowView;

/**
 * View for the simple edit exercise
 */
public class SimpleEditView implements ExerciseView<DecoratedRTA> {

    private MainWindowView mainView;
    private DecoratedRTA exerciseStatement = new DecoratedRTA();
    private DecoratedRTA exerciseComment = new DecoratedRTA();
    private DecoratedRTA exerciseResponse = new DecoratedRTA();
    private String responsePrompt = new String();
    private Node exerciseControlNode = new VBox();
    private double statementPrefHeight = 0;
    private double commentPrefHeight = 0;
    private double responsePrefHeight = 0;
    private Spinner<Double> statementHeightSpinner;
    private Spinner<Double> statementWidthSpinner;
    private Spinner<Double> commentHeightSpinner;
    private Spinner<Double> commentWidthSpinner;
    private Spinner<Double> responseHeightSpinner;
    private Spinner<Double> responseWidthSpinner;
    private Node currentSpinnerNode;
    private int pointsPossible;
    private TextField pointsEarnedTextField;



    /**
     * Create simple edit view
     * @param mainView the main view
     */
    public SimpleEditView(MainWindowView mainView) {
        this.mainView = mainView;

        Region spacer = new Region();
        spacer.setPrefWidth(150);
        VBox controlBox = (VBox) exerciseControlNode;
        controlBox.getChildren().add(spacer);
    }

    /**
     * Initialize statement, comment, and response areas including text height controls
     */
    void initializeViewDetails() {

        //statement rta
        RichTextArea statementRTA = exerciseStatement.getEditor();
        statementRTA.getStylesheets().add("slappTextArea.css");
        statementRTA.setEditable(false);

        double statementInitialHeight = Math.round(statementPrefHeight / mainView.getScalePageHeight() * 100.0);
        statementHeightSpinner = new Spinner<>(0.0, 999.0, statementInitialHeight, 1.0);
        statementHeightSpinner.setPrefWidth(60);
        statementHeightSpinner.setDisable(false);
        statementHeightSpinner.setTooltip(new Tooltip("Height as % of selected paper"));
        statementRTA.prefHeightProperty().bind(Bindings.max(45.0, Bindings.multiply(mainView.scalePageHeightProperty(), DoubleProperty.doubleProperty(statementHeightSpinner.getValueFactory().valueProperty()).divide(100.0))));
        statementHeightSpinner.valueProperty().addListener((obs, ov, nv) -> {
            Node increment = statementHeightSpinner.lookup(".increment-arrow-button");
            if (increment != null) increment.getOnMouseReleased().handle(null);
            Node decrement = statementHeightSpinner.lookup(".decrement-arrow-button");
            if (decrement != null) decrement.getOnMouseReleased().handle(null);
        });

        statementRTA.prefWidthProperty().bind(mainView.scalePageWidthProperty());
        statementWidthSpinner = new Spinner<>(0.0, 999.0, 100, 1.0);
        statementWidthSpinner.setPrefWidth(60);
        statementWidthSpinner.setDisable(true);
        statementWidthSpinner.setTooltip(new Tooltip("Width as % of selected paper"));

        //comment rta
        RichTextArea commentRTA = exerciseComment.getEditor();
        commentRTA.getStylesheets().add("slappTextArea.css");
        commentRTA.setPromptText("Commment:");

        double commentInitialHeight = Math.round(commentPrefHeight / mainView.getScalePageHeight() * 100.0);
        commentHeightSpinner = new Spinner<>(0.0, 999.0, commentInitialHeight, 1.0);
        commentHeightSpinner.setPrefWidth(60);
        commentHeightSpinner.setDisable(false);
        commentHeightSpinner.setTooltip(new Tooltip("Height as % of selected paper"));
        commentRTA.prefHeightProperty().bind(Bindings.max(45.0, Bindings.multiply(mainView.scalePageHeightProperty(), DoubleProperty.doubleProperty(commentHeightSpinner.getValueFactory().valueProperty()).divide(100.0))));
        commentHeightSpinner.valueProperty().addListener((obs, ov, nv) -> {
            Node increment = commentHeightSpinner.lookup(".increment-arrow-button");
            if (increment != null) increment.getOnMouseReleased().handle(null);
            Node decrement = commentHeightSpinner.lookup(".decrement-arrow-button");
            if (decrement != null) decrement.getOnMouseReleased().handle(null);
        });

        commentRTA.prefWidthProperty().bind(mainView.scalePageWidthProperty());
        commentWidthSpinner = new Spinner<>(0.0, 999.0, 100, 1.0);
        commentWidthSpinner.setPrefWidth(60);
        commentWidthSpinner.setDisable(true);
        commentWidthSpinner.setTooltip(new Tooltip("Width as % of selected paper"));

       //response rta
        RichTextArea responseRTA = exerciseResponse.getEditor();
        responseRTA.getStylesheets().add("slappTextArea.css");
        responseRTA.setPromptText(responsePrompt);

        double responseInitialHeight = Math.round(responsePrefHeight / mainView.getScalePageHeight() * 20.0 ) * 5;


        responseHeightSpinner = new Spinner<>(0.0, 999.0, responseInitialHeight, 5.0);
        responseHeightSpinner.setPrefWidth(60);
        responseHeightSpinner.setDisable(false);
        responseHeightSpinner.setTooltip(new Tooltip("Height as % of selected paper"));
        responseRTA.prefHeightProperty().bind(Bindings.max(15.0, Bindings.multiply(mainView.scalePageHeightProperty(), DoubleProperty.doubleProperty(responseHeightSpinner.getValueFactory().valueProperty()).divide(100.0))));
        responseHeightSpinner.valueProperty().addListener((obs, ov, nv) -> {
            Node increment = responseHeightSpinner.lookup(".increment-arrow-button");
            if (increment != null) increment.getOnMouseReleased().handle(null);
            Node decrement = responseHeightSpinner.lookup(".decrement-arrow-button");
            if (decrement != null) decrement.getOnMouseReleased().handle(null);
        });

        responseRTA.prefWidthProperty().bind(mainView.scalePageWidthProperty());
        responseWidthSpinner = new Spinner<>(0.0, 999.0, 100, 5.0);
        responseWidthSpinner.setPrefWidth(60);
        responseWidthSpinner.setDisable(true);
        responseWidthSpinner.setTooltip(new Tooltip("Width as % of selected paper"));

       //page height listener
        mainView.scalePageHeightProperty().addListener((ob, ov, nv) -> {

            statementRTA.prefHeightProperty().unbind();
            statementHeightSpinner.getValueFactory().setValue((double) Math.round(statementHeightSpinner.getValue() * ov.doubleValue() / nv.doubleValue()));
            statementRTA.prefHeightProperty().bind(Bindings.max(45.0, Bindings.multiply(nv.doubleValue(), DoubleProperty.doubleProperty(statementHeightSpinner.getValueFactory().valueProperty()).divide(100.0))));

            commentRTA.prefHeightProperty().unbind();
            commentHeightSpinner.getValueFactory().setValue((double) Math.round(commentHeightSpinner.getValue() * ov.doubleValue() / nv.doubleValue()));
            commentRTA.prefHeightProperty().bind(Bindings.max(45.0, Bindings.multiply(nv.doubleValue(), DoubleProperty.doubleProperty(commentHeightSpinner.getValueFactory().valueProperty()).divide(100.0))));

            responseRTA.prefHeightProperty().unbind();
            responseHeightSpinner.getValueFactory().setValue((double) Math.round(responseHeightSpinner.getValue() * ov.doubleValue() / nv.doubleValue() / 5.0) * 5.0);
            responseRTA.prefHeightProperty().bind(Bindings.max(15.0, Bindings.multiply(nv.doubleValue(), DoubleProperty.doubleProperty(responseHeightSpinner.getValueFactory().valueProperty()).divide(100.0))));

        });

        setSizeSpinners();
    }

    private void setSizeSpinners() {

        mainView.getMainScene().focusOwnerProperty().addListener((ob, ov, nv) -> {

            RichTextArea statementRTA = exerciseStatement.getEditor();
            if (inHierarchy(nv, statementRTA) && currentSpinnerNode != statementRTA) {
                currentSpinnerNode = statementRTA;
                mainView.updateSizeSpinners(statementHeightSpinner, statementWidthSpinner);
                return;
            }
            RichTextArea commentRTA = exerciseComment.getEditor();
            if (inHierarchy(nv, commentRTA) && currentSpinnerNode != commentRTA) {
                currentSpinnerNode = commentRTA;
                mainView.updateSizeSpinners(commentHeightSpinner, commentWidthSpinner);
                return;
            }
            RichTextArea responseRTA = exerciseResponse.getEditor();
            if (inHierarchy(nv, responseRTA) && currentSpinnerNode != responseRTA) {
                currentSpinnerNode = responseRTA;
                mainView.updateSizeSpinners(responseHeightSpinner, responseWidthSpinner);

            }


        });
    }

    private static boolean inHierarchy(Node node, Node potentialHierarchyElement) {
        if (potentialHierarchyElement == null) {
            return true;
        }
        while (node != null) {
            if (node == potentialHierarchyElement) {
                return true;
            }
            node = node.getParent();
        }
        return false;
    }

    /**
     * The prompt for the response area
     * @param prompt the prompt string
     */
    public void setResponsePrompt(String prompt) {   responsePrompt = prompt;  }

    /**
     * The preferred height of the comment area
     * @return the height value
     */
    public double getCommentPrefHeight() { return exerciseComment.getEditor().getPrefHeight();     }

    /**
     * The preferred height of the comment area
     * @param commentPrefHeight the height value
     */
    public void setCommentPrefHeight(double commentPrefHeight) {     this.commentPrefHeight = commentPrefHeight;   }

    /**
     * The preferred height of the response area
     * @return the height value
     */
    public double getResponsePrefHeight() {    return exerciseResponse.getEditor().getPrefHeight();   }

    /**
     * The preferred height of the response area
     * @param responsePrefHeight the height value
     */
    public void setResponsePrefHeight(double responsePrefHeight) {     this.responsePrefHeight = responsePrefHeight;  }

    /**
     * The DecoratedRTA for the response area
     * @return the DecoratedRTA
     */
    public DecoratedRTA getExerciseResponse() {    return exerciseResponse;   }

    /**
     * The DecoratedRTA for the response area
     * @param exerciseResponse the DecoratedRTA
     */
    public void setExerciseResponse(DecoratedRTA exerciseResponse) {   this.exerciseResponse = exerciseResponse;  }

    /**
     * The node displayed for a simple edit window in the FreeForm exercise
     * @return the exerciseResponse RTA
     */
    public Node getFFViewNode() {
        return exerciseResponse.getEditor();
    }

    Spinner getResponseHeightSpinner() {return responseHeightSpinner;}

    Spinner getResponseWidthSpinner() {return responseWidthSpinner;}


    /**
     * The comment decoratedRTA
     * @return comment DecoratedRTA
     */
    @Override
    public DecoratedRTA getExerciseComment() { return exerciseComment; }

    /**
     * The comment decoratedRTA
     * @param exerciseComment DecoratedRTA
     */
    @Override
    public void setExerciseComment(DecoratedRTA exerciseComment) { this.exerciseComment = exerciseComment; }

    /**
     * The exercise prompt DRTA
     * @return the statement DRTA
     */
    @Override
    public DecoratedRTA getExerciseStatement() { return exerciseStatement; }

    /**
     * The exercise prompt DRTA
     * @param exerciseStatement the statement DRTA
     */
    @Override
    public void setExerciseStatement(DecoratedRTA exerciseStatement) {  this.exerciseStatement = exerciseStatement;  }

    /**
     * The exercise prompt RTA
     * @return the statement RTA
     */
    @Override
    public Node getExerciseStatementNode() {  return exerciseStatement.getEditor();  }

    /**
     * The preferred height of the statement window
     * @param height the preferred height
     */
    public void setStatementPrefHeight(double height) {
        statementPrefHeight = height;
        exerciseStatement.getEditor().setPrefHeight(height);
    }

    /**
     * The node which includes content of this exercise
     * @return the exerciseResponse RTA
     */
    @Override
    public Node getExerciseContentNode() {     return exerciseResponse.getEditor();  }

    /**
     * The left control node - in this case, an empty VBox
     * @return the control node
     */
    @Override
    public Node getExerciseControl() { return exerciseControlNode; }

    /**
     * The right control is null
     * @return null
     */
    @Override
    public Node getRightControl() { return null; }

    @Override
    public Node getPointsNode() {
        if (pointsPossible > 0) {
            Label pointsPossibleLabel = new Label(" / " + pointsPossible);
            HBox pointBox = new HBox(pointsEarnedTextField, pointsPossibleLabel);
            return pointBox;
        }
        return null;
    }

    public void setPointsPossible(int pointsPossible) {
        this.pointsPossible = pointsPossible;
    }
    public TextField getPointsEarnedTextField() {
        return pointsEarnedTextField;
    }

}



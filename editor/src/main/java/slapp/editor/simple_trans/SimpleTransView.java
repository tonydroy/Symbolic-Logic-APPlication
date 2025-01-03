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

package slapp.editor.simple_trans;

import com.gluonhq.richtextarea.RichTextArea;
import com.gluonhq.richtextarea.RichTextAreaSkin;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.scene.Node;
import javafx.scene.control.Spinner;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import slapp.editor.decorated_rta.DecoratedRTA;
import slapp.editor.main_window.ExerciseView;
import slapp.editor.main_window.MainWindowView;

/**
 * View for the simple translate exercise
 */
public class SimpleTransView implements ExerciseView<DecoratedRTA> {

    private MainWindowView mainView;
    private DecoratedRTA exerciseStatement = new DecoratedRTA();
    private DecoratedRTA exerciseComment = new DecoratedRTA();
    private DecoratedRTA exerciseResponse = new DecoratedRTA();
    private DecoratedRTA exerciseInterpretation = new DecoratedRTA();
    private Node exerciseControlNode = new VBox();
    private double statementPrefHeight = 0;
    private double commentPrefHeight = 0;
    private double responsePrefHeight = 0;
    private double interpretationPrefHeight = 0;
    private Spinner<Double> statementHeightSpinner;
    private Spinner<Double> statementWidthSpinner;
    private Spinner<Double> commentHeightSpinner;
    private Spinner<Double> commentWidthSpinner;
    private Spinner<Double> responseHeightSpinner;
    private Spinner<Double> responseWidthSpinner;
    private Spinner<Double> interpretationHeightSpinner;
    private Spinner<Double> interpretationWidthSpinner;
    private Node currentSpinnerNode;


    /**
     * Construct simple translate view
     * @param mainView the main view
     */
    public SimpleTransView(MainWindowView mainView) {
        this.mainView = mainView;

        //the control box is empty, but want to hold the space
        Region spacer = new Region();
        spacer.setPrefWidth(150);
        VBox controlBox = (VBox) exerciseControlNode;
        controlBox.getChildren().add(spacer);
    }

    /**
     * Initialize page management for statement, comment, interpretation, and translate fields
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

       //interpretation rta

        RichTextArea interpretationRTA = exerciseInterpretation.getEditor();
        interpretationRTA.getStylesheets().add("slappTextArea.css");
        interpretationRTA.setPromptText("Interpretation Function");
        exerciseInterpretation.getKeyboardSelector().valueProperty().setValue(RichTextAreaSkin.KeyMapValue.BASE_AND_SANS);

        double interpretationInitialHeight = Math.round(interpretationPrefHeight / mainView.getScalePageHeight() * 20.0 ) * 5;


        interpretationHeightSpinner = new Spinner<>(0.0, 999.0, interpretationInitialHeight, 5.0);
        interpretationHeightSpinner.setPrefWidth(60);
        interpretationHeightSpinner.setDisable(false);
        interpretationHeightSpinner.setTooltip(new Tooltip("Height as % of selected paper"));
        interpretationRTA.prefHeightProperty().bind(Bindings.max(15.0, Bindings.multiply(mainView.scalePageHeightProperty(), DoubleProperty.doubleProperty(interpretationHeightSpinner.getValueFactory().valueProperty()).divide(100.0))));
        interpretationHeightSpinner.valueProperty().addListener((obs, ov, nv) -> {
            Node increment = interpretationHeightSpinner.lookup(".increment-arrow-button");
            if (increment != null) increment.getOnMouseReleased().handle(null);
            Node decrement = interpretationHeightSpinner.lookup(".decrement-arrow-button");
            if (decrement != null) decrement.getOnMouseReleased().handle(null);
        });

        interpretationRTA.prefWidthProperty().bind(mainView.scalePageWidthProperty());
        interpretationWidthSpinner = new Spinner<>(0.0, 999.0, 100, 5.0);
        interpretationWidthSpinner.setPrefWidth(60);
        interpretationWidthSpinner.setDisable(true);
        interpretationWidthSpinner.setTooltip(new Tooltip("Width as % of selected paper"));


        //response rta
        RichTextArea responseRTA = exerciseResponse.getEditor();
        responseRTA.getStylesheets().add("slappTextArea.css");
        responseRTA.setPromptText("Formal Translation");
        exerciseResponse.getKeyboardSelector().valueProperty().setValue(RichTextAreaSkin.KeyMapValue.ITALIC_AND_SANS);

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

            interpretationRTA.prefHeightProperty().unbind();
            interpretationHeightSpinner.getValueFactory().setValue((double) Math.round(interpretationHeightSpinner.getValue() * ov.doubleValue() / nv.doubleValue() / 5.0) * 5.0);
            interpretationRTA.prefHeightProperty().bind(Bindings.max(15.0, Bindings.multiply(nv.doubleValue(), DoubleProperty.doubleProperty(interpretationHeightSpinner.getValueFactory().valueProperty()).divide(100.0))));
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
                return;
            }
            RichTextArea interpretationRTA = exerciseInterpretation.getEditor();
            if (inHierarchy(nv, interpretationRTA) && currentSpinnerNode != interpretationRTA) {
                currentSpinnerNode = interpretationRTA;
                mainView.updateSizeSpinners(interpretationHeightSpinner, interpretationWidthSpinner);

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
     * Preferred height of the comment window
     * @return the height value
     */
    public double getCommentPrefHeight() { return exerciseComment.getEditor().getPrefHeight();     }

    /**
     * Preferred height of the comment window
     * @param commentPrefHeight the height value
     */
    public void setCommentPrefHeight(double commentPrefHeight) {     this.commentPrefHeight = commentPrefHeight;   }

    /**
     * Preferred height of the response (translation) window
     * @return the height value
     */
    public double getResponsePrefHeight() {    return exerciseResponse.getEditor().getPrefHeight();   }

    /**
     * Preferred height of the response (translation) window
     * @param responsePrefHeight the height value
     */
    public void setResponsePrefHeight(double responsePrefHeight) {     this.responsePrefHeight = responsePrefHeight;  }

    /**
     * The the response (translation) area view
     * @return the response DRTA
     */
    public DecoratedRTA getExerciseResponse() {    return exerciseResponse;   }

    /**
     * The response (translation) area view
     * @param exerciseResponse the response DRTA
     */
    public void setExerciseResponse(DecoratedRTA exerciseResponse) {   this.exerciseResponse = exerciseResponse;  }

    /**
     * The interpretation area view
     * @return the interpretation DRTA
     */
    public DecoratedRTA getExerciseInterpretation() {  return exerciseInterpretation;   }

    /**
     * The interpretation area view
     * @param exerciseInterpretation the interpretation DRTA
     */
    public void setExerciseInterpretation(DecoratedRTA exerciseInterpretation) {     this.exerciseInterpretation = exerciseInterpretation;  }

    /**
     * The preferred height of the interpretation window
     * @return the height value
     */
    public double getInterpretationPrefHeight() {   return exerciseInterpretation.getEditor().getPrefHeight();  }

    /**
     * The preferred height of the interpretation window
     * @param interpretationPrefHeight the height value
     */
    public void setInterpretationPrefHeight(double interpretationPrefHeight) {   this.interpretationPrefHeight = interpretationPrefHeight;   }


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
     * The exercise prompt (usually DecoratedRTA)
     * @return the statement (T)
     */
    @Override
    public DecoratedRTA getExerciseStatement() { return exerciseStatement; }

    /**
     * The exercise prompt (usually DecoratedRTA)
     * @param exerciseStatement the statement (T)
     */
    @Override
    public void setExerciseStatement(DecoratedRTA exerciseStatement) {  this.exerciseStatement = exerciseStatement;  }

    /**
     * The exercise prompt node (usually RTA)
     * @return the statement node
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
     * @return the content node
     */
    @Override
    public Node getExerciseContentNode() {     return new VBox(2, exerciseInterpretation.getEditor(), exerciseResponse.getEditor());  }

    /**
     * The (empty) left control node for this exercise
     * @return the control node
     */
    @Override
    public Node getExerciseControl() { return exerciseControlNode; }

    /**
     * The (null) right control node for this exercise
     * @return the control node
     */
    @Override
    public Node getRightControl() { return null; }


}



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

package slapp.editor.free_form;

import com.gluonhq.richtextarea.RichTextArea;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import slapp.editor.decorated_rta.DecoratedRTA;
import slapp.editor.main_window.ExerciseView;
import slapp.editor.main_window.MainWindowView;

import java.util.ArrayList;
import java.util.List;
import java.util.function.UnaryOperator;

/**
 * View for the free form exercise
 */
public class FreeFormView implements ExerciseView<DecoratedRTA> {

    private MainWindowView mainView;
    private DecoratedRTA statementDRTA = new DecoratedRTA();
    private DecoratedRTA commentDRTA = new DecoratedRTA();
    private double statementPrefHeight = 0;
    private double commentPrefHeight = 0;
    private Node exerciseControlNode;
    private Node freeFormControlNode;
    private List<ViewElement> viewElements = new ArrayList<>();
    private VBox contentBox = new VBox();
    private Spinner<Double> statementHeightSpinner;
    private Spinner<Double> statementWidthSpinner;
    private Spinner<Double> commentHeightSpinner;
    private Spinner<Double> commentWidthSpinner;
    private Node currentSpinnerNode;
    private Button indentButton;
    private Button outdentButton;
    private Button removeButton;
    private Button restoreButton;
    private Button moveUpButton;
    private Button moveDownButton;
    private Button addEditButton;
    private Button addHTreeButton;
    private Button addTTableButton;
    private Button addVTreeBaseItalButton;
    private Button addVTreeItalSansButton;
    private Button addVTreeScriptItalButton;
    private Button addNDrvtnItalSansButton;
    private Button addNDrvtnScriptItalButton;
    private Button addNDrvtnScriptSansButton;
    private Button addNDrvtnItalBBButton;
    private Button addADrvtnItalSansButton;
    private Button addADrvtnScriptItalButton;
    private Button addADrvtnScriptSansButton;
    private Button addADrvtnItalBBButton;

    private int pointsPossible;
    private TextField pointsEarnedTextField;


    /**
     * Construct the free form view
     * @param mainView the main view
     */
    public FreeFormView(MainWindowView mainView) {
        this.mainView = mainView;

        //all the buttons from which the exercise includes some
        indentButton = newControlsButton("Indent", "Indent current window.");
        outdentButton = newControlsButton("Outdent", "Outdent current window.");
        removeButton = newControlsButton("Remove", "Remove current window.");
        restoreButton = newControlsButton ("Restore", "Restore last removed item.");
        moveUpButton = newControlsButton("Move Up", "Place above previous item.");
        moveDownButton = newControlsButton("Move Down", "Place below following item.");
        addEditButton = newControlsButton("Simple Edit", "Insert simple editor.");
        addTTableButton = newControlsButton("Truth Table", "Insert truth table.");
        addHTreeButton = newControlsButton("Horiz Tree", "Insert horizontal tree.");
        addVTreeBaseItalButton = newControlsButton("Vert Tree", "Insert vertical tree.");
        addVTreeItalSansButton = newControlsButton("Vert Tree", "Insert vertical tree.");
        addVTreeScriptItalButton = newControlsButton("Vert Tree", "Insert vertical tree.");
        addNDrvtnItalSansButton = newControlsButton("Nat Drvtn", "Insert natural derivation.");
        addNDrvtnScriptItalButton = newControlsButton("Nat Drvtn", "Insert natural derivation.");
        addNDrvtnScriptSansButton = newControlsButton("Nat Drvtn", "Insert natural derivation.");
        addNDrvtnItalBBButton = newControlsButton("Nat Drvtn", "Insert natural derivation.");
        addADrvtnItalSansButton = newControlsButton("Ax Drvtn", "Insert axiomatic derivation.");
        addADrvtnScriptItalButton = newControlsButton("Ax Drvtn", "Insert axiomatic derivation.");
        addADrvtnScriptSansButton = newControlsButton("Ax Drvtn", "Insert axiomatic derivation.");
        addADrvtnItalBBButton = newControlsButton("Ax Drvtn", "Insert axiomatic derivation.");

        VBox rightControlBox = new VBox(20);
        rightControlBox.setAlignment(Pos.BASELINE_LEFT);
        rightControlBox.setPadding(new Insets(40,20,0,20));
        freeFormControlNode = rightControlBox;

        pointsEarnedTextField = new TextField();
        pointsEarnedTextField.setPrefWidth(27);
        pointsEarnedTextField.setAlignment(Pos.CENTER_RIGHT);
        pointsEarnedTextField.setPadding(new Insets(0,5,0,3));
        UnaryOperator<TextFormatter.Change> filter = change -> {
            String text = change.getText();
            if (text.matches("[0-9]*")) {
                return change;
            }
            return null;
        };
        TextFormatter<String> textFormatter = new TextFormatter<>(filter);
        pointsEarnedTextField.setTextFormatter(textFormatter);
    }

    /*
     * Construct new button
     * @param name Button name
     * @param tip Button tooltip
     * @return the button
     */
    private Button newControlsButton(String name, String tip) {
        Button button = new Button(name);
        button.setPrefWidth(100);
        button.setPrefHeight(28);
        button.setTooltip(new Tooltip(tip));
        return button;
    }

    /**
     * initialize statement and comment fields
     */
    void initializeViewDetails() {

        //statement
        RichTextArea statementRTA = statementDRTA.getEditor();
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

        statementRTA.maxWidthProperty().bind(mainView.scalePageWidthProperty());
        statementWidthSpinner = new Spinner<>(0.0, 999.0, 100, 1.0);
        statementWidthSpinner.setPrefWidth(60);
        statementWidthSpinner.setDisable(true);
        statementWidthSpinner.setTooltip(new Tooltip("Width as % of selected paper"));

        statementRTA.addEventFilter(MouseEvent.MOUSE_CLICKED, e -> {
            if (currentSpinnerNode != statementRTA) {
                currentSpinnerNode = statementRTA;
                mainView.updateSizeSpinners(statementHeightSpinner, statementWidthSpinner);
            }
        });

        //comment rta
        RichTextArea commentRTA = commentDRTA.getEditor();
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

        commentRTA.addEventFilter(MouseEvent.MOUSE_CLICKED, e -> {
            if (currentSpinnerNode != commentRTA) {
                currentSpinnerNode = commentRTA;
                mainView.updateSizeSpinners(commentHeightSpinner, commentWidthSpinner);
            }
        });

        //page height listener
        mainView.scalePageHeightProperty().addListener((ob, ov, nv) -> {

            statementRTA.prefHeightProperty().unbind();
            statementHeightSpinner.getValueFactory().setValue((double) Math.round(statementHeightSpinner.getValue() * ov.doubleValue() / nv.doubleValue()));
            statementRTA.prefHeightProperty().bind(Bindings.max(45.0, Bindings.multiply(nv.doubleValue(), DoubleProperty.doubleProperty(statementHeightSpinner.getValueFactory().valueProperty()).divide(100.0))));

            commentRTA.prefHeightProperty().unbind();
            commentHeightSpinner.getValueFactory().setValue((double) Math.round(commentHeightSpinner.getValue() * ov.doubleValue() / nv.doubleValue()));
            commentRTA.prefHeightProperty().bind(Bindings.max(45.0, Bindings.multiply(nv.doubleValue(), DoubleProperty.doubleProperty(commentHeightSpinner.getValueFactory().valueProperty()).divide(100.0))));
        });
    }

    /**
     * Update the content VBox from the view elements
     */
    void updateContentFromViewElements() {
        contentBox.getChildren().clear();
        for (ViewElement element : viewElements) {
            Node node = element.getNode();
            Region spacer = new Region();
            spacer.setPrefWidth(20.0 * element.getIndentLevel());
            HBox windowBox = new HBox(spacer, node);
            contentBox.getChildren().add(windowBox);
        }
    }

    void setBorders(int i) {
        for (ViewElement element : viewElements) {
            element.getNode().getStyleClass().remove("fx-border-color");
            element.getNode().setStyle("-fx-border-color: gainsboro");
      //      System.out.println("other: " + element.getNode().getStyle());
        }
        ViewElement currentElement = viewElements.get(i);
        currentElement.getNode().getStyleClass().remove("fx-border-color");
        currentElement.getNode().setStyle("-fx-border-color: lightblue");
    //    System.out.println("current: " + currentElement.getNode().getStyle());


    }

    void setContentSpinners(int index, Spinner heightSpinner, Spinner widthSpinner) {
        if (currentSpinnerNode != viewElements.get(index).getNode()) {
            currentSpinnerNode = viewElements.get(index).getNode();
            mainView.updateSizeSpinners(heightSpinner, widthSpinner);
        }
    }

    /**
     * The list of view elements
     * @return the list
     */
    List<ViewElement> getViewElements() {      return viewElements;  }

    /**
     * The indent button
     * @return the button
     */
    Button getIndentButton() {     return indentButton;  }

    /**
     * The outdent button
     * @return the button
     */
    Button getOutdentButton() {     return outdentButton;   }

    /**
     * The button to remove a current element
     * @return the button
     */
    Button getRemoveButton() {     return removeButton;   }

    /**
     * Button to restore last removed element
     * @return the button
     */
    Button getRestoreButton() {     return restoreButton;  }

    /**
     * Button to move current element up
     * @return the button
     */
    Button getMoveUpButton() { return moveUpButton; }

    /**
     * Button to move current element down
     * @return the button
     */
    Button getMoveDownButton() { return moveDownButton; }

    /**
     * Button to add simple edit element
     * @return the button
     */
    Button getAddEditButton() {     return addEditButton;  }

    /**
     * Button to add horizontal tree element
     * @return the button
     */
    Button getAddHTreeButton() {     return addHTreeButton;  }

    /**
     * Button to add truth table element
     * @return the button
     */
    Button getAddTTableButton() {      return addTTableButton;  }

    /**
     * Button to add vertical tree element (with Base/Ital default font)
     * @return the button
     */
    Button getAddVTreeBaseItalButton() {      return addVTreeBaseItalButton;   }

    /**
     * Button to add vertical tree element (with Ital/Sans default font)
     * @return the button
     */
    Button getAddVTreeItalSansButton() {     return addVTreeItalSansButton;   }

    /**
     * Button to add vertical tree element (with Script/Ital default font)
     * @return the button
     */
    Button getAddVTreeScriptItalButton() { return addVTreeScriptItalButton; }

    /**
     * Button to add natural derivation element (with Ital/Sans default font)
     * @return the button
     */
    Button getAddNDrvtnItalSansButton() {     return addNDrvtnItalSansButton;   }

    /**
     * Button to add natural derivation element (with Script/Ital default font)
     * @return the button
     */
    Button getAddNDrvtnScriptItalButton() {    return addNDrvtnScriptItalButton;   }

    /**
     * Button to add natural derivation element (with Script/Sans default font)
     * @return the button
     */
    Button getAddNDrvtnScriptSansButton() {    return addNDrvtnScriptSansButton;   }

    /**
     * Button to add natural derivation element (with Ital/Blackboard default font)
     * @return the button
     */
    Button getAddNDrvtnItalBBButton() {     return addNDrvtnItalBBButton;  }

    /**
     * Button to add axiomatic derivation element (with Ital/Sans default font)
     * @return the button
     */
    Button getAddADrvtnItalSansButton() {     return addADrvtnItalSansButton;   }

    /**
     * Button to add axiomatic derivation element (with Script/Ital default font)
     * @return the button
     */
    Button getAddADrvtnScriptItalButton() {     return addADrvtnScriptItalButton;  }

    /**
     * Button to add axiomatic derivation element (with Script/Sans default font)
     * @return the button
     */
    Button getAddADrvtnScriptSansButton() {     return addADrvtnScriptSansButton;  }

    /**
     * Button to add axiomatic derivation element (with Ital/Blackboard default font)
     * @return the button
     */
    Button getAddADrvtnItalBBButton() {     return addADrvtnItalBBButton;   }

    /**
     * The preferred height of the comment window
     * @return the height value
     */
    double getCommentPrefHeight() {    return commentDRTA.getEditor().getPrefHeight(); }

    /**
     * The preferred height of the comment window
     * @param commentPrefHeight the height value
     */
    void setCommentPrefHeight(double commentPrefHeight) {     this.commentPrefHeight = commentPrefHeight;   }

    /**
     * The (left) control node for the current free form element
     * @param exerciseControlNode the control node
     */
    void setExerciseControlNode(Node exerciseControlNode) {   this.exerciseControlNode = exerciseControlNode;  }

    /**
     * The comment decoratedRTA
     * @return comment DecoratedRTA
     */
    @Override
    public DecoratedRTA getExerciseComment() { return commentDRTA;  }

    /**
     * The comment decoratedRTA
     * @param exerciseComment DecoratedRTA
     */
    @Override
    public void setExerciseComment(DecoratedRTA exerciseComment) { commentDRTA = exerciseComment;   }

    /**
     * The exercise prompt
     * @return the statement decorated RTA (T)
     */
    @Override
    public DecoratedRTA getExerciseStatement() {  return statementDRTA;   }

    /**
     * The exercise prompt
     * @param exerciseStatement the statement decorated RTA (T)
     */
    @Override
    public void setExerciseStatement(DecoratedRTA exerciseStatement) { statementDRTA = exerciseStatement;   }

    /**
     * The exercise prompt node (RTA)
     * @return the statement node
     */
    @Override
    public Node getExerciseStatementNode() { return statementDRTA.getEditor();  }

    /**
     * The preferred height of the statement window
     * @param height the preferred height
     */
    @Override
    public void setStatementPrefHeight(double height) {
        statementPrefHeight = height;
        statementDRTA.getEditor().setPrefHeight(height);
    }

    /**
     * The node which includes content of this exercise
     * @return the content node
     */
    @Override
    public Node getExerciseContentNode() {   return contentBox;   }

    /**
     * The left control node for this exercise
     * @return the control node
     */
    @Override
    public Node getExerciseControl() {  return exerciseControlNode;   }

    /**
     * The right control node with controls for the exercise as a whole
     * @return
     */
    @Override
    public Node getRightControl() { return freeFormControlNode; }

    @Override
    public Node getPointsNode() {
        if (pointsPossible > 0) {
            if (!mainView.isInstructorFunctions()) pointsEarnedTextField.setDisable(true);
            return pointsEarnedTextField;
        }
        return null;
    }

    public void setPointsPossible(int pointsPossible) {
        this.pointsPossible = pointsPossible;
    }

    @Override
    public int getPointsPossible() { return pointsPossible;  }

    public TextField getPointsEarnedTextField() {
        return pointsEarnedTextField;
    }

}

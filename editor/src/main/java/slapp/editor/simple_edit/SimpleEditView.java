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
import com.gluonhq.richtextarea.model.Document;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import slapp.editor.EditorMain;
import slapp.editor.decorated_rta.DecoratedRTA;
import slapp.editor.main_window.ExerciseView;
import slapp.editor.main_window.MainWindowView;

import java.util.function.UnaryOperator;

import static slapp.editor.EditorMain.RTA_SCREEN_BMP;
import static slapp.editor.EditorMain.RTA_WIDTH_BMP;

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
    private VBox controlBox;
    private Button staticHelpButton;
    private Stage staticHelpStage;
    private Node rightControlNode;

    private final DoubleProperty localScalePageHeight = new SimpleDoubleProperty(1.0);
    private final DoubleProperty localScalePageWidth = new SimpleDoubleProperty(1.0);

    // SCORCHED EARTH RULE 2: Kill Switch for asynchronous event interception during teardown
    private boolean isTornDown = false;

    // SCORCHED EARTH RULE 3: Eradicate Anonymous Lambdas
    private ChangeListener<Node> exerciseFocusListener;
    private ChangeListener<Number> localScalePageHeightListener;
    private UnaryOperator<TextFormatter.Change> pointsEarnedFilter;

    // SCORCHED EARTH RULE 3: Eradicate Anonymous Lambdas

    private ChangeListener<Boolean> pointsEarnedFocusListener;
    private EventHandler<MouseEvent> pointsEarnedClickHandler;




    /**
     * Create simple edit view
     * @param mainView the main view
     */
    public SimpleEditView(MainWindowView mainView) {
        this.mainView = mainView;

        localScalePageHeight.bind(mainView.scalePageHeightProperty());
        localScalePageWidth.bind(mainView.scalePageWidthProperty());

        exerciseFocusListener = (ob, ov, nv) -> {
            if (isTornDown || this.mainView == null) return;
            try {
                RichTextArea statementRTA = exerciseStatement != null ? exerciseStatement.getEditor() : null;
                RichTextArea commentRTA = exerciseComment != null ? exerciseComment.getEditor() : null;
                RichTextArea responseRTA = exerciseResponse != null ? exerciseResponse.getEditor() : null;

                if (statementRTA != null && inHierarchy(nv, statementRTA)) {
                    if (currentSpinnerNode != statementRTA) {
                        currentSpinnerNode = statementRTA;
                        if (statementHeightSpinner != null && statementWidthSpinner != null) {
                            this.mainView.updateSizeSpinners(statementHeightSpinner, statementWidthSpinner);
                        }
                    }
                } else if (commentRTA != null && inHierarchy(nv, commentRTA)) {
                    if (currentSpinnerNode != commentRTA) {
                        currentSpinnerNode = commentRTA;
                        if (commentHeightSpinner != null && commentWidthSpinner != null) {
                            this.mainView.updateSizeSpinners(commentHeightSpinner, commentWidthSpinner);
                        }
                    }
                } else if (responseRTA != null && inHierarchy(nv, responseRTA)) {
                    if (currentSpinnerNode != responseRTA) {
                        currentSpinnerNode = responseRTA;
                        if (responseHeightSpinner != null && responseWidthSpinner != null) {
                            this.mainView.updateSizeSpinners(responseHeightSpinner, responseWidthSpinner);
                        }
                    }
                } else {
                    // THE FIX: Focus moved outside our recognized editors (e.g., to another exercise).
                    // Clear the cached state so the next click definitively forces a toolbar update!
                    currentSpinnerNode = null;
                }
            } catch (Exception e) {}
        };


        Region spacer = new Region();
        spacer.setMinHeight(40);
        spacer.setPrefWidth(150);

        controlBox = (VBox) exerciseControlNode;
        controlBox.getChildren().add(spacer);
        controlBox.setAlignment(Pos.TOP_CENTER);
        controlBox.setMinWidth(150); controlBox.setMaxWidth(150);
        controlBox.setPadding(new Insets(200,20,0,30));

        pointsEarnedTextField = new TextField();
        pointsEarnedTextField.setPrefWidth(27);
        pointsEarnedTextField.setAlignment(Pos.CENTER_RIGHT);
        pointsEarnedTextField.setPadding(new Insets(0,5,0,3));

        pointsEarnedFilter = change -> {
            if (isTornDown || change == null) return null;
            String text = change.getText();
            if (text.matches("[0-9]*")) return change;
            return null;
        };
        TextFormatter<String> textFormatter = new TextFormatter<>(pointsEarnedFilter);
        pointsEarnedTextField.setTextFormatter(textFormatter);

        pointsEarnedFocusListener = (obs, wasFocused, isNowFocused) -> {
            if (isTornDown || pointsEarnedTextField == null) return;
            if (isNowFocused) {
                Platform.runLater(() -> {
                    if (!isTornDown && pointsEarnedTextField != null) {
                        pointsEarnedTextField.selectAll();
                    }
                });
                if (mainView != null) {
                    mainView.textFieldInFocus();
                }
            } else if (pointsEarnedTextField.getText().isEmpty()) {
                pointsEarnedTextField.setText("0");
            }
        };
        pointsEarnedTextField.focusedProperty().addListener(pointsEarnedFocusListener);

        pointsEarnedClickHandler = event -> {
            if (isTornDown || pointsEarnedTextField == null) return;
            if (pointsEarnedTextField.getSelectedText().isEmpty()) {
                pointsEarnedTextField.selectAll();
            }
        };
        pointsEarnedTextField.setOnMouseClicked(pointsEarnedClickHandler);

    }

    /**
     * Initialize statement, comment, and response areas including text height controls
     */
    void initializeViewDetails() {
        double spinnerMin = (double) ((int) (100 * 45.0 / localScalePageHeight.get()));

        //statement rta
        RichTextArea statementRTA = exerciseStatement.getEditor();
        statementRTA.getStylesheets().add("slappTextArea.css");
        statementRTA.setEditable(false);

        double statementInitialHeight = Math.ceil((statementPrefHeight - RTA_SCREEN_BMP) / localScalePageHeight.get() * 100.0);
        statementHeightSpinner = new Spinner<>(spinnerMin, 999.0, statementInitialHeight, 1.0);
        statementHeightSpinner.setPrefWidth(65);
        statementHeightSpinner.setDisable(false);
        statementHeightSpinner.setTooltip(new Tooltip("Window height as % of selected paper"));
        statementRTA.prefHeightProperty().bind(Bindings.add(Bindings.max(45.0, Bindings.multiply(localScalePageHeight, DoubleProperty.doubleProperty(statementHeightSpinner.getValueFactory().valueProperty()).divide(100.0))), RTA_SCREEN_BMP));

        statementRTA.prefWidthProperty().bind(Bindings.add(localScalePageWidth, RTA_WIDTH_BMP));
        statementRTA.contentAreaWidthProperty().bind(localScalePageWidth);

        statementWidthSpinner = new Spinner<>(0.0, 999.0, 100, 1.0);
        statementWidthSpinner.setPrefWidth(65);
        statementWidthSpinner.setDisable(true);
        statementWidthSpinner.setTooltip(new Tooltip("Window width as % of selected paper"));

        //comment rta
        RichTextArea commentRTA = exerciseComment.getEditor();
        commentRTA.getStylesheets().add("slappTextArea.css");
        commentRTA.setPromptText("Comment:");

        double commentInitialHeight = Math.round((commentPrefHeight - RTA_SCREEN_BMP) / localScalePageHeight.get() * 100.0);
        commentHeightSpinner = new Spinner<>(spinnerMin, 999.0, commentInitialHeight, 1.0);
        commentHeightSpinner.setPrefWidth(65);
        commentHeightSpinner.setDisable(false);
        commentHeightSpinner.setTooltip(new Tooltip("Window height as % of selected paper"));

        commentRTA.prefHeightProperty().bind(Bindings.add(Bindings.max(45.0, Bindings.multiply(localScalePageHeight, DoubleProperty.doubleProperty(commentHeightSpinner.getValueFactory().valueProperty()).divide(100.0))), RTA_SCREEN_BMP));

        commentRTA.prefWidthProperty().bind(Bindings.add(localScalePageWidth, RTA_WIDTH_BMP));
        commentRTA.contentAreaWidthProperty().bind(localScalePageWidth);
        commentWidthSpinner = new Spinner<>(0.0, 999.0, 100, 1.0);
        commentWidthSpinner.setPrefWidth(65);
        commentWidthSpinner.setDisable(true);
        commentWidthSpinner.setTooltip(new Tooltip("Window width as % of selected paper"));

        //response rta
        RichTextArea responseRTA = exerciseResponse.getEditor();
        responseRTA.getStylesheets().add("slappTextArea.css");
        responseRTA.setPromptText(responsePrompt);
        double responseInitialHeight = Math.round((responsePrefHeight - RTA_SCREEN_BMP) / localScalePageHeight.get() * 100);

        responseHeightSpinner = new Spinner<>(spinnerMin, 999.0, responseInitialHeight, 1.0);
        responseHeightSpinner.setPrefWidth(65);
        responseHeightSpinner.setDisable(false);
        responseHeightSpinner.setTooltip(new Tooltip("Window height as % of selected paper"));
        responseRTA.prefHeightProperty().bind(Bindings.add(Bindings.max(45.0, Bindings.multiply(localScalePageHeight, DoubleProperty.doubleProperty(responseHeightSpinner.getValueFactory().valueProperty()).divide(100.0))), RTA_SCREEN_BMP));
        responseRTA.maxHeightProperty().bind(responseRTA.prefHeightProperty());

        responseRTA.prefWidthProperty().bind(Bindings.add(localScalePageWidth, RTA_WIDTH_BMP));
        responseRTA.contentAreaWidthProperty().bind(localScalePageWidth);
        responseWidthSpinner = new Spinner<>(0.0, 999.0, 100, 5.0);
        responseWidthSpinner.setPrefWidth(65);
        responseWidthSpinner.setDisable(true);
        responseWidthSpinner.setTooltip(new Tooltip("Window width as % of selected paper"));

        //page height listener
        localScalePageHeightListener = (ob, ov, nv) -> {
            if (isTornDown || this.mainView == null) return;
            try {
                double revSpinnerMin = (double) ((int) (100 * 45.0 / (double) nv));

                if (exerciseStatement != null && exerciseStatement.getEditor() != null) {
                    exerciseStatement.getEditor().prefHeightProperty().unbind();
                    if (statementHeightSpinner != null && statementHeightSpinner.getValueFactory() != null) {
                        statementHeightSpinner.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(revSpinnerMin, 999.0, (double) Math.round(statementHeightSpinner.getValue() * ov.doubleValue() / nv.doubleValue()), 1.0));
                        exerciseStatement.getEditor().prefHeightProperty().bind(Bindings.add(Bindings.max(45.0, Bindings.multiply(nv.doubleValue(), DoubleProperty.doubleProperty(statementHeightSpinner.getValueFactory().valueProperty()).divide(100.0))), RTA_SCREEN_BMP));
                    }
                }

                if (exerciseComment != null && exerciseComment.getEditor() != null) {
                    exerciseComment.getEditor().prefHeightProperty().unbind();
                    if (commentHeightSpinner != null && commentHeightSpinner.getValueFactory() != null) {
                        commentHeightSpinner.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(revSpinnerMin, 999.0, (double) Math.round(commentHeightSpinner.getValue() * ov.doubleValue() / nv.doubleValue()), 1.0));
                        exerciseComment.getEditor().prefHeightProperty().bind(Bindings.add(Bindings.max(45.0, Bindings.multiply(nv.doubleValue(), DoubleProperty.doubleProperty(commentHeightSpinner.getValueFactory().valueProperty()).divide(100.0))), RTA_SCREEN_BMP));
                    }
                }

                if (exerciseResponse != null && exerciseResponse.getEditor() != null) {
                    exerciseResponse.getEditor().prefHeightProperty().unbind();
                    if (responseHeightSpinner != null && responseHeightSpinner.getValueFactory() != null) {
                        responseHeightSpinner.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(revSpinnerMin, 999.0, (double) Math.round(responseHeightSpinner.getValue() * ov.doubleValue() / nv.doubleValue()), 1.0));
                        exerciseResponse.getEditor().prefHeightProperty().bind(Bindings.add(Bindings.max(45.0, Bindings.multiply(nv.doubleValue(), DoubleProperty.doubleProperty(responseHeightSpinner.getValueFactory().valueProperty()).divide(100.0))), RTA_SCREEN_BMP));
                    }
                }
            } catch (Exception e) {}
        };
        localScalePageHeight.addListener(localScalePageHeightListener);

        setSizeSpinners();
    }

    public void setRightControlBox() {
        staticHelpButton = new Button("Static Help");
        staticHelpButton.setPrefWidth(105);
        staticHelpButton.setTooltip(new Tooltip("Get static help text"));

        VBox rightControlBox = new VBox(40, staticHelpButton);
        rightControlBox.setAlignment(Pos.TOP_CENTER);
        rightControlBox.setPadding(new Insets(200,20,0,20));
        rightControlNode = rightControlBox;
    }

    public void showStaticHelp(Document doc) {
        if (isTornDown) return;
        if (staticHelpStage == null || !staticHelpStage.isShowing()) {
            RichTextArea hrta = new RichTextArea(EditorMain.mainStage);
            hrta.getActionFactory().open(doc).execute(new ActionEvent());
            hrta.setPadding(new Insets(20, 0, 20, 20));
            hrta.setBackground(new Background(new BackgroundFill(Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY)));
            hrta.setContentAreaWidth(400);
            hrta.setPrefWidth(400 + RTA_WIDTH_BMP);
            hrta.setPrefHeight(300);
            hrta.setEditable(false);

            Scene scene = new Scene(hrta);
            scene.getStylesheets().add(RichTextArea.class.getClassLoader().getResource("slappEditor.css").toExternalForm());
            hrta.applyCss();
            staticHelpStage = new Stage();
            staticHelpStage.setScene(scene);
            staticHelpStage.setTitle("SLAPP Text Help");
            staticHelpStage.initModality(Modality.NONE);
            staticHelpStage.getIcons().addAll(EditorMain.icons);
            staticHelpStage.initOwner(EditorMain.mainStage);
            Rectangle2D bounds = MainWindowView.getCurrentScreenBounds();
            staticHelpStage.setX(Math.min(EditorMain.mainStage.getX() + EditorMain.mainStage.getWidth(), bounds.getMaxX() - (420 + RTA_WIDTH_BMP)));
            staticHelpStage.setY(Math.min(EditorMain.mainStage.getY() + 20, bounds.getMaxY() - 320));

            staticHelpStage.show();
        }
    }

    private void setSizeSpinners() {
        try {
            if (mainView != null && mainView.getMainScene() != null && exerciseFocusListener != null) {
                mainView.getMainScene().focusOwnerProperty().addListener(exerciseFocusListener);
            }
        } catch (Exception e) {}
    }

    private static boolean inHierarchy(Node node, Node potentialHierarchyElement) {
        if (potentialHierarchyElement == null) return true;
        while (node != null) {
            if (node == potentialHierarchyElement) return true;
            node = node.getParent();
        }
        return false;
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

    public void cleanup() {
        // SCORCHED EARTH RULE 2: Engage Kill Switch
        isTornDown = true;

        try {
            if (pointsEarnedTextField != null) {
                if (pointsEarnedFocusListener != null) {
                    pointsEarnedTextField.focusedProperty().removeListener(pointsEarnedFocusListener);
                }
                if (pointsEarnedClickHandler != null) {
                    pointsEarnedTextField.removeEventHandler(MouseEvent.MOUSE_CLICKED, pointsEarnedClickHandler);
                }
                pointsEarnedTextField.setOnMouseClicked(null);
                pointsEarnedTextField.setTextFormatter(null);
            }
        } catch (Exception e) {}

        try {
            if (localScalePageHeight != null) {
                if (localScalePageHeightListener != null) localScalePageHeight.removeListener(localScalePageHeightListener);
                localScalePageHeight.unbind();
            }
        } catch (Exception e) {}

        try {
            if (localScalePageWidth != null) {
                localScalePageWidth.unbind();
            }
        } catch (Exception e) {}

        try {
            if (mainView != null && mainView.getMainScene() != null && exerciseFocusListener != null) {
                mainView.getMainScene().focusOwnerProperty().removeListener(exerciseFocusListener);
            }
        } catch (Exception e) {}

        try {
            if (pointsEarnedTextField != null) {
                pointsEarnedTextField.setTextFormatter(null);
            }
        } catch (Exception e) {}

        try {
            if (staticHelpButton != null) {
                staticHelpButton.setOnAction(null);
            }
        } catch (Exception e) {}

        try {
            if (exerciseStatement != null && exerciseStatement.getEditor() != null) {
                exerciseStatement.getEditor().prefHeightProperty().unbind();
                exerciseStatement.getEditor().prefWidthProperty().unbind();
                exerciseStatement.getEditor().contentAreaWidthProperty().unbind();
            }
        } catch (Exception e) {}

        try {
            if (exerciseComment != null && exerciseComment.getEditor() != null) {
                exerciseComment.getEditor().prefHeightProperty().unbind();
                exerciseComment.getEditor().prefWidthProperty().unbind();
                exerciseComment.getEditor().contentAreaWidthProperty().unbind();
            }
        } catch (Exception e) {}

        try {
            if (exerciseResponse != null && exerciseResponse.getEditor() != null) {
                exerciseResponse.getEditor().prefHeightProperty().unbind();
                exerciseResponse.getEditor().maxHeightProperty().unbind();
                exerciseResponse.getEditor().prefWidthProperty().unbind();
                exerciseResponse.getEditor().contentAreaWidthProperty().unbind();
            }
        } catch (Exception e) {}

        // ---> SCORCHED EARTH RULE 4: THE JAVAFX SKIN HAMMER (Async Separation) <---
        try {
            final RichTextArea stEditor = (exerciseStatement != null) ? exerciseStatement.getEditor() : null;
            final RichTextArea commEditor = (exerciseComment != null) ? exerciseComment.getEditor() : null;
            final RichTextArea respEditor = (exerciseResponse != null) ? exerciseResponse.getEditor() : null;
            final RichTextArea helpEditor = (staticHelpStage != null && staticHelpStage.getScene() != null
                    && staticHelpStage.getScene().getRoot() instanceof RichTextArea)
                    ? (RichTextArea) staticHelpStage.getScene().getRoot() : null;

            // STAGE 1: Execute async document injection IMMEDIATELY and synchronously clear prompt texts to disarm the NPE listener
            try { if (stEditor != null) { stEditor.setPromptText(""); stEditor.setFocusTraversable(false); if (stEditor.getDocument() == null || stEditor.getDocument().getText().isEmpty()) stEditor.getActionFactory().open(new Document(" ")).execute(new ActionEvent()); } } catch (Exception ex) {}
            try { if (commEditor != null) { commEditor.setPromptText(""); commEditor.setFocusTraversable(false); if (commEditor.getDocument() == null || commEditor.getDocument().getText().isEmpty()) commEditor.getActionFactory().open(new Document(" ")).execute(new ActionEvent()); } } catch (Exception ex) {}
            try { if (respEditor != null) { respEditor.setPromptText(""); respEditor.setFocusTraversable(false); if (respEditor.getDocument() == null || respEditor.getDocument().getText().isEmpty()) respEditor.getActionFactory().open(new Document(" ")).execute(new ActionEvent()); } } catch (Exception ex) {}
            try { if (helpEditor != null) { helpEditor.setPromptText(""); helpEditor.setFocusTraversable(false); if (helpEditor.getDocument() == null || helpEditor.getDocument().getText().isEmpty()) helpEditor.getActionFactory().open(new Document(" ")).execute(new ActionEvent()); } } catch (Exception ex) {}

            // STAGE 2: Wait 1.5s for ALL background tasks and internal Gluon listeners to complete BEFORE safely nulling the skin.
            if (stEditor != null || commEditor != null || respEditor != null || helpEditor != null) {
                javafx.animation.PauseTransition pt = new javafx.animation.PauseTransition(javafx.util.Duration.millis(1500));
                pt.setOnFinished(ev -> {
                    try { if (stEditor != null) stEditor.setSkin(null); } catch (Exception ex) {}
                    try { if (commEditor != null) commEditor.setSkin(null); } catch (Exception ex) {}
                    try { if (respEditor != null) respEditor.setSkin(null); } catch (Exception ex) {}
                    try { if (helpEditor != null) helpEditor.setSkin(null); } catch (Exception ex) {}
                });
                pt.play();
            }
        } catch (Exception e) {}

        // SCORCHED EARTH RULE 6: Disconnect UI Controls
        try {
            if (controlBox != null) {
                severFromParent(controlBox);
                controlBox.getChildren().clear();
            }
        } catch (Exception e) {}

        try {
            if (exerciseControlNode != null) {
                severFromParent(exerciseControlNode);
                if (exerciseControlNode instanceof javafx.scene.layout.Pane) ((javafx.scene.layout.Pane) exerciseControlNode).getChildren().clear();
            }
        } catch (Exception e) {}

        try {
            if (rightControlNode != null) {
                severFromParent(rightControlNode);
                if (rightControlNode instanceof javafx.scene.layout.Pane) ((javafx.scene.layout.Pane) rightControlNode).getChildren().clear();
            }
        } catch (Exception e) {}

        try {
            if (staticHelpStage != null) {
                staticHelpStage.close();
                if (staticHelpStage.getScene() != null) {
                    staticHelpStage.getScene().setRoot(new Pane());
                }
                staticHelpStage.setScene(null);
            }
        } catch (Exception e) {}

        // SCORCHED EARTH RULE 1: Final Nullification
        this.mainView = null;
        this.exerciseStatement = null;
        this.exerciseComment = null;
        this.exerciseResponse = null;
        this.responsePrompt = null;
        this.exerciseControlNode = null;
        this.statementHeightSpinner = null;
        this.statementWidthSpinner = null;
        this.commentHeightSpinner = null;
        this.commentWidthSpinner = null;
        this.responseHeightSpinner = null;
        this.responseWidthSpinner = null;
        this.currentSpinnerNode = null;
        this.pointsEarnedTextField = null;
        this.controlBox = null;
        this.staticHelpButton = null;
        this.staticHelpStage = null;
        this.rightControlNode = null;

        this.exerciseFocusListener = null;
        this.localScalePageHeightListener = null;
        this.pointsEarnedFilter = null;

        this.exerciseFocusListener = null;
        this.localScalePageHeightListener = null;
        this.pointsEarnedFilter = null;
        this.pointsEarnedFocusListener = null;
        this.pointsEarnedClickHandler = null;
    }



    public Button getStaticHelpButton() { return staticHelpButton; }
    public Stage getStaticHelpStage() { return staticHelpStage; }

    public void setResponsePrompt(String prompt) { responsePrompt = prompt; }

    public double getCommentPrefHeight() { return exerciseComment.getEditor().getPrefHeight(); }
    public void setCommentPrefHeight(double commentPrefHeight) { this.commentPrefHeight = commentPrefHeight; }

    public double getResponsePrefHeight() { return exerciseResponse.getEditor().getPrefHeight(); }
    public void setResponsePrefHeight(double responsePrefHeight) { this.responsePrefHeight = responsePrefHeight; }

    public DecoratedRTA getExerciseResponse() { return exerciseResponse; }
    public void setExerciseResponse(DecoratedRTA exerciseResponse) { this.exerciseResponse = exerciseResponse; }

    public Node getFFViewNode() { return exerciseResponse.getEditor(); }
    Spinner getResponseHeightSpinner() {return responseHeightSpinner;}
    Spinner getResponseWidthSpinner() {return responseWidthSpinner;}

    public VBox getControlBox() { return controlBox; }

    @Override
    public DecoratedRTA getExerciseComment() { return exerciseComment; }
    @Override
    public void setExerciseComment(DecoratedRTA exerciseComment) { this.exerciseComment = exerciseComment; }

    @Override
    public DecoratedRTA getExerciseStatement() { return exerciseStatement; }
    @Override
    public void setExerciseStatement(DecoratedRTA exerciseStatement) { this.exerciseStatement = exerciseStatement; }

    @Override
    public Node getExerciseStatementNode() { return exerciseStatement.getEditor(); }

    public void setStatementPrefHeight(double height) {
        statementPrefHeight = height;
        if (exerciseStatement != null && exerciseStatement.getEditor() != null) {
            exerciseStatement.getEditor().setPrefHeight(height);
        }
    }

    @Override
    public Node getExerciseContentNode() { return exerciseResponse != null ? exerciseResponse.getEditor() : null; }

    @Override
    public Node getExerciseControl() { return exerciseControlNode; }

    @Override
    public Node getRightControl() { return rightControlNode; }

    public void setRightControlNode(Node rightControlNode) { this.rightControlNode = rightControlNode; }

    @Override
    public Node getPointsNode() {
        if (pointsPossible > 0) {
            if (mainView != null && !mainView.isInstructorFunctions() && pointsEarnedTextField != null) {
                pointsEarnedTextField.setDisable(true);
            }
            return pointsEarnedTextField;
        }
        return null;
    }

    public void setPointsPossible(int pointsPossible) { this.pointsPossible = pointsPossible; }

    @Override
    public int getPointsPossible() { return pointsPossible; }

    public TextField getPointsEarnedTextField() { return pointsEarnedTextField; }

    public DoubleProperty localScalePageHeightProperty() { return localScalePageHeight; }
    public DoubleProperty localScalePageWidthProperty() { return localScalePageWidth; }

    public ChangeListener<Node> getExerciseFocusListener() { return exerciseFocusListener; }
}
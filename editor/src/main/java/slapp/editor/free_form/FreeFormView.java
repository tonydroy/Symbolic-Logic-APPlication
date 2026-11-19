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

import java.util.ArrayList;
import java.util.List;
import java.util.function.UnaryOperator;

import static slapp.editor.EditorMain.RTA_SCREEN_BMP;
import static slapp.editor.EditorMain.RTA_WIDTH_BMP;

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

    private Button staticHelpButton;
    private Stage staticHelpStage;
    private Node rightControlNode;

    private final DoubleProperty localScalePageHeight = new SimpleDoubleProperty(1.0);
    private final DoubleProperty localScalePageWidth = new SimpleDoubleProperty(1.0);

    // SCORCHED EARTH RULE 1: The Asynchronous Ghost "Kill Switch"
    private boolean isTornDown = false;

    // SCORCHED EARTH RULE 3: Eradicate Anonymous Lambdas
    private UnaryOperator<TextFormatter.Change> pointsFilter;
    private EventHandler<MouseEvent> statementClickHandler;
    private EventHandler<MouseEvent> commentClickHandler;
    private ChangeListener<Number> localScalePageHeightListener;
    private EventHandler<ActionEvent> staticHelpButtonHandler;

    private ChangeListener<Node> exerciseFocusListener;
    private ChangeListener<Boolean> pointsEarnedFocusListener;
    private EventHandler<MouseEvent> pointsEarnedClickHandler;

    /**
     * Construct the free form view
     * @param mainView the main view
     */
    public FreeFormView(MainWindowView mainView) {
        this.mainView = mainView;

        localScalePageHeight.bind(mainView.scalePageHeightProperty());
        localScalePageWidth.bind(mainView.scalePageWidthProperty());

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

        exerciseFocusListener = (ob, ov, nv) -> {
            if (isTornDown || this.mainView == null) return;
            try {
                RichTextArea statementRTA = statementDRTA != null ? statementDRTA.getEditor() : null;
                RichTextArea commentRTA = commentDRTA != null ? commentDRTA.getEditor() : null;

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
                } else {
                    // Shield the child exercise toolbars from being overwritten
                    boolean foundChildNode = false;
                    if (viewElements != null) {
                        for (int i = 0; i < viewElements.size(); i++) {
                            if (viewElements.get(i) != null && viewElements.get(i).getNode() != null) {
                                Node viewNode = viewElements.get(i).getNode();
                                if (inHierarchy(nv, viewNode)) {
                                    if (currentSpinnerNode != viewNode) {
                                        currentSpinnerNode = viewNode;
                                    }
                                    foundChildNode = true;
                                    break;
                                }
                            }
                        }
                    }

                    // The magic fix: Clear cached state when focus completely leaves tracked areas!
                    if (!foundChildNode) {
                        currentSpinnerNode = null;
                    }
                }
            } catch (Exception e) {}
        };


        VBox rightControlBox = new VBox(20);
        rightControlBox.setAlignment(Pos.BASELINE_LEFT);
        rightControlBox.setPadding(new Insets(40,20,0,20));
        freeFormControlNode = rightControlBox;

        pointsEarnedTextField = new TextField();
        pointsEarnedTextField.setPrefWidth(27);
        pointsEarnedTextField.setAlignment(Pos.CENTER_RIGHT);
        pointsEarnedTextField.setPadding(new Insets(0,5,0,3));

        pointsFilter = change -> {
            if (isTornDown || change == null) return null;
            String text = change.getText();
            if (text.matches("[0-9]*")) return change;
            return null;
        };
        TextFormatter<String> textFormatter = new TextFormatter<>(pointsFilter);
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


        VBox controlBox = new VBox();
        controlBox.setMinWidth(150);
        controlBox.setMaxWidth(150);
        exerciseControlNode = controlBox;
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
        if (isTornDown || this.mainView == null || statementDRTA == null || commentDRTA == null) return;
        double spinnerMin = (double) ((int) (100 * 45.0 / localScalePageHeight.get()));

        //statement
        RichTextArea statementRTA = statementDRTA.getEditor();
        statementRTA.getStylesheets().add("slappTextArea.css");
        statementRTA.setEditable(false);

        double statementInitialHeight = Math.ceil((statementPrefHeight - RTA_SCREEN_BMP) / localScalePageHeight.get() * 100.0);
        statementHeightSpinner = new Spinner<>(spinnerMin, 999.0, statementInitialHeight, 1.0);
        statementHeightSpinner.setPrefWidth(65);
        statementHeightSpinner.setDisable(false);
        statementHeightSpinner.setTooltip(new Tooltip("Window height as % of selected paper"));
        statementRTA.prefHeightProperty().bind(Bindings.add(Bindings.max(45.0, Bindings.multiply(localScalePageHeight, DoubleProperty.doubleProperty(statementHeightSpinner.getValueFactory().valueProperty()).divide(100.0))), RTA_SCREEN_BMP));

        statementRTA.maxWidthProperty().bind(Bindings.add(localScalePageWidth, RTA_WIDTH_BMP));
        statementRTA.prefWidthProperty().bind(Bindings.add(localScalePageWidth, RTA_WIDTH_BMP));
        statementRTA.contentAreaWidthProperty().bind(localScalePageWidth);

        statementWidthSpinner = new Spinner<>(0.0, 999.0, 100, 1.0);
        statementWidthSpinner.setPrefWidth(65);
        statementWidthSpinner.setDisable(true);
        statementWidthSpinner.setTooltip(new Tooltip("Window width as % of selected paper"));

        /*
        statementClickHandler = e -> {
            if (isTornDown || this.mainView == null) return;
            if (currentSpinnerNode != statementRTA) {
                currentSpinnerNode = statementRTA;
                if (statementHeightSpinner != null && statementWidthSpinner != null) {
                    mainView.updateSizeSpinners(statementHeightSpinner, statementWidthSpinner);
                }
            }
        };
        statementRTA.addEventFilter(MouseEvent.MOUSE_CLICKED, statementClickHandler);
         */

        //comment rta
        RichTextArea commentRTA = commentDRTA.getEditor();
        commentRTA.getStylesheets().add("slappTextArea.css");
        commentRTA.setPromptText("Comment:");

        double commentInitialHeight = Math.round((commentPrefHeight - RTA_SCREEN_BMP) / localScalePageHeight.get() * 100.0);
        commentHeightSpinner = new Spinner<>(spinnerMin, 999.0, commentInitialHeight, 1.0);
        commentHeightSpinner.setPrefWidth(65);
        commentHeightSpinner.setDisable(false);
        commentHeightSpinner.setTooltip(new Tooltip("Window height as % of selected paper"));
        commentRTA.prefHeightProperty().bind(Bindings.add(Bindings.max(45.0, Bindings.multiply(localScalePageHeight, DoubleProperty.doubleProperty(commentHeightSpinner.getValueFactory().valueProperty()).divide(100.0))), RTA_SCREEN_BMP));

        commentRTA.maxWidthProperty().bind(Bindings.add(localScalePageWidth, RTA_WIDTH_BMP));
        commentRTA.prefWidthProperty().bind(Bindings.add(localScalePageWidth, RTA_WIDTH_BMP));
        commentRTA.contentAreaWidthProperty().bind(localScalePageWidth);

        commentWidthSpinner = new Spinner<>(0.0, 999.0, 100, 1.0);
        commentWidthSpinner.setPrefWidth(65);
        commentWidthSpinner.setDisable(true);
        commentWidthSpinner.setTooltip(new Tooltip("Window width as % of selected paper"));

        /*
        commentClickHandler = e -> {
            if (isTornDown || this.mainView == null) return;
            if (currentSpinnerNode != commentRTA) {
                currentSpinnerNode = commentRTA;
                if (commentHeightSpinner != null && commentWidthSpinner != null) {
                    mainView.updateSizeSpinners(commentHeightSpinner, commentWidthSpinner);
                }
            }
        };
        commentRTA.addEventFilter(MouseEvent.MOUSE_CLICKED, commentClickHandler);
         */

        //page height listener
        localScalePageHeightListener = (ob, ov, nv) -> {
            if (isTornDown || this.mainView == null || statementRTA == null || commentRTA == null) return;
            double revSpinnerMin = (double) ((int) (100 * 45.0 / (double) nv));

            statementRTA.prefHeightProperty().unbind();
            if (statementHeightSpinner != null && statementHeightSpinner.getValueFactory() != null) {
                statementHeightSpinner.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(revSpinnerMin, 999.0, (double) Math.round(statementHeightSpinner.getValue() * ov.doubleValue() / nv.doubleValue()), 1.0));
                statementRTA.prefHeightProperty().bind(Bindings.add(Bindings.max(45.0, Bindings.multiply(nv.doubleValue(), DoubleProperty.doubleProperty(statementHeightSpinner.getValueFactory().valueProperty()).divide(100.0))), RTA_SCREEN_BMP));
            }

            commentRTA.prefHeightProperty().unbind();
            if (commentHeightSpinner != null && commentHeightSpinner.getValueFactory() != null) {
                commentHeightSpinner.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(revSpinnerMin, 999.0, (double) Math.round(commentHeightSpinner.getValue() * ov.doubleValue() / nv.doubleValue()), 1.0));
                commentRTA.prefHeightProperty().bind(Bindings.add(Bindings.max(45.0, Bindings.multiply(nv.doubleValue(), DoubleProperty.doubleProperty(commentHeightSpinner.getValueFactory().valueProperty()).divide(100.0))), RTA_SCREEN_BMP));
            }
        };
        localScalePageHeight.addListener(localScalePageHeightListener);


        // Attach it to the Scene
        if (mainView != null && mainView.getMainScene() != null && exerciseFocusListener != null) {
            mainView.getMainScene().focusOwnerProperty().addListener(exerciseFocusListener);
        }


    }

    private static boolean inHierarchy(Node node, Node potentialHierarchyElement) {
        if (potentialHierarchyElement == null) return true;
        while (node != null) {
            if (node == potentialHierarchyElement) return true;
            node = node.getParent();
        }
        return false;
    }

    public void setRightControlBox() {
        staticHelpButton = new Button("Static Help");
        staticHelpButton.setPrefWidth(105);
        staticHelpButton.setTooltip(new Tooltip("Get static help text"));
    }

    public void showStaticHelp(Document doc) {
        if (isTornDown || this.mainView == null) return;

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

    /**
     * Update the content VBox from the view elements
     */
    void updateContentFromViewElements() {
        if (isTornDown || this.mainView == null || contentBox == null || viewElements == null) return;

        // SCORCHED EARTH RULE 8: Safely sever to prevent ConcurrentModificationException
        if (contentBox.getChildren() != null) {
            List<Node> childrenCopy = new ArrayList<>(contentBox.getChildren());
            for (Node node : childrenCopy) {
                severFromParent(node);
            }
            contentBox.getChildren().clear();
        }

        for (ViewElement element : viewElements) {
            if (element != null && element.getNode() != null) {
                Node node = element.getNode();
                Region spacer = new Region();
                spacer.setPrefWidth(20.0 * element.getIndentLevel());
                HBox windowBox = new HBox(spacer, node);
                contentBox.getChildren().add(windowBox);
            }
        }
    }

    void setBorders(int i) {
        if (isTornDown || this.mainView == null || viewElements == null) return;
        for (ViewElement element : viewElements) {
            if (element != null && element.getNode() != null) {
                element.getNode().getStyleClass().remove("-fx-border-color");
                element.getNode().setStyle("-fx-border-color: gainsboro");
            }
        }
        if (i >= 0 && i < viewElements.size() && viewElements.get(i) != null && viewElements.get(i).getNode() != null) {
            ViewElement currentElement = viewElements.get(i);
            currentElement.getNode().getStyleClass().remove("-fx-border-color");
            currentElement.getNode().setStyle("-fx-border-color: lightblue");
        }
    }

    void setContentSpinners(int index, Spinner heightSpinner, Spinner widthSpinner) {
        if (isTornDown || this.mainView == null || viewElements == null || index < 0 || index >= viewElements.size() || viewElements.get(index) == null || viewElements.get(index).getNode() == null) return;
        if (currentSpinnerNode != viewElements.get(index).getNode()) {
            currentSpinnerNode = viewElements.get(index).getNode();
            mainView.updateSizeSpinners(heightSpinner, widthSpinner);
        }
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
        // KILL SWITCH: Engaged!
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
            if (mainView != null && mainView.getMainScene() != null && exerciseFocusListener != null) {
                mainView.getMainScene().focusOwnerProperty().removeListener(exerciseFocusListener);
            }
        } catch (Exception e) {}


        // SCORCHED EARTH RULE 2: Safe Global State & Null-Poisoning Prevention
        try {
            if (mainView != null) {
                mainView.textFieldInFocus(); // Remove toolbars safely
            }
        } catch (Exception e) {}

        // SCORCHED EARTH RULE 3: Defensive unbinding and detachment via multiple try-catch blocks
        try {
            if (localScalePageHeight != null) {
                if (localScalePageHeightListener != null) localScalePageHeight.removeListener(localScalePageHeightListener);
                localScalePageHeight.unbind();
            }
        } catch (Exception e) {}

        try { if (localScalePageWidth != null) localScalePageWidth.unbind(); } catch (Exception e) {}

        try { if (pointsEarnedTextField != null) pointsEarnedTextField.setTextFormatter(null); } catch (Exception e) {}

    /*
    try { if (statementDRTA != null && statementDRTA.getEditor() != null && statementClickHandler != null) statementDRTA.getEditor().removeEventFilter(MouseEvent.MOUSE_CLICKED, statementClickHandler); } catch (Exception e) {}
    try { if (commentDRTA != null && commentDRTA.getEditor() != null && commentClickHandler != null) commentDRTA.getEditor().removeEventFilter(MouseEvent.MOUSE_CLICKED, commentClickHandler); } catch (Exception e) {}
     */

        // Clear floating stages
        try { if (staticHelpStage != null) staticHelpStage.close(); } catch (Exception e) {}

        // ---> SCORCHED EARTH RULE 4: THE ISOLATED JAVAFX SKIN HAMMER <---
        try {
            final RichTextArea stEditor = (statementDRTA != null) ? statementDRTA.getEditor() : null;
            final RichTextArea commEditor = (commentDRTA != null) ? commentDRTA.getEditor() : null;

            // Unbind size properties synchronously
            if (stEditor != null) { stEditor.prefHeightProperty().unbind(); stEditor.prefWidthProperty().unbind(); stEditor.maxWidthProperty().unbind(); stEditor.minWidthProperty().unbind(); stEditor.contentAreaWidthProperty().unbind(); }
            if (commEditor != null) { commEditor.prefHeightProperty().unbind(); commEditor.prefWidthProperty().unbind(); commEditor.maxWidthProperty().unbind(); commEditor.minWidthProperty().unbind(); commEditor.contentAreaWidthProperty().unbind(); }

            final List<RichTextArea> rtasToClear = new ArrayList<>();
            if (stEditor != null) rtasToClear.add(stEditor);
            if (commEditor != null) rtasToClear.add(commEditor);

            if (!rtasToClear.isEmpty()) {
                // STAGE 1 (Immediate): Prompt wipe, focus disable, and document injection
                for (RichTextArea rta : rtasToClear) {
                    if (rta != null) {
                        try { rta.setPromptText(""); } catch (Exception e) {}
                        try { rta.setFocusTraversable(false); } catch (Exception e) {}
                        try {
                            if (rta.getDocument() == null || rta.getDocument().getText().isEmpty()) {
                                rta.getActionFactory().open(new Document(" ")).execute(new ActionEvent());
                            }
                        } catch (Exception e) {}
                    }
                }

                // STAGE 2 (Delayed Skin Removal): Push skin removal 1500ms into the future
                javafx.animation.PauseTransition pt = new javafx.animation.PauseTransition(javafx.util.Duration.millis(1500));
                pt.setOnFinished(ev -> {
                    for (RichTextArea rta : rtasToClear) {
                        try {
                            if (rta != null) {
                                rta.setSkin(null);
                            }
                        } catch (Exception ex) {}
                    }
                });
                pt.play();
            }
        } catch (Exception e) {}

        // ---> 4. DISMANTLE THE UI TREE <---
        try {
            if (contentBox != null) {
                if (contentBox.getChildren() != null) {
                    List<Node> childrenCopy = new ArrayList<>(contentBox.getChildren());
                    for (Node child : childrenCopy) {
                        severFromParent(child);
                    }
                }
                contentBox.getChildren().clear();
                severFromParent(contentBox);
            }
        } catch (Exception e) {}

        try {
            if (freeFormControlNode != null && freeFormControlNode instanceof VBox) {
                VBox fbox = (VBox) freeFormControlNode;
                if (fbox.getChildren() != null) fbox.getChildren().clear();
                severFromParent(fbox);
            }
        } catch (Exception e) {}

        try { if (exerciseControlNode != null) severFromParent(exerciseControlNode); } catch (Exception e) {}
        try { if (rightControlNode != null) severFromParent(rightControlNode); } catch (Exception e) {}

        try { if (indentButton != null) indentButton.setOnAction(null); } catch (Exception e) {}
        try { if (outdentButton != null) outdentButton.setOnAction(null); } catch (Exception e) {}
        try { if (removeButton != null) removeButton.setOnAction(null); } catch (Exception e) {}
        try { if (restoreButton != null) restoreButton.setOnAction(null); } catch (Exception e) {}
        try { if (moveUpButton != null) moveUpButton.setOnAction(null); } catch (Exception e) {}
        try { if (moveDownButton != null) moveDownButton.setOnAction(null); } catch (Exception e) {}
        try { if (addEditButton != null) addEditButton.setOnAction(null); } catch (Exception e) {}
        try { if (addHTreeButton != null) addHTreeButton.setOnAction(null); } catch (Exception e) {}
        try { if (addTTableButton != null) addTTableButton.setOnAction(null); } catch (Exception e) {}
        try { if (addVTreeBaseItalButton != null) addVTreeBaseItalButton.setOnAction(null); } catch (Exception e) {}
        try { if (addVTreeItalSansButton != null) addVTreeItalSansButton.setOnAction(null); } catch (Exception e) {}
        try { if (addVTreeScriptItalButton != null) addVTreeScriptItalButton.setOnAction(null); } catch (Exception e) {}
        try { if (addNDrvtnItalSansButton != null) addNDrvtnItalSansButton.setOnAction(null); } catch (Exception e) {}
        try { if (addNDrvtnScriptItalButton != null) addNDrvtnScriptItalButton.setOnAction(null); } catch (Exception e) {}
        try { if (addNDrvtnScriptSansButton != null) addNDrvtnScriptSansButton.setOnAction(null); } catch (Exception e) {}
        try { if (addNDrvtnItalBBButton != null) addNDrvtnItalBBButton.setOnAction(null); } catch (Exception e) {}
        try { if (addADrvtnItalSansButton != null) addADrvtnItalSansButton.setOnAction(null); } catch (Exception e) {}
        try { if (addADrvtnScriptItalButton != null) addADrvtnScriptItalButton.setOnAction(null); } catch (Exception e) {}
        try { if (addADrvtnScriptSansButton != null) addADrvtnScriptSansButton.setOnAction(null); } catch (Exception e) {}
        try { if (addADrvtnItalBBButton != null) addADrvtnItalBBButton.setOnAction(null); } catch (Exception e) {}
        try {
            if (staticHelpButton != null) {
                if (staticHelpButtonHandler != null) staticHelpButton.removeEventHandler(ActionEvent.ACTION, staticHelpButtonHandler);
                staticHelpButton.setOnAction(null);
            }
        } catch (Exception e) {}

        // SCORCHED EARTH RULE 10: NON-DESTRUCTIVE Data Teardown
        try {
            if (viewElements != null) {
                viewElements = null; // Don't call .clear()
            }
        } catch (Exception e) {}

        // 6. Complete nullification
        try { this.mainView = null; } catch (Exception e) {}
        try { this.statementDRTA = null; } catch (Exception e) {}
        try { this.commentDRTA = null; } catch (Exception e) {}
        try { this.exerciseControlNode = null; } catch (Exception e) {}
        try { this.freeFormControlNode = null; } catch (Exception e) {}
        try { this.contentBox = null; } catch (Exception e) {}
        try { this.statementHeightSpinner = null; } catch (Exception e) {}
        try { this.statementWidthSpinner = null; } catch (Exception e) {}
        try { this.commentHeightSpinner = null; } catch (Exception e) {}
        try { this.commentWidthSpinner = null; } catch (Exception e) {}
        try { this.currentSpinnerNode = null; } catch (Exception e) {}
        try { this.indentButton = null; } catch (Exception e) {}
        try { this.outdentButton = null; } catch (Exception e) {}
        try { this.removeButton = null; } catch (Exception e) {}
        try { this.restoreButton = null; } catch (Exception e) {}
        try { this.moveUpButton = null; } catch (Exception e) {}
        try { this.moveDownButton = null; } catch (Exception e) {}
        try { this.addEditButton = null; } catch (Exception e) {}
        try { this.addHTreeButton = null; } catch (Exception e) {}
        try { this.addTTableButton = null; } catch (Exception e) {}
        try { this.addVTreeBaseItalButton = null; } catch (Exception e) {}
        try { this.addVTreeItalSansButton = null; } catch (Exception e) {}
        try { this.addVTreeScriptItalButton = null; } catch (Exception e) {}
        try { this.addNDrvtnItalSansButton = null; } catch (Exception e) {}
        try { this.addNDrvtnScriptItalButton = null; } catch (Exception e) {}
        try { this.addNDrvtnScriptSansButton = null; } catch (Exception e) {}
        try { this.addNDrvtnItalBBButton = null; } catch (Exception e) {}
        try { this.addADrvtnItalSansButton = null; } catch (Exception e) {}
        try { this.addADrvtnScriptItalButton = null; } catch (Exception e) {}
        try { this.addADrvtnScriptSansButton = null; } catch (Exception e) {}
        try { this.addADrvtnItalBBButton = null; } catch (Exception e) {}
        try { this.pointsEarnedTextField = null; } catch (Exception e) {}
        try { this.staticHelpButton = null; } catch (Exception e) {}
        try { this.staticHelpStage = null; } catch (Exception e) {}
        try { this.rightControlNode = null; } catch (Exception e) {}
        try { this.pointsFilter = null; } catch (Exception e) {}
        try { this.statementClickHandler = null; } catch (Exception e) {}
        try { this.commentClickHandler = null; } catch (Exception e) {}
        try { this.localScalePageHeightListener = null; } catch (Exception e) {}
        try { this.staticHelpButtonHandler = null; } catch (Exception e) {}
        try { this.exerciseFocusListener = null; } catch (Exception e) {}

        try {this.pointsEarnedFocusListener = null; } catch (Exception e) {}
        try { this.pointsEarnedClickHandler = null; } catch (Exception e) {}
    }



    public Button getStaticHelpButton() { return staticHelpButton; }
    public Stage getStaticHelpStage() { return staticHelpStage; }

    // We can expose the setter for handler so the FFCheck or Exercise can bind to it
    public void setStaticHelpButtonHandler(EventHandler<ActionEvent> handler) {
        this.staticHelpButtonHandler = handler;
        if (staticHelpButton != null) staticHelpButton.setOnAction(handler);
    }

    List<ViewElement> getViewElements() { return viewElements; }
    Button getIndentButton() { return indentButton; }
    Button getOutdentButton() { return outdentButton; }
    Button getRemoveButton() { return removeButton; }
    Button getRestoreButton() { return restoreButton; }
    Button getMoveUpButton() { return moveUpButton; }
    Button getMoveDownButton() { return moveDownButton; }
    Button getAddEditButton() { return addEditButton; }
    Button getAddHTreeButton() { return addHTreeButton; }
    Button getAddTTableButton() { return addTTableButton; }
    Button getAddVTreeBaseItalButton() { return addVTreeBaseItalButton; }
    Button getAddVTreeItalSansButton() { return addVTreeItalSansButton; }
    Button getAddVTreeScriptItalButton() { return addVTreeScriptItalButton; }
    Button getAddNDrvtnItalSansButton() { return addNDrvtnItalSansButton; }
    Button getAddNDrvtnScriptItalButton() { return addNDrvtnScriptItalButton; }
    Button getAddNDrvtnScriptSansButton() { return addNDrvtnScriptSansButton; }
    Button getAddNDrvtnItalBBButton() { return addNDrvtnItalBBButton; }
    Button getAddADrvtnItalSansButton() { return addADrvtnItalSansButton; }
    Button getAddADrvtnScriptItalButton() { return addADrvtnScriptItalButton; }
    Button getAddADrvtnScriptSansButton() { return addADrvtnScriptSansButton; }
    Button getAddADrvtnItalBBButton() { return addADrvtnItalBBButton; }

    double getCommentPrefHeight() {
        if (commentDRTA == null || commentDRTA.getEditor() == null) return 0;
        return commentDRTA.getEditor().getPrefHeight();
    }
    void setCommentPrefHeight(double commentPrefHeight) { this.commentPrefHeight = commentPrefHeight; }
    void setExerciseControlNode(Node exerciseControlNode) { this.exerciseControlNode = exerciseControlNode; }

    @Override
    public DecoratedRTA getExerciseComment() { return commentDRTA; }
    @Override
    public void setExerciseComment(DecoratedRTA exerciseComment) { commentDRTA = exerciseComment; }
    @Override
    public DecoratedRTA getExerciseStatement() { return statementDRTA; }
    @Override
    public void setExerciseStatement(DecoratedRTA exerciseStatement) { statementDRTA = exerciseStatement; }
    @Override
    public Node getExerciseStatementNode() {
        if (statementDRTA == null) return new AnchorPane();
        return statementDRTA.getEditor();
    }

    @Override
    public void setStatementPrefHeight(double height) {
        if (statementDRTA == null || statementDRTA.getEditor() == null) return;
        statementPrefHeight = height;
        statementDRTA.getEditor().setPrefHeight(height);
    }

    @Override
    public Node getExerciseContentNode() { return contentBox; }
    @Override
    public Node getExerciseControl() { return exerciseControlNode; }
    @Override
    public Node getRightControl() { return freeFormControlNode; }
    @Override
    public Node getPointsNode() {
        if (pointsPossible > 0) {
            if (mainView != null && !mainView.isInstructorFunctions() && pointsEarnedTextField != null) pointsEarnedTextField.setDisable(true);
            return pointsEarnedTextField;
        }
        return null;
    }
    public void setPointsPossible(int pointsPossible) { this.pointsPossible = pointsPossible; }
    @Override
    public int getPointsPossible() { return pointsPossible; }
    public TextField getPointsEarnedTextField() { return pointsEarnedTextField; }
    public VBox getContentBox() { return contentBox; }
    public DoubleProperty localScalePageHeightProperty() { return localScalePageHeight; }
    public DoubleProperty localScalePageWidthProperty() { return localScalePageWidth; }

}
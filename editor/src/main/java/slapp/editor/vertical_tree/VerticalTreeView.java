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

package slapp.editor.vertical_tree;

import com.gluonhq.richtextarea.RichTextArea;
import com.gluonhq.richtextarea.RichTextAreaSkin;
import com.gluonhq.richtextarea.model.Document;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import slapp.editor.EditorMain;
import slapp.editor.decorated_rta.DecoratedRTA;
import slapp.editor.main_window.ExerciseView;
import slapp.editor.main_window.MainWindowView;
import slapp.editor.vertical_tree.drag_drop.RootLayout;

import java.util.function.UnaryOperator;
import static slapp.editor.EditorMain.RTA_SCREEN_BMP;
import static slapp.editor.EditorMain.RTA_WIDTH_BMP;


import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.event.EventHandler;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import java.util.ArrayList;
import java.util.List;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
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
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.text.TextFlow;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;
import java.util.function.UnaryOperator;


public class VerticalTreeView implements ExerciseView<DecoratedRTA> {

    private MainWindowView mainView;
    private RichTextAreaSkin.KeyMapValue defaultKeyboard;
    private RichTextAreaSkin.KeyMapValue defaultMapKeyboard;
    private BorderPane root;
    private RootLayout rootLayout;
    private DecoratedRTA exerciseComment = new DecoratedRTA();
    private DecoratedRTA exerciseStatement = new DecoratedRTA();
    private double statementPrefHeight = 0;
    private double commentPrefHeight = 0;
    private double mainPanePrefHeight = 0;
    private double mainPanePrefWidth = 0;
    private Spinner<Double> statementHeightSpinner;
    private Spinner<Double> statementWidthSpinner;
    private Spinner<Double> commentHeightSpinner;
    private Spinner<Double> commentWidthSpinner;
    private Spinner<Double> mainPaneHeightSpinner;
    private Spinner<Double> mainPaneWidthSpinner;
    private Node currentSpinnerNode;

    private VBox controlBox = new VBox(25);
    private Button undoButton;
    private Button redoButton;
    public BooleanProperty undoRedoFlag = new SimpleBooleanProperty();
    Node exerciseControlNode;
    SplitPane mainPane;

    private int pointsPossible;
    private TextField pointsEarnedTextField;

    //--------------------
    private Node rightControlNode;
    private Button checkButton;
    private Button checkProgButton;
    private Text bigCheck;
    private String checkMessage;
    private Text checkedElements;
    private Label checkTriesLabel;
    private Color checkColor;
    private Color checkElementsColor;
    private boolean checkShowing = false;
    private Button staticHelpButton;
    private Stage staticHelpStage;

    private VerticalTreeExercise vtExercise;

    private final DoubleProperty localScalePageHeight = new SimpleDoubleProperty(1.0);
    private final DoubleProperty localScalePageWidth = new SimpleDoubleProperty(1.0);

    // SCORCHED EARTH RULE 1 & 2: The Asynchronous Ghost "Kill Switch" & Named Listeners
    private boolean isTornDown = false;
    private ChangeListener<Node> exerciseFocusListener;
    private ChangeListener<Number> mainPaneHeightListener;
    private ChangeListener<Number> mainPaneWidthListener;
    private ChangeListener<Number> localScalePageHeightListener;
    private ChangeListener<Number> localScalePageWidthListener;
    private EventHandler<MouseEvent> mainPaneClickHandler;
    private UnaryOperator<TextFormatter.Change> pointsEarnedFilter;

    private ChangeListener<Boolean> pointsEarnedFocusListener;
    private EventHandler<MouseEvent> pointsEarnedClickHandler;

    VerticalTreeView(MainWindowView mainView, VerticalTreeExercise vtExercise) {
        this.mainView = mainView;

        localScalePageHeight.bind(mainView.scalePageHeightProperty());
        localScalePageWidth.bind(mainView.scalePageWidthProperty());

        exerciseFocusListener = (ob, ov, nv) -> {
            if (isTornDown || this.mainView == null) return;

            try {
                RichTextArea statementRTA = exerciseStatement != null ? exerciseStatement.getEditor() : null;
                RichTextArea commentRTA = exerciseComment != null ? exerciseComment.getEditor() : null;

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
                } else if (mainPane != null && inHierarchy(nv, mainPane)) {
                    if (currentSpinnerNode != mainPane) {
                        currentSpinnerNode = mainPane;
                        if (mainPaneHeightSpinner != null && mainPaneWidthSpinner != null) {
                            this.mainView.updateSizeSpinners(mainPaneHeightSpinner, mainPaneWidthSpinner);
                        }
                    }
                } else {
                    currentSpinnerNode = null;
                }
            } catch (Exception e) {}
        };

        this.vtExercise = vtExercise;

        root = new BorderPane();
        rootLayout = new RootLayout(this);
        root.setCenter(rootLayout);

        undoButton = new Button("Undo");
        redoButton = new Button("Redo");
        undoButton.setPrefWidth(64);
        redoButton.setPrefWidth(64);
        undoButton.setPrefHeight(28);
        redoButton.setPrefHeight(28);

        controlBox.getChildren().addAll(undoButton, redoButton);
        controlBox.setAlignment(Pos.TOP_CENTER);
        controlBox.setPadding(new Insets(40,20,0,20));  //60
        controlBox.setMinWidth(150); controlBox.setMaxWidth(150);
        exerciseControlNode = controlBox;

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

    void initializeViewDetails() {
        double spinnerMin = (double) ((int) (100 * 45.0 / localScalePageHeight.get()));

        //statement
        RichTextArea statementRTA = exerciseStatement.getEditor();
        statementRTA.getStylesheets().add("slappTextArea.css");
        statementRTA.setEditable(false);

        double statementInitialHeight = Math.ceil((statementPrefHeight - RTA_SCREEN_BMP) / localScalePageHeight.get() * 100.0);
        statementHeightSpinner = new Spinner<>(spinnerMin, 999.0, statementInitialHeight, 1.0);
        statementHeightSpinner.setPrefWidth(65);
        statementHeightSpinner.setDisable(false);
        statementHeightSpinner.setTooltip(new Tooltip("Window height as % of selected paper"));
        statementRTA.prefHeightProperty().bind(Bindings.add(Bindings.max(45.0, Bindings.multiply(localScalePageHeight, DoubleProperty.doubleProperty(statementHeightSpinner.getValueFactory().valueProperty()).divide(100.0))), RTA_SCREEN_BMP));

        statementRTA.maxWidthProperty().bind(Bindings.add(localScalePageWidth, RTA_WIDTH_BMP));
        statementRTA.contentAreaWidthProperty().bind(localScalePageWidth);

        statementWidthSpinner = new Spinner<>(0.0, 999.0, 100, 1.0);
        statementWidthSpinner.setPrefWidth(65);
        statementWidthSpinner.setDisable(true);
        statementWidthSpinner.setTooltip(new Tooltip("Window width as % of selected paper"));

        //comment
        RichTextArea commentRTA = exerciseComment.getEditor();
        commentRTA.getStylesheets().add("slappTextArea.css");
        commentRTA.setPromptText("Comment:");

        double commentInitialHeight = Math.round((commentPrefHeight - RTA_SCREEN_BMP) / localScalePageHeight.get() * 100.0);
        commentHeightSpinner = new Spinner<>(spinnerMin, 999.0, commentInitialHeight, 1.0);
        commentHeightSpinner.setPrefWidth(65);
        commentHeightSpinner.setDisable(false);
        commentHeightSpinner.setTooltip(new Tooltip("Window height as % of selected paper"));
        commentRTA.prefHeightProperty().bind(Bindings.add(Bindings.max(45.0, Bindings.multiply(localScalePageHeight, DoubleProperty.doubleProperty(commentHeightSpinner.getValueFactory().valueProperty()).divide(100.0))), RTA_SCREEN_BMP));

        commentRTA.maxWidthProperty().bind(Bindings.add(localScalePageWidth, RTA_WIDTH_BMP));
        commentRTA.minWidthProperty().bind(Bindings.add(localScalePageWidth, RTA_WIDTH_BMP));
        commentRTA.contentAreaWidthProperty().bind(localScalePageWidth);

        commentWidthSpinner = new Spinner<>(0.0, 999.0, 100, 1.0);
        commentWidthSpinner.setPrefWidth(65);
        commentWidthSpinner.setDisable(true);
        commentWidthSpinner.setTooltip(new Tooltip("Window width as % of selected paper"));

        //main pane
        AnchorPane mainPane1 = rootLayout.getMainPane();
        mainPane = rootLayout.getBase_pane();
        mainPane.setStyle("-fx-focus-color: gainsboro;");
        mainPane1.setMinHeight(250.0);   //150
        mainPane1.setMinWidth(localScalePageWidth.get() + RTA_WIDTH_BMP);

        mainPaneHeightSpinner = new Spinner<>(5, 999.0, 0, 1.0);
        mainPaneHeightSpinner.setPrefWidth(65);
        mainPaneHeightSpinner.setDisable(true);
        mainPaneHeightSpinner.setTooltip(new Tooltip("Window height as % of selected paper"));

        mainPaneHeightListener = (ob, ov, nv) -> {
            if (isTornDown || this.mainView == null || mainPaneHeightSpinner == null) return;
            mainPaneHeightSpinner.getValueFactory().setValue((double) Math.round(mainPane1.getHeight() / localScalePageHeight.get() * 100));
        };
        mainPane1.heightProperty().addListener(mainPaneHeightListener);


        mainPaneWidthSpinner = new Spinner<>(70.0, 999.0, 0, 1.0);
        mainPaneWidthSpinner.setPrefWidth(65);
        mainPaneWidthSpinner.setDisable(true);
        mainPaneWidthSpinner.setTooltip(new Tooltip("Window width as % of selected paper"));

        mainPaneWidthListener = (ob, ov, nv) -> {
            if (isTornDown || this.mainView == null || mainPaneWidthSpinner == null) return;
            mainPaneWidthSpinner.getValueFactory().setValue((double) Math.round((mainPane1.getWidth() - RTA_WIDTH_BMP) / localScalePageWidth.get() * 100));
        };
        mainPane1.widthProperty().addListener(mainPaneWidthListener);

        //page size listeners
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

            if (mainPaneHeightSpinner != null && mainPaneHeightSpinner.getValueFactory() != null) {
                mainPaneHeightSpinner.getValueFactory().setValue((double) Math.round(mainPane1.getHeight() / localScalePageHeight.get() * 100.0));
            }
        };
        localScalePageHeight.addListener(localScalePageHeightListener);

        localScalePageWidthListener = (ob, ov, nv) -> {
            if (isTornDown || this.mainView == null || mainPane == null) return;
            mainPane.prefWidthProperty().unbind();
            if (mainPaneWidthSpinner != null && mainPaneWidthSpinner.getValueFactory() != null) {
                mainPaneWidthSpinner.getValueFactory().setValue((double) Math.round((mainPane1.getWidth() - RTA_WIDTH_BMP) / localScalePageWidth.get() * 100));
            }
        };
        localScalePageWidth.addListener(localScalePageWidthListener);

        setSizeSpinners();
    }

    public void setRightControlBox() {
        bigCheck = new Text("\ue89a");
        bigCheck.setFont(Font.font("Noto Serif Combo", 72));

        checkedElements = new Text(checkMessage);
        checkedElements.setFont(Font.font("Noto Serif Combo", 11));
        TextFlow checkedElementsFlow = new TextFlow(checkedElements);
        checkedElementsFlow.setMaxWidth(150);

        VBox bigCheckBox = new VBox(0, bigCheck, checkedElementsFlow);
        bigCheckBox.setAlignment(Pos.CENTER);
        checkedElementsFlow.setTextAlignment(TextAlignment.CENTER);

        checkButton = new Button("Check Tree");
        checkButton.setPrefWidth(105);
        checkButton.setTooltip(new Tooltip("Check tree for correctness."));
        checkTriesLabel = new Label();

        checkProgButton = new Button();

        VBox checksBox = new VBox(10, checkButton, checkTriesLabel);
        checksBox.setAlignment(Pos.CENTER);
        checkTriesLabel.setAlignment(Pos.CENTER);

        staticHelpButton = new Button("Static Help");
        staticHelpButton.setPrefWidth(105);
        staticHelpButton.setTooltip(new Tooltip("Get static help text"));


        VBox rightControlBox = new VBox(40, bigCheckBox, checksBox, staticHelpButton );
        rightControlBox.setAlignment(Pos.TOP_CENTER);
        rightControlBox.setPadding(new Insets(60,20,0,20));
        rightControlNode = rightControlBox;

        deactivateBigCheck();
    }

    public void activateBigCheck() {
        if (isTornDown || bigCheck == null || checkedElements == null) return;
        bigCheck.setFill(checkColor);
        checkedElements.setText(checkMessage);
        checkedElements.setFill(checkElementsColor);

        if (checkShowing) {
            FadeTransition t1 = new FadeTransition(new Duration(250), checkedElements);
            t1.setToValue(1.0);
            t1.setInterpolator(Interpolator.DISCRETE);

            FadeTransition t2 = new FadeTransition(new Duration(250), bigCheck);
            t2.setToValue(1.0);
            t2.setInterpolator(Interpolator.DISCRETE);

            ParallelTransition pt = new ParallelTransition(t1, t2);
            pt.play();
        }
        else {
            bigCheck.setOpacity(1.0);
            checkedElements.setOpacity(1.0);
        }
    }

    //record status, and deactivate
    public void deactivateBigCheck() {
        if (isTornDown || bigCheck == null || checkedElements == null) return;
        if (bigCheck.getOpacity() > .5) checkShowing = true;
        else checkShowing = false;
        bigCheck.setOpacity(0.0);
        checkedElements.setOpacity(0.0);
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

    public Stage getStaticHelpStage() { return staticHelpStage; }

    /*
    private void setSizeSpinners() {
        mainPaneClickHandler = e -> {
            if (isTornDown || this.mainView == null || currentSpinnerNode == null) return;
            if (currentSpinnerNode != mainPane) {
                mainPane.requestFocus();
            }
        };
        mainPane.addEventFilter(MouseEvent.MOUSE_CLICKED, mainPaneClickHandler);

        if (mainView != null && mainView.getMainScene() != null) {
            mainView.getMainScene().focusOwnerProperty().addListener(exerciseFocusListener);
        }
    }

     */

    private void setSizeSpinners() {
        if (mainPane != null) {
            mainPaneClickHandler = e -> {
                // THE FIX: Notice there is NO "currentSpinnerNode == null" abort check here!
                if (isTornDown || this.mainView == null) return;

                if (currentSpinnerNode != mainPane) {
                    currentSpinnerNode = mainPane;

                    // Force the toolbars to appear immediately, bypassing the focus listener entirely!
                    if (mainPaneHeightSpinner != null && mainPaneWidthSpinner != null) {
                        mainView.updateSizeSpinners(mainPaneHeightSpinner, mainPaneWidthSpinner);
                    }

                    // Still request focus so the global Scene knows where we are
                    mainPane.requestFocus();
                }
            };
            mainPane.addEventFilter(MouseEvent.MOUSE_CLICKED, mainPaneClickHandler);
        }

        if (mainView != null && mainView.getMainScene() != null && exerciseFocusListener != null) {
            mainView.getMainScene().focusOwnerProperty().addListener(exerciseFocusListener);
        }
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

        // SCORCHED EARTH RULE 5: Defensive unbinding and detachment via isolated try-catch blocks
        try { if (mainPane != null && mainPaneClickHandler != null) mainPane.removeEventFilter(MouseEvent.MOUSE_CLICKED, mainPaneClickHandler); } catch (Exception e) {}
        try { if (rootLayout != null && rootLayout.getMainPane() != null && mainPaneHeightListener != null) rootLayout.getMainPane().heightProperty().removeListener(mainPaneHeightListener); } catch (Exception e) {}
        try { if (rootLayout != null && rootLayout.getMainPane() != null && mainPaneWidthListener != null) rootLayout.getMainPane().widthProperty().removeListener(mainPaneWidthListener); } catch (Exception e) {}

        try { if (localScalePageHeight != null && localScalePageHeightListener != null) localScalePageHeight.removeListener(localScalePageHeightListener); } catch (Exception e) {}
        try { if (localScalePageHeight != null) localScalePageHeight.unbind(); } catch (Exception e) {}

        try { if (localScalePageWidth != null && localScalePageWidthListener != null) localScalePageWidth.removeListener(localScalePageWidthListener); } catch (Exception e) {}
        try { if (localScalePageWidth != null) localScalePageWidth.unbind(); } catch (Exception e) {}

        try { if (undoRedoFlag != null) undoRedoFlag.unbind(); } catch (Exception e) {}

        // Isolate Exercise Statement unbindings
        try { if (exerciseStatement != null && exerciseStatement.getEditor() != null) exerciseStatement.getEditor().prefHeightProperty().unbind(); } catch (Exception e) {}
        try { if (exerciseStatement != null && exerciseStatement.getEditor() != null) exerciseStatement.getEditor().maxWidthProperty().unbind(); } catch (Exception e) {}
        try { if (exerciseStatement != null && exerciseStatement.getEditor() != null) exerciseStatement.getEditor().contentAreaWidthProperty().unbind(); } catch (Exception e) {}

        // Isolate Exercise Comment unbindings
        try { if (exerciseComment != null && exerciseComment.getEditor() != null) exerciseComment.getEditor().prefHeightProperty().unbind(); } catch (Exception e) {}
        try { if (exerciseComment != null && exerciseComment.getEditor() != null) exerciseComment.getEditor().prefWidthProperty().unbind(); } catch (Exception e) {}
        try { if (exerciseComment != null && exerciseComment.getEditor() != null) exerciseComment.getEditor().maxWidthProperty().unbind(); } catch (Exception e) {}
        try { if (exerciseComment != null && exerciseComment.getEditor() != null) exerciseComment.getEditor().minWidthProperty().unbind(); } catch (Exception e) {}
        try { if (exerciseComment != null && exerciseComment.getEditor() != null) exerciseComment.getEditor().contentAreaWidthProperty().unbind(); } catch (Exception e) {}

        // ---> SCORCHED EARTH RULE 4: THE JAVAFX SKIN HAMMER (DECOUPLED TIMER) <---
        try {
            final RichTextArea stEditor = (exerciseStatement != null) ? exerciseStatement.getEditor() : null;
            final RichTextArea commEditor = (exerciseComment != null) ? exerciseComment.getEditor() : null;

            // STAGE 1 (Immediate)
            if (stEditor != null) {
                try { stEditor.setPromptText(""); } catch (Exception e) {}
                try { stEditor.setFocusTraversable(false); } catch (Exception e) {}
                try { if (stEditor.getDocument() == null || stEditor.getDocument().getText().isEmpty()) stEditor.getActionFactory().open(new Document(" ")).execute(new ActionEvent()); } catch (Exception e) {}
            }

            if (commEditor != null) {
                try { commEditor.setPromptText(""); } catch (Exception e) {}
                try { commEditor.setFocusTraversable(false); } catch (Exception e) {}
                try { if (commEditor.getDocument() == null || commEditor.getDocument().getText().isEmpty()) commEditor.getActionFactory().open(new Document(" ")).execute(new ActionEvent()); } catch (Exception e) {}
            }

            // STAGE 2 (Delayed Skin Removal)
            javafx.animation.PauseTransition pt = new javafx.animation.PauseTransition(javafx.util.Duration.millis(1500));
            pt.setOnFinished(ev -> {
                try { if (stEditor != null) { stEditor.setSkin(null); } } catch (Exception ex) {}
                try { if (commEditor != null) { commEditor.setSkin(null); } } catch (Exception ex) {}
            });
            pt.play();
        } catch (Exception e) {}

        // SCORCHED EARTH RULE 8: The Undo/Redo Rebuild Fix (Prevent CME)
        try {
            if (rootLayout != null) {
                if (rootLayout.getMainPane() != null) {
                    if (rootLayout.getMainPane().getChildren() != null) {
                        List<Node> childrenCopy = new ArrayList<>(rootLayout.getMainPane().getChildren());
                        for (Node node : childrenCopy) {
                            try { node.getClass().getMethod("cleanup").invoke(node); } catch (Exception e) {}
                            severFromParent(node);
                        }
                    }
                    rootLayout.getMainPane().getChildren().clear();
                }
                if (rootLayout.getBase_pane() != null) {
                    rootLayout.getBase_pane().prefWidthProperty().unbind();
                }
            }
        } catch (Exception e) {}

        try { if (mainView != null && mainView.getMainScene() != null && exerciseFocusListener != null) mainView.getMainScene().focusOwnerProperty().removeListener(exerciseFocusListener); } catch (Exception e) {}
        try { if (pointsEarnedTextField != null) pointsEarnedTextField.setTextFormatter(null); } catch (Exception e) {}

        try { if (exerciseControlNode != null) severFromParent(exerciseControlNode); } catch (Exception e) {}
        try { if (controlBox != null) { severFromParent(controlBox); controlBox.getChildren().clear(); } } catch (Exception e) {}
        try { if (rightControlNode != null) severFromParent(rightControlNode); } catch (Exception e) {}
        try { if (root != null) severFromParent(root); } catch (Exception e) {}
        try { if (staticHelpStage != null) staticHelpStage.close(); } catch (Exception e) {}

        try { if (undoButton != null) undoButton.setOnAction(null); } catch (Exception e) {}
        try { if (redoButton != null) redoButton.setOnAction(null); } catch (Exception e) {}
        try { if (checkButton != null) checkButton.setOnAction(null); } catch (Exception e) {}
        try { if (checkProgButton != null) checkProgButton.setOnAction(null); } catch (Exception e) {}
        try { if (staticHelpButton != null) staticHelpButton.setOnAction(null); } catch (Exception e) {}

        // SCORCHED EARTH RULE 1: Final Nullification
        this.mainView = null;
        this.vtExercise = null;
        this.defaultKeyboard = null;
        this.defaultMapKeyboard = null;
        this.exerciseStatement = null;
        this.exerciseComment = null;
        this.root = null;
        this.rootLayout = null;
        this.controlBox = null;
        this.undoButton = null;
        this.redoButton = null;
        this.undoRedoFlag = null;
        this.exerciseControlNode = null;
        this.mainPane = null;
        this.pointsEarnedTextField = null;
        this.rightControlNode = null;
        this.checkButton = null;
        this.checkProgButton = null;
        this.bigCheck = null;
        this.checkMessage = null;
        this.checkedElements = null;
        this.checkTriesLabel = null;
        this.checkColor = null;
        this.checkElementsColor = null;
        this.staticHelpButton = null;
        this.staticHelpStage = null;
        this.exerciseFocusListener = null;
        this.statementHeightSpinner = null;
        this.statementWidthSpinner = null;
        this.commentHeightSpinner = null;
        this.commentWidthSpinner = null;
        this.mainPaneHeightSpinner = null;
        this.mainPaneWidthSpinner = null;
        this.currentSpinnerNode = null;
        this.pointsEarnedFilter = null;
        this.mainPaneHeightListener = null;
        this.mainPaneWidthListener = null;
        this.localScalePageHeightListener = null;
        this.localScalePageWidthListener = null;
        this.mainPaneClickHandler = null;

        this.pointsEarnedFocusListener = null;
        this.pointsEarnedClickHandler = null;
    }


    public MainWindowView getMainView() {
        return mainView;
    }

    public RootLayout getRootLayout() {return rootLayout;}

    public VBox getControlBox() {  return controlBox;  }

    public Button getUndoButton() { return undoButton;   }

    public Button getRedoButton() { return redoButton;   }

    public boolean isUndoRedoFlag() {    return undoRedoFlag.get();    }

    public BooleanProperty undoRedoFlagProperty() {    return undoRedoFlag;    }

    public void setUndoRedoFlag(boolean undoRedoFlag) {    this.undoRedoFlag.set(undoRedoFlag);    }

    public void setDefaultKeyboard(RichTextAreaSkin.KeyMapValue defaultKeyboard) {    this.defaultKeyboard = defaultKeyboard;  }

    public RichTextAreaSkin.KeyMapValue getDefaultKeyboard() {     return defaultKeyboard; }

    public RichTextAreaSkin.KeyMapValue getDefaultMapKeyboard() {
        return defaultMapKeyboard;
    }

    public void setDefaultMapKeyboard(RichTextAreaSkin.KeyMapValue defaultMapKeyboard) {
        this.defaultMapKeyboard = defaultMapKeyboard;
    }

    public double getCommentPrefHeight() {   return exerciseComment.getEditor().getPrefHeight();   }

    public void setCommentPrefHeight(double commentPrefHeight) {     this.commentPrefHeight = commentPrefHeight;  }


    public double getMainPanePrefHeight() { return rootLayout.getMainPane().getPrefHeight(); }
    public void setMainPanePrefHeight(double mainPanePrefHeight) { this.mainPanePrefHeight = mainPanePrefHeight; }



    public double getMainPanePrefWidth() { return rootLayout.getMainPane().getPrefWidth(); }
    public void setMainPanePrefWidth(double mainPanePrefWidth) { this.mainPanePrefWidth = mainPanePrefWidth; }

    Spinner getMainPaneHeightSpinner() { return mainPaneHeightSpinner; }
    Spinner getMainPaneWidthSpinner() { return mainPaneWidthSpinner; }

    public Button getCheckButton() {
        return checkButton;
    }

    public Button getCheckProgButton() {
        return checkProgButton;
    }

    public Button getstaticHelpButton() {
        return staticHelpButton;
    }

    public Label getCheckTriesLabel() {
        return checkTriesLabel;
    }

    public void setCheckMessage(String checkMessage) {
        this.checkMessage = checkMessage;
    }

    public void setCheckColor(Color checkColor) {
        this.checkColor = checkColor;
    }

    public void setCheckElementsColor(Color checkElementsColor) {
        this.checkElementsColor = checkElementsColor;
    }

    public String getCheckMessage() {
        return checkMessage;
    }


    @Override
    public DecoratedRTA getExerciseComment() {
        return exerciseComment;
    }

    @Override
    public void setExerciseComment(DecoratedRTA exerciseComment) { this.exerciseComment = exerciseComment;  }

    @Override
    public DecoratedRTA getExerciseStatement() {
        return exerciseStatement;
    }

    @Override
    public void setExerciseStatement(DecoratedRTA exerciseStatement) { this.exerciseStatement = exerciseStatement; }

    @Override
    public Node getExerciseStatementNode() { return exerciseStatement.getEditor();   }

    @Override
    public void setStatementPrefHeight(double height) {
        statementPrefHeight = height;
        exerciseStatement.getEditor().setPrefHeight(height);
    }

    @Override
    public Node getExerciseContentNode() {
        return new VBox(root);
    }
    @Override
    public Node getExerciseControl() {
        return exerciseControlNode;
    }
    @Override
    public Node getRightControl() { return rightControlNode; }

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

    public VerticalTreeExercise getVTExercise() {
        return vtExercise;
    }

    public DoubleProperty localScalePageHeightProperty() { return localScalePageHeight; }

    public DoubleProperty localScalePageWidthProperty() {
        return localScalePageWidth;
    }

    public ChangeListener<Node> getExerciseFocusListener() { return exerciseFocusListener; }

}
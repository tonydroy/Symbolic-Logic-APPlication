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

package slapp.editor.trans_uline;

import com.gluonhq.richtextarea.RichTextArea;
import com.gluonhq.richtextarea.RichTextAreaSkin;
import com.gluonhq.richtextarea.model.Document;
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
import slapp.editor.EditorMain;
import slapp.editor.decorated_rta.BoxedDRTA;
import slapp.editor.decorated_rta.DecoratedRTA;
import slapp.editor.main_window.*;

import java.util.ArrayList;
import java.util.List;
import java.util.function.UnaryOperator;
import static slapp.editor.EditorMain.*;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.control.*;
import javafx.scene.layout.*;


public class TransULView implements ExerciseView<DecoratedRTA> {

    private MainWindowView mainView;
    private TransULExercise transULExercise;
    private CheckBox aCheckBox = new CheckBox("simple");
    private CheckBox bCheckBox = new CheckBox("compound");
    private HBox choiceBox;
    private DecoratedRTA exerciseComment = new DecoratedRTA();
    private DecoratedRTA exerciseStatement = new DecoratedRTA();
    private SimpleULineBox ulineBox;
    private VBox ulineVBox;
    private BoxedDRTA mainOperatorBDRTA;
    private VBox contentBox;

    private double statementPrefHeight = 0;
    private double commentPrefHeight = 0;
    private double explainPrefHeight = 0;
    private Spinner<Double> statementHeightSpinner;
    private Spinner<Double> statementWidthSpinner;
    private Spinner<Double> commentHeightSpinner;
    private Spinner<Double> commentWidthSpinner;
    private Spinner<Double> choicesHeightSpinner;
    private Spinner<Double> choicesWidthSpinner;
    private Spinner<Double> ulineVBoxHeightSpinner;
    private Spinner<Double> ulineVBoxWidthSpinner;
    private Spinner<Double> mainOperatorHeightSpinner;
    private Spinner<Double> mainOperatorWidthSpinner;
    private Node currentSpinnerNode;
    private VBox controlBox = new VBox(25);
    private Button undoButton;
    private Button redoButton;
    public BooleanProperty undoRedoFlag = new SimpleBooleanProperty();
    private int pointsPossible;
    private TextField pointsEarnedTextField;
    private Node rightControlNode;
    private Button checkButton;
    private Text bigCheck;
    private Text checkedElements;
    private String checkMessage;
    private Label checkTriesLabel;
    private Color checkColor;
    private boolean checkShowing = false;
    private Button staticHelpButton;
    private Stage staticHelpStage;

    private final DoubleProperty localScalePageHeight = new SimpleDoubleProperty(1.0);
    private final DoubleProperty localScalePageWidth = new SimpleDoubleProperty(1.0);

    // SCORCHED EARTH RULE 2: Kill Switch for asynchronous event interception during teardown
    private boolean isTornDown = false;

    // SCORCHED EARTH RULE 3: Eradicate Anonymous Lambdas
    private ChangeListener<Node> exerciseFocusListener;
    private UnaryOperator<TextFormatter.Change> pointsEarnedFilter;
    private ChangeListener<Number> localScalePageHeightListener;
    private ChangeListener<Number> localScalePageWidthListener;

    private EventHandler<MouseEvent> choiceBoxClickHandler;
    private EventHandler<MouseEvent> ulineVBoxClickHandler;

    private ChangeListener<Boolean> pointsEarnedFocusListener;
    private EventHandler<MouseEvent> pointsEarnedClickHandler;



    // Track transitions for safe teardown
    private ParallelTransition checkAnimation;

    TransULView(MainWindowView mainView, TransULExercise transULExercise) {
        this.mainView = mainView;
        this.transULExercise = transULExercise;

        localScalePageHeight.bind(mainView.scalePageHeightProperty());
        localScalePageWidth.bind(mainView.scalePageWidthProperty());

        exerciseFocusListener = (ob, ov, nv) -> {
            if (isTornDown || this.mainView == null) return;

            try {
                RichTextArea statementRTA = exerciseStatement != null ? exerciseStatement.getEditor() : null;
                RichTextArea commentRTA = exerciseComment != null ? exerciseComment.getEditor() : null;
                HBox mainOpBox = mainOperatorBDRTA != null? mainOperatorBDRTA.getBoxedRTA() : null;

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
                } else if (choiceBox != null && inHierarchy(nv, choiceBox)) {
                    if (currentSpinnerNode != choiceBox) {
                        currentSpinnerNode = choiceBox;
                        if (choicesHeightSpinner != null && choicesHeightSpinner.getValueFactory() != null && choicesWidthSpinner != null) {
                            double choicesHeightValue = Math.round(choiceBox.getHeight() / localScalePageHeight.get() * 100);
                            choicesHeightSpinner.getValueFactory().setValue(choicesHeightValue);
                            this.mainView.updateSizeSpinners(choicesHeightSpinner, choicesWidthSpinner);
                        }
                    }
                } else if (ulineVBox != null && inHierarchy(nv, ulineVBox)) {
                    if (currentSpinnerNode != ulineVBox) {
                        currentSpinnerNode = ulineVBox;
                        if (ulineVBoxHeightSpinner != null && ulineVBoxHeightSpinner.getValueFactory() != null && ulineVBoxWidthSpinner != null) {
                            double ulineVBoxHeightValue = Math.round(ulineVBox.getHeight() / localScalePageHeight.get() * 100);
                            ulineVBoxHeightSpinner.getValueFactory().setValue(ulineVBoxHeightValue);
                            this.mainView.updateSizeSpinners(ulineVBoxHeightSpinner, ulineVBoxWidthSpinner);
                        }
                    }
                } else if (mainOpBox != null && inHierarchy(nv, mainOpBox)) {
                    if (currentSpinnerNode != mainOpBox) {
                        currentSpinnerNode = mainOpBox;
                        if (mainOperatorHeightSpinner != null && mainOperatorHeightSpinner.getValueFactory() != null && mainOperatorWidthSpinner != null) {
                            double mainOperatorBoxHeightValue = Math.round(mainOpBox.getHeight() / localScalePageHeight.get() * 100);
                            mainOperatorHeightSpinner.getValueFactory().setValue(mainOperatorBoxHeightValue);
                            this.mainView.updateSizeSpinners(mainOperatorHeightSpinner, mainOperatorWidthSpinner);
                        }
                    }
                } else {
                    currentSpinnerNode = null;
                }
            } catch (Exception e) {}
        };

        Font labelFont = new Font("Noto Serif Combo", 11);
        Label choiceLeadLabel = new Label("The following sentence is ");
        choiceLeadLabel.setFont(labelFont); aCheckBox.setFont(labelFont); bCheckBox.setFont(labelFont);
        choiceBox = new HBox(20, choiceLeadLabel, aCheckBox, bCheckBox);
        choiceBox.maxWidthProperty().bind(localScalePageWidth);
        choiceBox.minWidthProperty().bind(choiceBox.maxWidthProperty());
        choiceBox.setPadding(new Insets(7,0,7,RTA_WIDTH_BMP/2));
        choiceBox.setStyle("-fx-border-color: transparent; -fx-border-width: 0; -fx-background-color: white");


        ulineBox = new SimpleULineBox(transULExercise);
        ulineBox.getFormulaBox().getRTA().setStyle("-fx-border-width: 0; -fx-border-color: transparent");
        ulineVBox = new VBox(ulineBox);
        ulineVBox.setMinHeight(70);

   //     ulineVBox.setAlignment(Pos.CENTER_LEFT);
   //     ulineVBox.setPadding(new Insets(0,0,0, RTA_WIDTH_BMP/2));

        ulineVBox.setAlignment(Pos.TOP_LEFT);
        ulineVBox.setPadding(new Insets(20,0,0,RTA_WIDTH_BMP/2));

        ulineVBox.setStyle("-fx-background-color: rgb(245,245,245); -fx-border-width: 0; -fx-border-color: transparent");

        mainOperatorBDRTA = new BoxedDRTA();
        RichTextArea mainOperatorRTA = mainOperatorBDRTA.getRTA();
        mainOperatorBDRTA.getDRTA().getKeyboardSelector().valueProperty().setValue(RichTextAreaSkin.KeyMapValue.BASE);
        mainOperatorRTA.setMaxHeight(27);
        mainOperatorRTA.setMinHeight(27);
        mainOperatorRTA.getStylesheets().add("blueFormulaBox.css");
        mainOperatorRTA.setPromptText("Main Operator (use underscores '__' for blanks)");
        mainOperatorRTA.setStyle("-fx-border-width: 0; -fx-border-color: transparent");
        HBox moHBox = mainOperatorBDRTA.getBoxedRTA();
        moHBox.setPadding(new Insets(0,0,0, RTA_WIDTH_BMP/2));
        moHBox.setAlignment(Pos.CENTER_LEFT);
        moHBox.setStyle("-fx-background-color: rgb(245,245,245)");

        contentBox = new VBox(10, choiceBox, ulineVBox, moHBox);
        contentBox.setPadding(new Insets(15, 0, 15, 0));
        contentBox.setStyle("-fx-background-color: rgb(245,245,245); -fx-border-color: lightgray; -fx-border-width: 1.5");
        contentBox.setAlignment(Pos.CENTER_LEFT);

        undoButton = new Button("Undo");
        redoButton = new Button("Redo");
        undoButton.setPrefWidth(64);
        redoButton.setPrefWidth(64);
        undoButton.setPrefHeight(28);
        redoButton.setPrefHeight(28);

        controlBox.getChildren().addAll(undoButton, redoButton);
        controlBox.setAlignment(Pos.TOP_CENTER);
        controlBox.setPadding(new Insets(40,20,100,20));
        controlBox.setMinWidth(150); controlBox.setMaxWidth(150);

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

        //choice (null spinners)
        choicesHeightSpinner = new Spinner<>(0.0, 999.0, 0, 1.0);
        choicesHeightSpinner.setPrefWidth(65);
        choicesHeightSpinner.setDisable(true);
        choicesHeightSpinner.setTooltip(new Tooltip("Window height as % of selected paper"));

        choicesWidthSpinner = new Spinner<>(100.0, 999.0, 100.0, 1.0);
        choicesWidthSpinner.setPrefWidth(65);
        choicesWidthSpinner.setDisable(true);
        choicesWidthSpinner.setTooltip(new Tooltip("Window width as % of selected paper"));

        //uline box
        ulineVBoxHeightSpinner = new Spinner<>(0.0, 999.0, 0, 1.0);
        ulineVBoxHeightSpinner.setPrefWidth(65);
        ulineVBoxHeightSpinner.setDisable(true);
        ulineVBoxHeightSpinner.setTooltip(new Tooltip("Window height as % of selected paper"));

        double ulineBoxInitialWidth = Math.round(transULExercise.getExerciseModel().getUlBoxPrintWidth() / localScalePageWidth.get() * 100.0);
        ulineVBoxWidthSpinner = new Spinner<>(100.0, 999.0, ulineBoxInitialWidth, 1.0);
        ulineVBoxWidthSpinner.setPrefWidth(65);
        ulineVBoxWidthSpinner.setDisable(true);
        ulineVBoxWidthSpinner.setTooltip(new Tooltip("Window width as % of selected paper"));


        //main operator box
        mainOperatorHeightSpinner = new Spinner<>(0.0, 999.0, 0, 1.0);
        mainOperatorHeightSpinner.setPrefWidth(65);
        mainOperatorHeightSpinner.setDisable(true);
        mainOperatorHeightSpinner.setTooltip(new Tooltip("Window height as % of selected paper"));

        mainOperatorBDRTA.getRTA().minWidthProperty().bind(localScalePageWidth);
        mainOperatorWidthSpinner = new Spinner<>(100.0, 999.0, 100.0, 1.0);
        mainOperatorWidthSpinner.setPrefWidth(65);
        mainOperatorWidthSpinner.setDisable(true);
        mainOperatorWidthSpinner.setTooltip(new Tooltip("Window width as % of selected paper"));

        //page size listeners
        localScalePageHeightListener = (ob, ov, nv) -> {
            if (isTornDown || this.mainView == null) return;
            try {
                if (this.exerciseStatement == null || this.exerciseComment == null || this.choiceBox == null || this.ulineVBox == null || this.mainOperatorBDRTA == null || this.mainOperatorBDRTA.getRTA() == null) return;

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

                if (choicesHeightSpinner != null && choicesHeightSpinner.getValueFactory() != null) {
                    double choicesHeightValue = Math.round(choiceBox.getHeight() / localScalePageHeight.get() * 100);
                    choicesHeightSpinner.getValueFactory().setValue(choicesHeightValue);
                }

                if (ulineVBoxHeightSpinner != null && ulineVBoxHeightSpinner.getValueFactory() != null) {
                    double ulineBoxHeightValue = Math.round(ulineVBox.getHeight() / localScalePageHeight.get() * 100);
                    ulineVBoxHeightSpinner.getValueFactory().setValue(ulineBoxHeightValue);
                }

                if (mainOperatorHeightSpinner != null && mainOperatorHeightSpinner.getValueFactory() != null) {
                    double mainOperatorHeightValue = Math.round(mainOperatorBDRTA.getRTA().getHeight() / localScalePageHeight.get() * 100);
                    mainOperatorHeightSpinner.getValueFactory().setValue(mainOperatorHeightValue);
                }
            } catch (Exception e) {}
        };
        localScalePageHeight.addListener(localScalePageHeightListener);

        localScalePageWidthListener = (ob, ov, nv) -> {
            if (isTornDown || this.mainView == null || this.transULExercise == null || this.transULExercise.getExerciseModel() == null) return;
            try {
                if (ulineVBoxWidthSpinner != null && ulineVBoxWidthSpinner.getValueFactory() != null) {
                    double ulineBoxWidthValue = Math.round(transULExercise.getExerciseModel().getUlBoxPrintWidth() / localScalePageWidth.get() * 100.0);
                    ulineVBoxWidthSpinner.getValueFactory().setValue(ulineBoxWidthValue);
                }
            } catch (Exception e) {}
        };
        localScalePageWidth.addListener(localScalePageWidthListener);

        setSizeSpinners();
    }

    public void setRightControlBox() {
        if (isTornDown) return;
        try {
            bigCheck = new Text("\ue89a");
            bigCheck.setFont(Font.font("Noto Serif Combo", 72));

            checkedElements = new Text(checkMessage);
            checkedElements.setFont(Font.font("Noto Serif Combo", 11));
            TextFlow checkedElementsFlow = new TextFlow(checkedElements);
            checkedElementsFlow.setMaxWidth(150);

            VBox bigCheckBox = new VBox(0, bigCheck, checkedElementsFlow);
            bigCheckBox.setAlignment(Pos.CENTER);
            checkedElementsFlow.setTextAlignment(TextAlignment.CENTER);

            checkButton = new Button("Check");
            checkButton.setPrefWidth(105);
            checkButton.setTooltip(new Tooltip("Check tree for correctness."));
            checkTriesLabel = new Label();

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
        } catch (Exception e) {}
    }

    public void activateBigCheck() {
        if (isTornDown || bigCheck == null || checkedElements == null) return;
        try {
            bigCheck.setFill(Color.LAWNGREEN);
            checkedElements.setText(checkMessage);
            checkedElements.setFill(Color.GREEN);

            if (checkShowing) {
                FadeTransition t1 = new FadeTransition(javafx.util.Duration.millis(250), checkedElements);
                t1.setToValue(1.0);
                t1.setInterpolator(Interpolator.DISCRETE);

                FadeTransition t2 = new FadeTransition(javafx.util.Duration.millis(250), bigCheck);
                t2.setToValue(1.0);
                t2.setInterpolator(Interpolator.DISCRETE);

                if (checkAnimation != null) checkAnimation.stop();
                checkAnimation = new ParallelTransition(t1, t2);
                checkAnimation.play();
            }
            else {
                bigCheck.setOpacity(1.0);
                checkedElements.setOpacity(1.0);
            }
        } catch (Exception e) {}
    }

    public void deactivateBigCheck() {
        if (isTornDown) return;
        try { if (checkAnimation != null) checkAnimation.stop(); } catch (Exception e) {}
        try {
            if (bigCheck == null || checkedElements == null) return;
            if (bigCheck.getOpacity() > .5) checkShowing = true;
            else checkShowing = false;

            bigCheck.setOpacity(0.0);
            checkedElements.setOpacity(0.0);
        } catch (Exception e) {}
    }

    private void setSizeSpinners() {
        try {
            if (choiceBox != null) {
                choiceBoxClickHandler = e -> {
                    if (isTornDown || this.mainView == null) return;
                    if (currentSpinnerNode != choiceBox) {
                        currentSpinnerNode = choiceBox;
                        if (choicesHeightSpinner != null && choicesHeightSpinner.getValueFactory() != null && choicesWidthSpinner != null) {
                            double choicesHeightValue = Math.round(choiceBox.getHeight() / localScalePageHeight.get() * 100);
                            choicesHeightSpinner.getValueFactory().setValue(choicesHeightValue);
                            this.mainView.updateSizeSpinners(choicesHeightSpinner, choicesWidthSpinner);
                        }
                        choiceBox.requestFocus();
                    }
                };
                choiceBox.addEventFilter(MouseEvent.MOUSE_CLICKED, choiceBoxClickHandler);
            }

            if (ulineVBox != null) {
                ulineVBoxClickHandler = e -> {
                    if (isTornDown || this.mainView == null) return;
                    if (currentSpinnerNode != ulineVBox) {
                        currentSpinnerNode = ulineVBox;
                        if (ulineVBoxHeightSpinner != null && ulineVBoxHeightSpinner.getValueFactory() != null && ulineVBoxWidthSpinner != null) {
                            double ulineVBoxHeightValue = Math.round(ulineVBox.getHeight() / localScalePageHeight.get() * 100);
                            ulineVBoxHeightSpinner.getValueFactory().setValue(ulineVBoxHeightValue);
                            this.mainView.updateSizeSpinners(ulineVBoxHeightSpinner, ulineVBoxWidthSpinner);
                        }
                        ulineVBox.requestFocus();
                    }
                };
                ulineVBox.addEventFilter(MouseEvent.MOUSE_CLICKED, ulineVBoxClickHandler);
            }

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

    public void showStaticHelp(Document doc) {
        if (isTornDown || this.mainView == null) return;
        try {
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
                if (EditorMain.mainStage != null) {
                    staticHelpStage.setX(Math.min(EditorMain.mainStage.getX() + EditorMain.mainStage.getWidth(), bounds.getMaxX() - (420 + RTA_WIDTH_BMP)));
                    staticHelpStage.setY(Math.min(EditorMain.mainStage.getY() + 20, bounds.getMaxY() - 320));
                }

                staticHelpStage.show();
            }
        } catch (Exception e) {}
    }

    // SCORCHED EARTH RULE 6: Helper to Forcefully Yank Shared Nodes
    private void severFromParent(Node node) {
        try {
            if (node != null && node.getParent() != null) {
                if (node.getParent() instanceof javafx.scene.layout.Pane) {
                    ((javafx.scene.layout.Pane) node.getParent()).getChildren().remove(node);
                } else if (node.getParent() instanceof javafx.scene.Group) {
                    ((javafx.scene.Group) node.getParent()).getChildren().remove(node);
                }
            }
        } catch (Exception e) {}
    }

    // SCORCHED EARTH RULE 5: Rule of Isolation applied heavily inside cleanup()
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

        try { if (checkAnimation != null) checkAnimation.stop(); } catch (Exception e) {}
        try { if (choiceBox != null && choiceBoxClickHandler != null) choiceBox.removeEventFilter(MouseEvent.MOUSE_CLICKED, choiceBoxClickHandler); } catch (Exception e) {}
        try { if (ulineVBox != null && ulineVBoxClickHandler != null) ulineVBox.removeEventFilter(MouseEvent.MOUSE_CLICKED, ulineVBoxClickHandler); } catch (Exception e) {}


        // SCORCHED EARTH RULE 3: Eradicate Anonymous Lambdas (Remove tracked listeners safely)
        try { if (localScalePageHeight != null && localScalePageHeightListener != null) localScalePageHeight.removeListener(localScalePageHeightListener); } catch (Exception e) {}
        try { if (localScalePageHeight != null) localScalePageHeight.unbind(); } catch (Exception e) {}

        try { if (localScalePageWidth != null && localScalePageWidthListener != null) localScalePageWidth.removeListener(localScalePageWidthListener); } catch (Exception e) {}
        try { if (localScalePageWidth != null) localScalePageWidth.unbind(); } catch (Exception e) {}

        try { if (undoRedoFlag != null) undoRedoFlag.unbind(); } catch (Exception e) {}

        // Isolated focus listener removal to guarantee execution!
        try {
            if (mainView != null && mainView.getMainScene() != null && exerciseFocusListener != null) {
                mainView.getMainScene().focusOwnerProperty().removeListener(exerciseFocusListener);
            }
        } catch (Exception e) {}

        // Unbind localized properties piece by isolated piece
        try { if (exerciseStatement != null && exerciseStatement.getEditor() != null) exerciseStatement.getEditor().prefHeightProperty().unbind(); } catch (Exception e) {}
        try { if (exerciseStatement != null && exerciseStatement.getEditor() != null) exerciseStatement.getEditor().maxWidthProperty().unbind(); } catch (Exception e) {}
        try { if (exerciseStatement != null && exerciseStatement.getEditor() != null) exerciseStatement.getEditor().contentAreaWidthProperty().unbind(); } catch (Exception e) {}

        try { if (exerciseComment != null && exerciseComment.getEditor() != null) exerciseComment.getEditor().prefHeightProperty().unbind(); } catch (Exception e) {}
        try { if (exerciseComment != null && exerciseComment.getEditor() != null) exerciseComment.getEditor().minWidthProperty().unbind(); } catch (Exception e) {}
        try { if (exerciseComment != null && exerciseComment.getEditor() != null) exerciseComment.getEditor().maxWidthProperty().unbind(); } catch (Exception e) {}
        try { if (exerciseComment != null && exerciseComment.getEditor() != null) exerciseComment.getEditor().contentAreaWidthProperty().unbind(); } catch (Exception e) {}

        try { if (choiceBox != null) choiceBox.maxWidthProperty().unbind(); } catch (Exception e) {}
        try { if (choiceBox != null) choiceBox.minWidthProperty().unbind(); } catch (Exception e) {}

        try { if (mainOperatorBDRTA != null && mainOperatorBDRTA.getRTA() != null) mainOperatorBDRTA.getRTA().minWidthProperty().unbind(); } catch (Exception e) {}

        // ---> SCORCHED EARTH RULE 4: THE JAVAFX SKIN HAMMER <---
        try {
            final RichTextArea stEditor = (exerciseStatement != null) ? exerciseStatement.getEditor() : null;
            final RichTextArea commEditor = (exerciseComment != null) ? exerciseComment.getEditor() : null;
            final BoxedDRTA moBDRTA = (mainOperatorBDRTA != null) ? mainOperatorBDRTA : null;
            final RichTextArea moEditor = (moBDRTA != null) ? moBDRTA.getRTA() : null;

            final List<RichTextArea> rtasToClear = new ArrayList<>();
            if (stEditor != null) rtasToClear.add(stEditor);
            if (commEditor != null) rtasToClear.add(commEditor);
            if (moEditor != null) rtasToClear.add(moEditor);

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

        // SCORCHED EARTH RULE 1: Cascade child teardowns
        try { if (ulineBox != null) ulineBox.cleanup(); } catch (Exception e) {}

        // SCORCHED EARTH RULE 6: Forcefully detach shared nodes
        try { if (pointsEarnedTextField != null) pointsEarnedTextField.setTextFormatter(null); } catch (Exception e) {}
        try { if (pointsEarnedTextField != null) severFromParent(pointsEarnedTextField); } catch (Exception e) {}
        try { if (rightControlNode != null) severFromParent(rightControlNode); } catch (Exception e) {}
        try { if (controlBox != null) severFromParent(controlBox); } catch (Exception e) {}
        try { if (contentBox != null) severFromParent(contentBox); } catch (Exception e) {}
        try { if (exerciseStatement != null) severFromParent(exerciseStatement.getEditor()); } catch (Exception e) {}
        try { if (exerciseComment != null) severFromParent(exerciseComment.getEditor()); } catch (Exception e) {}

        try { if (statementHeightSpinner != null) severFromParent(statementHeightSpinner); } catch (Exception e) {}
        try { if (statementWidthSpinner != null) severFromParent(statementWidthSpinner); } catch (Exception e) {}
        try { if (commentHeightSpinner != null) severFromParent(commentHeightSpinner); } catch (Exception e) {}
        try { if (commentWidthSpinner != null) severFromParent(commentWidthSpinner); } catch (Exception e) {}
        try { if (choicesHeightSpinner != null) severFromParent(choicesHeightSpinner); } catch (Exception e) {}
        try { if (choicesWidthSpinner != null) severFromParent(choicesWidthSpinner); } catch (Exception e) {}
        try { if (ulineVBoxHeightSpinner != null) severFromParent(ulineVBoxHeightSpinner); } catch (Exception e) {}
        try { if (ulineVBoxWidthSpinner != null) severFromParent(ulineVBoxWidthSpinner); } catch (Exception e) {}
        try { if (mainOperatorHeightSpinner != null) severFromParent(mainOperatorHeightSpinner); } catch (Exception e) {}
        try { if (mainOperatorWidthSpinner != null) severFromParent(mainOperatorWidthSpinner); } catch (Exception e) {}

        // Clear children
        try { if (contentBox != null) contentBox.getChildren().clear(); } catch (Exception e) {}
        try { if (controlBox != null) controlBox.getChildren().clear(); } catch (Exception e) {}
        try { if (choiceBox != null) choiceBox.getChildren().clear(); } catch (Exception e) {}
        try { if (ulineVBox != null) ulineVBox.getChildren().clear(); } catch (Exception e) {}
        try { if (rightControlNode instanceof VBox) ((VBox) rightControlNode).getChildren().clear(); } catch (Exception e) {}

        // Unhook Actions
        try { if (choiceBox != null) choiceBox.setOnMousePressed(null); } catch (Exception e) {}
        try { if (undoButton != null) undoButton.setOnAction(null); } catch (Exception e) {}
        try { if (redoButton != null) redoButton.setOnAction(null); } catch (Exception e) {}
        try { if (checkButton != null) checkButton.setOnAction(null); } catch (Exception e) {}
        try { if (staticHelpButton != null) staticHelpButton.setOnAction(null); } catch (Exception e) {}

        try {
            if (aCheckBox != null) {
                aCheckBox.selectedProperty().unbind();
                aCheckBox.setOnAction(null);
            }
        } catch (Exception e) {}

        try {
            if (bCheckBox != null) {
                bCheckBox.selectedProperty().unbind();
                bCheckBox.setOnAction(null);
            }
        } catch (Exception e) {}

        // Close Popups
        try { if (staticHelpStage != null) staticHelpStage.close(); } catch (Exception e) {}

        // SCORCHED EARTH RULE 1: Final Nullification
        try { this.mainView = null; } catch (Exception e) {}
        try { this.transULExercise = null; } catch (Exception e) {}
        try { this.aCheckBox = null; } catch (Exception e) {}
        try { this.bCheckBox = null; } catch (Exception e) {}
        try { this.choiceBox = null; } catch (Exception e) {}
        try { this.exerciseComment = null; } catch (Exception e) {}
        try { this.exerciseStatement = null; } catch (Exception e) {}
        try { this.ulineBox = null; } catch (Exception e) {}
        try { this.ulineVBox = null; } catch (Exception e) {}
        try { this.mainOperatorBDRTA = null; } catch (Exception e) {}
        try { this.contentBox = null; } catch (Exception e) {}

        try { this.statementHeightSpinner = null; } catch (Exception e) {}
        try { this.statementWidthSpinner = null; } catch (Exception e) {}
        try { this.commentHeightSpinner = null; } catch (Exception e) {}
        try { this.commentWidthSpinner = null; } catch (Exception e) {}
        try { this.choicesHeightSpinner = null; } catch (Exception e) {}
        try { this.choicesWidthSpinner = null; } catch (Exception e) {}
        try { this.ulineVBoxHeightSpinner = null; } catch (Exception e) {}
        try { this.ulineVBoxWidthSpinner = null; } catch (Exception e) {}
        try { this.mainOperatorHeightSpinner = null; } catch (Exception e) {}
        try { this.mainOperatorWidthSpinner = null; } catch (Exception e) {}

        try { this.currentSpinnerNode = null; } catch (Exception e) {}
        try { this.controlBox = null; } catch (Exception e) {}
        try { this.undoButton = null; } catch (Exception e) {}
        try { this.redoButton = null; } catch (Exception e) {}
        try { this.pointsEarnedTextField = null; } catch (Exception e) {}
        try { this.rightControlNode = null; } catch (Exception e) {}
        try { this.checkButton = null; } catch (Exception e) {}
        try { this.bigCheck = null; } catch (Exception e) {}
        try { this.checkedElements = null; } catch (Exception e) {}
        try { this.checkMessage = null; } catch (Exception e) {}
        try { this.checkTriesLabel = null; } catch (Exception e) {}
        try { this.checkColor = null; } catch (Exception e) {}
        try { this.staticHelpButton = null; } catch (Exception e) {}
        try { this.staticHelpStage = null; } catch (Exception e) {}

        try { this.exerciseFocusListener = null; } catch (Exception e) {}
        try { this.pointsEarnedFilter = null; } catch (Exception e) {}
        try { this.localScalePageHeightListener = null; } catch (Exception e) {}
        try { this.localScalePageWidthListener = null; } catch (Exception e) {}
        try { this.checkAnimation = null; } catch (Exception e) {}
        try { this.choiceBoxClickHandler = null; } catch (Exception e) {}
        try { this.ulineVBoxClickHandler = null; } catch (Exception e) {}

        try {this.pointsEarnedFocusListener = null; } catch (Exception e) {}
        try { this.pointsEarnedClickHandler = null; } catch (Exception e) {}

    }

    public Button getUndoButton() { return undoButton;   }
    public Button getRedoButton() { return redoButton;   }
    public CheckBox getaCheckBox() {  return aCheckBox;  }
    public CheckBox getbCheckBox() {  return bCheckBox;  }
    public double getCommentPrefHeight() { return exerciseComment.getEditor().getPrefHeight();   }
    public void setCommentPrefHeight(double commentPrefHeight) {   this.commentPrefHeight = commentPrefHeight;  }
    public Button getCheckButton() { return checkButton; }
    public Button getstaticHelpButton() { return staticHelpButton; }
    public Label getCheckTriesLabel() { return checkTriesLabel; }
    public void setCheckMessage(String checkMessage) { this.checkMessage = checkMessage; }
    public String getCheckMessage() { return checkMessage; }
    public Stage getStaticHelpStage() { return staticHelpStage; }
    public SimpleULineBox getUlineBox() { return ulineBox; }
    public BoxedDRTA getMainOperatorBDRTA() { return mainOperatorBDRTA; }
    public VBox getControlBox() { return controlBox; }

    @Override
    public DecoratedRTA getExerciseComment() { return exerciseComment; }
    @Override
    public void setExerciseComment(DecoratedRTA exerciseComment) { this.exerciseComment = exerciseComment;  }
    @Override
    public DecoratedRTA getExerciseStatement() { return exerciseStatement; }
    @Override
    public void setExerciseStatement(DecoratedRTA exerciseStatement) { this.exerciseStatement = exerciseStatement; }

    @Override
    public Node getExerciseStatementNode() { return exerciseStatement.getEditor();   }

    @Override
    public void setStatementPrefHeight(double height) {
        statementPrefHeight = height;
        try {
            if (exerciseStatement != null && exerciseStatement.getEditor() != null) {
                exerciseStatement.getEditor().setPrefHeight(height);
            }
        } catch (Exception e) {}
    }

    @Override
    public Node getExerciseContentNode() { return contentBox;   }
    @Override
    public Node getExerciseControl() { return controlBox; }
    @Override
    public Node getRightControl() { return rightControlNode; }

    @Override
    public Node getPointsNode() {
        if (pointsPossible > 0) {
            try {
                if (mainView != null && !mainView.isInstructorFunctions() && pointsEarnedTextField != null) pointsEarnedTextField.setDisable(true);
            } catch (Exception e) {}
            return pointsEarnedTextField;
        }
        return null;
    }

    public void setPointsPossible(int pointsPossible) { this.pointsPossible = pointsPossible; }
    @Override
    public int getPointsPossible() { return pointsPossible;  }
    public TextField getPointsEarnedTextField() { return pointsEarnedTextField; }
    public DoubleProperty localScalePageHeightProperty() { return localScalePageHeight; }
    public DoubleProperty localScalePageWidthProperty() { return localScalePageWidth; }
    public ChangeListener<Node> getExerciseFocusListener() { return exerciseFocusListener; }
    public VBox getContentBox() { return contentBox; }
}
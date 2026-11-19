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

package slapp.editor.basic_trans;

import com.gluonhq.richtextarea.RichTextArea;
import com.gluonhq.richtextarea.RichTextAreaSkin;
import com.gluonhq.richtextarea.model.Document;
import javafx.animation.*;
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

import java.util.ArrayList;
import java.util.List;
import java.util.function.UnaryOperator;

import static slapp.editor.EditorMain.RTA_SCREEN_BMP;
import static slapp.editor.EditorMain.RTA_WIDTH_BMP;

/**
 * View for the simple translate exercise
 */


public class BasicTransView implements ExerciseView<DecoratedRTA> {

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
    private int pointsPossible;
    private TextField pointsEarnedTextField;
    private VBox controlBox;

    private Node rightControlNode;
    private Button checkButton;
    private Text bigCheck;
    private String checkMessage;
    private Text checkedElements;
    private Label checkTriesLabel;
    private Color checkSubtitleColor;
    private Color checkColor;
    private Button staticHelpButton;
    private Stage staticHelpStage;
    private String interpFieldPrompt = "Interpretation Function";

    private Label processingLabel;
    private ProgressIndicator progressIndicator;
    private FadeTransition spinnerFadeTransition;
    private FadeTransition labelFadeTransition;

    private PauseTransition delaySpinner;
    private ParallelTransition currentAnimations;
    private Timeline progressTimeline;
    private boolean checkShowing = false;
    private boolean needsBlink = false;
    private VBox bigCheckBox;
    private VBox processingBox;
    private StackPane statusPane;

    private final DoubleProperty localScalePageHeight = new SimpleDoubleProperty(1.0);
    private final DoubleProperty localScalePageWidth = new SimpleDoubleProperty(1.0);

    // SCORCHED EARTH RULE 2: Kill Switch for asynchronous event interception during teardown
    private boolean isTornDown = false;

    // SCORCHED EARTH RULE 3: Eradicate Anonymous Lambdas
    private ChangeListener<Node> exerciseFocusListener;
    private ChangeListener<Number> localScalePageHeightListener;
    private UnaryOperator<TextFormatter.Change> pointsEarnedFilter;
    private EventHandler<ActionEvent> delaySpinnerHandler;

    private ChangeListener<Boolean> pointsEarnedFocusListener;
    private EventHandler<MouseEvent> pointsEarnedClickHandler;

    private boolean requireUniv = true;

    /**
     * Construct simple translate view
     * @param mainView the main view
     */
    public BasicTransView(MainWindowView mainView) {
        this.mainView = mainView;

        localScalePageHeight.bind(mainView.scalePageHeightProperty());
        localScalePageWidth.bind(mainView.scalePageWidthProperty());

        exerciseFocusListener = (ob, ov, nv) -> {
            if (isTornDown || this.mainView == null) return;
            try {
                RichTextArea statementRTA = exerciseStatement != null ? exerciseStatement.getEditor() : null;
                RichTextArea commentRTA = exerciseComment != null ? exerciseComment.getEditor() : null;
                RichTextArea responseRTA = exerciseResponse != null? exerciseResponse.getEditor() : null;
                RichTextArea interpretationRTA = exerciseInterpretation != null ? exerciseInterpretation.getEditor() : null;

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
                } else if (interpretationRTA != null && inHierarchy(nv, interpretationRTA)) {
                    if (currentSpinnerNode != interpretationRTA) {
                        currentSpinnerNode = interpretationRTA;
                        if (interpretationHeightSpinner != null && interpretationWidthSpinner != null) {
                            this.mainView.updateSizeSpinners(interpretationHeightSpinner, interpretationWidthSpinner);
                        }
                    }
                } else {
                    currentSpinnerNode = null;
                }
            } catch (Exception e) {}
        };

        //the control box is empty, but want to hold the space
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
     * Initialize page management for statement, comment, interpretation, and translate fields
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
        commentRTA.maxHeightProperty().bind(commentRTA.prefHeightProperty());

        commentRTA.prefWidthProperty().bind(Bindings.add(localScalePageWidth, RTA_WIDTH_BMP));
        commentRTA.contentAreaWidthProperty().bind(localScalePageWidth);

        commentWidthSpinner = new Spinner<>(0.0, 999.0, 100, 1.0);
        commentWidthSpinner.setPrefWidth(65);
        commentWidthSpinner.setDisable(true);
        commentWidthSpinner.setTooltip(new Tooltip("Window width as % of selected paper"));

        //interpretation rta
        RichTextArea interpretationRTA = exerciseInterpretation.getEditor();
        interpretationRTA.getStylesheets().add("slappTextArea.css");
        interpretationRTA.setPromptText("Interpretation Function");

    //    System.out.println("pref height: " + interpretationPrefHeight);
        double interpretationInitialHeight = Math.round((interpretationPrefHeight - RTA_SCREEN_BMP) / localScalePageHeight.get() * 100 ) ;
        interpretationHeightSpinner = new Spinner<>(spinnerMin, 999.0, interpretationInitialHeight, 1.0);
        interpretationHeightSpinner.setPrefWidth(65);
        interpretationHeightSpinner.setDisable(false);
        interpretationHeightSpinner.setTooltip(new Tooltip("Window height as % of selected paper"));

        interpretationRTA.prefHeightProperty().bind(Bindings.add(Bindings.max(45.0, Bindings.multiply(localScalePageHeight, DoubleProperty.doubleProperty(interpretationHeightSpinner.getValueFactory().valueProperty()).divide(100.0))), RTA_SCREEN_BMP));
        interpretationRTA.maxHeightProperty().bind(interpretationRTA.prefHeightProperty());

        interpretationRTA.prefWidthProperty().bind(Bindings.add(localScalePageWidth, RTA_WIDTH_BMP));
        interpretationRTA.contentAreaWidthProperty().bind(localScalePageWidth);
        interpretationWidthSpinner = new Spinner<>(0.0, 999.0, 100, 5.0);
        interpretationWidthSpinner.setPrefWidth(65);
        interpretationWidthSpinner.setDisable(true);
        interpretationWidthSpinner.setTooltip(new Tooltip("Window width as % of selected paper"));


        //response rta
        RichTextArea responseRTA = exerciseResponse.getEditor();
        responseRTA.getStylesheets().add("slappTextArea.css");
        responseRTA.setPromptText("Formal Translation:");
        exerciseResponse.getKeyboardSelector().valueProperty().setValue(RichTextAreaSkin.KeyMapValue.ITALIC_AND_SANS);

        double responseInitialHeight = Math.round((responsePrefHeight - RTA_SCREEN_BMP) / localScalePageHeight.get() * 100.0);

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

                if (exerciseInterpretation != null && exerciseInterpretation.getEditor() != null) {
                    exerciseInterpretation.getEditor().prefHeightProperty().unbind();
                    if (interpretationHeightSpinner != null && interpretationHeightSpinner.getValueFactory() != null) {
                        interpretationHeightSpinner.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(revSpinnerMin, 999.0, (double) Math.round(interpretationHeightSpinner.getValue() * ov.doubleValue() / nv.doubleValue()), 1.0));
                        exerciseInterpretation.getEditor().prefHeightProperty().bind(Bindings.add(Bindings.max(45.0, Bindings.multiply(nv.doubleValue(), DoubleProperty.doubleProperty(interpretationHeightSpinner.getValueFactory().valueProperty()).divide(100.0))), RTA_SCREEN_BMP));
                    }
                }
            } catch (Exception e) {}
        };
        localScalePageHeight.addListener(localScalePageHeightListener);

        setSizeSpinners();
    }

    private void setSizeSpinners() {
        try {
            if (mainView != null && mainView.getMainScene() != null && exerciseFocusListener != null) {
                mainView.getMainScene().focusOwnerProperty().addListener(exerciseFocusListener);
            }
        } catch (Exception e) {}
    }

    public void setRightControlBox() {
        bigCheck = new Text("\ue89a");
        bigCheck.setFont(Font.font("Noto Serif Combo", 72));

        checkedElements = new Text(checkMessage);
        checkedElements.setFont(Font.font("Noto Serif Combo", 11));
        TextFlow checkedElementsFlow = new TextFlow(checkedElements);
        checkedElementsFlow.setMaxWidth(150);

        bigCheckBox = new VBox(0, bigCheck, checkedElementsFlow);
        bigCheckBox.setAlignment(Pos.CENTER);
        checkedElementsFlow.setTextAlignment(TextAlignment.CENTER);

        checkButton = new Button("Check");
        checkButton.setPrefWidth(105);
        checkButton.setTooltip(new Tooltip("Check translation for correctness."));
        checkTriesLabel = new Label();

        processingLabel = new Label("Equivalence Check:");
        processingLabel.setVisible(false);
        progressIndicator = new ProgressIndicator();
        progressIndicator.setPrefSize(60, 60);
        progressIndicator.setMinSize(60, 60);
        progressIndicator.setVisible(false);

        processingBox = new VBox(10, processingLabel, progressIndicator);
        processingBox.setAlignment(Pos.CENTER);

        VBox checksBox = new VBox(10, checkButton, checkTriesLabel );
        checksBox.setAlignment(Pos.CENTER);
        checkTriesLabel.setAlignment(Pos.CENTER);

        statusPane = new StackPane();
        statusPane.getChildren().addAll(bigCheckBox, processingBox);

        staticHelpButton = new Button("Static Help");
        staticHelpButton.setPrefWidth(105);
        staticHelpButton.setTooltip(new Tooltip("Get static help text"));

        VBox rightControlBox = new VBox(40, statusPane, checksBox, staticHelpButton);
        rightControlBox.setAlignment(Pos.TOP_CENTER);
        rightControlBox.setPadding(new Insets(60,20,0,20));
        rightControlNode = rightControlBox;

        deactivateBigCheck();
    }

    public void activateProcessingIndicator() {
        if (isTornDown) return;
        try {
            progressIndicator.setProgress(0.0);
            progressTimeline = new Timeline(
                    new KeyFrame(Duration.ZERO, new KeyValue(progressIndicator.progressProperty(), 0.0)),
                    new KeyFrame(Duration.seconds(15), new KeyValue(progressIndicator.progressProperty(), 0.999))
            );
            progressTimeline.play();

            delaySpinner = new PauseTransition(Duration.millis(300));
            delaySpinnerHandler = e -> {
                if (isTornDown) return;
                try {
                    needsBlink = false;
                    progressIndicator.setOpacity(0.0);
                    processingLabel.setOpacity(0.0);

                    progressIndicator.setVisible(true);
                    processingLabel.setVisible(true);

                    FadeTransition t1 = new FadeTransition(Duration.millis(400), progressIndicator);
                    t1.setToValue(1.0);

                    FadeTransition t2 = new FadeTransition(Duration.millis(400), processingLabel);
                    t2.setToValue(1.0);

                    currentAnimations = new ParallelTransition(t1, t2);
                    currentAnimations.play();
                } catch (Exception ex) {}
            };
            delaySpinner.setOnFinished(delaySpinnerHandler);
            delaySpinner.play();
        } catch (Exception e) {}
    }

    public void deactivateProcessingIndicator() {
        try { if (delaySpinner != null) delaySpinner.stop(); } catch (Exception e) {}
        try { if (progressTimeline != null) progressTimeline.stop(); } catch (Exception e) {}
        try { if (currentAnimations != null) currentAnimations.stop(); } catch (Exception e) {}

        try {
            if (progressIndicator != null) {
                progressIndicator.setVisible(false);
                progressIndicator.setProgress(ProgressIndicator.INDETERMINATE_PROGRESS);
            }
            if (processingLabel != null) {
                processingLabel.setVisible(false);
            }
        } catch (Exception e) {}
    }

    public void activateBigCheck() {
        if (isTornDown) return;
        deactivateProcessingIndicator();

        try {
            if (bigCheck != null && checkedElements != null) {
                bigCheck.setFill(checkColor);
                checkedElements.setText(checkMessage);
                checkedElements.setFill(checkSubtitleColor);

                bigCheck.setVisible(true);
                checkedElements.setVisible(true);

                if (needsBlink) {
                    FadeTransition t1 = new FadeTransition(Duration.millis(250), checkedElements);
                    t1.setFromValue(0.0);
                    t1.setToValue(1.0);
                    t1.setInterpolator(Interpolator.DISCRETE);

                    FadeTransition t2 = new FadeTransition(Duration.millis(250), bigCheck);
                    t2.setFromValue(0.0);
                    t2.setToValue(1.0);
                    t2.setInterpolator(Interpolator.DISCRETE);

                    currentAnimations = new ParallelTransition(t1, t2);
                    currentAnimations.play();
                } else {
                    bigCheck.setOpacity(1.0);
                    checkedElements.setOpacity(1.0);
                }

                needsBlink = false;
                checkShowing = true;
            }
        } catch (Exception e) {}
    }

    public void deactivateBigCheck() {
        try { if (currentAnimations != null) currentAnimations.stop(); } catch (Exception e) {}
        try {
            if (bigCheck != null) bigCheck.setVisible(false);
            if (checkedElements != null) checkedElements.setVisible(false);
        } catch (Exception e) {}
        checkShowing = false;
    }

    public void prepareForNewCheck() {
        if (isTornDown) return;
        needsBlink = checkShowing;
        deactivateBigCheck();
        deactivateProcessingIndicator();
    }

    public void clearStatusDisplay() {
        try { if (delaySpinner != null) delaySpinner.stop(); } catch (Exception e) {}
        try { if (progressTimeline != null) progressTimeline.stop(); } catch (Exception e) {}
        try { if (currentAnimations != null) currentAnimations.stop(); } catch (Exception e) {}

        try {
            if (bigCheck != null) bigCheck.setVisible(false);
            if (checkedElements != null) checkedElements.setVisible(false);
            if (progressIndicator != null) progressIndicator.setVisible(false);
            if (processingLabel != null) processingLabel.setVisible(false);
        } catch (Exception e) {}
    }

    public void showStaticHelp(Document doc) {
        if (isTornDown) return;
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
                staticHelpStage.setX(Math.min(EditorMain.mainStage.getX() + EditorMain.mainStage.getWidth(), bounds.getMaxX() - (420 + RTA_WIDTH_BMP)));
                staticHelpStage.setY(Math.min(EditorMain.mainStage.getY() + 20, bounds.getMaxY() - 320));

                staticHelpStage.show();
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

        // Halt Timelines and Transitions early
        try {
            if (delaySpinner != null) {
                delaySpinner.stop();
                delaySpinner.setOnFinished(null);
            }
        } catch (Exception e) {}
        try { if (progressTimeline != null) progressTimeline.stop(); } catch (Exception e) {}
        try { if (currentAnimations != null) currentAnimations.stop(); } catch (Exception e) {}

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
            if (checkButton != null) {
                checkButton.setOnAction(null);
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
                exerciseComment.getEditor().maxHeightProperty().unbind();
                exerciseComment.getEditor().prefWidthProperty().unbind();
                exerciseComment.getEditor().contentAreaWidthProperty().unbind();
            }
        } catch (Exception e) {}

        try {
            if (exerciseInterpretation != null && exerciseInterpretation.getEditor() != null) {
                exerciseInterpretation.getEditor().prefHeightProperty().unbind();
                exerciseInterpretation.getEditor().maxHeightProperty().unbind();
                exerciseInterpretation.getEditor().prefWidthProperty().unbind();
                exerciseInterpretation.getEditor().contentAreaWidthProperty().unbind();
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

        // ---> SCORCHED EARTH RULE 4: THE ISOLATED JAVAFX SKIN HAMMER <---
        try {
            final RichTextArea stEditor = (exerciseStatement != null) ? exerciseStatement.getEditor() : null;
            final RichTextArea commEditor = (exerciseComment != null) ? exerciseComment.getEditor() : null;
            final RichTextArea interpEditor = (exerciseInterpretation != null) ? exerciseInterpretation.getEditor() : null;
            final RichTextArea respEditor = (exerciseResponse != null) ? exerciseResponse.getEditor() : null;
            final RichTextArea helpEditor = (staticHelpStage != null && staticHelpStage.getScene() != null
                    && staticHelpStage.getScene().getRoot() instanceof RichTextArea)
                    ? (RichTextArea) staticHelpStage.getScene().getRoot() : null;
            final Stage helpStageRef = staticHelpStage;

            final List<RichTextArea> rtasToClear = new ArrayList<>();
            if (stEditor != null) rtasToClear.add(stEditor);
            if (commEditor != null) rtasToClear.add(commEditor);
            if (interpEditor != null) rtasToClear.add(interpEditor);
            if (respEditor != null) rtasToClear.add(respEditor);
            if (helpEditor != null) rtasToClear.add(helpEditor);

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
                    try {
                        if (helpStageRef != null) {
                            if (helpStageRef.getScene() != null) helpStageRef.getScene().setRoot(new Pane());
                            helpStageRef.close();
                        }
                    } catch (Exception ex) {}
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
            if (exerciseControlNode != null && exerciseControlNode != controlBox) {
                severFromParent(exerciseControlNode);
                if (exerciseControlNode instanceof Pane) ((Pane) exerciseControlNode).getChildren().clear();
            }
        } catch (Exception e) {}

        try {
            if (rightControlNode != null) {
                severFromParent(rightControlNode);
                if (rightControlNode instanceof Pane) ((Pane) rightControlNode).getChildren().clear();
            }
        } catch (Exception e) {}

        try {
            if (bigCheckBox != null) {
                severFromParent(bigCheckBox);
                bigCheckBox.getChildren().clear();
            }
        } catch (Exception e) {}

        try {
            if (processingBox != null) {
                severFromParent(processingBox);
                processingBox.getChildren().clear();
            }
        } catch (Exception e) {}

        try {
            if (statusPane != null) {
                severFromParent(statusPane);
                statusPane.getChildren().clear();
            }
        } catch (Exception e) {}

        try {
            if (staticHelpStage != null) {
                staticHelpStage.close();
            }
        } catch (Exception e) {}

        // SCORCHED EARTH RULE 1: Final Nullification
        this.mainView = null;
        this.exerciseStatement = null;
        this.exerciseComment = null;
        this.exerciseResponse = null;
        this.exerciseInterpretation = null;
        this.exerciseControlNode = null;
        this.statementHeightSpinner = null;
        this.statementWidthSpinner = null;
        this.commentHeightSpinner = null;
        this.commentWidthSpinner = null;
        this.responseHeightSpinner = null;
        this.responseWidthSpinner = null;
        this.interpretationHeightSpinner = null;
        this.interpretationWidthSpinner = null;
        this.currentSpinnerNode = null;
        this.pointsEarnedTextField = null;
        this.controlBox = null;
        this.rightControlNode = null;
        this.checkButton = null;
        this.bigCheck = null;
        this.checkMessage = null;
        this.checkedElements = null;
        this.checkTriesLabel = null;
        this.checkSubtitleColor = null;
        this.checkColor = null;
        this.staticHelpButton = null;
        this.staticHelpStage = null;
        this.interpFieldPrompt = null;
        this.processingLabel = null;
        this.progressIndicator = null;
        this.spinnerFadeTransition = null;
        this.labelFadeTransition = null;
        this.delaySpinner = null;
        this.currentAnimations = null;
        this.progressTimeline = null;
        this.bigCheckBox = null;
        this.processingBox = null;
        this.statusPane = null;

        this.exerciseFocusListener = null;
        this.localScalePageHeightListener = null;
        this.pointsEarnedFilter = null;
        this.delaySpinnerHandler = null;

        this.pointsEarnedFocusListener = null;
        this.pointsEarnedClickHandler = null;
    }

    public Label getCheckTriesLabel() { return checkTriesLabel; }
    public double getCommentPrefHeight() { return exerciseComment.getEditor().getPrefHeight(); }
    public void setCommentPrefHeight(double commentPrefHeight) { this.commentPrefHeight = commentPrefHeight; }
    public double getResponsePrefHeight() { return exerciseResponse.getEditor().getPrefHeight(); }
    public void setResponsePrefHeight(double responsePrefHeight) { this.responsePrefHeight = responsePrefHeight; }
    public DecoratedRTA getExerciseResponse() { return exerciseResponse; }
    public void setExerciseResponse(DecoratedRTA exerciseResponse) { this.exerciseResponse = exerciseResponse; }
    public DecoratedRTA getExerciseInterpretation() { return exerciseInterpretation; }
    public void setExerciseInterpretation(DecoratedRTA exerciseInterpretation) { this.exerciseInterpretation = exerciseInterpretation; }
    public double getInterpretationPrefHeight() { return exerciseInterpretation.getEditor().getPrefHeight(); }
    public void setInterpretationPrefHeight(double interpretationPrefHeight) { this.interpretationPrefHeight = interpretationPrefHeight; }

    public VBox getControlBox() { return controlBox; }
    public Button getCheckButton() { return checkButton; }
    public Button getStaticHelpButton() { return staticHelpButton; }
    public Stage getStaticHelpStage() { return staticHelpStage; }
    public void setCheckMessage(String checkMessage) { this.checkMessage = checkMessage; }
    public String getCheckMessage() { return checkMessage; }
    public void setCheckColor(Color checkColor) { this.checkColor = checkColor; }
    public void setCheckSubtitleColor(Color checkSubtitleColor) { this.checkSubtitleColor = checkSubtitleColor; }

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
    public Node getExerciseContentNode() {
        if (exerciseInterpretation != null && exerciseResponse != null) {
            return new VBox(2, exerciseInterpretation.getEditor(), exerciseResponse.getEditor());
        }
        return null;
    }

    @Override
    public Node getExerciseControl() { return exerciseControlNode; }
    @Override
    public Node getRightControl() { return rightControlNode; }

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

    public void setRequireUniv(boolean requireUniv) {
        this.requireUniv = requireUniv;
    }
}
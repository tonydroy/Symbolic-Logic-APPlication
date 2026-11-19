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

package slapp.editor.vert_tree_abexplain;

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

import java.util.function.UnaryOperator;
import static slapp.editor.EditorMain.RTA_SCREEN_BMP;
import static slapp.editor.EditorMain.RTA_WIDTH_BMP;

import javafx.scene.control.*;
import javafx.scene.layout.*;

import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.event.EventHandler;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import java.util.ArrayList;
import java.util.List;


public class VerticalTreeABExpView implements ExerciseView<DecoratedRTA> {

    private MainWindowView mainView;
    private VerticalTreeABExpExercise vtABExpExercise;
    private RichTextAreaSkin.KeyMapValue defaultKeyboard;
    private RichTextAreaSkin.KeyMapValue defaultMapKeyboard;
    private String explainPrompt = "";
    private Label choiceLeadLabel = new Label();
    private CheckBox aCheckBox = new CheckBox();
    private CheckBox bCheckBox = new CheckBox();
    private HBox choiceBox;
    private BorderPane root;
    private ABExpRootLayout rootLayout;
    private DecoratedRTA exerciseComment = new DecoratedRTA();
    private DecoratedRTA exerciseStatement = new DecoratedRTA();
    private double statementPrefHeight = 0;

    private double commentPrefHeight = 0;
    private double explainPrefHeight = 0;
    private double mainPanePrefHeight = 0;
    private double mainPanePrefWidth = 0;
    private Spinner<Double> statementHeightSpinner;
    private Spinner<Double> statementWidthSpinner;
    private Spinner<Double> commentHeightSpinner;
    private Spinner<Double> commentWidthSpinner;
    private Spinner<Double> explainHeightSpinner;
    private Spinner<Double> explainWidthSpinner;
    private Spinner<Double> mainPaneHeightSpinner;
    private Spinner<Double> mainPaneWidthSpinner;
    private Spinner<Double> choicesHeightSpinner;
    private Spinner<Double> choicesWidthSpinner;
    private Node currentSpinnerNode;

    private DecoratedRTA explainDRTA = new DecoratedRTA();
    private VBox controlBox = new VBox(25);
    private Button undoButton;
    private Button redoButton;
    public BooleanProperty undoRedoFlag = new SimpleBooleanProperty();
    Node exerciseControlNode;
    SplitPane mainPane;

    private int pointsPossible;
    private TextField pointsEarnedTextField;

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
    private Stage  staticHelpStage;

    private final DoubleProperty localScalePageHeight = new SimpleDoubleProperty(1.0);
    private final DoubleProperty localScalePageWidth = new SimpleDoubleProperty(1.0);

    // KILL SWITCH: Prevents asynchronous listener execution during teardown
    private boolean isTornDown = false;

    // SCORCHED EARTH RULE 3: Eradicate Anonymous Lambdas
    private ChangeListener<Node> exerciseFocusListener;
    private UnaryOperator<TextFormatter.Change> pointsEarnedFilter;
    private ChangeListener<Number> mainPaneHeightListener;
    private ChangeListener<Number> mainPaneWidthListener;
    private ChangeListener<Number> localScalePageHeightListener;
    private ChangeListener<Number> localScalePageWidthListener;
    private EventHandler<MouseEvent> mainPaneClickHandler;
    private EventHandler<MouseEvent> choiceBoxClickHandler;

    private ChangeListener<Boolean> pointsEarnedFocusListener;
    private EventHandler<MouseEvent> pointsEarnedClickHandler;


    VerticalTreeABExpView(MainWindowView mainView, VerticalTreeABExpExercise vtABExpExercise) {
        this.mainView = mainView;

        localScalePageHeight.bind(mainView.scalePageHeightProperty());
        localScalePageWidth.bind(mainView.scalePageWidthProperty());

        exerciseFocusListener = (ob, ov, nv) -> {
            if (isTornDown || this.mainView == null) return;

            try {
                RichTextArea statementRTA = exerciseStatement != null ? exerciseStatement.getEditor() : null;
                RichTextArea commentRTA = exerciseComment != null ? exerciseComment.getEditor() : null;
                RichTextArea explainRTA = explainDRTA != null ? explainDRTA.getEditor() : null;

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
                } else if (explainRTA != null && inHierarchy(nv, explainRTA)) {
                    if (currentSpinnerNode != explainRTA) {
                        currentSpinnerNode = explainRTA;
                        if (explainHeightSpinner != null && explainWidthSpinner != null) {
                            this.mainView.updateSizeSpinners(explainHeightSpinner, explainWidthSpinner);
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


        this.vtABExpExercise = vtABExpExercise;
        Font labelFont = new Font("Noto Serif Combo", 11);
        choiceLeadLabel.setFont(labelFont); aCheckBox.setFont(labelFont); bCheckBox.setFont(labelFont);
        choiceBox = new HBox(20, choiceLeadLabel, aCheckBox, bCheckBox);
        choiceBox.setPadding(new Insets(7,7,7,10));
        choiceBox.setStyle("-fx-border-color: gainsboro; -fx-background-color: white;");

        root = new BorderPane();
        rootLayout = new ABExpRootLayout(this);

        VBox contentBox = new VBox(3, rootLayout, choiceBox, explainDRTA.getEditor());
        root.setCenter(contentBox);

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

        //explain
        RichTextArea explainRTA = explainDRTA.getEditor();
        explainRTA.getStylesheets().add("slappTextArea.css");
        explainRTA.setPromptText(explainPrompt);

        double explainInitialHeight = Math.round((explainPrefHeight - RTA_SCREEN_BMP) / localScalePageHeight.get() * 100.0 );
        explainHeightSpinner = new Spinner<>(spinnerMin, 999.0, explainInitialHeight, 1.0);
        explainHeightSpinner.setPrefWidth(65);
        explainHeightSpinner.setDisable(false);
        explainHeightSpinner.setTooltip(new Tooltip("Window height as % of selected paper"));
        explainRTA.prefHeightProperty().bind(Bindings.add(Bindings.max(45.0, Bindings.multiply(localScalePageHeight, DoubleProperty.doubleProperty(explainHeightSpinner.getValueFactory().valueProperty()).divide(100.0))), RTA_SCREEN_BMP));

        explainRTA.maxWidthProperty().bind(Bindings.add(localScalePageWidth, RTA_WIDTH_BMP));
        explainRTA.minWidthProperty().bind(Bindings.add(localScalePageWidth, RTA_WIDTH_BMP));
        explainRTA.contentAreaWidthProperty().bind(localScalePageWidth);

        explainWidthSpinner = new Spinner<>(0.0, 999.0, 100, 1.0);
        explainWidthSpinner.setPrefWidth(65);
        explainWidthSpinner.setDisable(true);
        explainWidthSpinner.setTooltip(new Tooltip("Window width as % of selected paper"));

        //choice (null spinners)
        choicesHeightSpinner = new Spinner<>(0.0, 999.0, 0, 1.0);
        choicesHeightSpinner.setPrefWidth(65);
        choicesHeightSpinner.setDisable(true);
        choicesHeightSpinner.setTooltip(new Tooltip("Window height as % of selected paper"));

        choiceBox.maxWidthProperty().bind(Bindings.add(localScalePageWidth, RTA_WIDTH_BMP));
        choicesWidthSpinner = new Spinner<>(100.0, 999.0, 100.0, 1.0);
        choicesWidthSpinner.setPrefWidth(65);
        choicesWidthSpinner.setDisable(true);
        choicesWidthSpinner.setTooltip(new Tooltip("Window width as % of selected paper"));

        //main pane
        AnchorPane mainPane1 = rootLayout.getMain_pane();
        mainPane = rootLayout.getBase_pane();
        mainPane.setStyle("-fx-focus-color: gainsboro;");
        mainPane1.setMinHeight(250.0);
        mainPane1.setMinWidth(localScalePageWidth.get() + RTA_WIDTH_BMP);

        mainPaneHeightSpinner = new Spinner<>(5, 999.0, 0, 1.0);
        mainPaneHeightSpinner.setPrefWidth(65);
        mainPaneHeightSpinner.setDisable(true);
        mainPaneHeightSpinner.setTooltip(new Tooltip("Window height as % of selected paper"));

        mainPaneHeightListener = (ob, ov, nv) -> {
            if (isTornDown || this.mainView == null || mainPaneHeightSpinner == null || mainPaneHeightSpinner.getValueFactory() == null) return;
            mainPaneHeightSpinner.getValueFactory().setValue((double) Math.round(mainPane1.getHeight() / localScalePageHeight.get() * 100));
        };
        mainPane1.heightProperty().addListener(mainPaneHeightListener);

        mainPaneWidthSpinner = new Spinner<>(100.0, 999.0, 0, 1.0);
        mainPaneWidthSpinner.setPrefWidth(65);
        mainPaneWidthSpinner.setDisable(true);
        mainPaneWidthSpinner.setTooltip(new Tooltip("Window width as % of selected paper"));

        mainPaneWidthListener = (ob, ov, nv) -> {
            if (isTornDown || this.mainView == null || mainPaneWidthSpinner == null || mainPaneWidthSpinner.getValueFactory() == null) return;
            mainPaneWidthSpinner.getValueFactory().setValue((double) Math.round((mainPane1.getWidth() - RTA_WIDTH_BMP) / localScalePageWidth.get() * 100));
        };
        mainPane1.widthProperty().addListener(mainPaneWidthListener);


        //page size listeners
        localScalePageHeightListener = (ob, ov, nv) -> {
            if (isTornDown || this.mainView == null || statementRTA == null || commentRTA == null || explainRTA == null || choiceBox == null) return;
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

            explainRTA.prefHeightProperty().unbind();
            if (explainHeightSpinner != null && explainHeightSpinner.getValueFactory() != null) {
                explainHeightSpinner.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(revSpinnerMin, 999.0, (double) Math.round(explainHeightSpinner.getValue() * ov.doubleValue() / nv.doubleValue()), 1.0));
                explainRTA.prefHeightProperty().bind(Bindings.add(Bindings.max(45.0, Bindings.multiply(nv.doubleValue(), DoubleProperty.doubleProperty(explainHeightSpinner.getValueFactory().valueProperty()).divide(100.0))), RTA_SCREEN_BMP));
            }

            if (mainPaneHeightSpinner != null && mainPaneHeightSpinner.getValueFactory() != null) {
                mainPaneHeightSpinner.getValueFactory().setValue((double) Math.round(mainPane1.getHeight() / localScalePageHeight.get() * 100.0));
            }

            if (choicesHeightSpinner != null && choicesHeightSpinner.getValueFactory() != null) {
                double choicesHeightValue = Math.round(choiceBox.getHeight() / localScalePageHeight.get() * 100);
                choicesHeightSpinner.getValueFactory().setValue(choicesHeightValue);
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

    private void setSizeSpinners() {
        if (mainPane != null) {
            mainPaneClickHandler = e -> {
                if (isTornDown || this.mainView == null) return;
                if (currentSpinnerNode != mainPane) {
                    currentSpinnerNode = mainPane;
                    if (mainPaneHeightSpinner != null && mainPaneWidthSpinner != null) {
                        mainView.updateSizeSpinners(mainPaneHeightSpinner, mainPaneWidthSpinner);
                    }
                    mainPane.requestFocus();
                }
            };
            mainPane.addEventFilter(MouseEvent.MOUSE_CLICKED, mainPaneClickHandler);
        }

        if (choiceBox != null) {
            choiceBoxClickHandler = e -> {
                if (isTornDown || this.mainView == null) return;
                if (currentSpinnerNode != choiceBox) {
                    currentSpinnerNode = choiceBox;
                    if (choicesHeightSpinner != null && choicesHeightSpinner.getValueFactory() != null && choicesWidthSpinner != null) {
                        double choicesHeightValue = Math.round(choiceBox.getHeight() / localScalePageHeight.get() * 100);
                        choicesHeightSpinner.getValueFactory().setValue(choicesHeightValue);
                        mainView.updateSizeSpinners(choicesHeightSpinner, choicesWidthSpinner);
                    }
                    choiceBox.requestFocus();
                }
            };
            choiceBox.addEventFilter(MouseEvent.MOUSE_CLICKED, choiceBoxClickHandler);
        }

        if (mainView != null && mainView.getMainScene() != null && exerciseFocusListener != null) {
            mainView.getMainScene().focusOwnerProperty().addListener(exerciseFocusListener);
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

    // SCORCHED EARTH RULE 5: Rule of Isolation
    public void cleanup() {
        // SCORCHED EARTH RULE 1: Kill Switch
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

        // ---> 1. Remove Global Listeners FIRST <---
        try {
            if (mainView != null && mainView.getMainScene() != null && exerciseFocusListener != null) {
                mainView.getMainScene().focusOwnerProperty().removeListener(exerciseFocusListener);
            }
        } catch (Exception e) {}

        try { if (mainPane != null && mainPaneClickHandler != null) mainPane.removeEventFilter(MouseEvent.MOUSE_CLICKED, mainPaneClickHandler); } catch (Exception e) {}
        try { if (choiceBox != null && choiceBoxClickHandler != null) choiceBox.removeEventFilter(MouseEvent.MOUSE_CLICKED, choiceBoxClickHandler); } catch (Exception e) {}

        try {
            if (rootLayout != null && rootLayout.getMain_pane() != null) {
                if (mainPaneHeightListener != null) rootLayout.getMain_pane().heightProperty().removeListener(mainPaneHeightListener);
                if (mainPaneWidthListener != null) rootLayout.getMain_pane().widthProperty().removeListener(mainPaneWidthListener);
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
                if (localScalePageWidthListener != null) localScalePageWidth.removeListener(localScalePageWidthListener);
                localScalePageWidth.unbind();
            }
        } catch (Exception e) {}

        // ---> 2. Unbind Properties Synchronously <---
        try {
            if (exerciseStatement != null && exerciseStatement.getEditor() != null) {
                exerciseStatement.getEditor().prefHeightProperty().unbind();
                exerciseStatement.getEditor().maxWidthProperty().unbind();
                exerciseStatement.getEditor().contentAreaWidthProperty().unbind();
            }
        } catch (Exception e) {}

        try {
            if (exerciseComment != null && exerciseComment.getEditor() != null) {
                exerciseComment.getEditor().prefHeightProperty().unbind();
                exerciseComment.getEditor().prefWidthProperty().unbind();
                exerciseComment.getEditor().maxWidthProperty().unbind();
                exerciseComment.getEditor().minWidthProperty().unbind();
                exerciseComment.getEditor().contentAreaWidthProperty().unbind();
            }
        } catch (Exception e) {}

        try {
            if (explainDRTA != null && explainDRTA.getEditor() != null) {
                explainDRTA.getEditor().prefHeightProperty().unbind();
                explainDRTA.getEditor().prefWidthProperty().unbind();
                explainDRTA.getEditor().maxWidthProperty().unbind();
                explainDRTA.getEditor().minWidthProperty().unbind();
                explainDRTA.getEditor().contentAreaWidthProperty().unbind();
            }
        } catch (Exception e) {}

        try {
            if (choiceBox != null) {
                choiceBox.maxWidthProperty().unbind();
                severFromParent(choiceBox);
                choiceBox.getChildren().clear();
            }
        } catch (Exception e) {}

        try { if (undoRedoFlag != null) undoRedoFlag.unbind(); } catch (Exception e) {}

        // SCORCHED EARTH RULE 8: The Undo/Redo Rebuild Fix (Prevent CME)
        try {
            if (rootLayout != null) {
                if (rootLayout.getMain_pane() != null) {
                    if (rootLayout.getMain_pane().getChildren() != null) {
                        List<Node> childrenCopy = new ArrayList<>(rootLayout.getMain_pane().getChildren());
                        for (Node node : childrenCopy) {
                            try { node.getClass().getMethod("cleanup").invoke(node); } catch (Exception e) {}
                            severFromParent(node);
                        }
                    }
                    rootLayout.getMain_pane().getChildren().clear();
                }
                if (rootLayout.getBase_pane() != null) {
                    rootLayout.getBase_pane().prefWidthProperty().unbind();
                }
            }
        } catch (Exception e) {}

        // ---> 3. THE JAVAFX SKIN HAMMER (DECOUPLED TIMER) <---
        try {
            final RichTextArea stEditor = (exerciseStatement != null) ? exerciseStatement.getEditor() : null;
            final RichTextArea commEditor = (exerciseComment != null) ? exerciseComment.getEditor() : null;
            final RichTextArea expEditor = (explainDRTA != null) ? explainDRTA.getEditor() : null;

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

            if (expEditor != null) {
                try { expEditor.setPromptText(""); } catch (Exception e) {}
                try { expEditor.setFocusTraversable(false); } catch (Exception e) {}
                try { if (expEditor.getDocument() == null || expEditor.getDocument().getText().isEmpty()) expEditor.getActionFactory().open(new Document(" ")).execute(new ActionEvent()); } catch (Exception e) {}
            }

            // STAGE 2 (Delayed Skin Removal)
            javafx.animation.PauseTransition pt = new javafx.animation.PauseTransition(javafx.util.Duration.millis(1500));
            pt.setOnFinished(ev -> {
                try { if (stEditor != null) { stEditor.setSkin(null); } } catch (Exception ex) {}
                try { if (commEditor != null) { commEditor.setSkin(null); } } catch (Exception ex) {}
                try { if (expEditor != null) { expEditor.setSkin(null); } } catch (Exception ex) {}
            });
            pt.play();
        } catch (Exception e) {}

        // ---> 4. Dismantle UI Tree & Popups <---
        try {
            if (controlBox != null) {
                severFromParent(controlBox);
                controlBox.getChildren().clear();
            }
        } catch (Exception e) {}

        try { if (staticHelpStage != null) staticHelpStage.close(); } catch (Exception e) {}

        try { if (exerciseControlNode != null) severFromParent(exerciseControlNode); } catch (Exception e) {}
        try { if (rightControlNode != null) severFromParent(rightControlNode); } catch (Exception e) {}
        try { if (root != null) severFromParent(root); } catch (Exception e) {}

        // Nullify Action Handlers
        try { if (aCheckBox != null) aCheckBox.setOnAction(null); } catch (Exception e) {}
        try { if (bCheckBox != null) bCheckBox.setOnAction(null); } catch (Exception e) {}
        try { if (undoButton != null) undoButton.setOnAction(null); } catch (Exception e) {}
        try { if (redoButton != null) redoButton.setOnAction(null); } catch (Exception e) {}
        try { if (checkButton != null) checkButton.setOnAction(null); } catch (Exception e) {}
        try { if (checkProgButton != null) checkProgButton.setOnAction(null); } catch (Exception e) {}
        try { if (staticHelpButton != null) staticHelpButton.setOnAction(null); } catch (Exception e) {}
        try { if (choiceBox != null) choiceBox.setOnMouseClicked(null); } catch (Exception e) {}
        try { if (mainPane != null) mainPane.setOnMouseClicked(null); } catch (Exception e) {}

        try {
            if (pointsEarnedTextField != null) {
                pointsEarnedTextField.setTextFormatter(null);
            }
        } catch (Exception e) {}

        // ---> 5. Sever All Pointers <---
        try { this.mainView = null; } catch (Exception e) {}
        try { this.vtABExpExercise = null; } catch (Exception e) {}
        try { this.defaultKeyboard = null; } catch (Exception e) {}
        try { this.defaultMapKeyboard = null; } catch (Exception e) {}
        try { this.explainPrompt = null; } catch (Exception e) {}
        try { this.choiceLeadLabel = null; } catch (Exception e) {}
        try { this.aCheckBox = null; } catch (Exception e) {}
        try { this.bCheckBox = null; } catch (Exception e) {}
        try { this.choiceBox = null; } catch (Exception e) {}
        try { this.root = null; } catch (Exception e) {}
        try { this.rootLayout = null; } catch (Exception e) {}
        try { this.exerciseStatement = null; } catch (Exception e) {}
        try { this.exerciseComment = null; } catch (Exception e) {}
        try { this.explainDRTA = null; } catch (Exception e) {}
        try { this.controlBox = null; } catch (Exception e) {}
        try { this.undoButton = null; } catch (Exception e) {}
        try { this.redoButton = null; } catch (Exception e) {}
        try { this.exerciseControlNode = null; } catch (Exception e) {}
        try { this.mainPane = null; } catch (Exception e) {}
        try { this.pointsEarnedTextField = null; } catch (Exception e) {}
        try { this.rightControlNode = null; } catch (Exception e) {}
        try { this.checkButton = null; } catch (Exception e) {}
        try { this.checkProgButton = null; } catch (Exception e) {}
        try { this.bigCheck = null; } catch (Exception e) {}
        try { this.checkMessage = null; } catch (Exception e) {}
        try { this.checkedElements = null; } catch (Exception e) {}
        try { this.checkTriesLabel = null; } catch (Exception e) {}
        try { this.checkColor = null; } catch (Exception e) {}
        try { this.checkElementsColor = null; } catch (Exception e) {}
        try { this.staticHelpButton = null; } catch (Exception e) {}
        try { this.staticHelpStage = null; } catch (Exception e) {}
        try { this.exerciseFocusListener = null; } catch (Exception e) {}
        try { this.statementHeightSpinner = null; } catch (Exception e) {}
        try { this.statementWidthSpinner = null; } catch (Exception e) {}
        try { this.commentHeightSpinner = null; } catch (Exception e) {}
        try { this.commentWidthSpinner = null; } catch (Exception e) {}
        try { this.explainHeightSpinner = null; } catch (Exception e) {}
        try { this.explainWidthSpinner = null; } catch (Exception e) {}
        try { this.mainPaneHeightSpinner = null; } catch (Exception e) {}
        try { this.mainPaneWidthSpinner = null; } catch (Exception e) {}
        try { this.choicesHeightSpinner = null; } catch (Exception e) {}
        try { this.choicesWidthSpinner = null; } catch (Exception e) {}
        try { this.currentSpinnerNode = null; } catch (Exception e) {}
        try { this.pointsEarnedFilter = null; } catch (Exception e) {}
        try { this.mainPaneHeightListener = null; } catch (Exception e) {}
        try { this.mainPaneWidthListener = null; } catch (Exception e) {}
        try { this.localScalePageHeightListener = null; } catch (Exception e) {}
        try { this.localScalePageWidthListener = null; } catch (Exception e) {}
        try { this.mainPaneClickHandler = null; } catch (Exception e) {}
        try { this.choiceBoxClickHandler = null; } catch (Exception e) {}
        try { this.undoRedoFlag = null; } catch (Exception e) {}

        try {this.pointsEarnedFocusListener = null; } catch (Exception e) {}
        try { this.pointsEarnedClickHandler = null; } catch (Exception e) {}
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

    public MainWindowView getMainView() {
        return mainView;
    }

    public ABExpRootLayout getRootLayout() {return rootLayout;}

    public VBox getControlBox() {  return controlBox;  }

    public Button getUndoButton() { return undoButton;   }

    public Button getRedoButton() { return redoButton;   }

    public boolean isUndoRedoFlag() {    return undoRedoFlag.get();    }

    public BooleanProperty undoRedoFlagProperty() {    return undoRedoFlag;    }

    public void setUndoRedoFlag(boolean undoRedoFlag) {    this.undoRedoFlag.set(undoRedoFlag);    }

    public DecoratedRTA getExplainDRTA() { return explainDRTA; }

    public Label getChoiceLeadLabel() { return choiceLeadLabel;  }

    public CheckBox getaCheckBox() {  return aCheckBox;  }

    public CheckBox getbCheckBox() {  return bCheckBox;  }

    public void setExplainPrompt(String explainPrompt) {    this.explainPrompt = explainPrompt;  }

    public RichTextAreaSkin.KeyMapValue getDefaultKeyboard() {     return defaultKeyboard;   }

    public void setDefaultKeyboard(RichTextAreaSkin.KeyMapValue defaultKeyboard) {    this.defaultKeyboard = defaultKeyboard;  }

    public RichTextAreaSkin.KeyMapValue getDefaultMapKeyboard() {
        return defaultMapKeyboard;
    }

    public void setDefaultMapKeyboard(RichTextAreaSkin.KeyMapValue defaultMapKeyboard) {
        this.defaultMapKeyboard = defaultMapKeyboard;
    }

    public double getCommentPrefHeight() { return exerciseComment.getEditor().getPrefHeight();   }

    public void setCommentPrefHeight(double commentPrefHeight) {   this.commentPrefHeight = commentPrefHeight;  }

    public double getExplainPrefHeight() { return explainDRTA.getEditor().getPrefHeight();    }

    public void setExplainPrefHeight(double explainPrefHeight) {   this.explainPrefHeight = explainPrefHeight;  }

    public double getMainPanePrefHeight() {    return rootLayout.getMain_pane().getPrefHeight();    }

    public void setMainPanePrefHeight(double mainPanePrefHeight) { this.mainPanePrefHeight = mainPanePrefHeight;  }

    public double getMainPanePrefWidth() { return rootLayout.getMain_pane().getPrefWidth();   }

    public void setMainPanePrefWidth(double mainPanePrefWidth) {   this.mainPanePrefWidth = mainPanePrefWidth;  }

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
            if (mainView != null && !mainView.isInstructorFunctions()) pointsEarnedTextField.setDisable(true);
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

    public VerticalTreeABExpExercise getVtABExpExercise() {
        return vtABExpExercise;
    }

    public HBox getChoiceBox() {
        return choiceBox;
    }

    public DoubleProperty localScalePageHeightProperty() { return localScalePageHeight; }

    public DoubleProperty localScalePageWidthProperty() {
        return localScalePageWidth;
    }

    public ChangeListener<Node> getExerciseFocusListener() { return exerciseFocusListener; }

}
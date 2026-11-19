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

package slapp.editor.horizontal_tree;

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
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
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
import java.util.Arrays;
import java.util.List;
import java.util.function.UnaryOperator;

import static slapp.editor.EditorMain.RTA_SCREEN_BMP;
import static slapp.editor.EditorMain.RTA_WIDTH_BMP;
import javafx.animation.PauseTransition;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;


/**
 * View for the horizontal tree exercise
 */


public class HorizontalTreeView implements ExerciseView<DecoratedRTA> {
    private HorizontalTreeView self;
    private MainWindowView mainView;
    private DecoratedRTA exerciseComment = new DecoratedRTA();
    private DecoratedRTA exerciseStatement = new DecoratedRTA();
    private DecoratedRTA explainDRTA = new DecoratedRTA();
    private DecoratedRTA interpretationDRTA = new DecoratedRTA();
    private RichTextArea interpretationRTA;
    private Node exerciseControlNode = new VBox();
    private String explainPrompt = "";
    private VBox controlBox = new VBox(15);
    private double statementPrefHeight = 80;
    private double commentPrefHeight = 0;
    private double explainPrefHeight = 0;
    private double interpretationPrefHeight = 0;
    private Spinner<Double> statementHeightSpinner;
    private Spinner<Double> statementWidthSpinner;
    private Spinner<Double> commentHeightSpinner;
    private Spinner<Double> commentWidthSpinner;
    private Spinner<Double> explainHeightSpinner;
    private Spinner<Double> explainWidthSpinner;
    private Spinner<Double> interpretationHeightSpinner;
    private Spinner<Double> interpretationWidthSpinner;
    private Spinner<Double> mainPaneHeightSpinner;
    private Spinner<Double> mainPaneWidthSpinner;
    private Node currentSpinnerNode;
    private AnchorPane mainPane;
    private VBox centerBox;
    private Button undoButton;
    private Button redoButton;
    public BooleanProperty undoRedoFlag = new SimpleBooleanProperty();
    private ToggleButton formulaNodeToggle;
    private ToggleButton oneBranchToggle;
    private ToggleButton twoBranchToggle;
    private ToggleButton threeBranchToggle;
    private ToggleButton indefiniteBranchToggle;
    private ToggleButton verticalDotsToggle;
    private ToggleButton oneBranchTermToggle;
    private ToggleButton twoBranchTermToggle;
    private ToggleButton annotationToggle;
    private Button annotationPlus;
    private Button annotationMinus;
    private ToggleButton rulerButton;
    private ToggleGroup buttonGroup = new ToggleGroup();
    private List<TreePane> treePanes = new ArrayList<>();

    private Ruler axisNode;
    private boolean axis = false;
    private static BranchNode clickNode = null;

    private int pointsPossible;
    private TextField pointsEarnedTextField;

    //----------------
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
    private CheckBox permitEmptyBranchesCheck;

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
    private ChangeListener<Boolean> formulaNodeToggleListener;
    private ChangeListener<Boolean> oneBranchToggleListener;
    private ChangeListener<Boolean> twoBranchToggleListener;
    private ChangeListener<Boolean> threeBranchToggleListener;
    private ChangeListener<Boolean> indefinateBranchToggleListener;
    private ChangeListener<Boolean> oneBranchTermToggleListener;
    private ChangeListener<Boolean> twoBranchTermToggleListener;
    private ChangeListener<Boolean> verticalDotsToggleListener;
    private ChangeListener<Boolean> annotationToggleListener;
    private ChangeListener<Boolean> rulerButtonToggleListener;
    private EventHandler<ActionEvent> annotationPlusHandler;
    private EventHandler<ActionEvent> annotationMinusHandler;
    private EventHandler<MouseEvent> mainPaneClickHandler;

    EventHandler<MouseEvent> formulaNodeClickFilter;
    private EventHandler<MouseEvent> oneBranchClickFilter;
    private EventHandler<MouseEvent> twoBranchClickFilter;
    private EventHandler<MouseEvent> threeBranchClickFilter;
    private EventHandler<MouseEvent> indefinateBranchClickFilter;
    private EventHandler<MouseEvent> annotationClickFilter;
    private EventHandler<MouseEvent> dotsClickFilter;
    private EventHandler<MouseEvent> oneBranchTermClickFilter;
    private EventHandler<MouseEvent> twoBranchTermClickFilter;

    /**
     * Construct the horizontal tree view
     * @param mainView the main view
     */
    HorizontalTreeView(MainWindowView mainView) {
        self = this;
        this.mainView = mainView;

        localScalePageHeight.bind(mainView.scalePageHeightProperty());
        localScalePageWidth.bind(mainView.scalePageWidthProperty());

        exerciseFocusListener = (ob, ov, nv) -> {
            if (isTornDown || this.mainView == null ) return;
            try {
                RichTextArea statementRTA = exerciseStatement != null ? exerciseStatement.getEditor() : null;
                RichTextArea commentRTA = exerciseComment != null ? exerciseComment.getEditor() : null;
                RichTextArea explainRTA = explainDRTA != null ? explainDRTA.getEditor() : null;
                RichTextArea interpretationEditor = interpretationDRTA != null ? interpretationDRTA.getEditor() : null;

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
                } else if (interpretationEditor != null && inHierarchy(nv, interpretationEditor)) {
                    if (currentSpinnerNode != interpretationEditor) {
                        currentSpinnerNode = interpretationEditor;
                        if (interpretationHeightSpinner != null && interpretationWidthSpinner != null) {
                            this.mainView.updateSizeSpinners(interpretationHeightSpinner, interpretationWidthSpinner);
                        }
                    }
                } else if (mainPane != null && inHierarchy(nv, mainPane)) {
                    if (currentSpinnerNode != mainPane) {
                        currentSpinnerNode = mainPane;
                        if (mainPaneHeightSpinner != null && mainPaneWidthSpinner != null) {
                            if (mainPaneHeightSpinner.getValueFactory() != null) {
                                mainPaneHeightSpinner.getValueFactory().setValue((double) Math.round(mainPane.getHeight() / localScalePageHeight.get() * 100.0));
                            }
                            if (mainPaneWidthSpinner.getValueFactory() != null) {
                                mainPaneWidthSpinner.getValueFactory().setValue((double) Math.round((mainPane.getWidth() - RTA_WIDTH_BMP) / localScalePageWidth.get() * 100.0));
                            }
                            this.mainView.updateSizeSpinners(mainPaneHeightSpinner, mainPaneWidthSpinner);
                        }
                    }
                } else {
                    currentSpinnerNode = null;
                }
            } catch (Exception e) {}
        };

        mainPane = new AnchorPane();
        mainPane.setMinHeight(100);
        mainPane.setStyle("-fx-border-width: 2 2 2 2; -fx-border-color: gainsboro; -fx-background-color: white");
        mainPane.setBackground(new Background(new BackgroundFill(Color.WHITE, CornerRadii.EMPTY, Insets.EMPTY)));

        centerBox = new VBox(3, mainPane, explainDRTA.getEditor());


        mainPane.setPadding(new Insets(5));

        undoButton = new Button("Undo");
        redoButton = new Button("Redo");
        undoButton.setPrefWidth(64);

        redoButton.setPrefWidth(64);
        undoButton.setPrefHeight(28);
        redoButton.setPrefHeight(28);

        formulaNodeToggle = new ToggleButton();
        formulaNodeToggle.setPrefWidth(64);
        formulaNodeToggle.setPrefHeight(28);
        formulaNodeToggle.setMinHeight(28);
        formulaNodeToggle.setMaxHeight(28);
        Rectangle rectangle = new Rectangle(30,12);
        rectangle.setStyle("-fx-stroke: black; -fx-stroke-width: 1.5; -fx-fill: transparent;");
        Group boxToggleGraphic = new Group(rectangle);
        formulaNodeToggle.setGraphic(boxToggleGraphic);
        formulaNodeToggle.setTooltip(new Tooltip("Add (left click) or remove (double click) box with its branches"));
        formulaNodeToggle.setToggleGroup(buttonGroup);

        oneBranchToggle = new ToggleButton();
        oneBranchToggle.setPrefWidth(64);
        oneBranchToggle.setMinHeight(28);
        oneBranchToggle.setMaxHeight(28);
        Line oneLine = new Line(0, 0, 27, 0);
        oneLine.setStyle("-fx-stroke-width: 1.5");
        oneBranchToggle.setGraphic(new Group(oneLine));
        oneBranchToggle.setTooltip(new Tooltip("Add branch to selected node"));
        oneBranchToggle.setToggleGroup(buttonGroup);

        twoBranchToggle = new ToggleButton();
        twoBranchToggle.setPrefWidth(64);
        twoBranchToggle.setMinHeight(28);
        twoBranchToggle.setMaxHeight(28);
        Line twoStub = new Line(0,0,3,0);
        Line twoBrack = new Line(0,0,0, 9);
        Line twoLine1 = new Line(0, 0, 24, 0);
        Line twoLine2 = new Line(0, 0, 24, 0 );
        VBox twoVBox = new VBox(7, twoLine1, twoLine2);
        twoVBox.setAlignment(Pos.CENTER);
        twoStub.setStyle("-fx-stroke-width: 1.5");
        twoBrack.setStyle("-fx-stroke-width: 1.5");
        twoLine1.setStyle("-fx-stroke-width: 1.5");
        twoLine2.setStyle("-fx-stroke-width: 1.5");
        HBox twoHBox = new HBox(twoStub, twoBrack, twoVBox);
        twoHBox.setAlignment(Pos.CENTER);
        twoBranchToggle.setGraphic(twoHBox);
        twoBranchToggle.setTooltip(new Tooltip("Add branches to selected node"));
        twoBranchToggle.setToggleGroup(buttonGroup);

        threeBranchToggle = new ToggleButton();
        threeBranchToggle.setPrefWidth(64);
        threeBranchToggle.setMinHeight(28);
        threeBranchToggle.setMaxHeight(28);
        Line threeStub = new Line(0,0,3,0);
        Line threeBrack = new Line(0,0,0, 11);
        Line threeLine1 = new Line(0, 0, 24, 0);
        Line threeLine2 = new Line(0, 0, 24, 0 );
        Line threeLine3 = new Line(0,0,24,0);
        VBox threeVBox = new VBox(4, threeLine1, threeLine2, threeLine3);
        threeVBox.setAlignment(Pos.CENTER);
        threeStub.setStyle("-fx-stroke-width: 1.5");
        threeBrack.setStyle("-fx-stroke-width: 1.5");
        threeLine1.setStyle("-fx-stroke-width: 1.5");
        threeLine2.setStyle("-fx-stroke-width: 1.5");
        threeLine3.setStyle("-fx-stroke-width: 1.5");
        HBox threeHBox = new HBox(threeStub, threeBrack, threeVBox);
        threeHBox.setAlignment(Pos.CENTER);
        threeBranchToggle.setGraphic(threeHBox);
        threeBranchToggle.setTooltip(new Tooltip("Add branches to selected node"));
        threeBranchToggle.setToggleGroup(buttonGroup);

        indefiniteBranchToggle = new ToggleButton();
        indefiniteBranchToggle.setPrefWidth(64);
        indefiniteBranchToggle.setMinHeight(28);
        indefiniteBranchToggle.setMaxHeight(28);

        Line indefStub = new Line(0,6,3,6);
        Line indefBrack = new Line(3, 0,3, 12);
        Line indefLine = new Line(3,0,24,0);
        indefStub.setStyle("-fx-stroke-width: 1.5");
        indefBrack.setStyle("-fx-stroke-width: 1.5");
        indefLine.setStyle("-fx-stroke-width: 1.5");
        Line indefDots = new Line(10,4,10,14);
        indefDots.getStrokeDashArray().addAll(1.0,3.0);
        Group indefPane = new Group(indefStub, indefBrack, indefLine, indefDots);
        HBox indefHBox = new HBox(indefPane);
        indefHBox.setAlignment(Pos.CENTER);
        indefiniteBranchToggle.setAlignment(Pos.CENTER);
        indefiniteBranchToggle.setGraphic(indefPane);
        indefiniteBranchToggle.setTooltip(new Tooltip("Add indefinite branch to selected node"));
        indefiniteBranchToggle.setToggleGroup(buttonGroup);

        oneBranchTermToggle = new ToggleButton();
        oneBranchTermToggle.setPrefWidth(64);
        oneBranchTermToggle.setMinHeight(28);
        oneBranchTermToggle.setMaxHeight(28);
        Line oneTermLine = new Line(0,0,20,0);
        oneTermLine.setStyle("-fx-stroke-width: 1.5");
        Rectangle oneTermRec = new Rectangle(10,10);
        oneTermRec.setStyle("-fx-stroke: black; -fx-stroke-width: 1.5; -fx-fill: transparent;");
        HBox oneBranchHBox = new HBox(oneTermLine, oneTermRec);
        oneBranchHBox.setAlignment(Pos.CENTER);
        oneBranchTermToggle.setGraphic(oneBranchHBox);
        oneBranchTermToggle.setTooltip(new Tooltip("Branch to term from selected node"));
        oneBranchTermToggle.setToggleGroup(buttonGroup);

        twoBranchTermToggle = new ToggleButton();
        twoBranchTermToggle.setPrefWidth(64);
        twoBranchTermToggle.setMinHeight(28);
        twoBranchTermToggle.setMaxHeight(28);
        Line twoTermLine1 = new Line(0, 7, 20, 2);
        Line twoTermLine2 = new Line(0, 7, 20, 12);
        VBox twoTermVBox1 = new VBox(twoTermLine1, twoTermLine2);
        twoTermVBox1.setAlignment(Pos.CENTER);
        Rectangle twoTermRec1 = new Rectangle(7,7);
        twoTermRec1.setStyle("-fx-stroke: black; -fx-stroke-width: 1.5; -fx-fill: transparent;");
        Rectangle twoTermRec2 = new Rectangle(7, 7);
        twoTermRec2.setStyle("-fx-stroke: black; -fx-stroke-width: 1.5; -fx-fill: transparent;");
        VBox twoTermVBox2 = new VBox(2, twoTermRec1, twoTermRec2);
        twoTermVBox2.setAlignment(Pos.CENTER);
        HBox twoTermHBox = new HBox(twoTermVBox1, twoTermVBox2);
        twoTermHBox.setAlignment(Pos.CENTER);
        twoBranchTermToggle.setGraphic(twoTermHBox);
        twoBranchTermToggle.setTooltip(new Tooltip("Branch to terms from selected node"));
        twoBranchTermToggle.setToggleGroup(buttonGroup);

        verticalDotsToggle = new ToggleButton();
        verticalDotsToggle.setPrefWidth(64);
        verticalDotsToggle.setMinHeight(28);
        verticalDotsToggle.setMaxHeight(28);
        Line vDotsLine = new Line(0,0,0,16);
        vDotsLine.getStrokeDashArray().addAll(1.0, 3.0);
        vDotsLine.setStyle("-fx-stroke-width: 1.5");
        VBox verticalDotsVBox = new VBox(vDotsLine);
        verticalDotsVBox.setAlignment(Pos.CENTER);
        verticalDotsVBox.setPadding(new Insets(1,0,0,0));
        HBox verticalDotsHBox = new HBox(verticalDotsVBox);
        verticalDotsHBox.setAlignment(Pos.CENTER);
        verticalDotsToggle.setGraphic(verticalDotsHBox);
        verticalDotsToggle.setTooltip(new Tooltip("Dots to divide term from formula branches"));
        verticalDotsToggle.setToggleGroup(buttonGroup);

        annotationToggle = new ToggleButton();
        annotationToggle.setPrefWidth(44);
        annotationToggle.setPrefHeight(30);
        annotationToggle.setMinHeight(30);
        annotationToggle.setMaxHeight(30);
        AnchorPane boxesPane = new AnchorPane();
        boxesPane.setPadding(new Insets(0));
        Rectangle bigBox = new Rectangle(15,10);
        bigBox.setStyle("-fx-stroke: black; -fx-stroke-width: 1.5; -fx-fill: transparent;");
        Rectangle littleBox = new Rectangle(7,7);
        littleBox.setStyle("-fx-stroke: black; -fx-stroke-width: 1.5; -fx-fill: transparent;");
        boxesPane.getChildren().addAll(bigBox, littleBox);
        boxesPane.setTopAnchor(bigBox, 8.0);
        boxesPane.setLeftAnchor(littleBox, 15.0);
        boxesPane.setTopAnchor(littleBox, 3.0);
        HBox buttonBox = new HBox(boxesPane);
        buttonBox.setAlignment(Pos.CENTER);
        annotationToggle.setGraphic(buttonBox);
        annotationToggle.setTooltip(new Tooltip("Add (left click) or remove (double click) annotation box"));
        annotationToggle.setToggleGroup(buttonGroup);

        annotationPlus = new Button("+");
        annotationPlus.setFont(new Font(10));
        annotationPlus.setPadding(new Insets(0));
        annotationPlus.setPrefWidth(20); annotationPlus.setPrefHeight(12);

        annotationMinus = new Button("-");
        annotationMinus.setFont(new Font(10));
        annotationMinus.setPadding(new Insets(0));
        annotationMinus.setPrefWidth(20); annotationMinus.setPrefHeight(12);
        VBox annotationButtons = new VBox(annotationPlus, annotationMinus);
        HBox annotationBox = new HBox(annotationToggle, annotationButtons);
        annotationBox.setMaxHeight(30);
        annotationBox.setMaxWidth(64);

        axisNode = new Ruler();
        axisNode.setManaged(false);
        rulerButton = new ToggleButton();
        rulerButton.setPrefWidth(64);
        rulerButton.setMinHeight(28);
        rulerButton.setMaxHeight(28);
        Line ruler = new Line(0,3,30, 3);
        Line tick1 = new Line(5,0,5,6);
        Line tick2 = new Line(15, 0, 15, 6);
        Line tick3 = new Line(25, 0, 25, 6);
        ruler.setStyle("-fx-stroke-width: 0.75");
        tick1.setStyle("-fx-stroke-width: 0.75");
        tick2.setStyle("-fx-stroke-width: 0.75");
        tick3.setStyle("-fx-stroke-width: 0.75");
        Pane rulerPane = new Pane(ruler, tick1, tick2, tick3);
        HBox rulerHBox = new HBox(rulerPane);
        rulerHBox.setAlignment(Pos.CENTER);
        VBox rulerVBox = new VBox(rulerHBox);
        rulerVBox.setAlignment(Pos.CENTER);
        rulerButton.setGraphic(rulerVBox);
        rulerButton.setTooltip(new Tooltip("Add (right-click) or remove (left-click) horizontal ruler on tree"));

        Region spacer = new Region();
        spacer.setPrefWidth(5);
        spacer.setMinWidth(5);
        spacer.setMaxWidth(5);
        controlBox.getChildren().addAll(undoButton, redoButton, formulaNodeToggle, oneBranchToggle, twoBranchToggle, threeBranchToggle, indefiniteBranchToggle, verticalDotsToggle, oneBranchTermToggle, twoBranchTermToggle, spacer, annotationBox, rulerButton);
    //    controlBox.setMargin(rulerButton, new Insets(-10, 0, 0, 0));
        controlBox.setMargin(annotationBox, new Insets(-15, 0, 0, 0));

        controlBox.setAlignment(Pos.TOP_CENTER);
        controlBox.setPadding(new Insets(20,20,0,20));
        controlBox.setMinWidth(150); controlBox.setMaxWidth(150);
        exerciseControlNode = controlBox;

        pointsEarnedTextField = new TextField();
        pointsEarnedTextField.setPrefWidth(27);
        pointsEarnedTextField.setAlignment(Pos.CENTER_RIGHT);
        pointsEarnedTextField.setPadding(new Insets(0,5,0,3));

        pointsEarnedFilter = change -> {
            if (isTornDown || this.mainView == null || change == null) return null;
            String text = change.getText();
            if (text.matches("[0-9]*")) {
                return change;
            }
            return null;
        };
        TextFormatter<String> textFormatter = new TextFormatter<>(pointsEarnedFilter);
        pointsEarnedTextField.setTextFormatter(textFormatter);
    }

    /**
     * Deselect all the toggle buttons
     */
    void deselectToggles() {
        if (isTornDown || this.mainView == null) return;
        if (formulaNodeToggle != null) formulaNodeToggle.setSelected(false);
        if (oneBranchToggle != null) oneBranchToggle.setSelected(false);
        if (twoBranchToggle != null) twoBranchToggle.setSelected(false);
        if (threeBranchToggle != null) threeBranchToggle.setSelected(false);
        if (indefiniteBranchToggle != null) indefiniteBranchToggle.setSelected(false);
        if (verticalDotsToggle != null) verticalDotsToggle.setSelected(false);
        if (oneBranchTermToggle != null) oneBranchTermToggle.setSelected(false);
        if (twoBranchTermToggle != null) twoBranchTermToggle.setSelected(false);
        if (annotationToggle != null) annotationToggle.setSelected(false);
    }

    void initalizeInterpretationRTA() {
        if (isTornDown || this.mainView == null || interpretationRTA == null || interpretationDRTA == null) return;
        double spinnerMin = (double) ((int) (100 * 45.0 / localScalePageHeight.get()));

        interpretationRTA.getStylesheets().add("slappTextArea.css");
        interpretationRTA.setPromptText("Interpretation");
        if (interpretationDRTA.getKeyboardSelector() != null) {
            interpretationDRTA.getKeyboardSelector().valueProperty().setValue(RichTextAreaSkin.KeyMapValue.BASE_AND_SANS);
        }

        double interpretationInitialHeight = Math.round((interpretationPrefHeight - RTA_SCREEN_BMP) / localScalePageHeight.get() * 100 );

        interpretationHeightSpinner = new Spinner<>(spinnerMin, 999.0, interpretationInitialHeight, 1.0);
        interpretationHeightSpinner.setPrefWidth(65);
        interpretationHeightSpinner.setDisable(false);
        interpretationHeightSpinner.setTooltip(new Tooltip("Window height as % of selected paper"));
        interpretationRTA.prefHeightProperty().bind(Bindings.add(Bindings.max(45.0, Bindings.multiply(localScalePageHeight, DoubleProperty.doubleProperty(interpretationHeightSpinner.getValueFactory().valueProperty()).divide(100.0))), RTA_SCREEN_BMP));

        interpretationRTA.prefWidthProperty().bind(Bindings.add(localScalePageWidth, RTA_WIDTH_BMP));
        interpretationRTA.contentAreaWidthProperty().bind(localScalePageWidth);
        interpretationWidthSpinner = new Spinner<>(0.0, 999.0, 100, 1.0);
        interpretationWidthSpinner.setPrefWidth(65);
        interpretationWidthSpinner.setDisable(true);
        interpretationWidthSpinner.setTooltip(new Tooltip("Window width as % of selected paper"));

    }

    /**
     * Initialize statement, comment, explain, main pane, and button click filters
     */
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
        statementRTA.minWidthProperty().bind(statementRTA.prefWidthProperty());
        statementRTA.contentAreaWidthProperty().bind(localScalePageWidth);

        statementWidthSpinner = new Spinner<>(0.0, 999.0, 100, 1.0);
        statementWidthSpinner.setPrefWidth(65);
        statementWidthSpinner.setDisable(true);
        statementWidthSpinner.setTooltip(new Tooltip("Window width as % of selected paper"));

        //interpretation rta
        RichTextArea interpRTA = interpretationDRTA.getEditor();

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
        explainRTA.contentAreaWidthProperty().bind(localScalePageWidth);
        explainWidthSpinner = new Spinner<>(0.0, 999.0, 100, 1.0);
        explainWidthSpinner.setPrefWidth(65);
        explainWidthSpinner.setDisable(true);
        explainWidthSpinner.setTooltip(new Tooltip("Window width as % of selected paper"));



        //main pane
        mainPane.setMinHeight(150);
        mainPane.setMinWidth(localScalePageWidth.get() + RTA_WIDTH_BMP);
        mainPane.setPadding(new Insets(0, RTA_WIDTH_BMP, 0, 0));

        mainPaneHeightSpinner = new Spinner<>(0.0,999.0, 0,1.0);
        mainPaneHeightSpinner.setPrefWidth(65);
        mainPaneHeightSpinner.setDisable(true);
        mainPaneHeightSpinner.setTooltip(new Tooltip("Window height as % of selected paper"));

        mainPaneHeightListener = (ob, ov, nv) -> {
            if (isTornDown || this.mainView == null || mainPane == null || mainPaneHeightSpinner == null) return;
            if (mainPaneHeightSpinner.getValueFactory() != null) mainPaneHeightSpinner.getValueFactory().setValue((double) Math.round(mainPane.getHeight() / localScalePageHeight.get() * 100));
        };
        mainPane.heightProperty().addListener(mainPaneHeightListener);

        mainPaneWidthSpinner = new Spinner<>(0.0,999.0, 0,1.0);
        mainPaneWidthSpinner.setPrefWidth(65);
        mainPaneWidthSpinner.setDisable(true);
        mainPaneWidthSpinner.setTooltip(new Tooltip("Window width as % of selected paper"));

        mainPaneWidthListener = (ob, ov, nv) -> {
            if (isTornDown || this.mainView == null || mainPane == null || mainPaneWidthSpinner == null) return;
            if (mainPaneWidthSpinner.getValueFactory() != null) mainPaneWidthSpinner.getValueFactory().setValue((double) Math.round((mainPane.getWidth() - RTA_WIDTH_BMP) / localScalePageWidth.get() * 100));
            if (axisNode != null) axisNode.updateRuler((double) nv);
        };
        mainPane.widthProperty().addListener(mainPaneWidthListener);

        setSizeSpinners();

        //page size listeners
        localScalePageHeightListener = (ob, ov, nv) -> {
            if (isTornDown || this.mainView == null || statementRTA == null || commentRTA == null || explainRTA == null || interpRTA == null) return;
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

            interpRTA.prefHeightProperty().unbind();
            if (interpretationHeightSpinner != null && interpretationHeightSpinner.getValueFactory() != null) {
                interpretationHeightSpinner.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(revSpinnerMin, 999.0, (double) Math.round(interpretationHeightSpinner.getValue() * ov.doubleValue() / nv.doubleValue()), 1.0));
                interpRTA.prefHeightProperty().bind(Bindings.add(Bindings.max(45.0, Bindings.multiply(nv.doubleValue(), DoubleProperty.doubleProperty(interpretationHeightSpinner.getValueFactory().valueProperty()).divide(100.0))), RTA_SCREEN_BMP));
            }

            if (mainPaneHeightSpinner != null && mainPaneHeightSpinner.getValueFactory() != null && mainPane != null) {
                mainPaneHeightSpinner.getValueFactory().setValue((double) Math.round(mainPane.getHeight() / localScalePageHeight.get() * 100.0));
            }
        };
        localScalePageHeight.addListener(localScalePageHeightListener);

        localScalePageWidthListener = (ob, ov, nv) -> {
            if (isTornDown || this.mainView == null || mainPane == null || mainPaneWidthSpinner == null) return;
            if (mainPaneWidthSpinner.getValueFactory() != null) {
                mainPaneWidthSpinner.getValueFactory().setValue((double) Math.round((mainPane.getWidth() - RTA_WIDTH_BMP) / localScalePageWidth.get() * 100.0));
            }
            mainPane.setMinWidth(localScalePageWidth.get() + RTA_WIDTH_BMP);
        };
        localScalePageWidth.addListener(localScalePageWidthListener);

        //click filters
        formulaNodeClickFilter = new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (isTornDown || mainView == null || self == null || treePanes == null) return;
                if (event.getButton() == MouseButton.PRIMARY) {

                    if (event.getClickCount() == 1) {
                        boolean clickedOnExistingNode = false;
                        for (TreePane pane : treePanes) {
                            if (pane == null) continue;
                            BranchNode rootNode = pane.getRootBranchNode();
                            if (inHierarchy(event.getPickResult().getIntersectedNode(), rootNode)) {
                                clickedOnExistingNode = true;
                                break;
                            } else {
                                setClickedNode(event, rootNode);
                                if (clickNode != null && clickNode != rootNode) {
                                    clickedOnExistingNode = true;
                                    break;
                                }
                            }
                        }

                        if (!clickedOnExistingNode) {
                            if (formulaNodeToggle != null) formulaNodeToggle.setSelected(false);

                            TreePane treePane = new TreePane(self);
                            treePanes.add(treePane);
                            mainPane.getChildren().add(treePane);
                            refreshTreePanes();
                            treePane.relocateToGridPoint(new Point2D(event.getX(), event.getY()));
                            setUndoRedoFlag(true);
                            setUndoRedoFlag(false);
                            event.consume();
                        }

                    } else if (event.getClickCount() == 2) {
                        for (TreePane pane : treePanes) {
                            if (pane == null) continue;
                            BranchNode rootNode = pane.getRootBranchNode();

                            if (inHierarchy(event.getPickResult().getIntersectedNode(), rootNode)) {
                                treePanes.remove(pane);
                                // SCORCHED EARTH RULE 9: Explicit UI Deletion Cleanup (call before removing from parent)
                                try { pane.cleanup(); } catch (Exception e) {}
                                mainPane.getChildren().remove(pane);
                                severFromParent(pane);

                                if (formulaNodeToggle != null) formulaNodeToggle.setSelected(false);
                                setUndoRedoFlag(true);
                                setUndoRedoFlag(false);
                                event.consume();
                                break;
                            } else {
                                setClickedNode(event, rootNode);
                                if (clickNode != null && clickNode != rootNode) {
                                    if (clickNode.getContainer() != null && clickNode.getContainer().getDependents() != null) {
                                        clickNode.getContainer().getDependents().remove(clickNode);
                                        pane.refresh();
                                        // SCORCHED EARTH RULE 9: Explicit UI Deletion Cleanup
                                        try { clickNode.cleanup(); } catch(Exception e) {}
                                    }

                                    if (formulaNodeToggle != null) formulaNodeToggle.setSelected(false);
                                    setUndoRedoFlag(true);
                                    setUndoRedoFlag(false);
                                    event.consume();
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        };

        formulaNodeToggleListener = (ob, ov, nv) -> {
            if (isTornDown || this.mainView == null || mainPane == null || formulaNodeClickFilter == null) return;
            if (nv) mainPane.addEventFilter(MouseEvent.MOUSE_CLICKED, formulaNodeClickFilter);
            else mainPane.removeEventFilter(MouseEvent.MOUSE_CLICKED, formulaNodeClickFilter);
        };
        formulaNodeToggle.selectedProperty().addListener(formulaNodeToggleListener);

        oneBranchClickFilter = new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (isTornDown || mainView == null || self == null || treePanes == null) return;
                if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 1) {
                    for (TreePane pane : treePanes) {
                        if (pane == null) continue;
                        BranchNode rootNode = pane.getRootBranchNode();
                        setClickedNode(event, rootNode);
                        if (clickNode != null && clickNode.isFormulaNode() && clickNode.getDependents() != null && formulaDependents(clickNode.getDependents())) {
                            if (clickNode.getConnectorBoxedDRTA() != null && clickNode.getConnectorBoxedDRTA().getRTA() != null) {
                                clickNode.getConnectorBoxedDRTA().getRTA().getActionFactory().newDocumentNow().execute(new ActionEvent());
                            }
                            BranchNode branch1 = new BranchNode(clickNode, self);
                            clickNode.getDependents().add(branch1);
                            pane.refresh();
                            setUndoRedoFlag(true);
                            setUndoRedoFlag(false);
                        }
                    }
                }
            }
        };

        oneBranchToggleListener = (ob, ov, nv) -> {
            if (isTornDown || this.mainView == null || mainPane == null || oneBranchClickFilter == null) return;
            if (nv) mainPane.addEventFilter(MouseEvent.MOUSE_PRESSED, oneBranchClickFilter);
            else mainPane.removeEventFilter(MouseEvent.MOUSE_PRESSED, oneBranchClickFilter);
        };
        oneBranchToggle.selectedProperty().addListener(oneBranchToggleListener);

        twoBranchClickFilter = new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (isTornDown || mainView == null || self == null || treePanes == null) return;
                if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 1) {
                    for (TreePane pane : treePanes) {
                        if (pane == null) continue;
                        BranchNode rootNode = pane.getRootBranchNode();
                        setClickedNode(event, rootNode);
                        if (clickNode != null && clickNode.isFormulaNode() && clickNode.getDependents() != null && formulaDependents(clickNode.getDependents())) {
                            if (clickNode.getConnectorBoxedDRTA() != null && clickNode.getConnectorBoxedDRTA().getRTA() != null) {
                                clickNode.getConnectorBoxedDRTA().getRTA().getActionFactory().newDocumentNow().execute(new ActionEvent());
                            }
                            BranchNode branch1 = new BranchNode(clickNode, self);
                            BranchNode branch2 = new BranchNode(clickNode, self);
                            clickNode.getDependents().addAll(Arrays.asList(branch1, branch2));
                            pane.refresh();
                            setUndoRedoFlag(true);
                            setUndoRedoFlag(false);
                        }
                    }
                }
            }
        };

        twoBranchToggleListener = (ob, ov, nv) -> {
            if (isTornDown || this.mainView == null || mainPane == null || twoBranchClickFilter == null) return;
            if (nv) mainPane.addEventFilter(MouseEvent.MOUSE_PRESSED, twoBranchClickFilter);
            else mainPane.removeEventFilter(MouseEvent.MOUSE_PRESSED, twoBranchClickFilter);
        };
        twoBranchToggle.selectedProperty().addListener(twoBranchToggleListener);

        threeBranchClickFilter = new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (isTornDown || mainView == null || self == null || treePanes == null) return;
                if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 1) {
                    for (TreePane pane : treePanes) {
                        if (pane == null) continue;
                        BranchNode rootNode = pane.getRootBranchNode();
                        setClickedNode(event, rootNode);
                        if (clickNode != null && clickNode.isFormulaNode() && clickNode.getDependents() != null && formulaDependents(clickNode.getDependents())) {
                            if (clickNode.getConnectorBoxedDRTA() != null && clickNode.getConnectorBoxedDRTA().getRTA() != null) {
                                clickNode.getConnectorBoxedDRTA().getRTA().getActionFactory().newDocumentNow().execute(new ActionEvent());
                            }
                            BranchNode branch1 = new BranchNode(clickNode, self);
                            BranchNode branch2 = new BranchNode(clickNode, self);
                            BranchNode branch3 = new BranchNode(clickNode, self);
                            clickNode.getDependents().addAll(Arrays.asList(branch1, branch2, branch3));
                            pane.refresh();
                            setUndoRedoFlag(true);
                            setUndoRedoFlag(false);
                        }
                    }
                }
            }
        };

        threeBranchToggleListener = (ob, ov, nv) -> {
            if (isTornDown || this.mainView == null || mainPane == null || threeBranchClickFilter == null) return;
            if (nv) mainPane.addEventFilter(MouseEvent.MOUSE_PRESSED, threeBranchClickFilter);
            else mainPane.removeEventFilter(MouseEvent.MOUSE_PRESSED, threeBranchClickFilter);
        };
        threeBranchToggle.selectedProperty().addListener(threeBranchToggleListener);

        indefinateBranchClickFilter = new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (isTornDown || mainView == null || self == null || treePanes == null) return;
                if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 1) {
                    for (TreePane pane : treePanes) {
                        if (pane == null) continue;
                        BranchNode rootNode = pane.getRootBranchNode();
                        setClickedNode(event, rootNode);
                        if (clickNode != null && clickNode.isFormulaNode() && clickNode.getDependents() != null && formulaDependents(clickNode.getDependents())) {
                            BranchNode branch = new BranchNode(clickNode, self);
                            branch.setIndefiniteNode(true);
                            branch.setStyle("-fx-border-width: 0 0 0 0");
                            if (branch.getFormulaBoxedDRTA() != null && branch.getFormulaBoxedDRTA().getRTA() != null) {
                                RichTextArea rta = branch.getFormulaBoxedDRTA().getRTA();
                                rta.getActionFactory().open(new Document("   \u22ee")).execute(new ActionEvent());
                                rta.prefWidthProperty().unbind();
                                rta.setPrefWidth(24);
                            }
                            clickNode.getDependents().add(branch);
                            pane.refresh();
                            setUndoRedoFlag(true);
                            setUndoRedoFlag(false);
                        }
                    }
                }
            }
        };

        indefinateBranchToggleListener = (ob, ov, nv) -> {
            if (isTornDown || this.mainView == null || mainPane == null || indefinateBranchClickFilter == null) return;
            if (nv) mainPane.addEventFilter(MouseEvent.MOUSE_PRESSED, indefinateBranchClickFilter);
            else mainPane.removeEventFilter(MouseEvent.MOUSE_PRESSED, indefinateBranchClickFilter);
        };
        indefiniteBranchToggle.selectedProperty().addListener(indefinateBranchToggleListener);

        oneBranchTermClickFilter = new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (isTornDown || mainView == null || self == null || treePanes == null) return;
                if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 1) {
                    for (TreePane pane : treePanes) {
                        if (pane == null) continue;
                        BranchNode rootNode = pane.getRootBranchNode();
                        setClickedNode(event, rootNode);
                        if (clickNode != null && clickNode.getDependents() != null && termDependents(clickNode.getDependents())) {
                            BranchNode branch = new BranchNode(clickNode, self);
                            branch.setFormulaNode(false);
                            branch.setStyle("-fx-border-width: 0 0 0 0");
                            if (branch.getFormulaBoxedDRTA() != null && branch.getFormulaBoxedDRTA().getDRTA() != null) {
                                DecoratedRTA drta1 = branch.getFormulaBoxedDRTA().getDRTA();
                                drta1.getKeyboardSelector().valueProperty().setValue(RichTextAreaSkin.KeyMapValue.ITALIC_AND_SANS);
                            }
                            clickNode.getDependents().add(branch);
                            pane.refresh();
                            setUndoRedoFlag(true);
                            setUndoRedoFlag(false);
                        }
                    }
                }
            }
        };

        oneBranchTermToggleListener = (ob, ov, nv) -> {
            if (isTornDown || this.mainView == null || mainPane == null || oneBranchTermClickFilter == null) return;
            if (nv) mainPane.addEventFilter(MouseEvent.MOUSE_PRESSED, oneBranchTermClickFilter);
            else mainPane.removeEventFilter(MouseEvent.MOUSE_PRESSED, oneBranchTermClickFilter);
        };
        oneBranchTermToggle.selectedProperty().addListener(oneBranchTermToggleListener);

        twoBranchTermClickFilter = new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (isTornDown || mainView == null || self == null || treePanes == null) return;
                if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 1) {
                    for (TreePane pane : treePanes) {
                        if (pane == null) continue;
                        BranchNode rootNode = pane.getRootBranchNode();
                        setClickedNode(event, rootNode);
                        if (clickNode != null && clickNode.getDependents() != null && termDependents(clickNode.getDependents())) {
                            BranchNode branch1 = new BranchNode(clickNode, self);
                            BranchNode branch2 = new BranchNode(clickNode, self);
                            branch1.setFormulaNode(false);
                            branch2.setFormulaNode(false);
                            branch1.setStyle("-fx-border-width: 0 0 0 0");
                            branch2.setStyle("-fx-border-width: 0 0 0 0");

                            if (branch1.getFormulaBoxedDRTA() != null && branch1.getFormulaBoxedDRTA().getDRTA() != null) {
                                DecoratedRTA drta1 = branch1.getFormulaBoxedDRTA().getDRTA();
                                drta1.getKeyboardSelector().valueProperty().setValue(RichTextAreaSkin.KeyMapValue.ITALIC_AND_SANS);
                            }
                            if (branch2.getFormulaBoxedDRTA() != null && branch2.getFormulaBoxedDRTA().getDRTA() != null) {
                                DecoratedRTA drta2 = branch2.getFormulaBoxedDRTA().getDRTA();
                                drta2.getKeyboardSelector().valueProperty().setValue(RichTextAreaSkin.KeyMapValue.ITALIC_AND_SANS);
                            }

                            clickNode.getDependents().addAll(Arrays.asList(branch1, branch2));
                            pane.refresh();
                            setUndoRedoFlag(true);
                            setUndoRedoFlag(false);
                        }
                    }
                }
            }
        };

        twoBranchTermToggleListener = (ob, ov, nv) -> {
            if (isTornDown || this.mainView == null || mainPane == null || twoBranchTermClickFilter == null) return;
            if (nv) mainPane.addEventFilter(MouseEvent.MOUSE_PRESSED, twoBranchTermClickFilter);
            else mainPane.removeEventFilter(MouseEvent.MOUSE_PRESSED, twoBranchTermClickFilter);
        };
        twoBranchTermToggle.selectedProperty().addListener(twoBranchTermToggleListener);

        dotsClickFilter = new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (isTornDown || mainView == null || self == null || treePanes == null) return;
                for (TreePane pane : treePanes) {
                    if (pane == null) continue;
                    BranchNode rootNode = pane.getRootBranchNode();
                    setClickedNode(event, rootNode);
                    if (clickNode != null && clickNode.isFormulaNode()) {
                        if (event.getButton() == MouseButton.PRIMARY) {
                            if (event.getClickCount() == 1) {
                                clickNode.setDotDivider(true);
                                pane.refresh();
                                setUndoRedoFlag(true);
                                setUndoRedoFlag(false);
                            } else {
                                clickNode.setDotDivider(false);
                                pane.refresh();
                                setUndoRedoFlag(true);
                                setUndoRedoFlag(false);
                            }
                        }
                    }
                }
            }
        };

        verticalDotsToggleListener = (ob, ov, nv) -> {
            if (isTornDown || this.mainView == null || mainPane == null || dotsClickFilter == null) return;
            if (nv) mainPane.addEventFilter(MouseEvent.MOUSE_PRESSED, dotsClickFilter);
            else mainPane.removeEventFilter(MouseEvent.MOUSE_PRESSED, dotsClickFilter);
        };
        verticalDotsToggle.selectedProperty().addListener(verticalDotsToggleListener);

        annotationClickFilter = new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (isTornDown || mainView == null || self == null || treePanes == null) return;
                if (event.getButton() == MouseButton.PRIMARY) {
                    for (TreePane pane : treePanes) {
                        if (pane == null) continue;
                        BranchNode rootNode = pane.getRootBranchNode();
                        setClickedNode(event, rootNode);
                        if (clickNode != null) {
                            clickNode.processAnnotationRequest(event.getClickCount() == 1);
                            pane.refresh();
                            setUndoRedoFlag(true);
                            setUndoRedoFlag(false);
                            break;
                        }
                    }
                }
            }
        };

        annotationToggleListener = (ob, ov, nv) -> {
            if (isTornDown || this.mainView == null || mainPane == null || annotationClickFilter == null) return;
            if (nv)  mainPane.addEventFilter(MouseEvent.MOUSE_PRESSED, annotationClickFilter );
            else mainPane.removeEventFilter(MouseEvent.MOUSE_PRESSED, annotationClickFilter);
        };
        annotationToggle.selectedProperty().addListener(annotationToggleListener);

        annotationPlusHandler = e -> {
            if (isTornDown || mainView == null || treePanes == null) return;
            for (TreePane pane : treePanes) {
                if (pane != null) {
                    setAnnotations(pane.getRootBranchNode(), true);
                    pane.refresh();
                    setUndoRedoFlag(true);
                    setUndoRedoFlag(false);
                }
            }
            deselectToggles();
        };
        annotationPlus.setOnAction(annotationPlusHandler);

        annotationMinusHandler = e -> {
            if (isTornDown || mainView == null || treePanes == null) return;
            for (TreePane pane : treePanes) {
                if (pane != null) {
                    setAnnotations(pane.getRootBranchNode(), false);
                    pane.refresh();
                    setUndoRedoFlag(true);
                    setUndoRedoFlag(false);
                }
            }
            deselectToggles();
        };
        annotationMinus.setOnAction(annotationMinusHandler);

        rulerButtonToggleListener = (ob, ov, nv) -> {
            if (isTornDown || this.mainView == null) return;
            if (nv) {
                if (!axis) {
                    simpleAddAxis();
                    setUndoRedoFlag(true);
                    setUndoRedoFlag(false);
                }
            }
            else {
                if (axis) {
                    simpleRemoveAxis();
                    setUndoRedoFlag(true);
                    setUndoRedoFlag(false);
                }
            }
        };
        rulerButton.selectedProperty().addListener(rulerButtonToggleListener);
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

        checkButton = new Button("Check Tree(s)");
        checkButton.setPrefWidth(105);
        checkButton.setTooltip(new Tooltip("Check tree or trees for correctness."));
        checkTriesLabel = new Label();

        checkProgButton = new Button();

        VBox checksBox = new VBox(10, checkButton, checkTriesLabel);
        checksBox.setAlignment(Pos.CENTER);
        checkTriesLabel.setAlignment(Pos.CENTER);

        staticHelpButton = new Button("Static Help");
        staticHelpButton.setPrefWidth(105);
        staticHelpButton.setTooltip(new Tooltip("Get static help text"));

        permitEmptyBranchesCheck = new CheckBox("Empty Branches");

        VBox rightControlBox = new VBox(40, bigCheckBox, checksBox, staticHelpButton, permitEmptyBranchesCheck );
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
        if (isTornDown || mainView == null) return;

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
                if (isTornDown || this.mainView == null || mainPane == null) return;
                if (currentSpinnerNode != mainPane) {
                    mainPane.requestFocus();
                    currentSpinnerNode = mainPane;
                    if (mainPaneHeightSpinner != null && mainPaneWidthSpinner != null) {
                        if (mainPaneHeightSpinner.getValueFactory() != null) {
                            mainPaneHeightSpinner.getValueFactory().setValue((double) Math.round(mainPane.getHeight() / localScalePageHeight.get() * 100.0));
                        }
                        if (mainPaneWidthSpinner.getValueFactory() != null) {
                            mainPaneWidthSpinner.getValueFactory().setValue((double) Math.round((mainPane.getWidth() - RTA_WIDTH_BMP) / localScalePageWidth.get() * 100.0));
                        }
                        this.mainView.updateSizeSpinners(mainPaneHeightSpinner, mainPaneWidthSpinner);
                    }
                }
            };
            mainPane.addEventFilter(MouseEvent.MOUSE_CLICKED, mainPaneClickHandler);
        }


        if (mainView != null && mainView.getMainScene() != null && exerciseFocusListener != null) {
            mainView.getMainScene().focusOwnerProperty().addListener(exerciseFocusListener);
        }
    }

    /**
     * Add ruler axis to main pane
     */
    void simpleAddAxis() {
        if (isTornDown || mainPane == null || axisNode == null) return;
        axis = true;
        if (!mainPane.getChildren().contains(axisNode)) mainPane.getChildren().add(0, axisNode);
        axisNode.setLayoutX(5.0);
    }

    /**
     * Remove ruler axis from main pane
     */
    public void simpleRemoveAxis() {
        if (isTornDown || mainPane == null || axisNode == null) return;
        axis = false;
        mainPane.getChildren().remove(axisNode);
    }

    /**
     * There are formula dependents if (dependent list is empty or) all immediate children are formula nodes
     * @param dependentList the list of dependents
     * @return true if there are formula dependents and otherwise false
     */
    boolean formulaDependents(ArrayList<BranchNode> dependentList) {
        if (dependentList == null) return true;
        boolean formulaDependents = true;
        for (BranchNode node : dependentList) {
            if (!node.isFormulaNode()) {
                formulaDependents = false;
                break;
            }
        }
        return formulaDependents;
    }

    /**
     * There are term dependents if (dependent list is empty or) all immediate children are term nodes
     * @param dependentList the list of dependents
     * @return true if there are term dependents and otherwise false
     */
    boolean termDependents(ArrayList<BranchNode> dependentList) {
        if (dependentList == null) return true;
        boolean termDependents = true;
        for (BranchNode node : dependentList) {
            if (node.isFormulaNode()) {
                termDependents = false;
                break;
            }
        }
        return termDependents;
    }

    /**
     * Add or remove annotation fields on node and its children
     * @param node the node
     * @param add true if add, and otherwise (if remove) false
     */
    void setAnnotations(BranchNode node, boolean add) {
        if (node == null) return;
        node.processAnnotationRequest(add);
        if (node.getDependents() != null) {
            for (BranchNode child : node.getDependents()) {
                setAnnotations(child, add);
            }
        }
    }

    /**
     * A node is inHierarchy if it is among the ancestors of a given node
     * @param node the given (child node)
     * @param potentialHierarchyElement the potential ancestor
     * @return true if the potential ancestor is among the ancestors of the given node, and otherwise false
     */
    static boolean inHierarchy(Node node, Node potentialHierarchyElement) {
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
     * Identify branch node for mouse event among descendants of given node
     * @param event the mouse event
     * @param node the parent node
     */
    void setClickedNode(MouseEvent event, BranchNode node) {
        HorizontalTreeView.clickNode = null;
        findClickNodeInTree(event, node);
    }

    /**
     * Recursively run through tree, to find selected branch node
     * @param event the mouse event
     * @param node the parent node
     */
    void findClickNodeInTree(MouseEvent event, BranchNode node) {
        if (node == null || event == null || event.getPickResult() == null || event.getPickResult().getIntersectedNode() == null) return;
        if ((inHierarchy(event.getPickResult().getIntersectedNode(), node))) {
            HorizontalTreeView.clickNode = node;
        }
        else {
            if (node.getDependents() != null) {
                for (int i = 0; i < node.getDependents().size(); i++) {
                    BranchNode newNode = node.getDependents().get(i);
                    findClickNodeInTree(event, newNode);
                }
            }
        }
    }

    /**
     * Refresh the main pane by refreshing tree panes and ruler axis
     */
    void refreshTreePanes() {
        if (isTornDown || mainPane == null || treePanes == null) return;

        // SCORCHED EARTH RULE 8: Safely disconnect existing TreePanes to prevent concurrent issues
        if (mainPane.getChildren() != null && !mainPane.getChildren().isEmpty()) {
            List<Node> childrenCopy = new ArrayList<>(mainPane.getChildren());
            for (Node node : childrenCopy) {
                if (node instanceof TreePane) {
                    severFromParent(node);
                }
            }
        }

        for (TreePane pane : treePanes) {
            if (pane != null) {
                pane.refresh();
                mainPane.getChildren().add(pane);
            }
        }

        if (axis) {
            simpleAddAxis();
            if (rulerButton != null) rulerButton.setSelected(true);
        }
        else {
            if (rulerButton != null) rulerButton.setSelected(false);
        }
    }

    // SCORCHED EARTH RULE 6: Sever Shared Nodes helper
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


    // SCORCHED EARTH RULE 5: Rule of Isolation

    public void cleanup() {
        isTornDown = true;
        try { if (localScalePageHeight != null) { if (localScalePageHeightListener != null) localScalePageHeight.removeListener(localScalePageHeightListener); localScalePageHeight.unbind(); } } catch(Exception e){}
        try { if (localScalePageWidth != null) { if (localScalePageWidthListener != null) localScalePageWidth.removeListener(localScalePageWidthListener); localScalePageWidth.unbind(); } } catch(Exception e){}
        try { if (undoRedoFlag != null) undoRedoFlag.unbind(); } catch(Exception e){}

        try { if (formulaNodeToggle != null && formulaNodeToggleListener != null) formulaNodeToggle.selectedProperty().removeListener(formulaNodeToggleListener); } catch (Exception e){}
        try { if (oneBranchToggle != null && oneBranchToggleListener != null) oneBranchToggle.selectedProperty().removeListener(oneBranchToggleListener); } catch (Exception e){}
        try { if (twoBranchToggle != null && twoBranchToggleListener != null) twoBranchToggle.selectedProperty().removeListener(twoBranchToggleListener); } catch (Exception e){}
        try { if (threeBranchToggle != null && threeBranchToggleListener != null) threeBranchToggle.selectedProperty().removeListener(threeBranchToggleListener); } catch (Exception e){}
        try { if (indefiniteBranchToggle != null && indefinateBranchToggleListener != null) indefiniteBranchToggle.selectedProperty().removeListener(indefinateBranchToggleListener); } catch (Exception e){}
        try { if (oneBranchTermToggle != null && oneBranchTermToggleListener != null) oneBranchTermToggle.selectedProperty().removeListener(oneBranchTermToggleListener); } catch (Exception e){}
        try { if (twoBranchTermToggle != null && twoBranchTermToggleListener != null) twoBranchTermToggle.selectedProperty().removeListener(twoBranchTermToggleListener); } catch (Exception e){}
        try { if (verticalDotsToggle != null && verticalDotsToggleListener != null) verticalDotsToggle.selectedProperty().removeListener(verticalDotsToggleListener); } catch (Exception e){}
        try { if (annotationToggle != null && annotationToggleListener != null) annotationToggle.selectedProperty().removeListener(annotationToggleListener); } catch (Exception e){}
        try { if (rulerButton != null && rulerButtonToggleListener != null) rulerButton.selectedProperty().removeListener(rulerButtonToggleListener); } catch (Exception e){}

        try {
            if (mainView != null && mainView.getMainScene() != null && exerciseFocusListener != null) {
                mainView.getMainScene().focusOwnerProperty().removeListener(exerciseFocusListener);
            }
        } catch (Exception e){}

        try { if (pointsEarnedTextField != null) pointsEarnedTextField.setTextFormatter(null); } catch (Exception e){}
        try { if (staticHelpStage != null) staticHelpStage.close(); } catch (Exception e){}

        // ---> 3. SCORCHED EARTH RULE 4: THE JAVAFX SKIN HAMMER (Async Separation) <---
        try {
            final RichTextArea stEditor = (exerciseStatement != null) ? exerciseStatement.getEditor() : null;
            final RichTextArea commEditor = (exerciseComment != null) ? exerciseComment.getEditor() : null;
            final RichTextArea expEditor = (explainDRTA != null) ? explainDRTA.getEditor() : null;
            final RichTextArea interpDRTARichText = (interpretationDRTA != null) ? interpretationDRTA.getEditor() : null;
            final RichTextArea interpRTA = interpretationRTA;

            // Unbind size properties
            try { if (stEditor != null) { stEditor.prefHeightProperty().unbind(); stEditor.prefWidthProperty().unbind(); stEditor.maxWidthProperty().unbind(); stEditor.minWidthProperty().unbind(); stEditor.contentAreaWidthProperty().unbind(); } } catch (Exception e){}
            try { if (commEditor != null) { commEditor.prefHeightProperty().unbind(); commEditor.prefWidthProperty().unbind(); commEditor.maxWidthProperty().unbind(); commEditor.minWidthProperty().unbind(); commEditor.contentAreaWidthProperty().unbind(); } } catch (Exception e){}
            try { if (expEditor != null) { expEditor.prefHeightProperty().unbind(); expEditor.prefWidthProperty().unbind(); expEditor.maxWidthProperty().unbind(); expEditor.minWidthProperty().unbind(); expEditor.contentAreaWidthProperty().unbind(); } } catch (Exception e){}
            try { if (interpDRTARichText != null) { interpDRTARichText.prefHeightProperty().unbind(); interpDRTARichText.prefWidthProperty().unbind(); interpDRTARichText.maxWidthProperty().unbind(); interpDRTARichText.minWidthProperty().unbind(); interpDRTARichText.contentAreaWidthProperty().unbind(); } } catch (Exception e){}
            try { if (interpRTA != null) { interpRTA.prefHeightProperty().unbind(); interpRTA.prefWidthProperty().unbind(); interpRTA.maxWidthProperty().unbind(); interpRTA.minWidthProperty().unbind(); interpRTA.contentAreaWidthProperty().unbind(); } } catch (Exception e){}

            // STAGE 1: Execute async document injection IMMEDIATELY and synchronously clear prompt texts to disarm the NPE listener
            try { if (stEditor != null) { stEditor.setPromptText(""); stEditor.setFocusTraversable(false); if (stEditor.getDocument() == null || stEditor.getDocument().getText().isEmpty()) stEditor.getActionFactory().open(new Document(" ")).execute(new ActionEvent()); } } catch (Exception ex) {}
            try { if (commEditor != null) { commEditor.setPromptText(""); commEditor.setFocusTraversable(false); if (commEditor.getDocument() == null || commEditor.getDocument().getText().isEmpty()) commEditor.getActionFactory().open(new Document(" ")).execute(new ActionEvent()); } } catch (Exception ex) {}
            try { if (expEditor != null) { expEditor.setPromptText(""); expEditor.setFocusTraversable(false); if (expEditor.getDocument() == null || expEditor.getDocument().getText().isEmpty()) expEditor.getActionFactory().open(new Document(" ")).execute(new ActionEvent()); } } catch (Exception ex) {}
            try { if (interpDRTARichText != null) { interpDRTARichText.setPromptText(""); interpDRTARichText.setFocusTraversable(false); if (interpDRTARichText.getDocument() == null || interpDRTARichText.getDocument().getText().isEmpty()) interpDRTARichText.getActionFactory().open(new Document(" ")).execute(new ActionEvent()); } } catch (Exception ex) {}
            try { if (interpRTA != null) { interpRTA.setPromptText(""); interpRTA.setFocusTraversable(false); if (interpRTA != interpDRTARichText && (interpRTA.getDocument() == null || interpRTA.getDocument().getText().isEmpty())) interpRTA.getActionFactory().open(new Document(" ")).execute(new ActionEvent()); } } catch (Exception ex) {}

            // STAGE 2: Wait 1.5s for ALL background tasks and internal Gluon listeners to complete BEFORE safely nulling the skin.
            if (stEditor != null || commEditor != null || expEditor != null || interpDRTARichText != null || interpRTA != null) {
                PauseTransition pt = new PauseTransition(Duration.millis(1500));
                pt.setOnFinished(ev -> {
                    try { if (stEditor != null) stEditor.setSkin(null); } catch (Exception ex) {}
                    try { if (commEditor != null) commEditor.setSkin(null); } catch (Exception ex) {}
                    try { if (expEditor != null) expEditor.setSkin(null); } catch (Exception ex) {}
                    try { if (interpDRTARichText != null) interpDRTARichText.setSkin(null); } catch (Exception ex) {}
                    try { if (interpRTA != null && interpRTA != interpDRTARichText) interpRTA.setSkin(null); } catch (Exception ex) {}
                });
                pt.play();
            }
        } catch (Exception e) {}

        // SCORCHED EARTH RULE 8: Dismantle layout and listeners safely
        try {
            if (mainPane != null) {
                if (mainPaneHeightListener != null) mainPane.heightProperty().removeListener(mainPaneHeightListener);
                if (mainPaneWidthListener != null) mainPane.widthProperty().removeListener(mainPaneWidthListener);
                if (mainPaneClickHandler != null) mainPane.removeEventFilter(MouseEvent.MOUSE_CLICKED, mainPaneClickHandler);

                if (formulaNodeClickFilter != null) mainPane.removeEventFilter(MouseEvent.MOUSE_CLICKED, formulaNodeClickFilter);
                if (oneBranchClickFilter != null) mainPane.removeEventFilter(MouseEvent.MOUSE_PRESSED, oneBranchClickFilter);
                if (twoBranchClickFilter != null) mainPane.removeEventFilter(MouseEvent.MOUSE_PRESSED, twoBranchClickFilter);
                if (threeBranchClickFilter != null) mainPane.removeEventFilter(MouseEvent.MOUSE_PRESSED, threeBranchClickFilter);
                if (indefinateBranchClickFilter != null) mainPane.removeEventFilter(MouseEvent.MOUSE_PRESSED, indefinateBranchClickFilter);
                if (oneBranchTermClickFilter != null) mainPane.removeEventFilter(MouseEvent.MOUSE_PRESSED, oneBranchTermClickFilter);
                if (twoBranchTermClickFilter != null) mainPane.removeEventFilter(MouseEvent.MOUSE_PRESSED, twoBranchTermClickFilter);
                if (dotsClickFilter != null) mainPane.removeEventFilter(MouseEvent.MOUSE_PRESSED, dotsClickFilter);
                if (annotationClickFilter != null) mainPane.removeEventFilter(MouseEvent.MOUSE_PRESSED, annotationClickFilter);

                if (mainPane.getChildren() != null) {
                    List<Node> childrenCopy = new ArrayList<>(mainPane.getChildren());
                    for (Node n : childrenCopy) {
                        if (n instanceof TreePane) {
                            try { ((TreePane) n).cleanup(); } catch (Exception e) {}
                        }
                        severFromParent(n);
                    }
                    mainPane.getChildren().clear();
                }


                mainPane.minWidthProperty().unbind();
                mainPane.prefHeightProperty().unbind();
                mainPane.prefWidthProperty().unbind();
                severFromParent(mainPane);
            }
        } catch (Exception e){}

        try {
            if (controlBox != null) {
                if (controlBox.getChildren() != null) {
                    List<Node> cbChildren = new ArrayList<>(controlBox.getChildren());
                    for(Node n : cbChildren) {
                        severFromParent(n);
                    }
                    controlBox.getChildren().clear();
                }
                severFromParent(controlBox);
            }
        } catch (Exception e){}

        try {
            if (centerBox != null) {
                if (centerBox.getChildren() != null) {
                    List<Node> cntrChildren = new ArrayList<>(centerBox.getChildren());
                    for (Node n : cntrChildren) {
                        severFromParent(n);
                    }
                    centerBox.getChildren().clear();
                }
                severFromParent(centerBox);
            }
        } catch (Exception e){}

        try { if (exerciseControlNode != null) severFromParent(exerciseControlNode); } catch (Exception e){}
        try { if (rightControlNode != null) severFromParent(rightControlNode); } catch (Exception e){}
        try { if (treePanes != null) treePanes.clear(); } catch (Exception e){}

        // 5. Null everything
        try { this.self = null; } catch(Exception e){}
        try { this.mainView = null; } catch(Exception e){}
        try { this.exerciseComment = null; } catch(Exception e){}
        try { this.exerciseStatement = null; } catch(Exception e){}
        try { this.explainDRTA = null; } catch(Exception e){}
        try { this.interpretationDRTA = null; } catch(Exception e){}
        try { this.interpretationRTA = null; } catch(Exception e){}
        try { this.exerciseControlNode = null; } catch(Exception e){}
        try { this.controlBox = null; } catch(Exception e){}
        try { this.statementHeightSpinner = null; } catch(Exception e){}
        try { this.statementWidthSpinner = null; } catch(Exception e){}
        try { this.commentHeightSpinner = null; } catch(Exception e){}
        try { this.commentWidthSpinner = null; } catch(Exception e){}
        try { this.explainHeightSpinner = null; } catch(Exception e){}
        try { this.explainWidthSpinner = null; } catch(Exception e){}
        try { this.interpretationHeightSpinner = null; } catch(Exception e){}
        try { this.interpretationWidthSpinner = null; } catch(Exception e){}
        try { this.mainPaneHeightSpinner = null; } catch(Exception e){}
        try { this.mainPaneWidthSpinner = null; } catch(Exception e){}
        try { this.currentSpinnerNode = null; } catch(Exception e){}
        try { this.mainPane = null; } catch(Exception e){}
        try { this.centerBox = null; } catch(Exception e){}
        try { if (this.undoButton != null) this.undoButton.setOnAction(null); this.undoButton = null; } catch(Exception e){}
        try { if (this.redoButton != null) this.redoButton.setOnAction(null); this.redoButton = null; } catch(Exception e){}
        try { this.undoRedoFlag = null; } catch(Exception e){}
        try { this.formulaNodeToggle = null; } catch(Exception e){}
        try { this.oneBranchToggle = null; } catch(Exception e){}
        try { this.twoBranchToggle = null; } catch(Exception e){}
        try { this.threeBranchToggle = null; } catch(Exception e){}
        try { this.indefiniteBranchToggle = null; } catch(Exception e){}
        try { this.verticalDotsToggle = null; } catch(Exception e){}
        try { this.oneBranchTermToggle = null; } catch(Exception e){}
        try { this.twoBranchTermToggle = null; } catch(Exception e){}
        try { this.annotationToggle = null; } catch(Exception e){}
        try { if (this.annotationPlus != null) { if (annotationPlusHandler != null) this.annotationPlus.removeEventHandler(ActionEvent.ACTION, annotationPlusHandler); this.annotationPlus.setOnAction(null); } this.annotationPlus = null; } catch(Exception e){}
        try { if (this.annotationMinus != null) { if (annotationMinusHandler != null) this.annotationMinus.removeEventHandler(ActionEvent.ACTION, annotationMinusHandler); this.annotationMinus.setOnAction(null); } this.annotationMinus = null; } catch(Exception e){}
        try { this.rulerButton = null; } catch(Exception e){}
        try { this.buttonGroup = null; } catch(Exception e){}
        try { this.treePanes = null; } catch(Exception e){}
        try { this.formulaNodeClickFilter = null; } catch(Exception e){}
        try { this.oneBranchClickFilter = null; } catch(Exception e){}
        try { this.twoBranchClickFilter = null; } catch(Exception e){}
        try { this.threeBranchClickFilter = null; } catch(Exception e){}
        try { this.indefinateBranchClickFilter = null; } catch(Exception e){}
        try { this.annotationClickFilter = null; } catch(Exception e){}
        try { this.dotsClickFilter = null; } catch(Exception e){}
        try { this.oneBranchTermClickFilter = null; } catch(Exception e){}
        try { this.twoBranchTermClickFilter = null; } catch(Exception e){}
        try { this.axisNode = null; } catch(Exception e){}
        try { HorizontalTreeView.clickNode = null; } catch(Exception e){}
        try { this.pointsEarnedTextField = null; } catch(Exception e){}
        try { this.rightControlNode = null; } catch(Exception e){}
        try { if (this.checkButton != null) this.checkButton.setOnAction(null); this.checkButton = null; } catch(Exception e){}
        try { if (this.checkProgButton != null) this.checkProgButton.setOnAction(null); this.checkProgButton = null; } catch(Exception e){}
        try { this.bigCheck = null; } catch(Exception e){}
        try { this.checkMessage = null; } catch(Exception e){}
        try { this.checkedElements = null; } catch(Exception e){}
        try { this.checkTriesLabel = null; } catch(Exception e){}
        try { this.checkColor = null; } catch(Exception e){}
        try { this.checkElementsColor = null; } catch(Exception e){}
        try { if (this.staticHelpButton != null) this.staticHelpButton.setOnAction(null); this.staticHelpButton = null; } catch(Exception e){}
        try { this.staticHelpStage = null; } catch(Exception e){}
        try { this.permitEmptyBranchesCheck = null; } catch(Exception e){}
        try { this.exerciseFocusListener = null; } catch(Exception e){}
        try { this.mainPaneHeightListener = null; } catch(Exception e){}
        try { this.mainPaneWidthListener = null; } catch(Exception e){}
        try { this.localScalePageHeightListener = null; } catch(Exception e){}
        try { this.localScalePageWidthListener = null; } catch(Exception e){}
        try { this.formulaNodeToggleListener = null; } catch(Exception e){}
        try { this.oneBranchToggleListener = null; } catch(Exception e){}
        try { this.twoBranchToggleListener = null; } catch(Exception e){}
        try { this.threeBranchToggleListener = null; } catch(Exception e){}
        try { this.indefinateBranchToggleListener = null; } catch(Exception e){}
        try { this.oneBranchTermToggleListener = null; } catch(Exception e){}
        try { this.twoBranchTermToggleListener = null; } catch(Exception e){}
        try { this.verticalDotsToggleListener = null; } catch(Exception e){}
        try { this.annotationToggleListener = null; } catch(Exception e){}
        try { this.rulerButtonToggleListener = null; } catch(Exception e){}
        try { this.mainPaneClickHandler = null; } catch(Exception e){}
        try { this.annotationPlusHandler = null; } catch(Exception e){}
        try { this.annotationMinusHandler = null; } catch(Exception e){}
        try { this.pointsEarnedFilter = null; } catch(Exception e){}
    }

    Button getUndoButton() {    return undoButton;  }
    Button getRedoButton() {  return redoButton;  }
    BooleanProperty undoRedoFlagProperty() {    return undoRedoFlag;    }
    void setUndoRedoFlag(boolean undoRedoFlag) {  if (this.undoRedoFlag != null) this.undoRedoFlag.set(undoRedoFlag);  }
    ToggleButton getRulerButton() {    return rulerButton;  }
    MainWindowView getMainView() { return mainView; }
    DecoratedRTA getExplainDRTA() { return explainDRTA; }
    void setExplainPrompt(String explainPrompt) {  this.explainPrompt = explainPrompt; }
    public AnchorPane getMainPane() { return mainPane;}
    boolean isAxis() {   return axis; }
    public List<TreePane> getTreePanes() { return treePanes;   }

    double getCommentPrefHeight() {
        if (exerciseComment == null || exerciseComment.getEditor() == null) return 0;
        return exerciseComment.getEditor().getPrefHeight();
    }

    void setCommentPrefHeight(double commentPrefHeight) {   this.commentPrefHeight = commentPrefHeight;  }

    double getExplainPrefHeight() {
        if (explainDRTA == null || explainDRTA.getEditor() == null) return 0;
        return explainDRTA.getEditor().getPrefHeight();
    }

    void setExplainPrefHeight(double explainPrefHeight) {   this.explainPrefHeight = explainPrefHeight;  }

    Spinner<Double> getMainPaneHeightSpinner() {return mainPaneHeightSpinner;}
    Spinner<Double> getMainPaneWidthSpinner() {return mainPaneWidthSpinner;}

    @Override
    public DecoratedRTA getExerciseComment() { return exerciseComment;  }

    @Override
    public void setExerciseComment(DecoratedRTA exerciseComment) { this.exerciseComment = exerciseComment;  }

    @Override
    public DecoratedRTA getExerciseStatement() { return exerciseStatement; }

    @Override
    public void setExerciseStatement(DecoratedRTA exerciseStatement) { this.exerciseStatement = exerciseStatement;  }

    @Override
    public Node getExerciseStatementNode() {
        if (exerciseStatement == null) return new AnchorPane();
        return exerciseStatement.getEditor();
    }

    @Override
    public void setStatementPrefHeight(double height) {
        if (exerciseStatement == null || exerciseStatement.getEditor() == null) return;
        statementPrefHeight = height;
        exerciseStatement.getEditor().setPrefHeight(height);
    }

    @Override
    public Node getExerciseContentNode() {
        if (mainPane == null || explainDRTA == null || explainDRTA.getEditor() == null) return new VBox();
        return new VBox(2, mainPane, explainDRTA.getEditor());
    }

    @Override
    public Node getExerciseControl() { return exerciseControlNode;  }

    @Override
    public Node getRightControl() {return rightControlNode;}

    @Override
    public Node getPointsNode() {
        if (pointsPossible > 0) {
            if (mainView != null && !mainView.isInstructorFunctions() && pointsEarnedTextField != null) pointsEarnedTextField.setDisable(true);
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

    public String getCheckMessage() {
        return checkMessage;
    }

    public void setInterpretationPrefHeight(double interpretationPrefHeight) {
        this.interpretationPrefHeight = interpretationPrefHeight;
    }

    public void setInterpretationDRTA(DecoratedRTA interpretationDRTA) {
        this.interpretationDRTA = interpretationDRTA;
    }

    public Button getStaticHelpButton() {
        return staticHelpButton;
    }

    public Button getCheckButton() {
        return checkButton;
    }

    public CheckBox getPermitEmptyBranchesCheck() {
        return permitEmptyBranchesCheck;
    }

    public DecoratedRTA getInterpretationDRTA() {
        return interpretationDRTA;
    }

    public void setInterpretationRTA(RichTextArea interpretationRTA) {
        this.interpretationRTA = interpretationRTA;
    }

    public double getInterpretationPrefHeight() {
        if (interpretationRTA == null) return 0;
        return interpretationRTA.getPrefHeight();
    }

    public void setCheckColor(Color checkColor) {
        this.checkColor = checkColor;
    }

    public void setCheckElementsColor(Color checkElementsColor) {
        this.checkElementsColor = checkElementsColor;
    }

    public void setCheckMessage(String checkMessage) {
        this.checkMessage = checkMessage;
    }

    public Label getCheckTriesLabel() {
        return checkTriesLabel;
    }

    public ToggleButton getFormulaNodeToggle() {
        return formulaNodeToggle;
    }

    public DoubleProperty localScalePageHeightProperty() {
        return localScalePageHeight;
    }

    public DoubleProperty localScalePageWidthProperty() {
        return localScalePageWidth;
    }

    public ChangeListener<Node> getExerciseFocusListener() {
        return exerciseFocusListener;
    }
}
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
import com.gluonhq.richtextarea.model.Document;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.chart.NumberAxis;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import slapp.editor.PrintUtilities;
import slapp.editor.decorated_rta.DecoratedRTA;
import slapp.editor.main_window.ExerciseView;
import slapp.editor.main_window.MainWindowView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * View for the horizontal tree exercise
 */
public class HorizontalTreeView implements ExerciseView<DecoratedRTA> {
    private HorizontalTreeView self;
    private MainWindowView mainView;
    private DecoratedRTA exerciseComment = new DecoratedRTA();
    private DecoratedRTA exerciseStatement = new DecoratedRTA();
    private DecoratedRTA explainDRTA = new DecoratedRTA();
    private String explainPrompt = "";
    private VBox controlBox = new VBox(15);
    private double statementPrefHeight = 80;
    private double commentPrefHeight = 0;
    private double explainPrefHeight = 0;
    private Spinner<Double> statementHeightSpinner;
    private Spinner<Double> statementWidthSpinner;
    private Spinner<Double> commentHeightSpinner;
    private Spinner<Double> commentWidthSpinner;
    private Spinner<Double> explainHeightSpinner;
    private Spinner<Double> explainWidthSpinner;
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
    private Node exerciseControlNode;
    private List<TreePane> treePanes = new ArrayList<>();
    EventHandler formulaNodeClickFilter;
    private EventHandler oneBranchClickFilter;
    private EventHandler twoBranchClickFilter;
    private EventHandler threeBranchClickFilter;
    private EventHandler indefinateBranchClickFilter;
    private EventHandler annotationClickFilter;
    private EventHandler dotsClickFilter;
    private EventHandler oneBranchTermClickFilter;
    private EventHandler twoBranchTermClickFilter;
    private Ruler axisNode;
    private boolean axis = false;
    private static BranchNode clickNode = null;

    private int pointsPossible;
    private TextField pointsEarnedTextField;

    /**
     * Construct the horizontal tree view
     * @param mainView the main view
     */
    HorizontalTreeView(MainWindowView mainView) {
        self = this;
        this.mainView = mainView;

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
        formulaNodeToggle.setTooltip(new Tooltip("Add (left click) or remove (right click) box with its branches"));
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
        annotationToggle.setTooltip(new Tooltip("Add (left click) or remove (right click) annotation box"));
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
        controlBox.setMargin(rulerButton, new Insets(-10, 0, 0, 0));

        controlBox.setAlignment(Pos.BASELINE_RIGHT);
        controlBox.setPadding(new Insets(20,20,0,20));
        controlBox.setMinWidth(150); controlBox.setMaxWidth(150);
        exerciseControlNode = controlBox;
    }

    /**
     * Deselect all the toggle buttons
     */
    void deselectToggles() {
        formulaNodeToggle.setSelected(false);
        oneBranchToggle.setSelected(false);
        twoBranchToggle.setSelected(false);
        threeBranchToggle.setSelected(false);
        indefiniteBranchToggle.setSelected(false);
        verticalDotsToggle.setSelected(false);
        oneBranchTermToggle.setSelected(false);
        twoBranchTermToggle.setSelected(false);
        annotationToggle.setSelected(false);
    }

    /**
     * Initialize statement, comment, explain, main pane, and button click filters
     */
    void initializeViewDetails() {
        //statement
        RichTextArea statementRTA = exerciseStatement.getEditor();
        statementRTA.getStylesheets().add("slappTextArea.css");
        statementRTA.setEditable(false);

        double statementInitialHeight = Math.round(statementPrefHeight / mainView.getScalePageHeight() * 100.0 );
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



        //comment
        RichTextArea commentRTA = exerciseComment.getEditor();
        commentRTA.getStylesheets().add("slappTextArea.css");
        commentRTA.setPromptText("Comment:");

        double commentInitialHeight = Math.round(commentPrefHeight / mainView.getScalePageHeight() * 100.0 );
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

        commentRTA.maxWidthProperty().bind(mainView.scalePageWidthProperty());
        commentRTA.minWidthProperty().bind(mainView.scalePageWidthProperty());
        commentWidthSpinner = new Spinner<>(0.0, 999.0, 100, 1.0);
        commentWidthSpinner.setPrefWidth(60);
        commentWidthSpinner.setDisable(true);
        commentWidthSpinner.setTooltip(new Tooltip("Width as % of selected paper"));



        //explain
        RichTextArea explainRTA = explainDRTA.getEditor();
        explainRTA.getStylesheets().add("slappTextArea.css");
        explainRTA.setPromptText(explainPrompt);

        double explainInitialHeight = Math.round(explainPrefHeight / mainView.getScalePageHeight() * 100.0 );
        explainHeightSpinner = new Spinner<>(0.0, 999.0, explainInitialHeight, 1.0);
        explainHeightSpinner.setPrefWidth(60);
        explainHeightSpinner.setDisable(false);
        explainHeightSpinner.setTooltip(new Tooltip("Height as % of selected paper"));
        explainRTA.prefHeightProperty().bind(Bindings.max(45.0, Bindings.multiply(mainView.scalePageHeightProperty(), DoubleProperty.doubleProperty(explainHeightSpinner.getValueFactory().valueProperty()).divide(100.0))));
        explainHeightSpinner.valueProperty().addListener((obs, ov, nv) -> {
            Node increment = explainHeightSpinner.lookup(".increment-arrow-button");
            if (increment != null) increment.getOnMouseReleased().handle(null);
            Node decrement = explainHeightSpinner.lookup(".decrement-arrow-button");
            if (decrement != null) decrement.getOnMouseReleased().handle(null);
        });

        explainRTA.maxWidthProperty().bind(mainView.scalePageWidthProperty());
        explainRTA.minWidthProperty().bind(mainView.scalePageWidthProperty());
        explainWidthSpinner = new Spinner<>(0.0, 999.0, 100, 1.0);
        explainWidthSpinner.setPrefWidth(60);
        explainWidthSpinner.setDisable(true);
        explainWidthSpinner.setTooltip(new Tooltip("Width as % of selected paper"));



        //main pane
        mainPane.setMinHeight(150);
        mainPane.setMinWidth(mainView.getScalePageWidth());

        mainPaneHeightSpinner = new Spinner<>(0.0,999.0, 0,1.0);
        mainPaneHeightSpinner.setPrefWidth(60);
        mainPaneHeightSpinner.setDisable(true);
        mainPaneHeightSpinner.setTooltip(new Tooltip("Height as % of selected paper"));
        mainPane.heightProperty().addListener((ob, ov, nv) -> {
            mainPaneHeightSpinner.getValueFactory().setValue((double) Math.round(mainPane.getHeight() / mainView.getScalePageHeight() * 100));
        });

        mainPaneWidthSpinner = new Spinner<>(0.0,999.0, 0,1.0);
        mainPaneWidthSpinner.setPrefWidth(60);
        mainPaneWidthSpinner.setDisable(true);
        mainPaneWidthSpinner.setTooltip(new Tooltip("Height as % of selected paper"));
        mainPane.widthProperty().addListener((ob, ov, nv) -> {
            mainPaneWidthSpinner.getValueFactory().setValue((double) Math.round(mainPane.getWidth() /mainView.getScalePageWidth() * 100));
            axisNode.updateRuler((double) nv);

        });

        /*
        mainPane.addEventFilter(MouseEvent.MOUSE_CLICKED, e -> {
 //           if (currentSpinnerNode != mainPane) {
                currentSpinnerNode = mainPane;
                mainPaneHeightSpinner.getValueFactory().setValue((double) Math.round(mainPane.getHeight()/ mainView.getScalePageHeight() * 100.0));
                mainPaneWidthSpinner.getValueFactory().setValue((double) Math.round(mainPane.getWidth()/ mainView.getScalePageWidth() * 100.0));
                mainView.updateSizeSpinners(mainPaneHeightSpinner, mainPaneWidthSpinner);
  //          }
        });


         */
        setSizeSpinners();


        //page size listeners
        mainView.scalePageHeightProperty().addListener((ob, ov, nv) -> {

            statementRTA.prefHeightProperty().unbind();
            statementHeightSpinner.getValueFactory().setValue((double) Math.round(statementHeightSpinner.getValue() * ov.doubleValue() / nv.doubleValue()));
            statementRTA.prefHeightProperty().bind(Bindings.max(45.0, Bindings.multiply(nv.doubleValue(), DoubleProperty.doubleProperty(statementHeightSpinner.getValueFactory().valueProperty()).divide(100.0))));

            commentRTA.prefHeightProperty().unbind();
            commentHeightSpinner.getValueFactory().setValue((double) Math.round(commentHeightSpinner.getValue() * ov.doubleValue() / nv.doubleValue()));
            commentRTA.prefHeightProperty().bind(Bindings.max(45.0, Bindings.multiply(nv.doubleValue(), DoubleProperty.doubleProperty(commentHeightSpinner.getValueFactory().valueProperty()).divide(100.0))));

            explainRTA.prefHeightProperty().unbind();
            explainHeightSpinner.getValueFactory().setValue((double) Math.round(explainHeightSpinner.getValue() * ov.doubleValue() / nv.doubleValue()));
            explainRTA.prefHeightProperty().bind(Bindings.max(45.0, Bindings.multiply(nv.doubleValue(), DoubleProperty.doubleProperty(explainHeightSpinner.getValueFactory().valueProperty()).divide(100.0))));

            mainPaneHeightSpinner.getValueFactory().setValue((double) Math.round(mainPane.getHeight() / mainView.getScalePageHeight() * 100.0));
        });

        mainView.scalePageWidthProperty().addListener((ob, ov, nv) -> {
            mainPaneWidthSpinner.getValueFactory().setValue((double) Math.round(mainPane.getWidth() / mainView.getScalePageWidth() * 100.0));
            mainPane.setMinWidth(mainView.getScalePageWidth());
        });

        //click filters
        formulaNodeClickFilter = new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (event.getButton() == MouseButton.PRIMARY) {
                    TreePane treePane = new TreePane(self);
                    treePanes.add(treePane);
                    mainPane.getChildren().add(treePane);
                    refreshTreePanes();
                    treePane.relocateToGridPoint(new Point2D(event.getX(), event.getY()));
                    setUndoRedoFlag(true);
                    setUndoRedoFlag(false);
                }

                else if (event.getButton() == MouseButton.SECONDARY) {
                    for (TreePane pane : treePanes) {
                        BranchNode rootNode = pane.getRootBranchNode();
                        if (inHierarchy(event.getPickResult().getIntersectedNode(), rootNode)) {
                            treePanes.remove(pane);
                            mainPane.getChildren().remove(pane);
                            formulaNodeToggle.setSelected(false);
                            setUndoRedoFlag(true);
                            setUndoRedoFlag(false);
                            break;
                        } else {
                            setClickedNode(event, rootNode);
                            if (clickNode != null && clickNode != rootNode) {
                                clickNode.getContainer().getDependents().remove(clickNode);
                                pane.refresh();
                                formulaNodeToggle.setSelected(false);
                                setUndoRedoFlag(true);
                                setUndoRedoFlag(false);
                                break;
                            }
                        }
                    }
                }
                formulaNodeToggle.setSelected(false);
            }
        };
        formulaNodeToggle.selectedProperty().addListener((ob, ov, nv) -> {
            if (nv) mainPane.addEventFilter(MouseEvent.MOUSE_PRESSED, formulaNodeClickFilter);
            else mainPane.removeEventFilter(MouseEvent.MOUSE_PRESSED, formulaNodeClickFilter);
        });

        oneBranchClickFilter = new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (event.getButton() == MouseButton.PRIMARY) {
                    for (TreePane pane : treePanes) {
                        BranchNode rootNode = pane.getRootBranchNode();
                        setClickedNode(event, rootNode);
                        if (clickNode != null && clickNode.isFormulaNode() && formulaDependents(clickNode.getDependents())) {
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

        oneBranchToggle.selectedProperty().addListener((ob, ov, nv) -> {
            if (nv) mainPane.addEventFilter(MouseEvent.MOUSE_PRESSED, oneBranchClickFilter);
            else mainPane.removeEventFilter(MouseEvent.MOUSE_PRESSED, oneBranchClickFilter);
        });

        twoBranchClickFilter = new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (event.getButton() == MouseButton.PRIMARY) {
                    for (TreePane pane : treePanes) {
                        BranchNode rootNode = pane.getRootBranchNode();
                        setClickedNode(event, rootNode);
                        if (clickNode != null && clickNode.isFormulaNode() && formulaDependents(clickNode.getDependents())) {
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

        twoBranchToggle.selectedProperty().addListener((ob, ov, nv) -> {
            if (nv) mainPane.addEventFilter(MouseEvent.MOUSE_PRESSED, twoBranchClickFilter);
            else mainPane.removeEventFilter(MouseEvent.MOUSE_PRESSED, twoBranchClickFilter);
        });

        threeBranchClickFilter = new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (event.getButton() == MouseButton.PRIMARY) {
                    for (TreePane pane : treePanes) {
                        BranchNode rootNode = pane.getRootBranchNode();
                        setClickedNode(event, rootNode);
                        if (clickNode != null && clickNode.isFormulaNode() && formulaDependents(clickNode.getDependents())) {
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

        threeBranchToggle.selectedProperty().addListener((ob, ov, nv) -> {
            if (nv) mainPane.addEventFilter(MouseEvent.MOUSE_PRESSED, threeBranchClickFilter);
            else mainPane.removeEventFilter(MouseEvent.MOUSE_PRESSED, threeBranchClickFilter);
        });

        indefinateBranchClickFilter = new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (event.getButton() == MouseButton.PRIMARY) {
                    for (TreePane pane : treePanes) {
                        BranchNode rootNode = pane.getRootBranchNode();
                        setClickedNode(event, rootNode);
                        if (clickNode != null && clickNode.isFormulaNode() && formulaDependents(clickNode.getDependents())) {
                            BranchNode branch = new BranchNode(clickNode, self);
                            branch.setIndefiniteNode(true);
                            branch.setStyle("-fx-border-width: 0 0 0 0");
                            RichTextArea rta = branch.getFormulaBoxedDRTA().getRTA();
                            rta.getActionFactory().open(new Document("   \u22ee")).execute(new ActionEvent());
                            rta.prefWidthProperty().unbind();
                            rta.setPrefWidth(24);
                            clickNode.getDependents().add(branch);
                            pane.refresh();
                            setUndoRedoFlag(true);
                            setUndoRedoFlag(false);
                        }
                    }
                }
            }
        };

        indefiniteBranchToggle.selectedProperty().addListener((ob, ov, nv) -> {
            if (nv) mainPane.addEventFilter(MouseEvent.MOUSE_PRESSED, indefinateBranchClickFilter);
            else mainPane.removeEventFilter(MouseEvent.MOUSE_PRESSED, indefinateBranchClickFilter);
        });

        oneBranchTermClickFilter = new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (event.getButton() == MouseButton.PRIMARY) {
                    for (TreePane pane : treePanes) {
                        BranchNode rootNode = pane.getRootBranchNode();
                        setClickedNode(event, rootNode);
                        if (clickNode != null && termDependents(clickNode.getDependents())) {
                            BranchNode branch = new BranchNode(clickNode, self);
                            branch.setFormulaNode(false);
                            branch.setStyle("-fx-border-width: 0 0 0 0");
                            RichTextArea rta = branch.getFormulaBoxedDRTA().getRTA();
 //                           rta.setPrefWidth(24);
                            clickNode.getDependents().add(branch);
                            pane.refresh();
                            setUndoRedoFlag(true);
                            setUndoRedoFlag(false);
                        }
                    }
                }
            }
        };

        oneBranchTermToggle.selectedProperty().addListener((ob, ov, nv) -> {
            if (nv) mainPane.addEventFilter(MouseEvent.MOUSE_PRESSED, oneBranchTermClickFilter);
            else mainPane.removeEventFilter(MouseEvent.MOUSE_PRESSED, oneBranchTermClickFilter);
        });

        twoBranchTermClickFilter = new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if (event.getButton() == MouseButton.PRIMARY) {
                    for (TreePane pane : treePanes) {
                        BranchNode rootNode = pane.getRootBranchNode();
                        setClickedNode(event, rootNode);
                        if (clickNode != null && termDependents(clickNode.getDependents())) {
                            BranchNode branch1 = new BranchNode(clickNode, self);
                            BranchNode branch2 = new BranchNode(clickNode, self);
                            branch1.setFormulaNode(false);
                            branch2.setFormulaNode(false);
                            branch1.setStyle("-fx-border-width: 0 0 0 0");
                            branch2.setStyle("-fx-border-width: 0 0 0 0");
                            RichTextArea rta1 = branch1.getFormulaBoxedDRTA().getRTA();
                            RichTextArea rta2 = branch2.getFormulaBoxedDRTA().getRTA();
//                            rta1.setPrefWidth(24);
//                            rta2.setPrefWidth(24);
                            clickNode.getDependents().addAll(Arrays.asList(branch1, branch2));
                            pane.refresh();
                            setUndoRedoFlag(true);
                            setUndoRedoFlag(false);
                        }
                    }
                }
            }
        };

        twoBranchTermToggle.selectedProperty().addListener((ob, ov, nv) -> {
            if (nv) mainPane.addEventFilter(MouseEvent.MOUSE_PRESSED, twoBranchTermClickFilter);
            else mainPane.removeEventFilter(MouseEvent.MOUSE_PRESSED, twoBranchTermClickFilter);
        });

        dotsClickFilter = new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {

                for (TreePane pane : treePanes) {
                    BranchNode rootNode = pane.getRootBranchNode();
                    setClickedNode(event, rootNode);
                    if (clickNode != null && clickNode.isFormulaNode()) {
                        if (event.getButton() == MouseButton.PRIMARY) {
                            clickNode.setDotDivider(true);
                            pane.refresh();
                            setUndoRedoFlag(true);
                            setUndoRedoFlag(false);
                        }
                        else {
                            clickNode.setDotDivider(false);
                            pane.refresh();
                            setUndoRedoFlag(true);
                            setUndoRedoFlag(false);
                        }
                    }
                }
            }
        };

        verticalDotsToggle.selectedProperty().addListener((ob, ov, nv) -> {
            if (nv) mainPane.addEventFilter(MouseEvent.MOUSE_PRESSED, dotsClickFilter);
            else mainPane.removeEventFilter(MouseEvent.MOUSE_PRESSED, dotsClickFilter);
        });


        annotationClickFilter = new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                for (TreePane pane : treePanes) {
                    BranchNode rootNode = pane.getRootBranchNode();
                    setClickedNode(event, rootNode);
                    if (clickNode != null) {
                        clickNode.processAnnotationRequest(event.getButton() == MouseButton.PRIMARY);
                        pane.refresh();
                        setUndoRedoFlag(true);
                        setUndoRedoFlag(false);
                        break;
                    }
                }
            }
        };
        annotationToggle.selectedProperty().addListener((ob, ov, nv) -> {
            if (nv)  mainPane.addEventFilter(MouseEvent.MOUSE_PRESSED, annotationClickFilter );
            else mainPane.removeEventFilter(MouseEvent.MOUSE_PRESSED, annotationClickFilter);
        });

        annotationPlus.setOnAction(e -> {
            for (TreePane pane : treePanes) {
                setAnnotations(pane.getRootBranchNode(), true);
                pane.refresh();
                setUndoRedoFlag(true);
                setUndoRedoFlag(false);
            }
            deselectToggles();
        });
        annotationMinus.setOnAction(e -> {
            for (TreePane pane : treePanes) {
                setAnnotations(pane.getRootBranchNode(), false);
                pane.refresh();
                setUndoRedoFlag(true);
                setUndoRedoFlag(false);
            }
            deselectToggles();
        });

        rulerButton.selectedProperty().addListener((ob, ov, nv) -> {
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
        });

    }

    private void setSizeSpinners() {

        mainPane.addEventFilter(MouseEvent.MOUSE_CLICKED, e -> {
            if (currentSpinnerNode != mainPane) {
                mainPane.requestFocus();
            }
        });

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
            RichTextArea explainRTA = explainDRTA.getEditor();
            if (inHierarchy(nv, explainRTA) && currentSpinnerNode != explainRTA) {
                currentSpinnerNode = explainRTA;
                mainView.updateSizeSpinners(explainHeightSpinner, explainWidthSpinner);
                return;
            }

            if (inHierarchy(nv, mainPane) && currentSpinnerNode != mainPane) {
                currentSpinnerNode = mainPane;
                mainPaneHeightSpinner.getValueFactory().setValue((double) Math.round(mainPane.getHeight()/ mainView.getScalePageHeight() * 100.0));
                mainPaneWidthSpinner.getValueFactory().setValue((double) Math.round(mainPane.getWidth()/ mainView.getScalePageWidth() * 100.0));
                mainView.updateSizeSpinners(mainPaneHeightSpinner, mainPaneWidthSpinner);
            }
        });
    }



    /**
     * Add ruler axis to main pane
     */
    void simpleAddAxis() {
        axis = true;
        mainPane.getChildren().add(0, axisNode);
        axisNode.setLayoutX(5.0);

    }

    /**
     * Remove ruler axis from main pane
     */
    void simpleRemoveAxis() {
        axis = false;
        mainPane.getChildren().remove(axisNode);
    }


    /**
     * There are formula dependents if (dependent list is empty or) all immediate children are formula nodes
     * @param dependentList the list of dependents
     * @return true if there are formula dependents and otherwise false
     */
    boolean formulaDependents(ArrayList<BranchNode> dependentList) {
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
    //dependents empty or all termNodes
    boolean termDependents(ArrayList<BranchNode> dependentList) {
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
        node.processAnnotationRequest(add);
        for (BranchNode child : node.getDependents()) {
            setAnnotations(child, add);
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
        clickNode = null;
        findClickNodeInTree(event, node);
    }

    /**
     * Recursively run through tree, to find selected branch node
     * @param event the mouse event
     * @param node the parent node
     */
    void findClickNodeInTree(MouseEvent event, BranchNode node) {
        if ((inHierarchy(event.getPickResult().getIntersectedNode(), node))) {
            clickNode = node;
        }
        else {
            for (int i = 0; i < node.getDependents().size(); i++) {
                BranchNode newNode = node.getDependents().get(i);
                findClickNodeInTree(event, newNode);
            }
        }
    }

    /**
     * Refresh the main pane by refreshing tree panes and ruler axis
     */
    void refreshTreePanes() {
        mainPane.getChildren().clear();

        for (TreePane pane : treePanes) {
            pane.refresh();
            mainPane.getChildren().add(pane);
        }

        if (axis) {
            simpleAddAxis();
            rulerButton.setSelected(true);
        }
        else {
            rulerButton.setSelected(false);
        }
    }

    /**
     * Button to undo last main action
     * @return the button
     */
    Button getUndoButton() {    return undoButton;  }

    /**
     * Button to redo last main action
     * @return the button
     */
    Button getRedoButton() {  return redoButton;  }

    /**
     * Boolean property which initiates an undo/redo push when it flips to true
     * @return the boolean property
     */
    BooleanProperty undoRedoFlagProperty() {    return undoRedoFlag;    }

    /**
     * Boolean propertyh which initiates an undo/redo push when it flips to true
     * @param undoRedoFlag the boolean value
     */
    void setUndoRedoFlag(boolean undoRedoFlag) {  this.undoRedoFlag.set(undoRedoFlag);  }

    /**
     * Button to add or remove ruler from main pane
     * @return the button
     */
    ToggleButton getRulerButton() {    return rulerButton;  }

    /**
     * The MainWindowView
     * @return the view
     */
    MainWindowView getMainView() { return mainView; }

    /**
     * The DecoratedRTA for the explain field
     * @return the DRTA
     */
    DecoratedRTA getExplainDRTA() { return explainDRTA; }

    /**
     * The prompt for the explain field
     * @param explainPrompt the String prompt
     */
    void setExplainPrompt(String explainPrompt) {  this.explainPrompt = explainPrompt; }

    /**
     * The main (drawing) pain containing tree and ruler panes
     * @return the pane
     */
    AnchorPane getMainPane() { return mainPane;}

    /**
     * True if ruler axis is showing and otherwise false
     * @return the boolean value
     */
    boolean isAxis() {   return axis; }

    /**
     * List of TreePanes to show on the main pane
     * @return the list
     */
    List<TreePane> getTreePanes() { return treePanes;   }

    /**
     * The preferred height of the comment field
     * @return the height value
     */
    double getCommentPrefHeight() { return exerciseComment.getEditor().getPrefHeight();   }

    /**
     * The preferred height of the comment field
     * @param commentPrefHeight the height value
     */
    void setCommentPrefHeight(double commentPrefHeight) {   this.commentPrefHeight = commentPrefHeight;  }

    /**
     * The preferred height of the explanation field
     * @return the height value
     */
    double getExplainPrefHeight() { return explainDRTA.getEditor().getPrefHeight();    }

    /**
     * The preferred height of the explanation field
     * @param explainPrefHeight the height value
     */
    void setExplainPrefHeight(double explainPrefHeight) {   this.explainPrefHeight = explainPrefHeight;  }

    Spinner getMainPaneHeightSpinner() {return mainPaneHeightSpinner;}

    Spinner getMainPaneWidthSpinner() {return mainPaneWidthSpinner;}

    /**
     * The comment decoratedRTA
     * @return comment DecoratedRTA
     */
    @Override
    public DecoratedRTA getExerciseComment() { return exerciseComment;  }

    /**
     * The comment decoratedRTA
     * @param exerciseComment DecoratedRTA
     */
    @Override
    public void setExerciseComment(DecoratedRTA exerciseComment) { this.exerciseComment = exerciseComment;  }

    /**
     * The exercise prompt
     * @return the statement decorated RTA (T)
     */
    @Override
    public DecoratedRTA getExerciseStatement() { return exerciseStatement; }

    /**
     * The exercise prompt
     * @param exerciseStatement the statement decorated RTA (T)
     */
    @Override
    public void setExerciseStatement(DecoratedRTA exerciseStatement) { this.exerciseStatement = exerciseStatement;  }

    /**
     * The exercise prompt node (RTA)
     * @return the statement node
     */
    @Override
    public Node getExerciseStatementNode() { return exerciseStatement.getEditor();  }

    /**
     * The preferred height of the statement window
     * @param height the preferred height
     */
    @Override
    public void setStatementPrefHeight(double height) {
        statementPrefHeight = height;
        exerciseStatement.getEditor().setPrefHeight(height);
    }

    /**
     * The node which includes content of this exercise
     * @return the content node
     */
    @Override
    public Node getExerciseContentNode() { return centerBox; }

    /**
     * The left control node for this exercise
     * @return the control node
     */
    @Override
    public Node getExerciseControl() { return exerciseControlNode;  }

    /**
     * The (null) right control node for this exercise
     * @return the control node
     */
    @Override
    public Node getRightControl() { return null; }

    @Override
    public Node getPointsNode() {
        if (pointsPossible > 0) {
            Label pointsPossibleLabel = new Label(" / " + pointsPossible);
            if (!mainView.isInstructorFunctions()) pointsEarnedTextField.setDisable(true);
            HBox pointBox = new HBox(pointsEarnedTextField, pointsPossibleLabel);
            return pointBox;
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

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

package slapp.editor.truth_table_generate;

import com.gluonhq.richtextarea.RichTextArea;
import com.gluonhq.richtextarea.RichTextAreaSkin;
import com.gluonhq.richtextarea.model.Document;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.geometry.*;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
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
import slapp.editor.EditorAlerts;
import slapp.editor.EditorMain;
import slapp.editor.decorated_rta.BoxedDRTA;
import slapp.editor.decorated_rta.DecoratedRTA;
import slapp.editor.main_window.ControlType;
import slapp.editor.main_window.ExerciseView;
import slapp.editor.main_window.MainWindowView;
import slapp.editor.truth_table.TableHeadItem;

import java.util.ArrayList;
import java.util.List;
import java.util.function.UnaryOperator;
import static slapp.editor.EditorMain.RTA_SCREEN_BMP;
import static slapp.editor.EditorMain.RTA_WIDTH_BMP;
import javafx.animation.PauseTransition;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.geometry.VPos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.event.EventHandler;
import javafx.scene.control.*;
import javafx.scene.layout.*;


/**
 * View for the truth table generate exercise
 */


public class TruthTableGenView implements ExerciseView<DecoratedRTA> {
    private MainWindowView mainView;
    private String interpretationPrompt = "";
    private DecoratedRTA exerciseStatement = new DecoratedRTA();
    private DecoratedRTA exerciseComment = new DecoratedRTA();
    private DecoratedRTA interpretationDRTA = new DecoratedRTA();
    private DecoratedRTA explainDRTA = new DecoratedRTA();
    private double statementPrefHeight = 0;
    private double commentPrefHeight = 0;
    private double interpretationPrefHeight = 0;
    private double explainPrefHeight = 0;
    private Spinner<Double> statementHeightSpinner;
    private Spinner<Double> statementWidthSpinner;
    private Spinner<Double> commentHeightSpinner;
    private Spinner<Double> commentWidthSpinner;
    private Spinner<Double> explainHeightSpinner;
    private Spinner<Double> explainWidthSpinner;
    private Spinner<Double> interpretationHeightSpinner;
    private Spinner<Double> interpretationWidthSpinner;
    private Spinner<Double> choicesHeightSpinner;
    private Spinner<Double> choicesWidthSpinner;
    private Spinner<Double> tableGridHeightSpinner;
    private Spinner<Double> tableGridWidthSpinner;
    private Node currentSpinnerNode;
    private GridPane basicFormulasPane;
    private List<BoxedDRTA> basicFormulasBoxedDRTAs;
    private GridPane mainFormulasPane;
    private List<BoxedDRTA> mainFormulasBoxedDRTAs;
    private VBox controlBox;
    private Spinner<Integer> rowsSpinner;
    Button setupTableButton;
    private Button addBasicFormulaButton;
    private Button removeBasicFormulaButton;
    private Button addMainFormulaButton;
    private Button removeMainFormulaButton;
    private Label choiceLeadLabel = new Label();
    private CheckBox aCheckBox = new CheckBox();
    private CheckBox bCheckBox = new CheckBox();
    private GridPane tableGrid;
    private VBox resultsBox;
    private HBox choiceBox;
    private VBox centerBox;
    private String explainPrompt;
    private double contentFixedHeight = 70;
    private double formulaBoxHeight = 23;
    private List<TableHeadItem> tableHeadItemsList;
    private TextField[][] tableFields; //list of text field columns
    private BoxedDRTA[] rowCommentsArray;
    private ToggleButton[] highlightButtons;
    private int tableRows = 0;
    private VBox[] sizers;
    private Pane endPane;
    BoxedDRTA focusedBoxedDRTA;
    private int pointsPossible;
    private TextField pointsEarnedTextField;

    private Button staticHelpButton;
    private Stage staticHelpStage;
    private Node rightControlNode;

    private Button checkButton;
    private Text bigCheck;
    private String checkMessage;
    private Text checkedElements;
    private Label checkTriesLabel;
    private Color checkColor;
    private Color checkElementsColor;
    private boolean checkShowing = false;
    private CheckBox shortTableCheck;

    private final DoubleProperty localScalePageHeight = new SimpleDoubleProperty(1.0);
    private final DoubleProperty localScalePageWidth = new SimpleDoubleProperty(1.0);

    // SCORCHED EARTH RULE 1: The Asynchronous Ghost "Kill Switch"
    private boolean isTornDown = false;

    // SCORCHED EARTH RULE 3: Eradicate Anonymous Lambdas
    private ChangeListener<Node> exerciseFocusListener;
    private EventHandler<KeyEvent> basicFormulasKeyFilter;
    private EventHandler<ActionEvent> addBasicFormulaHandler;
    private EventHandler<ActionEvent> removeBasicFormulaHandler;
    private EventHandler<KeyEvent> mainFormulasKeyFilter;
    private EventHandler<ActionEvent> addMainFormulaHandler;
    private EventHandler<ActionEvent> removeMainFormulaHandler;
    private UnaryOperator<TextFormatter.Change> pointsFilter;
    private ChangeListener<Number> tableGridHeightListener;
    private ChangeListener<Number> tableGridWidthListener;
    private ChangeListener<Number> localScalePageHeightListener;
    private ChangeListener<Number> localScalePageWidthListener;
    private EventHandler<MouseEvent> tableGridClickHandler;
    private EventHandler<MouseEvent> choiceBoxClickHandler;
    private UnaryOperator<TextFormatter.Change> singleCharTextFilter;
    private EventHandler<KeyEvent> singleCharTypeFilter;
    private EventHandler<KeyEvent> singleCharPressFilter;
    private EventHandler<ActionEvent> highlightButtonHandler;
    private ChangeListener<Boolean> formulaFocusListener;
    private ChangeListener<Number> commentNodesWidthListener;
    private EventHandler<KeyEvent> commentKeyFilter;
    private ChangeListener<Boolean> commentFocusListener;

    private ChangeListener<Boolean> pointsEarnedFocusListener;
    private EventHandler<MouseEvent> pointsEarnedClickHandler;

    private boolean checkTrans;

    /**
     * Construct the truth table generate view
     * @param mainView the main window
     */
    TruthTableGenView(MainWindowView mainView) {
        this.mainView = mainView;

        localScalePageHeight.bind(mainView.scalePageHeightProperty());
        localScalePageWidth.bind(mainView.scalePageWidthProperty());

        exerciseFocusListener = (ob, ov, nv) -> {
            if (isTornDown || this.mainView == null) return;
            try {

                RichTextArea statementRTA = exerciseStatement != null ? exerciseStatement.getEditor() : null;
                RichTextArea commentRTA = exerciseComment != null ? exerciseComment.getEditor() : null;
                RichTextArea interpretationRTA = interpretationDRTA != null? interpretationDRTA.getEditor() : null;
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
                } else if (interpretationRTA != null && inHierarchy(nv, interpretationRTA)) {
                    if (currentSpinnerNode != interpretationRTA) {
                        currentSpinnerNode = interpretationRTA;
                        if (interpretationHeightSpinner != null && interpretationWidthSpinner != null) {
                            this.mainView.updateSizeSpinners(interpretationHeightSpinner, interpretationWidthSpinner);
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
                } else if (tableGrid != null && inHierarchy(nv, tableGrid)) {
                    if (currentSpinnerNode != tableGrid) {
                        currentSpinnerNode = tableGrid;
                        if (tableGridHeightSpinner != null && tableGridWidthSpinner != null) {
                            this.mainView.updateSizeSpinners(tableGridHeightSpinner, tableGridWidthSpinner);
                        }
                    }
                } else {
                    currentSpinnerNode = null;
                }
            } catch (Exception e) {}
        };

        Font labelFont = new Font("Noto Serif Combo", 11);
        choiceLeadLabel.setFont(labelFont); aCheckBox.setFont(labelFont); bCheckBox.setFont(labelFont);

        basicFormulasPane = new GridPane();
        basicFormulasPane.setVgap(10);
        basicFormulasBoxedDRTAs = new ArrayList<>();

        mainFormulasPane = new GridPane();
        mainFormulasPane.setVgap(10);
        mainFormulasBoxedDRTAs = new ArrayList<>();

        initializeDynamicHandlers();

        //basic
        Label basicFormulasLabel = new Label("Basic Sentences:");
        addBasicFormulaButton = new Button("+");
        removeBasicFormulaButton = new Button("-");
        addBasicFormulaButton.setFont(new Font(16));
        addBasicFormulaButton.setPadding(new Insets(0,5,0,5));
        removeBasicFormulaButton.setFont(new Font(16));
        removeBasicFormulaButton.setPadding(new Insets(1,8,1,8));

        HBox basicFormulaButtonBox = new HBox(20, addBasicFormulaButton, removeBasicFormulaButton);
        VBox basicFormulaControlBox = new VBox(10, basicFormulasLabel, basicFormulaButtonBox);

        basicFormulasBoxedDRTAs.add(newFormulaBoxedDRTA());
        updateBasicFormulasPaneFromList();

        //main
        Label mainFormulasLabel = new Label("Premises/Conclusion:");
        addMainFormulaButton = new Button("+");
        removeMainFormulaButton = new Button("-");
        addMainFormulaButton.setFont(new Font(16));
        addMainFormulaButton.setPadding(new Insets(0,5,0,5));
        removeMainFormulaButton.setFont(new Font(16));
        removeMainFormulaButton.setPadding(new Insets(1,8,1,8));

        HBox mainFormulaButtonBox = new HBox(20, addMainFormulaButton, removeMainFormulaButton);
        VBox mainFormulaControlBox = new VBox(10, mainFormulasLabel, mainFormulaButtonBox);

        mainFormulasBoxedDRTAs.add(newFormulaBoxedDRTA());
        updateMainFormulasPaneFromList();

        //basic events
        basicFormulasKeyFilter = e -> {
            if (isTornDown || this.mainView == null || basicFormulasBoxedDRTAs == null) return;
            try {
                int index = basicFormulasBoxedDRTAs.indexOf(focusedBoxedDRTA);
                if (index >= 0) {
                    KeyCode code = e.getCode();
                    if (code == KeyCode.DOWN || code == KeyCode.ENTER) {
                        if (index + 1 < basicFormulasBoxedDRTAs.size()) {
                            if (basicFormulasBoxedDRTAs.get(index + 1) != null && basicFormulasBoxedDRTAs.get(index + 1).getRTA() != null)
                                basicFormulasBoxedDRTAs.get(index + 1).getRTA().requestFocus();
                        }
                        e.consume();
                    }
                    if (code == KeyCode.UP) {
                        if (index > 0) {
                            if (basicFormulasBoxedDRTAs.get(index - 1) != null && basicFormulasBoxedDRTAs.get(index - 1).getRTA() != null)
                                basicFormulasBoxedDRTAs.get(index - 1).getRTA().requestFocus();
                        }
                        e.consume();
                    }
                }
            } catch (Exception ex) {}
        };
        basicFormulasPane.addEventFilter(KeyEvent.KEY_PRESSED, basicFormulasKeyFilter);

        addBasicFormulaHandler = e -> {
            if (isTornDown || this.mainView == null || basicFormulasBoxedDRTAs == null) return;
            try {
                basicFormulasBoxedDRTAs.add(newFormulaBoxedDRTA());
                updateBasicFormulasPaneFromList();
            } catch (Exception ex) {}
        };
        addBasicFormulaButton.setOnAction(addBasicFormulaHandler);

        removeBasicFormulaHandler = e -> {
            if (isTornDown || this.mainView == null || basicFormulasBoxedDRTAs == null) return;
            try {
                int index = basicFormulasBoxedDRTAs.size();
                index--;

                if (index > 0) {
                    BoxedDRTA removed = basicFormulasBoxedDRTAs.remove(index);
                    if (focusedBoxedDRTA == removed) focusedBoxedDRTA = null;
                    if (removed != null && removed.getRTA() != null) {
                        RichTextArea rta = removed.getRTA();
                        try { if (formulaFocusListener != null) rta.focusedProperty().removeListener(formulaFocusListener); } catch (Exception ex) {}
                        try { rta.setUserData(null); } catch (Exception ex) {}
                        try { rta.setPromptText(""); } catch (Exception ex) {}
                        try { rta.setFocusTraversable(false); } catch (Exception ex) {}
                        try {
                            if (rta.getDocument() == null || rta.getDocument().getText().isEmpty()) {
                                rta.getActionFactory().open(new Document(" ")).execute(new ActionEvent());
                            }
                        } catch (Exception ex) {}
                        javafx.animation.PauseTransition pt = new javafx.animation.PauseTransition(javafx.util.Duration.millis(1500));
                        pt.setOnFinished(ev -> {
                            try { rta.setSkin(null); } catch (Exception ex) {}
                        });
                        pt.play();
                    }
                    updateBasicFormulasPaneFromList();
                } else {
                    EditorAlerts.showSimpleAlert("Cannot Remove", "A truth table must include at least one basic sentence.");
                }
            } catch (Exception ex) {}
        };
        removeBasicFormulaButton.setOnAction(removeBasicFormulaHandler);

        //main events
        mainFormulasKeyFilter = e -> {
            if (isTornDown || this.mainView == null || mainFormulasBoxedDRTAs == null) return;
            try {
                int index = mainFormulasBoxedDRTAs.indexOf(focusedBoxedDRTA);
                if (index >= 0) {
                    KeyCode code = e.getCode();
                    if (code == KeyCode.ENTER || code == KeyCode.DOWN) {
                        if (index + 1 < mainFormulasBoxedDRTAs.size()) {
                            if (mainFormulasBoxedDRTAs.get(index + 1) != null && mainFormulasBoxedDRTAs.get(index + 1).getRTA() != null)
                                mainFormulasBoxedDRTAs.get(index + 1).getRTA().requestFocus();
                        }
                        e.consume();
                    }
                    if (code == KeyCode.UP) {
                        if (index > 0) {
                            if (mainFormulasBoxedDRTAs.get(index - 1) != null && mainFormulasBoxedDRTAs.get(index - 1).getRTA() != null)
                                mainFormulasBoxedDRTAs.get(index - 1).getRTA().requestFocus();
                        }
                        e.consume();
                    }
                }
            } catch (Exception ex) {}
        };
        mainFormulasPane.addEventFilter(KeyEvent.KEY_PRESSED, mainFormulasKeyFilter);

        addMainFormulaHandler = e -> {
            if (isTornDown || this.mainView == null || mainFormulasBoxedDRTAs == null) return;
            try {
                mainFormulasBoxedDRTAs.add(newFormulaBoxedDRTA());
                updateMainFormulasPaneFromList();
            } catch (Exception ex) {}
        };
        addMainFormulaButton.setOnAction(addMainFormulaHandler);

        removeMainFormulaHandler = e -> {
            if (isTornDown || this.mainView == null || mainFormulasBoxedDRTAs == null) return;
            try {
                int index = mainFormulasBoxedDRTAs.size();
                index--;

                if (index > 0) {
                    BoxedDRTA removed = mainFormulasBoxedDRTAs.remove(index);
                    if (focusedBoxedDRTA == removed) focusedBoxedDRTA = null;
                    if (removed != null && removed.getRTA() != null) {
                        RichTextArea rta = removed.getRTA();
                        try { if (formulaFocusListener != null) rta.focusedProperty().removeListener(formulaFocusListener); } catch (Exception ex) {}
                        try { rta.setUserData(null); } catch (Exception ex) {}
                        try { rta.setPromptText(""); } catch (Exception ex) {}
                        try { rta.setFocusTraversable(false); } catch (Exception ex) {}
                        try {
                            if (rta.getDocument() == null || rta.getDocument().getText().isEmpty()) {
                                rta.getActionFactory().open(new Document(" ")).execute(new ActionEvent());
                            }
                        } catch (Exception ex) {}
                        javafx.animation.PauseTransition pt = new javafx.animation.PauseTransition(javafx.util.Duration.millis(1500));
                        pt.setOnFinished(ev -> {
                            try { rta.setSkin(null); } catch (Exception ex) {}
                        });
                        pt.play();
                    }
                    updateMainFormulasPaneFromList();
                } else {




                    EditorAlerts.showSimpleAlert("Cannot Remove", "Your argument must contain at least a conclusion.");
                }
            } catch (Exception ex) {}
        };
        removeMainFormulaButton.setOnAction(removeMainFormulaHandler);

        //finish
        Label rowNumLabel = new Label("Rows ");
        rowsSpinner = new Spinner<>(0,256,0);
        rowsSpinner.setPrefWidth(65);
        HBox spinnerBox = new HBox(5, rowsSpinner, rowNumLabel);
        spinnerBox.setAlignment(Pos.CENTER_LEFT);

        setupTableButton = new Button("Setup");
        setupTableButton.setPrefWidth(100);

        controlBox = new VBox(20, mainFormulaControlBox, mainFormulasPane, new Separator(Orientation.HORIZONTAL), basicFormulaControlBox, basicFormulasPane, new Separator(Orientation.HORIZONTAL), spinnerBox, setupTableButton);
        controlBox.setPadding(new Insets(50, 15,0,15));
        controlBox.setMinWidth(175); controlBox.setMaxWidth(175);
        controlBox.setAlignment(Pos.TOP_LEFT);

        choiceBox = new HBox(20, choiceLeadLabel, aCheckBox, bCheckBox);
        choiceBox.setPadding(new Insets(5,0,5,5));
        choiceBox.setStyle("-fx-border-color: gainsboro; -fx-border-width: 1 1 1 1");



        resultsBox = new VBox(5, choiceBox, explainDRTA.getEditor());
        resultsBox.setPadding(new Insets(0,0,0,0));

        tableGrid = new GridPane();
        tableGrid.setPadding(new Insets(15, RTA_WIDTH_BMP,15,10));

        if (checkTrans) {
            centerBox = new VBox(10, interpretationDRTA.getEditor(), tableGrid, resultsBox);
        }
        else {
            centerBox = new VBox(10, tableGrid, resultsBox);
        }
        tableGrid.setStyle("-fx-border-color: gainsboro");
        interpretationDRTA.getEditor().setStyle("-fx-border-width: 2; -fx-border-color: gainsboro;");

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

    }

    private void initializeDynamicHandlers() {
        singleCharTextFilter = c -> {
            if (isTornDown || this.mainView == null || c == null) return null;
            try {
                if (c.getText().matches("[0-9a-zA-Z]")) {
                    TextField field = (TextField) c.getControl();
                    c.setRange(0, field.getText().length());
                    return c;
                } else if (c.getText().isEmpty()) {
                    return c;
                }
            } catch (Exception e) {}
            return null;
        };

        singleCharTypeFilter = e -> {
            if (isTornDown || this.mainView == null || tableFields == null) return;
            try {
                TextField field = (TextField) e.getSource();
                int[] pos = (int[]) field.getUserData();
                if (pos == null) return;
                int column = pos[0]; int row = pos[1];
                if (tableFields.length <= column || tableFields[column] == null) return;

                if (row < tableRows - 1) {
                    if (tableFields[column].length > row + 1 && tableFields[column][row + 1] != null) tableFields[column][row + 1].requestFocus();
                } else {
                    if (tableFields[column].length > 0 && tableFields[column][0] != null) tableFields[column][0].requestFocus();
                }
            } catch (Exception ex) {}
        };

        singleCharPressFilter = e -> {
            if (isTornDown || this.mainView == null || tableFields == null || tableHeadItemsList == null) return;
            try {
                TextField field = (TextField) e.getSource();
                int[] pos = (int[]) field.getUserData();
                if (pos == null) return;
                int column = pos[0]; int row = pos[1];
                if (tableFields.length <= column || tableFields[column] == null) return;

                KeyCode code = e.getCode();
                if (code == KeyCode.UP && row > 0) {
                    if (tableFields[column].length > row - 1 && tableFields[column][row - 1] != null) tableFields[column][row - 1].requestFocus();
                    e.consume();
                } else if (code == KeyCode.DOWN && row < tableRows - 1) {
                    if (tableFields[column].length > row + 1 && tableFields[column][row + 1] != null) tableFields[column][row + 1].requestFocus();
                    e.consume();
                } else if (code == KeyCode.RIGHT) {
                    int colRight = tableColToRight(column);
                    if (tableFields.length > colRight && tableFields[colRight] != null && tableFields[colRight].length > row && tableFields[colRight][row] != null) {
                        tableFields[colRight][row].requestFocus();
                    }
                    e.consume();
                }  else if (code == KeyCode.LEFT) {
                    int colLeft = tableColToLeft(column);
                    if (tableFields.length > colLeft && tableFields[colLeft] != null && tableFields[colLeft].length > row && tableFields[colLeft][row] != null) {
                        tableFields[colLeft][row].requestFocus();
                    }
                    e.consume();
                }
            } catch (Exception ex) {}
        };

        highlightButtonHandler = e -> {
            if (isTornDown || this.mainView == null || tableFields == null) return;
            try {
                ToggleButton button = (ToggleButton) e.getSource();
                if (button.getUserData() == null) return;
                int index = (int) button.getUserData();
                if (tableFields.length <= index || tableFields[index] == null) return;

                if (button.isSelected()) {
                    button.setStyle("-fx-border-radius: 10; -fx-border-color: dodgerblue; -fx-background-color: lightblue; -fx-background-radius: 10;");
                    for (int j = 0; j < tableRows; j++) {
                        if (tableFields[index].length > j && tableFields[index][j] != null) {
                            // Use -fx-control-inner-background to color the cell while keeping the default border intact
                            tableFields[index][j].setStyle("-fx-background-radius: 2; -fx-control-inner-background: lightblue;");
                        }
                    }
                } else {
                    button.setStyle("-fx-border-radius: 10; -fx-border-color: lightblue; -fx-background-color: ghostwhite;");
                    for (int j = 0; j < tableRows; j++) {
                        if (tableFields[index].length > j && tableFields[index][j] != null) {
                            // Revert completely to the original style set in newSingleCharTextField
                            tableFields[index][j].setStyle("-fx-background-radius: 2;");
                        }
                    }
                }
            } catch (Exception ex) {}
        };


        formulaFocusListener = (ob, ov, nv) -> {
            if (isTornDown || this.mainView == null) return;
            try {
                if (nv && ob instanceof javafx.beans.property.ReadOnlyProperty) {
                    Object bean = ((javafx.beans.property.ReadOnlyProperty<?>) ob).getBean();
                    if (bean instanceof RichTextArea) {
                        BoxedDRTA box = (BoxedDRTA) ((RichTextArea) bean).getUserData();
                        if (box != null && box.getDRTA() != null) {
                            mainView.editorInFocus(box.getDRTA(), ControlType.FIELD);
                            focusedBoxedDRTA = box;
                        }
                    }
                }
            } catch (Exception e) {}
        };

        commentNodesWidthListener = (observable, oldValue, newValue) -> {
            if (isTornDown || this.mainView == null) return;
            Platform.runLater(() -> {
                if (isTornDown || this.mainView == null) return;
                try {
                    if (tableGrid != null) tableGrid.requestLayout();
                } catch (Exception e) {}
            });
        };

        commentKeyFilter = e -> {
            if (isTornDown || this.mainView == null || e == null) return;
            if (e.getCode() == KeyCode.ENTER) e.consume();
        };

        commentFocusListener = (ob, ov, nv) -> {
            if (isTornDown || this.mainView == null) return;
            try {
                if (nv && ob instanceof javafx.beans.property.ReadOnlyProperty) {
                    Object bean = ((javafx.beans.property.ReadOnlyProperty<?>) ob).getBean();
                    if (bean instanceof RichTextArea) {
                        BoxedDRTA box = (BoxedDRTA) ((RichTextArea) bean).getUserData();
                        if (box != null && box.getDRTA() != null) {
                            mainView.editorInFocus(box.getDRTA(), ControlType.FIELD);
                        }
                    }
                }
            } catch (Exception e) {}
        };
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

    /**
     * Initialize table structure
     */
    public void updateTableGridFromTableItems() {
        if (isTornDown || this.mainView == null || tableGrid == null || tableHeadItemsList == null) return;

        // SCORCHED EARTH RULE 8: Safely sever existing nodes before clearing (Prevent CME)
        try {
            if (tableGrid.getChildren() != null) {
                List<Node> childrenCopy = new ArrayList<>(tableGrid.getChildren());
                for (Node n : childrenCopy) {
                    try { n.getClass().getMethod("cleanup").invoke(n); } catch (Exception e) {}
                    severFromParent(n);
                }
            }
            tableGrid.getChildren().clear();

            List<ColumnConstraints> gridColConstraints =  tableGrid.getColumnConstraints();
            gridColConstraints.clear();

            for (int i = 0; i < tableHeadItemsList.size(); i++) {
                TableHeadItem headItem = tableHeadItemsList.get(i);
                gridColConstraints.add(headItem.getColumnConstraints());

                if (!headItem.isBlankColumn()) {
                    TextFlow headFlow = headItem.getExpression();
                    headFlow.setStyle("-fx-border-color: black; -fx-border-width: 0 0 1 0;");
                    tableGrid.add(headFlow, i, 0);
                    for (int j = 0; j < tableRows; j++) {
                        if (tableFields != null && tableFields.length > i && tableFields[i] != null && tableFields[i][j] != null) {
                            tableGrid.add(tableFields[i][j], i, j + 2);
                        }
                    }
                    if (highlightButtons != null && highlightButtons.length > i && highlightButtons[i] != null) {
                        tableGrid.add(highlightButtons[i], i, tableRows + 3);
                        if (highlightButtons[i].isSelected()) {
                            highlightButtons[i].setStyle("-fx-border-radius: 10; -fx-border-color: dodgerblue; -fx-background-color: lightblue; -fx-background-radius: 10");
                            for (int j = 0; j < tableRows; j++) {
                                if (tableFields != null && tableFields.length > i && tableFields[i] != null && tableFields[i][j] != null) {
                                    tableFields[i][j].setStyle("-fx-background-radius: 2; -fx-control-inner-background: lightblue");
                                }
                            }
                        }
                    }

                } else if (headItem.isDividerColumn()) {
                    TextFlow headFlow = headItem.getExpression();
                    headFlow.setStyle("-fx-border-color: black; -fx-border-width: 0 0 1 1");
                    tableGrid.add(headFlow, i, 0);

                    for (int j = 0; j < tableRows + 3; j++) {
                        Pane dividerPane = new Pane();
                        dividerPane.setStyle("-fx-border-color: black; -fx-border-width: 0 0 0 1");
                        tableGrid.add(dividerPane, i, j + 1);
                    }
                } else if (headItem.isBlankColumn()) {
                    TextFlow headFlow = headItem.getExpression();
                    headFlow.setStyle("-fx-border-color: black; -fx-border-width: 0 0 1 0");
                    tableGrid.add(headFlow, i, 0);
                }
            }

            ColumnConstraints commentConstraints = new ColumnConstraints();
            commentConstraints.setMinWidth(100);
            gridColConstraints.add(commentConstraints);

            endPane = new Pane();
            endPane.setStyle("-fx-border-color: black; -fx-border-width: 0 0 1 0");
            tableGrid.add(endPane, tableHeadItemsList.size(), 0);
            for (int j = 0; j < tableRows; j++) {
                if (rowCommentsArray != null && rowCommentsArray.length > j && rowCommentsArray[j] != null && rowCommentsArray[j].getBoxedRTA() != null) {
                    if (j != 0 && j % 4 == 0) {
                        rowCommentsArray[j].getBoxedRTA().setAlignment(Pos.BOTTOM_LEFT);
                        rowCommentsArray[j].getBoxedRTA().setPadding(new Insets(0,0,1.5,0));
                    }
                    tableGrid.add(rowCommentsArray[j].getBoxedRTA(), tableHeadItemsList.size(), j + 2);
                }
            }

            sizers = new VBox[tableRows + 3];

            for (int i = 0; i < tableRows + 3; i++) {
                sizers[i] = new VBox();
                tableGrid.add(sizers[i], tableHeadItemsList.size() + 1, i);
            }

            //setup blank separator row gaps
            List<RowConstraints> tableRowConstraints = tableGrid.getRowConstraints();
            tableRowConstraints.clear();
            tableRowConstraints.add(new RowConstraints());
            tableRowConstraints.add(new RowConstraints(5));
            for (int i = 0; i < tableRows; i++) {
                RowConstraints rowCon = new RowConstraints();
                if (i != 0 && i % 4 == 0) {
                    rowCon.setMinHeight(30);
                    rowCon.setMaxHeight(30);
                    rowCon.setValignment(VPos.BOTTOM);
                }
                else {
                    rowCon.setMinHeight(25);
                    rowCon.setMaxHeight(25);
                }
                tableRowConstraints.add(rowCon);
            }
            tableRowConstraints.add(new RowConstraints(5));
            tableRowConstraints.add(new RowConstraints());
            tableGrid.requestFocus();
        } catch (Exception e) {}
    }

    /**
     * Update the (left-hand) basic formulas pane from the basicFormulasBoxedDRTAList
     */
    public void updateBasicFormulasPaneFromList() {
        if (isTornDown || this.mainView == null || basicFormulasPane == null || basicFormulasBoxedDRTAs == null) return;
        try {
            // SCORCHED EARTH RULE 8: Rebuild Fix
            if (basicFormulasPane.getChildren() != null) {
                List<Node> childrenCopy = new ArrayList<>(basicFormulasPane.getChildren());
                for (Node n : childrenCopy) {
                    try { n.getClass().getMethod("cleanup").invoke(n); } catch (Exception e) {}
                    severFromParent(n);
                }
            }
            basicFormulasPane.getChildren().clear();

            for (int i = 0; i < basicFormulasBoxedDRTAs.size(); i++) {
                if (basicFormulasBoxedDRTAs.get(i) != null && basicFormulasBoxedDRTAs.get(i).getBoxedRTA() != null) {
                    basicFormulasPane.add(basicFormulasBoxedDRTAs.get(i).getBoxedRTA(),0, i);
                }
            }
        } catch (Exception e) {}
    }

    /**
     * Update the (left-hand) main formulas pane from the mainFormulasBoxedDRTA list
     */
    public void updateMainFormulasPaneFromList() {
        if (isTornDown || this.mainView == null || mainFormulasPane == null || mainFormulasBoxedDRTAs == null) return;
        try {
            // SCORCHED EARTH RULE 8: Rebuild Fix
            if (mainFormulasPane.getChildren() != null) {
                List<Node> childrenCopy = new ArrayList<>(mainFormulasPane.getChildren());
                for (Node n : childrenCopy) {
                    try { n.getClass().getMethod("cleanup").invoke(n); } catch (Exception e) {}
                    severFromParent(n);
                }
            }
            mainFormulasPane.getChildren().clear();

            for (int i = 0; i < mainFormulasBoxedDRTAs.size(); i++) {
                if (mainFormulasBoxedDRTAs.get(i) != null && mainFormulasBoxedDRTAs.get(i).getBoxedRTA() != null) {
                    mainFormulasPane.add(mainFormulasBoxedDRTAs.get(i).getBoxedRTA(), 0, i);
                }
            }
        } catch (Exception e) {}
    }

    /**
     * New character field for colum and row
     * @param column the column
     * @param row the row
     * @return the text field
     */
    TextField newSingleCharTextField(int column, int row) {
        TextField singleCharField = new TextField() {
            @Override
            public void replaceText(int start, int end, String text) {
                if (isTornDown || mainView == null) return;
                super.replaceText(start, end, text.toUpperCase());
            }
        };

        singleCharField.setPadding(new Insets(0));
        singleCharField.setPrefWidth(15);
        singleCharField.setAlignment(Pos.CENTER);
        singleCharField.setStyle("-fx-background-radius: 2");
        singleCharField.setPadding(new Insets(3));
        singleCharField.setUserData(new int[]{column, row});

        TextFormatter<String> formatter = new TextFormatter<>(singleCharTextFilter);
        singleCharField.setTextFormatter(formatter);
        singleCharField.addEventFilter(KeyEvent.KEY_TYPED, singleCharTypeFilter);
        singleCharField.addEventFilter(KeyEvent.KEY_PRESSED, singleCharPressFilter);

        return singleCharField;
    }

    /**
     * First non-blank column to the right of given column
     */
    private int tableColToRight(int column) {
        if (tableHeadItemsList == null) return column;
        int rightCol = column;
        for (int i = column + 1; i < tableHeadItemsList.size(); i++) {
            TableHeadItem item = tableHeadItemsList.get(i);
            if (!item.isBlankColumn()) {
                rightCol = i;
                break;
            }
        }
        return rightCol;
    }

    /**
     * First non-blank column to left of given column
     */
    private int tableColToLeft(int column) {
        if (tableHeadItemsList == null) return column;
        int leftCol = column;
        for (int i = column - 1; i >= 0; i--) {
            TableHeadItem item = tableHeadItemsList.get(i);
            if (!item.isBlankColumn()) {
                leftCol = i;
                break;
            }
        }
        return leftCol;
    }

    /**
     * Highlight button for column at index
     */
    ToggleButton newHighlightButton(int index) {
        ToggleButton button = new ToggleButton();
        button.setPadding(new Insets(0));
        button.setPrefWidth(20);
        button.setStyle("-fx-border-radius: 10; -fx-border-color: lightblue; -fx-background-color: ghostwhite");
        button.setUserData(index);
        button.setOnAction(highlightButtonHandler);
        return button;
    }

    /**
     * Boxed DRTA for formula content (as basic formula, or table head item)
     */
    public BoxedDRTA newFormulaBoxedDRTA() {
        BoxedDRTA bdrta = new BoxedDRTA();
        DecoratedRTA drta = bdrta.getDRTA();
        drta.getKeyboardSelector().valueProperty().setValue(RichTextAreaSkin.KeyMapValue.ITALIC_AND_SANS);
        RichTextArea rta = bdrta.getRTA();
        rta.setMaxHeight(27);
        rta.setMinHeight(27);
        rta.setContentAreaWidth(500);
        rta.setMinWidth(150);
        rta.getStylesheets().add("RichTextFieldWide.css");
        rta.setPromptText("Formula");
        rta.setUserData(bdrta);
        rta.focusedProperty().addListener(formulaFocusListener);
        return bdrta;
    }

    /**
     * Boxed DRTA for a comment field
     */
    public BoxedDRTA newCommentBoxedDRTA() {
        BoxedDRTA bdrta = new BoxedDRTA();
        RichTextArea rta = bdrta.getRTA();
        rta.setMaxHeight(formulaBoxHeight);
        rta.setMinHeight(formulaBoxHeight);
        rta.setUserData(bdrta);

        RichTextAreaSkin rtaSkin = (RichTextAreaSkin) rta.getSkin();
        rta.prefWidthProperty().bind(Bindings.max(Bindings.add(rtaSkin.nodesWidthProperty(), 8), 100));
        rtaSkin.nodesWidthProperty().addListener(commentNodesWidthListener);
        rta.addEventFilter(KeyEvent.ANY, commentKeyFilter);
        rta.getStylesheets().add("RichTextField.css");
        rta.focusedProperty().addListener(commentFocusListener);
        return bdrta;
    }

    /**
     * Setup size controls for statement, comment, interpretation, explain, and table grid
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

        statementRTA.prefWidthProperty().bind(Bindings.add(localScalePageWidth, RTA_WIDTH_BMP));
        statementRTA.maxWidthProperty().bind(Bindings.add(localScalePageWidth, RTA_WIDTH_BMP));
        statementRTA.contentAreaWidthProperty().bind(localScalePageWidth);

        statementWidthSpinner = new Spinner<>(0.0, 999.0, 100, 1.0);
        statementWidthSpinner.setPrefWidth(65);
        statementWidthSpinner.setDisable(true);
        statementWidthSpinner.setTooltip(new Tooltip("Window width as % of selected paper"));

        //comment
        RichTextArea commentRTA = exerciseComment.getEditor();
        commentRTA.getStylesheets().add("slappTextArea.css");
        commentRTA.setPromptText("Comment");

        double commentInitialHeight = Math.round((commentPrefHeight - RTA_SCREEN_BMP) / localScalePageHeight.get() * 100.0);
        commentHeightSpinner = new Spinner<>(spinnerMin, 999.0, commentInitialHeight, 1.0);
        commentHeightSpinner.setPrefWidth(65);
        commentHeightSpinner.setDisable(false);
        commentHeightSpinner.setTooltip(new Tooltip("Window height as % of selected paper"));
        commentRTA.prefHeightProperty().bind(Bindings.add(Bindings.max(45.0, Bindings.multiply(localScalePageHeight, DoubleProperty.doubleProperty(commentHeightSpinner.getValueFactory().valueProperty()).divide(100.0))), RTA_SCREEN_BMP));

        commentRTA.prefWidthProperty().bind(Bindings.add(localScalePageWidth, RTA_WIDTH_BMP));
        commentRTA.maxWidthProperty().bind(Bindings.add(localScalePageWidth, RTA_WIDTH_BMP));
        commentRTA.contentAreaWidthProperty().bind(localScalePageWidth);

        commentWidthSpinner = new Spinner<>(0.0, 999.0, 100, 1.0);
        commentWidthSpinner.setPrefWidth(65);
        commentWidthSpinner.setDisable(true);
        commentWidthSpinner.setTooltip(new Tooltip("Window width as % of selected paper"));

        //interpretation
        RichTextArea interpretationRTA = interpretationDRTA.getEditor();
        interpretationRTA.getStylesheets().add("slappTextArea.css");
        interpretationRTA.setPromptText(interpretationPrompt);

        double interpretationInitialHeight = Math.round((interpretationPrefHeight - RTA_SCREEN_BMP) / localScalePageHeight.get() * 100.0 );
        interpretationHeightSpinner = new Spinner<>(spinnerMin, 999.0, interpretationInitialHeight, 1.0);
        interpretationHeightSpinner.setPrefWidth(65);
        interpretationHeightSpinner.setDisable(false);
        interpretationHeightSpinner.setTooltip(new Tooltip("Window height as % of selected paper"));
        interpretationRTA.prefHeightProperty().bind(Bindings.add(Bindings.max(45.0, Bindings.multiply(localScalePageHeight, DoubleProperty.doubleProperty(interpretationHeightSpinner.getValueFactory().valueProperty()).divide(100.0))), RTA_SCREEN_BMP));

        interpretationRTA.prefWidthProperty().bind(Bindings.add(localScalePageWidth, RTA_WIDTH_BMP));
        interpretationRTA.maxWidthProperty().bind(Bindings.add(localScalePageWidth, RTA_WIDTH_BMP));
        interpretationRTA.contentAreaWidthProperty().bind(localScalePageWidth);

        interpretationWidthSpinner = new Spinner<>(0.0, 999.0, 100, 1.0);
        interpretationWidthSpinner.setPrefWidth(65);
        interpretationWidthSpinner.setDisable(true);
        interpretationWidthSpinner.setTooltip(new Tooltip("Window width as % of selected paper"));

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

        explainRTA.prefWidthProperty().bind(Bindings.add(localScalePageWidth, RTA_WIDTH_BMP));
        explainRTA.maxWidthProperty().bind(Bindings.add(localScalePageWidth, RTA_WIDTH_BMP));
        explainRTA.contentAreaWidthProperty().bind(localScalePageWidth);

        explainWidthSpinner = new Spinner<>(0.0, 999.0, 100, 1.0);
        explainWidthSpinner.setPrefWidth(65);
        explainWidthSpinner.setDisable(true);
        explainWidthSpinner.setTooltip(new Tooltip("Window width as % of selected paper"));

        //table grid
        tableGridHeightSpinner = new Spinner<>(0.0,999.0, 0,1.0);
        tableGridHeightSpinner.setPrefWidth(65);
        tableGridHeightSpinner.setDisable(true);
        tableGridHeightSpinner.setTooltip(new Tooltip("Window height as % of selected paper"));
        tableGridHeightListener = (ob, ov, nv) -> {
            if (isTornDown || this.mainView == null || tableGridHeightSpinner == null || tableGrid == null) return;
            try {
                if (tableGridHeightSpinner.getValueFactory() != null) tableGridHeightSpinner.getValueFactory().setValue((double) Math.round(tableGrid.getHeight() / localScalePageHeight.get() * 100));
            } catch (Exception e) {}
        };
        tableGrid.heightProperty().addListener(tableGridHeightListener);

        tableGridWidthSpinner = new Spinner<>(0.0,999.0, 0,1.0);
        tableGridWidthSpinner.setPrefWidth(65);
        tableGridWidthSpinner.setDisable(true);
        tableGridWidthSpinner.setTooltip(new Tooltip("Window width as % of selected paper"));
        tableGridWidthListener = (ob, ov, nv) -> {
            if (isTornDown || this.mainView == null || tableGridWidthSpinner == null || tableGrid == null) return;
            try {
                if (tableGridWidthSpinner.getValueFactory() != null) tableGridWidthSpinner.getValueFactory().setValue((double) Math.round((tableGrid.getWidth() - RTA_WIDTH_BMP) / localScalePageWidth.get() * 100));
            } catch (Exception e) {}
        };
        tableGrid.widthProperty().addListener(tableGridWidthListener);

        //choices (null spinners)
        choicesHeightSpinner = new Spinner<>(0.0, 999.0, 0, 1.0);
        choicesHeightSpinner.setPrefWidth(65);
        choicesHeightSpinner.setDisable(true);
        choicesHeightSpinner.setTooltip(new Tooltip("Window height as % of selected paper"));

        choiceBox.maxWidthProperty().bind(Bindings.add(localScalePageWidth, RTA_WIDTH_BMP));
        choicesWidthSpinner = new Spinner<>(0.0, 999.0, 100.0, 1.0);
        choicesWidthSpinner.setPrefWidth(65);
        choicesWidthSpinner.setDisable(true);
        choicesWidthSpinner.setTooltip(new Tooltip("Window width as % of selected paper"));

        //page size listeners
        localScalePageHeightListener = (ob, ov, nv) -> {
            if (isTornDown || this.mainView == null || exerciseStatement == null || exerciseComment == null || explainDRTA == null || interpretationDRTA == null) return;
            try {
                double revSpinnerMin = (double) ((int) (100 * 45.0 / (double) nv));

                RichTextArea statementRTAUnbind = exerciseStatement.getEditor();
                statementRTAUnbind.prefHeightProperty().unbind();
                if (statementHeightSpinner != null && statementHeightSpinner.getValueFactory() != null) {
                    statementHeightSpinner.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(revSpinnerMin, 999.0, (double) Math.round(statementHeightSpinner.getValue() * ov.doubleValue() / nv.doubleValue()), 1.0));
                    statementRTAUnbind.prefHeightProperty().bind(Bindings.add(Bindings.max(45.0, Bindings.multiply(nv.doubleValue(), DoubleProperty.doubleProperty(statementHeightSpinner.getValueFactory().valueProperty()).divide(100.0))), RTA_SCREEN_BMP));
                }

                RichTextArea commentRTAUnbind = exerciseComment.getEditor();
                commentRTAUnbind.prefHeightProperty().unbind();
                if (commentHeightSpinner != null && commentHeightSpinner.getValueFactory() != null) {
                    commentHeightSpinner.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(revSpinnerMin, 999.0, (double) Math.round(commentHeightSpinner.getValue() * ov.doubleValue() / nv.doubleValue()), 1.0));
                    commentRTAUnbind.prefHeightProperty().bind(Bindings.add(Bindings.max(45.0, Bindings.multiply(nv.doubleValue(), DoubleProperty.doubleProperty(commentHeightSpinner.getValueFactory().valueProperty()).divide(100.0))), RTA_SCREEN_BMP));
                }

                RichTextArea explainRTAUnbind = explainDRTA.getEditor();
                explainRTAUnbind.prefHeightProperty().unbind();
                if (explainHeightSpinner != null && explainHeightSpinner.getValueFactory() != null) {
                    explainHeightSpinner.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(revSpinnerMin, 999.0, (double) Math.round(explainHeightSpinner.getValue() * ov.doubleValue() / nv.doubleValue()), 1.0));
                    explainRTAUnbind.prefHeightProperty().bind(Bindings.add(Bindings.max(45.0, Bindings.multiply(nv.doubleValue(), DoubleProperty.doubleProperty(explainHeightSpinner.getValueFactory().valueProperty()).divide(100.0))), RTA_SCREEN_BMP));
                }

                RichTextArea interpretationRTAUnbind = interpretationDRTA.getEditor();
                interpretationRTAUnbind.prefHeightProperty().unbind();
                if (interpretationHeightSpinner != null && interpretationHeightSpinner.getValueFactory() != null) {
                    interpretationHeightSpinner.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(revSpinnerMin, 999.0, (double) Math.round(interpretationHeightSpinner.getValue() * ov.doubleValue() / nv.doubleValue()), 1.0));
                    interpretationRTAUnbind.prefHeightProperty().bind(Bindings.add(Bindings.max(45.0, Bindings.multiply(nv.doubleValue(), DoubleProperty.doubleProperty(interpretationHeightSpinner.getValueFactory().valueProperty()).divide(100.0))), RTA_SCREEN_BMP));
                }

                if (tableGridHeightSpinner != null && tableGridHeightSpinner.getValueFactory() != null && tableGrid != null) {
                    tableGridHeightSpinner.getValueFactory().setValue((double) Math.round(tableGrid.getHeight() / localScalePageHeight.get() * 100.0));
                }
                if (choicesHeightSpinner != null && choicesHeightSpinner.getValueFactory() != null && choiceBox != null) {
                    choicesHeightSpinner.getValueFactory().setValue((double) Math.round(choiceBox.getHeight() / localScalePageHeight.get() * 100.0));
                }
            } catch (Exception e) {}
        };
        localScalePageHeight.addListener(localScalePageHeightListener);

        localScalePageWidthListener = (ob, ov, nv) -> {
            if (isTornDown || this.mainView == null || tableGridWidthSpinner == null || tableGrid == null) return;
            try {
                if (tableGridWidthSpinner.getValueFactory() != null) {
                    tableGridWidthSpinner.getValueFactory().setValue((double) Math.round((tableGrid.getWidth() - RTA_WIDTH_BMP) / localScalePageWidth.get() * 100.0));
                }
            } catch (Exception e) {}
        };
        localScalePageWidth.addListener(localScalePageWidthListener);

        setSizeSpinners();
    }

    public void setRightControlBox() {
        bigCheck = new Text("\ue89a");
        bigCheck.setFont(Font.font("Noto Serif Combo", 72));

        checkedElements = new Text("Choice Only");
        checkedElements.setFont(Font.font("Noto Serif Combo", 11));
        TextFlow checkedElementsFlow = new TextFlow(checkedElements);
        checkedElementsFlow.setMaxWidth(150);

        VBox bigCheckBox = new VBox(0, bigCheck, checkedElementsFlow);
        bigCheckBox.setAlignment(Pos.CENTER);
        checkedElementsFlow.setTextAlignment(TextAlignment.CENTER);

        checkButton = new Button("Check");
        checkButton.setPrefWidth(105);
        checkButton.setTooltip(new Tooltip("Check exercise for correctness."));
        checkTriesLabel = new Label();

        VBox checksBox = new VBox(10, checkButton, checkTriesLabel);
        checksBox.setAlignment(Pos.CENTER);
        checkTriesLabel.setAlignment(Pos.CENTER);

        staticHelpButton = new Button("Static Help");
        staticHelpButton.setPrefWidth(105);
        staticHelpButton.setTooltip(new Tooltip("Get static help text"));

        shortTableCheck = new CheckBox("Short Table");

        VBox rightControlBox = new VBox(40, bigCheckBox, checksBox, staticHelpButton, shortTableCheck);
        rightControlBox.setAlignment(Pos.TOP_CENTER);
        rightControlBox.setPadding(new Insets(40,20,0,20));
        rightControlNode = rightControlBox;
        deactivateBigCheck();
    }

    public void activateBigCheck() {
        if (bigCheck == null || checkedElements == null) return;
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
        } else {
            bigCheck.setOpacity(1.0);
            checkedElements.setOpacity(1.0);
        }
    }

    public void deactivateBigCheck() {
        if (bigCheck == null || checkedElements == null) return;
        if (bigCheck.getOpacity() > .5) checkShowing = true;
        else checkShowing = false;
        bigCheck.setOpacity(0.0);
        checkedElements.setOpacity(0.0);
    }

    public void showStaticHelp(Document doc) {
        if (isTornDown || mainView == null) return;
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

    private void setSizeSpinners() {
        tableGridClickHandler = e -> {
            if (isTornDown || this.mainView == null) return;
            try {
                if (currentSpinnerNode != tableGrid) {
                    tableGrid.requestFocus();
                }
            } catch (Exception ex) {}
        };
        tableGrid.addEventFilter(MouseEvent.MOUSE_CLICKED, tableGridClickHandler);

        choiceBoxClickHandler = e -> {
            if (isTornDown || this.mainView == null ) return;
            try {
                if (currentSpinnerNode != choiceBox) {
                    choiceBox.requestFocus();
                }
            } catch (Exception ex) {}
        };
        choiceBox.addEventFilter(MouseEvent.MOUSE_CLICKED, choiceBoxClickHandler);

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


    public void cleanup() {
        // KILL SWITCH: Engaged! Prevent any focus listeners from doing UI operations mid-teardown
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

        // SCORCHED EARTH RULE 5: Rule of Isolation applied via dozens of tiny isolated try-catch blocks
        try { if (tableGrid != null && tableGridClickHandler != null) tableGrid.removeEventFilter(MouseEvent.MOUSE_CLICKED, tableGridClickHandler); } catch (Exception e) {}
        try { if (choiceBox != null && choiceBoxClickHandler != null) choiceBox.removeEventFilter(MouseEvent.MOUSE_CLICKED, choiceBoxClickHandler); } catch (Exception e) {}

        try {
            if (mainView != null && mainView.getMainScene() != null && exerciseFocusListener != null) {
                mainView.getMainScene().focusOwnerProperty().removeListener(exerciseFocusListener);
            }
        } catch (Exception e) {}

        try { if (basicFormulasPane != null && basicFormulasKeyFilter != null) basicFormulasPane.removeEventFilter(KeyEvent.KEY_PRESSED, basicFormulasKeyFilter); } catch (Exception e) {}
        try { if (mainFormulasPane != null && mainFormulasKeyFilter != null) mainFormulasPane.removeEventFilter(KeyEvent.KEY_PRESSED, mainFormulasKeyFilter); } catch (Exception e) {}

        try {
            if (tableGrid != null && tableGridHeightListener != null) tableGrid.heightProperty().removeListener(tableGridHeightListener);
            if (tableGrid != null && tableGridWidthListener != null) tableGrid.widthProperty().removeListener(tableGridWidthListener);
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

        // Close Popups
        try { if (staticHelpStage != null) staticHelpStage.close(); } catch (Exception e) {}

        // Unbind RTA properties synchronously
        try {
            if (exerciseStatement != null && exerciseStatement.getEditor() != null) {
                exerciseStatement.getEditor().prefHeightProperty().unbind();
                exerciseStatement.getEditor().prefWidthProperty().unbind();
                exerciseStatement.getEditor().maxWidthProperty().unbind();
                exerciseStatement.getEditor().contentAreaWidthProperty().unbind();
            }
        } catch (Exception e) {}
        try {
            if (exerciseComment != null && exerciseComment.getEditor() != null) {
                exerciseComment.getEditor().prefHeightProperty().unbind();
                exerciseComment.getEditor().prefWidthProperty().unbind();
                exerciseComment.getEditor().maxWidthProperty().unbind();
                exerciseComment.getEditor().contentAreaWidthProperty().unbind();
            }
        } catch (Exception e) {}
        try {
            if (interpretationDRTA != null && interpretationDRTA.getEditor() != null) {
                interpretationDRTA.getEditor().prefHeightProperty().unbind();
                interpretationDRTA.getEditor().prefWidthProperty().unbind();
                interpretationDRTA.getEditor().maxWidthProperty().unbind();
                interpretationDRTA.getEditor().contentAreaWidthProperty().unbind();
            }
        } catch (Exception e) {}
        try {
            if (explainDRTA != null && explainDRTA.getEditor() != null) {
                explainDRTA.getEditor().prefHeightProperty().unbind();
                explainDRTA.getEditor().prefWidthProperty().unbind();
                explainDRTA.getEditor().maxWidthProperty().unbind();
                explainDRTA.getEditor().contentAreaWidthProperty().unbind();
            }
        } catch (Exception e) {}

        try { if (choiceBox != null) choiceBox.maxWidthProperty().unbind(); } catch (Exception e) {}

        try {
            if (rowCommentsArray != null) {
                for (BoxedDRTA bdrta : rowCommentsArray) {
                    if (bdrta != null && bdrta.getRTA() != null) {
                        bdrta.getRTA().prefWidthProperty().unbind();
                        if (commentFocusListener != null) bdrta.getRTA().focusedProperty().removeListener(commentFocusListener);
                        if (commentKeyFilter != null) bdrta.getRTA().removeEventFilter(KeyEvent.ANY, commentKeyFilter);
                        try {
                            RichTextAreaSkin rtaSkin = (RichTextAreaSkin) bdrta.getRTA().getSkin();
                            if (rtaSkin != null && commentNodesWidthListener != null) rtaSkin.nodesWidthProperty().removeListener(commentNodesWidthListener);
                        } catch(Exception ex) {}
                        bdrta.getRTA().setUserData(null);
                    }
                }
            }
        } catch (Exception e) {}

        try {
            if (basicFormulasBoxedDRTAs != null) {
                for (BoxedDRTA bdrta : basicFormulasBoxedDRTAs) {
                    if (bdrta != null && bdrta.getRTA() != null) {
                        if (formulaFocusListener != null) bdrta.getRTA().focusedProperty().removeListener(formulaFocusListener);
                        bdrta.getRTA().setUserData(null);
                    }
                }
            }
        } catch (Exception e) {}

        try {
            if (mainFormulasBoxedDRTAs != null) {
                for (BoxedDRTA bdrta : mainFormulasBoxedDRTAs) {
                    if (bdrta != null && bdrta.getRTA() != null) {
                        if (formulaFocusListener != null) bdrta.getRTA().focusedProperty().removeListener(formulaFocusListener);
                        bdrta.getRTA().setUserData(null);
                    }
                }
            }
        } catch (Exception e) {}

        try {
            if (tableFields != null) {
                for (int i = 0; i < tableFields.length; i++) {
                    if (tableFields[i] != null) {
                        for (int j = 0; j < tableFields[i].length; j++) {
                            if (tableFields[i][j] != null) {
                                if (singleCharTypeFilter != null) tableFields[i][j].removeEventFilter(KeyEvent.KEY_TYPED, singleCharTypeFilter);
                                if (singleCharPressFilter != null) tableFields[i][j].removeEventFilter(KeyEvent.KEY_PRESSED, singleCharPressFilter);
                                tableFields[i][j].setTextFormatter(null);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {}

        try {
            if (highlightButtons != null) {
                for (int i = 0; i < highlightButtons.length; i++) {
                    if (highlightButtons[i] != null) highlightButtons[i].setOnAction(null);
                }
            }
        } catch (Exception e) {}

        // ---> SCORCHED EARTH RULE 4: THE JAVAFX SKIN HAMMER <---
        try {
            final RichTextArea stEditor = (exerciseStatement != null) ? exerciseStatement.getEditor() : null;
            final RichTextArea commEditor = (exerciseComment != null) ? exerciseComment.getEditor() : null;
            final RichTextArea interpEditor = (interpretationDRTA != null) ? interpretationDRTA.getEditor() : null;
            final RichTextArea expEditor = (explainDRTA != null) ? explainDRTA.getEditor() : null;

            final List<RichTextArea> rtasToClear = new ArrayList<>();
            if (stEditor != null) rtasToClear.add(stEditor);
            if (commEditor != null) rtasToClear.add(commEditor);
            if (interpEditor != null) rtasToClear.add(interpEditor);
            if (expEditor != null) rtasToClear.add(expEditor);

            if (basicFormulasBoxedDRTAs != null) {
                for (BoxedDRTA bdrta : basicFormulasBoxedDRTAs) if (bdrta != null && bdrta.getRTA() != null) rtasToClear.add(bdrta.getRTA());
            }
            if (mainFormulasBoxedDRTAs != null) {
                for (BoxedDRTA bdrta : mainFormulasBoxedDRTAs) if (bdrta != null && bdrta.getRTA() != null) rtasToClear.add(bdrta.getRTA());
            }
            if (rowCommentsArray != null) {
                for (BoxedDRTA bdrta : rowCommentsArray) if (bdrta != null && bdrta.getRTA() != null) rtasToClear.add(bdrta.getRTA());
            }

            if (!rtasToClear.isEmpty()) {
                for (RichTextArea rta : rtasToClear) {
                    try {
                        if (rta.prefHeightProperty() != null) rta.prefHeightProperty().unbind();
                        if (rta.minWidthProperty() != null) rta.minWidthProperty().unbind();
                        if (rta.maxWidthProperty() != null) rta.maxWidthProperty().unbind();
                    } catch (Exception ex) {}
                }

                // STAGE 1 (Immediate): Prompt wipe, focus disable, and document injection
                for (RichTextArea rta : rtasToClear) {
                    if (rta != null) {
                        try {
                            rta.setPromptText("");
                        } catch (Exception e) {}
                        try {
                            rta.setFocusTraversable(false);
                        } catch (Exception e) {}
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

        // ---> 4. SCORCHED EARTH RULE 8: REBUILD FIX <---
        try {
            if (basicFormulasPane != null) {
                if (basicFormulasPane.getChildren() != null) {
                    List<Node> childrenCopy = new ArrayList<>(basicFormulasPane.getChildren());
                    for (Node node : childrenCopy) severFromParent(node);
                }
                basicFormulasPane.getChildren().clear();
            }
        } catch (Exception e) {}

        try {
            if (mainFormulasPane != null) {
                if (mainFormulasPane.getChildren() != null) {
                    List<Node> childrenCopy = new ArrayList<>(mainFormulasPane.getChildren());
                    for (Node node : childrenCopy) severFromParent(node);
                }
                mainFormulasPane.getChildren().clear();
            }
        } catch (Exception e) {}

        try {
            if (controlBox != null) {
                if (controlBox.getChildren() != null) {
                    List<Node> childrenCopy = new ArrayList<>(controlBox.getChildren());
                    for (Node node : childrenCopy) severFromParent(node);
                }
                controlBox.getChildren().clear();
            }
        } catch (Exception e) {}

        try {
            if (tableGrid != null) {
                if (tableGrid.getChildren() != null) {
                    List<Node> childrenCopy = new ArrayList<>(tableGrid.getChildren());
                    for (Node node : childrenCopy) severFromParent(node);
                }
                tableGrid.getChildren().clear();
                tableGrid.getRowConstraints().clear();
                tableGrid.getColumnConstraints().clear();
            }
        } catch (Exception e) {}

        try {
            if (resultsBox != null) {
                if (resultsBox.getChildren() != null) {
                    List<Node> childrenCopy = new ArrayList<>(resultsBox.getChildren());
                    for (Node node : childrenCopy) severFromParent(node);
                }
                resultsBox.getChildren().clear();
            }
        } catch (Exception e) {}

        try {
            if (centerBox != null) {
                if (centerBox.getChildren() != null) {
                    List<Node> childrenCopy = new ArrayList<>(centerBox.getChildren());
                    for (Node node : childrenCopy) severFromParent(node);
                }
                centerBox.getChildren().clear();
            }
        } catch (Exception e) {}

        try {
            if (choiceBox != null) {
                if (choiceBox.getChildren() != null) {
                    List<Node> childrenCopy = new ArrayList<>(choiceBox.getChildren());
                    for (Node node : childrenCopy) severFromParent(node);
                }
                choiceBox.getChildren().clear();
            }
        } catch (Exception e) {}

        try { if (pointsEarnedTextField != null) pointsEarnedTextField.setTextFormatter(null); } catch (Exception e) {}

        try { if (addBasicFormulaButton != null) addBasicFormulaButton.setOnAction(null); } catch (Exception e) {}
        try { if (removeBasicFormulaButton != null) removeBasicFormulaButton.setOnAction(null); } catch (Exception e) {}
        try { if (addMainFormulaButton != null) addMainFormulaButton.setOnAction(null); } catch (Exception e) {}
        try { if (removeMainFormulaButton != null) removeMainFormulaButton.setOnAction(null); } catch (Exception e) {}
        try { if (setupTableButton != null) setupTableButton.setOnAction(null); } catch (Exception e) {}



        try { if (checkButton != null) checkButton.setOnAction(null); } catch (Exception e) {}
        try { if (staticHelpButton != null) staticHelpButton.setOnAction(null); } catch (Exception e) {}

        // Null all fields
        this.mainView = null;
        this.interpretationPrompt = null;
        this.exerciseStatement = null;
        this.exerciseComment = null;
        this.interpretationDRTA = null;
        this.explainDRTA = null;
        this.statementHeightSpinner = null;
        this.statementWidthSpinner = null;
        this.commentHeightSpinner = null;
        this.commentWidthSpinner = null;
        this.explainHeightSpinner = null;
        this.explainWidthSpinner = null;
        this.interpretationHeightSpinner = null;
        this.interpretationWidthSpinner = null;
        this.choicesHeightSpinner = null;
        this.choicesWidthSpinner = null;
        this.tableGridHeightSpinner = null;
        this.tableGridWidthSpinner = null;
        this.currentSpinnerNode = null;
        this.basicFormulasPane = null;

        // SCORCHED EARTH RULE 10: NON-DESTRUCTIVE Data Teardown
        try {
            if (this.basicFormulasBoxedDRTAs != null) {
                this.basicFormulasBoxedDRTAs.clear();
            }
            this.basicFormulasBoxedDRTAs = null;
        } catch (Exception e) {}

        this.mainFormulasPane = null;
        try {
            if (this.mainFormulasBoxedDRTAs != null) {
                this.mainFormulasBoxedDRTAs.clear();
            }
            this.mainFormulasBoxedDRTAs = null;
        } catch (Exception e) {}

        this.controlBox = null;
        this.rowsSpinner = null;
        this.setupTableButton = null;
        this.choiceLeadLabel = null;
        this.aCheckBox = null;
        this.bCheckBox = null;
        this.tableGrid = null;
        this.resultsBox = null;
        this.choiceBox = null;
        this.centerBox = null;
        this.explainPrompt = null;
        this.tableHeadItemsList = null;

        if (this.tableFields != null) {
            for (int i = 0; i < this.tableFields.length; i++) {
                if (this.tableFields[i] != null) {
                    for (int j = 0; j < this.tableFields[i].length; j++) {
                        if (this.tableFields[i][j] != null) {
                            this.tableFields[i][j].setOnAction(null);
                            this.tableFields[i][j].setOnKeyPressed(null);
                            this.tableFields[i][j].setOnKeyTyped(null);
                        }
                        this.tableFields[i][j] = null;
                    }
                }
                this.tableFields[i] = null;
            }
        }
        this.tableFields = null;

        if (this.rowCommentsArray != null) {
            for (int i = 0; i < this.rowCommentsArray.length; i++) this.rowCommentsArray[i] = null;
        }
        this.rowCommentsArray = null;

        if (this.highlightButtons != null) {
            for (int i = 0; i < this.highlightButtons.length; i++) {
                if (this.highlightButtons[i] != null) this.highlightButtons[i].setOnAction(null);
                this.highlightButtons[i] = null;
            }
        }
        this.highlightButtons = null;

        if (this.sizers != null) {
            for (int i = 0; i < this.sizers.length; i++) {
                if (this.sizers[i] != null) this.sizers[i].getChildren().clear();
                this.sizers[i] = null;
            }
        }
        this.sizers = null;
        this.endPane = null;
        this.focusedBoxedDRTA = null;
        this.pointsEarnedTextField = null;
        this.staticHelpButton = null;
        this.staticHelpStage = null;
        this.rightControlNode = null;
        this.checkButton = null;
        this.bigCheck = null;
        this.checkMessage = null;
        this.checkedElements = null;
        this.checkTriesLabel = null;
        this.checkColor = null;
        this.checkElementsColor = null;
        this.shortTableCheck = null;

        // Nullify listeners cleanly
        this.exerciseFocusListener = null;
        this.basicFormulasKeyFilter = null;
        this.addBasicFormulaHandler = null;
        this.removeBasicFormulaHandler = null;
        this.mainFormulasKeyFilter = null;
        this.addMainFormulaHandler = null;
        this.removeMainFormulaHandler = null;
        this.pointsFilter = null;
        this.tableGridHeightListener = null;
        this.tableGridWidthListener = null;
        this.localScalePageHeightListener = null;
        this.localScalePageWidthListener = null;
        this.tableGridClickHandler = null;
        this.choiceBoxClickHandler = null;
        this.commentKeyFilter = null;
        this.commentFocusListener = null;
        this.commentNodesWidthListener = null;
        this.formulaFocusListener = null;
        this.singleCharTextFilter = null;
        this.singleCharTypeFilter = null;
        this.singleCharPressFilter = null;
        this.highlightButtonHandler = null;

        this.addBasicFormulaButton = null;
        this.removeBasicFormulaButton = null;
        this.addMainFormulaButton = null;
        this.removeMainFormulaButton = null;

        this.pointsEarnedFocusListener = null;
        this.pointsEarnedClickHandler = null;

    }

    public Button getCheckButton() { return checkButton; }

    public Label getCheckTriesLabel() { return checkTriesLabel; }

    public void setCheckMessage(String checkMessage) { this.checkMessage = checkMessage; }

    public void setCheckColor(Color checkColor) { this.checkColor = checkColor; }

    public void setCheckElementsColor(Color checkElementsColor) { this.checkElementsColor = checkElementsColor; }

    public String getCheckMessage() { return checkMessage; }

    public Button getStaticHelpButton() { return staticHelpButton; }

    public Stage getStaticHelpStage() { return staticHelpStage; }

    GridPane getTableGrid() { return tableGrid; }

    void setTableRows(int tableRows) { this.tableRows = tableRows;  }

    void setTableFields(TextField[][] tableFields) {  this.tableFields = tableFields; }

    List<TableHeadItem> getTableHeadItemsList() { return tableHeadItemsList;  }

    void setTableHeadItemsList(List<TableHeadItem> tableHeadItemsList) {   this.tableHeadItemsList = tableHeadItemsList;  }

    void setRowCommentsArray(BoxedDRTA[] rowComments) {  this.rowCommentsArray = rowComments;   }

    void setHighlightButtons(ToggleButton[] highlightButtons) {   this.highlightButtons = highlightButtons; }

    BoxedDRTA[] getRowCommentsArray() { return rowCommentsArray; }

    ToggleButton[] getHighlightButtons() { return highlightButtons; }

    TextField[][] getTableFields() { return tableFields; }

    Button getSetupTableButton() { return setupTableButton; }

    List<BoxedDRTA> getBasicFormulasBoxedDRTAs() {return basicFormulasBoxedDRTAs; }

    List<BoxedDRTA> getMainFormulasBoxedDRTAs() {return mainFormulasBoxedDRTAs; }

    Spinner<Integer> getRowsSpinner() { return rowsSpinner;  }

    Label getChoiceLeadLabel() { return choiceLeadLabel; }

    CheckBox getaCheckBox() { return aCheckBox;  }

    CheckBox getbCheckBox() { return bCheckBox; }

    DecoratedRTA getExplainDRTA() {return explainDRTA; }

    void setExplainDRTA(DecoratedRTA explainDRTA) {    this.explainDRTA = explainDRTA;  }

    void setInterpretationDRTA(DecoratedRTA drta) { interpretationDRTA = drta; }

    DecoratedRTA getInterpretationDRTA() {return interpretationDRTA; }

    void setInterpretationPrompt(String interpretationPrompt) {    this.interpretationPrompt = interpretationPrompt; }

    void setExplainPrompt(String explainPrompt) {   this.explainPrompt = explainPrompt; }

    double getCommentPrefHeight() { return exerciseComment.getEditor().getPrefHeight();  }

    void setCommentPrefHeight(double commentPrefHeight) { this.commentPrefHeight = commentPrefHeight;  }

    double getInterpretationPrefHeight() { return interpretationDRTA.getEditor().getPrefHeight();  }

    void setInterpretationPrefHeight(double interpretationPrefHeight) { this.interpretationPrefHeight = interpretationPrefHeight;  }

    double getExplainPrefHeight() { return explainDRTA.getEditor().getPrefHeight();  }

    void setExplainPrefHeight(double explainPrefHeight) { this.explainPrefHeight = explainPrefHeight;  }

    Spinner<Double> getTableGridHeightSpinner() { return tableGridHeightSpinner;  }

    Spinner<Double> getTableGridWidthSpinner() { return tableGridWidthSpinner;  }

    public VBox getControlBox() { return controlBox; }

    public VBox getCenterBox() { return centerBox; }

    @Override
    public DecoratedRTA getExerciseComment() { return exerciseComment; }

    @Override
    public void setExerciseComment(DecoratedRTA comment) { exerciseComment = comment;  }

    @Override
    public DecoratedRTA getExerciseStatement() { return exerciseStatement;  }

    @Override
    public void setExerciseStatement(DecoratedRTA statement) { exerciseStatement = statement; }

    @Override
    public Node getExerciseStatementNode() { return exerciseStatement.getEditor();  }

    @Override
    public void setStatementPrefHeight(double height) {
        statementPrefHeight = height;
        exerciseStatement.getEditor().setPrefHeight(height);
    }

    @Override
    public Node getExerciseContentNode() {return centerBox; }

    @Override
    public Node getExerciseControl() {  return controlBox; }

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

    public void setPointsPossible(int pointsPossible) { this.pointsPossible = pointsPossible; }

    @Override
    public int getPointsPossible() { return pointsPossible;  }

    public TextField getPointsEarnedTextField() { return pointsEarnedTextField; }

    public CheckBox getShortTableCheck() { return shortTableCheck; }

    public DoubleProperty localScalePageHeightProperty() { return localScalePageHeight; }

    public DoubleProperty localScalePageWidthProperty() { return localScalePageWidth; }

    public ChangeListener<Node> getExerciseFocusListener() { return exerciseFocusListener; }

    public void setCheckTrans(boolean checkTrans) {
        this.checkTrans = checkTrans;
        if (centerBox != null) {
            if (checkTrans) {
                centerBox.getChildren().setAll(interpretationDRTA.getEditor(), tableGrid, resultsBox);
            } else {
                centerBox.getChildren().setAll(tableGrid, resultsBox);
            }
        }
    }
}

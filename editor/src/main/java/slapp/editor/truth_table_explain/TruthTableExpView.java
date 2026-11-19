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

package slapp.editor.truth_table_explain;

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
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.geometry.VPos;
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
import javafx.scene.control.*;
import javafx.scene.layout.*;


public class TruthTableExpView implements ExerciseView<DecoratedRTA> {
    private MainWindowView mainView;
    private String explainPrompt = "";
    private DecoratedRTA exerciseStatement = new DecoratedRTA();
    private DecoratedRTA exerciseComment = new DecoratedRTA();
    private DecoratedRTA explainDRTA = new DecoratedRTA();
    private double statementPrefHeight = 0;
    private double commentPrefHeight = 0;
    private double explainPrefHeight = 0;
    private Spinner<Double> statementHeightSpinner;
    private Spinner<Double> statementWidthSpinner;
    private Spinner<Double> commentHeightSpinner;
    private Spinner<Double> commentWidthSpinner;
    private Spinner<Double> explainHeightSpinner;
    private Spinner<Double> explainWidthSpinner;
    private Spinner<Double> choicesHeightSpinner;
    private Spinner<Double> choicesWidthSpinner;
    private Spinner<Double> tableGridHeightSpinner;
    private Spinner<Double> tableGridWidthSpinner;
    private Node currentSpinnerNode;
    private GridPane basicFormulasPane;
    private List<BoxedDRTA> basicFormulasBoxedDRTAs;
    private VBox controlBox;
    private Spinner rowsSpinner;
    Button setupTableButton;
    private Button addBasicFormulaButton;
    private Button removeBasicFormulaButton;
    private Label choiceLeadLabel = new Label();
    private CheckBox aCheckBox = new CheckBox();
    private CheckBox bCheckBox = new CheckBox();
    private GridPane tableGrid;
    private VBox resultsBox;
    private HBox choiceBox;
    private VBox centerBox;
    private double formulaBoxHeight = 23;
    private List<TableHeadItem> tableHeadItemsList;
    private TextField[][]  tableFields; //list of text field columns
    private BoxedDRTA[] rowCommentsArray;
    private ToggleButton[] highlightButtons;
    private int tableRows = 0;
    private VBox[] sizers;
    private Pane endPane;
    BoxedDRTA focusedBoxedDRTA;
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
    private Stage  staticHelpStage;
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

    /**
     * Construct the truth table explain view
     * @param mainView the main window
     */
    TruthTableExpView(MainWindowView mainView) {
        this.mainView = mainView;

        localScalePageHeight.bind(mainView.scalePageHeightProperty());
        localScalePageWidth.bind(mainView.scalePageWidthProperty());

        exerciseFocusListener = (ob, ov, nv) -> {
            if (isTornDown || this.mainView == null ) return;

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
                        double choicesHeightValue = Math.round(choiceBox.getHeight() / localScalePageHeight.get() * 100);
                        if (choicesHeightSpinner != null && choicesHeightSpinner.getValueFactory() != null && choicesWidthSpinner != null) {
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

        Label basicFormulasLabel = new Label("Basic Sentences:");
        addBasicFormulaButton = new Button("+");
        removeBasicFormulaButton = new Button("-");
        addBasicFormulaButton.setFont(new Font(16));
        addBasicFormulaButton.setPadding(new Insets(0,5,0,5));
        removeBasicFormulaButton.setFont(new Font(16));
        removeBasicFormulaButton.setPadding(new Insets(1,8,1,8));

        HBox controlButtonBox = new HBox(20, addBasicFormulaButton, removeBasicFormulaButton);
        VBox upperControlBox = new VBox(10, basicFormulasLabel, controlButtonBox);

        // Pre-initialize class-level dynamic listeners
        initializeDynamicHandlers();

        basicFormulasBoxedDRTAs.add(newFormulaBoxedDRTA());
        updateBasicFormulasPaneFromList();

        basicFormulasKeyFilter = e -> {
            if (isTornDown || this.mainView == null || basicFormulasBoxedDRTAs == null) return;
            try {
                int index = basicFormulasBoxedDRTAs.indexOf(focusedBoxedDRTA);
                if (index >= 0) {
                    KeyCode code = e.getCode();
                    if (code == KeyCode.DOWN || code == KeyCode.ENTER) {
                        if (index + 1 < basicFormulasBoxedDRTAs.size()) {
                            basicFormulasBoxedDRTAs.get(index + 1).getRTA().requestFocus();
                        }
                        e.consume();
                    }
                    if (code == KeyCode.UP) {
                        if (index > 0) {
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
                int index = basicFormulasBoxedDRTAs.size() - 1;

                if (index > 0) {
                    BoxedDRTA removedDRTA = basicFormulasBoxedDRTAs.remove(index);
                    if (focusedBoxedDRTA == removedDRTA) focusedBoxedDRTA = null;
                    if (removedDRTA != null && removedDRTA.getRTA() != null) {
                        RichTextArea rta = removedDRTA.getRTA();
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

        Label rowNumLabel = new Label("Rows ");
        rowsSpinner = new Spinner<>(0,256,0);
        rowsSpinner.setPrefWidth(65);
        HBox spinnerBox = new HBox(5, rowsSpinner, rowNumLabel);
        spinnerBox.setAlignment(Pos.CENTER_LEFT);

        setupTableButton = new Button("Setup");
        setupTableButton.setPrefWidth(100);

        controlBox = new VBox(20, upperControlBox, basicFormulasPane, spinnerBox, setupTableButton);
        controlBox.setPadding(new Insets(150, 20, 0, 20));
        controlBox.setMinWidth(150); controlBox.setMaxWidth(150);
        controlBox.setAlignment(Pos.TOP_CENTER);

        choiceBox = new HBox(20, choiceLeadLabel, aCheckBox, bCheckBox);
        choiceBox.setPadding(new Insets(5,0,5,5));
        choiceBox.setStyle("-fx-border-color: gainsboro; -fx-border-width: 1 1 1 1");

        resultsBox = new VBox(5, choiceBox, explainDRTA.getEditor());
        resultsBox.setPadding(new Insets(0,0,0,0));

        tableGrid = new GridPane();
        tableGrid.setPadding(new Insets(15, RTA_WIDTH_BMP,15,10));
        centerBox = new VBox(5, tableGrid, resultsBox);
        tableGrid.setStyle("-fx-border-color: gainsboro");

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
                    if (tableFields[column][row + 1] != null) tableFields[column][row + 1].requestFocus();
                } else {
                    if (tableFields[column][0] != null) tableFields[column][0].requestFocus();
                }
            } catch (Exception ex) {}
        };

        singleCharPressFilter = e -> {
            if (isTornDown || this.mainView == null || tableFields == null) return;
            try {
                TextField field = (TextField) e.getSource();
                int[] pos = (int[]) field.getUserData();
                if (pos == null) return;
                int column = pos[0]; int row = pos[1];
                if (tableFields.length <= column || tableFields[column] == null) return;

                KeyCode code = e.getCode();
                if (code == KeyCode.UP && row > 0) {
                    if (tableFields[column][row - 1] != null) tableFields[column][row - 1].requestFocus();
                    e.consume();
                } else if (code == KeyCode.DOWN && row < tableRows - 1) {
                    if (tableFields[column][row + 1] != null) tableFields[column][row + 1].requestFocus();
                    e.consume();
                } else if (code == KeyCode.RIGHT) {
                    int rightCol = tableColToRight(column);
                    if (rightCol < tableFields.length && tableFields[rightCol] != null && tableFields[rightCol][row] != null) {
                        tableFields[rightCol][row].requestFocus();
                    }
                    e.consume();
                }  else if (code == KeyCode.LEFT) {
                    int leftCol = tableColToLeft(column);
                    if (leftCol >= 0 && leftCol < tableFields.length && tableFields[leftCol] != null && tableFields[leftCol][row] != null) {
                        tableFields[leftCol][row].requestFocus();
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
                        if (box != null) {
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
            if (isTornDown || this.mainView == null) return;
            if (e.getCode() == KeyCode.ENTER) e.consume();
        };

        commentFocusListener = (ob, ov, nv) -> {
            if (isTornDown || this.mainView == null) return;
            try {
                if (nv && ob instanceof javafx.beans.property.ReadOnlyProperty) {
                    Object bean = ((javafx.beans.property.ReadOnlyProperty<?>) ob).getBean();
                    if (bean instanceof RichTextArea) {
                        BoxedDRTA box = (BoxedDRTA) ((RichTextArea) bean).getUserData();
                        if (box != null) {
                            mainView.editorInFocus(box.getDRTA(), ControlType.FIELD);
                        }
                    }
                }
            } catch (Exception e) {}
        };
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
                        tableGrid.add(tableFields[i][j], i, j + 2);
                    }
                    tableGrid.add(highlightButtons[i], i, tableRows + 3);
                    if (highlightButtons[i].isSelected()) {
                        highlightButtons[i].setStyle("-fx-border-radius: 10; -fx-border-color: dodgerblue; -fx-background-color: lightblue; -fx-background-radius: 10");
                        for (int j = 0; j < tableRows; j++) {
                            if (tableFields[i][j] != null) tableFields[i][j].setStyle("-fx-background-radius: 2; -fx-control-inner-background: lightblue");
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
                if (j != 0 && j % 4 == 0) {
                    rowCommentsArray[j].getBoxedRTA().setAlignment(Pos.BOTTOM_LEFT);
                    rowCommentsArray[j].getBoxedRTA().setPadding(new Insets(0,0,1.5,0));
                }
                tableGrid.add(rowCommentsArray[j].getBoxedRTA(), tableHeadItemsList.size(), j + 2);
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
                } else {
                    rowCon.setMinHeight(25);
                    rowCon.setMaxHeight(25);
                }
                tableRowConstraints.add(rowCon);
            }
            tableRowConstraints.add(new RowConstraints(5));
            tableRowConstraints.add(new RowConstraints());
        } catch (Exception e) {}
    }

    /**
     * Update the (left-hand) basic formulas pane from the basicFormulasBoxedDRTAs
     */
    public void updateBasicFormulasPaneFromList() {
        if (isTornDown || this.mainView == null || basicFormulasPane == null || basicFormulasBoxedDRTAs == null) return;

        // SCORCHED EARTH RULE 8: Rebuild Fix
        try {
            if (basicFormulasPane.getChildren() != null) {
                List<Node> childrenCopy = new ArrayList<>(basicFormulasPane.getChildren());
                for (Node n : childrenCopy) {
                    try { n.getClass().getMethod("cleanup").invoke(n); } catch (Exception e) {}
                    severFromParent(n);
                }
                basicFormulasPane.getChildren().clear();
            }
            for (int i = 0; i < basicFormulasBoxedDRTAs.size(); i++) {
                basicFormulasPane.add(basicFormulasBoxedDRTAs.get(i).getBoxedRTA(),0, i);
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
        singleCharField.setMaxWidth(18);
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
     * @param column the given column index
     * @return the index of column to right
     */
    private int tableColToRight(int column) {
        int rightCol = column;
        if (tableHeadItemsList == null) return rightCol;
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
     * @param column the given column index
     * @return index of column to left
     */
    private int tableColToLeft(int column) {
        int leftCol = column;
        if (tableHeadItemsList == null) return leftCol;
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
     * @param index the column index
     * @return the ToggleButton
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
     * @return the BoxedDRTA
     */
    public BoxedDRTA newFormulaBoxedDRTA() {
        BoxedDRTA bdrta = new BoxedDRTA();
        DecoratedRTA drta = bdrta.getDRTA();
        drta.getKeyboardSelector().valueProperty().setValue(RichTextAreaSkin.KeyMapValue.ITALIC_AND_SANS);
        RichTextArea rta = bdrta.getRTA();
        rta.setMaxHeight(27);
        rta.setMinHeight(27);
        rta.setContentAreaWidth(200);
        rta.setPrefWidth(100);
        rta.getStylesheets().add("RichTextFieldWide.css");
        rta.setPromptText("Formula");

        rta.setUserData(bdrta);
        rta.focusedProperty().addListener(formulaFocusListener);

        return bdrta;
    }

    /**
     * Boxed DRTA for a comment field
     * @return the BoxedDRTA
     */
    public BoxedDRTA newCommentBoxedDRTA() {
        BoxedDRTA bdrta = new BoxedDRTA();
        RichTextArea rta = bdrta.getRTA();
        rta.setMaxHeight(formulaBoxHeight);
        rta.setMinHeight(formulaBoxHeight);

        RichTextAreaSkin rtaSkin = (RichTextAreaSkin) rta.getSkin();
        rta.prefWidthProperty().bind(Bindings.max(Bindings.add(rtaSkin.nodesWidthProperty(), 8), 100));
        rtaSkin.nodesWidthProperty().addListener(commentNodesWidthListener);
        rta.addEventFilter(KeyEvent.ANY, commentKeyFilter);

        rta.getStylesheets().add("RichTextField.css");

        rta.setUserData(bdrta);
        rta.focusedProperty().addListener(commentFocusListener);

        return bdrta;
    }

    /**
     * Setup size controls for statement, comment, explain, and table grid
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
        commentRTA.setPromptText("Comment:");

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
            try { tableGridHeightSpinner.getValueFactory().setValue((double) Math.round(tableGrid.getHeight() / localScalePageHeight.get() * 100)); } catch (Exception e) {}
        };
        tableGrid.heightProperty().addListener(tableGridHeightListener);

        tableGridWidthSpinner = new Spinner<>(100.0,999.0, 0,1.0);
        tableGridWidthSpinner.setPrefWidth(65);
        tableGridWidthSpinner.setDisable(true);
        tableGridWidthSpinner.setTooltip(new Tooltip("Window width as % of selected paper"));
        tableGridWidthListener = (ob, ov, nv) -> {
            if (isTornDown || this.mainView == null || tableGridWidthSpinner == null || tableGrid == null) return;
            try { tableGridWidthSpinner.getValueFactory().setValue((double) Math.round((tableGrid.getWidth() - RTA_WIDTH_BMP) / localScalePageWidth.get() * 100)); } catch (Exception e) {}
        };
        tableGrid.widthProperty().addListener(tableGridWidthListener);

        //choices (null spinners)
        choicesHeightSpinner = new Spinner<>(0.0, 999.0, 0, 1.0);
        choicesHeightSpinner.setPrefWidth(65);
        choicesHeightSpinner.setDisable(true);
        choicesHeightSpinner.setTooltip(new Tooltip("Window height as % of selected paper"));

        choiceBox.maxWidthProperty().bind(Bindings.add(localScalePageWidth, RTA_WIDTH_BMP));
        choicesWidthSpinner = new Spinner<>(100.0, 999.0, 100.0, 1.0);
        choicesWidthSpinner.setPrefWidth(65);
        choicesWidthSpinner.setDisable(true);
        choicesWidthSpinner.setTooltip(new Tooltip("Window width as % of selected paper"));

        //page size listeners
        localScalePageHeightListener = (ob, ov, nv) -> {
            if (isTornDown || this.mainView == null || exerciseStatement == null || exerciseComment == null || explainDRTA == null || tableGrid == null || tableGridHeightSpinner == null || choiceBox == null) return;
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

                if (tableGridHeightSpinner.getValueFactory() != null) {
                    tableGridHeightSpinner.getValueFactory().setValue((double) Math.round(tableGrid.getHeight() / localScalePageHeight.get() * 100.0));
                }
                if (choicesHeightSpinner != null && choicesHeightSpinner.getValueFactory() != null) {
                    choicesHeightSpinner.getValueFactory().setValue((double) Math.round(choiceBox.getHeight() / localScalePageHeight.get() * 100.0));
                }
            } catch (Exception e) {}
        };
        localScalePageHeight.addListener(localScalePageHeightListener);

        localScalePageWidthListener = (ob, ov, nv) -> {
            if (isTornDown || this.mainView == null || tableGrid == null || tableGridWidthSpinner == null || tableGridWidthSpinner.getValueFactory() == null) return;
            try {
                tableGridWidthSpinner.getValueFactory().setValue((double) Math.round((tableGrid.getWidth() - RTA_WIDTH_BMP) / localScalePageWidth.get() * 100.0));
            } catch (Exception e) {}
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

        checkButton = new Button("Check Table");
        checkButton.setPrefWidth(105);
        checkButton.setTooltip(new Tooltip("Check table for correctness."));
        checkTriesLabel = new Label();

        checkProgButton = new Button();

        VBox checksBox = new VBox(10, checkButton, checkTriesLabel);
        checksBox.setAlignment(Pos.CENTER);
        checkTriesLabel.setAlignment(Pos.CENTER);

        staticHelpButton = new Button("Static Help");
        staticHelpButton.setPrefWidth(105);
        staticHelpButton.setTooltip(new Tooltip("Get static help text"));

        shortTableCheck = new CheckBox("Short Table");

        VBox rightControlBox = new VBox(40, bigCheckBox, checksBox, staticHelpButton, shortTableCheck );
        rightControlBox.setAlignment(Pos.TOP_CENTER);
        rightControlBox.setPadding(new Insets(60,20,0,20));
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

    //record status, and deactivate
    public void deactivateBigCheck() {
        if (bigCheck == null || checkedElements == null) return;
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
        tableGridClickHandler = e -> {
            if (isTornDown || this.mainView == null || tableGrid == null) return;
            try {
                if (currentSpinnerNode != tableGrid) {
                    tableGrid.requestFocus();
                }
            } catch (Exception ex) {}
        };
        tableGrid.addEventFilter(MouseEvent.MOUSE_CLICKED, tableGridClickHandler);

        choiceBoxClickHandler = e -> {
            if (isTornDown || this.mainView == null || choiceBox == null) return;
            try {
                if (currentSpinnerNode != choiceBox) {
                    choiceBox.requestFocus();
                }
            } catch (Exception ex) {}
        };
        choiceBox.addEventFilter(MouseEvent.MOUSE_CLICKED, choiceBoxClickHandler);

        if (mainView != null && mainView.getMainScene() != null) {
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
        // KILL SWITCH: Engaged! Prevent any mid-teardown listeners from accessing half-destroyed UI tree
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

        // SCORCHED EARTH RULE 5: Rule of Isolation via tiny isolated try-catch blocks
        try {
            if (tableGrid != null && tableGridClickHandler != null) {
                tableGrid.removeEventFilter(MouseEvent.MOUSE_CLICKED, tableGridClickHandler);
            }
        } catch (Exception e) {}

        try {
            if (choiceBox != null && choiceBoxClickHandler != null) {
                choiceBox.removeEventFilter(MouseEvent.MOUSE_CLICKED, choiceBoxClickHandler);
            }
        } catch (Exception e) {}

        try {
            if (mainView != null && mainView.getMainScene() != null && exerciseFocusListener != null) {
                mainView.getMainScene().focusOwnerProperty().removeListener(exerciseFocusListener);
            }
        } catch (Exception e) {}

        try {
            if (basicFormulasPane != null && basicFormulasKeyFilter != null) {
                basicFormulasPane.removeEventFilter(KeyEvent.KEY_PRESSED, basicFormulasKeyFilter);
            }
        } catch (Exception e) {}

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

        try {
            if (choiceBox != null) choiceBox.maxWidthProperty().unbind();
        } catch (Exception e) {}

        // Unbind RTA properties
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
            final RichTextArea expEditor = (explainDRTA != null) ? explainDRTA.getEditor() : null;

            final List<RichTextArea> rtasToClear = new ArrayList<>();
            if (stEditor != null) rtasToClear.add(stEditor);
            if (commEditor != null) rtasToClear.add(commEditor);
            if (expEditor != null) rtasToClear.add(expEditor);

            if (basicFormulasBoxedDRTAs != null) {
                for (BoxedDRTA bdrta : basicFormulasBoxedDRTAs) {
                    if (bdrta != null && bdrta.getRTA() != null) rtasToClear.add(bdrta.getRTA());
                }
            }

            if (rowCommentsArray != null) {
                for (BoxedDRTA bdrta : rowCommentsArray) {
                    if (bdrta != null && bdrta.getRTA() != null) rtasToClear.add(bdrta.getRTA());
                }
            }

            if (!rtasToClear.isEmpty()) {
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

        // SCORCHED EARTH RULE 8: Safely sever sub-nodes (Iterate over children copy)
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
            if (controlBox != null) {
                if (controlBox.getChildren() != null) {
                    List<Node> childrenCopy = new ArrayList<>(controlBox.getChildren());
                    for (Node node : childrenCopy) severFromParent(node);
                }
                controlBox.getChildren().clear();
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
            if (resultsBox != null) {
                if (resultsBox.getChildren() != null) {
                    List<Node> childrenCopy = new ArrayList<>(resultsBox.getChildren());
                    for (Node node : childrenCopy) severFromParent(node);
                }
                resultsBox.getChildren().clear();
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
            if (pointsEarnedTextField != null) pointsEarnedTextField.setTextFormatter(null);
        } catch (Exception e) {}

        try { if (addBasicFormulaButton != null) addBasicFormulaButton.setOnAction(null); } catch (Exception e) {}
        try { if (removeBasicFormulaButton != null) removeBasicFormulaButton.setOnAction(null); } catch (Exception e) {}
        try { if (setupTableButton != null) setupTableButton.setOnAction(null); } catch (Exception e) {}

        try { if (checkButton != null) checkButton.setOnAction(null); } catch (Exception e) {}
        try { if (checkProgButton != null) checkProgButton.setOnAction(null); } catch (Exception e) {}
        try { if (staticHelpButton != null) staticHelpButton.setOnAction(null); } catch (Exception e) {}

        try {
            if (aCheckBox != null) {
                aCheckBox.setOnAction(null);
            }
        } catch(Exception e) {}
        try {
            if (bCheckBox != null) {
                bCheckBox.setOnAction(null);
            }
        } catch(Exception e) {}

        // Null out all fields safely
        this.mainView = null;
        this.exerciseStatement = null;
        this.exerciseComment = null;
        this.explainDRTA = null;
        this.statementHeightSpinner = null;
        this.statementWidthSpinner = null;
        this.commentHeightSpinner = null;
        this.commentWidthSpinner = null;
        this.explainHeightSpinner = null;
        this.explainWidthSpinner = null;
        this.choicesHeightSpinner = null;
        this.choicesWidthSpinner = null;
        this.tableGridHeightSpinner = null;
        this.tableGridWidthSpinner = null;
        this.currentSpinnerNode = null;
        this.basicFormulasPane = null;

        // SCORCHED EARTH RULE 10: NON-DESTRUCTIVE Data Teardown
        this.basicFormulasBoxedDRTAs = null;
        this.tableHeadItemsList = null;

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
        this.tableFields = null;
        this.rowCommentsArray = null;
        this.highlightButtons = null;
        this.sizers = null;
        this.endPane = null;
        this.focusedBoxedDRTA = null;
        this.pointsEarnedTextField = null;
        this.rightControlNode = null;
        this.checkButton = null;
        this.checkProgButton = null;
        this.bigCheck = null;
        this.checkMessage = null;
        this.checkedElements = null;
        this.checkTriesLabel = null;
        this.staticHelpButton = null;
        try {
            if (this.staticHelpStage != null) {
                this.staticHelpStage.close();
            }
        } catch (Exception e) {}
        this.staticHelpStage = null;
        this.shortTableCheck = null;
        this.explainPrompt = null;

        // Nullify listeners cleanly via matching variable names
        this.exerciseFocusListener = null;
        this.basicFormulasKeyFilter = null;
        this.addBasicFormulaHandler = null;
        this.removeBasicFormulaHandler = null;
        this.pointsFilter = null;
        this.tableGridHeightListener = null;
        this.tableGridWidthListener = null;
        this.localScalePageHeightListener = null;
        this.localScalePageWidthListener = null;
        this.tableGridClickHandler = null;
        this.choiceBoxClickHandler = null;
        this.singleCharTypeFilter = null;
        this.singleCharPressFilter = null;
        this.singleCharTextFilter = null;
        this.highlightButtonHandler = null;
        this.formulaFocusListener = null;
        this.commentNodesWidthListener = null;
        this.commentKeyFilter = null;
        this.commentFocusListener = null;
        this.addBasicFormulaButton = null;
        this.removeBasicFormulaButton = null;
        this.pointsEarnedFocusListener = null;
        this.pointsEarnedClickHandler = null;

    }

    public Button getCheckProgButton() {
        return checkProgButton;
    }

    public Label getCheckTriesLabel() {
        return checkTriesLabel;
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

    public Button getStaticHelpButton() {
        return staticHelpButton;
    }

    /**
     * The table grid pane
     * @return tghe grid
     */
    GridPane getTableGrid() { return tableGrid; }

    /**
     * Number of table (content) rows
     * @param tableRows the rows value
     */
    void setTableRows(int tableRows) { this.tableRows = tableRows;  }

    /**
     * The TextField[][] of table values
     * @param tableFields the array
     */
    void setTableFields(TextField[][] tableFields) {  this.tableFields = tableFields; }

    /**
     * The list of {@link slapp.editor.truth_table.TableHeadItem}
     * @return the list
     */
    List<TableHeadItem> getTableHeadItemsList() { return tableHeadItemsList;  }

    /**
     * The list of {@link slapp.editor.truth_table.TableHeadItem}
     * @param tableHeadItemsList the list
     */
    void setTableHeadItemsList(List<TableHeadItem> tableHeadItemsList) {   this.tableHeadItemsList = tableHeadItemsList;  }

    /**
     * The BoxedDRTA[] of row comments
     * @param rowComments the array
     */
    void setRowCommentsArray(BoxedDRTA[] rowComments) {  this.rowCommentsArray = rowComments;   }

    /**
     * The ToggleButton[] of highlight buttons
     * @param highlightButtons the array
     */
    void setHighlightButtons(ToggleButton[] highlightButtons) {   this.highlightButtons = highlightButtons; }

    /**
     * The BoxedDRTA[] of row comments
     * @return the array
     */
    BoxedDRTA[] getRowCommentsArray() { return rowCommentsArray; }

    /**
     * The ToggleButton[] of highlight buttons
     * @return the array
     */
    ToggleButton[] getHighlightButtons() { return highlightButtons; }

    /**
     * The TextField[][] of table values
     * @return the array
     */
    TextField[][] getTableFields() { return tableFields; }

    /**
     * The (left-hand) setup table button
     * @return the button
     */
    Button getSetupTableButton() { return setupTableButton; }

    /**
     * The list of basic formula Boxed DRTAs
     * @return the bdrta list
     */
    List<BoxedDRTA> getBasicFormulasBoxedDRTAs() {return basicFormulasBoxedDRTAs; }

    /**
     * The spinner to set the number of table rows
     * @return the spinner
     */
    Spinner getRowsSpinner() { return rowsSpinner;  }

    /**
     * The label for the choices lead
     * @return the label
     */
    Label getChoiceLeadLabel() { return choiceLeadLabel; }

    /**
     * The A check box
     * @return The CheckBox
     */
    CheckBox getaCheckBox() { return aCheckBox;  }

    /**
     * The B check box
     * @return the CheckBox
     */
    CheckBox getbCheckBox() { return bCheckBox; }

    /**
     * The Decorated RTA for the explain field
     * @return the DRTA
     */
    DecoratedRTA getExplainDRTA() {return explainDRTA; }

    /**
     * The prompt for the explain field
     * @param explainPrompt the String prompt
     */
    void setExplainPrompt(String explainPrompt) {    this.explainPrompt = explainPrompt; }

    /**
     * The preferred height of the comment window
     * @return the height value
     */
    double getCommentPrefHeight() { return exerciseComment.getEditor().getPrefHeight();  }

    /**
     * The preferred height of the comment window
     * @param commentPrefHeight the height value
     */
    void setCommentPrefHeight(double commentPrefHeight) { this.commentPrefHeight = commentPrefHeight;  }

    /**
     * The preferred height of the explain window
     * @return the height value
     */
    double getExplainPrefHeight() { return explainDRTA.getEditor().getPrefHeight();  }

    /**
     * The pref height of the explain window
     * @param explainPrefHeight the height value
     */
    void setExplainPrefHeight(double explainPrefHeight) { this.explainPrefHeight = explainPrefHeight;  }

    public VBox getControlBox() {
        return controlBox;
    }

    /**
     * The comment decoratedRTA
     * @return comment DecoratedRTA
     */
    @Override
    public DecoratedRTA getExerciseComment() { return exerciseComment; }

    /**
     * The comment decoratedRTA
     * @param comment DecoratedRTA
     */
    @Override
    public void setExerciseComment(DecoratedRTA comment) { exerciseComment = comment;  }

    /**
     * The exercise prompt
     * @return the statement decorated RTA (T)
     */
    @Override
    public DecoratedRTA getExerciseStatement() { return exerciseStatement;  }

    /**
     * The exercise prompt
     * @param statement the statement decorated RTA (T)
     */
    @Override
    public void setExerciseStatement(DecoratedRTA statement) { exerciseStatement = statement; }

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
    public Node getExerciseContentNode() {return centerBox; }

    /**
     * The left control node for this exercise
     * @return the control node
     */
    @Override
    public Node getExerciseControl() { return controlBox; }

    /**
     * The (null) right control node for this exercise
     * @return the control node
     */
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

    public String getCheckMessage() {
        return checkMessage;
    }

    public Button getCheckButton() {
        return checkButton;
    }

    public CheckBox getShortTableCheck() {
        return shortTableCheck;
    }

    public VBox getCenterBox() {
        return centerBox;
    }

    public DoubleProperty localScalePageHeightProperty() {  return localScalePageHeight;
    }

    public DoubleProperty localScalePageWidthProperty() {
        return localScalePageWidth;
    }

    public ChangeListener<Node> getExerciseFocusListener() {
        return exerciseFocusListener;
    }
}
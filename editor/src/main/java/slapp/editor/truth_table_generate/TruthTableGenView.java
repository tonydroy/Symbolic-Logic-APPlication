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
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.TextFlow;
import slapp.editor.EditorAlerts;
import slapp.editor.PrintUtilities;
import slapp.editor.decorated_rta.BoxedDRTA;
import slapp.editor.decorated_rta.DecoratedRTA;
import slapp.editor.main_window.ControlType;
import slapp.editor.main_window.ExerciseView;
import slapp.editor.main_window.MainWindowView;
import slapp.editor.truth_table.TableHeadItem;

import java.util.ArrayList;
import java.util.List;
import java.util.function.UnaryOperator;

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
    private Spinner rowsSpinner;
    Button setupTableButton;
    private Label choiceLeadLabel = new Label();
    private CheckBox aCheckBox = new CheckBox();
    private CheckBox bCheckBox = new CheckBox();
    private GridPane tableGrid;
    private VBox resultsBox;
    private HBox choiceBox;
    private VBox centerBox;
    private String explainPrompt;
    private double contentFixedHeight = 70;
    private double formulaBoxHeight = 22;
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

    /**
     * Construct the truth table generate view
     * @param mainView the main window
     */
    TruthTableGenView(MainWindowView mainView) {
        this.mainView = mainView;

        Font labelFont = new Font("Noto Serif Combo", 11);
        choiceLeadLabel.setFont(labelFont); aCheckBox.setFont(labelFont); bCheckBox.setFont(labelFont);

        basicFormulasPane = new GridPane();
        basicFormulasPane.setVgap(10);
        basicFormulasBoxedDRTAs = new ArrayList<>();

        mainFormulasPane = new GridPane();
        mainFormulasPane.setVgap(10);
        mainFormulasBoxedDRTAs = new ArrayList<>();

        //basic
        Label basicFormulasLabel = new Label("Basic Sentences:");
        Button addBasicFormulaButton = new Button("+");
        Button removeBasicFormulaButton = new Button("-");
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
        Button addMainFormulaButton = new Button("+");
        Button removeMainFormulaButton = new Button("-");
        addMainFormulaButton.setFont(new Font(16));
        addMainFormulaButton.setPadding(new Insets(0,5,0,5));
        removeMainFormulaButton.setFont(new Font(16));
        removeMainFormulaButton.setPadding(new Insets(1,8,1,8));

        HBox mainFormulaButtonBox = new HBox(20, addMainFormulaButton, removeMainFormulaButton);
        VBox mainFormulaControlBox = new VBox(10, mainFormulasLabel, mainFormulaButtonBox);

        mainFormulasBoxedDRTAs.add(newFormulaBoxedDRTA());
        updateMainFormulasPaneFromList();

        //basic
        basicFormulasPane.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
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
        });

        addBasicFormulaButton.setOnAction(e -> {
            basicFormulasBoxedDRTAs.add(newFormulaBoxedDRTA());
            updateBasicFormulasPaneFromList();
        });
        removeBasicFormulaButton.setOnAction(e -> {
           int index = basicFormulasBoxedDRTAs.size();
           index--;
           if (index > 0) {
               basicFormulasBoxedDRTAs.remove(index);
               updateBasicFormulasPaneFromList();
           } else {
               EditorAlerts.showSimpleAlert("Cannot Remove", "A truth table must include at least one basic sentence.");
           }
        });

        //main
        mainFormulasPane.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
            int index = mainFormulasBoxedDRTAs.indexOf(focusedBoxedDRTA);
            if (index >= 0) {
                KeyCode code = e.getCode();
                if (code == KeyCode.ENTER || code == KeyCode.DOWN) {
                    if (index + 1 < mainFormulasBoxedDRTAs.size()) {
                        mainFormulasBoxedDRTAs.get(index + 1).getRTA().requestFocus();
                    }
                    e.consume();
                }
                if (code == KeyCode.UP) {
                    if (index > 0) {
                        mainFormulasBoxedDRTAs.get(index - 1).getRTA().requestFocus();
                    }
                    e.consume();
                }
            }
        });

        addMainFormulaButton.setOnAction(e -> {
            mainFormulasBoxedDRTAs.add(newFormulaBoxedDRTA());
            updateMainFormulasPaneFromList();
        });
        removeMainFormulaButton.setOnAction(e -> {
            int index = mainFormulasBoxedDRTAs.size();
            index--;
            if (index > 0) {
                mainFormulasBoxedDRTAs.remove(index);
                updateMainFormulasPaneFromList();
            } else {
                EditorAlerts.showSimpleAlert("Cannot Remove", "Your argument must contain at least a conclusion.");
            }
        });

        //finish
        Label rowNumLabel = new Label("Rows ");
        rowsSpinner = new Spinner<>(0,256,0);
        rowsSpinner.setPrefWidth(65);
        HBox spinnerBox = new HBox(5, rowsSpinner, rowNumLabel);
        spinnerBox.setAlignment(Pos.CENTER_LEFT);

        setupTableButton = new Button("Setup");
        setupTableButton.setPrefWidth(100);

        controlBox = new VBox(20, basicFormulaControlBox, basicFormulasPane, mainFormulaControlBox, mainFormulasPane, spinnerBox, setupTableButton);
        controlBox.setPadding(new Insets(100, 15,0,15));
        controlBox.setMinWidth(150); controlBox.setMaxWidth(150);

        choiceBox = new HBox(20, choiceLeadLabel, aCheckBox, bCheckBox);
        choiceBox.setPadding(new Insets(5,0,5,5));
        choiceBox.setStyle("-fx-border-color: gainsboro; -fx-border-width: 1 1 1 1");

        resultsBox = new VBox(5, choiceBox, explainDRTA.getEditor());
        resultsBox.setPadding(new Insets(0,0,0,0));

        tableGrid = new GridPane();
        tableGrid.setPadding(new Insets(20,0,20,10));

        centerBox = new VBox(10, interpretationDRTA.getEditor(), tableGrid, resultsBox);
        tableGrid.setStyle("-fx-border-color: gainsboro");
        interpretationDRTA.getEditor().setStyle("-fx-border-width: 2; -fx-border-color: gainsboro;");
    }

    /**
     * Initialize table structure
     */
    public void updateTableGridFromTableItems() {
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
                    highlightButtons[i].setStyle("-fx-border-radius: 10; -fx-border-color: tomato; -fx-background-color: lavenderblush;");
                    for (int j = 0; j < tableRows; j++) {
                        tableFields[i][j].setStyle("-fx-background-radius: 2; -fx-background-color: pink");
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
            }
            else {
                rowCon.setMinHeight(25);
                rowCon.setMaxHeight(25);
            }
            tableRowConstraints.add(rowCon);

//            tableRowConstraints.add(new RowConstraints(25));

        }
        tableRowConstraints.add(new RowConstraints(5));
        tableRowConstraints.add(new RowConstraints());
    }

    /**
     * Update the (left-hand) basic formulas pane from the basicFormulasBoxedDRTAList
     */
    public void updateBasicFormulasPaneFromList() {
        basicFormulasPane.getChildren().clear();
        for (int i = 0; i < basicFormulasBoxedDRTAs.size(); i++) {
            basicFormulasPane.add(basicFormulasBoxedDRTAs.get(i).getBoxedRTA(),0, i);
        }
    }

    /**
     * Update the (left-hand) main formulas pane from the mainFormulasBoxedDRTA list
     */
    public void updateMainFormulasPaneFromList() {
        mainFormulasPane.getChildren().clear();
        for (int i = 0; i < mainFormulasBoxedDRTAs.size(); i++) {
            mainFormulasPane.add(mainFormulasBoxedDRTAs.get(i).getBoxedRTA(), 0, i);
        }
    }

    /**
     * New character field for colum and row
     * @param column the column
     * @param row the row
     * @return the text field
     */
    TextField newSingleCharTextField(int column, int row) {
        TextField singleCharField = new TextField();
        singleCharField.setPadding(new Insets(0));
        singleCharField.setPrefWidth(15);

        singleCharField.setAlignment(Pos.CENTER);
        singleCharField.setStyle("-fx-background-radius: 2");
        singleCharField.setPadding(new Insets(3));

        UnaryOperator<TextFormatter.Change> textFilter = c -> {
            if (c.getText().matches("[0-9a-zA-Z]")) {
                c.setRange(0, singleCharField.getText().length());
                return c;
            } else if (c.getText().isEmpty()) {
                return c;
            }
            return null;
        };
        TextFormatter<String> formatter = new TextFormatter<>(textFilter);
        singleCharField.setTextFormatter(formatter);

        singleCharField.addEventFilter(KeyEvent.KEY_TYPED, e -> {
           if (row < tableRows - 1) {
                tableFields[column][row + 1].requestFocus();
           } else {
                tableFields[column][0].requestFocus();
           }

        });

        singleCharField.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
            KeyCode code = e.getCode();
            if (code == KeyCode.UP && row > 0) {
                tableFields[column][row - 1].requestFocus();
                e.consume();
            } else if (code == KeyCode.DOWN && row < tableRows - 1) {
                tableFields[column][row + 1].requestFocus();
                e.consume();
            } else if (code == KeyCode.RIGHT) {
                tableFields[tableColToRight(column)][row].requestFocus();
                e.consume();
            }  else if (code == KeyCode.LEFT) {
                tableFields[tableColToLeft(column)][row].requestFocus();
                e.consume();
            }

        });
        return singleCharField;
    }

    /**
     * First non-blank column to the right of given column
     * @param column the given column index
     * @return the index of column to right
     */
    private int tableColToRight(int column) {
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
     * @param column the given column index
     * @return index of column to left
     */
    private int tableColToLeft(int column) {
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
     * @param index the column index
     * @return the ToggleButton
     */
    ToggleButton newHighlightButton(int index) {
        ToggleButton button = new ToggleButton();
        button.setPadding(new Insets(0));
        button.setPrefWidth(20);

        button.setStyle("-fx-border-radius: 10; -fx-border-color: lightblue; -fx-background-color: ghostwhite");

        button.setOnAction(e -> {
            if (button.isSelected()) {
                button.setStyle("-fx-border-radius: 10; -fx-border-color: tomato; -fx-background-color: lavenderblush;");
                for (int j = 0; j < tableRows; j++) {
                    tableFields[index][j].setStyle("-fx-background-radius: 2; -fx-background-color: pink");
                }

            } else {
                button.setStyle("-fx-border-radius: 10; -fx-border-color: lightblue; -fx-background-color: ghostwhite;");
                for (int j = 0; j < tableRows; j++) {
                    tableFields[index][j].setStyle("-fx-background-radius: 2; -fxBackground-color: white");
                }
            }
        });
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
        rta.setPrefWidth(125);
        rta.getStylesheets().add("RichTextFieldWide.css");
        rta.setPromptText("Formula");
        rta.focusedProperty().addListener((ob, ov, nv) -> {
            if (nv) {
                mainView.editorInFocus(drta, ControlType.FIELD);
                focusedBoxedDRTA = bdrta;
            }
        });
        return bdrta;
    }

    /**
     * Boxed DRTA for a comment field
     * @return the BoxedDRTA
     */
    public BoxedDRTA newCommentBoxedDRTA() {
        BoxedDRTA bdrta = new BoxedDRTA();
        DecoratedRTA drta = bdrta.getDRTA();
        RichTextArea rta = bdrta.getRTA();
        rta.setMaxHeight(formulaBoxHeight);
        rta.setMinHeight(formulaBoxHeight);

        RichTextAreaSkin rtaSkin = (RichTextAreaSkin) rta.getSkin();
        rta.prefWidthProperty().bind(Bindings.max(Bindings.add(rtaSkin.nodesWidthProperty(), 6), 100));
        rta.addEventFilter(KeyEvent.ANY, e -> {
            if (e.getCode() == KeyCode.ENTER) e.consume();
        });

        rta.getStylesheets().add("RichTextField.css");
//        rta.setPromptText("Comment");
        rta.focusedProperty().addListener((ob, ov, nv) -> {
            if (nv) {
                mainView.editorInFocus(drta, ControlType.FIELD);
            }
        });
        return bdrta;
    }

    /**
     * Setup size controls for statement, comment, interpretation, explain, and table grid
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

        //interpretation
        RichTextArea interpretationRTA = interpretationDRTA.getEditor();
        interpretationRTA.getStylesheets().add("slappTextArea.css");
        interpretationRTA.setPromptText(interpretationPrompt);

        double interpretationInitialHeight = Math.round(interpretationPrefHeight / mainView.getScalePageHeight() * 100.0 );
        interpretationHeightSpinner = new Spinner<>(0.0, 999.0, interpretationInitialHeight, 1.0);
        interpretationHeightSpinner.setPrefWidth(60);
        interpretationHeightSpinner.setDisable(false);
        interpretationHeightSpinner.setTooltip(new Tooltip("Height as % of selected paper"));
        interpretationRTA.prefHeightProperty().bind(Bindings.max(45.0, Bindings.multiply(mainView.scalePageHeightProperty(), DoubleProperty.doubleProperty(interpretationHeightSpinner.getValueFactory().valueProperty()).divide(100.0))));
        interpretationHeightSpinner.valueProperty().addListener((obs, ov, nv) -> {
            Node increment = interpretationHeightSpinner.lookup(".increment-arrow-button");
            if (increment != null) increment.getOnMouseReleased().handle(null);
            Node decrement = interpretationHeightSpinner.lookup(".decrement-arrow-button");
            if (decrement != null) decrement.getOnMouseReleased().handle(null);
        });

        interpretationRTA.maxWidthProperty().bind(mainView.scalePageWidthProperty());
        interpretationRTA.minWidthProperty().bind(mainView.scalePageWidthProperty());
        interpretationWidthSpinner = new Spinner<>(0.0, 999.0, 100, 1.0);
        interpretationWidthSpinner.setPrefWidth(60);
        interpretationWidthSpinner.setDisable(true);
        interpretationWidthSpinner.setTooltip(new Tooltip("Width as % of selected paper"));

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

        //table grid
        tableGridHeightSpinner = new Spinner<>(0.0,999.0, 0,1.0);
        tableGridHeightSpinner.setPrefWidth(60);
        tableGridHeightSpinner.setDisable(true);
        tableGridHeightSpinner.setTooltip(new Tooltip("Height as % of selected paper"));
        tableGrid.heightProperty().addListener((ob, ov, nv) -> {
            tableGridHeightSpinner.getValueFactory().setValue((double) Math.round(tableGrid.getHeight() / mainView.getScalePageHeight() * 100));
        });

        tableGridWidthSpinner = new Spinner<>(0.0,999.0, 0,1.0);
        tableGridWidthSpinner.setPrefWidth(60);
        tableGridWidthSpinner.setDisable(true);
        tableGridWidthSpinner.setTooltip(new Tooltip("Height as % of selected paper"));
        tableGrid.widthProperty().addListener((ob, ov, nv) -> {
            tableGridWidthSpinner.getValueFactory().setValue((double) Math.round(tableGrid.getWidth() / mainView.getScalePageWidth() * 100));
        });

        //choices (null spinners)
        choicesHeightSpinner = new Spinner<>(0.0, 999.0, 0, 1.0);
        choicesHeightSpinner.setPrefWidth(60);
        choicesHeightSpinner.setDisable(true);
        choicesHeightSpinner.setTooltip(new Tooltip("Height as % of selected paper"));

        choiceBox.maxWidthProperty().bind(mainView.scalePageWidthProperty());
        choicesWidthSpinner = new Spinner<>(0.0, 999.0, 100.0, 1.0);
        choicesWidthSpinner.setPrefWidth(60);
        choicesWidthSpinner.setDisable(true);
        choicesWidthSpinner.setTooltip(new Tooltip("Width as % of selected paper"));

        //page size listeners
        mainView.scalePageHeightProperty().addListener((ob, ov, nv) -> {

            statementRTA.prefHeightProperty().unbind();
            statementHeightSpinner.getValueFactory().setValue((double) Math.round(statementHeightSpinner.getValue() * ov.doubleValue() / nv.doubleValue()));
            statementRTA.prefHeightProperty().bind(Bindings.max(45.0, Bindings.multiply(nv.doubleValue(), DoubleProperty.doubleProperty(statementHeightSpinner.getValueFactory().valueProperty()).divide(100.0))));

            commentRTA.prefHeightProperty().unbind();
            commentHeightSpinner.getValueFactory().setValue((double) Math.round(commentHeightSpinner.getValue() * ov.doubleValue() / nv.doubleValue()));
            commentRTA.prefHeightProperty().bind(Bindings.max(45.0, Bindings.multiply(nv.doubleValue(), DoubleProperty.doubleProperty(commentHeightSpinner.getValueFactory().valueProperty()).divide(100.0))));

            interpretationRTA.prefHeightProperty().unbind();
            interpretationHeightSpinner.getValueFactory().setValue((double) Math.round(interpretationHeightSpinner.getValue() * ov.doubleValue() / nv.doubleValue()));
            interpretationRTA.prefHeightProperty().bind(Bindings.max(45.0, Bindings.multiply(nv.doubleValue(), DoubleProperty.doubleProperty(interpretationHeightSpinner.getValueFactory().valueProperty()).divide(100.0))));

            explainRTA.prefHeightProperty().unbind();
            explainHeightSpinner.getValueFactory().setValue((double) Math.round(explainHeightSpinner.getValue() * ov.doubleValue() / nv.doubleValue()));
            explainRTA.prefHeightProperty().bind(Bindings.max(45.0, Bindings.multiply(nv.doubleValue(), DoubleProperty.doubleProperty(explainHeightSpinner.getValueFactory().valueProperty()).divide(100.0))));

            tableGridHeightSpinner.getValueFactory().setValue((double) Math.round(tableGrid.getHeight() / mainView.getScalePageHeight() * 100.0));
            choicesHeightSpinner.getValueFactory().setValue((double) Math.round(choiceBox.getHeight() / mainView.getScalePageHeight() * 100.0));

        });

        mainView.scalePageWidthProperty().addListener((ob, ov, nv) -> {
            tableGridWidthSpinner.getValueFactory().setValue((double) Math.round(tableGrid.getWidth() / mainView.getScalePageWidth() * 100.0));
        });

        setSizeSpinners();
    }

    private void setSizeSpinners() {

        tableGrid.addEventFilter(MouseEvent.MOUSE_CLICKED, e -> {
            if (currentSpinnerNode != tableGrid) {
                tableGrid.requestFocus();
            }
        });
        choiceBox.addEventFilter(MouseEvent.MOUSE_CLICKED, e -> {
            if (currentSpinnerNode != choiceBox) {
                choiceBox.requestFocus();
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
            RichTextArea interpretationRTA = interpretationDRTA.getEditor();
            if (inHierarchy(nv, interpretationRTA) && currentSpinnerNode != interpretationRTA) {
                currentSpinnerNode = interpretationRTA;
                mainView.updateSizeSpinners(interpretationHeightSpinner, interpretationWidthSpinner);
                return;
            }
            RichTextArea explainRTA = explainDRTA.getEditor();
            if (inHierarchy(nv, explainRTA) && currentSpinnerNode != explainRTA) {
                currentSpinnerNode = explainRTA;
                mainView.updateSizeSpinners(explainHeightSpinner, explainWidthSpinner);
                return;
            }
            if (inHierarchy(nv, choiceBox) && currentSpinnerNode != choiceBox) {
                currentSpinnerNode = choiceBox;
                double choicesHeightValue = Math.round(choiceBox.getHeight() / mainView.getScalePageHeight() * 100);
                choicesHeightSpinner.getValueFactory().setValue(choicesHeightValue);
                mainView.updateSizeSpinners(choicesHeightSpinner, choicesWidthSpinner);
                return;
            }
            if (inHierarchy(nv, tableGrid) && currentSpinnerNode != tableGrid) {
                currentSpinnerNode = tableGrid;
                mainView.updateSizeSpinners(tableGridHeightSpinner, tableGridWidthSpinner);
            }

        });
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
     * The list of main formula Boxed DRTAs
     * @return the bdrta list
     */
    List<BoxedDRTA> getMainFormulasBoxedDRTAs() {return mainFormulasBoxedDRTAs; }

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
     * The Decorated RTA for the explain field
     * @param explainDRTA the DRTA
     */
    void setExplainDRTA(DecoratedRTA explainDRTA) {    this.explainDRTA = explainDRTA;  }

    /**
     * The Decorated RTA for the interpretation field
     * @param drta the DRTA
     */
    void setInterpretationDRTA(DecoratedRTA drta) { interpretationDRTA = drta; }

    /**
     * The Decorated RTA for the interpretation field
     * @return the DRTA
     */
    DecoratedRTA getInterpretationDRTA() {return interpretationDRTA; }

    /**
     * The prompt to appear in the interpretation field
     * @param interpretationPrompt the string prompt
     */
    void setInterpretationPrompt(String interpretationPrompt) {    this.interpretationPrompt = interpretationPrompt; }

    /**
     * The prompt for the explain field
     * @param explainPrompt the String prompt
     */
    void setExplainPrompt(String explainPrompt) {   this.explainPrompt = explainPrompt; }

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
     * The preferred height of the interpretation window
     * @return the height value
     */
    double getInterpretationPrefHeight() { return interpretationDRTA.getEditor().getPrefHeight();  }

    /**
     * The preferred height of the interpretation window
     * @param interpretationPrefHeight the height value
     */
    void setInterpretationPrefHeight(double interpretationPrefHeight) { this.interpretationPrefHeight = interpretationPrefHeight;  }

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

    Spinner getTableGridHeightSpinner() { return tableGridHeightSpinner;  }

    Spinner getTableGridWidthSpinner() { return tableGridWidthSpinner;  }

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
    public Node getExerciseControl() {  return controlBox; }

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

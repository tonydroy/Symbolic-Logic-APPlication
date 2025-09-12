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

package slapp.editor.truth_table;

import com.gluonhq.richtextarea.RichTextArea;
import com.gluonhq.richtextarea.RichTextAreaSkin;
import com.gluonhq.richtextarea.model.Document;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.event.ActionEvent;
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
import slapp.editor.PrintUtilities;
import slapp.editor.decorated_rta.BoxedDRTA;
import slapp.editor.decorated_rta.DecoratedRTA;
import slapp.editor.main_window.ControlType;
import slapp.editor.main_window.ExerciseView;
import slapp.editor.main_window.MainWindowView;

import java.util.ArrayList;
import java.util.List;
import java.util.function.UnaryOperator;

/**
 * View for the truth table exercise
 */
public class TruthTableView implements ExerciseView<DecoratedRTA> {
    private MainWindowView mainView;
    private DecoratedRTA exerciseStatement = new DecoratedRTA();
    private double statementPrefHeight = 0;
    private double commentPrefHeight = 0;
    private DecoratedRTA exerciseComment = new DecoratedRTA();
    private Spinner<Double> statementHeightSpinner;
    private Spinner<Double> statementWidthSpinner;
    private Spinner<Double> commentHeightSpinner;
    private Spinner<Double> commentWidthSpinner;
    private Spinner<Double> tableGridHeightSpinner;
    private Spinner<Double> tableGridWidthSpinner;
    private Node currentSpinnerNode;
    private GridPane basicFormulasPane;
    private List<BoxedDRTA> basicFormulasBoxedDRTAList;
    private VBox controlBox;
    private Spinner rowsSpinner;
    Button setupTableButton;
    private GridPane tableGrid;
    private VBox centerBox;
    private double contentFixedHeight = -50;
    private List<TableHeadItem> tableHeadItemsList;
    private TextField[][]  tableFields; //list of text field columns
    private BoxedDRTA[] rowCommentsArray;
    private ToggleButton[] highlightButtons;
    private int tableRows = 0;
    private VBox[] sizers;
    private Pane endPane;
    private int formulaBoxHeight = 22;
    private BoxedDRTA focusedBoxedDRTA;
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
    private Stage  staticHelpStage;;

    /**
     * Construct the truth table view
     * @param mainView the main window
     */
    TruthTableView(MainWindowView mainView) {
        this.mainView = mainView;

        basicFormulasPane = new GridPane();
        basicFormulasPane.setVgap(10);
        basicFormulasBoxedDRTAList = new ArrayList<>();

        Label basicFormulasLabel = new Label("Basic Sentences:");
        Button addBasicFormulaButton = new Button("+");
        Button removeBasicFormulaButton = new Button("-");
        addBasicFormulaButton.setFont(new Font(16));
        addBasicFormulaButton.setPadding(new Insets(0,5,0,5));
        removeBasicFormulaButton.setFont(new Font(16));
        removeBasicFormulaButton.setPadding(new Insets(1,8,1,8));

        HBox controlButtonBox = new HBox(20, addBasicFormulaButton, removeBasicFormulaButton);
        VBox upperControlBox = new VBox(10, basicFormulasLabel, controlButtonBox);

        BoxedDRTA boxedDRTA = newFormulaDRTAField();
        basicFormulasBoxedDRTAList.add(boxedDRTA);
        updateBasicFormulasPaneFromList();

        //this is messy: can't get an event filter on the RTA itself to fire.  Why?
        basicFormulasPane.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
            KeyCode code = e.getCode();
            int index = basicFormulasBoxedDRTAList.indexOf(focusedBoxedDRTA);
            if (index >= 0) {
                if (code == KeyCode.DOWN || code == KeyCode.ENTER) {
                    if (index + 1 < basicFormulasBoxedDRTAList.size()) {
                        basicFormulasBoxedDRTAList.get(index + 1).getRTA().requestFocus();
                    }
                    e.consume();
                }
                if (code == KeyCode.UP) {
                    if (index > 0) {
                        basicFormulasBoxedDRTAList.get(index - 1).getRTA().requestFocus();
                    }
                    e.consume();
                }
            }
        });

        addBasicFormulaButton.setOnAction(e -> {
            basicFormulasBoxedDRTAList.add(newFormulaDRTAField());
            updateBasicFormulasPaneFromList();
        });
        removeBasicFormulaButton.setOnAction(e -> {
           int index = basicFormulasBoxedDRTAList.size();
           index--;
           if (index > 0) {
               basicFormulasBoxedDRTAList.remove(index);
               updateBasicFormulasPaneFromList();
           } else {
               EditorAlerts.showSimpleAlert("Cannot Remove", "A truth table must include at least one basic sentence.");
           }
        });

        Label rowNumLabel = new Label("Rows ");
        rowsSpinner = new Spinner<>(0,256,0);
        rowsSpinner.setPrefWidth(65);
        //rowsSpinner.setEditable(true);  //maybe setup: would need to verify input, update on setup button (rather than 'enter').
        HBox spinnerBox = new HBox(5, rowsSpinner, rowNumLabel);
        spinnerBox.setAlignment(Pos.CENTER_LEFT);

        setupTableButton = new Button("Setup");
        setupTableButton.setPrefWidth(100);

        controlBox = new VBox(20, upperControlBox, basicFormulasPane, spinnerBox, setupTableButton);
        controlBox.setPadding(new Insets(100, 20, 00, 20));
        controlBox.setMinWidth(150); controlBox.setMaxWidth(150);

        tableGrid = new GridPane();
        tableGrid.setPadding(new Insets(20,0,20,10));
        centerBox = new VBox(10, tableGrid);
        tableGrid.setStyle("-fx-border-color: gainsboro");

        pointsEarnedTextField = new TextField();
        pointsEarnedTextField.setPrefWidth(27);
        pointsEarnedTextField.setAlignment(Pos.CENTER_RIGHT);
        pointsEarnedTextField.setPadding(new Insets(0,5,0,3));
        UnaryOperator<TextFormatter.Change> filter = change -> {
            String text = change.getText();
            if (text.matches("[0-9]*")) {
                return change;
            }
            return null;
        };
        TextFormatter<String> textFormatter = new TextFormatter<>(filter);
        pointsEarnedTextField.setTextFormatter(textFormatter);
    }

    /**
     * Initialize table structure
     */
    void updateTableGridFromTableItems() {
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
        }
        tableRowConstraints.add(new RowConstraints(5));
        tableRowConstraints.add(new RowConstraints());
    }

    /**
     * Update the (left-hand) basic formulas pane from the basicFormulasBoxedDRTAList
     */
    void updateBasicFormulasPaneFromList() {
        basicFormulasPane.getChildren().clear();
        for (int i = 0; i < basicFormulasBoxedDRTAList.size(); i++) {
            basicFormulasPane.add(basicFormulasBoxedDRTAList.get(i).getBoxedRTA(),0, i);
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
        singleCharField.setMaxWidth(18);

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
    BoxedDRTA newFormulaDRTAField() {
        BoxedDRTA bdrta = new BoxedDRTA();
        bdrta.getDRTA().getKeyboardSelector().valueProperty().setValue(RichTextAreaSkin.KeyMapValue.ITALIC_AND_SANS);
        RichTextArea rta = bdrta.getRTA();
        rta.setMaxHeight(27);
        rta.setMinHeight(27);
        rta.setContentAreaWidth(200);
        rta.setPrefWidth(100);
        rta.getStylesheets().add("RichTextFieldWide.css");
        rta.setPromptText("Formula");
        rta.focusedProperty().addListener((ob, ov, nv) -> {
            if (nv) {
                mainView.editorInFocus(bdrta.getDRTA(), ControlType.FIELD);
                focusedBoxedDRTA = bdrta;
            }
        });
        return bdrta;
    }

    /**
     * Boxed DRTA for a comment field
     * @return the BoxedDRTA
     */
    BoxedDRTA newCommentBoxedDRTA() {
        BoxedDRTA bdrta = new BoxedDRTA();
        RichTextArea rta = bdrta.getRTA();
        rta.setMaxHeight(formulaBoxHeight);
        rta.setMinHeight(formulaBoxHeight);

        RichTextAreaSkin rtaSkin = (RichTextAreaSkin) rta.getSkin();
        rta.prefWidthProperty().bind(Bindings.max(Bindings.add(rtaSkin.nodesWidthProperty(), 6), 100));
        rtaSkin.nodesWidthProperty().addListener((observable, oldValue, newValue) -> {
            Platform.runLater(() -> rta.getParent().requestLayout());
        });
        rta.addEventFilter(KeyEvent.ANY, e -> {
            if (e.getCode() == KeyCode.ENTER) e.consume();
        });

        rta.getStylesheets().add("RichTextField.css");
 //       rta.setPromptText("Comment");
        rta.focusedProperty().addListener((ob, ov, nv) -> {
            if (nv) {
                mainView.editorInFocus(bdrta.getDRTA(), ControlType.FIELD);
            }
        });
        return bdrta;
    }

    /**
     * Setup size controls for statement, comment, and table grid
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

        /*
        statementRTA.addEventFilter(MouseEvent.MOUSE_CLICKED, e -> {
            if (currentSpinnerNode != statementRTA) {
                currentSpinnerNode = statementRTA;
                mainView.updateSizeSpinners(statementHeightSpinner, statementWidthSpinner);
            }
        });

         */

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

        /*
        commentRTA.addEventFilter(MouseEvent.MOUSE_CLICKED, e -> {
            if (currentSpinnerNode != commentRTA) {
                currentSpinnerNode = commentRTA;
                mainView.updateSizeSpinners(commentHeightSpinner, commentWidthSpinner);
            }
        });

         */

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
        tableGridWidthSpinner.setTooltip(new Tooltip("Width as % of selected paper"));
        tableGrid.widthProperty().addListener((ob, ov, nv) -> {
            tableGridWidthSpinner.getValueFactory().setValue((double) Math.round(tableGrid.getWidth() / mainView.getScalePageWidth() * 100));
        });

        /*
        tableGrid.addEventFilter(MouseEvent.MOUSE_CLICKED, e -> {
            if (currentSpinnerNode != tableGrid) {
                currentSpinnerNode = tableGrid;
                tableGridHeightSpinner.getValueFactory().setValue((double) Math.round(tableGrid.getHeight()/ mainView.getScalePageHeight() * 100.0));
                tableGridWidthSpinner.getValueFactory().setValue((double) Math.round(tableGrid.getWidth()/ mainView.getScalePageWidth() * 100.0));
                mainView.updateSizeSpinners(tableGridHeightSpinner, tableGridWidthSpinner);
            }
        });

         */

        //page size listeners
        mainView.scalePageHeightProperty().addListener((ob, ov, nv) -> {

            statementRTA.prefHeightProperty().unbind();
            statementHeightSpinner.getValueFactory().setValue((double) Math.round(statementHeightSpinner.getValue() * ov.doubleValue() / nv.doubleValue()));
            statementRTA.prefHeightProperty().bind(Bindings.max(45.0, Bindings.multiply(nv.doubleValue(), DoubleProperty.doubleProperty(statementHeightSpinner.getValueFactory().valueProperty()).divide(100.0))));

            commentRTA.prefHeightProperty().unbind();
            commentHeightSpinner.getValueFactory().setValue((double) Math.round(commentHeightSpinner.getValue() * ov.doubleValue() / nv.doubleValue()));
            commentRTA.prefHeightProperty().bind(Bindings.max(45.0, Bindings.multiply(nv.doubleValue(), DoubleProperty.doubleProperty(commentHeightSpinner.getValueFactory().valueProperty()).divide(100.0))));

            tableGridHeightSpinner.getValueFactory().setValue((double) Math.round(tableGrid.getHeight() / mainView.getScalePageHeight() * 100.0));
        });

        mainView.scalePageWidthProperty().addListener((ob, ov, nv) -> {
            tableGridWidthSpinner.getValueFactory().setValue((double) Math.round(tableGrid.getWidth() / mainView.getScalePageWidth() * 100.0));
        });

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

        //     checkProgButton = new Button("Check Progress");
        //     checkProgButton.setPrefWidth(105);
        //     checkProgButton.setTooltip(new Tooltip("Check tree progress so far."));
        VBox checksBox = new VBox(10, checkButton, checkTriesLabel);
        checksBox.setAlignment(Pos.CENTER);
        checkTriesLabel.setAlignment(Pos.CENTER);
//        checksBox.setMargin(checkProgButton, new Insets(0,0,10, 0));

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
            hrta.setPrefWidth(400);
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
            staticHelpStage.setX(Math.min(EditorMain.mainStage.getX() + EditorMain.mainStage.getWidth(), bounds.getMaxX() - 420));
            staticHelpStage.setY(Math.min(EditorMain.mainStage.getY() + 20, bounds.getMaxY() - 320));

            staticHelpStage.show();
        }
    }

    public Stage getStaticHelpStage() { return staticHelpStage; }





    private void setSizeSpinners() {

        tableGrid.addEventFilter(MouseEvent.MOUSE_CLICKED, e -> {
            if (currentSpinnerNode != tableGrid) {
                tableGrid.requestFocus();
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
     * The TextField[][] of table values
     * @return the array
     */
    TextField[][] getTableFields() { return tableFields; }

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
     * The BoxedDRTA[] of row comments
     * @return the array
     */
    BoxedDRTA[] getRowCommentsArray() { return rowCommentsArray; }

    /**
     * The ToggleButton[] of highlight buttons
     * @param highlightButtons the array
     */
    void setHighlightButtons(ToggleButton[] highlightButtons) {   this.highlightButtons = highlightButtons; }

    /**
     * The ToggleButton[] of highlight buttons
     * @return the array
     */
    ToggleButton[] getHighlightButtons() { return highlightButtons; }

    /**
     * The (left-hand) setup table button
     * @return the button
     */
    Button getSetupTableButton() { return setupTableButton; }

    /**
     * The list of basic formula Boxed DRTAs
     * @return the list
     */
    List<BoxedDRTA> getBasicFormulasBoxedDRTAList() {return basicFormulasBoxedDRTAList; }

    /**
     * The spinner to set the number of table rows
     * @return the spinner
     */
    Spinner getRowsSpinner() { return rowsSpinner;  }

    /**
     * The preferred height of the comment window
     * @return the height value
     */
    double getCommentPrefHeight() { return exerciseComment.getEditor().getPrefHeight();   }

    /**
     * The preferred height of the comment window
     * @param commentPrefHeight the height value
     */
    void setCommentPrefHeight(double commentPrefHeight) {  this.commentPrefHeight = commentPrefHeight;   }

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
    public Node getExerciseContentNode() { return centerBox;  }

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

    public String getCheckMessage() {
        return checkMessage;
    }

    public Button getCheckButton() {
        return checkButton;
    }
}

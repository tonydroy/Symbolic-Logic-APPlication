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
import com.gluonhq.richtextarea.model.Document;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import slapp.editor.DiskUtilities;
import slapp.editor.PrintUtilities;
import slapp.editor.decorated_rta.BoxedDRTA;
import slapp.editor.decorated_rta.DecoratedRTA;
import slapp.editor.main_window.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Controller for the truth table exercise
 */
public class TruthTableExercise implements Exercise<TruthTableModel, TruthTableView> {
    private MainWindow mainWindow;
    private TruthTableModel truthTableModel;
    private TruthTableView truthTableView;
    private MainWindowView mainView;
    private boolean exerciseModified = false;
    private ParseDocForTTable docParser;
    private int tableColumns = 0;
    private int tableRows = 0;


    /**
     * Create truth table exercise with rows!= 0
     * @param model the model
     * @param mainWindow the main window
     */
    public TruthTableExercise(TruthTableModel model, MainWindow mainWindow) {
        this.mainWindow = mainWindow;
        this.truthTableModel = model;
        if (model.getOriginalModel() == null) {model.setOriginalModel(model); }
        this.mainView = mainWindow.getMainView();
        this.truthTableView = new TruthTableView(mainView);
        docParser = new ParseDocForTTable(truthTableModel.getUnaryOperators(), truthTableModel.getBinaryOperators());
        tableRows = truthTableModel.getTableRows();
        setTruthTableView();
    }

    /**
     * Create truth table exercise with rows == 0
     * @param model the model
     * @param mainWindow
     * @param createEmpty dummy parameter to differentiate constructors
     */
    public TruthTableExercise(TruthTableModel model, MainWindow mainWindow, boolean createEmpty) {
        this.mainWindow = mainWindow;
        this.truthTableModel = model;
        if (model.getOriginalModel() == null) {model.setOriginalModel(model); }
        this.mainView = mainWindow.getMainView();
        this.truthTableView = new TruthTableView(mainView);
        docParser = new ParseDocForTTable(truthTableModel.getUnaryOperators(), truthTableModel.getBinaryOperators());
        tableRows = truthTableModel.getTableRows();
        generateEmptyTableModel();                                 //** the difference
        setTruthTableView();
    }

    /**
     * Set model for empty table
     */
    void generateEmptyTableModel() {
        setupHeadItemsFromModel();
        truthTableModel.setEmptyTableContents(tableColumns);
    }

    /*
     * Setup truth table view from model
     */
    private void setTruthTableView() {
        truthTableView.setStatementPrefHeight(truthTableModel.getStatementPrefHeight());
        truthTableView.setCommentPrefHeight(truthTableModel.getCommentPrefHeight());

        truthTableView.setPointsPossible(truthTableModel.getPointsPossible());
        if (truthTableModel.getPointsEarned() >= 0) truthTableView.getPointsEarnedTextField().setText(Integer.toString(truthTableModel.getPointsEarned()));
        truthTableView.getPointsEarnedTextField().textProperty().addListener((ob, ov, nv) -> {
            exerciseModified = true;
        });

        truthTableView.setTableRows(tableRows);

        //statement
        DecoratedRTA statementDRTA = new DecoratedRTA();
        RichTextArea statementEditor = statementDRTA.getEditor();
        statementEditor.getActionFactory().open(truthTableModel.getExerciseStatement()).execute(new ActionEvent());

        mainView.editorInFocus(statementDRTA, ControlType.STATEMENT);
        statementEditor.focusedProperty().addListener((o, ov, nv) -> {
            if (nv) {
                mainView.editorInFocus(statementDRTA, ControlType.STATEMENT);
            }
        });
        truthTableView.setExerciseStatement(statementDRTA);

        //comment
        DecoratedRTA commentDRTA = new DecoratedRTA();
        RichTextArea commentEditor = commentDRTA.getEditor();
        commentEditor.getActionFactory().open(truthTableModel.getExerciseComment()).execute(new ActionEvent());
        commentEditor.getActionFactory().saveNow().execute(new ActionEvent());
        mainView.editorInFocus(commentDRTA, ControlType.AREA);
        commentEditor.addEventHandler(KeyEvent.KEY_RELEASED, e -> {
            exerciseModified = true;
            double commentTextHeight = mainView.getRTATextHeight(commentEditor);
            truthTableModel.setCommentTextHeight(commentTextHeight);
        });
        commentEditor.focusedProperty().addListener((o, ov, nv) -> {
            if (nv) {
                mainView.editorInFocus(commentDRTA, ControlType.AREA);
            }
        });
        truthTableView.setExerciseComment(commentDRTA);

        //initialize basic formulas control
        List<Document> basicFormulaDocs = truthTableModel.getBasicFormulas();
        if (!basicFormulaDocs.isEmpty()) truthTableView.getBasicFormulasBoxedDRTAList().clear();
        for (Document doc : basicFormulaDocs ) {
            BoxedDRTA bdrta = truthTableView.newFormulaDRTAField();
            RichTextArea rta = bdrta.getRTA();
            rta.getActionFactory().open(doc).execute(new ActionEvent());
            truthTableView.getBasicFormulasBoxedDRTAList().add(bdrta);
        }
        truthTableView.updateBasicFormulasPaneFromList();
        truthTableView.getRowsSpinner().getValueFactory().setValue(tableRows);

        Button setupTableButton = truthTableView.getSetupTableButton();
        setupTableButton.setOnAction(e -> {
            tableRows = (int) truthTableView.getRowsSpinner().getValue();
            truthTableModel.setTableRows(tableRows);
            truthTableView.setTableRows(tableRows);

            List<BoxedDRTA> basicFormulasBoxedDRTAList = truthTableView.getBasicFormulasBoxedDRTAList();
            List<Document> newBasicFormulaDocs = new ArrayList<>();
            for (BoxedDRTA bdrta : basicFormulasBoxedDRTAList) {
                RichTextArea rta = bdrta.getRTA();
                rta.getActionFactory().saveNow().execute(new ActionEvent());
                Document formulaDoc = rta.getDocument();
                newBasicFormulaDocs.add(formulaDoc);
            }
            truthTableModel.setBasicFormulas(newBasicFormulaDocs);
            setupHeadItemsFromModel();
            truthTableModel.setEmptyTableContents(tableColumns);
            updateViewTableItems();
            truthTableView.updateTableGridFromTableItems();
            exerciseModified = true;
        });

        //table contents
        updateViewTableItems();
        truthTableView.updateTableGridFromTableItems();

        truthTableView.initializeViewDetails();
    }

    /*
     * Update view items from model
     */
    private void updateViewTableItems() {
        setupHeadItemsFromModel();

        Document[] commentDocs = truthTableModel.getRowComments();
        BoxedDRTA[] commentBoxedDRTAs = new BoxedDRTA[tableRows];
        for (int i = 0; i < commentDocs.length; i++) {
            Document doc = commentDocs[i];
            BoxedDRTA bdrta = truthTableView.newCommentBoxedDRTA();
            RichTextArea rta = bdrta.getRTA();
            rta.getActionFactory().open(doc).execute(new ActionEvent());
            rta.getActionFactory().saveNow().execute(new ActionEvent());
            commentBoxedDRTAs[i] = bdrta;
        }
        truthTableView.setRowCommentsArray(commentBoxedDRTAs);


        ToggleButton[] buttons = new ToggleButton[tableColumns];
        TextField[][] columns = new TextField[tableColumns][tableRows];
        List<TableHeadItem> tableHeadItems = truthTableView.getTableHeadItemsList();


        for (int i = 0; i < tableColumns; i++) {
            TableHeadItem headItem = tableHeadItems.get(i);
            if (!headItem.isBlankColumn()) {
                TextField[] column = new TextField[tableRows];
                for (int j = 0; j < tableRows; j++) {
                    TextField charField = truthTableView.newSingleCharTextField(i, j);
                    charField.setText(truthTableModel.getTableValues()[i][j]);
                    charField.textProperty().addListener((ob, ov, nv) -> exerciseModified = true);
                    column[j] = charField;
                }
                columns[i] = column;
                ToggleButton highlightButton = truthTableView.newHighlightButton(i);
                highlightButton.setSelected(truthTableModel.getColumnHighlights()[i]);
                highlightButton.selectedProperty().addListener((ob, ov, nv) -> exerciseModified = true);
                buttons[i] = highlightButton;

            }
        }
        truthTableView.setTableFields(columns);
        truthTableView.setHighlightButtons(buttons);
    }

    /*
     * Update view head items from model
     */
    private void setupHeadItemsFromModel() {

        List<TableHeadItem> headList = new ArrayList<>();

        //basic formulas head items
        List<Document> basicFormulas = truthTableModel.getBasicFormulas();
        for (int i = 0; i < basicFormulas.size(); i++) {
            List<TableHeadItem> basicHeadItems = docParser.generateHeadItems(basicFormulas.get(i));
            headList.addAll(basicHeadItems);
            TableHeadItem spaceColHead = new TableHeadItem(new TextFlow(new Text("")), new ColumnConstraints(10));
            spaceColHead.setBlankColumn(true);
            headList.add(spaceColHead);
        }
        if (basicFormulas.size() == 0) {
            TableHeadItem stubHead = new TableHeadItem(new TextFlow(new Text("  ")), new ColumnConstraints(10));
            stubHead.setBlankColumn(true);
            headList.add(stubHead);
        }
        TableHeadItem dividerHead = new TableHeadItem(new TextFlow(new Text("")), new ColumnConstraints(10));
        dividerHead.setDividerColumn(true); dividerHead.setBlankColumn(true);
        headList.add(dividerHead);


        //main formula head items
        List<Document> mainFormulas = truthTableModel.getMainFormulas();
        for (int i = 0; i < mainFormulas.size() - 1; i++) {
            List<TableHeadItem> mainHeadItems = docParser.generateHeadItems(mainFormulas.get(i));
            headList.addAll(mainHeadItems);
            TableHeadItem spaceColHead = new TableHeadItem(new TextFlow(new Text("")), new ColumnConstraints(10));
            spaceColHead.setBlankColumn(true);
            headList.add(spaceColHead);
        }
        if (truthTableModel.isConclusionDivider()) {
            ColumnConstraints slashItemConstraints = new ColumnConstraints(10);
            slashItemConstraints.setHalignment(HPos.RIGHT);
            TableHeadItem slashHeadItem = new TableHeadItem(new TextFlow(new Text("/")), slashItemConstraints);
            slashHeadItem.setBlankColumn(true);
            headList.add(slashHeadItem);
        }
        if (mainFormulas.size() > 0) {
            List<TableHeadItem> finalHeadItems = docParser.generateHeadItems(mainFormulas.get(mainFormulas.size() - 1));
            headList.addAll(finalHeadItems);
        }
        TableHeadItem spaceColHead = new TableHeadItem(new TextFlow(new Text("")), new ColumnConstraints(10));
        spaceColHead.setBlankColumn(true);
        headList.add(spaceColHead);

        truthTableView.setTableHeadItemsList(headList);
        tableColumns = headList.size();
    }

    /**
     * The truth table model
     * @return the model
     */
    @Override
    public TruthTableModel getExerciseModel() { return truthTableModel;  }

    /**
     * The truth table view
     * @return the view
     */
    @Override
    public TruthTableView getExerciseView() { return truthTableView; }

    /**
     * Save exercise to disk
     * @param saveAs true if "save as" should be invoked, and otherwise false
     */
    @Override
    public void saveExercise(boolean saveAs) {
        boolean success = DiskUtilities.saveExercise(saveAs, getTruthTableModelFromView());
        if (success) exerciseModified = false;
    }

    /**
     * List of nodes to be sent to printer for this exercise
     * @return the node list
     */
    @Override
    public List<Node> getPrintNodes(ExerciseModel originalModel) {
        List<Node> nodeList = new ArrayList<>();
        TruthTableModel printModel = truthTableModel;
        TruthTableExercise printExercise = this;

        double nodeWidth = PrintUtilities.getPageWidth() / mainWindow.getBaseScale();

        //header node
        Label exerciseName = new Label(printModel.getExerciseName());
        exerciseName.setStyle("-fx-font-weight: bold;");
        HBox hbox = new HBox(exerciseName);
        hbox.setPadding(new Insets(0,0,10,0));

        Group headerRoot = new Group();
        Scene headerScene = new Scene(headerRoot);
        headerRoot.getChildren().add(hbox);
        headerRoot.applyCss();
        headerRoot.layout();
        double boxHeight = hbox.getHeight();
        hbox.setPrefHeight(boxHeight);
        nodeList.add(hbox);
        Separator headerSeparator = new Separator(Orientation.HORIZONTAL);
        headerSeparator.setPrefWidth(nodeWidth);
        nodeList.add(headerSeparator);

        //statement node
        RichTextArea statementRTA = printExercise.getExerciseView().getExerciseStatement().getEditor();
        statementRTA.prefHeightProperty().unbind();
        statementRTA.minWidthProperty().unbind();
        double statementHeight = printModel.getStatementTextHeight();
        statementRTA.setPrefHeight(statementHeight + 35.0);
        statementRTA.setContentAreaWidth(nodeWidth);
        statementRTA.setMinWidth(nodeWidth);
        statementRTA.getStylesheets().clear(); statementRTA.getStylesheets().add("richTextAreaPrinter.css");
        nodeList.add(statementRTA);

        Separator statementSeparator = new Separator(Orientation.HORIZONTAL);
        statementSeparator.setPrefWidth(100);
        HBox statementSepBox = new HBox(statementSeparator);
        statementSepBox.setMinWidth(nodeWidth);
        statementSepBox.setPadding(new Insets(5,0,10,0));
        statementSepBox.setAlignment(Pos.CENTER);
        nodeList.add(statementSepBox);

        //content node
        GridPane tablePane = printExercise.getExerciseView().getTableGrid();
        ObservableList<Node> gridItems = tablePane.getChildren();
        ToggleButton[] buttons = printExercise.getExerciseView().getHighlightButtons();
        for (int i = 0; i < buttons.length; i ++) {
            tablePane.getChildren().remove(buttons[i]);
        }
        tablePane.setPadding(new Insets(15, 0, 15, 0));

        tablePane.setPrefWidth(printModel.getGridWidth());

        HBox gridBox = new HBox(tablePane);
        gridBox.setAlignment(Pos.CENTER);
        nodeList.add(gridBox);

        Separator contentSeparator = new Separator(Orientation.HORIZONTAL);
        contentSeparator.setStyle("-fx-stroke-dash-array:0.1 5.0");
        contentSeparator.setPrefWidth(100);
        HBox contentSepBox = new HBox(contentSeparator);
        contentSepBox.setMinWidth(nodeWidth);
        contentSepBox.setPadding(new Insets(10,0,5,0));
        contentSepBox.setAlignment(Pos.CENTER);
        nodeList.add(contentSepBox);

        //comment node
        RichTextArea commentRTA = printExercise.getExerciseView().getExerciseComment().getEditor();
        commentRTA.prefHeightProperty().unbind();
        commentRTA.minWidthProperty().unbind();
        commentRTA.setPrefHeight(printModel.getCommentTextHeight() + 35.0);
        commentRTA.setContentAreaWidth(nodeWidth);
        commentRTA.setMinWidth(nodeWidth);
        commentRTA.getStylesheets().clear(); commentRTA.getStylesheets().add("richTextAreaPrinter.css");

        Node commentNode;
        if (printModel.getPointsPossible() > 0) {
            Label pointsLabel = new Label(Integer.toString(printModel.getPointsEarned()) + "/" + Integer.toString(printModel.getPointsPossible()));
            AnchorPane anchorPane = new AnchorPane(commentRTA, pointsLabel);
            anchorPane.setTopAnchor(commentRTA, 0.0);
            anchorPane.setLeftAnchor(commentRTA, 0.0);
            anchorPane.setBottomAnchor(pointsLabel, 3.0);
            anchorPane.setRightAnchor(pointsLabel, 3.0);
            anchorPane.setPrefHeight(printModel.getCommentTextHeight() + 35);
            commentNode = anchorPane;
        }
        else commentNode = commentRTA;
        nodeList.add(commentNode);

        return nodeList;
    }

    /**
     * Return to the initial (unworked) version of the exercise, retaining the comment only.
     * @return the initial exercise
     */
    @Override
    public Exercise<TruthTableModel, TruthTableView> resetExercise() {
        RichTextArea commentRTA = truthTableView.getExerciseComment().getEditor();
        commentRTA.getActionFactory().saveNow().execute(new ActionEvent());
        Document commentDocument = commentRTA.getDocument();
        int pointsEarned = -1;
        if (!truthTableView.getPointsEarnedTextField().getText().equals("")) pointsEarned = Integer.parseInt(truthTableView.getPointsEarnedTextField().getText());
        TruthTableModel originalModel = (TruthTableModel) (truthTableModel.getOriginalModel());
        originalModel.setExerciseComment(commentDocument);
        originalModel.setPointsEarned(pointsEarned);
        TruthTableExercise clearExercise = new TruthTableExercise(originalModel, mainWindow, true);
        return clearExercise;
    }

    /**
     * Exercise is modified if it is changed relative to last save
     * @return true if exercise is modified, and otherwise false
     */
    @Override
    public boolean isExerciseModified() {
        RichTextArea commentEditor = truthTableView.getExerciseComment().getEditor();
        if (commentEditor.isModified()) exerciseModified = true;

        BoxedDRTA[] rowComments = truthTableView.getRowCommentsArray();
        for (int i = 0; i < rowComments.length; i++) {
            RichTextArea rowCommentEditor = rowComments[i].getRTA();
            if (rowCommentEditor.isModified()) exerciseModified = true;
        }
        return exerciseModified;
    }

    /**
     * Exercise is modified if it is changed relative to last save
     * @param modified true if exercise is modified, and otherwise false
     */
    @Override
    public void setExerciseModified(boolean modified) { exerciseModified = modified; }

    /**
     * The (null) view node to be displayed as a component of the free form exercise.
     * @return null
     */
    @Override
    public Node getFFViewNode() {return null;}
    @Override
    public Spinner getFFHeightSpinner() {return null;}
    @Override
    public Spinner getFFWidthSpinner() {return null;}

    /**
     * The (null) node to be printed as a component of the free form exercise.
     * @return null
     */
    @Override
    public Node getFFPrintNode(ExerciseModel originalModel) {return null;}

    /**
     * Extract an {@link slapp.editor.main_window.ExerciseModel} from view of the exercise
     * @return the exercise model
     */
    @Override
    public ExerciseModel<TruthTableModel> getExerciseModelFromView() {
        return (ExerciseModel) getTruthTableModelFromView();
    }

    /*
     * Extract the truth table model from view
     * @return the model
     */
    private TruthTableModel getTruthTableModelFromView() {
        TruthTableModel model = new TruthTableModel();
        model.setExerciseName(truthTableModel.getExerciseName());
        model.setOriginalModel(truthTableModel.getOriginalModel());
        model.setStarted(truthTableModel.isStarted() || exerciseModified);

        model.setStatementPrefHeight(truthTableView.getExerciseStatement().getEditor().getPrefHeight());
        model.setCommentPrefHeight(truthTableView.getCommentPrefHeight());

        model.setGridWidth(truthTableView.getTableGrid().getWidth());

        model.setExerciseStatement(truthTableModel.getExerciseStatement());
        model.setStatementTextHeight(truthTableModel.getStatementTextHeight());
        model.setCommentTextHeight(truthTableModel.getCommentTextHeight());


        RichTextArea commentRTA = truthTableView.getExerciseComment().getEditor();
        commentRTA.getActionFactory().saveNow().execute(new ActionEvent());
        model.setExerciseComment(commentRTA.getDocument());

        model.setUnaryOperators(truthTableModel.getUnaryOperators());
        model.setBinaryOperators(truthTableModel.getBinaryOperators());
        model.setMainFormulas(truthTableModel.getMainFormulas());

        List<BoxedDRTA> basicBoxedDRTAs = truthTableView.getBasicFormulasBoxedDRTAList();
        List<Document> basicDocs = new ArrayList<>();
        for (BoxedDRTA bdrta : basicBoxedDRTAs) {
            RichTextArea rta = bdrta.getRTA();
            rta.getActionFactory().saveNow().execute(new ActionEvent());
            Document doc = rta.getDocument();
            basicDocs.add(doc);
        }
        model.setBasicFormulas(basicDocs);

        String[][] tableStrVals = new String[tableColumns][tableRows];
        TextField[][] tableFields = truthTableView.getTableFields();
        for (int i = 0; i < tableColumns; i ++) {
            String[] column = new String[tableRows];
            for (int j = 0; j < tableRows; j++) {
                if (tableFields[i][j] != null) {
                    String value = tableFields[i][j].getText();
                    column[j] = value;
                }
            }
            tableStrVals[i] = column;
        }
        model.setTableValues(tableStrVals);

        BoxedDRTA[] rowCommentsArray = truthTableView.getRowCommentsArray();
        Document[] commentDocs = new Document[tableRows];
        for (int i = 0; i < tableRows; i++) {
            BoxedDRTA bdrta = rowCommentsArray[i];
            RichTextArea rta = bdrta.getRTA();
            rta.getActionFactory().saveNow().execute(new ActionEvent());
            Document doc = rta.getDocument();
            commentDocs[i] = doc;
        }
        model.setRowComments(commentDocs);

        ToggleButton[] highlightButtons = truthTableView.getHighlightButtons();
        boolean[] highlightValues = new boolean[tableColumns];
        for (int i = 0; i < tableColumns; i++) {
            if (highlightButtons[i] != null) {
                highlightValues[i] = highlightButtons[i].isSelected();
            }
        }
        model.setColumnHighlights(highlightValues);

        model.setConclusionDivider(truthTableModel.isConclusionDivider());
        model.setTableRows(tableRows);

        model.setPointsPossible(truthTableModel.getPointsPossible());
        if (!truthTableView.getPointsEarnedTextField().getText().equals("")) model.setPointsEarned(Integer.parseInt(truthTableView.getPointsEarnedTextField().getText()));
        else model.setPointsEarned(-1);


        return model;
    }

}

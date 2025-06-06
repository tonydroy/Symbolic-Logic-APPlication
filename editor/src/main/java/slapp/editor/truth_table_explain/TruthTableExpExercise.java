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
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
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
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import slapp.editor.DiskUtilities;
import slapp.editor.PrintUtilities;
import slapp.editor.decorated_rta.BoxedDRTA;
import slapp.editor.decorated_rta.DecoratedRTA;
import slapp.editor.main_window.*;
import slapp.editor.truth_table.ParseDocForTTable;
import slapp.editor.truth_table.TableHeadItem;
import slapp.editor.truth_table.TruthTableExercise;
import slapp.editor.truth_table.TruthTableModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Controller for the truth table explain exercise
 */
public class TruthTableExpExercise implements Exercise<TruthTableExpModel, TruthTableExpView> {
    private MainWindow mainWindow;
    private TruthTableExpModel truthTableExpModel;
    private TruthTableExpView truthTableExpView;
    private MainWindowView mainView;
    private boolean exerciseModified = false;
    private ParseDocForTTable docParser;
    private int tableColumns = 0;
    private int tableRows = 0;
    private ColumnConstraints spacerConstraint;



    /**
     * Create truth table exercise with rows!= 0
     * @param model the model
     * @param mainWindow the main window
     */
    public TruthTableExpExercise(TruthTableExpModel model, MainWindow mainWindow) {
        this.mainWindow = mainWindow;
        this.truthTableExpModel = model;
        if (model.getOriginalModel() == null) {model.setOriginalModel(model); }
        this.mainView = mainWindow.getMainView();
        this.truthTableExpView = new TruthTableExpView(mainView);
        docParser = new ParseDocForTTable(truthTableExpModel.getUnaryOperators(), truthTableExpModel.getBinaryOperators());
        tableRows = truthTableExpModel.getTableRows();
        setTruthTableView();
    }

    /**
     * Create truth table exercise with rows == 0
     * @param model the model
     * @param mainWindow
     * @param createEmpty dummy parameter to differentiate constructors
     */
    public TruthTableExpExercise(TruthTableExpModel model, MainWindow mainWindow, boolean createEmpty) {
        this.mainWindow = mainWindow;
        this.truthTableExpModel = model;
        if (model.getOriginalModel() == null) {model.setOriginalModel(model); }
        this.mainView = mainWindow.getMainView();
        this.truthTableExpView = new TruthTableExpView(mainView);
        docParser = new ParseDocForTTable(truthTableExpModel.getUnaryOperators(), truthTableExpModel.getBinaryOperators());
        tableRows = truthTableExpModel.getTableRows();
        generateEmptyTableModel();                                 //** the difference
        setTruthTableView();
    }

    /**
     * Set model for empty table
     */
    void generateEmptyTableModel() {
        setupHeadItemsFromModel();
        truthTableExpModel.setEmptyTableContents(tableColumns);
    }

    /*
     * Setup truth table explain view from model
     */
    private void setTruthTableView() {
        spacerConstraint = new ColumnConstraints(10);

        truthTableExpView.setExplainPrompt(truthTableExpModel.getExplainPrompt());
        truthTableExpView.setStatementPrefHeight(truthTableExpModel.getStatementPrefHeight());
        truthTableExpView.setCommentPrefHeight(truthTableExpModel.getCommentPrefHeight());
        truthTableExpView.setExplainPrefHeight(truthTableExpModel.getExplainPrefHeight());
        truthTableExpView.setTableRows(tableRows);

        truthTableExpView.setPointsPossible(truthTableExpModel.getPointsPossible());
        if (truthTableExpModel.getPointsEarned() >= 0) truthTableExpView.getPointsEarnedTextField().setText(Integer.toString(truthTableExpModel.getPointsEarned()));
        truthTableExpView.getPointsEarnedTextField().textProperty().addListener((ob, ov, nv) -> {
            exerciseModified = true;
        });

        //statement
        DecoratedRTA statementDRTA = new DecoratedRTA();
        RichTextArea statementEditor = statementDRTA.getEditor();
        statementEditor.getActionFactory().open(truthTableExpModel.getExerciseStatement()).execute(new ActionEvent());

        mainView.editorInFocus(statementDRTA, ControlType.STATEMENT);
        statementEditor.focusedProperty().addListener((o, ov, nv) -> {
            if (nv) {
                mainView.editorInFocus(statementDRTA, ControlType.STATEMENT);
            }
        });
        truthTableExpView.setExerciseStatement(statementDRTA);

        //comment
        DecoratedRTA commentDRTA = new DecoratedRTA();
        RichTextArea commentEditor = commentDRTA.getEditor();
        commentEditor.getActionFactory().open(truthTableExpModel.getExerciseComment()).execute(new ActionEvent());
        commentEditor.getActionFactory().saveNow().execute(new ActionEvent());
        mainView.editorInFocus(commentDRTA, ControlType.AREA);
        commentEditor.addEventHandler(KeyEvent.KEY_RELEASED, e -> {
            exerciseModified = true;
            double commentTextHeight = mainView.getRTATextHeight(commentEditor);
            truthTableExpModel.setCommentTextHeight(commentTextHeight);
        });
        commentEditor.focusedProperty().addListener((o, ov, nv) -> {
            if (nv) {
                mainView.editorInFocus(commentDRTA, ControlType.AREA);
            }
        });
        truthTableExpView.setExerciseComment(commentDRTA);

        //choices
        truthTableExpView.getChoiceLeadLabel().setText(truthTableExpModel.getChoiceLead());
        CheckBox aCheckBox = truthTableExpView.getaCheckBox();
        CheckBox bCheckBox = truthTableExpView.getbCheckBox();
        aCheckBox.setText(truthTableExpModel.getaPrompt());
        aCheckBox.setSelected(truthTableExpModel.isaSelected());
        aCheckBox.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue ob, Boolean ov, Boolean nv) {
                if (nv == true) bCheckBox.setSelected(false);
                exerciseModified = true;
            }
        });
        bCheckBox.setText(truthTableExpModel.getbPrompt());
        bCheckBox.setSelected(truthTableExpModel.isbSelected());
        bCheckBox.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue ob, Boolean ov, Boolean nv) {
                if (nv == true) aCheckBox.setSelected(false);
                exerciseModified = true;
            }
        });

        //explain
        DecoratedRTA explainDRTA = truthTableExpView.getExplainDRTA();
        RichTextArea explainEditor = explainDRTA.getEditor();
        explainEditor.getActionFactory().open(truthTableExpModel.getExplainDocument()).execute(new ActionEvent());
        explainEditor.setPromptText("Explain:");
        explainEditor.getActionFactory().saveNow().execute(new ActionEvent());
        mainView.editorInFocus(explainDRTA, ControlType.AREA);
        explainEditor.addEventHandler(KeyEvent.KEY_RELEASED, e -> {
            exerciseModified = true;
            double explainTextHeight = mainView.getRTATextHeight(commentEditor);
            truthTableExpModel.setExplainTextHeight(explainTextHeight);
        });
        explainEditor.focusedProperty().addListener((o, ov, nv) -> {
            if (nv) {
                mainView.editorInFocus(explainDRTA, ControlType.AREA);
            }
        });

        //initialize basic formulas control
        List<Document> basicFormulaDocs = truthTableExpModel.getBasicFormulas();
        if (!basicFormulaDocs.isEmpty()) truthTableExpView.getBasicFormulasBoxedDRTAs().clear();
        for (Document doc : basicFormulaDocs ) {
            BoxedDRTA bdrta = truthTableExpView.newFormulaBoxedDRTA();
            RichTextArea rta = bdrta.getRTA();
            rta.getActionFactory().open(doc).execute(new ActionEvent());
            truthTableExpView.getBasicFormulasBoxedDRTAs().add(bdrta);
        }
        truthTableExpView.updateBasicFormulasPaneFromList();
        truthTableExpView.getRowsSpinner().getValueFactory().setValue(tableRows);

        Button setupTableButton = truthTableExpView.getSetupTableButton();
        setupTableButton.setOnAction(e -> {
            tableRows = (int) truthTableExpView.getRowsSpinner().getValue();
            truthTableExpModel.setTableRows(tableRows);
            truthTableExpView.setTableRows(tableRows);

            List<BoxedDRTA> basicFormulasBoxedDRTAs = truthTableExpView.getBasicFormulasBoxedDRTAs();
            List<Document> newBasicFormulaDocs = new ArrayList<>();
            for (BoxedDRTA bdrta : basicFormulasBoxedDRTAs) {
                RichTextArea rta = bdrta.getRTA();
                rta.getActionFactory().saveNow().execute(new ActionEvent());
                Document formulaDoc = rta.getDocument();
                newBasicFormulaDocs.add(formulaDoc);
            }
            truthTableExpModel.setBasicFormulas(newBasicFormulaDocs);
            setupHeadItemsFromModel();
            truthTableExpModel.setEmptyTableContents(tableColumns);
            updateViewTableItems();
            truthTableExpView.updateTableGridFromTableItems();
            exerciseModified = true;
        });

        //table contents
        updateViewTableItems();
        truthTableExpView.updateTableGridFromTableItems();

        truthTableExpView.initializeViewDetails();
    }

    /*
     * Update view items from model
     */
    private void updateViewTableItems() {
        setupHeadItemsFromModel();

        Document[] commentDocs = truthTableExpModel.getRowComments();
        BoxedDRTA[] commentBoxedDRTAs = new BoxedDRTA[tableRows];
        for (int i = 0; i < commentDocs.length; i++) {
            Document doc = commentDocs[i];
            BoxedDRTA bdrta = truthTableExpView.newCommentBoxedDRTA();
            RichTextArea rta = bdrta.getRTA();
            rta.getActionFactory().open(doc).execute(new ActionEvent());
            rta.getActionFactory().saveNow().execute(new ActionEvent());
            commentBoxedDRTAs[i] = bdrta;
        }
        truthTableExpView.setRowCommentsArray(commentBoxedDRTAs);

        ToggleButton[] buttons = new ToggleButton[tableColumns];
        TextField[][] columns = new TextField[tableColumns][tableRows];
        List<TableHeadItem> tableHeadItems = truthTableExpView.getTableHeadItemsList();


        for (int i = 0; i < tableColumns; i++) {
            TableHeadItem headItem = tableHeadItems.get(i);
            if (!headItem.isBlankColumn()) {
                TextField[] column = new TextField[tableRows];
                for (int j = 0; j < tableRows; j++) {
                    TextField charField = truthTableExpView.newSingleCharTextField(i, j);
                    charField.setText(truthTableExpModel.getTableValues()[i][j]);
                    charField.textProperty().addListener((ob, ov, nv) -> exerciseModified = true);
                    column[j] = charField;
                }
                columns[i] = column;
                ToggleButton highlightButton = truthTableExpView.newHighlightButton(i);
                highlightButton.setSelected(truthTableExpModel.getColumnHighlights()[i]);
                highlightButton.selectedProperty().addListener((ob, ov, nv) -> exerciseModified = true);
                buttons[i] = highlightButton;

            }
        }
        truthTableExpView.setTableFields(columns);
        truthTableExpView.setHighlightButtons(buttons);
    }

    /*
     * Update view head items from model
     */
    private void setupHeadItemsFromModel() {

        List<TableHeadItem> headList = new ArrayList<>();

        //basic formulas head items
        List<Document> basicFormulas = truthTableExpModel.getBasicFormulas();
        for (int i = 0; i < basicFormulas.size(); i++) {
            List<TableHeadItem> basicHeadItems = docParser.generateHeadItems(basicFormulas.get(i));
            headList.addAll(basicHeadItems);
            TableHeadItem spaceColHead = new TableHeadItem(new TextFlow(new Text("")), spacerConstraint);
            spaceColHead.setBlankColumn(true);
            headList.add(spaceColHead);
        }
        if (basicFormulas.size() == 0) {
            TableHeadItem stubHead = new TableHeadItem(new TextFlow(new Text("  ")), spacerConstraint);
            stubHead.setBlankColumn(true);
            headList.add(stubHead);
        }
        TableHeadItem dividerHead = new TableHeadItem(new TextFlow(new Text("")), spacerConstraint);
        dividerHead.setDividerColumn(true); dividerHead.setBlankColumn(true);
        headList.add(dividerHead);


        //main formula head items
        List<Document> mainFormulas = truthTableExpModel.getMainFormulas();
        for (int i = 0; i < mainFormulas.size() - 1; i++) {
            List<TableHeadItem> mainHeadItems = docParser.generateHeadItems(mainFormulas.get(i));
            headList.addAll(mainHeadItems);
            TableHeadItem spaceColHead = new TableHeadItem(new TextFlow(new Text("")), spacerConstraint);
            spaceColHead.setBlankColumn(true);
            headList.add(spaceColHead);
        }
        if (truthTableExpModel.isConclusionDivider()) {
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
        TableHeadItem spaceColHead = new TableHeadItem(new TextFlow(new Text("")), spacerConstraint);
        spaceColHead.setBlankColumn(true);
        headList.add(spaceColHead);

        truthTableExpView.setTableHeadItemsList(headList);
        tableColumns = headList.size();
    }

    /**
     * The truth table explain model
     * @return the model
     */
    @Override
    public TruthTableExpModel getExerciseModel() { return truthTableExpModel;  }

    /**
     * The truth table explain view
     * @return the view
     */
    @Override
    public TruthTableExpView getExerciseView() { return truthTableExpView; }

    /**
     * Save exercise to disk
     * @param saveAs true if "save as" should be invoked, and otherwise false
     */
    @Override
    public void saveExercise(boolean saveAs) {
        boolean success = DiskUtilities.saveExercise(saveAs, getTruthTableExpModelFromView());
        if (success) exerciseModified = false;
    }

    /**
     * List of nodes to be sent to printer for this exercise
     * @return the node list
     */
    @Override
    public List<Node> getPrintNodes(ExerciseModel originalModel) {
        List<Node> nodeList = new ArrayList<>();
        TruthTableExpModel printModel = truthTableExpModel;
        TruthTableExpExercise printExercise = this;

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

        Label leaderLabel = new Label(truthTableExpModel.getChoiceLead());
        CheckBox boxA = new CheckBox(truthTableExpModel.getaPrompt());
        boxA.setSelected(truthTableExpModel.isaSelected());
        CheckBox boxB = new CheckBox(truthTableExpModel.getbPrompt());
        boxB.setSelected(truthTableExpModel.isbSelected());
        Font labelFont = new Font("Noto Serif Combo", 11);
        leaderLabel.setFont(labelFont); boxA.setFont(labelFont); boxB.setFont(labelFont);

        HBox abBox = new HBox(20);
        abBox.setPadding(new Insets(10,10,10,0));
        abBox.getChildren().addAll(leaderLabel, boxA, boxB);
        nodeList.add(abBox);

        RichTextArea explanationRTA = printExercise.getExerciseView().getExplainDRTA().getEditor();
        explanationRTA.prefHeightProperty().unbind();
        explanationRTA.minWidthProperty().unbind();
        explanationRTA.setPrefHeight(printModel.getExplainTextHeight() + 35.0);
        explanationRTA.setContentAreaWidth(nodeWidth);
        explanationRTA.setMinWidth(nodeWidth);
        explanationRTA.getStylesheets().clear(); statementRTA.getStylesheets().add("richTextAreaPrinter.css");
        nodeList.add(explanationRTA);

        Separator contentSeparator = new Separator(Orientation.HORIZONTAL);
        contentSeparator.setStyle("-fx-stroke-dash-array:0.1 5.0");
        contentSeparator.setPrefWidth(100);
        HBox contentSepBox = new HBox(contentSeparator);
        contentSepBox.setMinWidth(nodeWidth);
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
    public Exercise<TruthTableExpModel, TruthTableExpView> resetExercise() {
        RichTextArea commentRTA = truthTableExpView.getExerciseComment().getEditor();
        commentRTA.getActionFactory().saveNow().execute(new ActionEvent());
        Document commentDocument = commentRTA.getDocument();
        int pointsEarned = -1;
        if (!truthTableExpView.getPointsEarnedTextField().getText().equals("")) pointsEarned = Integer.parseInt(truthTableExpView.getPointsEarnedTextField().getText());
        TruthTableExpModel originalModel = (TruthTableExpModel) (truthTableExpModel.getOriginalModel());
        originalModel.setExerciseComment(commentDocument);
        originalModel.setPointsEarned(pointsEarned);
        TruthTableExpExercise clearExercise = new TruthTableExpExercise(originalModel, mainWindow, true);
        return clearExercise;
    }

    /**
     * Exercise is modified if it is changed relative to last save
     * @return true if exercise is modified, and otherwise false
     */
    @Override
    public boolean isExerciseModified() {

        RichTextArea commentEditor = truthTableExpView.getExerciseComment().getEditor();
        if (commentEditor.isModified()) exerciseModified = true;

        RichTextArea explanationEditor = truthTableExpView.getExplainDRTA().getEditor();
        if (explanationEditor.isModified()) exerciseModified = true;

        BoxedDRTA[] rowComments = truthTableExpView.getRowCommentsArray();
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
    public ExerciseModel<TruthTableExpModel> getExerciseModelFromView() {
        return (ExerciseModel) getTruthTableExpModelFromView();
    }

    /*
     * Extract the truth table explain model from view
     * @return the model
     */
    private TruthTableExpModel getTruthTableExpModelFromView() {
        TruthTableExpModel model = new TruthTableExpModel();
        model.setExerciseName(truthTableExpModel.getExerciseName());
        model.setExplainPrompt(truthTableExpModel.getExplainPrompt());
        model.setOriginalModel(truthTableExpModel.getOriginalModel());
        model.setStarted(truthTableExpModel.isStarted() || exerciseModified);
        model.setStatementPrefHeight(truthTableExpView.getExerciseStatement().getEditor().getPrefHeight());
        model.setCommentPrefHeight(truthTableExpView.getCommentPrefHeight());
        model.setExplainPrefHeight(truthTableExpView.getExplainPrefHeight());
        model.setStatementTextHeight(truthTableExpModel.getStatementTextHeight());
        model.setCommentTextHeight(truthTableExpModel.getCommentTextHeight());
        model.setExplainTextHeight(truthTableExpModel.getExplainTextHeight());
        model.setExerciseStatement(truthTableExpModel.getExerciseStatement());
        model.setGridWidth(truthTableExpView.getTableGrid().getWidth());

        RichTextArea commentRTA = truthTableExpView.getExerciseComment().getEditor();
        commentRTA.getActionFactory().saveNow().execute(new ActionEvent());
        model.setExerciseComment(commentRTA.getDocument());

        RichTextArea explainRTA = truthTableExpView.getExplainDRTA().getEditor();
        explainRTA.getActionFactory().saveNow().execute(new ActionEvent());
        model.setExplainDocument(explainRTA.getDocument());

        model.setUnaryOperators(truthTableExpModel.getUnaryOperators());
        model.setBinaryOperators(truthTableExpModel.getBinaryOperators());
        model.setMainFormulas(truthTableExpModel.getMainFormulas());

        List<BoxedDRTA> basicBoxedDRTAs = truthTableExpView.getBasicFormulasBoxedDRTAs();
        List<Document> basicDocs = new ArrayList<>();
        for (BoxedDRTA bdrta : basicBoxedDRTAs) {
            RichTextArea rta = bdrta.getRTA();
            rta.getActionFactory().saveNow().execute(new ActionEvent());
            Document doc = rta.getDocument();
            basicDocs.add(doc);
        }
        model.setBasicFormulas(basicDocs);

        String[][] tableStrVals = new String[tableColumns][tableRows];
        TextField[][] tableFields = truthTableExpView.getTableFields();
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

        BoxedDRTA[] lineCommentBoxedDRTAs = truthTableExpView.getRowCommentsArray();
        Document[] commentDocs = new Document[tableRows];
        for (int i = 0; i < tableRows; i++) {
            BoxedDRTA bdrta = lineCommentBoxedDRTAs[i];
            RichTextArea rta = bdrta.getRTA();
            rta.getActionFactory().saveNow().execute(new ActionEvent());
            Document doc = rta.getDocument();
            commentDocs[i] = doc;
        }
        model.setRowComments(commentDocs);

        ToggleButton[] highlightButtons = truthTableExpView.getHighlightButtons();
        boolean[] highlightValues = new boolean[tableColumns];
        for (int i = 0; i < tableColumns; i++) {
            if (highlightButtons[i] != null) {
                highlightValues[i] = highlightButtons[i].isSelected();
            }
        }
        model.setColumnHighlights(highlightValues);

        model.setConclusionDivider(truthTableExpModel.isConclusionDivider());
        model.setChoiceLead(truthTableExpModel.getChoiceLead());
        model.setaPrompt(truthTableExpModel.getaPrompt());
        model.setaSelected(truthTableExpView.getaCheckBox().isSelected());
        model.setbPrompt(truthTableExpModel.getbPrompt());
        model.setbSelected(truthTableExpView.getbCheckBox().isSelected());

        model.setTableRows(tableRows);

        model.setPointsPossible(truthTableExpModel.getPointsPossible());
        if (!truthTableExpView.getPointsEarnedTextField().getText().equals("")) model.setPointsEarned(Integer.parseInt(truthTableExpView.getPointsEarnedTextField().getText()));
        else model.setPointsEarned(-1);

        return model;
    }

}

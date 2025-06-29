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

import java.util.ArrayList;
import java.util.List;

public class TruthTableGenExercise implements Exercise<TruthTableGenModel, TruthTableGenView> {
    private MainWindow mainWindow;
    private TruthTableGenModel truthTableGenModel;
    private TruthTableGenView truthTableGenView;
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
    public TruthTableGenExercise(TruthTableGenModel model, MainWindow mainWindow) {
        this.mainWindow = mainWindow;
        this.truthTableGenModel = model;
        if (model.getOriginalModel() == null) {model.setOriginalModel(model); }
        this.mainView = mainWindow.getMainView();
        this.truthTableGenView = new TruthTableGenView(mainView);
        docParser = new ParseDocForTTable(truthTableGenModel.getUnaryOperators(), truthTableGenModel.getBinaryOperators());
        tableRows = truthTableGenModel.getTableRows();
        setTruthTableView();

    }

    /**
     * Create truth table exercise with rows == 0
     * @param model the model
     * @param mainWindow
     * @param createEmpty dummy parameter to differentiate constructors
     */
    public TruthTableGenExercise(TruthTableGenModel model, MainWindow mainWindow, boolean createEmpty) {
        this.mainWindow = mainWindow;
        this.truthTableGenModel = model;
        if (model.getOriginalModel() == null) {model.setOriginalModel(model); }
        this.mainView = mainWindow.getMainView();
        this.truthTableGenView = new TruthTableGenView(mainView);
        docParser = new ParseDocForTTable(truthTableGenModel.getUnaryOperators(), truthTableGenModel.getBinaryOperators());
        tableRows = truthTableGenModel.getTableRows();
        generateEmptyTableModel();                                 //** the difference
        setTruthTableView();
    }

    /**
     * Set model for empty table
     */
    void generateEmptyTableModel() {
        setupHeadItemsFromModel();
        truthTableGenModel.setEmptyTableContents(tableColumns);
        truthTableGenView.getTableGrid().setPrefHeight(100);
    }

    /*
     * Setup truth table explain view from model
     */
    private void setTruthTableView() {
        spacerConstraint = new ColumnConstraints(10);
        truthTableGenView.setInterpretationPrompt(truthTableGenModel.getInterpretationPrompt());

        truthTableGenView.setStatementPrefHeight(truthTableGenModel.getStatementPrefHeight());
        truthTableGenView.setCommentPrefHeight(truthTableGenModel.getCommentPrefHeight());
        truthTableGenView.setInterpretationPrefHeight(truthTableGenModel.getInterpretationPrefHeight());
        truthTableGenView.setExplainPrefHeight(truthTableGenModel.getExplainPrefHeight());

        truthTableGenView.setExplainPrompt(truthTableGenModel.getExplainPrompt());
        truthTableGenView.setTableRows(tableRows);

        truthTableGenView.setPointsPossible(truthTableGenModel.getPointsPossible());
        if (truthTableGenModel.getPointsEarned() >= 0) truthTableGenView.getPointsEarnedTextField().setText(Integer.toString(truthTableGenModel.getPointsEarned()));
        truthTableGenView.getPointsEarnedTextField().textProperty().addListener((ob, ov, nv) -> {
            exerciseModified = true;
        });

        //statement
        DecoratedRTA statementDRTA = new DecoratedRTA();
        RichTextArea statementEditor = statementDRTA.getEditor();
        statementEditor.getActionFactory().open(truthTableGenModel.getExerciseStatement()).execute(new ActionEvent());
        mainView.editorInFocus(statementDRTA, ControlType.STATEMENT);
        statementEditor.focusedProperty().addListener((o, ov, nv) -> {
            if (nv) {
                mainView.editorInFocus(statementDRTA, ControlType.STATEMENT);
            }
        });
        truthTableGenView.setExerciseStatement(statementDRTA);

        //comment
        DecoratedRTA commentDRTA = new DecoratedRTA();
        RichTextArea commentEditor = commentDRTA.getEditor();
        commentEditor.getActionFactory().open(truthTableGenModel.getExerciseComment()).execute(new ActionEvent());
        commentEditor.getActionFactory().saveNow().execute(new ActionEvent());
        mainView.editorInFocus(commentDRTA, ControlType.AREA);
        commentEditor.addEventHandler(KeyEvent.KEY_RELEASED, e -> {
            exerciseModified = true;
            double commentTextHeight = mainView.getRTATextHeight(commentEditor);
            truthTableGenModel.setCommentTextHeight(commentTextHeight);
        });
        commentEditor.focusedProperty().addListener((o, ov, nv) -> {
            if (nv) {
                mainView.editorInFocus(commentDRTA, ControlType.AREA);
            }
        });
        truthTableGenView.setExerciseComment(commentDRTA);

        //choices
        truthTableGenView.getChoiceLeadLabel().setText(truthTableGenModel.getChoiceLead());
        CheckBox aCheckBox = truthTableGenView.getaCheckBox();
        CheckBox bCheckBox = truthTableGenView.getbCheckBox();
        aCheckBox.setText(truthTableGenModel.getaPrompt());
        aCheckBox.setSelected(truthTableGenModel.isaSelected());
        aCheckBox.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue ob, Boolean ov, Boolean nv) {
                if (nv == true) bCheckBox.setSelected(false);
                exerciseModified = true;
            }
        });
        bCheckBox.setText(truthTableGenModel.getbPrompt());
        bCheckBox.setSelected(truthTableGenModel.isbSelected());
        bCheckBox.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue ob, Boolean ov, Boolean nv) {
                if (nv == true) aCheckBox.setSelected(false);
                exerciseModified = true;
            }
        });

        //interpretation
        DecoratedRTA interpretationDRTA = truthTableGenView.getInterpretationDRTA();
        RichTextArea interpretationEditor = interpretationDRTA.getEditor();
        interpretationEditor.getActionFactory().open(truthTableGenModel.getExerciseInterpretation()).execute(new ActionEvent());
        interpretationEditor.setPromptText("Interpretation/Translation:");
        interpretationEditor.getActionFactory().saveNow().execute(new ActionEvent());
        interpretationEditor.addEventHandler(KeyEvent.KEY_RELEASED, e -> {
            exerciseModified = true;
            double interpretationTextHeight = mainView.getRTATextHeight(interpretationEditor);
            truthTableGenModel.setInterpretationTextHeight(interpretationTextHeight);
        });
        mainView.editorInFocus(interpretationDRTA, ControlType.AREA);
        interpretationEditor.focusedProperty().addListener((o, ov, nv) -> {
            if (nv) {
                mainView.editorInFocus(interpretationDRTA, ControlType.AREA);
            }
        });
        truthTableGenView.setInterpretationDRTA(interpretationDRTA);

        //explain
        DecoratedRTA explainDRTA = truthTableGenView.getExplainDRTA();
        RichTextArea explainEditor = explainDRTA.getEditor();
        explainEditor.getActionFactory().open(truthTableGenModel.getExplainDocument()).execute(new ActionEvent());
        explainEditor.getActionFactory().saveNow().execute(new ActionEvent());
        explainEditor.addEventHandler(KeyEvent.KEY_RELEASED, e -> {
            exerciseModified = true;
            double explainTextHeight = mainView.getRTATextHeight(commentEditor);
            truthTableGenModel.setExplainTextHeight(explainTextHeight);
        });
        mainView.editorInFocus(explainDRTA, ControlType.AREA);
        explainEditor.focusedProperty().addListener((o, ov, nv) -> {
            if (nv) {
                mainView.editorInFocus(explainDRTA, ControlType.AREA);
            }
        });
        truthTableGenView.setExplainDRTA(explainDRTA);


        //initialize basic and main formula controls
        List<Document> basicFormulaDocs = truthTableGenModel.getBasicFormulas();
        if (!basicFormulaDocs.isEmpty()) truthTableGenView.getBasicFormulasBoxedDRTAs().clear();
        for (Document doc : basicFormulaDocs ) {
            BoxedDRTA bdrta = truthTableGenView.newFormulaBoxedDRTA();
            RichTextArea rta = bdrta.getRTA();
            rta.getActionFactory().open(doc).execute(new ActionEvent());
            truthTableGenView.getBasicFormulasBoxedDRTAs().add(bdrta);
        }
        truthTableGenView.updateBasicFormulasPaneFromList();


        List<Document> mainFormulaDocs = truthTableGenModel.getMainFormulas();
        if (!mainFormulaDocs.isEmpty()) truthTableGenView.getMainFormulasBoxedDRTAs().clear();
        for (Document doc : mainFormulaDocs ) {
            BoxedDRTA bdrta = truthTableGenView.newFormulaBoxedDRTA();
            RichTextArea rta = bdrta.getRTA();
            rta.getActionFactory().open(doc).execute(new ActionEvent());
            truthTableGenView.getMainFormulasBoxedDRTAs().add(bdrta);
        }
        truthTableGenView.updateMainFormulasPaneFromList();


        truthTableGenView.getRowsSpinner().getValueFactory().setValue(tableRows);

        Button setupTableButton = truthTableGenView.getSetupTableButton();
        setupTableButton.setOnAction(e -> {
            tableRows = (int) truthTableGenView.getRowsSpinner().getValue();
            truthTableGenModel.setTableRows(tableRows);
            truthTableGenView.setTableRows(tableRows);

            List<BoxedDRTA> basicFormulasBoxedDRTAs = truthTableGenView.getBasicFormulasBoxedDRTAs();
            List<Document> newBasicFormulaDocs = new ArrayList<>();
            for (BoxedDRTA bdrta : basicFormulasBoxedDRTAs) {
                RichTextArea rta = bdrta.getRTA();
                rta.getActionFactory().saveNow().execute(new ActionEvent());
                Document formulaDoc = rta.getDocument();
                newBasicFormulaDocs.add(formulaDoc);
            }
            truthTableGenModel.setBasicFormulas(newBasicFormulaDocs);

            List<BoxedDRTA> mainFormulasBoxedDRTAs = truthTableGenView.getMainFormulasBoxedDRTAs();
            List<Document> newMainFormulaDocs = new ArrayList<>();
            for (BoxedDRTA bdrta : mainFormulasBoxedDRTAs) {
                RichTextArea rta = bdrta.getRTA();
                rta.getActionFactory().saveNow().execute(new ActionEvent());
                Document formulaDoc = rta.getDocument();
                newMainFormulaDocs.add(formulaDoc);
            }
            truthTableGenModel.setMainFormulas(newMainFormulaDocs);

            setupHeadItemsFromModel();
            truthTableGenModel.setEmptyTableContents(tableColumns);
            updateViewTableItems();
            truthTableGenView.updateTableGridFromTableItems();
            exerciseModified = true;
        });

        //table contents
        updateViewTableItems();
        truthTableGenView.updateTableGridFromTableItems();

        truthTableGenView.initializeViewDetails();
    }

    /*
     * Update view items from model
     */
    private void updateViewTableItems() {
        setupHeadItemsFromModel();

        Document[] commentDocs = truthTableGenModel.getRowComments();
        BoxedDRTA[] commentBoxedDRTAs = new BoxedDRTA[tableRows];
        for (int i = 0; i < commentDocs.length; i++) {
            Document doc = commentDocs[i];
            BoxedDRTA bdrta = truthTableGenView.newCommentBoxedDRTA();
            RichTextArea rta = bdrta.getRTA();
            rta.getActionFactory().open(doc).execute(new ActionEvent());
            rta.getActionFactory().saveNow().execute(new ActionEvent());
            commentBoxedDRTAs[i] = bdrta;
        }
        truthTableGenView.setRowCommentsArray(commentBoxedDRTAs);


        ToggleButton[] buttons = new ToggleButton[tableColumns];
        TextField[][] columns = new TextField[tableColumns][tableRows];
        List<TableHeadItem> tableHeadItems = truthTableGenView.getTableHeadItemsList();


        for (int i = 0; i < tableColumns; i++) {
            TableHeadItem headItem = tableHeadItems.get(i);
            if (!headItem.isBlankColumn()) {
                TextField[] column = new TextField[tableRows];
                for (int j = 0; j < tableRows; j++) {
                    TextField charField = truthTableGenView.newSingleCharTextField(i, j);
                    charField.setText(truthTableGenModel.getTableValues()[i][j]);
                    charField.textProperty().addListener((ob, ov, nv) -> exerciseModified = true);
                    column[j] = charField;
                }
                columns[i] = column;
                ToggleButton highlightButton = truthTableGenView.newHighlightButton(i);
                highlightButton.setSelected(truthTableGenModel.getColumnHighlights()[i]);
                highlightButton.selectedProperty().addListener((ob, ov, nv) -> exerciseModified = true);
                buttons[i] = highlightButton;

            }
        }
        truthTableGenView.setTableFields(columns);
        truthTableGenView.setHighlightButtons(buttons);
    }

    /*
     * Update view head items from model
     */
    private void setupHeadItemsFromModel() {

        List<TableHeadItem> headList = new ArrayList<>();

        //basic formulas head items
        List<Document> basicFormulas = truthTableGenModel.getBasicFormulas();
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
        List<Document> mainFormulas = truthTableGenModel.getMainFormulas();
        for (int i = 0; i < mainFormulas.size() - 1; i++) {
            List<TableHeadItem> mainHeadItems = docParser.generateHeadItems(mainFormulas.get(i));
            headList.addAll(mainHeadItems);
            TableHeadItem spaceColHead = new TableHeadItem(new TextFlow(new Text("")), spacerConstraint);
            spaceColHead.setBlankColumn(true);
            headList.add(spaceColHead);
        }
        if (truthTableGenModel.isConclusionDivider()) {
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

        truthTableGenView.setTableHeadItemsList(headList);
        tableColumns = headList.size();
    }

    /**
     * The truth table generate model
     * @return the model
     */
    @Override
    public TruthTableGenModel getExerciseModel() { return truthTableGenModel;  }

    /**
     * The truth table generate view
     * @return the view
     */
    @Override
    public TruthTableGenView getExerciseView() { return truthTableGenView; }

    /**
     * Save exercise to disk
     * @param saveAs true if "save as" should be invoked, and otherwise false
     */
    @Override
    public void saveExercise(boolean saveAs) {
        boolean success = DiskUtilities.saveExercise(saveAs, getTruthTableGenModelFromView());
        if (success) exerciseModified = false;
    }

    /**
     * List of nodes to be sent to printer for this exercise
     * @return the node list
     */
    @Override
    public List<Node> getPrintNodes(ExerciseModel originalModel) {
        List<Node> nodeList = new ArrayList<>();
        TruthTableGenModel printModel = truthTableGenModel;
        TruthTableGenExercise printExercise = this;

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
        statementRTA.setPadding(new Insets(0,0,10,0));
        statementRTA.getStylesheets().clear(); statementRTA.getStylesheets().add("richTextAreaPrinter.css");
        nodeList.add(statementRTA);

        Separator statementSeparator = new Separator(Orientation.HORIZONTAL);
        statementSeparator.setPrefWidth(100);
        HBox statementSepBox = new HBox(statementSeparator);
        statementSepBox.setMinWidth(nodeWidth);
        statementSepBox.setAlignment(Pos.CENTER);
        nodeList.add(statementSepBox);

        //interpretation node
        RichTextArea interpretationRTA = printExercise.getExerciseView().getInterpretationDRTA().getEditor();
        interpretationRTA.setStyle("-fx-border-width: 0;");
        interpretationRTA.prefHeightProperty().unbind();
        interpretationRTA.minWidthProperty().unbind();
        double interpretationHeight = printModel.getInterpretationTextHeight();
        interpretationRTA.setPrefHeight(interpretationHeight + 35.0);
        interpretationRTA.setContentAreaWidth(nodeWidth);
        interpretationRTA.setMinWidth(nodeWidth);
        interpretationRTA.getStylesheets().clear(); interpretationRTA.getStylesheets().add("richTextAreaPrinter.css");
        nodeList.add(interpretationRTA);

        //content node
        BoxedDRTA[] rowCommentsArray = printExercise.getExerciseView().getRowCommentsArray();
        Double[] commentPrintWidths = truthTableGenModel.getCommentPrintWidths();
        for (int i = 0; i < rowCommentsArray.length; i++) {
            BoxedDRTA bDRTA = rowCommentsArray[i];
            RichTextArea rta = bDRTA.getRTA();
            rta.setMinWidth(commentPrintWidths[i]);
        }

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

        Label leaderLabel = new Label(truthTableGenModel.getChoiceLead());
        CheckBox boxA = new CheckBox(truthTableGenModel.getaPrompt());
        boxA.setSelected(truthTableGenModel.isaSelected());
        CheckBox boxB = new CheckBox(truthTableGenModel.getbPrompt());
        boxB.setSelected(truthTableGenModel.isbSelected());
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
    public Exercise<TruthTableGenModel, TruthTableGenView> resetExercise() {
        RichTextArea commentRTA = truthTableGenView.getExerciseComment().getEditor();
        commentRTA.getActionFactory().saveNow().execute(new ActionEvent());
        Document commentDocument = commentRTA.getDocument();
        int pointsEarned = -1;
        if (!truthTableGenView.getPointsEarnedTextField().getText().equals("")) pointsEarned = Integer.parseInt(truthTableGenView.getPointsEarnedTextField().getText());
        TruthTableGenModel originalModel = (TruthTableGenModel) (truthTableGenModel.getOriginalModel());
        originalModel.setExerciseComment(commentDocument);
        originalModel.setPointsEarned(pointsEarned);
        TruthTableGenExercise clearExercise = new TruthTableGenExercise(originalModel, mainWindow, true);
        return clearExercise;
    }

    /**
     * Exercise is modified if it is changed relative to last save
     * @return true if exercise is modified, and otherwise false
     */
    @Override
    public boolean isExerciseModified() {
        RichTextArea commentEditor = truthTableGenView.getExerciseComment().getEditor();
        if (commentEditor.isModified()) exerciseModified = true;

        RichTextArea interpretationEditor = truthTableGenView.getInterpretationDRTA().getEditor();
        if (interpretationEditor.isModified()) exerciseModified = true;

        RichTextArea explanationEditor = truthTableGenView.getExplainDRTA().getEditor();
        if (explanationEditor.isModified()) exerciseModified = true;

        BoxedDRTA[] rowComments = truthTableGenView.getRowCommentsArray();
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
     * The view node to be displayed as a component of the free form exercise
     * @return the node
     */
    @Override
    public Node getFFViewNode() { return truthTableGenView.getTableGrid();}
    @Override
    public Spinner getFFHeightSpinner() {return truthTableGenView.getTableGridHeightSpinner();}
    @Override
    public Spinner getFFWidthSpinner() {return truthTableGenView.getTableGridWidthSpinner();}

    /**
     * The node to be printed as a component of the free form exercise
     * @return the node
     */
    @Override
    public Node getFFPrintNode(ExerciseModel originalModel) {
        TruthTableGenModel printModel = truthTableGenModel;
        TruthTableGenExercise printExercise = this;
        double nodeWidth = PrintUtilities.getPageWidth() / mainWindow.getBaseScale();


        BoxedDRTA[] rowCommentsArray = printExercise.getExerciseView().getRowCommentsArray();
        Double[] commentPrintWidths = truthTableGenModel.getCommentPrintWidths();
        for (int i = 0; i < rowCommentsArray.length; i++) {
            BoxedDRTA bDRTA = rowCommentsArray[i];
            RichTextArea rta = bDRTA.getRTA();
            rta.setMinWidth(commentPrintWidths[i]);
        }

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

        return gridBox;
    }

    /**
     * Extract an {@link slapp.editor.main_window.ExerciseModel} from view of the exercise
     * @return the exercise model
     */
    @Override
    public ExerciseModel<TruthTableGenModel> getExerciseModelFromView() {
        return (ExerciseModel) getTruthTableGenModelFromView();
    }

    /*
     * Extract the truth table generate model from view
     * @return the model
     */
    private TruthTableGenModel getTruthTableGenModelFromView() {
        TruthTableGenModel model = new TruthTableGenModel();
        model.setExerciseName(truthTableGenModel.getExerciseName());
        model.setInterpretationPrompt(truthTableGenModel.getInterpretationPrompt());
        model.setExplainPrompt(truthTableGenModel.getExplainPrompt());
        model.setOriginalModel(truthTableGenModel.getOriginalModel());
        model.setStarted(truthTableGenModel.isStarted() || exerciseModified);
        model.setStatementPrefHeight(truthTableGenView.getExerciseStatement().getEditor().getPrefHeight());
        model.setCommentPrefHeight(truthTableGenView.getCommentPrefHeight());
        model.setInterpretationPrefHeight(truthTableGenView.getInterpretationPrefHeight());
        model.setExplainPrefHeight(truthTableGenView.getExplainPrefHeight());
        model.setStatementTextHeight(truthTableGenModel.getStatementTextHeight());
        model.setCommentTextHeight(truthTableGenModel.getCommentTextHeight());
        model.setExplainTextHeight(truthTableGenModel.getExplainTextHeight());
        model.setInterpretationTextHeight(truthTableGenModel.getInterpretationTextHeight());
        model.setGridWidth(truthTableGenView.getTableGrid().getWidth());

        model.setExerciseStatement(truthTableGenModel.getExerciseStatement());

        RichTextArea commentRTA = truthTableGenView.getExerciseComment().getEditor();
        commentRTA.getActionFactory().saveNow().execute(new ActionEvent());
        model.setExerciseComment(commentRTA.getDocument());

        RichTextArea interpretationRTA = truthTableGenView.getInterpretationDRTA().getEditor();
        interpretationRTA.getActionFactory().saveNow().execute(new ActionEvent());
        model.setExerciseInterpretation(interpretationRTA.getDocument());

        RichTextArea explainRTA = truthTableGenView.getExplainDRTA().getEditor();
        explainRTA.getActionFactory().saveNow().execute(new ActionEvent());
        model.setExplainDocument(explainRTA.getDocument());

        model.setUnaryOperators(truthTableGenModel.getUnaryOperators());
        model.setBinaryOperators(truthTableGenModel.getBinaryOperators());
        model.setMainFormulas(truthTableGenModel.getMainFormulas());

        List<BoxedDRTA> basicBoxedDRTAs = truthTableGenView.getBasicFormulasBoxedDRTAs();
        List<Document> basicDocs = new ArrayList<>();
        for (BoxedDRTA bdrta : basicBoxedDRTAs) {
            RichTextArea rta = bdrta.getRTA();
            rta.getActionFactory().saveNow().execute(new ActionEvent());
            Document doc = rta.getDocument();
            basicDocs.add(doc);
        }
        model.setBasicFormulas(basicDocs);

        List<BoxedDRTA> mainBoxedDRTAs = truthTableGenView.getMainFormulasBoxedDRTAs();
        List<Document> mainDocs = new ArrayList<>();
        for (BoxedDRTA bdrta : mainBoxedDRTAs) {
            RichTextArea rta = bdrta.getRTA();
            rta.getActionFactory().saveNow().execute(new ActionEvent());
            Document doc = rta.getDocument();
            mainDocs.add(doc);
        }
        model.setMainFormulas(mainDocs);


        String[][] tableStrVals = new String[tableColumns][tableRows];
        TextField[][] tableFields = truthTableGenView.getTableFields();
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

        BoxedDRTA[] lineCommentBoxedDRTAs = truthTableGenView.getRowCommentsArray();
        Document[] commentDocs = new Document[tableRows];
        Double[] commentPrintWidths = new Double[tableRows];
        for (int i = 0; i < tableRows; i++) {
            BoxedDRTA bdrta = lineCommentBoxedDRTAs[i];
            RichTextArea rta = bdrta.getRTA();
            rta.getActionFactory().saveNow().execute(new ActionEvent());
            Document doc = rta.getDocument();
            commentDocs[i] = doc;
            commentPrintWidths[i] = rta.getPrefWidth();
        }
        model.setRowComments(commentDocs);
        model.setCommentPrintWidths(commentPrintWidths);

        ToggleButton[] highlightButtons = truthTableGenView.getHighlightButtons();
        boolean[] highlightValues = new boolean[tableColumns];
        for (int i = 0; i < tableColumns; i++) {
            if (highlightButtons[i] != null) {
                highlightValues[i] = highlightButtons[i].isSelected();
            }
        }
        model.setColumnHighlights(highlightValues);

        model.setConclusionDivider(truthTableGenModel.isConclusionDivider());
        model.setChoiceLead(truthTableGenModel.getChoiceLead());
        model.setaPrompt(truthTableGenModel.getaPrompt());
        model.setaSelected(truthTableGenView.getaCheckBox().isSelected());
        model.setbPrompt(truthTableGenModel.getbPrompt());
        model.setbSelected(truthTableGenView.getbCheckBox().isSelected());

        model.setTableRows(tableRows);
        model.setPointsPossible(truthTableGenModel.getPointsPossible());
        if (!truthTableGenView.getPointsEarnedTextField().getText().equals("")) model.setPointsEarned(Integer.parseInt(truthTableGenView.getPointsEarnedTextField().getText()));
        else model.setPointsEarned(-1);


        return model;
    }

    int getTableColumns() { return tableColumns; }

}

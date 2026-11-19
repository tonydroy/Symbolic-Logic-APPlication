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
import com.gluonhq.richtextarea.model.Document;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.*;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;
import javafx.scene.transform.Scale;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import slapp.editor.DiskUtilities;
import slapp.editor.PrintUtilities;
import slapp.editor.decorated_rta.BoxedDRTA;
import slapp.editor.decorated_rta.DecoratedRTA;
import slapp.editor.EditorMain;
import slapp.editor.main_window.*;
import slapp.editor.truth_table.ParseDocForTTable;
import slapp.editor.truth_table.TableHeadItem;
import java.util.ArrayList;
import java.util.List;
import static slapp.editor.EditorMain.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;


public class TruthTableExpExercise implements Exercise<TruthTableExpModel, TruthTableExpView> {
    private MainWindow mainWindow;
    private TruthTableExpModel truthTableExpModel;
    private TruthTableExpView truthTableExpView;
    private TTExpCheck ttCheck;
    private MainWindowView mainView;
    private boolean exerciseModified = false;
    private ParseDocForTTable docParser;
    private int tableColumns = 0;
    private int tableRows = 0;
    private List<Integer> basicFormulaCols = new ArrayList<>();
    private List<Integer> mainFormulaCols = new ArrayList<>();

    private Exercise auxExerciseA;
    private Exercise auxExerciseB;
    private Stage tStage;
    private Pane thumbPane = new Pane();
    private Scene tScene = new Scene(new Pane());

    // SCORCHED EARTH RULE 1: The Asynchronous Ghost "Kill Switch"
    private boolean isTornDown = false;

    // SCORCHED EARTH RULE 3: Eradicate Anonymous Lambdas
    private ChangeListener<String> pointsEarnedListener;
    private ChangeListener<Boolean> statementFocusListener;
    private ChangeListener<Number> commentFullHeightListener;
    private ChangeListener<Boolean> commentModifiedListener;
    private ChangeListener<Boolean> commentFocusListener;
    private ChangeListener<Boolean> aCheckBoxListener;
    private ChangeListener<Boolean> bCheckBoxListener;
    private ChangeListener<Number> explainFullHeightListener;
    private ChangeListener<Boolean> explainModifiedListener;
    private ChangeListener<Boolean> explainFocusListener;
    private EventHandler<ActionEvent> setupTableButtonHandler;
    private EventHandler<MouseEvent> thumbPaneClickHandler;
    private ChangeListener<String> charFieldListener;
    private ChangeListener<Boolean> highlightButtonListener;

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

        ttCheck = new TTExpCheck(this);
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
        ttCheck = new TTExpCheck(this);
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
        if (charFieldListener == null) {
            charFieldListener = (ob, ov, nv) -> {
                if (isTornDown || mainView == null) return;
                exerciseModified = true;
            };
        }
        if (highlightButtonListener == null) {
            highlightButtonListener = (ob, ov, nv) -> {
                if (isTornDown || mainView == null) return;
                exerciseModified = true;
            };
        }

        truthTableExpView.setExplainPrompt(truthTableExpModel.getExplainPrompt());
        truthTableExpView.setStatementPrefHeight(truthTableExpModel.getStatementPrefHeight());
        truthTableExpView.setCommentPrefHeight(truthTableExpModel.getCommentPrefHeight());
        truthTableExpView.setExplainPrefHeight(truthTableExpModel.getExplainPrefHeight());
        truthTableExpView.setTableRows(tableRows);

        truthTableExpView.setPointsPossible(truthTableExpModel.getPointsPossible());
        if (truthTableExpModel.getPointsEarned() >= 0) truthTableExpView.getPointsEarnedTextField().setText(Integer.toString(truthTableExpModel.getPointsEarned()));

        pointsEarnedListener = (ob, ov, nv) -> {
            if (isTornDown || mainView == null) return;
            exerciseModified = true;
        };
        truthTableExpView.getPointsEarnedTextField().textProperty().addListener(pointsEarnedListener);

        //statement
        DecoratedRTA statementDRTA = new DecoratedRTA();
        RichTextArea statementEditor = statementDRTA.getEditor();
        statementEditor.getActionFactory().open(truthTableExpModel.getExerciseStatement()).execute(new ActionEvent());

        mainView.editorInFocus(statementDRTA, ControlType.STATEMENT);
        statementFocusListener = (o, ov, nv) -> {
            if (isTornDown || mainView == null) return;
            try {
                if (nv) {
                    mainView.editorInFocus(statementDRTA, ControlType.STATEMENT);
                    mainView.setRTATextHeight(statementEditor.getFullHeight());
                } else {
                    mainView.undoRTATextHeight();
                }
            } catch (Exception e) {}
        };
        statementEditor.focusedProperty().addListener(statementFocusListener);
        truthTableExpView.setExerciseStatement(statementDRTA);

        //comment
        DecoratedRTA commentDRTA = new DecoratedRTA();
        RichTextArea commentEditor = commentDRTA.getEditor();
        commentEditor.getActionFactory().open(truthTableExpModel.getExerciseComment()).execute(new ActionEvent());
        commentEditor.getActionFactory().saveNow().execute(new ActionEvent());

        final boolean[] commentPrimed = {false};
        commentFullHeightListener = (ob, ov, nv) -> {
            if (isTornDown || mainView == null || truthTableExpModel == null) return;
            try {
                if (!commentPrimed[0]) {
                    commentPrimed[0] = true;
                    return;
                }
                double height = (double) nv;
                mainView.setRTATextHeight(height);
                truthTableExpModel.setCommentTextHeight(height);
            } catch (Exception e) {}
        };
        commentEditor.fullHeightProperty().addListener(commentFullHeightListener);

        commentModifiedListener = (ob, ov, nv) -> {
            if (isTornDown || mainView == null) return;
            if (nv) exerciseModified = true;
        };
        commentEditor.modifiedProperty().addListener(commentModifiedListener);

        mainView.editorInFocus(commentDRTA, ControlType.AREA);
        commentFocusListener = (o, ov, nv) -> {
            if (isTornDown || mainView == null) return;
            try {
                if (nv) {
                    mainView.editorInFocus(commentDRTA, ControlType.AREA);
                    mainView.setRTATextHeight(commentEditor.getFullHeight());
                } else {
                    mainView.undoRTATextHeight();
                }
            } catch (Exception e) {}
        };
        commentEditor.focusedProperty().addListener(commentFocusListener);
        truthTableExpView.setExerciseComment(commentDRTA);

        //choices
        truthTableExpView.getChoiceLeadLabel().setText(truthTableExpModel.getChoiceLead());
        CheckBox aCheckBox = truthTableExpView.getaCheckBox();
        CheckBox bCheckBox = truthTableExpView.getbCheckBox();
        aCheckBox.setText(truthTableExpModel.getaPrompt());
        aCheckBox.setSelected(truthTableExpModel.isaSelected());

        aCheckBoxListener = (ob, ov, nv) -> {
            if (isTornDown || mainView == null) return;
            try {
                if (nv) bCheckBox.setSelected(false);
                exerciseModified = true;
            } catch (Exception e) {}
        };
        aCheckBox.selectedProperty().addListener(aCheckBoxListener);

        bCheckBox.setText(truthTableExpModel.getbPrompt());
        bCheckBox.setSelected(truthTableExpModel.isbSelected());

        bCheckBoxListener = (ob, ov, nv) -> {
            if (isTornDown || mainView == null) return;
            try {
                if (nv) aCheckBox.setSelected(false);
                exerciseModified = true;
            } catch (Exception e) {}
        };
        bCheckBox.selectedProperty().addListener(bCheckBoxListener);

        //explain
        DecoratedRTA explainDRTA = truthTableExpView.getExplainDRTA();
        RichTextArea explainEditor = explainDRTA.getEditor();
        explainEditor.getActionFactory().open(truthTableExpModel.getExplainDocument()).execute(new ActionEvent());
        explainEditor.setPromptText(truthTableExpModel.getExplainPrompt());
        explainEditor.getActionFactory().saveNow().execute(new ActionEvent());

        final boolean[] explainPrimed = {false};
        explainFullHeightListener = (ob, ov, nv) -> {
            if (isTornDown || mainView == null || truthTableExpModel == null) return;
            try {
                if (!explainPrimed[0]) {
                    explainPrimed[0] = true;
                    return;
                }
                double height = (double) nv;
                mainView.setRTATextHeight(height);
                truthTableExpModel.setExplainTextHeight(height);
            } catch (Exception e) {}
        };
        explainEditor.fullHeightProperty().addListener(explainFullHeightListener);

        explainModifiedListener = (ob, ov, nv) -> {
            if (isTornDown || mainView == null) return;
            if (nv) exerciseModified = true;
        };
        explainEditor.modifiedProperty().addListener(explainModifiedListener);

        mainView.editorInFocus(explainDRTA, ControlType.AREA);
        explainFocusListener = (o, ov, nv) -> {
            if (isTornDown || mainView == null) return;
            try {
                if (nv) {
                    mainView.editorInFocus(explainDRTA, ControlType.AREA);
                    mainView.setRTATextHeight(explainEditor.getFullHeight());
                } else {
                    mainView.undoRTATextHeight();
                }
            } catch (Exception e) {}
        };
        explainEditor.focusedProperty().addListener(explainFocusListener);

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
        setupTableButtonHandler = e -> {
            if (isTornDown || mainView == null || truthTableExpView == null || truthTableExpModel == null) return;
            try {
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
            } catch (Exception ex) {}
        };
        setupTableButton.setOnAction(setupTableButtonHandler);

        //table contents
        updateViewTableItems();
        truthTableExpView.updateTableGridFromTableItems();

        truthTableExpView.initializeViewDetails();
        truthTableExpView.setRightControlBox();

        Platform.runLater(() -> {
            if (isTornDown || mainView == null) return;
            if (mainWindow != null && mainWindow.getCurrentExercise() != this) return;
            try {
                if (auxExerciseA != null) {
                    thumbPane = auxImage();
                    if (truthTableExpView != null && truthTableExpView.getControlBox() != null) {
                        truthTableExpView.getControlBox().getChildren().add(thumbPane);
                    }
                }
            } catch (Exception e) {}
        });
    }

    private Pane auxImage() {
        Pane localThumbPane = new Pane();
        if (auxExerciseA != null && auxExerciseA.getAuxExerNode() != null)  {

            localThumbPane.setStyle("-fx-background-color: FAFAFA;");
            Node auxExerNode = auxExerciseA.getAuxExerNode();
            auxExerNode.setMouseTransparent(true);
            auxExerNode.setFocusTraversable(false);
            localThumbPane.getChildren().add(auxExerNode);

            Group thumbRoot = new Group();
            Scene scene = new Scene(thumbRoot);
            thumbRoot.getChildren().add(localThumbPane);
            thumbRoot.applyCss();
            thumbRoot.layout();
            double thumbWidth = localThumbPane.getLayoutBounds().getWidth();
            double scale = thumbWidth > 0 ? 85.0 / thumbWidth : .16;


            Rectangle clip = new Rectangle();
            clip.setWidth(800);
            clip.setHeight(400);
            localThumbPane.setClip(clip);

            localThumbPane.getTransforms().clear();
            localThumbPane.getTransforms().add(new Scale(scale, scale));

            thumbPaneClickHandler = event -> {
                if (isTornDown || mainView == null || auxExerciseB == null) return;
                try {
                    if (tStage == null || !tStage.isShowing()) {
                        Pane pane1 = new Pane(auxExerciseB.getAuxExerNode());
                        pane1.setDisable(true);

                        pane1.setId("aux-pane");
                        String css = "#aux-pane { -fx-opacity: 0.85; } \n" +
                                "#aux-pane * { -fx-opacity: 1.0; } \n" +
                                "#aux-pane .caret { -fx-opacity: 0.0; }";
                        String cssUri = "data:text/css;base64," +
                                java.util.Base64.getEncoder().encodeToString(css.getBytes(java.nio.charset.StandardCharsets.UTF_8));
                        pane1.getStylesheets().add(cssUri);

                        Pane spacerPane = new Pane();
                        spacerPane.prefHeightProperty().bind(pane1.heightProperty());
                        spacerPane.prefWidthProperty().bind(pane1.widthProperty());
                        Group group = new Group(spacerPane);
                        AnchorPane comboPane = new AnchorPane(group, pane1);
                        ScrollPane tPane = new ScrollPane(comboPane);
                        tPane.setFitToHeight(false);
                        tPane.setFitToWidth(false);

                        tScene.setRoot(tPane);
                        tPane.applyCss();
                        tPane.layout();

                        tStage = new Stage();
                        tStage.setScene(tScene);
                        tStage.setTitle(((ExerciseModel) auxExerciseB.getExerciseModel()).getExerciseName());
                        tStage.initStyle(StageStyle.UTILITY);

              //          tStage.initOwner(EditorMain.mainStage);
                        tStage.initModality(Modality.NONE);
                        Stage mainStage = EditorMain.mainStage;
                        tStage.setX(mainStage.getX() + mainStage.getWidth()  - 500);
                        tStage.setY(mainStage.getY() + mainStage.getHeight()  - 300);

                        tStage.setWidth(pane1.getWidth() + RTA_WIDTH_BMP);
                        tStage.setHeight(Math.min(mainStage.getHeight() - 100, pane1.getHeight() + RTA_SCREEN_BMP + 25));

                        tStage.show();

                        Platform.runLater(() -> {
                            if (isTornDown || mainView == null || tStage == null) return;
                            try {
                                double prefX = mainStage.getX() + mainStage.getWidth() - tStage.getWidth() + 50;
                                double prefY = mainStage.getY() + mainStage.getHeight() - tStage.getHeight()+ 50;
                                Rectangle2D bounds = MainWindowView.getCurrentScreenBounds();

                                tStage.setX(Math.min(prefX, bounds.getMaxX() - tStage.getWidth()));
                                tStage.setY(Math.min(prefY, bounds.getMaxY() - tStage.getHeight()));
                            } catch (Exception e) {}
                        });

                    } else {
                        tStage.close();
                    }
                } catch (Exception e) {}
            };
            localThumbPane.setOnMouseClicked(thumbPaneClickHandler);
        }
        return localThumbPane;
    }

    public void clearStandingPopups() {
        try {
            if (truthTableExpView != null && truthTableExpView.getStaticHelpStage() != null) truthTableExpView.getStaticHelpStage().close();
            if (tStage != null) tStage.close();
        } catch (Exception e) {}
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
                    if (charFieldListener != null) charField.textProperty().addListener(charFieldListener);
                    column[j] = charField;
                }
                columns[i] = column;
                ToggleButton highlightButton = truthTableExpView.newHighlightButton(i);
                highlightButton.setSelected(truthTableExpModel.getColumnHighlights()[i]);
                if (highlightButtonListener != null) highlightButton.selectedProperty().addListener(highlightButtonListener);
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
        basicFormulaCols = new ArrayList<>();
        mainFormulaCols = new ArrayList<>();

        //basic formulas head items
        List<Document> basicFormulas = truthTableExpModel.getBasicFormulas();
        for (int i = 0; i < basicFormulas.size(); i++) {
            List<TableHeadItem> basicHeadItems = docParser.generateHeadItems(basicFormulas.get(i));
            basicFormulaCols.add(headList.size());
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
        List<Document> mainFormulas = truthTableExpModel.getMainFormulas();
        for (int i = 0; i < mainFormulas.size() - 1; i++) {
            mainFormulaCols.add(headList.size());
            List<TableHeadItem> mainHeadItems = docParser.generateHeadItems(mainFormulas.get(i));
            headList.addAll(mainHeadItems);
            TableHeadItem spaceColHead = new TableHeadItem(new TextFlow(new Text("")), new ColumnConstraints(10));
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
            mainFormulaCols.add(headList.size());
            headList.addAll(finalHeadItems);
        }
        TableHeadItem spaceColHead = new TableHeadItem(new TextFlow(new Text("")), new ColumnConstraints(10));
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

        Region spacer = new Region();
        Label pointsLabel = new Label("");
        if (printModel.getPointsPossible() > 0)
            pointsLabel.setText("   " + Integer.toString(printModel.getPointsEarned()) + "/" + Integer.toString(printModel.getPointsPossible()));

        HBox hbox = new HBox(exerciseName, spacer, printCheckNode(), pointsLabel);
        hbox.setAlignment(Pos.CENTER_LEFT);
        hbox.setHgrow(spacer, Priority.ALWAYS);
        hbox.setPadding(new Insets(0,0,10,0));
        hbox.setMinWidth(nodeWidth);

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
        statementRTA.maxWidthProperty().unbind();
        statementRTA.setPrefHeight(printModel.getStatementTextHeight() + RTA_PRINT_BMP);
        statementRTA.setMaxWidth(nodeWidth);
        statementRTA.getStylesheets().clear(); statementRTA.getStylesheets().add("richTextAreaPrinter.css");
        nodeList.add(statementRTA);

        Separator statementSeparator = new Separator(Orientation.HORIZONTAL);
        statementSeparator.setPrefWidth(100);
        HBox statementSepBox = new HBox(statementSeparator);
        statementSepBox.setMinWidth(nodeWidth);
        statementSepBox.setAlignment(Pos.CENTER);
        nodeList.add(statementSepBox);

        //content node
        BoxedDRTA[] rowCommentsArray = printExercise.getExerciseView().getRowCommentsArray();
        Double[] commentPrintWidths = truthTableExpModel.getCommentPrintWidths();
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
        tablePane.setStyle("-fx-border-width: 0; -webkit-fx-border-color: transparent;");
        tablePane.setPrefWidth(printModel.getGridWidth() - RTA_WIDTH_BMP);

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
        explanationRTA.maxWidthProperty().unbind();
        explanationRTA.setPrefHeight(Math.max(40, printModel.getExplainTextHeight() + RTA_PRINT_BMP));
        explanationRTA.setMaxWidth(nodeWidth);

        explanationRTA.getStylesheets().clear(); explanationRTA.getStylesheets().add("richTextAreaPrinter.css");
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
        commentRTA.maxWidthProperty().unbind();
        commentRTA.setPrefHeight(Math.max(40, printModel.getCommentTextHeight() + RTA_PRINT_BMP));
        commentRTA.setMaxWidth(nodeWidth);
        commentRTA.getStylesheets().clear(); commentRTA.getStylesheets().add("richTextAreaPrinter.css");


        nodeList.add(commentRTA);

        return nodeList;
    }

    TextFlow printCheckNode() {
        TextFlow flow = new TextFlow();
        flow.setMaxWidth(200);
        if (ttCheck != null && ttCheck.isCheckSuccess()) {
            Text bigCheck = new Text("\ue89a");
            bigCheck.setFont(Font.font("Noto Serif Combo", 14));
            Text message = new Text("  " + truthTableExpView.getCheckMessage());
            message.setFont(Font.font("Noto Serif Combo", 11));

            if (ttCheck.isCheckFinal()) {
                bigCheck.setFill(Color.LAWNGREEN);
                message.setFill(Color.GREEN);
            }
            else {
                bigCheck.setFill(Color.ORCHID);
                message.setFill(Color.PURPLE);
            }
            flow.getChildren().addAll(bigCheck, message);
        }
        return flow;
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
        TextField pointsField = truthTableExpView.getPointsEarnedTextField();
        if ((pointsField.isFocused() || truthTableExpModel.getPointsEarned() >= 0) && pointsField.getText().isEmpty()) {
            pointsField.setText("0");
        }
        if (!pointsField.getText().equals("")) pointsEarned = Integer.parseInt(pointsField.getText());


        TruthTableExpModel originalModel = (TruthTableExpModel) (truthTableExpModel.getOriginalModel());
        originalModel.setExerciseComment(commentDocument);
        double commentHeight = commentRTA.getFullHeight();
        originalModel.setCommentTextHeight(commentHeight > 0 ? commentHeight : truthTableExpModel.getCommentTextHeight());

        originalModel.setPointsEarned(pointsEarned);

        TTExpCheckSetup setup = originalModel.getCheckSetup();
        if (setup == null) setup = new TTExpCheckSetup();
        if (ttCheck != null) {
            setup.setCheckTries(ttCheck.getCheckTries());
        }
        setup.setCheckSuccess(false);
        originalModel.setCheckSetup(setup);

        TruthTableExpExercise clearExercise = new TruthTableExpExercise(originalModel, mainWindow, true);
        return clearExercise;
    }

    /**
     * Exercise is modified if it is changed relative to last save
     * @return true if exercise is modified, and otherwise false
     */
    @Override
    public boolean isExerciseModified() {
        if (truthTableExpView == null) return exerciseModified;

        if (truthTableExpView.getExerciseComment() != null && truthTableExpView.getExerciseComment().getEditor() != null) {
            RichTextArea commentEditor = truthTableExpView.getExerciseComment().getEditor();
            if (commentEditor.isModified()) exerciseModified = true;
        }

        if (truthTableExpView.getExplainDRTA() != null && truthTableExpView.getExplainDRTA().getEditor() != null) {
            RichTextArea explanationEditor = truthTableExpView.getExplainDRTA().getEditor();
            if (explanationEditor.isModified()) exerciseModified = true;
        }

        BoxedDRTA[] rowComments = truthTableExpView.getRowCommentsArray();
        if (rowComments != null) {
            for (int i = 0; i < rowComments.length; i++) {
                if (rowComments[i] != null && rowComments[i].getRTA() != null) {
                    RichTextArea rowCommentEditor = rowComments[i].getRTA();
                    if (rowCommentEditor.isModified()) exerciseModified = true;
                }
            }
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
        model.setExerciseStatement(truthTableExpModel.getExerciseStatement());
        model.setGridWidth(truthTableExpView.getTableGrid().getWidth());
        model.setAuxExerName(truthTableExpModel.getAuxExerName());

        RichTextArea commentRTA = truthTableExpView.getExerciseComment().getEditor();
        commentRTA.getActionFactory().saveNow().execute(new ActionEvent());
        model.setExerciseComment(commentRTA.getDocument());
        double commentTextHeight = commentRTA.getFullHeight();
        model.setCommentTextHeight(commentTextHeight > 0 ? commentTextHeight : truthTableExpModel.getCommentTextHeight());


        RichTextArea explainRTA = truthTableExpView.getExplainDRTA().getEditor();
        explainRTA.getActionFactory().saveNow().execute(new ActionEvent());
        model.setExplainDocument(explainRTA.getDocument());
        double explainTextHeight = explainRTA.getFullHeight();
        model.setExplainTextHeight(explainTextHeight > 0 ? explainTextHeight : truthTableExpModel.getExplainTextHeight());

        TTExpCheckSetup checkSetup = truthTableExpModel.getCheckSetup();
        if (ttCheck != null) {
            checkSetup.setCheckTries(ttCheck.getCheckTries());
            checkSetup.setCheckSuccess(ttCheck.isCheckSuccess());
            checkSetup.setShortTable(truthTableExpView.getShortTableCheck().isSelected());
            checkSetup.setCheckMessage(truthTableExpView.getCheckMessage());
        }
        model.setCheckSetup(checkSetup);

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
        TextField pointsField = truthTableExpView.getPointsEarnedTextField();
        if ((pointsField.isFocused() || truthTableExpModel.getPointsEarned() >= 0) && pointsField.getText().isEmpty()) {
            pointsField.setText("0");
        }
        if (!pointsField.getText().equals("")) model.setPointsEarned(Integer.parseInt(pointsField.getText()));
        else model.setPointsEarned(-1);

        return model;
    }

    public List<Integer> getBasicFormulaCols() {
        return basicFormulaCols;
    }

    public List<Integer> getMainFormulaCols() {
        return mainFormulaCols;
    }

    public int getTableColumns() {
        return tableColumns;
    }

    public int getTableRows() {
        return tableRows;
    }

    public MainWindow getMainWindow() {
        return mainWindow;
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

    @Override
    public void cleanup() {
        // KILL SWITCH: Engaged! Prevent any focus listeners from doing UI operations mid-teardown
        isTornDown = true;

        // SCORCHED EARTH RULE 2: No Global Null-Poisoning (safe method only to avoid Toolbar Null-Poisoning)
        try {
            if (mainView != null) {
                mainView.textFieldInFocus(); // Remove toolbars for RTAs safely
            }
        } catch (Exception e) {}

        // SCORCHED EARTH RULE 5: Rule of Isolation applied via dozens of tiny isolated try-catch blocks
        try {
            if (truthTableExpView != null && truthTableExpView.localScalePageHeightProperty() != null) {
                truthTableExpView.localScalePageHeightProperty().unbind();
            }
        } catch (Exception e) {}

        try {
            if (truthTableExpView != null && truthTableExpView.localScalePageWidthProperty() != null) {
                truthTableExpView.localScalePageWidthProperty().unbind();
            }
        } catch (Exception e) {}

        try {
            if (mainView != null && mainView.getMainScene() != null && truthTableExpView != null && truthTableExpView.getExerciseFocusListener() != null) {
                mainView.getMainScene().focusOwnerProperty().removeListener(truthTableExpView.getExerciseFocusListener());
            }
        } catch (Exception e) {}

        try {
            if (truthTableExpView != null && truthTableExpView.getPointsEarnedTextField() != null && pointsEarnedListener != null) {
                truthTableExpView.getPointsEarnedTextField().textProperty().removeListener(pointsEarnedListener);
            }
        } catch (Exception e) {}

        try {
            if (truthTableExpView != null && truthTableExpView.getExerciseStatement() != null && truthTableExpView.getExerciseStatement().getEditor() != null && statementFocusListener != null) {
                truthTableExpView.getExerciseStatement().getEditor().focusedProperty().removeListener(statementFocusListener);
            }
        } catch (Exception e) {}

        try {
            if (truthTableExpView != null && truthTableExpView.getExerciseComment() != null && truthTableExpView.getExerciseComment().getEditor() != null) {
                if (commentFullHeightListener != null) truthTableExpView.getExerciseComment().getEditor().fullHeightProperty().removeListener(commentFullHeightListener);
                if (commentModifiedListener != null) truthTableExpView.getExerciseComment().getEditor().modifiedProperty().removeListener(commentModifiedListener);
                if (commentFocusListener != null) truthTableExpView.getExerciseComment().getEditor().focusedProperty().removeListener(commentFocusListener);
            }
        } catch (Exception e) {}

        try {
            if (truthTableExpView != null && truthTableExpView.getaCheckBox() != null && aCheckBoxListener != null) {
                truthTableExpView.getaCheckBox().selectedProperty().removeListener(aCheckBoxListener);
            }
        } catch (Exception e) {}

        try {
            if (truthTableExpView != null && truthTableExpView.getbCheckBox() != null && bCheckBoxListener != null) {
                truthTableExpView.getbCheckBox().selectedProperty().removeListener(bCheckBoxListener);
            }
        } catch (Exception e) {}

        try {
            if (truthTableExpView != null && truthTableExpView.getExplainDRTA() != null && truthTableExpView.getExplainDRTA().getEditor() != null) {
                if (explainFullHeightListener != null) truthTableExpView.getExplainDRTA().getEditor().fullHeightProperty().removeListener(explainFullHeightListener);
                if (explainModifiedListener != null) truthTableExpView.getExplainDRTA().getEditor().modifiedProperty().removeListener(explainModifiedListener);
                if (explainFocusListener != null) truthTableExpView.getExplainDRTA().getEditor().focusedProperty().removeListener(explainFocusListener);
            }
        } catch (Exception e) {}

        try {
            if (truthTableExpView != null && truthTableExpView.getSetupTableButton() != null && setupTableButtonHandler != null) {
                truthTableExpView.getSetupTableButton().removeEventHandler(ActionEvent.ACTION, setupTableButtonHandler);
                truthTableExpView.getSetupTableButton().setOnAction(null);
            }
        } catch (Exception e) {}

        try {
            if (thumbPane != null && thumbPaneClickHandler != null) {
                thumbPane.removeEventHandler(MouseEvent.MOUSE_CLICKED, thumbPaneClickHandler);
                thumbPane.setOnMouseClicked(null);
            }
        } catch (Exception e) {}

        try {
            if (truthTableExpView != null && truthTableExpView.getTableFields() != null && charFieldListener != null) {
                TextField[][] fields = truthTableExpView.getTableFields();
                for (int i = 0; i < fields.length; i++) {
                    if (fields[i] != null) {
                        for (int j = 0; j < fields[i].length; j++) {
                            if (fields[i][j] != null) {
                                fields[i][j].textProperty().removeListener(charFieldListener);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {}

        try {
            if (truthTableExpView != null && truthTableExpView.getHighlightButtons() != null && highlightButtonListener != null) {
                ToggleButton[] btns = truthTableExpView.getHighlightButtons();
                for (int i = 0; i < btns.length; i++) {
                    if (btns[i] != null) {
                        btns[i].selectedProperty().removeListener(highlightButtonListener);
                    }
                }
            }
        } catch (Exception e) {}

        // ---> SCORCHED EARTH RULE 4: THE JAVAFX SKIN HAMMER (With Synchronous Unbinding Only) <---
        try {
            if (truthTableExpView != null) {
                final RichTextArea stEditor = (truthTableExpView.getExerciseStatement() != null) ? truthTableExpView.getExerciseStatement().getEditor() : null;
                final RichTextArea commEditor = (truthTableExpView.getExerciseComment() != null) ? truthTableExpView.getExerciseComment().getEditor() : null;
                final RichTextArea explainEditor = (truthTableExpView.getExplainDRTA() != null) ? truthTableExpView.getExplainDRTA().getEditor() : null;

                final List<RichTextArea> rtasToClear = new ArrayList<>();
                if (stEditor != null) rtasToClear.add(stEditor);
                if (commEditor != null) rtasToClear.add(commEditor);
                if (explainEditor != null) rtasToClear.add(explainEditor);

                if (truthTableExpView.getBasicFormulasBoxedDRTAs() != null) {
                    for (BoxedDRTA bdrta : truthTableExpView.getBasicFormulasBoxedDRTAs()) {
                        if (bdrta != null && bdrta.getRTA() != null) {
                            rtasToClear.add(bdrta.getRTA());
                        }
                    }
                }

                if (truthTableExpView.getRowCommentsArray() != null) {
                    for (BoxedDRTA bdrta : truthTableExpView.getRowCommentsArray()) {
                        if (bdrta != null && bdrta.getRTA() != null) {
                            rtasToClear.add(bdrta.getRTA());
                        }
                    }
                }

                if (!rtasToClear.isEmpty()) {
                    // Synchronously unbind
                    for (RichTextArea rta : rtasToClear) {
                        try {
                            if (rta.prefHeightProperty() != null) rta.prefHeightProperty().unbind();
                            if (rta.minWidthProperty() != null) rta.minWidthProperty().unbind();
                            if (rta.maxWidthProperty() != null) rta.maxWidthProperty().unbind();
                        } catch (Exception ex) {}
                    }
                }
            }
        } catch (Exception e) {}

        // ---> 4. DISMANTLE THE UI TREE (Rule 6 & Rule 8 compliant) <---
        try {
            if (truthTableExpView != null) {
                if (truthTableExpView.getControlBox() != null) {
                    if (truthTableExpView.getControlBox().getChildren() != null) {
                        List<Node> childrenCopy = new ArrayList<>(truthTableExpView.getControlBox().getChildren());
                        for (Node node : childrenCopy) {
                            try { node.getClass().getMethod("cleanup").invoke(node); } catch(Exception e) {}
                            severFromParent(node);
                        }
                    }
                    truthTableExpView.getControlBox().getChildren().clear();
                }
                if (truthTableExpView.getTableGrid() != null) {
                    if (truthTableExpView.getTableGrid().getChildren() != null) {
                        List<Node> childrenCopy = new ArrayList<>(truthTableExpView.getTableGrid().getChildren());
                        for (Node node : childrenCopy) {
                            try { node.getClass().getMethod("cleanup").invoke(node); } catch(Exception e) {}
                            severFromParent(node);
                        }
                    }
                    truthTableExpView.getTableGrid().getChildren().clear();
                }
            }
        } catch (Exception e) {}

        try {
            if (thumbPane != null) {
                thumbPane.getChildren().clear();
                severFromParent(thumbPane);
            }
        } catch (Exception e) {}

        // 5. Clear OS-level popups
        try { clearStandingPopups(); } catch (Exception e) {}

        // 6. Cascade cleanup to auxiliary exercises
        try { if (auxExerciseA != null) auxExerciseA.cleanup(); } catch (Exception e) {}
        try { if (auxExerciseB != null) auxExerciseB.cleanup(); } catch (Exception e) {}

        // 7. Tell View & Check to specifically clean up
        try { if (truthTableExpView != null) truthTableExpView.cleanup(); } catch (Exception e) {}
        try { if (ttCheck != null) ttCheck.cleanup(); } catch (Exception e) {}

        // 8. Final severed ties
        try { this.mainWindow = null; } catch (Exception e) {}
        try { this.truthTableExpModel = null; } catch (Exception e) {}
        try { this.truthTableExpView = null; } catch (Exception e) {}
        try { this.ttCheck = null; } catch (Exception e) {}
        try { this.mainView = null; } catch (Exception e) {}
        try { this.docParser = null; } catch (Exception e) {}

        // SCORCHED EARTH RULE 10: NON-DESTRUCTIVE DATA TEARDOWN (never use .clear() on these fields)
        try { this.basicFormulaCols = null; } catch (Exception e) {}
        try { this.mainFormulaCols = null; } catch (Exception e) {}

        try { this.auxExerciseA = null; } catch (Exception e) {}
        try { this.auxExerciseB = null; } catch (Exception e) {}
        try { this.tScene = null; } catch (Exception e) {}
        try { this.tStage = null; } catch (Exception e) {}
        try { this.thumbPane = null; } catch (Exception e) {}

        try { this.pointsEarnedListener = null; } catch (Exception e) {}
        try { this.statementFocusListener = null; } catch (Exception e) {}
        try { this.commentFullHeightListener = null; } catch (Exception e) {}
        try { this.commentModifiedListener = null; } catch (Exception e) {}
        try { this.commentFocusListener = null; } catch (Exception e) {}
        try { this.aCheckBoxListener = null; } catch (Exception e) {}
        try { this.bCheckBoxListener = null; } catch (Exception e) {}
        try { this.explainFullHeightListener = null; } catch (Exception e) {}
        try { this.explainModifiedListener = null; } catch (Exception e) {}
        try { this.explainFocusListener = null; } catch (Exception e) {}
        try { this.setupTableButtonHandler = null; } catch (Exception e) {}
        try { this.thumbPaneClickHandler = null; } catch (Exception e) {}
        try { this.charFieldListener = null; } catch (Exception e) {}
        try { this.highlightButtonListener = null; } catch (Exception e) {}
    }

    @Override
    public Node getAuxExerNode() {
        if (truthTableExpView == null || truthTableExpModel == null || truthTableExpView.getCenterBox() == null) return new AnchorPane();
        try {
            RichTextArea explainRTA = truthTableExpView.getExplainDRTA().getEditor();
            explainRTA.prefHeightProperty().unbind();
            explainRTA.setMinHeight(truthTableExpModel.getExplainTextHeight() + RTA_SCREEN_BMP);
            return truthTableExpView.getCenterBox();
        }
        catch (Exception e) { return new AnchorPane(); }
    }

    @Override
    public String getAuxExerName() {
        if (truthTableExpModel == null) return "";
        return truthTableExpModel.getAuxExerName();
    }

    @Override
    public void setAuxExerA(Exercise exer) {
        auxExerciseA = exer;
    }

    @Override
    public void setAuxExerB(Exercise exer) { auxExerciseB = exer; }
}
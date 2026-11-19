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
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.scene.transform.Scale;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import slapp.editor.DiskUtilities;
import slapp.editor.EditorMain;
import slapp.editor.PrintUtilities;
import slapp.editor.decorated_rta.BoxedDRTA;
import slapp.editor.decorated_rta.DecoratedRTA;
import slapp.editor.main_window.*;
import slapp.editor.truth_table.ParseDocForTTable;
import slapp.editor.truth_table.TableHeadItem;

import java.util.ArrayList;
import java.util.List;

import static slapp.editor.EditorMain.RTA_PRINT_BMP;
import static slapp.editor.EditorMain.RTA_SCREEN_BMP;
import static slapp.editor.EditorMain.RTA_WIDTH_BMP;


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

    private List<Integer> basicFormulaCols = new ArrayList<>();
    private List<Integer> mainFormulaCols = new ArrayList<>();

    private Exercise auxExerciseA;
    private Exercise auxExerciseB;
    private Stage tStage;
    private Pane thumbPane = new Pane();
    private Scene tScene = new Scene(new Pane());

    // BUG FIX: The FreeForm focus-stealing shield
    private VBox ffWrapperPane;

    private TTGCheck ttgCheck;

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
    private ChangeListener<Number> interpFullHeightListener;
    private ChangeListener<Boolean> interpModifiedListener;
    private ChangeListener<Boolean> interpFocusListener;
    private ChangeListener<Number> explainFullHeightListener;
    private ChangeListener<Boolean> explainModifiedListener;
    private ChangeListener<Boolean> explainFocusListener;
    private EventHandler<ActionEvent> setupTableButtonHandler;
    private EventHandler<MouseEvent> thumbPaneClickHandler;
    private ChangeListener<String> charFieldListener;
    private ChangeListener<Boolean> highlightButtonListener;

    public TruthTableGenExercise(TruthTableGenModel model, MainWindow mainWindow) {
        this.mainWindow = mainWindow;
        this.truthTableGenModel = model;
        if (model.getOriginalModel() == null) {model.setOriginalModel(model); }
        this.mainView = mainWindow.getMainView();
        this.truthTableGenView = new TruthTableGenView(mainView);
        docParser = new ParseDocForTTable(truthTableGenModel.getUnaryOperatorList(), truthTableGenModel.getBinaryOperatorList());
        tableRows = truthTableGenModel.getTableRows();
        setTruthTableView();
    }

    public TruthTableGenExercise(TruthTableGenModel model, MainWindow mainWindow, boolean createEmpty) {
        this.mainWindow = mainWindow;
        this.truthTableGenModel = model;
        if (model.getOriginalModel() == null) {model.setOriginalModel(model); }
        this.mainView = mainWindow.getMainView();
        this.truthTableGenView = new TruthTableGenView(mainView);
        tableRows = truthTableGenModel.getTableRows();
        docParser = new ParseDocForTTable(truthTableGenModel.getUnaryOperatorList(), truthTableGenModel.getBinaryOperatorList());
        generateEmptyTableModel();
        setTruthTableView();
    }

    void generateEmptyTableModel() {
        if (isTornDown) return;
        setupHeadItemsFromModel();
        truthTableGenModel.setEmptyTableContents(tableColumns);
        if (truthTableGenView != null && truthTableGenView.getTableGrid() != null) {
            truthTableGenView.getTableGrid().setPrefHeight(100);
        }
    }

    private void setTruthTableView() {
        if (isTornDown || mainView == null || truthTableGenView == null || truthTableGenModel == null) return;

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

        spacerConstraint = new ColumnConstraints(10);
        truthTableGenView.setInterpretationPrompt(truthTableGenModel.getInterpretationPrompt());
        truthTableGenView.setStatementPrefHeight(truthTableGenModel.getStatementPrefHeight());
        truthTableGenView.setCommentPrefHeight(truthTableGenModel.getCommentPrefHeight());
        truthTableGenView.setInterpretationPrefHeight(truthTableGenModel.getInterpFnPrefHeight());
        truthTableGenView.setExplainPrefHeight(truthTableGenModel.getExplainPrefHeight());
        truthTableGenView.setExplainPrompt(truthTableGenModel.getExplainPrompt());
        truthTableGenView.setTableRows(tableRows);
        truthTableGenView.setPointsPossible(truthTableGenModel.getPointsPossible());
        truthTableGenView.setCheckTrans(truthTableGenModel.isCheckTrans());

        if (truthTableGenModel.getPointsEarned() >= 0 && truthTableGenView.getPointsEarnedTextField() != null) {
            truthTableGenView.getPointsEarnedTextField().setText(Integer.toString(truthTableGenModel.getPointsEarned()));
        }

        pointsEarnedListener = (ob, ov, nv) -> {
            if (isTornDown || mainView == null) return;
            exerciseModified = true;
        };
        if (truthTableGenView.getPointsEarnedTextField() != null) {
            truthTableGenView.getPointsEarnedTextField().textProperty().addListener(pointsEarnedListener);
        }

        DecoratedRTA statementDRTA = new DecoratedRTA();
        RichTextArea statementEditor = statementDRTA.getEditor();
        statementEditor.getActionFactory().open(truthTableGenModel.getExerciseStatement()).execute(new ActionEvent());

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
        truthTableGenView.setExerciseStatement(statementDRTA);

        DecoratedRTA commentDRTA = new DecoratedRTA();
        RichTextArea commentEditor = commentDRTA.getEditor();
        commentEditor.getActionFactory().open(truthTableGenModel.getExerciseComment()).execute(new ActionEvent());
        commentEditor.getActionFactory().saveNow().execute(new ActionEvent());

        final boolean[] commentPrimed = {false};
        commentFullHeightListener = (ob, ov, nv) -> {
            if (isTornDown || mainView == null || truthTableGenModel == null) return;
            try {
                if (!commentPrimed[0]) {
                    commentPrimed[0] = true;
                    return;
                }
                double height = (double) nv;
                mainView.setRTATextHeight(height);
                truthTableGenModel.setCommentTextHeight(height);
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
        truthTableGenView.setExerciseComment(commentDRTA);

        if (truthTableGenView.getChoiceLeadLabel() != null) truthTableGenView.getChoiceLeadLabel().setText(truthTableGenModel.getChoiceLead());
        CheckBox aCheckBox = truthTableGenView.getaCheckBox();
        CheckBox bCheckBox = truthTableGenView.getbCheckBox();
        if (aCheckBox != null && bCheckBox != null) {
            aCheckBox.setText(truthTableGenModel.getaPrompt());
            aCheckBox.setSelected(truthTableGenModel.isaSelected());
            aCheckBoxListener = (ob, ov, nv) -> {
                if (isTornDown || mainView == null) return;
                try {
                    if (nv) bCheckBox.setSelected(false);
                    exerciseModified = true;
                } catch (Exception e) {}
            };
            aCheckBox.selectedProperty().addListener(aCheckBoxListener);

            bCheckBox.setText(truthTableGenModel.getbPrompt());
            bCheckBox.setSelected(truthTableGenModel.isbSelected());
            bCheckBoxListener = (ob, ov, nv) -> {
                if (isTornDown || mainView == null) return;
                try {
                    if (nv) aCheckBox.setSelected(false);
                    exerciseModified = true;
                } catch (Exception e) {}
            };
            bCheckBox.selectedProperty().addListener(bCheckBoxListener);
        }

        DecoratedRTA interpretationDRTA = truthTableGenView.getInterpretationDRTA();
        if (interpretationDRTA != null) {
            RichTextArea interpretationEditor = interpretationDRTA.getEditor();
            interpretationEditor.getActionFactory().open(truthTableGenModel.getInterpFnDoc()).execute(new ActionEvent());
            interpretationEditor.getActionFactory().saveNow().execute(new ActionEvent());
            interpretationEditor.setPromptText("Interpretation/Translation:");
            interpretationEditor.setEditable(!truthTableGenModel.isInterpFnIncluded());

            final boolean[] interpPrimed = {false};
            interpFullHeightListener = (ob, ov, nv) -> {
                if (isTornDown || mainView == null || truthTableGenModel == null) return;
                try {
                    if (!interpPrimed[0]) {
                        interpPrimed[0] = true;
                        return;
                    }
                    double height = (double) nv;
                    mainView.setRTATextHeight(height);
                    truthTableGenModel.setInterpFnTextHeight(height);
                } catch (Exception e) {}
            };
            interpretationEditor.fullHeightProperty().addListener(interpFullHeightListener);

            interpModifiedListener = (ob, ov, nv) -> {
                if (isTornDown || mainView == null) return;
                if (nv) exerciseModified = true;
            };
            interpretationEditor.modifiedProperty().addListener(interpModifiedListener);

            mainView.editorInFocus(interpretationDRTA, ControlType.AREA);
            interpFocusListener = (o, ov, nv) -> {
                if (isTornDown || mainView == null) return;
                try {
                    if (nv) {
                        mainView.editorInFocus(interpretationDRTA, ControlType.AREA);
                        mainView.setRTATextHeight(interpretationEditor.getFullHeight());
                    } else {
                        mainView.undoRTATextHeight();
                    }
                } catch (Exception e) {}
            };
            interpretationEditor.focusedProperty().addListener(interpFocusListener);
            truthTableGenView.setInterpretationDRTA(interpretationDRTA);
        }

        DecoratedRTA explainDRTA = truthTableGenView.getExplainDRTA();
        if (explainDRTA != null) {
            RichTextArea explainEditor = explainDRTA.getEditor();
            explainEditor.getActionFactory().open(truthTableGenModel.getExplainDocument()).execute(new ActionEvent());
            explainEditor.getActionFactory().saveNow().execute(new ActionEvent());

            final boolean[] explainPrimed = {false};
            explainFullHeightListener = (ob, ov, nv) -> {
                if (isTornDown || mainView == null || truthTableGenModel == null) return;
                try {
                    if (!explainPrimed[0]) {
                        explainPrimed[0] = true;
                        return;
                    }
                    double height = (double) nv;
                    mainView.setRTATextHeight(height);
                    truthTableGenModel.setExplainTextHeight(height);
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
            truthTableGenView.setExplainDRTA(explainDRTA);
        }

        List<Document> basicFormulaDocs = truthTableGenModel.getBasicFormulas();
        if (basicFormulaDocs != null && truthTableGenView.getBasicFormulasBoxedDRTAs() != null) {
            if (!basicFormulaDocs.isEmpty()) truthTableGenView.getBasicFormulasBoxedDRTAs().clear();
            for (Document doc : basicFormulaDocs) {
                BoxedDRTA bdrta = truthTableGenView.newFormulaBoxedDRTA();
                RichTextArea rta = bdrta.getRTA();
                rta.getActionFactory().open(doc).execute(new ActionEvent());
                truthTableGenView.getBasicFormulasBoxedDRTAs().add(bdrta);
            }
            truthTableGenView.updateBasicFormulasPaneFromList();
        }

        List<Document> mainFormulaDocs = truthTableGenModel.getMainFormulas();
        if (mainFormulaDocs != null && truthTableGenView.getMainFormulasBoxedDRTAs() != null) {
            if (!mainFormulaDocs.isEmpty()) truthTableGenView.getMainFormulasBoxedDRTAs().clear();
            for (Document doc : mainFormulaDocs) {
                BoxedDRTA bdrta = truthTableGenView.newFormulaBoxedDRTA();
                RichTextArea rta = bdrta.getRTA();
                rta.getActionFactory().open(doc).execute(new ActionEvent());
                truthTableGenView.getMainFormulasBoxedDRTAs().add(bdrta);
            }
            truthTableGenView.updateMainFormulasPaneFromList();
        }

        if (truthTableGenView.getRowsSpinner() != null && truthTableGenView.getRowsSpinner().getValueFactory() != null) {
            truthTableGenView.getRowsSpinner().getValueFactory().setValue(tableRows);
        }

        Button setupTableButton = truthTableGenView.getSetupTableButton();
        if (setupTableButton != null) {
            setupTableButtonHandler = e -> {
                if (isTornDown || mainView == null || truthTableGenModel == null || truthTableGenView == null) return;
                try {
                    tableRows = truthTableGenView.getRowsSpinner().getValue();
                    truthTableGenModel.setTableRows(tableRows);
                    truthTableGenView.setTableRows(tableRows);

                    List<BoxedDRTA> basicFormulasBoxedDRTAs = truthTableGenView.getBasicFormulasBoxedDRTAs();
                    if (basicFormulasBoxedDRTAs != null) {
                        List<Document> newBasicFormulaDocs = new ArrayList<>();
                        for (BoxedDRTA bdrta : basicFormulasBoxedDRTAs) {
                            RichTextArea rta = bdrta.getRTA();
                            rta.getActionFactory().saveNow().execute(new ActionEvent());
                            newBasicFormulaDocs.add(rta.getDocument());
                        }
                        truthTableGenModel.setBasicFormulas(newBasicFormulaDocs);
                    }

                    List<BoxedDRTA> mainFormulasBoxedDRTAs = truthTableGenView.getMainFormulasBoxedDRTAs();
                    if (mainFormulasBoxedDRTAs != null) {
                        List<Document> newMainFormulaDocs = new ArrayList<>();
                        for (BoxedDRTA bdrta : mainFormulasBoxedDRTAs) {
                            RichTextArea rta = bdrta.getRTA();
                            rta.getActionFactory().saveNow().execute(new ActionEvent());
                            newMainFormulaDocs.add(rta.getDocument());
                        }
                        truthTableGenModel.setMainFormulas(newMainFormulaDocs);
                    }

                    setupHeadItemsFromModel();
                    truthTableGenModel.setEmptyTableContents(tableColumns);
                    updateViewTableItems();
                    truthTableGenView.updateTableGridFromTableItems();
                    exerciseModified = true;
                    truthTableGenModel.setSetupExer(true);
                } catch (Exception ex) {}
            };
            setupTableButton.setOnAction(setupTableButtonHandler);
        }

        //table contents
        updateViewTableItems();
        truthTableGenView.updateTableGridFromTableItems();
        truthTableGenView.initializeViewDetails();
        truthTableGenView.setRightControlBox();

        try {
            ttgCheck = new TTGCheck(this);
        }
        catch (Exception e) {}


        Platform.runLater(() -> {

            if (isTornDown || mainView == null) return;
            if (mainWindow != null && mainWindow.getCurrentExercise() != this) return;
            try {
                if (auxExerciseA != null) {
                    thumbPane = auxImage();
                    if (truthTableGenView != null && truthTableGenView.getControlBox() != null && thumbPane != null) {
                        truthTableGenView.getControlBox().getChildren().add(thumbPane);
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
                    //    tStage.initOwner(EditorMain.mainStage);
                        tStage.initModality(Modality.NONE);
                        Stage mainStage = EditorMain.mainStage;
                        tStage.setX(mainStage.getX() + mainStage.getWidth()  - 500);
                        tStage.setY(mainStage.getY() + mainStage.getHeight()  - 300);
                        tStage.setWidth(pane1.getWidth() + RTA_WIDTH_BMP);
                        tStage.setHeight(Math.min(mainStage.getHeight() - 100, pane1.getHeight() + RTA_SCREEN_BMP + 25));
                        tStage.show();

                        Platform.runLater(() -> {
                            if (isTornDown || mainView == null || tStage == null) return;
                            double prefX = mainStage.getX() + mainStage.getWidth() - tStage.getWidth() + 50;
                            double prefY = mainStage.getY() + mainStage.getHeight() - tStage.getHeight()+ 50;
                            Rectangle2D bounds = MainWindowView.getCurrentScreenBounds();

                            tStage.setX(Math.min(prefX, bounds.getMaxX() - tStage.getWidth()));
                            tStage.setY(Math.min(prefY, bounds.getMaxY() - tStage.getHeight()));
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
            if (truthTableGenView != null && truthTableGenView.getStaticHelpStage() != null) truthTableGenView.getStaticHelpStage().close();
            if (tStage != null) tStage.close();
        } catch (Exception e) {}
    }

    private void updateViewTableItems() {
        if (isTornDown || truthTableGenModel == null || truthTableGenView == null) return;
        setupHeadItemsFromModel();

        Document[] commentDocs = truthTableGenModel.getRowComments();
        if (commentDocs != null) {
            BoxedDRTA[] commentBoxedDRTAs = new BoxedDRTA[tableRows];
            for (int i = 0; i < commentDocs.length && i < tableRows; i++) {
                Document doc = commentDocs[i];
                if (doc != null) {
                    BoxedDRTA bdrta = truthTableGenView.newCommentBoxedDRTA();
                    RichTextArea rta = bdrta.getRTA();
                    rta.getActionFactory().open(doc).execute(new ActionEvent());
                    rta.getActionFactory().saveNow().execute(new ActionEvent());
                    commentBoxedDRTAs[i] = bdrta;
                }
            }
            truthTableGenView.setRowCommentsArray(commentBoxedDRTAs);
        }

        ToggleButton[] buttons = new ToggleButton[tableColumns];
        TextField[][] columns = new TextField[tableColumns][tableRows];
        List<TableHeadItem> tableHeadItems = truthTableGenView.getTableHeadItemsList();

        if (tableHeadItems != null) {
            for (int i = 0; i < tableColumns; i++) {
                if (i >= tableHeadItems.size()) break;
                TableHeadItem headItem = tableHeadItems.get(i);
                if (headItem != null && !headItem.isBlankColumn()) {
                    TextField[] column = new TextField[tableRows];
                    for (int j = 0; j < tableRows; j++) {
                        TextField charField = truthTableGenView.newSingleCharTextField(i, j);
                        if (truthTableGenModel.getTableValues() != null && truthTableGenModel.getTableValues().length > i && truthTableGenModel.getTableValues()[i] != null && truthTableGenModel.getTableValues()[i].length > j) {
                            charField.setText(truthTableGenModel.getTableValues()[i][j]);
                        }
                        if (charFieldListener != null) charField.textProperty().addListener(charFieldListener);
                        column[j] = charField;
                    }
                    columns[i] = column;
                    ToggleButton highlightButton = truthTableGenView.newHighlightButton(i);

                    if (truthTableGenModel.getColumnHighlights() != null && i < truthTableGenModel.getColumnHighlights().length) {
                        highlightButton.setSelected(truthTableGenModel.getColumnHighlights()[i]);
                    }
                    if (highlightButtonListener != null) highlightButton.selectedProperty().addListener(highlightButtonListener);
                    buttons[i] = highlightButton;
                }
            }
        }
        truthTableGenView.setTableFields(columns);
        truthTableGenView.setHighlightButtons(buttons);
    }

    private void setupHeadItemsFromModel() {
        if (isTornDown || truthTableGenModel == null || truthTableGenView == null || docParser == null) return;
        List<TableHeadItem> headList = new ArrayList<>();

        basicFormulaCols = new ArrayList<>();
        mainFormulaCols = new ArrayList<>();

        List<Document> basicFormulas = truthTableGenModel.getBasicFormulas();
        if (basicFormulas != null) {
            for (int i = 0; i < basicFormulas.size(); i++) {
                List<TableHeadItem> basicHeadItems = docParser.generateHeadItems(basicFormulas.get(i));
                if (basicHeadItems != null) {
                    basicFormulaCols.add(headList.size());
                    headList.addAll(basicHeadItems);
                    TableHeadItem spaceColHead = new TableHeadItem(new TextFlow(new Text("")), spacerConstraint);
                    spaceColHead.setBlankColumn(true);
                    headList.add(spaceColHead);
                }
            }
            if (basicFormulas.isEmpty()) {
                TableHeadItem stubHead = new TableHeadItem(new TextFlow(new Text("  ")), spacerConstraint);
                stubHead.setBlankColumn(true);
                headList.add(stubHead);
            }
        }

        TableHeadItem dividerHead = new TableHeadItem(new TextFlow(new Text("")), spacerConstraint);
        dividerHead.setDividerColumn(true);
        dividerHead.setBlankColumn(true);
        headList.add(dividerHead);

        List<Document> mainFormulas = truthTableGenModel.getMainFormulas();
        if (mainFormulas != null) {
            for (int i = 0; i < mainFormulas.size() - 1; i++) {
                mainFormulaCols.add(headList.size());
                List<TableHeadItem> mainHeadItems = docParser.generateHeadItems(mainFormulas.get(i));
                if (mainHeadItems != null) {
                    headList.addAll(mainHeadItems);
                    TableHeadItem spaceColHead = new TableHeadItem(new TextFlow(new Text("")), spacerConstraint);
                    spaceColHead.setBlankColumn(true);
                    headList.add(spaceColHead);
                }
            }
            if (truthTableGenModel.isConclusionDivider()) {
                ColumnConstraints slashItemConstraints = new ColumnConstraints(10);
                slashItemConstraints.setHalignment(HPos.RIGHT);
                TableHeadItem slashHeadItem = new TableHeadItem(new TextFlow(new Text("/")), slashItemConstraints);
                slashHeadItem.setBlankColumn(true);
                headList.add(slashHeadItem);
            }
            if (!mainFormulas.isEmpty()) {
                List<TableHeadItem> finalHeadItems = docParser.generateHeadItems(mainFormulas.get(mainFormulas.size() - 1));
                if (finalHeadItems != null) {
                    mainFormulaCols.add(headList.size());
                    headList.addAll(finalHeadItems);
                }
            }
        }

        TableHeadItem spaceColHead = new TableHeadItem(new TextFlow(new Text("")), spacerConstraint);
        spaceColHead.setBlankColumn(true);
        headList.add(spaceColHead);

        truthTableGenView.setTableHeadItemsList(headList);
        tableColumns = headList.size();
    }

    @Override
    public TruthTableGenModel getExerciseModel() { return truthTableGenModel;  }

    @Override
    public TruthTableGenView getExerciseView() { return truthTableGenView; }

    @Override
    public void saveExercise(boolean saveAs) {
        boolean success = DiskUtilities.saveExercise(saveAs, getTruthTableGenModelFromView());
        if (success) exerciseModified = false;
    }

    @Override
    public Node getFFPrintNode(ExerciseModel originalModel) {
        if (truthTableGenModel == null || truthTableGenView == null) return null;
        TruthTableGenModel printModel = truthTableGenModel;
        TruthTableGenExercise printExercise = this;
        double nodeWidth = PrintUtilities.getPageWidth() / mainWindow.getBaseScale();

        BoxedDRTA[] rowCommentsArray = printExercise.getExerciseView().getRowCommentsArray();
        Double[] commentPrintWidths = truthTableGenModel.getCommentPrintWidths();
        if (rowCommentsArray != null && commentPrintWidths != null) {
            for (int i = 0; i < rowCommentsArray.length && i < commentPrintWidths.length; i++) {
                if (rowCommentsArray[i] != null && rowCommentsArray[i].getRTA() != null && commentPrintWidths[i] != null) {
                    RichTextArea rta = rowCommentsArray[i].getRTA();
                    rta.setMinWidth(commentPrintWidths[i]);
                }
            }
        }

        GridPane tablePane = printExercise.getExerciseView().getTableGrid();
        ToggleButton[] buttons = printExercise.getExerciseView().getHighlightButtons();
        if (buttons != null) {
            for (int i = 0; i < buttons.length; i++) {
                if (buttons[i] != null) tablePane.getChildren().remove(buttons[i]);
            }
        }
        tablePane.setPadding(new Insets(15, 0, 15, 0));
        tablePane.setStyle("-fx-border-width: 0; -webkit-fx-border-color: transparent;");
        tablePane.setPrefWidth(printModel.getGridWidth() - RTA_WIDTH_BMP);

        HBox gridBox = new HBox(tablePane);
        gridBox.setAlignment(Pos.CENTER);
        return gridBox;
    }

    @Override
    public List<Node> getPrintNodes(ExerciseModel originalModel) {
        if (truthTableGenModel == null || truthTableGenView == null) return new ArrayList<>();
        List<Node> nodeList = new ArrayList<>();
        TruthTableGenModel printModel = truthTableGenModel;
        TruthTableGenExercise printExercise = this;

        double nodeWidth = PrintUtilities.getPageWidth() / mainWindow.getBaseScale();

        // header node
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

        // statement node
        if (printExercise.getExerciseView().getExerciseStatement() != null && printExercise.getExerciseView().getExerciseStatement().getEditor() != null) {
            RichTextArea statementRTA = printExercise.getExerciseView().getExerciseStatement().getEditor();
            statementRTA.prefHeightProperty().unbind();
            statementRTA.minWidthProperty().unbind();
            statementRTA.maxWidthProperty().unbind();
            statementRTA.setPrefHeight(printModel.getStatementTextHeight() + RTA_PRINT_BMP);
            statementRTA.setMaxWidth(nodeWidth);
            statementRTA.getStylesheets().clear(); statementRTA.getStylesheets().add("richTextAreaPrinter.css");
            nodeList.add(statementRTA);
        }

        Separator statementSeparator = new Separator(Orientation.HORIZONTAL);
        statementSeparator.setPrefWidth(100);
        HBox statementSepBox = new HBox(statementSeparator);
        statementSepBox.setMinWidth(nodeWidth);
        statementSepBox.setAlignment(Pos.CENTER);
        nodeList.add(statementSepBox);


        // interpretation node
        if (printModel.isCheckTrans() && printExercise.getExerciseView().getInterpretationDRTA() != null && printExercise.getExerciseView().getInterpretationDRTA().getEditor() != null) {
            RichTextArea interpretationRTA = printExercise.getExerciseView().getInterpretationDRTA().getEditor();
            interpretationRTA.setStyle("-fx-border-width: 0;");
            interpretationRTA.prefHeightProperty().unbind();
            interpretationRTA.minWidthProperty().unbind();
            interpretationRTA.maxWidthProperty().unbind();
            interpretationRTA.setPrefHeight(printModel.getInterpFnTextHeight() + RTA_PRINT_BMP);
            interpretationRTA.setMaxWidth(nodeWidth);
            interpretationRTA.getStylesheets().clear(); interpretationRTA.getStylesheets().add("richTextAreaPrinter.css");
            nodeList.add(interpretationRTA);
        }


        // content node
        BoxedDRTA[] rowCommentsArray = printExercise.getExerciseView().getRowCommentsArray();
        Double[] commentPrintWidths = truthTableGenModel.getCommentPrintWidths();
        if (rowCommentsArray != null && commentPrintWidths != null) {
            for (int i = 0; i < rowCommentsArray.length && i < commentPrintWidths.length; i++) {
                if (rowCommentsArray[i] != null && rowCommentsArray[i].getRTA() != null && commentPrintWidths[i] != null) {
                    BoxedDRTA bDRTA = rowCommentsArray[i];
                    RichTextArea rta = bDRTA.getRTA();
                    rta.setMinWidth(commentPrintWidths[i]);
                }
            }
        }

        if (printExercise.getExerciseView().getTableGrid() != null) {
            GridPane tablePane = printExercise.getExerciseView().getTableGrid();
            ToggleButton[] buttons = printExercise.getExerciseView().getHighlightButtons();
            if (buttons != null) {
                for (int i = 0; i < buttons.length; i ++) {
                    try {
                        if (buttons[i] != null) tablePane.getChildren().remove(buttons[i]);
                    } catch(Exception e) {}
                }
            }
            tablePane.setPadding(new Insets(15, 0, 15, 0));
            tablePane.setStyle("-fx-border-width: 0; -webkit-fx-border-color: transparent;");
            tablePane.setPrefWidth(printModel.getGridWidth() - RTA_WIDTH_BMP);
            HBox gridBox = new HBox(tablePane);
            gridBox.setAlignment(Pos.CENTER);
            nodeList.add(gridBox);
        }

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

        if (printExercise.getExerciseView().getExplainDRTA() != null && printExercise.getExerciseView().getExplainDRTA().getEditor() != null) {
            RichTextArea explanationRTA = printExercise.getExerciseView().getExplainDRTA().getEditor();
            explanationRTA.prefHeightProperty().unbind();
            explanationRTA.minWidthProperty().unbind();
            explanationRTA.maxWidthProperty().unbind();
            explanationRTA.setPrefHeight(Math.max(40, printModel.getExplainTextHeight() + RTA_PRINT_BMP));
            explanationRTA.setMaxWidth(nodeWidth);
            explanationRTA.getStylesheets().clear();
            explanationRTA.getStylesheets().add("richTextAreaPrinter.css");
            nodeList.add(explanationRTA);
        }

        Separator contentSeparator = new Separator(Orientation.HORIZONTAL);
        contentSeparator.setStyle("-fx-stroke-dash-array:0.1 5.0");
        contentSeparator.setPrefWidth(100);
        HBox contentSepBox = new HBox(contentSeparator);
        contentSepBox.setMinWidth(nodeWidth);
        contentSepBox.setAlignment(Pos.CENTER);
        nodeList.add(contentSepBox);

        // comment node
        if (printExercise.getExerciseView().getExerciseComment() != null && printExercise.getExerciseView().getExerciseComment().getEditor() != null) {
            RichTextArea commentRTA = printExercise.getExerciseView().getExerciseComment().getEditor();
            commentRTA.prefHeightProperty().unbind();
            commentRTA.minWidthProperty().unbind();
            commentRTA.maxWidthProperty().unbind();
            commentRTA.setPrefHeight(Math.max(40, printModel.getCommentTextHeight() + RTA_PRINT_BMP));
            commentRTA.setMaxWidth(nodeWidth);
            commentRTA.getStylesheets().clear(); commentRTA.getStylesheets().add("richTextAreaPrinter.css");


            nodeList.add(commentRTA);
        }

        return nodeList;
    }

    TextFlow printCheckNode() {
        TextFlow flow = new TextFlow();
        flow.setMaxWidth(200);
        if (ttgCheck != null && truthTableGenView != null && ttgCheck.isCheckSuccess()) {
            Text bigCheck = new Text("\ue89a");
            bigCheck.setFont(Font.font("Noto Serif Combo", 14));
            Text message = new Text("  " + truthTableGenView.getCheckMessage());
            message.setFont(Font.font("Noto Serif Combo", 11));

            bigCheck.setFill(Color.LAWNGREEN);
            message.setFill(Color.GREEN);

            flow.getChildren().addAll(bigCheck, message);
        }
        return flow;
    }

    @Override
    public Exercise<TruthTableGenModel, TruthTableGenView> resetExercise() {
        if (truthTableGenView == null || truthTableGenView.getExerciseComment() == null) return null;

        RichTextArea commentRTA = truthTableGenView.getExerciseComment().getEditor();
        commentRTA.getActionFactory().saveNow().execute(new ActionEvent());
        Document commentDocument = commentRTA.getDocument();

        int pointsEarned = -1;
        TextField pointsField = truthTableGenView.getPointsEarnedTextField();
        if ((pointsField.isFocused() || truthTableGenModel.getPointsEarned() >= 0) && pointsField.getText().isEmpty()) {
            pointsField.setText("0");
        }
        if (!pointsField.getText().equals("")) pointsEarned = Integer.parseInt(pointsField.getText());



        TruthTableGenModel originalModel = (TruthTableGenModel) (truthTableGenModel.getOriginalModel());
        if (originalModel != null) {
            originalModel.setExerciseComment(commentDocument);
            double commentHeight = commentRTA.getFullHeight();
            originalModel.setCommentTextHeight(commentHeight > 0 ? commentHeight : truthTableGenModel.getCommentTextHeight());

            originalModel.setPointsEarned(pointsEarned);

            TTGCheckSetup setup = originalModel.getCheckSetup();
            if (setup == null) setup = new TTGCheckSetup();
            if (ttgCheck != null) {
                setup.setCheckTries(ttgCheck.getCheckTries());
            }
            setup.setCheckSuccess(false);
            originalModel.setCheckSetup(setup);
        }

        TruthTableGenExercise clearExercise = new TruthTableGenExercise(originalModel, mainWindow, true);
        return clearExercise;
    }

    @Override
    public boolean isExerciseModified() {
        if (truthTableGenView == null) return false;

        if (truthTableGenView.getExerciseComment() != null && truthTableGenView.getExerciseComment().getEditor() != null) {
            RichTextArea commentEditor = truthTableGenView.getExerciseComment().getEditor();
            if (commentEditor.isModified()) exerciseModified = true;
        }

        if (truthTableGenView.getInterpretationDRTA() != null && truthTableGenView.getInterpretationDRTA().getEditor() != null) {
            RichTextArea interpretationEditor = truthTableGenView.getInterpretationDRTA().getEditor();
            if (interpretationEditor.isModified()) exerciseModified = true;
        }

        if (truthTableGenView.getExplainDRTA() != null && truthTableGenView.getExplainDRTA().getEditor() != null) {
            RichTextArea explanationEditor = truthTableGenView.getExplainDRTA().getEditor();
            if (explanationEditor.isModified()) exerciseModified = true;
        }

        BoxedDRTA[] rowComments = truthTableGenView.getRowCommentsArray();
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

    @Override
    public void setExerciseModified(boolean modified) { exerciseModified = modified; }

    @Override
    public Node getFFViewNode() {
        if (truthTableGenView == null || truthTableGenView.getTableGrid() == null) return new GridPane();

        // BUG FIX: Wrap the tableGrid in a customized non-focusable Pane.
        // This explicitly blocks FreeFormExercise's handleMouseEvent() from successfully
        // forcing 'requestFocus()' onto this root node, thereby preventing it from
        // silently stealing focus away from nested TextFields!
        if (ffWrapperPane == null) {
            ffWrapperPane = new VBox(truthTableGenView.getTableGrid()) {
                @Override
                public void requestFocus() {
                    // Refuse forceful focus interception. Let JavaFX organically handle
                    // the target focus on the inner TextFields.
                }
            };
            ffWrapperPane.setAlignment(Pos.CENTER);
        } else {
            if (truthTableGenView.getTableGrid().getParent() != ffWrapperPane) {
                ffWrapperPane.getChildren().setAll(truthTableGenView.getTableGrid());
            }
        }
        return ffWrapperPane;
    }

    @Override
    public Spinner getFFHeightSpinner() {
        if (truthTableGenView == null) return new Spinner();
        return truthTableGenView.getTableGridHeightSpinner();
    }

    @Override
    public Spinner getFFWidthSpinner() {
        if (truthTableGenView == null) return new Spinner();
        return truthTableGenView.getTableGridWidthSpinner();
    }

    @Override
    public slapp.editor.main_window.ExerciseModel<TruthTableGenModel> getExerciseModelFromView() {
        return (ExerciseModel) getTruthTableGenModelFromView();
    }

    private TruthTableGenModel getTruthTableGenModelFromView() {
        if (truthTableGenModel == null || truthTableGenView == null) return null;
        TruthTableGenModel model = new TruthTableGenModel();
        model.setExerciseName(truthTableGenModel.getExerciseName());
        model.setAuxExerName(truthTableGenModel.getAuxExerName());
        model.setInterpretationPrompt(truthTableGenModel.getInterpretationPrompt());
        model.setExplainPrompt(truthTableGenModel.getExplainPrompt());
        model.setOriginalModel(truthTableGenModel.getOriginalModel());
        model.setStarted(truthTableGenModel.isStarted() || exerciseModified);
        model.setSetupExer(truthTableGenModel.isSetupExer());

        TTGCheckSetup checkSetup = truthTableGenModel.getCheckSetup();
        if (ttgCheck != null && checkSetup != null) {
            checkSetup.setCheckTries(ttgCheck.getCheckTries());
            checkSetup.setCheckSuccess(ttgCheck.isCheckSuccess());
            checkSetup.setCheckMessage(truthTableGenView.getCheckMessage());
            if (ttgCheck.getCheckSetup() != null) {
                checkSetup.setKeyInterpFnDoc(ttgCheck.getCheckSetup().keyInterpFnDoc);
            }
        }
        model.setCheckSetup(checkSetup);

        if (truthTableGenView.getExerciseStatement() != null && truthTableGenView.getExerciseStatement().getEditor() != null) {
            model.setStatementPrefHeight(truthTableGenView.getExerciseStatement().getEditor().getPrefHeight());
        }
        model.setCommentPrefHeight(truthTableGenView.getCommentPrefHeight());
        model.setInterpFnPrefHeight(truthTableGenView.getInterpretationPrefHeight());
        model.setExplainPrefHeight(truthTableGenView.getExplainPrefHeight());
        model.setStatementTextHeight(truthTableGenModel.getStatementTextHeight());


        if (truthTableGenView.getTableGrid() != null) {
            model.setGridWidth(truthTableGenView.getTableGrid().getWidth());
        }

        model.setExerciseStatement(truthTableGenModel.getExerciseStatement());

        if (truthTableGenView.getExerciseComment() != null && truthTableGenView.getExerciseComment().getEditor() != null) {
            RichTextArea commentRTA = truthTableGenView.getExerciseComment().getEditor();
            commentRTA.getActionFactory().saveNow().execute(new ActionEvent());
            model.setExerciseComment(commentRTA.getDocument());
            double commentTextHeight = commentRTA.getFullHeight();
            model.setCommentTextHeight(commentTextHeight > 0 ? commentTextHeight : truthTableGenModel.getCommentTextHeight());
        }

        if (truthTableGenView.getInterpretationDRTA() != null && truthTableGenView.getInterpretationDRTA().getEditor() != null) {
            RichTextArea interpretationRTA = truthTableGenView.getInterpretationDRTA().getEditor();
            interpretationRTA.getActionFactory().saveNow().execute(new ActionEvent());
            model.setInterpFnDoc(interpretationRTA.getDocument());
            double interpTextHeight = interpretationRTA.getFullHeight();
            model.setInterpFnTextHeight(interpTextHeight > 0 ? interpTextHeight : truthTableGenModel.getInterpFnTextHeight());
        }

        if (truthTableGenView.getExplainDRTA() != null && truthTableGenView.getExplainDRTA().getEditor() != null) {
            RichTextArea explainRTA = truthTableGenView.getExplainDRTA().getEditor();
            explainRTA.getActionFactory().saveNow().execute(new ActionEvent());
            model.setExplainDocument(explainRTA.getDocument());
            double explainTextHeight = explainRTA.getFullHeight();
            model.setExplainTextHeight(explainTextHeight > 0 ? explainTextHeight : truthTableGenModel.getExplainTextHeight());
        }

        model.setUnaryOperatorList(truthTableGenModel.getUnaryOperatorList());
        model.setBinaryOperatorList(truthTableGenModel.getBinaryOperatorList());
        model.setMainFormulas(truthTableGenModel.getMainFormulas());

        List<BoxedDRTA> basicBoxedDRTAs = truthTableGenView.getBasicFormulasBoxedDRTAs();
        if (basicBoxedDRTAs != null) {
            List<Document> basicDocs = new ArrayList<>();
            for (BoxedDRTA bdrta : basicBoxedDRTAs) {
                if (bdrta != null && bdrta.getRTA() != null) {
                    RichTextArea rta = bdrta.getRTA();
                    rta.getActionFactory().saveNow().execute(new ActionEvent());
                    Document doc = rta.getDocument();
                    basicDocs.add(doc);
                }
            }
            model.setBasicFormulas(basicDocs);
        }

        List<BoxedDRTA> mainBoxedDRTAs = truthTableGenView.getMainFormulasBoxedDRTAs();
        if (mainBoxedDRTAs != null) {
            List<Document> mainDocs = new ArrayList<>();
            for (BoxedDRTA bdrta : mainBoxedDRTAs) {
                if (bdrta != null && bdrta.getRTA() != null) {
                    RichTextArea rta = bdrta.getRTA();
                    rta.getActionFactory().saveNow().execute(new ActionEvent());
                    Document doc = rta.getDocument();
                    mainDocs.add(doc);
                }
            }
            model.setMainFormulas(mainDocs);
        }

        String[][] tableStrVals = new String[tableColumns][tableRows];
        TextField[][] tableFields = truthTableGenView.getTableFields();
        if (tableFields != null) {
            for (int i = 0; i < tableColumns && i < tableFields.length; i ++) {
                String[] column = new String[tableRows];
                for (int j = 0; j < tableRows; j++) {
                    if (tableFields[i] != null && tableFields[i].length > j && tableFields[i][j] != null) {
                        String value = tableFields[i][j].getText();
                        column[j] = value;
                    }
                }
                tableStrVals[i] = column;
            }
        }
        model.setTableValues(tableStrVals);

        BoxedDRTA[] lineCommentBoxedDRTAs = truthTableGenView.getRowCommentsArray();
        if (lineCommentBoxedDRTAs != null) {
            Document[] commentDocs = new Document[tableRows];
            Double[] commentPrintWidths = new Double[tableRows];
            for (int i = 0; i < tableRows && i < lineCommentBoxedDRTAs.length; i++) {
                if (lineCommentBoxedDRTAs[i] != null && lineCommentBoxedDRTAs[i].getRTA() != null) {
                    BoxedDRTA bdrta = lineCommentBoxedDRTAs[i];
                    RichTextArea rta = bdrta.getRTA();
                    rta.getActionFactory().saveNow().execute(new ActionEvent());
                    Document doc = rta.getDocument();
                    commentDocs[i] = doc;
                    commentPrintWidths[i] = rta.getPrefWidth();
                }
            }
            model.setRowComments(commentDocs);
            model.setCommentPrintWidths(commentPrintWidths);
        }

        ToggleButton[] highlightButtons = truthTableGenView.getHighlightButtons();
        if (highlightButtons != null) {
            boolean[] highlightValues = new boolean[tableColumns];
            for (int i = 0; i < tableColumns && i < highlightButtons.length; i++) {
                if (highlightButtons[i] != null) {
                    highlightValues[i] = highlightButtons[i].isSelected();
                }
            }
            model.setColumnHighlights(highlightValues);
        }

        model.setConclusionDivider(truthTableGenModel.isConclusionDivider());
        model.setChoiceLead(truthTableGenModel.getChoiceLead());
        model.setaPrompt(truthTableGenModel.getaPrompt());
        if (truthTableGenView.getaCheckBox() != null) model.setaSelected(truthTableGenView.getaCheckBox().isSelected());
        model.setbPrompt(truthTableGenModel.getbPrompt());
        if (truthTableGenView.getbCheckBox() != null) model.setbSelected(truthTableGenView.getbCheckBox().isSelected());

        model.setTableRows(tableRows);


        model.setPointsPossible(truthTableGenModel.getPointsPossible());
        TextField pointsField = truthTableGenView.getPointsEarnedTextField();
        if ((pointsField.isFocused() || truthTableGenModel.getPointsEarned() >= 0) && pointsField.getText().isEmpty()) {
            pointsField.setText("0");
        }
        if (!pointsField.getText().equals("")) model.setPointsEarned(Integer.parseInt(pointsField.getText()));
        else model.setPointsEarned(-1);


        return model;
    }

    public MainWindow getMainWindow() { return mainWindow; }

    int getTableColumns() { return tableColumns; }

    public int getTableRows() { return tableRows; }

    public List<Integer> getMainFormulaCols() { return mainFormulaCols; }

    public List<Integer> getBasicFormulaCols() { return basicFormulaCols; }

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
            if (truthTableGenView != null && truthTableGenView.getPointsEarnedTextField() != null && pointsEarnedListener != null) {
                truthTableGenView.getPointsEarnedTextField().textProperty().removeListener(pointsEarnedListener);
            }
        } catch (Exception e) {}

        try {
            if (truthTableGenView != null && truthTableGenView.getExerciseStatement() != null && truthTableGenView.getExerciseStatement().getEditor() != null && statementFocusListener != null) {
                truthTableGenView.getExerciseStatement().getEditor().focusedProperty().removeListener(statementFocusListener);
            }
        } catch (Exception e) {}

        try {
            if (truthTableGenView != null && truthTableGenView.getExerciseComment() != null && truthTableGenView.getExerciseComment().getEditor() != null) {
                if (commentFullHeightListener != null) truthTableGenView.getExerciseComment().getEditor().fullHeightProperty().removeListener(commentFullHeightListener);
                if (commentModifiedListener != null) truthTableGenView.getExerciseComment().getEditor().modifiedProperty().removeListener(commentModifiedListener);
                if (commentFocusListener != null) truthTableGenView.getExerciseComment().getEditor().focusedProperty().removeListener(commentFocusListener);
            }
        } catch (Exception e) {}

        try {
            if (truthTableGenView != null && truthTableGenView.getaCheckBox() != null && aCheckBoxListener != null) {
                truthTableGenView.getaCheckBox().selectedProperty().removeListener(aCheckBoxListener);
            }
        } catch (Exception e) {}

        try {
            if (truthTableGenView != null && truthTableGenView.getbCheckBox() != null && bCheckBoxListener != null) {
                truthTableGenView.getbCheckBox().selectedProperty().removeListener(bCheckBoxListener);
            }
        } catch (Exception e) {}

        try {
            if (truthTableGenView != null && truthTableGenView.getInterpretationDRTA() != null && truthTableGenView.getInterpretationDRTA().getEditor() != null) {
                if (interpFullHeightListener != null) truthTableGenView.getInterpretationDRTA().getEditor().fullHeightProperty().removeListener(interpFullHeightListener);
                if (interpModifiedListener != null) truthTableGenView.getInterpretationDRTA().getEditor().modifiedProperty().removeListener(interpModifiedListener);
                if (interpFocusListener != null) truthTableGenView.getInterpretationDRTA().getEditor().focusedProperty().removeListener(interpFocusListener);
            }
        } catch (Exception e) {}

        try {
            if (truthTableGenView != null && truthTableGenView.getExplainDRTA() != null && truthTableGenView.getExplainDRTA().getEditor() != null) {
                if (explainFullHeightListener != null) truthTableGenView.getExplainDRTA().getEditor().fullHeightProperty().removeListener(explainFullHeightListener);
                if (explainModifiedListener != null) truthTableGenView.getExplainDRTA().getEditor().modifiedProperty().removeListener(explainModifiedListener);
                if (explainFocusListener != null) truthTableGenView.getExplainDRTA().getEditor().focusedProperty().removeListener(explainFocusListener);
            }
        } catch (Exception e) {}

        try {
            if (truthTableGenView != null && truthTableGenView.getSetupTableButton() != null && setupTableButtonHandler != null) {
                truthTableGenView.getSetupTableButton().removeEventHandler(ActionEvent.ACTION, setupTableButtonHandler);
                truthTableGenView.getSetupTableButton().setOnAction(null);
            }
        } catch (Exception e) {}

        try {
            if (thumbPane != null && thumbPaneClickHandler != null) {
                thumbPane.removeEventHandler(MouseEvent.MOUSE_CLICKED, thumbPaneClickHandler);
                thumbPane.setOnMouseClicked(null);
            }
        } catch (Exception e) {}

        // SCORCHED EARTH RULE 9: Explicit safe detachment of dynamic array components
        try {
            if (truthTableGenView != null && truthTableGenView.getTableFields() != null && charFieldListener != null) {
                TextField[][] fields = truthTableGenView.getTableFields();
                for (int i = 0; i < fields.length; i++) {
                    if (fields[i] != null) {
                        for (int j = 0; j < fields[i].length; j++) {
                            if (fields[i][j] != null) fields[i][j].textProperty().removeListener(charFieldListener);
                        }
                    }
                }
            }
        } catch (Exception e) {}

        try {
            if (truthTableGenView != null && truthTableGenView.getHighlightButtons() != null && highlightButtonListener != null) {
                ToggleButton[] btns = truthTableGenView.getHighlightButtons();
                for (int i = 0; i < btns.length; i++) {
                    if (btns[i] != null) btns[i].selectedProperty().removeListener(highlightButtonListener);
                }
            }
        } catch (Exception e) {}

        try {
            if (thumbPane != null) {
                thumbPane.getChildren().clear();
                severFromParent(thumbPane);
            }
        } catch (Exception e) {}

        try {
            if (ffWrapperPane != null) {
                ffWrapperPane.getChildren().clear();
                severFromParent(ffWrapperPane);
            }
        } catch (Exception e) {}

        // 5. Clear OS-level popups
        try { clearStandingPopups(); } catch (Exception e) {}

        // 6. Cascade cleanup to auxiliary exercises
        try { if (auxExerciseA != null) auxExerciseA.cleanup(); } catch (Exception e) {}
        try { if (auxExerciseB != null) auxExerciseB.cleanup(); } catch (Exception e) {}


        // 7. Tell View & Check to specifically clean up
        try { if (truthTableGenView != null) truthTableGenView.cleanup(); } catch (Exception e) {}
        try { if (ttgCheck != null) ttgCheck.cleanup(); } catch (Exception e) {}

        // 8. Final severed ties
        try { this.mainWindow = null; } catch (Exception e) {}
        try { this.truthTableGenModel = null; } catch (Exception e) {}
        try { this.truthTableGenView = null; } catch (Exception e) {}
        try { this.ttgCheck = null; } catch (Exception e) {}
        try { this.mainView = null; } catch (Exception e) {}
        try { this.docParser = null; } catch (Exception e) {}
        try { this.spacerConstraint = null; } catch (Exception e) {}

        // SCORCHED EARTH RULE 10: NON-DESTRUCTIVE DATA TEARDOWN (never use .clear() on these fields)
        try { this.basicFormulaCols = null; } catch (Exception e) {}
        try { this.mainFormulaCols = null; } catch (Exception e) {}

        try { this.auxExerciseA = null; } catch (Exception e) {}
        try { this.auxExerciseB = null; } catch (Exception e) {}
        try { this.tScene = null; } catch (Exception e) {}
        try { this.tStage = null; } catch (Exception e) {}
        try { this.thumbPane = null; } catch (Exception e) {}
        try { this.ffWrapperPane = null; } catch (Exception e) {}

        try { this.pointsEarnedListener = null; } catch (Exception e) {}
        try { this.statementFocusListener = null; } catch (Exception e) {}
        try { this.commentFullHeightListener = null; } catch (Exception e) {}
        try { this.commentModifiedListener = null; } catch (Exception e) {}
        try { this.commentFocusListener = null; } catch (Exception e) {}
        try { this.aCheckBoxListener = null; } catch (Exception e) {}
        try { this.bCheckBoxListener = null; } catch (Exception e) {}
        try { this.interpFullHeightListener = null; } catch (Exception e) {}
        try { this.interpModifiedListener = null; } catch (Exception e) {}
        try { this.interpFocusListener = null; } catch (Exception e) {}
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
        if (truthTableGenView == null || truthTableGenModel == null || truthTableGenView.getCenterBox() == null) return new AnchorPane();
        try {
            RichTextArea explainRTA = truthTableGenView.getExplainDRTA().getEditor();
            explainRTA.prefHeightProperty().unbind();
            explainRTA.setMinHeight(truthTableGenModel.getExplainTextHeight() + RTA_SCREEN_BMP);
            return truthTableGenView.getCenterBox();
        }
        catch (Exception e) { return new AnchorPane(); }
    }

    @Override
    public String getAuxExerName() {
        if (truthTableGenModel == null) return "";
        return truthTableGenModel.getAuxExerName();
    }

    @Override
    public void setAuxExerA(Exercise exer) {
        auxExerciseA = exer;
    }

    @Override
    public void setAuxExerB(Exercise exer) { auxExerciseB = exer; }
}
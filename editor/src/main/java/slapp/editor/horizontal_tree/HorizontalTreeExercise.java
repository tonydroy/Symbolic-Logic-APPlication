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
import com.gluonhq.richtextarea.RichTextAreaSkin;
import com.gluonhq.richtextarea.model.Document;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.Spinner;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import org.apache.commons.lang3.SerializationUtils;
import slapp.editor.DiskUtilities;
import slapp.editor.PrintUtilities;
import slapp.editor.decorated_rta.BoxedDRTA;
import slapp.editor.decorated_rta.DecoratedRTA;
import slapp.editor.main_window.*;

import java.util.ArrayList;
import java.util.List;
import static slapp.editor.EditorMain.*;
import javafx.animation.PauseTransition;
import javafx.util.Duration;
import javafx.beans.value.ChangeListener;
import javafx.event.EventHandler;


/**
 * Controller for the horizontal tree exercise
 */


public class HorizontalTreeExercise implements Exercise<HorizontalTreeModel, HorizontalTreeView> {
    private MainWindow mainWindow;
    private MainWindowView mainView;
    private HorizontalTreeModel horizontalTreeModel;
    private HorizontalTreeView horizontalTreeView;
    private boolean exerciseModified = false;
    private UndoRedoList<HorizontalTreeModel> undoRedoList = new UndoRedoList<>(100);

    public BooleanProperty undoRedoFlag = new SimpleBooleanProperty();
    private HTCheck htCheck;
    DecoratedRTA interpretationDRTA;
    RichTextArea interpretationEditor;

    // KILL SWITCH: Prevents asynchronous listener execution during teardown
    private boolean isTornDown = false;
    // REBUILD GUARD: Prevents spurious undo/redo pushes during UI reconstruction
    private boolean isRebuilding = false;

    // SCORCHED EARTH RULE 3: Eradicate Anonymous Lambdas
    private ChangeListener<Boolean> undoRedoFlagListener;
    private ChangeListener<Number> interpFullHeightListener;
    private ChangeListener<Boolean> interpModifiedListener;
    private ChangeListener<Boolean> interpFocusListener;
    private ChangeListener<String> pointsEarnedListener;
    private ChangeListener<Boolean> statementFocusListener;
    private ChangeListener<Number> commentFullHeightListener;
    private ChangeListener<Boolean> commentModifiedListener;
    private ChangeListener<Boolean> commentFocusListener;
    private ChangeListener<Number> explainFullHeightListener;
    private ChangeListener<Boolean> explainModifiedListener;
    private ChangeListener<Boolean> explainFocusListener;
    private EventHandler<ActionEvent> undoButtonHandler;
    private EventHandler<ActionEvent> redoButtonHandler;

    /**
     * Construct horizontal tree exercise from model
     * @param model the model
     * @param mainWindow the main window
     */
    public HorizontalTreeExercise(HorizontalTreeModel model, MainWindow mainWindow) {
        this.mainWindow = mainWindow;
        this.horizontalTreeModel = model;
        if (model.getOriginalModel() == null) {model.setOriginalModel(model); }
        this.mainView = mainWindow.getMainView();
        this.horizontalTreeView = new HorizontalTreeView(mainView);
        setHorizontalTreeView();
        htCheck = new HTCheck(this);
        setInterpretationDRTA();
        horizontalTreeView.initalizeInterpretationRTA();                          //view setup that runs after htCheck created - which runs after set htView
        undoRedoFlag.set(false);
        undoRedoFlag.bind(horizontalTreeView.undoRedoFlagProperty());

        undoRedoFlagListener = (ob, ov, nv) -> {
            if (isTornDown || isRebuilding || mainView == null || undoRedoList == null) return;
            if (nv) {
                exerciseModified = true;
                pushUndoRedo();
            }
        };
        undoRedoFlag.addListener(undoRedoFlagListener);

        HorizontalTreeModel deepCopy = (HorizontalTreeModel) SerializationUtils.clone(horizontalTreeModel);
        undoRedoList.push(deepCopy);
        updateUndoRedoButtons();
    }

    private void setInterpretationDRTA() {
        interpretationDRTA = new DecoratedRTA();
        interpretationEditor = interpretationDRTA.getEditor();
        interpretationEditor.getActionFactory().open(htCheck.getCheckSetup().getInterpretationDoc()).execute(new ActionEvent());
        interpretationEditor.getActionFactory().saveNow().execute(new ActionEvent());

        final boolean[] interpPrimed = {false};
        interpFullHeightListener = (ob, ov, nv) -> {
            if (isTornDown || mainView == null || htCheck == null || htCheck.getCheckSetup() == null) return;
            if (!interpPrimed[0]) {
                interpPrimed[0] = true;
                return;
            }
            double height = (double) nv;
            mainView.setRTATextHeight(height);
            htCheck.getCheckSetup().setInterpretationTextHeight(height);
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
                }
                else mainView.undoRTATextHeight();
            } catch (Exception e) {}
        };
        interpretationEditor.focusedProperty().addListener(interpFocusListener);

        horizontalTreeView.setInterpretationDRTA(interpretationDRTA);
        horizontalTreeView.setInterpretationRTA(interpretationEditor);
        horizontalTreeView.setInterpretationPrefHeight(htCheck.getCheckSetup().getInterpretationPrefHeight());
    }


    /*
     * Set up the horizontal tree view from the model
     */
    private void setHorizontalTreeView() {
        horizontalTreeView.setStatementPrefHeight(horizontalTreeModel.getStatementPrefHeight());
        horizontalTreeView.setCommentPrefHeight(horizontalTreeModel.getCommentPrefHeight());
        horizontalTreeView.setExplainPrefHeight(horizontalTreeModel.getExplainPrefHeight());

        horizontalTreeView.setPointsPossible(horizontalTreeModel.getPointsPossible());
        if (horizontalTreeModel.getPointsEarned() >= 0) horizontalTreeView.getPointsEarnedTextField().setText(Integer.toString(horizontalTreeModel.getPointsEarned()));

        pointsEarnedListener = (ob, ov, nv) -> {
            if (isTornDown || mainView == null) return;
            exerciseModified = true;
        };
        horizontalTreeView.getPointsEarnedTextField().textProperty().addListener(pointsEarnedListener);

        //statement
        DecoratedRTA statementDRTA = new DecoratedRTA();
        RichTextArea statementEditor = statementDRTA.getEditor();
        statementEditor.getActionFactory().open(horizontalTreeModel.getExerciseStatement()).execute(new ActionEvent());

        mainView.editorInFocus(statementDRTA, ControlType.STATEMENT);
        statementFocusListener = (o, ov, nv) -> {
            if (isTornDown || mainView == null) return;
            try {
                if (nv) {
                    mainView.editorInFocus(statementDRTA, ControlType.STATEMENT);
                    mainView.setRTATextHeight(statementEditor.getFullHeight());
                }
                else mainView.undoRTATextHeight();
            } catch (Exception e) {}
        };
        statementEditor.focusedProperty().addListener(statementFocusListener);
        horizontalTreeView.setExerciseStatement(statementDRTA);


        //comment
        DecoratedRTA commentDRTA = new DecoratedRTA();
        RichTextArea commentEditor = commentDRTA.getEditor();
        commentEditor.setPromptText("Comment: ");
        commentEditor.getActionFactory().open(horizontalTreeModel.getExerciseComment()).execute(new ActionEvent());
        commentEditor.getActionFactory().saveNow().execute(new ActionEvent());

        final boolean[] commentPrimed = {false};
        commentFullHeightListener = (ob, ov, nv) -> {
            if (isTornDown || mainView == null || horizontalTreeModel == null) return;
            if (!commentPrimed[0]) {
                commentPrimed[0] = true;
                return;
            }
            double height = (double) nv;
            mainView.setRTATextHeight(height);
            horizontalTreeModel.setCommentTextHeight(height);
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
                }
                else mainView.undoRTATextHeight();
            } catch (Exception e) {}
        };
        commentEditor.focusedProperty().addListener(commentFocusListener);
        horizontalTreeView.setExerciseComment(commentDRTA);


        //explain
        DecoratedRTA explainDRTA = horizontalTreeView.getExplainDRTA();
        RichTextArea explainEditor = explainDRTA.getEditor();
        explainEditor.getActionFactory().open(horizontalTreeModel.getExplainDocument()).execute(new ActionEvent());
        explainEditor.getActionFactory().saveNow().execute(new ActionEvent());
        horizontalTreeView.setExplainPrompt(horizontalTreeModel.getExplainPrompt());

        final boolean[] explainPrimed = {false};
        explainFullHeightListener = (ob, ov, nv) -> {
            if (isTornDown || mainView == null || horizontalTreeModel == null) return;
            if (!explainPrimed[0]) {
                explainPrimed[0] = true;
                return;
            }
            double height = (double) nv;
            mainView.setRTATextHeight(height);
            horizontalTreeModel.setExplainTextHeight(height);
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
                }
                else mainView.undoRTATextHeight();
            } catch (Exception e) {}
        };
        explainEditor.focusedProperty().addListener(explainFocusListener);

        //buttons and cleanup
        undoButtonHandler = e -> {
            if (isTornDown || mainView == null) return;
            undoAction();
        };
        horizontalTreeView.getUndoButton().setOnAction(undoButtonHandler);

        redoButtonHandler = e -> {
            if (isTornDown || mainView == null) return;
            redoAction();
        };
        horizontalTreeView.getRedoButton().setOnAction(redoButtonHandler);

        horizontalTreeView.initializeViewDetails();
        horizontalTreeView.setRightControlBox();

        refreshViewFromModel();
    }

    /*
     * Refresh view including panes and ruler axis from model
     */
    private void refreshViewFromModel() {
        if (isTornDown || horizontalTreeView == null || horizontalTreeModel == null) return;
        populateTreePanes();
        horizontalTreeView.refreshTreePanes();

        if (horizontalTreeModel.isAxis() && !horizontalTreeView.isAxis()) {
            horizontalTreeView.simpleAddAxis();
            horizontalTreeView.getRulerButton().setSelected(true);
        }
        else {
            horizontalTreeView.simpleRemoveAxis();
            horizontalTreeView.getRulerButton().setSelected(false);
        }
    }

    /*
     * Populate view tree pane list from model
     */
    private void populateTreePanes() {
        if (isTornDown || horizontalTreeView == null || horizontalTreeView.getTreePanes() == null || horizontalTreeModel == null) return;

        isRebuilding = true; // Lock undo/redo listener
        try {
            // SCORCHED EARTH RULE 8: Safely sever existing panes before re-populating to prevent CME
            if (horizontalTreeView.getTreePanes() != null && !horizontalTreeView.getTreePanes().isEmpty()) {
                List<TreePane> panesCopy = new ArrayList<>(horizontalTreeView.getTreePanes());
                horizontalTreeView.getTreePanes().clear(); // Clear FIRST before triggering node teardowns

                for (TreePane tp : panesCopy) {
                    try { tp.cleanup(); } catch (Exception e) {}
                    severFromParent(tp);
                }

            }

            for (TreeModel treeModel : horizontalTreeModel.getTreeModels()) {
                TreePane treePane = new TreePane(horizontalTreeView);
                treePane.setLayoutX(treeModel.getPaneXlayout());
                treePane.setLayoutY(treeModel.getPaneYlayout());
                BranchNode rootNode = treePane.getRootBranchNode();
                rootNode.setLayoutX(treeModel.getRootXlayout());
                rootNode.setLayoutY(treeModel.getRootYlayout());
                setTreeNodes(rootNode, treeModel.getRoot());
                horizontalTreeView.getTreePanes().add(treePane);
            }
        } finally {
            isRebuilding = false; // Unlock
        }
    }

    /*
     * Recursively fill out nodes on branch
     * @param branchNode current node to update
     * @param branchModel model for the node
     */
    private void setTreeNodes(BranchNode branchNode, BranchModel branchModel) {
        if (isTornDown || branchNode == null || branchModel == null) return;
        if (branchModel.isAnnotation()) {
            branchNode.addAnnotation();
            branchNode.setAnnBump(branchNode.getAnnotationWidth() * .75 + 5);
            branchNode.setAnnotation(true);
        }
        branchNode.setFormulaNode(branchModel.isFormulaBranch());
        branchNode.setIndefiniteNode(branchModel.isIndefiniteNumBranch());
        branchNode.setDotDivider(branchModel.isDotDivider());
        branchNode.setRoot(branchModel.isRootBranch());
        branchNode.setFormPrintWidth(branchModel.getFormPrintWidth());
        branchNode.setConnPrintWidth(branchModel.getConnPrintWidth());

        if (branchNode.getAnnotationField() != null) {
            branchNode.getAnnotationField().getRTA().getActionFactory().open(branchModel.getAnnotationDoc()).execute(new ActionEvent());
            branchNode.getAnnotationField().getRTA().getActionFactory().saveNow().execute(new ActionEvent());
        }

        BoxedDRTA formulaBDRTA = branchNode.getFormulaBoxedDRTA();
        if (formulaBDRTA != null) {
            RichTextArea formulaRTA = formulaBDRTA.getRTA();
            formulaRTA.getActionFactory().open(branchModel.getFormulaDoc()).execute(new ActionEvent());
            formulaRTA.getActionFactory().saveNow().execute(new ActionEvent());
            if (branchModel.isFormulaBranch()) {
                formulaBDRTA.getDRTA().getKeyboardSelector().valueProperty().setValue(RichTextAreaSkin.KeyMapValue.SANS_AND_ITALIC);
            }
            else {
                formulaBDRTA.getDRTA().getKeyboardSelector().valueProperty().setValue(RichTextAreaSkin.KeyMapValue.ITALIC_AND_SANS);
            }
        }

        BoxedDRTA connectBDRTA = branchNode.getConnectorBoxedDRTA();
        if (connectBDRTA != null) {
            RichTextArea connectRTA = connectBDRTA.getRTA();
            connectRTA.getActionFactory().open(branchModel.getConnectorDoc()).execute(new ActionEvent());
            connectRTA.getActionFactory().saveNow().execute(new ActionEvent());
            connectBDRTA.getDRTA().getKeyboardSelector().valueProperty().setValue(RichTextAreaSkin.KeyMapValue.ITALIC_AND_SANS);
        }

        if (!branchModel.isFormulaBranch()) branchNode.setStyle("-fx-border-color: white white white white; -fx-border-width: 0 0 0 0");
        if (branchModel.isIndefiniteNumBranch()) branchNode.setStyle("-fx-border-color: white white white white; -fx-border-width: 0 0 0 0");

        if (branchModel.getDependents() != null) {
            for (BranchModel dependentMod : branchModel.getDependents()) {
                BranchNode dependentNode = new BranchNode(branchNode, horizontalTreeView);
                branchNode.getDependents().add(dependentNode);
                setTreeNodes(dependentNode, dependentMod);
            }
        }
    }

    /*
     * Perform undo action
     */
    private void undoAction() {
        if (undoRedoList == null) return;
        HorizontalTreeModel undoElement = undoRedoList.getUndoElement();
        if (undoElement != null) {
            horizontalTreeModel = (HorizontalTreeModel) SerializationUtils.clone(undoElement);
            refreshViewFromModel();
            updateUndoRedoButtons();
            if (horizontalTreeView != null) horizontalTreeView.deselectToggles();
        }
    }

    /*
     * Perform redo action
     */
    private void redoAction() {
        if (undoRedoList == null) return;
        HorizontalTreeModel redoElement = undoRedoList.getRedoElement();
        if (redoElement != null) {
            horizontalTreeModel = (HorizontalTreeModel) SerializationUtils.clone(redoElement);
            refreshViewFromModel();
            updateUndoRedoButtons();
            if (horizontalTreeView != null) horizontalTreeView.deselectToggles();
        }
    }

    /*
     * Update undo/redo button disable based on undo redo stack
     */
    public void updateUndoRedoButtons() {
        if (horizontalTreeView == null || undoRedoList == null) return;
        if (horizontalTreeView.getUndoButton() != null) horizontalTreeView.getUndoButton().setDisable(!undoRedoList.canUndo());
        if (horizontalTreeView.getRedoButton() != null) horizontalTreeView.getRedoButton().setDisable(!undoRedoList.canRedo());
    }

    /*
     * Push current state onto undo/redo stack
     */
    private void pushUndoRedo() {
        if (undoRedoList == null || horizontalTreeModel == null) return;
        HorizontalTreeModel model = getHorizontalTreeModelFromView();
        HorizontalTreeModel deepCopy = (HorizontalTreeModel) SerializationUtils.clone(model);
        undoRedoList.push(deepCopy);
        updateUndoRedoButtons();
    }

    @Override
    public HorizontalTreeModel getExerciseModel() { return horizontalTreeModel;  }

    @Override
    public HorizontalTreeView getExerciseView() { return horizontalTreeView;  }

    @Override
    public void saveExercise(boolean saveAs) {
        boolean success = DiskUtilities.saveExercise(saveAs, getHorizontalTreeModelFromView());
        if (success) {
            exerciseModified = false;
        }
    }

    @Override
    public List<Node> getPrintNodes(ExerciseModel originalModel) {
        if (horizontalTreeView == null || horizontalTreeModel == null) return new ArrayList<>();
        List<Node> nodeList = new ArrayList<>();
        HorizontalTreeExercise printExercise = this;
        HorizontalTreeModel printModel = horizontalTreeModel;

        double nodeWidth = PrintUtilities.getPageWidth() / mainWindow.getBaseScale();

        //header node
        Label exerciseName = new Label(horizontalTreeModel.getExerciseName());
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
        statementRTA.maxWidthProperty().unbind();
        statementRTA.setPrefHeight(printModel.getStatementTextHeight() + RTA_PRINT_BMP);
        statementRTA.setMaxWidth(nodeWidth);
        statementRTA.setMinWidth(nodeWidth);
        statementRTA.getStylesheets().clear(); statementRTA.getStylesheets().add("richTextAreaPrinter.css");
        nodeList.add(statementRTA);

        Separator statementSeparator = new Separator(Orientation.HORIZONTAL);
        statementSeparator.setPrefWidth(100);
        HBox statementSepBox = new HBox(statementSeparator);
        statementSepBox.setMinWidth(nodeWidth);
        statementSepBox.setAlignment(Pos.CENTER);
        nodeList.add(statementSepBox);

        //content
        AnchorPane mainPane = printExercise.getExerciseView().getMainPane();
        mainPane.setMinHeight(0.0);
        Group root = new Group();
        Scene scene = new Scene(root);
        root.getChildren().add(mainPane);
        root.applyCss();
        root.layout();

        Ruler axis = new Ruler();
        double axisWidth = Math.max(nodeWidth, printModel.getMainPaneWidth() - RTA_WIDTH_BMP);

        axis.updateRuler(axisWidth);

        mainPane.setStyle("-fx-background-color: transparent");

        mainPane.setPadding(new Insets(0));
        mainPane.minWidthProperty().unbind();
        mainPane.setMinWidth(printModel.getMainPaneWidth() - RTA_WIDTH_BMP);
        mainPane.setMaxWidth(printModel.getMainPaneWidth() - RTA_WIDTH_BMP);

        printExercise.getExerciseView().simpleRemoveAxis();

        ObservableList<Node> mainNodes = mainPane.getChildren();
        for (Node mainNode : mainNodes) {
            if (mainNode instanceof TreePane) {
                TreePane treePane = (TreePane) mainNode;
                ObservableList<Node> paneNodes = treePane.getChildren();
                for (Node paneNode : paneNodes) {
                    if (paneNode instanceof BranchNode) {
                        BranchNode branchNode = (BranchNode) paneNode;
                        if (branchNode.getFormulaBoxedDRTA() != null) branchNode.getFormulaBoxedDRTA().getRTA().setStyle("-fx-border-color: transparent");
                        if (branchNode.getConnectorBoxedDRTA() != null) branchNode.getConnectorBoxedDRTA().getRTA().setStyle("-fx-border-color: transparent");
                        if (branchNode.getAnnotationField() != null) {
                            branchNode.getAnnotationField().getRTA().setStyle("-fx-border-color: transparent");
                        }

                        if (branchNode.getFormulaBoxedDRTA() != null) branchNode.getFormulaBoxedDRTA().getRTA().setMinWidth(branchNode.getFormPrintWidth());
                        if (branchNode.getConnectorBoxedDRTA() != null) branchNode.getConnectorBoxedDRTA().getRTA().setMinWidth(branchNode.getConnPrintWidth());
                    }
                }
            }
        }

        AnchorPane mainStack = new AnchorPane(axis, mainPane);
        mainStack.setLeftAnchor(axis, 5.0);

        if (!horizontalTreeModel.isAxis()) mainStack.getChildren().remove(axis);
        HBox contentHBox = new HBox(mainStack);
        contentHBox.setAlignment(Pos.CENTER);
        contentHBox.setPadding(new Insets(0,0,0, 0));
        contentHBox.setMaxWidth(axisWidth );
        nodeList.add(contentHBox);

       //explain node
        RichTextArea explainRTA = printExercise.getExerciseView().getExplainDRTA().getEditor();
        explainRTA.prefHeightProperty().unbind();
        explainRTA.minWidthProperty().unbind();
        explainRTA.maxWidthProperty().unbind();
        explainRTA.maxHeightProperty().unbind();
        explainRTA.setPrefHeight(Math.max(40, printModel.getExplainTextHeight() + RTA_PRINT_BMP));
        explainRTA.setMaxWidth(nodeWidth);
        explainRTA.setMinWidth(nodeWidth);
        explainRTA.getStylesheets().clear();
        explainRTA.getStylesheets().add("richTextAreaPrinter.css");
        nodeList.add(explainRTA);

        Separator explainSeparator = new Separator(Orientation.HORIZONTAL);
        explainSeparator.setPrefWidth(100);
        HBox explainSepBox = new HBox(explainSeparator);
        explainSepBox.setMinWidth(nodeWidth);
        explainSepBox.setAlignment(Pos.CENTER);
        nodeList.add(explainSepBox);

        //comment node
        RichTextArea commentRTA = printExercise.getExerciseView().getExerciseComment().getEditor();
        commentRTA.prefHeightProperty().unbind();
        commentRTA.minWidthProperty().unbind();
        commentRTA.maxWidthProperty().unbind();
        commentRTA.setPrefHeight(Math.max(40, printModel.getCommentTextHeight() + RTA_PRINT_BMP));
        commentRTA.setMaxWidth(nodeWidth);
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
            commentNode = anchorPane;
        }
        else commentNode = commentRTA;
        nodeList.add(commentNode);

        return nodeList;
    }

    @Override
    public Exercise<HorizontalTreeModel, HorizontalTreeView> resetExercise() {
        if (horizontalTreeView == null || horizontalTreeView.getExerciseComment() == null) return null;
        RichTextArea commentRTA = horizontalTreeView.getExerciseComment().getEditor();
        commentRTA.getActionFactory().saveNow().execute(new ActionEvent());
        Document commentDocument = commentRTA.getDocument();

        int pointsEarned = -1;
        if (!horizontalTreeView.getPointsEarnedTextField().getText().equals("")) pointsEarned = Integer.parseInt(horizontalTreeView.getPointsEarnedTextField().getText());
        HorizontalTreeModel originalModel = (HorizontalTreeModel) (horizontalTreeModel.getOriginalModel());
        originalModel.setExerciseComment(commentDocument);
        double commentHeight = commentRTA.getFullHeight();
        originalModel.setCommentTextHeight(commentHeight > 0 ? commentHeight : horizontalTreeModel.getCommentTextHeight());

        originalModel.setPointsEarned(pointsEarned);
        HorizontalTreeExercise clearExercise = new HorizontalTreeExercise(originalModel, mainWindow);
        return clearExercise;
    }

    @Override
    public boolean isExerciseModified() {      return exerciseModified;  }

    @Override
    public void setExerciseModified(boolean modified) { exerciseModified = modified;   }

    @Override
    public Node getFFViewNode() {
        if (horizontalTreeView == null) return new AnchorPane();
        return horizontalTreeView.getMainPane();
    }

    @Override
    public Spinner getFFHeightSpinner() {
        if (horizontalTreeView == null) return new Spinner();
        return horizontalTreeView.getMainPaneHeightSpinner();
    }

    @Override
    public Spinner getFFWidthSpinner() {
        if (horizontalTreeView == null) return new Spinner();
        return horizontalTreeView.getMainPaneWidthSpinner();
    }

    @Override
    public Node getFFPrintNode(ExerciseModel originalModel) {
        if (horizontalTreeView == null || horizontalTreeModel == null) return new HBox();
        HorizontalTreeExercise printExercise = this;
        HorizontalTreeModel printModel = horizontalTreeModel;
        double nodeWidth = PrintUtilities.getPageWidth() / mainWindow.getBaseScale();

        AnchorPane mainPane = printExercise.getExerciseView().getMainPane();
        mainPane.setMinHeight(0.0);

        Group root = new Group();
        Scene scene = new Scene(root);
        root.getChildren().add(mainPane);
        root.applyCss();
        root.layout();

        Ruler axis = new Ruler();
        double axisWidth = Math.max(nodeWidth, printModel.getMainPaneWidth() - RTA_WIDTH_BMP);

        axis.updateRuler(axisWidth);
        mainPane.setStyle("-fx-background-color: transparent");

        mainPane.setPadding(new Insets(0));
        mainPane.minWidthProperty().unbind();
        mainPane.setMinWidth(printModel.getMainPaneWidth() - RTA_WIDTH_BMP);
        mainPane.setMaxWidth(printModel.getMainPaneWidth() - RTA_WIDTH_BMP);

        printExercise.getExerciseView().simpleRemoveAxis();

        ObservableList<Node> mainNodes = mainPane.getChildren();
        for (Node mainNode : mainNodes) {
            if (mainNode instanceof TreePane) {
                TreePane treePane = (TreePane) mainNode;
                ObservableList<Node> paneNodes = treePane.getChildren();
                for (Node paneNode : paneNodes) {
                    if (paneNode instanceof BranchNode) {
                        BranchNode branchNode = (BranchNode) paneNode;
                        if (branchNode.getFormulaBoxedDRTA() != null) branchNode.getFormulaBoxedDRTA().getRTA().setStyle("-fx-border-color: transparent");
                        if (branchNode.getConnectorBoxedDRTA() != null) branchNode.getConnectorBoxedDRTA().getRTA().setStyle("-fx-border-color: transparent");
                        if (branchNode.getAnnotationField() != null) {
                            branchNode.getAnnotationField().getRTA().setStyle("-fx-border-color: transparent");
                        }

                        if (branchNode.getFormulaBoxedDRTA() != null) branchNode.getFormulaBoxedDRTA().getRTA().setMinWidth(branchNode.getFormPrintWidth());
                        if (branchNode.getConnectorBoxedDRTA() != null) branchNode.getConnectorBoxedDRTA().getRTA().setMinWidth(branchNode.getConnPrintWidth());
                    }
                }
            }
        }

        AnchorPane mainStack = new AnchorPane(axis, mainPane);
        mainStack.setLeftAnchor(axis, 5.0);

        if (!horizontalTreeModel.isAxis()) mainStack.getChildren().remove(axis);
        HBox contentHBox = new HBox(mainStack);
        contentHBox.setAlignment(Pos.CENTER);
        contentHBox.setPadding(new Insets(0,0,0, 0));
        contentHBox.setMaxWidth(axisWidth );

        return contentHBox;
    }

    @Override
    public ExerciseModel<HorizontalTreeModel> getExerciseModelFromView() {
        return (ExerciseModel) getHorizontalTreeModelFromView();
    }

    public HorizontalTreeModel getHorizontalTreeModelFromView() {
        if (horizontalTreeView == null || horizontalTreeModel == null) return new HorizontalTreeModel();

        HorizontalTreeModel model = new HorizontalTreeModel();

        model.setExerciseName(horizontalTreeModel.getExerciseName());
        model.setExplainPrompt(horizontalTreeModel.getExplainPrompt());
        model.setOriginalModel(horizontalTreeModel.getOriginalModel());
        model.setStarted(horizontalTreeModel.isStarted() || exerciseModified);

        HTcheckSetup checkSetup = horizontalTreeModel.getCheckSetup();
        if (htCheck != null && horizontalTreeView != null) {
            checkSetup.setCheckTries(htCheck.getCheckTries());
            checkSetup.setCheckSuccess(htCheck.isCheckSuccess());
            checkSetup.setCheckMessage(horizontalTreeView.getCheckMessage());
            if (horizontalTreeView.getPermitEmptyBranchesCheck() != null) {
                checkSetup.setEmptyBranchesSelected(horizontalTreeView.getPermitEmptyBranchesCheck().isSelected());
            }
        }

        model.setCheckSetup(checkSetup);

        model.setExerciseStatement(horizontalTreeModel.getExerciseStatement());
        model.setStatementPrefHeight(horizontalTreeView.getExerciseStatement().getEditor().getPrefHeight());
        model.setStatementTextHeight(horizontalTreeModel.getStatementTextHeight());

        RichTextArea commentRTA = horizontalTreeView.getExerciseComment().getEditor();
        commentRTA.getActionFactory().saveNow().execute(new ActionEvent());
        model.setExerciseComment(commentRTA.getDocument());
        model.setCommentPrefHeight(horizontalTreeView.getCommentPrefHeight());
        double commentTextHeight = commentRTA.getFullHeight();
        model.setCommentTextHeight(commentTextHeight > 0 ? commentTextHeight : horizontalTreeModel.getCommentTextHeight());

        RichTextArea explainRTA = horizontalTreeView.getExplainDRTA().getEditor();
        explainRTA.getActionFactory().saveNow().execute(new ActionEvent());
        model.setExplainDocument(explainRTA.getDocument());
        model.setExplainPrefHeight(horizontalTreeView.getExplainPrefHeight());
        double explainTextHeight = explainRTA.getFullHeight();
        model.setExplainTextHeight(explainTextHeight > 0 ? explainTextHeight : horizontalTreeModel.getExplainTextHeight());

        model.setMainPaneWidth(horizontalTreeView.getMainPane().getWidth());
        model.setAxis(horizontalTreeView.isAxis());

        RichTextArea interpretationRTA = horizontalTreeView.getInterpretationDRTA().getEditor();
        interpretationRTA.getActionFactory().saveNow().execute(new ActionEvent());
        Document interpretationDocument = interpretationRTA.getDocument();

        checkSetup.setInterpretationDoc(interpretationDocument);
        checkSetup.setInterpretationPrefHeight(horizontalTreeView.getInterpretationPrefHeight());


        double interpTextHeight = interpretationRTA.getFullHeight();
        double txtHeight = interpTextHeight > 0 ? interpTextHeight : checkSetup.getInterpretationTextHeight();
        checkSetup.setInterpretationTextHeight(Math.max(60, txtHeight));


        model.setPointsPossible(horizontalTreeModel.getPointsPossible());
        if (!horizontalTreeView.getPointsEarnedTextField().getText().equals("")) model.setPointsEarned(Integer.parseInt(horizontalTreeView.getPointsEarnedTextField().getText()));
        else model.setPointsEarned(-1);

        List<TreePane> treePanes = horizontalTreeView.getTreePanes();
        List<TreeModel> treeModels = model.getTreeModels();
        for (TreePane treePane : treePanes) {
            TreeModel treeModel = new TreeModel();
            treeModel.setPaneXlayout(treePane.getLayoutX());
            treeModel.setPaneYlayout(treePane.getLayoutY());

            BranchNode rootNode = treePane.getRootBranchNode();
            BranchModel rootModel = new BranchModel();
            setBranchModel(rootModel, rootNode);
            treeModel.setRoot(rootModel);

            treeModel.setRootXlayout(rootNode.getLayoutX());
            treeModel.setRootYlayout(rootNode.getLayoutY());

            treeModels.add(treeModel);
        }
        return model;
    }

    private void setBranchModel(BranchModel model, BranchNode node) {
        if (node == null) return;
        model.setAnnotation(node.isAnnotation());
        model.setFormulaBranch(node.isFormulaNode());
        model.setIndefiniteNumBranch(node.isIndefiniteNode());
        model.setDotDivider(node.isDotDivider());
        model.setRootBranch(node.isRoot());

        if (node.getAnnotationField() != null) {
            RichTextArea annotationRTA = node.getAnnotationField().getRTA();
            annotationRTA.getActionFactory().saveNow().execute(new ActionEvent());
            model.setAnnotationDoc(annotationRTA.getDocument());
        }

        if (node.getFormulaBoxedDRTA() != null) {
            RichTextArea formulaRTA = node.getFormulaBoxedDRTA().getRTA();
            formulaRTA.getActionFactory().saveNow().execute(new ActionEvent());
            model.setFormulaDoc(formulaRTA.getDocument());
            model.setFormPrintWidth(formulaRTA.getPrefWidth());
        }

        if (node.getConnectorBoxedDRTA() != null) {
            RichTextArea connectRTA = node.getConnectorBoxedDRTA().getRTA();
            connectRTA.getActionFactory().saveNow().execute(new ActionEvent());
            model.setConnectorDoc(connectRTA.getDocument());
            model.setConnPrintWidth(connectRTA.getPrefWidth());
        }

        // SCORCHED EARTH DEFENSIVE GUARD: Ensure getDependents() returns non-null before iterating
        if (node.getDependents() != null) {
            for (BranchNode dependentNode : node.getDependents()) {
                BranchModel dependentMod = new BranchModel();
                model.getDependents().add(dependentMod);
                setBranchModel(dependentMod, dependentNode);
            }
        }
    }

    public MainWindow getMainWindow() {
        return mainWindow;
    }

    public HorizontalTreeView getHorizontalTreeView() {
        return horizontalTreeView;
    }

    // SCORCHED EARTH RULE 6: Sever Shared Nodes helper
    private void severFromParent(Node node) {
        try {
            if (node != null && node.getParent() != null) {
                if (node.getParent() instanceof javafx.scene.layout.Pane) {
                    ((javafx.scene.layout.Pane) node.getParent()).getChildren().remove(node);
                } else if (node.getParent() instanceof javafx.scene.Group) {
                    ((javafx.scene.Group) node.getParent()).getChildren().remove(node);
                }
            }
        } catch (Exception e) {}
    }

    public void cleanup() {
        isTornDown = true;
        try {
            if (mainView != null) {
                mainView.textFieldInFocus();
            }
        } catch (Exception e) {}

        try { if (undoRedoFlag != null && undoRedoFlagListener != null) undoRedoFlag.removeListener(undoRedoFlagListener); } catch (Exception e) {}
        try { if (undoRedoFlag != null) undoRedoFlag.unbind(); } catch (Exception e) {}

        try {
            if (horizontalTreeView != null) {
                if (horizontalTreeView.localScalePageHeightProperty() != null) horizontalTreeView.localScalePageHeightProperty().unbind();
                if (horizontalTreeView.localScalePageWidthProperty() != null) horizontalTreeView.localScalePageWidthProperty().unbind();
            }
        } catch (Exception e) {}

        try {
            if (mainView != null && mainView.getMainScene() != null && horizontalTreeView != null && horizontalTreeView.getExerciseFocusListener() != null) {
                mainView.getMainScene().focusOwnerProperty().removeListener(horizontalTreeView.getExerciseFocusListener());
            }
        } catch (Exception e) {}

        try { if (interpretationEditor != null && interpFullHeightListener != null) interpretationEditor.fullHeightProperty().removeListener(interpFullHeightListener); } catch (Exception e) {}
        try { if (interpretationEditor != null && interpModifiedListener != null) interpretationEditor.modifiedProperty().removeListener(interpModifiedListener); } catch (Exception e) {}
        try { if (interpretationEditor != null && interpFocusListener != null) interpretationEditor.focusedProperty().removeListener(interpFocusListener); } catch (Exception e) {}

        try { if (horizontalTreeView != null && horizontalTreeView.getPointsEarnedTextField() != null && pointsEarnedListener != null) horizontalTreeView.getPointsEarnedTextField().textProperty().removeListener(pointsEarnedListener); } catch (Exception e) {}

        try { if (horizontalTreeView != null && horizontalTreeView.getExerciseStatement() != null && horizontalTreeView.getExerciseStatement().getEditor() != null && statementFocusListener != null) horizontalTreeView.getExerciseStatement().getEditor().focusedProperty().removeListener(statementFocusListener); } catch (Exception e) {}
        try { if (horizontalTreeView != null && horizontalTreeView.getExerciseComment() != null && horizontalTreeView.getExerciseComment().getEditor() != null) {
            if (commentFullHeightListener != null) horizontalTreeView.getExerciseComment().getEditor().fullHeightProperty().removeListener(commentFullHeightListener);
            if (commentModifiedListener != null) horizontalTreeView.getExerciseComment().getEditor().modifiedProperty().removeListener(commentModifiedListener);
            if (commentFocusListener != null) horizontalTreeView.getExerciseComment().getEditor().focusedProperty().removeListener(commentFocusListener);
        } } catch (Exception e) {}
        try { if (horizontalTreeView != null && horizontalTreeView.getExplainDRTA() != null && horizontalTreeView.getExplainDRTA().getEditor() != null) {
            if (explainFullHeightListener != null) horizontalTreeView.getExplainDRTA().getEditor().fullHeightProperty().removeListener(explainFullHeightListener);
            if (explainModifiedListener != null) horizontalTreeView.getExplainDRTA().getEditor().modifiedProperty().removeListener(explainModifiedListener);
            if (explainFocusListener != null) horizontalTreeView.getExplainDRTA().getEditor().focusedProperty().removeListener(explainFocusListener);
        } } catch (Exception e) {}

        // ---> SCORCHED EARTH RULE 4: CONTROLLER ONLY UNBINDS (No Timers, No setSkin(null)) <---
        try {
            if (horizontalTreeView != null || interpretationDRTA != null) {
                final RichTextArea stEditor = (horizontalTreeView != null && horizontalTreeView.getExerciseStatement() != null) ? horizontalTreeView.getExerciseStatement().getEditor() : null;
                final RichTextArea commEditor = (horizontalTreeView != null && horizontalTreeView.getExerciseComment() != null) ? horizontalTreeView.getExerciseComment().getEditor() : null;
                final RichTextArea expEditor = (horizontalTreeView != null && horizontalTreeView.getExplainDRTA() != null) ? horizontalTreeView.getExplainDRTA().getEditor() : null;
                final RichTextArea interpEditor = (interpretationDRTA != null) ? interpretationDRTA.getEditor() : null;

                try { if (stEditor != null) { stEditor.prefHeightProperty().unbind(); stEditor.minWidthProperty().unbind(); stEditor.maxWidthProperty().unbind(); } } catch(Exception e){}
                try { if (commEditor != null) { commEditor.prefHeightProperty().unbind(); commEditor.minWidthProperty().unbind(); commEditor.maxWidthProperty().unbind(); } } catch (Exception e){}
                try { if (expEditor != null) { expEditor.prefHeightProperty().unbind(); expEditor.minWidthProperty().unbind(); expEditor.maxWidthProperty().unbind(); } } catch(Exception e){}
                try { if (interpEditor != null) { interpEditor.prefHeightProperty().unbind(); interpEditor.minWidthProperty().unbind(); interpEditor.maxWidthProperty().unbind(); } } catch(Exception e){}
            }
        } catch (Exception e) {}

        try {
            if (horizontalTreeView != null && horizontalTreeView.getTreePanes() != null) {
                List<TreePane> panesCopy = new ArrayList<>(horizontalTreeView.getTreePanes());
                for (TreePane tp : panesCopy) {
                    if (tp != null) {
                        try { tp.cleanup(); } catch (Exception e) {}
                        severFromParent(tp);
                    }
                }
                horizontalTreeView.getTreePanes().clear();
            }
        } catch (Exception e) {}

        try {
            if (horizontalTreeView != null && horizontalTreeView.getMainPane() != null) {
                AnchorPane mainPane = horizontalTreeView.getMainPane();
                if (mainPane.getChildren() != null) {
                    List<Node> childrenCopy = new ArrayList<>(mainPane.getChildren());
                    for (Node node : childrenCopy) {
                        if (node instanceof TreePane) {
                            try { ((TreePane) node).cleanup(); } catch (Exception e) {}
                        }
                        severFromParent(node);
                    }
                    mainPane.getChildren().clear();
                }
            }
        } catch (Exception e) {}



        try {
            if (horizontalTreeView != null) {
                if (horizontalTreeView.getUndoButton() != null) {
                    if (undoButtonHandler != null) horizontalTreeView.getUndoButton().removeEventHandler(ActionEvent.ACTION, undoButtonHandler);
                    horizontalTreeView.getUndoButton().setOnAction(null);
                }
                if (horizontalTreeView.getRedoButton() != null) {
                    if (redoButtonHandler != null) horizontalTreeView.getRedoButton().removeEventHandler(ActionEvent.ACTION, redoButtonHandler);
                    horizontalTreeView.getRedoButton().setOnAction(null);
                }
            }
        } catch (Exception e) {}

        try { if (undoRedoList != null) undoRedoList = null; } catch (Exception e) {}

        try { if (horizontalTreeView != null) horizontalTreeView.cleanup(); } catch (Exception e) {}
        try { if (htCheck != null) htCheck.cleanup(); } catch (Exception e) {}

        try { this.mainWindow = null; } catch (Exception e) {}
        try { this.mainView = null; } catch (Exception e) {}
        try { this.horizontalTreeModel = null; } catch (Exception e) {}
        try { this.horizontalTreeView = null; } catch (Exception e) {}
        try { this.undoRedoFlag = null; } catch (Exception e) {}
        try { this.htCheck = null; } catch (Exception e) {}
        try { this.interpretationDRTA = null; } catch (Exception e) {}
        try { this.interpretationEditor = null; } catch (Exception e) {}

        try { this.undoRedoFlagListener = null; } catch (Exception e) {}
        try { this.interpFullHeightListener = null; } catch (Exception e) {}
        try { this.interpModifiedListener = null; } catch (Exception e) {}
        try { this.interpFocusListener = null; } catch (Exception e) {}
        try { this.pointsEarnedListener = null; } catch (Exception e) {}
        try { this.statementFocusListener = null; } catch (Exception e) {}
        try { this.commentFullHeightListener = null; } catch (Exception e) {}
        try { this.commentModifiedListener = null; } catch (Exception e) {}
        try { this.commentFocusListener = null; } catch (Exception e) {}
        try { this.explainFullHeightListener = null; } catch (Exception e) {}
        try { this.explainModifiedListener = null; } catch (Exception e) {}
        try { this.explainFocusListener = null; } catch (Exception e) {}
        try { this.undoButtonHandler = null; } catch (Exception e) {}
        try { this.redoButtonHandler = null; } catch (Exception e) {}
    }

}
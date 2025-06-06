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
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.Spinner;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.apache.commons.lang3.SerializationUtils;
import slapp.editor.DiskUtilities;
import slapp.editor.PrintUtilities;
import slapp.editor.decorated_rta.DecoratedRTA;
import slapp.editor.main_window.*;
import slapp.editor.vertical_tree.VerticalTreeExercise;

import java.util.ArrayList;
import java.util.List;

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
        undoRedoFlag.set(false);
        undoRedoFlag.bind(horizontalTreeView.undoRedoFlagProperty());
        undoRedoFlag.addListener((ob, ov, nv) -> {
            if (nv) {
                exerciseModified = true;
                pushUndoRedo();
            }
        });
        pushUndoRedo();
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
        horizontalTreeView.getPointsEarnedTextField().textProperty().addListener((ob, ov, nv) -> {
            exerciseModified = true;
        });

        //statement
        DecoratedRTA statementDRTA = new DecoratedRTA();
        RichTextArea statementEditor = statementDRTA.getEditor();
        statementEditor.getActionFactory().open(horizontalTreeModel.getExerciseStatement()).execute(new ActionEvent());

        statementEditor.focusedProperty().addListener((o, ov, nv) -> {
            if (nv) {
                mainView.editorInFocus(statementDRTA, ControlType.STATEMENT);
            }
        });
        horizontalTreeView.setExerciseStatement(statementDRTA);

        //comment
        DecoratedRTA commentDRTA = new DecoratedRTA();
        RichTextArea commentEditor = commentDRTA.getEditor();
        commentEditor.setPromptText("Comment: ");
        commentEditor.getActionFactory().open(horizontalTreeModel.getExerciseComment()).execute(new ActionEvent());

        commentEditor.addEventHandler(KeyEvent.KEY_RELEASED, e -> {
            exerciseModified = true;
            double commentTextHeight = mainView.getRTATextHeight(commentEditor);
            horizontalTreeModel.setCommentTextHeight(commentTextHeight);
        });

        commentEditor.focusedProperty().addListener((o, ov, nv) -> {
            if (nv) {
                mainView.editorInFocus(commentDRTA, ControlType.AREA);
            }
        });
        horizontalTreeView.setExerciseComment(commentDRTA);

        //explain
        DecoratedRTA explainDRTA = horizontalTreeView.getExplainDRTA();
        RichTextArea explainEditor = explainDRTA.getEditor();
        explainEditor.getActionFactory().open(horizontalTreeModel.getExplainDocument()).execute(new ActionEvent());
        horizontalTreeView.setExplainPrompt(horizontalTreeModel.getExplainPrompt());

        explainEditor.addEventHandler(KeyEvent.KEY_RELEASED, e -> {
            exerciseModified = true;
            double explanationTextHeight = mainView.getRTATextHeight(explainEditor);
            horizontalTreeModel.setExplainTextHeight(explanationTextHeight);
        });

        explainEditor.getActionFactory().saveNow().execute(new ActionEvent());
        mainView.editorInFocus(explainDRTA, ControlType.AREA);
        explainEditor.focusedProperty().addListener((o, ov, nv) -> {
            if (nv) {
                mainView.editorInFocus(explainDRTA, ControlType.AREA);
            }
        });

        //buttons and cleanup
        horizontalTreeView.getUndoButton().setOnAction(e -> undoAction());
        horizontalTreeView.getRedoButton().setOnAction(e -> redoAction());

        horizontalTreeView.initializeViewDetails();

        refreshViewFromModel();
 //       horizontalTreeView.setAnnotationModified(false);
    }

    /*
     * Refresh view including panes and ruler axis from model
     */
    private void refreshViewFromModel() {
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
        horizontalTreeView.getTreePanes().clear();

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
    }

    /*
     * Recursively fill out nodes on branch
     * @param branchNode current node to update
     * @param branchModel model for the node
     */
    private void setTreeNodes(BranchNode branchNode, BranchModel branchModel) {
        if (branchModel.isAnnotation()) {
            branchNode.addAnnotation();
            branchNode.setAnnBump(branchNode.getAnnotationWidth() * .75 + 5);
            branchNode.setAnnotation(true);
        }
        branchNode.setFormulaNode(branchModel.isFormulaBranch());
        branchNode.setIndefiniteNode(branchModel.isIndefiniteNumBranch());
        branchNode.setDotDivider(branchModel.isDotDivider());
        branchNode.setRoot(branchModel.isRootBranch());

//        branchNode.getAnnotationField().getRTA().getActionFactory().open(new Document(branchModel.getAnnotationText())).execute(new ActionEvent());
        branchNode.getAnnotationField().getRTA().getActionFactory().open(branchModel.getAnnotationDoc()).execute(new ActionEvent());

        RichTextArea formulaRTA = branchNode.getFormulaBoxedDRTA().getRTA();
        formulaRTA.getActionFactory().open(branchModel.getFormulaDoc()).execute(new ActionEvent());
        formulaRTA.getActionFactory().saveNow().execute(new ActionEvent());

        RichTextArea connectRTA = branchNode.getConnectorBoxedDRTA().getRTA();
        connectRTA.getActionFactory().open(branchModel.getConnectorDoc()).execute(new ActionEvent());
        connectRTA.getActionFactory().saveNow().execute(new ActionEvent());


        if (!branchModel.isFormulaBranch()) branchNode.setStyle("-fx-border-color: white white white white; -fx-border-width: 0 0 0 0");
        if (branchModel.isIndefiniteNumBranch()) branchNode.setStyle("-fx-border-color: white white white white; -fx-border-width: 0 0 0 0");


        for (BranchModel dependentMod : branchModel.getDependents()) {
            BranchNode dependentNode = new BranchNode(branchNode, horizontalTreeView);
            branchNode.getDependents().add(dependentNode);
            setTreeNodes(dependentNode, dependentMod);

        }
    }

    /*
     * Perform undo action
     */
    private void undoAction() {
        HorizontalTreeModel undoElement = undoRedoList.getUndoElement();
        if (undoElement != null) {
            horizontalTreeModel = (HorizontalTreeModel) SerializationUtils.clone(undoElement);
            refreshViewFromModel();
            updateUndoRedoButtons();
            horizontalTreeView.deselectToggles();
        }
    }

    /*
     * Perform redo action
     */
    private void redoAction() {
        HorizontalTreeModel redoElement = undoRedoList.getRedoElement();
        if (redoElement != null) {
            horizontalTreeModel = (HorizontalTreeModel) SerializationUtils.clone(redoElement);
            refreshViewFromModel();
            updateUndoRedoButtons();
            horizontalTreeView.deselectToggles();
        }
    }

    /*
     * Update undo/redo button disable based on undo redo stack
     */
    private void updateUndoRedoButtons() {
        horizontalTreeView.getUndoButton().setDisable(!undoRedoList.canUndo());
        horizontalTreeView.getRedoButton().setDisable(!undoRedoList.canRedo());
    }

    /*
     * Push current state onto undo/redo stack
     */
    private void pushUndoRedo() {
        HorizontalTreeModel model = getHorizontalTreeModelFromView();
        HorizontalTreeModel deepCopy = (HorizontalTreeModel) SerializationUtils.clone(model);
        undoRedoList.push(deepCopy);
        updateUndoRedoButtons();
    }


    /**
     * The Horizontal tree model
     * @return the model
     */
    @Override
    public HorizontalTreeModel getExerciseModel() { return horizontalTreeModel;  }

    /**
     * The horizontal tree view
     * @return the view
     */
    @Override
    public HorizontalTreeView getExerciseView() { return horizontalTreeView;  }

    /**
     * Save exercise to disk
     * @param saveAs true if "save as" should be invoked, and otherwise false
     */
    @Override
    public void saveExercise(boolean saveAs) {
        boolean success = DiskUtilities.saveExercise(saveAs, getHorizontalTreeModelFromView());
        if (success) {
            exerciseModified = false;
        }
    }

    /**
     * List of nodes to be sent to printer for this exercise
     * @return the node list
     */
    @Override
    public List<Node> getPrintNodes(ExerciseModel originalModel) {
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

        //content
        AnchorPane mainPane = printExercise.getExerciseView().getMainPane();
        Group root = new Group();
        Scene scene = new Scene(root);
        root.getChildren().add(mainPane);
        root.applyCss();
        root.layout();

        Ruler axis = new Ruler();
 //       double axisWidth = Math.max(nodeWidth, mainPane.getWidth() - 10);
        double axisWidth = Math.max(nodeWidth, printModel.getMainPaneWidth() - 10);

        axis.updateRuler(axisWidth);

        mainPane.setStyle("-fx-background-color: transparent");
        mainPane.setPrefWidth(printModel.getMainPaneWidth());

        printExercise.getExerciseView().simpleRemoveAxis();

        ObservableList<Node> mainNodes = mainPane.getChildren();
        for (Node mainNode : mainNodes) {

            if (mainNode instanceof TreePane) {
                TreePane treePane = (TreePane) mainNode;
                ObservableList<Node> paneNodes = treePane.getChildren();
                for (Node paneNode : paneNodes) {
                    if (paneNode instanceof BranchNode) {
                        BranchNode branchNode = (BranchNode) paneNode;
                        branchNode.getFormulaBoxedDRTA().getRTA().setStyle("-fx-border-color: transparent");
                        branchNode.getConnectorBoxedDRTA().getRTA().setStyle("-fx-border-color: transparent");
                        branchNode.getAnnotationField().getRTA().setStyle("-fx-border-color: transparent");
                    }
                }
            }
        }

        AnchorPane mainStack = new AnchorPane(axis, mainPane);
        mainStack.setLeftAnchor(axis, 5.0);

        if (!horizontalTreeModel.isAxis()) mainStack.getChildren().remove(axis);
        HBox contentHBox = new HBox(mainStack);
        contentHBox.setAlignment(Pos.CENTER);
        contentHBox.setPadding(new Insets(10,0,20, 0));
        contentHBox.setMaxWidth(axisWidth);
        nodeList.add(contentHBox);


        //explain node
        RichTextArea explainRTA = printExercise.getExerciseView().getExplainDRTA().getEditor();
        explainRTA.prefHeightProperty().unbind();
        explainRTA.minWidthProperty().unbind();
        double explainHeight = printModel.getExplainTextHeight();
        explainRTA.setPrefHeight(explainHeight + 35.0);
        explainRTA.setContentAreaWidth(nodeWidth);
        explainRTA.setMinWidth(nodeWidth);
        explainRTA.getStylesheets().clear(); statementRTA.getStylesheets().add("richTextAreaPrinter.css");
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
    public Exercise<HorizontalTreeModel, HorizontalTreeView> resetExercise() {
        RichTextArea commentRTA = horizontalTreeView.getExerciseComment().getEditor();
        commentRTA.getActionFactory().saveNow().execute(new ActionEvent());
        Document commentDocument = commentRTA.getDocument();
        int pointsEarned = -1;
        if (!horizontalTreeView.getPointsEarnedTextField().getText().equals("")) pointsEarned = Integer.parseInt(horizontalTreeView.getPointsEarnedTextField().getText());
        HorizontalTreeModel originalModel = (HorizontalTreeModel) (horizontalTreeModel.getOriginalModel());
        originalModel.setExerciseComment(commentDocument);
        originalModel.setPointsEarned(pointsEarned);
        HorizontalTreeExercise clearExercise = new HorizontalTreeExercise(originalModel, mainWindow);
        return clearExercise;
    }

    /**
     * Exercise is modified if it is changed relative to last save
     * @return true if exercise is modified, and otherwise false
     */
    @Override
    public boolean isExerciseModified() {      return exerciseModified;  }

    /**
     * Exercise is modified if it is changed relative to last save
     * @param modified true if exercise is modified, and otherwise false
     */
    @Override
    public void setExerciseModified(boolean modified) { exerciseModified = modified;   }

    /**
     * The view node to be displayed as a component of the free form exercise.  This will be a (possibly modified)
     * portion of the regular exercise view.
     * @return the view node
     */
    @Override
    public Node getFFViewNode() { return horizontalTreeView.getMainPane(); }

    @Override
    public Spinner getFFHeightSpinner() {return horizontalTreeView.getMainPaneHeightSpinner();}
    @Override
    public Spinner getFFWidthSpinner() {return horizontalTreeView.getMainPaneWidthSpinner();}

    /**
     * The node to be printed as a component of the free form exercise.
     * @return the print node
     */
    @Override
    public Node getFFPrintNode(ExerciseModel originalModel) {
        HorizontalTreeExercise printExercise = this;
        HorizontalTreeModel printModel = horizontalTreeModel;
        double nodeWidth = PrintUtilities.getPageWidth() / mainWindow.getBaseScale();

        AnchorPane mainPane = printExercise.getExerciseView().getMainPane();
        Group root = new Group();
        Scene scene = new Scene(root);
        root.getChildren().add(mainPane);
        root.applyCss();
        root.layout();

        Ruler axis = new Ruler();
        double axisWidth = Math.max(nodeWidth, printModel.getMainPaneWidth() - 10);

        axis.updateRuler(axisWidth);

        mainPane.setStyle("-fx-background-color: transparent");
        mainPane.setPrefWidth(printModel.getMainPaneWidth());
        printExercise.getExerciseView().simpleRemoveAxis();

        ObservableList<Node> mainNodes = mainPane.getChildren();
        for (Node mainNode : mainNodes) {

            if (mainNode instanceof TreePane) {
                TreePane treePane = (TreePane) mainNode;
                ObservableList<Node> paneNodes = treePane.getChildren();
                for (Node paneNode : paneNodes) {
                    if (paneNode instanceof BranchNode) {
                        BranchNode branchNode = (BranchNode) paneNode;
                        branchNode.getFormulaBoxedDRTA().getRTA().setStyle("-fx-border-color: transparent");
                        branchNode.getConnectorBoxedDRTA().getRTA().setStyle("-fx-border-color: transparent");

                        branchNode.getAnnotationField().getRTA().setStyle("-fx-border-color: transparent");

                     //   branchNode.getAnnotationField().setStyle("-fx-background-color: transparent");
                    }
                }
            }
        }

        AnchorPane mainStack = new AnchorPane(axis, mainPane);
        mainStack.setLeftAnchor(axis, 5.0);

        if (!horizontalTreeModel.isAxis()) mainStack.getChildren().remove(axis);
        HBox contentHBox = new HBox(mainStack);
        contentHBox.setAlignment(Pos.CENTER);
        contentHBox.setPadding(new Insets(10,0,20, 0));
        contentHBox.setMaxWidth(axisWidth);

        return contentHBox;
    }

    /**
     * Extract an {@link slapp.editor.main_window.ExerciseModel} from view of the exercise
     * @return the exercise model
     */
    @Override
    public ExerciseModel<HorizontalTreeModel> getExerciseModelFromView() {
        return (ExerciseModel) getHorizontalTreeModelFromView();
    }

    /*
     * Extract the horizontal tree model from view
     * @return the model
     */
    private HorizontalTreeModel getHorizontalTreeModelFromView() {
        HorizontalTreeModel model = new HorizontalTreeModel();

        model.setExerciseName(horizontalTreeModel.getExerciseName());
        model.setExplainPrompt(horizontalTreeModel.getExplainPrompt());
        model.setOriginalModel(horizontalTreeModel.getOriginalModel());
        model.setStarted(horizontalTreeModel.isStarted() || exerciseModified);

        model.setExerciseStatement(horizontalTreeModel.getExerciseStatement());
        model.setStatementPrefHeight(horizontalTreeView.getExerciseStatement().getEditor().getPrefHeight());
        model.setStatementTextHeight(horizontalTreeModel.getStatementTextHeight());

        RichTextArea commentRTA = horizontalTreeView.getExerciseComment().getEditor();
        commentRTA.getActionFactory().saveNow().execute(new ActionEvent());
        model.setExerciseComment(commentRTA.getDocument());
        model.setCommentPrefHeight(horizontalTreeView.getCommentPrefHeight());
        model.setCommentTextHeight(horizontalTreeModel.getCommentTextHeight());

        RichTextArea explainRTA = horizontalTreeView.getExplainDRTA().getEditor();
        explainRTA.getActionFactory().saveNow().execute(new ActionEvent());
        model.setExplainDocument(explainRTA.getDocument());
        model.setExplainPrefHeight(horizontalTreeView.getExplainPrefHeight());
        model.setExplainTextHeight(horizontalTreeModel.getExplainTextHeight());

        model.setMainPaneWidth(horizontalTreeView.getMainPane().getWidth());

        model.setAxis(horizontalTreeView.isAxis());

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

    /*
     * Recursively populate nodes on branch from view
     * @param model the current branch model
     * @param node the current branch node
     */
    private void setBranchModel(BranchModel model, BranchNode node) {
        model.setAnnotation(node.isAnnotation());
        model.setFormulaBranch(node.isFormulaNode());
        model.setIndefiniteNumBranch(node.isIndefiniteNode());
        model.setDotDivider(node.isDotDivider());
        model.setRootBranch(node.isRoot());
        model.setAnnotation(node.isAnnotation());

//        model.tripAnnotationSwitch();
        RichTextArea annotationRTA = node.getAnnotationField().getRTA();
        annotationRTA.getActionFactory().saveNow().execute(new ActionEvent());
        model.setAnnotationDoc(annotationRTA.getDocument());

//        model.setAnnotationText(node.getAnnotationField().getRTA().getDocument().getText());

        RichTextArea formulaRTA = node.getFormulaBoxedDRTA().getRTA();
        formulaRTA.getActionFactory().saveNow().execute(new ActionEvent());
        model.setFormulaDoc(formulaRTA.getDocument());

        RichTextArea connectRTA = node.getConnectorBoxedDRTA().getRTA();
        connectRTA.getActionFactory().saveNow().execute(new ActionEvent());
        model.setConnectorDoc(connectRTA.getDocument());

        for (BranchNode dependentNode : node.getDependents()) {
            BranchModel dependentMod = new BranchModel();
            model.getDependents().add(dependentMod);
            setBranchModel(dependentMod, dependentNode);
        }
    }

}

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

package slapp.editor.horiz_trees;

import com.gluonhq.richtextarea.RichTextArea;
import com.gluonhq.richtextarea.model.Document;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import slapp.editor.DiskUtilities;
import slapp.editor.EditorAlerts;
import slapp.editor.PrintUtilities;
import slapp.editor.decorated_rta.DecoratedRTA;
import slapp.editor.free_form.*;
import slapp.editor.horizontal_tree.*;
import slapp.editor.main_window.*;

import javax.swing.text.View;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static slapp.editor.derivation.DerivationExercise.inHierarchy;
import static slapp.editor.EditorMain.RTA_PRINT_BMP;
import static slapp.editor.EditorMain.RTA_SCREEN_BMP;

/**
 * Controller for the free form exercise
 */
public class HorizTreesExercise implements Exercise<HorizTreesModel, HorizTreesView> {

    private MainWindow mainWindow;
    private MainWindowView mainView;
    private HorizTreesModel horizTreesModel;
    private HorizTreesView horizTreesView;
    private boolean exerciseModified = false;
    private List<Exercise> exerciseList = new ArrayList<>();
    private Exercise activeExercise = null;
    private UndoRedoList<HorizTreesModel> undoRedoList = new UndoRedoList<>(20);
    private EventHandler exerciseClickFilter;
    private ModelElement lastRemovedElement;

    private HorizTreesCheck htCheck;
    DecoratedRTA interpretationDRTA;
    RichTextArea interpretationEditor;


    /**
     * Construct free form exercise from model
     * @param model the model
     * @param mainWindow the main window
     */
    public HorizTreesExercise(HorizTreesModel model, MainWindow mainWindow) {
        this.mainWindow = mainWindow;
        mainView = mainWindow.getMainView();
        this.horizTreesModel = model;
        if (model.getOriginalModel() == null) {model.setOriginalModel(model);}
        if (model.getModelElements().isEmpty()) {
            HorizontalTreeModel stub = new HorizontalTreeModel();
            model.getModelElements().add(new ModelElement(stub, 0));
        }



        exerciseClickFilter = new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                List<ViewElement> viewElements = horizTreesView.getViewElements();

                for (int i = 0; i < exerciseList.size(); i++) {
                    ViewElement viewElement = viewElements.get(i);
                    if (inHierarchy(event.getPickResult().getIntersectedNode(), viewElement.getNode().getParent())) {
                        Exercise exer = exerciseList.get(i);
                        activeExercise = exer;
                        ExerciseView exerView = (ExerciseView) exer.getExerciseView();
                        horizTreesView.setExerciseControlNode(exerView.getExerciseControl());
                        mainWindow.getMainView().setUpLeftControl(horizTreesView.getExerciseControl());

                        horizTreesView.setBorders(i);
                        horizTreesView.setContentSpinners(i, exerciseList.get(i).getFFHeightSpinner(), exerciseList.get(i).getFFWidthSpinner());
                        ((HorizontalTreeExercise) exer).updateUndoRedoButtons();
                    }
                    else ((HorizontalTreeExercise) exerciseList.get(i)).getHorizontalTreeView().getFormulaNodeToggle().setSelected(false);
                }
            }
        };

        mainView.getMainScene().addEventFilter(MouseEvent.MOUSE_PRESSED, exerciseClickFilter);

        this.horizTreesView = new HorizTreesView(mainView);
        horizTreesView.setRightControlBox();

        htCheck = new HorizTreesCheck(this);
        setInterpretationDRTA();
        setHorizTreesView();
        horizTreesView.initalizeInterpretationRTA();
        setActiveExercise(0);
    }

    private void setInterpretationDRTA() {

        interpretationDRTA = new DecoratedRTA();
        interpretationEditor = interpretationDRTA.getEditor();

        interpretationEditor.addEventHandler(KeyEvent.KEY_RELEASED, e -> {
            exerciseModified = true;
            double interpretationTextHeight = mainView.getRTATextHeight(interpretationEditor);
            htCheck.getCheckSetup().setInterpretationTextHeight(interpretationTextHeight);
        });
        interpretationEditor.getActionFactory().open(htCheck.getCheckSetup().getInterpretationDoc()).execute(new ActionEvent());

        mainView.editorInFocus(interpretationDRTA, ControlType.AREA);
        interpretationEditor.focusedProperty().addListener((o, ov, nv) -> {
            if (nv) {
                mainView.editorInFocus(interpretationDRTA, ControlType.AREA);
                double interpretationTextHeight = mainView.getRTATextHeight(interpretationEditor);
                htCheck.getCheckSetup().setInterpretationTextHeight(interpretationTextHeight);
            }
        });

        horizTreesView.setInterpretationDRTA(interpretationDRTA);
        horizTreesView.setInterpretationRTA(interpretationEditor);
        horizTreesView.setInterpretationPrefHeight(htCheck.getCheckSetup().getInterpretationPrefHeight());

    }

    /*
     * Set up the free form view from the model
     */
    private void setHorizTreesView() {
        horizTreesView.setStatementPrefHeight(horizTreesModel.getStatementPrefHeight());
        horizTreesView.setCommentPrefHeight(horizTreesModel.getCommentPrefHeight());
        horizTreesView.setExplainPrefHeight(horizTreesModel.getExplainPrefHeight());

        horizTreesView.setPointsPossible(horizTreesModel.getPointsPossible());
        if (horizTreesModel.getPointsEarned() >= 0) horizTreesView.getPointsEarnedTextField().setText(Integer.toString(horizTreesModel.getPointsEarned()));
        horizTreesView.getPointsEarnedTextField().textProperty().addListener((ob, ov, nv) -> {
            exerciseModified = true;
        });

        //statement
        DecoratedRTA statementDRTA = new DecoratedRTA();
        RichTextArea statementEditor = statementDRTA.getEditor();
        statementEditor.getActionFactory().open(horizTreesModel.getExerciseStatement()).execute(new ActionEvent());

        statementEditor.focusedProperty().addListener((o, ov, nv) -> {
            if (nv) {
                mainView.editorInFocus(statementDRTA, ControlType.STATEMENT);
                mainView.getRTATextHeight(statementEditor);
            }
        });
        horizTreesView.setExerciseStatement(statementDRTA);
        statementEditor.setPrefHeight(statementEditor.getFullHeight() + RTA_SCREEN_BMP);

        //comment
        DecoratedRTA commentDRTA = new DecoratedRTA();
        RichTextArea commentEditor = commentDRTA.getEditor();

        commentEditor.addEventHandler(KeyEvent.KEY_RELEASED, e -> {
            exerciseModified = true;
            double commentTextHeight = mainView.getRTATextHeight(commentEditor);
            horizTreesModel.setCommentTextHeight(commentTextHeight);
        });
        commentEditor.getActionFactory().open(horizTreesModel.getExerciseComment()).execute(new ActionEvent());

        commentEditor.focusedProperty().addListener((o, ov, nv) -> {
            if (nv) {
                mainView.editorInFocus(commentDRTA, ControlType.AREA);
                double commentTextHeight = mainView.getRTATextHeight(commentEditor);
                horizTreesModel.setCommentTextHeight(commentTextHeight);
            }
        });
        horizTreesView.setExerciseComment(commentDRTA);

        //explain
        DecoratedRTA explainDRTA = horizTreesView.getExplainDRTA();
        RichTextArea explainEditor = explainDRTA.getEditor();
        explainEditor.getActionFactory().open(horizTreesModel.getExplainDocument()).execute(new ActionEvent());
        horizTreesView.setExplainPrompt(horizTreesModel.getExplainPrompt());

        explainEditor.addEventHandler(KeyEvent.KEY_RELEASED, e -> {
            exerciseModified = true;
            double explanationTextHeight = mainView.getRTATextHeight(explainEditor);
            horizTreesModel.setExplainTextHeight(explanationTextHeight);
        });

        explainEditor.getActionFactory().saveNow().execute(new ActionEvent());
        mainView.editorInFocus(explainDRTA, ControlType.AREA);
        explainEditor.focusedProperty().addListener((o, ov, nv) -> {
            if (nv) {
                mainView.editorInFocus(explainDRTA, ControlType.AREA);
                double explanationTextHeight = mainView.getRTATextHeight(explainEditor);
                horizTreesModel.setExplainTextHeight(explanationTextHeight);
            }
        });

        //buttons
        horizTreesView.getRemoveButton().setOnAction(e -> removeAction());
        horizTreesView.getRestoreButton().setOnAction(e -> restoreAction());

        horizTreesView.getMoveUpButton().setOnAction(e -> moveUpAction());
        horizTreesView.getMoveDownButton().setOnAction(e -> moveDownAction());


        horizTreesView.getAddHTreeButton().setOnAction(e -> addHTreeAction());


        horizTreesView.getRestoreButton().setDisable(true);
        horizTreesView.getRemoveButton().setDisable(false);

        //cleanup
//        horizTreesView.setRightControlBox();
        horizTreesView.initializeViewDetails();

        if (htCheck.getCheckSetup().getTargetFormulas().size() > 1) populateRightControlBox();
        setElementsFromModel();
        horizTreesView.updateContentFromViewElements();
    }



    /*
     * Fill the free form control based on selected buttons
     */
    private void populateRightControlBox() {
        VBox controlBox = (VBox) horizTreesView.getRightControl();
        ObservableList<Node> members = controlBox.getChildren();
        members.add(new Separator(Orientation.HORIZONTAL));

        for (ElementTypes type : horizTreesModel.getElementTypes()) {
            switch(type) {
                case HORIZ_TREE: {
                    members.add(horizTreesView.getAddHTreeButton());
                    break;
                }
            }
        }
        members.addAll(horizTreesView.getMoveUpButton(), horizTreesView.getMoveDownButton(), horizTreesView.getRemoveButton(), horizTreesView.getRestoreButton());

    }



    /*
     * Populate the view element list from model
     */
    private void setElementsFromModel() {
        TypeSelectorFactories typeFactory = new TypeSelectorFactories(mainWindow);
        List<ViewElement> viewElements = horizTreesView.getViewElements();
        viewElements.clear();
        exerciseList.clear();

        for (ModelElement modelElement : horizTreesModel.getModelElements()) {
            ExerciseModel model = modelElement.getModel();
            int indentLevel = modelElement.getIndentLevel();

            Exercise exer = typeFactory.getExerciseFromModelObject(model);
            Node viewNode = exer.getFFViewNode();

            exerciseList.add(exer);
            ViewElement viewElement = new ViewElement(viewNode, indentLevel);
            viewElements.add(viewElement);
        }
    }



    /*
     * Make view element the active exercise
     * @param index index of the element to activate
     */
    private void setActiveExercise(int index) {
        Exercise exer = exerciseList.get(index);
        activeExercise = exer;
        ExerciseView exerView = (ExerciseView) exer.getExerciseView();
        horizTreesView.setExerciseControlNode(exerView.getExerciseControl());
        mainWindow.getMainView().setUpLeftControl(horizTreesView.getExerciseControl());



    //    exer.getFFViewNode().setStyle("-fx-border-color: lightblue");
        horizTreesView.setBorders(index);

        Platform.runLater(() -> {
            horizTreesView.setContentSpinners(index, exerciseList.get(index).getFFHeightSpinner(), exerciseList.get(index).getFFWidthSpinner());
        });

    }



    /*
     * Remove the active exercise
     */
    private void removeAction()  {
        horizTreesModel = getHorizTreesModelFromView();
        List<ModelElement> modelElements = horizTreesModel.getModelElements();
        if (modelElements.size() > 1) {
            int index = exerciseList.indexOf(activeExercise);
            lastRemovedElement = modelElements.get(index);
            modelElements.remove(index);
            setElementsFromModel();
            horizTreesView.updateContentFromViewElements();

            horizTreesView.getRestoreButton().setDisable(false);
            exerciseModified = true;
            if (index > 0) setActiveExercise(index - 1);
            else setActiveExercise(index);
        } else {
            EditorAlerts.fleetingRedPopup("Exercise must contain at least one element.");
        }
    }

    /*
     * Restore last removed view element
     */
    private void restoreAction() {
        horizTreesModel = getHorizTreesModelFromView();
        List<ModelElement> modelElements = horizTreesModel.getModelElements();
        int index = exerciseList.indexOf(activeExercise);
        modelElements.add(++index, lastRemovedElement);
        setElementsFromModel();
        horizTreesView.updateContentFromViewElements();

        horizTreesView.getRestoreButton().setDisable(true);
        setActiveExercise(index);
        exerciseModified = true;
    }

    /*
     * Exchange position of current element with one above
     */
    private void moveUpAction() {
        horizTreesModel = getHorizTreesModelFromView();
        List<ModelElement> modelElements = horizTreesModel.getModelElements();
        int index = exerciseList.indexOf(activeExercise);
        if (index > 0) {
            Collections.swap(modelElements, index, index - 1);
            setElementsFromModel();
            horizTreesView.updateContentFromViewElements();
            exerciseModified = true;
            setActiveExercise(--index);
        } else {
            EditorAlerts.fleetingRedPopup("Cannot move top item up.");
        }
    }

    /*
     * Exchange position of current element with one below
     */
    private void moveDownAction() {
        horizTreesModel = getHorizTreesModelFromView();
        List<ModelElement> modelElements = horizTreesModel.getModelElements();
        int index = exerciseList.indexOf(activeExercise);
        if (index + 1 < modelElements.size()) {
            Collections.swap(modelElements, index, index + 1);
            setElementsFromModel();
            horizTreesView.updateContentFromViewElements();
            exerciseModified = true;
            setActiveExercise(++index);
        } else {
            EditorAlerts.fleetingRedPopup("Cannot move bottom item down.");
        }
    }





    /*
     * Add Horizontal Tree exercise after active exercise
     */
    private void addHTreeAction() {

        List<Node> treeBoxes = horizTreesView.getContentBox().getChildren();
        int index = exerciseList.indexOf(activeExercise);
        horizTreesModel = getHorizTreesModelFromView();
        HorizontalTreeModel newMod = new HorizontalTreeModel();
        ModelElement element = new ModelElement(newMod, 0);
        horizTreesModel.getModelElements().add(++index, element);
        setElementsFromModel();
        horizTreesView.updateContentFromViewElements();
        setActiveExercise(index);
        horizTreesView.getRemoveButton().setDisable(false);
        exerciseModified = true;

    }



    public Exercise getActiveExercise() { return activeExercise;}

    /**
     * The free form exercise does not contribute any node to a free form exercise
     * @return null
     */
    @Override
    public Node getFFViewNode() {return null;}

    @Override
    public Spinner getFFHeightSpinner() {return null;}
    @Override
    public Spinner getFFWidthSpinner() {return null;}

    /**
     * The free form exercise does not contribute any node to a free form exercise
     * @return null
     */
    @Override
    public Node getFFPrintNode(ExerciseModel originalModel) {return null;}

    /**
     * The free form model
     * @return the model
     */
    @Override
    public HorizTreesModel getExerciseModel() { return horizTreesModel;   }

    /**
     * The free form view
     * @return the view
     */
    @Override
    public HorizTreesView getExerciseView() { return horizTreesView;   }

    /**
     * Save exercise to disk
     * @param saveAs true if "save as" should be invoked, and otherwise false
     */
    @Override
    public void saveExercise(boolean saveAs) {
        boolean success = DiskUtilities.saveExercise(saveAs, getHorizTreesModelFromView());
        if (success) {
            exerciseModified = false;
            for (Exercise exercise : exerciseList) exercise.setExerciseModified(false);
        }
    }

    /**
     * List of nodes to be sent to printer for this exercise
     * @return the node list
     */
    @Override
    public List<Node> getPrintNodes(ExerciseModel originalModel) {
        List<Node> nodeList = new ArrayList<>();
        HorizTreesModel printModel = horizTreesModel;
        HorizTreesExercise printExercise = this;
        double nodeWidth = PrintUtilities.getPageWidth() / mainWindow.getBaseScale();


        //header node
        Label exerciseName = new Label(printModel.getExerciseName());
        exerciseName.setStyle("-fx-font-weight: bold;");
        Region spacer = new Region();
        HBox hbox = new HBox(exerciseName, spacer, printCheckNode());
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
        statementRTA.setMinWidth(nodeWidth);

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
        interpretationRTA.prefHeightProperty().unbind();
        interpretationRTA.minWidthProperty().unbind();
        interpretationRTA.maxWidthProperty().unbind();
        double interpTextHeight = htCheck.getCheckSetup().getInterpretationTextHeight();
        interpretationRTA.setPrefHeight(Math.max(40, interpTextHeight + RTA_PRINT_BMP));
        interpretationRTA.setMaxWidth(nodeWidth);
        interpretationRTA.setMinWidth(nodeWidth);

        interpretationRTA.getStylesheets().clear(); interpretationRTA.getStylesheets().add("richTextAreaPrinter.css");
        nodeList.add(interpretationRTA);

        for (int i = 0; i < exerciseList.size(); i++) {
            Exercise exerElement = exerciseList.get(i);

            HorizTreesModel originalHTmodel = (HorizTreesModel) originalModel;
            ModelElement originalModelElement = originalHTmodel.getModelElements().get(i);
            ExerciseModel originalItemModel = originalModelElement.getModel();

            Node printNode = exerElement.getFFPrintNode(originalItemModel);

            nodeList.add(exerElement.getFFPrintNode(originalItemModel));
        }

        //explain node
        RichTextArea explainRTA = printExercise.getExerciseView().getExplainDRTA().getEditor();
        explainRTA.prefHeightProperty().unbind();
        explainRTA.minWidthProperty().unbind();
        explainRTA.maxWidthProperty().unbind();
        explainRTA.setPrefHeight(Math.max(40, printModel.getCommentTextHeight() + RTA_PRINT_BMP));
        explainRTA.setMaxWidth(nodeWidth);
        explainRTA.setMinWidth(nodeWidth);

        explainRTA.getStylesheets().clear(); statementRTA.getStylesheets().add("richTextAreaPrinter.css");
        nodeList.add(explainRTA);

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
  //          anchorPane.setPrefHeight(printModel.getCommentTextHeight());
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
    public Exercise<HorizTreesModel, HorizTreesView> resetExercise() {
        RichTextArea commentRTA = horizTreesView.getExerciseComment().getEditor();
        commentRTA.getActionFactory().saveNow().execute(new ActionEvent());
        Document commentDocument = commentRTA.getDocument();
        int pointsEarned = -1;
        if (!horizTreesView.getPointsEarnedTextField().getText().equals("")) pointsEarned = Integer.parseInt(horizTreesView.getPointsEarnedTextField().getText());
        HorizTreesModel originalModel = (HorizTreesModel) (horizTreesModel.getOriginalModel());
        originalModel.setExerciseComment(commentDocument);
        originalModel.setPointsEarned(pointsEarned);
        HorizTreesExercise clearExercise = new HorizTreesExercise(originalModel, mainWindow);
        exerciseModified = false;
        return clearExercise;
    }

    TextFlow printCheckNode() {
        TextFlow flow = new TextFlow();
        flow.setMaxWidth(200);
        if (htCheck.isCheckSuccess()) {
            Text bigCheck = new Text("\ue89a");
            bigCheck.setFont(Font.font("Noto Serif Combo", 14));
            Text message = new Text("  " + horizTreesView.getCheckMessage());
            message.setFont(Font.font("Noto Serif Combo", 11));

            bigCheck.setFill(Color.LAWNGREEN);
            message.setFill(Color.GREEN);
            flow.getChildren().addAll(bigCheck, message);
        }
        return flow;
    }

    /**
     * Exercise is modified if it is changed relative to last save
     * @return true if exercise is modified, and otherwise false
     */
    @Override
    public boolean isExerciseModified() {

        for (Exercise exercise : exerciseList) {
            if (exercise.isExerciseModified()) {
                exerciseModified = true;
            }
        }
        return exerciseModified;
    }

    /**
     * Exercise is modified if it is changed relative to last save
     * @param modified true if exercise is modified, and otherwise false
     */
    @Override
    public void setExerciseModified(boolean modified) {
        if (modified) exerciseModified = true;
        else {
            exerciseModified = false;
            for (Exercise exercise : exerciseList) {
                exercise.setExerciseModified(false);
            }
        }
    }

    /**
     * Extract an {@link ExerciseModel} from view of the exercise
     * @return the exercise model
     */
    @Override
    public ExerciseModel<HorizTreesModel> getExerciseModelFromView() {
        return (ExerciseModel) getHorizTreesModelFromView();
    }

    /*
     * Extract the free form model from view
     * @return the model
     */
    private HorizTreesModel getHorizTreesModelFromView() {
        HorizTreesModel model = new HorizTreesModel(horizTreesModel.getExerciseName(), horizTreesModel.getElementTypes());
        model.setExplainPrompt(horizTreesModel.getExplainPrompt());
        model.setOriginalModel(horizTreesModel.getOriginalModel());
        model.setStarted(horizTreesModel.isStarted() || exerciseModified);

        HorizTreesCheckSetup checkSetup = new HorizTreesCheckSetup();
        HorizTreesCheckSetup horizTreesCheckSetup = horizTreesModel.getCheckSetup();
        if (htCheck != null) {
            checkSetup.setCheckTries(htCheck.getCheckTries());
            checkSetup.setCheckSuccess(htCheck.isCheckSuccess());
            checkSetup.setCheckMessage(horizTreesView.getCheckMessage());
            checkSetup.setEmptyBranchesSelected(horizTreesView.getPermitEmptyBranchesCheck().isSelected());
            checkSetup.setObjLangName(horizTreesCheckSetup.getObjLangName());
            checkSetup.setCheckMax(horizTreesCheckSetup.getCheckMax());
            checkSetup.setStaticHelp(horizTreesCheckSetup.isStaticHelp());
            checkSetup.setStaticHelpDoc(horizTreesCheckSetup.getStaticHelpDoc());
            checkSetup.setEmptyBranchesOK(horizTreesCheckSetup.isEmptyBranchesOK());
            checkSetup.setTargetFormulas(horizTreesCheckSetup.getTargetFormulas());
            checkSetup.setInterpretationTextHeight(horizTreesCheckSetup.getInterpretationTextHeight());
            checkSetup.setInterpretationPrefHeight(horizTreesCheckSetup.getInterpretationPrefHeight());
        }
        model.setCheckSetup(checkSetup);


        model.setExerciseStatement(horizTreesModel.getExerciseStatement());
        model.setStatementPrefHeight(horizTreesView.getExerciseStatement().getEditor().getPrefHeight());
        model.setStatementTextHeight(horizTreesModel.getStatementTextHeight());

        RichTextArea commentRTA = horizTreesView.getExerciseComment().getEditor();
        commentRTA.getActionFactory().saveNow().execute(new ActionEvent());
        model.setExerciseComment(commentRTA.getDocument());
        model.setCommentPrefHeight(horizTreesView.getCommentPrefHeight());
        model.setCommentTextHeight(horizTreesModel.getCommentTextHeight());

        RichTextArea explainRTA = horizTreesView.getExplainDRTA().getEditor();
        explainRTA.getActionFactory().saveNow().execute(new ActionEvent());
        model.setExplainDocument(explainRTA.getDocument());
        model.setExplainPrefHeight(horizTreesView.getExplainPrefHeight());
        model.setExplainTextHeight(horizTreesModel.getExplainTextHeight());

        RichTextArea interpretationRTA = horizTreesView.getInterpretationDRTA().getEditor();
        interpretationRTA.getActionFactory().saveNow().execute(new ActionEvent());
        Document interpretationDocument = interpretationRTA.getDocument();
        checkSetup.setInterpretationDoc(interpretationDocument);
        checkSetup.setInterpretationPrefHeight(horizTreesView.getInterpretationPrefHeight());
        checkSetup.setInterpretationTextHeight(Math.max(60, horizTreesModel.getCheckSetup().getInterpretationTextHeight()));  //+29

        model.setPointsPossible(horizTreesModel.getPointsPossible());
        if (!horizTreesView.getPointsEarnedTextField().getText().equals("")) model.setPointsEarned(Integer.parseInt(horizTreesView.getPointsEarnedTextField().getText()));
        else model.setPointsEarned(-1);

        List<ModelElement> modelElements = new ArrayList<>();
        for (int i = 0; i < exerciseList.size(); i++) {
            Exercise exer = exerciseList.get(i);
            ExerciseModel exerModel = exer.getExerciseModelFromView();
            int indent = horizTreesView.getViewElements().get(i).getIndentLevel();
            ModelElement element = new ModelElement(exerModel, indent);
            modelElements.add(element);
        }
        model.setModelElements(modelElements);

        return model;
    }

    public List<Exercise> getExerciseList() {return exerciseList;}

    public MainWindow getMainWindow() {
        return mainWindow;
    }

    public HorizTreesView getHorizTreesView() {
        return horizTreesView;
    }
}

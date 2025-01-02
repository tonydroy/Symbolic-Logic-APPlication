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

package slapp.editor.free_form;

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
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.Spinner;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import slapp.editor.DiskUtilities;
import slapp.editor.EditorAlerts;
import slapp.editor.PrintUtilities;
import slapp.editor.decorated_rta.DecoratedRTA;
import slapp.editor.derivation.DerivationModel;
import slapp.editor.derivation.LineType;
import slapp.editor.derivation.ModelLine;
import slapp.editor.horizontal_tree.HorizontalTreeModel;
import slapp.editor.main_window.*;
import slapp.editor.simple_edit.SimpleEditModel;
import slapp.editor.truth_table_generate.TruthTableGenExercise;
import slapp.editor.truth_table_generate.TruthTableGenModel;
import slapp.editor.vertical_tree.VerticalTreeExercise;
import slapp.editor.vertical_tree.VerticalTreeModel;
import slapp.editor.vertical_tree.VerticalTreeView;
import slapp.editor.vertical_tree.drag_drop.DragIconType;
import slapp.editor.vertical_tree.object_models.ObjectControlType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.gluonhq.richtextarea.RichTextAreaSkin.KeyMapValue.*;
import static slapp.editor.derivation.DerivationExercise.inHierarchy;

/**
 * Controller for the free form exercise
 */
public class FreeFormExercise implements Exercise<FreeFormModel, FreeFormView> {

    private MainWindow mainWindow;
    private MainWindowView mainView;
    private FreeFormModel freeFormModel;
    private FreeFormView freeFormView;
    private boolean exerciseModified = false;
    private List<Exercise> exerciseList = new ArrayList<>();
    private Exercise activeExercise = null;
    private UndoRedoList<FreeFormModel> undoRedoList = new UndoRedoList<>(20);
    private EventHandler exerciseClickFilter;
    private ModelElement lastRemovedElement;

    /**
     * Construct free form exercise from model
     * @param model the model
     * @param mainWindow the main window
     */
    public FreeFormExercise(FreeFormModel model, MainWindow mainWindow) {
        this.mainWindow = mainWindow;
        mainView = mainWindow.getMainView();
        freeFormModel = model;
        if (model.getOriginalModel() == null) {model.setOriginalModel(model);}
        if (model.getModelElements().isEmpty()) {
            SimpleEditModel stub = new SimpleEditModel("", "Simple Edit");
            model.getModelElements().add(new ModelElement(stub, 0));
        }


        exerciseClickFilter = new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                List<ViewElement> viewElements = freeFormView.getViewElements();
                for (int i = 0; i < exerciseList.size(); i++) {
                    ViewElement viewElement = viewElements.get(i);
                    if (inHierarchy(event.getPickResult().getIntersectedNode(), viewElement.getNode().getParent())) {
                    //    viewElement.getNode().requestFocus();
                        Exercise exer = exerciseList.get(i);
                        activeExercise = exer;

                        ExerciseType type = ((ExerciseModel)exer.getExerciseModel()).getExerciseType();
                        if (type == ExerciseType.SIMPLE_EDIT || type ==ExerciseType.HORIZONTAL_TREE) {
                            freeFormView.getIndentButton().setDisable(true);
                            freeFormView.getOutdentButton().setDisable(true);
                        } else {
                            freeFormView.getIndentButton().setDisable(false);
                            freeFormView.getOutdentButton().setDisable(false);
                        }

                        ExerciseView exerView = (ExerciseView) exer.getExerciseView();

                        freeFormView.setExerciseControlNode(exerView.getExerciseControl());
                        mainWindow.getMainView().setUpLeftControl(freeFormView.getExerciseControl());

             //           viewElement.getNode().setStyle("-fx-border-color: lightblue");
                        freeFormView.setBorders(i);
                        freeFormView.setContentSpinners(i, exerciseList.get(i).getFFHeightSpinner(), exerciseList.get(i).getFFWidthSpinner());
                    }
              //      else viewElement.getNode().setStyle("-fx-border-color: gainsboro");
                }
            }
        };

        mainView.getMainScene().addEventFilter(MouseEvent.MOUSE_PRESSED, exerciseClickFilter);

        freeFormView = new FreeFormView(mainView);
        setFreeFormView();
        setActiveExercise(0);
    }

    /*
     * Set up the free form view from the model
     */
    private void setFreeFormView() {
        freeFormView.setStatementPrefHeight(freeFormModel.getStatementPrefHeight());
        freeFormView. setCommentPrefHeight(freeFormModel.getCommentPrefHeight());

        //statement
        DecoratedRTA statementDRTA = new DecoratedRTA();
        RichTextArea statementEditor = statementDRTA.getEditor();
        statementEditor.getActionFactory().open(freeFormModel.getExerciseStatement()).execute(new ActionEvent());

        statementEditor.focusedProperty().addListener((o, ov, nv) -> {
            if (nv) {
                mainView.editorInFocus(statementDRTA, ControlType.STATEMENT);
            }
        });
        freeFormView.setExerciseStatement(statementDRTA);

        //comment
        DecoratedRTA commentDRTA = new DecoratedRTA();
        RichTextArea commentEditor = commentDRTA.getEditor();

        commentEditor.addEventHandler(KeyEvent.KEY_RELEASED, e -> {
            exerciseModified = true;
            double commentTextHeight = mainView.getRTATextHeight(commentEditor);
            freeFormModel.setCommentTextHeight(commentTextHeight);
        });
        commentEditor.getActionFactory().open(freeFormModel.getExerciseComment()).execute(new ActionEvent());

        commentEditor.focusedProperty().addListener((o, ov, nv) -> {
            if (nv) {
                mainView.editorInFocus(commentDRTA, ControlType.AREA);
            }
        });
        freeFormView.setExerciseComment(commentDRTA);

        //buttons
        freeFormView.getRemoveButton().setOnAction(e -> removeAction());
        freeFormView.getRestoreButton().setOnAction(e -> restoreAction());
        freeFormView.getIndentButton().setOnAction(e -> indentAction());
        freeFormView.getOutdentButton().setOnAction(e -> outdentAction());
        freeFormView.getMoveUpButton().setOnAction(e -> moveUpAction());
        freeFormView.getMoveDownButton().setOnAction(e -> moveDownAction());

        freeFormView.getAddEditButton().setOnAction(e -> addEditAction());
        freeFormView.getAddHTreeButton().setOnAction(e -> addHTreeAction());
        freeFormView.getAddTTableButton().setOnAction(e -> addTTableAction());

        freeFormView.getAddVTreeBaseItalButton().setOnAction(e -> addVTreeBaseItalAction());
        freeFormView.getAddVTreeItalSansButton().setOnAction(e -> addVTreeItalSansAction());
        freeFormView.getAddVTreeScriptItalButton().setOnAction(e -> addVTreeScriptItalAction());
        freeFormView.getAddNDrvtnItalSansButton().setOnAction(e -> addNDrvtnItalSansAction());
        freeFormView.getAddNDrvtnScriptItalButton().setOnAction(e -> addNDrvtnScriptItalAction());
        freeFormView.getAddNDrvtnScriptSansButton().setOnAction(e -> addNDrvtnScriptSansAction());
        freeFormView.getAddNDrvtnItalBBButton().setOnAction(e -> addNDrvtnItalBBAction());
        freeFormView.getAddADrvtnItalSansButton().setOnAction(e -> addADrvtnItalSansAction());
        freeFormView.getAddADrvtnScriptItalButton().setOnAction(e -> addADrvtnScriptItalicAction());
        freeFormView.getAddADrvtnScriptSansButton().setOnAction(e -> addADrvtnScriptSansAction());
        freeFormView.getAddADrvtnItalBBButton().setOnAction(e -> addADrvtnItalBBAction());

        freeFormView.getRestoreButton().setDisable(true);
        freeFormView.getRemoveButton().setDisable(true);

        //cleanup
        freeFormView.initializeViewDetails();
        populateRightControlBox();
        setElementsFromModel();
        freeFormView.updateContentFromViewElements();
    }

    /*
     * Fill the free form control based on selected buttons
     */
    private void populateRightControlBox() {
        VBox controlBox = (VBox) freeFormView.getRightControl();
        ObservableList<Node> members = controlBox.getChildren();
        members.clear();

        for (ElementTypes type : freeFormModel.getElementTypes()) {
            switch(type) {

                case SIMPLE_EDIT: {
                    members.add(freeFormView.getAddEditButton());
                    break;
                }
                case HORIZ_TREE: {
                    members.add(freeFormView.getAddHTreeButton());
                    break;
                }
                case TRUTH_TABLE: {
                    members.add(freeFormView.getAddTTableButton());
                    break;
                }
                case VERT_TREE_BASE_ITAL: {
                    members.add(freeFormView.getAddVTreeBaseItalButton());
                    break;
                }
                case VERT_TREE_ITAL_SANS: {
                    members.add(freeFormView.getAddVTreeItalSansButton());
                    break;
                }
                case VERT_TREE_SCRIPT_ITAL: {
                    members.add(freeFormView.getAddVTreeScriptItalButton());
                    break;
                }
                case N_DERIVATION_ITAL_SANS: {
                    members.add(freeFormView.getAddNDrvtnItalSansButton());
                    break;
                }
                case N_DERIVATION_SCRIPT_ITAL: {
                    members.add(freeFormView.getAddNDrvtnScriptItalButton());
                    break;
                }
                case N_DERIVATION_SCIRPT_SANS: {
                    members.add(freeFormView.getAddNDrvtnScriptSansButton());
                    break;
                }
                case N_DERIVATION_ITAL_BB: {
                    members.add(freeFormView.getAddNDrvtnItalBBButton());
                    break;
                }
                case A_DERIVATION_ITAL_SANS: {
                    members.add(freeFormView.getAddADrvtnItalSansButton());
                    break;
                }
                case A_DERIVATION_SCRIPT_ITAL: {
                    members.add(freeFormView.getAddADrvtnScriptItalButton());
                    break;
                }
                case A_DERIVATION_SCRIPT_SANS: {
                    members.add(freeFormView.getAddADrvtnScriptSansButton());
                    break;
                }
                case A_DERIVATION_ITAL_BB: {
                    members.add(freeFormView.getAddADrvtnItalBBButton());
                    break;
                }
            }
        }
        members.addAll(freeFormView.getIndentButton(), freeFormView.getOutdentButton(), freeFormView.getMoveUpButton(), freeFormView.getMoveDownButton(), freeFormView.getRemoveButton(), freeFormView.getRestoreButton());
        controlBox.setMargin(freeFormView.getIndentButton(), new Insets(25,0,0,0));
    }

    /*
     * Populate the view element list from model
     */
    private void setElementsFromModel() {
        TypeSelectorFactories typeFactory = new TypeSelectorFactories(mainWindow);
        List<ViewElement> viewElements = freeFormView.getViewElements();
        viewElements.clear();
        exerciseList.clear();

        for (ModelElement modelElement : freeFormModel.getModelElements()) {
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
     * Increase indent of current view element
     */
    private void indentAction() {
        int index = exerciseList.indexOf(activeExercise);
        ViewElement viewElement = freeFormView.getViewElements().get(index);
        int indent = viewElement.getIndentLevel();
        viewElement.setIndentLevel(++indent);
        freeFormView.updateContentFromViewElements();
        exerciseModified = true;
    }

    /*
     * Decrease indent of current view element
     */
    private void outdentAction() {
        int index = exerciseList.indexOf(activeExercise);
        ViewElement viewElement = freeFormView.getViewElements().get(index);
        int indent = viewElement.getIndentLevel();
        if (indent > 0) viewElement.setIndentLevel(--indent);
        freeFormView.updateContentFromViewElements();
        exerciseModified = true;
    }

    /*
     * Make view element the active exercise
     * @param index index of the element to activate
     */
    private void setActiveExercise(int index) {
        Exercise exer = exerciseList.get(index);
        activeExercise = exer;
        ExerciseView exerView = (ExerciseView) exer.getExerciseView();
        freeFormView.setExerciseControlNode(exerView.getExerciseControl());
        mainWindow.getMainView().setUpLeftControl(freeFormView.getExerciseControl());

        ExerciseType type = ((ExerciseModel)exer.getExerciseModel()).getExerciseType();

        if (type == ExerciseType.SIMPLE_EDIT || type == ExerciseType.HORIZONTAL_TREE) {
            freeFormView.getIndentButton().setDisable(true);
            freeFormView.getOutdentButton().setDisable(true);
        } else {
            freeFormView.getIndentButton().setDisable(false);
            freeFormView.getOutdentButton().setDisable(false);
        }
        freeFormView.getRemoveButton().setDisable(false);


    //    exer.getFFViewNode().setStyle("-fx-border-color: lightblue");
        freeFormView.setBorders(index);

        Platform.runLater(() -> {
            freeFormView.setContentSpinners(index, exerciseList.get(index).getFFHeightSpinner(), exerciseList.get(index).getFFWidthSpinner());
        });

    }



    /*
     * Remove the active exercise
     */
    private void removeAction()  {
        freeFormModel = getFreeFormModelFromView();
        List<ModelElement> modelElements = freeFormModel.getModelElements();
        if (modelElements.size() > 1) {
            int index = exerciseList.indexOf(activeExercise);
            lastRemovedElement = modelElements.get(index);
            modelElements.remove(index);
            setElementsFromModel();
            freeFormView.updateContentFromViewElements();

            freeFormView.getRestoreButton().setDisable(false);
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
        freeFormModel = getFreeFormModelFromView();
        List<ModelElement> modelElements = freeFormModel.getModelElements();
        int index = exerciseList.indexOf(activeExercise);
        modelElements.add(++index, lastRemovedElement);
        setElementsFromModel();
        freeFormView.updateContentFromViewElements();

        freeFormView.getRestoreButton().setDisable(true);
        setActiveExercise(index);
        exerciseModified = true;
    }

    /*
     * Exchange position of current element with one above
     */
    private void moveUpAction() {
        freeFormModel = getFreeFormModelFromView();
        List<ModelElement> modelElements = freeFormModel.getModelElements();
        int index = exerciseList.indexOf(activeExercise);
        if (index > 0) {
            Collections.swap(modelElements, index, index - 1);
            setElementsFromModel();
            freeFormView.updateContentFromViewElements();
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
        freeFormModel = getFreeFormModelFromView();
        List<ModelElement> modelElements = freeFormModel.getModelElements();
        int index = exerciseList.indexOf(activeExercise);
        if (index + 1 < modelElements.size()) {
            Collections.swap(modelElements, index, index + 1);
            setElementsFromModel();
            freeFormView.updateContentFromViewElements();
            exerciseModified = true;
            setActiveExercise(++index);
        } else {
            EditorAlerts.fleetingRedPopup("Cannot move bottom item down.");
        }
    }

    /*
     * Add Simple Edit exercise after active exercise
     */
    private void addEditAction() {
        freeFormModel = getFreeFormModelFromView();
        SimpleEditModel newMod = new SimpleEditModel("", "Simple Edit");
        int index = exerciseList.indexOf(activeExercise);
        ModelElement element = new ModelElement(newMod, 0);
        freeFormModel.getModelElements().add(++index, element);
        setElementsFromModel();
        freeFormView.updateContentFromViewElements();
        setActiveExercise(index);
        freeFormView.getRemoveButton().setDisable(false);
        exerciseModified = true;
    }


    /*
     * Add Truth Table exercise after active exercise
     */
    private void addTTableAction() {
        freeFormModel = getFreeFormModelFromView();

        TruthTableGenModel newMod = new TruthTableGenModel();
        newMod.setConclusionDivider(true);
        List<String> unaryList = new ArrayList<>();
        unaryList.add("\u223c");
        newMod.setUnaryOperators(unaryList);
        List<String> binaryList = new ArrayList<>();
        binaryList.addAll(Arrays.asList("\u2192", "\u2194", "\u2227", "\u2228"));
        newMod.setBinaryOperators(binaryList);
        TruthTableGenExercise exer = new TruthTableGenExercise(newMod, mainWindow, true);

        int index = exerciseList.indexOf(activeExercise);
        ModelElement element = new ModelElement(newMod, 0);
        freeFormModel.getModelElements().add(++index, element);

        setElementsFromModel();
        freeFormView.updateContentFromViewElements();
        setActiveExercise(index);
        freeFormView.getRemoveButton().setDisable(false);
        exerciseModified = true;

    }

    /*
     * Add Horizontal Tree exercise after active exercise
     */
    private void addHTreeAction() {
        freeFormModel = getFreeFormModelFromView();
        HorizontalTreeModel newMod = new HorizontalTreeModel();
        int index = exerciseList.indexOf(activeExercise);
        ModelElement element = new ModelElement(newMod, 0);
        freeFormModel.getModelElements().add(++index, element);
        setElementsFromModel();
        freeFormView.updateContentFromViewElements();
        setActiveExercise(index);
        freeFormView.getRemoveButton().setDisable(false);
        exerciseModified = true;
    }

    /*
     * Add Vertical Tree exercise (with Base/Italic default font) after active exercise
     */
    private void addVTreeBaseItalAction() {
        freeFormModel = getFreeFormModelFromView();
        VerticalTreeModel newMod = new VerticalTreeModel();

        newMod.getDragIconList().addAll(Arrays.asList(DragIconType.tree_field, DragIconType.bracket, DragIconType.dashed_line, DragIconType.map_field));
        newMod.getObjectControlList().addAll(Arrays.asList(ObjectControlType.FORMULA_BOX, ObjectControlType.CIRCLE, ObjectControlType.STAR,
                ObjectControlType.ANNOTATION, ObjectControlType.UNDERLINE, ObjectControlType.MAPPING));
        newMod.setDefaultKeyboardType(BASE);

        int index = exerciseList.indexOf(activeExercise);
        ModelElement element = new ModelElement(newMod, 0);
        freeFormModel.getModelElements().add(++index, element);
        setElementsFromModel();
        freeFormView.updateContentFromViewElements();
        setActiveExercise(index);
        freeFormView.getRemoveButton().setDisable(false);
        exerciseModified = true;
    }

    /*
     * Add Vertical Tree exercise (with Italic/Sans default font) after active exercise
     */
    private void addVTreeItalSansAction() {
        freeFormModel = getFreeFormModelFromView();
        VerticalTreeModel newMod = new VerticalTreeModel();

        newMod.getDragIconList().addAll(Arrays.asList(DragIconType.tree_field, DragIconType.bracket, DragIconType.dashed_line, DragIconType.map_field));
        newMod.getObjectControlList().addAll(Arrays.asList(ObjectControlType.FORMULA_BOX, ObjectControlType.CIRCLE, ObjectControlType.STAR,
                ObjectControlType.ANNOTATION, ObjectControlType.UNDERLINE, ObjectControlType.MAPPING));
        newMod.setDefaultKeyboardType(ITALIC_AND_SANS);

        int index = exerciseList.indexOf(activeExercise);
        ModelElement element = new ModelElement(newMod, 0);
        freeFormModel.getModelElements().add(++index, element);
        setElementsFromModel();
        freeFormView.updateContentFromViewElements();
        setActiveExercise(index);
        freeFormView.getRemoveButton().setDisable(false);
        exerciseModified = true;
    }

    /*
     * Add Vertical Tree exercise (with Script/Italic default font) after active exercise
     */
    private void addVTreeScriptItalAction() {
        freeFormModel = getFreeFormModelFromView();
        VerticalTreeModel newMod = new VerticalTreeModel();

        newMod.getDragIconList().addAll(Arrays.asList(DragIconType.tree_field, DragIconType.bracket, DragIconType.dashed_line, DragIconType.map_field));
        newMod.getObjectControlList().addAll(Arrays.asList(ObjectControlType.FORMULA_BOX, ObjectControlType.CIRCLE, ObjectControlType.STAR,
                ObjectControlType.ANNOTATION, ObjectControlType.UNDERLINE, ObjectControlType.MAPPING));
        newMod.setDefaultKeyboardType(SCRIPT_AND_ITALIC);

        int index = exerciseList.indexOf(activeExercise);
        ModelElement element = new ModelElement(newMod, 0);
        freeFormModel.getModelElements().add(++index, element);
        setElementsFromModel();
        freeFormView.updateContentFromViewElements();
        setActiveExercise(index);
        freeFormView.getRemoveButton().setDisable(false);
        exerciseModified = true;
    }

    /*
     * Add Derivation exercise (with natural derivation settings, and Italic/Sans default font) after active exercise
     */
    private void addNDrvtnItalSansAction() {
        freeFormModel = getFreeFormModelFromView();

        List<ModelLine> modelLines = new ArrayList<>();
        ModelLine line1 = new ModelLine(1, new Document(), "", LineType.MAIN_CONTENT_LINE);
        ModelLine line2 = new ModelLine(1, new Document(), "", LineType.MAIN_CONTENT_LINE);
        ModelLine line3 = new ModelLine(1, new Document(), "", LineType.MAIN_CONTENT_LINE);
        modelLines.addAll(Arrays.asList(line1, line2, line3));

        DerivationModel newMod = new DerivationModel("", false, 80, .64, true, true, ITALIC_AND_SANS,
                new Document(), new Document(), modelLines);

        int index = exerciseList.indexOf(activeExercise);
        ModelElement element = new ModelElement(newMod, 0);
        freeFormModel.getModelElements().add(++index, element);
        setElementsFromModel();
        freeFormView.updateContentFromViewElements();
        setActiveExercise(index);
        freeFormView.getRemoveButton().setDisable(false);
        exerciseModified = true;
    }

    /*
     * Add Derivation exercise (with natural derivation settings, and Script/Italic default font) after active exercise
     */
    private void addNDrvtnScriptItalAction() {
        freeFormModel = getFreeFormModelFromView();

        List<ModelLine> modelLines = new ArrayList<>();
        ModelLine line1 = new ModelLine(1, new Document(), "", LineType.MAIN_CONTENT_LINE);
        ModelLine line2 = new ModelLine(1, new Document(), "", LineType.MAIN_CONTENT_LINE);
        ModelLine line3 = new ModelLine(1, new Document(), "", LineType.MAIN_CONTENT_LINE);
        modelLines.addAll(Arrays.asList(line1, line2, line3));

        DerivationModel newMod = new DerivationModel("", false, 80, .64, true, true, SCRIPT_AND_ITALIC,
                new Document(), new Document(), modelLines);

        int index = exerciseList.indexOf(activeExercise);
        ModelElement element = new ModelElement(newMod, 0);
        freeFormModel.getModelElements().add(++index, element);
        setElementsFromModel();
        freeFormView.updateContentFromViewElements();
        setActiveExercise(index);
        freeFormView.getRemoveButton().setDisable(false);
        exerciseModified = true;
    }

    /*
     * Add Derivation exercise (with natural derivation settings, and Script/Sans default font) after active exercise
     */
    private void addNDrvtnScriptSansAction() {
        freeFormModel = getFreeFormModelFromView();

        List<ModelLine> modelLines = new ArrayList<>();
        ModelLine line1 = new ModelLine(1, new Document(), "", LineType.MAIN_CONTENT_LINE);
        ModelLine line2 = new ModelLine(1, new Document(), "", LineType.MAIN_CONTENT_LINE);
        ModelLine line3 = new ModelLine(1, new Document(), "", LineType.MAIN_CONTENT_LINE);
        modelLines.addAll(Arrays.asList(line1, line2, line3));

        DerivationModel newMod = new DerivationModel("", false, 80, .64, true, true, SCRIPT_AND_SANS,
                new Document(), new Document(), modelLines);

        int index = exerciseList.indexOf(activeExercise);
        ModelElement element = new ModelElement(newMod, 0);
        freeFormModel.getModelElements().add(++index, element);
        setElementsFromModel();
        freeFormView.updateContentFromViewElements();
        setActiveExercise(index);
        freeFormView.getRemoveButton().setDisable(false);
        exerciseModified = true;
    }

    /*
     * Add Derivation exercise (with natural derivation settings, and Italic/Blackboard default font) after active exercise
     */
    private void addNDrvtnItalBBAction() {
        freeFormModel = getFreeFormModelFromView();

        List<ModelLine> modelLines = new ArrayList<>();
        ModelLine line1 = new ModelLine(1, new Document(), "", LineType.MAIN_CONTENT_LINE);
        ModelLine line2 = new ModelLine(1, new Document(), "", LineType.MAIN_CONTENT_LINE);
        ModelLine line3 = new ModelLine(1, new Document(), "", LineType.MAIN_CONTENT_LINE);
        modelLines.addAll(Arrays.asList(line1, line2, line3));

        DerivationModel newMod = new DerivationModel("", false, 80, .64, true, true, ITALIC_AND_BLACKBOARD,
                new Document(), new Document(), modelLines);

        int index = exerciseList.indexOf(activeExercise);
        ModelElement element = new ModelElement(newMod, 0);
        freeFormModel.getModelElements().add(++index, element);
        setElementsFromModel();
        freeFormView.updateContentFromViewElements();
        setActiveExercise(index);
        freeFormView.getRemoveButton().setDisable(false);
        exerciseModified = true;
    }

    /*
     * Add Derivation exercise (with axiomatic derivation settings, and Italic/Sans default font) after active exercise
     */
    private void addADrvtnItalSansAction() {
        freeFormModel = getFreeFormModelFromView();

        List<ModelLine> modelLines = new ArrayList<>();
        ModelLine line1 = new ModelLine(1, new Document(), "", LineType.MAIN_CONTENT_LINE);
        ModelLine line2 = new ModelLine(1, new Document(), "", LineType.MAIN_CONTENT_LINE);
        ModelLine line3 = new ModelLine(1, new Document(), "", LineType.MAIN_CONTENT_LINE);
        modelLines.addAll(Arrays.asList(line1, line2, line3));

        DerivationModel newMod = new DerivationModel("", false, 80, .64, false, false, ITALIC_AND_SANS,
                new Document(), new Document(), modelLines);

        int index = exerciseList.indexOf(activeExercise);
        ModelElement element = new ModelElement(newMod, 0);
        freeFormModel.getModelElements().add(++index, element);
        setElementsFromModel();
        freeFormView.updateContentFromViewElements();
        setActiveExercise(index);
        freeFormView.getRemoveButton().setDisable(false);
        exerciseModified = true;
    }

    /*
     * Add Derivation exercise (with axiomatic derivation settings, and Script/Italic default font) after active exercise
     */
    private void addADrvtnScriptItalicAction() {
        freeFormModel = getFreeFormModelFromView();

        List<ModelLine> modelLines = new ArrayList<>();
        ModelLine line1 = new ModelLine(1, new Document(), "", LineType.MAIN_CONTENT_LINE);
        ModelLine line2 = new ModelLine(1, new Document(), "", LineType.MAIN_CONTENT_LINE);
        ModelLine line3 = new ModelLine(1, new Document(), "", LineType.MAIN_CONTENT_LINE);
        modelLines.addAll(Arrays.asList(line1, line2, line3));

        DerivationModel newMod = new DerivationModel("", false, 80, .64, false, false, SCRIPT_AND_ITALIC,
                new Document(), new Document(), modelLines);

        int index = exerciseList.indexOf(activeExercise);
        ModelElement element = new ModelElement(newMod, 0);
        freeFormModel.getModelElements().add(++index, element);
        setElementsFromModel();
        freeFormView.updateContentFromViewElements();
        setActiveExercise(index);
        freeFormView.getRemoveButton().setDisable(false);
        exerciseModified = true;
    }

    /*
     * Add Derivation exercise (with axiomatic derivation settings, and Script/Sans default font) after active exercise
     */
    private void addADrvtnScriptSansAction() {
        freeFormModel = getFreeFormModelFromView();

        List<ModelLine> modelLines = new ArrayList<>();
        ModelLine line1 = new ModelLine(1, new Document(), "", LineType.MAIN_CONTENT_LINE);
        ModelLine line2 = new ModelLine(1, new Document(), "", LineType.MAIN_CONTENT_LINE);
        ModelLine line3 = new ModelLine(1, new Document(), "", LineType.MAIN_CONTENT_LINE);
        modelLines.addAll(Arrays.asList(line1, line2, line3));

        DerivationModel newMod = new DerivationModel("", false, 80, .64, false, false, SCRIPT_AND_SANS,
                new Document(), new Document(), modelLines);

        int index = exerciseList.indexOf(activeExercise);
        ModelElement element = new ModelElement(newMod, 0);
        freeFormModel.getModelElements().add(++index, element);
        setElementsFromModel();
        freeFormView.updateContentFromViewElements();
        setActiveExercise(index);
        freeFormView.getRemoveButton().setDisable(false);
        exerciseModified = true;
    }

    /*
     * Add Derivation exercise (with axiomatic derivation settings, and Italic/Blackboard default font) after active exercise
     */
    private void addADrvtnItalBBAction() {
        freeFormModel = getFreeFormModelFromView();

        List<ModelLine> modelLines = new ArrayList<>();
        ModelLine line1 = new ModelLine(1, new Document(), "", LineType.MAIN_CONTENT_LINE);
        ModelLine line2 = new ModelLine(1, new Document(), "", LineType.MAIN_CONTENT_LINE);
        ModelLine line3 = new ModelLine(1, new Document(), "", LineType.MAIN_CONTENT_LINE);
        modelLines.addAll(Arrays.asList(line1, line2, line3));

        DerivationModel newMod = new DerivationModel("", false, 80, .64, false, false, ITALIC_AND_BLACKBOARD,
                new Document(), new Document(), modelLines);

        int index = exerciseList.indexOf(activeExercise);
        ModelElement element = new ModelElement(newMod, 0);
        freeFormModel.getModelElements().add(++index, element);
        setElementsFromModel();
        freeFormView.updateContentFromViewElements();
        setActiveExercise(index);
        freeFormView.getRemoveButton().setDisable(false);
        exerciseModified = true;
    }

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
    public FreeFormModel getExerciseModel() { return freeFormModel;   }

    /**
     * The free form view
     * @return the view
     */
    @Override
    public FreeFormView getExerciseView() { return freeFormView;   }

    /**
     * Save exercise to disk
     * @param saveAs true if "save as" should be invoked, and otherwise false
     */
    @Override
    public void saveExercise(boolean saveAs) {
        boolean success = DiskUtilities.saveExercise(saveAs, getFreeFormModelFromView());
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
        FreeFormModel printModel = freeFormModel;
        FreeFormExercise printExercise = this;
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

        //content nodes
        for (int i = 0; i < exerciseList.size(); i++) {
            Exercise exerElement = exerciseList.get(i);

            FreeFormModel originalFFmodel = (FreeFormModel) originalModel;
            ModelElement originalModelElement = originalFFmodel.getModelElements().get(i);

            ExerciseModel originalItemModel = originalModelElement.getModel();
            Region spacer = new Region();
            spacer.setPrefWidth(20.0 * originalModelElement.getIndentLevel());
            HBox elementBox = new HBox(spacer, exerElement.getFFPrintNode(originalItemModel));

            nodeList.add(elementBox);
        }

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
        nodeList.add(commentRTA);

        return nodeList;
    }

    /**
     * Return to the initial (unworked) version of the exercise, retaining the comment only.
     * @return the initial exercise
     */
    @Override
    public Exercise<FreeFormModel, FreeFormView> resetExercise() {
        RichTextArea commentRTA = freeFormView.getExerciseComment().getEditor();
        commentRTA.getActionFactory().saveNow().execute(new ActionEvent());
        Document commentDocument = commentRTA.getDocument();
        FreeFormModel originalModel = (FreeFormModel) (freeFormModel.getOriginalModel());
        originalModel.setExerciseComment(commentDocument);
        FreeFormExercise clearExercise = new FreeFormExercise(originalModel, mainWindow);
        exerciseModified = false;
        return clearExercise;
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
     * Extract an {@link slapp.editor.main_window.ExerciseModel} from view of the exercise
     * @return the exercise model
     */
    @Override
    public ExerciseModel<FreeFormModel> getExerciseModelFromView() {
        return (ExerciseModel) getFreeFormModelFromView();
    }

    /*
     * Extract the free form model from view
     * @return the model
     */
    private FreeFormModel getFreeFormModelFromView() {
        FreeFormModel model = new FreeFormModel(freeFormModel.getExerciseName(), freeFormModel.getElementTypes());
        model.setOriginalModel(freeFormModel.getOriginalModel());
        model.setStarted(freeFormModel.isStarted() || exerciseModified);
        model.setExerciseStatement(freeFormModel.getExerciseStatement());

        model.setStatementPrefHeight(freeFormView.getExerciseStatement().getEditor().getPrefHeight());
        model.setStatementTextHeight(freeFormModel.getStatementTextHeight());

        RichTextArea commentRTA =freeFormView.getExerciseComment().getEditor();
        commentRTA.getActionFactory().saveNow().execute(new ActionEvent());
        model.setExerciseComment(commentRTA.getDocument());
        model.setCommentPrefHeight(freeFormView.getCommentPrefHeight());
        model.setCommentTextHeight(freeFormModel.getCommentTextHeight());

        List<ModelElement> modelElements = new ArrayList<>();
        for (int i = 0; i < exerciseList.size(); i++) {
            Exercise exer = exerciseList.get(i);
            ExerciseModel exerModel = exer.getExerciseModelFromView();
            int indent = freeFormView.getViewElements().get(i).getIndentLevel();
            ModelElement element = new ModelElement(exerModel, indent);
            modelElements.add(element);
        }
        model.setModelElements(modelElements);

        return model;
    }

    public List<Exercise> getExerciseList() {return exerciseList;}


}

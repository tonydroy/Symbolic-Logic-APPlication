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
import javafx.beans.value.ChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.shape.Rectangle;
import javafx.scene.transform.Scale;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import slapp.editor.EditorMain;
import slapp.editor.DiskUtilities;
import slapp.editor.EditorAlerts;
import slapp.editor.PrintUtilities;
import slapp.editor.decorated_rta.DecoratedRTA;
import slapp.editor.derivation.DerivationModel;
import slapp.editor.derivation.LineType;
import slapp.editor.derivation.ModelLine;
import slapp.editor.horizontal_tree.HorizontalTreeExercise;
import slapp.editor.horizontal_tree.HorizontalTreeModel;
import slapp.editor.horizontal_tree.HorizontalTreeView;
import slapp.editor.main_window.*;
import slapp.editor.simple_edit.SimpleEditModel;
import slapp.editor.truth_table_generate.TruthTableGenExercise;
import slapp.editor.truth_table_generate.TruthTableGenModel;

import static slapp.editor.EditorMain.*;
import slapp.editor.vertical_tree.VerticalTreeModel;
import slapp.editor.vertical_tree.drag_drop.DragIconType;
import slapp.editor.vertical_tree.object_models.ObjectControlType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.gluonhq.richtextarea.RichTextAreaSkin.KeyMapValue.*;
import static slapp.editor.derivation.DerivationExercise.inHierarchy;
import static slapp.editor.EditorMain.RTA_PRINT_BMP;
import static slapp.editor.EditorMain.RTA_SCREEN_BMP;

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
    private EventHandler<MouseEvent> exerciseClickFilter;
    private ModelElement lastRemovedElement;

    private Exercise auxExerciseA;
    private Exercise auxExerciseB;
    Stage tStage;
    Pane thumbPane = new Pane();
    Scene tScene = new Scene(new Pane());

    private FFCheck ffCheck;

    // SCORCHED EARTH RULE 1: The Asynchronous Ghost "Kill Switch"
    private boolean isTornDown = false;

    // SCORCHED EARTH RULE 3: Eradicate Anonymous Lambdas
    private EventHandler<MouseEvent> thumbPaneClickHandler;
    private ChangeListener<String> pointsEarnedListener;
    private ChangeListener<Boolean> statementFocusListener;
    private ChangeListener<Number> commentFullHeightListener;
    private ChangeListener<Boolean> commentModifiedListener;
    private ChangeListener<Boolean> commentFocusListener;

    private EventHandler<ActionEvent> removeButtonHandler;
    private EventHandler<ActionEvent> restoreButtonHandler;
    private EventHandler<ActionEvent> indentButtonHandler;
    private EventHandler<ActionEvent> outdentButtonHandler;
    private EventHandler<ActionEvent> moveUpButtonHandler;
    private EventHandler<ActionEvent> moveDownButtonHandler;
    private EventHandler<ActionEvent> addEditButtonHandler;
    private EventHandler<ActionEvent> addHTreeButtonHandler;
    private EventHandler<ActionEvent> addTTableButtonHandler;
    private EventHandler<ActionEvent> addVTreeBaseItalButtonHandler;
    private EventHandler<ActionEvent> addVTreeItalSansButtonHandler;
    private EventHandler<ActionEvent> addVTreeScriptItalButtonHandler;
    private EventHandler<ActionEvent> addNDrvtnItalSansButtonHandler;
    private EventHandler<ActionEvent> addNDrvtnScriptItalButtonHandler;
    private EventHandler<ActionEvent> addNDrvtnScriptSansButtonHandler;
    private EventHandler<ActionEvent> addNDrvtnItalBBButtonHandler;
    private EventHandler<ActionEvent> addADrvtnItalSansButtonHandler;
    private EventHandler<ActionEvent> addADrvtnScriptItalButtonHandler;
    private EventHandler<ActionEvent> addADrvtnScriptSansButtonHandler;
    private EventHandler<ActionEvent> addADrvtnItalBBButtonHandler;

    public FreeFormExercise(FreeFormModel model, MainWindow mainWindow) {
        this.mainWindow = mainWindow;
        this.mainView = mainWindow.getMainView();
        this.freeFormModel = model;
        if (model.getOriginalModel() == null) model.setOriginalModel(model);
        if (model.getModelElements().isEmpty()) {
            SimpleEditModel stub = new SimpleEditModel("", "Simple Edit");
            model.getModelElements().add(new ModelElement(stub, 0));
        }

        this.freeFormView = new FreeFormView(mainView);
        setFreeFormView();

        Platform.runLater(() -> {
            if (isTornDown || mainView == null) return;
            if (mainWindow != null && mainWindow.getCurrentExercise() != this) return;
            if (auxExerciseA != null) thumbPane = auxImage();
            Platform.runLater(() -> {
                if (isTornDown || mainView == null) return;
                if (mainWindow != null && mainWindow.getCurrentExercise() != this) return;
                setActiveExercise(0);
            });
        });
    }

    @Override
    public void handleMouseEvent(MouseEvent event) {
        if (isTornDown || mainView == null || freeFormView == null || exerciseList == null || event == null) return;
        List<ViewElement> viewElements = freeFormView.getViewElements();
        if (viewElements == null) return;

        for (int i = 0; i < exerciseList.size(); i++) {
            if (i >= viewElements.size() || viewElements.get(i) == null || viewElements.get(i).getNode() == null || viewElements.get(i).getNode().getParent() == null) continue;
            ViewElement viewElement = viewElements.get(i);

            if (inHierarchy(event.getPickResult().getIntersectedNode(), viewElement.getNode().getParent())) {
                Exercise exer = exerciseList.get(i);
                if (exer == null) continue;
                activeExercise = exer;

                ExerciseType type = ((ExerciseModel)exer.getExerciseModel()).getExerciseType();
                if (type == ExerciseType.SIMPLE_EDIT || type == ExerciseType.HORIZONTAL_TREE || type == ExerciseType.VERTICAL_TREE) {
                    if (freeFormView.getIndentButton() != null) freeFormView.getIndentButton().setDisable(true);
                    if (freeFormView.getOutdentButton() != null) freeFormView.getOutdentButton().setDisable(true);
                } else {
                    if (freeFormView.getIndentButton() != null) freeFormView.getIndentButton().setDisable(false);
                    if (freeFormView.getOutdentButton() != null) freeFormView.getOutdentButton().setDisable(false);
                }

                ExerciseView exerView = (ExerciseView) exer.getExerciseView();
                if (exerView != null) setExerciseControl(exerView.getExerciseControl());
                freeFormView.setBorders(i);
                viewElement.getNode().requestFocus();
                freeFormView.setContentSpinners(i, exer.getFFHeightSpinner(), exer.getFFWidthSpinner());
                if (exer instanceof HorizontalTreeExercise) {
                    ((HorizontalTreeExercise) exer).updateUndoRedoButtons();
                }
            } else {
                Exercise exer = exerciseList.get(i);
                if (exer instanceof HorizontalTreeExercise) {
                    HorizontalTreeView htv = ((HorizontalTreeExercise) exer).getHorizontalTreeView();
                    if (htv != null && htv.getFormulaNodeToggle() != null) {
                        htv.getFormulaNodeToggle().setSelected(false);
                    }
                }
            }
        }
    }

    private void setExerciseControl(Node exerControlNode) {
        if (isTornDown || mainView == null || freeFormView == null) return;
        VBox controlBox = new VBox(0, exerControlNode, thumbPane);
        controlBox.setMaxWidth(175);
        controlBox.setMinWidth(175);
        controlBox.setAlignment(Pos.TOP_CENTER);
        VBox.setMargin(thumbPane, new Insets(40, 0, 0, 30));

        freeFormView.setExerciseControlNode(controlBox);
        if (mainWindow != null && mainWindow.getMainView() != null) {
            if (mainWindow.getCurrentExercise() == this) {
                mainWindow.getMainView().setUpLeftControl(freeFormView.getExerciseControl());
            }
        }
    }

    private Pane auxImage() {
        Pane thumbPane = new Pane();
        if (auxExerciseA != null && auxExerciseA.getAuxExerNode() != null) {
            thumbPane.setStyle("-fx-background-color: FAFAFA;");
            Node auxExerNode = auxExerciseA.getAuxExerNode();
            auxExerNode.setMouseTransparent(true);
            auxExerNode.setFocusTraversable(false);
            thumbPane.getChildren().add(auxExerNode);

            Group thumbRoot = new Group();
            Scene scene = new Scene(thumbRoot);
            thumbRoot.getChildren().add(thumbPane);
            thumbRoot.applyCss();
            thumbRoot.layout();

            double thumbWidth = thumbPane.getLayoutBounds().getWidth();
            double scale = thumbWidth > 0 ? 85.0 / thumbWidth : .16;

            Rectangle clip = new Rectangle();
            clip.setWidth(800);
            clip.setHeight(400);
            thumbPane.setClip(clip);

            thumbPane.getTransforms().clear();
            thumbPane.getTransforms().add(new Scale(scale, scale));

            thumbPaneClickHandler = event -> {
                if (isTornDown || mainView == null || auxExerciseB == null) return;
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

            //        tStage.initOwner(EditorMain.mainStage);
                    tStage.initModality(Modality.NONE);
                    Stage mainStage = EditorMain.mainStage;
                    tStage.setX(mainStage.getX() + mainStage.getWidth() - 500);
                    tStage.setY(mainStage.getY() + mainStage.getHeight() - 300);
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
            };
            thumbPane.setOnMouseClicked(thumbPaneClickHandler);
        }
        return thumbPane;
    }

    public void clearStandingPopups() {
        if (isTornDown) return;
        try {
            if (freeFormView != null && freeFormView.getStaticHelpStage() != null) freeFormView.getStaticHelpStage().close();
            if (tStage != null) tStage.close();
        } catch (Exception e) {}
    }

    private void setFreeFormView() {
        if (isTornDown || freeFormView == null || freeFormModel == null) return;
        freeFormView.setStatementPrefHeight(freeFormModel.getStatementPrefHeight());
        freeFormView.setCommentPrefHeight(freeFormModel.getCommentPrefHeight());
        freeFormView.setPointsPossible(freeFormModel.getPointsPossible());

        if (freeFormModel.getPointsEarned() >= 0 && freeFormView.getPointsEarnedTextField() != null) {
            freeFormView.getPointsEarnedTextField().setText(Integer.toString(freeFormModel.getPointsEarned()));
        }

        pointsEarnedListener = (ob, ov, nv) -> {
            if (isTornDown || mainView == null) return;
            exerciseModified = true;
        };
        if (freeFormView.getPointsEarnedTextField() != null) freeFormView.getPointsEarnedTextField().textProperty().addListener(pointsEarnedListener);

        // statement
        DecoratedRTA statementDRTA = new DecoratedRTA();
        RichTextArea statementEditor = statementDRTA.getEditor();
        statementEditor.getActionFactory().open(freeFormModel.getExerciseStatement()).execute(new ActionEvent());

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
        freeFormView.setExerciseStatement(statementDRTA);

        // comment
        DecoratedRTA commentDRTA = new DecoratedRTA();
        RichTextArea commentEditor = commentDRTA.getEditor();
        commentEditor.getActionFactory().open(freeFormModel.getExerciseComment()).execute(new ActionEvent());
        commentEditor.getActionFactory().saveNow().execute(new ActionEvent());

        final boolean[] commentPrimed = {false};
        commentFullHeightListener = (ob, ov, nv) -> {
            if (isTornDown || mainView == null || freeFormModel == null) return;
            if (!commentPrimed[0]) {
                commentPrimed[0] = true;
                return;
            }
            double height = (double) nv;
            mainView.setRTATextHeight(height);
            freeFormModel.setCommentTextHeight(height);
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
        freeFormView.setExerciseComment(commentDRTA);

        // buttons
        removeButtonHandler = e -> { if (isTornDown || mainView == null) return; removeAction(); };
        restoreButtonHandler = e -> { if (isTornDown || mainView == null) return; restoreAction(); };
        indentButtonHandler = e -> { if (isTornDown || mainView == null) return; indentAction(); };
        outdentButtonHandler = e -> { if (isTornDown || mainView == null) return; outdentAction(); };
        moveUpButtonHandler = e -> { if (isTornDown || mainView == null) return; moveUpAction(); };
        moveDownButtonHandler = e -> { if (isTornDown || mainView == null) return; moveDownAction(); };

        addEditButtonHandler = e -> { if (isTornDown || mainView == null) return; addEditAction(); };
        addHTreeButtonHandler = e -> { if (isTornDown || mainView == null) return; addHTreeAction(); };
        addTTableButtonHandler = e -> { if (isTornDown || mainView == null) return; addTTableAction(); };
        addVTreeBaseItalButtonHandler = e -> { if (isTornDown || mainView == null) return; addVTreeBaseItalAction(); };
        addVTreeItalSansButtonHandler = e -> { if (isTornDown || mainView == null) return; addVTreeItalSansAction(); };
        addVTreeScriptItalButtonHandler = e -> { if (isTornDown || mainView == null) return; addVTreeScriptItalAction(); };
        addNDrvtnItalSansButtonHandler = e -> { if (isTornDown || mainView == null) return; addNDrvtnItalSansAction(); };
        addNDrvtnScriptItalButtonHandler = e -> { if (isTornDown || mainView == null) return; addNDrvtnScriptItalAction(); };
        addNDrvtnScriptSansButtonHandler = e -> { if (isTornDown || mainView == null) return; addNDrvtnScriptSansAction(); };
        addNDrvtnItalBBButtonHandler = e -> { if (isTornDown || mainView == null) return; addNDrvtnItalBBAction(); };
        addADrvtnItalSansButtonHandler = e -> { if (isTornDown || mainView == null) return; addADrvtnItalSansAction(); };
        addADrvtnScriptItalButtonHandler = e -> { if (isTornDown || mainView == null) return; addADrvtnScriptItalicAction(); };
        addADrvtnScriptSansButtonHandler = e -> { if (isTornDown || mainView == null) return; addADrvtnScriptSansAction(); };
        addADrvtnItalBBButtonHandler = e -> { if (isTornDown || mainView == null) return; addADrvtnItalBBAction(); };

        if (freeFormView.getRemoveButton() != null) freeFormView.getRemoveButton().setOnAction(removeButtonHandler);
        if (freeFormView.getRestoreButton() != null) freeFormView.getRestoreButton().setOnAction(restoreButtonHandler);
        if (freeFormView.getIndentButton() != null) freeFormView.getIndentButton().setOnAction(indentButtonHandler);
        if (freeFormView.getOutdentButton() != null) freeFormView.getOutdentButton().setOnAction(outdentButtonHandler);
        if (freeFormView.getMoveUpButton() != null) freeFormView.getMoveUpButton().setOnAction(moveUpButtonHandler);
        if (freeFormView.getMoveDownButton() != null) freeFormView.getMoveDownButton().setOnAction(moveDownButtonHandler);

        if (freeFormView.getAddEditButton() != null) freeFormView.getAddEditButton().setOnAction(addEditButtonHandler);
        if (freeFormView.getAddHTreeButton() != null) freeFormView.getAddHTreeButton().setOnAction(addHTreeButtonHandler);
        if (freeFormView.getAddTTableButton() != null) freeFormView.getAddTTableButton().setOnAction(addTTableButtonHandler);
        if (freeFormView.getAddVTreeBaseItalButton() != null) freeFormView.getAddVTreeBaseItalButton().setOnAction(addVTreeBaseItalButtonHandler);
        if (freeFormView.getAddVTreeItalSansButton() != null) freeFormView.getAddVTreeItalSansButton().setOnAction(addVTreeItalSansButtonHandler);
        if (freeFormView.getAddVTreeScriptItalButton() != null) freeFormView.getAddVTreeScriptItalButton().setOnAction(addVTreeScriptItalButtonHandler);
        if (freeFormView.getAddNDrvtnItalSansButton() != null) freeFormView.getAddNDrvtnItalSansButton().setOnAction(addNDrvtnItalSansButtonHandler);
        if (freeFormView.getAddNDrvtnScriptItalButton() != null) freeFormView.getAddNDrvtnScriptItalButton().setOnAction(addNDrvtnScriptItalButtonHandler);
        if (freeFormView.getAddNDrvtnScriptSansButton() != null) freeFormView.getAddNDrvtnScriptSansButton().setOnAction(addNDrvtnScriptSansButtonHandler);
        if (freeFormView.getAddNDrvtnItalBBButton() != null) freeFormView.getAddNDrvtnItalBBButton().setOnAction(addNDrvtnItalBBButtonHandler);
        if (freeFormView.getAddADrvtnItalSansButton() != null) freeFormView.getAddADrvtnItalSansButton().setOnAction(addADrvtnItalSansButtonHandler);
        if (freeFormView.getAddADrvtnScriptItalButton() != null) freeFormView.getAddADrvtnScriptItalButton().setOnAction(addADrvtnScriptItalButtonHandler);
        if (freeFormView.getAddADrvtnScriptSansButton() != null) freeFormView.getAddADrvtnScriptSansButton().setOnAction(addADrvtnScriptSansButtonHandler);
        if (freeFormView.getAddADrvtnItalBBButton() != null) freeFormView.getAddADrvtnItalBBButton().setOnAction(addADrvtnItalBBButtonHandler);

        if (freeFormView.getRestoreButton() != null) freeFormView.getRestoreButton().setDisable(true);
        if (freeFormView.getRemoveButton() != null) freeFormView.getRemoveButton().setDisable(false);

        freeFormView.initializeViewDetails();
        freeFormView.setRightControlBox();
        setupStaticHelp();
        setElementsFromModel();
        freeFormView.updateContentFromViewElements();
        ffCheck = new FFCheck(this);
    }

    private void setupStaticHelp() {
        if (isTornDown || freeFormView == null || freeFormView.getRightControl() == null || freeFormModel == null || freeFormModel.getElementTypes() == null) return;
        VBox controlBox = (VBox) freeFormView.getRightControl();
        ObservableList<Node> members = controlBox.getChildren();
        members.clear();

        for (ElementTypes type : freeFormModel.getElementTypes()) {
            switch(type) {
                case SIMPLE_EDIT: { members.add(freeFormView.getAddEditButton()); break; }
                case HORIZ_TREE: { members.add(freeFormView.getAddHTreeButton()); break; }
                case TRUTH_TABLE: { members.add(freeFormView.getAddTTableButton()); break; }
                case VERT_TREE_BASE_ITAL: { members.add(freeFormView.getAddVTreeBaseItalButton()); break; }
                case VERT_TREE_ITAL_SANS: { members.add(freeFormView.getAddVTreeItalSansButton()); break; }
                case VERT_TREE_SCRIPT_ITAL: { members.add(freeFormView.getAddVTreeScriptItalButton()); break; }
                case N_DERIVATION_ITAL_SANS: { members.add(freeFormView.getAddNDrvtnItalSansButton()); break; }
                case N_DERIVATION_SCRIPT_ITAL: { members.add(freeFormView.getAddNDrvtnScriptItalButton()); break; }
                case N_DERIVATION_SCIRPT_SANS: { members.add(freeFormView.getAddNDrvtnScriptSansButton()); break; }
                case N_DERIVATION_ITAL_BB: { members.add(freeFormView.getAddNDrvtnItalBBButton()); break; }
                case A_DERIVATION_ITAL_SANS: { members.add(freeFormView.getAddADrvtnItalSansButton()); break; }
                case A_DERIVATION_SCRIPT_ITAL: { members.add(freeFormView.getAddADrvtnScriptItalButton()); break; }
                case A_DERIVATION_SCRIPT_SANS: { members.add(freeFormView.getAddADrvtnScriptSansButton()); break; }
                case A_DERIVATION_ITAL_BB: { members.add(freeFormView.getAddADrvtnItalBBButton()); break; }
            }
        }
        members.addAll(new Separator(Orientation.HORIZONTAL), freeFormView.getIndentButton(), freeFormView.getOutdentButton(), freeFormView.getMoveUpButton(),
                freeFormView.getMoveDownButton(), freeFormView.getRemoveButton(), freeFormView.getRestoreButton(), new Separator(Orientation.HORIZONTAL), freeFormView.getStaticHelpButton());
    }

    private void setElementsFromModel() {
        if (isTornDown || mainWindow == null || freeFormView == null || freeFormModel == null) return;
        TypeSelectorFactories typeFactory = new TypeSelectorFactories(mainWindow);
        List<ViewElement> viewElements = freeFormView.getViewElements();

        // SCORCHED EARTH RULE 8: The Undo/Redo Rebuild Fix (Prevent CME & Leaks)
        if (exerciseList != null) {
            List<Exercise> copy = new ArrayList<>(exerciseList);
            for (Exercise ex : copy) {
                try { if (ex != null) ex.cleanup(); } catch (Exception e) {}
            }
            exerciseList.clear();
        }
        if (viewElements != null) {
            List<ViewElement> copy = new ArrayList<>(viewElements);
            for (ViewElement ve : copy) {
                try { if (ve != null && ve.getNode() != null) severFromParent(ve.getNode()); } catch (Exception e) {}
            }
            viewElements.clear();
        }

        if (freeFormModel.getModelElements() != null) {
            for (ModelElement modelElement : freeFormModel.getModelElements()) {
                if (modelElement != null) {
                    ExerciseModel model = modelElement.getModel();
                    int indentLevel = modelElement.getIndentLevel();

                    Exercise exer = typeFactory.getExerciseFromModelObject(model);
                    if (exer != null) {
                        Node viewNode = exer.getFFViewNode();
                        exerciseList.add(exer);
                        ViewElement viewElement = new ViewElement(viewNode, indentLevel);
                        viewElements.add(viewElement);
                    }
                }
            }
        }
    }

    private void indentAction() {
        if (isTornDown || mainView == null || exerciseList == null || freeFormView == null || freeFormView.getViewElements() == null) return;
        int index = exerciseList.indexOf(activeExercise);
        if (index < 0 || index >= freeFormView.getViewElements().size()) return;
        ViewElement viewElement = freeFormView.getViewElements().get(index);
        if (viewElement != null) {
            viewElement.setIndentLevel(viewElement.getIndentLevel() + 1);
            freeFormView.updateContentFromViewElements();
            exerciseModified = true;
        }
    }

    private void outdentAction() {
        if (isTornDown || mainView == null || exerciseList == null || freeFormView == null || freeFormView.getViewElements() == null) return;
        int index = exerciseList.indexOf(activeExercise);
        if (index < 0 || index >= freeFormView.getViewElements().size()) return;
        ViewElement viewElement = freeFormView.getViewElements().get(index);
        if (viewElement != null && viewElement.getIndentLevel() > 0) {
            viewElement.setIndentLevel(viewElement.getIndentLevel() - 1);
            freeFormView.updateContentFromViewElements();
            exerciseModified = true;
        }
    }

    private void setActiveExercise(int index) {
        if (isTornDown || mainView == null || exerciseList == null || index < 0 || index >= exerciseList.size() || freeFormView == null) return;
        Exercise exer = exerciseList.get(index);
        if (exer == null) return;
        activeExercise = exer;
        if (exer.getExerciseView() != null) {
            ExerciseView exerView = (ExerciseView) exer.getExerciseView();
            setExerciseControl(exerView.getExerciseControl());
        }

        if (exer.getExerciseModel() != null) {
            ExerciseType type = ((ExerciseModel)exer.getExerciseModel()).getExerciseType();
            if (type == ExerciseType.SIMPLE_EDIT || type == ExerciseType.HORIZONTAL_TREE || type == ExerciseType.VERTICAL_TREE) {
                if (freeFormView.getIndentButton() != null) freeFormView.getIndentButton().setDisable(true);
                if (freeFormView.getOutdentButton() != null) freeFormView.getOutdentButton().setDisable(true);
            } else {
                if (freeFormView.getIndentButton() != null) freeFormView.getIndentButton().setDisable(false);
                if (freeFormView.getOutdentButton() != null) freeFormView.getOutdentButton().setDisable(false);
            }
        }
        if (freeFormView.getRemoveButton() != null) freeFormView.getRemoveButton().setDisable(false);

        if (exer instanceof HorizontalTreeExercise) {
            ((HorizontalTreeExercise) exer).updateUndoRedoButtons();
        }

        freeFormView.setBorders(index);
        Platform.runLater(() -> {
            if (isTornDown || mainView == null || freeFormView == null || exerciseList == null || index >= exerciseList.size() || exerciseList.get(index) == null) return;
            freeFormView.setContentSpinners(index, exerciseList.get(index).getFFHeightSpinner(), exerciseList.get(index).getFFWidthSpinner());
        });
    }

    private void removeAction()  {
        if (isTornDown || mainView == null) return;
        freeFormModel = getFreeFormModelFromView();
        if (freeFormModel == null || exerciseList == null || freeFormView == null) return;
        List<ModelElement> modelElements = freeFormModel.getModelElements();
        if (modelElements != null && modelElements.size() > 1) {
            int index = exerciseList.indexOf(activeExercise);
            if (index >= 0 && index < modelElements.size()) {
                lastRemovedElement = modelElements.get(index);
                modelElements.remove(index);
                // SCORCHED EARTH RULE 9 implicitly covered because setElementsFromModel() isolates cleanup()
                setElementsFromModel();
                freeFormView.updateContentFromViewElements();

                if (freeFormView.getRestoreButton() != null) freeFormView.getRestoreButton().setDisable(false);
                exerciseModified = true;
                if (index > 0) setActiveExercise(index - 1);
                else setActiveExercise(index);
            }
        } else {
            EditorAlerts.fleetingRedPopup("Exercise must contain at least one element.");
        }
    }

    private void restoreAction() {
        if (isTornDown || mainView == null || lastRemovedElement == null) return;
        freeFormModel = getFreeFormModelFromView();
        if (freeFormModel == null || exerciseList == null || freeFormView == null) return;
        List<ModelElement> modelElements = freeFormModel.getModelElements();
        int index = exerciseList.indexOf(activeExercise);
        if (modelElements != null && index >= -1) {
            modelElements.add(++index, lastRemovedElement);
            setElementsFromModel();
            freeFormView.updateContentFromViewElements();

            if (freeFormView.getRestoreButton() != null) freeFormView.getRestoreButton().setDisable(true);
            setActiveExercise(index);
            exerciseModified = true;
        }
    }

    private void moveUpAction() {
        if (isTornDown || mainView == null) return;
        freeFormModel = getFreeFormModelFromView();
        if (freeFormModel == null || exerciseList == null || freeFormView == null) return;
        List<ModelElement> modelElements = freeFormModel.getModelElements();
        int index = exerciseList.indexOf(activeExercise);
        if (modelElements != null && index > 0 && index < modelElements.size()) {
            Collections.swap(modelElements, index, index - 1);
            setElementsFromModel();
            freeFormView.updateContentFromViewElements();
            exerciseModified = true;
            setActiveExercise(--index);
        } else {
            EditorAlerts.fleetingRedPopup("Cannot move top item up.");
        }
    }

    private void moveDownAction() {
        if (isTornDown || mainView == null) return;
        freeFormModel = getFreeFormModelFromView();
        if (freeFormModel == null || exerciseList == null || freeFormView == null) return;
        List<ModelElement> modelElements = freeFormModel.getModelElements();
        int index = exerciseList.indexOf(activeExercise);
        if (modelElements != null && index >= 0 && index + 1 < modelElements.size()) {
            Collections.swap(modelElements, index, index + 1);
            setElementsFromModel();
            freeFormView.updateContentFromViewElements();
            exerciseModified = true;
            setActiveExercise(++index);
        } else {
            EditorAlerts.fleetingRedPopup("Cannot move bottom item down.");
        }
    }

    private void insertElement(ExerciseModel newMod) {
        int index = exerciseList.indexOf(activeExercise);
        if (index < 0) return;
        ModelElement element = new ModelElement(newMod, 0);
        freeFormModel.getModelElements().add(++index, element);
        setElementsFromModel();
        freeFormView.updateContentFromViewElements();
        setActiveExercise(index);
        if (freeFormView.getRemoveButton() != null) freeFormView.getRemoveButton().setDisable(false);
        exerciseModified = true;

        if (freeFormView.getViewElements() != null && index < freeFormView.getViewElements().size()) {
            ViewElement ve = freeFormView.getViewElements().get(index);
            if (ve != null) scrollToNode(ve.getNode());
        }
    }

    private void addEditAction() {
        if (isTornDown || mainView == null || exerciseList == null || freeFormView == null) return;
        freeFormModel = getFreeFormModelFromView();
        if (freeFormModel != null && freeFormModel.getModelElements() != null) {
            SimpleEditModel newMod = new SimpleEditModel("", "Simple Edit");
            insertElement(newMod);
        }
    }

    private void addTTableAction() {
        if (isTornDown || mainView == null || exerciseList == null || freeFormView == null) return;
        freeFormModel = getFreeFormModelFromView();
        if (freeFormModel != null && freeFormModel.getModelElements() != null) {
            TruthTableGenModel newMod = new TruthTableGenModel();
            newMod.setConclusionDivider(true);
            List<String> unaryList = new ArrayList<>();
            unaryList.add("\u223c");
            newMod.setUnaryOperatorList(unaryList);
            List<String> binaryList = new ArrayList<>();
            binaryList.addAll(Arrays.asList("\u2192", "\u2194", "\u2227", "\u2228"));
            newMod.setBinaryOperatorList(binaryList);

            insertElement(newMod);
        }
    }

    private void addHTreeAction() {
        if (isTornDown || mainView == null || exerciseList == null || freeFormView == null) return;
        freeFormModel = getFreeFormModelFromView();
        if (freeFormModel != null && freeFormModel.getModelElements() != null) {
            HorizontalTreeModel newMod = new HorizontalTreeModel();

            insertElement(newMod);
        }
    }

    private void addVTreeBaseItalAction() {
        if (isTornDown || mainView == null || exerciseList == null || freeFormView == null) return;
        freeFormModel = getFreeFormModelFromView();
        if (freeFormModel != null && freeFormModel.getModelElements() != null) {
            VerticalTreeModel newMod = new VerticalTreeModel();
            newMod.getDragIconList().addAll(Arrays.asList(DragIconType.tree_field, DragIconType.bracket, DragIconType.dashed_line, DragIconType.map_field));
            newMod.getObjectControlList().addAll(Arrays.asList(ObjectControlType.FORMULA_BOX, ObjectControlType.CIRCLE, ObjectControlType.STAR,
                    ObjectControlType.ANNOTATION, ObjectControlType.UNDERLINE, ObjectControlType.MAPPING));
            newMod.setDefaultKeyboardType(BASE);
            newMod.setDefaultMapKeyboardType(BASE);

            insertElement(newMod);
        }
    }

    private void addVTreeItalSansAction() {
        if (isTornDown || mainView == null || exerciseList == null || freeFormView == null) return;
        freeFormModel = getFreeFormModelFromView();
        if (freeFormModel != null && freeFormModel.getModelElements() != null) {
            VerticalTreeModel newMod = new VerticalTreeModel();
            newMod.getDragIconList().addAll(Arrays.asList(DragIconType.tree_field, DragIconType.bracket, DragIconType.dashed_line, DragIconType.map_field));
            newMod.getObjectControlList().addAll(Arrays.asList(ObjectControlType.FORMULA_BOX, ObjectControlType.CIRCLE, ObjectControlType.STAR,
                    ObjectControlType.ANNOTATION, ObjectControlType.UNDERLINE, ObjectControlType.MAPPING));
            newMod.setDefaultKeyboardType(ITALIC_AND_SANS);
            newMod.setDefaultMapKeyboardType(BASE);

            insertElement(newMod);
        }
    }

    private void addVTreeScriptItalAction() {
        if (isTornDown || mainView == null || exerciseList == null || freeFormView == null) return;
        freeFormModel = getFreeFormModelFromView();
        if (freeFormModel != null && freeFormModel.getModelElements() != null) {
            VerticalTreeModel newMod = new VerticalTreeModel();
            newMod.getDragIconList().addAll(Arrays.asList(DragIconType.tree_field, DragIconType.bracket, DragIconType.dashed_line, DragIconType.map_field));
            newMod.getObjectControlList().addAll(Arrays.asList(ObjectControlType.FORMULA_BOX, ObjectControlType.CIRCLE, ObjectControlType.STAR,
                    ObjectControlType.ANNOTATION, ObjectControlType.UNDERLINE, ObjectControlType.MAPPING));
            newMod.setDefaultKeyboardType(SCRIPT_AND_ITALIC);
            newMod.setDefaultMapKeyboardType(BASE);

            insertElement(newMod);
        }
    }

    private void addNDrvtnItalSansAction() {
        if (isTornDown || mainView == null || exerciseList == null || freeFormView == null) return;
        freeFormModel = getFreeFormModelFromView();
        if (freeFormModel != null && freeFormModel.getModelElements() != null) {
            List<ModelLine> modelLines = new ArrayList<>();
            modelLines.addAll(Arrays.asList(
                    new ModelLine(1, new Document(), "", LineType.MAIN_CONTENT_LINE),
                    new ModelLine(1, new Document(), "", LineType.MAIN_CONTENT_LINE),
                    new ModelLine(1, new Document(), "", LineType.MAIN_CONTENT_LINE)
            ));

            DerivationModel newMod = new DerivationModel("", false, 80, .64, true, true, ITALIC_AND_SANS,
                    new Document(), new Document(), modelLines);

            insertElement(newMod);
        }
    }

    private void addNDrvtnScriptItalAction() {
        if (isTornDown || mainView == null || exerciseList == null || freeFormView == null) return;
        freeFormModel = getFreeFormModelFromView();
        if (freeFormModel != null && freeFormModel.getModelElements() != null) {
            List<ModelLine> modelLines = new ArrayList<>();
            modelLines.addAll(Arrays.asList(
                    new ModelLine(1, new Document(), "", LineType.MAIN_CONTENT_LINE),
                    new ModelLine(1, new Document(), "", LineType.MAIN_CONTENT_LINE),
                    new ModelLine(1, new Document(), "", LineType.MAIN_CONTENT_LINE)
            ));

            DerivationModel newMod = new DerivationModel("", false, 80, .64, true, true, SCRIPT_AND_ITALIC,
                    new Document(), new Document(), modelLines);

            insertElement(newMod);
        }
    }

    private void addNDrvtnScriptSansAction() {
        if (isTornDown || mainView == null || exerciseList == null || freeFormView == null) return;
        freeFormModel = getFreeFormModelFromView();
        if (freeFormModel != null && freeFormModel.getModelElements() != null) {
            List<ModelLine> modelLines = new ArrayList<>();
            modelLines.addAll(Arrays.asList(
                    new ModelLine(1, new Document(), "", LineType.MAIN_CONTENT_LINE),
                    new ModelLine(1, new Document(), "", LineType.MAIN_CONTENT_LINE),
                    new ModelLine(1, new Document(), "", LineType.MAIN_CONTENT_LINE)
            ));

            DerivationModel newMod = new DerivationModel("", false, 80, .64, true, true, SCRIPT_AND_SANS,
                    new Document(), new Document(), modelLines);

            insertElement(newMod);
        }
    }

    private void addNDrvtnItalBBAction() {
        if (isTornDown || mainView == null || exerciseList == null || freeFormView == null) return;
        freeFormModel = getFreeFormModelFromView();
        if (freeFormModel != null && freeFormModel.getModelElements() != null) {
            List<ModelLine> modelLines = new ArrayList<>();
            modelLines.addAll(Arrays.asList(
                    new ModelLine(1, new Document(), "", LineType.MAIN_CONTENT_LINE),
                    new ModelLine(1, new Document(), "", LineType.MAIN_CONTENT_LINE),
                    new ModelLine(1, new Document(), "", LineType.MAIN_CONTENT_LINE)
            ));

            DerivationModel newMod = new DerivationModel("", false, 80, .64, true, true, ITALIC_AND_BLACKBOARD,
                    new Document(), new Document(), modelLines);

            insertElement(newMod);
        }
    }

    private void addADrvtnItalSansAction() {
        if (isTornDown || mainView == null || exerciseList == null || freeFormView == null) return;
        freeFormModel = getFreeFormModelFromView();
        if (freeFormModel != null && freeFormModel.getModelElements() != null) {
            List<ModelLine> modelLines = new ArrayList<>();
            modelLines.addAll(Arrays.asList(
                    new ModelLine(1, new Document(), "", LineType.MAIN_CONTENT_LINE),
                    new ModelLine(1, new Document(), "", LineType.MAIN_CONTENT_LINE),
                    new ModelLine(1, new Document(), "", LineType.MAIN_CONTENT_LINE)
            ));

            DerivationModel newMod = new DerivationModel("", false, 80, .64, false, false, ITALIC_AND_SANS,
                    new Document(), new Document(), modelLines);

            insertElement(newMod);
        }
    }

    private void addADrvtnScriptItalicAction() {
        if (isTornDown || mainView == null || exerciseList == null || freeFormView == null) return;
        freeFormModel = getFreeFormModelFromView();
        if (freeFormModel != null && freeFormModel.getModelElements() != null) {
            List<ModelLine> modelLines = new ArrayList<>();
            modelLines.addAll(Arrays.asList(
                    new ModelLine(1, new Document(), "", LineType.MAIN_CONTENT_LINE),
                    new ModelLine(1, new Document(), "", LineType.MAIN_CONTENT_LINE),
                    new ModelLine(1, new Document(), "", LineType.MAIN_CONTENT_LINE)
            ));

            DerivationModel newMod = new DerivationModel("", false, 80, .64, false, false, SCRIPT_AND_ITALIC,
                    new Document(), new Document(), modelLines);

            insertElement(newMod);
        }
    }

    private void addADrvtnScriptSansAction() {
        if (isTornDown || mainView == null || exerciseList == null || freeFormView == null) return;
        freeFormModel = getFreeFormModelFromView();
        if (freeFormModel != null && freeFormModel.getModelElements() != null) {
            List<ModelLine> modelLines = new ArrayList<>();
            modelLines.addAll(Arrays.asList(
                    new ModelLine(1, new Document(), "", LineType.MAIN_CONTENT_LINE),
                    new ModelLine(1, new Document(), "", LineType.MAIN_CONTENT_LINE),
                    new ModelLine(1, new Document(), "", LineType.MAIN_CONTENT_LINE)
            ));

            DerivationModel newMod = new DerivationModel("", false, 80, .64, false, false, SCRIPT_AND_SANS,
                    new Document(), new Document(), modelLines);

            insertElement(newMod);
        }
    }

    private void addADrvtnItalBBAction() {
        if (isTornDown || mainView == null || exerciseList == null || freeFormView == null) return;
        freeFormModel = getFreeFormModelFromView();
        if (freeFormModel != null && freeFormModel.getModelElements() != null) {
            List<ModelLine> modelLines = new ArrayList<>();
            modelLines.addAll(Arrays.asList(
                    new ModelLine(1, new Document(), "", LineType.MAIN_CONTENT_LINE),
                    new ModelLine(1, new Document(), "", LineType.MAIN_CONTENT_LINE),
                    new ModelLine(1, new Document(), "", LineType.MAIN_CONTENT_LINE)
            ));

            DerivationModel newMod = new DerivationModel("", false, 80, .64, false, false, ITALIC_AND_BLACKBOARD,
                    new Document(), new Document(), modelLines);

            insertElement(newMod);
        }
    }

    public Exercise getActiveExercise() { return activeExercise;}

    private void scrollToNode(Node node) {
        if (isTornDown || mainView == null || node == null) return;
        Platform.runLater(() -> {
            if (isTornDown || mainView == null || node.getScene() == null) return;
            Parent root = node.getScene().getRoot();
            if (root != null) {
                root.applyCss();
                root.layout();
            }
            mainView.scrollToNode(node);
        });
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
            if (exerciseList != null) {
                for (Exercise exercise : exerciseList) {
                    if (exercise != null) exercise.setExerciseModified(false);
                }
            }
        }
    }

    /**
     * List of nodes to be sent to printer for this exercise
     * @return the node list
     */
    @Override
    public List<Node> getPrintNodes(ExerciseModel originalModel) {
        if (freeFormModel == null || freeFormView == null) return new ArrayList<>();
        List<Node> nodeList = new ArrayList<>();
        FreeFormModel printModel = freeFormModel;
        FreeFormExercise printExercise = this;
        double nodeWidth = PrintUtilities.getPageWidth() / mainWindow.getBaseScale();

        //header node
        Label exerciseName = new Label(printModel.getExerciseName());
        exerciseName.setStyle("-fx-font-weight: bold;");

        Region spacer = new Region();
        Label pointsLabel = new Label("");
        if (printModel.getPointsPossible() > 0)
            pointsLabel.setText("   " + Integer.toString(printModel.getPointsEarned()) + "/" + Integer.toString(printModel.getPointsPossible()));

        HBox hbox = new HBox(exerciseName, spacer, pointsLabel);
        hbox.setAlignment(Pos.CENTER_LEFT);
        hbox.setHgrow(spacer, Priority.ALWAYS);
        hbox.setMinWidth(nodeWidth);
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
        statementRTA.prefWidthProperty().unbind();
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

        //content nodes
        if (exerciseList != null) {
            for (int i = 0; i < exerciseList.size(); i++) {
                Exercise exerElement = exerciseList.get(i);
                if (exerElement != null) {
                    FreeFormModel originalFFmodel = (FreeFormModel) originalModel;
                    if (originalFFmodel != null && originalFFmodel.getModelElements() != null && originalFFmodel.getModelElements().size() > i) {
                        ModelElement originalModelElement = originalFFmodel.getModelElements().get(i);
                        if (originalModelElement != null) {
                            ExerciseModel originalItemModel = originalModelElement.getModel();
                            Region nodeSpacer = new Region();
                            nodeSpacer.setPrefWidth(20.0 * originalModelElement.getIndentLevel());
                            HBox elementBox = new HBox(nodeSpacer, exerElement.getFFPrintNode(originalItemModel));
                            elementBox.setStyle("-fx-border-width: 0; -fx-border-color: transparent;");

                            nodeList.add(elementBox);
                        }
                    }
                }
            }
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
        commentRTA.prefWidthProperty().unbind();
        commentRTA.minWidthProperty().unbind();
        commentRTA.maxWidthProperty().unbind();
        commentRTA.setPrefHeight(Math.max(40, printModel.getCommentTextHeight() + RTA_PRINT_BMP));
        commentRTA.setMaxWidth(nodeWidth);
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
        if (freeFormView == null || freeFormView.getExerciseComment() == null) return null;
        RichTextArea commentRTA = freeFormView.getExerciseComment().getEditor();
        commentRTA.getActionFactory().saveNow().execute(new ActionEvent());
        Document commentDocument = commentRTA.getDocument();


        int pointsEarned = -1;
        TextField pointsField = freeFormView.getPointsEarnedTextField();
        if ((pointsField.isFocused() || freeFormModel.getPointsEarned() >= 0) && pointsField.getText().isEmpty()) {
            pointsField.setText("0");
        }
        if (!pointsField.getText().equals("")) pointsEarned = Integer.parseInt(pointsField.getText());


        FreeFormModel originalModel = (FreeFormModel) (freeFormModel.getOriginalModel());
        if (originalModel != null) {
            originalModel.setExerciseComment(commentDocument);
            double commentHeight = commentRTA.getFullHeight();
            originalModel.setCommentTextHeight(commentHeight > 0 ? commentHeight : freeFormModel.getCommentTextHeight());
            originalModel.setPointsEarned(pointsEarned);
        }
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
        if (exerciseList != null) {
            for (Exercise exercise : exerciseList) {
                if (exercise != null && exercise.isExerciseModified()) {
                    exerciseModified = true;
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
    public void setExerciseModified(boolean modified) {
        if (modified) exerciseModified = true;
        else {
            exerciseModified = false;
            if (exerciseList != null) {
                for (Exercise exercise : exerciseList) {
                    if (exercise != null) exercise.setExerciseModified(false);
                }
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
        if (freeFormModel == null || freeFormView == null) return null;
        FreeFormModel model = new FreeFormModel(freeFormModel.getExerciseName(), freeFormModel.getElementTypes());
        model.setOriginalModel(freeFormModel.getOriginalModel());
        model.setStarted(freeFormModel.isStarted() || exerciseModified);
        model.setExerciseStatement(freeFormModel.getExerciseStatement());
        model.setAuxExerName(freeFormModel.getAuxExerName());
        model.setCheckSetup(freeFormModel.getCheckSetup());

        if (freeFormView.getExerciseStatement() != null && freeFormView.getExerciseStatement().getEditor() != null) {
            model.setStatementPrefHeight(freeFormView.getExerciseStatement().getEditor().getPrefHeight());
        }
        model.setStatementTextHeight(freeFormModel.getStatementTextHeight());

        RichTextArea commentRTA = freeFormView.getExerciseComment().getEditor();
        commentRTA.getActionFactory().saveNow().execute(new ActionEvent());
        model.setExerciseComment(commentRTA.getDocument());
        model.setCommentPrefHeight(freeFormView.getCommentPrefHeight());
        double commentTextHeight = commentRTA.getFullHeight();
        model.setCommentTextHeight(commentTextHeight > 0 ? commentTextHeight : freeFormModel.getCommentTextHeight());

        model.setPointsPossible(freeFormModel.getPointsPossible());
        TextField pointsField = freeFormView.getPointsEarnedTextField();
        if ((pointsField.isFocused() || freeFormModel.getPointsEarned() >= 0) && pointsField.getText().isEmpty()) {
            pointsField.setText("0");
        }
        if (!pointsField.getText().equals("")) model.setPointsEarned(Integer.parseInt(pointsField.getText()));
        else model.setPointsEarned(-1);

        List<ModelElement> modelElements = new ArrayList<>();
        if (exerciseList != null && freeFormView.getViewElements() != null) {
            for (int i = 0; i < exerciseList.size(); i++) {
                if (i >= freeFormView.getViewElements().size()) continue;
                Exercise exer = exerciseList.get(i);
                if (exer != null && freeFormView.getViewElements().get(i) != null) {
                    ExerciseModel exerModel = exer.getExerciseModelFromView();
                    int indent = freeFormView.getViewElements().get(i).getIndentLevel();
                    ModelElement element = new ModelElement(exerModel, indent);
                    modelElements.add(element);
                }
            }
        }
        model.setModelElements(modelElements);

        return model;
    }

    public List<Exercise> getExerciseList() {return exerciseList;}

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
        // KILL SWITCH: Engaged!
        isTornDown = true;

        // SCORCHED EARTH RULE 2: Safe Global State & Null-Poisoning Prevention
        try {
            if (mainView != null) {
                mainView.textFieldInFocus(); // Remove toolbars for RTAs safely
            }
        } catch (Exception e) {}

        // SCORCHED EARTH RULE 3: Isolated try-catch blocks for removing listeners
        try { if (thumbPane != null && thumbPaneClickHandler != null) thumbPane.removeEventHandler(MouseEvent.MOUSE_CLICKED, thumbPaneClickHandler); } catch (Exception e) {}
        try { if (thumbPane != null) thumbPane.setOnMouseClicked(null); } catch (Exception e) {}

        try { if (freeFormView != null && freeFormView.getPointsEarnedTextField() != null && pointsEarnedListener != null) freeFormView.getPointsEarnedTextField().textProperty().removeListener(pointsEarnedListener); } catch (Exception e) {}

        try { if (freeFormView != null && freeFormView.getExerciseStatement() != null && freeFormView.getExerciseStatement().getEditor() != null && statementFocusListener != null) freeFormView.getExerciseStatement().getEditor().focusedProperty().removeListener(statementFocusListener); } catch (Exception e) {}

        try {
            if (freeFormView != null && freeFormView.getExerciseComment() != null && freeFormView.getExerciseComment().getEditor() != null) {
                if (commentFullHeightListener != null) freeFormView.getExerciseComment().getEditor().fullHeightProperty().removeListener(commentFullHeightListener);
                if (commentModifiedListener != null) freeFormView.getExerciseComment().getEditor().modifiedProperty().removeListener(commentModifiedListener);
                if (commentFocusListener != null) freeFormView.getExerciseComment().getEditor().focusedProperty().removeListener(commentFocusListener);
            }
        } catch (Exception e) {}

        try { if (freeFormView != null && freeFormView.getRemoveButton() != null && removeButtonHandler != null) { freeFormView.getRemoveButton().removeEventHandler(ActionEvent.ACTION, removeButtonHandler); freeFormView.getRemoveButton().setOnAction(null); } } catch (Exception e) {}
        try { if (freeFormView != null && freeFormView.getRestoreButton() != null && restoreButtonHandler != null) { freeFormView.getRestoreButton().removeEventHandler(ActionEvent.ACTION, restoreButtonHandler); freeFormView.getRestoreButton().setOnAction(null); } } catch (Exception e) {}
        try { if (freeFormView != null && freeFormView.getIndentButton() != null && indentButtonHandler != null) { freeFormView.getIndentButton().removeEventHandler(ActionEvent.ACTION, indentButtonHandler); freeFormView.getIndentButton().setOnAction(null); } } catch (Exception e) {}
        try { if (freeFormView != null && freeFormView.getOutdentButton() != null && outdentButtonHandler != null) { freeFormView.getOutdentButton().removeEventHandler(ActionEvent.ACTION, outdentButtonHandler); freeFormView.getOutdentButton().setOnAction(null); } } catch (Exception e) {}
        try { if (freeFormView != null && freeFormView.getMoveUpButton() != null && moveUpButtonHandler != null) { freeFormView.getMoveUpButton().removeEventHandler(ActionEvent.ACTION, moveUpButtonHandler); freeFormView.getMoveUpButton().setOnAction(null); } } catch (Exception e) {}
        try { if (freeFormView != null && freeFormView.getMoveDownButton() != null && moveDownButtonHandler != null) { freeFormView.getMoveDownButton().removeEventHandler(ActionEvent.ACTION, moveDownButtonHandler); freeFormView.getMoveDownButton().setOnAction(null); } } catch (Exception e) {}
        try { if (freeFormView != null && freeFormView.getAddEditButton() != null && addEditButtonHandler != null) { freeFormView.getAddEditButton().removeEventHandler(ActionEvent.ACTION, addEditButtonHandler); freeFormView.getAddEditButton().setOnAction(null); } } catch (Exception e) {}
        try { if (freeFormView != null && freeFormView.getAddHTreeButton() != null && addHTreeButtonHandler != null) { freeFormView.getAddHTreeButton().removeEventHandler(ActionEvent.ACTION, addHTreeButtonHandler); freeFormView.getAddHTreeButton().setOnAction(null); } } catch (Exception e) {}
        try { if (freeFormView != null && freeFormView.getAddTTableButton() != null && addTTableButtonHandler != null) { freeFormView.getAddTTableButton().removeEventHandler(ActionEvent.ACTION, addTTableButtonHandler); freeFormView.getAddTTableButton().setOnAction(null); } } catch (Exception e) {}
        try { if (freeFormView != null && freeFormView.getAddVTreeBaseItalButton() != null && addVTreeBaseItalButtonHandler != null) { freeFormView.getAddVTreeBaseItalButton().removeEventHandler(ActionEvent.ACTION, addVTreeBaseItalButtonHandler); freeFormView.getAddVTreeBaseItalButton().setOnAction(null); } } catch (Exception e) {}
        try { if (freeFormView != null && freeFormView.getAddVTreeItalSansButton() != null && addVTreeItalSansButtonHandler != null) { freeFormView.getAddVTreeItalSansButton().removeEventHandler(ActionEvent.ACTION, addVTreeItalSansButtonHandler); freeFormView.getAddVTreeItalSansButton().setOnAction(null); } } catch (Exception e) {}
        try { if (freeFormView != null && freeFormView.getAddVTreeScriptItalButton() != null && addVTreeScriptItalButtonHandler != null) { freeFormView.getAddVTreeScriptItalButton().removeEventHandler(ActionEvent.ACTION, addVTreeScriptItalButtonHandler); freeFormView.getAddVTreeScriptItalButton().setOnAction(null); } } catch (Exception e) {}
        try { if (freeFormView != null && freeFormView.getAddNDrvtnItalSansButton() != null && addNDrvtnItalSansButtonHandler != null) { freeFormView.getAddNDrvtnItalSansButton().removeEventHandler(ActionEvent.ACTION, addNDrvtnItalSansButtonHandler); freeFormView.getAddNDrvtnItalSansButton().setOnAction(null); } } catch (Exception e) {}
        try { if (freeFormView != null && freeFormView.getAddNDrvtnScriptItalButton() != null && addNDrvtnScriptItalButtonHandler != null) { freeFormView.getAddNDrvtnScriptItalButton().removeEventHandler(ActionEvent.ACTION, addNDrvtnScriptItalButtonHandler); freeFormView.getAddNDrvtnScriptItalButton().setOnAction(null); } } catch (Exception e) {}
        try { if (freeFormView != null && freeFormView.getAddNDrvtnScriptSansButton() != null && addNDrvtnScriptSansButtonHandler != null) { freeFormView.getAddNDrvtnScriptSansButton().removeEventHandler(ActionEvent.ACTION, addNDrvtnScriptSansButtonHandler); freeFormView.getAddNDrvtnScriptSansButton().setOnAction(null); } } catch (Exception e) {}
        try { if (freeFormView != null && freeFormView.getAddNDrvtnItalBBButton() != null && addNDrvtnItalBBButtonHandler != null) { freeFormView.getAddNDrvtnItalBBButton().removeEventHandler(ActionEvent.ACTION, addNDrvtnItalBBButtonHandler); freeFormView.getAddNDrvtnItalBBButton().setOnAction(null); } } catch (Exception e) {}
        try { if (freeFormView != null && freeFormView.getAddADrvtnItalSansButton() != null && addADrvtnItalSansButtonHandler != null) { freeFormView.getAddADrvtnItalSansButton().removeEventHandler(ActionEvent.ACTION, addADrvtnItalSansButtonHandler); freeFormView.getAddADrvtnItalSansButton().setOnAction(null); } } catch (Exception e) {}
        try { if (freeFormView != null && freeFormView.getAddADrvtnScriptItalButton() != null && addADrvtnScriptItalButtonHandler != null) { freeFormView.getAddADrvtnScriptItalButton().removeEventHandler(ActionEvent.ACTION, addADrvtnScriptItalButtonHandler); freeFormView.getAddADrvtnScriptItalButton().setOnAction(null); } } catch (Exception e) {}
        try { if (freeFormView != null && freeFormView.getAddADrvtnScriptSansButton() != null && addADrvtnScriptSansButtonHandler != null) { freeFormView.getAddADrvtnScriptSansButton().removeEventHandler(ActionEvent.ACTION, addADrvtnScriptSansButtonHandler); freeFormView.getAddADrvtnScriptSansButton().setOnAction(null); } } catch (Exception e) {}
        try { if (freeFormView != null && freeFormView.getAddADrvtnItalBBButton() != null && addADrvtnItalBBButtonHandler != null) { freeFormView.getAddADrvtnItalBBButton().removeEventHandler(ActionEvent.ACTION, addADrvtnItalBBButtonHandler); freeFormView.getAddADrvtnItalBBButton().setOnAction(null); } } catch (Exception e) {}

        // ---> SCORCHED EARTH RULE 4: THE JAVAFX SKIN HAMMER (With Synchronous Unbinding Only) <---
        try {
            final RichTextArea stEditor = (freeFormView != null && freeFormView.getExerciseStatement() != null) ? freeFormView.getExerciseStatement().getEditor() : null;
            final RichTextArea commEditor = (freeFormView != null && freeFormView.getExerciseComment() != null) ? freeFormView.getExerciseComment().getEditor() : null;

            if (stEditor != null) {
                stEditor.prefHeightProperty().unbind();
                stEditor.prefWidthProperty().unbind();
                stEditor.minWidthProperty().unbind();
                stEditor.maxWidthProperty().unbind();
            }
            if (commEditor != null) {
                commEditor.prefHeightProperty().unbind();
                commEditor.prefWidthProperty().unbind();
                commEditor.minWidthProperty().unbind();
                commEditor.maxWidthProperty().unbind();
            }
        } catch (Exception e) {}

        // ---> 4. DISMANTLE THE UI TREE <---
        try {
            if (thumbPane != null) {
                thumbPane.getChildren().clear();
                severFromParent(thumbPane);
            }
        } catch (Exception e) {}

        // SCORCHED EARTH RULE 10: NON-DESTRUCTIVE Data Teardown
        try { if (undoRedoList != null) undoRedoList = null; } catch (Exception e) {}


        // 5. Clear OS-level popups
        try {
            if (freeFormView != null && freeFormView.getStaticHelpStage() != null) freeFormView.getStaticHelpStage().close();
            if (tStage != null) tStage.close();
            if (tScene != null) tScene.setRoot(new Pane());
        } catch (Exception e) {}

        // 6. Cascade cleanup
        try {
            if (exerciseList != null) {
                List<Exercise> copyExers = new ArrayList<>(exerciseList);
                for (Exercise exer : copyExers) {
                    try { if (exer != null) exer.cleanup(); } catch (Exception e) {}
                }
                exerciseList = null; // Rule 10
            }
        } catch (Exception e) {}

        try { if (auxExerciseA != null) auxExerciseA.cleanup(); } catch (Exception e) {}
        try { if (auxExerciseB != null) auxExerciseB.cleanup(); } catch (Exception e) {}

        try { if (ffCheck != null) ffCheck.cleanup(); } catch (Exception e) {}
        // Wait to cleanup the view until end so we don't nullify objects we need first
        try { if (freeFormView != null) freeFormView.cleanup(); } catch (Exception e) {}


        // Wait to cleanup the view until end so we don't nullify objects we need first
        try { if (freeFormView != null) freeFormView.cleanup(); } catch (Exception e) {}
        // If FFCheck eventually gets a cleanup(), call it here:
        try { if (ffCheck != null) ffCheck.getClass().getMethod("cleanup").invoke(ffCheck); } catch (Exception e) {}

        // 7. Final nullifications
        try { this.mainWindow = null; } catch (Exception e) {}
        try { this.mainView = null; } catch (Exception e) {}
        try { this.freeFormModel = null; } catch (Exception e) {}
        try { this.freeFormView = null; } catch (Exception e) {}
        try { this.activeExercise = null; } catch (Exception e) {}
        try { this.exerciseClickFilter = null; } catch (Exception e) {}
        try { this.lastRemovedElement = null; } catch (Exception e) {}
        try { this.ffCheck = null; } catch (Exception e) {}
        try { this.auxExerciseA = null; } catch (Exception e) {}
        try { this.auxExerciseB = null; } catch (Exception e) {}
        try { this.tStage = null; } catch (Exception e) {}
        try { this.thumbPane = null; } catch (Exception e) {}
        try { this.tScene = null; } catch (Exception e) {}

        try { this.thumbPaneClickHandler = null; } catch (Exception e) {}
        try { this.pointsEarnedListener = null; } catch (Exception e) {}
        try { this.statementFocusListener = null; } catch (Exception e) {}
        try { this.commentFullHeightListener = null; } catch (Exception e) {}
        try { this.commentModifiedListener = null; } catch (Exception e) {}
        try { this.commentFocusListener = null; } catch (Exception e) {}
        try { this.removeButtonHandler = null; } catch (Exception e) {}
        try { this.restoreButtonHandler = null; } catch (Exception e) {}
        try { this.indentButtonHandler = null; } catch (Exception e) {}
        try { this.outdentButtonHandler = null; } catch (Exception e) {}
        try { this.moveUpButtonHandler = null; } catch (Exception e) {}
        try { this.moveDownButtonHandler = null; } catch (Exception e) {}
        try { this.addEditButtonHandler = null; } catch (Exception e) {}
        try { this.addHTreeButtonHandler = null; } catch (Exception e) {}
        try { this.addTTableButtonHandler = null; } catch (Exception e) {}
        try { this.addVTreeBaseItalButtonHandler = null; } catch (Exception e) {}
        try { this.addVTreeItalSansButtonHandler = null; } catch (Exception e) {}
        try { this.addVTreeScriptItalButtonHandler = null; } catch (Exception e) {}
        try { this.addNDrvtnItalSansButtonHandler = null; } catch (Exception e) {}
        try { this.addNDrvtnScriptItalButtonHandler = null; } catch (Exception e) {}
        try { this.addNDrvtnScriptSansButtonHandler = null; } catch (Exception e) {}
        try { this.addNDrvtnItalBBButtonHandler = null; } catch (Exception e) {}
        try { this.addADrvtnItalSansButtonHandler = null; } catch (Exception e) {}
        try { this.addADrvtnScriptItalButtonHandler = null; } catch (Exception e) {}
        try { this.addADrvtnScriptSansButtonHandler = null; } catch (Exception e) {}
        try { this.addADrvtnItalBBButtonHandler = null; } catch (Exception e) {}
    }

    @Override
    public Node getAuxExerNode() {
        if (freeFormView == null) return new Pane();
        return freeFormView.getContentBox();
    }

    @Override
    public String getAuxExerName() {
        if (freeFormModel == null) return "";
        return freeFormModel.getAuxExerName();
    }

    @Override
    public void setAuxExerA(Exercise exer) {
        auxExerciseA = exer;
    }

    @Override
    public void setAuxExerB(Exercise exer) { auxExerciseB = exer; }

}
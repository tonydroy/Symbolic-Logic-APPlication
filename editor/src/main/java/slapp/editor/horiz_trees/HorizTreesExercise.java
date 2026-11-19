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
import slapp.editor.EditorMain;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import static slapp.editor.EditorMain.*;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.Spinner;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.beans.value.ChangeListener;


import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
    private EventHandler<MouseEvent> exerciseClickFilter;
    private ModelElement lastRemovedElement;

    private HorizTreesCheck htCheck;
    DecoratedRTA interpretationDRTA;
    RichTextArea interpretationEditor;

    private Exercise auxExerciseA;
    private Exercise auxExerciseB;
    Stage tStage;
    Pane thumbPane = new Pane();
    Scene tScene = new Scene(new Pane());

    // SCORCHED EARTH RULE 2: Kill Switch for asynchronous event interception during teardown
    private boolean isTornDown = false;

    // SCORCHED EARTH RULE 3: Eradicate Anonymous Lambdas
    private EventHandler<MouseEvent> thumbPaneClickHandler;
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
    private EventHandler<ActionEvent> removeButtonHandler;
    private EventHandler<ActionEvent> restoreButtonHandler;
    private EventHandler<ActionEvent> moveUpButtonHandler;
    private EventHandler<ActionEvent> moveDownButtonHandler;
    private EventHandler<ActionEvent> addHTreeButtonHandler;

    public HorizTreesExercise(HorizTreesModel model, MainWindow mainWindow) {
        this.mainWindow = mainWindow;
        mainView = mainWindow.getMainView();
        this.horizTreesModel = model;
        if (model.getOriginalModel() == null) {model.setOriginalModel(model);}
        if (model.getModelElements().isEmpty()) {
            HorizontalTreeModel stub = new HorizontalTreeModel();
            model.getModelElements().add(new ModelElement(stub, 0));
        }

        this.horizTreesView = new HorizTreesView(mainView);
        horizTreesView.setRightControlBox();

        htCheck = new HorizTreesCheck(this);
        setInterpretationDRTA();
        setHorizTreesView();
        horizTreesView.initalizeInterpretationRTA();

        Platform.runLater(() -> {
            if (isTornDown || mainView == null) return;
            if (mainWindow != null && mainWindow.getCurrentExercise() != this) return;
            if (auxExerciseA != null) {
                thumbPane = auxImage();
                setActiveExercise(0);
            }
        });

        setInitialTreeWindow();
    }

    @Override
    public void handleMouseEvent(MouseEvent event) {
        if (isTornDown || mainView == null || horizTreesView == null || exerciseList == null || event == null) return;
        try {
            List<ViewElement> viewElements = horizTreesView.getViewElements();
            if (viewElements == null) return;

            for (int i = 0; i < exerciseList.size(); i++) {
                if (i >= viewElements.size() || viewElements.get(i) == null || viewElements.get(i).getNode() == null || viewElements.get(i).getNode().getParent() == null) continue;
                ViewElement viewElement = viewElements.get(i);
                if (inHierarchy(event.getPickResult().getIntersectedNode(), viewElement.getNode().getParent())) {
                    Exercise exer = exerciseList.get(i);
                    if (exer == null) continue;
                    activeExercise = exer;
                    ExerciseView exerView = (ExerciseView) exer.getExerciseView();
                    if (exerView != null) {
                        setExerciseControl(exerView.getExerciseControl());
                    }

                    horizTreesView.setBorders(i);
                    horizTreesView.setContentSpinners(i, exerciseList.get(i).getFFHeightSpinner(), exerciseList.get(i).getFFWidthSpinner());
                    if (exer instanceof HorizontalTreeExercise) {
                        ((HorizontalTreeExercise) exer).updateUndoRedoButtons();
                    }
                }
                else {
                    Exercise exer = exerciseList.get(i);
                    if (exer instanceof HorizontalTreeExercise) {
                        HorizontalTreeView htv = ((HorizontalTreeExercise) exer).getHorizontalTreeView();
                        if (htv != null && htv.getFormulaNodeToggle() != null) {
                            htv.getFormulaNodeToggle().setSelected(false);
                        }
                    }
                }
            }
        } catch (Exception e) {}
    }

    private static boolean inHierarchy(Node node, Node potentialHierarchyElement) {
        if (potentialHierarchyElement == null) return true;
        while (node != null) {
            if (node == potentialHierarchyElement) return true;
            node = node.getParent();
        }
        return false;
    }

    public void setInitialTreeWindow() {
        if (isTornDown || exerciseList == null || exerciseList.isEmpty() || exerciseList.get(0) == null) return;
        try {
            Exercise exer = exerciseList.get(0);
            activeExercise = exer;
            ExerciseView exerView = (ExerciseView) exer.getExerciseView();
            if (exerView == null) return;
            setExerciseControl(exerView.getExerciseControl());

            if (horizTreesView != null) {
                horizTreesView.setBorders(0);
                horizTreesView.setContentSpinners(0, exerciseList.get(0).getFFHeightSpinner(), exerciseList.get(0).getFFWidthSpinner());
            }
            if (exer instanceof HorizontalTreeExercise) {
                ((HorizontalTreeExercise) exer).updateUndoRedoButtons();
            }
        } catch (Exception e) {}
    }

    private void setExerciseControl(Node exerControlNode) {
        if (isTornDown || mainView == null || horizTreesView == null) return;
        try {
            VBox controlBox = new VBox(30, exerControlNode, thumbPane);
            controlBox.setMaxWidth(150);
            controlBox.setAlignment(Pos.CENTER);
            VBox.setMargin(thumbPane, new Insets(0, 0, 0, 30));

            horizTreesView.setExerciseControlNode(controlBox);

            // THE FIX: ONLY overwrite the MainWindow's left control box if THIS is the active exercise!
            // This prevents Auxiliary exercises from hijacking the global UI.
            if (mainWindow != null && mainWindow.getMainView() != null) {
                if (mainWindow.getCurrentExercise() == this) {
                    mainWindow.getMainView().setUpLeftControl(horizTreesView.getExerciseControl());
                }
            }
        } catch (Exception e) {}
    }

    private Pane auxImage() {
        Pane thumbPane = new Pane();
        if (auxExerciseA != null && auxExerciseA.getAuxExerNode() != null)  {
            try {
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

                 //       tStage.initOwner(EditorMain.mainStage);
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
                };
                thumbPane.setOnMouseClicked(thumbPaneClickHandler);
            } catch (Exception e) {}
        }
        return thumbPane;
    }

    public void clearStandingPopups() {
        if (isTornDown) return;
        try {
            if (horizTreesView != null && horizTreesView.getStaticHelpStage() != null) horizTreesView.getStaticHelpStage().close();
            if (tStage != null) tStage.close();
        } catch (Exception e) {}
    }

    private void setInterpretationDRTA() {
        if (isTornDown) return;
        try {
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
                    } else {
                        mainView.undoRTATextHeight();
                    }
                } catch (Exception e) {}
            };
            interpretationEditor.focusedProperty().addListener(interpFocusListener);

            horizTreesView.setInterpretationDRTA(interpretationDRTA);
            horizTreesView.setInterpretationRTA(interpretationEditor);
            horizTreesView.setInterpretationPrefHeight(htCheck.getCheckSetup().getInterpretationPrefHeight());
        } catch (Exception e) {}
    }

    /*
     * Set up the free form view from the model
     */
    private void setHorizTreesView() {
        if (isTornDown || horizTreesView == null || horizTreesModel == null) return;

        horizTreesView.setStatementPrefHeight(horizTreesModel.getStatementPrefHeight());
        horizTreesView.setCommentPrefHeight(horizTreesModel.getCommentPrefHeight());
        horizTreesView.setExplainPrefHeight(horizTreesModel.getExplainPrefHeight());
        horizTreesView.setPointsPossible(horizTreesModel.getPointsPossible());

        if (horizTreesModel.getPointsEarned() >= 0) horizTreesView.getPointsEarnedTextField().setText(Integer.toString(horizTreesModel.getPointsEarned()));
        if (horizTreesView.getPointsEarnedTextField() != null) {
            pointsEarnedListener = (ob, ov, nv) -> {
                if (isTornDown || mainView == null) return;
                exerciseModified = true;
            };
            horizTreesView.getPointsEarnedTextField().textProperty().addListener(pointsEarnedListener);
        }

        //statement
        DecoratedRTA statementDRTA = new DecoratedRTA();
        RichTextArea statementEditor = statementDRTA.getEditor();
        statementEditor.getActionFactory().open(horizTreesModel.getExerciseStatement()).execute(new ActionEvent());

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
        horizTreesView.setExerciseStatement(statementDRTA);

        //comment
        DecoratedRTA commentDRTA = new DecoratedRTA();
        RichTextArea commentEditor = commentDRTA.getEditor();
        commentEditor.getActionFactory().open(horizTreesModel.getExerciseComment()).execute(new ActionEvent());
        commentEditor.getActionFactory().saveNow().execute(new ActionEvent());

        final boolean[] commentPrimed = {false};
        commentFullHeightListener = (ob, ov, nv) -> {
            if (isTornDown || mainView == null || horizTreesModel == null) return;
            if (!commentPrimed[0]) {
                commentPrimed[0] = true;
                return;
            }
            double height = (double) nv;
            mainView.setRTATextHeight(height);
            horizTreesModel.setCommentTextHeight(height);
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
        horizTreesView.setExerciseComment(commentDRTA);

        //explain
        DecoratedRTA explainDRTA = horizTreesView.getExplainDRTA();
        RichTextArea explainEditor = explainDRTA.getEditor();
        explainEditor.getActionFactory().open(horizTreesModel.getExplainDocument()).execute(new ActionEvent());
        explainEditor.getActionFactory().saveNow().execute(new ActionEvent());

        horizTreesView.setExplainPrompt(horizTreesModel.getExplainPrompt());

        final boolean[] explainPrimed = {false};
        explainFullHeightListener = (ob, ov, nv) -> {
            if (isTornDown || mainView == null || horizTreesModel == null) return;
            if (!explainPrimed[0]) {
                explainPrimed[0] = true;
                return;
            }
            double height = (double) nv;
            mainView.setRTATextHeight(height);
            horizTreesModel.setExplainTextHeight(height);
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

        //buttons
        removeButtonHandler = e -> {
            if (isTornDown || mainView == null) return;
            removeAction();
        };
        if (horizTreesView.getRemoveButton() != null) horizTreesView.getRemoveButton().setOnAction(removeButtonHandler);

        restoreButtonHandler = e -> {
            if (isTornDown || mainView == null) return;
            restoreAction();
        };
        if (horizTreesView.getRestoreButton() != null) horizTreesView.getRestoreButton().setOnAction(restoreButtonHandler);

        moveUpButtonHandler = e -> {
            if (isTornDown || mainView == null) return;
            moveUpAction();
        };
        if (horizTreesView.getMoveUpButton() != null) horizTreesView.getMoveUpButton().setOnAction(moveUpButtonHandler);

        moveDownButtonHandler = e -> {
            if (isTornDown || mainView == null) return;
            moveDownAction();
        };
        if (horizTreesView.getMoveDownButton() != null) horizTreesView.getMoveDownButton().setOnAction(moveDownButtonHandler);

        addHTreeButtonHandler = e -> {
            if (isTornDown || mainView == null) return;
            addHTreeAction();
        };
        if (horizTreesView.getAddHTreeButton() != null) horizTreesView.getAddHTreeButton().setOnAction(addHTreeButtonHandler);

        if (horizTreesView.getRestoreButton() != null) horizTreesView.getRestoreButton().setDisable(true);
        if (horizTreesView.getRemoveButton() != null) horizTreesView.getRemoveButton().setDisable(false);

        //cleanup
        horizTreesView.initializeViewDetails();

        if (htCheck != null && htCheck.getCheckSetup() != null) {
            List<Document> targetFormulas = htCheck.getCheckSetup().getTargetFormulas();
            int realTargets = 0;
            for (Document doc : targetFormulas) {
                if (!doc.getText().equals("")) realTargets++;
            }
            if (realTargets != 1) populateRightControlBox();
        }
        setElementsFromModel();
        horizTreesView.updateContentFromViewElements();
    }

    private void populateRightControlBox() {
        if (isTornDown || horizTreesView == null || horizTreesModel == null) return;
        try {
            VBox controlBox = (VBox) horizTreesView.getRightControl();
            if (controlBox == null) return;
            ObservableList<Node> members = controlBox.getChildren();
            members.add(new Separator(Orientation.HORIZONTAL));

            for (ElementTypes type : horizTreesModel.getElementTypes()) {
                if (type == ElementTypes.HORIZ_TREE) {
                    members.add(horizTreesView.getAddHTreeButton());
                }
            }
            members.addAll(horizTreesView.getMoveUpButton(), horizTreesView.getMoveDownButton(), horizTreesView.getRemoveButton(), horizTreesView.getRestoreButton());
        } catch (Exception e) {}
    }

    private void setElementsFromModel() {
        if (isTornDown || mainWindow == null || horizTreesView == null || horizTreesModel == null) return;
        try {
            TypeSelectorFactories typeFactory = new TypeSelectorFactories(mainWindow);
            List<ViewElement> viewElements = horizTreesView.getViewElements();

            // SCORCHED EARTH RULE 8: The Undo/Redo Rebuild Fix (Prevent CME & Leaks)
            if (exerciseList != null) {
                List<Exercise> copy = new ArrayList<>(exerciseList);
                for (Exercise ex : copy) {
                    try { if (ex != null) ex.cleanup(); } catch (Exception e) {}
                }
                exerciseList.clear(); // Safe because this is a runtime rebuild, not a teardown!
            }

            if (viewElements != null) {
                List<ViewElement> copy = new ArrayList<>(viewElements);
                for (ViewElement ve : copy) {
                    try { if (ve != null && ve.getNode() != null) severFromParent(ve.getNode()); } catch (Exception e) {}
                }
                viewElements.clear(); // Safe runtime rebuild
            }

            for (ModelElement modelElement : horizTreesModel.getModelElements()) {
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
        } catch (Exception e) {}
    }

    private void setActiveExercise(int index) {
        if (isTornDown || exerciseList == null || index < 0 || index >= exerciseList.size() || exerciseList.get(index) == null || horizTreesView == null) return;
        try {
            Exercise exer = exerciseList.get(index);
            activeExercise = exer;
            if (exer != null && exer.getExerciseView() != null) {
                ExerciseView exerView = (ExerciseView) exer.getExerciseView();
                setExerciseControl(exerView.getExerciseControl());

                if (horizTreesView != null) horizTreesView.setBorders(index);

                Platform.runLater(() -> {
                    if (isTornDown || mainView == null || horizTreesView == null || exerciseList == null || index >= exerciseList.size() || exerciseList.get(index) == null) return;
                    try { horizTreesView.setContentSpinners(index, exerciseList.get(index).getFFHeightSpinner(), exerciseList.get(index).getFFWidthSpinner()); } catch (Exception e) {}
                });
            }
        } catch (Exception e) {}
    }

    private void removeAction()  {
        if (isTornDown || mainView == null) return;
        try {
            horizTreesModel = getHorizTreesModelFromView();
            if (horizTreesModel == null || exerciseList == null || horizTreesView == null) return;
            List<ModelElement> modelElements = horizTreesModel.getModelElements();
            if (modelElements != null && modelElements.size() > 1) {
                int index = exerciseList.indexOf(activeExercise);
                if (index >= 0 && index < modelElements.size()) {
                    lastRemovedElement = modelElements.get(index);
                    modelElements.remove(index);

                    setElementsFromModel();
                    horizTreesView.updateContentFromViewElements();

                    if (horizTreesView.getRestoreButton() != null) horizTreesView.getRestoreButton().setDisable(false);
                    exerciseModified = true;
                    if (index > 0) setActiveExercise(index - 1);
                    else setActiveExercise(index);
                }
            } else {
                EditorAlerts.fleetingRedPopup("Exercise must contain at least one element.");
            }
        } catch (Exception e) {}
    }

    private void restoreAction() {
        if (isTornDown || mainView == null || lastRemovedElement == null) return;
        try {
            horizTreesModel = getHorizTreesModelFromView();
            if (horizTreesModel == null || exerciseList == null || horizTreesView == null) return;
            List<ModelElement> modelElements = horizTreesModel.getModelElements();
            int index = exerciseList.indexOf(activeExercise);
            if (modelElements != null && index >= -1) {
                modelElements.add(++index, lastRemovedElement);
                setElementsFromModel();
                horizTreesView.updateContentFromViewElements();

                if (horizTreesView.getRestoreButton() != null) horizTreesView.getRestoreButton().setDisable(true);
                setActiveExercise(index);
                exerciseModified = true;
            }
        } catch (Exception e) {}
    }

    private void moveUpAction() {
        if (isTornDown || mainView == null) return;
        try {
            horizTreesModel = getHorizTreesModelFromView();
            if (horizTreesModel == null || exerciseList == null || horizTreesView == null) return;
            List<ModelElement> modelElements = horizTreesModel.getModelElements();
            int index = exerciseList.indexOf(activeExercise);
            if (modelElements != null && index > 0 && index < modelElements.size()) {
                Collections.swap(modelElements, index, index - 1);
                setElementsFromModel();
                horizTreesView.updateContentFromViewElements();
                exerciseModified = true;
                setActiveExercise(--index);
            } else {
                EditorAlerts.fleetingRedPopup("Cannot move top item up.");
            }
        } catch (Exception e) {}
    }

    private void moveDownAction() {
        if (isTornDown || mainView == null) return;
        try {
            horizTreesModel = getHorizTreesModelFromView();
            if (horizTreesModel == null || exerciseList == null || horizTreesView == null) return;
            List<ModelElement> modelElements = horizTreesModel.getModelElements();
            int index = exerciseList.indexOf(activeExercise);
            if (modelElements != null && index >= 0 && index + 1 < modelElements.size()) {
                Collections.swap(modelElements, index, index + 1);
                setElementsFromModel();
                horizTreesView.updateContentFromViewElements();
                exerciseModified = true;
                setActiveExercise(++index);
            } else {
                EditorAlerts.fleetingRedPopup("Cannot move bottom item down.");
            }
        } catch (Exception e) {}
    }

    private void addHTreeAction() {
        if (isTornDown || mainView == null || horizTreesView == null || horizTreesView.getContentBox() == null || exerciseList == null) return;
        try {
            int index = exerciseList.indexOf(activeExercise);
            if (index < 0) return;
            horizTreesModel = getHorizTreesModelFromView();
            if (horizTreesModel != null && horizTreesModel.getModelElements() != null) {
                HorizontalTreeModel newMod = new HorizontalTreeModel();
                ModelElement element = new ModelElement(newMod, 0);
                horizTreesModel.getModelElements().add(++index, element);
                setElementsFromModel();
                horizTreesView.updateContentFromViewElements();
                setActiveExercise(index);
                if (horizTreesView.getRemoveButton() != null) horizTreesView.getRemoveButton().setDisable(false);
                exerciseModified = true;
            }
        } catch (Exception e) {}
    }

    public Exercise getActiveExercise() { return activeExercise;}

    @Override
    public Node getFFViewNode() {return null;}

    @Override
    public Spinner getFFHeightSpinner() {return null;}
    @Override
    public Spinner getFFWidthSpinner() {return null;}

    @Override
    public Node getFFPrintNode(ExerciseModel originalModel) {return null;}

    @Override
    public HorizTreesModel getExerciseModel() { return horizTreesModel;   }

    @Override
    public HorizTreesView getExerciseView() { return horizTreesView;   }

    @Override
    public void saveExercise(boolean saveAs) {
        if (isTornDown) return;
        try {
            boolean success = DiskUtilities.saveExercise(saveAs, getHorizTreesModelFromView());
            if (success) {
                exerciseModified = false;
                if (exerciseList != null) {
                    for (Exercise exercise : exerciseList) {
                        if (exercise != null) exercise.setExerciseModified(false);
                    }
                }
            }
        } catch (Exception e) {}
    }

    @Override
    public List<Node> getPrintNodes(ExerciseModel originalModel) {
        if (isTornDown || horizTreesModel == null || horizTreesView == null) return new ArrayList<>();
        List<Node> nodeList = new ArrayList<>();
        try {
            HorizTreesModel printModel = horizTreesModel;
            HorizTreesExercise printExercise = this;
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

            if (exerciseList != null) {
                for (int i = 0; i < exerciseList.size(); i++) {
                    Exercise exerElement = exerciseList.get(i);
                    if (exerElement != null) {
                        HorizTreesModel originalHTmodel = (HorizTreesModel) originalModel;
                        if (originalHTmodel != null && originalHTmodel.getModelElements() != null && originalHTmodel.getModelElements().size() > i) {
                            ModelElement originalModelElement = originalHTmodel.getModelElements().get(i);
                            if (originalModelElement != null) {
                                ExerciseModel originalItemModel = originalModelElement.getModel();
                                nodeList.add(exerElement.getFFPrintNode(originalItemModel));
                            }
                        }
                    }
                }
            }

            //explain node
            RichTextArea explainRTA = printExercise.getExerciseView().getExplainDRTA().getEditor();
            explainRTA.prefHeightProperty().unbind();
            explainRTA.minWidthProperty().unbind();
            explainRTA.maxWidthProperty().unbind();
            explainRTA.setPrefHeight(Math.max(40, printModel.getExplainTextHeight() + RTA_PRINT_BMP));
            explainRTA.setMaxWidth(nodeWidth);
            explainRTA.setMinWidth(nodeWidth);

            explainRTA.getStylesheets().clear();
            explainRTA.getStylesheets().add("richTextAreaPrinter.css");
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


            nodeList.add(commentRTA);

        } catch (Exception e) {}
        return nodeList;
    }

    @Override
    public Exercise<HorizTreesModel, HorizTreesView> resetExercise() {
        if (isTornDown || horizTreesView == null || horizTreesView.getExerciseComment() == null) return null;
        try {
            RichTextArea commentRTA = horizTreesView.getExerciseComment().getEditor();
            commentRTA.getActionFactory().saveNow().execute(new ActionEvent());
            Document commentDocument = commentRTA.getDocument();

            int pointsEarned = -1;
            TextField pointsField = horizTreesView.getPointsEarnedTextField();
            if ((pointsField.isFocused() || horizTreesModel.getPointsEarned() >= 0) && pointsField.getText().isEmpty()) {
                pointsField.setText("0");
            }
            if (!pointsField.getText().equals("")) pointsEarned = Integer.parseInt(pointsField.getText());


            HorizTreesModel originalModel = (HorizTreesModel) (horizTreesModel.getOriginalModel());
            if (originalModel != null) {
                originalModel.setExerciseComment(commentDocument);

                double commentHeight = commentRTA.getFullHeight();
                originalModel.setCommentTextHeight(commentHeight > 0 ? commentHeight : horizTreesModel.getCommentTextHeight());

                originalModel.setPointsEarned(pointsEarned);
            }
            HorizTreesExercise clearExercise = new HorizTreesExercise(originalModel, mainWindow);
            exerciseModified = false;
            return clearExercise;
        } catch (Exception e) { return null; }
    }

    TextFlow printCheckNode() {
        TextFlow flow = new TextFlow();
        flow.setMaxWidth(200);
        try {
            if (htCheck != null && horizTreesView != null && htCheck.isCheckSuccess()) {
                Text bigCheck = new Text("\ue89a");
                bigCheck.setFont(Font.font("Noto Serif Combo", 14));
                Text message = new Text("  " + horizTreesView.getCheckMessage());
                message.setFont(Font.font("Noto Serif Combo", 11));

                bigCheck.setFill(Color.LAWNGREEN);
                message.setFill(Color.GREEN);
                flow.getChildren().addAll(bigCheck, message);
            }
        } catch (Exception e) {}
        return flow;
    }

    @Override
    public boolean isExerciseModified() {
        if (isTornDown) return exerciseModified;
        if (exerciseList != null) {
            for (Exercise exercise : exerciseList) {
                if (exercise != null && exercise.isExerciseModified()) {
                    exerciseModified = true;
                }
            }
        }
        return exerciseModified;
    }

    @Override
    public void setExerciseModified(boolean modified) {
        if (isTornDown) return;
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

    @Override
    public ExerciseModel<HorizTreesModel> getExerciseModelFromView() {
        return (ExerciseModel) getHorizTreesModelFromView();
    }

    private HorizTreesModel getHorizTreesModelFromView() {
        if (isTornDown || horizTreesModel == null || horizTreesView == null) return null;

        try {
            HorizTreesModel model = new HorizTreesModel(horizTreesModel.getExerciseName(), horizTreesModel.getElementTypes());
            model.setExplainPrompt(horizTreesModel.getExplainPrompt());
            model.setAuxExerName(horizTreesModel.getAuxExerName());
            model.setOriginalModel(horizTreesModel.getOriginalModel());
            model.setStarted(horizTreesModel.isStarted() || exerciseModified);

            HorizTreesCheckSetup checkSetup = new HorizTreesCheckSetup();
            HorizTreesCheckSetup horizTreesCheckSetup = horizTreesModel.getCheckSetup();
            if (htCheck != null && horizTreesView != null && horizTreesCheckSetup != null) {
                checkSetup.setCheckTries(htCheck.getCheckTries());
                checkSetup.setCheckSuccess(htCheck.isCheckSuccess());
                checkSetup.setCheckMessage(horizTreesView.getCheckMessage());
                if (horizTreesView.getPermitEmptyBranchesCheck() != null) {
                    checkSetup.setEmptyBranchesSelected(horizTreesView.getPermitEmptyBranchesCheck().isSelected());
                }
                checkSetup.setObjLangName(horizTreesCheckSetup.getObjLangName());
                checkSetup.setCheckMax(horizTreesCheckSetup.getCheckMax());
                checkSetup.setStaticHelp(horizTreesCheckSetup.isStaticHelp());
                checkSetup.setStaticHelpDoc(horizTreesCheckSetup.getStaticHelpDoc());
                checkSetup.setEmptyBranchesOK(horizTreesCheckSetup.isEmptyBranchesOK());
                checkSetup.setTargetFormulas(horizTreesCheckSetup.getTargetFormulas());
                checkSetup.setInterpretationTextHeight(horizTreesCheckSetup.getInterpretationTextHeight());
                checkSetup.setInterpretationPrefHeight(horizTreesCheckSetup.getInterpretationPrefHeight());
                checkSetup.setRootCount(horizTreesCheckSetup.getRootCount());
            }
            model.setCheckSetup(checkSetup);


            model.setExerciseStatement(horizTreesModel.getExerciseStatement());
            if (horizTreesView.getExerciseStatement() != null && horizTreesView.getExerciseStatement().getEditor() != null) {
                model.setStatementPrefHeight(horizTreesView.getExerciseStatement().getEditor().getPrefHeight());
            }
            model.setStatementTextHeight(horizTreesModel.getStatementTextHeight());

            RichTextArea commentRTA = horizTreesView.getExerciseComment().getEditor();
            commentRTA.getActionFactory().saveNow().execute(new ActionEvent());
            model.setExerciseComment(commentRTA.getDocument());
            model.setCommentPrefHeight(horizTreesView.getCommentPrefHeight());
            double commentTextHeight = commentRTA.getFullHeight();
            model.setCommentTextHeight(commentTextHeight > 0 ? commentTextHeight : horizTreesModel.getCommentTextHeight());

            RichTextArea explainRTA = horizTreesView.getExplainDRTA().getEditor();
            explainRTA.getActionFactory().saveNow().execute(new ActionEvent());
            model.setExplainDocument(explainRTA.getDocument());
            model.setExplainPrefHeight(horizTreesView.getExplainPrefHeight());
            double explainTextHeight = explainRTA.getFullHeight();
            model.setExplainTextHeight(explainTextHeight > 0 ? explainTextHeight : horizTreesModel.getExplainTextHeight());


            RichTextArea interpretationRTA = horizTreesView.getInterpretationDRTA().getEditor();
            interpretationRTA.getActionFactory().saveNow().execute(new ActionEvent());
            Document interpretationDocument = interpretationRTA.getDocument();
            checkSetup.setInterpretationDoc(interpretationDocument);
            checkSetup.setInterpretationPrefHeight(horizTreesView.getInterpretationPrefHeight());

            if (horizTreesModel.getCheckSetup() != null) {
                double interpTextHeight = interpretationRTA.getFullHeight();
                double txtHeight = interpTextHeight > 0 ? interpTextHeight : checkSetup.getInterpretationTextHeight();
                checkSetup.setInterpretationTextHeight(Math.max(60, txtHeight));
            }

            model.setPointsPossible(horizTreesModel.getPointsPossible());
            TextField pointsField = horizTreesView.getPointsEarnedTextField();
            if ((pointsField.isFocused() || horizTreesModel.getPointsEarned() >= 0) && pointsField.getText().isEmpty()) {
                pointsField.setText("0");
            }
            if (!pointsField.getText().equals("")) model.setPointsEarned(Integer.parseInt(pointsField.getText()));
            else model.setPointsEarned(-1);

            List<ModelElement> modelElements = new ArrayList<>();
            if (exerciseList != null && horizTreesView.getViewElements() != null) {
                for (int i = 0; i < exerciseList.size(); i++) {
                    if (i >= horizTreesView.getViewElements().size()) continue;
                    Exercise exer = exerciseList.get(i);
                    if (exer != null && horizTreesView.getViewElements().get(i) != null) {
                        ExerciseModel exerModel = exer.getExerciseModelFromView();
                        int indent = horizTreesView.getViewElements().get(i).getIndentLevel();
                        ModelElement element = new ModelElement(exerModel, indent);
                        modelElements.add(element);
                    }
                }
            }
            model.setModelElements(modelElements);

            return model;
        } catch (Exception e) { return null; }
    }

    public List<Exercise> getExerciseList() {return exerciseList;}

    public MainWindow getMainWindow() {
        return mainWindow;
    }

    public HorizTreesView getHorizTreesView() {
        return horizTreesView;
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
        // SCORCHED EARTH RULE 2: Kill Switch Engaged
        isTornDown = true;

        // SCORCHED EARTH RULE 7: Safe Global State Clearance
        try {
            if (mainView != null) {
                mainView.textFieldInFocus(); // Remove toolbars for RTAs safely
            }
        } catch (Exception e) {}

        // SCORCHED EARTH RULE 3: Explicit Anonymous Lambda Removal
        try { if (thumbPane != null && thumbPaneClickHandler != null) thumbPane.removeEventHandler(MouseEvent.MOUSE_CLICKED, thumbPaneClickHandler); } catch (Exception e) {}
        try { if (thumbPane != null) thumbPane.setOnMouseClicked(null); } catch (Exception e) {}

        try { if (interpretationEditor != null && interpFullHeightListener != null) interpretationEditor.fullHeightProperty().removeListener(interpFullHeightListener); } catch (Exception e) {}
        try { if (interpretationEditor != null && interpModifiedListener != null) interpretationEditor.modifiedProperty().removeListener(interpModifiedListener); } catch (Exception e) {}
        try { if (interpretationEditor != null && interpFocusListener != null) interpretationEditor.focusedProperty().removeListener(interpFocusListener); } catch (Exception e) {}

        try { if (horizTreesView != null && horizTreesView.getPointsEarnedTextField() != null && pointsEarnedListener != null) horizTreesView.getPointsEarnedTextField().textProperty().removeListener(pointsEarnedListener); } catch (Exception e) {}

        try { if (horizTreesView != null && horizTreesView.getExerciseStatement() != null && horizTreesView.getExerciseStatement().getEditor() != null && statementFocusListener != null) horizTreesView.getExerciseStatement().getEditor().focusedProperty().removeListener(statementFocusListener); } catch (Exception e) {}

        try {
            if (horizTreesView != null && horizTreesView.getExerciseComment() != null && horizTreesView.getExerciseComment().getEditor() != null) {
                if (commentFullHeightListener != null) horizTreesView.getExerciseComment().getEditor().fullHeightProperty().removeListener(commentFullHeightListener);
                if (commentModifiedListener != null) horizTreesView.getExerciseComment().getEditor().modifiedProperty().removeListener(commentModifiedListener);
                if (commentFocusListener != null) horizTreesView.getExerciseComment().getEditor().focusedProperty().removeListener(commentFocusListener);
            }
        } catch (Exception e) {}

        try {
            if (horizTreesView != null && horizTreesView.getExplainDRTA() != null && horizTreesView.getExplainDRTA().getEditor() != null) {
                if (explainFullHeightListener != null) horizTreesView.getExplainDRTA().getEditor().fullHeightProperty().removeListener(explainFullHeightListener);
                if (explainModifiedListener != null) horizTreesView.getExplainDRTA().getEditor().modifiedProperty().removeListener(explainModifiedListener);
                if (explainFocusListener != null) horizTreesView.getExplainDRTA().getEditor().focusedProperty().removeListener(explainFocusListener);
            }
        } catch (Exception e) {}

        try { if (horizTreesView != null && horizTreesView.getRemoveButton() != null && removeButtonHandler != null) { horizTreesView.getRemoveButton().removeEventHandler(ActionEvent.ACTION, removeButtonHandler); horizTreesView.getRemoveButton().setOnAction(null); } } catch (Exception e) {}
        try { if (horizTreesView != null && horizTreesView.getRestoreButton() != null && restoreButtonHandler != null) { horizTreesView.getRestoreButton().removeEventHandler(ActionEvent.ACTION, restoreButtonHandler); horizTreesView.getRestoreButton().setOnAction(null); } } catch (Exception e) {}
        try { if (horizTreesView != null && horizTreesView.getMoveUpButton() != null && moveUpButtonHandler != null) { horizTreesView.getMoveUpButton().removeEventHandler(ActionEvent.ACTION, moveUpButtonHandler); horizTreesView.getMoveUpButton().setOnAction(null); } } catch (Exception e) {}
        try { if (horizTreesView != null && horizTreesView.getMoveDownButton() != null && moveDownButtonHandler != null) { horizTreesView.getMoveDownButton().removeEventHandler(ActionEvent.ACTION, moveDownButtonHandler); horizTreesView.getMoveDownButton().setOnAction(null); } } catch (Exception e) {}
        try { if (horizTreesView != null && horizTreesView.getAddHTreeButton() != null && addHTreeButtonHandler != null) { horizTreesView.getAddHTreeButton().removeEventHandler(ActionEvent.ACTION, addHTreeButtonHandler); horizTreesView.getAddHTreeButton().setOnAction(null); } } catch (Exception e) {}

        // ---> SCORCHED EARTH RULE 4: CONTROLLER ONLY UNBINDS. (No Timers, No setSkin(null)) <---
        try {
            final RichTextArea stEditor = (horizTreesView != null && horizTreesView.getExerciseStatement() != null) ? horizTreesView.getExerciseStatement().getEditor() : null;
            final RichTextArea commEditor = (horizTreesView != null && horizTreesView.getExerciseComment() != null) ? horizTreesView.getExerciseComment().getEditor() : null;
            final RichTextArea expEditor = (horizTreesView != null && horizTreesView.getExplainDRTA() != null) ? horizTreesView.getExplainDRTA().getEditor() : null;
            final RichTextArea interpEditor1 = (interpretationDRTA != null) ? interpretationDRTA.getEditor() : null;
            final RichTextArea interpEditor2 = interpretationEditor;

            if (stEditor != null) { stEditor.prefHeightProperty().unbind(); stEditor.minWidthProperty().unbind(); stEditor.maxWidthProperty().unbind(); }
            if (commEditor != null) { commEditor.prefHeightProperty().unbind(); commEditor.minWidthProperty().unbind(); commEditor.maxWidthProperty().unbind(); }
            if (expEditor != null) { expEditor.prefHeightProperty().unbind(); expEditor.minWidthProperty().unbind(); expEditor.maxWidthProperty().unbind(); }
            if (interpEditor1 != null) { interpEditor1.prefHeightProperty().unbind(); interpEditor1.minWidthProperty().unbind(); interpEditor1.maxWidthProperty().unbind(); }
            if (interpEditor2 != null) { interpEditor2.prefHeightProperty().unbind(); interpEditor2.minWidthProperty().unbind(); interpEditor2.maxWidthProperty().unbind(); }
        } catch (Exception e) {}

        // SCORCHED EARTH RULE 6: DISMANTLE THE UI TREE
        try {
            if (thumbPane != null) {
                thumbPane.getChildren().clear();
                severFromParent(thumbPane);
            }
        } catch (Exception e) {}

        try { if (undoRedoList != null) undoRedoList = null; } catch (Exception e) {}
        try {
            clearStandingPopups();
            if (tScene != null) tScene.setRoot(new Pane());
        } catch (Exception e) {}

        // SCORCHED EARTH RULE 1 & 8: Cascade cleanup
        try {
            if (exerciseList != null) {
                List<Exercise> copyExers = new ArrayList<>(exerciseList);
                for (Exercise exer : copyExers) {
                    try { if (exer != null) exer.cleanup(); } catch (Exception e) {}
                }
                exerciseList = null;
            }
        } catch (Exception e) {}

        try { if (auxExerciseA != null) auxExerciseA.cleanup(); } catch (Exception e) {}
        try { if (auxExerciseB != null) auxExerciseB.cleanup(); } catch (Exception e) {}

        try { if (htCheck != null) htCheck.cleanup(); } catch (Exception e) {}
        try { if (horizTreesView != null) horizTreesView.cleanup(); } catch (Exception e) {}


        // Final nullifications
        try { this.mainWindow = null; } catch (Exception e) {}
        try { this.mainView = null; } catch (Exception e) {}
        try { this.horizTreesModel = null; } catch (Exception e) {}
        try { this.horizTreesView = null; } catch (Exception e) {}
        try { this.activeExercise = null; } catch (Exception e) {}
        try { this.exerciseClickFilter = null; } catch (Exception e) {}
        try { this.lastRemovedElement = null; } catch (Exception e) {}
        try { this.htCheck = null; } catch (Exception e) {}
        try { this.interpretationDRTA = null; } catch (Exception e) {}
        try { this.interpretationEditor = null; } catch (Exception e) {}
        try { this.auxExerciseA = null; } catch (Exception e) {}
        try { this.auxExerciseB = null; } catch (Exception e) {}
        try { this.tStage = null; } catch (Exception e) {}
        try { this.thumbPane = null; } catch (Exception e) {}
        try { this.tScene = null; } catch (Exception e) {}

        try { this.thumbPaneClickHandler = null; } catch (Exception e) {}
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
        try { this.removeButtonHandler = null; } catch (Exception e) {}
        try { this.restoreButtonHandler = null; } catch (Exception e) {}
        try { this.moveUpButtonHandler = null; } catch (Exception e) {}
        try { this.moveDownButtonHandler = null; } catch (Exception e) {}
        try { this.addHTreeButtonHandler = null; } catch (Exception e) {}
    }

    @Override
    public Node getAuxExerNode() {
        if (horizTreesView == null || horizTreesModel == null || htCheck == null || htCheck.getCheckSetup() == null || horizTreesView.getContentBox() == null) return new Pane();
        try {
            RichTextArea explainRTA = horizTreesView.getExplainDRTA().getEditor();
            explainRTA.prefHeightProperty().unbind();
            explainRTA.setMinHeight(horizTreesModel.getExplainTextHeight() + RTA_SCREEN_BMP);
            RichTextArea interpRTA = horizTreesView.getInterpretationDRTA().getEditor();
            interpRTA.prefHeightProperty().unbind();
            interpRTA.setMinHeight(htCheck.getCheckSetup().getInterpretationTextHeight() + RTA_SCREEN_BMP);
            return horizTreesView.getContentBox();
        }
        catch (Exception e) { return new Pane(); }
    }

    @Override
    public String getAuxExerName() {
        if (horizTreesModel == null) return "";
        return horizTreesModel.getAuxExerName();
    }

    @Override
    public void setAuxExerA(Exercise exer) {
        auxExerciseA = exer;
    }

    @Override
    public void setAuxExerB(Exercise exer) { auxExerciseB = exer; }

}
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

package slapp.editor.vert_tree_abefexplain;

import com.gluonhq.richtextarea.RichTextArea;
import com.gluonhq.richtextarea.model.Document;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.layout.AnchorPane;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.scene.transform.Scale;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.apache.commons.lang3.SerializationUtils;
import slapp.editor.DiskUtilities;
import slapp.editor.EditorMain;
import slapp.editor.PrintUtilities;
import slapp.editor.decorated_rta.BoxedDRTA;
import slapp.editor.decorated_rta.DecoratedRTA;
import slapp.editor.main_window.*;
import slapp.editor.vertical_tree.*;
import slapp.editor.vertical_tree.drag_drop.DragIconType;
import slapp.editor.vertical_tree.object_models.*;

import java.util.ArrayList;
import java.util.List;
import static slapp.editor.EditorMain.*;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.Spinner;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.event.EventHandler;
import javafx.scene.control.*;
import javafx.scene.layout.*;


public class VerticalTreeABEFExpExercise implements Exercise<VerticalTreeABEFExpModel, VerticalTreeABEFExpView>, VTAuxExer {

    private MainWindow mainWindow;
    private MainWindowView mainView;
    private VerticalTreeABEFExpModel verticalTreeABEFExpModel;
    private VerticalTreeABEFExpView verticalTreeABEFExpView;
    private VTABEFExpCheck vtABEFExpCheck;
    private boolean exerciseModified = false;
    private UndoRedoList<VerticalTreeABEFExpModel> undoRedoList = new UndoRedoList<>(50);
    public BooleanProperty undoRedoFlag = new SimpleBooleanProperty();
    private Exercise auxExerciseA;
    private Exercise auxExerciseB;
    Stage tStage;
    Pane thumbPane = new Pane();
    Scene tScene = new Scene(new Pane());

    // SCORCHED EARTH RULE 1: The Asynchronous Ghost "Kill Switch"
    private boolean isTornDown = false;

    // SCORCHED EARTH RULE 3: Eradicate Anonymous Lambdas
    private ChangeListener<Boolean> undoRedoFlagListener;
    private ChangeListener<String> pointsEarnedListener;
    private ChangeListener<Boolean> statementFocusListener;
    private ChangeListener<Number> commentFullHeightListener;
    private ChangeListener<Boolean> commentModifiedListener;
    private ChangeListener<Boolean> commentFocusListener;
    private ChangeListener<Boolean> aCheckBoxListener;
    private ChangeListener<Boolean> bCheckBoxListener;
    private ChangeListener<Boolean> eCheckBoxListener;
    private ChangeListener<Boolean> fCheckBoxListener;
    private ChangeListener<Number> explainFullHeightListener;
    private ChangeListener<Boolean> explainModifiedListener;
    private ChangeListener<Boolean> explainFocusListener;
    private EventHandler<ActionEvent> undoButtonHandler;
    private EventHandler<ActionEvent> redoButtonHandler;
    private EventHandler<MouseEvent> thumbPaneClickHandler;

    private boolean isRebuilding = false;
    private boolean isUndoQueued = false;

    public VerticalTreeABEFExpExercise(VerticalTreeABEFExpModel model, MainWindow mainWindow) {
        this.mainWindow = mainWindow;
        this.verticalTreeABEFExpModel = model;
        if (model.getOriginalModel() == null) {model.setOriginalModel(model); }
        this.mainView = mainWindow.getMainView();
        this.verticalTreeABEFExpView = new VerticalTreeABEFExpView(mainView, this);
        setVerticalTreeView();

        vtABEFExpCheck = new VTABEFExpCheck(this);
        undoRedoList = new UndoRedoList<>(50);

        undoRedoFlag.set(false);
        undoRedoFlag.bind(verticalTreeABEFExpView.undoRedoFlagProperty());

        undoRedoFlagListener = (ob, ov, nv) -> {
            if (isTornDown || isRebuilding || mainView == null) return;
            if (nv) {
                exerciseModified = true;
                pushUndoRedo();
            }
        };
        undoRedoFlag.addListener(undoRedoFlagListener);

        VerticalTreeABEFExpModel deepCopy = (VerticalTreeABEFExpModel) SerializationUtils.clone(verticalTreeABEFExpModel);
        undoRedoList.push(deepCopy);
        updateUndoRedoButtons();
        Platform.runLater(() -> {
            if (isTornDown || mainView == null) return;
            exerciseModified = false;
        });
    }

    private void setVerticalTreeView() {
        verticalTreeABEFExpView.setExplainPrompt(verticalTreeABEFExpModel.getExplainPrompt());
        verticalTreeABEFExpView.setDefaultKeyboard(verticalTreeABEFExpModel.getDefaultKeyboardType());
        verticalTreeABEFExpView.setDefaultMapKeyboard(verticalTreeABEFExpModel.getDefaultMapKeyboardType());
        verticalTreeABEFExpView.setStatementPrefHeight(verticalTreeABEFExpModel.getStatementPrefHeight());
        verticalTreeABEFExpView.setCommentPrefHeight(verticalTreeABEFExpModel.getCommentPrefHeight());
        verticalTreeABEFExpView.setExplainPrefHeight(verticalTreeABEFExpModel.getExplainPrefHeight());
        verticalTreeABEFExpView.setMainPanePrefHeight(verticalTreeABEFExpModel.getMainPanePrefHeight());
        verticalTreeABEFExpView.setMainPanePrefWidth(verticalTreeABEFExpModel.getMainPanePrefWidth());

        verticalTreeABEFExpView.setPointsPossible(verticalTreeABEFExpModel.getPointsPossible());
        if (verticalTreeABEFExpModel.getPointsEarned() >= 0) verticalTreeABEFExpView.getPointsEarnedTextField().setText(Integer.toString(verticalTreeABEFExpModel.getPointsEarned()));

        pointsEarnedListener = (ob, ov, nv) -> {
            if (isTornDown || mainView == null) return;
            exerciseModified = true;
        };
        verticalTreeABEFExpView.getPointsEarnedTextField().textProperty().addListener(pointsEarnedListener);

        //statement
        DecoratedRTA statementDRTA = new DecoratedRTA();
        RichTextArea statementEditor = statementDRTA.getEditor();
        statementEditor.getActionFactory().open(verticalTreeABEFExpModel.getExerciseStatement()).execute(new ActionEvent());

        mainView.editorInFocus(statementDRTA, ControlType.STATEMENT);

        statementFocusListener = (o, ov, nv) -> {
            if (isTornDown || mainView == null) return;
            try {
                if (nv) {
                    mainView.editorInFocus(statementDRTA, ControlType.STATEMENT);
                    mainView.setRTATextHeight(statementEditor.getFullHeight());
                } else mainView.undoRTATextHeight();
            } catch (Exception e) {}
        };
        statementEditor.focusedProperty().addListener(statementFocusListener);
        verticalTreeABEFExpView.setExerciseStatement(statementDRTA);

        //comment
        DecoratedRTA commentDRTA = new DecoratedRTA();
        RichTextArea commentEditor = commentDRTA.getEditor();
        commentEditor.setPromptText("Comment: ");
        commentEditor.getActionFactory().open(verticalTreeABEFExpModel.getExerciseComment()).execute(new ActionEvent());
        commentEditor.getActionFactory().saveNow().execute(new ActionEvent());

        final boolean[] commentPrimed = {false};
        commentFullHeightListener = (ob, ov, nv) -> {
            if (isTornDown || mainView == null || verticalTreeABEFExpModel == null) return;
            if (!commentPrimed[0]) {
                commentPrimed[0] = true;
                return;
            }
            double height = (double) nv;
            mainView.setRTATextHeight(height);
            verticalTreeABEFExpModel.setCommentTextHeight(height);
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
                } else mainView.undoRTATextHeight();
            } catch (Exception e) {}
        };
        commentEditor.focusedProperty().addListener(commentFocusListener);
        verticalTreeABEFExpView.setExerciseComment(commentDRTA);

        //choices
        verticalTreeABEFExpView.getABChoiceLeadLabel().setText(verticalTreeABEFExpModel.getABChoiceLead());
        CheckBox aCheckBox = verticalTreeABEFExpView.getaCheckBox();
        CheckBox bCheckBox = verticalTreeABEFExpView.getbCheckBox();

        aCheckBox.setText(verticalTreeABEFExpModel.getaPrompt());
        aCheckBox.setSelected(verticalTreeABEFExpModel.isaSelected());
        aCheckBoxListener = (ob, ov, nv) -> {
            if (isTornDown || mainView == null) return;
            if (nv != null && nv) bCheckBox.setSelected(false);
            exerciseModified = true;
        };
        aCheckBox.selectedProperty().addListener(aCheckBoxListener);

        bCheckBox.setText(verticalTreeABEFExpModel.getbPrompt());
        bCheckBox.setSelected(verticalTreeABEFExpModel.isbSelected());
        bCheckBoxListener = (ob, ov, nv) -> {
            if (isTornDown || mainView == null) return;
            if (nv != null && nv) aCheckBox.setSelected(false);
            exerciseModified = true;
        };
        bCheckBox.selectedProperty().addListener(bCheckBoxListener);

        verticalTreeABEFExpView.getEFChoiceLeadLabel().setText(verticalTreeABEFExpModel.getEFChoiceLead());
        CheckBox eCheckBox = verticalTreeABEFExpView.geteCheckBox();
        CheckBox fCheckBox = verticalTreeABEFExpView.getfCheckBox();

        eCheckBox.setText(verticalTreeABEFExpModel.getePrompt());
        eCheckBox.setSelected(verticalTreeABEFExpModel.iseSelected());
        eCheckBoxListener = (ob, ov, nv) -> {
            if (isTornDown || mainView == null) return;
            if (nv != null && nv) fCheckBox.setSelected(false);
            exerciseModified = true;
        };
        eCheckBox.selectedProperty().addListener(eCheckBoxListener);

        fCheckBox.setText(verticalTreeABEFExpModel.getfPrompt());
        fCheckBox.setSelected(verticalTreeABEFExpModel.isfSelected());
        fCheckBoxListener = (ob, ov, nv) -> {
            if (isTornDown || mainView == null) return;
            if (nv != null && nv) eCheckBox.setSelected(false);
            exerciseModified = true;
        };
        fCheckBox.selectedProperty().addListener(fCheckBoxListener);

        //explain
        DecoratedRTA explainDRTA = verticalTreeABEFExpView.getExplainDRTA();
        RichTextArea explainEditor = explainDRTA.getEditor();
        explainEditor.getActionFactory().open(verticalTreeABEFExpModel.getExplainDocument()).execute(new ActionEvent());
        explainEditor.setPromptText(verticalTreeABEFExpModel.getExplainPrompt());
        explainEditor.getActionFactory().saveNow().execute(new ActionEvent());

        final boolean[] explainPrimed = {false};
        explainFullHeightListener = (ob, ov, nv) -> {
            if (isTornDown || mainView == null || verticalTreeABEFExpModel == null) return;
            if (!explainPrimed[0]) {
                explainPrimed[0] = true;
                return;
            }
            double height = (double) nv;
            mainView.setRTATextHeight(height);
            verticalTreeABEFExpModel.setExplainTextHeight(height);
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
                } else mainView.undoRTATextHeight();
            } catch (Exception e) {}
        };
        explainEditor.focusedProperty().addListener(explainFocusListener);

        //cleanup handlers
        undoButtonHandler = e -> {
            if (isTornDown || mainView == null) return;
            undoAction();
        };
        verticalTreeABEFExpView.getUndoButton().setOnAction(undoButtonHandler);

        redoButtonHandler = e -> {
            if (isTornDown || mainView == null) return;
            redoAction();
        };
        verticalTreeABEFExpView.getRedoButton().setOnAction(redoButtonHandler);

        populateControlBox();
        for (DragIconType type : verticalTreeABEFExpModel.getDragIconList()) {
            verticalTreeABEFExpView.getRootLayout().addDragIcon(type);
        }
        populateMainPaneNodes();
        verticalTreeABEFExpView.initializeViewDetails();
        verticalTreeABEFExpView.setRightControlBox();
    }

    private void populateMainPaneNodes() {
        if (verticalTreeABEFExpView == null || verticalTreeABEFExpView.getRootLayout() == null) return;
        AnchorPane mainPane = verticalTreeABEFExpView.getRootLayout().getMain_pane();

        isRebuilding = true;

        try {
            // SCORCHED EARTH RULE 8: The Undo/Redo Rebuild Fix (Prevent CME)
            if (mainPane.getChildren() != null) {
                List<Node> childrenCopy = new ArrayList<>(mainPane.getChildren());
                for (Node node : childrenCopy) {
                    try {
                        node.getClass().getMethod("cleanup").invoke(node);
                    } catch (Exception e) {
                    }
                    severFromParent(node);
                }
            }
            mainPane.getChildren().clear();

            for (VerticalBracketMod bracketMod : verticalTreeABEFExpModel.getVerticalBrackets()) {
                ABEFExpVerticalBracket bracket = new ABEFExpVerticalBracket(verticalTreeABEFExpView);
                mainPane.getChildren().add(bracket);
                bracket.setLayoutX(bracketMod.getLayoutX());
                bracket.setLayoutY(bracketMod.getLayoutY());
                bracket.getMainPane().setPrefHeight(bracketMod.getHeight());
            }

            for (DashedLineMod dlMod : verticalTreeABEFExpModel.getDashedLineMods()) {
                ABEFExpDashedLine dashedLine = new ABEFExpDashedLine(verticalTreeABEFExpView);
                mainPane.getChildren().add(dashedLine);
                dashedLine.setLayoutX(dlMod.getLayoutX());
                dashedLine.setLayoutY(dlMod.getLayoutY());
                dashedLine.getMainPane().setPrefWidth(dlMod.getWidth());
            }

            for (MapFormulaBoxMod mapBoxMod : verticalTreeABEFExpModel.getMapFormulaBoxes()) {
                ABEFExpMapFormulaBox mapFormulaBox = new ABEFExpMapFormulaBox(verticalTreeABEFExpView);
                mainPane.getChildren().add(mapFormulaBox);
                mapFormulaBox.setLayoutX(mapBoxMod.getLayoutX());
                mapFormulaBox.setLayoutY(mapBoxMod.getLayoutY());
                mapFormulaBox.setIdString(mapBoxMod.getIdString());
                mapFormulaBox.setmLinkIds(mapBoxMod.getLinkIdStrings());

                mapFormulaBox.setPrintWidth(mapBoxMod.getPrintWidth());

                BoxedDRTA formulaBox = mapFormulaBox.getFormulaBox();
                RichTextArea mapBoxRTA = formulaBox.getRTA();
                mapBoxRTA.getActionFactory().open(mapBoxMod.getText()).execute(new ActionEvent());
                mapBoxRTA.getActionFactory().saveNow().execute(new ActionEvent());
            }

            for (TreeFormulaBoxMod treeBoxMod : verticalTreeABEFExpModel.getTreeFormulaBoxes()) {
                ABEFExpTreeFormulaBox treeFormulaBox = new ABEFExpTreeFormulaBox(verticalTreeABEFExpView);
                mainPane.getChildren().add(treeFormulaBox);
                treeFormulaBox.setLayoutX(treeBoxMod.getLayoutX());
                treeFormulaBox.setLayoutY(treeBoxMod.getLayoutY());
                treeFormulaBox.setIdString(treeBoxMod.getIdString());
                treeFormulaBox.setmLinkIds(treeBoxMod.getLinkIdStrings());

                treeFormulaBox.setCircleIndexes(treeBoxMod.getCircleIndexes());
                treeFormulaBox.setUlineIndexes(treeBoxMod.getUlineIndexes());
                treeFormulaBox.setUlineInclusion(treeBoxMod.isUlineInclusion());

                treeFormulaBox.setPrintWidth(treeBoxMod.getPrintWidth());

                BoxedDRTA treeFormulaDRTA = treeFormulaBox.getFormulaBox();
                RichTextArea treeBoxRTA = treeFormulaDRTA.getRTA();
                treeBoxRTA.getActionFactory().open(treeBoxMod.getText()).execute(new ActionEvent());
                treeBoxRTA.getActionFactory().saveNow().execute(new ActionEvent());

                if (treeBoxMod.isBoxed()) treeFormulaBox.addBox();
                if (treeBoxMod.isStarred()) treeFormulaBox.addStar();
                if (treeBoxMod.isAnnotation()) {
                    treeFormulaBox.addAnnotation();
                    treeFormulaBox.setAnnotationDoc(treeBoxMod.getAnnotationDoc());
                }
                treeFormulaBox.setCircleXAnchors(treeBoxMod.getCircleXAnchors());
                treeFormulaBox.setRtaBoundsHeight(treeBoxMod.getRtaBoundsHeight());
                treeFormulaBox.setRtaBoundsMinY(treeBoxMod.getRtaBoundsMinY());
                if (treeBoxMod.isCircled()) {
                    treeFormulaBox.setCircled(treeBoxMod.isCircled());
                    treeFormulaBox.setCircle();
                }

                for (UnderlineMod underlineMod : treeBoxMod.getUnderlineList()) {
                    treeFormulaBox.addLineToPane(underlineMod.getStartX(), underlineMod.getLength(), underlineMod.getyPos());
                }
                treeFormulaBox.setBaseline(treeBoxMod.getBaseline());
            }

            ObservableList<Node> nodesList = mainPane.getChildren();
            for (ClickableNodeLinkMod nodeLinkMod : verticalTreeABEFExpModel.getClickableNodeLinks()) {
                ABEFExpClickableNodeLink nodeLink = new ABEFExpClickableNodeLink(verticalTreeABEFExpView);
                mainPane.getChildren().add(nodeLink);
                nodeLink.setId(nodeLinkMod.getIdString());
                ABEFExpTreeFormulaBox source = null;
                ABEFExpTreeFormulaBox target = null;
                for (Node node : nodesList) {
                    if (node instanceof ABEFExpTreeFormulaBox) {
                        ABEFExpTreeFormulaBox treeBox = (ABEFExpTreeFormulaBox) node;
                        if (treeBox.getIdString().equals(nodeLinkMod.getSourceId())) source = treeBox;
                        if (treeBox.getIdString().equals(nodeLinkMod.getTargetId())) target = treeBox;
                    }
                }
                if (source != null && target != null) {
                    nodeLink.bindEnds(source, target);
                }
            }

            for (MapQuestionMarkerMod mapQuestMod : verticalTreeABEFExpModel.getMapQuestionMarkers()) {
                ABEFExpMapQuestionMarker mapQuestion = new ABEFExpMapQuestionMarker(verticalTreeABEFExpView);
                mainPane.getChildren().add(mapQuestion);
                mapQuestion.setId(mapQuestMod.getIdString());

                for (Node node : nodesList) {
                    if (node instanceof ABEFExpMapFormulaBox) {
                        ABEFExpMapFormulaBox mapBox = (ABEFExpMapFormulaBox) node;
                        if (mapBox.getIdString().equals(mapQuestMod.getTargetId())) {
                            mapBox.setMapStage(mapQuestMod.getTargetMapStage());
                            mapBox.setMapXAnchors(mapQuestMod.getTargetXAnchors());
                            mapQuestion.bindQuestionLabel(mapBox);
                            mapBox.undoMappingRequest();
                            break;
                        }
                    }
                }
            }

            for (ClickableMapLinkMod mapLinkMod : verticalTreeABEFExpModel.getClickableMapLinks()) {
                ABEFExpClickableMapLink mapLink = new ABEFExpClickableMapLink(verticalTreeABEFExpView);
                mainPane.getChildren().add(0, mapLink);
                mapLink.setId(mapLinkMod.getIdString());

                ABEFExpMapFormulaBox source = null;
                ABEFExpMapFormulaBox target = null;
                for (Node node : nodesList) {
                    if (node instanceof ABEFExpMapFormulaBox) {
                        ABEFExpMapFormulaBox mapFormulaBox = (ABEFExpMapFormulaBox) node;
                        if (mapFormulaBox.getIdString().equals(mapLinkMod.getSourceId())) {
                            source = mapFormulaBox;
                            source.setMapStage(mapLinkMod.getSourceMapStage());
                            source.setMapXAnchors(mapLinkMod.getSourceXAnchors());
                        }
                        if (mapFormulaBox.getIdString().equals(mapLinkMod.getTargetId())) {
                            target = mapFormulaBox;
                            target.setMapStage(mapLinkMod.getTargetMapStage());
                            target.setMapXAnchors(mapLinkMod.getTargetXAnchors());
                        }
                    }
                }
                if (source != null && target != null) {
                    mapLink.bindEnds(source, target);
                    source.undoMappingRequest();
                    target.undoMappingRequest();
                }
            }
        } finally {
            isRebuilding = false;
        }
    }

    private void populateControlBox() {
        if (verticalTreeABEFExpView == null || verticalTreeABEFExpView.getControlBox() == null) return;
        VBox controlBox = verticalTreeABEFExpView.getControlBox();
        controlBox.setAlignment(Pos.TOP_CENTER);

        ABEFExpRootLayout layout = verticalTreeABEFExpView.getRootLayout();
        ToggleGroup buttonGroup = verticalTreeABEFExpView.getRootLayout().getButtonGroup();

        for (ObjectControlType type : verticalTreeABEFExpModel.getObjectControlList()) {
            switch(type) {
                case FORMULA_BOX: {
                    ToggleButton boxButton = layout.getBoxToggle();
                    controlBox.getChildren().add(boxButton);
                    boxButton.setToggleGroup(buttonGroup);
                    break;
                }
                case CIRCLE: {
                    ToggleButton circleButton = layout.getCircleToggle();
                    controlBox.getChildren().add(circleButton);
                    circleButton.setToggleGroup(buttonGroup);
                    break;
                }
                case STAR: {
                    ToggleButton starButton = layout.getStarToggle();
                    controlBox.getChildren().add(starButton);
                    starButton.setToggleGroup(buttonGroup);
                    break;
                }
                case ANNOTATION: {
                    controlBox.getChildren().add(layout.getAnnotationBox());
                    layout.getAnnotationToggle().setToggleGroup(buttonGroup);
                    break;
                }
                case UNDERLINE: {
                    ToggleButton underlineButton = layout.getUnderlineToggle();
                    controlBox.getChildren().add(underlineButton);
                    underlineButton.setToggleGroup(buttonGroup);
                    break;
                }
                case MAPPING: {
                    ToggleButton mappingButton = layout.getMappingToggle();
                    controlBox.getChildren().add(mappingButton);
                    mappingButton.setToggleGroup(buttonGroup);
                    controlBox.setMargin(mappingButton, new Insets(10, 0, 0, 0));
                }
            }
        }

        Platform.runLater(() -> {
            if (isTornDown || mainView == null) return;
            if (mainWindow != null && mainWindow.getCurrentExercise() != this) return;
            if (auxExerciseA != null) {
                thumbPane = auxImage();
                if (verticalTreeABEFExpView != null && verticalTreeABEFExpView.getControlBox() != null) {
                    verticalTreeABEFExpView.getControlBox().getChildren().add(thumbPane);
                }
            }
        });
    }

    private Pane auxImage() {
        Pane thumbPane = new Pane();
        if (auxExerciseA != null && auxExerciseA.getAuxExerNode() != null)  {

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
            };
            thumbPane.setOnMouseClicked(thumbPaneClickHandler);
        }
        return thumbPane;
    }

    public void handleMouseEvent(MouseEvent event) {
        if (isTornDown || mainView == null || verticalTreeABEFExpView == null || verticalTreeABEFExpView.getRootLayout() == null) return;

        ObservableList<Node> nodesList = verticalTreeABEFExpView.getRootLayout().getMain_pane().getChildren();
        for (Node node : nodesList) {
            if (node instanceof ABEFExpTreeFormulaBox) {
                ABEFExpTreeFormulaBox treeBox = (ABEFExpTreeFormulaBox) node;
                RichTextArea treeRTA = treeBox.getFormulaBox().getRTA();
                if (treeRTA != null && treeRTA.isModified()) {
                    verticalTreeABEFExpView.getVtABEFExpExercise().setExerciseModified(true);
                    verticalTreeABEFExpView.getVtABEFExpExercise().pushUndoRedo();
                    treeRTA.getActionFactory().saveNow().execute(new ActionEvent());
                }
                if (treeBox.isAnnotation() && treeBox.getAnnotationField() != null) {
                    RichTextArea annRTA = treeBox.getAnnotationField().getRTA();
                    if (annRTA != null && annRTA.isModified()) {
                        verticalTreeABEFExpView.getVtABEFExpExercise().setExerciseModified(true);
                        verticalTreeABEFExpView.getVtABEFExpExercise().pushUndoRedo();
                        annRTA.getActionFactory().saveNow().execute(new ActionEvent());
                    }
                }

            }
            if (node instanceof ABEFExpMapFormulaBox) {
                RichTextArea mapRTA = ((ABEFExpMapFormulaBox) node).getFormulaBox().getRTA();
                if (mapRTA != null && mapRTA.isModified()) {
                    verticalTreeABEFExpView.getVtABEFExpExercise().setExerciseModified(true);
                    verticalTreeABEFExpView.getVtABEFExpExercise().pushUndoRedo();
                    mapRTA.getActionFactory().saveNow().execute(new ActionEvent());
                }
            }
        }
    }

    private void undoAction() {
        if (isTornDown || undoRedoList == null || mainView == null) return;
        VerticalTreeABEFExpModel undoElement = undoRedoList.getUndoElement();
        if (undoElement != null) {
            verticalTreeABEFExpModel = (VerticalTreeABEFExpModel) SerializationUtils.clone(undoElement);
            populateMainPaneNodes();
            updateUndoRedoButtons();
        }
    }

    private void redoAction() {
        if (isTornDown || undoRedoList == null || mainView == null) return;
        VerticalTreeABEFExpModel redoElement = undoRedoList.getRedoElement();
        if (redoElement != null) {
            verticalTreeABEFExpModel = (VerticalTreeABEFExpModel) SerializationUtils.clone(redoElement);
            populateMainPaneNodes();
            updateUndoRedoButtons();
        }
    }

    private void updateUndoRedoButtons() {
        if (verticalTreeABEFExpView == null || undoRedoList == null) return;
        if (verticalTreeABEFExpView.getUndoButton() != null) verticalTreeABEFExpView.getUndoButton().setDisable(!undoRedoList.canUndo());
        if (verticalTreeABEFExpView.getRedoButton() != null) verticalTreeABEFExpView.getRedoButton().setDisable(!undoRedoList.canRedo());
    }

    public void pushUndoRedo() {
        if (isTornDown || isRebuilding || undoRedoList == null || verticalTreeABEFExpModel == null) return;

        exerciseModified = true;

        // DEBOUNCER: If a loop triggers multiple saves in the same frame, group them
        if (!isUndoQueued) {
            isUndoQueued = true; // Lock the queue

            // Queue the actual save for the end of the current JavaFX frame
            Platform.runLater(() -> {
                isUndoQueued = false; // Release the lock

                // Double check teardown/rebuild status after the delay
                if (isTornDown || isRebuilding || undoRedoList == null || verticalTreeABEFExpModel == null) return;

                VerticalTreeABEFExpModel model = getVerticalTreeModelFromView();
                VerticalTreeABEFExpModel deepCopy = (VerticalTreeABEFExpModel) SerializationUtils.clone(model);
                undoRedoList.push(deepCopy);
                updateUndoRedoButtons();
            });
        }
    }


    /*
    public void pushUndoRedo() {
        if (isTornDown || isRebuilding || undoRedoList == null || verticalTreeABEFExpModel == null) return;
        VerticalTreeABEFExpModel model = getVerticalTreeModelFromView();
        VerticalTreeABEFExpModel deepCopy = (VerticalTreeABEFExpModel) SerializationUtils.clone(model);
        undoRedoList.push(deepCopy);
        updateUndoRedoButtons();
        exerciseModified = true;
    }

     */

    @Override
    public VerticalTreeABEFExpModel getExerciseModel() {  return verticalTreeABEFExpModel;    }

    @Override
    public VerticalTreeABEFExpView getExerciseView() {  return verticalTreeABEFExpView;    }

    @Override
    public void saveExercise(boolean saveAs) {
        boolean success = DiskUtilities.saveExercise(saveAs, getVerticalTreeModelFromView());
        if (success) exerciseModified = false;
    }

    @Override
    public List<Node> getPrintNodes(ExerciseModel originalModel) {
        List<Node> nodeList = new ArrayList<>();
        VerticalTreeABEFExpModel printModel = verticalTreeABEFExpModel;
        VerticalTreeABEFExpExercise printExercise = this;

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

        //content node
        AnchorPane mainPane = printExercise.getExerciseView().getRootLayout().getMain_pane();
        mainPane.setStyle("-fx-background-color: transparent");
        mainPane.setPadding(new Insets(0));
        mainPane.setMinHeight(0);

        VerticalTreeABEFExpModel originalVTmod = (VerticalTreeABEFExpModel) originalModel;
        mainPane.setMinWidth(originalVTmod.getMainPanePrefWidth() - RTA_WIDTH_BMP);

        ObservableList<Node> nodes = mainPane.getChildren();
        for (Node node : nodes) {
            if (node instanceof ABEFExpTreeFormulaBox) {
                ABEFExpTreeFormulaBox treeBox = (ABEFExpTreeFormulaBox) node;
                treeBox.getFormulaBox().getRTA().setStyle("-fx-border-width: 0; -fx-border-color: transparent");
                treeBox.getFormulaBox().getRTA().setMinWidth(treeBox.getPrintWidth());
                if (treeBox.getAnnotationField() != null) treeBox.getAnnotationField().getRTA().setStyle("-fx-border-width: 0; -fx-border-color: transparent");
            }
            if (node instanceof ABEFExpMapFormulaBox) {
                ABEFExpMapFormulaBox mapBox = (ABEFExpMapFormulaBox) node;
                mapBox.getFormulaBox().getRTA().setStyle("-fx-border-width: 0; -fx-border-color: transparent");
                mapBox.getFormulaBox().getRTA().setMinWidth(mapBox.getPrintWidth());
            }
        }

        HBox contentHBox = new HBox(mainPane);
        contentHBox.setAlignment(Pos.CENTER);
        contentHBox.setPadding(new Insets(0,0,20, 0));
        nodeList.add(contentHBox);

        Label abLeaderLabel = new Label(verticalTreeABEFExpModel.getABChoiceLead());
        CheckBox boxA = new CheckBox(verticalTreeABEFExpModel.getaPrompt());
        boxA.setSelected(verticalTreeABEFExpModel.isaSelected());
        CheckBox boxB = new CheckBox(verticalTreeABEFExpModel.getbPrompt());
        boxB.setSelected(verticalTreeABEFExpModel.isbSelected());
        Font labelFont = new Font("Noto Serif Combo", 11);
        abLeaderLabel.setFont(labelFont); boxA.setFont(labelFont); boxB.setFont(labelFont);

        HBox abBox = new HBox(20);
        abBox.setPadding(new Insets(10,10,10,0));
        abBox.setStyle("-fx-border-width: 0; -fx-border-color: transparent;");
        abBox.getChildren().addAll(abLeaderLabel, boxA, boxB);
        nodeList.add(abBox);

        Label efLeaderLabel = new Label(verticalTreeABEFExpModel.getEFChoiceLead());
        CheckBox boxE = new CheckBox(verticalTreeABEFExpModel.getePrompt());
        boxE.setSelected(verticalTreeABEFExpModel.iseSelected());
        CheckBox boxF = new CheckBox(verticalTreeABEFExpModel.getfPrompt());
        boxF.setSelected(verticalTreeABEFExpModel.isfSelected());
        efLeaderLabel.setFont(labelFont); boxE.setFont(labelFont); boxF.setFont(labelFont);

        HBox efBox = new HBox(20);
        efBox.setPadding(new Insets(10,10,20,0));
        efBox.setStyle("-fx-border-width: 0; -fx-border-color: transparent;");
        efBox.getChildren().addAll(efLeaderLabel, boxE, boxF);
        nodeList.add(efBox);


        RichTextArea explanationRTA = printExercise.getExerciseView().getExplainDRTA().getEditor();
        explanationRTA.prefHeightProperty().unbind();
        explanationRTA.minWidthProperty().unbind();
        explanationRTA.maxWidthProperty().unbind();
        explanationRTA.setPrefHeight(Math.max(40, printModel.getExplainTextHeight() + RTA_PRINT_BMP));
        explanationRTA.setMaxWidth(nodeWidth);
        explanationRTA.setMinWidth(nodeWidth);

        explanationRTA.getStylesheets().clear();
        explanationRTA.getStylesheets().add("richTextAreaPrinter.css");
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
        commentRTA.setMinWidth(nodeWidth);

        commentRTA.getStylesheets().clear(); commentRTA.getStylesheets().add("richTextAreaPrinter.css");


        nodeList.add(commentRTA);

        return nodeList;
    }

    TextFlow printCheckNode() {
        TextFlow flow = new TextFlow();
        flow.setMaxWidth(200);
        if (vtABEFExpCheck != null && vtABEFExpCheck.isCheckSuccess()) {
            Text bigCheck = new Text("\ue89a");
            bigCheck.setFont(Font.font("Noto Serif Combo", 14));
            Text message = new Text("  " + verticalTreeABEFExpView.getCheckMessage());
            message.setFont(Font.font("Noto Serif Combo", 11));

            if (vtABEFExpCheck.isCheckFinal()) {
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

    @Override
    public Exercise<VerticalTreeABEFExpModel, VerticalTreeABEFExpView> resetExercise() {
        RichTextArea commentRTA = verticalTreeABEFExpView.getExerciseComment().getEditor();
        commentRTA.getActionFactory().saveNow().execute(new ActionEvent());
        Document commentDocument = commentRTA.getDocument();

        int pointsEarned = -1;
        TextField pointsField = verticalTreeABEFExpView.getPointsEarnedTextField();
        if ((pointsField.isFocused() || verticalTreeABEFExpModel.getPointsEarned() >= 0) && pointsField.getText().isEmpty()) {
            pointsField.setText("0");
        }
        if (!pointsField.getText().equals("")) pointsEarned = Integer.parseInt(pointsField.getText());


        VerticalTreeABEFExpModel originalModel = (VerticalTreeABEFExpModel) (verticalTreeABEFExpModel.getOriginalModel());
        originalModel.setExerciseComment(commentDocument);

        double commentHeight = commentRTA.getFullHeight();
        originalModel.setCommentTextHeight(commentHeight > 0 ? commentHeight : verticalTreeABEFExpModel.getCommentTextHeight());

        originalModel.setPointsEarned(pointsEarned);

        VTcheckSetup setup = originalModel.getCheckSetup();
        if (vtABEFExpCheck != null) {
            setup.setCheckTries(vtABEFExpCheck.getCheckTries());
        }
        setup.setCheckSuccess(false);
        setup.setChoiceSuccess(false);
        originalModel.setCheckSetup(setup);

        VerticalTreeABEFExpExercise clearExercise = new VerticalTreeABEFExpExercise(originalModel, mainWindow);
        return clearExercise;
    }

    @Override
    public boolean isExerciseModified() {
        if (verticalTreeABEFExpView == null) return false;

        RichTextArea commentEditor = verticalTreeABEFExpView.getExerciseComment().getEditor();
        if (commentEditor != null && commentEditor.isModified()) {   exerciseModified = true;      }

        if (verticalTreeABEFExpView.getExplainDRTA() != null) {
            RichTextArea explanationEditor = verticalTreeABEFExpView.getExplainDRTA().getEditor();
            if (explanationEditor != null && explanationEditor.isModified()) exerciseModified = true;
        }

        if (verticalTreeABEFExpView.getRootLayout() == null || verticalTreeABEFExpView.getRootLayout().getMain_pane() == null) return exerciseModified;

        ObservableList<Node> nodes = verticalTreeABEFExpView.getRootLayout().getMain_pane().getChildren();
        for (Node node : nodes) {
            if (node instanceof ABEFExpTreeFormulaBox) {
                ABEFExpTreeFormulaBox treeBox = (ABEFExpTreeFormulaBox) node;
                if (treeBox.getFormulaBox().getRTA().isModified()) exerciseModified = true;

                if (treeBox.isAnnotation() && treeBox.getAnnotationField() != null) {
                    RichTextArea annRTA = treeBox.getAnnotationField().getRTA();
                    if (annRTA != null && annRTA.isModified()) exerciseModified = true;
                }
            }
            if (node instanceof ABEFExpMapFormulaBox) {
                ABEFExpMapFormulaBox mapBox = (ABEFExpMapFormulaBox) node;
                if (mapBox.getFormulaBox().getRTA().isModified()) exerciseModified = true;
            }
        }
        return exerciseModified;
    }

    @Override
    public void setExerciseModified(boolean modified) { exerciseModified = modified; }

    @Override
    public Node getFFViewNode() {return null;}

    @Override
    public Node getFFPrintNode(ExerciseModel originalModel) {return null;}

    @Override
    public Spinner getFFHeightSpinner() {return null;}

    @Override
    public Spinner getFFWidthSpinner() {return null;}

    @Override
    public ExerciseModel<VerticalTreeABEFExpModel> getExerciseModelFromView() {
        return (ExerciseModel) getVerticalTreeModelFromView();
    }

    private VerticalTreeABEFExpModel getVerticalTreeModelFromView() {
        VerticalTreeABEFExpModel model = new VerticalTreeABEFExpModel();

        model.setExerciseName(verticalTreeABEFExpModel.getExerciseName());
        model.setDefaultKeyboardType(verticalTreeABEFExpModel.getDefaultKeyboardType());
        model.setDefaultMapKeyboardType(verticalTreeABEFExpModel.getDefaultMapKeyboardType());
        model.setExplainPrompt(verticalTreeABEFExpModel.getExplainPrompt());
        model.setOriginalModel(verticalTreeABEFExpModel.getOriginalModel());

        model.setABChoiceLead(verticalTreeABEFExpModel.getABChoiceLead());
        model.setaPrompt(verticalTreeABEFExpModel.getaPrompt());
        model.setaSelected(verticalTreeABEFExpView.getaCheckBox().isSelected());
        model.setbPrompt(verticalTreeABEFExpModel.getbPrompt());
        model.setbSelected(verticalTreeABEFExpView.getbCheckBox().isSelected());
        model.setEFChoiceLead(verticalTreeABEFExpModel.getEFChoiceLead());
        model.setePrompt(verticalTreeABEFExpModel.getePrompt());
        model.seteSelected(verticalTreeABEFExpView.geteCheckBox().isSelected());
        model.setfPrompt(verticalTreeABEFExpModel.getfPrompt());
        model.setfSelected(verticalTreeABEFExpView.getfCheckBox().isSelected());
        model.setDragIconList(verticalTreeABEFExpModel.getDragIconList());
        model.setObjectControlList(verticalTreeABEFExpModel.getObjectControlList());
        model.setStarted(verticalTreeABEFExpModel.isStarted() || exerciseModified);

        VTcheckSetup checkSetup = verticalTreeABEFExpModel.getCheckSetup();
        if (vtABEFExpCheck != null) checkSetup.setCheckTries(vtABEFExpCheck.getCheckTries());
        if (vtABEFExpCheck != null) checkSetup.setCheckSuccess(vtABEFExpCheck.isCheckSuccess());
        if (vtABEFExpCheck != null && verticalTreeABEFExpView != null) checkSetup.setCheckMessage(verticalTreeABEFExpView.getCheckMessage());
        model.setCheckSetup(checkSetup);


        model.setStatementPrefHeight(verticalTreeABEFExpView.getExerciseStatement().getEditor().getPrefHeight());
        model.setCommentPrefHeight(verticalTreeABEFExpView.getCommentPrefHeight());
        model.setExplainPrefHeight(verticalTreeABEFExpView.getExplainPrefHeight());
        model.setStatementTextHeight(verticalTreeABEFExpModel.getStatementTextHeight());
        model.setExerciseStatement(verticalTreeABEFExpModel.getExerciseStatement());

        model.setMainPanePrefHeight(verticalTreeABEFExpView.getMainPanePrefHeight());
        model.setMainPanePrefWidth(verticalTreeABEFExpView.getRootLayout().getMain_pane().getWidth());

        RichTextArea commentRTA = verticalTreeABEFExpView.getExerciseComment().getEditor();
        commentRTA.getActionFactory().saveNow().execute(new ActionEvent());
        model.setExerciseComment(commentRTA.getDocument());
        double commentTextHeight = commentRTA.getFullHeight();
        model.setCommentTextHeight(commentTextHeight > 0 ? commentTextHeight : verticalTreeABEFExpModel.getCommentTextHeight());

        RichTextArea explainRTA = verticalTreeABEFExpView.getExplainDRTA().getEditor();
        explainRTA.getActionFactory().saveNow().execute(new ActionEvent());
        model.setExplainDocument(explainRTA.getDocument());
        double explainTextHeight = explainRTA.getFullHeight();
        model.setExplainTextHeight(explainTextHeight > 0 ? explainTextHeight : verticalTreeABEFExpModel.getExplainTextHeight());

        ABEFExpRootLayout rootLayout = verticalTreeABEFExpView.getRootLayout();
        AnchorPane mainPane = rootLayout.getMain_pane();
        ObservableList<Node> nodesList = mainPane.getChildren();

        for (Node node : nodesList) {

            if (node instanceof ABEFExpTreeFormulaBox) {
                ABEFExpTreeFormulaBox originalTreeBox = (ABEFExpTreeFormulaBox) node;
                TreeFormulaBoxMod newTreeMod = new TreeFormulaBoxMod();
                newTreeMod.setIdString(originalTreeBox.getIdString());
                newTreeMod.setLayoutX(originalTreeBox.getLayoutX());
                newTreeMod.setLayoutY(originalTreeBox.getLayoutY());
                newTreeMod.setLinkIdStrings(originalTreeBox.getmLinkIds());

                newTreeMod.setCircleIndexes(originalTreeBox.getCircleIndexes());
                newTreeMod.setUlineIndexes(originalTreeBox.getUlineIndexes());
                newTreeMod.setUlineInclusion(originalTreeBox.isUlineInclusion());

                BoxedDRTA treeFormulaBox = originalTreeBox.getFormulaBox();
                RichTextArea treeRTA = treeFormulaBox.getRTA();
                newTreeMod.setPrintWidth(treeRTA.getPrefWidth());

                treeRTA.getActionFactory().saveNow().execute(new ActionEvent());
                newTreeMod.setText(treeRTA.getDocument());

                newTreeMod.setBoxed(originalTreeBox.isBoxed());
                newTreeMod.setStarred(originalTreeBox.isStarred());
                newTreeMod.setAnnotation(originalTreeBox.isAnnotation());
                newTreeMod.setAnnotationDoc(originalTreeBox.getAnnotationDoc());
                newTreeMod.setCircled(originalTreeBox.isCircled());
                newTreeMod.setCircleXAnchors(originalTreeBox.getCircleXAnchors());
                newTreeMod.setRtaBoundsHeight(originalTreeBox.getRtaBoundsHeight());
                newTreeMod.setRtaBoundsMinY(originalTreeBox.getRtaBoundsMinY());

                ObservableList<Node> linesList = originalTreeBox.getLinesPane().getChildren();
                for (Node lineNode : linesList) {
                    if (lineNode instanceof Line) {
                        Line line = (Line) lineNode;
                        AnchorPane linesPane = originalTreeBox.getLinesPane();
                        UnderlineMod underlineModel = new UnderlineMod(linesPane.getLeftAnchor(line), line.getEndX(), linesPane.getBottomAnchor(line)  );
                        newTreeMod.getUnderlineList().add(underlineModel);
                    }
                }
                newTreeMod.setBaseline(originalTreeBox.getBaseline());
                model.getTreeFormulaBoxes().add(newTreeMod);

            } else if (node instanceof ABEFExpMapFormulaBox) {
                ABEFExpMapFormulaBox originalMapBox = (ABEFExpMapFormulaBox) node;
                MapFormulaBoxMod newMapMod = new MapFormulaBoxMod();
                newMapMod.setIdString(originalMapBox.getIdString());
                newMapMod.setLayoutX(originalMapBox.getLayoutX());
                newMapMod.setLayoutY(originalMapBox.getLayoutY());
                newMapMod.setLinkIdStrings(originalMapBox.getmLinkIds());

                BoxedDRTA formulaBox = originalMapBox.getFormulaBox();
                RichTextArea mapRTA = formulaBox.getRTA();
                newMapMod.setPrintWidth(mapRTA.getPrefWidth());

                mapRTA.getActionFactory().saveNow().execute(new ActionEvent());
                newMapMod.setText(mapRTA.getDocument());

                model.getMapFormulaBoxes().add(newMapMod);

            } else if (node instanceof ABEFExpVerticalBracket) {
                ABEFExpVerticalBracket vBrack = (ABEFExpVerticalBracket) node;
                VerticalBracketMod brack = new VerticalBracketMod(node.getLayoutX(), node.getLayoutY(), vBrack.getMainPane().getPrefHeight());
                model.getVerticalBrackets().add(brack);

            } else if (node instanceof ABEFExpDashedLine) {
                ABEFExpDashedLine dLine = (ABEFExpDashedLine) node;
                DashedLineMod dlMod = new DashedLineMod(node.getLayoutX(), node.getLayoutY(), dLine.getMainPane().getPrefWidth());
                model.getDashedLineMods().add(dlMod);

            } else if (node instanceof ABEFExpClickableNodeLink) {
                ABEFExpClickableNodeLink cLink = (ABEFExpClickableNodeLink) node;
                ClickableNodeLinkMod cMod = new ClickableNodeLinkMod(cLink.getIdString(), cLink.getTargetId(), cLink.getSourceId());
                model.getClickableNodeLinks().add(cMod);

            } else if (node instanceof ABEFExpClickableMapLink) {
                ABEFExpClickableMapLink mapLink = (ABEFExpClickableMapLink) node;
                ClickableMapLinkMod mapLinkMod = new ClickableMapLinkMod();
                mapLinkMod.setIdString(mapLink.getIdString());
                mapLinkMod.setSourceId(mapLink.getSourceId()); mapLinkMod.setTargetId(mapLink.getTargetId());
                mapLinkMod.setSourceMapStage(mapLink.getSourceMapStage()); mapLinkMod.setTargetMapStage(mapLink.getTargetMapStage());
                mapLinkMod.setSourceXAnchors(mapLink.getSourceXAnchors()); mapLinkMod.setTargetXAnchors(mapLink.getTargetXAnchors());
                model.getClickableMapLinks().add(mapLinkMod);

            } else if (node instanceof ABEFExpMapQuestionMarker) {
                ABEFExpMapQuestionMarker mapQuest = (ABEFExpMapQuestionMarker) node;
                MapQuestionMarkerMod qMod = new MapQuestionMarkerMod(mapQuest.getIdString(), mapQuest.getTargetId(), mapQuest.getTargetMapStage(), mapQuest.getTargetXAnchors());
                model.getMapQuestionMarkers().add(qMod);
            }
        }

        model.setPointsPossible(verticalTreeABEFExpModel.getPointsPossible());
        TextField pointsField = verticalTreeABEFExpView.getPointsEarnedTextField();
        if ((pointsField.isFocused() || verticalTreeABEFExpModel.getPointsEarned() >= 0) && pointsField.getText().isEmpty()) {
            pointsField.setText("0");
        }
        if (!pointsField.getText().equals("")) model.setPointsEarned(Integer.parseInt(pointsField.getText()));
        else model.setPointsEarned(-1);

        return model;
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

    // SCORCHED EARTH RULE 5: Dozens of isolated try-catch blocks
    @Override
    public void cleanup() {
        // KILL SWITCH: Engaged! Prevent any focus listeners from doing UI operations mid-teardown
        isTornDown = true;

        // SCORCHED EARTH RULE 2: Clear global MainView references (safe method only to avoid Toolbar Null-Poisoning)
        try {
            if (mainView != null) {
                mainView.textFieldInFocus(); // Remove toolbars for RTAs safely
            }
        } catch (Exception e) {}

        // 1. Unbind scaling properties defensively
        try { if (undoRedoFlag != null && undoRedoFlagListener != null) undoRedoFlag.removeListener(undoRedoFlagListener); } catch (Exception e) {}
        try { if (undoRedoFlag != null) undoRedoFlag.unbind(); } catch (Exception e) {}

        try {
            if (verticalTreeABEFExpView != null && verticalTreeABEFExpView.localScalePageHeightProperty() != null) {
                verticalTreeABEFExpView.localScalePageHeightProperty().unbind();
            }
        } catch (Exception e) {}
        try {
            if (verticalTreeABEFExpView != null && verticalTreeABEFExpView.localScalePageWidthProperty() != null) {
                verticalTreeABEFExpView.localScalePageWidthProperty().unbind();
            }
        } catch (Exception e) {}
        try {
            if (mainView != null && mainView.getMainScene() != null && verticalTreeABEFExpView != null && verticalTreeABEFExpView.getExerciseFocusListener() != null) {
                mainView.getMainScene().focusOwnerProperty().removeListener(verticalTreeABEFExpView.getExerciseFocusListener());
            }
        } catch (Exception e) {}

        // Disconnect specific property listeners
        try {
            if (verticalTreeABEFExpView != null && verticalTreeABEFExpView.getPointsEarnedTextField() != null && pointsEarnedListener != null) {
                verticalTreeABEFExpView.getPointsEarnedTextField().textProperty().removeListener(pointsEarnedListener);
            }
        } catch (Exception e) {}

        try {
            if (verticalTreeABEFExpView != null && verticalTreeABEFExpView.getExerciseStatement() != null && verticalTreeABEFExpView.getExerciseStatement().getEditor() != null && statementFocusListener != null) {
                verticalTreeABEFExpView.getExerciseStatement().getEditor().focusedProperty().removeListener(statementFocusListener);
            }
        } catch (Exception e) {}

        try {
            if (verticalTreeABEFExpView != null && verticalTreeABEFExpView.getExerciseComment() != null && verticalTreeABEFExpView.getExerciseComment().getEditor() != null) {
                if (commentFullHeightListener != null) verticalTreeABEFExpView.getExerciseComment().getEditor().fullHeightProperty().removeListener(commentFullHeightListener);
                if (commentModifiedListener != null) verticalTreeABEFExpView.getExerciseComment().getEditor().modifiedProperty().removeListener(commentModifiedListener);
                if (commentFocusListener != null) verticalTreeABEFExpView.getExerciseComment().getEditor().focusedProperty().removeListener(commentFocusListener);
            }
        } catch (Exception e) {}

        try {
            if (verticalTreeABEFExpView != null && verticalTreeABEFExpView.getExplainDRTA() != null && verticalTreeABEFExpView.getExplainDRTA().getEditor() != null) {
                if (explainFullHeightListener != null) verticalTreeABEFExpView.getExplainDRTA().getEditor().fullHeightProperty().removeListener(explainFullHeightListener);
                if (explainModifiedListener != null) verticalTreeABEFExpView.getExplainDRTA().getEditor().modifiedProperty().removeListener(explainModifiedListener);
                if (explainFocusListener != null) verticalTreeABEFExpView.getExplainDRTA().getEditor().focusedProperty().removeListener(explainFocusListener);
            }
        } catch (Exception e) {}

        try {
            if (verticalTreeABEFExpView != null) {
                if (verticalTreeABEFExpView.getaCheckBox() != null && aCheckBoxListener != null) verticalTreeABEFExpView.getaCheckBox().selectedProperty().removeListener(aCheckBoxListener);
                if (verticalTreeABEFExpView.getbCheckBox() != null && bCheckBoxListener != null) verticalTreeABEFExpView.getbCheckBox().selectedProperty().removeListener(bCheckBoxListener);
                if (verticalTreeABEFExpView.geteCheckBox() != null && eCheckBoxListener != null) verticalTreeABEFExpView.geteCheckBox().selectedProperty().removeListener(eCheckBoxListener);
                if (verticalTreeABEFExpView.getfCheckBox() != null && fCheckBoxListener != null) verticalTreeABEFExpView.getfCheckBox().selectedProperty().removeListener(fCheckBoxListener);
            }
        } catch (Exception e) {}

        // ---> SCORCHED EARTH RULE 4: THE JAVAFX SKIN HAMMER (DEFERRED TO VIEW) <---
        try {
            // Skin hammer operations completely deferred to the View
        } catch (Exception e) {}

        // Destroy sub-components embedded in AnchorPanes
        try {
            if (verticalTreeABEFExpView != null && verticalTreeABEFExpView.getRootLayout() != null && verticalTreeABEFExpView.getRootLayout().getMain_pane() != null) {
                AnchorPane mainPane = verticalTreeABEFExpView.getRootLayout().getMain_pane();
                if (mainPane.getChildren() != null) {
                    List<Node> childrenCopy = new ArrayList<>(mainPane.getChildren());
                    for (Node node : childrenCopy) {
                        try { node.getClass().getMethod("cleanup").invoke(node); } catch (Exception e) {}
                        severFromParent(node);
                    }
                }
                mainPane.getChildren().clear();
            }
        } catch (Exception e) {}

        // ---> 4. DISMANTLE THE UI TREE <---
        try {
            if (verticalTreeABEFExpView != null) {
                if (verticalTreeABEFExpView.getControlBox() != null) {
                    severFromParent(verticalTreeABEFExpView.getControlBox());
                    verticalTreeABEFExpView.getControlBox().getChildren().clear();
                }
                if (verticalTreeABEFExpView.getUndoButton() != null) {
                    if (undoButtonHandler != null) verticalTreeABEFExpView.getUndoButton().removeEventHandler(ActionEvent.ACTION, undoButtonHandler);
                    verticalTreeABEFExpView.getUndoButton().setOnAction(null);
                }
                if (verticalTreeABEFExpView.getRedoButton() != null) {
                    if (redoButtonHandler != null) verticalTreeABEFExpView.getRedoButton().removeEventHandler(ActionEvent.ACTION, redoButtonHandler);
                    verticalTreeABEFExpView.getRedoButton().setOnAction(null);
                }
                try { if (verticalTreeABEFExpView.getCheckButton() != null) verticalTreeABEFExpView.getCheckButton().setOnAction(null); } catch (Exception e) {}
                try { if (verticalTreeABEFExpView.getCheckProgButton() != null) verticalTreeABEFExpView.getCheckProgButton().setOnAction(null); } catch (Exception e) {}
                try { if (verticalTreeABEFExpView.getstaticHelpButton() != null) verticalTreeABEFExpView.getstaticHelpButton().setOnAction(null); } catch (Exception e) {}
            }
        } catch (Exception e) {}

        try {
            if (thumbPane != null) {
                if (thumbPaneClickHandler != null) thumbPane.removeEventHandler(MouseEvent.MOUSE_CLICKED, thumbPaneClickHandler);
                thumbPane.setOnMouseClicked(null);
                thumbPane.getChildren().clear();
                severFromParent(thumbPane);
            }
        } catch (Exception e) {}

        // SCORCHED EARTH RULE 10: NON-DESTRUCTIVE Data Teardown
        try {
            if (undoRedoList != null) {
                undoRedoList = null;
            }
        } catch (Exception e) {}

        // 5. Clear OS-level popups
        try { clearStandingPopups(); } catch (Exception e) {}

        // 6. Cascade cleanup to auxiliary exercises
        try { if (auxExerciseA != null) auxExerciseA.cleanup(); } catch (Exception e) {}
        try { if (auxExerciseB != null) auxExerciseB.cleanup(); } catch (Exception e) {}

        // 7. Tell View & Check to specifically clean up (ensure their cleanup is similarly robust)
        try { if (verticalTreeABEFExpView != null) verticalTreeABEFExpView.cleanup(); } catch (Exception e) {}
        try { if (vtABEFExpCheck != null) vtABEFExpCheck.cleanup(); } catch (Exception e) {}

        // 8. Final severed ties
        try { this.verticalTreeABEFExpModel = null; } catch (Exception e) {}
        try { this.mainWindow = null; } catch (Exception e) {}
        try { this.mainView = null; } catch (Exception e) {}
        try { this.vtABEFExpCheck = null; } catch (Exception e) {}
        try { this.auxExerciseA = null; } catch (Exception e) {}
        try { this.auxExerciseB = null; } catch (Exception e) {}
        try { this.verticalTreeABEFExpView = null; } catch (Exception e) {}
        try { this.tScene = null; } catch (Exception e) {}
        try { this.tStage = null; } catch (Exception e) {}
        try { this.thumbPane = null; } catch (Exception e) {}

        try { this.undoRedoFlagListener = null; } catch (Exception e) {}
        try { this.pointsEarnedListener = null; } catch (Exception e) {}
        try { this.statementFocusListener = null; } catch (Exception e) {}
        try { this.commentFullHeightListener = null; } catch (Exception e) {}
        try { this.commentModifiedListener = null; } catch (Exception e) {}
        try { this.commentFocusListener = null; } catch (Exception e) {}
        try { this.aCheckBoxListener = null; } catch (Exception e) {}
        try { this.bCheckBoxListener = null; } catch (Exception e) {}
        try { this.eCheckBoxListener = null; } catch (Exception e) {}
        try { this.fCheckBoxListener = null; } catch (Exception e) {}
        try { this.explainFullHeightListener = null; } catch (Exception e) {}
        try { this.explainModifiedListener = null; } catch (Exception e) {}
        try { this.explainFocusListener = null; } catch (Exception e) {}
        try { this.undoButtonHandler = null; } catch (Exception e) {}
        try { this.redoButtonHandler = null; } catch (Exception e) {}
        try { this.thumbPaneClickHandler = null; } catch (Exception e) {}
        try { this.undoRedoFlag = null; } catch (Exception e) {}
    }


    @Override
    public String getAuxExerName() {
        if (verticalTreeABEFExpModel == null || verticalTreeABEFExpModel.getCheckSetup() == null) return "";
        return getExerciseModel().getCheckSetup().getAuxExerName();
    }

    @Override
    public Node getAuxExerNode() {
        if (verticalTreeABEFExpView == null || verticalTreeABEFExpView.getRootLayout() == null || verticalTreeABEFExpModel == null) return new Pane();
        RichTextArea explainRTA = null;
        if (verticalTreeABEFExpView.getExplainDRTA() != null) explainRTA = verticalTreeABEFExpView.getExplainDRTA().getEditor();
        if (explainRTA != null) {
            explainRTA.prefHeightProperty().unbind();
            explainRTA.setPrefHeight(verticalTreeABEFExpModel.getExplainTextHeight() + RTA_SCREEN_BMP);
        }

        return new VBox(5, verticalTreeABEFExpView.getRootLayout().getMain_pane(), verticalTreeABEFExpView.getChoicesBox(), (explainRTA != null ? explainRTA : new Pane()));
    }

    @Override
    public void setAuxExerA(Exercise exer) {
        auxExerciseA = exer;
    }

    @Override
    public void setAuxExerB(Exercise exer) {
        auxExerciseB = exer;
    }

    @Override
    public void clearStandingPopups() {
        try {
            if (verticalTreeABEFExpView != null && verticalTreeABEFExpView.getStaticHelpStage() != null) verticalTreeABEFExpView.getStaticHelpStage().close();
        } catch (Exception e) {}
        if (tStage != null) tStage.close();
    }

    @Override
    public AnchorPane getMainPane() {
        if (verticalTreeABEFExpView == null || verticalTreeABEFExpView.getRootLayout() == null) return new AnchorPane();
        return verticalTreeABEFExpView.getRootLayout().getMain_pane();
    }

    @Override
    public VTAuxCheck getVTAuxCheck() {
        return (VTAuxCheck) vtABEFExpCheck;
    }

    @Override
    public VTAuxExer getVTAuxExer() {
        if (auxExerciseA instanceof VTAuxExer) return (VTAuxExer) auxExerciseA;
        else return null;
    }

}
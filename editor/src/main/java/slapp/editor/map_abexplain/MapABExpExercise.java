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

package slapp.editor.map_abexplain;

import com.gluonhq.richtextarea.RichTextArea;
import com.gluonhq.richtextarea.RichTextAreaSkin;
import com.gluonhq.richtextarea.model.Document;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
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
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import org.apache.commons.lang3.SerializationUtils;
import slapp.editor.DiskUtilities;
import slapp.editor.EditorMain;
import slapp.editor.PrintUtilities;
import slapp.editor.decorated_rta.BoxedDRTA;
import slapp.editor.decorated_rta.DecoratedRTA;
import slapp.editor.main_window.*;
import slapp.editor.vertical_tree.VTAuxCheck;
import slapp.editor.vertical_tree.VTAuxExer;
import slapp.editor.vertical_tree.drag_drop.DragIconType;
import slapp.editor.vertical_tree.object_models.*;
import java.util.ArrayList;
import java.util.List;
import static slapp.editor.EditorMain.*;
import static slapp.editor.EditorMain.RTA_WIDTH_BMP;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.event.EventHandler;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
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

import java.util.ArrayList;
import java.util.List;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
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

import java.util.ArrayList;
import java.util.List;

public class MapABExpExercise implements Exercise<MapABExpModel, MapABExpView>, VTAuxExer {

    MainWindow mainWindow;
    MainWindowView mainView;
    MapABExpModel mapModel;
    MapABExpView mapView;
    private MapABExpCheck mapCheck;
    private boolean exerciseModified = false;
    private UndoRedoList<MapABExpModel> undoRedoList = new UndoRedoList<>(50);
    public BooleanProperty undoRedoFlag = new SimpleBooleanProperty();
    private Exercise auxExerciseA;
    private Exercise auxExerciseB;
    Stage tStage;
    Pane thumbPane = new Pane();
    Scene tScene = new Scene(new Pane());

    // SCORCHED EARTH RULE 2: The Asynchronous Ghost "Kill Switch"
    private boolean isTornDown = false;
    private boolean isRebuilding = false;
    private boolean isUndoQueued = false;

    // SCORCHED EARTH RULE 3: Eradicate Anonymous Lambdas
    private ChangeListener<Boolean> undoRedoFlagListener;
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
    private EventHandler<ActionEvent> undoButtonHandler;
    private EventHandler<ActionEvent> redoButtonHandler;
    private EventHandler<MouseEvent> thumbPaneClickHandler;

    public MapABExpExercise(MapABExpModel model, MainWindow mainWindow) {
        this.mainWindow = mainWindow;
        this.mapModel = model;
        if (model.getOriginalModel() == null) {model.setOriginalModel(model); }
        this.mainView = mainWindow.getMainView();
        this.mapView = new MapABExpView(mainView, this);
        setMapView();

        mapCheck = new MapABExpCheck(this);
        undoRedoList = new UndoRedoList<>(50);

        undoRedoFlag.set(false);
        undoRedoFlag.bind(mapView.undoRedoFlagProperty());

        undoRedoFlagListener = (ob, ov, nv) -> {
            if (isTornDown || isRebuilding || mainView == null) return;
            try {
                if (nv != null && nv) {
                    exerciseModified = true;
                    pushUndoRedo();
                }
            } catch (Exception e) {}
        };
        undoRedoFlag.addListener(undoRedoFlagListener);

        MapABExpModel deepCopy = (MapABExpModel) SerializationUtils.clone(mapModel);
        undoRedoList.push(deepCopy);
        updateUndoRedoButtons();

        Platform.runLater(() -> {
            if (isTornDown || mainView == null) return;
            exerciseModified = false;
        });
    }

    private void setMapView() {
        if (isTornDown || mapView == null || mapModel == null) return;

        mapView.setExplainPrompt(mapModel.getExplainPrompt());
        mapView.setDefaultKeyboard(mapModel.getDefaultKeyboardType());
        mapView.setStatementPrefHeight(mapModel.getStatementPrefHeight());
        mapView.setCommentPrefHeight(mapModel.getCommentPrefHeight());
        mapView.setExplainPrefHeight(mapModel.getExplainPrefHeight());
        mapView.setMainPanePrefHeight(mapModel.getMainPanePrefHeight());
        mapView.setMainPanePrefWidth(mapModel.getMainPanePrefWidth());

        mapView.setPointsPossible(mapModel.getPointsPossible());
        if (mapModel.getPointsEarned() >= 0) mapView.getPointsEarnedTextField().setText(Integer.toString(mapModel.getPointsEarned()));

        pointsEarnedListener = (ob, ov, nv) -> {
            if (isTornDown || mainView == null) return;
            exerciseModified = true;
        };
        mapView.getPointsEarnedTextField().textProperty().addListener(pointsEarnedListener);

        //Statement
        DecoratedRTA statementDRTA = new DecoratedRTA();
        RichTextArea statementEditor = statementDRTA.getEditor();
        statementEditor.getActionFactory().open(mapModel.getExerciseStatement()).execute(new ActionEvent());

        mainView.editorInFocus(statementDRTA, ControlType.STATEMENT);

        statementFocusListener = (o, ov, nv) -> {
            if (isTornDown || mainView == null) return;
            try {
                if (nv != null && nv) {
                    mainView.editorInFocus(statementDRTA, ControlType.STATEMENT);
                    mainView.setRTATextHeight(statementEditor.getFullHeight());
                } else mainView.undoRTATextHeight();
            } catch (Exception e) {}
        };
        statementEditor.focusedProperty().addListener(statementFocusListener);
        mapView.setExerciseStatement(statementDRTA);

        //comment
        DecoratedRTA commentDRTA = new DecoratedRTA();
        RichTextArea commentEditor = commentDRTA.getEditor();
        commentEditor.setPromptText("Comment: ");
        commentEditor.getActionFactory().open(mapModel.getExerciseComment()).execute(new ActionEvent());
        commentEditor.getActionFactory().saveNow().execute(new ActionEvent());

        final boolean[] commentPrimed = {false};
        commentFullHeightListener = (ob, ov, nv) -> {
            if (isTornDown || mainView == null || mapModel == null) return;
            if (!commentPrimed[0]) {
                commentPrimed[0] = true;
                return;
            }
            double height = (double) nv;
            mainView.setRTATextHeight(height);
            mapModel.setCommentTextHeight(height);
        };
        commentEditor.fullHeightProperty().addListener(commentFullHeightListener);

        commentModifiedListener = (ob, ov, nv) -> {
            if (isTornDown || mainView == null) return;
            if (nv != null && nv) exerciseModified = true;
        };
        commentEditor.modifiedProperty().addListener(commentModifiedListener);

        mainView.editorInFocus(commentDRTA, ControlType.AREA);
        commentFocusListener = (o, ov, nv) -> {
            if (isTornDown || mainView == null) return;
            try {
                if (nv != null && nv) {
                    mainView.editorInFocus(commentDRTA, ControlType.AREA);
                    mainView.setRTATextHeight(commentEditor.getFullHeight());
                } else mainView.undoRTATextHeight();
            } catch (Exception e) {}
        };
        commentEditor.focusedProperty().addListener(commentFocusListener);
        mapView.setExerciseComment(commentDRTA);

        //choices
        mapView.getChoiceLeadLabel().setText(mapModel.getChoiceLead());
        CheckBox aCheckBox = mapView.getaCheckBox();
        CheckBox bCheckBox = mapView.getbCheckBox();
        aCheckBox.setText(mapModel.getaPrompt());
        aCheckBox.setSelected(mapModel.isaSelected());

        aCheckBoxListener = (ObservableValue<? extends Boolean> ob, Boolean ov, Boolean nv) -> {
            if (isTornDown || mainView == null) return;
            try {
                if (nv != null && nv) bCheckBox.setSelected(false);
                exerciseModified = true;
            } catch (Exception e) {}
        };
        aCheckBox.selectedProperty().addListener(aCheckBoxListener);

        bCheckBox.setText(mapModel.getbPrompt());
        bCheckBox.setSelected(mapModel.isbSelected());

        bCheckBoxListener = (ObservableValue<? extends Boolean> ob, Boolean ov, Boolean nv) -> {
            if (isTornDown || mainView == null) return;
            try {
                if (nv != null && nv) aCheckBox.setSelected(false);
                exerciseModified = true;
            } catch (Exception e) {}
        };
        bCheckBox.selectedProperty().addListener(bCheckBoxListener);

        //explain
        DecoratedRTA explainDRTA = mapView.getExplainDRTA();
        RichTextArea explainEditor = explainDRTA.getEditor();
        explainEditor.getActionFactory().open(mapModel.getExplainDocument()).execute(new ActionEvent());
        explainEditor.getActionFactory().saveNow().execute(new ActionEvent());
        explainEditor.setPromptText(mapModel.getExplainPrompt());

        final boolean[] explainPrimed = {false};
        explainFullHeightListener = (ob, ov, nv) -> {
            if (isTornDown || mainView == null || mapModel == null) return;
            if (!explainPrimed[0]) {
                explainPrimed[0] = true;
                return;
            }
            double height = (double) nv;
            mainView.setRTATextHeight(height);
            mapModel.setExplainTextHeight(height);
        };
        explainEditor.fullHeightProperty().addListener(explainFullHeightListener);

        explainModifiedListener = (ob, ov, nv) -> {
            if (isTornDown || mainView == null) return;
            if (nv != null && nv) exerciseModified = true;
        };
        explainEditor.modifiedProperty().addListener(explainModifiedListener);

        mainView.editorInFocus(explainDRTA, ControlType.AREA);
        explainFocusListener = (o, ov, nv) -> {
            if (isTornDown || mainView == null) return;
            try {
                if (nv != null && nv) {
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
        if (mapView.getUndoButton() != null) mapView.getUndoButton().setOnAction(undoButtonHandler);

        redoButtonHandler = e -> {
            if (isTornDown || mainView == null) return;
            redoAction();
        };
        if (mapView.getRedoButton() != null) mapView.getRedoButton().setOnAction(redoButtonHandler);

        for (DragIconType type : mapModel.getDragIconList()) {
            mapView.getRootLayout().addDragIcon(type);
        }
        populateMainPaneNodes();
        mapView.initializeViewDetails();
        mapView.setRightControlBox();

        Platform.runLater(() -> {
            if (isTornDown || mainView == null) return;
            if (mainWindow != null && mainWindow.getCurrentExercise() != this) return;
            try {
                if (auxExerciseA != null && mapView != null && mapView.getControlBox() != null) {
                    if (thumbPane != null) {
                        if (thumbPaneClickHandler != null) thumbPane.removeEventHandler(MouseEvent.MOUSE_CLICKED, thumbPaneClickHandler);
                        thumbPane.setOnMouseClicked(null);
                        severFromParent(thumbPane);
                        mapView.getControlBox().getChildren().remove(thumbPane);
                    }

                    thumbPane = auxImage();

                    if (!mapView.getControlBox().getChildren().contains(thumbPane)) {
                        mapView.getControlBox().getChildren().add(thumbPane);
                    }
                }
            } catch (Exception e) {}
        });
    }

    private void populateMainPaneNodes() {
        if (isTornDown || mapView == null || mapView.getRootLayout() == null) return;

        isRebuilding = true;
        try {
            AnchorPane mainPane = mapView.getRootLayout().getMain_pane();

            // SCORCHED EARTH RULE 8: The Undo/Redo Rebuild Fix (Prevent CME)
            if (mainPane.getChildren() != null) {
                List<Node> childrenCopy = new ArrayList<>(mainPane.getChildren());
                for (Node node : childrenCopy) {
                    try { node.getClass().getMethod("cleanup").invoke(node); } catch (Exception e) {}
                    severFromParent(node);
                }
            }
            mainPane.getChildren().clear();

            for (VerticalBracketMod bracketMod : mapModel.getVerticalBrackets()) {
                MapABExpVerticalBracket bracket = new MapABExpVerticalBracket(mapView);
                mainPane.getChildren().add(bracket);
                bracket.setLayoutX(bracketMod.getLayoutX());
                bracket.setLayoutY(bracketMod.getLayoutY());
                bracket.getMainPane().setPrefHeight(bracketMod.getHeight());
            }

            for (DashedLineMod dlMod : mapModel.getDashedLineMods()) {
                MapABExpDashedLine dashedLine = new MapABExpDashedLine(mapView);
                mainPane.getChildren().add(dashedLine);
                dashedLine.setLayoutX(dlMod.getLayoutX());
                dashedLine.setLayoutY(dlMod.getLayoutY());
                dashedLine.getMainPane().setPrefWidth(dlMod.getWidth());
            }

            for (MapFormulaBoxMod mapBoxMod : mapModel.getMapFormulaBoxes()) {
                MapABExpFormulaBox mapFormulaBox = new MapABExpFormulaBox(mapView);
                mainPane.getChildren().add(mapFormulaBox);
                mapFormulaBox.setLayoutX(mapBoxMod.getLayoutX());
                mapFormulaBox.setLayoutY(mapBoxMod.getLayoutY());
                mapFormulaBox.setIdString(mapBoxMod.getIdString());
                mapFormulaBox.setmLinkIds(mapBoxMod.getLinkIdStrings());
                mapFormulaBox.setPrintWidth(mapBoxMod.getPrintWidth());
                mapFormulaBox.setMapIndexItems(mapBoxMod.getMapIndexItems());
                mapFormulaBox.setMetaBox(mapBoxMod.isMetaBox());

                if (mapBoxMod.isMetaBox()) {
                    mapFormulaBox.getFormulaBox().getDRTA().getKeyboardSelector().valueProperty().setValue(RichTextAreaSkin.KeyMapValue.SCRIPT_AND_SANS);
                } else {
                    mapFormulaBox.getFormulaBox().getDRTA().getKeyboardSelector().valueProperty().setValue(RichTextAreaSkin.KeyMapValue.ITALIC_AND_SANS);
                }

                BoxedDRTA formulaBox = mapFormulaBox.getFormulaBox();
                RichTextArea mapBoxRTA = formulaBox.getRTA();
                mapBoxRTA.getActionFactory().open(mapBoxMod.getText()).execute(new ActionEvent());
                mapBoxRTA.getActionFactory().saveNow().execute(new ActionEvent());
            }

            for (TreeFormulaBoxMod treeBoxMod : mapModel.getTreeFormulaBoxes()) {
                MapABExpTreeFormulaBox treeFormulaBox = new MapABExpTreeFormulaBox(mapView);
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
            for (ClickableNodeLinkMod nodeLinkMod : mapModel.getClickableNodeLinks()) {
                MapABExpClickableNodeLink nodeLink = new MapABExpClickableNodeLink(mapView);
                mainPane.getChildren().add(nodeLink);
                nodeLink.setId(nodeLinkMod.getIdString());
                MapABExpTreeFormulaBox source = null;
                MapABExpTreeFormulaBox target = null;
                for (Node node : nodesList) {
                    if (node instanceof MapABExpTreeFormulaBox) {
                        MapABExpTreeFormulaBox treeBox = (MapABExpTreeFormulaBox) node;
                        if (treeBox.getIdString().equals(nodeLinkMod.getSourceId())) source = treeBox;
                        if (treeBox.getIdString().equals(nodeLinkMod.getTargetId())) target = treeBox;
                    }
                }
                if (source != null && target != null) {nodeLink.bindEnds(source, target); }
            }

            for (MapQuestionMarkerMod mapQuestMod : mapModel.getMapQuestionMarkers()) {
                MapABExpQuestionMarker mapQuestion = new MapABExpQuestionMarker(mapView);
                mainPane.getChildren().add(mapQuestion);
                mapQuestion.setId(mapQuestMod.getIdString());

                for (Node node : nodesList) {
                    if (node instanceof MapABExpFormulaBox) {
                        MapABExpFormulaBox mapBox = (MapABExpFormulaBox) node;
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

            for (ClickableMapLinkMod mapLinkMod : mapModel.getClickableMapLinks()) {
                MapABExpClickableMapLink mapLink = new MapABExpClickableMapLink(mapView);
                mainPane.getChildren().add(0, mapLink);
                mapLink.setIdString(mapLinkMod.getIdString());

                MapABExpFormulaBox source = null;
                MapABExpFormulaBox target = null;
                for (Node node : nodesList) {
                    if (node instanceof MapABExpFormulaBox) {
                        MapABExpFormulaBox mapFormulaBox = (MapABExpFormulaBox) node;
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

    private Pane auxImage() {
        Pane newThumbPane = new Pane();
        if (auxExerciseA != null && auxExerciseA.getAuxExerNode() != null)  {
            try {
                newThumbPane.setStyle("-fx-background-color: FAFAFA;");
                Node auxExerNode = auxExerciseA.getAuxExerNode();

                severFromParent(auxExerNode);

                auxExerNode.setMouseTransparent(true);
                auxExerNode.setFocusTraversable(false);
                newThumbPane.getChildren().add(auxExerNode);

                Group thumbRoot = new Group();
                Scene scene = new Scene(thumbRoot);
                thumbRoot.getChildren().add(newThumbPane);
                thumbRoot.applyCss();
                thumbRoot.layout();

                double thumbWidth = newThumbPane.getLayoutBounds().getWidth();
                double scale = thumbWidth > 0 ? 85.0 / thumbWidth : .16;

                Rectangle clip = new Rectangle();
                clip.setWidth(800);
                clip.setHeight(400);
                newThumbPane.setClip(clip);

                newThumbPane.getTransforms().clear();
                newThumbPane.getTransforms().add(new Scale(scale, scale));

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
                  //      tStage.initOwner(EditorMain.mainStage);
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
                newThumbPane.setOnMouseClicked(thumbPaneClickHandler);
            } catch (Exception e) {}
        }
        return newThumbPane;
    }

    @Override
    public void setAuxExerA(Exercise exer) {
        this.auxExerciseA = exer;
    }

    @Override
    public void setAuxExerB(Exercise exer) {
        this.auxExerciseB = exer;
    }

    private void undoAction() {
        if (isTornDown || undoRedoList == null) return;
        try {
            MapABExpModel undoElement = undoRedoList.getUndoElement();
            if (undoElement != null) {
                mapModel = (MapABExpModel) SerializationUtils.clone(undoElement);
                populateMainPaneNodes();
                updateUndoRedoButtons();
            }
        } catch (Exception e) {}
    }

    private void redoAction() {
        if (isTornDown || undoRedoList == null) return;
        try {
            MapABExpModel redoElement = undoRedoList.getRedoElement();
            if (redoElement != null) {
                mapModel = (MapABExpModel) SerializationUtils.clone(redoElement);
                populateMainPaneNodes();
                updateUndoRedoButtons();
            }
        } catch (Exception e) {}
    }

    private void updateUndoRedoButtons() {
        if (isTornDown || mapView == null || undoRedoList == null) return;
        try {
            if (mapView.getUndoButton() != null) mapView.getUndoButton().setDisable(!undoRedoList.canUndo());
            if (mapView.getRedoButton() != null) mapView.getRedoButton().setDisable(!undoRedoList.canRedo());
        } catch (Exception e) {}
    }

    public void pushUndoRedo() {
        if (isTornDown || isRebuilding || undoRedoList == null || mapModel == null) return;
        exerciseModified = true;

        if (!isUndoQueued) {
            isUndoQueued = true;

            Platform.runLater(() -> {
                isUndoQueued = false;
                if (isTornDown || isRebuilding || undoRedoList == null || mapModel == null) return;

                MapABExpModel model = getMapModelFromView();
                MapABExpModel deepCopy = (MapABExpModel) SerializationUtils.clone(model);
                undoRedoList.push(deepCopy);
                updateUndoRedoButtons();
            });
        }
    }

    @Override
    public MapABExpModel getExerciseModel() {  return mapModel;    }

    @Override
    public MapABExpView getExerciseView() {  return mapView;    }

    @Override
    public void saveExercise(boolean saveAs) {
        if (isTornDown) return;
        try {
            boolean success = DiskUtilities.saveExercise(saveAs, getMapModelFromView());
            if (success) exerciseModified = false;
        } catch (Exception e) {}
    }

    @Override
    public List<Node> getPrintNodes(ExerciseModel originalModel) {
        if (isTornDown || mapModel == null || mapView == null) return new ArrayList<>();
        List<Node> nodeList = new ArrayList<>();
        try {
            MapABExpModel printModel = mapModel;
            MapABExpExercise printExercise = this;

            double nodeWidth = PrintUtilities.getPageWidth() / mainWindow.getBaseScale();

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

            AnchorPane mainPane = printExercise.getExerciseView().getRootLayout().getMain_pane();
            mainPane.setStyle("-fx-background-color: transparent");
            mainPane.setPadding(new Insets(0));
            mainPane.setMinHeight(0);

            MapABExpModel originalVTmod = (MapABExpModel) originalModel;
            mainPane.setMinWidth(originalVTmod.getMainPanePrefWidth() - RTA_WIDTH_BMP);

            ObservableList<Node> nodes = mainPane.getChildren();
            for (Node node : nodes) {
                if (node instanceof MapABExpTreeFormulaBox) {
                    MapABExpTreeFormulaBox treeBox = (MapABExpTreeFormulaBox) node;
                    treeBox.getFormulaBox().getRTA().setStyle("-fx-border-width: 0; -fx-border-color: transparent");
                    treeBox.getFormulaBox().getRTA().setMinWidth(treeBox.getPrintWidth());
                    if (treeBox.getAnnotationField() != null) treeBox.getAnnotationField().getRTA().setStyle("-fx-border-width: 0; -fx-border-color: transparent");
                }
                if (node instanceof MapABExpFormulaBox) {
                    MapABExpFormulaBox mapBox = (MapABExpFormulaBox) node;
                    mapBox.getFormulaBox().getRTA().setStyle("-fx-border-width: 0; -fx-border-color: transparent");
                    mapBox.getFormulaBox().getRTA().setMinWidth(mapBox.getPrintWidth());
                }
            }

            HBox contentHBox = new HBox(mainPane);
            contentHBox.setAlignment(Pos.CENTER);
            contentHBox.setPadding(new Insets(0,0,20, 0));
            nodeList.add(contentHBox);

            Label leaderLabel = new Label(mapModel.getChoiceLead());
            CheckBox boxA = new CheckBox(mapModel.getaPrompt());
            boxA.setSelected(mapModel.isaSelected());
            CheckBox boxB = new CheckBox(mapModel.getbPrompt());
            boxB.setSelected(mapModel.isbSelected());
            Font labelFont = new Font("Noto Serif Combo", 11);
            leaderLabel.setFont(labelFont); boxA.setFont(labelFont); boxB.setFont(labelFont);

            HBox abBox = new HBox(20);
            abBox.setPadding(new Insets(10,10,10,0));
            abBox.setStyle("-fx-border-width: 0; -fx-border-color: transparent;");
            abBox.getChildren().addAll(leaderLabel, boxA, boxB);
            nodeList.add(abBox);

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

    TextFlow printCheckNode() {
        TextFlow flow = new TextFlow();
        flow.setMaxWidth(200);
        try {
            if (mapCheck != null && mapCheck.isCheckSuccess()) {
                Text bigCheck = new Text("\ue89a");
                bigCheck.setFont(Font.font("Noto Serif Combo", 14));
                Text message = new Text("  " + mapView.getCheckMessage());
                message.setFont(Font.font("Noto Serif Combo", 11));

                if (mapCheck.isCheckFinal()) {
                    bigCheck.setFill(Color.LAWNGREEN);
                    message.setFill(Color.GREEN);
                } else {
                    bigCheck.setFill(Color.ORCHID);
                    message.setFill(Color.PURPLE);
                }
                flow.getChildren().addAll(bigCheck, message);
            }
        } catch (Exception e) {}
        return flow;
    }

    @Override
    public Exercise<MapABExpModel, MapABExpView> resetExercise() {
        if (isTornDown || mapView == null || mapView.getExerciseComment() == null) return null;
        try {
            RichTextArea commentRTA = mapView.getExerciseComment().getEditor();
            commentRTA.getActionFactory().saveNow().execute(new ActionEvent());
            Document commentDocument = commentRTA.getDocument();


            int pointsEarned = -1;
            TextField pointsField = mapView.getPointsEarnedTextField();
            if ((pointsField.isFocused() || mapModel.getPointsEarned() >= 0) && pointsField.getText().isEmpty()) {
                pointsField.setText("0");
            }
            if (!pointsField.getText().equals("")) pointsEarned = Integer.parseInt(pointsField.getText());


            MapABExpModel originalModel = (MapABExpModel) (mapModel.getOriginalModel());

            if (originalModel != null) {
                originalModel.setExerciseComment(commentDocument);
                double commentHeight = commentRTA.getFullHeight();
                originalModel.setCommentTextHeight(commentHeight > 0 ? commentHeight : mapModel.getCommentTextHeight());

                originalModel.setPointsEarned(pointsEarned);
                if (originalModel.getMapFormulaBoxes() != null) {
                    for (MapFormulaBoxMod formulaBoxMod : originalModel.getMapFormulaBoxes()) {
                        formulaBoxMod.setMapIndexItems(new ArrayList<>());
                    }
                }

                MapCheckSetup setup = originalModel.getCheckSetup();
                if (setup != null) {
                    if (mapCheck != null) {
                        setup.setCheckTries(mapCheck.getCheckTries());
                    }
                    setup.setCheckSuccess(false);
                    setup.setChoiceSuccess(false);
                }
                originalModel.setCheckSetup(setup);
            }
            return new MapABExpExercise(originalModel, mainWindow);
        } catch (Exception e) { return null; }
    }

    @Override
    public boolean isExerciseModified() {
        if (isTornDown || mapView == null) return false;
        try {
            if (mapView.getExerciseComment() != null) {
                RichTextArea commentEditor = mapView.getExerciseComment().getEditor();
                if (commentEditor != null && commentEditor.isModified()) exerciseModified = true;
            }

            if (mapView.getExplainDRTA() != null) {
                RichTextArea explanationEditor = mapView.getExplainDRTA().getEditor();
                if (explanationEditor != null && explanationEditor.isModified()) exerciseModified = true;
            }

            if (mapView.getRootLayout() == null || mapView.getRootLayout().getMain_pane() == null) return exerciseModified;
            ObservableList<Node> nodes = mapView.getRootLayout().getMain_pane().getChildren();
            for (Node node : nodes) {
                if (node instanceof MapABExpTreeFormulaBox) {
                    MapABExpTreeFormulaBox treeBox = (MapABExpTreeFormulaBox) node;
                    if (treeBox.getFormulaBox() != null && treeBox.getFormulaBox().getRTA() != null && treeBox.getFormulaBox().getRTA().isModified()) exerciseModified = true;
                }
                if (node instanceof MapABExpTreeFormulaBox) {
                    MapABExpTreeFormulaBox treeBox = (MapABExpTreeFormulaBox) node;
                    if (treeBox.getFormulaBox() != null && treeBox.getFormulaBox().getRTA() != null && treeBox.getFormulaBox().getRTA().isModified()) exerciseModified = true;
                    if (treeBox.isAnnotation() && treeBox.getAnnotationField() != null && treeBox.getAnnotationField().getRTA() != null && treeBox.getAnnotationField().getRTA().isModified()) exerciseModified = true;
                }
            }
        } catch (Exception e) {}
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
    public ExerciseModel<MapABExpModel> getExerciseModelFromView() {
        return (ExerciseModel) getMapModelFromView();
    }

    private MapABExpModel getMapModelFromView() {
        if (isTornDown || mapModel == null || mapView == null) return null;
        try {
            MapABExpModel model = new MapABExpModel();

            model.setExerciseName(mapModel.getExerciseName());

            // ---> THE FIX THAT RESOLVES THE MISSING AUX EXERCISE BUG <---
            model.setAuxExerName(mapModel.getAuxExerName());

            model.setDefaultKeyboardType(mapModel.getDefaultKeyboardType());
            model.setExplainPrompt(mapModel.getExplainPrompt());
            model.setOriginalModel(mapModel.getOriginalModel());

            model.setChoiceLead(mapModel.getChoiceLead());
            model.setaPrompt(mapModel.getaPrompt());
            if (mapView.getaCheckBox() != null) model.setaSelected(mapView.getaCheckBox().isSelected());
            model.setbPrompt(mapModel.getbPrompt());
            if (mapView.getbCheckBox() != null) model.setbSelected(mapView.getbCheckBox().isSelected());
            model.setDragIconList(mapModel.getDragIconList());
            model.setObjectControlList(mapModel.getObjectControlList());
            model.setStarted(mapModel.isStarted() || exerciseModified);

            MapCheckSetup checkSetup = mapModel.getCheckSetup();
            if (mapCheck != null) {
                checkSetup.setCheckTries(mapCheck.getCheckTries());
                checkSetup.setCheckSuccess(mapCheck.isCheckSuccess());
                checkSetup.setChoiceSuccess(mapCheck.isChoiceSuccess());
                checkSetup.setCheckFinal(mapCheck.isCheckFinal());
                checkSetup.setCheckMessage(mapView.getCheckMessage());
            }
            model.setCheckSetup(checkSetup);

            if (mapView.getExerciseStatement() != null && mapView.getExerciseStatement().getEditor() != null) {
                model.setStatementPrefHeight(mapView.getExerciseStatement().getEditor().getPrefHeight());
            }
            model.setCommentPrefHeight(mapView.getCommentPrefHeight());
            model.setExplainPrefHeight(mapView.getExplainPrefHeight());
            model.setStatementTextHeight(mapModel.getStatementTextHeight());

            model.setExerciseStatement(mapModel.getExerciseStatement());

            model.setMainPanePrefHeight(mapView.getMainPanePrefHeight());
            if (mapView.getRootLayout() != null && mapView.getRootLayout().getMain_pane() != null) {
                model.setMainPanePrefWidth(mapView.getRootLayout().getMain_pane().getWidth());
            }

            if (mapView.getExerciseComment() != null && mapView.getExerciseComment().getEditor() != null) {
                RichTextArea commentRTA = mapView.getExerciseComment().getEditor();
                commentRTA.getActionFactory().saveNow().execute(new ActionEvent());
                model.setExerciseComment(commentRTA.getDocument());

                double commentTextHeight = commentRTA.getFullHeight();
                model.setCommentTextHeight(commentTextHeight > 0 ? commentTextHeight : mapModel.getCommentTextHeight());
            }

            if (mapView.getExplainDRTA() != null && mapView.getExplainDRTA().getEditor() != null) {
                RichTextArea explainRTA = mapView.getExplainDRTA().getEditor();
                explainRTA.getActionFactory().saveNow().execute(new ActionEvent());
                model.setExplainDocument(explainRTA.getDocument());

                double explainTextHeight = explainRTA.getFullHeight();
                model.setExplainTextHeight(explainTextHeight > 0 ? explainTextHeight : mapModel.getExplainTextHeight());
            }

            if (mapView.getRootLayout() != null && mapView.getRootLayout().getMain_pane() != null) {
                MapABExpRootLayout rootLayout = mapView.getRootLayout();
                AnchorPane mainPane = rootLayout.getMain_pane();
                ObservableList<Node> nodesList = mainPane.getChildren();

                for (Node node : nodesList) {
                    if (node instanceof MapABExpTreeFormulaBox) {
                        MapABExpTreeFormulaBox originalTreeBox = (MapABExpTreeFormulaBox) node;
                        TreeFormulaBoxMod newTreeMod = new TreeFormulaBoxMod();
                        newTreeMod.setIdString(originalTreeBox.getIdString());
                        newTreeMod.setLayoutX(originalTreeBox.getLayoutX());
                        newTreeMod.setLayoutY(originalTreeBox.getLayoutY());
                        newTreeMod.setLinkIdStrings(originalTreeBox.getmLinkIds());
                        newTreeMod.setCircleIndexes(originalTreeBox.getCircleIndexes());
                        newTreeMod.setUlineIndexes(originalTreeBox.getUlineIndexes());
                        newTreeMod.setUlineInclusion(originalTreeBox.isUlineInclusion());

                        if (originalTreeBox.getFormulaBox() != null && originalTreeBox.getFormulaBox().getRTA() != null) {
                            BoxedDRTA treeFormulaBox = originalTreeBox.getFormulaBox();
                            RichTextArea treeRTA = treeFormulaBox.getRTA();
                            newTreeMod.setPrintWidth(treeRTA.getPrefWidth());
                            treeRTA.getActionFactory().saveNow().execute(new ActionEvent());
                            newTreeMod.setText(treeRTA.getDocument());
                        }

                        newTreeMod.setBoxed(originalTreeBox.isBoxed());
                        newTreeMod.setStarred(originalTreeBox.isStarred());
                        newTreeMod.setAnnotation(originalTreeBox.isAnnotation());
                        newTreeMod.setAnnotationDoc(originalTreeBox.getAnnotationDoc());
                        newTreeMod.setCircled(originalTreeBox.isCircled());
                        newTreeMod.setCircleXAnchors(originalTreeBox.getCircleXAnchors());
                        newTreeMod.setRtaBoundsHeight(originalTreeBox.getRtaBoundsHeight());
                        newTreeMod.setRtaBoundsMinY(originalTreeBox.getRtaBoundsMinY());

                        if (originalTreeBox.getLinesPane() != null && originalTreeBox.getLinesPane().getChildren() != null) {
                            ObservableList<Node> linesList = originalTreeBox.getLinesPane().getChildren();
                            for (Node lineNode : linesList) {
                                if (lineNode instanceof Line) {
                                    Line line = (Line) lineNode;
                                    AnchorPane linesPane = originalTreeBox.getLinesPane();
                                    UnderlineMod underlineModel = new UnderlineMod(linesPane.getLeftAnchor(line), line.getEndX(), linesPane.getBottomAnchor(line)  );
                                    newTreeMod.getUnderlineList().add(underlineModel);
                                }
                            }
                        }
                        newTreeMod.setBaseline(originalTreeBox.getBaseline());
                        model.getTreeFormulaBoxes().add(newTreeMod);

                    } else if (node instanceof MapABExpFormulaBox) {
                        MapABExpFormulaBox originalMapBox = (MapABExpFormulaBox) node;
                        MapFormulaBoxMod newMapMod = new MapFormulaBoxMod();
                        newMapMod.setIdString(originalMapBox.getIdString());
                        newMapMod.setLayoutX(originalMapBox.getLayoutX());
                        newMapMod.setLayoutY(originalMapBox.getLayoutY());
                        newMapMod.setLinkIdStrings(originalMapBox.getmLinkIds());
                        newMapMod.setMapIndexItems(originalMapBox.getMapIndexItems());
                        newMapMod.setMetaBox(originalMapBox.isMetaBox());

                        if (originalMapBox.getFormulaBox() != null && originalMapBox.getFormulaBox().getRTA() != null) {
                            BoxedDRTA formulaBox = originalMapBox.getFormulaBox();
                            RichTextArea mapRTA = formulaBox.getRTA();
                            newMapMod.setPrintWidth(mapRTA.getPrefWidth());
                            mapRTA.getActionFactory().saveNow().execute(new ActionEvent());
                            newMapMod.setText(mapRTA.getDocument());
                        }
                        model.getMapFormulaBoxes().add(newMapMod);

                    } else if (node instanceof MapABExpVerticalBracket) {
                        MapABExpVerticalBracket vBrack = (MapABExpVerticalBracket) node;
                        VerticalBracketMod brack = new VerticalBracketMod(node.getLayoutX(), node.getLayoutY(), vBrack.getMainPane().getPrefHeight());
                        model.getVerticalBrackets().add(brack);

                    } else if (node instanceof MapABExpDashedLine) {
                        MapABExpDashedLine dLine = (MapABExpDashedLine) node;
                        DashedLineMod dlMod = new DashedLineMod(node.getLayoutX(), node.getLayoutY(), dLine.getMainPane().getPrefWidth());
                        model.getDashedLineMods().add(dlMod);

                    } else if (node instanceof MapABExpClickableNodeLink) {
                        MapABExpClickableNodeLink cLink = (MapABExpClickableNodeLink) node;
                        ClickableNodeLinkMod cMod = new ClickableNodeLinkMod(cLink.getIdString(), cLink.getTargetId(), cLink.getSourceId());
                        model.getClickableNodeLinks().add(cMod);

                    } else if (node instanceof MapABExpClickableMapLink) {
                        MapABExpClickableMapLink mapLink = (MapABExpClickableMapLink) node;
                        ClickableMapLinkMod mapLinkMod = new ClickableMapLinkMod();
                        mapLinkMod.setIdString(mapLink.getIdString());
                        mapLinkMod.setSourceId(mapLink.getSourceId()); mapLinkMod.setTargetId(mapLink.getTargetId());
                        mapLinkMod.setSourceMapStage(mapLink.getSourceMapStage()); mapLinkMod.setTargetMapStage(mapLink.getTargetMapStage());
                        mapLinkMod.setSourceXAnchors(mapLink.getSourceXAnchors()); mapLinkMod.setTargetXAnchors(mapLink.getTargetXAnchors());
                        model.getClickableMapLinks().add(mapLinkMod);

                    } else if (node instanceof MapABExpQuestionMarker) {
                        MapABExpQuestionMarker mapQuest = (MapABExpQuestionMarker) node;
                        MapQuestionMarkerMod qMod = new MapQuestionMarkerMod(mapQuest.getIdString(), mapQuest.getTargetId(), mapQuest.getTargetMapStage(), mapQuest.getTargetXAnchors());
                        model.getMapQuestionMarkers().add(qMod);
                    }
                }
            }

            model.setPointsPossible(mapModel.getPointsPossible());
            TextField pointsField = mapView.getPointsEarnedTextField();
            if ((pointsField.isFocused() || mapModel.getPointsEarned() >= 0) && pointsField.getText().isEmpty()) {
                pointsField.setText("0");
            }
            if (!pointsField.getText().equals("")) model.setPointsEarned(Integer.parseInt(pointsField.getText()));
            else model.setPointsEarned(-1);

            return model;
        } catch (Exception e) { return null; }
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

    // SCORCHED EARTH RULE 5: The Rule of Isolation (dozens of tiny, isolated try-catch blocks)
    @Override
    public void cleanup() {
        // KILL SWITCH: Engaged! Prevents ghost focus events mid-teardown
        isTornDown = true;

        // SCORCHED EARTH RULE 2: No Global Null-Poisoning (safe method only)
        try {
            if (mainView != null) {
                mainView.textFieldInFocus();
            }
        } catch (Exception e) {}

        // Listeners
        try { if (undoRedoFlag != null && undoRedoFlagListener != null) undoRedoFlag.removeListener(undoRedoFlagListener); } catch (Exception e) {}
        try { if (undoRedoFlag != null) undoRedoFlag.unbind(); } catch (Exception e) {}

        try { if (mapView != null && mapView.getPointsEarnedTextField() != null && pointsEarnedListener != null) mapView.getPointsEarnedTextField().textProperty().removeListener(pointsEarnedListener); } catch (Exception e) {}

        try { if (mapView != null && mapView.getExerciseStatement() != null && mapView.getExerciseStatement().getEditor() != null && statementFocusListener != null) mapView.getExerciseStatement().getEditor().focusedProperty().removeListener(statementFocusListener); } catch (Exception e) {}

        try {
            if (mapView != null && mapView.getExerciseComment() != null && mapView.getExerciseComment().getEditor() != null) {
                if (commentFullHeightListener != null) mapView.getExerciseComment().getEditor().fullHeightProperty().removeListener(commentFullHeightListener);
                if (commentModifiedListener != null) mapView.getExerciseComment().getEditor().modifiedProperty().removeListener(commentModifiedListener);
                if (commentFocusListener != null) mapView.getExerciseComment().getEditor().focusedProperty().removeListener(commentFocusListener);
            }
        } catch (Exception e) {}

        try { if (mapView != null && mapView.getaCheckBox() != null && aCheckBoxListener != null) mapView.getaCheckBox().selectedProperty().removeListener(aCheckBoxListener); } catch (Exception e) {}
        try { if (mapView != null && mapView.getbCheckBox() != null && bCheckBoxListener != null) mapView.getbCheckBox().selectedProperty().removeListener(bCheckBoxListener); } catch (Exception e) {}

        try {
            if (mapView != null && mapView.getExplainDRTA() != null && mapView.getExplainDRTA().getEditor() != null) {
                if (explainFullHeightListener != null) mapView.getExplainDRTA().getEditor().fullHeightProperty().removeListener(explainFullHeightListener);
                if (explainModifiedListener != null) mapView.getExplainDRTA().getEditor().modifiedProperty().removeListener(explainModifiedListener);
                if (explainFocusListener != null) mapView.getExplainDRTA().getEditor().focusedProperty().removeListener(explainFocusListener);
            }
        } catch (Exception e) {}

        try { if (mapView != null && mapView.getUndoButton() != null && undoButtonHandler != null) { mapView.getUndoButton().removeEventHandler(ActionEvent.ACTION, undoButtonHandler); mapView.getUndoButton().setOnAction(null); } } catch (Exception e) {}
        try { if (mapView != null && mapView.getRedoButton() != null && redoButtonHandler != null) { mapView.getRedoButton().removeEventHandler(ActionEvent.ACTION, redoButtonHandler); mapView.getRedoButton().setOnAction(null); } } catch (Exception e) {}

        // Thumbnail
        try {
            if (thumbPane != null) {
                if (thumbPaneClickHandler != null) thumbPane.removeEventHandler(MouseEvent.MOUSE_CLICKED, thumbPaneClickHandler);
                thumbPane.setOnMouseClicked(null);
                severFromParent(thumbPane);
                thumbPane.getChildren().clear();
            }
        } catch (Exception e) {}

        try { clearStandingPopups(); } catch (Exception e) {}

        // Cascade cleanup
        try { if (auxExerciseA != null) auxExerciseA.cleanup(); } catch (Exception e) {}
        try { if (auxExerciseB != null) auxExerciseB.cleanup(); } catch (Exception e) {}
        try { if (mapView != null) mapView.cleanup(); } catch (Exception e) {}
        try { if (mapCheck != null) ((MapABExpCheck)mapCheck).cleanup(); } catch (Exception e) {}

        // Nullify
        try { this.undoRedoFlagListener = null; } catch (Exception e) {}
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
        try { this.undoButtonHandler = null; } catch (Exception e) {}
        try { this.redoButtonHandler = null; } catch (Exception e) {}
        try { this.thumbPaneClickHandler = null; } catch (Exception e) {}

        try { this.mainWindow = null; } catch (Exception e) {}
        try { this.mainView = null; } catch (Exception e) {}
        try { this.mapModel = null; } catch (Exception e) {}
        try { this.mapView = null; } catch (Exception e) {}
        try { this.mapCheck = null; } catch (Exception e) {}
        try { this.auxExerciseA = null; } catch (Exception e) {}
        try { this.auxExerciseB = null; } catch (Exception e) {}
        try { this.tStage = null; } catch (Exception e) {}
        try { this.thumbPane = null; } catch (Exception e) {}
        try { this.tScene = null; } catch (Exception e) {}

        // Non destructive
        try { this.undoRedoList = null; } catch (Exception e) {}
    }

    public MainWindow getMainWindow() { return mainWindow; }

    @Override
    public Node getAuxExerNode() {
        if (mapView == null || mapModel == null || mapView.getExplainDRTA() == null || mapView.getContentBox() == null) return new Pane();
        RichTextArea explainRTA = mapView.getExplainDRTA().getEditor();
        if (explainRTA != null) {
            explainRTA.prefHeightProperty().unbind();
            explainRTA.setMinHeight(mapModel.getExplainTextHeight() + RTA_SCREEN_BMP);
        }
        return new VBox(5, mapView.getRootLayout().getMain_pane(), mapView.getChoiceBox(), explainRTA);
    }

    @Override
    public String getAuxExerName() {
        if (mapModel == null) return "";
        return mapModel.getAuxExerName();
    }



    @Override
    public void clearStandingPopups() {
        try {
            if (mapView != null && mapView.getStaticHelpStage() != null) mapView.getStaticHelpStage().close();
            if (tStage != null) tStage.close();
        } catch (Exception e) {}
    }

    @Override
    public AnchorPane getMainPane() {
        if (mapView == null || mapView.getRootLayout() == null) return new AnchorPane();
        return mapView.getRootLayout().getMain_pane();
    }

    @Override
    public VTAuxCheck getVTAuxCheck() { return (VTAuxCheck) mapCheck; }

    @Override
    public VTAuxExer getVTAuxExer() { return (VTAuxExer) auxExerciseA; }
}
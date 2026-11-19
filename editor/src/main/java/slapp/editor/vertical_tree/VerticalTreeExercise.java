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

package slapp.editor.vertical_tree;

import com.gluonhq.richtextarea.RichTextArea;
import com.gluonhq.richtextarea.model.Document;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.*;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.event.ActionEvent;
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
import slapp.editor.DiskUtilities;
import slapp.editor.EditorMain;
import slapp.editor.PrintUtilities;
import slapp.editor.decorated_rta.BoxedDRTA;
import slapp.editor.decorated_rta.DecoratedRTA;
import slapp.editor.main_window.*;
import slapp.editor.vertical_tree.drag_drop.*;
import slapp.editor.vertical_tree.object_models.*;

import java.util.*;

import static slapp.editor.EditorMain.*;


import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
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


public class VerticalTreeExercise implements Exercise<VerticalTreeModel, VerticalTreeView>, VTAuxExer {

    private MainWindow mainWindow;
    private MainWindowView mainView;
    private VerticalTreeModel verticalTreeModel;
    private VerticalTreeView verticalTreeView;
    private VTcheck vtCheck;
    private boolean exerciseModified = false;
    private UndoRedoList<VerticalTreeModel> undoRedoList = new UndoRedoList<>(50);
    public BooleanProperty undoRedoFlag = new SimpleBooleanProperty();
    private Exercise auxExerciseA;
    private Exercise auxExerciseB;
    Stage tStage;
    Pane thumbPane = new Pane();
    Scene tScene = new Scene(new Pane());

    // SCORCHED EARTH RULE 1 & 2: The Asynchronous Ghost "Kill Switch"
    private boolean isTornDown = false;

    // SCORCHED EARTH RULE 3: Eradicate Anonymous Lambdas
    private ChangeListener<Boolean> undoRedoFlagListener;
    private ChangeListener<String> pointsEarnedListener;
    private ChangeListener<Boolean> statementFocusListener;
    private ChangeListener<Number> commentFullHeightListener;
    private ChangeListener<Boolean> commentModifiedListener;
    private ChangeListener<Boolean> commentFocusListener;
    private EventHandler<ActionEvent> undoButtonHandler;
    private EventHandler<ActionEvent> redoButtonHandler;
    private EventHandler<MouseEvent> thumbPaneClickHandler;
    private boolean isRebuilding = false;
    private boolean isUndoQueued = false;

    public VerticalTreeExercise(VerticalTreeModel model, MainWindow mainWindow) {
        this.mainWindow = mainWindow;
        this.verticalTreeModel = model;
        if (model.getOriginalModel() == null) {model.setOriginalModel(model); }
        this.mainView = mainWindow.getMainView();

        this.verticalTreeView = new VerticalTreeView(mainView, this);

        setVerticalTreeView();

        vtCheck = new VTcheck(this);

        undoRedoList = new UndoRedoList<>(50);

        undoRedoFlag.set(false);
        undoRedoFlag.bind(verticalTreeView.undoRedoFlagProperty());

        undoRedoFlagListener = (ob, ov, nv) -> {
            if (isTornDown || isRebuilding || mainView == null) return;
            if (nv) {
                exerciseModified = true;
                pushUndoRedo();
            }
        };
        undoRedoFlag.addListener(undoRedoFlagListener);

        VerticalTreeModel deepCopy = (VerticalTreeModel) SerializationUtils.clone(verticalTreeModel);
        undoRedoList.push(deepCopy);
        updateUndoRedoButtons();

        Platform.runLater(() -> {
            if (isTornDown || mainView == null) return;
            exerciseModified = false;
        });
    }

    private void setVerticalTreeView() {
        verticalTreeView.setDefaultKeyboard(verticalTreeModel.getDefaultKeyboardType());
        verticalTreeView.setDefaultMapKeyboard(verticalTreeModel.getDefaultMapKeyboardType());
        verticalTreeView.setStatementPrefHeight(verticalTreeModel.getStatementPrefHeight());
        verticalTreeView.setCommentPrefHeight(verticalTreeModel.getCommentPrefHeight());
        verticalTreeView.setMainPanePrefHeight(verticalTreeModel.getMainPanePrefHeight());
        verticalTreeView.setMainPanePrefWidth(verticalTreeModel.getMainPanePrefWidth());

        verticalTreeView.setPointsPossible(verticalTreeModel.getPointsPossible());
        if (verticalTreeModel.getPointsEarned() >= 0) verticalTreeView.getPointsEarnedTextField().setText(Integer.toString(verticalTreeModel.getPointsEarned()));

        pointsEarnedListener = (ob, ov, nv) -> {
            if (isTornDown || mainView == null) return;
            exerciseModified = true;
        };
        verticalTreeView.getPointsEarnedTextField().textProperty().addListener(pointsEarnedListener);

        //statement
        DecoratedRTA statementDRTA = new DecoratedRTA();
        RichTextArea statementEditor = statementDRTA.getEditor();
        statementEditor.getActionFactory().open(verticalTreeModel.getExerciseStatement()).execute(new ActionEvent());

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
        verticalTreeView.setExerciseStatement(statementDRTA);

        //comment
        DecoratedRTA commentDRTA = new DecoratedRTA();
        RichTextArea commentEditor = commentDRTA.getEditor();
        commentEditor.setPromptText("Comment: ");
        commentEditor.getActionFactory().open(verticalTreeModel.getExerciseComment()).execute(new ActionEvent());
        commentEditor.getActionFactory().saveNow().execute(new ActionEvent());

        final boolean[] commentPrimed = {false};

        commentFullHeightListener = (ob, ov, nv) -> {
            if (isTornDown || mainView == null || verticalTreeModel == null) return;
            if (!commentPrimed[0]) {
                commentPrimed[0] = true;
                return;
            }
            double height = (double) nv;
            mainView.setRTATextHeight(height);
            verticalTreeModel.setCommentTextHeight(height);
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
        verticalTreeView.setExerciseComment(commentDRTA);

        //cleanup
        undoButtonHandler = e -> {
            if (isTornDown || mainView == null) return;
            undoAction();
        };
        verticalTreeView.getUndoButton().setOnAction(undoButtonHandler);

        redoButtonHandler = e -> {
            if (isTornDown || mainView == null) return;
            redoAction();
        };
        verticalTreeView.getRedoButton().setOnAction(redoButtonHandler);

        populateControlBox();
        for (DragIconType type : verticalTreeModel.getDragIconList()) {
            verticalTreeView.getRootLayout().addDragIcon(type);
        }

        populateMainPaneNodes();
        verticalTreeView.initializeViewDetails();
        verticalTreeView.setRightControlBox();
    }

    private void populateMainPaneNodes() {
        if (verticalTreeView == null || verticalTreeView.getRootLayout() == null) return;
        AnchorPane mainPane = verticalTreeView.getRootLayout().getMainPane();

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

            for (VerticalBracketMod bracketMod : verticalTreeModel.getVerticalBrackets()) {
                VerticalBracket bracket = new VerticalBracket(verticalTreeView);
                mainPane.getChildren().add(bracket);
                bracket.setLayoutX(bracketMod.getLayoutX());
                bracket.setLayoutY(bracketMod.getLayoutY());
                bracket.getMainPane().setPrefHeight(bracketMod.getHeight());
            }

            for (DashedLineMod dlMod : verticalTreeModel.getDashedLineMods()) {
                DashedLine dashedLine = new DashedLine(verticalTreeView);
                mainPane.getChildren().add(dashedLine);
                dashedLine.setLayoutX(dlMod.getLayoutX());
                dashedLine.setLayoutY(dlMod.getLayoutY());
                dashedLine.getMainPane().setPrefWidth(dlMod.getWidth());
            }

            for (MapFormulaBoxMod mapBoxMod : verticalTreeModel.getMapFormulaBoxes()) {
                MapFormulaBox mapFormulaBox = new MapFormulaBox(verticalTreeView);
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

            for (TreeFormulaBoxMod treeBoxMod : verticalTreeModel.getTreeFormulaBoxes()) {
                TreeFormulaBox treeFormulaBox = new TreeFormulaBox(verticalTreeView);
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
            for (ClickableNodeLinkMod nodeLinkMod : verticalTreeModel.getClickableNodeLinks()) {
                ClickableNodeLink nodeLink = new ClickableNodeLink(verticalTreeView);
                mainPane.getChildren().add(nodeLink);
                nodeLink.setId(nodeLinkMod.getIdString());
                TreeFormulaBox source = null;
                TreeFormulaBox target = null;
                for (Node node : nodesList) {
                    if (node instanceof TreeFormulaBox) {
                        TreeFormulaBox treeBox = (TreeFormulaBox) node;
                        if (treeBox.getIdString().equals(nodeLinkMod.getSourceId())) source = treeBox;
                        if (treeBox.getIdString().equals(nodeLinkMod.getTargetId())) target = treeBox;
                    }
                }
                if (source != null && target != null) {
                    nodeLink.bindEnds(source, target);
                }

            }

            for (MapQuestionMarkerMod mapQuestMod : verticalTreeModel.getMapQuestionMarkers()) {
                MapQuestionMarker mapQuestion = new MapQuestionMarker(verticalTreeView);
                mainPane.getChildren().add(mapQuestion);
                mapQuestion.setId(mapQuestMod.getIdString());

                for (Node node : nodesList) {
                    if (node instanceof MapFormulaBox) {
                        MapFormulaBox mapBox = (MapFormulaBox) node;
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

            for (ClickableMapLinkMod mapLinkMod : verticalTreeModel.getClickableMapLinks()) {
                ClickableMapLink mapLink = new ClickableMapLink(verticalTreeView);
                mainPane.getChildren().add(0, mapLink);
                mapLink.setId(mapLinkMod.getIdString());

                MapFormulaBox source = null;
                MapFormulaBox target = null;
                for (Node node : nodesList) {
                    if (node instanceof MapFormulaBox) {
                        MapFormulaBox mapFormulaBox = (MapFormulaBox) node;
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
        if (verticalTreeView == null || verticalTreeView.getControlBox() == null) return;
        VBox controlBox = verticalTreeView.getControlBox();
        controlBox.setAlignment(Pos.TOP_CENTER);

        RootLayout layout = verticalTreeView.getRootLayout();
        ToggleGroup buttonGroup = verticalTreeView.getRootLayout().getButtonGroup();

        for (ObjectControlType type : verticalTreeModel.getObjectControlList()) {
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
                    break;
                }
            }
        }

        Platform.runLater(() -> {
            if (isTornDown || mainView == null) return;
            if (mainWindow != null && mainWindow.getCurrentExercise() != this) return;
            if (auxExerciseA != null) {
                thumbPane = auxImage();
                if (verticalTreeView != null && verticalTreeView.getControlBox() != null) {
                    verticalTreeView.getControlBox().getChildren().add(thumbPane);
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

                    tStage.setWidth(pane1.getWidth() + EditorMain.RTA_WIDTH_BMP);
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
        if (isTornDown || mainView == null || verticalTreeView == null || verticalTreeView.getRootLayout() == null) return;

        ObservableList<Node> nodesList = verticalTreeView.getRootLayout().getMainPane().getChildren();
        for (Node node : nodesList) {
            if (node instanceof TreeFormulaBox) {
                TreeFormulaBox treeBox =  (TreeFormulaBox) node;

                RichTextArea treeRTA = treeBox.getFormulaBox().getRTA();
                if (treeRTA != null && treeRTA.isModified()) {
                    verticalTreeView.getVTExercise().setExerciseModified(true);
                    verticalTreeView.getVTExercise().pushUndoRedo();
                    treeRTA.getActionFactory().saveNow().execute(new ActionEvent());
                }
                if (treeBox.isAnnotation() && treeBox.getAnnotationField() != null) {
                    RichTextArea annRTA = treeBox.getAnnotationField().getRTA();
                    if (annRTA != null && annRTA.isModified()) {
                        verticalTreeView.getVTExercise().setExerciseModified(true);
                        verticalTreeView.getVTExercise().pushUndoRedo();
                        annRTA.getActionFactory().saveNow().execute(new ActionEvent());
                    }
                }

            }
            if (node instanceof MapFormulaBox) {
                RichTextArea mapRTA = ((MapFormulaBox) node).getFormulaBox().getRTA();
                if (mapRTA != null && mapRTA.isModified()) {
                    verticalTreeView.getVTExercise().setExerciseModified(true);
                    verticalTreeView.getVTExercise().pushUndoRedo();
                    mapRTA.getActionFactory().saveNow().execute(new ActionEvent());
                }
            }
        }
    }

    private void undoAction() {
        if (isTornDown || undoRedoList == null) return;
        VerticalTreeModel undoElement = undoRedoList.getUndoElement();
        if (undoElement != null) {
            verticalTreeModel = (VerticalTreeModel) SerializationUtils.clone(undoElement);
            populateMainPaneNodes();
            updateUndoRedoButtons();
        }
    }

    private void redoAction() {
        if (isTornDown || undoRedoList == null) return;
        VerticalTreeModel redoElement = undoRedoList.getRedoElement();
        if (redoElement != null) {
            verticalTreeModel = (VerticalTreeModel) SerializationUtils.clone(redoElement);
            populateMainPaneNodes();
            updateUndoRedoButtons();
        }
    }

    private void updateUndoRedoButtons() {
        if (isTornDown || verticalTreeView == null || undoRedoList == null) return;
        if (verticalTreeView.getUndoButton() != null) verticalTreeView.getUndoButton().setDisable(!undoRedoList.canUndo());
        if (verticalTreeView.getRedoButton() != null) verticalTreeView.getRedoButton().setDisable(!undoRedoList.canRedo());
    }

    public void pushUndoRedo() {
        if (isTornDown || isRebuilding || undoRedoList == null || verticalTreeModel == null) return;

        exerciseModified = true;

        // DEBOUNCER: If a loop triggers multiple saves in the same frame, group them
        if (!isUndoQueued) {
            isUndoQueued = true; // Lock the queue

            // Queue the actual save for the end of the current JavaFX frame
            Platform.runLater(() -> {
                isUndoQueued = false; // Release the lock

                // Double check teardown/rebuild status after the delay
                if (isTornDown || isRebuilding || undoRedoList == null || verticalTreeModel == null) return;

                VerticalTreeModel model = getVerticalTreeModelFromView();
                VerticalTreeModel deepCopy = (VerticalTreeModel) SerializationUtils.clone(model);
                undoRedoList.push(deepCopy);
                updateUndoRedoButtons();
            });
        }
    }

    /*
    public void pushUndoRedo() {
        if (isTornDown || isRebuilding || undoRedoList == null || verticalTreeModel == null) return;
        VerticalTreeModel model = getVerticalTreeModelFromView();
        VerticalTreeModel deepCopy = (VerticalTreeModel) SerializationUtils.clone(model);
        undoRedoList.push(deepCopy);
        updateUndoRedoButtons();
        exerciseModified = true;
    }
     */

    @Override
    public VerticalTreeModel getExerciseModel() {  return verticalTreeModel;    }

    @Override
    public VerticalTreeView getExerciseView() {  return verticalTreeView;    }

    @Override
    public void saveExercise(boolean saveAs) {
        boolean success = DiskUtilities.saveExercise(saveAs, getVerticalTreeModelFromView());
        if (success) exerciseModified = false;
    }

    @Override
    public List<Node> getPrintNodes(ExerciseModel originalModel) {
        List<Node> nodeList = new ArrayList<>();
        VerticalTreeModel printModel = verticalTreeModel;
        VerticalTreeExercise printExercise = this;

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
        statementRTA.setPrefHeight(printModel.getStatementTextHeight() + EditorMain.RTA_PRINT_BMP);
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
        AnchorPane mainPane = printExercise.getExerciseView().getRootLayout().getMainPane();
        mainPane.setStyle("-fx-background-color: transparent");
        mainPane.setPadding(new Insets(0));
        mainPane.setMinHeight(0);

        VerticalTreeModel originalVTmod = (VerticalTreeModel) printModel;
        mainPane.setMinWidth(originalVTmod.getMainPanePrefWidth() - EditorMain.RTA_WIDTH_BMP);

        ObservableList<Node> nodes = mainPane.getChildren();
        for (Node node : nodes) {
            if (node instanceof TreeFormulaBox) {
                TreeFormulaBox treeBox = (TreeFormulaBox) node;
                treeBox.getFormulaBox().getRTA().setStyle("-fx-border-width: 0; -fx-border-color: transparent");
                treeBox.getFormulaBox().getRTA().setMinWidth(treeBox.getPrintWidth());
                if (treeBox.getAnnotationField() != null) treeBox.getAnnotationField().getRTA().setStyle("-fx-border-width: 0; -fx-border-color: transparent");
            }
            if (node instanceof MapFormulaBox) {
                MapFormulaBox mapBox = (MapFormulaBox) node;
                mapBox.getFormulaBox().getRTA().setStyle("-fx-border-width: 0; -fx-border-color: transparent");
                mapBox.getFormulaBox().getRTA().setMinWidth(mapBox.getPrintWidth());
            }
        }

        Group root = new Group();
        Scene scene = new Scene(root);
        root.getChildren().add(mainPane);
        root.applyCss();
        root.layout();

        HBox contentHBox = new HBox(mainPane);
        contentHBox.setAlignment(Pos.CENTER);
        contentHBox.setPadding(new Insets(0,0,20, 0));

        nodeList.add(contentHBox);

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
        commentRTA.setPrefHeight(Math.max(40, printModel.getCommentTextHeight() + EditorMain.RTA_PRINT_BMP));
        commentRTA.setMaxWidth(nodeWidth);
        commentRTA.setMinWidth(nodeWidth);

        commentRTA.getStylesheets().clear(); commentRTA.getStylesheets().add("richTextAreaPrinter.css");


        nodeList.add(commentRTA);

        return nodeList;
    }

    TextFlow printCheckNode() {
        TextFlow flow = new TextFlow();
        flow.setMaxWidth(200);
        if (vtCheck != null && vtCheck.isCheckSuccess()) {
            Text bigCheck = new Text("\ue89a");
            bigCheck.setFont(Font.font("Noto Serif Combo", 14));
            Text message = new Text("  " + verticalTreeView.getCheckMessage());
            message.setFont(Font.font("Noto Serif Combo", 11));

            if (vtCheck.isCheckFinal()) {
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
    public Exercise<VerticalTreeModel, VerticalTreeView> resetExercise() {
        RichTextArea commentRTA = verticalTreeView.getExerciseComment().getEditor();
        commentRTA.getActionFactory().saveNow().execute(new ActionEvent());
        Document commentDocument = commentRTA.getDocument();


        int pointsEarned = -1;
        TextField pointsField = verticalTreeView.getPointsEarnedTextField();
        if ((pointsField.isFocused() || verticalTreeModel.getPointsEarned() >= 0) && pointsField.getText().isEmpty()) {
            pointsField.setText("0");
        }
        if (!pointsField.getText().equals("")) pointsEarned = Integer.parseInt(pointsField.getText());



        VerticalTreeModel originalModel = (VerticalTreeModel) (verticalTreeModel.getOriginalModel());
        originalModel.setExerciseComment(commentDocument);

        double commentHeight = commentRTA.getFullHeight();
        originalModel.setCommentTextHeight(commentHeight > 0 ? commentHeight : verticalTreeModel.getCommentTextHeight());

        originalModel.setPointsEarned(pointsEarned);
        VTcheckSetup setup = originalModel.getCheckSetup();
        if (vtCheck != null) {
            setup.setCheckTries(vtCheck.getCheckTries());
        }
        setup.setCheckSuccess(false);
        originalModel.setCheckSetup(setup);

        VerticalTreeExercise clearExercise = new VerticalTreeExercise(originalModel, mainWindow);
        return clearExercise;
    }

    @Override
    public boolean isExerciseModified() {
        if (verticalTreeView == null) return false;
        RichTextArea commentEditor = verticalTreeView.getExerciseComment().getEditor();
        if (commentEditor.isModified()) {
            exerciseModified = true;
        }
        if (verticalTreeView.getRootLayout() == null || verticalTreeView.getRootLayout().getMainPane() == null) return exerciseModified;
        ObservableList<Node> nodes = verticalTreeView.getRootLayout().getMainPane().getChildren();
        for (Node node : nodes) {
            if (node instanceof TreeFormulaBox) {
                TreeFormulaBox treeBox = (TreeFormulaBox) node;
                if (treeBox.getFormulaBox().getRTA().isModified()) exerciseModified = true;
                if (treeBox.isAnnotation() && treeBox.getAnnotationField() != null) {
                    RichTextArea annRTA = treeBox.getAnnotationField().getRTA();
                    if (annRTA != null && annRTA.isModified()) exerciseModified = true;
                }
            }
            if (node instanceof MapFormulaBox) {
                MapFormulaBox mapBox = (MapFormulaBox) node;
                if (mapBox.getFormulaBox().getRTA().isModified()) exerciseModified = true;
            }
        }
        return exerciseModified;
    }

    @Override
    public void setExerciseModified(boolean modified) { exerciseModified = modified; }

    @Override
    public Node getFFViewNode() {return verticalTreeView.getExerciseContentNode();}

    @Override
    public Spinner getFFHeightSpinner() {return verticalTreeView.getMainPaneHeightSpinner();}

    @Override
    public Spinner getFFWidthSpinner() {return verticalTreeView.getMainPaneWidthSpinner();}

    @Override
    public Node getFFPrintNode(ExerciseModel originalModel) {
        VerticalTreeModel printModel = verticalTreeModel;
        VerticalTreeExercise printExercise = this;
        double nodeWidth = PrintUtilities.getPageWidth() / mainWindow.getBaseScale();

        AnchorPane mainPane = printExercise.getExerciseView().getRootLayout().getMainPane();
        mainPane.setStyle("-fx-background-color: transparent");
        mainPane.setPadding(new Insets(0));

        VerticalTreeModel originalVTmod = (VerticalTreeModel) originalModel;
        mainPane.setMinWidth(originalVTmod.getMainPanePrefWidth() - EditorMain.RTA_WIDTH_BMP);

        ObservableList<Node> nodes = mainPane.getChildren();
        for (Node node : nodes) {
            if (node instanceof TreeFormulaBox) {
                TreeFormulaBox treeBox = (TreeFormulaBox) node;
                treeBox.getFormulaBox().getRTA().setStyle("-fx-border-color: transparent");
                treeBox.getFormulaBox().getRTA().setMinWidth(treeBox.getPrintWidth());
                if (treeBox.getAnnotationField() != null) treeBox.getAnnotationField().getRTA().setStyle("-fx-border-color: transparent");
            }
            if (node instanceof MapFormulaBox) {
                MapFormulaBox mapBox = (MapFormulaBox) node;
                ((MapFormulaBox) node).getFormulaBox().getRTA().setStyle("-fx-border-color: transparent");
                mapBox.getFormulaBox().getRTA().setMinWidth(mapBox.getPrintWidth());
            }
        }

        HBox contentHBox = new HBox(mainPane);
        contentHBox.setAlignment(Pos.CENTER);
        contentHBox.setPadding(new Insets(0,0,20, 0));

        return contentHBox;
    }

    @Override
    public ExerciseModel<VerticalTreeModel> getExerciseModelFromView() {
        return (ExerciseModel) getVerticalTreeModelFromView();
    }

    private VerticalTreeModel getVerticalTreeModelFromView() {

        VerticalTreeModel model = new VerticalTreeModel();

        model.setExerciseName(verticalTreeModel.getExerciseName());
        model.setDefaultKeyboardType(verticalTreeModel.getDefaultKeyboardType());
        model.setDefaultMapKeyboardType(verticalTreeModel.getDefaultMapKeyboardType());
        model.setOriginalModel(verticalTreeModel.getOriginalModel());
        model.setDragIconList(verticalTreeModel.getDragIconList());
        model.setObjectControlList(verticalTreeModel.getObjectControlList());
        model.setStarted(verticalTreeModel.isStarted() || exerciseModified);

        VTcheckSetup checkSetup = verticalTreeModel.getCheckSetup();
        if (vtCheck != null) checkSetup.setCheckTries(vtCheck.getCheckTries());
        if (vtCheck != null) checkSetup.setCheckSuccess(vtCheck.isCheckSuccess());
        model.setCheckSetup(checkSetup);

        model.setStatementPrefHeight(verticalTreeView.getExerciseStatement().getEditor().getPrefHeight());
        model.setCommentPrefHeight(verticalTreeView.getCommentPrefHeight());
        model.setStatementTextHeight(verticalTreeModel.getStatementTextHeight());
        model.setMainPanePrefHeight(verticalTreeView.getMainPanePrefHeight());

        model.setMainPanePrefWidth(verticalTreeView.getRootLayout().getMainPane().getWidth());
        model.setExerciseStatement(verticalTreeModel.getExerciseStatement());

        RichTextArea commentRTA = verticalTreeView.getExerciseComment().getEditor();
        commentRTA.getActionFactory().saveNow().execute(new ActionEvent());
        model.setExerciseComment(commentRTA.getDocument());

        double commentTextHeight = commentRTA.getFullHeight();
        model.setCommentTextHeight(commentTextHeight > 0 ? commentTextHeight : verticalTreeModel.getCommentTextHeight());


        RootLayout rootLayout = verticalTreeView.getRootLayout();
        AnchorPane mainPane = rootLayout.getMainPane();
        ObservableList<Node> nodesList = mainPane.getChildren();

        for (Node node : nodesList) {

            if (node instanceof TreeFormulaBox) {
                TreeFormulaBox originalTreeBox = (TreeFormulaBox) node;
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

            } else if (node instanceof MapFormulaBox) {
                MapFormulaBox originalMapBox = (MapFormulaBox) node;
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

            } else if (node instanceof VerticalBracket) {
                VerticalBracket vBrack = (VerticalBracket) node;
                VerticalBracketMod brack = new VerticalBracketMod(node.getLayoutX(), node.getLayoutY(), vBrack.getMainPane().getPrefHeight());
                model.getVerticalBrackets().add(brack);

            } else if (node instanceof DashedLine) {
                DashedLine dLine = (DashedLine) node;
                DashedLineMod dlMod = new DashedLineMod(node.getLayoutX(), node.getLayoutY(), dLine.getMainPane().getPrefWidth());
                model.getDashedLineMods().add(dlMod);

            } else if (node instanceof ClickableNodeLink) {
                ClickableNodeLink cLink = (ClickableNodeLink) node;
                ClickableNodeLinkMod cMod = new ClickableNodeLinkMod(cLink.getIdString(), cLink.getTargetId(), cLink.getSourceId());
                model.getClickableNodeLinks().add(cMod);

            } else if (node instanceof ClickableMapLink) {
                ClickableMapLink mapLink = (ClickableMapLink) node;
                ClickableMapLinkMod mapLinkMod = new ClickableMapLinkMod();
                mapLinkMod.setIdString(mapLink.getIdString());
                mapLinkMod.setSourceId(mapLink.getSourceId()); mapLinkMod.setTargetId(mapLink.getTargetId());
                mapLinkMod.setSourceMapStage(mapLink.getSourceMapStage()); mapLinkMod.setTargetMapStage(mapLink.getTargetMapStage());
                mapLinkMod.setSourceXAnchors(mapLink.getSourceXAnchors()); mapLinkMod.setTargetXAnchors(mapLink.getTargetXAnchors());
                model.getClickableMapLinks().add(mapLinkMod);

            } else if (node instanceof MapQuestionMarker) {
                MapQuestionMarker mapQuest = (MapQuestionMarker) node;
                MapQuestionMarkerMod qMod = new MapQuestionMarkerMod(mapQuest.getIdString(), mapQuest.getTargetId(), mapQuest.getTargetMapStage(), mapQuest.getTargetXAnchors());
                model.getMapQuestionMarkers().add(qMod);
            }
        }

        model.setPointsPossible(verticalTreeModel.getPointsPossible());
        TextField pointsField = verticalTreeView.getPointsEarnedTextField();
        if ((pointsField.isFocused() || verticalTreeModel.getPointsEarned() >= 0) && pointsField.getText().isEmpty()) {
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

    // SCORCHED EARTH RULE 5: Heavily Isolated cleanup
    @Override
    public void cleanup() {
        // KILL SWITCH: Engaged! Prevent any focus listeners from doing UI operations mid-teardown
        isTornDown = true;

        // SCORCHED EARTH RULE 7: Clear global MainView references (safe method only to avoid Toolbar Null-Poisoning)
        try { if (mainView != null) mainView.textFieldInFocus(); } catch (Exception e) {}

        // 1. Unbind local properties defensively
        try { if (undoRedoFlag != null && undoRedoFlagListener != null) undoRedoFlag.removeListener(undoRedoFlagListener); } catch (Exception e) {}
        try { if (undoRedoFlag != null) undoRedoFlag.unbind(); } catch (Exception e) {}

        try {
            if (verticalTreeView != null && verticalTreeView.localScalePageHeightProperty() != null) {
                verticalTreeView.localScalePageHeightProperty().unbind();
            }
        } catch (Exception e) {}

        try {
            if (verticalTreeView != null && verticalTreeView.localScalePageWidthProperty() != null) {
                verticalTreeView.localScalePageWidthProperty().unbind();
            }
        } catch (Exception e) {}

        try {
            if (mainView != null && mainView.getMainScene() != null && verticalTreeView != null && verticalTreeView.getExerciseFocusListener() != null) {
                mainView.getMainScene().focusOwnerProperty().removeListener(verticalTreeView.getExerciseFocusListener());
            }
        } catch (Exception e) {}

        try {
            if (verticalTreeView != null && verticalTreeView.getPointsEarnedTextField() != null && pointsEarnedListener != null) {
                verticalTreeView.getPointsEarnedTextField().textProperty().removeListener(pointsEarnedListener);
            }
        } catch (Exception e) {}

        try {
            if (verticalTreeView != null && verticalTreeView.getExerciseStatement() != null && verticalTreeView.getExerciseStatement().getEditor() != null && statementFocusListener != null) {
                verticalTreeView.getExerciseStatement().getEditor().focusedProperty().removeListener(statementFocusListener);
            }
        } catch (Exception e) {}

        try {
            if (verticalTreeView != null && verticalTreeView.getExerciseComment() != null && verticalTreeView.getExerciseComment().getEditor() != null) {
                if (commentFullHeightListener != null) verticalTreeView.getExerciseComment().getEditor().fullHeightProperty().removeListener(commentFullHeightListener);
            }
        } catch (Exception e) {}

        try {
            if (verticalTreeView != null && verticalTreeView.getExerciseComment() != null && verticalTreeView.getExerciseComment().getEditor() != null) {
                if (commentModifiedListener != null) verticalTreeView.getExerciseComment().getEditor().modifiedProperty().removeListener(commentModifiedListener);
            }
        } catch (Exception e) {}

        try {
            if (verticalTreeView != null && verticalTreeView.getExerciseComment() != null && verticalTreeView.getExerciseComment().getEditor() != null) {
                if (commentFocusListener != null) verticalTreeView.getExerciseComment().getEditor().focusedProperty().removeListener(commentFocusListener);
            }
        } catch (Exception e) {}

        // ---> SCORCHED EARTH RULE 4: THE JAVAFX SKIN HAMMER (DEFERRED TO VIEW) <---
        try {
            // Skin Hammer deferred completely to the view
        } catch (Exception e) {}

        // SCORCHED EARTH RULE 8: Destroy sub-components embedded in AnchorPanes safely
        try {
            if (verticalTreeView != null && verticalTreeView.getRootLayout() != null && verticalTreeView.getRootLayout().getMainPane() != null) {
                AnchorPane mainPane = verticalTreeView.getRootLayout().getMainPane();
                if (mainPane.getChildren() != null) {
                    List<Node> childrenCopy = new ArrayList<>(mainPane.getChildren());
                    for (Node node : childrenCopy) {
                        try { node.getClass().getMethod("cleanup").invoke(node); } catch(Exception e) {}
                        severFromParent(node);
                    }
                }
                mainPane.getChildren().clear();
            }
        } catch (Exception e) {}

        // ---> 4. DISMANTLE THE UI TREE <---
        try {
            if (verticalTreeView != null) {
                if (verticalTreeView.getControlBox() != null) {
                    severFromParent(verticalTreeView.getControlBox());
                    verticalTreeView.getControlBox().getChildren().clear();
                }
                if (verticalTreeView.getUndoButton() != null) {
                    if (undoButtonHandler != null) verticalTreeView.getUndoButton().removeEventHandler(ActionEvent.ACTION, undoButtonHandler);
                    verticalTreeView.getUndoButton().setOnAction(null);
                }
                if (verticalTreeView.getRedoButton() != null) {
                    if (redoButtonHandler != null) verticalTreeView.getRedoButton().removeEventHandler(ActionEvent.ACTION, redoButtonHandler);
                    verticalTreeView.getRedoButton().setOnAction(null);
                }
                if (verticalTreeView.getCheckButton() != null) verticalTreeView.getCheckButton().setOnAction(null);
                if (verticalTreeView.getCheckProgButton() != null) verticalTreeView.getCheckProgButton().setOnAction(null);
                if (verticalTreeView.getstaticHelpButton() != null) verticalTreeView.getstaticHelpButton().setOnAction(null);
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
        try { if (undoRedoList != null) undoRedoList = null; } catch (Exception e) {}

        // 5. Clear OS-level popups
        try { clearStandingPopups(); } catch (Exception e) {}
        try {
            if (verticalTreeView != null && verticalTreeView.getStaticHelpStage() != null) {
                verticalTreeView.getStaticHelpStage().close();
            }
        } catch (Exception e) {}

        // 6. Cascade cleanup to auxiliary exercises
        try { if (auxExerciseA != null) auxExerciseA.cleanup(); } catch (Exception e) {}
        try { if (auxExerciseB != null) auxExerciseB.cleanup(); } catch (Exception e) {}

        // 7. Tell View & Check to specifically clean up
        try { if (verticalTreeView != null) verticalTreeView.cleanup(); } catch (Exception e) {}
        try { if (vtCheck != null) vtCheck.cleanup(); } catch (Exception e) {}

        // 8. Final severed ties
        try { this.verticalTreeModel = null; } catch (Exception e) {}
        try { this.mainWindow = null; } catch (Exception e) {}
        try { this.mainView = null; } catch (Exception e) {}
        try { this.vtCheck = null; } catch (Exception e) {}
        try { this.auxExerciseA = null; } catch (Exception e) {}
        try { this.auxExerciseB = null; } catch (Exception e) {}
        try { this.verticalTreeView = null; } catch (Exception e) {}
        try { this.tScene = null; } catch (Exception e) {}
        try { this.tStage = null; } catch (Exception e) {}
        try { this.thumbPane = null; } catch (Exception e) {}

        try { this.undoRedoFlagListener = null; } catch (Exception e) {}
        try { this.pointsEarnedListener = null; } catch (Exception e) {}
        try { this.statementFocusListener = null; } catch (Exception e) {}
        try { this.commentFullHeightListener = null; } catch (Exception e) {}
        try { this.commentModifiedListener = null; } catch (Exception e) {}
        try { this.commentFocusListener = null; } catch (Exception e) {}
        try { this.undoButtonHandler = null; } catch (Exception e) {}
        try { this.redoButtonHandler = null; } catch (Exception e) {}
        try { this.thumbPaneClickHandler = null; } catch (Exception e) {}
        try { this.undoRedoFlag = null; } catch (Exception e) {}
    }

    @Override
    public Node getAuxExerNode() {
        if (verticalTreeView == null || verticalTreeView.getRootLayout() == null) return new Pane();
        return verticalTreeView.getRootLayout().getMainPane();
    }

    @Override
    public String getAuxExerName() {
        if (verticalTreeModel == null || verticalTreeModel.getCheckSetup() == null) return "";
        return getExerciseModel().getCheckSetup().getAuxExerName();
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
        if (verticalTreeView != null && verticalTreeView.getStaticHelpStage() != null) verticalTreeView.getStaticHelpStage().close();
        if (tStage != null) tStage.close();
    }

    @Override
    public AnchorPane getMainPane() {
        if (verticalTreeView == null || verticalTreeView.getRootLayout() == null) return new AnchorPane();
        return verticalTreeView.getRootLayout().getMainPane();
    }

    @Override
    public VTAuxCheck getVTAuxCheck() {
        return (VTAuxCheck) vtCheck;
    }

    @Override
    public VTAuxExer getVTAuxExer() {
        if (auxExerciseA instanceof VTAuxExer) return (VTAuxExer) auxExerciseA;
        else return null;
    }
}
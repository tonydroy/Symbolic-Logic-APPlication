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

package slapp.editor.trans_tree;

import com.gluonhq.richtextarea.RichTextArea;
import com.gluonhq.richtextarea.model.Document;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
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
import slapp.editor.vertical_tree.drag_drop.DragIconType;
import slapp.editor.vertical_tree.object_models.*;
import java.util.ArrayList;
import java.util.List;
import static slapp.editor.EditorMain.*;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.Spinner;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.beans.value.ChangeListener;
import javafx.event.EventHandler;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import java.util.HashMap;
import java.util.Map;
import javafx.scene.control.*;
import javafx.scene.layout.*;


public class TransTreeExercise implements Exercise<TransTreeModel, TransTreeView>, TTBoxContainer {

    MainWindow mainWindow;
    MainWindowView mainView;
    TransTreeModel transTreeModel;
    TransTreeView transTreeView;
    private TransTreeCheck transTreeCheck;
    private boolean exerciseModified = false;
    private UndoRedoList<TransTreeModel> undoRedoList = new UndoRedoList<>(50);
    public BooleanProperty undoRedoFlag = new SimpleBooleanProperty();
    private Exercise auxExerciseA;
    private Exercise auxExerciseB;
    Stage tStage;
    Pane thumbPane = new Pane();
    Scene tScene = new Scene(new Pane());

    // SCORCHED EARTH RULE 2: Kill Switch for asynchronous event interception during teardown
    private boolean isTornDown = false;

    // SCORCHED EARTH RULE 3: Eradicate Anonymous Lambdas & Track Dynamic Listeners
    private ChangeListener<Boolean> undoRedoFlagListener;
    private ChangeListener<String> pointsEarnedListener;
    private ChangeListener<Boolean> statementFocusListener;
    private ChangeListener<Number> commentFullHeightListener;
    private ChangeListener<Boolean> commentModifiedListener;
    private ChangeListener<Boolean> commentFocusListener;
    private ChangeListener<Number> interpFnFullHeightListener;
    private ChangeListener<Boolean> interpFnModifiedListener;
    private ChangeListener<Boolean> interpFnFocusListener;
    private EventHandler<ActionEvent> undoActionHandler;
    private EventHandler<ActionEvent> redoActionHandler;
    private ChangeListener<Boolean> omitTreesListener;
    private EventHandler<MouseEvent> thumbPaneMouseClickedHandler;

    // SCORCHED EARTH RULE 3: Use IdentityHashMap to safely track dynamically generated listeners
    private Map<RichTextArea, ChangeListener<Number>> parseTreeTextLengthListeners = new java.util.IdentityHashMap<>();

    public TransTreeExercise(TransTreeModel model, MainWindow mainWindow) {
        this.mainWindow = mainWindow;
        this.transTreeModel = model;
        if (model.getOriginalModel() == null) {
            model.setOriginalModel(model);
        }
        this.mainView = mainWindow.getMainView();
        this.transTreeView = new TransTreeView(mainView, this);
        setTransTreeView();

        transTreeCheck = new TransTreeCheck(this);
        undoRedoFlag.set(false);
        undoRedoFlag.bind(transTreeView.undoRedoFlagProperty());

        // Assign named listener
        undoRedoFlagListener = (ob, ov, nv) -> {
            if (isTornDown || mainView == null) return;
            if (nv) {
                exerciseModified = true;
                pushUndoRedo();
            }
        };
        undoRedoFlag.addListener(undoRedoFlagListener);

        TransTreeModel deepCopy = (TransTreeModel) SerializationUtils.clone(transTreeModel);
        undoRedoList.push(deepCopy);
        updateUndoRedoButtons();

        Platform.runLater(() -> {
            if (isTornDown || mainView == null) return;
            exerciseModified = false;
        });
    }

    private void setTransTreeView() {

        transTreeView.setStatementPrefHeight(transTreeModel.getStatementPrefHeight());
        transTreeView.setCommentPrefHeight(transTreeModel.getCommentPrefHeight());
        transTreeView.setInterpFnPrefHeight(transTreeModel.getInterpFnPrefHeight());
        transTreeView.setMainPanePrefHeight(transTreeModel.getMainPanePrefHeight());
        transTreeView.setMainPanePrefWidth(transTreeModel.getMainPanePrefWidth());

        transTreeView.setPointsPossible(transTreeModel.getPointsPossible());
        if (transTreeModel.getPointsEarned() >= 0)
            transTreeView.getPointsEarnedTextField().setText(Integer.toString(transTreeModel.getPointsEarned()));

        pointsEarnedListener = (ob, ov, nv) -> {
            if (isTornDown || mainView == null) return;
            exerciseModified = true;
        };
        transTreeView.getPointsEarnedTextField().textProperty().addListener(pointsEarnedListener);

        //statement
        DecoratedRTA statementDRTA = new DecoratedRTA();
        RichTextArea statementEditor = statementDRTA.getEditor();
        statementEditor.getActionFactory().open(transTreeModel.getExerciseStatement()).execute(new ActionEvent());

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
        transTreeView.setExerciseStatement(statementDRTA);
        statementEditor.setPrefHeight(statementEditor.getFullHeight() + RTA_SCREEN_BMP);

        //comment
        DecoratedRTA commentDRTA = new DecoratedRTA();
        RichTextArea commentEditor = commentDRTA.getEditor();

        commentEditor.getActionFactory().open(transTreeModel.getExerciseComment()).execute(new ActionEvent());
        commentEditor.getActionFactory().saveNow().execute(new ActionEvent());

        final boolean[] commentPrimed = {false};
        commentFullHeightListener = (ob, ov, nv) -> {
            if (isTornDown || mainView == null || transTreeModel == null) return;
            if (!commentPrimed[0]) {
                commentPrimed[0] = true;
                return;
            }
            double height = (double) nv;
            mainView.setRTATextHeight(height);
            transTreeModel.setCommentTextHeight(height);
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
        transTreeView.setExerciseComment(commentDRTA);

        //interpretation fn
        DecoratedRTA interpFnDRTA = transTreeView.getInterpFnDRTA();
        RichTextArea interpFnEditor = interpFnDRTA.getEditor();
        interpFnEditor.getActionFactory().open(transTreeModel.getInterpFnDoc()).execute(new ActionEvent());
        interpFnEditor.getActionFactory().saveNow().execute(new ActionEvent());

        if (transTreeModel.isInterpFnIncluded()) interpFnEditor.setEditable(false);

        final boolean[] interpPrimed = {false};
        interpFnFullHeightListener = (ob, ov, nv) -> {
            if (isTornDown || mainView == null || transTreeModel == null) return;
            if (!interpPrimed[0]) {
                interpPrimed[0] = true;
                return;
            }
            double height = (double) nv;
            mainView.setRTATextHeight(height);
            transTreeModel.setInterpFnTextHeight(height);
        };
        interpFnEditor.fullHeightProperty().addListener(interpFnFullHeightListener);

        interpFnModifiedListener = (ob, ov, nv) -> {
            if (isTornDown || mainView == null) return;
            if (nv) exerciseModified = true;
        };
        interpFnEditor.modifiedProperty().addListener(interpFnModifiedListener);

        mainView.editorInFocus(interpFnDRTA, ControlType.AREA);
        interpFnFocusListener = (o, ov, nv) -> {
            if (isTornDown || mainView == null) return;
            try {
                if (nv) {
                    mainView.editorInFocus(interpFnDRTA, ControlType.AREA);
                    mainView.setRTATextHeight(interpFnEditor.getFullHeight());
                }
                else mainView.undoRTATextHeight();
            } catch (Exception e) {}
        };
        interpFnEditor.focusedProperty().addListener(interpFnFocusListener);

        //cleanup
        undoActionHandler = e -> {
            if (isTornDown || mainView == null) return;
            undoAction();
        };
        transTreeView.getUndoButton().setOnAction(undoActionHandler);

        redoActionHandler = e -> {
            if (isTornDown || mainView == null) return;
            redoAction();
        };
        transTreeView.getRedoButton().setOnAction(redoActionHandler);

        populateControlBox();
        if (!transTreeModel.getCheckSetup().isParseTreeFromAux()) transTreeView.getRootLayout().addDragIcon(DragIconType.parse_field);
        if (!transTreeModel.getCheckSetup().isExcludeFormalTree()) transTreeView.getRootLayout().addDragIcon(DragIconType.tree_field);

        populateMainPaneNodes();

        if (transTreeModel.getCheckSetup().isParseTreeFromAux()) transTreeView.setInterpFieldPrompt("No need to duplicate interpretation function.");

        transTreeView.initializeViewDetails();
        transTreeView.setRightControlBox();

        transTreeView.getOmitTreesCheckBox().setDisable(!transTreeModel.getCheckSetup().isTreesOptional());
        transTreeView.getOmitTreesCheckBox().setSelected(transTreeModel.getCheckSetup().isTreesOmitted());

        omitTreesListener = (ob, ov, nv) -> {
            if (isTornDown || mainView == null || transTreeModel == null || transTreeModel.getCheckSetup() == null) return;
            exerciseModified = true;
            transTreeModel.getCheckSetup().setTreesOmitted(nv);
        };
        transTreeView.getOmitTreesCheckBox().selectedProperty().addListener(omitTreesListener);
    }

    private void populateMainPaneNodes() {
        if (transTreeView == null || transTreeView.getRootLayout() == null) return;
        AnchorPane mainPane = transTreeView.getRootLayout().getMain_pane();

        // SCORCHED EARTH RULE 8 & 9: CME Fix & Dynamic UI Deletion Fix
        if (mainPane.getChildren() != null) {
            List<Node> childrenCopy = new ArrayList<>(mainPane.getChildren());
            for (Node node : childrenCopy) {
                try {
                    if (node instanceof ParseTreeBox) ((ParseTreeBox) node).cleanup();
                    else if (node instanceof FormulaTreeBox) ((FormulaTreeBox) node).cleanup();
                    else {
                        try { node.getClass().getMethod("cleanup").invoke(node); } catch (Exception e) {}
                    }
                    severFromParent(node); // Dynamically sever old nodes
                } catch (Exception e) {}
            }
            mainPane.getChildren().clear();
        }

        // Safe unhook of old dynamic text length listeners
        if (parseTreeTextLengthListeners != null) {
            for (Map.Entry<RichTextArea, ChangeListener<Number>> entry : parseTreeTextLengthListeners.entrySet()) {
                try {
                    entry.getKey().textLengthProperty().removeListener(entry.getValue());
                } catch (Exception e) {}
            }
            parseTreeTextLengthListeners.clear();
        }

        for (TreeBoxMod treeBoxMod : transTreeModel.getParseTreeBoxes()) {
            ParseTreeBox parseTreeBox = new ParseTreeBox(this);
            mainPane.getChildren().add(parseTreeBox);
            parseTreeBox.setLayoutX(treeBoxMod.getLayoutX());
            parseTreeBox.setLayoutY(treeBoxMod.getLayoutY());
            parseTreeBox.setIdString(treeBoxMod.getIdString());
            parseTreeBox.setmLinkIds(treeBoxMod.getLinkIdStrings());

            parseTreeBox.setUlineIndexes(treeBoxMod.getUlineIndexes());
            parseTreeBox.setTreeObjects(treeBoxMod.getTreeObjs());

            parseTreeBox.setPrintWidth(treeBoxMod.getPrintWidth());

            BoxedDRTA parseTreeDRTA = parseTreeBox.getFormulaBox();
            RichTextArea parseTreeBoxRTA = parseTreeDRTA.getRTA();
            parseTreeBoxRTA.getActionFactory().open(treeBoxMod.getText()).execute(new ActionEvent());
            parseTreeBoxRTA.getActionFactory().saveNow().execute(new ActionEvent());

            parseTreeBox.setRtaBoundsHeight(treeBoxMod.getRtaBoundsHeight());
            parseTreeBox.setRtaBoundsMinY(treeBoxMod.getRtaBoundsMinY());

            parseTreeBox.setGray(treeBoxMod.isGray());
            if (treeBoxMod.isGray()) parseTreeBox.getFormulaBox().setVTtreeBoxMutedHighlight();

            for (UnderlineMod underlineMod : treeBoxMod.getUnderlineList()) {
                parseTreeBox.addLineToPane(underlineMod.getStartX(), underlineMod.getLength(), underlineMod.getyPos());
            }
            parseTreeBox.setBaseline(treeBoxMod.getBaseline());

            Platform.runLater(() -> {
                if (isTornDown || mainView == null || parseTreeBox == null || parseTreeBoxRTA == null) return;
                ChangeListener<Number> textLengthListener = (ob, ov, nv) -> {
                    if (isTornDown || mainView == null) return;
                    if (parseTreeBox.isOpened()) {
                        parseTreeBox.clearLines();
                    }
                    parseTreeBox.setOpened(true);
                };
                parseTreeBoxRTA.textLengthProperty().addListener(textLengthListener);
                parseTreeTextLengthListeners.put(parseTreeBoxRTA, textLengthListener);
            });
        }

        for (TreeBoxMod formulaTreeBoxMod : transTreeModel.getFormulaTreeBoxes()) {
            FormulaTreeBox formulaTreeBox = new FormulaTreeBox(this);
            mainPane.getChildren().add(formulaTreeBox);
            formulaTreeBox.setLayoutX(formulaTreeBoxMod.getLayoutX());
            formulaTreeBox.setLayoutY(formulaTreeBoxMod.getLayoutY());
            formulaTreeBox.setIdString(formulaTreeBoxMod.getIdString());
            formulaTreeBox.setmLinkIds(formulaTreeBoxMod.getLinkIdStrings());

            formulaTreeBox.setUlineIndexes(formulaTreeBoxMod.getUlineIndexes());

            formulaTreeBox.setPrintWidth(formulaTreeBoxMod.getPrintWidth());

            BoxedDRTA formulaTreeDRTA = formulaTreeBox.getFormulaBox();
            RichTextArea formulaTreeBoxRTA = formulaTreeDRTA.getRTA();
            formulaTreeBoxRTA.getActionFactory().open(formulaTreeBoxMod.getText()).execute(new ActionEvent());
            formulaTreeBoxRTA.getActionFactory().saveNow().execute(new ActionEvent());

            formulaTreeBox.setRtaBoundsHeight(formulaTreeBoxMod.getRtaBoundsHeight());
            formulaTreeBox.setRtaBoundsMinY(formulaTreeBoxMod.getRtaBoundsMinY());

            formulaTreeBox.setGray(formulaTreeBoxMod.isGray());
            if (formulaTreeBoxMod.isGray()) formulaTreeBox.getFormulaBox().setVTtreeBoxMutedHighlight();
        }

        ObservableList<Node> nodesList = mainPane.getChildren();
        for (ClickableNodeLinkMod nodeLinkMod : transTreeModel.getClickableNodeLinks()) {
            TTClickableNodeLink nodeLink = new TTClickableNodeLink(transTreeView);
            mainPane.getChildren().add(nodeLink);
            nodeLink.setId(nodeLinkMod.getIdString());
            TTreeBox source = null;
            TTreeBox target = null;
            for (Node node : nodesList) {
                if (node instanceof TTreeBox) {
                    TTreeBox tTreeBox = (TTreeBox) node;
                    if (tTreeBox.getIdString().equals(nodeLinkMod.getSourceId())) source = tTreeBox;
                    if (tTreeBox.getIdString().equals(nodeLinkMod.getTargetId())) target = tTreeBox;
                }
            }
            if (source != null && target != null) {
                nodeLink.bindEnds(source, target);
            }
        }
    }


    private void populateControlBox() {
        if (transTreeView == null || transTreeView.getControlBox() == null) return;
        VBox controlBox = transTreeView.getControlBox();
        controlBox.setAlignment(Pos.TOP_CENTER);

        Platform.runLater(() -> {
            if (isTornDown || mainView == null) return;
            if (mainWindow != null && mainWindow.getCurrentExercise() != this) return;
            if (auxExerciseA != null) {
                thumbPane = auxImage();
                if (transTreeView != null && transTreeView.getControlBox() != null) {
                    transTreeView.getControlBox().getChildren().add(thumbPane);
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

            thumbPaneMouseClickedHandler = event -> {
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
            thumbPane.setOnMouseClicked(thumbPaneMouseClickedHandler);
        }
        return thumbPane;
    }

    public void handleMouseEvent(MouseEvent event) {
        if (isTornDown || mainView == null || transTreeView == null || transTreeView.getRootLayout() == null) return;

        // Wrap in a copy just to be safe if any action fires an event that modifies the UI
        List<Node> nodesList = new ArrayList<>(transTreeView.getRootLayout().getMain_pane().getChildren());
        for (Node node : nodesList) {
            try {
                if (node instanceof TTreeBox) {
                    RichTextArea boxRTA = ((TTreeBox) node).getFormulaBox().getRTA();
                    if (boxRTA != null && boxRTA.isModified()) {
                        if (transTreeView.getTransTreeExercise() != null) {
                            transTreeView.getTransTreeExercise().setExerciseModified(true);
                            transTreeView.getTransTreeExercise().pushUndoRedo();
                        }
                        boxRTA.getActionFactory().saveNow().execute(new ActionEvent());
                    }
                }
            } catch (Exception e) {}
        }
    }


    private void undoAction() {
        if (isTornDown || undoRedoList == null) return;
        TransTreeModel undoElement = undoRedoList.getUndoElement();
        if (undoElement != null) {
            transTreeModel = (TransTreeModel) SerializationUtils.clone(undoElement);
            populateMainPaneNodes();
            updateUndoRedoButtons();
        }
    }

    private void redoAction() {
        if (isTornDown || undoRedoList == null) return;
        TransTreeModel redoElement = undoRedoList.getRedoElement();
        if (redoElement != null) {
            transTreeModel = (TransTreeModel) SerializationUtils.clone(redoElement);
            populateMainPaneNodes();
            updateUndoRedoButtons();
        }
    }

    private void updateUndoRedoButtons() {
        if (isTornDown || transTreeView == null || undoRedoList == null) return;
        try { if (transTreeView.getUndoButton() != null) transTreeView.getUndoButton().setDisable(!undoRedoList.canUndo()); } catch (Exception e) {}
        try { if (transTreeView.getRedoButton() != null) transTreeView.getRedoButton().setDisable(!undoRedoList.canRedo()); } catch (Exception e) {}
    }

    public void pushUndoRedo() {
        if (isTornDown || undoRedoList == null || transTreeModel == null) return;
        try {
            TransTreeModel model = getTransTreeModelFromView();
            TransTreeModel deepCopy = (TransTreeModel) SerializationUtils.clone(model);
            undoRedoList.push(deepCopy);
            updateUndoRedoButtons();
            exerciseModified = true;
        } catch (Exception e) {}
    }


    @Override
    public TransTreeModel getExerciseModel() {  return transTreeModel;    }

    @Override
    public TransTreeView getExerciseView() {  return transTreeView;    }

    @Override
    public void saveExercise(boolean saveAs) {
        boolean success = DiskUtilities.saveExercise(saveAs, getTransTreeModelFromView());
        if (success) exerciseModified = false;
    }

    @Override
    public List<Node> getPrintNodes(ExerciseModel originalModel) {
        List<Node> nodeList = new ArrayList<>();
        TransTreeModel printModel = transTreeModel;
        TransTreeExercise printExercise = this;

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

        TransTreeModel originalTTmod = (TransTreeModel) originalModel;
        mainPane.setMinWidth(originalTTmod.getMainPanePrefWidth() - RTA_WIDTH_BMP);

        ObservableList<Node> nodes = mainPane.getChildren();
        for (Node node : nodes) {
            if (node instanceof TTreeBox) {
                TTreeBox treeBox = (TTreeBox) node;
                treeBox.getFormulaBox().getRTA().setStyle("-fx-border-width: 0; -fx-border-color: transparent");
                treeBox.getFormulaBox().getRTA().setMinWidth(treeBox.getPrintWidth());
            }
        }

        HBox contentHBox = new HBox(mainPane);
        contentHBox.setAlignment(Pos.CENTER);
        contentHBox.setPadding(new Insets(0,0,0, 0));

        nodeList.add(contentHBox);

        //interp fn
        RichTextArea interpFnRTA = printExercise.getExerciseView().getInterpFnDRTA().getEditor();
        interpFnRTA.prefHeightProperty().unbind();
        interpFnRTA.minWidthProperty().unbind();
        interpFnRTA.maxWidthProperty().unbind();
        interpFnRTA.setPrefHeight(Math.max(40, printModel.getInterpFnTextHeight() + RTA_PRINT_BMP));
        interpFnRTA.setMaxWidth(nodeWidth);
        interpFnRTA.setMinWidth(nodeWidth);

        interpFnRTA.getStylesheets().clear();
        interpFnRTA.getStylesheets().add("richTextAreaPrinter.css");
        nodeList.add(interpFnRTA);

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
        flow.setMaxWidth(300);
        if (transTreeCheck != null && transTreeCheck.isCheckSuccess()) {
            Text bigCheck = new Text("\ue89a");
            bigCheck.setFont(Font.font("Noto Serif Combo", 14));
            Text message = new Text("  " + transTreeView.getCheckMessage());
            message.setFont(Font.font("Noto Serif Combo", 11));

            if (transTreeModel != null && transTreeModel.getCheckSetup() != null && transTreeModel.getCheckSetup().isCheckGreen()) {
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
    public Exercise<TransTreeModel, TransTreeView> resetExercise() {
        if (transTreeView == null || transTreeModel == null) return null;
        RichTextArea commentRTA = transTreeView.getExerciseComment().getEditor();
        commentRTA.getActionFactory().saveNow().execute(new ActionEvent());
        Document commentDocument = commentRTA.getDocument();

        int pointsEarned = -1;
        TextField pointsField = transTreeView.getPointsEarnedTextField();
        if ((pointsField.isFocused() || transTreeModel.getPointsEarned() >= 0) && pointsField.getText().isEmpty()) {
            pointsField.setText("0");
        }
        if (!pointsField.getText().equals("")) pointsEarned = Integer.parseInt(pointsField.getText());


        TransTreeModel originalModel = (TransTreeModel) (transTreeModel.getOriginalModel());
        originalModel.setExerciseComment(commentDocument);
        double commentHeight = commentRTA.getFullHeight();
        originalModel.setCommentTextHeight(commentHeight > 0 ? commentHeight : transTreeModel.getCommentTextHeight());



        originalModel.setPointsEarned(pointsEarned);

        TTreeCheckSetup setup = originalModel.getCheckSetup();
        if (transTreeCheck != null) {
            setup.setCheckTries(transTreeCheck.getCheckTries());
        }
        setup.setCheckSuccess(false);
        originalModel.setCheckSetup(setup);

        TransTreeExercise clearExercise = new TransTreeExercise(originalModel, mainWindow);
        return clearExercise;
    }

    @Override
    public boolean isExerciseModified() {
        if (transTreeView == null) return false;

        try {
            if (transTreeView.getExerciseComment() != null) {
                RichTextArea commentEditor = transTreeView.getExerciseComment().getEditor();
                if (commentEditor != null && commentEditor.isModified()) {   exerciseModified = true;      }
            }

            if (transTreeView.getInterpFnDRTA() != null) {
                RichTextArea interpFnEditor = transTreeView.getInterpFnDRTA().getEditor();
                if (interpFnEditor != null && interpFnEditor.isModified()) exerciseModified = true;
            }

            if (transTreeView.getRootLayout() == null || transTreeView.getRootLayout().getMain_pane() == null) return exerciseModified;

            ObservableList<Node> nodes = transTreeView.getRootLayout().getMain_pane().getChildren();
            for (Node node : nodes) {
                if (node instanceof TTreeBox) {
                    TTreeBox treeBox = (TTreeBox) node;
                    if (treeBox.getFormulaBox() != null && treeBox.getFormulaBox().getRTA() != null) {
                        if (treeBox.getFormulaBox().getRTA().isModified()) exerciseModified = true;
                    }
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
    public ExerciseModel<TransTreeModel> getExerciseModelFromView() {
        return (ExerciseModel) getTransTreeModelFromView();
    }

    private TransTreeModel getTransTreeModelFromView() {
        if (transTreeModel == null || transTreeView == null) return new TransTreeModel();
        TransTreeModel model = new TransTreeModel();

        model.setExerciseName(transTreeModel.getExerciseName());
        model.setOriginalModel(transTreeModel.getOriginalModel());
        model.setAuxExerName(transTreeModel.getAuxExerName());
        model.setStarted(transTreeModel.isStarted() || exerciseModified);
        model.setInterpFnIncluded(transTreeModel.isInterpFnIncluded());

        TTreeCheckSetup checkSetup = transTreeModel.getCheckSetup();
        if (transTreeCheck != null) {
            checkSetup.setCheckTries(transTreeCheck.getCheckTries());
            checkSetup.setCheckSuccess(transTreeCheck.isCheckSuccess());
            checkSetup.setCheckMessage(transTreeView.getCheckMessage());
        }

        model.setCheckSetup(checkSetup);

        model.setStatementPrefHeight(transTreeView.getExerciseStatement().getEditor().getPrefHeight());
        model.setCommentPrefHeight(transTreeView.getCommentPrefHeight());
        model.setInterpFnPrefHeight(transTreeView.getInterpFnPrefHeight());
        model.setStatementTextHeight(transTreeModel.getStatementTextHeight());

        model.setExerciseStatement(transTreeModel.getExerciseStatement());

        model.setMainPanePrefHeight(transTreeView.getMainPanePrefHeight());
        model.setMainPanePrefWidth(transTreeView.getRootLayout().getMain_pane().getWidth());

        RichTextArea commentRTA = transTreeView.getExerciseComment().getEditor();
        commentRTA.getActionFactory().saveNow().execute(new ActionEvent());
        model.setExerciseComment(commentRTA.getDocument());
        double commentTextHeight = commentRTA.getFullHeight();
        model.setCommentTextHeight(commentTextHeight > 0 ? commentTextHeight : transTreeModel.getCommentTextHeight());


        RichTextArea interpFnRTA = transTreeView.getInterpFnDRTA().getEditor();
        interpFnRTA.getActionFactory().saveNow().execute(new ActionEvent());
        model.setInterpFnDoc(interpFnRTA.getDocument());
        double interpTextHeight = interpFnRTA.getFullHeight();
        model.setInterpFnTextHeight(interpTextHeight > 0 ? interpTextHeight : transTreeModel.getInterpFnTextHeight());

        TTreeRootLayout rootLayout = transTreeView.getRootLayout();
        AnchorPane mainPane = rootLayout.getMain_pane();
        ObservableList<Node> nodesList = mainPane.getChildren();

        for (Node node : nodesList) {

            if (node instanceof ParseTreeBox) {
                ParseTreeBox originalTreeBox = (ParseTreeBox) node;
                TreeBoxMod treeBoxMod = new TreeBoxMod();
                treeBoxMod.setIdString(originalTreeBox.getIdString());
                treeBoxMod.setLayoutX(originalTreeBox.getLayoutX());
                treeBoxMod.setLayoutY(originalTreeBox.getLayoutY());
                treeBoxMod.setLinkIdStrings(originalTreeBox.getmLinkIds());
                treeBoxMod.setGray(originalTreeBox.isGray());

                treeBoxMod.setUlineIndexes(originalTreeBox.getUlineIndexes());
                treeBoxMod.setTreeObjs(originalTreeBox.getTreeObjects());

                BoxedDRTA treeFormulaBox = originalTreeBox.getFormulaBox();
                RichTextArea treeRTA = treeFormulaBox.getRTA();
                treeBoxMod.setPrintWidth(treeRTA.getPrefWidth());

                treeRTA.getActionFactory().saveNow().execute(new ActionEvent());
                treeBoxMod.setText(treeRTA.getDocument());

                treeBoxMod.setRtaBoundsHeight(originalTreeBox.getRtaBoundsHeight());
                treeBoxMod.setRtaBoundsMinY(originalTreeBox.getRtaBoundsMinY());

                ObservableList<Node> linesList = originalTreeBox.getLinesPane().getChildren();
                for (Node lineNode : linesList) {
                    if (lineNode instanceof Line) {
                        Line line = (Line) lineNode;
                        AnchorPane linesPane = originalTreeBox.getLinesPane();
                        UnderlineMod underlineModel = new UnderlineMod(linesPane.getLeftAnchor(line), line.getEndX(), linesPane.getBottomAnchor(line)  );
                        treeBoxMod.getUnderlineList().add(underlineModel);
                    }
                }
                treeBoxMod.setBaseline(originalTreeBox.getBaseline());
                model.getParseTreeBoxes().add(treeBoxMod);
            }

            else if (node instanceof FormulaTreeBox) {
                FormulaTreeBox originalTreeBox = (FormulaTreeBox) node;
                TreeBoxMod newTreeMod = new TreeBoxMod();
                newTreeMod.setIdString(originalTreeBox.getIdString());
                newTreeMod.setLayoutX(originalTreeBox.getLayoutX());
                newTreeMod.setLayoutY(originalTreeBox.getLayoutY());
                newTreeMod.setLinkIdStrings(originalTreeBox.getmLinkIds());
                newTreeMod.setGray(originalTreeBox.isGray());

                BoxedDRTA treeFormulaBox = originalTreeBox.getFormulaBox();
                RichTextArea treeRTA = treeFormulaBox.getRTA();
                newTreeMod.setPrintWidth(treeRTA.getPrefWidth());

                treeRTA.getActionFactory().saveNow().execute(new ActionEvent());
                newTreeMod.setText(treeRTA.getDocument());

                newTreeMod.setRtaBoundsHeight(originalTreeBox.getRtaBoundsHeight());
                newTreeMod.setRtaBoundsMinY(originalTreeBox.getRtaBoundsMinY());

                model.getFormulaTreeBoxes().add(newTreeMod);
            }

            else if (node instanceof TTClickableNodeLink) {
                TTClickableNodeLink cLink = (TTClickableNodeLink) node;
                ClickableNodeLinkMod cMod = new ClickableNodeLinkMod(cLink.getIdString(), cLink.getTargetId(), cLink.getSourceId());
                model.getClickableNodeLinks().add(cMod);
            }
        }

        model.setPointsPossible(transTreeModel.getPointsPossible());
        TextField pointsField = transTreeView.getPointsEarnedTextField();
        if ((pointsField.isFocused() || transTreeModel.getPointsEarned() >= 0) && pointsField.getText().isEmpty()) {
            pointsField.setText("0");
        }
        if (!pointsField.getText().equals("")) model.setPointsEarned(Integer.parseInt(pointsField.getText()));
        else model.setPointsEarned(-1);

        return model;
    }

    public MainWindow getMainWindow() {
        return mainWindow;
    }

    public Exercise getAuxExerciseA() {
        return auxExerciseA;
    }

    public Exercise getAuxExerciseB() {
        return auxExerciseB;
    }

    // SCORCHED EARTH RULE 6: Sever Shared Nodes Helper
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
        // SCORCHED EARTH RULE 2: Engage Kill Switch
        isTornDown = true;

        // SCORCHED EARTH RULE 7: Safe Global State & Null-Poisoning Prevention
        try {
            if (mainView != null) {
                mainView.textFieldInFocus(); // Remove toolbars for RTAs safely! (No editorInFocus(null) calls)
            }
        } catch (Exception e) {}

        // SCORCHED EARTH RULE 5: Rule of Isolation applied heavily below via multiple try-catch blocks
        try {
            if (undoRedoFlag != null && undoRedoFlagListener != null) {
                undoRedoFlag.removeListener(undoRedoFlagListener);
            }
        } catch (Exception e) {}

        try {
            if (undoRedoFlag != null) {
                undoRedoFlag.unbind();
            }
        } catch (Exception e) {}

        try {
            if (transTreeView != null && transTreeView.localScalePageHeightProperty() != null) {
                transTreeView.localScalePageHeightProperty().unbind();
            }
        } catch (Exception e) {}

        try {
            if (transTreeView != null && transTreeView.localScalePageWidthProperty() != null) {
                transTreeView.localScalePageWidthProperty().unbind();
            }
        } catch (Exception e) {}

        try {
            if (transTreeView != null && mainView != null && mainView.getMainScene() != null && transTreeView.getExerciseFocusListener() != null) {
                mainView.getMainScene().focusOwnerProperty().removeListener(transTreeView.getExerciseFocusListener());
            }
        } catch (Exception e) {}

        try {
            if (transTreeView != null && transTreeView.getPointsEarnedTextField() != null && pointsEarnedListener != null) {
                transTreeView.getPointsEarnedTextField().textProperty().removeListener(pointsEarnedListener);
            }
        } catch (Exception e) {}

        try {
            if (transTreeView != null && transTreeView.getExerciseStatement() != null && transTreeView.getExerciseStatement().getEditor() != null) {
                if (statementFocusListener != null) transTreeView.getExerciseStatement().getEditor().focusedProperty().removeListener(statementFocusListener);
            }
        } catch (Exception e) {}

        try {
            if (transTreeView != null && transTreeView.getExerciseComment() != null && transTreeView.getExerciseComment().getEditor() != null) {
                RichTextArea editor = transTreeView.getExerciseComment().getEditor();
                if (commentFullHeightListener != null) editor.fullHeightProperty().removeListener(commentFullHeightListener);
                if (commentModifiedListener != null) editor.modifiedProperty().removeListener(commentModifiedListener);
                if (commentFocusListener != null) editor.focusedProperty().removeListener(commentFocusListener);
            }
        } catch (Exception e) {}

        try {
            if (transTreeView != null && transTreeView.getInterpFnDRTA() != null && transTreeView.getInterpFnDRTA().getEditor() != null) {
                RichTextArea editor = transTreeView.getInterpFnDRTA().getEditor();
                if (interpFnFullHeightListener != null) editor.fullHeightProperty().removeListener(interpFnFullHeightListener);
                if (interpFnModifiedListener != null) editor.modifiedProperty().removeListener(interpFnModifiedListener);
                if (interpFnFocusListener != null) editor.focusedProperty().removeListener(interpFnFocusListener);
            }
        } catch (Exception e) {}

        try {
            if (transTreeView != null && transTreeView.getUndoButton() != null) {
                if (undoActionHandler != null) transTreeView.getUndoButton().removeEventHandler(ActionEvent.ACTION, undoActionHandler);
                transTreeView.getUndoButton().setOnAction(null);
            }
        } catch (Exception e) {}

        try {
            if (transTreeView != null && transTreeView.getRedoButton() != null) {
                if (redoActionHandler != null) transTreeView.getRedoButton().removeEventHandler(ActionEvent.ACTION, redoActionHandler);
                transTreeView.getRedoButton().setOnAction(null);
            }
        } catch (Exception e) {}

        try {
            if (transTreeView != null && transTreeView.getOmitTreesCheckBox() != null) {
                if (omitTreesListener != null) transTreeView.getOmitTreesCheckBox().selectedProperty().removeListener(omitTreesListener);
                transTreeView.getOmitTreesCheckBox().selectedProperty().unbind();
            }
        } catch (Exception e) {}

        try {
            if (parseTreeTextLengthListeners != null) {
                for (Map.Entry<RichTextArea, ChangeListener<Number>> entry : parseTreeTextLengthListeners.entrySet()) {
                    try { entry.getKey().textLengthProperty().removeListener(entry.getValue()); } catch (Exception e) {}
                }
                parseTreeTextLengthListeners.clear();
            }
        } catch (Exception e) {}

        try {
            if (thumbPane != null) {
                if (thumbPaneMouseClickedHandler != null) thumbPane.removeEventHandler(MouseEvent.MOUSE_CLICKED, thumbPaneMouseClickedHandler);
                thumbPane.setOnMouseClicked(null);
                severFromParent(thumbPane);
                thumbPane.getChildren().clear();
            }
        } catch (Exception e) {}

        // ---> SCORCHED EARTH RULE 4: THE ISOLATED JAVAFX SKIN HAMMER (With Synchronous Unbinding Only) <---
        try {
            if (transTreeView != null) {
                // Capture the editors before clearing ties
                final RichTextArea stEditor = (transTreeView.getExerciseStatement() != null) ? transTreeView.getExerciseStatement().getEditor() : null;
                final RichTextArea commEditor = (transTreeView.getExerciseComment() != null) ? transTreeView.getExerciseComment().getEditor() : null;
                final RichTextArea interpEditor = (transTreeView.getInterpFnDRTA() != null) ? transTreeView.getInterpFnDRTA().getEditor() : null;

                try { if (stEditor != null) { stEditor.prefHeightProperty().unbind(); stEditor.minWidthProperty().unbind(); stEditor.maxWidthProperty().unbind(); } } catch (Exception e) {}
                try { if (commEditor != null) { commEditor.prefHeightProperty().unbind(); commEditor.minWidthProperty().unbind(); commEditor.maxWidthProperty().unbind(); } } catch (Exception e) {}
                try { if (interpEditor != null) { interpEditor.prefHeightProperty().unbind(); interpEditor.minWidthProperty().unbind(); interpEditor.maxWidthProperty().unbind(); } } catch (Exception e) {}

                // SCORCHED EARTH RULE 8: Rebuild Fix (Prevent CME inside dynamic nodes)
                try {
                    if (transTreeView.getRootLayout() != null && transTreeView.getRootLayout().getMain_pane() != null) {
                        List<Node> childrenCopy = new ArrayList<>(transTreeView.getRootLayout().getMain_pane().getChildren());

                        for (Node node : childrenCopy) {
                            try {
                                if (node instanceof ParseTreeBox) ((ParseTreeBox) node).cleanup();
                                else if (node instanceof FormulaTreeBox) ((FormulaTreeBox) node).cleanup();
                                else {
                                    try { node.getClass().getMethod("cleanup").invoke(node); } catch (Exception e) {}
                                }
                                severFromParent(node);
                            } catch (Exception ex) {}
                        }
                        transTreeView.getRootLayout().getMain_pane().getChildren().clear();
                    }
                } catch (Exception e) {}
            }
        } catch (Exception e) {}

        // DISMANTLE THE UI TREE
        try {
            if (transTreeView != null && transTreeView.getControlBox() != null) {
                severFromParent(transTreeView.getControlBox());
                transTreeView.getControlBox().getChildren().clear();
            }
        } catch (Exception e) {}

        // Clear OS-level popups
        try { clearStandingPopups(); } catch (Exception e) {}

        // SCORCHED EARTH RULE 1: Cascade Cleanup to Views, Checks, and Auxillary Models
        try { if (auxExerciseA != null) auxExerciseA.cleanup(); } catch (Exception e) {}
        try { if (auxExerciseB != null) auxExerciseB.cleanup(); } catch (Exception e) {}
        try { if (transTreeView != null) transTreeView.cleanup(); } catch (Exception e) {}
        try { if (transTreeCheck != null) transTreeCheck.cleanup(); } catch (Exception e) {}

        // SCORCHED EARTH RULE 1 & 10: Final Nullification and Non-Destructive List Teardown (No .clear() on undoRedoList)
        try { this.transTreeModel = null; } catch (Exception e) {}
        try { this.mainWindow = null; } catch (Exception e) {}
        try { this.mainView = null; } catch (Exception e) {}
        try { this.transTreeCheck = null; } catch (Exception e) {}
        try { this.undoRedoList = null; } catch (Exception e) {} // ONLY NULLIFY, DO NOT CALL .clear()
        try { this.undoRedoFlag = null; } catch (Exception e) {}
        try { this.auxExerciseA = null; } catch (Exception e) {}
        try { this.auxExerciseB = null; } catch (Exception e) {}
        try { this.transTreeView = null; } catch (Exception e) {}
        try { this.tStage = null; } catch (Exception e) {}
        try { this.thumbPane = null; } catch (Exception e) {}
        try { this.tScene = null; } catch (Exception e) {}

        try { this.undoRedoFlagListener = null; } catch (Exception e) {}
        try { this.pointsEarnedListener = null; } catch (Exception e) {}
        try { this.statementFocusListener = null; } catch (Exception e) {}
        try { this.commentFullHeightListener = null; } catch (Exception e) {}
        try { this.commentModifiedListener = null; } catch (Exception e) {}
        try { this.commentFocusListener = null; } catch (Exception e) {}
        try { this.interpFnFullHeightListener = null; } catch (Exception e) {}
        try { this.interpFnModifiedListener = null; } catch (Exception e) {}
        try { this.interpFnFocusListener = null; } catch (Exception e) {}
        try { this.undoActionHandler = null; } catch (Exception e) {}
        try { this.redoActionHandler = null; } catch (Exception e) {}
        try { this.omitTreesListener = null; } catch (Exception e) {}
        try { this.thumbPaneMouseClickedHandler = null; } catch (Exception e) {}
        try { this.parseTreeTextLengthListeners = null; } catch (Exception e) {}
    }


    @Override
    public String getAuxExerName() {
        if (transTreeModel == null || transTreeModel.getCheckSetup() == null) return "";
        return getExerciseModel().getAuxExerName();
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
        if (transTreeView != null && transTreeView.getStaticHelpStage() != null) transTreeView.getStaticHelpStage().close();
        if (tStage != null) tStage.close();
    }

    @Override
    public Node getAuxExerNode() {
        if (transTreeView == null || transTreeView.getInterpFnDRTA() == null || transTreeView.getRootLayout() == null) return new VBox();
        RichTextArea interpFnRTA = transTreeView.getInterpFnDRTA().getEditor();
        interpFnRTA.prefHeightProperty().unbind();
        if (transTreeModel != null) {
            interpFnRTA.setPrefHeight(transTreeModel.getInterpFnTextHeight() + RTA_SCREEN_BMP);
        }

        return new VBox(5, transTreeView.getRootLayout().getMain_pane(), transTreeView.getInterpFnDRTA().getEditor() );
    }

    @Override
    public void editorInFocus(DecoratedRTA decoratedRTA, ControlType control) {
        if (isTornDown || mainView == null) return;
        mainView.editorInFocus(decoratedRTA, control);
    }

    @Override
    public DoubleProperty getScalePageWidth() {
        if (isTornDown || mainView == null) {
            return new SimpleDoubleProperty(1.0);
        }
        return mainView.scalePageWidthProperty();
    }
}
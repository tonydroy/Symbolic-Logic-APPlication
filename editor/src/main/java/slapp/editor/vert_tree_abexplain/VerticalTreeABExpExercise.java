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

package slapp.editor.vert_tree_abexplain;

import com.gluonhq.richtextarea.RichTextArea;
import com.gluonhq.richtextarea.model.Document;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import org.apache.commons.lang3.SerializationUtils;
import slapp.editor.DiskUtilities;
import slapp.editor.PrintUtilities;
import slapp.editor.decorated_rta.BoxedDRTA;
import slapp.editor.decorated_rta.DecoratedRTA;
import slapp.editor.main_window.*;
import slapp.editor.vertical_tree.VerticalTreeExercise;
import slapp.editor.vertical_tree.VerticalTreeModel;
import slapp.editor.vertical_tree.VerticalTreeView;
import slapp.editor.vertical_tree.drag_drop.DragIconType;
import slapp.editor.vertical_tree.object_models.*;

import java.util.ArrayList;
import java.util.List;

public class VerticalTreeABExpExercise implements Exercise<VerticalTreeABExpModel, VerticalTreeABExpView> {

    MainWindow mainWindow;
    MainWindowView mainView;
    VerticalTreeABExpModel verticalTreeABExpModel;
    VerticalTreeABExpView verticalTreeABExpView;
    private boolean exerciseModified = false;
    private UndoRedoList<VerticalTreeABExpModel> undoRedoList = new UndoRedoList<>(50);
    public BooleanProperty undoRedoFlag = new SimpleBooleanProperty();


    public VerticalTreeABExpExercise(VerticalTreeABExpModel model, MainWindow mainWindow) {
        this.mainWindow = mainWindow;
        this.verticalTreeABExpModel = model;
        if (model.getOriginalModel() == null) {model.setOriginalModel(model); }
        this.mainView = mainWindow.getMainView();
        this.verticalTreeABExpView = new VerticalTreeABExpView(mainView);
        setVerticalTreeView();
        undoRedoFlag.set(false);
        undoRedoFlag.bind(verticalTreeABExpView.undoRedoFlagProperty());
        undoRedoFlag.addListener((ob, ov, nv) -> {
            if (nv) {
                exerciseModified = true;
                pushUndoRedo();
            }
        });
        pushUndoRedo();
    }

    private void setVerticalTreeView() {
        verticalTreeABExpView.setExplainPrompt(verticalTreeABExpModel.getExplainPrompt());
        verticalTreeABExpView.setDefaultKeyboard(verticalTreeABExpModel.getDefaultKeyboardType());
        verticalTreeABExpView.setStatementPrefHeight(verticalTreeABExpModel.getStatementPrefHeight());
        verticalTreeABExpView.setCommentPrefHeight(verticalTreeABExpModel.getCommentPrefHeight());
        verticalTreeABExpView.setExplainPrefHeight(verticalTreeABExpModel.getExplainPrefHeight());
        verticalTreeABExpView.setMainPanePrefHeight(verticalTreeABExpModel.getMainPanePrefHeight());
        verticalTreeABExpView.setMainPanePrefWidth(verticalTreeABExpModel.getMainPanePrefWidth());

        //Statement
        DecoratedRTA statementDRTA = new DecoratedRTA();
        RichTextArea statementEditor = statementDRTA.getEditor();
        statementEditor.getActionFactory().open(verticalTreeABExpModel.getExerciseStatement()).execute(new ActionEvent());

        statementEditor.focusedProperty().addListener((o, ov, nv) -> {
            if (nv) {
                mainView.editorInFocus(statementDRTA, ControlType.STATEMENT);
            }
        });
        verticalTreeABExpView.setExerciseStatement(statementDRTA);

        //comment
        DecoratedRTA commentDRTA = new DecoratedRTA();
        RichTextArea commentEditor = commentDRTA.getEditor();
        commentEditor.setPromptText("Comment: ");
        commentEditor.getActionFactory().open(verticalTreeABExpModel.getExerciseComment()).execute(new ActionEvent());
        mainView.editorInFocus(commentDRTA, ControlType.AREA);
        commentEditor.addEventHandler(KeyEvent.KEY_RELEASED, e -> {
            exerciseModified = true;
            double commentTextHeight = mainView.getRTATextHeight(commentEditor);
            verticalTreeABExpModel.setCommentTextHeight(commentTextHeight);
        });
        commentEditor.focusedProperty().addListener((o, ov, nv) -> {
            if (nv) {
                mainView.editorInFocus(commentDRTA, ControlType.AREA);
            }
        });
        verticalTreeABExpView.setExerciseComment(commentDRTA);

        //choices
        verticalTreeABExpView.getChoiceLeadLabel().setText(verticalTreeABExpModel.getChoiceLead());
        CheckBox aCheckBox = verticalTreeABExpView.getaCheckBox();
        CheckBox bCheckBox = verticalTreeABExpView.getbCheckBox();
        aCheckBox.setText(verticalTreeABExpModel.getaPrompt());
        aCheckBox.setSelected(verticalTreeABExpModel.isaSelected());
        aCheckBox.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue ob, Boolean ov, Boolean nv) {
                if (nv == true) bCheckBox.setSelected(false);
                exerciseModified = true;
            }
        });
        bCheckBox.setText(verticalTreeABExpModel.getbPrompt());
        bCheckBox.setSelected(verticalTreeABExpModel.isbSelected());
        bCheckBox.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue ob, Boolean ov, Boolean nv) {
                if (nv == true) aCheckBox.setSelected(false);
                exerciseModified = true;
            }
        });

        //explain
        DecoratedRTA explainDRTA = verticalTreeABExpView.getExplainDRTA();
        RichTextArea explainEditor = explainDRTA.getEditor();
        explainEditor.getActionFactory().open(verticalTreeABExpModel.getExplainDocument()).execute(new ActionEvent());
        explainEditor.setPromptText("Explain:");
        explainEditor.getActionFactory().saveNow().execute(new ActionEvent());
        mainView.editorInFocus(explainDRTA, ControlType.AREA);
        explainEditor.addEventHandler(KeyEvent.KEY_RELEASED, e -> {
            exerciseModified = true;
            double explainTextHeight = mainView.getRTATextHeight(explainEditor);
            verticalTreeABExpModel.setExplainTextHeight(explainTextHeight);
        });
        explainEditor.focusedProperty().addListener((o, ov, nv) -> {
            if (nv) {
                mainView.editorInFocus(explainDRTA, ControlType.AREA);
            }
        });

        //cleanup
        verticalTreeABExpView.getUndoButton().setOnAction(e -> undoAction());
        verticalTreeABExpView.getRedoButton().setOnAction(e -> redoAction());

        populateControlBox();
        for (DragIconType type : verticalTreeABExpModel.getDragIconList()) {
            verticalTreeABExpView.getRootLayout().addDragIcon(type);
        }
        populateMainPaneNodes();
        verticalTreeABExpView.initializeViewDetails();
    }

    private void populateMainPaneNodes() {
        AnchorPane mainPane = verticalTreeABExpView.getRootLayout().getMain_pane();
        mainPane.getChildren().clear();

        for (VerticalBracketMod bracketMod : verticalTreeABExpModel.getVerticalBrackets()) {
            ABExpVerticalBracket bracket = new ABExpVerticalBracket(verticalTreeABExpView);
            mainPane.getChildren().add(bracket);
            bracket.setLayoutX(bracketMod.getLayoutX());
            bracket.setLayoutY(bracketMod.getLayoutY());
            bracket.getMainPane().setPrefHeight(bracketMod.getHeight());
        }

        for (DashedLineMod dlMod : verticalTreeABExpModel.getDashedLineMods()) {
            ABExpDashedLine dashedLine = new ABExpDashedLine(verticalTreeABExpView);
            mainPane.getChildren().add(dashedLine);
            dashedLine.setLayoutX(dlMod.getLayoutX());
            dashedLine.setLayoutY(dlMod.getLayoutY());
            dashedLine.getMainPane().setPrefWidth(dlMod.getWidth());
        }

        for (MapFormulaBoxMod mapBoxMod : verticalTreeABExpModel.getMapFormulaBoxes()) {
            ABExpMapFormulaBox mapFormulaBox = new ABExpMapFormulaBox(verticalTreeABExpView);
            mainPane.getChildren().add(mapFormulaBox);
            mapFormulaBox.setLayoutX(mapBoxMod.getLayoutX());
            mapFormulaBox.setLayoutY(mapBoxMod.getLayoutY());
            mapFormulaBox.setIdString(mapBoxMod.getIdString());
            mapFormulaBox.setmLinkIds(mapBoxMod.getLinkIdStrings());

            BoxedDRTA formulaBox = mapFormulaBox.getFormulaBox();
            RichTextArea mapBoxRTA = formulaBox.getRTA();
//            mapBoxRTA.setPrefWidth(mapBoxMod.getWidth());
            mapBoxRTA.getActionFactory().open(mapBoxMod.getText()).execute(new ActionEvent());
            mapBoxRTA.getActionFactory().saveNow().execute(new ActionEvent());
        }

        for (TreeFormulaBoxMod treeBoxMod : verticalTreeABExpModel.getTreeFormulaBoxes()) {
            ABExpTreeFormulaBox treeFormulaBox = new ABExpTreeFormulaBox(verticalTreeABExpView);
            mainPane.getChildren().add(treeFormulaBox);
            treeFormulaBox.setLayoutX(treeBoxMod.getLayoutX());
            treeFormulaBox.setLayoutY(treeBoxMod.getLayoutY());
            treeFormulaBox.setIdString(treeBoxMod.getIdString());
            treeFormulaBox.setmLinkIds(treeBoxMod.getLinkIdStrings());

            BoxedDRTA treeFormulaDRTA = treeFormulaBox.getFormulaBox();
            RichTextArea treeBoxRTA = treeFormulaDRTA.getRTA();
//            treeBoxRTA.setPrefWidth(treeBoxMod.getWidth());
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
        for (ClickableNodeLinkMod nodeLinkMod : verticalTreeABExpModel.getClickableNodeLinks()) {
            ABExpClickableNodeLink nodeLink = new ABExpClickableNodeLink(verticalTreeABExpView);
            mainPane.getChildren().add(nodeLink);
            nodeLink.setId(nodeLinkMod.getIdString());
            ABExpTreeFormulaBox source = null;
            ABExpTreeFormulaBox target = null;
            for (Node node : nodesList) {
                if (node instanceof ABExpTreeFormulaBox) {
                    ABExpTreeFormulaBox treeBox = (ABExpTreeFormulaBox) node;
                    if (treeBox.getIdString().equals(nodeLinkMod.getSourceId())) source = treeBox;
                    if (treeBox.getIdString().equals(nodeLinkMod.getTargetId())) target = treeBox;
                }
            }
            if (source != null && target != null) {nodeLink.bindEnds(source, target); }

        }

        for (MapQuestionMarkerMod mapQuestMod : verticalTreeABExpModel.getMapQuestionMarkers()) {
            ABExpMapQuestionMarker mapQuestion = new ABExpMapQuestionMarker(verticalTreeABExpView);
            mainPane.getChildren().add(mapQuestion);
            mapQuestion.setId(mapQuestMod.getIdString());

            for (Node node : nodesList) {
                if (node instanceof ABExpMapFormulaBox) {
                    ABExpMapFormulaBox mapBox = (ABExpMapFormulaBox) node;
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

        for (ClickableMapLinkMod mapLinkMod : verticalTreeABExpModel.getClickableMapLinks()) {
            ABExpClickableMapLink mapLink = new ABExpClickableMapLink(verticalTreeABExpView);
            mainPane.getChildren().add(0, mapLink);
            mapLink.setId(mapLinkMod.getIdString());

            ABExpMapFormulaBox source = null;
            ABExpMapFormulaBox target = null;
            for (Node node : nodesList) {
                if (node instanceof ABExpMapFormulaBox) {
                    ABExpMapFormulaBox mapFormulaBox = (ABExpMapFormulaBox) node;
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

    }

    private void populateControlBox() {
        VBox controlBox = verticalTreeABExpView.getControlBox();
        ABExpRootLayout layout = verticalTreeABExpView.getRootLayout();
        ToggleGroup buttonGroup = verticalTreeABExpView.getRootLayout().getButtonGroup();

        for (ObjectControlType type : verticalTreeABExpModel.getObjectControlList()) {
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
                    controlBox.setMargin(layout.getAnnotationBox(), new Insets(20, 0, -15, 0));  //don't understand weird spacing this "papers over"
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
                    controlBox.setMargin(mappingButton, new Insets(10, 0, 0, 0));  //don't understand spacing this "papers over"
                }
            }
        }
    }


    private void undoAction() {
        VerticalTreeABExpModel undoElement = undoRedoList.getUndoElement();
        if (undoElement != null) {
            verticalTreeABExpModel = (VerticalTreeABExpModel) SerializationUtils.clone(undoElement);
            populateMainPaneNodes();
            updateUndoRedoButtons();
        }
    }

    private void redoAction() {
        VerticalTreeABExpModel redoElement = undoRedoList.getRedoElement();
        if (redoElement != null) {
            verticalTreeABExpModel = (VerticalTreeABExpModel) SerializationUtils.clone(redoElement);
            populateMainPaneNodes();
            updateUndoRedoButtons();
        }
    }

    private void updateUndoRedoButtons() {
        verticalTreeABExpView.getUndoButton().setDisable(!undoRedoList.canUndo());
        verticalTreeABExpView.getRedoButton().setDisable(!undoRedoList.canRedo());
    }

    private void pushUndoRedo() {
        VerticalTreeABExpModel model = getVerticalTreeModelFromView();
        VerticalTreeABExpModel deepCopy = (VerticalTreeABExpModel) SerializationUtils.clone(model);
        undoRedoList.push(deepCopy);
        updateUndoRedoButtons();
    }


    @Override
    public VerticalTreeABExpModel getExerciseModel() {  return verticalTreeABExpModel;    }
    @Override
    public VerticalTreeABExpView getExerciseView() {  return verticalTreeABExpView;    }

    @Override
    public void saveExercise(boolean saveAs) {
        boolean success = DiskUtilities.saveExercise(saveAs, getVerticalTreeModelFromView());
        if (success) exerciseModified = false;
    }

    @Override
    public List<Node> getPrintNodes(ExerciseModel originalModel) {
        List<Node> nodeList = new ArrayList<>();
        VerticalTreeABExpModel printModel = verticalTreeABExpModel;
        VerticalTreeABExpExercise printExercise = this;

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

        //content node
        AnchorPane mainPane = printExercise.getExerciseView().getRootLayout().getMain_pane();
        mainPane.setStyle("-fx-background-color: transparent");

        VerticalTreeABExpModel originalVTmod = (VerticalTreeABExpModel) originalModel;
        mainPane.setMinWidth(originalVTmod.getMainPanePrefWidth());


        ObservableList<Node> nodes = mainPane.getChildren();
        for (Node node : nodes) {
            if (node instanceof ABExpTreeFormulaBox) {
                ABExpTreeFormulaBox treeBox = (ABExpTreeFormulaBox) node;
                treeBox.getFormulaBox().getRTA().setStyle("-fx-border-color: transparent");
                if (treeBox.getAnnotationField() != null) treeBox.getAnnotationField().getRTA().setStyle("-fx-border-color: transparent");
            }
            if (node instanceof ABExpMapFormulaBox) {
                ((ABExpMapFormulaBox) node).getFormulaBox().getRTA().setStyle("-fx-border-color: transparent");
            }
        }

        HBox contentHBox = new HBox(mainPane);
        contentHBox.setAlignment(Pos.CENTER);
        contentHBox.setPadding(new Insets(0,0,20, 0));
        nodeList.add(contentHBox);

        Label leaderLabel = new Label(verticalTreeABExpModel.getChoiceLead());
        CheckBox boxA = new CheckBox(verticalTreeABExpModel.getaPrompt());
        boxA.setSelected(verticalTreeABExpModel.isaSelected());
        CheckBox boxB = new CheckBox(verticalTreeABExpModel.getbPrompt());
        boxB.setSelected(verticalTreeABExpModel.isbSelected());
        Font labelFont = new Font("Noto Serif Combo", 11);
        leaderLabel.setFont(labelFont); boxA.setFont(labelFont); boxB.setFont(labelFont);

        HBox abBox = new HBox(20);
        abBox.setPadding(new Insets(10,10,10,0));
        abBox.getChildren().addAll(leaderLabel, boxA, boxB);
        nodeList.add(abBox);


        RichTextArea explanationRTA = printExercise.getExerciseView().getExplainDRTA().getEditor();
        explanationRTA.prefHeightProperty().unbind();
        explanationRTA.minWidthProperty().unbind();
        explanationRTA.setPrefHeight(printModel.getExplainTextHeight() + 35.0);
        explanationRTA.setContentAreaWidth(nodeWidth);
        explanationRTA.setMinWidth(nodeWidth);
        explanationRTA.getStylesheets().clear(); statementRTA.getStylesheets().add("richTextAreaPrinter.css");
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
        commentRTA.setPrefHeight(printModel.getCommentTextHeight() + 35.0);
        commentRTA.setContentAreaWidth(nodeWidth);
        commentRTA.setMinWidth(nodeWidth);
        commentRTA.getStylesheets().clear(); commentRTA.getStylesheets().add("richTextAreaPrinter.css");
        nodeList.add(commentRTA);

        return nodeList;
    }

    @Override
    public Exercise<VerticalTreeABExpModel, VerticalTreeABExpView> resetExercise() {
        RichTextArea commentRTA = verticalTreeABExpView.getExerciseComment().getEditor();
        commentRTA.getActionFactory().saveNow().execute(new ActionEvent());
        Document commentDocument = commentRTA.getDocument();
        VerticalTreeABExpModel originalModel = (VerticalTreeABExpModel) (verticalTreeABExpModel.getOriginalModel());
        originalModel.setExerciseComment(commentDocument);
        VerticalTreeABExpExercise clearExercise = new VerticalTreeABExpExercise(originalModel, mainWindow);
        return clearExercise;
    }

    @Override
    public boolean isExerciseModified() {
        RichTextArea commentEditor = verticalTreeABExpView.getExerciseComment().getEditor();
        if (commentEditor.isModified()) {   exerciseModified = true;      }

        RichTextArea explanationEditor = verticalTreeABExpView.getExplainDRTA().getEditor();
        if (explanationEditor.isModified()) exerciseModified = true;

        ObservableList<Node> nodes = verticalTreeABExpView.getRootLayout().getMain_pane().getChildren();
        for (Node node : nodes) {
            if (node instanceof ABExpTreeFormulaBox) {
                ABExpTreeFormulaBox treeBox = (ABExpTreeFormulaBox) node;
                if (treeBox.getFormulaBox().getRTA().isModified()) exerciseModified = true;
            }
            if (node instanceof ABExpMapFormulaBox) {
                ABExpMapFormulaBox mapBox = (ABExpMapFormulaBox) node;
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
    public ExerciseModel<VerticalTreeABExpModel> getExerciseModelFromView() {
        return (ExerciseModel) getVerticalTreeModelFromView();
    }

    private VerticalTreeABExpModel getVerticalTreeModelFromView() {
        VerticalTreeABExpModel model = new VerticalTreeABExpModel();

        model.setExerciseName(verticalTreeABExpModel.getExerciseName());
        model.setDefaultKeyboardType(verticalTreeABExpModel.getDefaultKeyboardType());
        model.setExplainPrompt(verticalTreeABExpModel.getExplainPrompt());
        model.setOriginalModel(verticalTreeABExpModel.getOriginalModel());
        model.setChoiceLead(verticalTreeABExpModel.getChoiceLead());
        model.setaPrompt(verticalTreeABExpModel.getaPrompt());
        model.setaSelected(verticalTreeABExpView.getaCheckBox().isSelected());
        model.setbPrompt(verticalTreeABExpModel.getbPrompt());
        model.setbSelected(verticalTreeABExpView.getbCheckBox().isSelected());

        model.setStatementPrefHeight(verticalTreeABExpView.getExerciseStatement().getEditor().getPrefHeight());
        model.setCommentPrefHeight(verticalTreeABExpView.getCommentPrefHeight());
        model.setExplainPrefHeight(verticalTreeABExpView.getExplainPrefHeight());
        model.setStatementTextHeight(verticalTreeABExpModel.getStatementTextHeight());
        model.setCommentTextHeight(verticalTreeABExpModel.getCommentTextHeight());
        model.setExplainTextHeight(verticalTreeABExpModel.getExplainTextHeight());
        model.setExerciseStatement(verticalTreeABExpModel.getExerciseStatement());

        model.setMainPanePrefHeight(verticalTreeABExpView.getMainPanePrefHeight());
        model.setMainPanePrefWidth(verticalTreeABExpView.getRootLayout().getMain_pane().getWidth());

        model.setDragIconList(verticalTreeABExpModel.getDragIconList());
        model.setObjectControlList(verticalTreeABExpModel.getObjectControlList());
        model.setStarted(verticalTreeABExpModel.isStarted() || exerciseModified);

        RichTextArea commentRTA = verticalTreeABExpView.getExerciseComment().getEditor();
        commentRTA.getActionFactory().saveNow().execute(new ActionEvent());
        model.setExerciseComment(commentRTA.getDocument());

        RichTextArea explainRTA = verticalTreeABExpView.getExplainDRTA().getEditor();
        explainRTA.getActionFactory().saveNow().execute(new ActionEvent());
        model.setExplainDocument(explainRTA.getDocument());

        ABExpRootLayout rootLayout = verticalTreeABExpView.getRootLayout();
        AnchorPane mainPane = rootLayout.getMain_pane();
        ObservableList<Node> nodesList = mainPane.getChildren();

        for (Node node : nodesList) {

            if (node instanceof ABExpTreeFormulaBox) {
                ABExpTreeFormulaBox originalTreeBox = (ABExpTreeFormulaBox) node;
                TreeFormulaBoxMod newTreeMod = new TreeFormulaBoxMod();
                newTreeMod.setIdString(originalTreeBox.getIdString());
                newTreeMod.setLayoutX(originalTreeBox.getLayoutX());
                newTreeMod.setLayoutY(originalTreeBox.getLayoutY());
                newTreeMod.setLinkIdStrings(originalTreeBox.getmLinkIds());

                BoxedDRTA treeFormulaBox = originalTreeBox.getFormulaBox();
//                newTreeMod.setWidth(treeFormulaBox.getRTA().getPrefWidth());
                RichTextArea treeRTA = treeFormulaBox.getRTA();
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

            } else if (node instanceof ABExpMapFormulaBox) {
                ABExpMapFormulaBox originalMapBox = (ABExpMapFormulaBox) node;
                MapFormulaBoxMod newMapMod = new MapFormulaBoxMod();
                newMapMod.setIdString(originalMapBox.getIdString());
                newMapMod.setLayoutX(originalMapBox.getLayoutX());
                newMapMod.setLayoutY(originalMapBox.getLayoutY());
                newMapMod.setLinkIdStrings(originalMapBox.getmLinkIds());

                BoxedDRTA formulaBox = originalMapBox.getFormulaBox();
 //               newMapMod.setWidth(formulaBox.getRTA().getPrefWidth());
                RichTextArea mapRTA = formulaBox.getRTA();
                mapRTA.getActionFactory().saveNow().execute(new ActionEvent());
                newMapMod.setText(mapRTA.getDocument());

                model.getMapFormulaBoxes().add(newMapMod);

            } else if (node instanceof ABExpVerticalBracket) {
                ABExpVerticalBracket vBrack = (ABExpVerticalBracket) node;
                VerticalBracketMod brack = new VerticalBracketMod(node.getLayoutX(), node.getLayoutY(), vBrack.getMainPane().getPrefHeight());
                model.getVerticalBrackets().add(brack);

            } else if (node instanceof ABExpDashedLine) {
                ABExpDashedLine dLine = (ABExpDashedLine) node;
                DashedLineMod dlMod = new DashedLineMod(node.getLayoutX(), node.getLayoutY(), dLine.getMainPane().getPrefWidth());
                model.getDashedLineMods().add(dlMod);

            } else if (node instanceof ABExpClickableNodeLink) {
                ABExpClickableNodeLink cLink = (ABExpClickableNodeLink) node;
                ClickableNodeLinkMod cMod = new ClickableNodeLinkMod(cLink.getIdString(), cLink.getTargetId(), cLink.getSourceId());
                model.getClickableNodeLinks().add(cMod);

            } else if (node instanceof ABExpClickableMapLink) {
                ABExpClickableMapLink mapLink = (ABExpClickableMapLink) node;
                ClickableMapLinkMod mapLinkMod = new ClickableMapLinkMod();
                mapLinkMod.setIdString(mapLink.getIdString());
                mapLinkMod.setSourceId(mapLink.getSourceId()); mapLinkMod.setTargetId(mapLink.getTargetId());
                mapLinkMod.setSourceMapStage(mapLink.getSourceMapStage()); mapLinkMod.setTargetMapStage(mapLink.getTargetMapStage());
                mapLinkMod.setSourceXAnchors(mapLink.getSourceXAnchors()); mapLinkMod.setTargetXAnchors(mapLink.getTargetXAnchors());
                model.getClickableMapLinks().add(mapLinkMod);

            } else if (node instanceof ABExpMapQuestionMarker) {
                ABExpMapQuestionMarker mapQuest = (ABExpMapQuestionMarker) node;
                MapQuestionMarkerMod qMod = new MapQuestionMarkerMod(mapQuest.getIdString(), mapQuest.getTargetId(), mapQuest.getTargetMapStage(), mapQuest.getTargetXAnchors());
                model.getMapQuestionMarkers().add(qMod);
            }  
        }
        return model;
    }

}

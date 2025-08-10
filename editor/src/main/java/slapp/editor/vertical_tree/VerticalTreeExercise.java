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
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.event.ActionEvent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
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
import slapp.editor.derivation.CheckSetup;
import slapp.editor.main_window.*;
import slapp.editor.vert_tree_abefexplain.VerticalTreeABEFExpExercise;
import slapp.editor.vert_tree_abefexplain.VerticalTreeABEFExpView;
import slapp.editor.vert_tree_abexplain.VerticalTreeABExpExercise;
import slapp.editor.vert_tree_abexplain.VerticalTreeABExpView;
import slapp.editor.vert_tree_explain.VerticalTreeExpExercise;
import slapp.editor.vert_tree_explain.VerticalTreeExpView;
import slapp.editor.vertical_tree.drag_drop.*;
import slapp.editor.vertical_tree.object_models.*;
import java.util.ArrayList;
import java.util.List;

public class VerticalTreeExercise implements Exercise<VerticalTreeModel, VerticalTreeView>,  VTAuxExer {

    private MainWindow mainWindow;
    private MainWindowView mainView;
    private VerticalTreeModel verticalTreeModel;
    private VerticalTreeView verticalTreeView;
    private VTcheck vtCheck;
    private boolean exerciseModified = false;
    private UndoRedoList<VerticalTreeModel> undoRedoList = new UndoRedoList<>(50);   //50
    public BooleanProperty undoRedoFlag = new SimpleBooleanProperty();
    private Exercise auxExerciseA;
    private Exercise auxExerciseB;
    Stage tStage;
    Pane thumbPane = new Pane();




    public VerticalTreeExercise(VerticalTreeModel model, MainWindow mainWindow) {
        this.mainWindow = mainWindow;
        this.verticalTreeModel = model;
        if (model.getOriginalModel() == null) {model.setOriginalModel(model); }
        this.mainView = mainWindow.getMainView();
        this.verticalTreeView = new VerticalTreeView(mainView, this);

        setVerticalTreeView();

//        if (verticalTreeModel.getTreeFormulaBoxes() == null)
//            verticalTreeModel.setCheckSetup(new VTcheckSetup());

        vtCheck = new VTcheck(this);


        undoRedoFlag.set(false);
        undoRedoFlag.bind(verticalTreeView.undoRedoFlagProperty());
        undoRedoFlag.addListener((ob, ov, nv) -> {
            if (nv) {
                exerciseModified = true;
                pushUndoRedo();
            }
        });
        pushUndoRedo();
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
        verticalTreeView.getPointsEarnedTextField().textProperty().addListener((ob, ov, nv) -> {
            exerciseModified = true;
        });

        //statement
        DecoratedRTA statementDRTA = new DecoratedRTA();
        RichTextArea statementEditor = statementDRTA.getEditor();
        statementEditor.getActionFactory().open(verticalTreeModel.getExerciseStatement()).execute(new ActionEvent());
        statementEditor.focusedProperty().addListener((o, ov, nv) -> {
            if (nv) {
                mainView.editorInFocus(statementDRTA, ControlType.STATEMENT);
            }
        });
        verticalTreeView.setExerciseStatement(statementDRTA);

        //comment
        DecoratedRTA commentDRTA = new DecoratedRTA();
        RichTextArea commentEditor = commentDRTA.getEditor();
        commentEditor.setPromptText("Comment: ");
        commentEditor.getActionFactory().open(verticalTreeModel.getExerciseComment()).execute(new ActionEvent());
        mainView.editorInFocus(commentDRTA, ControlType.AREA);
        commentEditor.addEventHandler(KeyEvent.KEY_RELEASED, e -> {
            exerciseModified = true;
            double commentTextHeight = mainView.getRTATextHeight(commentEditor);
            verticalTreeModel.setCommentTextHeight(commentTextHeight);
        });
        commentEditor.focusedProperty().addListener((o, ov, nv) -> {
            if (nv) {
                mainView.editorInFocus(commentDRTA, ControlType.AREA);
            }
        });
        verticalTreeView.setExerciseComment(commentDRTA);

        //cleanup
        verticalTreeView.getUndoButton().setOnAction(e -> undoAction());
        verticalTreeView.getRedoButton().setOnAction(e -> redoAction());

        populateControlBox();
        for (DragIconType type : verticalTreeModel.getDragIconList()) {
            verticalTreeView.getRootLayout().addDragIcon(type);
        }
        populateMainPaneNodes();
        verticalTreeView.initializeViewDetails();
        verticalTreeView.setRightControlBox();
    }

    private void populateMainPaneNodes() {
        AnchorPane mainPane = verticalTreeView.getRootLayout().getMainPane();
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
//            mapBoxRTA.setPrefWidth(mapBoxMod.getWidth());
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
            if (source != null && target != null) {nodeLink.bindEnds(source, target); }

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

    }

    private void populateControlBox() {
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



        Platform.runLater(() -> {
            if (auxExerciseA != null) {
                thumbPane = auxImage();
                verticalTreeView.getControlBox().getChildren().add(thumbPane);
            }
        });
    }

    private Pane auxImage() {
        AnchorPane mainPane = null;
        Pane thumbPane = new Pane();
        if (auxExerciseA instanceof VTAuxExer) mainPane = ((VTAuxExer) auxExerciseA).getMainPane();

        if(mainPane != null) {
            mainPane.setStyle("-fx-border-color: black; -fx-border-width: 1; -fx-background-color: FAFAFA;");
            thumbPane.getChildren().add(mainPane);

            Group thumbRoot = new Group();
            Scene scene = new Scene(thumbRoot);
            thumbRoot.getChildren().add(thumbPane);
            thumbRoot.applyCss();
            thumbRoot.layout();
            double scale = 85.0 / mainPane.getLayoutBounds().getWidth();   //

            thumbPane.getTransforms().clear();
            thumbPane.getTransforms().add(new Scale(scale, scale));
            thumbPane.setMaxWidth(85);


            thumbPane.setOnMouseClicked(event -> {
                if (tStage == null || !tStage.isShowing()) {
                    Pane tPane = null;
                    if (auxExerciseB instanceof VTAuxExer) tPane = ((VTAuxExer) auxExerciseB).getMainPane();

                    if (tPane != null) {

                        Scene tScene = new Scene(tPane);
                        tPane.setDisable(true);
                        tPane.applyCss();
                        tPane.layout();



                        tStage = new Stage();
                        tStage.setScene(tScene);
                        tStage.setTitle(((ExerciseModel) auxExerciseB.getExerciseModel()).getExerciseName());
                        tStage.initStyle(StageStyle.UTILITY);

                        tStage.initOwner(EditorMain.mainStage);
                        tStage.initModality(Modality.NONE);
                        Stage mainStage = EditorMain.mainStage;
                        tStage.setX(mainStage.getX() + mainStage.getWidth()  - 500);
                        tStage.setY(mainStage.getY() + mainStage.getHeight()  - 300);
                        tStage.show();

             //           tStage.setX(mainStage.getX() + mainStage.getWidth() - tStage.getWidth() + 50);
              //          tStage.setY(mainStage.getY() + mainStage.getHeight() - tStage.getHeight() + 50);
                        Platform.runLater(() -> {
                            tStage.sizeToScene();
                            tStage.setX(mainStage.getX() + mainStage.getWidth() - tStage.getWidth() + 50);
                            tStage.setY(mainStage.getY() + mainStage.getHeight() - tStage.getHeight() + 50);

                        });
                    }
                } else {
                    tStage.close();
                }
            });

        }

        return thumbPane;
    }


    private void undoAction() {
        VerticalTreeModel undoElement = undoRedoList.getUndoElement();
    //    System.out.println("undo: " + undoElement);
        if (undoElement != null) {
            verticalTreeModel = (VerticalTreeModel) SerializationUtils.clone(undoElement);
            populateMainPaneNodes();
            updateUndoRedoButtons();
        }
    }

    private void redoAction() {
        VerticalTreeModel redoElement = undoRedoList.getRedoElement();
        if (redoElement != null) {
            verticalTreeModel = (VerticalTreeModel) SerializationUtils.clone(redoElement);
            populateMainPaneNodes();
            updateUndoRedoButtons();
        }
    }

    private void updateUndoRedoButtons() {
        verticalTreeView.getUndoButton().setDisable(!undoRedoList.canUndo());
        verticalTreeView.getRedoButton().setDisable(!undoRedoList.canRedo());
    }

    public void pushUndoRedo() {

        VerticalTreeModel model = getVerticalTreeModelFromView();
        VerticalTreeModel deepCopy = (VerticalTreeModel) SerializationUtils.clone(model);
        undoRedoList.push(deepCopy);
        updateUndoRedoButtons();
        exerciseModified = true;
    }


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
        AnchorPane mainPane = printExercise.getExerciseView().getRootLayout().getMainPane();
        mainPane.setStyle("-fx-background-color: transparent");

        mainPane.setMinHeight(0);


        VerticalTreeModel originalVTmod = (VerticalTreeModel) printModel;
        mainPane.setMinWidth(originalVTmod.getMainPanePrefWidth());

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
                mapBox.getFormulaBox().getRTA().setStyle("-fx-border-color: transparent");

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

    @Override
    public Exercise<VerticalTreeModel, VerticalTreeView> resetExercise() {
        RichTextArea commentRTA = verticalTreeView.getExerciseComment().getEditor();
        commentRTA.getActionFactory().saveNow().execute(new ActionEvent());
        Document commentDocument = commentRTA.getDocument();
        int pointsEarned = -1;
        if (!verticalTreeView.getPointsEarnedTextField().getText().equals("")) pointsEarned = Integer.parseInt(verticalTreeView.getPointsEarnedTextField().getText());
        VerticalTreeModel originalModel = (VerticalTreeModel) (verticalTreeModel.getOriginalModel());
        originalModel.setExerciseComment(commentDocument);
        originalModel.setPointsEarned(pointsEarned);
        VTcheckSetup setup = originalModel.getCheckSetup();
        setup.setCheckTries(vtCheck.getCheckTries());
        setup.setCheckSuccess(false);
        originalModel.setCheckSetup(setup);


        VerticalTreeExercise clearExercise = new VerticalTreeExercise(originalModel, mainWindow);
        return clearExercise;
    }

    @Override
    public boolean isExerciseModified() {
        RichTextArea commentEditor = verticalTreeView.getExerciseComment().getEditor();
        if (commentEditor.isModified()) {
            exerciseModified = true;
        }
        ObservableList<Node> nodes = verticalTreeView.getRootLayout().getMainPane().getChildren();
        for (Node node : nodes) {
            if (node instanceof TreeFormulaBox) {
                TreeFormulaBox treeBox = (TreeFormulaBox) node;
                if (treeBox.getFormulaBox().getRTA().isModified()) exerciseModified = true;
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

        VerticalTreeModel originalVTmod = (VerticalTreeModel) originalModel;
        mainPane.setMinWidth(originalVTmod.getMainPanePrefWidth());

        ObservableList<Node> nodes = mainPane.getChildren();
        for (Node node : nodes) {
            if (node instanceof TreeFormulaBox) {
                TreeFormulaBox treeBox = (TreeFormulaBox) node;
                treeBox.getFormulaBox().getRTA().setStyle("-fx-border-color: transparent");
                treeBox.getFormulaBox().getRTA().setMinWidth(treeBox.getPrintWidth());
                if (treeBox.getAnnotationField() != null) treeBox.getAnnotationField().getRTA().setStyle("-fx-bprder-color: transparent");
            }
            if (node instanceof MapFormulaBox) {
                ((MapFormulaBox) node).getFormulaBox().getRTA().setStyle("-fx-border-color: transparent");
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

 //       new Exception().printStackTrace();

        VerticalTreeModel model = new VerticalTreeModel();

        model.setExerciseName(verticalTreeModel.getExerciseName());
        model.setDefaultKeyboardType(verticalTreeModel.getDefaultKeyboardType());
        model.setDefaultMapKeyboardType(verticalTreeModel.getDefaultMapKeyboardType());
        model.setOriginalModel(verticalTreeModel.getOriginalModel());
        model.setDragIconList(verticalTreeModel.getDragIconList());
        model.setObjectControlList(verticalTreeModel.getObjectControlList());
        model.setStarted(verticalTreeModel.isStarted() || exerciseModified);

        VTcheckSetup checkSetup = verticalTreeModel.getCheckSetup();
        checkSetup.setCheckTries(vtCheck.getCheckTries());
        checkSetup.setCheckSuccess(vtCheck.isCheckSuccess());
        model.setCheckSetup(verticalTreeModel.getCheckSetup());

        model.setStatementPrefHeight(verticalTreeView.getExerciseStatement().getEditor().getPrefHeight());
        model.setCommentPrefHeight(verticalTreeView.getCommentPrefHeight());
        model.setStatementTextHeight(verticalTreeModel.getStatementTextHeight());
        model.setCommentTextHeight(verticalTreeModel.getCommentTextHeight());
        model.setMainPanePrefHeight(verticalTreeView.getMainPanePrefHeight());


        model.setMainPanePrefWidth(verticalTreeView.getRootLayout().getMainPane().getWidth());
        model.setExerciseStatement(verticalTreeModel.getExerciseStatement());

        RichTextArea commentRTA = verticalTreeView.getExerciseComment().getEditor();
        commentRTA.getActionFactory().saveNow().execute(new ActionEvent());
        model.setExerciseComment(commentRTA.getDocument());

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
                newTreeMod.setPrintWidth(treeRTA.getPrefWidth());     /////////////

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
        if (!verticalTreeView.getPointsEarnedTextField().getText().equals("")) model.setPointsEarned(Integer.parseInt(verticalTreeView.getPointsEarnedTextField().getText()));
        else model.setPointsEarned(-1);

        VTcheckSetup setup = verticalTreeModel.getCheckSetup();
        setup.setCheckSuccess(vtCheck.isCheckSuccess());
        setup.setCheckTries(vtCheck.getCheckTries());
        model.setCheckSetup(setup);
        ///

        return model;
    }

    public MainWindow getMainWindow() {
        return mainWindow;
    }

    @Override
    public String getAuxExerName() {
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
        if (verticalTreeView.getStaticHelpStage() != null) verticalTreeView.getStaticHelpStage().close();
        if (tStage != null) tStage.close();
    }

    @Override
    public AnchorPane getMainPane() {
        return verticalTreeView.getRootLayout().getMainPane();
    }

    @Override
    public VTAuxCheck getVTAuxCheck() {
        return (VTAuxCheck) vtCheck;
    }

    public Exercise getAuxExerciseA() {
        return auxExerciseA;
    }

    public VTcheck getVtCheck() {
        return vtCheck;
    }

    @Override
    public VTAuxExer getVTAuxExer() {
        return (VTAuxExer) auxExerciseA;
    }
}

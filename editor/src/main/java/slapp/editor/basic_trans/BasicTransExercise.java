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

package slapp.editor.basic_trans;

import com.gluonhq.richtextarea.RichTextArea;
import com.gluonhq.richtextarea.RichTextAreaSkin;
import com.gluonhq.richtextarea.model.Document;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
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
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.scene.transform.Scale;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import slapp.editor.DiskUtilities;
import slapp.editor.EditorMain;
import slapp.editor.PrintUtilities;
import slapp.editor.decorated_rta.DecoratedRTA;
import slapp.editor.main_window.*;

import java.util.ArrayList;
import java.util.List;

import static slapp.editor.EditorMain.*;

/**
 * Simple Translate exercise has separate text-entry areas for interpretation and translation.
 */

public class BasicTransExercise implements Exercise<BasicTransModel, BasicTransView> {
    private MainWindow mainWindow;
    private BasicTransModel transModel;
    private BasicTransView transView;
    private MainWindowView mainView;
    private boolean exerciseModified = false;
    private BasicTransCheck basicTransCheck;

    private Exercise auxExerciseA;
    private Exercise auxExerciseB;
    Stage tStage;
    Pane thumbPane = new Pane();
    Scene tScene = new Scene(new Pane());

    List<RichTextArea> rtaNodes;

    // SCORCHED EARTH RULE 2: Kill Switch for asynchronous event interception during teardown
    private boolean isTornDown = false;

    // SCORCHED EARTH RULE 3: Eradicate Anonymous Lambdas
    private ChangeListener<String> pointsEarnedListener;
    private ChangeListener<Boolean> statementFocusListener;
    private ChangeListener<Number> commentFullHeightListener;
    private ChangeListener<Boolean> commentModifiedListener;
    private ChangeListener<Boolean> commentFocusListener;
    private ChangeListener<Number> interpFullHeightListener;
    private ChangeListener<Boolean> interpModifiedListener;
    private ChangeListener<Boolean> interpFocusListener;
    private ChangeListener<Number> responseFullHeightListener;
    private ChangeListener<Boolean> responseModifiedListener;
    private ChangeListener<Boolean> responseFocusListener;
    private EventHandler<MouseEvent> thumbPaneClickHandler;

    /**
     * Construct simple translate exercise
     * @param model a BasicTransModel
     * @param mainWindow the main window
     */
    public BasicTransExercise(BasicTransModel model, MainWindow mainWindow) {
        this.mainWindow = mainWindow;
        this.transModel = model;
        if (model.getOriginalModel() == null) {model.setOriginalModel(model); }
        this.mainView = mainWindow.getMainView();
        this.transView = new BasicTransView(mainView);

        setTransView();
        this.basicTransCheck = new BasicTransCheck(this);

        if (basicTransCheck.isRequireUniv()) {
            transView.getExerciseInterpretation().getKeyboardSelector().valueProperty().setValue(RichTextAreaSkin.KeyMapValue.BASE_AND_SANS);
        }
        else {
            transView.getExerciseInterpretation().getKeyboardSelector().valueProperty().setValue(RichTextAreaSkin.KeyMapValue.BASE);
        }

    }

    /*
     * Set up statement, comment, interpretation, and response view elements
     */
    private void setTransView() {

        transView.setStatementPrefHeight(transModel.getStatementPrefHeight());
        transView.setCommentPrefHeight(transModel.getCommentPrefHeight());
        transView.setResponsePrefHeight(transModel.getResponsePrefHeight());
        transView.setInterpretationPrefHeight(transModel.getInterpFnPrefHeight());
     //  System.out.println("pref height: " + transModel.getInterpFnPrefHeight());


        transView.setPointsPossible(transModel.getPointsPossible());
        if (transModel.getPointsEarned() >= 0) transView.getPointsEarnedTextField().setText(Integer.toString(transModel.getPointsEarned()));

        pointsEarnedListener = (ob, ov, nv) -> {
            if (isTornDown || mainView == null) return;
            exerciseModified = true;
        };
        transView.getPointsEarnedTextField().textProperty().addListener(pointsEarnedListener);

        //statement
        DecoratedRTA statementDRTA = new DecoratedRTA();
        RichTextArea statementEditor = statementDRTA.getEditor();
        statementEditor.getActionFactory().open(transModel.getExerciseStatement()).execute(new ActionEvent());

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
        transView.setExerciseStatement(statementDRTA);

        //comment
        DecoratedRTA commentDRTA = new DecoratedRTA();
        RichTextArea commentEditor = commentDRTA.getEditor();
        commentEditor.getActionFactory().open(transModel.getExerciseComment()).execute(new ActionEvent());
        commentEditor.getActionFactory().saveNow().execute(new ActionEvent());

        final boolean[] commentPrimed = {false};
        commentFullHeightListener = (ob, ov, nv) -> {
            if (isTornDown || mainView == null || transModel == null) return;
            if (!commentPrimed[0]) {
                commentPrimed[0] = true;
                return;
            }
            double height = (double) nv;
            mainView.setRTATextHeight(height);
            transModel.setCommentTextHeight(height);
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
        transView.setExerciseComment(commentDRTA);

        //interpretation function
        DecoratedRTA interpFnDRTA = new DecoratedRTA();
        RichTextArea interpretationEditor = interpFnDRTA.getEditor();

        interpretationEditor.getActionFactory().open(transModel.getInterpFnDoc()).execute(new ActionEvent());
        interpretationEditor.getActionFactory().saveNow().execute(new ActionEvent());

        if (transModel.isInterpFnIncluded()) interpretationEditor.setEditable(false);

        final boolean[] interpPrimed = {false};
        interpFullHeightListener = (ob, ov, nv) -> {
            if (isTornDown || mainView == null || transModel == null) return;
            if (!interpPrimed[0]) {
                interpPrimed[0] = true;
                return;
            }
            double height = (double) nv;
            mainView.setRTATextHeight(height);
            transModel.setInterpFnTextHeight(height);
        };
        interpretationEditor.fullHeightProperty().addListener(interpFullHeightListener);

        interpModifiedListener = (ob, ov, nv) -> {
            if (isTornDown || mainView == null) return;
            if (nv) exerciseModified = true;
        };
        interpretationEditor.modifiedProperty().addListener(interpModifiedListener);

        mainView.editorInFocus(interpFnDRTA, ControlType.AREA);
        interpFocusListener = (o, ov, nv) -> {
            if (isTornDown || mainView == null) return;
            try {
                if (nv) {
                    mainView.editorInFocus(interpFnDRTA, ControlType.AREA);
                    mainView.setRTATextHeight(interpretationEditor.getFullHeight());
                } else mainView.undoRTATextHeight();
            } catch (Exception e) {}
        };
        interpretationEditor.focusedProperty().addListener(interpFocusListener);
        transView.setExerciseInterpretation(interpFnDRTA);

        //response
        DecoratedRTA responseDRTA = new DecoratedRTA();
        RichTextArea responseEditor = responseDRTA.getEditor();

        responseEditor.getActionFactory().open(transModel.getExerciseResponse()).execute(new ActionEvent());
        responseEditor.getActionFactory().saveNow().execute(new ActionEvent());

        final boolean[] responsePrimed = {false};
        responseFullHeightListener = (ob, ov, nv) -> {
            if (isTornDown || mainView == null || transModel == null) return;
            if (!responsePrimed[0]) {
                responsePrimed[0] = true;
                return;
            }
            double height = (double) nv;
            mainView.setRTATextHeight(height);
            transModel.setResponseTextHeight(height);
        };
        responseEditor.fullHeightProperty().addListener(responseFullHeightListener);

        responseModifiedListener = (ob, ov, nv) -> {
            if (isTornDown || mainView == null) return;
            if (nv) exerciseModified = true;
        };
        responseEditor.modifiedProperty().addListener(responseModifiedListener);

        mainView.editorInFocus(responseDRTA, ControlType.AREA);
        responseFocusListener = (o, ov, nv) -> {
            if (isTornDown || mainView == null) return;
            try {
                if (nv) {
                    mainView.editorInFocus(responseDRTA, ControlType.AREA);
                    mainView.setRTATextHeight(responseEditor.getFullHeight());
                } else mainView.undoRTATextHeight();
            } catch (Exception e) {}
        };
        responseEditor.focusedProperty().addListener(responseFocusListener);
        transView.setExerciseResponse(responseDRTA);

        //cleanup
        transView.initializeViewDetails();
        transView.setRightControlBox();

        Platform.runLater(() -> {
            if (isTornDown || mainView == null) return;
            if (mainWindow != null && mainWindow.getCurrentExercise() != this) return;
            if (auxExerciseA != null) {
                thumbPane = auxImage();
                if (transView != null && transView.getControlBox() != null) {
                    transView.getControlBox().getChildren().add(thumbPane);
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

           //         tStage.initOwner(EditorMain.mainStage);
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

    public void clearStandingPopups() {
        if (tStage != null) tStage.close();
        if (tScene != null) tScene.setRoot(new Pane());
    }

    @Override
    public BasicTransModel getExerciseModel() { return transModel; }

    @Override
    public BasicTransView getExerciseView() { return transView; }

    @Override
    public void saveExercise(boolean saveAs) {
        boolean success = DiskUtilities.saveExercise(saveAs, getSimpleTransModelFromView());
        if (success) exerciseModified = false;
    }

    @Override
    public List<Node> getPrintNodes(ExerciseModel originalModel) {
        List<Node> nodeList = new ArrayList<>();
        BasicTransModel printModel = transModel;
        BasicTransExercise printExercise = this;
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
        statementRTA.setPrefHeight(printModel.getStatementTextHeight() + RTA_PRINT_BMP);
        statementRTA.setMaxWidth(nodeWidth);
        statementRTA.getStylesheets().clear(); statementRTA.getStylesheets().add("richTextAreaPrinter.css");
        nodeList.add(statementRTA);

        Separator statementSeparator = new Separator(Orientation.HORIZONTAL);
        statementSeparator.setPrefWidth(100);
        HBox statementSepBox = new HBox(statementSeparator);
        statementSepBox.setMinWidth(nodeWidth);
        statementSepBox.setAlignment(Pos.CENTER);
        nodeList.add(statementSepBox);

        RichTextArea interpretationRTA = printExercise.getExerciseView().getExerciseInterpretation().getEditor();
        interpretationRTA.prefHeightProperty().unbind();
        interpretationRTA.minWidthProperty().unbind();
        interpretationRTA.setPrefHeight(printModel.getInterpFnTextHeight() + RTA_PRINT_BMP);
        interpretationRTA.setMaxWidth(nodeWidth);
        interpretationRTA.getStylesheets().clear(); interpretationRTA.getStylesheets().add("richTextAreaPrinter.css");
        nodeList.add(interpretationRTA);

        Separator interpretationSeparator = new Separator(Orientation.HORIZONTAL);
        interpretationSeparator.setPrefWidth(100);
        HBox interpretationSepBox = new HBox(interpretationSeparator);
        interpretationSepBox.setMinWidth(nodeWidth);
        interpretationSepBox.setAlignment(Pos.CENTER);
        nodeList.add(interpretationSepBox);

        RichTextArea responseRTA = printExercise.getExerciseView().getExerciseResponse().getEditor();
        responseRTA.prefHeightProperty().unbind();
        responseRTA.minWidthProperty().unbind();
        responseRTA.maxHeightProperty().unbind();
        responseRTA.setMaxWidth(nodeWidth);
        responseRTA.getStylesheets().clear(); responseRTA.getStylesheets().add("richTextAreaPrinter.css");
        double responseHeight = Math.max(40, printModel.getResponseTextHeight() + RTA_PRINT_BMP);
        responseRTA.setMinHeight(responseHeight);
        responseRTA.setMaxHeight(responseHeight);
        nodeList.add(responseRTA);

        Separator responseSeparator = new Separator(Orientation.HORIZONTAL);
        responseSeparator.setPrefWidth(100);
        HBox responseSepBox = new HBox(responseSeparator);
        responseSepBox.setMinWidth(nodeWidth);
        responseSepBox.setAlignment(Pos.CENTER);
        nodeList.add(responseSepBox);

        RichTextArea commentRTA = printExercise.getExerciseView().getExerciseComment().getEditor();
        commentRTA.prefHeightProperty().unbind();
        commentRTA.minWidthProperty().unbind();
        commentRTA.setPrefHeight(Math.max(40, printModel.getCommentTextHeight() + RTA_PRINT_BMP));
        commentRTA.setMaxWidth(nodeWidth);
        commentRTA.getStylesheets().clear(); commentRTA.getStylesheets().add("richTextAreaPrinter.css");


        nodeList.add(commentRTA);

        return nodeList;
    }

    TextFlow printCheckNode() {
        TextFlow flow = new TextFlow();
        flow.setMaxWidth(300);
        if (transModel != null && transModel.getCheckSetup() != null && transModel.getCheckSetup().isCheckSuccess()) {
            Text bigCheck = new Text("\ue89a");
            bigCheck.setFont(Font.font("Noto Serif Combo", 14));
            Text message = new Text("  " + (transView != null ? transView.getCheckMessage() : ""));
            message.setFont(Font.font("Noto Serif Combo", 11));

            if (transModel.getCheckSetup().isCheckGreen()) {
                bigCheck.setFill(Color.LAWNGREEN);
                message.setFill(Color.GREEN);
            } else {
                bigCheck.setFill(Color.ORCHID);
                message.setFill(Color.PURPLE);
            }
            flow.getChildren().addAll(bigCheck, message);
        }
        return flow;
    }

    public MainWindow getMainWindow() { return mainWindow; }

    @Override
    public BasicTransExercise resetExercise() {
        RichTextArea commentRTA = transView.getExerciseComment().getEditor();
        commentRTA.getActionFactory().saveNow().execute(new ActionEvent());
        Document commentDocument = commentRTA.getDocument();

        int pointsEarned = -1;
        TextField pointsField = transView.getPointsEarnedTextField();
        if ((pointsField.isFocused() || transModel.getPointsEarned() >= 0) && pointsField.getText().isEmpty()) {
            pointsField.setText("0");
        }
        if (!pointsField.getText().equals("")) pointsEarned = Integer.parseInt(pointsField.getText());


        BasicTransModel originalModel = (BasicTransModel) (transModel.getOriginalModel());
        originalModel.setExerciseComment(commentDocument);
        double commentHeight = commentRTA.getFullHeight();
        originalModel.setCommentTextHeight(commentHeight > 0 ? commentHeight : transModel.getCommentTextHeight());

        originalModel.setPointsEarned(pointsEarned);
        return new BasicTransExercise(originalModel, mainWindow);
    }

    @Override
    public boolean isExerciseModified() {
        if (transView == null) return false;
        try {
            if (transView.getExerciseComment() != null && transView.getExerciseComment().getEditor().isModified()) exerciseModified = true;
            if (transView.getExerciseResponse() != null && transView.getExerciseResponse().getEditor().isModified()) exerciseModified = true;
            if (transView.getExerciseInterpretation() != null && transView.getExerciseInterpretation().getEditor().isModified()) exerciseModified = true;
        } catch (Exception e) {}
        return exerciseModified;
    }

    @Override
    public void setExerciseModified(boolean modified) { this.exerciseModified = modified; }

    @Override
    public Node getFFViewNode() { return null; }
    @Override
    public Spinner getFFHeightSpinner() { return null; }
    @Override
    public Spinner getFFWidthSpinner() { return null; }
    @Override
    public Node getFFPrintNode(ExerciseModel originalModel) { return null; }

    @Override
    public ExerciseModel getExerciseModelFromView() { return (ExerciseModel) getSimpleTransModelFromView(); }

    private BasicTransModel getSimpleTransModelFromView() {
        BasicTransModel model = new BasicTransModel(transModel.getExerciseName());
        model.setOriginalModel(transModel.getOriginalModel());

        model.setExerciseStatement(transModel.getExerciseStatement());
        model.setStatementPrefHeight(transView.getExerciseStatement().getEditor().getPrefHeight());
        model.setStatementTextHeight(transModel.getStatementTextHeight());
        model.setAuxExerName(transModel.getAuxExerName());
        model.setInterpFnIncluded(transModel.isInterpFnIncluded());

        RichTextArea commentRTA = transView.getExerciseComment().getEditor();
        commentRTA.getActionFactory().saveNow().execute(new ActionEvent());
        Document commentDocument = commentRTA.getDocument();
        model.setExerciseComment(commentDocument);
        model.setCommentPrefHeight(transView.getCommentPrefHeight());
        double commentTextHeight = commentRTA.getFullHeight();
        model.setCommentTextHeight(commentTextHeight > 0 ? commentTextHeight : transModel.getCommentTextHeight());

        RichTextArea responseRTA = transView.getExerciseResponse().getEditor();
        responseRTA.getActionFactory().saveNow().execute(new ActionEvent());
        Document responseDocument = responseRTA.getDocument();
        model.setExerciseResponse(responseDocument);
        model.setResponsePrefHeight(transView.getResponsePrefHeight());
        double responseTextHeight = responseRTA.getFullHeight();
        model.setResponseTextHeight(responseTextHeight > 0 ? responseTextHeight : transModel.getResponseTextHeight());


        RichTextArea interpretationRTA = transView.getExerciseInterpretation().getEditor();
        interpretationRTA.getActionFactory().saveNow().execute(new ActionEvent());
        Document interpretationDocument = interpretationRTA.getDocument();
        model.setInterpFnDoc(interpretationDocument);
        model.setInterpFnPrefHeight(transView.getInterpretationPrefHeight());
        double interpTextHeight = interpretationRTA.getFullHeight();
        model.setInterpFnTextHeight(interpTextHeight > 0 ? interpTextHeight : transModel.getInterpFnTextHeight());

        model.setPointsPossible(transModel.getPointsPossible());
        TextField pointsField = transView.getPointsEarnedTextField();
        if ((pointsField.isFocused() || transModel.getPointsEarned() >= 0) && pointsField.getText().isEmpty()) {
            pointsField.setText("0");
        }
        if (!pointsField.getText().equals("")) model.setPointsEarned(Integer.parseInt(pointsField.getText()));
        else model.setPointsEarned(-1);



        boolean started = (transModel.isStarted() || exerciseModified);
        model.setStarted(started); transModel.setStarted(started);

        BTransCheckSetup setup = transModel.getCheckSetup();
        if (basicTransCheck != null) {
            setup.setCheckTries(basicTransCheck.getCheckTries());
        }

        model.setCheckSetup(setup);

        return model;
    }

    @Override
    public Node getAuxExerNode() {
        if (transView == null || transView.getExerciseInterpretation() == null || transView.getExerciseResponse() == null) return new VBox();
        RichTextArea interpRTA = transView.getExerciseInterpretation().getEditor();
        RichTextArea respRTA = transView.getExerciseResponse().getEditor();

        try {
            if (interpRTA != null) {
                interpRTA.prefHeightProperty().unbind();
                interpRTA.maxHeightProperty().unbind();
                interpRTA.setMinHeight(Math.max(100, (transModel != null ? transModel.getInterpFnTextHeight() : 0) + RTA_SCREEN_BMP));
      //          interpRTA.setMaxHeight(Math.max(100, (transModel != null ? transModel.getInterpFnTextHeight() : 0) + RTA_SCREEN_BMP));
                interpRTA.setMinWidth(PrintUtilities.getPageWidth() / (mainWindow != null ? mainWindow.getBaseScale() : 1.0) + RTA_WIDTH_BMP);
                interpRTA.setMaxWidth(PrintUtilities.getPageWidth() / (mainWindow != null ? mainWindow.getBaseScale() : 1.0) + RTA_WIDTH_BMP);
            }
            if (respRTA != null) {
                respRTA.prefHeightProperty().unbind();
                respRTA.maxHeightProperty().unbind();
                respRTA.setMinHeight(Math.max(100, (transModel != null ? transModel.getResponseTextHeight() : 0) + RTA_SCREEN_BMP));
         //       respRTA.setMaxHeight(Math.max(100, (transModel != null ? transModel.getResponseTextHeight() : 0) + RTA_SCREEN_BMP));
                respRTA.setMinWidth(PrintUtilities.getPageWidth() / (mainWindow != null ? mainWindow.getBaseScale() : 1.0) + RTA_WIDTH_BMP);
                respRTA.setMaxWidth(PrintUtilities.getPageWidth() / (mainWindow != null ? mainWindow.getBaseScale() : 1.0) + RTA_WIDTH_BMP);
            }
        } catch (Exception e) { }

        return new VBox(5, interpRTA, respRTA);
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

    // SCORCHED EARTH RULE 1 & 5: Cleanup cascading and isolation
    @Override
    public void cleanup() {
        // SCORCHED EARTH RULE 2: Engage Kill Switch
        isTornDown = true;

        // SCORCHED EARTH RULE 7: Safe Global State & Null-Poisoning Prevention
        try {
            if (mainView != null) {
                mainView.textFieldInFocus(); // Safely clear toolbars
            }
        } catch (Exception e) {}

        // SCORCHED EARTH RULE 5: Rule of Isolation
        try {
            if (transView != null && transView.localScalePageHeightProperty() != null) {
                transView.localScalePageHeightProperty().unbind();
            }
        } catch (Exception e) {}

        try {
            if (transView != null && transView.localScalePageWidthProperty() != null) {
                transView.localScalePageWidthProperty().unbind();
            }
        } catch (Exception e) {}

        try {
            if (mainView != null && mainView.getMainScene() != null && transView != null && transView.getExerciseFocusListener() != null) {
                mainView.getMainScene().focusOwnerProperty().removeListener(transView.getExerciseFocusListener());
            }
        } catch (Exception e) {}

        try { if (transView != null && transView.getPointsEarnedTextField() != null && pointsEarnedListener != null) transView.getPointsEarnedTextField().textProperty().removeListener(pointsEarnedListener); } catch (Exception e) {}
        try { if (transView != null && transView.getExerciseStatement() != null && statementFocusListener != null) transView.getExerciseStatement().getEditor().focusedProperty().removeListener(statementFocusListener); } catch (Exception e) {}

        try {
            if (transView != null && transView.getExerciseComment() != null) {
                if (commentFullHeightListener != null) transView.getExerciseComment().getEditor().fullHeightProperty().removeListener(commentFullHeightListener);
                if (commentModifiedListener != null) transView.getExerciseComment().getEditor().modifiedProperty().removeListener(commentModifiedListener);
                if (commentFocusListener != null) transView.getExerciseComment().getEditor().focusedProperty().removeListener(commentFocusListener);
            }
        } catch (Exception e) {}

        try {
            if (transView != null && transView.getExerciseInterpretation() != null) {
                if (interpFullHeightListener != null) transView.getExerciseInterpretation().getEditor().fullHeightProperty().removeListener(interpFullHeightListener);
                if (interpModifiedListener != null) transView.getExerciseInterpretation().getEditor().modifiedProperty().removeListener(interpModifiedListener);
                if (interpFocusListener != null) transView.getExerciseInterpretation().getEditor().focusedProperty().removeListener(interpFocusListener);
            }
        } catch (Exception e) {}

        try {
            if (transView != null && transView.getExerciseResponse() != null) {
                if (responseFullHeightListener != null) transView.getExerciseResponse().getEditor().fullHeightProperty().removeListener(responseFullHeightListener);
                if (responseModifiedListener != null) transView.getExerciseResponse().getEditor().modifiedProperty().removeListener(responseModifiedListener);
                if (responseFocusListener != null) transView.getExerciseResponse().getEditor().focusedProperty().removeListener(responseFocusListener);
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

        // ---> SCORCHED EARTH RULE 4: THE JAVAFX SKIN HAMMER <---
        // (Completely removed from Controller per Rule 1. Document injection and skin removal deferred entirely to View.)
        // Note: There were no synchronous unbindings embedded directly inside this removed block.

        try { clearStandingPopups(); } catch (Exception e) {}

        // SCORCHED EARTH RULE 1: Cascade Cleanup
        try { if (auxExerciseA != null) auxExerciseA.cleanup(); } catch (Exception e) {}
        try { if (auxExerciseB != null) auxExerciseB.cleanup(); } catch (Exception e) {}
        try { if (transView != null) transView.cleanup(); } catch (Exception e) {}
        try { if (basicTransCheck != null) { basicTransCheck.cleanup(); } } catch (Exception e) {}

        // SCORCHED EARTH RULE 1: Final Nullification
        try { this.transModel = null; } catch (Exception e) {}
        try { this.mainWindow = null; } catch (Exception e) {}
        try { this.mainView = null; } catch (Exception e) {}
        try { this.basicTransCheck = null; } catch (Exception e) {}
        try { this.auxExerciseA = null; } catch (Exception e) {}
        try { this.auxExerciseB = null; } catch (Exception e) {}
        try { this.transView = null; } catch (Exception e) {}
        try { this.tScene = null; } catch (Exception e) {}
        try { this.tStage = null; } catch (Exception e) {}
        try { this.thumbPane = null; } catch (Exception e) {}

        // SCORCHED EARTH RULE 10: Non-destructive List Teardown (only clear pointers)
        try { this.rtaNodes = null; } catch (Exception e) {}

        try { this.pointsEarnedListener = null; } catch (Exception e) {}
        try { this.statementFocusListener = null; } catch (Exception e) {}
        try { this.commentFullHeightListener = null; } catch (Exception e) {}
        try { this.commentModifiedListener = null; } catch (Exception e) {}
        try { this.commentFocusListener = null; } catch (Exception e) {}
        try { this.interpFullHeightListener = null; } catch (Exception e) {}
        try { this.interpModifiedListener = null; } catch (Exception e) {}
        try { this.interpFocusListener = null; } catch (Exception e) {}
        try { this.responseFullHeightListener = null; } catch (Exception e) {}
        try { this.responseModifiedListener = null; } catch (Exception e) {}
        try { this.responseFocusListener = null; } catch (Exception e) {}
        try { this.thumbPaneClickHandler = null; } catch (Exception e) {}
    }

    @Override
    public String getAuxExerName() {
        if (transModel == null) return "";
        return transModel.getAuxExerName();
    }

    @Override
    public void setAuxExerA(Exercise exer) {
        auxExerciseA = exer;
    }

    @Override
    public void setAuxExerB(Exercise exer) { auxExerciseB = exer; }
}
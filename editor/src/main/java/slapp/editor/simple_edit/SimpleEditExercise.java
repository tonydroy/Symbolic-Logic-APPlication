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


package slapp.editor.simple_edit;

import com.gluonhq.richtextarea.RichTextArea;
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
import javafx.scene.shape.Rectangle;
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
 * The controller for the simple edit exercise
 */


public class SimpleEditExercise implements Exercise<SimpleEditModel, SimpleEditView> {

    private MainWindow mainWindow;
    private SimpleEditModel editModel;
    private SimpleEditView editView;
    private MainWindowView mainView;
    private boolean exerciseModified = false;
    private SECheck seCheck;

    private Exercise auxExerciseA;
    private Exercise auxExerciseB;
    Stage tStage;
    Pane thumbPane = new Pane();
    Scene tScene = new Scene(new Pane());

    // SCORCHED EARTH RULE 2: Kill Switch for asynchronous event interception during teardown
    private boolean isTornDown = false;

    // SCORCHED EARTH RULE 3: Eradicate Anonymous Lambdas
    private ChangeListener<String> pointsEarnedListener;
    private ChangeListener<Boolean> statementFocusListener;
    private ChangeListener<Number> commentFullHeightListener;
    private ChangeListener<Boolean> commentModifiedListener;
    private ChangeListener<Boolean> commentFocusListener;
    private ChangeListener<Number> responseFullHeightListener;
    private ChangeListener<Boolean> responseModifiedListener;
    private ChangeListener<Boolean> responseFocusListener;
    private EventHandler<MouseEvent> thumbPaneClickHandler;

    public SimpleEditExercise(SimpleEditModel model, MainWindow mainWindow) {
        this.mainWindow = mainWindow;
        this.editModel = model;
        if (model.getOriginalModel() == null) {model.setOriginalModel(model); }
        this.mainView = mainWindow.getMainView();
        this.editView = new SimpleEditView(mainView);
        setEditView();
    }

    private void setEditView() {
        editView.setResponsePrompt(editModel.getResponsePrompt());
        editView.setStatementPrefHeight(editModel.getStatementPrefHeight());
        editView.setCommentPrefHeight(editModel.getCommentPrefHeight());
        editView.setResponsePrefHeight(editModel.getResponsePrefHeight());

        editView.setPointsPossible(editModel.getPointsPossible());
        if (editModel.getPointsEarned() >= 0) editView.getPointsEarnedTextField().setText(Integer.toString(editModel.getPointsEarned()));

        pointsEarnedListener = (ob, ov, nv) -> {
            if (isTornDown || mainView == null) return;
            exerciseModified = true;
        };
        editView.getPointsEarnedTextField().textProperty().addListener(pointsEarnedListener);

        //statement
        DecoratedRTA statementDRTA = new DecoratedRTA();
        RichTextArea statementEditor = statementDRTA.getEditor();
        statementEditor.getActionFactory().open(editModel.getExerciseStatement()).execute(new ActionEvent());

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
        editView.setExerciseStatement(statementDRTA);

        //comment
        DecoratedRTA commentDRTA = new DecoratedRTA();
        RichTextArea commentEditor = commentDRTA.getEditor();
        commentEditor.getActionFactory().open(editModel.getExerciseComment()).execute(new ActionEvent());
        commentEditor.getActionFactory().saveNow().execute(new ActionEvent());

        final boolean[] commentPrimed = {false};
        commentFullHeightListener = (ob, ov, nv) -> {
            if (isTornDown || mainView == null || editModel == null) return;
            if (!commentPrimed[0]) {
                commentPrimed[0] = true;
                return;
            }
            double height = (double) nv;
            mainView.setRTATextHeight(height);
            editModel.setCommentTextHeight(height);
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
        editView.setExerciseComment(commentDRTA);

        //response
        DecoratedRTA responseDRTA = new DecoratedRTA();
        RichTextArea responseEditor = responseDRTA.getEditor();
        responseEditor.getActionFactory().open(editModel.getExerciseResponse()).execute(new ActionEvent());
        responseEditor.getActionFactory().saveNow().execute(new ActionEvent());

        final boolean[] responsePrimed = {false};
        responseFullHeightListener = (ob, ov, nv) -> {
            if (isTornDown || mainView == null || editModel == null) return;
            if (!responsePrimed[0]) {
                responsePrimed[0] = true;
                return;
            }
            double height = (double) nv;
            mainView.setRTATextHeight(height);
            editModel.setResponseTextHeight(height);
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
        editView.setExerciseResponse(responseDRTA);

        //cleanup
        editView.initializeViewDetails();
        editView.setRightControlBox();
        seCheck = new SECheck(this);

        Platform.runLater(() -> {
            if (isTornDown || mainView == null) return;
            if (mainWindow != null && mainWindow.getCurrentExercise() != this) return;
            if (auxExerciseA != null) {
                thumbPane = auxImage();
                if (editView != null && editView.getControlBox() != null) {
                    editView.getControlBox().getChildren().add(thumbPane);
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
             //       tStage.initOwner(EditorMain.mainStage);
                    tStage.initModality(Modality.NONE);
                    Stage mainStage = EditorMain.mainStage;
                    tStage.setX(mainStage.getX() + mainStage.getWidth()  - 500);
                    tStage.setY(mainStage.getY() + mainStage.getHeight()  - 300);

                    tStage.setWidth(pane1.getWidth() + RTA_WIDTH_BMP);
                    tStage.setHeight(Math.min(mainStage.getHeight() - 100, pane1.getHeight() + RTA_SCREEN_BMP + 25));  //25
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
    }

    @Override
    public SimpleEditModel getExerciseModel() { return editModel; }

    @Override
    public SimpleEditView getExerciseView() { return editView; }

    @Override
    public void saveExercise(boolean saveAs) {
        boolean success = DiskUtilities.saveExercise(saveAs, getSimpleEditModelFromView());
        if (success) exerciseModified = false;
    }

    @Override
    public List<Node> getPrintNodes(ExerciseModel originalModel) {
        List<Node> nodeList = new ArrayList<>();
        SimpleEditModel printModel = (SimpleEditModel) originalModel;
        SimpleEditExercise printExercise = this;

        double nodeWidth = PrintUtilities.getPageWidth() / mainWindow.getBaseScale();
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

    @Override
    public SimpleEditExercise resetExercise() {
        RichTextArea commentRTA = editView.getExerciseComment().getEditor();
        commentRTA.getActionFactory().saveNow().execute(new ActionEvent());
        Document commentDocument = commentRTA.getDocument();


        int pointsEarned = -1;
        TextField pointsField = editView.getPointsEarnedTextField();
        if ((pointsField.isFocused() || editModel.getPointsEarned() >= 0) && pointsField.getText().isEmpty()) {
            pointsField.setText("0");
        }
        if (!pointsField.getText().equals("")) pointsEarned = Integer.parseInt(pointsField.getText());


        SimpleEditModel originalModel = (SimpleEditModel) (editModel.getOriginalModel());
        originalModel.setExerciseComment(commentDocument);

        double commentHeight = commentRTA.getFullHeight();
        originalModel.setCommentTextHeight(commentHeight > 0 ? commentHeight : editModel.getCommentTextHeight());

        originalModel.setPointsEarned(pointsEarned);
        return new SimpleEditExercise(originalModel, mainWindow);
    }

    @Override
    public boolean isExerciseModified() {
        if (editView == null) return false;
        try {
            if (editView.getExerciseComment() != null && editView.getExerciseComment().getEditor().isModified()) exerciseModified = true;
            if (editView.getExerciseResponse() != null && editView.getExerciseResponse().getEditor().isModified()) exerciseModified = true;
        } catch (Exception e) {}
        return exerciseModified;
    }

    @Override
    public void setExerciseModified(boolean modified) { this.exerciseModified = modified; }

    @Override
    public Node getFFViewNode() { return editView.getFFViewNode();}
    @Override
    public Spinner getFFHeightSpinner() {return editView.getResponseHeightSpinner();}
    @Override
    public Spinner getFFWidthSpinner() {return editView.getResponseWidthSpinner();}

    @Override
    public Node getFFPrintNode(ExerciseModel originalModel) {
        SimpleEditModel printModel = (SimpleEditModel) originalModel;
        SimpleEditExercise printExercise = this;
        double nodeWidth = PrintUtilities.getPageWidth() / mainWindow.getBaseScale();

        RichTextArea responseRTA = printExercise.getExerciseView().getExerciseResponse().getEditor();
        responseRTA.prefHeightProperty().unbind();
        responseRTA.minWidthProperty().unbind();
        responseRTA.maxHeightProperty().unbind();
        responseRTA.getStylesheets().clear(); responseRTA.getStylesheets().add("richTextAreaPrinter.css");
        responseRTA.setMaxWidth(nodeWidth);


        double responseHeight = Math.max(40, printModel.getResponseTextHeight() + RTA_PRINT_BMP);
        responseRTA.setMinHeight(responseHeight);
        responseRTA.setMaxHeight(responseHeight);

        return responseRTA;
    }

    @Override
    public ExerciseModel getExerciseModelFromView() { return (ExerciseModel) getSimpleEditModelFromView(); }

    private SimpleEditModel getSimpleEditModelFromView() {
        SimpleEditModel model = new SimpleEditModel(editModel.getExerciseName(), editModel.getResponsePrompt());
        model.setOriginalModel(editModel.getOriginalModel());
        model.setExerciseStatement(editModel.getExerciseStatement());
        model.setStatementPrefHeight(editView.getExerciseStatement().getEditor().getPrefHeight());
        model.setStatementTextHeight(editModel.getStatementTextHeight());
        model.setAuxExerName(editModel.getAuxExerName());
        model.setCheckSetup(editModel.getCheckSetup());

        RichTextArea commentRTA = editView.getExerciseComment().getEditor();
        commentRTA.getActionFactory().saveNow().execute(new ActionEvent());
        Document commentDocument = commentRTA.getDocument();
        model.setExerciseComment(commentDocument);
        model.setCommentPrefHeight(editView.getCommentPrefHeight());

        double commentTextHeight = commentRTA.getFullHeight();
        model.setCommentTextHeight(commentTextHeight > 0 ? commentTextHeight : editModel.getCommentTextHeight());

        RichTextArea responseRTA = editView.getExerciseResponse().getEditor();
        if (responseRTA.isModified()) exerciseModified = true;
        responseRTA.getActionFactory().saveNow().execute(new ActionEvent());
        Document responseDocument = responseRTA.getDocument();
        model.setExerciseResponse(responseDocument);
        model.setResponsePrefHeight(editView.getResponsePrefHeight());

        double responseTextHeight = responseRTA.getFullHeight();
        model.setResponseTextHeight(responseTextHeight > 0 ? responseTextHeight : editModel.getResponseTextHeight());

        model.setPointsPossible(editModel.getPointsPossible());
        TextField pointsField = editView.getPointsEarnedTextField();
        if ((pointsField.isFocused() || editModel.getPointsEarned() >= 0) && pointsField.getText().isEmpty()) {
            pointsField.setText("0");
        }
        if (!pointsField.getText().equals("")) model.setPointsEarned(Integer.parseInt(pointsField.getText()));
        else model.setPointsEarned(-1);

        boolean started = (editModel.isStarted() || exerciseModified);
        model.setStarted(started); editModel.setStarted(started);

        return model;
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
        // SCORCHED EARTH RULE 2: Engage Kill Switch
        isTornDown = true;

        // SCORCHED EARTH RULE 7: Safe Global State & Null-Poisoning Prevention
        try {
            if (mainView != null) {
                mainView.textFieldInFocus(); // Safely clear toolbars
            }
        } catch (Exception e) {}

        try {
            if (editView != null && editView.localScalePageHeightProperty() != null) {
                editView.localScalePageHeightProperty().unbind();
            }
        } catch (Exception e) {}

        try {
            if (editView != null && editView.localScalePageWidthProperty() != null) {
                editView.localScalePageWidthProperty().unbind();
            }
        } catch (Exception e) {}

        try {
            if (mainView != null && mainView.getMainScene() != null && editView != null && editView.getExerciseFocusListener() != null) {
                mainView.getMainScene().focusOwnerProperty().removeListener(editView.getExerciseFocusListener());
            }
        } catch (Exception e) {}

        // SCORCHED EARTH RULE 5: Rule of Isolation
        try { if (editView != null && editView.getPointsEarnedTextField() != null && pointsEarnedListener != null) editView.getPointsEarnedTextField().textProperty().removeListener(pointsEarnedListener); } catch (Exception e) {}
        try { if (editView != null && editView.getExerciseStatement() != null && statementFocusListener != null) editView.getExerciseStatement().getEditor().focusedProperty().removeListener(statementFocusListener); } catch (Exception e) {}

        try {
            if (editView != null && editView.getExerciseComment() != null) {
                if (commentFullHeightListener != null) editView.getExerciseComment().getEditor().fullHeightProperty().removeListener(commentFullHeightListener);
                if (commentModifiedListener != null) editView.getExerciseComment().getEditor().modifiedProperty().removeListener(commentModifiedListener);
                if (commentFocusListener != null) editView.getExerciseComment().getEditor().focusedProperty().removeListener(commentFocusListener);
            }
        } catch (Exception e) {}

        try {
            if (editView != null && editView.getExerciseResponse() != null) {
                if (responseFullHeightListener != null) editView.getExerciseResponse().getEditor().fullHeightProperty().removeListener(responseFullHeightListener);
                if (responseModifiedListener != null) editView.getExerciseResponse().getEditor().modifiedProperty().removeListener(responseModifiedListener);
                if (responseFocusListener != null) editView.getExerciseResponse().getEditor().focusedProperty().removeListener(responseFocusListener);
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

        // ---> SCORCHED EARTH RULE 4: CONTROLLER DEFERS TO VIEW (No Timers, No setSkin(null)) <---

        try { clearStandingPopups(); } catch (Exception e) {}

        // SCORCHED EARTH RULE 1: Cascade Cleanup (Check before View so View getters are still live)
        try { if (seCheck != null) seCheck.cleanup(); } catch (Exception e) {}
        try { if (auxExerciseA != null) auxExerciseA.cleanup(); } catch (Exception e) {}
        try { if (auxExerciseB != null) auxExerciseB.cleanup(); } catch (Exception e) {}
        try { if (editView != null) editView.cleanup(); } catch (Exception e) {}


        // SCORCHED EARTH RULE 1: Final Nullification
        try { this.editModel = null; } catch (Exception e) {}
        try { this.mainWindow = null; } catch (Exception e) {}
        try { this.mainView = null; } catch (Exception e) {}
        try { this.seCheck = null; } catch (Exception e) {}
        try { this.auxExerciseA = null; } catch (Exception e) {}
        try { this.auxExerciseB = null; } catch (Exception e) {}
        try { this.editView = null; } catch (Exception e) {}
        try { this.tScene = null; } catch (Exception e) {}
        try { this.tStage = null; } catch (Exception e) {}
        try { this.thumbPane = null; } catch (Exception e) {}

        try { this.pointsEarnedListener = null; } catch (Exception e) {}
        try { this.statementFocusListener = null; } catch (Exception e) {}
        try { this.commentFullHeightListener = null; } catch (Exception e) {}
        try { this.commentModifiedListener = null; } catch (Exception e) {}
        try { this.commentFocusListener = null; } catch (Exception e) {}
        try { this.responseFullHeightListener = null; } catch (Exception e) {}
        try { this.responseModifiedListener = null; } catch (Exception e) {}
        try { this.responseFocusListener = null; } catch (Exception e) {}
        try { this.thumbPaneClickHandler = null; } catch (Exception e) {}
    }


    @Override
    public Node getAuxExerNode() {
        if (editView == null || editView.getExerciseResponse() == null) return new Pane();
        RichTextArea rta = editView.getExerciseResponse().getEditor();
        rta.maxHeightProperty().unbind();
        rta.setMaxHeight(Math.max(200, editModel != null ? editModel.getResponseTextHeight() + RTA_SCREEN_BMP : 200));
        rta.setMinHeight(Math.max(200, editModel != null ? editModel.getResponseTextHeight() + RTA_SCREEN_BMP : 200));
        rta.setMinWidth(PrintUtilities.getPageWidth() / (mainWindow != null ? mainWindow.getBaseScale() : 1.0) + RTA_WIDTH_BMP);
        rta.setMaxWidth(PrintUtilities.getPageWidth() / (mainWindow != null ? mainWindow.getBaseScale() : 1.0) + RTA_WIDTH_BMP);
        return rta;
    }

    @Override
    public String getAuxExerName() {
        if (editModel == null) return "";
        return editModel.getAuxExerName();
    }

    @Override
    public void setAuxExerA(Exercise exer) { auxExerciseA = exer; }

    @Override
    public void setAuxExerB(Exercise exer) { auxExerciseB = exer; }
}
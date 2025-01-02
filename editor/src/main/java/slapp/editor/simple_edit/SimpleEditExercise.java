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
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.Spinner;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import slapp.editor.DiskUtilities;
import slapp.editor.PrintUtilities;
import slapp.editor.decorated_rta.DecoratedRTA;
import slapp.editor.main_window.*;
import slapp.editor.page_editor.PageEditExercise;
import slapp.editor.page_editor.PageEditModel;
import slapp.editor.page_editor.PageEditView;

import java.util.ArrayList;
import java.util.List;

/**
 * The controller for the simple edit exercise
 */
public class SimpleEditExercise implements Exercise<SimpleEditModel, SimpleEditView> {

    private MainWindow mainWindow;
    private SimpleEditModel editModel;
    private SimpleEditView editView;
    private MainWindowView mainView;
    private boolean exerciseModified = false;

    /**
     * Create simple edit exercise
     * @param model the SimpleEditModel
     * @param mainWindow the MainWindow
     */
    public SimpleEditExercise(SimpleEditModel model, MainWindow mainWindow) {
        this.mainWindow = mainWindow;
        this.editModel = model;
        if (model.getOriginalModel() == null) {model.setOriginalModel(model); }
        mainView = mainWindow.getMainView();
        editView = new SimpleEditView(mainView);

        setEditView();
    }

    /**
     * Set statement, comment, response in the view
     */
    private void setEditView() {

        editView.setResponsePrompt(editModel.getResponsePrompt());
        editView.setStatementPrefHeight(editModel.getStatementPrefHeight());
        editView.setCommentPrefHeight(editModel.getCommentPrefHeight());
        editView.setResponsePrefHeight(editModel.getResponsePrefHeight());

        //statement
        DecoratedRTA statementDRTA = new DecoratedRTA();
        RichTextArea statementEditor = statementDRTA.getEditor();
        statementEditor.getActionFactory().open(editModel.getExerciseStatement()).execute(new ActionEvent());

        mainView.editorInFocus(statementDRTA, ControlType.STATEMENT);
        statementEditor.focusedProperty().addListener((o, ov, nv) -> {
            if (nv) {
                mainView.editorInFocus(statementDRTA, ControlType.STATEMENT);
            }
        });
        editView.setExerciseStatement(statementDRTA);

        //comment
        DecoratedRTA commentDRTA = new DecoratedRTA();
        RichTextArea commentEditor = commentDRTA.getEditor();

        commentEditor.addEventHandler(KeyEvent.KEY_RELEASED, e -> {
            exerciseModified = true;
            double commentTextHeight = mainView.getRTATextHeight(commentEditor);
            editModel.setCommentTextHeight(commentTextHeight);
        });
        commentEditor.getActionFactory().open(editModel.getExerciseComment()).execute(new ActionEvent());

        mainView.editorInFocus(commentDRTA, ControlType.AREA);
        commentEditor.focusedProperty().addListener((o, ov, nv) -> {
            if (nv) {
                mainView.editorInFocus(commentDRTA, ControlType.AREA);
            }
        });
        editView.setExerciseComment(commentDRTA);

        //response
        DecoratedRTA responseDRTA = new DecoratedRTA();
        RichTextArea responseEditor = responseDRTA.getEditor();

        responseEditor.addEventHandler(KeyEvent.KEY_RELEASED, e -> {
            exerciseModified = true;
            double responseTextHeight = mainView.getRTATextHeight(responseEditor);
            editModel.setResponseTextHeight(responseTextHeight);
        });
        responseEditor.getActionFactory().open(editModel.getExerciseResponse()).execute(new ActionEvent());

        mainView.editorInFocus(responseDRTA, ControlType.AREA);
        responseEditor.focusedProperty().addListener((o, ov, nv) -> {
            if (nv) {
                mainView.editorInFocus(responseDRTA, ControlType.AREA);
            }
        });
        editView.setExerciseResponse(responseDRTA);

        //cleanup
        editView.initializeViewDetails();
    }

    /**
     * The exercise model
     * @return the model
     */
    @Override
    public SimpleEditModel getExerciseModel() {
        return editModel;
    }

    /**
     * The exercise view
     * @return the view
     */
    @Override
    public SimpleEditView getExerciseView() {
        return editView;
    }

    /**
     * Save exercise to disk
     * @param saveAs true if "save as" should be invoked, and otherwise false
     */
    @Override
    public void saveExercise(boolean saveAs) {
        boolean success = DiskUtilities.saveExercise(saveAs, getSimpleEditModelFromView());
        if (success) exerciseModified = false;
    }

    /**
     * List of nodes to be sent to printer for this exercise
     * @return the node list
     */
    @Override
    public List<Node> getPrintNodes(ExerciseModel originalModel) {
        List<Node> nodeList = new ArrayList<>();
        SimpleEditModel printModel = editModel;
        SimpleEditExercise printExercise = this;
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

        //response node
        RichTextArea responseRTA = printExercise.getExerciseView().getExerciseResponse().getEditor();
        responseRTA.prefHeightProperty().unbind();
        responseRTA.minWidthProperty().unbind();
        responseRTA.setPrefHeight(printModel.getResponseTextHeight() + 35.0);
        responseRTA.setContentAreaWidth(nodeWidth);
        responseRTA.setMinWidth(nodeWidth);
        responseRTA.getStylesheets().clear(); responseRTA.getStylesheets().add("richTextAreaPrinter.css");
        nodeList.add(responseRTA);

        Separator responseSeparator = new Separator(Orientation.HORIZONTAL);
        responseSeparator.setPrefWidth(100);
        HBox responseSepBox = new HBox(responseSeparator);
        responseSepBox.setMinWidth(nodeWidth);
        responseSepBox.setAlignment(Pos.CENTER);
        nodeList.add(responseSepBox);

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

    /**
     * Return to the initial (unworked) version of the exercise, retaining the comment only.
     * @return the initial exercise
     */
    @Override
    public SimpleEditExercise resetExercise() {
        RichTextArea commentRTA = editView.getExerciseComment().getEditor();
        commentRTA.getActionFactory().saveNow().execute(new ActionEvent());
        Document commentDocument = commentRTA.getDocument();
        SimpleEditModel originalModel = (SimpleEditModel) (editModel.getOriginalModel());
        originalModel.setExerciseComment(commentDocument);
        SimpleEditExercise clearExercise = new SimpleEditExercise(originalModel, mainWindow);
        return clearExercise;
    }

    /**
     * Exercise is modified if it is changed relative to last save
     * @return true if exercise is modified, and otherwise false
     */
    @Override
    public boolean isExerciseModified() {
        RichTextArea commentEditor = editView.getExerciseComment().getEditor();
        if (commentEditor.isModified()) exerciseModified = true;
        RichTextArea responseEditor = editView.getExerciseResponse().getEditor();
        if (responseEditor.isModified()) exerciseModified = true;
        return exerciseModified;
    }

    /**
     * Exercise is modified if it is changed relative to last save
     * @param modified true if exercise is modified, and otherwise false
     */
    @Override
    public void setExerciseModified(boolean modified) {
        this.exerciseModified = modified;
    }

    /**
     * The view node to be displayed as a component of the free form exercise.
     * @return the view node
     */
    @Override
    public Node getFFViewNode() {   return editView.getFFViewNode();}

    @Override
    public Spinner getFFHeightSpinner() {return editView.getResponseHeightSpinner();}

    @Override
    public Spinner getFFWidthSpinner() {return editView.getResponseWidthSpinner();}

    /**
     * The node to be printed as a component of the free form exercise.
     * @return the print node
     */
    @Override
    public Node getFFPrintNode(ExerciseModel originalModel) {
        SimpleEditModel printModel = editModel;
        SimpleEditExercise printExercise = this;
        double nodeWidth = PrintUtilities.getPageWidth() / mainWindow.getBaseScale();

        RichTextArea responseRTA = printExercise.getExerciseView().getExerciseResponse().getEditor();
        responseRTA.prefHeightProperty().unbind();
        responseRTA.minWidthProperty().unbind();
        responseRTA.setPrefHeight(printModel.getResponseTextHeight() + 20);
        responseRTA.setContentAreaWidth(nodeWidth);
        responseRTA.setMinWidth(nodeWidth);
        responseRTA.getStylesheets().clear(); responseRTA.getStylesheets().add("richTextAreaPrinter.css");
        return responseRTA;
    }

    /**
     * Extract an {@link slapp.editor.main_window.ExerciseModel} from view of the exercise
     * @return the exercise model
     */
    @Override
    public ExerciseModel getExerciseModelFromView() {
        return (ExerciseModel) getSimpleEditModelFromView();
    }

    /**
     * Extract SimpleEditModelFrom view of the exercise
     * @return the SimpleEditModel
     */
    private SimpleEditModel getSimpleEditModelFromView() {
        SimpleEditModel model = new SimpleEditModel(editModel.getExerciseName(), editModel.getResponsePrompt());
        model.setOriginalModel(editModel.getOriginalModel());

        model.setExerciseStatement(editModel.getExerciseStatement());
        model.setStatementPrefHeight(editView.getExerciseStatement().getEditor().getPrefHeight());
        model.setStatementTextHeight(editModel.getStatementTextHeight());

        RichTextArea commentRTA = editView.getExerciseComment().getEditor();
        commentRTA.getActionFactory().saveNow().execute(new ActionEvent());
        Document commentDocument = commentRTA.getDocument();
        model.setExerciseComment(commentDocument);
        model.setCommentPrefHeight(editView.getCommentPrefHeight());
        model.setCommentTextHeight(editModel.getCommentTextHeight());

        RichTextArea responseRTA = editView.getExerciseResponse().getEditor();
        if (responseRTA.isModified()) exerciseModified = true;
        responseRTA.getActionFactory().saveNow().execute(new ActionEvent());
        Document responseDocument = responseRTA.getDocument();
        model.setExerciseResponse(responseDocument);
        model.setResponsePrefHeight(editView.getResponsePrefHeight());
        model.setResponseTextHeight(editModel.getResponseTextHeight());


        boolean started = (editModel.isStarted() || exerciseModified);
        model.setStarted(started); editModel.setStarted(started);

        return model;
    }



}

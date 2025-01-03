/*
Copyright (c) 2024 Tony Roy

This file is part of the Symbolic Logic APPlication (SLAPP).

SLAPP is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.

SLAPP is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License along with SLAPP.
If not, see <https://www.gnu.org/licenses/>.
 */

package slapp.editor.ab_explain;

import com.gluonhq.richtextarea.RichTextArea;
import com.gluonhq.richtextarea.model.Document;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import slapp.editor.EditorAlerts;
import slapp.editor.PrintUtilities;
import slapp.editor.decorated_rta.DecoratedRTA;
import slapp.editor.main_window.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import slapp.editor.DiskUtilities;
import slapp.editor.page_editor.PageContent;

import static javafx.scene.control.ButtonType.OK;

/**
 * AB exercise has binary choice and explain field
 */
public class ABexercise implements Exercise<ABmodel, ABview> {
    private MainWindow mainWindow;
    private ABmodel abModel;
    private ABview abView;
    private MainWindowView mainView;
    private boolean exerciseModified = false;
    private int lastPageNum = -1;
    private Font labelFont = new Font("Noto Serif Combo", 11);


    /**
     * Construct AB edit exercise
     * @param model an AB edit model
     * @param mainWindow the main window
     */
    public ABexercise(ABmodel model, MainWindow mainWindow) {
        this.mainWindow = mainWindow;
        this.abModel = model;
        if (model.getOriginalModel() == null) {model.setOriginalModel(model); }
        this.mainView = mainWindow.getMainView();
        this.abView = new ABview(mainView);

        setEditView();
    }

    /*
     * Set up choices, statement, comment, and pagination view elements
     */
    private void setEditView() {

        abView.setContentPrompt(abModel.getContentPrompt());
        abView.setStatementPrefHeight(abModel.getStatementPrefHeight());
        abView.setCommentPrefHeight(abModel.getCommentPrefHeight());
        abView.setPaginationPrefHeight(abModel.getPaginationPrefHeight());

        //choices
        ABmodelExtra modelFields = abModel.getModelFields();
        abView.getLeaderLabel().setText(modelFields.getLeader());
        CheckBox aCheckBox = abView.getCheckBoxA();
        CheckBox bCheckBox = abView.getCheckBoxB();
        aCheckBox.setText(modelFields.getPromptA());
        aCheckBox.setSelected(modelFields.getValueA());
        aCheckBox.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue ob, Boolean ov, Boolean nv) {
                if (nv == true) bCheckBox.setSelected(false);
                exerciseModified = true;
            }
        });
        bCheckBox.setText(modelFields.getPromptB());
        bCheckBox.setSelected(modelFields.getValueB());
        bCheckBox.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue ob, Boolean ov, Boolean nv) {
                if (nv == true) aCheckBox.setSelected(false);
                exerciseModified = true;
            }
        });

        //statement
        DecoratedRTA statementDRTA = new DecoratedRTA();
        RichTextArea statementEditor = statementDRTA.getEditor();
        statementEditor.getActionFactory().open(abModel.getExerciseStatement()).execute(new ActionEvent());

        mainView.editorInFocus(statementDRTA, ControlType.STATEMENT);
        statementEditor.focusedProperty().addListener((o, ov, nv) -> {
            if (nv) {
                mainView.editorInFocus(statementDRTA, ControlType.STATEMENT);
            }
        });
        abView.setExerciseStatement(statementDRTA);

        //comment
        DecoratedRTA commentDRTA = new DecoratedRTA();
        RichTextArea commentEditor = commentDRTA.getEditor();

        commentEditor.addEventHandler(KeyEvent.KEY_RELEASED, e -> {
            exerciseModified = true;
            double commentTextHeight = mainView.getRTATextHeight(commentEditor);
            abModel.setCommentTextHeight(commentTextHeight);
        });
        commentEditor.getActionFactory().open(abModel.getExerciseComment()).execute(new ActionEvent());

        mainView.editorInFocus(commentDRTA, ControlType.AREA);
        commentEditor.focusedProperty().addListener((o, ov, nv) -> {
            if (nv) {
                mainView.editorInFocus(commentDRTA, ControlType.AREA);
            }
        });
        abView.setExerciseComment(commentDRTA);

        //pagination
        ArrayList<DecoratedRTA> contentList = new ArrayList<>();
        List<PageContent> pageContents = abModel.getPageContents();
        for (int i = 0; i < pageContents.size(); i++) {
            PageContent pageContent = pageContents.get(i);
            Document doc = pageContent.getPageDoc();
            DecoratedRTA drta = new DecoratedRTA();
            RichTextArea pageEditor = drta.getEditor();

            pageEditor.addEventHandler(KeyEvent.KEY_RELEASED, e -> {
                exerciseModified = true;
                double pageTextHeight = mainView.getRTATextHeight(pageEditor);
                pageContent.setTextHeight(pageTextHeight);
            });

            pageEditor.getActionFactory().open(doc).execute(new ActionEvent());
            pageEditor.getActionFactory().saveNow().execute(new ActionEvent());
            mainView.editorInFocus(drta, ControlType.AREA);
            pageEditor.focusedProperty().addListener((o, ov, nv) -> {
                if (nv) {
                    mainView.editorInFocus(drta, ControlType.AREA);
                }
            });
            contentList.add(drta);
        }
        abView.setContentPageList(contentList);

        //cleanup
        abView.initializeViewDetails();
        abView.getAddPageButton().setOnAction(e -> addPageAction());
        abView.getRemovePageButton().setOnAction(e -> removePageAction());
    }

    /*
     * Add page after the current page
     */
    private void addPageAction() {
        int newPageIndex = abView.getContentPageIndex() + 1;
        abModel.addBlankContentPage(newPageIndex);

        DecoratedRTA drta = new DecoratedRTA();
        RichTextArea editor = drta.getEditor();
        editor.getActionFactory().saveNow().execute(new ActionEvent());

        editor.addEventHandler(KeyEvent.KEY_RELEASED, e -> {
            exerciseModified = true;
            double textHeight = mainView.getRTATextHeight(editor);
            abModel.getPageContents().get(newPageIndex).setTextHeight(textHeight);
        });

        mainView.editorInFocus(drta, ControlType.AREA);
        editor.focusedProperty().addListener((o, ov, nv) -> {
            if (nv) {
                mainView.editorInFocus(drta, ControlType.AREA);
            }
        });
        abView.addBlankContentPage(newPageIndex, drta);
    }

    /*
     * Remove the current page
     */
    private void removePageAction() {
        if (abModel.getPageContents().size() <= 1) {
            EditorAlerts.showSimpleAlert("Cannot Remove", "Your response must include at least one page.");
        }
        else {
            int currentPageIndex = abView.getContentPageIndex();
            boolean okContinue = true;
            if (abView.getContentPageList().get(currentPageIndex).getEditor().isModified()) {
                Alert confirm = EditorAlerts.confirmationAlert("Confirm Remove", "This page appears to have unsaved changes.  Continue to remove?");
                Optional<ButtonType> result = confirm.showAndWait();
                if (result.get() != OK) okContinue = false;
            }
            if (okContinue) {
                abModel.getPageContents().remove(currentPageIndex);
                abView.removeContentPage(currentPageIndex);
                exerciseModified = true;
            }
        }
    }

    /**
     * Model for this AB edit exercise
     * @return the model
     */
    @Override
    public ABmodel getExerciseModel() {
        return abModel;
    }

    /**
     * View for this AB edit exercise
     * @return the view
     */
    @Override
    public ABview getExerciseView() {
        return abView;
    }

    /**
     * Save exercise to disk
     * @param saveAs true if "save as" should be invoked, and otherwise false
     */
    @Override
    public void saveExercise(boolean saveAs) {
        boolean success = DiskUtilities.saveExercise(saveAs, getABmodelFromView());
        if (success) exerciseModified = false;
    }

    /**
     * List of nodes to be sent to printer for this exercise
     * @return the node list
     */
    @Override
    public List<Node> getPrintNodes(ExerciseModel originalModel) {
        List<Node> nodeList = new ArrayList<>();
        ABexercise printExercise = this;
        ABmodel printModel = abModel;

        double nodeWidth = PrintUtilities.getPageWidth() / mainWindow.getBaseScale();

        //header node
        Label exerciseName = new Label(abModel.getExerciseName());
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


        //content nodes
        ABmodelExtra fields = abModel.getModelFields();
        Label leaderLabel = new Label(fields.getLeader());
        CheckBox boxA = new CheckBox(fields.getPromptA());
        boxA.setSelected(fields.getValueA());
        CheckBox boxB = new CheckBox(fields.getPromptB());
        boxB.setSelected(fields.getValueB());
        leaderLabel.setFont(labelFont); boxA.setFont(labelFont); boxB.setFont(labelFont);

        HBox abBox = new HBox(20);
        abBox.setPadding(new Insets(10,10,10,0));
        abBox.getChildren().addAll(leaderLabel, boxA, boxB);
        nodeList.add(abBox);

        List<DecoratedRTA> pageList = printExercise.getExerciseView().getContentPageList();
        List<PageContent> pageContents = printModel.getPageContents();
        for (int i = 0; i < pageList.size(); i++) {
            DecoratedRTA drta = pageList.get(i);
            RichTextArea pageRTA = drta.getEditor();
            pageRTA.prefHeightProperty().unbind();

            pageRTA.setPrefHeight(pageContents.get(i).getTextHeight() + 35);
            pageRTA.setContentAreaWidth(nodeWidth);
            pageRTA.setPrefWidth(nodeWidth);
            nodeList.add(pageRTA);
        }

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

    /**
     * Return to the initial (unworked) version of the exercise, retaining the comment only.
     * @return the initial exercise
     */
    @Override
    public ABexercise resetExercise() {
        RichTextArea commentRTA = abView.getExerciseComment().getEditor();
        commentRTA.getActionFactory().saveNow().execute(new ActionEvent());
        Document commentDocument = commentRTA.getDocument();
        ABmodel originalModel = (ABmodel) (abModel.getOriginalModel());
        originalModel.setExerciseComment(commentDocument);
        ABexercise clearExercise = new ABexercise(originalModel, mainWindow);
        return clearExercise;
    }

    /**
     * Exercise is modified if it is changed relative to last save
     * @return true if exercise is modified, and otherwise false
     */
    @Override
    public boolean isExerciseModified() {
        RichTextArea commentEditor = abView.getExerciseComment().getEditor();
        if (commentEditor.isModified()) exerciseModified = true;

        List<DecoratedRTA> exerciseContent = abView.getContentPageList();
        for (DecoratedRTA drta : exerciseContent) {
            RichTextArea editor = drta.getEditor();
            if (editor.isModified()) {
                exerciseModified = true;
            }
        }
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
     * No FF view node for the AB page edit exercise
     * @return null
     */
    @Override
    public Node getFFViewNode() {return null;}

    /**
     * No FF print node for the page edit exercise
     * @return null
     */
    @Override
    public Node getFFPrintNode(ExerciseModel originalModel) {return null;}

    @Override
    public Spinner getFFHeightSpinner() {return null;}
    @Override
    public Spinner getFFWidthSpinner() {return null;}

    /**
     * Extract an {@link slapp.editor.main_window.ExerciseModel} from view of the exercise
     * @return the exercise model
     */
    @Override
    public ExerciseModel getExerciseModelFromView() {
        return (ExerciseModel) getABmodelFromView();
    }

    /*
     * Extract AB edit model from view
     * @return the page edit model
     */
    private ABmodel getABmodelFromView() {
        RichTextArea commentRTA = abView.getExerciseComment().getEditor();
        commentRTA.getActionFactory().saveNow().execute(new ActionEvent());
        Document commentDocument = commentRTA.getDocument();

        List<DecoratedRTA> exerciseContent = abView.getContentPageList();
        List<PageContent> currentContents = abModel.getPageContents();
        List<PageContent> contentList = new ArrayList<>();

        for (int i = 0; i < exerciseContent.size(); i++) {
            DecoratedRTA drta = exerciseContent.get(i);
            RichTextArea editor = drta.getEditor();
            if (editor.isModified()) exerciseModified = true;
            editor.getActionFactory().saveNow().execute(new ActionEvent());
            Document doc = editor.getDocument();
            double textHeight = currentContents.get(i).getTextHeight();
            PageContent pageContent = new PageContent(doc, textHeight);
            contentList.add(pageContent);
        }

        String name = abModel.getExerciseName();
        String prompt = abModel.getContentPrompt();

        String leader = abModel.getModelFields().getLeader();
        String promptA = abModel.getModelFields().getPromptA();
        boolean valueA = abView.getCheckBoxA().isSelected();
        String promptB = abModel.getModelFields().getPromptB();
        boolean valueB = abView.getCheckBoxB().isSelected();
        ABmodelExtra extra = new ABmodelExtra(leader, promptA, valueA, promptB, valueB);

        boolean started = (abModel.isStarted() || exerciseModified);
        abModel.setStarted(started);
        double statementHeight = abView.getExerciseStatement().getEditor().getPrefHeight();
        Document statementDocument = abModel.getExerciseStatement();
        ABmodel newModel = new ABmodel(name, extra, started, prompt, statementHeight, statementDocument, commentDocument, contentList);
        newModel.setOriginalModel(abModel.getOriginalModel());
        newModel.setCommentPrefHeight(abView.getCommentPrefHeight());
        newModel.setPaginationPrefHeight(abView.getPaginationPrefHeight());
        newModel.setCommentTextHeight(abModel.getCommentTextHeight());
        newModel.setStatementTextHeight(abModel.getStatementTextHeight());

        return newModel;
    }

}


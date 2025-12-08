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

package slapp.editor.page_editor;

import com.gluonhq.richtextarea.RichTextArea;
import com.gluonhq.richtextarea.model.Document;
import javafx.application.Platform;
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
import slapp.editor.EditorAlerts;
import slapp.editor.PrintUtilities;
import slapp.editor.decorated_rta.DecoratedRTA;
import slapp.editor.main_window.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import slapp.editor.DiskUtilities;

import static javafx.scene.control.ButtonType.OK;

/**
 * Basic page edit exsericise
 */
public class PageEditExercise implements Exercise<PageEditModel, PageEditView> {
    private MainWindow mainWindow;
    private PageEditModel editModel;
    private PageEditView editView;
    private MainWindowView mainView;
    private boolean exerciseModified = false;
    private int lastPageNum = -1;

    /**
     * Construct page edit exercise
     * @param model a page edit model
     * @param mainWindow the main window
     */
    public PageEditExercise(PageEditModel model, MainWindow mainWindow) {
        this.mainWindow = mainWindow;
        this.editModel = model;
        if (model.getOriginalModel() == null) {model.setOriginalModel(model); }
        this.mainView = mainWindow.getMainView();
        this.editView = new PageEditView(mainView);

        setEditView();
    }

    /*
     * Set up statement, comment and pagination view elements.
     */
    private void setEditView() {

        editView.setContentPrompt(editModel.getContentPrompt());
        editView.setStatementPrefHeight(editModel.getStatementPrefHeight());
        editView.setCommentPrefHeight(editModel.getCommentPrefHeight());
        editView.setPaginationPrefHeight(editModel.getPaginationPrefHeight());

        editView.setPointsPossible(editModel.getPointsPossible());
        if (editModel.getPointsEarned() >= 0) editView.getPointsEarnedTextField().setText(Integer.toString(editModel.getPointsEarned()));
        editView.getPointsEarnedTextField().textProperty().addListener((ob, ov, nv) -> {
            exerciseModified = true;
        });

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

        //pagination
        List<DecoratedRTA> contentList = new ArrayList<>();
        List<PageContent> pageContents = editModel.getPageContents();
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
        editView.setContentPageList(contentList);

        //cleanup
        editView.initializeViewDetails();
        editView.getAddPageButton().setOnAction(e -> addPageAction());
        editView.getRemovePageButton().setOnAction(e -> removePageAction());
    }

    /*
     * Add page after the current page
     */
    private void addPageAction() {
        int newPageIndex = editView.getContentPageIndex() + 1;
        editModel.addBlankContentPage(newPageIndex);

        DecoratedRTA drta = new DecoratedRTA();
        RichTextArea editor = drta.getEditor();
        editor.getActionFactory().saveNow().execute(new ActionEvent());

        editor.addEventHandler(KeyEvent.KEY_RELEASED, e -> {
            exerciseModified = true;
            double textHeight = mainView.getRTATextHeight(editor);
            editModel.getPageContents().get(newPageIndex).setTextHeight(textHeight);
        });


        mainView.editorInFocus(drta, ControlType.AREA);
        editor.focusedProperty().addListener((o, ov, nv) -> {
            if (nv) {
                mainView.editorInFocus(drta, ControlType.AREA);
            }
        });
        editView.addBlankContentPage(newPageIndex, drta);

    }

    /*
     * Remove the current page
     */
    private void removePageAction() {
        if (editModel.getPageContents().size() <= 1) {
            EditorAlerts.showSimpleAlert("Cannot Remove", "Your response must include at least one page.");
        }
        else {
            int currentPageIndex = editView.getContentPageIndex();
            boolean okContinue = true;
            if (editView.getContentPageList().get(currentPageIndex).getEditor().isModified()) {
                Alert confirm = EditorAlerts.confirmationAlert("Confirm Remove", "This page appears to have unsaved changes.  Continue to remove?");
                Optional<ButtonType> result = confirm.showAndWait();
                if (result.get() != OK) okContinue = false;
            }
            if (okContinue) {
                editModel.getPageContents().remove(currentPageIndex);
                editView.removeContentPage(currentPageIndex);
                exerciseModified = true;

            }
        }
    }

    /**
     * Model for this page edit exercise
     * @return the model
     */
    @Override
    public PageEditModel getExerciseModel() {
        return editModel;
    }

    /**
     * View for this page edit exercise
     * @return the view
     */
    @Override
    public PageEditView getExerciseView() {
        return editView;
    }

    /**
     * Save exercise to disk
     * @param saveAs true if "save as" should be invoked, and otherwise false
     */
    @Override
    public void saveExercise(boolean saveAs) {
        boolean success = DiskUtilities.saveExercise(saveAs, getPageEditModelFromView());
        if (success) exerciseModified = false;
    }

    /**
     * List of nodes to be sent to printer for this exercise
     * @return the node list
     */
    @Override
    public List<Node> getPrintNodes(ExerciseModel originalModel) {
        List<Node> nodeList = new ArrayList<>();


        PageEditModel printModel = editModel;
        PageEditExercise printExercise = this;

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


        //content nodes
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

    /**
     * Return to the initial (unworked) version of the exercise, retaining the comment only.
     * @return the initial exercise
     */
    @Override
    public PageEditExercise resetExercise() {
        RichTextArea commentRTA = editView.getExerciseComment().getEditor();
        commentRTA.getActionFactory().saveNow().execute(new ActionEvent());
        Document commentDocument = commentRTA.getDocument();
        int pointsEarned = -1;
        if (!editView.getPointsEarnedTextField().getText().equals("")) pointsEarned = Integer.parseInt(editView.getPointsEarnedTextField().getText());
        PageEditModel originalModel = (PageEditModel) (editModel.getOriginalModel());
        originalModel.setExerciseComment(commentDocument);
        originalModel.setPointsEarned(pointsEarned);
        PageEditExercise clearExercise = new PageEditExercise(originalModel, mainWindow);
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

        List<DecoratedRTA> exerciseContent = editView.getContentPageList();
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
     * No FF view node for the page edit exercise
     * @return null
     */
    @Override
    public Node getFFViewNode() {return null;}
    @Override
    public Spinner getFFHeightSpinner() {return null;}
    @Override
    public Spinner getFFWidthSpinner() {return null;}

    /**
     * No FF print node for the page edit exercise
     * @return null
     */
    @Override
    public Node getFFPrintNode(ExerciseModel originalModel) {return null;}

    /**
     * Extract an {@link slapp.editor.main_window.ExerciseModel} from view of the exercise
     * @return the exercise model
     */
    @Override
    public ExerciseModel getExerciseModelFromView() {
        return (ExerciseModel) getPageEditModelFromView();
    }

    /*
     * Extract page edit model from view
     * @return the page edit model
     */
    private PageEditModel getPageEditModelFromView() {
        RichTextArea commentRTA = editView.getExerciseComment().getEditor();
        commentRTA.getActionFactory().saveNow().execute(new ActionEvent());
        Document commentDocument = commentRTA.getDocument();


        List<DecoratedRTA> exerciseContent = editView.getContentPageList();
        List<PageContent> currentContents = editModel.getPageContents();
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

        String name = editModel.getExerciseName();
        String prompt = editModel.getContentPrompt();


        boolean started = (editModel.isStarted() || exerciseModified);
        editModel.setStarted(started);

        Document statementDocument = editModel.getExerciseStatement();
        double statementHeight = editView.getExerciseStatement().getEditor().getPrefHeight();

        PageEditModel newModel = new PageEditModel(name, started, prompt, statementHeight, statementDocument, commentDocument, contentList);
        newModel.setOriginalModel(editModel.getOriginalModel());
        newModel.setCommentPrefHeight(editView.getCommentPrefHeight());
        newModel.setPaginationPrefHeight(editView.getPaginationPrefHeight());
        newModel.setCommentTextHeight(editModel.getCommentTextHeight());
        newModel.setStatementTextHeight(editModel.getStatementTextHeight());

        newModel.setPointsPossible(editModel.getPointsPossible());
        if (!editView.getPointsEarnedTextField().getText().equals("")) newModel.setPointsEarned(Integer.parseInt(editView.getPointsEarnedTextField().getText()));
        else newModel.setPointsEarned(-1);

        return newModel;
    }

}

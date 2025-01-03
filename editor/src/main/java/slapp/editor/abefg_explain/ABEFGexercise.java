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

package slapp.editor.abefg_explain;

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
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import slapp.editor.DiskUtilities;
import slapp.editor.EditorAlerts;
import slapp.editor.PrintUtilities;
import slapp.editor.decorated_rta.DecoratedRTA;
import slapp.editor.main_window.*;
import slapp.editor.page_editor.PageContent;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static javafx.scene.control.ButtonType.OK;

/**
 * AB/EFG exercise has binary and tertiary choices and explain field
 */
public class ABEFGexercise implements Exercise<ABEFGmodel, ABEFGview> {
    private MainWindow mainWindow;
    private ABEFGmodel abefgModel;
    private ABEFGview abefgView;
    private MainWindowView mainView;
    private boolean exerciseModified = false;
    private int lastPageNum = -1;
    private Font labelFont = new Font("Noto Serif Combo", 11);


    /**
     * Construct AB/EFG edit exercise
     * @param model an AB/EFG edit model
     * @param mainWindow the main window
     */
    public ABEFGexercise(ABEFGmodel model, MainWindow mainWindow) {
        this.mainWindow = mainWindow;
        this.abefgModel = model;
        if (model.getOriginalModel() == null) {model.setOriginalModel(model); }
        this.mainView = mainWindow.getMainView();
        this.abefgView = new ABEFGview(mainView);

        setEditView();
    }

    /*
     * Set up choices, statement, comment, and pagination view elements
     */
    private void setEditView() {

        abefgView.setContentPrompt(abefgModel.getContentPrompt());
        abefgView.setStatementPrefHeight(abefgModel.getStatementPrefHeight());
        abefgView.setCommentPrefHeight(abefgModel.getCommentPrefHeight());
        abefgView.setPaginationPrefHeight(abefgModel.getPaginationPrefHeight());

        //choices
        ABEFGmodelExtra modelFields = abefgModel.getModelFields();
        abefgView.getLeaderLabelAB().setText(modelFields.getLeaderAB());
        abefgView.getLeaderLabelEFG().setText(modelFields.getLeaderEFG());
        CheckBox checkBoxA = abefgView.getCheckBoxA();
        CheckBox checkBoxB = abefgView.getCheckBoxB();
        CheckBox checkBoxE = abefgView.getCheckBoxE();
        CheckBox checkBoxF = abefgView.getCheckBoxF();
        CheckBox checkBoxG = abefgView.getCheckBoxG();

        checkBoxA.setText(modelFields.getPromptA());
        checkBoxA.setSelected(modelFields.getValueA());
        checkBoxA.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue ob, Boolean ov, Boolean nv) {
                if (nv == true) checkBoxB.setSelected(false);
                exerciseModified = true;
            }
        });

        checkBoxB.setText(modelFields.getPromptB());
        checkBoxB.setSelected(modelFields.getValueB());
        checkBoxB.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue ob, Boolean ov, Boolean nv) {
                if (nv == true) checkBoxA.setSelected(false);
                exerciseModified = true;
            }
        });

        checkBoxE.setText(modelFields.getPromptE());
        checkBoxE.setSelected(modelFields.getValueE());
        checkBoxE.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue ob, Boolean ov, Boolean nv) {
                if (nv == true) {
                    checkBoxF.setSelected(false);
                    checkBoxG.setSelected(false);
                    exerciseModified = true;
                }
            }
        });

        checkBoxF.setText(modelFields.getPromptF());
        checkBoxF.setSelected(modelFields.getValueF());
        checkBoxF.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue ob, Boolean ov, Boolean nv) {
                if (nv == true) {
                    checkBoxE.setSelected(false);
                    checkBoxG.setSelected(false);
                    exerciseModified = true;
                }
            }
        });

        checkBoxG.setText(modelFields.getPromptG());
        checkBoxG.setSelected(modelFields.getValueG());
        checkBoxG.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue ob, Boolean ov, Boolean nv) {
                if (nv == true) {
                    checkBoxE.setSelected(false);
                    checkBoxF.setSelected(false);
                    exerciseModified = true;
                }
            }
        });

        //statement
        DecoratedRTA statementDRTA = new DecoratedRTA();
        RichTextArea statementEditor = statementDRTA.getEditor();
        statementEditor.getActionFactory().open(abefgModel.getExerciseStatement()).execute(new ActionEvent());

        mainView.editorInFocus(statementDRTA, ControlType.STATEMENT);
        statementEditor.focusedProperty().addListener((o, ov, nv) -> {
            if (nv) {
                mainView.editorInFocus(statementDRTA, ControlType.STATEMENT);
            }
        });
        abefgView.setExerciseStatement(statementDRTA);

        //comment
        DecoratedRTA commentDRTA = new DecoratedRTA();
        RichTextArea commentEditor = commentDRTA.getEditor();

        commentEditor.addEventHandler(KeyEvent.KEY_RELEASED, e -> {
            exerciseModified = true;
            double commentTextHeight = mainView.getRTATextHeight(commentEditor);
            abefgModel.setCommentTextHeight(commentTextHeight);
        });
        commentEditor.getActionFactory().open(abefgModel.getExerciseComment()).execute(new ActionEvent());

        mainView.editorInFocus(commentDRTA, ControlType.AREA);
        commentEditor.focusedProperty().addListener((o, ov, nv) -> {
            if (nv) {
                mainView.editorInFocus(commentDRTA, ControlType.AREA);
            }
        });
        abefgView.setExerciseComment(commentDRTA);

        //pagination
        List<DecoratedRTA> contentList = new ArrayList<>();
        List<PageContent> pageContents = abefgModel.getPageContents();

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
        abefgView.setContentPageList(contentList);

        //cleanup
        abefgView.initializeViewDetails();
        abefgView.getAddPageButton().setOnAction(e -> addPageAction());
        abefgView.getRemovePageButton().setOnAction(e -> removePageAction());
    }

    /*
     * Add page after the current page
     */
    private void addPageAction() {
        int newPageIndex = abefgView.getContentPageIndex() + 1;
        abefgModel.addBlankContentPage(newPageIndex);

        DecoratedRTA drta = new DecoratedRTA();
        RichTextArea editor = drta.getEditor();
        editor.getActionFactory().saveNow().execute(new ActionEvent());

        editor.addEventHandler(KeyEvent.KEY_RELEASED, e -> {
            exerciseModified = true;
            double textHeight = mainView.getRTATextHeight(editor);
            abefgModel.getPageContents().get(newPageIndex).setTextHeight(textHeight);
        });

        mainView.editorInFocus(drta, ControlType.AREA);
        editor.focusedProperty().addListener((o, ov, nv) -> {
            if (nv) {
                mainView.editorInFocus(drta, ControlType.AREA);
            }
        });
        abefgView.addBlankContentPage(newPageIndex, drta);
    }

    /*
     * Remove the current page
     */
    private void removePageAction() {
        if (abefgModel.getPageContents().size() <= 1) {
            EditorAlerts.showSimpleAlert("Cannot Remove", "Your response must include at least one page.");
        }
        else {
            int currentPageIndex = abefgView.getContentPageIndex();
            boolean okContinue = true;
            if (abefgView.getContentPageList().get(currentPageIndex).getEditor().isModified()) {
                Alert confirm = EditorAlerts.confirmationAlert("Confirm Remove", "This page appears to have unsaved changes.  Continue to remove?");
                Optional<ButtonType> result = confirm.showAndWait();
                if (result.get() != OK) okContinue = false;
            }
            if (okContinue) {
                abefgModel.getPageContents().remove(currentPageIndex);
                abefgView.removeContentPage(currentPageIndex);
                exerciseModified = true;
            }
        }
    }

    /**
     * Model for this AB/EFG edit exercise
     * @return the model
     */
    @Override
    public ABEFGmodel getExerciseModel() {
        return abefgModel;
    }

    /**
     * View for this AB/EFG edit exercise
     * @return the view
     */
    @Override
    public ABEFGview getExerciseView() {
        return abefgView;
    }

    /**
     * Save exercise to disk
     * @param saveAs true if "save as" should be invoked, and otherwise false
     */
    @Override
    public void saveExercise(boolean saveAs) {
        boolean success = DiskUtilities.saveExercise(saveAs, getABEFGmodelFromView());
        if (success) exerciseModified = false;
    }

    /**
     * List of nodes to be sent to printer for this exercise
     * @return the node list
     */
    @Override
    public List<Node> getPrintNodes(ExerciseModel originalModel) {
        List<Node> nodeList = new ArrayList<>();
        ABEFGmodel printModel = abefgModel;
        ABEFGexercise printExercise = this;
        double nodeWidth = PrintUtilities.getPageWidth() / mainWindow.getBaseScale();

        //header node
        Label exerciseName = new Label(abefgModel.getExerciseName());
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
        statementRTA.prefWidthProperty().unbind();
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
        ABEFGmodelExtra fields = abefgModel.getModelFields();
        Label leaderLabelAB = new Label(fields.getLeaderAB());
        CheckBox boxA = new CheckBox(fields.getPromptA());
        boxA.setSelected(fields.getValueA());
        CheckBox boxB = new CheckBox(fields.getPromptB());
        boxB.setSelected(fields.getValueB());
        leaderLabelAB.setFont(labelFont); boxA.setFont(labelFont); boxB.setFont(labelFont);
        HBox abBox = new HBox(20);
        abBox.getChildren().addAll(leaderLabelAB, boxA, boxB);

        Label leaderLabelEFG = new Label(fields.getLeaderEFG());
        CheckBox boxE = new CheckBox(fields.getPromptE());
        boxE.setSelected(fields.getValueE());
        CheckBox boxF = new CheckBox(fields.getPromptF());
        boxF.setSelected(fields.getValueF());
        CheckBox boxG = new CheckBox(fields.getPromptG());
        boxG.setSelected(fields.getValueG());
        leaderLabelEFG.setFont(labelFont); boxE.setFont(labelFont); boxF.setFont(labelFont); boxG.setFont(labelFont);
        HBox efgBox = new HBox(20);
        efgBox.getChildren().addAll(leaderLabelEFG, boxE, boxF, boxG);

        VBox checksBox = new VBox(10, abBox, efgBox);
        checksBox.setPadding(new Insets(10,0,20,0));

        nodeList.add(checksBox);

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
    public ABEFGexercise resetExercise() {
        RichTextArea commentRTA = abefgView.getExerciseComment().getEditor();
        commentRTA.getActionFactory().saveNow().execute(new ActionEvent());
        Document commentDocument = commentRTA.getDocument();
        ABEFGmodel originalModel = (ABEFGmodel) (abefgModel.getOriginalModel());
        originalModel.setExerciseComment(commentDocument);
        ABEFGexercise clearExercise = new ABEFGexercise(originalModel, mainWindow);
        return clearExercise;
    }

    /**
     * Exercise is modified if it is changed relative to last save
     * @return true if exercise is modified, and otherwise false
     */
    @Override
    public boolean isExerciseModified() {
        RichTextArea commentEditor = abefgView.getExerciseComment().getEditor();
        if (commentEditor.isModified()) exerciseModified = true;

        List<DecoratedRTA> exerciseContent = abefgView.getContentPageList();
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
     * No FF view node for the AB/EFG page edit exercise
     * @return null
     */
    @Override
    public Node getFFViewNode() {return null;}

    /**
     * No FF print node for the AB/EFG page edit exercise
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
        return (ExerciseModel) getABEFGmodelFromView();
    }

    /*
     * Extract AB/EFG edit model from view
     * @return the page edit model
     */
    private ABEFGmodel getABEFGmodelFromView() {
        RichTextArea commentRTA = abefgView.getExerciseComment().getEditor();
        commentRTA.getActionFactory().saveNow().execute(new ActionEvent());
        Document commentDocument = commentRTA.getDocument();

        List<DecoratedRTA> exerciseContent = abefgView.getContentPageList();
        List<PageContent> currentContents = abefgModel.getPageContents();
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
        String name = abefgModel.getExerciseName();
        String prompt = abefgModel.getContentPrompt();

        ABEFGmodelExtra fields = abefgModel.getModelFields();
        String leaderAB = fields.getLeaderAB();
        String promptA = fields.getPromptA();
        boolean valueA = abefgView.getCheckBoxA().isSelected();
        String promptB = fields.getPromptB();
        boolean valueB = abefgView.getCheckBoxB().isSelected();

        String leaderEFG = fields.getLeaderEFG();
        String promptE = fields.getPromptE();
        boolean valueE = abefgView.getCheckBoxE().isSelected();
        String promptF = fields.getPromptF();
        boolean valueF = abefgView.getCheckBoxF().isSelected();
        String promptG = fields.getPromptG();
        boolean valueG = abefgView.getCheckBoxG().isSelected();


        ABEFGmodelExtra extra = new ABEFGmodelExtra(leaderAB, promptA, valueA, promptB, valueB, leaderEFG, promptE, valueE, promptF, valueF, promptG, valueG);

        boolean started = (abefgModel.isStarted() || exerciseModified);
        abefgModel.setStarted(started);

        Document statementDocument = abefgModel.getExerciseStatement();
        double statementHeight = abefgView.getExerciseStatement().getEditor().getPrefHeight();

        ABEFGmodel newModel = new ABEFGmodel(name, extra, started, prompt, statementHeight, statementDocument, commentDocument, contentList);
        newModel.setOriginalModel(abefgModel.getOriginalModel());
        newModel.setCommentPrefHeight(abefgView.getCommentPrefHeight());
        newModel.setPaginationPrefHeight(abefgView.getPaginationPrefHeight());
        newModel.setCommentTextHeight(abefgModel.getCommentTextHeight());
        newModel.setStatementTextHeight(abefgModel.getStatementTextHeight());

        return newModel;
    }

}


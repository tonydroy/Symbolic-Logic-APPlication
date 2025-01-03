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

package slapp.editor.horizontal_tree;

import com.gluonhq.richtextarea.model.Document;
import slapp.editor.PrintUtilities;
import slapp.editor.main_window.ExerciseModel;
import slapp.editor.main_window.ExerciseType;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Model for the horizontal tree exercise
 */
public class HorizontalTreeModel implements ExerciseModel<Document>, Serializable {
    private static final long serialVersionUID = 100L;

    private String exerciseName = new String("");
    private ExerciseType exerciseType = ExerciseType.HORIZONTAL_TREE;
    private ExerciseModel<Document> originalModel = null;
    private boolean started = false;
    private Document exerciseStatement = new Document();
    private double statementPrefHeight = 80;
    private double statementTextHeight = 0;
    private Document exerciseComment = new Document();
    private double commentPrefHeight = 60;
    private double commentTextHeight = 80;
    private Document explainDocument = new Document();
    private double explainPrefHeight = 70;
    private double explainTextHeight = 0;
    private String explainPrompt = "";
    private List<TreeModel> treeModels = new ArrayList<>();
    private double mainPaneWidth;
    private boolean axis = false;


    /**
     * Construct horizontal tree model
     */
    public HorizontalTreeModel() {}

    /**
     * The Document for the explanation field
     * @return the document
     */
    Document getExplainDocument() {return explainDocument; }

    /**
     * The prompt for the explain field
     * @return the String prompt
     */
    String getExplainPrompt() {   return explainPrompt;  }

    /**
     * The prompt for the explain field
     * @param explainPrompt the string prompt
     */
    void setExplainPrompt(String explainPrompt) { this.explainPrompt = explainPrompt; }


    /**
     * The exercise name (which doubles as file name)
     * @param exerciseName the String name
     */
    void setExerciseName(String exerciseName) { this.exerciseName = exerciseName;  }

    /**
     * The document for the exercise statement field
     * @param exerciseStatement the Document
     */
    void setExerciseStatement(Document exerciseStatement) {   this.exerciseStatement = exerciseStatement;    }

    /**
     * The document for the explanation field
     * @param explainDocument the Document
     */
    void setExplainDocument(Document explainDocument) { this.explainDocument = explainDocument;  }

    /**
     * True if axis is to appear, and otherwise false
     * @param axis the boolean value
     */
    void setAxis(boolean axis) { this.axis = axis; }

    /**
     * True if axis is to appear and otherwise false
     * @return the boolean value
     */
    boolean isAxis() {     return axis;  }

    /**
     * The list of tree models for this exercise
     * @return the list of models
     */
    List<TreeModel> getTreeModels() {  return treeModels;  }

    /**
     * The preferred height of the comment field
     * @return the height value
     */
    double getCommentPrefHeight() {     return commentPrefHeight;  }

    /**
     * The preferred height of the comment field
     * @param commentPrefHeight the height value
     */
    void setCommentPrefHeight(double commentPrefHeight) {     this.commentPrefHeight = commentPrefHeight;   }

    /**
     * The preferred height of the explanation field
     * @return the height value
     */
    double getExplainPrefHeight() {     return explainPrefHeight;  }

    /**
     * The preferred height of the explanation field
     * @param explainPrefHeight the height value
     */
    void setExplainPrefHeight(double explainPrefHeight) {     this.explainPrefHeight = explainPrefHeight;   }

    /**
     * The text height in the statement field
     * @return the height value
     */
    double getStatementTextHeight() {     return statementTextHeight;  }

    /**
     * The text height in the statement field
     * @param statementTextHeight the height value
     */
    void setStatementTextHeight(double statementTextHeight) {     this.statementTextHeight = statementTextHeight;   }

    /**
     * The text height in the comment field
     * @return the height value
     */
    double getCommentTextHeight() {     return commentTextHeight;  }

    /**
     * The text height in the comment field
     * @param commentTextHeight the height value
     */
    void setCommentTextHeight(double commentTextHeight) {     this.commentTextHeight = commentTextHeight;   }

    /**
     * The text height in the explanation field
     * @return the height value
     */
    double getExplainTextHeight() {     return explainTextHeight;  }

    /**
     * The text height in the explanation field
     * @param explainTextHeight the height value
     */
    void setExplainTextHeight(double explainTextHeight) {     this.explainTextHeight = explainTextHeight;   }

    /**
     * Width of the main (drawing) pane
     * @return the width value
     */
    double getMainPaneWidth() {     return mainPaneWidth;  }

    /**
     * Width of the main (drawing) pane
     * @param mainPaneWidth the width value
     */
    void setMainPaneWidth(double mainPaneWidth) {     this.mainPaneWidth = mainPaneWidth;  }

    /**
     * The exercise name (used also as file name)
     * @return the name string
     */
    @Override
    public String getExerciseName() { return exerciseName; }

    /**
     * The {@link slapp.editor.main_window.ExerciseType}
     * @return the HORIZONTAL_TREE exercise type
     */
    @Override
    public ExerciseType getExerciseType() { return exerciseType;  }

    /**
     * Exercise is <em>started</em> if it is differs from the originally created version.
     * @return true if started, and otherwise false.
     */
    @Override
    public boolean isStarted() { return started; }

    /**
     * Exercise is <em>started</em> if it differs from the originally created version.
     * @param started true if started, and otherwise false
     */
    @Override
    public void setStarted(boolean started) { this.started = started;  }

    /**
     * The comment on this exercise, which may be by either an instructor or student
     * @return the comment Document.
     */
    @Override
    public Document getExerciseComment() { return exerciseComment; }

    /**
     * The exercise comment on this exercise, which may be by either an instructor or student
     * @param document the comment Document
     */
    @Override
    public void setExerciseComment(Document document) { this.exerciseComment = document;  }

    /**
     * The exercise prompt
     * @return The statement document (T)
     */
    @Override
    public Document getExerciseStatement() { return exerciseStatement; }

    /**
     * The pref height of the exercise statement window
     * @return the statement height
     */
    @Override
    public double getStatementPrefHeight() { return statementPrefHeight;  }

    /**
     * The height of the exercise statement window.
     * @param height the statement height
     */
    @Override
    public void setStatementPrefHeight(double height) { this.statementPrefHeight = height; }

    /**
     * The original model for the (unworked) exercise
     * @return the original model
     */
    @Override
    public ExerciseModel<Document> getOriginalModel() { return originalModel; }

    /**
     * The original model for the (unworked exercise)
     * @param exerciseModel the original model
     */
    @Override
    public void setOriginalModel(ExerciseModel<Document> exerciseModel) { this.originalModel = exerciseModel; }

    /**
     * Override toString with the exercise name
     * @return the string representation
     */
    @Override
    public String toString() { return exerciseName; }


}

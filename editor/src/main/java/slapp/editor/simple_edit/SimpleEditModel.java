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

import com.gluonhq.richtextarea.model.Document;
import slapp.editor.main_window.ExerciseModel;
import slapp.editor.main_window.ExerciseType;
import slapp.editor.page_editor.PageContent;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Model for the SimpleEdit exercise
 */
public class SimpleEditModel implements ExerciseModel <Document>, Serializable {
    private static final long serialVersionUID = 100L;
    private String exerciseName = new String("");
    private ExerciseType exerciseType = ExerciseType.SIMPLE_EDIT;
    private ExerciseModel<Document> originalModel = null;
    private boolean started = false;
    private Document exerciseStatement = new Document();
    private double statementPrefHeight = 80;
    private double statementTextHeight = 0;
    private Document exerciseComment = new Document("");
    private double commentPrefHeight = 60;
    private double commentTextHeight = 0;
    private Document exerciseResponse = new Document("");
    private double responsePrefHeight = 350;
    private double responseTextHeight = 0;
    private String responsePrompt = "";
    private int pointsPossible;
    private int pointsEarned;


    /**
     * Construct the SimpleEditModel
     * @param name exercise name
     * @param prompt content area prompt
     */
    public SimpleEditModel(String name, String prompt) {
        exerciseName = name;
        responsePrompt = prompt;
        this.pointsPossible = 0;
        this.pointsEarned = -1;
    }

    /**
     * The comment text height is the height of the text in the comment area (not the height of the comment window).
     * @return the text height
     */
    double getCommentTextHeight() {
        return commentTextHeight;
    }

    /**
     * The comment text height is the height of the text in the comment area (not the height of the comment window).
     * @param commentTextHeight the height value
     */
    void setCommentTextHeight(double commentTextHeight) {
        this.commentTextHeight = commentTextHeight;
    }

    /**
     * The exercise exercise prompt
     * @param exerciseStatement the prompt Document
     */
    void setExerciseStatement(Document exerciseStatement) {  this.exerciseStatement = exerciseStatement;  }

    /**
     * The statement text height is the height of the text in the statement area (not the height of the statement window).
     * @return the height value
     */
    double getStatementTextHeight() {
        return statementTextHeight;
    }

    /**
     * The statement text height is the height of the text in the statement area (not the height of the statement window)
     * @param statementTextHeight the height value
     */
    void setStatementTextHeight(double statementTextHeight) {    this.statementTextHeight = statementTextHeight;  }

    /**
     * The commentPrefHeight is the preferred height of the comment window.
     * @return the height value
     */
    double getCommentPrefHeight() {     return commentPrefHeight;  }

    /**
     * The commentPrefHeight is the preferred height of the comment window.
     * @param commentPrefHeight the height value
     */
    void setCommentPrefHeight(double commentPrefHeight) {    this.commentPrefHeight = commentPrefHeight;  }

    /**
     * The exercise response is the main work area Document
     * @return the response document
     */
    Document getExerciseResponse() {   return exerciseResponse;  }

    /**
     * The exercise response is the main work area Document
     * @param exerciseResponse the response document
     */
    void setExerciseResponse(Document exerciseResponse) {   this.exerciseResponse = exerciseResponse;  }

    /**
     * The responsePrefHeight is the preferred height of the response window
     * @return the height value
     */
    double getResponsePrefHeight() {     return responsePrefHeight;  }

    /**
     * The responsePrefHeight is the preferred height of the response window
     * @param responsePrefHeight the height value
     */
    public void setResponsePrefHeight(double responsePrefHeight) {    this.responsePrefHeight = responsePrefHeight;  }

    /**
     * The responseTextHeight is the height of the text in the response area (need not be height of window)
     * @return the height value
     */
    double getResponseTextHeight() {     return responseTextHeight;  }

    /**
     * The responseTextHeight is the height of the text in the response area (need not be height of window)
     * @param responseTextHeight the height value
     */
    void setResponseTextHeight(double responseTextHeight) {     this.responseTextHeight = responseTextHeight;  }

    /**
     * The responsePrompt is the prompt string to appear in the response area
     * @return the prompt string
     */
    String getResponsePrompt() {    return responsePrompt;   }

    /**
     * The exerrcise name (used also as file name)
     * @param exerciseName the name string
     */
    void setExerciseName(String exerciseName) {    this.exerciseName = exerciseName;   }

    /**
     * The exercise name (used also as file name)
     * @return the name string
     */
    @Override
    public String getExerciseName() {return exerciseName; }


    /**
     * Every exercise has an {@link slapp.editor.main_window.ExerciseType}
     * @return the SIMPLE_EDIT exercise type
     */
    @Override
    public ExerciseType getExerciseType() { return exerciseType; }

    /**
     * Exercise is <em>started</em> if it is differs from the originally created version.
     * @return true if started, and otherwise false.
     */
    @Override
    public boolean isStarted() { return started;  }

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
    public Document getExerciseComment() { return exerciseComment;  }

    /**
     * The exercise comment on this exercise, which may be by either an instructor or student
     * @param document the comment Document
     */
    @Override
    public void setExerciseComment(Document document) {this.exerciseComment = document; }

    /**
     * The exercise prompt
     * @return The statement (T)
     */
    @Override
    public Document getExerciseStatement() { return exerciseStatement; }

    /**
     * The pref height of the exercise statement window.
     * @return the statement height
     */
    @Override
    public double getStatementPrefHeight() {return statementPrefHeight;  }

    /**
     * The pref height of the exercise statement window.
     * @param statementPrefHeight the statement height
     */
    @Override
    public void setStatementPrefHeight(double statementPrefHeight) {this.statementPrefHeight = statementPrefHeight; }

    /**
     * The original model for the (unworked) exercise
     * @return the original model
     */
    @Override
    public ExerciseModel<Document> getOriginalModel() { return (ExerciseModel) originalModel; }

    /**
     * The original model for the (unworked) exercise
     * @param originalModel the original madel
     */
    public void setOriginalModel(ExerciseModel<Document> originalModel) { this.originalModel = originalModel; }

    /**
     * Override toString with the exercise name
     * @return the name string
     */
    @Override
    public String toString() { return exerciseName; }

    @Override
    public int getPointsPossible() {
        return pointsPossible;
    }

    @Override
    public void setPointsPossible(int pointsPossible) {
        this.pointsPossible = pointsPossible;
    }

    @Override
    public int getPointsEarned() {
        return pointsEarned;
    }

    @Override
    public void setPointsEarned(int pointsEarned) {
        this.pointsEarned = pointsEarned;
    }
}

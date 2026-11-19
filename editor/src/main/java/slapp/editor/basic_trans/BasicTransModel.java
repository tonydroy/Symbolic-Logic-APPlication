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

import com.gluonhq.richtextarea.model.Document;
import slapp.editor.main_window.ExerciseModel;
import slapp.editor.main_window.ExerciseType;
import slapp.editor.parser.grammatical_parts.Formula;

import java.io.Serializable;
import java.util.List;

/**
 * Model for the simple translate exercise.
 */
public class BasicTransModel implements ExerciseModel<Document>, Serializable {
    private static final long serialVersionUID = 100L;

    private String exerciseName = new String("");
    private ExerciseType exerciseType = ExerciseType.BASIC_TRANS;
    private ExerciseModel<Document> originalModel = null;
    private boolean started = false;
    private Document exerciseStatement = new Document();
    private double statementPrefHeight = 80;
    private double statementTextHeight = 0;
    private Document exerciseComment = new Document("");
    private double commentPrefHeight = 70;   //60
    private double commentTextHeight = 0;
    private Document interpFnDoc = new Document();
    private double interpFnPrefHeight = 125;   //90
    private double interpFnTextHeight = 0;
    private Document exerciseResponse = new Document("");
    private double responsePrefHeight = 100;  // 55
    private double responseTextHeight = 0;
    private int pointsPossible;
    private int pointsEarned;
    private String auxExerName;
    private List<Document> targetFormulas;
    private boolean interpFnIncluded;
    private BTransCheckSetup checkSetup;
    private KeyObject keyObject;
    private Formula keyFormula;


    /**
     * Set up the simple translate model
     * @param name the exercise name.
     */
    public BasicTransModel(String name) {
        exerciseName = name;
        this.pointsPossible = 0;
        this.pointsEarned = -1;
        this.auxExerName = "";
    }

    public BTransCheckSetup getCheckSetup() {
        return checkSetup;
    }

    public void setCheckSetup(BTransCheckSetup checkSetup) {
        this.checkSetup = checkSetup;
    }

    public boolean isInterpFnIncluded() {
        return interpFnIncluded;
    }

    public void setInterpFnIncluded(boolean interpFnIncluded) {
        this.interpFnIncluded = interpFnIncluded;
    }

    public KeyObject getKeyObject() {
        return keyObject;
    }

    public void setKeyObject(KeyObject keyObject) {
        this.keyObject = keyObject;
    }

    public List<Document> getTargetFormulas() {
        return targetFormulas;
    }

    public void setTargetFormulas(List<Document> targetFormulas) {
        this.targetFormulas = targetFormulas;
    }

    /**
     * Height of text in comment area
     * @return the height value
     */
    double getCommentTextHeight() {      return commentTextHeight;   }

    /**
     * The height of text in comment area
     * @param commentTextHeight the height value
     */
    void setCommentTextHeight(double commentTextHeight) {      this.commentTextHeight = commentTextHeight;   }

    /**
     * The the exercise prompt
     * @param exerciseStatement the statement document
     */
    void setExerciseStatement(Document exerciseStatement) {  this.exerciseStatement = exerciseStatement;  }

    /**
     * Height of text in the statement area.
     * @return the height value
     */
    double getStatementTextHeight() {    return statementTextHeight;  }

    /**
     * Height of text in the statement area.
     * @param statementTextHeight the height value
     */
    void setStatementTextHeight(double statementTextHeight) {    this.statementTextHeight = statementTextHeight;  }

    /**
     * The preferred height of the comment window.
     * @return the height value
     */
    double getCommentPrefHeight() {     return commentPrefHeight;  }

    /**
     * The preferred height of the comment window
     * @param commentPrefHeight the height value
     */
    void setCommentPrefHeight(double commentPrefHeight) {    this.commentPrefHeight = commentPrefHeight;  }

    /**
     * The response from the main translate area
     * @return the response document
     */
    Document getExerciseResponse() {   return exerciseResponse;  }

    /**
     * The response from the main translate area
     * @param exerciseResponse the response document
     */
    void setExerciseResponse(Document exerciseResponse) {   this.exerciseResponse = exerciseResponse;  }

    /**
     * The preferred height of the response window
     * @return the height value
     */
    double getResponsePrefHeight() {     return responsePrefHeight;  }

    /**
     * The preferred height of the response window
     * @param responsePrefHeight the height value
     */
    void setResponsePrefHeight(double responsePrefHeight) {    this.responsePrefHeight = responsePrefHeight;  }

    /**
     * Height of text in the response area
     * @return the height value
     */
    double getResponseTextHeight() {     return responseTextHeight;  }

    /**
     * Height of text in the response area
     * @param responseTextHeight the height value
     */
    void setResponseTextHeight(double responseTextHeight) {     this.responseTextHeight = responseTextHeight;  }

    /**
     * The area for interpretation function
     * @return the interpretation document
     */
    Document getInterpFnDoc() {   return interpFnDoc;  }

    /**
     * The area for interpretation function
     * @param interpFnDoc the interpretation document
     */
    void setInterpFnDoc(Document interpFnDoc) {   this.interpFnDoc = interpFnDoc;  }

    /**
     * The preferred height of the interpretation window
     * @return the height value
     */
    double getInterpFnPrefHeight() {     return interpFnPrefHeight;  }

    /**
     * The preferred height of the interpretation window
     * @param interpFnPrefHeight the height value
     */
    void setInterpFnPrefHeight(double interpFnPrefHeight) {    this.interpFnPrefHeight = interpFnPrefHeight;  }

    /**
     * Height of text in the interpretation area
     * @return the height value
     */
    double getInterpFnTextHeight() {     return interpFnTextHeight;  }

    /**
     * Height of text in the interpretation area
     * @param interpFnTextHeight the height value
     */
    void setInterpFnTextHeight(double interpFnTextHeight) {     this.interpFnTextHeight = interpFnTextHeight;  }

    public String getAuxExerName() {
        return auxExerName;
    }

    public void setAuxExerName(String auxExerName) {
        this.auxExerName = auxExerName;
    }



    /**
     * The exercise name (used also as file name)
     * @return the name string
     */
    @Override
    public String getExerciseName() {return exerciseName; }

    /**
     * Every exercise has an {@link slapp.editor.main_window.ExerciseType}
     * @return the SIMPLE_TRANS exercise type
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
     * @return The statement document (T)
     */
    @Override
    public Document getExerciseStatement() { return exerciseStatement; }

    /**
     * The pref height of the exercise statement window
     * @return the statement height
     */
    @Override
    public double getStatementPrefHeight() {return statementPrefHeight;  }

    /**
     * The height of the exercise statement window.
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
     * The original model for the (unworked exercise)
     * @param originalModel the original model
     */
    @Override
    public void setOriginalModel(ExerciseModel<Document> originalModel) { this.originalModel = originalModel; }

    /**
     * Override toString to return the exercise name
     * @return the string representation
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
        if (pointsEarned >= 0) return pointsEarned;
        else return 0;
    }

    @Override
    public void setPointsEarned(int pointsEarned) {
        this.pointsEarned = pointsEarned;
    }
}

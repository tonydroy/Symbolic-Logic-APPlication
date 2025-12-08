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

package slapp.editor.horiz_trees;

import com.gluonhq.richtextarea.model.Document;
import slapp.editor.free_form.ElementTypes;
import slapp.editor.free_form.ModelElement;
import slapp.editor.horizontal_tree.HTcheckSetup;
import slapp.editor.main_window.ExerciseModel;
import slapp.editor.main_window.ExerciseType;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Model for the free form exercise
 */
public class HorizTreesModel implements ExerciseModel<Document>, Serializable {
    private static final long serialVersionUID = 100L;
    private String exerciseName = "";
    private ExerciseType exerciseType = ExerciseType.HORIZ_TREES;
    private ExerciseModel<Document> originalModel = null;
    private boolean started = false;
    private Document statementDocument = new Document("");
    private double statementPrefHeight = 80;
    private double statementTextHeight = 0;
    private Document commentDocument = new Document("");
    private double commentPrefHeight = 60;
    private double commentTextHeight = 0;
    private Document explainDocument = new Document();
    private double explainPrefHeight = 70;
    private double explainTextHeight = 0;
    private String explainPrompt = "";
    private List<ModelElement> modelElements = new ArrayList<>();
    private List<ElementTypes> elementTypes = new ArrayList<>();
    private double mainPaneWidth;
    private boolean axis = false;
    private int pointsPossible;
    private int pointsEarned;

    private HorizTreesCheckSetup checkSetup;

    /**
     * Construct free form model
     * @param name exercise name (also file name)
     * @param types the {@link ElementTypes} accommodated by this instance of the free form exercise
     */
    public HorizTreesModel(String name, List<ElementTypes> types) {
        exerciseName = name;
        elementTypes = types;
        this.pointsPossible = 0;
        this.pointsEarned = -1;
    }

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
     * The exercise prompt document
     * @param exerciseStatement the document
     */
    void setExerciseStatement(Document exerciseStatement) {  statementDocument = exerciseStatement;  }

    /**
     * The height of the statement text
     * @return the height value
     */
    double getStatementTextHeight() {
        return statementTextHeight;
    }

    /**
     * The height of the statement text
     * @param statementTextHeight the height value
     */
    void setStatementTextHeight(double statementTextHeight) {    this.statementTextHeight = statementTextHeight;  }

    /**
     * The preferred height of the comment window
     * @return the height value
     */
    double getCommentPrefHeight() {     return commentPrefHeight;  }

    /**
     * The preferred height of the comment window
     * @param commentPrefHeight the height value
     */
    void setCommentPrefHeight(double commentPrefHeight) {    this.commentPrefHeight = commentPrefHeight;  }

    /**
     * The height of the comment text
     * @return the height value
     */
    double getCommentTextHeight() {
        return commentTextHeight;
    }

    /**
     * The height of the comment text
     * @param commentTextHeight the height value
     */
    void setCommentTextHeight(double commentTextHeight) {
        this.commentTextHeight = commentTextHeight;
    }

    /**
     * The list of {@link ModelElement} for this exercise
     * @return the list of elements
     */
    List<ModelElement> getModelElements() {    return modelElements;  }

    /**
     * The list of {@link ModelElement} for this exercise
     * @param modelElements the list of elements
     */
    void setModelElements(List<ModelElement> modelElements) {    this.modelElements = modelElements; }

    /**
     * The list of {@link ElementTypes} that may be included in this exercise
     * @return the list of element types
     */
    List<ElementTypes> getElementTypes() {     return elementTypes;  }

    /**
     * The exercise name (used also as file name)
     * @return the name string
     */

    public HorizTreesCheckSetup getCheckSetup() {
        return checkSetup;
    }

    public void setCheckSetup(HorizTreesCheckSetup checkSetup) {
        this.checkSetup = checkSetup;
    }


    public double getExplainPrefHeight() {
        return explainPrefHeight;
    }

    public void setExplainPrefHeight(double explainPrefHeight) {
        this.explainPrefHeight = explainPrefHeight;
    }

    public void setExplainDocument(Document explainDocument) {
        this.explainDocument = explainDocument;
    }

    @Override
    public String getExerciseName() {  return exerciseName;  }

    /**
     * The {@link ExerciseType}
     * @return the FREE_FORM exercise type
     */
    @Override
    public ExerciseType getExerciseType() {  return exerciseType;  }

    /**
     * Exercise is <em>started</em> if it is differs from the originally created version.
     * @return true if started, and otherwise false.
     */
    @Override
    public boolean isStarted() { return started;   }

    /**
     * Exercise is <em>started</em> if it differs from the originally created version.
     * @param started true if started, and otherwise false
     */
    @Override
    public void setStarted(boolean started) { this.started = started;   }

    /**
     * The comment on this exercise, which may be by either an instructor or student
     * @return the comment Document.
     */
    @Override
    public Document getExerciseComment() { return commentDocument;   }

    /**
     * The exercise comment on this exercise, which may be by either an instructor or student
     * @param comment the comment Document
     */
    @Override
    public void setExerciseComment(Document comment) { commentDocument = comment;  }

    /**
     * The exercise prompt
     * @return The statement document (T)
     */
    @Override
    public Document getExerciseStatement() { return statementDocument;  }

    /**
     * The pref height of the exercise statement window
     * @return the statement height
     */
    @Override
    public double getStatementPrefHeight() { return statementPrefHeight;   }

    /**
     * The height of the exercise statement window.
     * @param height the statement height
     */
    @Override
    public void setStatementPrefHeight(double height) { statementPrefHeight = height;    }

    /**
     * The original model for the (unworked) exercise
     * @return the original model
     */
    @Override
    public ExerciseModel<Document> getOriginalModel() { return (ExerciseModel) originalModel;  }

    /**
     * The original model for the (unworked exercise)
     * @param exerciseModel the original model
     */
    @Override
    public void setOriginalModel(ExerciseModel<Document> exerciseModel) { originalModel = exerciseModel;   }

    /**
     * Override toString with the exercise name
     * @return the string representation
     */
    @Override
    public String toString() { return exerciseName;  }

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

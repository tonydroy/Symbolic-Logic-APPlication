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

package slapp.editor.derivation_explain;

import com.gluonhq.richtextarea.RichTextAreaSkin;
import com.gluonhq.richtextarea.model.Document;
import slapp.editor.PrintUtilities;
import slapp.editor.derivation.CheckSetup;
import slapp.editor.derivation.ModelLine;
import slapp.editor.main_window.ExerciseModel;
import slapp.editor.main_window.ExerciseType;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Model for the derivation explain exercise.
 */
public class DrvtnExpModel implements ExerciseModel<Document>, Serializable {
    private static final long serialVersionUID = 100L;
    private String exerciseName = new String("");
    private ExerciseType exerciseType  = ExerciseType.DRVTN_EXP;
    private ExerciseModel<Document> originalModel = null;
    private boolean started = false;

    private double splitPanePrefWidth;
    private double gridWidth = 0;
    private String contentPrompt ="";
    private boolean isLeftmostScopeLine = true;
    private boolean defaultShelf = true;
    private RichTextAreaSkin.KeyMapValue keyboardSelector;
    private Document exerciseStatement = new Document();
    private double statementPrefHeight = 80;
    private double statementTextHeight = 0;
    private Document exerciseComment = new Document();
    private double commentPrefHeight = 60;
    private double commentTextHeight = 0;
    private Document explanationDocument = new Document();
    private double explanationPrefHeight = 120;
    private double explanationTextHeight = 0;
    private List<ModelLine> derivationLines = new ArrayList<>();

    private CheckSetup checkSetup;

    /**
     * Construct derivation explain model
     * @param name exercise name (also file name)
     * @param started true if started and otherwise false
     * @param statementPrefHeight preferred height of the statement window
     * @param gridWidth fraction of split paine allocated to the derivation
     * @param contentPrompt prompt for the explain field
     * @param isLeftmostScopeLine true if is leftmost scope line and otherwise false
     * @param defaultShelf true if subderivations come with shelf line
     * @param keyboardSelector the default keyboard for formula fields
     * @param exerciseStatement the statement Document
     * @param exerciseComment the comment Document
     * @param explanationDocument the explanation Document
     * @param derivationLines the list of {@link slapp.editor.derivation.ModelLine}
     */
    public DrvtnExpModel(String name, boolean started, double statementPrefHeight, double gridWidth, String contentPrompt, boolean isLeftmostScopeLine, boolean defaultShelf, RichTextAreaSkin.KeyMapValue keyboardSelector,
                         Document exerciseStatement, Document exerciseComment, Document explanationDocument, List<ModelLine> derivationLines) {
        this.exerciseName = name;
        this.started = started;
        this.statementPrefHeight = statementPrefHeight;
        this.gridWidth = gridWidth;
        this.contentPrompt = contentPrompt;
        this.isLeftmostScopeLine = isLeftmostScopeLine;
        this.defaultShelf = defaultShelf;
        this.keyboardSelector = keyboardSelector;
        this.exerciseStatement = exerciseStatement;
        this.exerciseComment = exerciseComment;
        this.explanationDocument = explanationDocument;
        this.derivationLines = derivationLines;
        this.splitPanePrefWidth = PrintUtilities.getPageWidth();
        this.checkSetup = new CheckSetup();
    }

    /**
     * Scope line from premises to conclusion
     * @return true if is leftmost line and otherwise false
     */
    boolean isLeftmostScopeLine() { return isLeftmostScopeLine; }

    /**
     * The small shelf under the first line of subderivation
     * @return true if subderivations are introduced with shelf line
     */
    boolean isDefaultShelf() { return defaultShelf; }

    /**
     * The fraction of the split pane allocated to the derivation
     * @return the fraction value
     */
    double getGridWidth() {return gridWidth; }

    /**
     * The Document for the explain field
     * @return
     */
    Document getExplanationDocument() { return explanationDocument;  }

    /**
     * List of {@link slapp.editor.derivation.ModelLine}
     * @return the list of derivation lines
     */
    List<ModelLine> getDerivationLines() { return derivationLines; }

    /**
     * The prompt for the explain field
     * @return the prompt string
     */
    String getContentPrompt() { return contentPrompt; }

    /**
     * The default keyboard for derivation lines
     * @return the {@link com.gluonhq.richtextarea.RichTextAreaSkin.KeyMapValue}
     */
    RichTextAreaSkin.KeyMapValue getKeyboardSelector() {return keyboardSelector;}


    /**
     * The preferred height of the comment window
     * @return the height value
     */
    double getCommentPrefHeight() {
        return commentPrefHeight;
    }

    /**
     * The preferred height of the comment window
     * @param commentPrefHeight the height value
     */
    void setCommentPrefHeight(double commentPrefHeight) {    this.commentPrefHeight = commentPrefHeight; }

    /**
     * The preferred height of the explain window
     * @return the height value
     */
    double getExplanationPrefHeight() {    return explanationPrefHeight;  }

    /**
     * The preferred height of the explain window
     * @param explanationPrefHeight the height value
     */
    void setExplanationPrefHeight(double explanationPrefHeight) {  this.explanationPrefHeight = explanationPrefHeight;   }

    /**
     * The preferred width of the split pane
     * @return the width value
     */
    double getSplitPanePrefWidth() {    return splitPanePrefWidth;  }

    /**
     * The preferred width of the split pane
     * @param splitPanePrefWidth the width value
     */
    void setSplitPanePrefWidth(double splitPanePrefWidth) {    this.splitPanePrefWidth = splitPanePrefWidth;  }

    /**
     * The height of text in the statement field
     * @return the height value
     */
    double getStatementTextHeight() {    return statementTextHeight;  }

    /**
     * The height of text in the statement field
     * @param statementTextHeight the height value
     */
    void setStatementTextHeight(double statementTextHeight) {  this.statementTextHeight = statementTextHeight;  }

    /**
     * Height of text in the comment field
     * @return the height value
     */
    double getCommentTextHeight() {    return commentTextHeight;  }

    /**
     * Height of text in the comment field
     * @param commentTextHeight the height value
     */
    void setCommentTextHeight(double commentTextHeight) {    this.commentTextHeight = commentTextHeight;   }

    /**
     * Height of text in the explanation field
     * @return the height value
     */
    double getExplanationTextHeight() {     return explanationTextHeight;  }

    /**
     * Height of text in the explanation field
     * @param explanationTextHeight the height value
     */
    void setExplanationTextHeight(double explanationTextHeight) {   this.explanationTextHeight = explanationTextHeight;   }

    public CheckSetup getCheckSetup() {
        return checkSetup;
    }

    public void setCheckSetup(CheckSetup checkSetup) {
        this.checkSetup = checkSetup;
    }

    /**
     * The exercise name (used also as file name)
     * @return the name string
     */
    @Override
    public String getExerciseName() { return exerciseName; }

    /**
     * The {@link slapp.editor.main_window.ExerciseType}
     * @return the DRVTN_EXP exercise type
     */
    @Override
    public ExerciseType getExerciseType() { return exerciseType; }

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
    public void setStarted(boolean started) { this.started = started; }

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
    public double getStatementPrefHeight() { return statementPrefHeight; }

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
    public ExerciseModel<Document> getOriginalModel() { return originalModel;  }

    /**
     * The original model for the (unworked exercise)
     * @param originalModel the original model
     */
    public void setOriginalModel(ExerciseModel<Document> originalModel) { this.originalModel = originalModel;  }

    /**
     * Override toString with the exercise name
     * @return the string representation
     */
    @Override
    public String toString() { return exerciseName; }

}

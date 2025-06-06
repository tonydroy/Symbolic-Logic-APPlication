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

package slapp.editor.truth_table_generate;

import com.gluonhq.richtextarea.model.Document;
import slapp.editor.main_window.ExerciseModel;
import slapp.editor.main_window.ExerciseType;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Model for the truth table generate exercise
 */
public class TruthTableGenModel implements ExerciseModel<Document>, Serializable {
    private static final long serialVersionUID = 100L;
    private String exerciseName = new String("");
    private String interpretationPrompt = "";
    private ExerciseType exerciseType = ExerciseType.TRUTH_TABLE_ABEXP;
    private ExerciseModel<Document> originalModel = null;
    private boolean started = false;
    private Document exerciseStatement = new Document();
    private double statementPrefHeight = 80;
    private double statementTextHeight = 0;
    private Document exerciseComment = new Document();
    private double commentPrefHeight = 60;
    private double commentTextHeight = 0;
    private Document exerciseInterpretation = new Document();
    private double interpretationPrefHeight = 125;
    private double interpretationTextHeight = 0;
    private Document explainDocument = new Document();
    private double explainPrefHeight = 60;
    private double explainTextHeight = 0;
    private List<String> unaryOperators = new ArrayList<>();
    private List<String> binaryOperators = new ArrayList<>();
    private List<Document> mainFormulas = new ArrayList<>();
    private List<Document> basicFormulas = new ArrayList<>();
    private String[][]  tableValues;   //[w][h]
    private double gridWidth;
    private Document[] rowComments; //[h]
    private boolean[] columnHighlights; //[w]
    private boolean conclusionDivider = false;
    private String choiceLead = new String("");
    private String aPrompt = new String("");
    private boolean aSelected = false;
    private String bPrompt = new String("");
    private boolean bSelected = false;
    private String explainPrompt = "";
    private int tableRows = 0;
    private int pointsPossible;
    private int pointsEarned;


    /**
     * Construct truth table generate model
     */
    public TruthTableGenModel(){
        pointsPossible = 0;
        pointsEarned = -1;
    }

    /**
     * Fill in empty values for table contents
     * @param columns number of table columns
     */
    void setEmptyTableContents(int columns) {
        String[][] mainValues = new String[columns][tableRows];
        for (int i = 0; i < columns; i++) {
            String[] column = new String[tableRows];
            for (int j = 0; j < tableRows; j++) {
                column[j] = "";
            }
            mainValues[i] = column;
        }
        tableValues = mainValues;

        boolean[] highlights = new boolean[columns];
        for (int i = 0; i < columns; i++) {
            highlights[i] = false;
        }
        columnHighlights = highlights;

        Document[] cmts = new Document[tableRows];
        for (int i = 0; i < tableRows; i++) {
            cmts[i] = new Document();
        }
        rowComments = cmts;
    }

    /**
     * The exercise name (doubles as file name)
     * @param exerciseName the string name
     */
    void setExerciseName(String exerciseName) {
        this.exerciseName = exerciseName;
    }

    /**
     * The Document for the exercise prompt field
     * @param exerciseStatement the Document
     */
    void setExerciseStatement(Document exerciseStatement) {
        this.exerciseStatement = exerciseStatement;
    }

    /**
     * The list of unary operators for this sentential language
     * @param unaryOperators the list of Strings
     */
    public void setUnaryOperators(List<String> unaryOperators) {
        this.unaryOperators = unaryOperators;
    }

    /**
     * The list of binary operators for this sentential language
     * @param binaryOperators the list of strings
     */
    public void setBinaryOperators(List<String> binaryOperators) {
        this.binaryOperators = binaryOperators;
    }

    /**
     * The list of formulas to be evaluated in the main part of this table
     * @param mainFormulas the list of formula Documents
     */
    void setMainFormulas(List<Document> mainFormulas) {
        this.mainFormulas = mainFormulas;
    }

    /**
     * The array String[][] of values (usually 'T' or 'F' appearing in the table
     * @param tableValues the array
     */
    void setTableValues(String[][] tableValues) {
        this.tableValues = tableValues;
    }

    /**
     * The array Document[] of row comments
     * @param rowComments the array
     */
    void setRowComments(Document[] rowComments) {
        this.rowComments = rowComments;
    }

    /**
     * The array Boolean[] of columns to be shown with highlight
     * @param columnHighlights true if highlight and otherwise false
     */
    void setColumnHighlights(boolean[] columnHighlights) {
        this.columnHighlights = columnHighlights;
    }

    /**
     * The conclusion divider adds ' / ' before the final formula column
     * @param conclusionDivider true if is divider and otherwise false
     */
    public void setConclusionDivider(boolean conclusionDivider) {
        this.conclusionDivider = conclusionDivider;
    }

    /**
     * The leader to the choice boxes
     * @param choiceLead the string lead
     */
    void setChoiceLead(String choiceLead) {
        this.choiceLead = choiceLead;
    }

    /**
     * Prompt for the A choice
     * @param aPrompt the string prompt
     */
    void setaPrompt(String aPrompt) {
        this.aPrompt = aPrompt;
    }

    /**
     * The boolean value of the A choice
     * @param aSelected the boolean value
     */
    void setaSelected(boolean aSelected) {
        this.aSelected = aSelected;
    }

    /**
     * The prompt for the B choice
     * @param bPrompt the string prompt
     */
    void setbPrompt(String bPrompt) {
        this.bPrompt = bPrompt;
    }

    /**
     * The boolean value of the B choice
     * @param bSelected the boolean value
     */
    void setbSelected(boolean bSelected) {
        this.bSelected = bSelected;
    }

    /**
     * The Document for the explain field
     * @param explainDocument the Document
     */
    void setExplainDocument(Document explainDocument) {
        this.explainDocument = explainDocument;
    }

    /**
     * The prompt for the explain field
     * @return the string prompt
     */
    String getExplainPrompt() {    return explainPrompt;  }

    /**
     * The prompt for the explain field
     * @param explainPrompt the string prompt
     */
    void setExplainPrompt(String explainPrompt) {  this.explainPrompt = explainPrompt;  }

    /**
     * The number of (non-header, footer) rows for this table
     * @param tableRows the rows value
     */
    void setTableRows(int tableRows) { this.tableRows = tableRows;  }

    /**
     * The number of (non-header, footer) rows for this table
     * @return the rows value
     */
    int getTableRows() { return tableRows; }

    /**
     * The array Document[] of comments on the rows
     * @return the array
     */
    Document[] getRowComments() { return rowComments;}

    /**
     * The array Boolean[] of columns to be shown with highlight
     * @return true if heighlight and otherwise false
     */
    boolean[] getColumnHighlights() { return columnHighlights; }

    /**
     * The array String[][] of values (usually 'T' or 'F') appearing in the table
     * @return the array
     */
    String[][] getTableValues() { return tableValues; }

    /**
     * The conclusion divider adds ' / ' before the final formula column
     * @return true if is divider and otherwise false
     */
    boolean isConclusionDivider() { return conclusionDivider; }

    /**
     * The list of formulas to be evaluated in the main part of this table
     * @return the list of formula documents
     */
    List<Document> getMainFormulas() { return mainFormulas; }

    /**
     * The list of unary operators for this sentential language
     * @return the list of Strings
     */
    List<String> getUnaryOperators() { return unaryOperators; }

    /**
     * The list of binary operators for this sentential language
     * @return the list of strings
     */
    List<String> getBinaryOperators() { return binaryOperators; }

    /**
     * The list of basic formula Documents
     * @param basicFormulas the list
     */
    void setBasicFormulas(List<Document> basicFormulas) { this.basicFormulas = basicFormulas;  }

    /**
     * The list of basic formula Documents
     * @return the list
     */
    List<Document> getBasicFormulas() { return basicFormulas; }

    /**
     * The Document for the explain field
     * @return the document
     */
    Document getExplainDocument() { return explainDocument; }

    /**
     * The document for the interpretation field
     * @return the document
     */
    Document getExerciseInterpretation() { return exerciseInterpretation; }

    /**
     * The document for the interpretation field
     * @param exerInterp the document
     */
    void setExerciseInterpretation(Document exerInterp) { this.exerciseInterpretation = exerInterp; }

    /**
     * The lead for the choice boxes
     * @return the string lead
     */
    String getChoiceLead() {return choiceLead; }

    /**
     * The prompt for the A choice
     * @return the string prompt
     */
    String getaPrompt() { return aPrompt; }

    /**
     * The boolean value of the A choice
     * @return the boolean value
     */
    boolean isaSelected() { return aSelected; }

    /**
     * The prompt for the B choice
     * @return the string prompt
     */
    String getbPrompt() { return bPrompt; }

    /**
     * The boolean value of the B choice
     * @return the boolean value
     */
    boolean isbSelected() { return bSelected; }

    /**
     * The prompt for the interpretation field
     * @return the string prompt
     */
    String getInterpretationPrompt() {     return interpretationPrompt;  }

    /**
     * The prompt for the interpretation field
     * @param interpretationPrompt the string prompt
     */
    void setInterpretationPrompt(String interpretationPrompt) {     this.interpretationPrompt = interpretationPrompt;  }

    /**
     * The preferred height of the comment window
     * @return the height value
     */
    double getCommentPrefHeight() {    return commentPrefHeight;  }

    /**
     * The preferred height of the comment window
     * @param commentPrefHeight the height value
     */
    void setCommentPrefHeight(double commentPrefHeight) {  this.commentPrefHeight = commentPrefHeight; }

    /**
     * The preferred height of the interpretation window
     * @return the height value
     */
    double getInterpretationPrefHeight() {  return interpretationPrefHeight;  }

    /**
     * The preferred height of the interpretation window
     * @param interpretationPrefHeight the height value
     */
    void setInterpretationPrefHeight(double interpretationPrefHeight) { this.interpretationPrefHeight = interpretationPrefHeight;   }

    /**
     * The preferred height of the explain window
     * @return the height value
     */
    double getExplainPrefHeight() { return explainPrefHeight; }

    /**
     * The preferred height of the explain window
     * @param explainPrefHeight the height value
     */
    void setExplainPrefHeight(double explainPrefHeight) { this.explainPrefHeight = explainPrefHeight; }

    /**
     * The height of text in the statement (prompt) window
     * @return the height value
     */
    double getStatementTextHeight() {     return statementTextHeight;  }

    /**
     * The height of text in the statement (prompt) window
     * @param statementTextHeight the height value
     */
    void setStatementTextHeight(double statementTextHeight) {     this.statementTextHeight = statementTextHeight;   }

    /**
     * The height of text in the comment window
     * @return the height value
     */
    double getCommentTextHeight() {   return commentTextHeight;  }

    /**
     * The height of text in the comment window
     * @param commentTextHeight the height value
     */
    void setCommentTextHeight(double commentTextHeight) {   this.commentTextHeight = commentTextHeight;  }

    /**
     * The height of text in the interpretation window
     * @return the height value
     */
    double getInterpretationTextHeight() {  return interpretationTextHeight;  }

    /**
     * The height of text in the interpretation window
     * @param interpretationTextHeight the height value
     */
    void setInterpretationTextHeight(double interpretationTextHeight) {  this.interpretationTextHeight = interpretationTextHeight;   }

    /**
     * The height of text in the explain window
     * @return the height value
     */
    double getExplainTextHeight() {     return explainTextHeight;  }

    /**
     * The height of text in the explain window
     * @param explainTextHeight the height value
     */
    void setExplainTextHeight(double explainTextHeight) {    this.explainTextHeight = explainTextHeight;   }

    /**
     * The width of the table grid pane
     * @return the width value
     */
    double getGridWidth() {    return gridWidth; }

    /**
     * The width of the table grid pane
     * @param gridWidth the width value
     */
    void setGridWidth(double gridWidth) {    this.gridWidth = gridWidth; }

    /**
     * The exercise name (used also as file name)
     * @return the name string
     */
    @Override
    public String getExerciseName() {        return exerciseName;   }

    /**
     * The {@link slapp.editor.main_window.ExerciseType}
     * @return the TRUTH_TABLE_GENERATE exercise type
     */
    @Override
    public ExerciseType getExerciseType() { return exerciseType; }

    /**
     * Exercise is <em>started</em> if it is differs from the originally created version.
     * @return true if started, and otherwise false.
     */
    @Override
    public boolean isStarted() {     return started;    }

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
    public Document getExerciseComment() {      return exerciseComment;   }

    /**
     * The exercise prompt
     * @return The statement document (T)
     */
    @Override
    public Document getExerciseStatement() {       return exerciseStatement;   }

    /**
     * The comment on this exercise, which may be by either an instructor or student
     * @param comment the comment Document
     */
    @Override
    public void setExerciseComment(Document comment) { exerciseComment = comment;    }

    /**
     * The pref height of the exercise statement window
     * @return the statement height
     */
    @Override
    public double getStatementPrefHeight() {       return statementPrefHeight;   }

    /**
     * The pref height of the exercise statement window.
     * @param height the statement height
     */
    @Override
    public void setStatementPrefHeight(double height) { statementPrefHeight = height;  }

    /**
     * The original model for the (unworked) exercise
     * @return the original model
     */
    @Override
    public ExerciseModel<Document> getOriginalModel() {  return originalModel;  }

    /**
     * The original model for the (unworked exercise)
     * @param originalModel the original model
     */
    public void setOriginalModel(ExerciseModel<Document> originalModel) {  this.originalModel = originalModel;  }

    /**
     * Override toString with the exercise name
     * @return the string representation
     */
    @Override
    public String toString() {      return exerciseName;    }

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

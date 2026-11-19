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

package slapp.editor.trans_tree;

import com.gluonhq.richtextarea.model.Document;
import slapp.editor.main_window.ExerciseModel;
import slapp.editor.main_window.ExerciseType;
import slapp.editor.vertical_tree.object_models.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class TransTreeModel implements ExerciseModel<Document>, Serializable {
    private static final long serialVersionUID = 100L;
    private String exerciseName = new String("");
    private ExerciseType exerciseType = ExerciseType.TRANS_TREES;
    private ExerciseModel<Document> originalModel = null;
    private boolean started = false;
    private Document exerciseStatement = new Document();
    private double statementPrefHeight = 80;
    private double statementTextHeight = 0;
    private Document exerciseComment = new Document();
    private double commentPrefHeight = 60;
    private double commentTextHeight = 0;
    private Document interpFnDoc = new Document();
    private double interpFnPrefHeight = 70;
    private double interpFnTextHeight = 0;
    private double mainPanePrefHeight = 150;  //300
    private double mainPanePrefWidth;
    private List<TreeBoxMod> formulaTreeBoxes = new ArrayList<>();
    private List<TreeBoxMod> parseTreeBoxes = new ArrayList<>();
    private int pointsPossible;
    private int pointsEarned;
    private TTreeCheckSetup checkSetup;
    private String auxExerName;
    private List<Document> mainFormulas = new ArrayList<>();
    private boolean interpFnIncluded;
    private List<ClickableNodeLinkMod> clickableNodeLinks = new ArrayList<>();


    public TransTreeModel(){
        this.pointsPossible = 0;
        this.pointsEarned = -1;
        this.checkSetup = new TTreeCheckSetup();
    }

    public TTreeCheckSetup getCheckSetup() {
        return checkSetup;
    }

    public void setCheckSetup(TTreeCheckSetup checkSetup) {
        this.checkSetup = checkSetup;
    }

    void setExerciseName(String exerciseName) { this.exerciseName = exerciseName;    }

    void setExerciseStatement(Document exerciseStatement) { this.exerciseStatement = exerciseStatement;    }

    void setInterpFnDoc(Document interpFnDoc) {this.interpFnDoc = interpFnDoc; }

    Document getInterpFnDoc() {  return interpFnDoc;  }

    double getCommentPrefHeight() {    return commentPrefHeight;  }

    void setCommentPrefHeight(double commentPrefHeight) {    this.commentPrefHeight = commentPrefHeight;   }

    double getInterpFnPrefHeight() {    return interpFnPrefHeight;   }

    void setInterpFnPrefHeight(double interpFnPrefHeight) {    this.interpFnPrefHeight = interpFnPrefHeight;   }

    double getMainPanePrefHeight() {     return mainPanePrefHeight;  }

    void setMainPanePrefHeight(double mainPanePrefHeight) {     this.mainPanePrefHeight = mainPanePrefHeight;  }

    double getMainPanePrefWidth() {     return mainPanePrefWidth;   }

    void setMainPanePrefWidth(double mainPanePrefWidth) {      this.mainPanePrefWidth = mainPanePrefWidth;  }

    double getStatementTextHeight() {   return statementTextHeight;  }

    void setStatementTextHeight(double statementTextHeight) {   this.statementTextHeight = statementTextHeight;  }

    double getCommentTextHeight() {   return commentTextHeight;  }

    void setCommentTextHeight(double commentTextHeight) {  this.commentTextHeight = commentTextHeight;  }

    double getInterpFnTextHeight() {   return interpFnTextHeight;  }

    void setInterpFnTextHeight(double interpFnTextHeight) {  this.interpFnTextHeight = interpFnTextHeight; }

    public String getAuxExerName() { return auxExerName;  }

    public void setAuxExerName(String auxExerName) { this.auxExerName = auxExerName;  }

    public List<TreeBoxMod> getFormulaTreeBoxes() {
        return formulaTreeBoxes;
    }

    public void setFormulaTreeBoxes(List<TreeBoxMod> formulaTreeBoxes) {
        this.formulaTreeBoxes = formulaTreeBoxes;
    }

    public List<TreeBoxMod> getParseTreeBoxes() {
        return parseTreeBoxes;
    }

    public void setParseTreeBoxes(List<TreeBoxMod> parseTreeBoxes) {
        this.parseTreeBoxes = parseTreeBoxes;
    }

    public List<ClickableNodeLinkMod> getClickableNodeLinks() {
        return clickableNodeLinks;
    }

    public void setClickableNodeLinks(List<ClickableNodeLinkMod> clickableNodeLinks) {
        this.clickableNodeLinks = clickableNodeLinks;
    }

    public List<Document> getMainFormulas() {
        return mainFormulas;
    }

    public void setMainFormulas(List<Document> mainFormulas) {
        this.mainFormulas = mainFormulas;
    }

    public boolean isInterpFnIncluded() {
        return interpFnIncluded;
    }

    public void setInterpFnIncluded(boolean interpFnIncluded) {
        this.interpFnIncluded = interpFnIncluded;
    }

    @Override
    public String getExerciseName() { return exerciseName;    }
    @Override
    public ExerciseType getExerciseType() { return exerciseType; }
    @Override
    public boolean isStarted() {  return started;    }
    @Override
    public void setStarted(boolean started) {this.started = started; }
    @Override
    public Document getExerciseComment() { return exerciseComment;    }
    @Override
    public Document getExerciseStatement() {  return exerciseStatement;    }
    @Override
    public void setExerciseComment(Document document) { this.exerciseComment = document;   }
    @Override
    public double getStatementPrefHeight() {    return statementPrefHeight;  }
    @Override
    public void setStatementPrefHeight(double height) { this.statementPrefHeight = height; }
    @Override
    public ExerciseModel<Document> getOriginalModel() {  return originalModel;  }
    public void setOriginalModel(ExerciseModel<Document> originalModel) {  this.originalModel = originalModel; }
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

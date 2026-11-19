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

package slapp.editor.trans_uline;

import com.gluonhq.richtextarea.model.Document;
import slapp.editor.main_window.ExerciseModel;
import slapp.editor.main_window.ExerciseType;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class TransULModel implements ExerciseModel<Document>, Serializable {
    private static final long serialVersionUID = 100L;

    private String exerciseName = new String("");
    private ExerciseType exerciseType = ExerciseType.TRANS_ULINE;
    private ExerciseModel<Document> originalModel = null;
    private boolean started = false;
    private Document exerciseStatement = new Document();
    private double statementPrefHeight = 80;
    private double statementTextHeight = 0;
    private Document exerciseComment = new Document();
    private double commentPrefHeight = 60;
    private double commentTextHeight = 0;
    private boolean aSelected = false;
    private boolean bSelected = false;
    private int pointsPossible;
    private int pointsEarned;
    private ULcheckSetup checkSetup;
    private String auxExerName;
    private List<ULTreeObj> mainModel = new ArrayList<>();
    private List<ULTreeObj> keyModel = new ArrayList<>();
    private double ulBoxPrintWidth;

    private String mainOperator = "";


    public TransULModel(){
        this.pointsPossible = 0;
        this.pointsEarned = -1;
        this.checkSetup = new ULcheckSetup();
        this.auxExerName = "";
    }

    public String getAuxExerName() {
        return auxExerName;
    }

    public void setAuxExerName(String auxExerName) {
        this.auxExerName = auxExerName;
    }

    public ULcheckSetup getCheckSetup() {
        return checkSetup;
    }

    public void setCheckSetup(ULcheckSetup checkSetup) {
        this.checkSetup = checkSetup;
    }

    public double getCommentPrefHeight() {
        return commentPrefHeight;
    }

    public void setCommentPrefHeight(double commentPrefHeight) {
        this.commentPrefHeight = commentPrefHeight;
    }

    public double getCommentTextHeight() {
        return commentTextHeight;
    }

    public void setCommentTextHeight(double commentTextHeight) {
        this.commentTextHeight = commentTextHeight;
    }

    public void setExerciseName(String exerciseName) {
        this.exerciseName = exerciseName;
    }

    public void setExerciseStatement(Document exerciseStatement) {
        this.exerciseStatement = exerciseStatement;
    }

    public void setExerciseType(ExerciseType exerciseType) {
        this.exerciseType = exerciseType;
    }

    public double getStatementTextHeight() {
        return statementTextHeight;
    }

    public List<ULTreeObj> getMainModel() {
        return mainModel;
    }

    public void setMainModel(List<ULTreeObj> mainModel) {
        this.mainModel = mainModel;
    }

    public void setStatementTextHeight(double statementTextHeight) {
        this.statementTextHeight = statementTextHeight;
    }

    public boolean isaSelected() {
        return aSelected;
    }

    public void setaSelected(boolean aSelected) {
        this.aSelected = aSelected;
    }

    public boolean isbSelected() {
        return bSelected;
    }

    public void setbSelected(boolean bSelected) {
        this.bSelected = bSelected;
    }

    public String getMainOperator() {
        return mainOperator;
    }

    public void setMainOperator(String mainOperator) {
        this.mainOperator = mainOperator;
    }

    public double getUlBoxPrintWidth() {
        return ulBoxPrintWidth;
    }

    public void setUlBoxPrintWidth(double ulBoxWidth) {
        this.ulBoxPrintWidth = ulBoxWidth;
    }

    public List<ULTreeObj> getKeyModel() {
        return keyModel;
    }

    public void setKeyModel(List<ULTreeObj> keyModel) {
        this.keyModel = keyModel;
    }

    @Override
    public Document getExerciseComment() {
        return exerciseComment;
    }

    @Override
    public String getExerciseName() {
        return exerciseName;
    }

    @Override
    public Document getExerciseStatement() {
        return exerciseStatement;
    }

    @Override
    public ExerciseType getExerciseType() {
        return exerciseType;
    }

    @Override
    public ExerciseModel<Document> getOriginalModel() {
        return originalModel;
    }

    @Override
    public int getPointsEarned() {
        if (pointsEarned >= 0) return pointsEarned;
        else return 0;
    }

    @Override
    public int getPointsPossible() {
        return pointsPossible;
    }

    @Override
    public double getStatementPrefHeight() {
        return statementPrefHeight;
    }

    @Override
    public boolean isStarted() {
        return started;
    }

    @Override
    public void setExerciseComment(Document comment) {
        exerciseComment = comment;
    }

    @Override
    public void setOriginalModel(ExerciseModel<Document> exerciseModel) {
        originalModel = exerciseModel;
    }

    @Override
    public void setPointsEarned(int pointsEarned) {
        this.pointsEarned = pointsEarned;
    }

    @Override
    public void setPointsPossible(int pointsPossible) {
        this.pointsPossible = pointsPossible;
    }

    @Override
    public void setStarted(boolean started) {
        this.started = started;
    }

    @Override
    public void setStatementPrefHeight(double height) {
        statementPrefHeight = height;
    }

    @Override
    public String toString() { return exerciseName; }

}

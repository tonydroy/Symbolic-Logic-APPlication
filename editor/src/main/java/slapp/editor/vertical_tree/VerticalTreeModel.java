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

package slapp.editor.vertical_tree;

import com.gluonhq.richtextarea.RichTextAreaSkin;
import com.gluonhq.richtextarea.model.Document;
import slapp.editor.PrintUtilities;
import slapp.editor.main_window.ExerciseModel;
import slapp.editor.main_window.ExerciseType;
import slapp.editor.vertical_tree.drag_drop.DragIconType;
import slapp.editor.vertical_tree.object_models.*;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class VerticalTreeModel implements ExerciseModel<Document>, Serializable {
    private static final long serialVersionUID = 100L;
    private String exerciseName = new String("");
    private ExerciseType exerciseType = ExerciseType.VERTICAL_TREE;
    private RichTextAreaSkin.KeyMapValue defaultKeyboardType;
    private ExerciseModel<Document> originalModel = null;
    private boolean started = false;
    private Document exerciseStatement = new Document();
    private double statementPrefHeight = 80;
    private double statementTextHeight = 0;
    private Document exerciseComment = new Document();
    private double commentPrefHeight = 60;
    private double commentTextHeight = 0;
    private double mainPanePrefHeight = 150;  //350

    //this is not a preferred but a true width -- serialization keeps name same after switch from spinner to automatic sizing
    private double mainPanePrefWidth;



    private List<DragIconType> dragIconList = new ArrayList<>();
    private List<ObjectControlType> objectControlList = new ArrayList<>();
    private List<TreeFormulaBoxMod> treeFormulaBoxes = new ArrayList<>();
    private List<MapFormulaBoxMod> mapFormulaBoxes = new ArrayList<>();
    private List<VerticalBracketMod> verticalBrackets = new ArrayList<>();
    private List<DashedLineMod> dashedLineMods = new ArrayList<>();
    private List<ClickableNodeLinkMod> clickableNodeLinks = new ArrayList<>();
    private List<ClickableMapLinkMod> clickableMapLinks = new ArrayList<>();
    private List <MapQuestionMarkerMod> mapQuestionMarkers = new ArrayList<>();


    public VerticalTreeModel(){}


    void setExerciseName(String exerciseName) { this.exerciseName = exerciseName;    }

    void setExerciseStatement(Document exerciseStatement) { this.exerciseStatement = exerciseStatement;    }

    public List<DragIconType> getDragIconList() { return dragIconList;    }

    void setDragIconList(List<DragIconType> dragIconList) { this.dragIconList = dragIconList;    }

    public List<ObjectControlType> getObjectControlList() {  return objectControlList;    }

    void setObjectControlList(List<ObjectControlType> objectControlList) { this.objectControlList = objectControlList;    }

    List<TreeFormulaBoxMod> getTreeFormulaBoxes() {  return treeFormulaBoxes;   }

    List<MapFormulaBoxMod> getMapFormulaBoxes() {  return mapFormulaBoxes;    }

    List<VerticalBracketMod> getVerticalBrackets() {  return verticalBrackets;    }

    List<DashedLineMod> getDashedLineMods() {  return dashedLineMods;   }

    List<ClickableNodeLinkMod> getClickableNodeLinks() {  return clickableNodeLinks;    }

    List<ClickableMapLinkMod> getClickableMapLinks() {  return clickableMapLinks;   }

    List<MapQuestionMarkerMod> getMapQuestionMarkers() {  return mapQuestionMarkers;    }

    public RichTextAreaSkin.KeyMapValue getDefaultKeyboardType() {    return defaultKeyboardType;  }

    public void setDefaultKeyboardType(RichTextAreaSkin.KeyMapValue defaultKeyboardType) {   this.defaultKeyboardType = defaultKeyboardType;  }

    double getCommentPrefHeight() {     return commentPrefHeight;  }

    void setCommentPrefHeight(double commentPrefHeight) {      this.commentPrefHeight = commentPrefHeight;  }

    double getMainPanePrefHeight() { return mainPanePrefHeight;  }

    void setMainPanePrefHeight(double mainPanePrefHeight) {  this.mainPanePrefHeight = mainPanePrefHeight;  }

    double getMainPanePrefWidth() {     return mainPanePrefWidth;  }

    void setMainPanePrefWidth(double mainPanePrefWidth) {    this.mainPanePrefWidth = mainPanePrefWidth; }

    double getStatementTextHeight() {  return statementTextHeight;  }

    void setStatementTextHeight(double statementTextHeight) {    this.statementTextHeight = statementTextHeight;  }

    double getCommentTextHeight() {    return commentTextHeight; }

    void setCommentTextHeight(double commentTextHeight) {    this.commentTextHeight = commentTextHeight;  }

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

}

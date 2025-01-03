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

package slapp.editor.front_page;

import com.gluonhq.richtextarea.model.Document;
import slapp.editor.main_window.ExerciseModel;
import slapp.editor.main_window.ExerciseType;

/**
 * Dummy model to support FrontPageExercise
 */
public class FrontPageModel implements ExerciseModel<Document> {

    @Override
    public String getExerciseName() { return ""; }
    @Override
    public ExerciseType getExerciseType() { return null; }
    @Override
    public boolean isStarted() { return false; }
    @Override
    public void setStarted(boolean started) { }
    @Override
    public Document getExerciseComment() { return null; }
    @Override
    public void setExerciseComment(Document document) {}
    @Override
    public Document getExerciseStatement() { return null; }
    @Override
    public double getStatementPrefHeight() { return 0; }
    @Override
    public void setStatementPrefHeight(double height) { }

    @Override
    public ExerciseModel<Document> getOriginalModel() { return null;  }

    @Override
    public void setOriginalModel(ExerciseModel<Document> exerciseModel) {  }

    @Override
    public String toString() { return ""; }

}

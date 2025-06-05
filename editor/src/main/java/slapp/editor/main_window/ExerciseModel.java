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

package slapp.editor.main_window;

import com.gluonhq.richtextarea.model.Document;
import java.io.Serializable;

/**
 * Interface for exercise model
 *
 * @param <T> The Class for the exercise statement
 */
//TODO Rip this generic?
    //Initially, I thought statements would include derivations or the like.
    //In fact, however, they are all Document (with any special items pushed into the work area)
public interface ExerciseModel<T> extends Serializable {

    /**
     * The exercise name (used also as file name)
     * @return the name string
     */
    String getExerciseName();

    /**
     * Every exercise has an {@link slapp.editor.main_window.ExerciseType}
     * @return the exercise type
     */
    ExerciseType getExerciseType();

    /**
     * Exercise is <em>started</em> if it is differs from the originally created version.
     * @return true if started, and otherwise false.
     */
    boolean isStarted();

    /**
     * Exercise is <em>started</em> if it differs from the originally created version.
     * @param started true if started, and otherwise false
     */
    void setStarted(boolean started);

    /**
     * The comment on this exercise, which may be by either an instructor or student
     * @return the comment Document.
     */
    Document getExerciseComment();

    /**
     * The exercise prompt
     * @return The statement (T)
     */
    T getExerciseStatement();

    /**
     * The exercise comment on this exercise, which may be by either an instructor or student
     * @param comment the comment Document
     */
    void setExerciseComment(Document comment);

    /**
     * The pref height of the exercise statement window
     * @return the statement height
     */
    double getStatementPrefHeight();

    /**
     * The height of the exercise statement window.
     * @param height the statement height
     */
    void setStatementPrefHeight(double height);

    /**
     * Override toString (usually with the exercise name)
     * @return the string representation
     */
    String toString();

    /**
     * The original model for the (unworked) exercise
     * @return the original model
     */
    ExerciseModel<T> getOriginalModel();

    /**
     * The original model for the (unworked exercise)
     * @param exerciseModel the original model
     */
    void setOriginalModel(ExerciseModel<T> exerciseModel);

    int getPointsPossible();

    void setPointsPossible(int pointsPossible);

    int getPointsEarned();

    void setPointsEarned(int pointsEarned);

}



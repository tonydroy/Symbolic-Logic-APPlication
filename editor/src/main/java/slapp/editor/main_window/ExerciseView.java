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

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.scene.Node;
import slapp.editor.decorated_rta.DecoratedRTA;

/**
 * Interface for the exercise view
 *
 * @param <T> the class for the statement view
 */
//TODO Rip this generic?
    //Initially, I thought statements would include derivations or the like.
    //In fact, however, they are all DecoratedRTA (with any special items pushed into the work area)
public interface ExerciseView<T> {

    /**
     * The comment decoratedRTA
     * @return comment DecoratedRTA
     */
    DecoratedRTA getExerciseComment();

    /**
     * The comment decoratedRTA
     * @param exerciseComment DecoratedRTA
     */
    void setExerciseComment(DecoratedRTA exerciseComment);

     /**
     * The exercise prompt (usually DecoratedRTA)
     * @return the statement (T)
     */
    T getExerciseStatement();

    /**
     * The exercise prompt (usually DecoratedRTA)
     * @param exerciseStatement the statement (T)
     */
    void setExerciseStatement(T exerciseStatement);

    /**
     * The exercise prompt node (usually RTA)
     * @return the statement node
     */
    Node getExerciseStatementNode();

     /**
     * The preferred height of the statement window
     * @param height the preferred height
     */
    void setStatementPrefHeight(double height);

    /**
     * The node which includes content of this exercise
     * @return the content node
     */
    Node getExerciseContentNode();

    /**
     * The left control node for this exercise
     * @return the control node
     */
    Node getExerciseControl();

    /**
     * The right control node for this exercise
     * @return the control node
     */
    Node getRightControl();

    default void clearStandingPopups() {};



}

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

import javafx.scene.Node;
import javafx.scene.control.Spinner;

import java.util.List;

/**
 * Interface for exercise controller
 *
 * @param <T> The model Class
 * @param <U> The view class
 */
public interface Exercise<T,U> {

    /**
     * The exercise model is different for different exercise types
     * @return the model
     */
    T getExerciseModel();

    /**
     * The exercise view is different for different exercise types
     * @return the view
     */
    U getExerciseView();

    /**
     * Save exercise to disk
     * @param saveAs true if "save as" should be invoked, and otherwise false
     */
    void saveExercise(boolean saveAs);

    /**
     * List of nodes to be sent to printer for this exercise
     * @return the node list
     */
    List<Node> getPrintNodes(ExerciseModel originalModel);

    /**
     * Return to the initial (unworked) version of the exercise, retaining the comment only.
     * @return the initial exercise
     */
    Exercise<T,U> resetExercise();


     /**
     * Exercise is modified if it is changed relative to last save
     * @return true if exercise is modified, and otherwise false
     */
    boolean isExerciseModified();

    /**
     * Exercise is modified if it is changed relative to last save
     * @param modified true if exercise is modified, and otherwise false
     */
    void setExerciseModified(boolean modified);

    /**
     * Extract an {@link slapp.editor.main_window.ExerciseModel} from view of the exercise
     * @return the exercise model
     */
    ExerciseModel<T> getExerciseModelFromView();

    /**
     * The view node to be displayed as a component of the free form exercise.  This will be a (possibly modified)
     * portion of the regular exercise view.
     * @return the view node
     */
    Node getFFViewNode();

    /**
     * The node to be printed as a component of the free form exercise.  This will be a (possibly modified)
     * portion of the regular print nodes.
     * @return the print node
     */
    Node getFFPrintNode(ExerciseModel originalModel);

    Spinner getFFHeightSpinner();
    Spinner getFFWidthSpinner();

}

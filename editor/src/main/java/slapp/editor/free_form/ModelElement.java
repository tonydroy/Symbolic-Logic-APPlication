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

package slapp.editor.free_form;

import slapp.editor.main_window.Exercise;
import slapp.editor.main_window.ExerciseModel;

import java.io.Serializable;

/**
 * Exercise model for a single element of the free form exercise
 */
public class ModelElement implements Serializable {
    private static final long serialVersionUID = 100L;
    private ExerciseModel model = null;
    private int indentLevel = 0;

    /**
     * Construct the model element
     * @param model The exercise model for this element
     * @param indentLevel the indent level of this element
     */
    ModelElement(ExerciseModel model, int indentLevel) {
        this.model = model;
        this.indentLevel = indentLevel;
    }

    /**
     * The exercise model for this element
     * @return the exercise model
     */
    ExerciseModel getModel() {      return model;  }

    /**
     * The exercise model for this element
     * @param model the model
     */
    void setModel(ExerciseModel model) {     this.model = model;  }

    /**
     * The indent level for this element
     * @return the indent level
     */
    int getIndentLevel() {     return indentLevel;  }

    /**
     * The indent level for this element
     * @param indent the indent level
     */
    void setIndent(int indent) {     this.indentLevel = indent;  }

}

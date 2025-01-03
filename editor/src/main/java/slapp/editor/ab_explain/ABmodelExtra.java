/*
Copyright (c) 2024 Tony Roy

This file is part of the Symbolic Logic APPlication (SLAPP).

SLAPP is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.

SLAPP is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License along with SLAPP.
If not, see <https://www.gnu.org/licenses/>.
 */

package slapp.editor.ab_explain;

import java.io.Serializable;

/**
 * Supplement to AB model to support the choice fields
 */
class ABmodelExtra implements Serializable {
    private static final long serialVersionUID = 100L;
    private String leader = "";
    private String promptA = "";
    private boolean valueA = false;
    private String promptB = "";
    private boolean valueB = false;


    /**
     * Create ABmodelExtra with empty defaults
     */
    ABmodelExtra() {}

    /**
     * CreateABmodelExtra with parameters
     * @param leader the choice field leader
     * @param promptA prompt for the A-choice     *
     * @param valueA value of the A-choice
     * @param promptB prompt for the B-choice
     * @param valueB value of the B-choice
     */
    ABmodelExtra(String leader, String promptA, boolean valueA, String promptB, boolean valueB) {
        this();
        this.leader = leader;
        this.promptA = promptA;
        this.valueA = valueA;
        this.promptB = promptB;
        this.valueB = valueB;
    }

    /**
     * The leader appears before the choice fields
     * @return the String leader
     */
    String getLeader() {
        return leader;
    }

    /**
     * The A-prompt appears with the A-field
     * @return the String prompt
     */
    String getPromptA() {
        return promptA;
    }

    /**
     * The value of the A check box
     * @return true if selected and otherwise false
     */
    boolean getValueA() {
        return valueA;
    }

    /**
     * The B-prompt appears with the B-field
     * @return the string prompt
     */
    String getPromptB() {
        return promptB;
    }

    /**
     * The value of the B check box.
     * @return true if selected and otherwise false
     */
    boolean getValueB() {
        return valueB;
    }

}

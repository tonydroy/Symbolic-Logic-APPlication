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

/**
 * Enum of SLAPP exercise types.  Each type has a label and a longer description.
 */
public enum ExerciseType {

    /**
     * Exercise with simple edit area box (mainly for use in the 'free-form' exercise)
     */
    SIMPLE_EDIT("Simple Edit", "Exercise with simple edit area box (mainly for use in the 'free-form' exercise)."),

    /**
     * Exercise requires a text response -- anywhere from short answer, to multiple pages.
     */
    PAGE_EDIT("Page Edit", "Exercise requires a text response -- anywhere from short answer, to multiple pages."),

    /**
     * Exercise requires binary choice with an explanation.
     */
    AB_EXPLAIN("AB explain", "Exercise requires binary choice with an explanation."),

    /**
     * Exercise requires both binary and tertiary choice with an explanation.
     */
    ABEFG_EXPLAIN("AB/EFG explain", "Exercise requires both binary and tertiary choice with an explanation."),

    /**
     * Exercise with fields for interpretation function and formal translation.
     */
    SIMPLE_TRANS("Simple Translation", "Exercise with fields for interpretation function and formal translation."),

    /**
     * Exercise requires a derivation.
     */
    DERIVATION("Derivation", "Exercise requires a derivation."),

    /**
     * Exercise requires a derivation and explanation.
     */
    DRVTN_EXP("Derivation explain", "Exercise requires a derivation and explanation."),

    /**
     * Exercise requires a horizontal tree (of the type encountered in chapters 4 and 5 of SL) and an explanation.
     */
    HORIZONTAL_TREE("Horizontal Tree Explain", "Exercise requires a horizontal tree (of the type encountered in chapters 4 and 5 of SL) and an explanation."),

    /**
     * Exercise requires a truth table.
     */
    TRUTH_TABLE("Truth table", "Exercise requires a truth table."),

    /**
     * Exercise requires a truth table with a binary choice and explanation.
     */
    TRUTH_TABLE_ABEXP("Truth table AB explain", "Exercise requires a truth table with a binary choice and explanation."),

    /**
     * Exercise requires an interpretation, creation of truth table, with a binary choice and explanation.
     */
    TRUTH_TABLE_GENERATE("Truth table gen explain", "Exercise requires an interpretation, creation of truth table, with a binary choice and explanation."),

    /**
     * Exercise requires a vertical tree (or map) of types as encountered in chapters 2, 4, and 5 of SL.
     */
    VERTICAL_TREE("Vertical Tree", "Exercise requires a vertical tree (or map) of types as encountered in chapters 2, 4, and 5 of SL."),

    /**
     * Exercise requires a vertical tree (or map) with an explanation.
     */
    VERTICAL_TREE_EXP ("Vertical Tree Explain", "Exercise requires a vertical tree (or map) with an explanation."),

    /**
     * Exercise requires a vertical tree (or map) with a binary choice and explanation.
     */
    VERTICAL_TREE_ABEXP ("Vertical Tree AB Explain", "Exercise requires a vertical tree (or map) with a binary choice and explanation."),

    /**
     * Exercise requires a vertical tree (or map) with a pair of binary choices and explanation.
     */
    VERTICAL_TREE_ABEFEXP ("Vertical Tree AB/EF Explain", "Exercise requires a vertical tree (or map) with a pair of binary choices and explanation."),

    /**
     * Exercise permits the insertion of elements from multiple exercise types.
     */
    FREE_FORM ("Free Form", "Exercise permits the insertion of elements from multiple exercise types.");


    public final String label;
    public final String description;

    /**
     * Create the type with label and description
     * @param label the label
     * @param description the (longer) description
     */
    private ExerciseType(String label, String description) {

        this.label = label;
        this.description = description;
    }

    /**
     * toString returns the label
     *
     * @return the label String
     */
    public String toString() { return label; }

    /**
     * The longer description
     *
     * @return the description
     */
    public String getDescription() {return description; }

}

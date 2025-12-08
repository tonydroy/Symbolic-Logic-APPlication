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
 * The ControlType sets the active controls for a RichText area
 *
 */
public enum ControlType {

    /**
     * None of the control bar controls active
     */
    NONE("no control"),

    /**
     * No RTA controls active (but keyboard and window controls ok)
     */
    STATEMENT("statement"),

    /**
     * Controls appropriate for RTA produced string (for derivation Justification)
     */
    JUSTIFICATION("justification"),

    /**
     * Controls appropriate to a one-line field active
     */
    FIELD("field"),

    /**
     * All controls active
     */
    AREA("area");


    private final String label;

    /**
     * ControlType constructor with String parameter
     *
     * @param label String label
     */
    private ControlType(String label) {
        this.label = label;
    }

    /**
     * returns the label
     *
     * @return label string
     */
    public String toString() { return label; }

    }

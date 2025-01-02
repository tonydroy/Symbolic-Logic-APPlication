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

package slapp.editor.main_window.assignment;

import java.io.Serializable;

/**
 * Simple lable-value pair for optional student or instructor header items
 */
public class AssignmentHeaderItem implements Serializable {
    private static final long serialVersionUID = 100L;
    private String label = "";
    private String value = "";

    /**
     * Create header item pair from the parameters label and valud
     * @param label the item label
     * @param value the value for the label
     */
    public AssignmentHeaderItem(String label, String value) {
        this.label = label;
        this.value = value;
    }

    /**
     * Header pair label
     * @param label label String
     */
    public void setLabel(String label) { this.label = label; }

    /**
     * Header pair value
     *
     * @param value value String
     */
    public void setValue(String value) { this.value = value; }

    /**
     * Header pair label
     *
     * @return label String
     */
    public String getLabel() {return label; }

    /**
     * Header pair value
     *
     * @return value string
     */
    public String getValue() { return value; }

}

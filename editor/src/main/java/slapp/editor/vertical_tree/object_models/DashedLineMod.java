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

package slapp.editor.vertical_tree.object_models;

import java.io.Serializable;

/**
 * Model for the horizontal dashed line
 */
public class DashedLineMod implements Serializable {
    private static final long serialVersionUID = 100L;
    private double layoutX;
    private double layoutY;
    private double width;

    /**
     * Construct the dashed line model
     * @param layoutX X layout position in the main pane
     * @param layoutY Y layout position in the main pane
     * @param width width (length) of line
     */
    public DashedLineMod(double layoutX, double layoutY, double width) {
        this.layoutX = layoutX;
        this.layoutY = layoutY;
        this.width = width;
    }

    /**
     * X layout in the main pane
     * @return the double layout value
     */
    public double getLayoutX() {  return layoutX;  }

    /**
     * Y layout in the main pane
     * @return the double layout value
     */
    public double getLayoutY() { return layoutY;   }

    /**
     * The width (length) of line
     * @return the double length value
     */
    public double getWidth() { return width;  }
}

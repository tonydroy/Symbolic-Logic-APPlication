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
 * Model for the vertical bracket
 */
public class VerticalBracketMod implements Serializable {
    private static final long serialVersionUID = 100L;
    private double layoutX;
    private double layoutY;
    private double height;

    /**
     * Construct bracket model
     * @param layoutX  bracket layout X
     * @param layoutY bracket layout Y
     * @param height bracket height
     */
    public VerticalBracketMod(double layoutX, double layoutY, double height) {
        this.layoutX = layoutX;
        this.layoutY = layoutY;
        this.height = height;
    }

    /**
     * The layout x value
     * @return the double layout value
     */
    public double getLayoutX() { return layoutX;  }

    /**
     * The layout y value
     * @return the double layout value
     */
    public double getLayoutY() { return layoutY;  }

    /**
     * The height value
     * @return the double height value
     */
    public double getHeight() { return height; }
}

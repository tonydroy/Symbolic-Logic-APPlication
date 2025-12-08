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

package slapp.editor.vertical_tree.drag_drop;

import javafx.geometry.Point2D;

import java.io.Serializable;

/**
 * Serializable version of {@link javafx.geometry.Point2D}
 */
public class Point2dSerial extends Point2D implements Serializable {
    private static final long serialVersionUID = 100L;

    public Point2dSerial(double x, double y) {
        super(x, y);
        // TODO Auto-generated constructor stub
    }
}

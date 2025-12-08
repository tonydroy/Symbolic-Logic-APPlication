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

package slapp.editor;

import javafx.scene.Node;

/**
 * Adjunct to print utilities, for members of the PrintBuffer list
 */
public class PrintBufferItem {
    Node node;
    double height;
    double width;
    double scale = 1.0;

    /**
     * Construct buffer item with node, height width
     * @param node
     * @param height
     * @param width
     */
    PrintBufferItem(Node node, double height, double width) {
        this.node = node;
        this.height = height;
        this.width = width;
    }

    /**
     * The node to print
     * @return the node
     */
    public Node getNode() {
        return node;
    }

    /**
     * The height of the print node
     *
     * @return the height
     */
    public double getHeight() {
        return height;
    }

    /**
     * The width of the print node
     *
     * @return the width
     */
    public double getWidth() {
        return width;
    }

    /**
     * The scale at which to print the node
     * @return the scale
     */
    public double getScale() {
        return scale;
    }

    /**
     * The scale at which to print the node
     * @param scale the scale
     */
    public void setScale(double scale) {
        this.scale = scale;
    }
}

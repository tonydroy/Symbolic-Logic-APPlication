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

import javafx.scene.Node;

/**
 * Single member of the free-form mvew
 */
public class ViewElement {

    private Node node;
    private int indentLevel = 0;


    /**
     * Construct view element
     * @param node the view node
     * @param indentLevel indent level of the node
     */
    public ViewElement(Node node, int indentLevel) {
        this.node = node;
        this.indentLevel = indentLevel;
    }

    /**
     * The node of this element
     * @return the node
     */
    public Node getNode() {     return node;  }

    /**
     * The node of this element
     * @param node tghe node
     */
    public void setNode(Node node) {     this.node = node;   }

    /**
     * The indent level for this node
     * @return the indent level
     */
    public int getIndentLevel() {     return indentLevel;   }

    /**
     * The indent level for this node
     * @param indentLevel the indent level
     */
    public void setIndentLevel(int indentLevel) {     this.indentLevel = indentLevel;   }

    // SCORCHED EARTH RULE 6: Sever Shared Nodes helper
    private void severFromParent(Node node) {
        try {
            if (node != null && node.getParent() != null) {
                if (node.getParent() instanceof javafx.scene.layout.Pane) ((javafx.scene.layout.Pane) node.getParent()).getChildren().remove(node);
                else if (node.getParent() instanceof javafx.scene.Group) ((javafx.scene.Group) node.getParent()).getChildren().remove(node);
            }
        } catch (Exception e) {}
    }

    public void cleanup() {
        // SCORCHED EARTH RULE 5 & 6: Break UI ties and sever node
        try {
            if (node != null) {
                severFromParent(node);
            }
        } catch (Exception e) {}

        // SCORCHED EARTH RULE 1: Final nullification
        try { this.node = null; } catch (Exception e) {}
    }

}
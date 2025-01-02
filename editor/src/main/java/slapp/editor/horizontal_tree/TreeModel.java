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

package slapp.editor.horizontal_tree;

import java.io.Serializable;

/**
 * Model for pain containing a single tree
 */
public class TreeModel implements Serializable {
    private static final long serialVersionUID = 100L;
    private double paneXlayout;
    private double paneYlayout;
    private double rootXlayout;
    private double rootYlayout;
    private BranchModel root;


    /**
     * The X layout of the pane
     * @param paneXlayout the X value
     */
    void setPaneXlayout(double paneXlayout) {    this.paneXlayout = paneXlayout;   }

    /**
     * The X layout of the pane
     * @return the X value
     */
    double getPaneXlayout() {  return paneXlayout;  }

    /**
     * The Y layout of the pane
     * @param paneYlayout the Y value
     */
    void setPaneYlayout(double paneYlayout) {   this.paneYlayout = paneYlayout;    }

    /**
     * The Y layout of the pane
     * @return the Y value
     */
    double getPaneYlayout() { return paneYlayout;  }

    /**
     * Branch model for the root node (which has as descendants the rest of the tree)
     * @param root the BranchModel
     */
    void setRoot(BranchModel root) {     this.root = root;  }

    /**
     * Branch model for the root node (which has as descendants the rest of the tree)
     * @return the Branch Model
     */
    BranchModel getRoot() {   return root;  }

    /**
     * The X-layout of the root node
     * @return the layout value
     */
    double getRootXlayout() {     return rootXlayout;   }

    /**
     * The X layout of the root node
     * @param rootXlayout the layout value
     */
    void setRootXlayout(double rootXlayout) {     this.rootXlayout = rootXlayout;   }

    /**
     * The Y layout of the root node
     * @return the layout value
     */
    double getRootYlayout() {     return rootYlayout;   }

    /**
     * The Y layout of the root node
     * @param rootYlayout the layout value
     */
    void setRootYlayout(double rootYlayout) {     this.rootYlayout = rootYlayout;   }

}

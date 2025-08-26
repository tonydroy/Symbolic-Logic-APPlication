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

import com.gluonhq.richtextarea.model.Document;
import slapp.editor.map_abexplain.MapIndexItem;

import java.io.Serializable;
import java.util.List;

/**
 * Model for the map formula box
 */
public class MapFormulaBoxMod implements Serializable {
    private static final long serialVersionUID = 100L;
    private String idString;
    private double layoutX;
    private double layoutY;
    private Document text;
    private List<String> linkIdStrings;
    double printWidth;
    List<MapIndexItem> mapIndexItems;

    public MapFormulaBoxMod() {
        printWidth = 0;
    }

    /**
     * id string for this node
     * @return the id string
     */
    public String getIdString() {       return idString;   }

    /**
     * id string for this node
     * @param idString the id string
     */
    public void setIdString(String idString) {       this.idString = idString;   }

    /**
     * The x layout of the map formula box in the main pane
     * @return the x layout value
     */
    public double getLayoutX() {       return layoutX;  }

    /**
     * The x layout of the map formula box in the main pane
     * @param layoutX the x layout value
     */
    public void setLayoutX(double layoutX) {       this.layoutX = layoutX;   }

    /**
     * The y layout of the map formula box in the main pane
     * @return the y layout value
     */
    public double getLayoutY() {       return layoutY;   }

    /**
     * The y layout of the map formula box in the main pane
     * @param layoutY the y layout value
     */
    public void setLayoutY(double layoutY) {       this.layoutY = layoutY;   }

    /**
     * The content of this box
     * @return the content Document
     */
    public Document getText() {       return text;   }

    /**
     * The content of this box
     * @param text the content Document
     */
    public void setText(Document text) {       this.text = text;   }

    /**
     * The list of String IDs linked to this box
     * @return the String list
     */
    public List<String> getLinkIdStrings() {       return linkIdStrings;   }

    /**
     * The list of String IDs linked to this box
     * @param linkIdStrings the String list
     */
    public void setLinkIdStrings(List<String> linkIdStrings) {       this.linkIdStrings = linkIdStrings;   }

    public double getPrintWidth() {
        return printWidth;
    }

    public void setPrintWidth(double printWidth) {
        this.printWidth = printWidth;
    }

    public List<MapIndexItem> getMapIndexItems() {
        return mapIndexItems;
    }

    public void setMapIndexItems(List<MapIndexItem> mapIndexItems) {
        this.mapIndexItems = mapIndexItems;
    }
}

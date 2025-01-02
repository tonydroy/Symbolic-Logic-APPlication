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
 * Model for clickable node link.  Line dragged from source tree formula box to target formula box.
 */
public class ClickableNodeLinkMod implements Serializable {
    private static final long serialVersionUID = 100L;
    private String idString;
    private String sourceId;
    private String targetId;


    /**
     * Construct clickable node link model
     * @param idString id of this node
     * @param sourceId id of source node
     * @param targetId ide of target node
     */
    public ClickableNodeLinkMod(String idString, String sourceId, String targetId) {
        this.idString = idString;
        this.sourceId = sourceId;
        this.targetId = targetId;
    }

    /**
     * id of this node
     * @return the id string
     */
    public String getIdString() {
        return idString;
    }

    /**
     * id of source node
     * @return the id string
     */
    public String getSourceId() {
        return sourceId;
    }

    /**
     * id of target node
     * @return the id string
     */
    public String getTargetId() {
        return targetId;
    }
}

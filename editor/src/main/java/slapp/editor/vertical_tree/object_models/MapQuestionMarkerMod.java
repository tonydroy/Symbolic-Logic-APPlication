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
 * Model for map question marker.  Question marker that attaches to a target map formula box.
 */
public class MapQuestionMarkerMod implements Serializable {
    private static final long serialVersionUID = 100L;
    private String idString;
    private String targetId;
    int targetMapStage;
    private Double[] targetXAnchors;

    /**
     * Construct map question marker model
     * @param idString id of this node
     * @param targetId id of target node
     * @param targetMapStage number of target markers (1 or 2) for this node
     * @param targetXAnchors target x anchors for this node
     */
    public MapQuestionMarkerMod(String idString, String targetId, int targetMapStage, Double[] targetXAnchors) {
        this.idString = idString;
        this.targetId = targetId;
        this.targetMapStage = targetMapStage;
        this.targetXAnchors = targetXAnchors;
    }

    /**
     * The id of this node
     * @return the id string
     */
    public String getIdString() {
        return idString;
    }

    /**
     * The id of the target for this node
     * @return the id string
     */
    public String getTargetId() {
        return targetId;
    }

    /**
     * The number of target markers (1 or 2) for this node
     * @return the number of markers
     */
    public int getTargetMapStage() {
        return targetMapStage;
    }

    /**
     * The Double[] of target x anchors for this node
     * @return the anchors
     */
    public Double[] getTargetXAnchors() {
        return targetXAnchors;
    }

}

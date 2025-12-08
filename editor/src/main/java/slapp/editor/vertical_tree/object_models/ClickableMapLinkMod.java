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
 * Model for the clickable map link.  Connector from source map formula box to target map formula box
 */
public class ClickableMapLinkMod implements Serializable {
    private static final long serialVersionUID = 100L;
    private String idString;
    private String sourceId;
    private String targetId;
    private int sourceMapStage;
    private int targetMapStage;
    private Double[] sourceXAnchors;
    private Double[] targetXAnchors;

    /**
     * The id for this node
     * @return the id String
     */
    public String getIdString() {      return idString;   }

    /**
     * The id for this node
     * @param idString the id string
     */
    public void setIdString(String idString) {     this.idString = idString;   }

    /**
     * The id of the source map formula box
     * @return the id string
     */
    public String getSourceId() {      return sourceId;   }

    /**
     * The id of the source map formula box
     * @param sourceId the id string
     */
    public void setSourceId(String sourceId) {      this.sourceId = sourceId;   }

    /**
     * The id of the target map formula box
     * @return the id string
     */
    public String getTargetId() {      return targetId;   }

    /**
     * The id of the target map formula box
     * @param targetId the id string
     */
    public void setTargetId(String targetId) {      this.targetId = targetId;   }

    /**
     * The number of source markers (1 or 2) for this connector
     * @return the id string
     */
    public int getSourceMapStage() {      return sourceMapStage;   }

    /**
     * The number of source markers (1 or 2) for this connector
     * @param sourceMapStage the stage value
     */
    public void setSourceMapStage(int sourceMapStage) {      this.sourceMapStage = sourceMapStage;   }

    /**
     * The number of target markers (1 or 2) for this connector
     * @return the stage value
     */
    public int getTargetMapStage() {      return targetMapStage;   }

    /**
     * The number of target markers (1 or 2) for this connector
     * @param targetMapStage the stage value
     */
    public void setTargetMapStage(int targetMapStage) {      this.targetMapStage = targetMapStage;   }

    /**
     * The Double[] x source anchors for this connector
     * @return the anchors
     */
    public Double[] getSourceXAnchors() {      return sourceXAnchors;   }

    /**
     * The Double[] x source anchors for this connector
     * @param sourceXAnchors the anchors
     */
    public void setSourceXAnchors(Double[] sourceXAnchors) {       this.sourceXAnchors = sourceXAnchors;   }

    /**
     * The Double[] x target anchors for this connector
     * @return the anchors
     */
    public Double[] getTargetXAnchors() {       return targetXAnchors;   }

    /**
     * The Double[] x target anchors for this connector
     * @param targetXAnchors the anchors
     */
    public void setTargetXAnchors(Double[] targetXAnchors) {       this.targetXAnchors = targetXAnchors;   }

}

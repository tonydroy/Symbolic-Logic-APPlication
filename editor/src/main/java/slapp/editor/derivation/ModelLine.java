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

package slapp.editor.derivation;

import com.gluonhq.richtextarea.model.Document;

import java.io.Serializable;

/**
 * Supplement to DerivationModel for content of a single derivation line.
 */
public class ModelLine implements Serializable {
    private static final long serialVersionUID = 100L;
    private int depth;
    private Document lineContentDoc;
    private String justification = "";
    private LineType lineType = LineType.MAIN_CONTENT_LINE;


    /**
     * Construct model line
     * @param depth the scope depth of the line
     * @param lineContent the main content of the line
     * @param justification the justification of the line
     * @param type the {@link slapp.editor.derivation.LineType}
     */
    public ModelLine(int depth, Document lineContent, String justification, LineType type) {
        this.depth = depth;
        this.lineContentDoc = lineContent;
        this.justification = justification;
        this.lineType = type;
    }

    /**
     * The scope depth of this line
     * @return the depth value
     */
    public int getDepth() {
        return depth;
    }

    /**
     * The content Document (usually null in case of shelf or gap line)
     * @return
     */
    public Document getLineContentDoc() {
        return lineContentDoc;
    }

    /**
     * The justification String (usually empty for shelf or gap line)
     * @return
     */
    public String getJustification() {
        return justification;
    }

    /**
     * The {@link slapp.editor.derivation.LineType}
     * @return the type of this line
     */
    public LineType getLineType() {
        return lineType;
    }


}

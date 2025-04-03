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

import javafx.scene.control.Label;
import javafx.scene.text.TextFlow;
import slapp.editor.decorated_rta.BoxedDRTA;
import slapp.editor.decorated_rta.DecoratedRTA;

import java.util.ArrayList;
import java.util.List;

/**
 * Adjunct to derivation view for the contents of a single derivation line.
 */
public class ViewLine {
    private Label lineNumberLabel = null;
    private int depth = 0;
    private LineType lineType = null;
    private BoxedDRTA lineContentBoxedDRTA = null;
    private TextFlow justificationFlow = null;
    private List<Label> clientLabels = new ArrayList<Label>();
    private boolean lineHighlight;
    private List<String> assumptionList;

    /**
     * Construct view line from scratch
     */
    public ViewLine(){}

    /**
     * Construct view line from components
     * @param lineNumberLabel label set to the number of this line
     * @param depth scope depth of this line
     * @param lineType {@link slapp.editor.derivation.LineType}
     * @param bdrta the main formula field
     * @param justificationFlow the justification field
     * @param clientLabels list of justification line labels linked to this line
     */
    public ViewLine(Label lineNumberLabel, int depth, LineType lineType, BoxedDRTA bdrta, TextFlow justificationFlow, List<Label> clientLabels) {
        this.lineNumberLabel = lineNumberLabel;
        this.depth = depth;
        this.lineType = lineType;
        this.lineContentBoxedDRTA = bdrta;
        this.justificationFlow = justificationFlow;
        this.clientLabels = clientLabels;

    }

    public boolean isLineHighlight() {
        return lineHighlight;
    }

    public void setLineHighlight(boolean lineHighlight) {
        this.lineHighlight = lineHighlight;
    }

    public int realDepth() {
        if (LineType.isGapLine(lineType) == true) {
            return depth -1;
        }
        else {
            return depth;
        }
    }

    public List<String> getAssumptionList() {
        return assumptionList;
    }

    public void setAssumptionList(List<String> assumptionList) {
        this.assumptionList = assumptionList;
    }

    /**
     * label to set the number of this line
     * @param lineNumberLabel the label
     */
    public void setLineNumberLabel(Label lineNumberLabel) {
        this.lineNumberLabel = lineNumberLabel;
    }

    /**
     * label to set the number of this line
     * @return the line number label
     */
    public Label getLineNumberLabel() {
        return lineNumberLabel;
    }

    /**
     * The scope depth of this line
     * @param depth the depth value
     */
    public void setDepth(int depth) { this.depth = depth; }

    /**
     * The scope depth of this line
     * @return the depth value
     */
    public int getDepth() {
        return depth;
    }

    /**
     * The {@link slapp.editor.derivation.LineType} for this line
     * @param type the type
     */
    public void setLineType(LineType type) {
        this.lineType = type;
    }

    /**
     * The {@link slapp.editor.derivation.LineType} for this line
     * @return the line type
     */
    public LineType getLineType() {
        return lineType;
    }

    /**
     * The main formula field
     * @param lineContentBoxedDRTA the main formula boxedDRTA
     */
    public void setLineContentBoxedDRTA(BoxedDRTA lineContentBoxedDRTA) {
        this.lineContentBoxedDRTA = lineContentBoxedDRTA;
    }

    /**
     * The main formula field
     * @return the main formula boxed DRTA
     */
    public BoxedDRTA getLineContentBoxedDRTA() {
        return lineContentBoxedDRTA;
    }

    /**
     * The justification field
     * @param justificationFlow the justification text flow
     */
    public void setJustificationFlow(TextFlow justificationFlow) {this.justificationFlow = justificationFlow; }

    /**
     * The justification field
     * @return the justification text flow
     */
    public TextFlow getJustificationFlow() {
        return justificationFlow;
    }

    /**
     * List of justification line labels linked to this line
     * @return the client list
     */
    public List<Label> getClientLabels() {
        return clientLabels;
    }

}

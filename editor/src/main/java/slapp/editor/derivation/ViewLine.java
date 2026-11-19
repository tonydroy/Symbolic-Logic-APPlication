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

    // SCORCHED EARTH RULE 2: Kill Switch
    private boolean isTornDown = false;

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

    // SCORCHED EARTH RULE 6: Sever Shared Nodes helper (Using fully qualified paths to obey strict imports)
    private void severFromParent(javafx.scene.Node node) {
        try {
            if (node != null && node.getParent() != null) {
                if (node.getParent() instanceof javafx.scene.layout.Pane) {
                    ((javafx.scene.layout.Pane) node.getParent()).getChildren().remove(node);
                } else if (node.getParent() instanceof javafx.scene.Group) {
                    ((javafx.scene.Group) node.getParent()).getChildren().remove(node);
                }
            }
        } catch (Exception e) {}
    }

    // SCORCHED EARTH RULE 1: Cascading Cleanup
    public void cleanup() {
        // KILL SWITCH: Engaged
        isTornDown = true;

        // SCORCHED EARTH RULE 5: Rule of Isolation Break Down
        try {
            if (lineNumberLabel != null) {
                if (lineNumberLabel.textProperty() != null) lineNumberLabel.textProperty().unbind();
                severFromParent(lineNumberLabel);
            }
        } catch (Exception e) {}

        try {
            if (clientLabels != null) {
                for (Label label : clientLabels) {
                    try {
                        if (label != null) {
                            if (label.textProperty() != null) label.textProperty().unbind();
                            severFromParent(label);
                        }
                    } catch (Exception e) {}
                }
            }
        } catch (Exception e) {}

        try {
            if (lineContentBoxedDRTA != null) {
                try { lineContentBoxedDRTA.cleanup(); } catch (Exception e) {}
                try { if (lineContentBoxedDRTA.getBoxedRTA() != null) severFromParent(lineContentBoxedDRTA.getBoxedRTA()); } catch (Exception e) {}
                try { if (lineContentBoxedDRTA.getRTA() != null) severFromParent(lineContentBoxedDRTA.getRTA()); } catch (Exception e) {}
            }
        } catch (Exception e) {}

        try {
            if (justificationFlow != null) {
                // SCORCHED EARTH RULE 8: Safely sever dynamic children to prevent CME
                if (justificationFlow.getChildren() != null) {
                    List<javafx.scene.Node> flowChildrenCopy = new ArrayList<>(justificationFlow.getChildren());
                    for (javafx.scene.Node n : flowChildrenCopy) {
                        try {
                            if (n instanceof Label) {
                                if (((Label) n).textProperty() != null) {
                                    ((Label) n).textProperty().unbind();
                                }
                            }
                        } catch (Exception ex) {}
                        severFromParent(n);
                    }
                }
                justificationFlow.getChildren().clear();
                severFromParent(justificationFlow);
            }
        } catch (Exception e) {}

        // SCORCHED EARTH RULE 10: Non-destructive data nullifications (do NOT call .clear() on data lists)
        try { this.clientLabels = null; } catch (Exception e) {}
        try { this.assumptionList = null; } catch (Exception e) {}

        // Final Explicit Class Nullifications
        this.lineNumberLabel = null;
        this.lineType = null;
        this.lineContentBoxedDRTA = null;
        this.justificationFlow = null;
    }

    public boolean isLineHighlight() {
        if (isTornDown) return false;
        return lineHighlight;
    }

    public void setLineHighlight(boolean lineHighlight) {
        if (isTornDown) return;
        this.lineHighlight = lineHighlight;
    }

    public int realDepth() {
        if (isTornDown) return 0;
        try {
            if (LineType.isGapLine(lineType)) {
                return depth - 1;
            } else {
                return depth;
            }
        } catch (Exception e) { return depth; }
    }

    public List<String> getAssumptionList() {
        if (isTornDown) return null;
        return assumptionList;
    }

    public void setAssumptionList(List<String> assumptionList) {
        if (isTornDown) return;
        this.assumptionList = assumptionList;
    }

    /**
     * label to set the number of this line
     * @param lineNumberLabel the label
     */
    public void setLineNumberLabel(Label lineNumberLabel) {
        if (isTornDown) return;
        this.lineNumberLabel = lineNumberLabel;
    }

    /**
     * label to set the number of this line
     * @return the line number label
     */
    public Label getLineNumberLabel() {
        if (isTornDown) return null;
        return lineNumberLabel;
    }

    /**
     * The scope depth of this line
     * @param depth the depth value
     */
    public void setDepth(int depth) {
        if (isTornDown) return;
        this.depth = depth;
    }

    /**
     * The scope depth of this line
     * @return the depth value
     */
    public int getDepth() {
        if (isTornDown) return 0;
        return depth;
    }

    /**
     * The {@link slapp.editor.derivation.LineType} for this line
     * @param type the type
     */
    public void setLineType(LineType type) {
        if (isTornDown) return;
        this.lineType = type;
    }

    /**
     * The {@link slapp.editor.derivation.LineType} for this line
     * @return the line type
     */
    public LineType getLineType() {
        if (isTornDown) return null;
        return lineType;
    }

    /**
     * The main formula field
     * @param lineContentBoxedDRTA the main formula boxedDRTA
     */
    public void setLineContentBoxedDRTA(BoxedDRTA lineContentBoxedDRTA) {
        if (isTornDown) return;
        this.lineContentBoxedDRTA = lineContentBoxedDRTA;
    }

    /**
     * The main formula field
     * @return the main formula boxed DRTA
     */
    public BoxedDRTA getLineContentBoxedDRTA() {
        if (isTornDown) return null;
        return lineContentBoxedDRTA;
    }

    /**
     * The justification field
     * @param justificationFlow the justification text flow
     */
    public void setJustificationFlow(TextFlow justificationFlow) {
        if (isTornDown) return;
        this.justificationFlow = justificationFlow;
    }

    /**
     * The justification field
     * @return the justification text flow
     */
    public TextFlow getJustificationFlow() {
        if (isTornDown) return null;
        return justificationFlow;
    }

    /**
     * List of justification line labels linked to this line
     * @return the client list
     */
    public List<Label> getClientLabels() {
        if (isTornDown) return null;
        return clientLabels;
    }
}
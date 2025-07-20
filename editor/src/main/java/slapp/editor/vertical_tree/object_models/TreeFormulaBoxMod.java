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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Model for the tree formula box
 */
public class TreeFormulaBoxMod implements Serializable {
    private static final long serialVersionUID = 100L;
    private String idString;
    private double layoutX;
    private double layoutY;
    private Document text;
    private List<String> linkIdStrings;
    boolean boxed;
    boolean starred;
    boolean annotation;

   Document annotationDoc;
//    String annotationText;
    Double[] circleXAnchors;
    boolean circled;
    private int[] circleIndexes;
    double rtaBoundsHeight;
    double rtaBoundsMinY;
    List<UnderlineMod> underlineList = new ArrayList<>();
    List<Integer> baseline;

    double printWidth;

    public TreeFormulaBoxMod() {
        printWidth = 0;
        circleIndexes = new int[2];
    }

    /**
     * id string for this node
     * @return the string
     */
    public String getIdString() {      return idString;   }

    /**
     * id string for this node
     * @param idString the string
     */
    public void setIdString(String idString) {      this.idString = idString;   }

    /**
     * layout X in main pane
     * @return the layout value
     */
    public double getLayoutX() {       return layoutX;   }

    /**
     * layout X in main pane
     * @param layoutX the layout value
     */
    public void setLayoutX(double layoutX) {       this.layoutX = layoutX;   }

    /**
     * layout y in main pane
     * @return the layout value
     */
    public double getLayoutY() {       return layoutY;   }

    /**
     * layout y in main pane
     * @param layoutY the layout value
     */
    public void setLayoutY(double layoutY) {       this.layoutY = layoutY;   }

    /**
     * The box content
     * @return the content Document
     */
    public Document getText() {       return text;   }

    /**
     * The box content
     * @param text the content Document
     */
    public void setText(Document text) {       this.text = text;   }

    /**
     * id strings for connectors linked to this node
     * @return the list of strings
     */
    public List<String> getLinkIdStrings() {       return linkIdStrings;   }

    /**
     * id strings for connectors linked to this node
     * @param linkIdStrings the list of strings
     */
    public void setLinkIdStrings(List<String> linkIdStrings) {       this.linkIdStrings = linkIdStrings;   }

    /**
     * A boxed node has a solid border around the middle box, including the formula and lines pane
     * @return true if boxed and otherwise false
     */
    public boolean isBoxed() {       return boxed;   }

    /**
     * A boxed node has a solid border around the middle box, including the formula and lines pane
     * @param boxed true if boxed and otherwise false
     */
    public void setBoxed(boolean boxed) {       this.boxed = boxed;   }

    /**
     * A starred node has a superscript star in its upper right corner
     * @return true if starred and otherwise false
     */
    public boolean isStarred() {       return starred;   }

    /**
     * A starred node has a superscript star in its upper right corner
     * @param starred true if starred and otherwise false
     */
    public void setStarred(boolean starred) {       this.starred = starred;   }

    /**
     * An annotated node has a superscript text field in its upper right corner
     * @return true if annotated and otherwise false
     */
    public boolean isAnnotation() {       return annotation;   }

    /**
     * An annotated node has a superscript text field in its upper right corner
     * @param annotation true if annotated and otherwise false
     */
    public void setAnnotation(boolean annotation) {       this.annotation = annotation;   }

    /**
     * The text for the annotation field
     * @return the String text
     */
 //   public String getAnnotationText() {       return annotationText;   }
    public Document getAnnotationDoc() { return annotationDoc; }

    /**
     * The text for the annotation field
     * @param annotationDoc the String text
     */
//    public void setAnnotationText(String annotationText) {       this.annotationText = annotationText;   }
    public void setAnnotationDoc(Document annotationDoc) { this.annotationDoc = annotationDoc; }

    /**
     * Anchor positions for the left and right edges of a circle
     * @return the anchors
     */
    public Double[] getCircleXAnchors() {       return circleXAnchors;   }

    /**
     * Anchor positions for the left and right edges of a circle
     * @param circleXAnchors the anchors
     */
    public void setCircleXAnchors(Double[] circleXAnchors) {       this.circleXAnchors = circleXAnchors;   }

    /**
     * A box is circled if it contains a circle with edges at the circle x anchors
     * @return true if circled and otherwise false
     */
    public boolean isCircled() {       return circled;   }

    /**
     * A box is circled if it contains a circle with edges at the circle x anchors
     * @param circled
     */
    public void setCircled(boolean circled) {       this.circled = circled;   }

    /**
     * List of {@link slapp.editor.vertical_tree.object_models.UnderlineMod} for this node
     */
    public List<UnderlineMod> getUnderlineList() {       return underlineList;   }

    /**
     * As a function of x position, the baseline list "traces" y position of top lines as distance from bottom of the lines pane
     * @return the baseline list
     */
    public List<Integer> getBaseline() {       return baseline;   }

    /**
     * As a function of x position, the baseline list "traces" y position of top lines as distance from bottom of the lines pane
     * @param baseline the baseline list
     */
    public void setBaseline(List<Integer> baseline) {       this.baseline = baseline;   }

    /**
     * Height of bounds for the RTA in the tree formula box
     * @return the height value
     */
    public double getRtaBoundsHeight() {       return rtaBoundsHeight;   }

    /**
     * Height of bounds for the RTA in the tree formula box
     * @param rtaBoundsHeight the height value
     */
    public void setRtaBoundsHeight(double rtaBoundsHeight) {       this.rtaBoundsHeight = rtaBoundsHeight;   }

    /**
     * The least Y position of bounds for the RTA in the tree formula box
     * @return the y position value
     */
    public double getRtaBoundsMinY() {       return rtaBoundsMinY;   }

    /**
     * The least Y position of bounds for the RTA in the tree formula box
     * @param rtaBoundsMinY the y position value
     */
    public void setRtaBoundsMinY(double rtaBoundsMinY) {       this.rtaBoundsMinY = rtaBoundsMinY;   }


    public double getPrintWidth() {
        return printWidth;
    }

    public void setPrintWidth(double printWidth) {
        this.printWidth = printWidth;
    }

    public int[] getCircleIndexes() {
        if (circleIndexes != null) return circleIndexes;
        else return new int[2];
    }

    public void setCircleIndexes(int[] circleIndexes) {
        this.circleIndexes = circleIndexes;
    }
}

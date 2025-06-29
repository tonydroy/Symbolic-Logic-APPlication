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

import com.gluonhq.richtextarea.model.Document;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;

/**
 * Model for node which may itself have child nodes -- and so constitute a branching structure
 */
class BranchModel implements Serializable {
    private static final long serialVersionUID = 100L;
    private boolean annotation;
    private boolean formulaBranch;
    private boolean indefiniteNumBranch;
    private boolean dotDivider;
    private boolean rootBranch;
    private Document formulaDoc;
    private Document connectorDoc;
    private ArrayList<BranchModel> dependents = new ArrayList<>();
    private Document annotationDoc = new Document();
    private double formPrintWidth;
    private double connPrintWidth;





    public Document getAnnotationDoc() {
        return annotationDoc;
    }

    public void setAnnotationDoc(Document annotationDoc) {
        this.annotationDoc = annotationDoc;
    }

    /**
     * The list of child nodes
      * @return the list
     */
    ArrayList<BranchModel> getDependents() {   return dependents;  }

    /**
     * True if the node includes an annotation field and otherwise false.
     * @param annotation the boolean value
     */
    void setAnnotation(boolean annotation) {    this.annotation = annotation;   }

    /**
     * True if node includes an annotation field and otherwise false.
     * @return the boolean value
     */
    boolean isAnnotation() {     return annotation;  }

    /**
     * True if node is for a formula field and otherwise (if term branch) false
     * @param formulaBranch the boolean value
     */
    void setFormulaBranch(boolean formulaBranch) {    this.formulaBranch = formulaBranch;  }

    /**
     * True is node is for formula field and otherwise (if term branch) false3
     * @return the boolean value
     */
    boolean isFormulaBranch() {     return formulaBranch;  }

    /**
     * True if node holds just the three vertical dots and otherwise false
     * @param indefiniteNumBranch the boolean value
     */
    void setIndefiniteNumBranch(boolean indefiniteNumBranch) {   this.indefiniteNumBranch = indefiniteNumBranch;   }

    /**
     * True if node holds just the three vertical dots and otherwise false
     * @return the boolean value3
     */
    boolean isIndefiniteNumBranch() {   return indefiniteNumBranch;  }

    /**
     * True if node includes the vertical (formula/term) dotted divider
     * @param dotDivider the boolean value
     */
    void setDotDivider(boolean dotDivider) {    this.dotDivider = dotDivider;  }

    /**
     * True if node includes the (formula/term) dotted divider
     * @return the boolean value
     */
    boolean isDotDivider() { return dotDivider; }

    /**
     * True if this is a root node (not a child)
     * @param rootBranch the boolean value
     */
    void setRootBranch(boolean rootBranch) {   this.rootBranch = rootBranch;   }

    /**
     * True if this is a root node (not a child)
     * @return the boolean value
     */
    boolean isRootBranch() {  return rootBranch;  }

    /**
     * Annotation text for this node (if there is an annotation)
     * @param annotationText the String text
     */
 //   void setAnnotationText(String annotationText) {     this.annotationText = annotationText;   }

    /**
     * Annotation text for this node (if there is an annotation)
     * @return the string text
     */
//    String getAnnotationText() {    return annotationText;  }

    /**
     * The Document for the formula or term at this node
     * @param formulaDoc The Document
     */
    void setFormulaDoc(Document formulaDoc) {     this.formulaDoc = formulaDoc;   }

    /**
     * The Document for the formula or term at this node
     * @return the Document
     */
    Document getFormulaDoc() {    return formulaDoc;   }


    /**
     * The Document for the connector at this node (if it is a formula node)
     * @param connectorDoc the Document
     */
    void setConnectorDoc(Document connectorDoc) {    this.connectorDoc = connectorDoc;   }

    /**
     * The Documment for the connector at this node (if it is a formula node)
     * @return the Document
     */
    Document getConnectorDoc() {    return connectorDoc;  }


    public double getFormPrintWidth() {
        return formPrintWidth;
    }

    public void setFormPrintWidth(double formPrintWidth) {
        this.formPrintWidth = formPrintWidth;
    }

    public double getConnPrintWidth() {
        return connPrintWidth;
    }

    public void setConnPrintWidth(double connPrintWidth) {
        this.connPrintWidth = connPrintWidth;
    }
}

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

import com.gluonhq.richtextarea.RichTextArea;
import com.gluonhq.richtextarea.RichTextAreaSkin;
import com.gluonhq.richtextarea.action.TextDecorateAction;
import com.gluonhq.richtextarea.model.ParagraphDecoration;
import com.gluonhq.richtextarea.model.TextDecoration;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.scene.transform.Scale;
import slapp.editor.decorated_rta.BoxedDRTA;
import slapp.editor.decorated_rta.DecoratedRTA;
import slapp.editor.main_window.ControlType;

import java.util.ArrayList;

/**
 * Node which may have other Nodes as children (and so constitute a branch structure)
 */
public class BranchNode extends HBox {
    BranchNode self;
    boolean root = false;
    BranchNode container;
    HorizontalTreeView horizontalTreeView;
    BoxedDRTA formulaBoxedDRTA;
    BoxedDRTA connectorBoxedDRTA;
    BoxedDRTA annotationField;
    ArrayList<BranchNode> dependents = new ArrayList<>();
    static double offsetY = 48;  //34
    static double leafPos = -48;
    double minYlayout;
    double maxYlayout;
    double Xlayout;
    double Ylayout;
    boolean annotation = false;
    boolean formulaNode = true;
    boolean indefiniteNode = false;
    boolean dotDivider;
    double annotationWidth  = 40;
    double rootBump = 0;
    double annBump = 0;
    double formulaBoxHeight = 22.5;
    ChangeListener formulaFocusListener;
    ChangeListener connectorFocusListener;


    /**
     * Construct Branch node
     * @param container the parent node (if any)
     * @param horizontalTreeView the horizontal tree view
     */
    public BranchNode(BranchNode container, HorizontalTreeView horizontalTreeView) {
        super();
        this.container = container;
        this.horizontalTreeView = horizontalTreeView;
        self = this;
        formulaBoxedDRTA = newFormulaBoxedDRTA();
        connectorBoxedDRTA = newFormulaBoxedDRTA();
        self.getChildren().add(formulaBoxedDRTA.getBoxedRTA());
        self.setStyle("-fx-border-color: white white black white; -fx-border-width: 0 0 1.5 0");
        self.setPadding(new Insets(0, 4, 0, 2));

        annotationField = new BoxedDRTA();
        RichTextArea annRTA = annotationField.getRTA();
        annRTA.setPrefWidth(annotationWidth);
        annRTA.setPrefHeight(20);
        annotationField.getDRTA().getKeyboardSelector().valueProperty().setValue(RichTextAreaSkin.KeyMapValue.BASE_AND_SANS);
        annRTA.addEventFilter(KeyEvent.ANY, e -> {
            if (e.getCode() == KeyCode.ENTER) e.consume();
        });
        annRTA.getStylesheets().add("slappAnnotation.css");
        annRTA.setPromptText("");

        annRTA.focusedProperty().addListener((ob, ov, nv) -> {
            if (nv)  horizontalTreeView.getMainView().editorInFocus(annotationField.getDRTA(), ControlType.FIELD);
            else {
                if (annRTA.isModified()) {
                    horizontalTreeView.setUndoRedoFlag(true);
                    horizontalTreeView.setUndoRedoFlag(false);
                    annRTA.getActionFactory().saveNow().execute(new ActionEvent());
                }

            }
        });

        annotationField.getBoxedRTA().getTransforms().add(new Scale(.75,.75));

        formulaFocusListener = new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue ob, Boolean ov, Boolean nv) {
                if (nv) {
                    horizontalTreeView.getMainView().editorInFocus(formulaBoxedDRTA.getDRTA(), ControlType.FIELD);
                }
                else {
                    if (formulaBoxedDRTA.getRTA().isModified()) {
                        horizontalTreeView.setUndoRedoFlag(true);
                        horizontalTreeView.setUndoRedoFlag(false);
                        formulaBoxedDRTA.getRTA().getActionFactory().saveNow().execute(new ActionEvent());
                    }
                }
            }
        };
        formulaBoxedDRTA.getRTA().focusedProperty().addListener(formulaFocusListener);

        connectorFocusListener = new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue ob, Boolean ov, Boolean nv) {
                if (nv) {
                    horizontalTreeView.getMainView().editorInFocus(connectorBoxedDRTA.getDRTA(), ControlType.FIELD);
                }
                else {
                    if (connectorBoxedDRTA.getRTA().isModified()) {
                        horizontalTreeView.setUndoRedoFlag(true);
                        horizontalTreeView.setUndoRedoFlag(false);
                        connectorBoxedDRTA.getRTA().getActionFactory().saveNow().execute(new ActionEvent());
                    }
                }
            }
        };
        connectorBoxedDRTA.getRTA().focusedProperty().addListener(connectorFocusListener);
    }

    /**
     * Initial step for branch layout
     * @param xVal initial x offset (usually zero)
     */
    void doLayout(double xVal) {
        leafPos = -48;
        setLayout(xVal);
    }

    /**
     * Set X,Y layout for this node, based recursively calling for children
     * @param xVal the X position for this node
     * @return the Y value
     */
    double setLayout(double xVal) {
        Xlayout = xVal;
        if (dependents.isEmpty()) {
            leafPos = leafPos + offsetY;
            Ylayout = leafPos;
            if (!formulaNode) Ylayout -= formulaBoxHeight/2; //Ylayout -= 14;
            return Ylayout;
        }
        else {
            BranchNode initialNode = dependents.get(0);

            if (!initialNode.isFormulaNode())   minYlayout = initialNode.setLayout(xVal + formulaBoxedDRTA.getRTA().getPrefWidth() + annBump + rootBump + 36);
            else  minYlayout = initialNode.setLayout(xVal + formulaBoxedDRTA.getRTA().getPrefWidth() + annBump + rootBump + connectorBoxedDRTA.getRTA().getPrefWidth() + 9);

            maxYlayout = minYlayout;
            for (int i = 1; i < dependents.size(); i++) {
                BranchNode node = dependents.get(i);

              if (!initialNode.isFormulaNode()) maxYlayout = node.setLayout(xVal + formulaBoxedDRTA.getRTA().getPrefWidth() + annBump + rootBump + 36);
              else maxYlayout = node.setLayout(xVal + formulaBoxedDRTA.getRTA().getPrefWidth() + annBump + rootBump + connectorBoxedDRTA.getRTA().getPrefWidth() + 9);
            }
            Ylayout = minYlayout + (maxYlayout - minYlayout)/2.0;
            return Ylayout;
        }
    }

    /**
     * Add node and children recursively to pane including brackets
     * @param pane the pane to which the branch is added
     * @param offsetX X offset for branch (usually zero)
     * @param offsetY Y offset for branch (usually zero)
     */
    void addToPane (Pane pane, double offsetX, double offsetY) {
        pane.getChildren().add(self);
        self.setLayoutX(Xlayout + offsetX);
        self.setLayoutY(Ylayout + offsetY);

        if (dotDivider) {
            Line dotLine = new Line(0,0,0,27);
            dotLine.getStrokeDashArray().addAll(1.0, 4.0);
            pane.getChildren().add(dotLine);
            dotLine.setLayoutX(self.Xlayout + formulaBoxedDRTA.getRTA().getPrefWidth() + annBump + rootBump + 13);  //9
            dotLine.setLayoutY(self.Ylayout + formulaBoxHeight/2);
        }
        if (!dependents.isEmpty()) {
            if (dependents.get(0).isFormulaNode()) {
                offsetY = 0.0;
                if (dependents.size() == 1) {
                    HBox simpleConnector = connectorBoxedDRTA.getBoxedRTA();
                    pane.getChildren().add(simpleConnector);
                    BranchNode dependent = dependents.get(0);

                    simpleConnector.setLayoutX(Xlayout + formulaBoxedDRTA.getRTA().getPrefWidth() + annBump + rootBump + 10); ///8
                    simpleConnector.setLayoutY(dependent.getYLayout() + formulaBoxHeight * 3/8);
                }
                else if (dependents.size() > 1) {
                    BranchNode topNode = dependents.get(0);
                    BranchNode bottomNode = dependents.get(dependents.size() - 1);
                    double top = topNode.getYLayout();
                    double bottom = bottomNode.getYLayout();
                    HBox bracketBox = newBracketBox(top, bottom);
                    pane.getChildren().add(bracketBox);

                    bracketBox.setLayoutX(Xlayout + formulaBoxedDRTA.getRTA().getPrefWidth() + annBump + rootBump + 3);
                    bracketBox.setLayoutY(topNode.getYLayout() + formulaBoxHeight + .8);
                }
            }
            else {
                offsetY = formulaBoxHeight/2;
                Pane branchPane = newTermBranch();
                pane.getChildren().add(branchPane);
                BranchNode topNode = dependents.get(0);

                double dotsBump = 6;
                if (dotDivider) dotsBump = 16;   //12

                branchPane.setLayoutX(Xlayout + formulaBoxedDRTA.getRTA().getPrefWidth() +annBump + rootBump + dotsBump );
                branchPane.setLayoutY(topNode.getYLayout() + formulaBoxHeight + 1.5);
            }
        }

        for (int i = 0; i < dependents.size(); i++) {
            BranchNode node = dependents.get(i);
            node.addToPane(pane, offsetX, offsetY);
        }
    }

    /**
     * BoxedDRTA for formula or connector field
     * @return the BoxedDRTA
     */
    private BoxedDRTA newFormulaBoxedDRTA() {
        BoxedDRTA boxedDRTA = new BoxedDRTA();

        DecoratedRTA drta = boxedDRTA.getDRTA();
        drta.getKeyboardSelector().valueProperty().setValue(RichTextAreaSkin.KeyMapValue.ITALIC_AND_SANS);
        RichTextArea rta = boxedDRTA.getRTA();
        rta.setMaxHeight(formulaBoxHeight);
        rta.setMinHeight(formulaBoxHeight);

        RichTextAreaSkin rtaSkin = (RichTextAreaSkin) rta.getSkin();
        rta.prefWidthProperty().bind(Bindings.max(Bindings.add(rtaSkin.nodesWidthProperty(), 6), 12));

        rtaSkin.nodesWidthProperty().addListener((observable, oldValue, newValue) -> {
            Platform.runLater(() -> rta.getParent().requestLayout());
        });

        rta.prefWidthProperty().addListener((ob, ov, nv) -> {
            horizontalTreeView.refreshTreePanes();
        });
        rta.addEventFilter(KeyEvent.ANY, e -> {
            if (e.getCode() == KeyCode.ENTER) e.consume();
        });
        rta.getStylesheets().add("RichTextField.css");
        rta.setPromptText("");
        return boxedDRTA;
    }

    /**
     * The bracket for term branching
     * @return the pane
     */
    private Pane newTermBranch() {
        double top = dependents.get(0).getYLayout();
        double bottom  = dependents.get(dependents.size() - 1).getYLayout();
        double center = (bottom - top)/2;
        Pane branchPane = new Pane();
        double xEnd = 31;
        if (dotDivider) xEnd = 24;
        for (BranchNode node : dependents) {
            Line line = new Line(0, center, xEnd, node.getYLayout() - top);
            branchPane.getChildren().add(line);
        }
        return branchPane;
    }

    /**
     * The box for formula branching which contains the connector formula, stub line, and vertical bracket line
     * @param top the Y position of the top of the bracket
     * @param bottom the Y position of the bottom of the bracket
     * @return the HBox
     */
    private HBox newBracketBox(double top, double bottom) {
        double height = bottom - top;
        connectorBoxedDRTA.getBoxedRTA().setPadding(new Insets(0,0,6,0));

        Group rtaBox = new Group(connectorBoxedDRTA.getBoxedRTA());
        Line stub = new Line(0, 0, 3, 0);
        stub.setStyle("-fx-stroke-width: 1.5");
        Line bracket = new Line(0,0, 0, height);
        bracket.setStyle("-fx-stroke-width: 1.5");
        HBox bracketBox = new HBox(rtaBox, stub, bracket);
        bracketBox.setAlignment(Pos.CENTER);

        return bracketBox;
    }

    /**
     * Add/remove annotation field on node
     * @param add true if add, false if remove
     */
    void processAnnotationRequest(boolean add) {
        if (add) {
            if (!annotation) {
                addAnnotation();
                annBump = annotationWidth * .75 + 3;
            }
        }
        else {
            if (annotation) {
                self.getChildren().remove(annotationField.getBoxedRTA());
                annotation = false;
                annBump = 0;
            }
        }
    }

    /**
     * Add annotation field (if not there already)
     */
    void addAnnotation() {
        if (!annotation) {
            self.getChildren().add(annotationField.getBoxedRTA());
            self.setMargin(annotationField.getBoxedRTA(), new Insets(0, 0, .25 * 11.0, 0));
            annotation = true;
        }
    }

    /**
     * True if node includes annotation field and otherwise false
     * @return the boolean value
     */
    boolean isAnnotation() {    return annotation;  }

    /**
     * True if node includes annotation field and otherwise false
     * @param annotation the boolean value
     */
    void setAnnotation(boolean annotation) {   this.annotation = annotation; }

    /**
     * The annotation boxed drta
     * @return the field
     */
    BoxedDRTA getAnnotationField() {return annotationField; }

    /**
     * The width bump associated with the existence or not of an annotation field
     * @param annBump the width value
     */
    void setAnnBump(double annBump) {    this.annBump = annBump;   }

    /**
     * The width of an annotation field
     * @return the width value
     */
    double getAnnotationWidth() {     return annotationWidth;  }

    /**
     * True if node has just the three vertical dots, and otherwise false
     * @return the boolean value
     */
    boolean isIndefiniteNode() {    return indefiniteNode;   }

    /**
     *True if node has just the three vertical dots, and otherwise false
     * @param indefiniteNode
     */
    void setIndefiniteNode(boolean indefiniteNode) {    this.indefiniteNode = indefiniteNode;  }

    /**
     * True if node includes the vertical (formula/term) dotted divider
     * @return
     */
    boolean isDotDivider() {    return dotDivider;  }

    /**
     * True if node includes vertical (formula / term dotted divider
     * @param dotDivider
     */
    void setDotDivider(boolean dotDivider) { this.dotDivider = dotDivider; }

    /**
     * True if is root node (is not a child) and otherwise false
     * @return the boolean value
     */
    boolean isRoot() {     return root;  }

    /**
     * The BoxedDRTA for the formula at this node.
     * @return the BoxedDRTA
     */
    BoxedDRTA getFormulaBoxedDRTA() {  return formulaBoxedDRTA;  }

    /**
     * The BoxedDRTA for the connector at this node (for a formula rather than a term branch)
     * @return the BoxedDRTA
     */
    BoxedDRTA getConnectorBoxedDRTA() {     return connectorBoxedDRTA;  }

    /**
     * The container is the parent node (if any)
     * @return the BranchNode
     */
    BranchNode getContainer() {return container; }

    /**
     * The XLayout of this node
     * @return
     */
    double getXLayout() { return Xlayout; }

    /**
     * True if this is a root node and otherwise false
     * @param root the boolean value
     */
    void setRoot(boolean root) {    this.root = root;}

    /**
     * The Y layout of this node
     * @return the Y value
     */
    double getYLayout() { return Ylayout; }

    /**
     * The list of dependents of this node
     * @return the list
     */
    ArrayList<BranchNode> getDependents() {  return dependents;  }

    /**
     * The X bump associated with a root node
     * @param rootBump the X value
     */
    void setRootBump(double rootBump) {    this.rootBump = rootBump;  }

    /**
     * True if formula node, and otherwise (if term node) false
     * @return the boolean value
     */
    boolean isFormulaNode() {  return formulaNode; }

    /**
     * True if formula node, and otherwise (if term node) false
     * @param formulaNode
     */
    void setFormulaNode(boolean formulaNode) { this.formulaNode = formulaNode; }

}

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
import com.gluonhq.richtextarea.model.Document;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Line;
import javafx.scene.transform.Scale;
import slapp.editor.decorated_rta.BoxedDRTA;
import slapp.editor.decorated_rta.DecoratedRTA;
import slapp.editor.m_parse.m_grammatical_parts.FormulaPacket;
import slapp.editor.m_parse.m_grammatical_parts.TermPacket;
import slapp.editor.main_window.ControlType;
import java.util.ArrayList;
import java.util.List;
import javafx.animation.PauseTransition;
import javafx.util.Duration;
import javafx.event.EventHandler;
import javafx.scene.Node;


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
    double annotationWidth  = 42;
    double rootBump = 0;
    double annBump = 0;
    double formulaBoxHeight = 24;
    double formPrintWidth;
    double connPrintWidth;

    FormulaPacket formulaPacket = null;
    TermPacket termPacket = null;

    // KILL SWITCH: Prevents asynchronous listener execution during teardown
    private boolean isTornDown = false;

    // SCORCHED EARTH RULE 3: Eradicate Anonymous Lambdas
    private EventHandler<KeyEvent> annotationKeyFilter;
    private ChangeListener<Boolean> annotationFocusListener;
    private ChangeListener<Boolean> formulaFocusListener;
    private ChangeListener<Boolean> connectorFocusListener;

    // Separate listeners so the formula's listeners aren't overwritten by the connector's
    private ChangeListener<Number> formNodesWidthListener;
    private ChangeListener<Number> connNodesWidthListener;
    private ChangeListener<Number> formPrefWidthListener;
    private ChangeListener<Number> connPrefWidthListener;
    private EventHandler<KeyEvent> formKeyFilter;
    private EventHandler<KeyEvent> connKeyFilter;

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
        formulaBoxedDRTA = newFormulaBoxedDRTA(true);
        connectorBoxedDRTA = newFormulaBoxedDRTA(false);
        connectorBoxedDRTA.getDRTA().getKeyboardSelector().valueProperty().setValue(RichTextAreaSkin.KeyMapValue.ITALIC_AND_SANS);
        self.getChildren().add(formulaBoxedDRTA.getBoxedRTA());
        self.setStyle("-fx-border-color: white white black white; -fx-border-width: 0 0 1.5 0");
        self.setPadding(new Insets(0, 4, 0, 2));

        annotationField = new BoxedDRTA();
        RichTextArea annRTA = annotationField.getRTA();
        annRTA.setPrefWidth(annotationWidth);
        annRTA.setPrefHeight(20);
        annotationField.getDRTA().getKeyboardSelector().valueProperty().setValue(RichTextAreaSkin.KeyMapValue.SANS_AND_ITALIC);

        annotationKeyFilter = e -> {
            if (isTornDown || this.horizontalTreeView == null || e == null) return;
            if (e.getCode() == KeyCode.ENTER) e.consume();
        };
        annRTA.addEventFilter(KeyEvent.ANY, annotationKeyFilter);

        annRTA.getStylesheets().add("slappAnnotation.css");
        annRTA.setPromptText("");

        annotationFocusListener = (ob, ov, nv) -> {
            if (isTornDown || this.horizontalTreeView == null || this.horizontalTreeView.getMainView() == null) return;
            try {
                if (nv)  horizontalTreeView.getMainView().editorInFocus(annotationField.getDRTA(), ControlType.FIELD);
                else {
                    if (annRTA.isModified()) {
                        horizontalTreeView.setUndoRedoFlag(true);
                        horizontalTreeView.setUndoRedoFlag(false);
                        annRTA.getActionFactory().saveNow().execute(new ActionEvent());
                    }
                }
            } catch (Exception e) {}
        };
        annRTA.focusedProperty().addListener(annotationFocusListener);

        annotationField.getBoxedRTA().getTransforms().add(new Scale(.75,.75));

        formulaFocusListener = new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> ob, Boolean ov, Boolean nv) {
                if (isTornDown || BranchNode.this.horizontalTreeView == null || BranchNode.this.horizontalTreeView.getMainView() == null || formulaBoxedDRTA == null) return;
                try {
                    if (nv) {
                        horizontalTreeView.getMainView().editorInFocus(formulaBoxedDRTA.getDRTA(), ControlType.FIELD);
                    }
                    else {
                        if (formulaBoxedDRTA.getRTA() != null && formulaBoxedDRTA.getRTA().isModified()) {
                            horizontalTreeView.setUndoRedoFlag(true);
                            horizontalTreeView.setUndoRedoFlag(false);
                            formulaBoxedDRTA.getRTA().getActionFactory().saveNow().execute(new ActionEvent());
                        }
                    }
                } catch (Exception e) {}
            }
        };
        formulaBoxedDRTA.getRTA().focusedProperty().addListener(formulaFocusListener);

        connectorFocusListener = new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> ob, Boolean ov, Boolean nv) {
                if (isTornDown || BranchNode.this.horizontalTreeView == null || BranchNode.this.horizontalTreeView.getMainView() == null || connectorBoxedDRTA == null) return;
                try {
                    if (nv) {
                        horizontalTreeView.getMainView().editorInFocus(connectorBoxedDRTA.getDRTA(), ControlType.FIELD);
                    }
                    else {
                        if (connectorBoxedDRTA.getRTA() != null && connectorBoxedDRTA.getRTA().isModified()) {
                            horizontalTreeView.setUndoRedoFlag(true);
                            horizontalTreeView.setUndoRedoFlag(false);
                            connectorBoxedDRTA.getRTA().getActionFactory().saveNow().execute(new ActionEvent());
                        }
                    }
                } catch (Exception e) {}
            }
        };
        connectorBoxedDRTA.getRTA().focusedProperty().addListener(connectorFocusListener);
    }

    /**
     * Initial step for branch layout
     * @param xVal initial x offset (usually zero)
     */
    void doLayout(double xVal) {
        if (isTornDown) return;
        leafPos = -48;
        setLayout(xVal);
    }

    /**
     * Set X,Y layout for this node, based recursively calling for children
     * @param xVal the X position for this node
     * @return the Y value
     */
    double setLayout(double xVal) {
        if (isTornDown || formulaBoxedDRTA == null || formulaBoxedDRTA.getRTA() == null) return Ylayout;
        Xlayout = xVal;
        if (dependents.isEmpty()) {
            leafPos = leafPos + offsetY;
            Ylayout = leafPos;
            if (!formulaNode) Ylayout -= formulaBoxHeight/2;
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
        if (isTornDown || pane == null || formulaBoxedDRTA == null || formulaBoxedDRTA.getRTA() == null) return;
        pane.getChildren().add(self);
        self.setLayoutX(Xlayout + offsetX);
        self.setLayoutY(Ylayout + offsetY);

        if (dotDivider) {
            Line dotLine = new Line(0,0,0,27);
            dotLine.getStrokeDashArray().addAll(1.0, 4.0);
            pane.getChildren().add(dotLine);
            dotLine.setLayoutX(self.Xlayout + formulaBoxedDRTA.getRTA().getPrefWidth() + annBump + rootBump + 13);
            dotLine.setLayoutY(self.Ylayout + formulaBoxHeight/2);
        }
        if (!dependents.isEmpty()) {
            if (dependents.get(0).isFormulaNode()) {
                offsetY = 0.0;
                if (dependents.size() == 1) {
                    HBox simpleConnector = connectorBoxedDRTA.getBoxedRTA();
                    pane.getChildren().add(simpleConnector);
                    BranchNode dependent = dependents.get(0);

                    simpleConnector.setLayoutX(Xlayout + formulaBoxedDRTA.getRTA().getPrefWidth() + annBump + rootBump + 10);
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
                if (dotDivider) dotsBump = 16;

                branchPane.setLayoutX(Xlayout + formulaBoxedDRTA.getRTA().getPrefWidth() +annBump + rootBump + dotsBump );
                branchPane.setLayoutY(topNode.getYLayout() + formulaBoxHeight + 1.5);
            }
        }

        // SCORCHED EARTH RULE 8: Defensive iteration over dependents
        List<BranchNode> safeDependents = new ArrayList<>(dependents);
        for (BranchNode node : safeDependents) {
            if (node != null) node.addToPane(pane, offsetX, offsetY);
        }
    }

    /**
     * BoxedDRTA for formula or connector field
     * @param isFormulaField true if this is for the formulaBoxedDRTA, false if connectorBoxedDRTA
     * @return the BoxedDRTA
     */
    private BoxedDRTA newFormulaBoxedDRTA(boolean isFormulaField) {
        BoxedDRTA boxedDRTA = new BoxedDRTA();

        DecoratedRTA drta = boxedDRTA.getDRTA();
        if (formulaNode) {
            drta.getKeyboardSelector().valueProperty().setValue(RichTextAreaSkin.KeyMapValue.SANS_AND_ITALIC);
        }
        else {
            if (!formulaNode) {drta.getKeyboardSelector().valueProperty().setValue(RichTextAreaSkin.KeyMapValue.ITALIC_AND_SANS);}
        }

        RichTextArea rta = boxedDRTA.getRTA();
        rta.setMaxHeight(formulaBoxHeight);
        rta.setMinHeight(formulaBoxHeight);

        RichTextAreaSkin rtaSkin = (RichTextAreaSkin) rta.getSkin();
        rta.prefWidthProperty().bind(Bindings.max(Bindings.add(rtaSkin.nodesWidthProperty(), 8), 12));

        ChangeListener<Number> nodesWidthL = (observable, oldValue, newValue) -> {
            if (isTornDown || this.horizontalTreeView == null) return;
            Platform.runLater(() -> {
                if (isTornDown) return;
                if (rta != null && rta.getParent() != null) rta.getParent().requestLayout();
            });
        };
        rtaSkin.nodesWidthProperty().addListener(nodesWidthL);

        ChangeListener<Number> prefWidthL = (ob, ov, nv) -> {
            if (isTornDown || this.horizontalTreeView == null) return;
            horizontalTreeView.refreshTreePanes();
        };
        rta.prefWidthProperty().addListener(prefWidthL);

        EventHandler<KeyEvent> keyFilter = e -> {
            if (isTornDown || this.horizontalTreeView == null || e == null) return;
            if (e.getCode() == KeyCode.ENTER) e.consume();
        };
        rta.addEventFilter(KeyEvent.ANY, keyFilter);

        // Save listeners to correct variables
        if (isFormulaField) {
            formNodesWidthListener = nodesWidthL;
            formPrefWidthListener = prefWidthL;
            formKeyFilter = keyFilter;
        } else {
            connNodesWidthListener = nodesWidthL;
            connPrefWidthListener = prefWidthL;
            connKeyFilter = keyFilter;
        }

        rta.getStylesheets().add("RichTextField.css");
        rta.setPromptText("");
        return boxedDRTA;
    }

    /**
     * The bracket for term branching
     * @return the pane
     */
    private Pane newTermBranch() {
        Pane branchPane = new Pane();
        if (isTornDown || dependents == null || dependents.isEmpty()) return branchPane;
        double top = dependents.get(0).getYLayout();
        double bottom  = dependents.get(dependents.size() - 1).getYLayout();
        double center = (bottom - top)/2;
        double xEnd = 31;
        if (dotDivider) xEnd = 24;

        List<BranchNode> safeDependents = new ArrayList<>(dependents);
        for (BranchNode node : safeDependents) {
            if (node != null) {
                Line line = new Line(0, center, xEnd, node.getYLayout() - top);
                branchPane.getChildren().add(line);
            }
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
        if (isTornDown) return new HBox();
        double height = bottom - top;
        if (connectorBoxedDRTA != null && connectorBoxedDRTA.getBoxedRTA() != null) {
            connectorBoxedDRTA.getBoxedRTA().setPadding(new Insets(0,0,6,0));
        }

        Group rtaBox = (connectorBoxedDRTA != null) ? new Group(connectorBoxedDRTA.getBoxedRTA()) : new Group();
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
        if (isTornDown) return;
        if (add) {
            if (!annotation) {
                addAnnotation();
                annBump = annotationWidth * .75 + 3;
            }
        }
        else {
            if (annotation) {
                if (annotationField != null && annotationField.getBoxedRTA() != null) {
                    self.getChildren().remove(annotationField.getBoxedRTA());
                }
                annotation = false;
                annBump = 0;
            }
        }
    }

    /**
     * Add annotation field (if not there already)
     */
    void addAnnotation() {
        if (isTornDown) return;
        if (!annotation) {
            if (annotationField != null && annotationField.getRTA() != null) {
                annotationField.getRTA().getActionFactory().newDocumentNow().execute(new ActionEvent());
                self.getChildren().add(annotationField.getBoxedRTA());
                self.setMargin(annotationField.getBoxedRTA(), new Insets(0, 0, .25 * 11.0, 0));
                annotation = true;

            }
        }
    }

    // SCORCHED EARTH RULE 6: Sever Shared Nodes helper
    private void severFromParent(Node node) {
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

    // SCORCHED EARTH RULE 5: Rule of Isolation
    public void cleanup() {
        // SCORCHED EARTH RULE 1: Engage kill switch
        isTornDown = true;

        // 1. Remove Focus & Key Listeners safely
        try {
            if (annotationField != null && annotationField.getRTA() != null) {
                if (annotationKeyFilter != null) annotationField.getRTA().removeEventFilter(KeyEvent.ANY, annotationKeyFilter);
                if (annotationFocusListener != null) annotationField.getRTA().focusedProperty().removeListener(annotationFocusListener);
            }
        } catch (Exception e) {}

        try {
            if (formulaBoxedDRTA != null && formulaBoxedDRTA.getRTA() != null) {
                RichTextArea rta = formulaBoxedDRTA.getRTA();
                if (formulaFocusListener != null) rta.focusedProperty().removeListener(formulaFocusListener);
                if (formPrefWidthListener != null) rta.prefWidthProperty().removeListener(formPrefWidthListener);
                if (formKeyFilter != null) rta.removeEventFilter(KeyEvent.ANY, formKeyFilter);
                if (rta.getSkin() instanceof RichTextAreaSkin) {
                    RichTextAreaSkin rtaSkin = (RichTextAreaSkin) rta.getSkin();
                    if (formNodesWidthListener != null) rtaSkin.nodesWidthProperty().removeListener(formNodesWidthListener);
                }
            }
        } catch (Exception e) {}

        try {
            if (connectorBoxedDRTA != null && connectorBoxedDRTA.getRTA() != null) {
                RichTextArea rta = connectorBoxedDRTA.getRTA();
                if (connectorFocusListener != null) rta.focusedProperty().removeListener(connectorFocusListener);
                if (connPrefWidthListener != null) rta.prefWidthProperty().removeListener(connPrefWidthListener);
                if (connKeyFilter != null) rta.removeEventFilter(KeyEvent.ANY, connKeyFilter);
                if (rta.getSkin() instanceof RichTextAreaSkin) {
                    RichTextAreaSkin rtaSkin = (RichTextAreaSkin) rta.getSkin();
                    if (connNodesWidthListener != null) rtaSkin.nodesWidthProperty().removeListener(connNodesWidthListener);
                }
            }
        } catch (Exception e) {}


        // 2. SCORCHED EARTH RULE 8: Cascade cleanup downwards BEFORE unbinding, using a defensive copy
        try {
            if (dependents != null) {
                List<BranchNode> dependentsCopy = new ArrayList<>(dependents);
                for (BranchNode child : dependentsCopy) {
                    if (child != null) {
                        try { child.cleanup(); } catch (Exception e) {}
                        severFromParent(child);
                    }
                }
                dependents.clear();
            }
        } catch (Exception e) {}

        // 3. Unbind and Skin Hammer
        try {
            final RichTextArea formRTA = (formulaBoxedDRTA != null) ? formulaBoxedDRTA.getRTA() : null;
            final RichTextArea connRTA = (connectorBoxedDRTA != null) ? connectorBoxedDRTA.getRTA() : null;
            final RichTextArea annRTA = (annotationField != null) ? annotationField.getRTA() : null;

            try { if (formRTA != null) formRTA.prefWidthProperty().unbind(); } catch(Exception e){}
            try { if (connRTA != null) connRTA.prefWidthProperty().unbind(); } catch(Exception e){}
            try { if (annRTA != null) annRTA.prefWidthProperty().unbind(); } catch(Exception e){}


            // STAGE 1: Execute document injection synchronously on empty RTAs before timer starts
            try {
                if (formRTA != null) {
                    formRTA.setPromptText("");
                    formRTA.setFocusTraversable(false);
                    if (formRTA.getDocument() == null || formRTA.getDocument().getText().isEmpty()) {
                        formRTA.getActionFactory().open(new Document(" ")).execute(new ActionEvent());
                    }
                }
            } catch (Exception ex) {}
            try {
                if (connRTA != null) {
                    connRTA.setPromptText("");
                    connRTA.setFocusTraversable(false);
                    if (connRTA.getDocument() == null || connRTA.getDocument().getText().isEmpty()) {
                        connRTA.getActionFactory().open(new Document(" ")).execute(new ActionEvent());
                    }
                }
            } catch (Exception ex) {}

            try {
                if (annRTA != null) {
                    annRTA.setPromptText("");
                    annRTA.setFocusTraversable(false);
                    if (annRTA.getDocument() == null || annRTA.getDocument().getText().isEmpty()) {
                        annRTA.getActionFactory().open(new Document(" ")).execute(new ActionEvent());
                    }
                }
            } catch (Exception ex) {}

            // STAGE 2: Delay setSkin(null) until background Gluon skin tasks finish
            if (formRTA != null || connRTA != null || annRTA != null) {
                PauseTransition pt = new PauseTransition(Duration.millis(1500));
                pt.setOnFinished(ev -> {
                    try { if (formRTA != null) formRTA.setSkin(null); } catch (Exception ex) {}
                    try { if (connRTA != null) connRTA.setSkin(null); } catch (Exception ex) {}
                    try { if (annRTA != null) annRTA.setSkin(null); } catch (Exception ex) {}
                });
                pt.play();
            }

        } catch (Exception e) {}

        // 4. Clear layout safely
        try {
            if (this.getChildren() != null) {
                List<Node> childrenCopy = new ArrayList<>(this.getChildren());
                for (Node n : childrenCopy) {
                    severFromParent(n);
                }
                this.getChildren().clear();
            }
        } catch (Exception e) {}

        severFromParent(this);

        // 5. Final severed ties
        try { this.self = null; } catch(Exception e){}
        try { this.container = null; } catch(Exception e){}
        try { this.horizontalTreeView = null; } catch(Exception e){}
        try { this.formulaBoxedDRTA = null; } catch(Exception e){}
        try { this.connectorBoxedDRTA = null; } catch(Exception e){}
        try { this.annotationField = null; } catch(Exception e){}
        try { this.dependents = null; } catch(Exception e){}
        try { this.formulaFocusListener = null; } catch(Exception e){}
        try { this.connectorFocusListener = null; } catch(Exception e){}
        try { this.annotationFocusListener = null; } catch(Exception e){}
        try { this.annotationKeyFilter = null; } catch(Exception e){}
        try { this.formNodesWidthListener = null; } catch(Exception e){}
        try { this.connNodesWidthListener = null; } catch(Exception e){}
        try { this.formPrefWidthListener = null; } catch(Exception e){}
        try { this.connPrefWidthListener = null; } catch(Exception e){}
        try { this.formKeyFilter = null; } catch(Exception e){}
        try { this.connKeyFilter = null; } catch(Exception e){}
        try { this.formulaPacket = null; } catch(Exception e){}
        try { this.termPacket = null; } catch(Exception e){}
    }

    public boolean isAnnotation() {    return annotation;  }
    void setAnnotation(boolean annotation) {   this.annotation = annotation; }
    public BoxedDRTA getAnnotationField() {return annotationField; }
    void setAnnBump(double annBump) {    this.annBump = annBump;   }
    double getAnnotationWidth() {     return annotationWidth;  }
    boolean isIndefiniteNode() {    return indefiniteNode;   }
    void setIndefiniteNode(boolean indefiniteNode) {    this.indefiniteNode = indefiniteNode;  }
    boolean isDotDivider() {    return dotDivider;  }
    void setDotDivider(boolean dotDivider) { this.dotDivider = dotDivider; }
    boolean isRoot() {     return root;  }
    public BoxedDRTA getFormulaBoxedDRTA() {  return formulaBoxedDRTA;  }
    public BoxedDRTA getConnectorBoxedDRTA() {     return connectorBoxedDRTA;  }
    BranchNode getContainer() {return container; }
    double getXLayout() { return Xlayout; }
    void setRoot(boolean root) {    this.root = root;}
    double getYLayout() { return Ylayout; }
    public ArrayList<BranchNode> getDependents() {  return dependents;  }
    void setRootBump(double rootBump) {    this.rootBump = rootBump;  }
    public boolean isFormulaNode() {  return formulaNode; }
    void setFormulaNode(boolean formulaNode) { this.formulaNode = formulaNode; }
    public double getFormPrintWidth() { return formPrintWidth; }
    public void setFormPrintWidth(double formPrintWidth) { this.formPrintWidth = formPrintWidth; }
    public double getConnPrintWidth() { return connPrintWidth; }
    public void setConnPrintWidth(double connPrintWidth) { this.connPrintWidth = connPrintWidth; }
    public FormulaPacket getFormulaPacket() { return formulaPacket; }
    public void setFormulaPacket(FormulaPacket formulaPacket) { this.formulaPacket = formulaPacket; }
    public TermPacket getTermPacket() { return termPacket; }
    public void setTermPacket(TermPacket termPacket) { this.termPacket = termPacket; }

}

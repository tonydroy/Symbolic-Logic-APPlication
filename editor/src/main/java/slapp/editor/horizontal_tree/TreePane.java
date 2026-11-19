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

import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.input.*;
import javafx.scene.layout.Pane;
import slapp.editor.EditorMain;
import slapp.editor.vertical_tree.drag_drop.DragContainer;

import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.Node;
import java.util.ArrayList;
import java.util.List;


/**
 * Pane containing a simple root branch node (and so single tree structure)
 */


public class TreePane extends Pane {

    TreePane self;
    HorizontalTreeView horizontalTreeView;
    BranchNode rootBranchNode;
    Label leftDragLabel;

    // KILL SWITCH: Prevents asynchronous listener execution during teardown
    private boolean isTornDown = false;

    // SCORCHED EARTH RULE 3: Eradicate Anonymous Lambdas
    private EventHandler<DragEvent> mContextDragOver;
    private EventHandler<DragEvent> mContextDragDropped;
    private EventHandler<MouseEvent> leftDragEnteredHandler;
    private EventHandler<MouseEvent> leftDragExitedHandler;
    private EventHandler<MouseEvent> leftDragDetectedHandler;

    private Point2D mDragOffset = new Point2D (0.0, 0.0);
    private boolean numberAxis = false;


    /**
     * Construct Tree Pane with a single root branch node
     * @param horizontalTreeView the horizontal tree view
     */
    TreePane(HorizontalTreeView horizontalTreeView) {
        self = this;
        this.horizontalTreeView = horizontalTreeView;
        rootBranchNode = new BranchNode(null, horizontalTreeView);
        rootBranchNode.setRoot(true);
        rootBranchNode.setRootBump(8.0);

        leftDragLabel = new Label("");
        leftDragLabel.setMaxWidth(10);
        leftDragLabel.setMinWidth(10);
        leftDragLabel.setPrefWidth(10);
        leftDragLabel.setMaxHeight(21);
        leftDragLabel.setMinHeight(21);
        leftDragLabel.setPadding(new Insets(0));

        leftDragEnteredHandler = e -> {
            if (isTornDown || this.self == null || leftDragLabel == null) return;
            leftDragLabel.setStyle("-fx-background-color: grey; -fx-background-radius: 5 0 0 5;");
        };
        leftDragLabel.setOnMouseEntered(leftDragEnteredHandler);

        leftDragExitedHandler = e -> {
            if (isTornDown || this.self == null || leftDragLabel == null) return;
            leftDragLabel.setStyle("-fx-background-color: transparent");
        };
        leftDragLabel.setOnMouseExited(leftDragExitedHandler);

        rootBranchNode.getChildren().add(0, leftDragLabel);
        rootBranchNode.setPadding(new Insets(0, 4, 0, 0));

        buildNodeDragHandlers();
    }

    /**
     * Redraw tree on pane, moving pane to keep rootBranchNode in the same visual position (if possible)
     */
    void refresh() {
        if (isTornDown || rootBranchNode == null || self == null) return;

        double startX = rootBranchNode.getLayoutX();
        double startY = rootBranchNode.getLayoutY();

        // SCORCHED EARTH RULE 8 FIX: Defensive UI clear WITHOUT cleanup(), because these nodes are active and just being redrawn
        if (this.getChildren() != null && !this.getChildren().isEmpty()) {
            List<Node> childrenCopy = new ArrayList<>(this.getChildren());
            for (Node n : childrenCopy) {
                severFromParent(n);
            }
            this.getChildren().clear();
        }

        rootBranchNode.doLayout(0);
        rootBranchNode.addToPane(self, 0, 0);

        double deltaX = startX - rootBranchNode.getXLayout();
        self.setLayoutX(self.getLayoutX() + deltaX);
        double deltaY = startY - rootBranchNode.getYLayout();
        self.setLayoutY(self.getLayoutY() + deltaY);

        double newY = self.getLayoutY();
        if (newY < 5.0) self.setLayoutY(5.0001);
    }

    /**
     * Processing for drag event
     */
    public void buildNodeDragHandlers() {

        //process drag event (keeping pane within upper bound of window)
        mContextDragOver = new EventHandler<DragEvent>() {
            @Override
            public void handle(DragEvent event) {
                if (isTornDown || self == null || getParent() == null || event == null) return;

                event.acceptTransferModes(TransferMode.ANY);

                if (event.getY() - self.getLayoutBounds().getHeight()/2 > 5.0) {
                    relocateToPoint(new Point2D(event.getSceneX(), event.getSceneY()));
                }
                else {
                    relocateToGridPoint(new Point2D(event.getX(),5.0001));
                }
                event.consume();
            }
        };

        //process drag dropped
        mContextDragDropped = new EventHandler <DragEvent> () {
            @Override
            public void handle(DragEvent event) {
                if (isTornDown || self == null || getParent() == null || horizontalTreeView == null || event == null) return;

                getParent().setOnDragOver(null);
                getParent().setOnDragDropped(null);
                event.setDropCompleted(true);

                relocateToPoint(new Point2D(event.getSceneX(), event.getSceneY()));

                horizontalTreeView.setUndoRedoFlag(true);
                horizontalTreeView.setUndoRedoFlag(false);

                self.setCursor(Cursor.DEFAULT);
            }
        };

        //for drag detected on left drag label
        leftDragDetectedHandler = new EventHandler <MouseEvent> () {
            @Override
            public void handle(MouseEvent event) {
                if (isTornDown || self == null || getParent() == null || event == null) return;

                getParent().setOnDragOver(null);
                getParent().setOnDragDropped(null);
                getParent().setOnDragOver (mContextDragOver);
                getParent().setOnDragDropped (mContextDragDropped);

                //begin drag ops

                if (self.getLayoutY() > 5.0) {
                    relocateToPoint(new Point2D(event.getSceneX(), event.getSceneY()));
                }
                else {
                    relocateToGridPoint(new Point2D(event.getX(), 5.001));
                }

                ClipboardContent content = new ClipboardContent();
                DragContainer container = new DragContainer();

                container.addData ("type", "formula box");
                content.put(DragContainer.AddNode, container);

                Dragboard db = startDragAndDrop(TransferMode.MOVE);
                db.setDragView(EditorMain.emptyImage);
                db.setContent(content);
                event.consume();
            }

        };
        leftDragLabel.setOnDragDetected (leftDragDetectedHandler);

    }

    /*
     * Given scene coordinates, relocate
     * @param p
     */
    private void relocateToPoint (Point2D p) {
        if (isTornDown || getParent() == null) return;
        Point2D localCoords = getParent().sceneToLocal(p);
        relocate ( (int) localCoords.getX(),  (int) ((localCoords.getY() - (getBoundsInLocal().getHeight()) / 2 ))  );
    }

    /**
     * Relocate to given coordinates in parent
     * @param p the point
     */
    void relocateToGridPoint(Point2D p) {
        if (isTornDown || p == null) return;
        relocate ((int) p.getX(), p.getY());
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

        // 1. Remove Handlers safely
        try {
            if (leftDragLabel != null) {
                if (leftDragEnteredHandler != null) leftDragLabel.removeEventHandler(MouseEvent.MOUSE_ENTERED, leftDragEnteredHandler);
                leftDragLabel.setOnMouseEntered(null);

                if (leftDragExitedHandler != null) leftDragLabel.removeEventHandler(MouseEvent.MOUSE_EXITED, leftDragExitedHandler);
                leftDragLabel.setOnMouseExited(null);

                if (leftDragDetectedHandler != null) leftDragLabel.removeEventHandler(MouseEvent.DRAG_DETECTED, leftDragDetectedHandler);
                leftDragLabel.setOnDragDetected(null);
            }
        } catch (Exception e) {}

        try {
            Node parentNode = (getParent() != null) ? getParent() : (horizontalTreeView != null ? horizontalTreeView.getMainPane() : null);
            if (parentNode != null) {
                if (mContextDragOver != null) parentNode.removeEventHandler(DragEvent.DRAG_OVER, mContextDragOver);
                parentNode.setOnDragOver(null);

                if (mContextDragDropped != null) parentNode.removeEventHandler(DragEvent.DRAG_DROPPED, mContextDragDropped);
                parentNode.setOnDragDropped(null);
            }
        } catch (Exception e) {}



        // 2. Cascade cleanup to nodes
        try {
            if (rootBranchNode != null) {
                rootBranchNode.cleanup();
            }
        } catch (Exception e) {}

        // 3. SCORCHED EARTH RULE 8: Clear layout
        try {
            if (this.getChildren() != null) {
                List<Node> childrenCopy = new ArrayList<>(this.getChildren());
                for (Node n : childrenCopy) {
                    severFromParent(n);
                }
                this.getChildren().clear();
            }
            severFromParent(this);
        } catch (Exception e) {}

        // 4. Null fields
        try { this.self = null; } catch (Exception e) {}
        try { this.horizontalTreeView = null; } catch (Exception e) {}
        try { this.rootBranchNode = null; } catch (Exception e) {}
        try { this.leftDragLabel = null; } catch (Exception e) {}
        try { this.mContextDragOver = null; } catch (Exception e) {}
        try { this.mContextDragDropped = null; } catch (Exception e) {}
        try { this.leftDragEnteredHandler = null; } catch (Exception e) {}
        try { this.leftDragExitedHandler = null; } catch (Exception e) {}
        try { this.leftDragDetectedHandler = null; } catch (Exception e) {}
        try { this.mDragOffset = null; } catch (Exception e) {}
    }

    /**
     * The branch node (containing the tree structure)
     * @return the node
     */
    public BranchNode getRootBranchNode() { return rootBranchNode; }
}
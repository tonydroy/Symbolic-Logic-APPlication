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

import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.chart.NumberAxis;
import javafx.scene.control.Label;
import javafx.scene.input.*;
import javafx.scene.layout.Pane;
import slapp.editor.EditorMain;
import slapp.editor.vertical_tree.drag_drop.DragContainer;
import slapp.editor.vertical_tree.drag_drop.Point2dSerial;

/**
 * Pane containing a simple root branch node (and so single tree structure)
 */
public class TreePane extends Pane {

    TreePane self;
    HorizontalTreeView horizontalTreeView;
    BranchNode rootBranchNode;
    Label leftDragLabel;
    private EventHandler<DragEvent> mContextDragOver;
    private EventHandler <DragEvent> mContextDragDropped;
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

        leftDragLabel.setOnMouseEntered(e -> {
            leftDragLabel.setStyle("-fx-background-color: grey; -fx-background-radius: 5 0 0 5;");
        });
        leftDragLabel.setOnMouseExited(e -> {
            leftDragLabel.setStyle("-fx-background-color: transparent");
        });

        rootBranchNode.getChildren().add(0, leftDragLabel);
        rootBranchNode.setPadding(new Insets(0, 4, 0, 0));

        buildNodeDragHandlers();
    }

    /**
     * Redraw tree on pane, moving pane to keep rootBranchNode in the same visual position (if possible)
     */
    void refresh() {

        double startX = rootBranchNode.getLayoutX();
        double startY = rootBranchNode.getLayoutY();

        this.getChildren().clear();
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

                event.acceptTransferModes(TransferMode.ANY);

                if (event.getY() - self.getLayoutBounds().getHeight()/2 > 5.0) {
                    relocateToPoint(new Point2dSerial(event.getSceneX(), event.getSceneY()));
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

                getParent().setOnDragOver(null);
                getParent().setOnDragDropped(null);
                event.setDropCompleted(true);

 //               relocateToGridPoint(new Point2D(event.getX(), event.getY()));
                relocateToPoint(new Point2D(event.getSceneX(), event.getSceneY()));

                horizontalTreeView.setUndoRedoFlag(true);
                horizontalTreeView.setUndoRedoFlag(false);

                self.setCursor(Cursor.DEFAULT);
            }
        };

        //for drag detected on left drag label
        leftDragLabel.setOnDragDetected ( new EventHandler <MouseEvent> () {
            @Override
            public void handle(MouseEvent event) {

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

        });

    }

    /*
     * Given scene coordinates, relocate
     * @param p
     */
    private void relocateToPoint (Point2D p) {
        Point2D localCoords = getParent().sceneToLocal(p);
        relocate ( (int) localCoords.getX(),  (int) ((localCoords.getY() - (getBoundsInLocal().getHeight()) / 2 ))  );
    }

    /**
     * Relocate to given coordinates in parent
     * @param p the point
     */
    void relocateToGridPoint(Point2D p) {
        relocate ((int) p.getX(), p.getY());
    }

    /**
     * The branch node (containing the tree structure)
     * @return the node
     */
    BranchNode getRootBranchNode() { return rootBranchNode; }



}

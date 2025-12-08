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

package slapp.editor.vertical_tree.drag_drop;


import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.*;
import javafx.scene.text.TextAlignment;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.UUID;

public class DraggableNode extends AnchorPane {

    private AnchorPane root_pane;
    private AnchorPane left_link_handle;
    private AnchorPane right_link_handle;
    private Label title_bar;
    private Label close_button;

    private EventHandler <MouseEvent> mLinkHandleDragDetected;
    private EventHandler <DragEvent> mLinkHandleDragDropped;
    private EventHandler <DragEvent> mContextLinkDragOver;
    private EventHandler <DragEvent> mContextLinkDragDropped;

    private EventHandler <DragEvent> mContextDragOver;
    private EventHandler <DragEvent> mContextDragDropped;

    private DragIconType mType = null;

    private Point2D mDragOffset = new Point2D (0.0, 0.0);

    private final DraggableNode self;

    private NodeLink mDragLink = null;
    private AnchorPane right_pane = null;

    private final List <String> mLinkIds = new ArrayList <String> ();

    public DraggableNode() {
        self = this;
        self.setPrefHeight(96); self.setPrefWidth(80);
        self.getStylesheets().add("/drag_drop.css");
        self.getStyleClass().add("dragicon");

 //       DragResizer.makeResizable(this);  works fine

        left_link_handle = new AnchorPane();
        left_link_handle.setPrefWidth(25);
        left_link_handle.getStyleClass().add("left-link-handle");
        AnchorPane node_body = new AnchorPane();
        node_body.setPrefWidth(30.0);
        right_link_handle = new AnchorPane();
        right_link_handle.setPrefWidth(25);
        right_link_handle.getStyleClass().add("right-link-handle");
        HBox bottomBox = new HBox(left_link_handle, node_body, right_link_handle);
        bottomBox.setPrefHeight(66.0);

        close_button = new Label("X");
        close_button.setPadding(new Insets(0,10,0,0));
        close_button.setAlignment(Pos.TOP_RIGHT);
        close_button.setTextAlignment(TextAlignment.RIGHT);
        title_bar = new Label("label");

        GridPane grid = new GridPane();
        grid.getStyleClass().add("node-overlay");
        ColumnConstraints columnConstraints1 = new ColumnConstraints();
        columnConstraints1.setHalignment(HPos.CENTER); columnConstraints1.setHgrow(Priority.SOMETIMES);
        ColumnConstraints columnConstraints2 = new ColumnConstraints();
        columnConstraints2.setHalignment(HPos.RIGHT); columnConstraints2.setHgrow(Priority.SOMETIMES); columnConstraints2.setMaxWidth(25);
        grid.getColumnConstraints().addAll(columnConstraints1, columnConstraints2);
        RowConstraints rowConstraints = new RowConstraints();
        rowConstraints.setMinHeight(10); rowConstraints.setPrefHeight(30); rowConstraints.setVgrow(Priority.SOMETIMES);
        grid.getRowConstraints().add(rowConstraints);
        grid.add(title_bar, 0, 0); grid.add(close_button, 1, 0);
        VBox mainBox = new VBox(grid, bottomBox);

        self.getChildren().add(mainBox);
        self.setBottomAnchor(mainBox, 0.0); self.setLeftAnchor(mainBox, 0.0); self.setTopAnchor(mainBox, 0.0); self.setRightAnchor(mainBox, 0.0);


        //provide a universally unique identifier for this object
        setId(UUID.randomUUID().toString());

        initialize();

    }


    private void initialize() {

        buildNodeDragHandlers();
        buildLinkDragHandlers();

        left_link_handle.setOnDragDetected(mLinkHandleDragDetected);
        right_link_handle.setOnDragDetected(mLinkHandleDragDetected);

        left_link_handle.setOnDragDropped(mLinkHandleDragDropped);
        right_link_handle.setOnDragDropped(mLinkHandleDragDropped);

        mDragLink = new NodeLink();
        mDragLink.setVisible(false);

        parentProperty().addListener(new ChangeListener() {

            @Override
            public void changed(ObservableValue observable,
                                Object oldValue, Object newValue) {
                right_pane = (AnchorPane) getParent();

            }

        });

        /*  What is this for?? appears as black circle in upper left of draggable node

        Circle c =new Circle();
        c.setRadius(5.0f);
        this.getChildren().add(c);

         */

    }

    public void registerLink(String linkId) {
        mLinkIds.add(linkId);
    }

    public void relocateToPoint (Point2D p) {

        //relocates the object to a point that has been converted to
        //scene coordinates
        Point2D localCoords = getParent().sceneToLocal(p);

        relocate (
                (int) (localCoords.getX() - mDragOffset.getX()),
                (int) (localCoords.getY() - mDragOffset.getY())
        );
    }

    public DragIconType getType () { return mType; }

    public void setType (DragIconType type) {

        mType = type;

        getStyleClass().clear();
        getStyleClass().add("dragicon");


    }

    public void buildNodeDragHandlers() {

        mContextDragOver = new EventHandler <DragEvent>() {

            //dragover to handle node dragging in the right pane view
            @Override
            public void handle(DragEvent event) {

                event.acceptTransferModes(TransferMode.ANY);
                relocateToPoint(new Point2dSerial( event.getSceneX(), event.getSceneY()));

                event.consume();
            }
        };

        //dragdrop for node dragging
        mContextDragDropped = new EventHandler <DragEvent> () {

            @Override
            public void handle(DragEvent event) {

                getParent().setOnDragOver(null);
                getParent().setOnDragDropped(null);

                event.setDropCompleted(true);

                event.consume();
            }
        };

        //close button click
        close_button.setOnMouseClicked( new EventHandler <MouseEvent> () {

            @Override
            public void handle(MouseEvent event) {
                AnchorPane parent  = (AnchorPane) self.getParent();
                parent.getChildren().remove(self);

                //iterate each link id connected to this node
                //find it's corresponding component in the right-hand
                //AnchorPane and delete it.

                //Note:  other nodes connected to these links are not being
                //notified that the link has been removed.
                for (ListIterator <String> iterId = mLinkIds.listIterator();
                     iterId.hasNext();) {

                    String id = iterId.next();

                    for (ListIterator <Node> iterNode = parent.getChildren().listIterator();
                         iterNode.hasNext();) {

                        Node node = iterNode.next();

                        if (node.getId() == null)
                            continue;

                        if (node.getId().equals(id))
                            iterNode.remove();
                    }

                    iterId.remove();
                }
            }

        });

        //drag detection for node dragging
        title_bar.setOnDragDetected ( new EventHandler <MouseEvent> () {

            @Override
            public void handle(MouseEvent event) {

                getParent().setOnDragOver(null);
                getParent().setOnDragDropped(null);

                getParent().setOnDragOver (mContextDragOver);
                getParent().setOnDragDropped (mContextDragDropped);

                //begin drag ops
                mDragOffset = new Point2D(event.getX(), event.getY());

                relocateToPoint(
                        new Point2D(event.getSceneX(), event.getSceneY())
                );

                ClipboardContent content = new ClipboardContent();
                DragContainer container = new DragContainer();

                container.addData ("type", mType.toString());
                content.put(DragContainer.AddNode, container);

                startDragAndDrop (TransferMode.ANY).setContent(content);

                event.consume();
            }

    });
    }

    private void buildLinkDragHandlers() {

        mLinkHandleDragDetected = new EventHandler <MouseEvent> () {

            @Override
            public void handle(MouseEvent event) {

                getParent().setOnDragOver(null);
                getParent().setOnDragDropped(null);

                getParent().setOnDragOver(mContextLinkDragOver);
                getParent().setOnDragDropped(mContextLinkDragDropped);

                //Set up user-draggable link
                right_pane.getChildren().add(0,mDragLink);

                mDragLink.setVisible(false);

                Point2D p = new Point2D(
                        getLayoutX() + (getWidth() / 2.0),
                        getLayoutY() + (getHeight() / 2.0)
                );

                mDragLink.setStart(p);

                //Drag content code
                ClipboardContent content = new ClipboardContent();
                DragContainer container = new DragContainer();


                //pass the UUID of the source node for later lookup
                container.addData("source", getId());

                content.put(DragContainer.AddLink, container);

                startDragAndDrop (TransferMode.ANY).setContent(content);

                event.consume();
            }
        };

        mLinkHandleDragDropped = new EventHandler <DragEvent> () {

            @Override
            public void handle(DragEvent event) {

                getParent().setOnDragOver(null);
                getParent().setOnDragDropped(null);

                //get the drag data.  If it's null, abort.
                //This isn't the drag event we're looking for.
                DragContainer container =
                        (DragContainer) event.getDragboard().getContent(DragContainer.AddLink);

                if (container == null)
                    return;

                //hide the draggable NodeLink and remove it from the right-hand AnchorPane's children
                mDragLink.setVisible(false);
                right_pane.getChildren().remove(0);

                AnchorPane link_handle = (AnchorPane) event.getSource();

                ClipboardContent content = new ClipboardContent();

                //pass the UUID of the target node for later lookup
                container.addData("target", getId());

                content.put(DragContainer.AddLink, container);

                event.getDragboard().setContent(content);
                event.setDropCompleted(true);
                event.consume();
            }
        };

        mContextLinkDragOver = new EventHandler <DragEvent> () {

            @Override
            public void handle(DragEvent event) {
                event.acceptTransferModes(TransferMode.ANY);

                //Relocate end of user-draggable link
                if (!mDragLink.isVisible())
                    mDragLink.setVisible(true);

                mDragLink.setEnd(new Point2D(event.getX(), event.getY()));

                event.consume();

            }
        };

        //drop event for link creation
        mContextLinkDragDropped = new EventHandler <DragEvent> () {

            @Override
            public void handle(DragEvent event) {


                getParent().setOnDragOver(null);
                getParent().setOnDragDropped(null);

                //hide the draggable NodeLink and remove it from the right-hand AnchorPane's children
                mDragLink.setVisible(false);
                right_pane.getChildren().remove(0);

                event.setDropCompleted(true);
                event.consume();
            }

        };

    }


}

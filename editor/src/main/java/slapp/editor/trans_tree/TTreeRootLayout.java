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

package slapp.editor.trans_tree;


import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import slapp.editor.EditorMain;
import slapp.editor.vertical_tree.drag_drop.DragContainer;
import slapp.editor.vertical_tree.drag_drop.DragIcon;
import slapp.editor.vertical_tree.drag_drop.DragIconType;
import slapp.editor.vertical_tree.drag_drop.DraggableNode;

import static slapp.editor.EditorMain.RTA_WIDTH_BMP;

public class TTreeRootLayout extends AnchorPane {

    TransTreeView transTreeView;
    SplitPane base_pane;
    AnchorPane main_pane;
    HBox top_pane;
    EventHandler underlineClickFilter;

    private DragIcon mDragOverIcon = null;
    private EventHandler<DragEvent> mIconDragOverRoot = null;
    private EventHandler<DragEvent> mIconDragDropped = null;
    private EventHandler<DragEvent> mIconDragOverRightPane = null;

    public TTreeRootLayout(TransTreeView transTreeView) {
        this.transTreeView = transTreeView;
        top_pane = new HBox();
        main_pane = new AnchorPane();
        base_pane = new SplitPane();
        base_pane.getItems().addAll(top_pane, main_pane);
        base_pane.setOrientation(Orientation.VERTICAL);
        this.getChildren().add(base_pane);

        setupWindow();
        initialize();

    }

    private void setupWindow() {
        top_pane.setStyle("-fx-background-color: #FCFCFC;");
        top_pane.setPadding(new Insets(0, 0, 0, 20));

        this.getStylesheets().add("/drag_drop.css");
        main_pane.setStyle("-fx-background-color: white," +
                "linear-gradient(from 0.5px 0.0px to 24.5px  0.0px, repeat, #f5f5f5 1%, transparent 5%)," +
                "linear-gradient(from 0.0px 0.5px to  0.0px 24.5px, repeat, #f5f5f5 1%, transparent 5%);");
        main_pane.setPadding(new Insets(5,RTA_WIDTH_BMP,20,5));

        this.setMinWidth(200); this.setMinHeight(100);
        this.setBottomAnchor(base_pane, 0.0); this.setTopAnchor(base_pane, 0.0); this.setLeftAnchor(base_pane, 0.0); this.setRightAnchor(base_pane, 0.0);

        top_pane.setSpacing(40);
        top_pane.setAlignment(Pos.CENTER);
        top_pane.setMinHeight(32);
        top_pane.setMaxHeight(32);

        base_pane.setResizableWithParent(main_pane, true);



    }

    public void initialize() {


        //Add one icon that will be used for the drag-drop process
        //This is added as a child to the root anchorpane so it can be visible
        //on both sides of the split pane.
        mDragOverIcon = new DragIcon();

        mDragOverIcon.setVisible(false);
        mDragOverIcon.setOpacity(0.65);
        getChildren().add(mDragOverIcon);

        buildDragHandlers();
    }

    public void addDragIcon(DragIconType type) {
        DragIcon icn = new DragIcon();
        addDragDetection(icn);
        icn.setType(type);
        top_pane.getChildren().add(icn);
    }

    public static boolean inHierarchy(Node node, Node potentialHierarchyElement) {
        if (potentialHierarchyElement == null) {
            return true;
        }
        while (node != null) {
            if (node == potentialHierarchyElement) {
                return true;
            }
            node = node.getParent();
        }
        return false;
    }

    private void addDragDetection(DragIcon dragIcon) {
        dragIcon.setOnDragDetected (new EventHandler <MouseEvent> () {
            @Override
            public void handle(MouseEvent event) {

                // set drag event handlers on their respective objects
                base_pane.setOnDragOver(mIconDragOverRoot);
                main_pane.setOnDragOver(mIconDragOverRightPane);
                main_pane.setOnDragDropped(mIconDragDropped);

                // get a reference to the clicked DragIcon object
                DragIcon icn = (DragIcon) event.getSource();

                //begin drag ops
                mDragOverIcon.setType(icn.getType());
                mDragOverIcon.relocateToPoint(new Point2D (event.getSceneX(), event.getSceneY()));

                ClipboardContent content = new ClipboardContent();
                DragContainer container = new DragContainer();

                container.addData ("type", mDragOverIcon.getType().toString());
                content.put(DragContainer.AddNode, container);

                Dragboard db = mDragOverIcon.startDragAndDrop(TransferMode.MOVE);
                db.setDragView(EditorMain.emptyImage);
                db.setContent(content);
    //            mDragOverIcon.startDragAndDrop (TransferMode.ANY).setContent(content);

                mDragOverIcon.setVisible(true);
                mDragOverIcon.setMouseTransparent(true);
                event.consume();
            }
        });
    }

    private void buildDragHandlers() {

        //drag over transition to move widget form left pane to right pane
        mIconDragOverRoot = new EventHandler <DragEvent>() {
            @Override
            public void handle(DragEvent event) {

                Point2D p = main_pane.sceneToLocal(event.getSceneX(), event.getSceneY());

                //turn on transfer mode and track in the right-pane's context
                //if (and only if) the mouse cursor falls within the right pane's bounds.
                if (!main_pane.boundsInLocalProperty().get().contains(p)) {

                    event.acceptTransferModes(TransferMode.ANY);
                    mDragOverIcon.relocateToPoint(new Point2D(event.getSceneX(), event.getSceneY()));
                    return;
                }

                event.consume();
            }
        };

        mIconDragOverRightPane = new EventHandler <DragEvent> () {
            @Override
            public void handle(DragEvent event) {

                event.acceptTransferModes(TransferMode.MOVE);

                //convert the mouse coordinates to scene coordinates,
                //then convert back to coordinates that are relative to
                //the parent of mDragIcon.  Since mDragIcon is a child of the root
                //pane, coodinates must be in the root pane's coordinate system to work
                //properly.
                mDragOverIcon.relocateToPoint(
                        new Point2D(event.getSceneX(), event.getSceneY())
                );
                event.consume();
            }
        };

        mIconDragDropped = new EventHandler <DragEvent> () {
            @Override
            public void handle(DragEvent event) {

                DragContainer container =
                        (DragContainer) event.getDragboard().getContent(DragContainer.AddNode);

                container.addData("scene_coords",
                        new Point2D(event.getSceneX(), event.getSceneY()));

                ClipboardContent content = new ClipboardContent();
                content.put(DragContainer.AddNode, container);

                event.getDragboard().setContent(content);
                event.setDropCompleted(true);

            }
        };

        this.setOnDragDone (new EventHandler <DragEvent> (){
            @Override
            public void handle (DragEvent event) {

                main_pane.removeEventHandler(DragEvent.DRAG_OVER, mIconDragOverRightPane);
                main_pane.removeEventHandler(DragEvent.DRAG_DROPPED, mIconDragDropped);
                base_pane.removeEventHandler(DragEvent.DRAG_OVER, mIconDragOverRoot);

                mDragOverIcon.setVisible(false);
                //stop drag icon from preventing resize- by moving icon out of main pane
                mDragOverIcon.relocateToPoint(  new Point2D(0, 0) );

                //Create node drag operation
                DragContainer container =
                        (DragContainer) event.getDragboard().getContent(DragContainer.AddNode);

                if (container != null) {
                    if (container.getValue("scene_coords") != null) {

                        if (container.getValue("type").equals(DragIconType.tree_field.toString())) {
                            FormulaTreeBox formulaTreeBox = new FormulaTreeBox(transTreeView.getTransTreeExercise());
                            main_pane.getChildren().add(formulaTreeBox);
                            Point2D cursorPoint = container.getValue("scene_coords");
                            formulaTreeBox.relocateToGridPoint(new Point2D(cursorPoint.getX(), cursorPoint.getY()));
                            transTreeView.setUndoRedoFlag(true);
                            transTreeView.setUndoRedoFlag(false);
                        }

                        else if (container.getValue("type").equals(DragIconType.parse_field.toString())) {
                            ParseTreeBox parseTreeBox = new ParseTreeBox(transTreeView.getTransTreeExercise());
                            main_pane.getChildren().add(parseTreeBox);
                            Point2D cursorPoint = container.getValue("scene_coords");
                            parseTreeBox.relocateToGridPoint(new Point2D(cursorPoint.getX(), cursorPoint.getY()));
                            transTreeView.setUndoRedoFlag(true);
                            transTreeView.setUndoRedoFlag(false);
                            parseTreeBox.formulaBox.getRTA().textLengthProperty().addListener((ob, ov, nv) -> {
                                parseTreeBox.clearLines();
                            });

                        }

                        else {

                            DraggableNode node = new DraggableNode();
                            node.setType(DragIconType.valueOf(container.getValue("type")));
                            main_pane.getChildren().add(node);
                            Point2D cursorPoint = container.getValue("scene_coords");
                            node.relocateToPoint(
                                    new Point2D(cursorPoint.getX() - 32, cursorPoint.getY() - 32)
                            );
                        }
                    }
                }


                //AddLink drag operation
                container =
                        (DragContainer) event.getDragboard().getContent(DragContainer.AddLink);

                if (container != null) {

                    //bind the ends of our link to the nodes whose id's are stored in the drag container
                    String sourceId = container.getValue("source");
                    String targetId = container.getValue("target");

                    if (sourceId != null && targetId != null && !sourceId.equals(targetId)) {

     //                   //	System.out.println(container.getData());
     //                  NodeLink link = new NodeLink();
                        TTClickableNodeLink link = new TTClickableNodeLink(transTreeView);


                        //add our link at the top of the rendering order so it's rendered first
                        main_pane.getChildren().add(0,link);

                        TTreeBox source = null;
                        TTreeBox target = null;

                        for (Node n: main_pane.getChildren()) {

                            if (n.getId() == null)
                                continue;

                            if (n.getId().equals(sourceId))
                                source = (TTreeBox) n;

                            if (n.getId().equals(targetId))
                                target = (TTreeBox) n;



                        }

                        if (source != null && target != null && ((source instanceof ParseTreeBox && target instanceof ParseTreeBox) || source instanceof FormulaTreeBox && target instanceof FormulaTreeBox)) {
                            link.bindEnds(source, target);
                            transTreeView.setUndoRedoFlag(true);
                            transTreeView.setUndoRedoFlag(false);
                        }
                    }

                }

                event.consume();
            }
        });
    }

    public SplitPane getBase_pane() {
        return base_pane;
    }

    public AnchorPane getMain_pane() {
        return main_pane;
    }

    public HBox getTop_pane() {
        return top_pane;
    }







}
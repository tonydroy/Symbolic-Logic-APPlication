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

package slapp.editor.vert_tree_explain;


import javafx.collections.ObservableList;
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
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import slapp.editor.EditorAlerts;
//import slapp.editor.vertical_tree.drag_drop.*;
import slapp.editor.EditorMain;
import slapp.editor.vertical_tree.drag_drop.DragIcon;
import slapp.editor.vertical_tree.drag_drop.DragIconType;
import slapp.editor.vertical_tree.drag_drop.DragContainer;
import slapp.editor.vertical_tree.drag_drop.DraggableNode;

import java.util.ArrayList;
import java.util.List;

public class ExpRootLayout extends AnchorPane {

    VerticalTreeExpView verticalTreeView;
    SplitPane base_pane;
    AnchorPane main_pane;
    HBox top_pane;


    EventHandler boxClickFilter;
    EventHandler starClickFilter;
    EventHandler annotationClickFilter;
    EventHandler circleClickFilter;
    EventHandler underlineClickFilter;
    EventHandler mappingClickFilter;
    EventHandler mappingKeyFilter;
    ToggleButton boxToggle;
    ToggleButton starToggle;
    ToggleButton annotationToggle;
    Button annotationPlus;
    Button annotationMinus;
    HBox annotationBox;
    ToggleButton circleToggle;
    ToggleButton underlineToggle;
    ToggleButton mappingToggle;
    ToggleGroup buttonGroup;

    private DragIcon mDragOverIcon = null;
    private EventHandler<DragEvent> mIconDragOverRoot = null;
    private EventHandler<DragEvent> mIconDragDropped = null;
    private EventHandler<DragEvent> mIconDragOverRightPane = null;

    public ExpRootLayout(VerticalTreeExpView verticalTreeView) {
        this.verticalTreeView = verticalTreeView;
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
 //       RightDragResizer.makeResizable(base_pane);
     //   BottomDragResizer.makeResizable(base_pane);
        top_pane.setStyle("-fx-background-color: #FCFCFC;");
        top_pane.setPadding(new Insets(0, 0, 0, 20));

        boxToggle = new ToggleButton();
        boxToggle.setPrefWidth(64);
        boxToggle.setPrefHeight(28);
        boxToggle.setMinHeight(28);
        boxToggle.setMaxHeight(28);
        Rectangle rectangle = new Rectangle(20,15);
        rectangle.setStyle("-fx-stroke: black; -fx-stroke-width: 1.5; -fx-fill: transparent;");
        HBox boxToggleGraphic = new HBox(rectangle);
        boxToggleGraphic.setAlignment(Pos.CENTER);
        boxToggle.setGraphic(boxToggleGraphic);
        boxToggle.setTooltip(new Tooltip("Add (left click) or remove (right click) box"));


        starToggle = new ToggleButton("\uf005");  //LineAwsomeSolid.STAR
        starToggle.getStyleClass().add("lasolid-icon");
        starToggle.setPrefWidth(64);
        starToggle.setPrefHeight(28);
        starToggle.setStyle("-fx-font-size: 20");
        starToggle.setTooltip(new Tooltip("Add (left click) or remove (right click) star"));

        annotationToggle = new ToggleButton();
        annotationToggle.setPrefWidth(44);
        annotationToggle.setPrefHeight(30);
        annotationToggle.setMinHeight(30);
        annotationToggle.setMaxHeight(30);
        AnchorPane boxesPane = new AnchorPane();
        boxesPane.setPadding(new Insets(0));
        Rectangle bigBox = new Rectangle(15,10);
        bigBox.setStyle("-fx-stroke: black; -fx-stroke-width: 1.5; -fx-fill: transparent;");
        Rectangle littleBox = new Rectangle(7,7);
        littleBox.setStyle("-fx-stroke: black; -fx-stroke-width: 1.5; -fx-fill: transparent;");
        boxesPane.getChildren().addAll(bigBox, littleBox);
        boxesPane.setTopAnchor(bigBox, 8.0);
        boxesPane.setLeftAnchor(littleBox, 15.0);
        boxesPane.setTopAnchor(littleBox, 3.0);
        HBox buttonBox = new HBox(boxesPane);
        buttonBox.setAlignment(Pos.CENTER);
        annotationToggle.setGraphic(buttonBox);
        annotationToggle.setTooltip(new Tooltip("Add (left click) or remove (right click) annotation box"));

        annotationPlus = new Button("+");
        annotationPlus.setFont(new Font(10));
        annotationPlus.setPadding(new Insets(0));
        annotationPlus.setPrefWidth(20); annotationPlus.setPrefHeight(12);
        annotationMinus = new Button("-");
        annotationMinus.setFont(new Font(10));
        annotationMinus.setPadding(new Insets(0));
        annotationMinus.setPrefWidth(20); annotationMinus.setPrefHeight(12);
        VBox annotationButtons = new VBox(annotationPlus, annotationMinus);
        annotationBox = new HBox(annotationToggle, annotationButtons);
        annotationBox.setMaxHeight(30);
        annotationBox.setMaxWidth(64);


        circleToggle = new ToggleButton();
        circleToggle.setPrefWidth(64);
        circleToggle.setPrefHeight(28);
        circleToggle.setMinHeight(28);
        circleToggle.setMaxHeight(28);
        HBox circlePane = new HBox();
        Circle circleIcon = new Circle(7);
        circleIcon.setStyle("-fx-stroke: black; -fx-stroke-width: 1.5; -fx-fill: transparent;");
        circlePane.getChildren().add(circleIcon);
        circlePane.setAlignment(Pos.CENTER);
        circleToggle.setGraphic(circlePane);
        circleToggle.setTooltip(new Tooltip("Add (left click) or remove (right click) circle"));

        underlineToggle = new ToggleButton("\uf7a4");  //LineAwesomeSolid.GRIP_LINES
        underlineToggle.getStyleClass().add("lasolid-icon");
        underlineToggle.setPrefWidth(64);
        underlineToggle.setPrefHeight(28);
        underlineToggle.setTooltip(new Tooltip("Add (left click) or remove (right click) underline"));

        mappingToggle = new ToggleButton();
        mappingToggle.setGraphic(new Text("\u21a7"));
        mappingToggle.setPadding(new Insets(0,0,3,0));
        mappingToggle.setStyle("-fx-font-family: Noto Serif Combo; -fx-font-size: 30");
        mappingToggle.setPrefWidth(64);
        mappingToggle.setPrefHeight(28);
        mappingToggle.setMinHeight(28);
        mappingToggle.setMaxHeight(28);
        mappingToggle.setTooltip(new Tooltip("Add (left click) or remove (right click) map lines"));



        buttonGroup = new ToggleGroup();



        this.getStylesheets().add("/drag_drop.css");
        main_pane.setStyle("-fx-background-color: white," +
                "linear-gradient(from 0.5px 0.0px to 24.5px  0.0px, repeat, #f5f5f5 1%, transparent 5%)," +
                "linear-gradient(from 0.0px 0.5px to  0.0px 24.5px, repeat, #f5f5f5 1%, transparent 5%);");
        main_pane.setPadding(new Insets(5,20,20,5));




        this.setMinWidth(200); this.setMinHeight(100);
        this.setBottomAnchor(base_pane, 0.0); this.setTopAnchor(base_pane, 0.0); this.setLeftAnchor(base_pane, 0.0); this.setRightAnchor(base_pane, 0.0);

        top_pane.setSpacing(20);
        top_pane.setAlignment(Pos.CENTER);
        top_pane.setMinHeight(32);
        top_pane.setMaxHeight(32);

        base_pane.setResizableWithParent(main_pane, true);


        boxClickFilter = new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                ObservableList<Node> nodesList = main_pane.getChildren();
                for (Node node : nodesList) {
                    if (inHierarchy(event.getPickResult().getIntersectedNode(), node)) {
                        if (node instanceof ExpTreeFormulaBox) {
                            ((ExpTreeFormulaBox) node).processBoxRequest(event.getButton() == MouseButton.PRIMARY);
                            break;
                        }
                    }
                }
 //               event.consume();
            }
        };
        boxToggle.selectedProperty().addListener((ob, ov, nv) -> {
            if (nv)  main_pane.addEventFilter(MouseEvent.MOUSE_PRESSED, boxClickFilter );
            else main_pane.removeEventFilter(MouseEvent.MOUSE_PRESSED, boxClickFilter);
        });

        starClickFilter = new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                ObservableList<Node> nodesList = main_pane.getChildren();
                for (Node node : nodesList) {
                    if (inHierarchy(event.getPickResult().getIntersectedNode(), node)) {
                        if (node instanceof ExpTreeFormulaBox) {
                            ((ExpTreeFormulaBox) node).processStarRequest(event.getButton() == MouseButton.PRIMARY);
                            break;
                        }
                    }
                }
 //               event.consume();
            }
        };
        starToggle.selectedProperty().addListener((ob, ov, nv) -> {
            if (nv)  main_pane.addEventFilter(MouseEvent.MOUSE_PRESSED, starClickFilter );
            else main_pane.removeEventFilter(MouseEvent.MOUSE_PRESSED, starClickFilter);
        });

        annotationClickFilter = new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                ObservableList<Node> nodesList = main_pane.getChildren();
                for (Node node : nodesList) {
                    if (inHierarchy(event.getPickResult().getIntersectedNode(), node)) {
                        if (node instanceof ExpTreeFormulaBox) {
                            ((ExpTreeFormulaBox) node).processAnnotationRequest(event.getButton() == MouseButton.PRIMARY);
                            break;
                        }
                    }
                }
  //              event.consume();
            }
        };
        annotationToggle.selectedProperty().addListener((ob, ov, nv) -> {
            if (nv)  main_pane.addEventFilter(MouseEvent.MOUSE_PRESSED, annotationClickFilter );
            else main_pane.removeEventFilter(MouseEvent.MOUSE_PRESSED, annotationClickFilter);
        });

        annotationPlus.setOnAction(e -> {
            ObservableList<Node> nodesList = main_pane.getChildren();
            for (Node node : nodesList) {
                if (node instanceof ExpTreeFormulaBox) {
                    ((ExpTreeFormulaBox) node).processAnnotationRequest(true);
                }
            }
            annotationToggle.setSelected(false);
        });

        annotationMinus.setOnAction(e -> {
           ObservableList<Node> nodesList = main_pane.getChildren();
           for (Node node : nodesList) {
               if (node instanceof ExpTreeFormulaBox) {
                   ((ExpTreeFormulaBox) node).processAnnotationRequest(false);
               }
           }
            annotationToggle.setSelected(false);
        });

        circleClickFilter = new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                ObservableList<Node> nodesList = main_pane.getChildren();
                for (Node node : nodesList) {
                    if (inHierarchy(event.getPickResult().getIntersectedNode(), node)) {
                        if (node instanceof ExpTreeFormulaBox) {
                            ((ExpTreeFormulaBox) node).processCircleRequest(event.getButton() == MouseButton.PRIMARY);
                            break;
                        }
                    }
                }
 //               event.consume();
            }
        };
        circleToggle.selectedProperty().addListener((ob, ov, nv) -> {
            if (nv)  main_pane.addEventFilter(MouseEvent.MOUSE_PRESSED, circleClickFilter );
            else {
                main_pane.removeEventFilter(MouseEvent.MOUSE_PRESSED, circleClickFilter);
                ObservableList<Node> nodesList = main_pane.getChildren();
                for (Node node : nodesList) {
                    if (node instanceof ExpTreeFormulaBox) {
                        ((ExpTreeFormulaBox) node).undoCircleRequest();
                    }
                }
            }
        });

        underlineClickFilter = new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                ObservableList<Node> nodesList = main_pane.getChildren();
                for (Node node : nodesList) {
                    if (inHierarchy(event.getPickResult().getIntersectedNode(), node)) {
                        if (node instanceof ExpTreeFormulaBox) {
                            ((ExpTreeFormulaBox) node).processUnderlineRequest(event.getButton() == MouseButton.PRIMARY);
                            break;
                        }
                    }
                }
//               event.consume();
            }
        };
        underlineToggle.selectedProperty().addListener((ob, ov, nv) -> {
            if (nv)  main_pane.addEventFilter(MouseEvent.MOUSE_PRESSED, underlineClickFilter );
            else {
                main_pane.removeEventFilter(MouseEvent.MOUSE_PRESSED, underlineClickFilter);
                ObservableList<Node> nodesList = main_pane.getChildren();
                for (Node node : nodesList) {
                    if (node instanceof ExpTreeFormulaBox) {
                        ((ExpTreeFormulaBox) node).undoUnderlineRequest();
                    }
                }
            }
        });

        mappingClickFilter = new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                ObservableList<Node> nodesList = main_pane.getChildren();
                for (Node node : nodesList) {
                    if (inHierarchy(event.getPickResult().getIntersectedNode(), node)) {
                        if (node instanceof ExpMapFormulaBox) {
                            ((ExpMapFormulaBox) node).processMappingRequest(event.getButton() == MouseButton.PRIMARY);
                            break;
                        }
                    }
                }
 //               event.consume();
            }
        };
        mappingKeyFilter = new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent e) {
                KeyCode code = e.getCode();
                if (code == KeyCode.F11) {
                    ObservableList<Node> nodesList = main_pane.getChildren();
                    List<ExpMapFormulaBox> formulaBoxes = new ArrayList<>();
                    for (Node node : nodesList) {
                        if (node instanceof ExpMapFormulaBox) {
                            ExpMapFormulaBox mapFormulaBox = (ExpMapFormulaBox) node;
                            if (mapFormulaBox.getMapStage() > 0) {
                                formulaBoxes.add(mapFormulaBox);
                            }
                        }
                    }
                    if (formulaBoxes.size() == 2) {
                        ExpClickableMapLink mapLink = new ExpClickableMapLink(verticalTreeView);
                        main_pane.getChildren().add(0, mapLink);
                        mapLink.bindEnds(formulaBoxes.get(0), formulaBoxes.get(1));
                        verticalTreeView.setUndoRedoFlag(true);
                        verticalTreeView.setUndoRedoFlag(false);
                        formulaBoxes.get(0).undoMappingRequest();
                        formulaBoxes.get(1).undoMappingRequest();
                    } else {
                        EditorAlerts.fleetingRedPopup("Map requires two marked nodes.");
                    }
                    e.consume();
                } else if (code == KeyCode.F12) {
                    ObservableList<Node> nodesList = main_pane.getChildren();
                    for (Node node : nodesList) {
                        if (inHierarchy(verticalTreeView.getMainView().getMainScene().focusOwnerProperty().get(), node)) {
                            if (node instanceof ExpMapFormulaBox) {
                                ExpMapFormulaBox mapFormulaBox = (ExpMapFormulaBox) node;
                                if (mapFormulaBox.getMapStage() > 0) {

                                    ExpMapQuestionMarker mapQuestionMarker = new ExpMapQuestionMarker(verticalTreeView);
                                    main_pane.getChildren().add(0, mapQuestionMarker);
                                    mapQuestionMarker.bindQuestionLabel(mapFormulaBox);
                                    verticalTreeView.setUndoRedoFlag(true);
                                    verticalTreeView.setUndoRedoFlag(false);

                                    mapFormulaBox.undoMappingRequest();
                                } else {
                                    EditorAlerts.fleetingRedPopup("Question requires marker(s).");
                                }
                                break;
                            }
                        }
                    }
                    e.consume();
                }
            }
        };

        mappingToggle.selectedProperty().addListener((ob, ov, nv) -> {
            if (nv) {
                main_pane.addEventFilter(MouseEvent.MOUSE_PRESSED, mappingClickFilter);
                main_pane.addEventFilter(KeyEvent.KEY_PRESSED, mappingKeyFilter);
            }
            else {
                main_pane.removeEventFilter(MouseEvent.MOUSE_PRESSED, mappingClickFilter);
                main_pane.removeEventFilter(KeyEvent.KEY_PRESSED, mappingKeyFilter);
                ObservableList<Node> nodesList = main_pane.getChildren();
                for (Node node : nodesList) {
                    if (node instanceof ExpMapFormulaBox) {
                        ((ExpMapFormulaBox) node).undoMappingRequest();
                    }
                }
            }
        });
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

                        if (container.getValue("type").equals(DragIconType.DASHED_LINE.toString())) {
                            ExpDashedLine line = new ExpDashedLine(verticalTreeView);
                            main_pane.getChildren().add(line);
                            Point2D cursorPoint = container.getValue("scene_coords");
                            line.relocateToGridPoint(new Point2D(cursorPoint.getX() - 28, cursorPoint.getY()));
                            verticalTreeView.setUndoRedoFlag(true);
                            verticalTreeView.setUndoRedoFlag(false);
                        }

                        else if (container.getValue("type").equals(DragIconType.BRACKET.toString())) {
                            ExpVerticalBracket bracket = new ExpVerticalBracket(verticalTreeView);
                            main_pane.getChildren().add(bracket);
                            Point2D cursorPoint = container.getValue("scene_coords");
                            bracket.relocateToGridPoint(new Point2D(cursorPoint.getX(), cursorPoint.getY()));
                            verticalTreeView.setUndoRedoFlag(true);
                            verticalTreeView.setUndoRedoFlag(false);
                        }

                        else if (container.getValue("type").equals(DragIconType.TREE_FIELD.toString())) {
                            ExpTreeFormulaBox treeFormulaBox = new ExpTreeFormulaBox(verticalTreeView);
                            main_pane.getChildren().add(treeFormulaBox);
                            Point2D cursorPoint = container.getValue("scene_coords");
                            treeFormulaBox.relocateToGridPoint(new Point2D(cursorPoint.getX(), cursorPoint.getY()));
                            verticalTreeView.setUndoRedoFlag(true);
                            verticalTreeView.setUndoRedoFlag(false);


                        }

                        else if (container.getValue("type").equals(DragIconType.MAP_FIELD.toString())) {
                            ExpMapFormulaBox mapFormulaBox = new ExpMapFormulaBox(verticalTreeView);
                            main_pane.getChildren().add(mapFormulaBox);
                            Point2D cursorPoint = container.getValue("scene_coords");
                            mapFormulaBox.relocateToGridPoint(new Point2D(cursorPoint.getX(), cursorPoint.getY()));
                            verticalTreeView.setUndoRedoFlag(true);
                            verticalTreeView.setUndoRedoFlag(false);
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

                /*
				//Move node drag operation
				container =
						(DragContainer) event.getDragboard().getContent(DragContainer.DragNode);

				if (container != null) {
					if (container.getValue("type") != null)
						System.out.println ("Moved node " + container.getValue("type"));
				}
                 */


                //AddLink drag operation
                container =
                        (DragContainer) event.getDragboard().getContent(DragContainer.AddLink);

                if (container != null) {

                    //bind the ends of our link to the nodes whose id's are stored in the drag container
                    String sourceId = container.getValue("source");
                    String targetId = container.getValue("target");

                    if (sourceId != null && targetId != null) {

     //                   //	System.out.println(container.getData());
     //                  NodeLink link = new NodeLink();
                        ExpClickableNodeLink link = new ExpClickableNodeLink(verticalTreeView);


                        //add our link at the top of the rendering order so it's rendered first
                        main_pane.getChildren().add(0,link);

                        ExpTreeFormulaBox source = null;
                        ExpTreeFormulaBox target = null;

                        for (Node n: main_pane.getChildren()) {

                            if (n.getId() == null)
                                continue;

                            if (n.getId().equals(sourceId))
                                source = (ExpTreeFormulaBox) n;

                            if (n.getId().equals(targetId))
                                target = (ExpTreeFormulaBox) n;

                        }

                        if (source != null && target != null)
                            link.bindEnds(source, target);
                            verticalTreeView.setUndoRedoFlag(true);
                            verticalTreeView.setUndoRedoFlag(false);
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



    public ToggleButton getBoxToggle() {
        return boxToggle;
    }

    public ToggleButton getStarToggle() {
        return starToggle;
    }

    public HBox getAnnotationBox() {
        return annotationBox;
    }

    public ToggleButton getAnnotationToggle() {
        return annotationToggle;
    }

    public ToggleButton getCircleToggle() {
        return circleToggle;
    }

    public ToggleButton getUnderlineToggle() {
        return underlineToggle;
    }

    public ToggleButton getMappingToggle() {
        return mappingToggle;
    }

    public ToggleGroup getButtonGroup() {
        return buttonGroup;
    }



}
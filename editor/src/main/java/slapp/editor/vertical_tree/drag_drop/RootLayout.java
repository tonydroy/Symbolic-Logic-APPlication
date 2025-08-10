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


import com.gluonhq.richtextarea.RichTextArea;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
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
import slapp.editor.EditorMain;
import slapp.editor.vertical_tree.VerticalTreeView;

import java.util.ArrayList;
import java.util.List;

public class RootLayout extends AnchorPane {

    VerticalTreeView verticalTreeView;
    SplitPane base_pane;
    AnchorPane mainPane;
    HBox topPane;


    EventHandler boxClickFilter;
    EventHandler starClickFilter;
    EventHandler annotationClickFilter;
    EventHandler circleClickFilter;
    EventHandler underlineClickFilter;
    EventHandler mappingClickFilter;
    EventHandler mappingKeyFilter;

    EventHandler rtaClickFilter;

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

    public RootLayout(VerticalTreeView verticalTreeView) {
        this.verticalTreeView = verticalTreeView;
        topPane = new HBox();

        mainPane = new AnchorPane();
        base_pane = new SplitPane();
        base_pane.getItems().addAll(topPane, mainPane);
        base_pane.setOrientation(Orientation.VERTICAL);


        this.getChildren().add(base_pane);

        setupWindow();
        initialize();

    }

    private void setupWindow() {
 //       RightDragResizer.makeResizable(base_pane);
     //   BottomDragResizer.makeResizable(base_pane);
        topPane.setStyle("-fx-background-color: #FCFCFC;");
        topPane.setPadding(new Insets(0, 0, 0, 20));

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
        mainPane.setStyle("-fx-background-color: white," +
                "linear-gradient(from 0.5px 0.0px to 24.5px  0.0px, repeat, #f5f5f5 1%, transparent 5%)," +
                "linear-gradient(from 0.0px 0.5px to  0.0px 24.5px, repeat, #f5f5f5 1%, transparent 5%);");
        mainPane.setPadding(new Insets(5,20,20,5));                //5 0 0 5




        this.setMinWidth(200); this.setMinHeight(100);
        this.setBottomAnchor(base_pane, 0.0); this.setTopAnchor(base_pane, 0.0); this.setLeftAnchor(base_pane, 0.0); this.setRightAnchor(base_pane, 0.0);

        topPane.setSpacing(20);
        topPane.setAlignment(Pos.CENTER);
        topPane.setMinHeight(32);
        topPane.setMaxHeight(32);

        base_pane.setResizableWithParent(mainPane, true);


        rtaClickFilter = new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                ObservableList<Node> nodesList = mainPane.getChildren();
                for (Node node : nodesList) {
                    if (node instanceof TreeFormulaBox) {
                        RichTextArea treeRTA = ((TreeFormulaBox) node).getFormulaBox().getRTA();
                        if (treeRTA.isModified()) {
                            verticalTreeView.getVTExercise().setExerciseModified(true);
                            verticalTreeView.getVTExercise().pushUndoRedo();
                            treeRTA.getActionFactory().saveNow().execute(new ActionEvent());
                        }
                    }
                    if (node instanceof MapFormulaBox) {
                        RichTextArea mapRTA = ((MapFormulaBox) node).getFormulaBox().getRTA();
                        if (mapRTA.isModified()) {
                            verticalTreeView.getVTExercise().setExerciseModified(true);
                            verticalTreeView.getVTExercise().pushUndoRedo();
                            mapRTA.getActionFactory().saveNow().execute(new ActionEvent());
                        }
                    }
                }

            }
        };
        verticalTreeView.getMainView().getMainScene().addEventFilter(MouseEvent.MOUSE_PRESSED, rtaClickFilter);


        boxClickFilter = new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                ObservableList<Node> nodesList = mainPane.getChildren();
                for (Node node : nodesList) {
                    if (inHierarchy(event.getPickResult().getIntersectedNode(), node)) {
                        if (node instanceof TreeFormulaBox) {
                            ((TreeFormulaBox) node).processBoxRequest(event.getButton() == MouseButton.PRIMARY);
                            break;
                        }
                    }
                }
 //               event.consume();
            }
        };
        boxToggle.selectedProperty().addListener((ob, ov, nv) -> {
            if (nv)  mainPane.addEventFilter(MouseEvent.MOUSE_PRESSED, boxClickFilter );
            else mainPane.removeEventFilter(MouseEvent.MOUSE_PRESSED, boxClickFilter);
        });

        starClickFilter = new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                ObservableList<Node> nodesList = mainPane.getChildren();
                for (Node node : nodesList) {
                    if (inHierarchy(event.getPickResult().getIntersectedNode(), node)) {
                        if (node instanceof TreeFormulaBox) {
                            ((TreeFormulaBox) node).processStarRequest(event.getButton() == MouseButton.PRIMARY);
                            break;
                        }
                    }
                }
 //               event.consume();
            }
        };
        starToggle.selectedProperty().addListener((ob, ov, nv) -> {
            if (nv)  mainPane.addEventFilter(MouseEvent.MOUSE_PRESSED, starClickFilter );
            else mainPane.removeEventFilter(MouseEvent.MOUSE_PRESSED, starClickFilter);
        });

        annotationClickFilter = new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                ObservableList<Node> nodesList = mainPane.getChildren();
                for (Node node : nodesList) {
                    if (inHierarchy(event.getPickResult().getIntersectedNode(), node)) {
                        if (node instanceof TreeFormulaBox) {
                            ((TreeFormulaBox) node).processAnnotationRequest(event.getButton() == MouseButton.PRIMARY);
                            break;
                        }
                    }
                }
  //              event.consume();
            }
        };
        annotationToggle.selectedProperty().addListener((ob, ov, nv) -> {
            if (nv)  mainPane.addEventFilter(MouseEvent.MOUSE_PRESSED, annotationClickFilter );
            else mainPane.removeEventFilter(MouseEvent.MOUSE_PRESSED, annotationClickFilter);
        });

        annotationPlus.setOnAction(e -> {
            ObservableList<Node> nodesList = mainPane.getChildren();
            for (Node node : nodesList) {
                if (node instanceof TreeFormulaBox) {
                    ((TreeFormulaBox) node).processAnnotationRequest(true);
                }
            }
            annotationToggle.setSelected(false);
        });

        annotationMinus.setOnAction(e -> {
           ObservableList<Node> nodesList = mainPane.getChildren();
           for (Node node : nodesList) {
               if (node instanceof TreeFormulaBox) {
                   ((TreeFormulaBox) node).processAnnotationRequest(false);
               }
           }
            annotationToggle.setSelected(false);
        });

        circleClickFilter = new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                ObservableList<Node> nodesList = mainPane.getChildren();
                for (Node node : nodesList) {
                    if (inHierarchy(event.getPickResult().getIntersectedNode(), node)) {
                        if (node instanceof TreeFormulaBox) {
                            ((TreeFormulaBox) node).processCircleRequest(event.getButton() == MouseButton.PRIMARY);
                            break;
                        }
                    }
                }
 //               event.consume();
            }
        };
        circleToggle.selectedProperty().addListener((ob, ov, nv) -> {
            if (nv)  mainPane.addEventFilter(MouseEvent.MOUSE_PRESSED, circleClickFilter );
            else {
                mainPane.removeEventFilter(MouseEvent.MOUSE_PRESSED, circleClickFilter);
                ObservableList<Node> nodesList = mainPane.getChildren();
                for (Node node : nodesList) {
                    if (node instanceof TreeFormulaBox) {
                        ((TreeFormulaBox) node).undoCircleRequest();
                    }
                }
            }
        });

        underlineClickFilter = new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                ObservableList<Node> nodesList = mainPane.getChildren();
                for (Node node : nodesList) {
                    if (inHierarchy(event.getPickResult().getIntersectedNode(), node)) {
                        if (node instanceof TreeFormulaBox) {
                            ((TreeFormulaBox) node).processUnderlineRequest(event.getButton() == MouseButton.PRIMARY);
                            break;
                        }
                    }
                }
//               event.consume();
            }
        };
        underlineToggle.selectedProperty().addListener((ob, ov, nv) -> {
            if (nv)  mainPane.addEventFilter(MouseEvent.MOUSE_PRESSED, underlineClickFilter );
            else {
                mainPane.removeEventFilter(MouseEvent.MOUSE_PRESSED, underlineClickFilter);
                ObservableList<Node> nodesList = mainPane.getChildren();
                for (Node node : nodesList) {
                    if (node instanceof TreeFormulaBox) {
                        ((TreeFormulaBox) node).undoUnderlineRequest();
                    }
                }
            }
        });

        mappingClickFilter = new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                ObservableList<Node> nodesList = mainPane.getChildren();
                for (Node node : nodesList) {
                    if (inHierarchy(event.getPickResult().getIntersectedNode(), node)) {
                        if (node instanceof MapFormulaBox) {
                            ((MapFormulaBox) node).processMappingRequest(event.getButton() == MouseButton.PRIMARY);
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
                    ObservableList<Node> nodesList = mainPane.getChildren();
                    List<MapFormulaBox> formulaBoxes = new ArrayList<>();
                    for (Node node : nodesList) {
                        if (node instanceof MapFormulaBox) {
                            MapFormulaBox mapFormulaBox = (MapFormulaBox) node;
                            if (mapFormulaBox.getMapStage() > 0) {
                                formulaBoxes.add(mapFormulaBox);
                            }
                        }
                    }
                    if (formulaBoxes.size() == 2) {
                        ClickableMapLink mapLink = new ClickableMapLink(verticalTreeView);
                        mainPane.getChildren().add(0, mapLink);
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
                    ObservableList<Node> nodesList = mainPane.getChildren();
                    for (Node node : nodesList) {
                        if (inHierarchy(verticalTreeView.getMainView().getMainScene().focusOwnerProperty().get(), node)) {
                            if (node instanceof MapFormulaBox) {
                                MapFormulaBox mapFormulaBox = (MapFormulaBox) node;
                                if (mapFormulaBox.getMapStage() > 0) {

                                    MapQuestionMarker mapQuestionMarker = new MapQuestionMarker(verticalTreeView);
                                    mainPane.getChildren().add(0, mapQuestionMarker);
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
                mainPane.addEventFilter(MouseEvent.MOUSE_PRESSED, mappingClickFilter);
                mainPane.addEventFilter(KeyEvent.KEY_PRESSED, mappingKeyFilter);
            }
            else {
                mainPane.removeEventFilter(MouseEvent.MOUSE_PRESSED, mappingClickFilter);
                mainPane.removeEventFilter(KeyEvent.KEY_PRESSED, mappingKeyFilter);
                ObservableList<Node> nodesList = mainPane.getChildren();
                for (Node node : nodesList) {
                    if (node instanceof MapFormulaBox) {
                        ((MapFormulaBox) node).undoMappingRequest();
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
        topPane.getChildren().add(icn);
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
                mainPane.setOnDragOver(mIconDragOverRightPane);
                mainPane.setOnDragDropped(mIconDragDropped);

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

 //               mDragOverIcon.startDragAndDrop (TransferMode.ANY).setContent(content);
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

                Point2D p = mainPane.sceneToLocal(event.getSceneX(), event.getSceneY());

                //turn on transfer mode and track in the right-pane's context
                //if (and only if) the mouse cursor falls within the right pane's bounds.

                if (!mainPane.boundsInLocalProperty().get().contains(p)) {
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
                mDragOverIcon.relocateToPoint(  new Point2D(event.getSceneX(), event.getSceneY()) );
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

                mainPane.removeEventHandler(DragEvent.DRAG_OVER, mIconDragOverRightPane);
                mainPane.removeEventHandler(DragEvent.DRAG_DROPPED, mIconDragDropped);
                base_pane.removeEventHandler(DragEvent.DRAG_OVER, mIconDragOverRoot);

                mDragOverIcon.setVisible(false);

                //stop drag icon from preventing resize- by moving icon out of main pane
                mDragOverIcon.relocateToPoint(  new Point2D(0, 0) );

                //Create node drag operation
                DragContainer container =
                        (DragContainer) event.getDragboard().getContent(DragContainer.AddNode);

                if (container != null) {
                    if (container.getValue("scene_coords") != null) {

                        if (container.getValue("type").equals(DragIconType.dashed_line.toString())) {
                            DashedLine line = new DashedLine(verticalTreeView);
                            mainPane.getChildren().add(line);
                            Point2D cursorPoint = container.getValue("scene_coords");
                            line.relocateToGridPoint(new Point2D(cursorPoint.getX() - 28, cursorPoint.getY()));
                            verticalTreeView.setUndoRedoFlag(true);
                            verticalTreeView.setUndoRedoFlag(false);
                        }

                        else if (container.getValue("type").equals(DragIconType.bracket.toString())) {
                            VerticalBracket bracket = new VerticalBracket(verticalTreeView);
                            mainPane.getChildren().add(bracket);
                            Point2D cursorPoint = container.getValue("scene_coords");
                            bracket.relocateToGridPoint(new Point2D(cursorPoint.getX(), cursorPoint.getY()));
                            verticalTreeView.setUndoRedoFlag(true);
                            verticalTreeView.setUndoRedoFlag(false);
                        }

                        else if (container.getValue("type").equals(DragIconType.tree_field.toString())) {
                            TreeFormulaBox treeFormulaBox = new TreeFormulaBox(verticalTreeView);
                            mainPane.getChildren().add(treeFormulaBox);
                            Point2D cursorPoint = container.getValue("scene_coords");
                            treeFormulaBox.relocateToGridPoint(new Point2D(cursorPoint.getX(), cursorPoint.getY()));
                            verticalTreeView.setUndoRedoFlag(true);
                            verticalTreeView.setUndoRedoFlag(false);
                        }

                        else if (container.getValue("type").equals(DragIconType.map_field.toString())) {
                            MapFormulaBox mapFormulaBox = new MapFormulaBox(verticalTreeView);
                            mainPane.getChildren().add(mapFormulaBox);
                            Point2D cursorPoint = container.getValue("scene_coords");
                            mapFormulaBox.relocateToGridPoint(new Point2D(cursorPoint.getX(), cursorPoint.getY()));
                            verticalTreeView.setUndoRedoFlag(true);
                            verticalTreeView.setUndoRedoFlag(false);
                        }




                        else {

                            DraggableNode node = new DraggableNode();

                            node.setType(DragIconType.valueOf(container.getValue("type")));
                            mainPane.getChildren().add(node);

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
                        ClickableNodeLink link = new ClickableNodeLink(verticalTreeView);


                        //add our link at the top of the rendering order so it's rendered first
                        mainPane.getChildren().add(0,link);

                        TreeFormulaBox source = null;
                        TreeFormulaBox target = null;

                        for (Node n: mainPane.getChildren()) {

                            if (n.getId() == null)
                                continue;

                            if (n.getId().equals(sourceId))
                                source = (TreeFormulaBox) n;

                            if (n.getId().equals(targetId))
                                target = (TreeFormulaBox) n;

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

    public AnchorPane getMainPane() {
        return mainPane;
    }

    public HBox getTopPane() {
        return topPane;
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
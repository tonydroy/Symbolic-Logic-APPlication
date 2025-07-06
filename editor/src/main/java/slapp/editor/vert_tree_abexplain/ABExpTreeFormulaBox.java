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

package slapp.editor.vert_tree_abexplain;

import com.gluonhq.richtextarea.RichTextArea;
import com.gluonhq.richtextarea.RichTextAreaSkin;
import com.gluonhq.richtextarea.model.Document;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.transform.Scale;
import slapp.editor.EditorAlerts;
import slapp.editor.EditorMain;
import slapp.editor.decorated_rta.BoxedDRTA;
import slapp.editor.decorated_rta.DecoratedRTA;
import slapp.editor.main_window.ControlType;
import slapp.editor.vert_tree_abexplain.ABExpRightDragResizer;
import slapp.editor.vert_tree_abexplain.VerticalTreeABExpView;
import slapp.editor.vertical_tree.drag_drop.DragContainer;
import slapp.editor.vertical_tree.drag_drop.DragIconType;
import slapp.editor.vertical_tree.drag_drop.NodeLink;
import slapp.editor.vertical_tree.drag_drop.Point2dSerial;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.UUID;

public class ABExpTreeFormulaBox extends AnchorPane {
    private VerticalTreeABExpView verticalTreeView;

    private AnchorPane top_link_handle;
    private AnchorPane bottom_link_handle;
    private Label leftDragLabel;
    private Label closeLabel;

    private EventHandler <MouseEvent> mLinkHandleDragDetected;
    private EventHandler <DragEvent> mLinkHandleDragDropped;
    private EventHandler <DragEvent> mContextLinkDragOver;
    private EventHandler <DragEvent> mContextLinkDragDropped;
    private EventHandler<DragEvent> mContextDragOver;
    private EventHandler <DragEvent> mContextDragDropped;

//    private ClickableNodeLink mDragLink = null;
    private NodeLink mDragLink = null;
    private AnchorPane right_pane = null;
    private List<String> mLinkIds = new ArrayList<>();

    private DragIconType mType = DragIconType.tree_field;

    private Point2D mDragOffset = new Point2D (0.0, 0.0);

    private final ABExpTreeFormulaBox self;

    private BoxedDRTA formulaBox;

    private HBox mainBox;
    private GridPane labelPane;
    private VBox centerBox;
    private VBox middleBox;
    private AnchorPane linesPane = new AnchorPane();

    private String idString;
    private boolean boxed = false;
    private boolean starred = false;
    private boolean annotation = false;
    private boolean annotationChanged = false;
    private BoxedDRTA annotationField;



    Rectangle oval = new Rectangle();
    EventHandler circleKeyFilter;
    int circleStage = 0;
    Label[] circleMarkers;
    Double[] circleXAnchors = new Double[2];
    double rtaBoundsHeight;
    double rtaBoundsMinY;
    boolean circled = false;
    EventHandler ulineKeyFilter;
    int ulineStage = 0;
    Label[] ulineMarkers;
    Double[] ulineXAnchors = new Double[2];
    double ulineSpace = 3.0;
    List<Integer> baseline = new ArrayList<>();

    private double printWidth;







    public ABExpTreeFormulaBox(VerticalTreeABExpView verticalTreeView) {
        this.verticalTreeView = verticalTreeView;
        self = this;
        circleMarkers = new Label[]{new Label("|"), new Label("|")};
        ulineMarkers = new Label[]{new Label("|"), new Label("|")};



        top_link_handle = new AnchorPane();
        top_link_handle.setPrefHeight(9);
        top_link_handle.setMaxWidth(30);
        top_link_handle.setOnMouseEntered(e -> top_link_handle.setStyle("-fx-background-color: grey; -fx-background-radius: 5 5 0 0") );
        top_link_handle.setOnDragOver(e -> top_link_handle.setStyle("-fx-background-color: grey; -fx-background-radius: 5 5 0 0") );
        top_link_handle.setOnMouseExited(e ->  top_link_handle.setStyle("-fx-background-color: transparent") );
        top_link_handle.setOnDragExited(e ->  top_link_handle.setStyle("-fx-background-color: transparent" ));

        bottom_link_handle = new AnchorPane();
        bottom_link_handle.setPrefHeight(9);
        bottom_link_handle.setMaxWidth(30);
        bottom_link_handle.setStyle("-fx-background-radius: 0 0 5 5");
        bottom_link_handle.setOnMouseEntered(e -> bottom_link_handle.setStyle("-fx-background-color: grey; -fx-background-radius: 0 0 5 5"));
        bottom_link_handle.setOnDragOver(e -> bottom_link_handle.setStyle("-fx-background-color: grey; -fx-background-radius: 0 0 5 5") );
        bottom_link_handle.setOnMouseExited(e -> bottom_link_handle.setStyle("fx-background-color: transparent"));
        bottom_link_handle.setOnDragExited(e ->  bottom_link_handle.setStyle("-fx-background-color: transparent"));



        leftDragLabel = new Label("");
        leftDragLabel.setMaxWidth(10);
        leftDragLabel.setMinWidth(10);
        leftDragLabel.setMaxHeight(10);
        leftDragLabel.setPadding(new Insets(0));
        //       leftDragLabel.setStyle("-fx-background-color: red");

        closeLabel = new Label();
        closeLabel.setMaxHeight(10);
        closeLabel.setMaxWidth(10);
        closeLabel.setPadding(new Insets(0));

        labelPane = new GridPane();
        ColumnConstraints columnConstraints = new ColumnConstraints(10);
        columnConstraints.setHgrow(Priority.NEVER);
        labelPane.getColumnConstraints().add(columnConstraints);
        RowConstraints closeRowConstraints = new RowConstraints(10);
        closeRowConstraints.setVgrow(Priority.NEVER);
        RowConstraints moveRowConstraints = new RowConstraints(10);
        moveRowConstraints.setVgrow(Priority.NEVER);
        labelPane.getRowConstraints().addAll(closeRowConstraints, moveRowConstraints);
        labelPane.add(closeLabel, 0, 0); labelPane.add(leftDragLabel, 0, 1);


        leftDragLabel.setOnMouseEntered(e -> {
            leftDragLabel.setStyle("-fx-background-color: grey; -fx-background-radius: 0 0 0 5;");
            closeLabel.setStyle("-fx-background-color: black; -fx-background-radius: 5 0 0 0;");
//            leftDragLabel.setCursor(Cursor.MOVE);
        });
        leftDragLabel.setOnMouseExited(e -> {
            leftDragLabel.setStyle("-fx-background-color: transparent");
            closeLabel.setStyle("-fx-background-color:transparent");
            //           leftDragLabel.setCursor(Cursor.DEFAULT);
        });

        closeLabel.setOnMouseEntered(e -> {
            leftDragLabel.setStyle("-fx-background-color: grey; -fx-background-radius: 0 0 0 5");
            closeLabel.setStyle("-fx-background-color: black; -fx-background-radius: 5 0 0 0");
//            leftDragLabel.setCursor(Cursor.MOVE);
        });
        closeLabel.setOnMouseExited(e -> {
            leftDragLabel.setStyle("-fx-background-color: transparent");
            closeLabel.setStyle("-fx-background-color:transparent");
            //           leftDragLabel.setCursor(Cursor.DEFAULT);
        });


        formulaBox = newFormulaBoxedDRTA();
//        ABExpRightDragResizer resizer = new ABExpRightDragResizer(verticalTreeView);
//        resizer.makeResizable(formulaBox.getRTA());

        middleBox = new VBox(formulaBox.getBoxedRTA(), linesPane);


        centerBox = new VBox(top_link_handle, middleBox, bottom_link_handle);
        centerBox.setAlignment(Pos.CENTER);

        mainBox = new HBox(labelPane, centerBox);
        mainBox.setAlignment(Pos.TOP_CENTER);
        mainBox.setMargin(labelPane, new Insets(10, 0, 0, 0));


        self.getChildren().addAll(mainBox);
//        self.setBottomAnchor(mainBox, 0.0);
        self.setLeftAnchor(mainBox, 0.0);
        self.setTopAnchor(mainBox, 0.0);
 //       self.setRightAnchor(mainBox, 0.0);

        setId(UUID.randomUUID().toString());
        idString = getId();
        initialize();
    }

    private void initialize() {
        buildNodeDragHandlers();
        buildLinkDragHandlers();

        top_link_handle.setOnDragDetected(mLinkHandleDragDetected);
        bottom_link_handle.setOnDragDetected(mLinkHandleDragDetected);

        top_link_handle.setOnDragDropped(mLinkHandleDragDropped);
        bottom_link_handle.setOnDragDropped(mLinkHandleDragDropped);

  //      mDragLink = new ClickableNodeLink();
        mDragLink = new NodeLink();
        mDragLink.setVisible(false);

        parentProperty().addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue ob, Object ov, Object nv) {
                right_pane = (AnchorPane) getParent();
            }
        });

        circleKeyFilter = new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent e) {
                RichTextArea rta = formulaBox.getRTA();
                KeyCode code = e.getCode();
                Bounds rtaBounds = self.sceneToLocal(rta.localToScene(rta.getBoundsInLocal()));
                rtaBoundsHeight = rtaBounds.getHeight();
                rtaBoundsMinY = rtaBounds.getMinY();

                if (code == KeyCode.F10) {
                    if (circleStage < 2) {
                        if (!circled) {
                            Bounds caretBounds = ((RichTextAreaSkin) formulaBox.getRTA().getSkin()).getCaretPosition();
                            Bounds newCaretBounds = rta.sceneToLocal(caretBounds);
                            double xAnchor = newCaretBounds.getMaxX() + rtaBounds.getMinX() - 1.0;
                            double yAnchor = newCaretBounds.getMaxY() * .75 + rtaBounds.getMinY();

                            Label marker = circleMarkers[circleStage];
                            circleXAnchors[circleStage] = xAnchor;
                            self.getChildren().add(marker);
                            self.setLeftAnchor(marker, xAnchor);
                            self.setTopAnchor(marker, yAnchor);
                            circleStage++;
                        }
                        else EditorAlerts.fleetingRedPopup("Text field has at most one circle annotation.");
                    } else {
                        self.getChildren().removeAll(circleMarkers);
                        circleStage = 0;
                    }
                    e.consume();
                } else if (code == KeyCode.F11) {
                    if (circleStage < 2) {
                        EditorAlerts.fleetingRedPopup("Circle requires two markers.");
                    }
                    else if (circleStage == 2) {
                        setCircle();
                        circleStage++;
                        circled = true;
                    } else {
                        EditorAlerts.fleetingRedPopup("Text field has at most one circle annotation.");
                    }
                    e.consume();
                }
            }
        };


        ulineKeyFilter = new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent e) {
                RichTextArea rta = formulaBox.getRTA();
                KeyCode code = e.getCode();
                Bounds rtaBounds = self.sceneToLocal(rta.localToScene(rta.getBoundsInLocal()));
                if (code == KeyCode.F10) {
                    if (ulineStage < 2) {
                        Bounds caretBounds = ((RichTextAreaSkin) formulaBox.getRTA().getSkin()).getCaretPosition();
                        Bounds newCaretBounds = rta.sceneToLocal(caretBounds);
                        double xAnchor = newCaretBounds.getMaxX() + rtaBounds.getMinX() - 2.0;
                        double yAnchor = newCaretBounds.getMaxY() * .75 + rtaBounds.getMinY();

                        Label marker = ulineMarkers[ulineStage];
                        ulineXAnchors[ulineStage] = xAnchor;
                        self.getChildren().add(marker);
                        self.setLeftAnchor(marker, xAnchor);
                        self.setTopAnchor(marker, yAnchor);
                        ulineStage++;
                    } else {
                        self.getChildren().removeAll(ulineMarkers);
                        ulineStage = 0;
                    }
                    e.consume();
                }
                else if (code == KeyCode.F11) {
                    if (ulineStage == 2) {
                        double minX = Math.min(ulineXAnchors[0], ulineXAnchors[1]);
                        double maxX = Math.max(ulineXAnchors[0], ulineXAnchors[1]);
                        self.getChildren().removeAll(ulineMarkers);
                        setLine(minX - rtaBounds.getMinX(), maxX - rtaBounds.getMinX());
                        ulineStage = 0;
                    } else {
                        EditorAlerts.fleetingRedPopup("Underline requires two markers.");
                    }
                    e.consume();
                }
            }
        };


    }

    public void setCircle() {

        double minX = Math.min(circleXAnchors[0], circleXAnchors[1]);
        double maxX = Math.max(circleXAnchors[0], circleXAnchors[1]);
        self.getChildren().removeAll(circleMarkers);
        circleStage = 0;
        self.getChildren().add(oval);
        oval.setWidth(maxX - minX);

        oval.setHeight(rtaBoundsHeight - 6.0);
        oval.setStyle("-fx-fill: transparent; -fx-stroke: black; -fx-stroke-width: 1;");
        oval.setArcHeight(rtaBoundsHeight - 6.0);
        oval.setArcWidth((maxX - minX));
        self.setLeftAnchor(oval, minX);
        self.setTopAnchor(oval, rtaBoundsMinY + 2.0);
        verticalTreeView.setUndoRedoFlag(true);
        verticalTreeView.setUndoRedoFlag(false);
    }

    private void setLine(double startX, double endX) {
        //make sure there is a baseline for new line by extending baseline to endX
        int intStartX = (int) Math.round(startX);
        int intEndX = (int) Math.round(endX);
        for (int i = baseline.size(); i <= intEndX; i++) {
            baseline.add(-((int) ulineSpace));
        }

        //find base for new line
        int maxBase = 0;
        for (int i = intStartX; i <= intEndX; i++) {
            if (baseline.get(i) > maxBase) {
                maxBase = baseline.get(i);
            }
        }

        //get yPosition of new line and update baseline
        double yPos = (double) maxBase + ulineSpace;
        for (int i = intStartX; i <= intEndX; i++) {
            baseline.set(i, (int) Math.round(yPos));
        }

        //add line to linesPane
        addLineToPane(startX, endX - startX, yPos);
        verticalTreeView.setUndoRedoFlag(true);
        verticalTreeView.setUndoRedoFlag(false);
    }

    public void addLineToPane(double startX, double length, double yPos) {
        Line line = new Line(0, 0, length, 0);
        linesPane.getChildren().add(line);
        linesPane.setLeftAnchor(line, startX);
        linesPane.setBottomAnchor(line, yPos);
    }

    public void registerLink(String linkId) {mLinkIds.add(linkId); }

    public void setType (DragIconType type) {
        mType = type;
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

                relocateToGridPoint2( new Point2D(event.getSceneX(), event.getSceneY())  );
                self.setCursor(Cursor.DEFAULT);
                verticalTreeView.setUndoRedoFlag(true);
                verticalTreeView.setUndoRedoFlag(false);
            }
        };


        //close button click
        closeLabel.setOnMouseClicked( new EventHandler <MouseEvent> () {
            @Override
            public void handle(MouseEvent event) {
                self.requestFocus();
                AnchorPane parent  = (AnchorPane) self.getParent();
                parent.getChildren().remove(self);

                //iterate each link id connected to this node
                //find it's corresponding component in the right-hand
                //AnchorPane and delete it.

                //Note:  other nodes connected to these links are not being
                //notified that the link has been removed.
                for (ListIterator<String> iterId = mLinkIds.listIterator();
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
                verticalTreeView.setUndoRedoFlag(true);
                verticalTreeView.setUndoRedoFlag(false);

                parent.requestFocus();
            }
        });



        leftDragLabel.setOnDragDetected ( new EventHandler <MouseEvent> () {

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

                Dragboard db = startDragAndDrop(TransferMode.MOVE);
                db.setDragView(EditorMain.emptyImage);
                db.setContent(content);
     //           startDragAndDrop (TransferMode.ANY).setContent(content);
                formulaBox.getRTA().requestFocus();
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
                        getLayoutY() + (middleBox.getHeight()/2) + 9

 //                       getLayoutY() + (getHeight() / 2.0)
                );

                mDragLink.setStart(p);

                //Drag content code
                ClipboardContent content = new ClipboardContent();
                DragContainer container = new DragContainer();


                //pass the UUID of the source node for later lookup
                container.addData("source", getId());

                content.put(DragContainer.AddLink, container);

                Dragboard db = startDragAndDrop(TransferMode.MOVE);
                db.setDragView(EditorMain.emptyImage);
                db.setContent(content);
 //               startDragAndDrop (TransferMode.ANY).setContent(content);

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

     //           bottom_link_handle.setStyle("-fx-background-color: transparent");
     //           top_link_handle.setStyle("-fx-background-color: transparent");

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


    public void relocateToGridPoint (Point2D p) {

        //relocates the object to a point that has been converted to
        //scene coordinates
        Point2D localCoords = getParent().sceneToLocal(p);
        double localY = Math.round((localCoords.getY() - 16) / 24.0) * 24.0;

        relocate (
                (int) localCoords.getX() - 36,
                (int) (localY - 7 )
        );
        self.requestFocus();
    }


    // I don't understand why both this and the following method are required to drop on line - should be same w/o offset localY??
    public void relocateToGridPoint2 (Point2D p) {

        //relocates the object to a point that has been converted to
        //scene coordinates
        Point2D localCoords = getParent().sceneToLocal(p);
        double localY = Math.round((localCoords.getY() - 16) / 24.0) * 24.0 ;

        relocate (
                (int) localCoords.getX(),
                (int) (localY - 7)
        );
    }




    public void relocateToPoint (Point2D p) {

        //relocates the object to a point that has been converted to
        //scene coordinates
        Point2D localCoords = getParent().sceneToLocal(p);


        //       double localY = Math.round(localCoords.getY() / 24.0) * 24.0;

        relocate (
                (int) localCoords.getX(),
                //           (int) ((localCoords.getX() - (getBoundsInLocal().getWidth()) / 2)),
                (int) ((localCoords.getY() - (getBoundsInLocal().getHeight()) / 2 ))
        );
    }

    private BoxedDRTA newFormulaBoxedDRTA() {
        BoxedDRTA boxedDRTA = new BoxedDRTA();
        DecoratedRTA drta = boxedDRTA.getDRTA();
        drta.getKeyboardSelector().valueProperty().setValue(verticalTreeView.getDefaultKeyboard());
        RichTextArea rta = boxedDRTA.getRTA();
        rta.setMaxHeight(24);
        rta.setMinHeight(24);
        rta.setPrefWidth(36);

        RichTextAreaSkin rtaSkin = (RichTextAreaSkin) rta.getSkin();
        rta.prefWidthProperty().bind(Bindings.max(Bindings.add(rtaSkin.nodesWidthProperty(), 6), 12));
        rtaSkin.nodesWidthProperty().addListener((observable, oldValue, newValue) -> {
            Platform.runLater(() -> rta.getParent().requestLayout());
        });
        rta.addEventFilter(KeyEvent.ANY, e -> {
            if (e.getCode() == KeyCode.ENTER) e.consume();
        });

//        rta.setContentAreaWidth(500);
       rta.getStylesheets().add("blueFormulaBox.css");
        rta.setPromptText("");
 //       rta.getActionFactory().saveNow().execute(new ActionEvent());   ---------- this blanks text on open exercise.
        rta.focusedProperty().addListener((ob, ov, nv) -> {
            if (nv) {
                verticalTreeView.getMainView().editorInFocus(drta, ControlType.FIELD);
            } else {
                if (rta.isModified()) {
                    verticalTreeView.setUndoRedoFlag(true);
                    verticalTreeView.setUndoRedoFlag(false);
                    rta.getActionFactory().saveNow().execute(new ActionEvent());
                }

            }
        });
        return boxedDRTA;
    }

    private BoxedDRTA newAnnotationBoxedDRTA() {

        BoxedDRTA annBDRTA = new BoxedDRTA();
        RichTextArea annRTA = annBDRTA.getRTA();
        annRTA.setPrefWidth(42);
        annBDRTA.getBoxedRTA().setMaxHeight(20);
        annBDRTA.getDRTA().getKeyboardSelector().valueProperty().setValue(RichTextAreaSkin.KeyMapValue.BASE_AND_SANS);
        annRTA.addEventFilter(KeyEvent.ANY, e -> {
            if (e.getCode() == KeyCode.ENTER) e.consume();
        });
        annRTA.getStylesheets().add("slappAnnotation.css");
        annRTA.setPromptText("");

        annRTA.focusedProperty().addListener((ob, ov, nv) -> {
            if (nv)  verticalTreeView.getMainView().editorInFocus(annBDRTA.getDRTA(), ControlType.FIELD);
            else {
                if (annRTA.isModified()) {
                    verticalTreeView.setUndoRedoFlag(true);
                    verticalTreeView.setUndoRedoFlag(false);
                    annRTA.getActionFactory().saveNow().execute(new ActionEvent());
                }

            }
        });
        annBDRTA.getBoxedRTA().getTransforms().add(new Scale(.75,.75));

        return annBDRTA;
    }

    public void processBoxRequest(boolean add) {
        if (add) {
            if (!boxed) {
                middleBox.setStyle("-fx-border-color: black; -fx-border-width: 1 1 1 1");
                boxed = true;
                verticalTreeView.setUndoRedoFlag(true);
                verticalTreeView.setUndoRedoFlag(false);
            }
        } else {
            if (boxed) {
                middleBox.setStyle("-fx-border-width: 0 0 0 0");
                boxed = false;
                verticalTreeView.setUndoRedoFlag(true);
                verticalTreeView.setUndoRedoFlag(false);
            }
        }
    }

    //for repopulating window (when we do not desire undo/redo push
    public void addBox() {
        middleBox.setStyle("-fx-border-color: black; -fx-border-width: 1 1 1 1");
        boxed = true;
    }

    public void processStarRequest(boolean add) {
        if (add) {
            if (!starred) {
                addStar();
                verticalTreeView.setUndoRedoFlag(true);
                verticalTreeView.setUndoRedoFlag(false);
            }
        } else {
            if (starred) {
                mainBox.getChildren().clear();
                mainBox.getChildren().addAll(labelPane, centerBox);
                starred = false;
                verticalTreeView.setUndoRedoFlag(true);
                verticalTreeView.setUndoRedoFlag(false);
            }
        }
    }

    public void addStar() {
        Label star = new Label("\u2605");
        star.setStyle("-fx-font-size: 8");
        mainBox.getChildren().clear();
        mainBox.getChildren().addAll(labelPane, centerBox, star);
        mainBox.setMargin(star, new Insets(6,0, 0, 0));
        starred = true;
    }

    public void processAnnotationRequest(boolean add) {
        if (add) {
            if (!annotation) {
                addAnnotation();
                verticalTreeView.setUndoRedoFlag(true);
                verticalTreeView.setUndoRedoFlag(false);
            }

        } else {
            if (annotation) {
                mainBox.getChildren().clear();
                mainBox.getChildren().addAll(labelPane, centerBox);
                annotation = false;
                verticalTreeView.setUndoRedoFlag(true);
                verticalTreeView.setUndoRedoFlag(false);
            }
        }
    }

    public void addAnnotation() {
        annotationField = newAnnotationBoxedDRTA();

        mainBox.getChildren().clear();
        mainBox.getChildren().addAll(labelPane, centerBox, annotationField.getBoxedRTA());
        mainBox.setMargin(annotationField.getBoxedRTA(), new Insets(8, 0, 0, 0));

        annotation = true;
    }


    public void setAnnotationDoc(Document doc) {
        if (annotationField != null) {
            annotationField.getRTA().getActionFactory().open(doc).execute(new ActionEvent());
        }
    }

    public Document getAnnotationDoc() {
        if (annotationField != null) {
            annotationField.getRTA().getActionFactory().saveNow().execute(new ActionEvent());
            return annotationField.getRTA().getDocument();
        } else {
            return new Document();
        }
    }



    public void processCircleRequest(boolean add) {
        if (add) {
            RichTextArea rta = formulaBox.getRTA();
            rta.requestFocus();
            self.addEventFilter(KeyEvent.KEY_PRESSED, circleKeyFilter);
        } else {
            undoCircleRequest();
            self.getChildren().remove(oval);
            circled = false;
            verticalTreeView.setUndoRedoFlag(true);
            verticalTreeView.setUndoRedoFlag(false);
        }
    }

    public void undoCircleRequest() {
        self.removeEventFilter(KeyEvent.KEY_PRESSED, circleKeyFilter);
        self.getChildren().removeAll(circleMarkers[0], circleMarkers[1]);
        circleStage = 0;
    }
    public void processUnderlineRequest(boolean add) {
        if (add) {
            RichTextArea rta = formulaBox.getRTA();
            rta.requestFocus();
            self.addEventFilter(KeyEvent.KEY_PRESSED, ulineKeyFilter);
        } else {
            undoUnderlineRequest();
            linesPane.getChildren().clear();
            baseline.clear();
            verticalTreeView.setUndoRedoFlag(true);
            verticalTreeView.setUndoRedoFlag(false);
        }
    }

    public void undoUnderlineRequest() {
        self.removeEventFilter(KeyEvent.KEY_PRESSED, ulineKeyFilter);
        self.getChildren().removeAll(ulineMarkers[0], ulineMarkers[1]);
        ulineStage = 0;
    }

    public VBox getMiddleBox() {
        return middleBox;
    }

    public List<String> getmLinkIds() {
        return mLinkIds;
    }

    public void setmLinkIds(List<String> mLinkIds) {
        this.mLinkIds = mLinkIds;
    }

    public String getIdString() {
        return idString;
    }

    public void setIdString(String idString) {
        this.idString = idString;
        setId(idString);

    }

    public BoxedDRTA getFormulaBox() {
        return formulaBox;
    }
    public VBox getCenterBox() {return centerBox;}

    public boolean isBoxed() {
        return boxed;
    }

    public boolean isStarred() {
        return starred;
    }

    public boolean isAnnotation() {
        return annotation;
    }

    public BoxedDRTA getAnnotationField() {return annotationField; }

    public boolean isCircled() {
        return circled;
    }

    public void setCircled(boolean circled) {
        this.circled = circled;
    }

    public Double[] getCircleXAnchors() {
        return circleXAnchors;
    }

    public void setCircleXAnchors(Double[] circleXAnchors) {
        this.circleXAnchors = circleXAnchors;
    }

    public double getRtaBoundsHeight() {
        return rtaBoundsHeight;
    }

    public void setRtaBoundsHeight(double rtaBoundsHeight) {
        this.rtaBoundsHeight = rtaBoundsHeight;
    }
    public double getRtaBoundsMinY() {
        return rtaBoundsMinY;
    }

    public void setRtaBoundsMinY(double rtaBoundsMinY) {
        this.rtaBoundsMinY = rtaBoundsMinY;
    }

    public AnchorPane getLinesPane() {
        return linesPane;
    }

    public List<Integer> getBaseline() {
        return baseline;
    }

    public void setBaseline(List<Integer> baseline) {
        this.baseline = baseline;
    }

    public double getPrintWidth() {
        return printWidth;
    }

    public void setPrintWidth(double printWidth) {
        this.printWidth = printWidth;
    }
}

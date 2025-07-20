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
import com.gluonhq.richtextarea.RichTextAreaSkin;
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
import javafx.scene.input.*;
import javafx.scene.layout.*;
import javafx.scene.shape.Rectangle;
import slapp.editor.EditorMain;
import slapp.editor.decorated_rta.BoxedDRTA;
import slapp.editor.decorated_rta.DecoratedRTA;
import slapp.editor.main_window.ControlType;
import slapp.editor.vertical_tree.VerticalTreeView;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.UUID;

public class MapFormulaBox extends AnchorPane {
    private VerticalTreeView verticalTreeView;
    private Label leftDragLabel;
    private Label closeLabel;
    private EventHandler<DragEvent> mContextDragOver;
    private EventHandler <DragEvent> mContextDragDropped;
    private AnchorPane right_pane = null;
    private List<String> mLinkIds = new ArrayList<>();
    private DragIconType mType = DragIconType.map_field;
    private Point2D mDragOffset = new Point2D (0.0, 0.0);
    private final MapFormulaBox self;
    private BoxedDRTA formulaBox;
    private HBox mainBox;
    private GridPane labelPane;
    private VBox centerBox;
    private String idString = "";
    Rectangle oval = new Rectangle();
    EventHandler mappingKeyFilter;
    int mapStage = 0;
    Label[] mapMarkers;
    Double[] mapXAnchors = new Double[2];
    private double printWidth;


    public MapFormulaBox(VerticalTreeView verticalTreeView) {
        this.verticalTreeView = verticalTreeView;
        self = this;
        mapMarkers = new Label[]{new Label("|"), new Label("|")};

        leftDragLabel = new Label("");
        leftDragLabel.setMaxWidth(10);
        leftDragLabel.setMinWidth(10);
        leftDragLabel.setMaxHeight(10);
        leftDragLabel.setPadding(new Insets(0));

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
//        RightDragResizer resizer = new RightDragResizer(verticalTreeView);
//        resizer.makeResizable(formulaBox.getRTA());

        centerBox = new VBox(formulaBox.getBoxedRTA());
        centerBox.setAlignment(Pos.CENTER);

        mainBox = new HBox(labelPane, centerBox);
        mainBox.setAlignment(Pos.TOP_CENTER);
        mainBox.setMargin(labelPane, new Insets(2, 0, 0, 0));

        self.getChildren().addAll(mainBox);
        self.setLeftAnchor(mainBox, 0.0);
        self.setTopAnchor(mainBox, 0.0);

        setId(UUID.randomUUID().toString());
        idString = getId();
        initialize();
    }

    private void initialize() {
        buildNodeDragHandlers();
        parentProperty().addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue ob, Object ov, Object nv) {
                right_pane = (AnchorPane) getParent();
            }
        });

        mappingKeyFilter = new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent e) {
                RichTextArea rta = formulaBox.getRTA();
                KeyCode code = e.getCode();
                Bounds rtaBounds = self.sceneToLocal(rta.localToScene(rta.getBoundsInLocal()));
                if (code == KeyCode.F10) {
                    if (mapStage < 2) {
                        Bounds caretBounds = ((RichTextAreaSkin) formulaBox.getRTA().getSkin()).getCaretPosition();
                        Bounds newCaretBounds = rta.sceneToLocal(caretBounds);
                        double xAnchor = newCaretBounds.getMaxX() + rtaBounds.getMinX() - 1.0;
                        double yAnchor = newCaretBounds.getMaxY() * .75 + rtaBounds.getMinY();

                        Label marker = mapMarkers[mapStage];
                        mapXAnchors[mapStage] = xAnchor;
                        self.getChildren().add(marker);
                        self.setLeftAnchor(marker, xAnchor);
                        self.setTopAnchor(marker, yAnchor);
                        mapStage++;
                    } else {
                        self.getChildren().removeAll(mapMarkers);
                        mapStage = 0;
                    }
                    e.consume();
                }
            }
        };

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

                relocateToGridPoint2( new Point2D(event.getSceneX(), event.getSceneY()) );
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
  //              startDragAndDrop (TransferMode.ANY).setContent(content);
                formulaBox.getRTA().requestFocus();
                event.consume();
            }

        });

    }


    public void relocateToGridPoint (Point2D p) {

        //for object dropped onto pane
        Point2D localCoords = getParent().sceneToLocal(p);
        double localY = Math.round((localCoords.getY() - 16) / 24.0) * 24.0;

        relocate ((int) localCoords.getX() - 44,  (int) (localY ) );
        self.requestFocus();
    }


    // I don't understand why both this and the following method are required to drop on line - should be same w/o offset localY??
    public void relocateToGridPoint2 (Point2D p) {

        //for object moved in pane
        Point2D localCoords = getParent().sceneToLocal(p);
        double localY = Math.round((localCoords.getY() - 16) / 24.0) * 24.0 ;

        relocate (  (int) localCoords.getX(), (int) (localY )    );
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
        drta.getKeyboardSelector().valueProperty().setValue(verticalTreeView.getDefaultMapKeyboard());
        RichTextArea rta = boxedDRTA.getRTA();
        rta.setMaxHeight(24);
        rta.setMinHeight(24);
        rta.setPrefWidth(36);
 //       rta.setContentAreaWidth(500);

        RichTextAreaSkin rtaSkin = (RichTextAreaSkin) rta.getSkin();
        rta.prefWidthProperty().bind(Bindings.max(Bindings.add(rtaSkin.nodesWidthProperty(), 6), 12));
        rtaSkin.nodesWidthProperty().addListener((observable, oldValue, newValue) -> {
            Platform.runLater(() -> rta.getParent().requestLayout());
        });
        rta.addEventFilter(KeyEvent.ANY, e -> {
            if (e.getCode() == KeyCode.ENTER) e.consume();
        });


       rta.getStylesheets().add("greenFormulaBox.css");
        rta.setPromptText("");
 //       rta.getActionFactory().saveNow().execute(new ActionEvent());  messes up rta on reopen
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






    public void processMappingRequest(boolean add) {
        if (add) {
            RichTextArea rta = formulaBox.getRTA();
            rta.requestFocus();
            self.addEventFilter(KeyEvent.KEY_PRESSED, mappingKeyFilter);
        } else {
            undoMappingRequest();
        }
    }

    public void undoMappingRequest() {
        self.removeEventFilter(KeyEvent.KEY_PRESSED, mappingKeyFilter);
        self.getChildren().removeAll(mapMarkers[0], mapMarkers[1]);
        mapStage = 0;
    }


    public VBox getCenterBox() {
        return centerBox;
    }

    public Double[] getMapXAnchors() {
        return mapXAnchors;
    }

    public void setMapXAnchors(Double[] mapXAnchors) {
        this.mapXAnchors = mapXAnchors;
    }

    public int getMapStage() {
        return mapStage;
    }

    public void setMapStage(int mapStage) {
        this.mapStage = mapStage;
    }

    public String getIdString() {
        return idString;
    }

    public void setIdString(String idString) {
        this.idString = idString;
        setId(idString);
    }

    public List<String> getmLinkIds() {
        return mLinkIds;
    }

    public void setmLinkIds(List<String> mLinkIds) {
        this.mLinkIds = mLinkIds;
    }

    public BoxedDRTA getFormulaBox() {
        return formulaBox;
    }

    public double getPrintWidth() {
        return printWidth;
    }

    public void setPrintWidth(double printWidth) {
        this.printWidth = printWidth;
    }
}

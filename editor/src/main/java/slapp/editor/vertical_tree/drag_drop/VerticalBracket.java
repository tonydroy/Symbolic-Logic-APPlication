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

import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import slapp.editor.EditorMain;
import slapp.editor.vertical_tree.VerticalTreeView;

/**
 * Vertical bracket draggable object
 */
public class VerticalBracket extends AnchorPane {
    private VerticalTreeView verticalTreeView;
    private Label topDragLabel;
    private Label closeLabel;
    private EventHandler<DragEvent> mContextDragOver;
    private EventHandler <DragEvent> mContextDragDropped;
    private DragIconType mType = DragIconType.BRACKET;
    private Point2D mDragOffset = new Point2D (0.0, 0.0);
    private final VerticalBracket self;
    private VBox mainPane;


    /**
     * Construct the vertical bracket
     * @param verticalTreeView the {@link slapp.editor.vertical_tree.VerticalTreeView}
     */
    public VerticalBracket(VerticalTreeView verticalTreeView) {
        self = this;
        this.verticalTreeView = verticalTreeView;

        topDragLabel = new Label("");
        topDragLabel.setMaxHeight(10);
        topDragLabel.setMinHeight(10);
        topDragLabel.setMaxWidth(10);
        topDragLabel.setPadding(new Insets(0));
        //       bottomDragLabel.setStyle("-fx-background-color: red");

        closeLabel = new Label();
        closeLabel.setMaxHeight(10);
        closeLabel.setMinHeight(10);
        closeLabel.setMaxWidth(10);
        closeLabel.setPadding(new Insets(0));

        GridPane labelPane = new GridPane();
        RowConstraints rowConstraints = new RowConstraints(10);
        rowConstraints.setVgrow(Priority.NEVER);
        labelPane.getRowConstraints().add(rowConstraints);
        ColumnConstraints closeColumnConstraints = new ColumnConstraints(10);
        closeColumnConstraints.setHgrow(Priority.NEVER);
        ColumnConstraints moveColumnConstraints = new ColumnConstraints(10);
        moveColumnConstraints.setHgrow(Priority.NEVER);
        labelPane.getColumnConstraints().addAll(moveColumnConstraints, closeColumnConstraints);
        labelPane.add(topDragLabel, 0, 0); labelPane.add(closeLabel, 1, 0);

        topDragLabel.setOnMouseEntered(e -> {
            topDragLabel.setStyle("-fx-background-color: grey; -fx-background-radius: 8 0 0 8;");
            closeLabel.setStyle("-fx-background-color: black; -fx-background-radius: 0 8 8 0;");
        });

        topDragLabel.setOnMouseExited(e -> {
            topDragLabel.setStyle("-fx-background-color: transparent");
            closeLabel.setStyle("-fx-background-color:transparent");
        });

        closeLabel.setOnMouseEntered(e -> {
            topDragLabel.setStyle("-fx-background-color: grey; -fx-background-radius: 8 0 0 8");
            closeLabel.setStyle("-fx-background-color: black; -fx-background-radius: 0 8 8 0");
        });

        closeLabel.setOnMouseExited(e -> {
            topDragLabel.setStyle("-fx-background-color: transparent");
            closeLabel.setStyle("-fx-background-color:transparent");
        });



        Pane brackPane = new Pane();
        brackPane.setMinWidth(8.0); brackPane.setMaxWidth(8.0);
        brackPane.setStyle("-fx-border-width: 1.5 0 1.5 1.5; -fx-border-color: black transparent black black; -fx-border-radius: 5 0 0 5; -fx-background-color: transparent");

        mainPane = new VBox();
        mainPane.setMinWidth(24.0);
        mainPane.setPrefWidth(24.0);
        mainPane.setPrefHeight(24);
        mainPane.setPadding(new Insets(0, 0, 0, 8));

        mainPane.getChildren().add(brackPane);
        mainPane.setVgrow(brackPane, Priority.ALWAYS);
        BottomDragResizer resizer = new BottomDragResizer(verticalTreeView);
        resizer.makeResizable(mainPane);

        VBox mainBox = new VBox(labelPane, mainPane);
        self.getChildren().addAll(mainBox);
        mainBox.setVgrow(mainPane, Priority.ALWAYS);
        self.setBottomAnchor(mainBox, 0.0); self.setLeftAnchor(mainBox, 0.0); self.setTopAnchor(mainBox, 0.0); self.setRightAnchor(mainBox, 0.0);

        buildNodeDragHandlers();
    }

    /**
     * Set up drag events
     */
    public void buildNodeDragHandlers() {

        /*
         * for dragging in the work area
         */
        mContextDragOver = new EventHandler <DragEvent>() {
            @Override
            public void handle(DragEvent event) {
                event.acceptTransferModes(TransferMode.ANY);
                relocateToPoint(new Point2dSerial( event.getSceneX(), event.getSceneY()));
                event.consume();
            }
        };

        /*
         * for dropping in the work area
         */
        mContextDragDropped = new EventHandler <DragEvent> () {
            @Override
            public void handle(DragEvent event) {

                getParent().setOnDragOver(null);
                getParent().setOnDragDropped(null);
                event.setDropCompleted(true);

                relocateToGridPoint2( new Point2D(event.getSceneX(), event.getSceneY())   );
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
                verticalTreeView.setUndoRedoFlag(true);
                verticalTreeView.setUndoRedoFlag(false);

                parent.requestFocus();
            }

        });

        /*
         * Set up dragging by the top drag label
         */
        topDragLabel.setOnDragDetected (new EventHandler <MouseEvent> () {

            @Override
            public void handle(MouseEvent event) {

                getParent().setOnDragOver(null);
                getParent().setOnDragDropped(null);

                getParent().setOnDragOver (mContextDragOver);
                getParent().setOnDragDropped (mContextDragDropped);

                //begin drag ops
                mDragOffset = new Point2D(event.getX(), event.getY());

                relocateToPoint(   new Point2D(event.getSceneX(), event.getSceneY())   );

                ClipboardContent content = new ClipboardContent();
                DragContainer container = new DragContainer();

                container.addData ("type", mType.toString());
                content.put(DragContainer.AddNode, container);

                Dragboard db = startDragAndDrop(TransferMode.MOVE);
                db.setDragView(EditorMain.emptyImage); //force small dragging icon esp. on Mac
                db.setContent(content);

                self.requestFocus();
                event.consume();
            }
        });
    }

    /**
     * Relocate object to grid point.  Used for initial drop.
     * @param p the point in scene coordinates
     */
    public void relocateToGridPoint (Point2D p) {
        Point2D localCoords = getParent().sceneToLocal(p);
        double localY = Math.round((localCoords.getY() - 24)  / 24.0) * 24.0;
        relocate ((int) localCoords.getX() - 18, (int) localY + 3  );
        self.requestFocus();
    }

    /**
     * Relocate object to grid point.  Used for dropping from within pane
     * @param p the point in scene coordinates
     */
    public void relocateToGridPoint2 (Point2D p) {
        Point2D localCoords = getParent().sceneToLocal(p);
        double localY = Math.round((localCoords.getY() + 24) / 24.0) * 24.0 ;

        relocate ( (int) localCoords.getX() - 12,  (int) localY - 20  );
    }


    /**
     * Relocate object to point.  Used for dragging in work area
     * @param p the point in scene coordinates
     */
    public void relocateToPoint (Point2D p) {
        Point2D localCoords = getParent().sceneToLocal(p);
        relocate (   (int) ((localCoords.getX() - (getBoundsInLocal().getWidth()) / 2)),   (int) localCoords.getY()  );
    }

    /**
     * The pone which contains the bracket itself
     * @return the Anchor Pane
     */
    public VBox getMainPane() {
        return mainPane;
    }
}

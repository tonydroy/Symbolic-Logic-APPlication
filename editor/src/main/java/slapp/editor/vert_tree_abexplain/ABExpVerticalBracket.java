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

import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.control.Label;
import javafx.scene.input.*;
import javafx.scene.layout.*;
import slapp.editor.EditorMain;
import slapp.editor.vert_tree_abexplain.ABExpBottomDragResizer;
import slapp.editor.vert_tree_abexplain.VerticalTreeABExpView;
import slapp.editor.vertical_tree.drag_drop.DragContainer;
import slapp.editor.vertical_tree.drag_drop.DragIconType;
import slapp.editor.vertical_tree.drag_drop.Point2dSerial;

public class ABExpVerticalBracket extends AnchorPane {
    private VerticalTreeABExpView verticalTreeView;
    private Label topDragLabel;
    private Label closeLabel;

    private EventHandler<DragEvent> mContextDragOver;
    private EventHandler <DragEvent> mContextDragDropped;

    private DragIconType mType = DragIconType.bracket;

    private Point2D mDragOffset = new Point2D (0.0, 0.0);

    private final ABExpVerticalBracket self;

    private VBox mainPane;



    public ABExpVerticalBracket(VerticalTreeABExpView verticalTreeView) {
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
//            leftDragLabel.setCursor(Cursor.MOVE);
        });
        topDragLabel.setOnMouseExited(e -> {
            topDragLabel.setStyle("-fx-background-color: transparent");
            closeLabel.setStyle("-fx-background-color:transparent");
            //           leftDragLabel.setCursor(Cursor.DEFAULT);
        });

        closeLabel.setOnMouseEntered(e -> {
            topDragLabel.setStyle("-fx-background-color: grey; -fx-background-radius: 8 0 0 8");
            closeLabel.setStyle("-fx-background-color: black; -fx-background-radius: 0 8 8 0");
//            leftDragLabel.setCursor(Cursor.MOVE);
        });
        closeLabel.setOnMouseExited(e -> {
            topDragLabel.setStyle("-fx-background-color: transparent");
            closeLabel.setStyle("-fx-background-color:transparent");
            //           leftDragLabel.setCursor(Cursor.DEFAULT);
        });


        Pane brackPane = new Pane();
        brackPane.setMinWidth(8.0); brackPane.setMaxWidth(8.0);
//        brackPane.setMinHeight(24); brackPane.setMaxHeight(24);
        brackPane.setStyle("-fx-border-width: 1.5 0.0 1.5 1.5; -fx-border-color: black transparent black black; -fx-border-radius: 5 0 0 5; -fx-background-color: transparent");

        mainPane = new VBox();
        mainPane.setMinWidth(24.0);
        mainPane.setPrefWidth(24.0);
        mainPane.setPrefHeight(24);
        mainPane.setPadding(new Insets(0, 0, 0, 8));

        mainPane.getChildren().add(brackPane);
        mainPane.setVgrow(brackPane, Priority.ALWAYS);
        ABExpBottomDragResizer resizer = new ABExpBottomDragResizer(verticalTreeView);
        resizer.makeResizable(mainPane);

        VBox mainBox = new VBox(labelPane, mainPane);
        self.getChildren().addAll(mainBox);
        mainBox.setVgrow(mainPane, Priority.ALWAYS);
        self.setBottomAnchor(mainBox, 0.0); self.setLeftAnchor(mainBox, 0.0); self.setTopAnchor(mainBox, 0.0); self.setRightAnchor(mainBox, 0.0);

        initialize();
    }

    private void initialize() {
        buildNodeDragHandlers();
    }

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



        topDragLabel.setOnDragDetected (new EventHandler <MouseEvent> () {

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
      //          startDragAndDrop (TransferMode.ANY).setContent(content);
                self.requestFocus();
                event.consume();
            }

        });

    }

    public void relocateToGridPoint (Point2D p) {

        //relocates the object to a point that has been converted to
        //scene coordinates
        Point2D localCoords = getParent().sceneToLocal(p);


        double localY = Math.round((localCoords.getY() - 24)  / 24.0) * 24.0;

        relocate (
                (int) localCoords.getX() - 18,
                (int) localY + 3
        );
        self.requestFocus();
    }


    // I don't understand why both this and the following method are required to drop on line - should be same w/o offset localY??
    public void relocateToGridPoint2 (Point2D p) {

        //relocates the object to a point that has been converted to
        //scene coordinates
        Point2D localCoords = getParent().sceneToLocal(p);


        double localY = Math.round((localCoords.getY() + 24) / 24.0) * 24.0 ;

        relocate (
                (int) localCoords.getX() - 12,
                //        (int) ((localCoords.getX() - (getBoundsInLocal().getWidth()) / 2)),
                (int) localY - 20


                //             (int) (localCoords.getY() - (getBoundsInLocal().getHeight() / 2 ))
        );
    }




    public void relocateToPoint (Point2D p) {

        //relocates the object to a point that has been converted to
        //scene coordinates
        Point2D localCoords = getParent().sceneToLocal(p);


        //       double localY = Math.round(localCoords.getY() / 24.0) * 24.0;

        relocate (
  //              (int) localCoords.getX(),
                           (int) ((localCoords.getX() - (getBoundsInLocal().getWidth()) / 2)),

                (int) localCoords.getY()
  //              (int) ((localCoords.getY() - (getBoundsInLocal().getHeight()) / 2 ))
        );
    }

    public VBox getMainPane() {
        return mainPane;
    }
}

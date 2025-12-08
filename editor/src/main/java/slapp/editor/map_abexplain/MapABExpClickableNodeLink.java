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

package slapp.editor.map_abexplain;

import slapp.editor.map_abexplain.MapABExpTreeFormulaBox;
import com.gluonhq.richtextarea.RichTextArea;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.Cursor;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;

import java.util.UUID;

import static javafx.beans.binding.Bindings.add;
import static javafx.beans.binding.Bindings.subtract;



public class MapABExpClickableNodeLink extends Pane {

    MapABExpView mapView;
    Line node_link;
    Line node_link1;

    String idString;
    String sourceId;
    String targetId;


    public MapABExpClickableNodeLink(MapABExpView mapView) {
        this.mapView = mapView;
        this.getStylesheets().add("/drag_drop.css");

        this.setPickOnBounds(false);
        this.setStyle("-fx-background-color: null");

        node_link = new Line(); node_link1 = new Line();
        this.getChildren().addAll(node_link, node_link1);

        //provide a universally unique identifier for this object
        setId(UUID.randomUUID().toString());
        idString = getId();

        node_link1.setStrokeWidth(7);
        node_link1.setStroke(Color.TRANSPARENT);
        node_link1.setOnMouseEntered(e -> setCursor(Cursor.HAND));
        node_link1.setOnMouseExited(e -> setCursor(Cursor.DEFAULT));
        node_link1.setOnMouseClicked(e -> {
            if (e.getButton() == MouseButton.SECONDARY) {
                AnchorPane parent = (AnchorPane) this.getParent();
                parent.getChildren().remove(this);
                mapView.setUndoRedoFlag(true);
                mapView.setUndoRedoFlag(false);
            }
        });
    }



    //This makes the live part of the line just the center - revert to whole line?
    public void bindEnds (MapABExpTreeFormulaBox source, MapABExpTreeFormulaBox target) {
        sourceId = source.getId();
        targetId = target.getId();

        DoubleProperty centerDivisor = new SimpleDoubleProperty(2.0);
        DoubleProperty startFraction = new SimpleDoubleProperty(0.25);
        DoubleProperty endFraction = new SimpleDoubleProperty(0.75);
        DoubleProperty deltaXProperty = new SimpleDoubleProperty();
        DoubleProperty deltaYProperty = new SimpleDoubleProperty();


        RichTextArea simpleSource = source.getFormulaBox().getRTA();
        RichTextArea simpleTarget = target.getFormulaBox().getRTA();
        node_link.startXProperty().bind(add(add(source.layoutXProperty(), source.getCenterBox().layoutXProperty()), (simpleSource.widthProperty().divide(centerDivisor))));
        node_link.endXProperty().bind(add(add(target.layoutXProperty(), target.getCenterBox().layoutXProperty()), (simpleTarget.widthProperty().divide(centerDivisor))));

//        node_link.startXProperty().bind(add(source.layoutXProperty(), (source.widthProperty().divide(centerDivisor))));
//        node_link.endXProperty().bind(add(target.layoutXProperty(), (target.widthProperty().divide(centerDivisor))));
        deltaXProperty.bind(subtract(node_link.endXProperty(), node_link.startXProperty()));
        node_link1.startXProperty().bind(add(node_link.startXProperty(), (deltaXProperty.multiply(startFraction))));
        node_link1.endXProperty().bind(add(node_link.startXProperty(), (deltaXProperty.multiply(endFraction))));

 //       node_link1.startXProperty().bind(add(source.layoutXProperty(), (source.widthProperty().divide(centerDivisor))));
 //       node_link1.endXProperty().bind(add(target.layoutXProperty(), (target.widthProperty().divide(centerDivisor))));

        DoubleProperty sourceBoxHeightProperty = new SimpleDoubleProperty();
        sourceBoxHeightProperty.bind(add(source.getMiddleBox().heightProperty(), 9.0));

        DoubleProperty targetBoxHeightProperty = new SimpleDoubleProperty();
        targetBoxHeightProperty.bind(add(target.getMiddleBox().heightProperty(), 9.0));

        if (source.getLayoutY() < target.getLayoutY()) {
            node_link.startYProperty().bind(add(source.layoutYProperty(), sourceBoxHeightProperty));
            node_link.endYProperty().bind(add(target.layoutYProperty(), 9.0));
  //          node_link1.startYProperty().bind(add(source.layoutYProperty(), (source.getHeight() - 9.0)));
  //          node_link1.endYProperty().bind(add(target.layoutYProperty(), 9.0));
        } else {
            node_link.startYProperty().bind(add(source.layoutYProperty(), 9.0));
            node_link.endYProperty().bind(add(target.layoutYProperty(), targetBoxHeightProperty));
 //           node_link1.startYProperty().bind(add(source.layoutYProperty(), 9.0));
 //           node_link1.endYProperty().bind(add(target.layoutYProperty(), (target.getHeight() - 9.0)));
        }
        deltaYProperty.bind(subtract(node_link.endYProperty(), node_link.startYProperty()));
        node_link1.startYProperty().bind(add(node_link.startYProperty(), (deltaYProperty.multiply(startFraction))));
        node_link1.endYProperty().bind(add(node_link.startYProperty(), (deltaYProperty.multiply(endFraction))));

        source.registerLink (getId());
        target.registerLink (getId());

        this.requestFocus();
    }

    public String getIdString() {
        return idString;
    }

    public String getSourceId() {
        return sourceId;
    }

    public String getTargetId() {
        return targetId;
    }
}

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

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.Cursor;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import slapp.editor.vert_tree_abexplain.ABExpMapFormulaBox;

import java.util.Arrays;
import java.util.UUID;

import static javafx.beans.binding.Bindings.add;
import static javafx.beans.binding.Bindings.subtract;

public class MapABExpClickableMapLink extends Pane {

    MapABExpView mapView;
    Line node_link;
    Line node_link1;

    String idString;
    String sourceId;
    String targetId;

    int sourceMapStage;
    int targetMapStage;
    Double[] sourceXAnchors;
    Double[] targetXAnchors;


    public MapABExpClickableMapLink(MapABExpView mapView) {
        this.mapView = mapView;
        this.getStylesheets().add("/drag_drop.css");

        this.setPickOnBounds(false);
        this.setStyle("-fx-background-color: null");

        node_link = new Line();
        node_link1 = new Line();


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

        this.getChildren().addAll(node_link, node_link1);

    }

    public void bindEnds (MapABExpMapFormulaBox box1, MapABExpMapFormulaBox box2) {
        MapABExpMapFormulaBox source;
        MapABExpMapFormulaBox target;


        if (box1.getLayoutY() < box2.getLayoutY()) {
            source = box1;
            target = box2;
        } else {
            source = box2;
            target = box1;
        }

        sourceId = source.getIdString();
        targetId = target.getIdString();
        sourceMapStage = source.getMapStage();
        targetMapStage = target.getMapStage();
        sourceXAnchors = Arrays.copyOf(source.getMapXAnchors(), 2);
        targetXAnchors = Arrays.copyOf(target.getMapXAnchors(), 2);


        DoubleProperty startFraction = new SimpleDoubleProperty(0.25);
        DoubleProperty endFraction = new SimpleDoubleProperty(0.75);
        DoubleProperty deltaXProperty = new SimpleDoubleProperty();
        DoubleProperty deltaYProperty = new SimpleDoubleProperty();

        DoubleProperty sourceBoxHeightProperty = new SimpleDoubleProperty();
        DoubleProperty sourceBrackHeightProperty = new SimpleDoubleProperty();
        DoubleProperty sourceOffsetXProperty = new SimpleDoubleProperty();

        DoubleProperty targetOffsetXProperty = new SimpleDoubleProperty();



        if (source.getMapStage() == 1) {
            sourceOffsetXProperty.bind(add(source.layoutXProperty(), 3.0));
            node_link.startXProperty().bind(add(sourceOffsetXProperty, source.getMapXAnchors()[0]));
            sourceBoxHeightProperty.bind(add(source.getCenterBox().heightProperty(), -4.0));
            node_link.startYProperty().bind(add(source.layoutYProperty(), sourceBoxHeightProperty));

        } else {
            if (source.getMapXAnchors()[0] > source.getMapXAnchors()[1]) {
            double temp = source.getMapXAnchors()[0];
            source.getMapXAnchors()[0] = source.getMapXAnchors()[1];
            source.getMapXAnchors()[1] = temp;
        }

            double bracketWidth = source.getMapXAnchors()[1] - source.getMapXAnchors()[0];
            Pane brackPane = getUpBracket(bracketWidth);
            this.getChildren().add(brackPane);
            brackPane.layoutXProperty().bind(add(source.layoutXProperty(), source.getMapXAnchors()[0]));
            sourceBoxHeightProperty.bind(add(source.getCenterBox().heightProperty(), 0.0));
            brackPane.layoutYProperty().bind(add(source.layoutYProperty(), sourceBoxHeightProperty));

            node_link.startXProperty().bind(add(source.layoutXProperty(), source.getMapXAnchors()[0] + bracketWidth/2.0));
            sourceBrackHeightProperty.bind(add(source.getCenterBox().heightProperty(), 4.0));
            node_link.startYProperty().bind(add(source.layoutYProperty(), sourceBrackHeightProperty));

        }

        if (target.getMapStage() == 1) {
            targetOffsetXProperty.bind(add(target.layoutXProperty(), 3.0 ));
            node_link.endXProperty().bind(add(targetOffsetXProperty, target.getMapXAnchors()[0]));
            node_link.endYProperty().bind(add(target.layoutYProperty(), 4.0));

        } else {
            if (target.getMapXAnchors()[0] > target.getMapXAnchors()[1]) {
                double temp = target.getMapXAnchors()[0];
                target.getMapXAnchors()[0] = target.getMapXAnchors()[1];
                target.getMapXAnchors()[1] = temp;
            }

            double bracketWidth = target.getMapXAnchors()[1] - target.getMapXAnchors()[0];
            Pane brackPane = getDownBracket(bracketWidth);
            this.getChildren().add(brackPane);
            brackPane.layoutXProperty().bind(add(target.layoutXProperty(), target.getMapXAnchors()[0]));
            brackPane.layoutYProperty().bind(add(target.layoutYProperty(), -4.0));

            node_link.endXProperty().bind(add(target.layoutXProperty(), target.getMapXAnchors()[0] + bracketWidth/2.0));
            node_link.endYProperty().bind(add(target.layoutYProperty(), -4.0));
        }

        deltaXProperty.bind(subtract(node_link.endXProperty(), node_link.startXProperty()));
        node_link1.startXProperty().bind(add(node_link.startXProperty(), (deltaXProperty.multiply(startFraction))));
        node_link1.endXProperty().bind(add(node_link.startXProperty(), (deltaXProperty.multiply(endFraction))));
        deltaYProperty.bind(subtract(node_link.endYProperty(), node_link.startYProperty()));
        node_link1.startYProperty().bind(add(node_link.startYProperty(), (deltaYProperty.multiply(startFraction))));
        node_link1.endYProperty().bind(add(node_link.startYProperty(), (deltaYProperty.multiply(endFraction))));

        source.registerLink (getId());
        target.registerLink (getId());

    }

    private Pane getUpBracket(double width) {
        Pane brackPane = new Pane();
        brackPane.setMinHeight(4.0); brackPane.setMaxHeight(4.0);
        brackPane.setMinWidth(width); brackPane.setMaxWidth(width);
        brackPane.setStyle("-fx-border-width: 0 1 1 1; -fx-border-color: black; -fx-border-radius: 0 0 3 3");
        return brackPane;
    }

    private Pane getDownBracket(double width) {
        Pane brackPane = new Pane();
        brackPane.setMinHeight(4.0); brackPane.setMaxHeight(4.0);
        brackPane.setMinWidth(width); brackPane.setMaxWidth(width);
        brackPane.setStyle("-fx-border-width: 1 1 0 1; -fx-border-color: black; -fx-border-radius: 3 3 0 0");
        return brackPane;
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

    public int getSourceMapStage() {
        return sourceMapStage;
    }

    public int getTargetMapStage() {
        return targetMapStage;
    }

    public Double[] getSourceXAnchors() {
        return sourceXAnchors;
    }

    public Double[] getTargetXAnchors() {
        return targetXAnchors;
    }
}

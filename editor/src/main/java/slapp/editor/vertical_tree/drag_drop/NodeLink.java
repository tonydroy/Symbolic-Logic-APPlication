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



import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Point2D;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Line;
import java.util.UUID;
import static javafx.beans.binding.Bindings.add;


public class NodeLink extends Pane {

    Line node_link;


    public NodeLink() {
        this.getStylesheets().add("/drag_drop.css");

        node_link = new Line();
        this.getChildren().add(node_link);

        //provide a universally unique identifier for this object
        setId(UUID.randomUUID().toString());


    }




    public void setStart(Point2D startPoint) {

        node_link.setStartX(startPoint.getX());
        node_link.setStartY(startPoint.getY());
    }

    public void setEnd(Point2D endPoint) {

        node_link.setEndX(endPoint.getX());
        node_link.setEndY(endPoint.getY());
    }


    public void bindEnds (TreeFormulaBox source, TreeFormulaBox target) {

        DoubleProperty constantProp = new SimpleDoubleProperty(2.0);

        node_link.startXProperty().bind(add(source.layoutXProperty(), (source.widthProperty().divide(constantProp))));
        node_link.endXProperty().bind(add(target.layoutXProperty(), (target.widthProperty().divide(constantProp))));

        if (source.getLayoutY() < target.getLayoutY()) {
            node_link.startYProperty().bind(add(source.layoutYProperty(), (source.getHeight() - 9.0)));
            node_link.endYProperty().bind(add(target.layoutYProperty(), 9.0));
        } else {
            node_link.startYProperty().bind(add(source.layoutYProperty(), 9.0));
            node_link.endYProperty().bind(add(target.layoutYProperty(), (target.getHeight() - 9.0)));
        }


        source.registerLink (getId());
        target.registerLink (getId());
    }

}
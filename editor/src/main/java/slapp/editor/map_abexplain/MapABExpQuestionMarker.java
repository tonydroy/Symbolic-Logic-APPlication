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
import javafx.scene.control.Label;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;

import java.util.Arrays;
import java.util.UUID;

import static javafx.beans.binding.Bindings.add;

public class MapABExpQuestionMarker extends Pane {

    MapABExpView verticalTreeView;
    Label questionLabel;

    String idString;
    String targetId;
    int targetMapStage;
    Double[] targetXAnchors;



    public MapABExpQuestionMarker(MapABExpView verticalTreeView) {
        this.verticalTreeView = verticalTreeView;
        this.getStylesheets().add("/drag_drop.css");
        this.setPickOnBounds(false);
        this.setStyle("-fx-background-color: null");

        questionLabel = new Label("?");
        questionLabel.setStyle("-fx-font-family: Noto Serif Combo; -fx-font-size: 11");

        this.getChildren().add(questionLabel);

        setId(UUID.randomUUID().toString());
        idString = getId();


        questionLabel.setOnMouseEntered(e -> setCursor(Cursor.HAND));
        questionLabel.setOnMouseExited(e -> setCursor(Cursor.DEFAULT));
        questionLabel.setOnMouseClicked(e -> {
            if (e.getButton() == MouseButton.SECONDARY) {
                AnchorPane parent = (AnchorPane) this.getParent();
                parent.getChildren().remove(this);
                verticalTreeView.setUndoRedoFlag(true);
                verticalTreeView.setUndoRedoFlag(false);
            }
        });
    }

    public void bindQuestionLabel (MapABExpFormulaBox formulaBox) {
        targetMapStage = formulaBox.getMapStage();
        targetXAnchors = Arrays.copyOf(formulaBox.getMapXAnchors(), 2);
        targetId = formulaBox.getIdString();



        DoubleProperty offsetXProperty = new SimpleDoubleProperty();

        if (formulaBox.getMapStage() == 1) {
            offsetXProperty.bind(add(formulaBox.layoutXProperty(), 3.0 ));
            questionLabel.layoutXProperty().bind(add(formulaBox.layoutXProperty(), formulaBox.getMapXAnchors()[0]));
            questionLabel.layoutYProperty().bind(add(formulaBox.layoutYProperty(), -14.0));
        } else {
            if (formulaBox.getMapXAnchors()[0] > formulaBox.getMapXAnchors()[1]) {
                double temp = formulaBox.getMapXAnchors()[0];
                formulaBox.getMapXAnchors()[0] = formulaBox.getMapXAnchors()[1];
                formulaBox.getMapXAnchors()[1] = temp;
            }

            double bracketWidth = formulaBox.getMapXAnchors()[1] - formulaBox.getMapXAnchors()[0];
            Pane brackPane = getDownBracket(bracketWidth);
            this.getChildren().add(brackPane);
            brackPane.layoutXProperty().bind(add(formulaBox.layoutXProperty(), formulaBox.getMapXAnchors()[0]));
            brackPane.layoutYProperty().bind(add(formulaBox.layoutYProperty(), -4.0));

            questionLabel.layoutXProperty().bind(add(formulaBox.layoutXProperty(), formulaBox.getMapXAnchors()[0] + bracketWidth/2.0 - 3));
            questionLabel.layoutYProperty().bind(add(formulaBox.layoutYProperty(), -16.0));


        }

        formulaBox.registerLink (getId());
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

    public String getTargetId() {
        return targetId;
    }

    public int getTargetMapStage() {
        return targetMapStage;
    }

    public Double[] getTargetXAnchors() {
        return targetXAnchors;
    }
}

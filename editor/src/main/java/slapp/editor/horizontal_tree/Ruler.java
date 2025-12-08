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

package slapp.editor.horizontal_tree;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;

//JavaFX NumberAxis is not right as its tick gap, rather than number of ticks, changes with width

/**
 * Pane with ruler line to appear at top of horizontal tree window
 */
public class Ruler extends Pane {
    double tickGap = 20.0;

    /**
     * Create the ruler pane
     */
    public Ruler() {
        this.setPadding(new Insets(5, 0, 0, 0));
    }

    /**
     * Update the ruler based on the desired witdth
     * @param width the width value
     */
    public void updateRuler(double width) {
        this.getChildren().clear();
        width = width - 10;

        int totalTicks = (int)(width/tickGap);

        Line axis = new Line(0,0, width,0);
        axis.setStyle("-fx-stroke-width: 1.2; -fx-stroke: grey");
        this.getChildren().add(axis);

        for (int tickCount = 0; tickCount <= totalTicks; tickCount++) {
            double xPos = tickCount * tickGap;
            if (tickCount % 5 == 0) {
                Line majorTick = new Line(xPos, 1, xPos, 5);
                majorTick.setStyle("-fx-stroke-width: 1.2; -fx-stroke: grey");
                this.getChildren().add(majorTick);
                Label number = new Label(String.valueOf(tickCount));
                number.setFont(new Font(8));
                this.getChildren().add(number);
                number.setLayoutX(xPos - 3);
                number.setLayoutY(7);
                number.setStyle("-fx-text-fill: grey");
            }
            else {
                Line minorTick = new Line(xPos, 1, xPos, 3);
                minorTick.setStyle("-fx-stroke-width: 1.2; -fx-stroke: grey");
                this.getChildren().add(minorTick);
            }
        }
    }

}

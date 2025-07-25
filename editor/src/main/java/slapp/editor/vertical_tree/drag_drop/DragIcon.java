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

import javafx.geometry.Point2D;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;

public class DragIcon extends AnchorPane {

//    AnchorPane root_pane;



    private DragIconType mType = null;

    public DragIcon() {
        this.getStylesheets().add("/drag_drop.css");
        this.getStyleClass().add("dragicon");
//        this.setPrefHeight(64); this.setPrefWidth(64);
//        this.setMaxWidth(64); this.setMinWidth(64); this.setMaxHeight(64); this.setMinHeight(64);
    }


    public void relocateToPoint (Point2D p) {

        //relocates the object to a point that has been converted to
        //scene coordinates
        Point2D localCoords = getParent().sceneToLocal(p);

        relocate (
                (int) (localCoords.getX() - (getBoundsInLocal().getWidth() / 2)),
                (int) (localCoords.getY() - (getBoundsInLocal().getHeight() / 2))
        );
    }

    public DragIconType getType () { return mType; }

    public void setType (DragIconType type) {

        mType = type;

        getStyleClass().clear();
        getStyleClass().add("dragicon");

        //added because the cubic curve will persist into other icons
        if (this.getChildren().size() > 0)
            getChildren().clear();

        switch (mType) {

            case TREE_FIELD:
                Rectangle rectangle = new Rectangle(48, 20);
                rectangle.setFill(Color.rgb(230, 245, 255));
                rectangle.setStrokeWidth(1.5);
                rectangle.setStroke(Color.BLACK);
                rectangle.setArcHeight(8); rectangle.setArcWidth(8);

                Line line = new Line(0,0,0,12);
                line.setStrokeWidth(1.0);
                Line topLine = new Line(0, 0, 8, 0);
                Line bottomLine = new Line(0, 12, 8, 12);

                AnchorPane fieldPane = new AnchorPane(rectangle, line, topLine, bottomLine);
                fieldPane.setLeftAnchor(line, 8.0);
                fieldPane.setTopAnchor(line, 3.0);

                fieldPane.setLeftAnchor(topLine, 4.0);
                fieldPane.setTopAnchor(topLine, 3.0);

                fieldPane.setLeftAnchor(bottomLine, 4.0);
                fieldPane.setTopAnchor(bottomLine, 16.0);

                this.setPrefHeight(24); this.setPrefWidth(64);
                this.setMaxWidth(64); this.setMinWidth(64); this.setMaxHeight(24); this.setMinHeight(24);

                getChildren().add(fieldPane);
  //              this.setLeftAnchor(fieldPane, 8.0);
  //              this.setTopAnchor(fieldPane, 20.0);
                break;

            case MAP_FIELD:
                Rectangle mrectangle = new Rectangle(48, 20);
                mrectangle.setFill(Color.rgb(230, 255, 245));
                mrectangle.setStrokeWidth(1.5);
                mrectangle.setStroke(Color.BLACK);
                mrectangle.setArcHeight(8); mrectangle.setArcWidth(8);

                Line mline = new Line(0,0,0,12);
                mline.setStrokeWidth(1.0);
                Line mtopLine = new Line(0, 0, 8, 0);
                Line mbottomLine = new Line(0, 12, 8, 12);

                AnchorPane mfieldPane = new AnchorPane(mrectangle, mline, mtopLine, mbottomLine);
                mfieldPane.setLeftAnchor(mline, 8.0);
                mfieldPane.setTopAnchor(mline, 3.0);

                mfieldPane.setLeftAnchor(mtopLine, 4.0);
                mfieldPane.setTopAnchor(mtopLine, 3.0);

                mfieldPane.setLeftAnchor(mbottomLine, 4.0);
                mfieldPane.setTopAnchor(mbottomLine, 16.0);

                this.setPrefHeight(24); this.setPrefWidth(64);
                this.setMaxWidth(64); this.setMinWidth(64); this.setMaxHeight(24); this.setMinHeight(24);

                getChildren().add(mfieldPane);
                //              this.setLeftAnchor(fieldPane, 8.0);
                //              this.setTopAnchor(fieldPane, 20.0);
                break;

            case BRACKET:
                Pane brackPane = new Pane();
                brackPane.setMinWidth(8.0); brackPane.setMaxWidth(8.0);
                brackPane.setMinHeight(24); brackPane.setMaxHeight(24);
                brackPane.setStyle("-fx-border-width: 1.5 0.0 1.5 1.5; -fx-border-color: black; -fx-border-radius: 5 0 0 5");

                Rectangle bracketRec = new Rectangle(24, 32);
                bracketRec.setFill(Color.rgb(235, 235, 235));
                bracketRec.setStroke(Color.TRANSPARENT);
                bracketRec.setArcHeight(8); bracketRec.setArcWidth(8);

                AnchorPane bracketPane = new AnchorPane(bracketRec, brackPane);
                bracketPane.setLeftAnchor(brackPane, 8.0);
                bracketPane.setTopAnchor(brackPane, 4.0);

                this.setPrefHeight(24); this.setPrefWidth(64);
                this.setMaxWidth(64); this.setMinWidth(64); this.setMaxHeight(32); this.setMinHeight(32);

                getChildren().add(bracketPane);
                this.setLeftAnchor(bracketPane, 14.0);
                break;

            case DASHED_LINE:

                Rectangle dashLineRec = new Rectangle(54, 24);
                dashLineRec.setFill(Color.rgb(235, 235, 235));
                dashLineRec.setStroke(Color.TRANSPARENT);
                dashLineRec.setArcHeight(8); dashLineRec.setArcWidth(8);

                Line dashedLine = new Line(0,0,48,0);
                dashedLine.setStrokeWidth(1.5);
                dashedLine.getStrokeDashArray().addAll(5.0,5.0);

                AnchorPane dashLinePane = new AnchorPane(dashLineRec, dashedLine);
                dashLinePane.setLeftAnchor(dashedLine, 4.0);
                dashLinePane.setTopAnchor(dashedLine, 12.0);

                this.setPrefHeight(24); this.setPrefWidth(64);
                this.setMaxWidth(64); this.setMinWidth(64); this.setMaxHeight(24); this.setMinHeight(24);
                getChildren().add(dashLinePane);
      //          this.setTopAnchor(dashLinePane, 12.0);
      //          this.setLeftAnchor(dashLinePane, 8.0);
                break;



            default:
                break;
        }
    }
}

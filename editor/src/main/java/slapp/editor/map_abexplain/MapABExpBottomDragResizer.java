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

import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;

public class MapABExpBottomDragResizer {

    private MapABExpView mapView;
    private static final int RESIZE_MARGIN = 12;

    private Region region;

    private double y;

    private boolean initMinHeight;

    private boolean dragging;

    public MapABExpBottomDragResizer(MapABExpView mapView)  {
        this.mapView = mapView;
    }

    public void makeResizable(Region region) {
        this.region = region;

        region.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                mousePressed(event);
            }});
        region.setOnMouseDragged(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                mouseDragged(event);
            }});
        region.setOnMouseMoved(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                mouseOver(event);
            }});
        region.setOnMouseReleased(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                mouseReleased(event);
            }});
    }

    protected void mouseReleased(MouseEvent event) {
        dragging = false;
        region.setCursor(Cursor.DEFAULT);
        mapView.setUndoRedoFlag(true);
        mapView.setUndoRedoFlag(false);
        region.setPrefHeight(Math.round((float) region.getPrefHeight() / 24) * 24 + 3) ;  //make bottom of vertical bracket snap to center of grid row
    }

    protected void mouseOver(MouseEvent event) {
        if(isInDraggableZone(event) || dragging) {
            region.setCursor(Cursor.V_RESIZE);
        }
        else {
            region.setCursor(Cursor.DEFAULT);
        }
    }

    protected boolean isInDraggableZone(MouseEvent event) {
        return event.getY() > (region.getHeight() - RESIZE_MARGIN);
    }

    protected void mouseDragged(MouseEvent event) {
        if(!dragging) {
            return;
        }
        region.requestFocus();
        double mousey = event.getY();

 //       double newHeight = region.getMinHeight() + (mousey - y);
  //      region.setMinHeight(newHeight);

        double newHeight = region.getPrefHeight() + (mousey - y);
        region.setPrefHeight(newHeight);

        y = mousey;
    }

    protected void mousePressed(MouseEvent event) {

        // ignore clicks outside of the draggable margin
        if(!isInDraggableZone(event)) {
            return;
        }

        dragging = true;

        // make sure that the minimum height is set to the current height once,
        // setting a min height that is smaller than the current height will
        // have no effect
        if (!initMinHeight) {
     //       region.setMinHeight(region.getHeight());
            region.setPrefHeight(region.getHeight());
            initMinHeight = true;
        }

        y = event.getY();
    }
}

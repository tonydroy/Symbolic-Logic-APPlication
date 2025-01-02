package slapp.editor.vert_tree_abefexplain;

import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import slapp.editor.vert_tree_abefexplain.VerticalTreeABEFExpView;

/**
 * {@link ABEFExpRightDragResizer} can be used to add mouse listeners to a {@link Region}
 * and make it resizable by the user by clicking and dragging the border in the
 * same way as a window.
 * <p>
 * Only height resizing is currently implemented. Usage: <pre>DragResizer.makeResizable(myAnchorPane);</pre>
 *
 * @author atill
 *
 * https://gist.github.com/andytill/4369729
 *
 */
public class ABEFExpRightDragResizer {

    /**
     * The margin around the control that a user can click in to start resizing
     * the region.
     */

    VerticalTreeABEFExpView verticalTreeView;
    private static final int RESIZE_MARGIN = 12;

    private Region region;

    private double x;

    private boolean initMinWidth;

    private boolean dragging;


    public ABEFExpRightDragResizer(VerticalTreeABEFExpView verticalTreeView) {
        this.verticalTreeView = verticalTreeView;
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
            public void handle(MouseEvent event) {mouseOver(event);  }});
        region.setOnMouseReleased(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                mouseReleased(event);
            }});
    }

    protected void mouseReleased(MouseEvent event) {
        dragging = false;
        region.setCursor(Cursor.DEFAULT);
        verticalTreeView.setUndoRedoFlag(true);
        verticalTreeView.setUndoRedoFlag(false);
    }

    protected void mouseOver(MouseEvent event) {
        if(isInDraggableZone(event) || dragging) {
            region.setCursor(Cursor.H_RESIZE);
        }
        else {
            region.setCursor(Cursor.DEFAULT);
        }
    }

    protected boolean isInDraggableZone(MouseEvent event) {

        return event.getX() > (region.getWidth() - RESIZE_MARGIN);
    }

    protected void mouseDragged(MouseEvent event) {
        if(!dragging) {
            return;
        }
        region.requestFocus();
        double mousex = event.getX();

//        double newWidth = region.getMinWidth() + (mousex - x);
//        region.setMinWidth(newWidth);

        double newWidth = region.getPrefWidth() + (mousex - x);
        region.setPrefWidth(newWidth);

        x = mousex;
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
        if (!initMinWidth) {


//            region.setMinWidth(region.getWidth());
            region.setPrefWidth(region.getWidth());
            initMinWidth = true;
        }

        x = event.getX();
    }
}

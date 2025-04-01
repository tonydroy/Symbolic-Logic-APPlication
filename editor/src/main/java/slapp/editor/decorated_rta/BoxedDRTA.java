/*
Copyright (c) 2024 Tony Roy

This file is part of the Symbolic Logic APPlication (SLAPP).

SLAPP is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License
as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.

SLAPP is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License along with SLAPP.
If not, see <https://www.gnu.org/licenses/>.
 */

package slapp.editor.decorated_rta;

import com.gluonhq.richtextarea.RichTextArea;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

/**
 * DecoratedRTA and HBox with editor as member.  The RTA is very hard to focus by mouse when it is formatted like
 * a text field (with a single text line).  The boxedDRTA puts the RTA in an HBox and lets a mouse event on the
 * box focus the RTA.
 */
public class BoxedDRTA {

    private DecoratedRTA drta;
    private HBox boxedRTA;

    /**
     * Set up the new DecoratedRTA and corresponding boxedRTA
     */
    public BoxedDRTA() {
        drta = new DecoratedRTA();
        RichTextArea rta = drta.getEditor();
        rta.setContentAreaWidth(2000);
        boxedRTA = new HBox(rta);

        EventHandler<MouseEvent> mouseEventHandler = e -> {
            rta.requestFocus();
            e.consume();
        };
        boxedRTA.addEventFilter(MouseEvent.MOUSE_CLICKED, mouseEventHandler);
    }



    public void setBackgroundColor() {
        RichTextArea rta = drta.getEditor();
        rta.getStylesheets().clear();
        rta.getStylesheets().add("slappDerivationHighlight.css");
    }

    public void resetBackgroundColor() {
        RichTextArea rta = drta.getEditor();
        rta.getStylesheets().clear();
        rta.getStylesheets().add("slappDerivation.css");
    }

    /**
     * The decorated RTA created the constructor
     * @return the decoratedRTA
     */
    public DecoratedRTA getDRTA() {
        return drta;
    }

    /**
     * The boxedRTA derived from the DecoratedRTA
     * @return the boxedRTA
     */
    public HBox getBoxedRTA() {
        return boxedRTA;
    }

    /**
     * The RTA derived from the DecoratedRTA
     * @return the RTA
     */
    public RichTextArea getRTA() {return drta.getEditor(); }
}

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

import javafx.scene.input.DataFormat;
import javafx.util.Pair;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class DragContainer implements Serializable {
    private static final long serialVersionUID = 100L;

    /**
     *
     */


    public static final DataFormat AddNode =
            new DataFormat("drag_drop.DragIcon.add");

    public static final DataFormat DragNode =
            new DataFormat("drag_drop.DraggableNode.drag");

    public static final DataFormat AddLink =
            new DataFormat("drag_drop.NodeLink.add");

    private final List <Pair<String, Object> > mDataPairs = new ArrayList <Pair<String, Object> > ();



    public void addData (String key, Object value) {
        mDataPairs.add(new Pair<String, Object>(key, value));
    }

    public <T> T getValue (String key) {

        for (Pair<String, Object> data: mDataPairs) {

            if (data.getKey().equals(key))
                return (T) data.getValue();

        }

        return null;
    }

    public List <Pair<String, Object> > getData () {
        return mDataPairs;
    }
}

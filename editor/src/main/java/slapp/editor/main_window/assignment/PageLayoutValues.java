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

package slapp.editor.main_window.assignment;

import javafx.print.PageOrientation;

import java.io.Serializable;

/**
 * Serializable object to hold page setup values (except Paper)
 */
public class PageLayoutValues implements Serializable {
    private static final long serialVersionUID = 100L;
    PageOrientation orientation;
    double leftMargin;
    double rightMargin;
    double topMargin;
    double bottomMargin;

    /**
     * Create object with values from the parameters
     *
     * @param orientation the page orientation
     * @param leftMargin the left margin
     * @param rightMargin the right margin
     * @param topMargin the top margin
     * @param bottomMargin the bottom margin
     */
    public PageLayoutValues(PageOrientation orientation, double leftMargin, double rightMargin, double topMargin, double bottomMargin) {
        this.orientation = orientation;
        this.leftMargin = leftMargin;
        this.rightMargin = rightMargin;
        this.topMargin = topMargin;
        this.bottomMargin = bottomMargin;
    }

    /**
     * The {@link javafx.print.PageOrientation}
     *
     * @return the PageOrientation value
     */
    public PageOrientation getOrientation() {
        return orientation;
    }

    /**
     * The left margin
     *
     * @return the left margin
     */
    public double getLeftMargin() {
        return leftMargin;
    }

    /**
     * The right margin
     *
     * @return the right margin
     */
    public double getRightMargin() {
        return rightMargin;
    }

    /**
     * The top margin
     *
     * @return the top margin
     */
    public double getTopMargin() {
        return topMargin;
    }

    /**
     * The bottom margin
     *
     * @return the bottom margin
     */
    public double getBottomMargin() {
        return bottomMargin;
    }


}

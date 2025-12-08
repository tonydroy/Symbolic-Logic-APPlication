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

package slapp.editor.truth_table;

import javafx.scene.layout.ColumnConstraints;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.util.List;

/**
 * Contains information for head of a truth table column
 */
public class TableHeadItem {
    private TextFlow expression;
    private ColumnConstraints columnConstraints;
    boolean blankColumn = false;
    boolean dividerColumn = false;

    /**
     * Construct the head item
     * @param expression The TextFlow content
     * @param columnConstraints constraints to be applied to grid pane for this column
     */
    public TableHeadItem(TextFlow expression, ColumnConstraints columnConstraints) {
        this.expression = expression;
        this.columnConstraints = columnConstraints;
    }

    /**
     * The expression to appear in the column head
     * @return the TextFlow expression
     */
    public TextFlow getExpression() {
        return expression;
    }

    /**
     * The constraints that apply to this column of the table GridPane
     * @return
     */
    public ColumnConstraints getColumnConstraints() {
        return columnConstraints;
    }

    /**
     * A blank column is one that does not take values (usually T or F)
     * @return true if blank column and otherwise false
     */
    public boolean isBlankColumn() { return blankColumn; }

    /**
     * A blank column is one that does not take values (usually T or F)
     * @param blankColumn true if blank column and otherwise false
     */
    public void setBlankColumn(boolean blankColumn) {this.blankColumn = blankColumn;  }

    /**
     * A divider column separates the basic sentences from the rest of the table
     * @return true if divder column and otherwise false
     */
    public boolean isDividerColumn() { return dividerColumn; }

    /**
     * Divider column separates basic sentences from rest of table
     * @param dividerColumn true if divider column and otherwise false
     */
    public void setDividerColumn(boolean dividerColumn) { this.dividerColumn = dividerColumn; }





}

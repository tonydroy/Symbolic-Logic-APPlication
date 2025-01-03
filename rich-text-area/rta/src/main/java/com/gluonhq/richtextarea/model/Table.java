/*
 * Copyright (c) 2022, Gluon
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL GLUON BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.gluonhq.richtextarea.model;

import com.gluonhq.richtextarea.Selection;
import com.gluonhq.richtextarea.Tools;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.gluonhq.richtextarea.viewmodel.RichTextAreaViewModel.Direction;

/**
 * Table is a {@link Paragraph} with a single text string that contains
 * {@link TextBuffer#ZERO_WIDTH_TABLE_SEPARATOR zero width separators} to
 * indicate the cell separation, ending with an end of line character {@code "\n"}.
 *
 * The number of rows and columns of the table is defined by the {@link TableDecoration} that
 * decorates the paragraph.
 *
 * For instance, the following string corresponds to a 1 row x 2 columns table:
 *
 * <pre>
 * {@code some text|some more text<n>
 * }</pre>
 *
 * The start of the paragraph defines the start of the first cell, and the end of line
 * defines the end of the last cell. Zero width separators (in number of rows x columns - 1)
 * define the separation of the inner cells.
 */
public class Table {

    public static final Logger LOGGER = Logger.getLogger(Table.class.getName());

    private final UnitBuffer text;
    private final int start;
    private final int rows;
    private final int columns;
    private final List<Integer> positions;
    private final List<String> textCells;

    public Table(UnitBuffer text, int start, int rows, int columns) {
        this.text = text;
        this.start = start;
        this.rows = rows;
        this.columns = columns;

        positions = getTablePositions();
        textCells = getTextForCells();
    }

    public int getRows() {
        return rows;
    }

    public int getColumns() {
        return columns;
    }

    public int getTableTextLength() {
        return text.length() - (text.getInternalText().endsWith("\n") ? 1 : 0);
    }

    public boolean isCaretAtStartOfCell(int caret) {
        return caret == start || positions.stream().anyMatch(i -> i + 1 == caret);
    }

    public boolean isCaretAtEmptyCell(int caret) {
       int currentCell = getCurrentCell(caret);
       return textCells.get(currentCell).isEmpty();
    }

    public Selection getCellSelection(int caret) {
        int currentCell = getCurrentCell(caret);
        if (currentCell == 0) {
            return new Selection(start, start + textCells.get(0).length());
        } else {
            return new Selection(positions.get(currentCell - 1) + 1, positions.get(currentCell));
        }
    }

    public int getNextRow(int caret, Direction direction) {
        return getCurrentRow(caret) + (direction == Direction.DOWN ? 1 : 0);
    }

    public int getNextColumn(int caret, Direction direction) {
        return getCurrentColumn(caret) + (direction == Direction.FORWARD ? 1 : 0);
    }

    public int getCaretAt(int caret, Direction direction) {
        int currentRow = getCurrentRow(caret);
        if (direction == Direction.DOWN) {
            // move caret to end of current row
            return positions.get((currentRow + 1) * columns - 1);
        } else {
            // move caret to beginning of current row
            return currentRow == 0 ? start : positions.get(currentRow * columns - 1);
        }
    }

     public int getCaretAtNextRow(int caret, Direction direction) {
        int currentRow = getCurrentRow(caret);
        int currentCol = getCurrentColumn(caret);
        if (direction == Direction.DOWN) {
            // move caret to same column of next row, or after end of paragraph
            return currentRow < rows - 1 ?
                    positions.get((currentRow + 1) * columns + currentCol) : start + text.length();
        } else {
            // move caret to same column of previous row, or before start of paragraph
            return currentRow == 0 ?
                    Math.max(start - 1, 0) : positions.get((currentRow - 1) * columns + currentCol);
        }
    }

    public List<Integer> selectNextCell(int caret, Direction direction) {
        int currentCell = getCurrentCell(caret);
        if (direction == Direction.FORWARD) {
            // move caret to next cell, or after end of paragraph
            return currentCell < positions.size() - 1 ?
                    List.of(positions.get(currentCell) + 1, positions.get(currentCell + 1)) :
                    List.of(start + text.length());
        } else {
            // move caret to prev cell, or before start of paragraph
            return currentCell == 0 ?
                    List.of(Math.max(start - 1, 0)) :
                    List.of(currentCell == 1 ? start : positions.get(currentCell - 2) + 1, positions.get(currentCell - 1));
        }
    }

    public int getCaretAtColumn(int column) {
        return positions.get(column);
    }

    public int getRowLength(int caret) {
        int currentRow = getCurrentRow(caret);
        return getCaretAt(caret, Direction.DOWN) - getCaretAt(caret, Direction.UP) + (currentRow == 0 ? 1 : 0);
    }

    public int getCurrentRow(int caret) {
        return (int) text.getInternalText().substring(0, caret - start).codePoints()
                .filter(c -> c == TextBuffer.ZERO_WIDTH_TABLE_SEPARATOR)
                .count() / columns;
    }

    public int getCurrentColumn(int caret) {
        return (int) text.getInternalText().substring(0, caret - start).codePoints()
                .filter(c -> c == TextBuffer.ZERO_WIDTH_TABLE_SEPARATOR)
                .count() % columns;
    }

    public UnitBuffer addColumnAndGetTableText(int caret, Direction direction) {
        int currentCol = getCurrentColumn(caret);
        UnitBuffer buffer = new UnitBuffer(text.getUnitList());
        for (int i = rows - 1; i >= 0; i--) {
            int pos;
            if (direction == Direction.FORWARD) {
                // add separator to end of current column, for each row
                pos = positions.get(i * columns + currentCol) - start;
            } else {
                // add separator to beginning of current column, for each row
                pos = currentCol == 0 && i == 0 ? 0 : positions.get(i * columns + currentCol - 1) - start;
            }
            buffer.insert(new TextUnit(TextBuffer.ZERO_WIDTH_TEXT), pos);
        }
        removeLineFeed(buffer);
        return buffer;
    }

    public UnitBuffer removeColumnAndGetText(int caret) {
        int currentCol = getCurrentColumn(caret);
        UnitBuffer buffer = new UnitBuffer(text.getUnitList());
        for (int i = rows - 1; i >= 0; i--) {
            // remove text from current column, for each row
            int posStart = currentCol == 0 && i == 0 ? 0 : positions.get(i * columns + currentCol - 1) - start;
            int posEnd = positions.get(i * columns + currentCol) - start + (currentCol == 0 && i == 0 ? 1 : 0);
            buffer.remove(posStart, posEnd);
        }
        removeLineFeed(buffer);
        return buffer;
    }

    /**
     * prints the table for debugging purposes in a matrix form
     * [ ... | ... |
     * ... | ... |
     * ... | ... ]
     * Emoji anchors are replaced with ⓔ
     */
    public void printTable() {
        if (!LOGGER.isLoggable(Level.FINE)) {
            return;
        }
        String tableText = text.getInternalText()
                .replaceAll("" + TextBuffer.ZERO_WIDTH_TABLE_SEPARATOR, "|")
                .replaceAll(TextBuffer.EMOJI_ANCHOR_TEXT, "\u24D4")
                .replaceAll("\n", "]");
        int start = 0;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < rows; i++) {
            int end = positions.get((i + 1) * columns - 1);
            sb.append(tableText, start, end + 1);
            if (i < rows - 1) {
                sb.append("\n");
            }
            start = end;
        }
        LOGGER.fine("Table:\n" + sb);
    }

    private static void removeLineFeed(UnitBuffer buffer) {
        Unit unit = buffer.getUnitList().get(buffer.getUnitList().size() - 1);
        String internalText = unit.getInternalText();
        if (unit instanceof TextUnit && internalText.endsWith("\n")) {
            buffer.getUnitList().remove(unit);
            buffer.getUnitList().add(new TextUnit(internalText.substring(0, internalText.length() - 1)));
        }
    }

    private int getCurrentCell(int caret) {
        int currentRow = getCurrentRow(caret);
        int currentCol = getCurrentColumn(caret);
        return currentRow * columns + currentCol;
    }

    List<Integer> getTablePositions() {
        String internalText = text.getInternalText();
        List<Integer> positions = IntStream.iterate(internalText.indexOf(TextBuffer.ZERO_WIDTH_TABLE_SEPARATOR),
                        index -> index >= 0,
                        index -> internalText.indexOf(TextBuffer.ZERO_WIDTH_TABLE_SEPARATOR, index + 1))
                .boxed()
                .map(i -> i + start)
                .collect(Collectors.toList());
        positions.add(start + text.length() - 1);
        return positions;
    }

    private List<String> getTextForCells() {
        return Stream.of(text.getInternalText().split("" + TextBuffer.ZERO_WIDTH_TABLE_SEPARATOR))
                .map(s -> s.replace("\n", ""))
                .collect(Collectors.toList());
    }
}

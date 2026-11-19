/*
 * Copyright (c) 2022, 2023, Gluon
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
package com.gluonhq.richtextarea;

import com.gluonhq.emoji.EmojiSkinTone;
import com.gluonhq.richtextarea.model.Decoration;
import com.gluonhq.richtextarea.model.Document;
import com.gluonhq.richtextarea.model.ImageDecoration;
import com.gluonhq.richtextarea.model.Paragraph;
import com.gluonhq.richtextarea.model.ParagraphDecoration;
import com.gluonhq.richtextarea.model.PieceTable;
import com.gluonhq.richtextarea.model.Table;
import com.gluonhq.richtextarea.model.TableDecoration;
import com.gluonhq.richtextarea.model.TextBuffer;
import com.gluonhq.richtextarea.model.TextDecoration;
import com.gluonhq.richtextarea.model.UnitBuffer;
import com.gluonhq.richtextarea.viewmodel.ActionCmd;
import com.gluonhq.richtextarea.viewmodel.ActionCmdFactory;
import com.gluonhq.richtextarea.viewmodel.ActionCmdOverlineSelection;
import com.gluonhq.richtextarea.viewmodel.RichTextAreaViewModel;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.binding.ObjectBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.css.CssMetaData;
import javafx.css.Styleable;
import javafx.css.StyleableProperty;
import javafx.css.StyleablePropertyFactory;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.skin.ListViewSkin;
import javafx.scene.control.skin.VirtualFlow;
import javafx.scene.image.Image;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Path;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;

import static com.gluonhq.richtextarea.CharModification.*;
import static com.gluonhq.richtextarea.CharModification.SLASH;
import static com.gluonhq.richtextarea.viewmodel.RichTextAreaViewModel.Direction;
import static java.util.Map.entry;

import java.util.logging.Logger;
import javafx.scene.input.KeyCode;
import javafx.util.Duration;

import static javafx.scene.input.KeyCode.*;
import static javafx.scene.input.KeyCombination.*;
import static javafx.scene.text.FontPosture.ITALIC;
import static javafx.scene.text.FontPosture.REGULAR;
import static javafx.scene.text.FontWeight.BOLD;
import static javafx.scene.text.FontWeight.NORMAL;


public class RichTextAreaSkin extends SkinBase<RichTextArea> {
    private RichTextArea control;

    static final Logger LOG = Logger.getLogger(RichTextAreaSkin.class.getName());

    interface ActionBuilder extends Function<KeyEvent, ActionCmd>{}

    // TODO need to find a better way to find next row caret position
    private final RichTextAreaViewModel viewModel = new RichTextAreaViewModel(this::getNextRowPosition, this::getNextTableCellPosition);

    public static final double DEFAULT_FONT_SIZE = 11;  //changed from 14

    private static final ActionCmdFactory ACTION_CMD_FACTORY = new ActionCmdFactory();

    //ai added
    private int lastScrollIndex = -1;
    private long lastScrollTime = 0;
    private long lastPixelScrollTime = 0;
    private final CaretTracker caretTracker;
    private ParagraphListView paragraphListView;
    private double lastReportedFullHeight = -1.0;

    // Cache these so we only instantiate them once
    private RichListCell measurementCell;
    private Scene measurementScene;
    private boolean initialFocusHandled = false;



    // Wait 250ms after the last change before calculating.
// You can lower this to 100ms if you want it to feel more instant.
    private final PauseTransition heightDebouncer = new PauseTransition(Duration.millis(250));

    


    private Map<KeyCodeCombination, String> keyPressedCharMap;
    private Map<Character, String> keyTypedCharMap;
    private final Map<Character, String> BASE_MAP = initializeBaseMap();
    private final Map<KeyCodeCombination, String> ITALIC_ALT_MAP = initializeItalicAltMap();
    private final Map<Character, String> SANS_MAP = initializeSansMap();
    private final Map<KeyCodeCombination, String> SCRIPT_ALT_MAP = initializeScriptAltMap();
    private final Map<Character, String> ITALIC_MAP = initializeItalicMap();
    private final Map<Character, String> SCRIPT_MAP = initializeScriptMap();
    private final Map<Character, String> GREEK_MAP = initializeGreekMap();
    private final Map<KeyCodeCombination, String> SANS_ALT_MAP = initializeSansAltMap();
    private final Map<KeyCodeCombination, String> BLACKBOARD_MAP = initializeBlackboardMap();
    private final Map<KeyCodeCombination, String> FRAKTUR_MAP = initializeFrakturMap();
    private final Map<KeyCodeCombination, String> SPECIAL_CHARACTER_MAP = initializeSpecialCharacterMap();
    private final Map<KeyCodeCombination, String> SANS_ITALIC_ALT_MAP = initializeSansItalicAltMap();
    private final Map<KeyCodeCombination, String> SPECIAL_WITH_ITALIC_MAP;
    private final Map<KeyCodeCombination, String> SPECIAL_WITH_SANS_MAP;
    private final Map<KeyCodeCombination, String> SPECIAL_WITH_SCRIPT_MAP;
    private final Map<KeyCodeCombination, String> SPECIAL_WITH_BLACKBOARD_MAP;
    private final Map<KeyCodeCombination, String> SPECIAL_WITH_FRAKTUR_MAP;
    private final Map<KeyCodeCombination, String> SPECIAL_WITH_SANS_ITAL_MAP;

    private DoubleProperty nodesWidth = new SimpleDoubleProperty();

    private BooleanProperty overlineOn = new SimpleBooleanProperty(false);
    private boolean resettingOverline = false;
    private ObjectProperty<KeyMapValue> keyMapState = new SimpleObjectProperty<>(KeyMapValue.BASE);
    private ObjectProperty<KeyMapValue> lastKeyMapState = new SimpleObjectProperty<>(KeyMapValue.BASE);

//    private CharModification modification = NONE;
    
    private ObjectProperty<CharModification> modification = new SimpleObjectProperty<>(CharModification.NONE);
    private CharModMaps charModMaps = new CharModMaps();

    public void setMaps(KeyMapValue request) {
        switch (request) {
            case BASE: {
                keyPressedCharMap = SPECIAL_WITH_ITALIC_MAP;
                keyTypedCharMap = BASE_MAP;
                break;
            }
            case BASE_AND_SCRIPT: {
                keyPressedCharMap = SPECIAL_WITH_SCRIPT_MAP;
                keyTypedCharMap = BASE_MAP;
                break;
            }
            case BASE_AND_SANS: {
                keyPressedCharMap = SPECIAL_WITH_SANS_MAP;
                keyTypedCharMap = BASE_MAP;
                break;
            }
            case BASE_AND_SANS_ITALIC: {
                keyPressedCharMap = SPECIAL_WITH_SANS_ITAL_MAP;
                keyTypedCharMap = BASE_MAP;
                break;
            }
            case ITALIC_AND_SANS: {
                keyPressedCharMap = SPECIAL_WITH_SANS_MAP;
                keyTypedCharMap = ITALIC_MAP;
                break;
            }
            case SANS_AND_ITALIC: {
                keyPressedCharMap = SPECIAL_WITH_ITALIC_MAP;
                keyTypedCharMap = SANS_MAP;
                break;
            }
            case SCRIPT_AND_ITALIC: {
                keyPressedCharMap = SPECIAL_WITH_ITALIC_MAP;
                keyTypedCharMap = SCRIPT_MAP;
                break;
            }
            case SCRIPT_AND_SANS: {
                keyPressedCharMap = SPECIAL_WITH_SANS_MAP;
                keyTypedCharMap = SCRIPT_MAP;
                break;
            }
            case ITALIC_AND_BLACKBOARD: {
                keyPressedCharMap = SPECIAL_WITH_BLACKBOARD_MAP;
                keyTypedCharMap = ITALIC_MAP;
                break;
            }
            case GREEK_AND_FRAKTUR: {
                keyPressedCharMap = SPECIAL_WITH_FRAKTUR_MAP;
                keyTypedCharMap = GREEK_MAP;
                break;
            }
        }
    }



    private final Map<KeyCombination, ActionBuilder> INPUT_MAP = Map.ofEntries(
            entry(new KeyCodeCombination(RIGHT, SHIFT_ANY, ALT_ANY, CONTROL_ANY, SHORTCUT_ANY), e -> ACTION_CMD_FACTORY.caretMove(Direction.FORWARD, e)),
            entry(new KeyCodeCombination(LEFT, SHIFT_ANY, ALT_ANY, CONTROL_ANY, SHORTCUT_ANY), e -> ACTION_CMD_FACTORY.caretMove(Direction.BACK, e)),
            entry(new KeyCodeCombination(DOWN, SHIFT_ANY, ALT_ANY, SHORTCUT_ANY), e -> ACTION_CMD_FACTORY.caretMove(Direction.DOWN, e)),
            entry(new KeyCodeCombination(UP, SHIFT_ANY, ALT_ANY, SHORTCUT_ANY), e -> ACTION_CMD_FACTORY.caretMove(Direction.UP, e)),
            entry(new KeyCodeCombination(HOME, SHIFT_ANY), e -> ACTION_CMD_FACTORY.caretMove(Direction.BACK, e.isShiftDown(), false, true)),
            entry(new KeyCodeCombination(END, SHIFT_ANY), e -> ACTION_CMD_FACTORY.caretMove(Direction.FORWARD, e.isShiftDown(), false, true)),
            entry(new KeyCodeCombination(A, SHORTCUT_DOWN), e -> ACTION_CMD_FACTORY.selectAll()),
            entry(new KeyCodeCombination(C, SHORTCUT_DOWN), e -> ACTION_CMD_FACTORY.copy()),
            entry(new KeyCodeCombination(X, SHORTCUT_DOWN), e -> ACTION_CMD_FACTORY.cut()),
            entry(new KeyCodeCombination(V, SHORTCUT_DOWN), e -> ACTION_CMD_FACTORY.paste()),
            entry(new KeyCodeCombination(Z, SHORTCUT_DOWN, SHIFT_ANY), e -> e.isShiftDown() ? ACTION_CMD_FACTORY.redo() : ACTION_CMD_FACTORY.undo()),
            entry(new KeyCodeCombination(ENTER, SHIFT_ANY), e -> {
                ParagraphDecoration decoration = viewModel.getDecorationAtParagraph();
                Paragraph paragraph = viewModel.getParagraphWithCaret().orElse(null);
                if (decoration != null && decoration.getGraphicType() != ParagraphDecoration.GraphicType.NONE) {
                    int level = decoration.getIndentationLevel();
                    if (level > 0 && paragraph != null && viewModel.isEmptyParagraph(paragraph)) {
                        // on empty paragraphs, Enter is the same as shift+tab
                        return ACTION_CMD_FACTORY.decorate(ParagraphDecoration.builder().fromDecoration(decoration).indentationLevel(level - 1).build());
                    }
                } else if (paragraph != null && paragraph.getStart() < paragraph.getEnd() &&
                        decoration != null && decoration.hasTableDecoration()) {
                    int caretPosition = viewModel.getCaretPosition();
                    UnitBuffer buffer = new UnitBuffer();
                    viewModel.walkFragments((u, d) -> buffer.append(u), paragraph.getStart(), paragraph.getEnd());
                    Table table = new Table(buffer,
                            paragraph.getStart(), decoration.getTableDecoration().getRows(), decoration.getTableDecoration().getColumns());
                    // move up/down rows
                    int nextCaretAt = table.getCaretAtNextRow(caretPosition, e.isShiftDown() ? Direction.UP : Direction.DOWN);
                    viewModel.setCaretPosition(nextCaretAt);
                    if (nextCaretAt == 0 || nextCaretAt == viewModel.getTextLength()) {
                        // insert new line before/after the table and reset decoration
                        return ACTION_CMD_FACTORY.insertAndDecorate("\n", ParagraphDecoration.builder().presets().build());
                    }
                    return null;
                }
                if (getSkinnable().getOnAction() != null && !e.isShiftDown()) {
                    getSkinnable().getOnAction().handle(new ActionEvent());
                    return null;
                }
                return ACTION_CMD_FACTORY.insertText("\n");
            }),
            entry(new KeyCodeCombination(BACK_SPACE, SHIFT_ANY), e -> {
                int caret = viewModel.getCaretPosition();
                Paragraph paragraph = viewModel.getParagraphWithCaret().orElse(null);
                ParagraphDecoration decoration = viewModel.getDecorationAtParagraph();
                if (decoration != null && paragraph != null) {
                    if (decoration.hasTableDecoration()) {
                        UnitBuffer buffer = new UnitBuffer();
                        viewModel.walkFragments((u, d) -> buffer.append(u), paragraph.getStart(), paragraph.getEnd());
                        Table table = new Table(buffer,
                                paragraph.getStart(), decoration.getTableDecoration().getRows(), decoration.getTableDecoration().getColumns());
                        if (table.isCaretAtStartOfCell(caret)) {
                            // check backspace at beginning of each cell to prevent moving text from one cell to the other.
                            // and just move caret if cell was empty:
                            if (table.isCaretAtEmptyCell(caret)) {
                                return ACTION_CMD_FACTORY.caretMove(Direction.BACK, false, false, false);
                            }
                            return null;
                        }
                    } else if (paragraph.getStart() == caret) {
                        // check backspace at beginning of paragraph:
                        if (decoration.getGraphicType() != ParagraphDecoration.GraphicType.NONE) {
                            return ACTION_CMD_FACTORY.decorate(ParagraphDecoration.builder().fromDecoration(decoration).graphicType(ParagraphDecoration.GraphicType.NONE).build());
                        } else if (decoration.getIndentationLevel() > 0) {
                            //decrease indentation level
                            return ACTION_CMD_FACTORY.decorate(ParagraphDecoration.builder().fromDecoration(decoration).indentationLevel(decoration.getIndentationLevel() - 1).build());
                        } else {
                            // if previous paragraph is a table:
                            int index = viewModel.getParagraphList().indexOf(paragraph);
                            if (index > 0) {
                                if (viewModel.getParagraphList().get(index - 1).getDecoration().hasTableDecoration()) {
                                    // just move to last cell
                                    return ACTION_CMD_FACTORY.caretMove(Direction.BACK, false, false, false);
                                }
                            }
                        }
                    }
                }
                return ACTION_CMD_FACTORY.removeText(-1);
            }),
            entry(new KeyCodeCombination(BACK_SPACE, SHORTCUT_DOWN, SHIFT_ANY), e -> {
                int caret = viewModel.getCaretPosition();
                Paragraph paragraph = viewModel.getParagraphWithCaret().orElse(null);
                ParagraphDecoration decoration = viewModel.getDecorationAtParagraph();
                if (paragraph != null && decoration != null && decoration.hasTableDecoration()) {
                    // TODO: remove cell content, else if empty move to prev cell
                    return null;
                } else if (paragraph != null && paragraph.getStart() == caret) {
                    // if previous paragraph is a table:
                    int index = viewModel.getParagraphList().indexOf(paragraph);
                    if (index > 0) {
                        if (viewModel.getParagraphList().get(index - 1).getDecoration().hasTableDecoration()) {
                            // just move to last cell
                            return ACTION_CMD_FACTORY.caretMove(Direction.BACK, false, false, false);
                        }
                    }
                }
                if (Tools.MAC) {
                    // CMD + BACKSPACE or CMD + SHIFT + BACKSPACE removes line in Mac
                    return ACTION_CMD_FACTORY.removeText(0, RichTextAreaViewModel.Remove.LINE);
                }
                // CTRL + BACKSPACE removes word in Windows and Linux
                // SHIFT + CTRL + BACKSPACE removes line in Windows and Linux
                return ACTION_CMD_FACTORY.removeText(0, e.isShiftDown() ? RichTextAreaViewModel.Remove.LINE : RichTextAreaViewModel.Remove.WORD);
            }),
            entry(new KeyCodeCombination(BACK_SPACE, ALT_DOWN), e -> {
                if (Tools.MAC) {
                    int caret = viewModel.getCaretPosition();
                    Paragraph paragraph = viewModel.getParagraphWithCaret().orElse(null);
                    ParagraphDecoration decoration = viewModel.getDecorationAtParagraph();
                    if (paragraph != null && decoration != null && decoration.hasTableDecoration()) {
                        // TODO: remove prev word from cell if any, else if empty move to prev cell, else nothing
                        return null;
                    } else if (paragraph != null && paragraph.getStart() == caret) {
                        // if previous paragraph is a table:
                        int index = viewModel.getParagraphList().indexOf(paragraph);
                        if (index > 0) {
                            if (viewModel.getParagraphList().get(index - 1).getDecoration().hasTableDecoration()) {
                                // just move to last cell
                                return ACTION_CMD_FACTORY.caretMove(Direction.BACK, false, false, false);
                            }
                        }
                    }
                    return ACTION_CMD_FACTORY.removeText(0, RichTextAreaViewModel.Remove.WORD);
                }
                return null;
            }),
            entry(new KeyCodeCombination(DELETE), e -> ACTION_CMD_FACTORY.removeText(0)),
            entry(new KeyCodeCombination(B, SHORTCUT_DOWN), e -> {
                TextDecoration decoration = (TextDecoration) viewModel.getDecorationAtCaret();
                FontWeight fontWeight = decoration.getFontWeight() == BOLD ? NORMAL : BOLD;
                return ACTION_CMD_FACTORY.decorate(TextDecoration.builder().fromDecoration(decoration).fontWeight(fontWeight).build());
            }),
            entry(new KeyCodeCombination(I, SHORTCUT_DOWN), e -> {
                TextDecoration decoration = (TextDecoration) viewModel.getDecorationAtCaret();
                FontPosture fontPosture = decoration.getFontPosture() == ITALIC ? REGULAR : ITALIC;
                return ACTION_CMD_FACTORY.decorate(TextDecoration.builder().fromDecoration(decoration).fontPosture(fontPosture).build());
            }),
            entry(new KeyCodeCombination(U, SHORTCUT_DOWN), e -> {
                TextDecoration decoration = (TextDecoration) viewModel.getDecorationAtCaret();
                Boolean underlineState = decoration.isUnderline() == true ? false : true;
                return ACTION_CMD_FACTORY.decorate(TextDecoration.builder().fromDecoration(decoration).underline(underlineState).build());
            }),


            entry(new KeyCodeCombination(PAGE_DOWN), e -> {
                TextDecoration decoration = (TextDecoration) viewModel.getDecorationAtCaret();
                Boolean subscriptState = (decoration.isSubscript() || decoration.isTransSubscript()) == true ? false : true;
                e.consume();
                return ACTION_CMD_FACTORY.decorate(TextDecoration.builder().fromDecoration(decoration).subscript(subscriptState).transSubscript(false).superscript(false).transSuperscript(false).build());
            }),
            entry(new KeyCodeCombination(PAGE_DOWN, SHIFT_DOWN), e -> {
                TextDecoration decoration = (TextDecoration) viewModel.getDecorationAtCaret();
                Boolean transSubscriptState = (decoration.isTransSubscript() || decoration.isSubscript()) == true ? false : true;
                return ACTION_CMD_FACTORY.decorate(TextDecoration.builder().fromDecoration(decoration).transSubscript(transSubscriptState).transSuperscript(false).subscript(false).superscript(false).build());
            }),


            entry(new KeyCodeCombination(PAGE_UP), e -> {
                TextDecoration decoration = (TextDecoration) viewModel.getDecorationAtCaret();
                Boolean superscriptState = (decoration.isSuperscript() || decoration.isTransSuperscript()) == true ? false : true;
                e.consume();
                return ACTION_CMD_FACTORY.decorate(TextDecoration.builder().fromDecoration(decoration).superscript(superscriptState).transSuperscript(false).subscript(false).transSubscript(false).build());
            }),
            entry(new KeyCodeCombination(PAGE_UP, SHIFT_DOWN), e -> {
                TextDecoration decoration = (TextDecoration) viewModel.getDecorationAtCaret();
                Boolean transSuperscriptState = (decoration.isTransSuperscript() || decoration.isSuperscript()) == true ? false : true;
                e.consume();
                return ACTION_CMD_FACTORY.decorate(TextDecoration.builder().fromDecoration(decoration).transSuperscript(transSuperscriptState).transSubscript(false).subscript(false).superscript(false).build());
            }),



            // to change keyboards
            entry(new KeyCodeCombination(F1), e -> {
                setKeyMapState(KeyMapValue.BASE);
                return null;
            }),
            entry(new KeyCodeCombination(F2), e-> {
                setKeyMapState(KeyMapValue.BASE_AND_SCRIPT);
                return null;
            }),
            entry(new KeyCodeCombination(F3), e-> {
                setKeyMapState(KeyMapValue.BASE_AND_SANS);
                return null;
            }),
            entry(new KeyCodeCombination(F3, SHIFT_DOWN), e -> {
                setKeyMapState(KeyMapValue.BASE_AND_SANS_ITALIC);
                return null;
            }),

            entry(new KeyCodeCombination(F4), e -> {
                setKeyMapState(KeyMapValue.ITALIC_AND_SANS);
                return null;
            }),

            entry(new KeyCodeCombination(F4, SHIFT_DOWN), e -> {
                setKeyMapState(KeyMapValue.SANS_AND_ITALIC);
                return null;
            }),


            entry(new KeyCodeCombination(F5), e -> {
                setKeyMapState(KeyMapValue.ITALIC_AND_BLACKBOARD);
                return null;
            }),
            entry(new KeyCodeCombination(F6), e -> {
                setKeyMapState(KeyMapValue.SCRIPT_AND_ITALIC);
                return null;
            }),
            entry(new KeyCodeCombination(F7), e -> {
                setKeyMapState(KeyMapValue.SCRIPT_AND_SANS);
                return null;
            }),
            entry(new KeyCodeCombination(F8), e -> {
                setKeyMapState(KeyMapValue.GREEK_AND_FRAKTUR);
                return null;
            }),
            entry(new KeyCodeCombination(F9), e-> {
                KeyMapValue temp = keyMapState.get();
                keyMapState.set(lastKeyMapState.get());
                lastKeyMapState.set(temp);
                return null;
            }),

            entry(new KeyCodeCombination(DIGIT0, SHORTCUT_DOWN), e -> {
                toggleOverlineOn();
                return null;
            }),

            //to set character modifications
            entry(new KeyCodeCombination(Z, SHORTCUT_DOWN, ALT_DOWN, SHIFT_DOWN), e -> {
                if (getModification() != OVERLINE) setModification(OVERLINE);
                else {
                    if (!overlineOn.getValue()) setModification(NONE);
                }
                return null;
            }),

            entry(new KeyCodeCombination(X, SHORTCUT_DOWN, ALT_DOWN, SHIFT_DOWN), e -> {
                if (getModification() != MAC) {
                    overlineOn.setValue(false);
                    setModification(MAC);
                }
                else setModification(NONE);
                return null;
            }),

            entry(new KeyCodeCombination(C, SHORTCUT_DOWN, ALT_DOWN, SHIFT_DOWN), e -> {
                if (getModification() != VEC) {
                    overlineOn.setValue(false);
                    setModification(VEC);
                }
                else setModification(NONE);
                return null;
            }),

            entry(new KeyCodeCombination(V, SHORTCUT_DOWN, ALT_DOWN, SHIFT_DOWN), e -> {
                if (getModification() != HAT) {
                    overlineOn.setValue(false);
                    setModification(HAT);
                }
                else setModification(NONE);
                return null;
            }),

            entry(new KeyCodeCombination(KeyCode.B, SHORTCUT_DOWN, ALT_DOWN, SHIFT_DOWN), e -> {
                if (getModification() != SLASH) {
                    overlineOn.setValue(false);
                    setModification(SLASH);
                }
                else setModification(NONE);
                return null;
            }),

            entry(new KeyCodeCombination(KeyCode.ESCAPE), e -> {
                overlineOn.setValue(false);
                setModification(NONE);
                return null;
            }),


            entry(new KeyCodeCombination(TAB, SHIFT_ANY), e -> {
                ParagraphDecoration decoration = viewModel.getDecorationAtParagraph();
                Paragraph paragraph = viewModel.getParagraphWithCaret().orElse(null);
                if (decoration != null && decoration.getGraphicType() != ParagraphDecoration.GraphicType.NONE) {
                    int level = Math.max(decoration.getIndentationLevel() + (e.isShiftDown() ? -1 : 1), 0);
                    return ACTION_CMD_FACTORY.decorate(ParagraphDecoration.builder().fromDecoration(decoration).indentationLevel(level).build());
                } else if (decoration != null && decoration.hasTableDecoration() &&
                        paragraph != null && paragraph.getStart() < paragraph.getEnd()) {
                    int caretPosition = viewModel.getCaretPosition();
                    UnitBuffer buffer = new UnitBuffer();
                    viewModel.walkFragments((u, d) -> buffer.append(u), paragraph.getStart(), paragraph.getEnd());
                    Table table = new Table(buffer,
                            paragraph.getStart(), decoration.getTableDecoration().getRows(), decoration.getTableDecoration().getColumns());
                    // select content of prev/next cell if non-empty, or move to prev/next cell
                    List<Integer> selectionAtNextCell = table.selectNextCell(caretPosition, e.isShiftDown() ? Direction.BACK : Direction.FORWARD);
                    int start = selectionAtNextCell.get(0);
                    viewModel.clearSelection();
                    viewModel.setCaretPosition(start);
                    if (selectionAtNextCell.size() == 2) {
                        int end = selectionAtNextCell.get(1);
                        if (start < end) {
                            // select content
                            return ACTION_CMD_FACTORY.selectCell(new Selection(start, end));
                        }
                    }
                }
                return null;
            })
    );

    private static final Point2D DEFAULT_POINT_2D = new Point2D(-1, -1);

 //   private final ParagraphListView paragraphListView;  //ai added above
    private final SortedList<Paragraph> paragraphSortedList = new SortedList<>(viewModel.getParagraphList(), Comparator.comparing(Paragraph::getStart));

    final ContextMenu contextMenu = new ContextMenu();

    private ObservableList<MenuItem> tableCellContextMenuItems;
    private ObservableList<MenuItem> tableContextMenuItems;
    private ObservableList<MenuItem> keyboardContextMenuItems;
    private ObservableList<MenuItem> editableContextMenuItems;
    private ObservableList<MenuItem> nonEditableContextMenuItems;

    private final EventHandler<ContextMenuEvent> contextMenuEventEventHandler = e -> {
        contextMenu.show((Node) e.getSource(), e.getScreenX(), e.getScreenY());
        e.consume();
    };

    private final Map<Integer, Font> fontCache = new ConcurrentHashMap<>();
    private final Map<String, Image> imageCache = new ConcurrentHashMap<>();
    private final SmartTimer objectsCacheEvictionTimer;

    private final Consumer<TextBuffer.Event> textChangeListener = e -> refreshTextFlow();
    int lastValidCaretPosition = -1;
    int mouseDragStart = -1;
    int dragAndDropStart = -1;
    int anchorIndex = -1;

    private final Text promptNode;



    final DoubleProperty textFlowPrefWidthProperty = new SimpleDoubleProperty() {
        @Override
        protected void invalidated() {
            if (paragraphListView != null) {
                Platform.runLater(paragraphListView::updateLayout);
            }
        }
    };


    private final ChangeListener<Number> controlPrefWidthListener;
    private int nonTextNodesCount;
    AtomicInteger nonTextNodes = new AtomicInteger();

    // attachedProperty
    private final BooleanProperty attachedProperty = new SimpleBooleanProperty() {
        @Override
        protected void invalidated() {
            if (promptVisibleBinding == null) {
                promptVisibleBinding = Bindings.createBooleanBinding(
                        () -> {
                            Point2D point2D = caretOriginProperty.get();
                            boolean visible = viewModel.getTextLength() == 0 && viewModel.getCaretPosition() == 0 &&
                                    point2D.getX() > DEFAULT_POINT_2D.getX() && point2D.getY() > DEFAULT_POINT_2D.getY();
                            if (visible) {
                                updatePromptNodeLocation();
                            }
                            return visible;
                        },
                        viewModel.caretPositionProperty(), viewModel.textLengthProperty(), caretOriginProperty);
            }

            if (get()) {
                // bind control properties to viewModel properties, to forward the changes of the later
                getSkinnable().textLengthProperty.bind(viewModel.textLengthProperty());
                getSkinnable().modifiedProperty.bind(viewModel.savedProperty().not());
                getSkinnable().selectionProperty.bind(viewModel.selectionProperty());
                getSkinnable().decorationAtCaret.bind(viewModel.decorationAtCaretProperty());
                getSkinnable().decorationAtParagraph.bind(viewModel.decorationAtParagraphProperty());
                caretPositionProperty.bind(viewModel.caretPositionProperty());
                getSkinnable().caretOriginProperty.bind(caretOriginProperty);
                getSkinnable().caretRowColumnProperty.bind(caretRowColumnProperty);
                promptNode.visibleProperty().bind(promptVisibleBinding);
                promptNode.fontProperty().bind(promptFontBinding);
            } else {
                // unbind control properties from viewModel properties, to avoid forwarding
                // the internal changes of the latter, while it performs an action
                getSkinnable().textLengthProperty.unbind();
                getSkinnable().modifiedProperty.unbind();
                getSkinnable().selectionProperty.unbind();
                getSkinnable().decorationAtCaret.unbind();
                getSkinnable().decorationAtParagraph.unbind();
                caretPositionProperty.unbind();
                getSkinnable().caretOriginProperty.unbind();
                getSkinnable().caretRowColumnProperty.unbind();
                promptNode.visibleProperty().unbind();
                promptNode.fontProperty().unbind();
            }
        }
    };


    private final ChangeListener<Document> documentChangeListener = (obs, ov, nv) -> {
        if (!attachedProperty.get()) {
            return;
        }
        if (ov == null && nv != null) {
            // new/open
            dispose();
            setup(nv);
            getSkinnable().documentProperty.set(nv);
        } else if (nv != null) {
            // save
            getSkinnable().documentProperty.set(nv);
        }
    };

    final ObjectProperty<Point2D> caretOriginProperty = new SimpleObjectProperty<>(this, "caretOrigin", DEFAULT_POINT_2D) {
        @Override
        protected void invalidated() {
            viewModel.getParagraphWithCaret().ifPresentOrElse(p -> {
                int row = viewModel.getParagraphList().indexOf(p);
                int col = caretPositionProperty.get() - p.getStart();
                caretRowColumnProperty.set(new Point2D(col, row));
            }, () -> caretRowColumnProperty.set(DEFAULT_POINT_2D));
        }
    };
    private final ObjectProperty<Point2D> caretRowColumnProperty = new SimpleObjectProperty<>(this, "caretRowColumn", DEFAULT_POINT_2D);

    private final ObjectBinding<Font> promptFontBinding = Bindings.createObjectBinding(this::getPromptNodeFont,
            viewModel.decorationAtCaretProperty(), viewModel.decorationAtParagraphProperty());
    private BooleanBinding promptVisibleBinding;

    private final IntegerProperty caretPositionProperty = new SimpleIntegerProperty() {
        @Override
        protected void invalidated() {
            int caret = get();
            int externalCaret = caret;
            if (caret > -1) {
                String text = viewModel.getTextBuffer().getText(0, caret);
                externalCaret = text.length();
            }
            //ai modified
            getSkinnable().caretPosition.set(externalCaret);
            viewModel.getParagraphWithCaret()
                    .ifPresent(paragraph -> RichTextAreaSkin.this.scrollIfNeeded());  //ai change
        }
    };

    private final InvalidationListener focusListener;
    private final EventHandler<DragEvent> dndHandler = this::dndListener;

    private final ChangeListener<Boolean> tableAllowedListener;
    private final ChangeListener<EmojiSkinTone> skinToneChangeListener;

    private final ResourceBundle resources;

    //ai added

    // ---------------------------------------------------------
    // 1. MISSING FACTORY METHOD (Fixes compilation error)
    // ---------------------------------------------------------
 //   protected RichListCell createCell() {
  //      return new RichListCell(this);
 //   }

    // =========================================================================
    // 2. BRIDGE METHOD (Connects Listeners to Layout Loop)
    // =========================================================================
    public void scrollIfNeeded () {
        if (caretTracker != null) {
            caretTracker.requestScroll();
        }
    }

    // =========================================================================
    // 3. DELEGATE METHODS (Fixes 'this::' scope in ViewModel)
    // =========================================================================
    public int getNextRowPosition(int caretPosition, boolean down) {
        if (paragraphListView == null) return caretPosition;
        return paragraphListView.getNextRowPosition (caretPosition, down);
    }

    public int getNextTableCellPosition (int caretPosition, boolean next) {
        if (paragraphListView == null) return caretPosition;
        return paragraphListView.getNextTableCellPosition (next);
    }

    //ai
    // Add this interface inside your RichTextAreaSkin class
    public interface UpdatableCell {
        void forceUpdate();
    }

    // 1. REPLACED FACTORY METHOD

    protected RichListCell createCell() {
        class DiagnosticRichListCell extends RichListCell implements UpdatableCell {
            private double stableHeight = -1.0;
            private boolean mutingLayout = false; // Layout mute shield flag

            public DiagnosticRichListCell (RichTextAreaSkin skin) {
                super (skin);
                setStyle("-fx-alignment: top-left;");
                setAlignment(javafx.geometry.Pos.TOP_LEFT);
            }

            @Override
            public void requestLayout() {
                if (mutingLayout) return; // Silences VirtualFlow flex during Stealth Redraw
                super.requestLayout();
            }

            @Override
            public void updateItem(Paragraph item, boolean empty) {
                super.updateItem(item, empty);
                int currentIndex = getIndex();

                // CRITICAL FIX: Wiping memory strictly on Empty or Offline Measurement cell!
                // DO NOT wipe on index changes! Returns shift all paragraphs down, changing their index.
                // Wiping here forces every single cell to bypass the 5px deadband and thrash layout.
                if (empty || item == null || currentIndex == -1) {
                    stableHeight = -1.0;
                }
            }

            @Override
            protected double computePrefHeight(double width) {
                Paragraph p = getItem();
                double safeWidth = width <= 0.0 ? (getWidth() > 0 ? getWidth() : (getSkinnable() != null ? getSkinnable().getContentAreaWidth() : 500.0)) : width;

                // MEASUREMENT CELL ISOLATION: No deadband, pure math.
                if (getIndex() == -1) {
                    applyCss();
                    double raw = super.computePrefHeight(safeWidth);
                    return raw > 0 ? raw + 2.0 : 24.0;
                }

                if (p == null) return stableHeight > 0 ? stableHeight : 24.0;
                if (width <= 0.0 && stableHeight > 0) return stableHeight;

                if (stableHeight == -1.0) {
                    applyCss();
                    double raw = super.computePrefHeight(safeWidth);
                    stableHeight = raw > 0 ? raw + 2.0 : 24.0;
                    return stableHeight;
                }

                double raw = super.computePrefHeight(safeWidth);

                // CRITICAL ZERO-HEIGHT SHIELD: Empty nodes can momentarily compute 0.0.
                // Defaulting to 22.0 base ensures the cell mathematically shrinks instead of freezing!
                if (raw <= 0.0) raw = 22.0;

                double padded = raw + 2.0;
                if (Math.abs(padded - stableHeight) >= 5.0) {
                    stableHeight = padded;
                }
                return stableHeight;
            }

            @Override protected double computeMinHeight(double width) { return computePrefHeight(width); }
            @Override protected double computeMaxHeight(double width) { return computePrefHeight(width); }

            @Override
            public void forceUpdate() {
                Paragraph p = getItem();
                if (p != null) {
                    mutingLayout = true; // Engage Mute Shield
                    updateItem(null, false);
                    updateItem(p, false);
                    mutingLayout = false; // Disengage Mute Shield

                    applyCss();
                    layout(); // Enforce local rendering safely
                }
            }
        }
        return new DiagnosticRichListCell(this);
    }



    //----------------------
// 4. THE CARET TRACKER (Unchained Typist V585 The Mathematical Override)
//
    // 4. THE CARET TRACKER (Unchained Typist V585 The Mathematical Override)
//
    // 4. THE CARET TRACKER (Unchained Typist V585 The Mathematical Override)
//
    // 4. THE CARET TRACKER (Unchained Typist V585 The Mathematical Override)
//
    // 4. THE CARET TRACKER (Unchained Typist V585 The Mathematical Override)
//
    // 4. THE CARET TRACKER (Unchained Typist V585 The Mathematical Override)
//
    // 4. THE CARET TRACKER (Unchained Typist V585 The Mathematical Override)
//

    // 4. THE CARET TRACKER (Unchained Typist V585 The Mathematical Override)
//
    // 4. THE CARET TRACKER (Unchained Typist V585 The Mathematical Override)
//
    // 4. THE CARET TRACKER (Unchained Typist V585 The Mathematical Override)
//
    // 4. THE CARET TRACKER (Unchained Typist V585 The Mathematical Override)
//
    // 4. THE CARET TRACKER (Unchained Typist V585 The Mathematical Override)
//
    // 4. THE CARET TRACKER (Unchained Typist V585 The Mathematical Override)
//
    // 4. THE CARET TRACKER (Unchained Typist V585 The Mathematical Override)
//
    private class CaretTracker extends javafx.animation.AnimationTimer {
        private final boolean DEBUG_MODE = false;
        private boolean isBooted = false;

        private int framesAlive = 0;
        public int blackoutFrames = 0;
        private int massiveCooldownFrames = 0;

        private int bottomPegFrames = 0;
        private boolean isTitaniumPeg = false;

        private int radarPanicStreak = 0;
        private int radarMuteFrames = 0;
        private int recentDeleteFrames = 0;
        private int mathShieldStreak = 0;

        private double lastRadarJump = 0.0;
        private int lastRadarCaretIdx = -1;
        private int lastKnownCaretIdx = -1;
        private int radarStuckStreak = 0;

        private double lastCaretTop = -1000.0;
        private int lastCaretTopPos = -1; // 🚨 V74: Binds physical coordinate to logical character position

        private double lastKick = 0.0;
        private int anchorStuckCount = 0;
        private int stuckCount = 0;

        private int yieldStreak = 0;
        private boolean bypassShield = false;
        private int collapseGuardFrames = 0;
        private int invalidLayoutStreak = 0;

        private int pinLockFrames = 0;
        private int activeAnchorFrames = 0;
        private int strictCaretPinFrames = 0;
        private boolean pendingAnchorRecovery = false;

        private int lastTextLen = -1;
        private int lastListSize = -1;
        public int lastCaretPos = -1;
        private boolean lastCaretVisible = true;
        private boolean isClamping = false;
        private boolean isProcessing = false;

        private boolean justScrolled = false;
        private boolean targetAcquired = false;
        private double estimatedRowHeight = 16.0;

        private javafx.scene.control.ScrollBar cachedVerticalScrollBar = null;

        // 🚨 PHOTOGRAPHIC MEMORY MAP
        private final java.util.Map<Integer, Double> visibleCellsMemory = new java.util.HashMap<>();
        private int memoryTopIdx = -1;
        private double memoryTopMinY = -1000.0;

        private int activeAnchorTargetIdx = -1;
        private double activeExpectedMinY = -1000.0;

        private double lastFlowPosition = -1.0;

        private javafx.scene.Node getRefNode(RichVirtualFlow flow) {
            try {
                javafx.scene.layout.Region container = (javafx.scene.layout.Region) flow.lookup(".clipped-container");
                if (container != null) return container;
            } catch (Throwable t) {}
            return flow;
        }

        private boolean isVerticalScrollBarVisible() {
            // If we already found the scrollbar, check if it's currently active/visible.
            if (cachedVerticalScrollBar != null) {
                return cachedVerticalScrollBar.isVisible() && cachedVerticalScrollBar.getOpacity() > 0.0;
            }

            // If we haven't found it yet, look it up in the Scene Graph.
            if (paragraphListView != null) {
                for (javafx.scene.Node node : paragraphListView.lookupAll(".scroll-bar")) {
                    if (node instanceof javafx.scene.control.ScrollBar) {
                        javafx.scene.control.ScrollBar sb = (javafx.scene.control.ScrollBar) node;
                        if (sb.getOrientation() == javafx.geometry.Orientation.VERTICAL) {
                            cachedVerticalScrollBar = sb; // Cache it for all future fast-checks!
                            return sb.isVisible() && sb.getOpacity() > 0.0;
                        }
                    }
                }
            }
            return false;
        }

        private boolean evaluateIsStrictStringEnd(int currentLen, int currentCaretPos) {
            return (currentLen > 0 && currentCaretPos >= currentLen - 10);
        }

        private boolean evaluateIsDocSmall(RichVirtualFlow flow, double h, int currentList) {
            if (currentList <= 0) return true;

            double expectedDocHeight = currentList * estimatedRowHeight;
            if (h > 10.0 && expectedDocHeight < (h * 1.5)) {
                return true;
            }

            int firstVis = flow.getFirstVisibleIndex();
            int lastVis = flow.getLastVisibleIndex();
            if (firstVis != -1 && lastVis != -1) {
                return (firstVis <= 4 && lastVis >= currentList - 3);
            }
            return false;
        }

        private boolean evaluateIsAbsoluteEnd(RichVirtualFlow flow, javafx.scene.Node refNode, double h, int currentLen, int currentList, int currentCaretPos, int cIdx) {
            if (evaluateIsStrictStringEnd(currentLen, currentCaretPos)) return true;
            if (currentLen > 0 && currentCaretPos < currentLen - 50) return false;

            boolean isAbs = false;
            if (currentLen > 0 && currentCaretPos >= currentLen - 25) isAbs = true;
            else if (cIdx != -1 && currentList > 0 && cIdx >= currentList - 2) isAbs = true;
            else if (lastKnownCaretIdx != -1 && lastListSize > 0 && lastKnownCaretIdx >= lastListSize - 2) isAbs = true;

            // 🚨 V74: Strictly bypass heuristic if cursor logically moved
            double currentCaretTop = (currentCaretPos == lastCaretTopPos) ? lastCaretTop : -1000.0;

            CaretInfo currentInfo = getTrueCaretBounds(cIdx, refNode, flow, false);
            if (currentInfo.bounds != null) currentCaretTop = currentInfo.bounds.getMinY();
            else if (currentInfo.cellFound && currentInfo.cellBounds != null) currentCaretTop = currentInfo.cellBounds.getMinY();

            if (isAbs && currentCaretTop != -1000.0 && h > 50) {
                if (currentCaretTop < h * 0.5) isAbs = false;
            } else if (isAbs && currentCaretTop == -1000.0 && h > 50) {
                if (!evaluateIsStrictStringEnd(currentLen, currentCaretPos)) isAbs = false;
            }
            return isAbs;
        }

        public void requestScroll() {
            if (isClamping || isProcessing) return;

            isProcessing = true;
            boolean shouldStartTimer = false;
            try {
                if (paragraphListView != null && paragraphListView.virtualFlow != null) {
                    RichVirtualFlow flow = paragraphListView.virtualFlow;
                    javafx.scene.Node refNode = getRefNode(flow);
                    double h = refNode != null ? refNode.getLayoutBounds().getHeight() : 0.0;

                    if (h < 10.0) {
                        lastTextLen = viewModel.getTextLength();
                        lastListSize = paragraphListView.getItems().size();
                        lastCaretPos = viewModel.getCaretPosition();
                        try {
                            Object pOptObj = viewModel.getParagraphWithCaret();
                            if (pOptObj != null && ((java.util.Optional<?>) pOptObj).isPresent()) {
                                lastKnownCaretIdx = paragraphListView.getItems().indexOf(((java.util.Optional<?>) pOptObj).get());
                            }
                        } catch (Throwable t) {}

                        boolean isTrackerAwake = (framesAlive > 0 || blackoutFrames > 0 || bottomPegFrames > 0 || activeAnchorFrames > 0 || collapseGuardFrames > 0 || strictCaretPinFrames > 0);
                        if (!isTrackerAwake) {
                            framesAlive = 0; blackoutFrames = 0; bottomPegFrames = 0;
                            activeAnchorFrames = 0; strictCaretPinFrames = 0; collapseGuardFrames = 0;
                            lastCaretTopPos = -1;
                        }
                        return;
                    }

                    shouldStartTimer = true;
                    boolean isTrackerAwake = (framesAlive > 0 || blackoutFrames > 0 || bottomPegFrames > 0 || activeAnchorFrames > 0 || collapseGuardFrames > 0 || strictCaretPinFrames > 0);

                    int cIdx = lastKnownCaretIdx;
                    try {
                        Object pOptObj = viewModel.getParagraphWithCaret();
                        if (pOptObj != null && ((java.util.Optional<?>) pOptObj).isPresent()) {
                            cIdx = paragraphListView.getItems().indexOf(((java.util.Optional<?>) pOptObj).get());
                        }
                    } catch (Throwable t) {}
                    if (cIdx == -1) cIdx = lastKnownCaretIdx;

                    int currentLen = viewModel.getTextLength();
                    int currentList = paragraphListView.getItems().size();
                    int currentCaretPos = viewModel.getCaretPosition();

                    if (!isTrackerAwake && lastTextLen != -1) {
                        double freshPos = flow.getPosition();
                        if (freshPos >= -0.05 && freshPos <= 1.05) lastFlowPosition = freshPos;
                        captureMemory(flow, refNode);

                        CaretInfo info = getTrueCaretBounds(cIdx, refNode, flow, false);
                        if (info.bounds != null) {
                            lastCaretTop = info.bounds.getMinY();
                            lastCaretTopPos = currentCaretPos;
                        } else if (info.cellFound && info.cellBounds != null) {
                            lastCaretTop = info.cellBounds.getMinY();
                            lastCaretTopPos = currentCaretPos;
                        }

                        if (!visibleCellsMemory.isEmpty() && cIdx != -1) {
                            int minVis = Integer.MAX_VALUE;
                            int maxVis = Integer.MIN_VALUE;
                            for (int idx : visibleCellsMemory.keySet()) {
                                if (idx < minVis) minVis = idx;
                                if (idx > maxVis) maxVis = idx;
                            }
                            lastCaretVisible = (cIdx >= minVis - 1 && cIdx <= maxVis + 1);
                        } else {
                            lastCaretVisible = true;
                        }

                        // Clean wake artifacts to prevent carry-over mutes
                        lastRadarJump = 0.0; lastRadarCaretIdx = -1; radarStuckStreak = 0;
                        anchorStuckCount = 0; stuckCount = 0; yieldStreak = 0; bypassShield = false;
                    }

                    boolean isDocSmall = evaluateIsDocSmall(flow, h, currentList);

                    if (lastTextLen == -1) {
                        lastTextLen = currentLen;
                        lastListSize = currentList;
                        lastCaretPos = currentCaretPos;
                        lastKnownCaretIdx = cIdx;
                        captureMemory(flow, refNode);
                        double fp = flow.getPosition();
                        if (fp >= -0.05 && fp <= 1.05) lastFlowPosition = fp;
                        return;
                    }

                    boolean isAbsoluteEnd = evaluateIsAbsoluteEnd(flow, refNode, h, currentLen, currentList, currentCaretPos, cIdx);
                    boolean isMutating = (currentLen != lastTextLen || currentList != lastListSize);

                    if (isMutating) {
                        processMutationEngine(flow, refNode, h, currentLen, currentList, currentCaretPos, cIdx, isAbsoluteEnd, isDocSmall);
                    }

                    double currentPos = flow.getPosition();
                    boolean isSyncPanic = (currentPos > 1.05 || currentPos < -0.05);

                    if (isSyncPanic) {
                   //     System.out.println("[DIAG-TRACKER] SYNC Panic Rescue Net Triggered: " + currentPos + " -> Restoring Ratio smoothly");
                        isClamping = true;

                        try {
                            double rescue = lastFlowPosition >= 0 ? lastFlowPosition : 0.95;
                            if (rescue <= 0.01 && currentList > 0 && memoryTopIdx > 3) {
                                rescue = ((double) memoryTopIdx / currentList);
                            }
                            if (rescue > 1.0) rescue = 1.0;
                            if (rescue < 0.0) rescue = 0.0;
                            if (!isAbsoluteEnd && rescue >= 0.99 && currentPos > 1.05) rescue = 0.98;
                            if (isAbsoluteEnd && currentPos > 1.05) rescue = 1.0;

                            if (isDocSmall && rescue >= 0.98) rescue = 0.95;

                            flow.setPosition(rescue);
                            executeAbsoluteSync(flow);
                        } catch (Throwable t) {
                     //       System.out.println("[DIAG-TRACKER] Rescue Net Exception: " + t);
                        }

                        collapseGuardFrames = Math.max(collapseGuardFrames, 3);
                        if (lastCaretVisible) pendingAnchorRecovery = true;
                        isClamping = false;
                    }

                    if (!isTrackerAwake && !isMutating) {
                        if (currentPos >= -0.05 && currentPos <= 1.05) {
                            if (!(currentPos == 0.0 && memoryTopIdx > 5)) {
                                lastFlowPosition = currentPos;
                            }
                        }
                        captureMemory(flow, refNode);
                    }
                }
            } catch (Throwable t) {
            //   System.out.println("[DIAG-TRACKER] Fatal RequestScroll Exception: " + t);
            } finally {
                isProcessing = false;
                boolean isTrackerAwake = (framesAlive > 0 || blackoutFrames > 0 || bottomPegFrames > 0 || activeAnchorFrames > 0 || collapseGuardFrames > 0 || strictCaretPinFrames > 0);
                if (shouldStartTimer || isTrackerAwake) {
                    if (shouldStartTimer) {
                        framesAlive = Math.max(framesAlive, 60);
                        stuckCount = 0;
                        bypassShield = false;
                    }
                    this.start();
                } else {
                    framesAlive = 0;
                    this.stop();
                }
            }
        }

        private void processMutationEngine(RichVirtualFlow flow, javafx.scene.Node refNode, double h, int currentLen, int currentList, int currentCaretPos, int cIdx, boolean isAbsoluteEnd, boolean isDocSmall) {
            double currentPos = flow.getPosition();
            boolean isViewAtBottom = (lastFlowPosition >= 0.95 || currentPos >= 0.95 || (isAbsoluteEnd && lastCaretVisible));

            int lenDelta = Math.abs(currentLen - lastTextLen);
            int listDelta = currentList - lastListSize;

            boolean textShrank = (currentLen < lastTextLen);
            boolean textGrew = (currentLen > lastTextLen);
            boolean listChanged = (currentList != lastListSize);
            boolean listShrank = (currentList < lastListSize);
            boolean listGrew = (currentList > lastListSize);

            boolean isShrinking = (textShrank || listShrank);
            boolean isGrowing = (textGrew || listGrew);

            boolean paragraphChanged = (cIdx != -1 && lastKnownCaretIdx != -1 && cIdx != lastKnownCaretIdx);
            boolean isMassiveEdit = (lenDelta > 20 || Math.abs(listDelta) > 1);
            boolean isStructuralEdit = (isMassiveEdit || listChanged || (paragraphChanged && (textShrank || textGrew)));
            boolean isInlineEdit = (!isStructuralEdit && (textShrank || textGrew));

            boolean useBottomPeg = isAbsoluteEnd && isViewAtBottom;

            lastKick = 0.0; stuckCount = 0; anchorStuckCount = 0;

            int minCaret = cIdx;
            if (lastKnownCaretIdx != -1 && cIdx != -1) {
                minCaret = Math.min(lastKnownCaretIdx, cIdx);
            }
            if (minCaret < 0) minCaret = 0;

            boolean keepExistingAnchor = (activeAnchorFrames > 0 && activeAnchorTargetIdx != -1 && activeExpectedMinY != -1000.0 && activeAnchorTargetIdx < minCaret);
            int protectedAnchorIdx = activeAnchorTargetIdx;
            double protectedAnchorY = activeExpectedMinY;

            activeAnchorTargetIdx = -1;
            activeExpectedMinY = -1000.0;

            if (keepExistingAnchor) {
                activeAnchorTargetIdx = protectedAnchorIdx;
                activeExpectedMinY = protectedAnchorY;
             //   System.out.println("[DIAG-TRACKER] Anchor Protection Matrix: Sequence Sabotage Blocked! Retained safe anchor: " + protectedAnchorIdx);
            } else {
                if (!visibleCellsMemory.isEmpty()) {
                    int bestAbove = -1;
                    for (int idx : visibleCellsMemory.keySet()) {
                        if (idx < minCaret && (bestAbove == -1 || idx > bestAbove)) bestAbove = idx;
                    }

                    if (bestAbove != -1) {
                        activeAnchorTargetIdx = bestAbove;
                        activeExpectedMinY = visibleCellsMemory.get(bestAbove);
                   //     System.out.println("[DIAG-TRACKER] Anchor mapped to untouched line ABOVE: " + bestAbove);
                    } else if (visibleCellsMemory.containsKey(minCaret)) {
                        activeAnchorTargetIdx = minCaret;
                        activeExpectedMinY = visibleCellsMemory.get(minCaret);
                  //      System.out.println("[DIAG-TRACKER] Anchor mapped to Top-Line Survivor: " + minCaret);
                    } else if (memoryTopIdx != -1) {
                        activeAnchorTargetIdx = memoryTopIdx;
                        if (listDelta < 0 && memoryTopIdx > minCaret) {
                            activeAnchorTargetIdx = Math.max(0, memoryTopIdx + listDelta);
                        } else if (listDelta > 0 && memoryTopIdx >= minCaret) {
                            activeAnchorTargetIdx = memoryTopIdx + listDelta;
                        }
                        activeExpectedMinY = memoryTopMinY;
                  //      System.out.println("[DIAG-TRACKER] Anchor mapped to Fallback Top Line: " + activeAnchorTargetIdx);
                    }
                } else if (memoryTopIdx != -1) {
                    activeAnchorTargetIdx = memoryTopIdx;
                    if (listDelta < 0 && memoryTopIdx > minCaret) {
                        activeAnchorTargetIdx = Math.max(0, memoryTopIdx + listDelta);
                    } else if (listDelta > 0 && memoryTopIdx >= minCaret) {
                        activeAnchorTargetIdx = memoryTopIdx + listDelta;
                    }
                    activeExpectedMinY = memoryTopMinY;
                 //   System.out.println("[DIAG-TRACKER] Anchor mapped to Fallback Top Line: " + activeAnchorTargetIdx);
                }

                if (activeAnchorTargetIdx < 0 && activeAnchorTargetIdx != -1) {
                    activeAnchorTargetIdx = 0;
                }
            }

            if (textShrank || listShrank) recentDeleteFrames = 15;
            else if (textGrew || listGrew) recentDeleteFrames = 0;

            if (isMassiveEdit) {
                blackoutFrames = 15; massiveCooldownFrames = 45;
                if (isShrinking) recentDeleteFrames = 30;
                activeAnchorTargetIdx = -1; activeExpectedMinY = -1000.0;
            } else if (!lastCaretVisible) {
                // 🚨 V74: Test 8 completely excluded as requested. Offscreen yielding left natively intact.
           //     System.out.println("[DIAG-TRACKER] Offscreen Edit - Complete Native Yield");
                collapseGuardFrames = Math.max(collapseGuardFrames, 8);
                bottomPegFrames = 0; pinLockFrames = 0; activeAnchorFrames = 0; strictCaretPinFrames = 0;
                activeAnchorTargetIdx = -1; activeExpectedMinY = -1000.0;
            } else if (isShrinking && useBottomPeg) {
            //    System.out.println("[DIAG-TRACKER] Absolute Bottom Delete - Titanium Peg (" + (isDocSmall ? "Pixel" : "Ratio") + ")");
                bottomPegFrames = Math.max(bottomPegFrames, 12);
                isTitaniumPeg = true; collapseGuardFrames = 0; pinLockFrames = 0; activeAnchorFrames = 0; strictCaretPinFrames = 0;
                activeAnchorTargetIdx = -1; activeExpectedMinY = -1000.0;
                if (h >= 10.0) {
                    if (isDocSmall) flow.scrollPixels(Math.max(h * 2.0, 1000.0)); else flow.setPosition(1.0);
                    executeAbsoluteSync(flow);
                    if (isDocSmall) flow.scrollPixels(Math.max(h * 2.0, 1000.0)); else flow.setPosition(1.0);
                }
            } else if (isGrowing && useBottomPeg) {
            //    System.out.println("[DIAG-TRACKER] Absolute Bottom Return - Fast Peg (" + (isDocSmall ? "Pixel" : "Ratio") + ")");
                bottomPegFrames = Math.max(bottomPegFrames, 3);
                isTitaniumPeg = false;
                collapseGuardFrames = 0; pinLockFrames = 0; activeAnchorFrames = 0; strictCaretPinFrames = 0;
                activeAnchorTargetIdx = -1; activeExpectedMinY = -1000.0;
                if (h >= 10.0) {
                    if (isDocSmall) flow.scrollPixels(Math.max(h * 2.0, 1000.0)); else flow.setPosition(1.0);
                    executeAbsoluteSync(flow);
                    if (isDocSmall) flow.scrollPixels(Math.max(h * 2.0, 1000.0)); else flow.setPosition(1.0);
                }
            } else if (activeAnchorTargetIdx != -1 && (isStructuralEdit || isInlineEdit) && !useBottomPeg) {
           //     System.out.println("[DIAG-TRACKER] Universal Titanium Vise Armed (" + (isStructuralEdit ? "Structural" : "Inline") + ")");
                activeAnchorFrames = Math.max(activeAnchorFrames, isStructuralEdit ? 12 : 6);
                bottomPegFrames = 0; pinLockFrames = 0; collapseGuardFrames = 0;
                radarMuteFrames = Math.max(radarMuteFrames, isStructuralEdit ? 15 : 5);
                strictCaretPinFrames = 0;
            } else if (isShrinking && activeAnchorTargetIdx == -1 && lastCaretTop != -1000.0 && !useBottomPeg) {
         //       System.out.println("[DIAG-TRACKER] Top Line Deletion Detected. Activating Strict Caret Pin.");
                strictCaretPinFrames = Math.max(strictCaretPinFrames, 8);
                collapseGuardFrames = 0; pinLockFrames = 0; activeAnchorFrames = 0;
                radarMuteFrames = 15;
            } else {
           //     System.out.println("[DIAG-TRACKER] Blind Edit - Disarming Vise");
                collapseGuardFrames = 0;
                pinLockFrames = 4; activeAnchorFrames = 0; strictCaretPinFrames = 0;
            }

            lastTextLen = currentLen; lastListSize = currentList; lastCaretPos = currentCaretPos;
            lastKnownCaretIdx = cIdx;
        }

        @Override
        public void handle(long now) {
            if (!isBooted) {
           //     System.out.println("====== V74 MASTER CARET TRACKER (TEST 4 ARROW LAG FIX) ======");
                isBooted = true;

                // 🚨 ANTI-BUBBLE SHIELD
                try {
                    if (paragraphListView != null) {
                        paragraphListView.addEventHandler(
                                javafx.scene.input.ScrollEvent.ANY,
                                event -> {
                                    // 🚨 CONDITIONAL CONSUME: Only consume if the RichTextArea has a scrollbar
                                    if (isVerticalScrollBarVisible()) {
                                        event.consume();
                                    }
                                }
                        );
                        //        System.out.println("[DIAG-TRACKER] Anti-Bubble Scroll Shield Installed.");
                    }
                    /*
                    if (paragraphListView != null) {
                        paragraphListView.addEventHandler(
                                javafx.scene.input.ScrollEvent.ANY,
                                event -> event.consume()
                        );
                //        System.out.println("[DIAG-TRACKER] Anti-Bubble Scroll Shield Installed.");
                    }
                     */
                } catch (Throwable t) {
            //        System.out.println("[DIAG-TRACKER] Failed to install Anti-Bubble Shield: " + t);
                }
            }
            if (isProcessing) return;
            isProcessing = true;
            try {
                processTrackingFrame();
            } catch (Throwable t) {
          //      System.out.println("[DIAG-TRACKER] Fatal Handle Exception: " + t);
            } finally {
                isProcessing = false;
            }
        }

        private void processTrackingFrame() {
            if (paragraphListView == null || paragraphListView.virtualFlow == null) return;
            RichVirtualFlow flow = paragraphListView.virtualFlow;

            boolean isLayoutInvalid = (flow.getScene() == null || flow.getScene().getWindow() == null || !flow.getScene().getWindow().isShowing());
            javafx.scene.Node refNode = getRefNode(flow);
            double h = refNode != null ? refNode.getLayoutBounds().getHeight() : 0.0;
            if (h < 10.0) isLayoutInvalid = true;

            boolean isTightViewport = (h <= 380.0);
            boolean isUltraTight = (h <= 150.0);

            if (isLayoutInvalid) {
                invalidLayoutStreak++;
                boolean isHolding = false;

                if (invalidLayoutStreak > 60) {
                    if (activeAnchorFrames > 0) { activeAnchorFrames--; isHolding = true; }
                    if (strictCaretPinFrames > 0) { strictCaretPinFrames--; isHolding = true; }
                    if (blackoutFrames > 0) { blackoutFrames--; isHolding = true; }
                    if (collapseGuardFrames > 0) { collapseGuardFrames--; isHolding = true; }
                    if (framesAlive > 0) { framesAlive--; isHolding = true; }
                } else {
                    isHolding = (activeAnchorFrames > 0 || strictCaretPinFrames > 0 || blackoutFrames > 0 || collapseGuardFrames > 0 || framesAlive > 0);
                }

                if (isHolding) {
                    return;
                }

                framesAlive = 0; blackoutFrames = 0; massiveCooldownFrames = 0;
                radarPanicStreak = 0; radarMuteFrames = 0; collapseGuardFrames = 0; recentDeleteFrames = 0;
                lastRadarJump = 0.0; lastRadarCaretIdx = -1; radarStuckStreak = 0; lastKnownCaretIdx = -1;
                justScrolled = false; bypassShield = false; bottomPegFrames = 0; activeAnchorFrames = 0; strictCaretPinFrames = 0; pendingAnchorRecovery = false;
                mathShieldStreak = 0; lastCaretTopPos = -1;
                this.stop();
                return;
            } else {
                invalidLayoutStreak = 0;
            }

            if (framesAlive <= 0 && blackoutFrames <= 0 && bottomPegFrames <= 0 && activeAnchorFrames <= 0 && collapseGuardFrames <= 0 && strictCaretPinFrames <= 0) {
                justScrolled = false; bypassShield = false; mathShieldStreak = 0; this.stop(); return;
            }

            if (framesAlive > 0) framesAlive--;
            if (radarMuteFrames > 0) radarMuteFrames--;
            if (massiveCooldownFrames > 0) massiveCooldownFrames--;
            if (strictCaretPinFrames > 0) strictCaretPinFrames--;

            if (blackoutFrames > 0) {
                lastFlowPosition = -1.0; blackoutFrames--;
                visibleCellsMemory.clear(); memoryTopIdx = -1; memoryTopMinY = -1000.0; activeAnchorTargetIdx = -1;
                lastKick = 0.0; lastCaretTop = -1000.0; lastCaretTopPos = -1; lastRadarJump = 0.0; lastRadarCaretIdx = -1; radarStuckStreak = 0;
                lastKnownCaretIdx = -1; stuckCount = 0; yieldStreak = 0; bypassShield = false; collapseGuardFrames = 0;
                radarPanicStreak = 0; radarMuteFrames = 0; pinLockFrames = 0; framesAlive = Math.max(framesAlive, 45);
                recentDeleteFrames = 0; bottomPegFrames = 0; activeAnchorFrames = 0; strictCaretPinFrames = 0;
                mathShieldStreak = 0;
                if (blackoutFrames == 0 && flow != null) executeAbsoluteSync(flow);
                return;
            }

            int currentListSize = paragraphListView.getItems().size();
            int caretIdx = -1;
            Object pOptObj = viewModel.getParagraphWithCaret();
            if (pOptObj != null && ((java.util.Optional<?>) pOptObj).isPresent()) {
                Paragraph targetPara = (Paragraph) ((java.util.Optional<?>) pOptObj).get();
                caretIdx = paragraphListView.getItems().indexOf(targetPara);
            }
            if (caretIdx == -1) caretIdx = lastKnownCaretIdx;

            boolean isGhostCaret = (caretIdx == -1);

            int currentTextLen = viewModel.getTextLength();
            int currentCaretPos = viewModel.getCaretPosition();

            boolean isDocSmall = evaluateIsDocSmall(flow, h, currentListSize);

            boolean isAbsoluteEnd = evaluateIsAbsoluteEnd(flow, refNode, h, currentTextLen, currentListSize, currentCaretPos, caretIdx);
            boolean isStrictStringEnd = evaluateIsStrictStringEnd(currentTextLen, currentCaretPos);

            if (lastTextLen != -1 && (currentTextLen != lastTextLen || currentListSize != lastListSize)) {
                processMutationEngine(flow, refNode, h, currentTextLen, currentListSize, currentCaretPos, caretIdx, isAbsoluteEnd, isDocSmall);
            } else if (recentDeleteFrames > 0) {
                recentDeleteFrames--;
            }

            boolean peggedToBottom = (bottomPegFrames > 0);

            // =================================================================
            // 🚨 2. ACTIVE BOTTOM PEG EXECUTION
            // =================================================================
            if (bottomPegFrames > 0) {
                bottomPegFrames--;
                if (isDocSmall) flow.scrollPixels(Math.max(h * 2.0, 1000.0)); else flow.setPosition(1.0);
                if (isTitaniumPeg && bottomPegFrames > 2) {
                    executeAbsoluteSync(flow);
                    if (isDocSmall) flow.scrollPixels(Math.max(h * 2.0, 1000.0)); else flow.setPosition(1.0);
                } else if (bottomPegFrames == 0) {
                    if (isTitaniumPeg) {
                        executeAbsoluteSync(flow);
                        if (isDocSmall) flow.scrollPixels(Math.max(h * 2.0, 1000.0)); else flow.setPosition(1.0);
                    }
                    captureMemory(flow, refNode);
                    lastFlowPosition = 1.0;
                }
                framesAlive = Math.max(framesAlive, 2);
                mathShieldStreak = 0;
                return;
            }

            // =================================================================
            // 🚨 3. VOID RESCUE NET
            // =================================================================
            if (flow.getScene() != null && flow.getScene().getWindow() != null && flow.getScene().getWindow().isShowing()) {
                double currentFlowPos = flow.getPosition();
                boolean isSyncPanic = (currentFlowPos > 1.05 || currentFlowPos < -0.05);

                if (isSyncPanic) {
                    if (activeAnchorFrames > 0 || strictCaretPinFrames > 0) {
                        // Muted. Anchor will Pre-Sync Rescue.
                    } else if (collapseGuardFrames > 0 && bottomPegFrames == 0) {
                        framesAlive = Math.max(framesAlive, 2);
                        return;
                    } else {
                        double rescuePos = lastFlowPosition >= 0 ? lastFlowPosition : 0.95;
                        if (rescuePos <= 0.01 && currentListSize > 0 && memoryTopIdx > 3) {
                            rescuePos = ((double) memoryTopIdx / currentListSize);
                        }
                        if (rescuePos > 1.0) rescuePos = 1.0;
                        if (rescuePos < 0.0) rescuePos = 0.0;

                        if (!isAbsoluteEnd && rescuePos >= 0.99 && currentFlowPos > 1.05) rescuePos = 0.98;
                        if (isAbsoluteEnd && currentFlowPos > 1.05) rescuePos = 1.0;

                        if (isDocSmall && rescuePos >= 0.98) rescuePos = 0.95;

                   //     System.out.println("[DIAG-TRACKER] Void Rescue Net Triggered: " + rescuePos);
                        flow.setPosition(rescuePos);

                        if (isAbsoluteEnd) { visibleCellsMemory.clear(); memoryTopIdx = -1; memoryTopMinY = -1000.0; }
                        if (collapseGuardFrames < 2) collapseGuardFrames = 2;
                        framesAlive = Math.max(framesAlive, 2);
                    }
                }
            }

            if (collapseGuardFrames > 0) {
                collapseGuardFrames--;
                if (activeAnchorFrames == 0 && bottomPegFrames == 0) {
                    framesAlive = Math.max(framesAlive, 2);
                    return;
                }
            }

            // =================================================================
            // 🚨 5. THE TITANIUM VISE
            // =================================================================
            if (activeAnchorFrames > 0) {
                activeAnchorFrames--;
                framesAlive = Math.max(framesAlive, 2);

                double preSyncPos = flow.getPosition();
                boolean isNativePanic = (preSyncPos > 1.05 || preSyncPos < -0.05);

                if (isNativePanic) {
                    double safePos = lastFlowPosition >= 0 ? lastFlowPosition : 0.95;
                    if (safePos <= 0.01 && currentListSize > 0 && memoryTopIdx > 3) {
                        safePos = ((double) memoryTopIdx / currentListSize);
                    }
                    if (safePos > 1.0) safePos = 1.0;
                    if (safePos < 0.0) safePos = 0.0;

                    if (!isAbsoluteEnd && safePos >= 0.99 && preSyncPos > 1.05) safePos = 0.98;
                    if (isAbsoluteEnd && preSyncPos > 1.05) safePos = 1.0;

                    if (isDocSmall && safePos >= 0.98) safePos = 0.95;

                    flow.setPosition(safePos);
                //    System.out.println("[DIAG-TRACKER] Anchor Pre-Sync Rescue: Crushed Native Panic (" + preSyncPos + ") to " + safePos);
                }

                executeAbsoluteSync(flow);

                double postSyncPos = flow.getPosition();
                boolean postPanic = (postSyncPos > 1.05 || postSyncPos < -0.05);

                if (postPanic) {
                    double safePos = lastFlowPosition >= 0 ? lastFlowPosition : 0.95;
                    if (safePos <= 0.01 && currentListSize > 0 && memoryTopIdx > 3) {
                        safePos = ((double) memoryTopIdx / currentListSize);
                    }
                    if (safePos > 1.0) safePos = 1.0;
                    if (safePos < 0.0) safePos = 0.0;

                    if (isDocSmall && safePos >= 0.98) safePos = 0.95;

               //     System.out.println("[DIAG-TRACKER] Synchronous Panic Caught during Vise Sync! " + postSyncPos + " -> Reverting to " + safePos);
                    flow.setPosition(safePos);
                    executeAbsoluteSync(flow);
                }

                if (activeAnchorTargetIdx != -1 && activeExpectedMinY != -1000.0) {
                    int targetIdx = activeAnchorTargetIdx;
                    if (targetIdx >= paragraphListView.getItems().size()) targetIdx = paragraphListView.getItems().size() - 1;

                    for (int lockPass = 0; lockPass < 3; lockPass++) {
                        double currentMinY = getCellMinY(flow, refNode, targetIdx);
                        double expectedMinY = activeExpectedMinY;

                        if (currentMinY == -1000.0) {
                            for(int i = 1; i <= 30; i++) {
                                int fallbackIdx = targetIdx - i;
                                if (fallbackIdx >= 0) {
                                    double fallbackMinY = getCellMinY(flow, refNode, fallbackIdx);
                                    if (fallbackMinY != -1000.0) {
                                        currentMinY = fallbackMinY;
                                        if (visibleCellsMemory.containsKey(fallbackIdx)) {
                                            expectedMinY = visibleCellsMemory.get(fallbackIdx);
                                 //           System.out.println("[DIAG-TRACKER] Vise Blind! Mapped to strict memory fallback ABOVE: " + fallbackIdx);
                                        } else {
                                            expectedMinY = activeExpectedMinY - (estimatedRowHeight * i);
                                  //          System.out.println("[DIAG-TRACKER] Vise Blind! Mapped to geometric fallback ABOVE: " + fallbackIdx);
                                        }
                                        targetIdx = fallbackIdx;
                                        break;
                                    }
                                }
                            }
                        }

                        if (currentMinY == -1000.0) {
                      //      System.out.println("[DIAG-TRACKER] Vise Fatal Blind! Executing Ratio-based RAM recovery for index " + targetIdx);
                            try {
                                double safe = lastFlowPosition >= 0 ? lastFlowPosition : 0.5;
                                if (safe <= 0.01 && currentListSize > 0 && memoryTopIdx > 3) {
                                    safe = ((double) memoryTopIdx / currentListSize);
                                }
                                if (safe > 1.0) safe = 1.0;
                                if (safe < 0.0) safe = 0.0;

                                if (isDocSmall && safe >= 0.98) safe = 0.95;

                                flow.setPosition(safe);
                                executeAbsoluteSync(flow);
                                currentMinY = getCellMinY(flow, refNode, targetIdx);
                                expectedMinY = activeExpectedMinY;
                            } catch (Throwable t) {
                          //      System.out.println("[DIAG-TRACKER] Ratio Recovery Exception: " + t);
                            }
                        }

                        if (currentMinY != -1000.0) {
                            double shift = currentMinY - expectedMinY;
                            shift = applyPhysicalClamp(flow, refNode, h, shift, true);

                            double maxShift = Math.max(h * 4.0, estimatedRowHeight * 40.0);

                            if (Math.abs(shift) > 0.1 && Math.abs(shift) < maxShift) {
                                double clampedShift = Math.round(shift);

                                if (Math.abs(clampedShift) >= 1.0) {
                                    if (Math.abs(clampedShift) <= 2.0 && Math.abs(lastKick) == Math.abs(clampedShift)) {
                                        anchorStuckCount++;
                                    } else if (lastKick == clampedShift) {
                                        anchorStuckCount++;
                                    } else {
                                        anchorStuckCount = 0;
                                    }

                                    if (anchorStuckCount >= 2) {
                                //        System.out.println("[DIAG-TRACKER] Vise hit physical floor loop-lock. Disarming Vise permanently.");
                                        activeAnchorFrames = 0;
                                        break;
                                    }

                               //     System.out.println("[DIAG-TRACKER] Titanium Vise Lock-Pass " + lockPass + " Kick: " + clampedShift);
                                    doScroll(flow, clampedShift, isTightViewport ? "Tight Vise" : "Space Vise");
                                    executeAbsoluteSync(flow);

                                    double postSyncY = getCellMinY(flow, refNode, targetIdx);
                                    if (postSyncY != -1000.0 && Math.abs(postSyncY - currentMinY) < 1.0) {
                                        activeAnchorFrames = Math.max(activeAnchorFrames, 2);
                                    }

                                    lastKick = clampedShift;

                                    if (targetIdx != activeAnchorTargetIdx) {
                                        activeAnchorTargetIdx = targetIdx;
                                        activeExpectedMinY = expectedMinY;
                                    }
                                    continue;
                                } else {
                             //       System.out.println("[DIAG-TRACKER] Vise Perfect Lock Achieved (< 1.0px).");
                                    break;
                                }
                            } else if (Math.abs(shift) >= maxShift) {
                        //        System.out.println("[DIAG-TRACKER] Massive Hallucination Ignored in Vise Loop! Shift: " + shift);
                                break;
                            } else {
                       //         System.out.println("[DIAG-TRACKER] Vise Perfect Lock Achieved (< 0.1px).");
                                break;
                            }
                        } else {
                       //     System.out.println("[DIAG-TRACKER] Vise Fatal Blind! Native RAM recovery failed.");
                            break;
                        }
                    }
                } else {
              //     System.out.println("[DIAG-TRACKER] Anchor Failed: TargetIdx (" + activeAnchorTargetIdx + ") invalid.");
                    activeAnchorFrames = 0;
                }

                if (activeAnchorFrames == 0) {
                    captureMemory(flow, refNode);
                    pinLockFrames = 3;
                    radarMuteFrames = Math.max(radarMuteFrames, 5);
                    anchorStuckCount = 0;
                }

                return;
            }

            // =================================================================
            // 🚨 6. NORMAL TRACKING LOGIC
            // =================================================================
            targetAcquired = false;

            if (strictCaretPinFrames > 0) strictCaretPinFrames--;

            if (isGhostCaret) {
                targetAcquired = true;
            } else {
                boolean isInlineEdit = (currentTextLen != lastTextLen && activeAnchorFrames == 0 && bottomPegFrames == 0 && strictCaretPinFrames == 0);
                if (isInlineEdit && !peggedToBottom) pinLockFrames = Math.max(pinLockFrames, 4);
                else if (pinLockFrames > 0 && currentTextLen == lastTextLen) pinLockFrames--;

                int currentFirstIdx = flow.getFirstVisibleIndex();
                justScrolled = false;

                double pad = 1.0;
                double triggerBottom = Math.max(10.0, h - pad);
                double triggerTop = pad;

                for (int pass = 0; pass < 8; pass++) {
                    boolean effectiveShield = (!isTightViewport && !bypassShield && pass == 0 && strictCaretPinFrames == 0);
                    CaretInfo info = getTrueCaretBounds(caretIdx, refNode, flow, effectiveShield);

                    if (info.bounds == null && info.cellFound && info.cellBounds != null) {
                        double cellH = info.cellBounds.getMaxY() - info.cellBounds.getMinY();
                        if (massiveCooldownFrames > 0 && !isUltraTight) { yieldStreak = 0; framesAlive = Math.max(framesAlive, 2); break; }
                        if (cellH > Math.max(h * 2.0, 400.0)) { yieldStreak = 0; framesAlive = Math.max(framesAlive, 2); break; }

                        // 🚨 V74: Ensures bounds hallucination doesn't fire across active logical cursor navigation
                        if (!isTightViewport && bypassShield && lastCaretTop != -1000.0 && currentCaretPos == lastCaretTopPos) {
                            double activeTop = lastCaretTop;
                            if (activeTop < info.cellBounds.getMinY()) activeTop = info.cellBounds.getMinY();
                            if (activeTop > info.cellBounds.getMaxY() - estimatedRowHeight) activeTop = Math.max(info.cellBounds.getMinY(), info.cellBounds.getMaxY() - estimatedRowHeight);
                            double activeBot = activeTop + estimatedRowHeight;
                            info.bounds = new javafx.geometry.BoundingBox(0, activeTop, 10, activeBot - activeTop);
                        } else if (lastCaretTop != -1000.0 && currentCaretPos == lastCaretTopPos) {
                            double activeTop = lastCaretTop;
                            if (activeTop < info.cellBounds.getMinY()) activeTop = info.cellBounds.getMinY();
                            if (activeTop > info.cellBounds.getMaxY() - estimatedRowHeight) activeTop = Math.max(info.cellBounds.getMinY(), info.cellBounds.getMaxY() - estimatedRowHeight);
                            double activeBot = activeTop + estimatedRowHeight;
                            if (isAbsoluteEnd && info.cellBounds.getMaxY() > activeBot) activeBot = info.cellBounds.getMaxY();
                            info.bounds = new javafx.geometry.BoundingBox(0, activeTop, 10, activeBot - activeTop);
                        } else {
                            double cTop = info.cellBounds.getMinY();
                            double cBot = isTightViewport ? (cTop + Math.max(1.0, Math.min(10.0, cellH))) : info.cellBounds.getMaxY();
                            if (isAbsoluteEnd) cBot = info.cellBounds.getMaxY();
                            info.bounds = new javafx.geometry.BoundingBox(0, cTop, 10, Math.max(1.0, cBot - cTop));
                        }
                    }

                    double activeBot = -1000.0;
                    double activeTop = -1000.0;

                    if (info.bounds != null) {
                        activeBot = info.bounds.getMaxY();
                        activeTop = info.bounds.getMinY();
                    } else if (!isTightViewport && lastCaretTop != -1000.0 && currentCaretPos == lastCaretTopPos) {
                        activeTop = lastCaretTop;
                        if (info.cellBounds != null && lastCaretTop < info.cellBounds.getMinY() - 10.0) activeTop = info.cellBounds.getMinY();
                        activeBot = activeTop + estimatedRowHeight;
                    } else if (!isTightViewport && info.cellBounds != null) {
                        activeTop = info.cellBounds.getMinY();
                        activeBot = activeTop + estimatedRowHeight;
                    }

                    if (effectiveShield && info.cellMutating) {
                        if (activeBot != -1000.0) {
                            boolean isClippedOffScreen = (activeBot > h + 2.0 || activeTop < -2.0);
                            if (isClippedOffScreen) {
                                if (recentDeleteFrames > 0) { yieldStreak = 0; framesAlive = Math.max(framesAlive, 2); break; }
                                else { bypassShield = true; }
                            } else { yieldStreak = 0; framesAlive = Math.max(framesAlive, 2); break; }
                        } else { yieldStreak = 0; framesAlive = Math.max(framesAlive, 2); break; }
                    }

                    if (info.bounds == null && info.cellFound && info.cellBounds != null) {
                        if (bypassShield && lastCaretTop != -1000.0 && currentCaretPos == lastCaretTopPos) {
                            info.bounds = new javafx.geometry.BoundingBox(0, activeTop, 10, activeBot - activeTop);
                        } else {
                            yieldStreak = 0; framesAlive = Math.max(framesAlive, 2); break;
                        }
                    }

                    if (info.bounds == null) {
                        if (strictCaretPinFrames > 0 && caretIdx != -1) {
                       //     System.out.println("[DIAG-TRACKER] Caret Pin Blind! Forcing Ratio recovery");
                            try {
                                double safe = lastFlowPosition >= 0 ? lastFlowPosition : 0.5;
                                if (isDocSmall && safe >= 0.98) safe = 0.95;

                                flow.setPosition(safe);
                                executeAbsoluteSync(flow);
                            } catch (Throwable t) {
                          //      System.out.println("[DIAG-TRACKER] Pin Recovery Exception: " + t);
                            }
                            continue;
                        }

                        if (info.cellFound) {
                            boolean activelyTypingCheck = (framesAlive > 5);
                            if (!activelyTypingCheck) break;
                        }

                        int currentLast = flow.getLastVisibleIndex();

                        boolean isPhysicallyVisible = false;
                        if (activeBot != -1000.0 && activeTop != -1000.0) {
                            if (activeBot >= triggerTop - 10.0 && activeTop <= triggerBottom + 10.0) {
                                isPhysicallyVisible = true;
                            }
                        }

                        if ((caretIdx > currentLast || caretIdx < currentFirstIdx) && !isPhysicallyVisible) {
                            if (radarMuteFrames > 0) { yieldStreak = 0; framesAlive = Math.max(framesAlive, 2); break; }

                            // 🚨 V74: Math Shield drops instantly if user navigates to a new logical character
                            if (lastCaretTop != -1000.0 && framesAlive > 0 && currentCaretPos == lastCaretTopPos) {
                                double projectedY = lastCaretTop;
                                double safeTop = triggerTop - (estimatedRowHeight * 1.5);
                                double safeBottom = triggerBottom + (estimatedRowHeight * 1.5);

                                if (projectedY >= safeTop && projectedY <= safeBottom) {
                                    mathShieldStreak++;
                                    if (mathShieldStreak <= 5) {
                                   //     System.out.println("[DIAG-TRACKER] Math Shield: Suppressing false Radar kick. Cursor geometrically proven on screen. Streak: " + mathShieldStreak);
                                        radarPanicStreak = 0;
                                        framesAlive = Math.max(framesAlive, 2);
                                        break;
                                    } else {
                                //        System.out.println("[DIAG-TRACKER] Math Shield: Streak timeout reached. Yielding to native Radar.");
                                    }
                                } else {
                                    mathShieldStreak = 0;
                                }
                            } else {
                                mathShieldStreak = 0;
                            }

                            if (!isTightViewport) {
                                radarPanicStreak++;
                                if (radarPanicStreak < 3) { framesAlive = Math.max(framesAlive, 2); break; }
                            }

                            if (pass == 0) {
                                executeAbsoluteSync(flow);
                                int newFirst = flow.getFirstVisibleIndex(); int newLast = flow.getLastVisibleIndex();
                                if (newFirst != -1 && newLast != -1) {
                                    if (caretIdx >= newFirst && caretIdx <= newLast) {
                                        mathShieldStreak = 0;
                                        continue;
                                    }
                                    currentFirstIdx = newFirst; currentLast = newLast;
                                }
                            }

                            double rawDistance = (caretIdx > currentLast) ? (caretIdx - currentLast) * estimatedRowHeight : (currentFirstIdx - caretIdx) * estimatedRowHeight;
                            double jump = Math.round(rawDistance); jump = Math.max(jump, 1.0); if (jump > 150.0) jump = 150.0;

                            if (!isTightViewport) {
                                if (Math.abs(jump - lastRadarJump) < 1.0 && caretIdx == lastRadarCaretIdx) radarStuckStreak++;
                                else radarStuckStreak = 0;

                                lastRadarJump = jump; lastRadarCaretIdx = caretIdx;
                                if (radarStuckStreak > 1) { radarMuteFrames = 5; radarStuckStreak = 0; break; }
                            }

                            double kick = caretIdx > currentLast ? jump : -jump;
                            double clampedKick = applyPhysicalClamp(flow, refNode, h, kick, false);

                            if (Math.abs(clampedKick) < 0.5) {
                                targetAcquired = true;
                                break;
                            }

                            doScroll(flow, clampedKick, isTightViewport ? "Tight Radar" : "Space Radar");
                            justScrolled = true; executeAbsoluteSync(flow); continue;
                        }

                        mathShieldStreak = 0;

                        if (!isTightViewport) {
                            boolean isTargetVisibleRange = (currentFirstIdx != -1 && currentLast != -1 && caretIdx >= currentFirstIdx && caretIdx <= currentLast);
                            if (isTargetVisibleRange && !bypassShield) {
                                yieldStreak++;
                                if (yieldStreak > 15) { executeAbsoluteSync(flow); yieldStreak = 0; bypassShield = true; break; }
                                framesAlive = Math.max(framesAlive, 2); break;
                            } else yieldStreak = 0;
                        }

                        break;
                    } else {
                        yieldStreak = 0; radarPanicStreak = 0; radarStuckStreak = 0; lastRadarJump = 0.0; mathShieldStreak = 0;
                        double caretTop = info.bounds.getMinY(); double caretBottom = info.bounds.getMaxY();

                        // Strict Caret Pin explicitly relies on previous unverified coordinate.
                        double targetCaretY = lastCaretTop;

                        // 🚨 V74: Anti-Bounce (Stuck Count) is perfectly bypassed if the user's cursor logically navigated.
                        // This fixes the "Arrow-Down Lag" without breaking text insertion stability.
                        if (lastCaretTop != -1000.0 && Math.abs(lastKick) > 0.5 && strictCaretPinFrames == 0) {
                            if (currentCaretPos == lastCaretTopPos) {
                                double actualMove = Math.abs(caretTop - lastCaretTop);
                                if (actualMove < Math.abs(lastKick) * 0.2) {
                                    stuckCount++; if (stuckCount >= 2) { targetAcquired = true; break; }
                                } else stuckCount = 0;
                            } else {
                                stuckCount = 0; // Navigating logical index permanently bypasses the stuck floor lock!
                            }
                        } else if (Math.abs(lastKick) <= 0.5) stuckCount = 0;

                        double kick = 0.0;

                        if (strictCaretPinFrames > 0 && targetCaretY != -1000.0) {
                            kick = caretTop - targetCaretY;
                        } else if (caretBottom > triggerBottom) {
                            if (!isTightViewport && recentDeleteFrames > 0 && !isAbsoluteEnd) kick = 0.0;
                            else {
                                kick = (caretBottom - triggerBottom);
                                if (caretTop - kick < triggerTop) kick = Math.max(0, caretTop - triggerTop);
                            }
                        } else if (caretTop < triggerTop) {
                            if (!isTightViewport && recentDeleteFrames > 0 && !isAbsoluteEnd) kick = 0.0;
                            else kick = (caretTop - triggerTop);
                        }

                        kick = Math.round(kick);

                        if (kick > 0.0 && flow.getPosition() >= 0.999 && isStrictStringEnd) {
                            kick = 0.0;
                        }

                        double clampedKick = applyPhysicalClamp(flow, refNode, h, kick, strictCaretPinFrames > 0);

                        if (Math.abs(clampedKick) <= 2.0) {
                            targetAcquired = true; lastKick = 0.0;
                            lastCaretTop = (strictCaretPinFrames > 0 && targetCaretY != -1000.0) ? targetCaretY : caretTop;
                            lastCaretTopPos = currentCaretPos; // Lock logical position
                            if (strictCaretPinFrames > 0) {
                                // Pin continues holding layout hostage until timeout
                            } else {
                                break;
                            }
                        }
                        if (lastKick != 0.0 && Math.signum(clampedKick) != Math.signum(lastKick)) {
                            if (Math.abs(clampedKick) < 30.0) { targetAcquired = true; break; }
                        }

                        double maxSafeKick = Math.max(150.0, h * 1.5);
                        if (clampedKick > maxSafeKick) clampedKick = maxSafeKick;
                        if (clampedKick < -maxSafeKick) clampedKick = -maxSafeKick;
                        clampedKick = Math.round(clampedKick);

                        lastCaretTop = (strictCaretPinFrames > 0 && targetCaretY != -1000.0) ? targetCaretY : caretTop;
                        lastCaretTopPos = currentCaretPos; // Lock logical position
                        lastKick = clampedKick;

                        if (Math.abs(clampedKick) > 0.0) {
                            doScroll(flow, clampedKick, isTightViewport ? (strictCaretPinFrames > 0 ? "Tight Caret Pin" : "Tight Lock") : (strictCaretPinFrames > 0 ? "Space Caret Pin" : "Space Lock"));
                            executeAbsoluteSync(flow);
                        }
                        continue;
                    }
                }
            }

            if (targetAcquired) {
                mathShieldStreak = 0;
                if (peggedToBottom) framesAlive = 0;
                else if (justScrolled || pinLockFrames > 0 || pendingAnchorRecovery || strictCaretPinFrames > 0) framesAlive = Math.max(framesAlive, 1);
                else framesAlive = 0;
            } else framesAlive = Math.max(framesAlive, 3);

            boolean isYielding = (collapseGuardFrames > 0 || bottomPegFrames > 0 || blackoutFrames > 0 || activeAnchorFrames > 0 || strictCaretPinFrames > 0);

            if (!isYielding) {
                captureMemory(flow, refNode);

                if (!visibleCellsMemory.isEmpty() && lastKnownCaretIdx != -1) {
                    int minVis = Integer.MAX_VALUE;
                    int maxVis = Integer.MIN_VALUE;
                    for (int idx : visibleCellsMemory.keySet()) {
                        if (idx < minVis) minVis = idx;
                        if (idx > maxVis) maxVis = idx;
                    }
                    lastCaretVisible = (lastKnownCaretIdx >= minVis - 1 && lastKnownCaretIdx <= maxVis + 1);
                } else {
                    lastCaretVisible = true;
                }

                double finalPos = flow.getPosition();
                if (finalPos >= -0.05 && finalPos <= 1.05) {
                    if (!(finalPos == 0.0 && memoryTopIdx > 5)) {
                        lastFlowPosition = finalPos;
                    }
                }
            }
        }

        private void doScroll(RichVirtualFlow flow, double pixels, String source) {
            if (Math.abs(pixels) > 0.0) {
            //    System.out.println("[DIAG-TRACKER] " + source + " Kick: " + pixels);
                flow.scrollPixels(pixels);
                justScrolled = true;
            }
        }

        private double applyPhysicalClamp(RichVirtualFlow flow, javafx.scene.Node refNode, double h, double kick, boolean isAnchor) {
            if (isAnchor) return kick; // Absolute Immunity

            if (kick > 0.0) {
                int currentLast = flow.getLastVisibleIndex();
                if (currentLast != -1 && currentLast >= paragraphListView.getItems().size() - 2) {
                    double lastCellMaxY = getCellMaxY(flow, refNode, paragraphListView.getItems().size() - 1);
                    if (lastCellMaxY != -1000.0) {
                        double maxAllowed = Math.max(0.0, lastCellMaxY - h);
                        if (kick > maxAllowed) return Math.floor(maxAllowed);
                    }
                }
            } else if (kick < 0.0) {
                int currentFirst = flow.getFirstVisibleIndex();
                if (currentFirst <= 1) {
                    double firstCellMinY = getCellMinY(flow, refNode, 0);
                    if (firstCellMinY != -1000.0) {
                        double minAllowed = Math.min(0.0, firstCellMinY);
                        if (kick < minAllowed) return Math.ceil(minAllowed);
                    }
                }
            }
            return kick;
        }

        private void executeAbsoluteSync(RichVirtualFlow flow) {
            try {
                if (paragraphListView.getParent() != null) {
                    paragraphListView.getParent().layout();
                }
                paragraphListView.requestLayout();
                flow.requestLayout();
                paragraphListView.applyCss();
                flow.applyCss();
                paragraphListView.layout();
                flow.layout();
            } catch (Throwable t) {}
        }

        private double getCellMinY(RichVirtualFlow flow, javafx.scene.Node refNode, int targetIdx) {
            try {
                javafx.scene.Group sheet = (javafx.scene.Group) flow.lookup(".sheet");
                if (sheet != null) {
                    for (javafx.scene.Node node : sheet.getChildren()) {
                        if (node instanceof javafx.scene.control.ListCell) {
                            javafx.scene.control.ListCell<?> cell = (javafx.scene.control.ListCell<?>) node;
                            if (cell.getIndex() == targetIdx) {
                                javafx.geometry.Bounds cb = cell.getBoundsInLocal();
                                if (cb != null) {
                                    javafx.geometry.Bounds scene = cell.localToScene(cb);
                                    if (scene != null && refNode != null) {
                                        javafx.geometry.Bounds local = refNode.sceneToLocal(scene);
                                        if (local != null) return local.getMinY();
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (Throwable t) {}
            return -1000.0;
        }

        private double getCellMaxY(RichVirtualFlow flow, javafx.scene.Node refNode, int targetIdx) {
            try {
                javafx.scene.Group sheet = (javafx.scene.Group) flow.lookup(".sheet");
                if (sheet != null) {
                    for (javafx.scene.Node node : sheet.getChildren()) {
                        if (node instanceof javafx.scene.control.ListCell) {
                            javafx.scene.control.ListCell<?> cell = (javafx.scene.control.ListCell<?>) node;
                            if (cell.getIndex() == targetIdx) {
                                javafx.geometry.Bounds cb = cell.getBoundsInLocal();
                                if (cb != null) {
                                    javafx.geometry.Bounds scene = cell.localToScene(cb);
                                    if (scene != null && refNode != null) {
                                        javafx.geometry.Bounds local = refNode.sceneToLocal(scene);
                                        if (local != null) return local.getMaxY();
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (Throwable t) {}
            return -1000.0;
        }

        private class CaretInfo {
            boolean cellFound = false;
            boolean cellMutating = false;
            javafx.geometry.Bounds bounds = null;
            javafx.geometry.Bounds cellBounds = null;
        }

        private CaretInfo getTrueCaretBounds(int caretIdx, javafx.scene.Node refNode, RichVirtualFlow flow, boolean useShield) {
            CaretInfo info = new CaretInfo();
            try {
                javafx.scene.Group sheet = (javafx.scene.Group) flow.lookup(".sheet");
                if (sheet != null) {
                    for (javafx.scene.Node node : sheet.getChildren()) {
                        if (node instanceof javafx.scene.control.ListCell) {
                            javafx.scene.control.ListCell<?> cell = (javafx.scene.control.ListCell<?>) node;
                            if (cell.getIndex() == caretIdx) {
                                info.cellFound = true;
                                javafx.geometry.Bounds cbCell = cell.getBoundsInLocal();
                                if (cbCell != null) {
                                    javafx.geometry.Bounds cellScene = cell.localToScene(cbCell);
                                    if (cellScene != null) info.cellBounds = refNode.sceneToLocal(cellScene);
                                }
                                for (javafx.scene.Node caretNode : cell.lookupAll(".caret")) {
                                    if (caretNode instanceof javafx.scene.shape.Path) {
                                        javafx.scene.shape.Path path = (javafx.scene.shape.Path) caretNode;
                                        if (!path.getElements().isEmpty()) {
                                            javafx.geometry.Bounds cbLocal = path.getBoundsInLocal();
                                            if (cbLocal.getWidth() > 0.1 || cbLocal.getHeight() > 0.1) {
                                                javafx.geometry.Bounds caretSceneBounds = path.localToScene(cbLocal);
                                                if (caretSceneBounds != null) {
                                                    info.bounds = refNode.sceneToLocal(caretSceneBounds);
                                                    if (info.bounds != null) {
                                                        double ch = info.bounds.getHeight();
                                                        if (ch > 8.0 && ch < 50.0) {
                                                            if (Math.abs(ch - estimatedRowHeight) > 5.0) estimatedRowHeight = ch;
                                                            else estimatedRowHeight = (estimatedRowHeight * 0.95) + (ch * 0.05);
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                                break;
                            }
                        }
                    }
                }
            } catch (Throwable t) {}
            return info;
        }

        private void captureMemory(RichVirtualFlow flow, javafx.scene.Node refNode) {
            int prevIdx = memoryTopIdx;

            int newFirstIdx = flow.getFirstVisibleIndex();
            int newLastIdx = flow.getLastVisibleIndex();

            if (newFirstIdx <= 0 && prevIdx > 5) {
                double currentRatio = flow.getPosition();
                int totalItems = paragraphListView.getItems().size();
                if (totalItems > 10 && currentRatio > 0.15) {
                    return;
                }
            }

            if (newFirstIdx == -1 || newLastIdx == -1) {
                return;
            }

            java.util.Map<Integer, Double> tempMemory = new java.util.HashMap<>();
            int tempTopIdx = -1;
            double tempTopMinY = -1000.0;

            try {
                javafx.scene.Group sheet = (javafx.scene.Group) flow.lookup(".sheet");
                if (sheet != null) {
                    int capFirst = Math.max(0, newFirstIdx);
                    int capLast = Math.min(paragraphListView.getItems().size() - 1, newLastIdx);

                    double h = refNode != null ? refNode.getLayoutBounds().getHeight() : 1000.0;

                    for (javafx.scene.Node node : sheet.getChildren()) {
                        if (node instanceof javafx.scene.control.ListCell) {
                            javafx.scene.control.ListCell<?> cell = (javafx.scene.control.ListCell<?>) node;
                            int idx = cell.getIndex();
                            if (idx >= capFirst && idx <= capLast) {
                                double y = getCellMinY(flow, refNode, idx);
                                if (y != -1000.0) {
                                    double maxY = getCellMaxY(flow, refNode, idx);
                                    if (maxY != -1000.0 && maxY >= -1.0 && y <= h + 1.0) {
                                        tempMemory.put(idx, y);
                                        if (tempTopIdx == -1 || idx < tempTopIdx) {
                                            if (idx >= newFirstIdx) {
                                                tempTopIdx = idx;
                                                tempTopMinY = y;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (Throwable t) {}

            if (!tempMemory.isEmpty()) {
                visibleCellsMemory.clear();
                visibleCellsMemory.putAll(tempMemory);
                if (tempTopIdx != -1) {
                    memoryTopIdx = tempTopIdx;
                    memoryTopMinY = tempTopMinY;
                } else {
                    int minKey = Integer.MAX_VALUE;
                    for (int k : tempMemory.keySet()) {
                        if (k < minKey) minKey = k;
                    }
                    memoryTopIdx = minKey;
                    memoryTopMinY = tempMemory.get(minKey);
                }
            }
        }
    }



    //------------------ ai end ------------------------


    private class RichVirtualFlow extends VirtualFlow<ListCell<Paragraph>> {
        RichVirtualFlow(RichTextArea control) {
            super();

            // BOOT-TRACE: Catch exactly when and where the native scrollbar moves
            positionProperty().addListener((obs, oldVal, newVal) -> {
            //    System.out.println("[BOOT-TRACE] VirtualFlow Position Changed: " + oldVal + " -> " + newVal);
            });

            ReadOnlyObjectProperty<Bounds> clippedBounds = lookup(".clipped-container").layoutBoundsProperty();
            textFlowPrefWidthProperty.bind(Bindings.createDoubleBinding(() ->
                            control.getContentAreaWidth() > 0 ?
                                    control.getContentAreaWidth() :
                                    clippedBounds.get().getWidth() > 0 ?
                                            clippedBounds.get().getWidth() - 10 : -1,
                    control.contentAreaWidthProperty(), clippedBounds));
        }

        @Override
        protected void rebuildCells() {
            super.rebuildCells();
        }

        int getLastVisibleIndex() {
            ListCell<Paragraph> last = getLastVisibleCell();
            return (last != null) ? last.getIndex() : -1;
        }

        int getFirstVisibleIndex() {
            ListCell<Paragraph> first = getFirstVisibleCell();
            return (first != null) ? first.getIndex() : -1;
        }
    }






    private class ParagraphListView extends ListView<Paragraph> {

        final RichVirtualFlow virtualFlow;
        private Group sheet;
        private Region container;

        //ai
        private final javafx.beans.value.ChangeListener<Boolean> uiRefreshListener = (obs, oldVal, newVal) -> {
            javafx.application.Platform.runLater(this::refreshAllVisibleCells);
        };

        public ParagraphListView(RichTextArea control) {
            virtualFlow = new RichVirtualFlow(control);
            getStyleClass().setAll("paragraph-list-view");

            //ai added
            this.setFixedCellSize(Region.USE_COMPUTED_SIZE);

            // AI ADDED: Listen for the paste signal from the ViewModel
            // --> Replace "viewModel" with the actual reference inside RichTextAreaSkin <--
    //        viewModel.forceUiRefreshProperty().addListener((obs, oldVal, newVal) -> {
    //            javafx.application.Platform.runLater(this::refreshAllVisibleCells);
     //       });

            viewModel.forceUiRefreshProperty().addListener(new javafx.beans.value.WeakChangeListener<>(uiRefreshListener));

            //

            addEventHandler(MouseEvent.MOUSE_DRAGGED, e -> {
                // if a dragging event was started from a listCell, but the event gets out of the
                // listView bounds, forward event to cell -> paragraphTile -> TextFlow, so selection
                // can continue
                if (anchorIndex != -1 && !getLayoutBounds().contains(e.getX(), e.getY())) {
                    // Translate listView event to a point (in listView coordinates) that could be over a listCell
                    Point2D listPoint = localToScreen(getPointInListView(e));

                    getSheet().getChildren().stream()
                            .filter(RichListCell.class::isInstance)
                            .filter(cell -> cell.getLayoutBounds().contains(cell.screenToLocal(listPoint)))
                            .map(RichListCell.class::cast)
                            .findFirst()
                            .ifPresent(cell -> {
                                Point2D cellPoint = cell.screenToLocal(listPoint);
                                Point2D cellScreenPoint = cell.localToScreen(cellPoint);
                                MouseEvent mouseEvent = new MouseEvent(e.getSource(), e.getTarget(), e.getEventType(),
                                        cellPoint.getX(), cellPoint.getY(), cellScreenPoint.getX(), cellScreenPoint.getY(),
                                        MouseButton.PRIMARY, 1,
                                        false, false, false, false,
                                        true, false, false,
                                        false, false, false, null);
                                cell.forwardDragEvent(mouseEvent);
                            });
                    double y = e.getY();
                    if (y < 0 || y > getHeight()) {
                        // scroll some pixels to prevent mouse getting stuck in a cell
                        virtualFlow.scrollPixels(y < 0 ? (y / 10) - 1 : (y - getHeight()) / 10 + 1);
                    }
                }
            });

            addEventHandler(DragEvent.DRAG_OVER, event -> {
                if (dragAndDropStart != -1) {
                    Point2D localEvent = getContainer().screenToLocal(event.getScreenX(), event.getScreenY());
                    if (localEvent.getY() < getContainer().getLayoutBounds().getMinY() || localEvent.getY() > getContainer().getLayoutBounds().getMaxY()) {
                        virtualFlow.scrollPixels(localEvent.getY() <= getContainer().getLayoutBounds().getMinY() ? -5 : 5);
                    }
                }
            });
        }

        private Point2D getPointInListView(MouseEvent e) {
            double iniX = getInsets().getLeft();
            double endX = iniX + getContainer().getLayoutBounds().getWidth() - getInsets().getRight();
            double iniY = getInsets().getTop();
            double endY = iniY + getContainer().getLayoutBounds().getHeight() - getInsets().getBottom();
            double deltaX = e.getX() < iniX ? e.getX() - iniX : e.getX() > endX ? e.getX() - endX : 0;
            double deltaY = e.getY() < iniY ? e.getY() - iniY : e.getY() > endY ? e.getY() - endY : 0;
            return new Point2D(e.getX() - deltaX, e.getY() - deltaY);
        }

        private Group getSheet() {
            if (sheet == null) {
                sheet = (Group) virtualFlow.lookup(".sheet");
            }
            return sheet;
        }

        private Region getContainer() {
            if (container == null) {
                container = (Region) virtualFlow.lookup(".clipped-container");
            }
            return container;
        }

        @Override
        protected Skin<?> createDefaultSkin() {
            return new ListViewSkin<>(this) {
                @Override
                protected VirtualFlow<ListCell<Paragraph>> createVirtualFlow() {
                    return virtualFlow;
                }
            };
        }

        void evictUnusedObjects() {
            Set<Font> usedFonts = new HashSet<>();
            Set<Image> usedImages = new HashSet<>();
            getSheet().getChildren().stream()
                    .filter(RichListCell.class::isInstance)
                    .map(RichListCell.class::cast)
                    .forEach(cell -> cell.evictUnusedObjects(usedFonts, usedImages));

            List<Font> cachedFonts = new ArrayList<>(getFontCache().values());
            cachedFonts.removeAll(usedFonts);
            if (!cachedFonts.isEmpty()) {
                getFontCache().values().removeAll(cachedFonts);
            }

            List<Image> cachedImages = new ArrayList<>(getImageCache().values());
            cachedImages.removeAll(usedImages);
            if (!cachedImages.isEmpty()) {
                getImageCache().values().removeAll(cachedImages);
            }
        }



        int getNextRowPosition(double x, boolean down) {
            return getSheet().getChildren().stream()
                    .filter(RichListCell.class::isInstance)
                    .map(RichListCell.class::cast)
                    .filter(RichListCell::hasCaret)
                    .mapToInt(cell -> cell.getNextRowPosition(x, down))
                    .findFirst()
                    .orElse(-1);
        }

        int getNextTableCellPosition(boolean down) {
            return getSheet().getChildren().stream()
                    .filter(RichListCell.class::isInstance)
                    .map(RichListCell.class::cast)
                    .filter(RichListCell::hasCaret)
                    .mapToInt(cell -> cell.getNextTableCellPosition(down))
                    .findFirst()
                    .orElse(-1);
        }

        void resetCaret() {
            getSheet().getChildren().stream()
                    .filter(RichListCell.class::isInstance)
                    .map(RichListCell.class::cast)
                    .filter(RichListCell::hasCaret)
                    .findFirst()
                    .ifPresent(RichListCell::resetCaret);
        }

        void updateLayout() {
            Group sheet = getSheet();
            if (sheet != null) {
                for (javafx.scene.Node node : sheet.getChildren()) {
                    if (node instanceof RichListCell) {
                        RichListCell cell = (RichListCell) node;
                        Paragraph p = cell.getItem();
                        if (p != null && cell.hasCaret()) {
                            if (cell instanceof UpdatableCell) {
                                ((UpdatableCell) cell).forceUpdate();
                            } else {
                                cell.updateItem(null, false);
                                cell.updateItem(p, false);
                                cell.applyCss();
                                cell.layout();
                            }
                        }
                    }
                }
            }

            if (RichTextAreaSkin.this.caretTracker != null) {
                RichTextAreaSkin.this.caretTracker.requestScroll();
            }
        }

        //ai added

        //ai added
        void refreshAllVisibleCells() {
            Group sheet = getSheet();
            if (sheet != null) {
                for (javafx.scene.Node node : sheet.getChildren()) {
                    if (node instanceof RichListCell) {
                        RichListCell cell = (RichListCell) node;
                        Paragraph p = cell.getItem();

                        // Notice we removed the && cell.hasCaret() limitation here!
                        if (p != null) {
                            if (cell instanceof UpdatableCell) {
                                ((UpdatableCell) cell).forceUpdate();
                            } else {
                                // Hard reset clears recycled text node formatting (superscript bleed)
                                cell.updateItem(null, false);
                                cell.updateItem(p, false);
                                cell.applyCss();
                                cell.layout();
                                cell.requestLayout();
                            }
                        }
                    }
                }
            }
            paragraphHeightCache.clear();
            lastReportedFullHeight = -1.0;
            virtualFlow.requestLayout();
            heightDebouncer.playFromStart();
        }

        /*
        void refreshAllVisibleCells() {
            Group sheet = getSheet();
            if (sheet != null) {
                for (javafx.scene.Node node : sheet.getChildren()) {
                    if (node instanceof RichListCell) {
                        RichListCell cell = (RichListCell) node;
                        Paragraph p = cell.getItem();

                        // Notice we removed the && cell.hasCaret() limitation here!
                        if (p != null) {
                            if (cell instanceof UpdatableCell) {
                                ((UpdatableCell) cell).forceUpdate();
                            } else {
                                // Hard reset clears recycled text node formatting (superscript bleed)
                                cell.updateItem(null, false);
                                cell.updateItem(p, false);
                                cell.applyCss();
                                cell.layout();

                            }
                        }
                    }
                }
            }
        }

         */

    }





    // --- prompt text fill
    private final StyleableProperty<Paint> promptTextFill = FACTORY.createStyleablePaintProperty(getSkinnable(), "promptTextFill", "-fx-prompt-text-fill", c -> {
        final RichTextAreaSkin skin = (RichTextAreaSkin) c.getSkin();
        return skin.promptTextFill;
    }, Color.GRAY);
    protected final void setPromptTextFill(Paint value) {
        promptTextFill.setValue(value);
    }
    protected final Paint getPromptTextFill() {
        return promptTextFill.getValue();
    }
    protected final ObjectProperty<Paint> promptTextFillProperty() {
        return (ObjectProperty<Paint>) promptTextFill;
    }

    private final DoubleProperty fullHeightProperty = new SimpleDoubleProperty(this, "fullHeight") {
        @Override
        protected void invalidated() {
            getSkinnable().fullHeightProperty.setValue(get() + getSkinnable().getPadding().getTop() + getSkinnable().getPadding().getBottom());
        }
    };




    protected RichTextAreaSkin(final RichTextArea control) {
        super(control);

        //ai added
        this.paragraphListView = new ParagraphListView(control);
        getChildren().add(paragraphListView);
        this.paragraphListView.setCellFactory(p -> createCell());

        this.caretTracker = new CaretTracker();
        this.caretTracker.start();

        // Tell the timer to run your method when it finishes counting down
     //   heightDebouncer.setOnFinished(event -> computeFullHeight());
        heightDebouncer.setOnFinished(event -> Platform.runLater(this::computeFullHeight));

        // TRIGGER 1: Window/Control resizing (changes text wrapping)
        getSkinnable().widthProperty().addListener((obs, oldVal, newVal) -> {
            heightDebouncer.playFromStart(); // playFromStart() resets and restarts the clock
        });

        // TRIGGER 2: If Gluon's document items list changes (e.g. new paragraph)
        paragraphSortedList.addListener((javafx.collections.ListChangeListener.Change<? extends Paragraph> c) -> {
            paragraphHeightCache.clear();
            heightDebouncer.playFromStart();
        });

        // TRIGGER 2: If Gluon's document items list changes (e.g. new paragraph)
    //    paragraphListView.getItems().addListener((javafx.collections.ListChangeListener.Change<?> c) -> {
    //        heightDebouncer.playFromStart();
     //   });

        this.control = control;

        nodesWidth.bind(viewModel.nodesWidthProperty());

        overlineOnProperty().addListener((ob, ov, nv) -> {
            if (resettingOverline) {
                return;
            }
            if (viewModel.getSelection().isDefined()) {
                execute(new ActionCmdOverlineSelection(charModMaps.getOverlineMap()));
                setModification(NONE);
                if (nv) {
                    Platform.runLater(() -> {
                        resettingOverline = true;
                        try {
                            overlineOn.set(false);
                        } finally {
                            resettingOverline = false;
                        }
                    });
                }
                return;
            }
            if (nv) setModification(OVERLINE);
            else setModification(NONE);
        });

        /*
        overlineOnProperty().addListener((ob, ov, nv) -> {
            if (nv) setModification(OVERLINE);
            else setModification(NONE);

        });

         */


        Map<KeyCodeCombination, String> tempMap = new HashMap();
        tempMap.putAll(SPECIAL_CHARACTER_MAP);
        tempMap.putAll(SANS_ALT_MAP);
        SPECIAL_WITH_SANS_MAP = Collections.unmodifiableMap(tempMap);

        Map<KeyCodeCombination, String> tempMap1 = new HashMap();
        tempMap1.putAll(SPECIAL_CHARACTER_MAP);
        tempMap1.putAll(BLACKBOARD_MAP);
        SPECIAL_WITH_BLACKBOARD_MAP = Collections.unmodifiableMap(tempMap1);

        Map<KeyCodeCombination, String> tempMap2 = new HashMap();
        tempMap2.putAll(SPECIAL_CHARACTER_MAP);
        tempMap2.putAll(FRAKTUR_MAP);
        SPECIAL_WITH_FRAKTUR_MAP = Collections.unmodifiableMap(tempMap2);

        Map<KeyCodeCombination, String> tempMap3 = new HashMap();
        tempMap3.putAll(SPECIAL_CHARACTER_MAP);
        tempMap3.putAll(ITALIC_ALT_MAP);
        SPECIAL_WITH_ITALIC_MAP = Collections.unmodifiableMap(tempMap3);

        Map<KeyCodeCombination, String> tempMap4 = new HashMap();
        tempMap4.putAll(SPECIAL_CHARACTER_MAP);
        tempMap4.putAll(SCRIPT_ALT_MAP);
        SPECIAL_WITH_SCRIPT_MAP = Collections.unmodifiableMap(tempMap4);

        Map<KeyCodeCombination, String> tempMap5 = new HashMap<>();
        tempMap5.putAll(SPECIAL_CHARACTER_MAP);
        tempMap5.putAll(SANS_ITALIC_ALT_MAP);
        SPECIAL_WITH_SANS_ITAL_MAP = Collections.unmodifiableMap(tempMap5);


        resources = ResourceBundle.getBundle("com.gluonhq.richtextarea.rich-text-area");

    //    paragraphListView = new ParagraphListView(control);  //ai moved this line up



        paragraphListView.setItems(paragraphSortedList);
        paragraphListView.setFocusTraversable(false);
//        getChildren().add(paragraphListView);   //ai moved up
        paragraphListView.setCellFactory(p -> new RichListCell(this));  //comment out in favor of line at top of constructor??
        objectsCacheEvictionTimer = new SmartTimer(paragraphListView::evictUnusedObjects, 1000, 60000);
        controlPrefWidthListener = (obs, ov, nv) -> {
            refreshTextFlow();
            paragraphListView.updateLayout();
        };

        tableAllowedListener = (obs, ov, nv) -> viewModel.setTableAllowed(nv);
        skinToneChangeListener = (obs, ov, nv) -> refreshTextFlow();



        focusListener = obs -> {
            if (getSkinnable().isFocused()) {
                // Ignore the very first focus event on boot to prevent corrupt layout snapshots
                if (!initialFocusHandled) {
                    initialFocusHandled = true;
                    paragraphListView.updateLayout();
                    return;
                }

                if (caretTracker != null) {
                    // Paralyze the tracker so it doesn't fight the native focus assignment
                    caretTracker.blackoutFrames = Math.max(caretTracker.blackoutFrames, 15);
                }

                // V585 Passive Focus Fix:
                // Do ABSOLUTELY NOTHING else here. No updateLayout(), no resetCaret().
                // Manually forcing a cell redraw while the native focus event is trying to
                // re-attach the caret path causes fractional height shifts and VirtualFlow panics.
            }
        };


        control.documentProperty().addListener((obs, ov, nv) -> {
            if (viewModel.isSaved()) {
                getSkinnable().requestFocus();
                return;
            }
            if (ov != null) {
                dispose();
            }
            setup(nv);
        });

        // set prompt text
        promptNode = new Text();
        setupPromptNode();
        viewModel.attachedProperty().subscribe((b0, b) -> attachedProperty.set(b));
        setup(control.getDocument());

        //this needs other initialization to work
        setMaps(KeyMapValue.BASE);
        String fontFamily = "Noto Serif Combo";
        TextDecoration decoration = (TextDecoration) viewModel.getDecorationAtCaret();
        ActionCmd actionCmd = ACTION_CMD_FACTORY.decorate(TextDecoration.builder().fromDecoration(decoration).fontFamily(fontFamily).build());
        if (actionCmd != null) {
            execute(actionCmd);
        }

        //ai added


        Platform.runLater(() -> {
            control.requestLayout();
            // Force the scene graph to resolve
            // This often wakes up the caret path if it's stuck at (0,0)
            control.layout();
            scrollIfNeeded();
        });
    }

    /// PROPERTIES ///////////////////////////////////////////////////////////////


    /// PUBLIC METHODS  /////////////////////////////////////////////////////////

    @Override
    public void dispose() {


        viewModel.clearSelection();
        viewModel.removeChangeListener(textChangeListener);
        viewModel.documentProperty().removeListener(documentChangeListener);
        viewModel.autoSaveProperty().unbind();
        lastValidCaretPosition = -1;
        promptNode.textProperty().unbind();
        promptNode.fillProperty().unbind();
        getSkinnable().editableProperty().removeListener(this::editableChangeListener);
        getSkinnable().tableAllowedProperty().removeListener(tableAllowedListener);
        getSkinnable().setOnKeyPressed(null);
        getSkinnable().setOnKeyTyped(null);
        getSkinnable().widthProperty().removeListener(controlPrefWidthListener);
        getSkinnable().focusedProperty().removeListener(focusListener);
        getSkinnable().removeEventHandler(DragEvent.ANY, dndHandler);
        getSkinnable().skinToneProperty().removeListener(skinToneChangeListener);
        contextMenu.getItems().clear();
        tableCellContextMenuItems = null;
        tableContextMenuItems = null;
        keyboardContextMenuItems = null;
        editableContextMenuItems = null;
        nonEditableContextMenuItems = null;
        attachedProperty.set(false);
        objectsCacheEvictionTimer.pause();
        LOG.fine("Disposed skin and paused timer");
    }

    public RichTextAreaViewModel getViewModel() {
        return viewModel;
    }

    Map<Integer, Font> getFontCache() {
        return fontCache;
    }

    Map<String, Image> getImageCache() {
        return imageCache;
    }

    /// PRIVATE METHODS /////////////////////////////////////////////////////////

    private void setup (Document document) {
        if (document == null) {
            return;
        }
        attachedProperty.set(false);

        viewModel.setTextBuffer (new PieceTable (document));

        // 1. SEED SYNCHRONOUSLY
        // We stop asking PieceTable for position 0, because it returns the end of the text chunk (e.g. 3069).
        lastValidCaretPosition = 0;
        viewModel.setCaretPosition(0);

        viewModel.addChangeListener (textChangeListener);

        // 2. Load the Document
        viewModel.setDocument (document);

        viewModel.documentProperty().addListener(documentChangeListener);
        viewModel.autoSaveProperty().bind(getSkinnable().autoSaveProperty());
        promptNode.textProperty().bind(getSkinnable().promptTextProperty());
        promptNode.fillProperty().bind(promptTextFillProperty());
        getSkinnable().setOnContextMenuRequested (contextMenuEventEventHandler);
        getSkinnable().editableProperty().addListener(this::editableChangeListener);
        getSkinnable().tableAllowedProperty().addListener (tableAllowedListener);
        viewModel.setTableAllowed (getSkinnable().isTableAllowed());
        getSkinnable().setOnKeyPressed (this::keyPressedListener);
        getSkinnable().setOnKeyTyped (this::keyTypedListener);
        getSkinnable().widthProperty().addListener (controlPrefWidthListener);

        // =========================================================
        // NEW FIX: FORCE LINE RE-WRAPPING ON RESIZE
        // JavaFX ListViews aggressively cache cell layouts. When the width
        // changes, we must force the internal paragraphListView to remeasure
        // the text so it can accurately re-wrap lines (especially when editable is false).
        // =========================================================
        getSkinnable().widthProperty().addListener((obs, oldVal, newVal) -> {
            if (paragraphListView != null && oldVal.doubleValue() != newVal.doubleValue()) {

                // Platform.runLater ensures the outer RichTextArea has fully adopted
                // the new bounds before we ask the text cells to remeasure themselves.
                javafx.application.Platform.runLater(() -> {

                    // This is the magic bullet: it forces the virtualized list to
                    // completely discard its cached bounds and redraw the text nodes.
                    paragraphListView.refresh();

                });
            }
        });
        // =========================================================




        getSkinnable().focusedProperty().addListener (focusListener);
        getSkinnable().addEventHandler (DragEvent.ANY, dndHandler);
        getSkinnable().skinToneProperty().addListener (skinToneChangeListener);

        refreshTextFlow();
        requestLayout();
        editableChangeListener(null);
        attachedProperty.set(true);

        // 3. Keep the viewport locked to the top AND forcefully pin the caret
        Runnable applyBootSync = () -> {
            if (viewModel != null) {
                // Fire the 'HOME' keyboard command to guarantee the caret goes to the absolute top
                ActionCmd homeCmd = ACTION_CMD_FACTORY.caretMove(Direction.BACK, false, false, true);
                if (homeCmd != null) {
                    homeCmd.apply(viewModel);
                }
                if (caretTracker != null) {
                    caretTracker.lastCaretPos = viewModel.getCaretPosition();
                }
            }
            if (paragraphListView != null) {
                // Force an explicit layout pass before scrolling
                paragraphListView.requestLayout();
                paragraphListView.layout();
                paragraphListView.scrollTo(0);
            }

            // =========================================================
            // NEW FIX: BOOTSTRAP PROMPT TEXT VISIBILITY
            // =========================================================
            // 1. Manually trigger the focus listener to evaluate prompt visibility.
            //    By passing the inverse of the current focus as the "old value",
            //    we simulate a state transition that guarantees the listener runs.
            boolean currentFocus = getSkinnable().isFocused();
            if (focusListener instanceof javafx.beans.value.ChangeListener) {
                @SuppressWarnings("unchecked")
                javafx.beans.value.ChangeListener<Boolean> listener =
                        (javafx.beans.value.ChangeListener<Boolean>) focusListener;
                listener.changed(getSkinnable().focusedProperty(), !currentFocus, currentFocus);
            } else if (focusListener instanceof javafx.beans.InvalidationListener) {
                ((javafx.beans.InvalidationListener) focusListener).invalidated(getSkinnable().focusedProperty());
            }

            // 2. Position the prompt text based on the newly calculated caret layout.
            //    (Safe to do here because paragraphListView.layout() just populated caretOriginProperty)
            if (promptNode.isVisible() && caretOriginProperty.get() != null) {
                updatePromptNodeLocation();
            }
            // =========================================================

            paragraphHeightCache.clear();
            lastReportedFullHeight = -1.0;
            heightDebouncer.playFromStart();

        };


        if (getSkinnable().getHeight() > 0) {
            // Double runLater ensures JavaFX completes its layout pulse before we scroll
            Platform.runLater(() -> Platform.runLater(applyBootSync));
        } else {
            javafx.beans.value.ChangeListener<Number> heightListener = new javafx.beans.value.ChangeListener<Number>() {
                @Override
                public void changed(javafx.beans.value.ObservableValue<? extends Number> obs, Number oldVal, Number newVal) {
                    if (newVal.doubleValue() > 0) {
                        getSkinnable().heightProperty().removeListener(this);
                        // Double runLater to survive the Pagination load cycle
                        Platform.runLater(() -> Platform.runLater(applyBootSync));
                    }
                }
            };
            getSkinnable().heightProperty().addListener(heightListener);
        }
    }

    private void setupPromptNode() {
        promptNode.setMouseTransparent(true);
        promptNode.setManaged(false);
        promptNode.setVisible(false);
        promptNode.getStyleClass().add("prompt");
        double promptNodeWidth = promptNode.prefWidth(-1);
        double promptNodeHeight = promptNode.prefHeight(promptNodeWidth);
        promptNode.resize(promptNodeWidth, promptNodeHeight);
        getChildren().add(promptNode);
    }

    private Font getPromptNodeFont() {
        Decoration decorationAtCaret = viewModel.getDecorationAtCaret();
        if (decorationAtCaret instanceof TextDecoration) {
            TextDecoration textDecoration = (TextDecoration) decorationAtCaret;

            int hash = Objects.hash(
                    textDecoration.getFontFamily(),
                    textDecoration.getFontWeight(),
                    textDecoration.getFontPosture(),
                    textDecoration.getFontSize());

            return getFontCache().computeIfAbsent(hash,
                    h -> Font.font(

                    textDecoration.getFontFamily(),
                    textDecoration.getFontWeight(),
                    textDecoration.getFontPosture(),
                    textDecoration.getFontSize()
            ));
        }
        return Font.font(DEFAULT_FONT_SIZE);
    }

    private void updatePromptNodeLocation() {
        double promptNodeWidth = promptNode.prefWidth(-1);
        TextAlignment alignment = viewModel.getDecorationAtParagraph().getAlignment();
        Point2D origin = caretOriginProperty.get();
        double x = origin.getX();
        if (alignment == TextAlignment.CENTER) {
            x -= promptNodeWidth / 2;
        } else if (alignment == TextAlignment.RIGHT) {
            x -= promptNodeWidth;
        }
        promptNode.relocate(x, origin.getY());
    }

    // TODO Need more optimal way of rendering text fragments.
    //  For now rebuilding the whole text flow

    private void refreshTextFlow() {
        objectsCacheEvictionTimer.pause();
        try {
            nonTextNodes.set(0);
            viewModel.resetCharacterIterator();

            // This natively destroys and recreates the cell
            paragraphListView.updateLayout();

            // =========================================================
            // V585 FIX 1: STOP THE DOWNWARD TYPING SAG
            // ALWAYS request a clean layout on the parent control!
            // This prevents the newly rebuilt cell from accumulating "dirty" fractional
            // heights that push the text down while typing.
            // =========================================================
            if (getSkinnable() != null) {
                getSkinnable().requestLayout();
            }

            // Keep the original image/table refresh logic
            if (nonTextNodesCount != nonTextNodes.get()) {
                requestLayout();
            }

            nonTextNodesCount = nonTextNodes.get();
            getSkinnable().requestFocus();
        } finally {
            objectsCacheEvictionTimer.start();
        }
    }



    Paragraph getLastParagraph() {
        return paragraphSortedList.get(paragraphSortedList.size() - 1);
    }

    private void editableChangeListener(Observable o) {
        boolean editable = getSkinnable().isEditable();
        viewModel.setEditable(editable);
        viewModel.setCaretPosition(editable ? lastValidCaretPosition : -1);
        paragraphListView.setCursor(editable ? Cursor.TEXT : Cursor.DEFAULT);
        populateContextMenu(editable);
        Platform.runLater(RichTextAreaSkin.this::scrollIfNeeded);  //before ai had paragraphListView::scrollIfNeeded
    }

    private void requestLayout() {
        paragraphListView.refresh();
        getSkinnable().requestLayout();
    }

    private Optional<Path> caret() {
        return paragraphListView.lookupAll(".caret").stream()
                .filter(Path.class::isInstance)
                .map(Path.class::cast)
                .filter(path -> !path.getElements().isEmpty())
                .findFirst();
    }

    // So far the only way to find prev/next row location is to use the size of the caret,
    // which always has the height of the row. Adding line spacing to it allows us to find a point which
    // belongs to the desired row. Then using the `hitTest` we can find the related caret position.
    private int getNextRowPosition(double x, Boolean down) {
        ObservableList<Paragraph> items = paragraphListView.getItems();
        int caretPosition = viewModel.getCaretPosition();
        int nextRowPosition = Math.min(viewModel.getTextLength(),
                paragraphListView.getNextRowPosition(x, down != null && down));
        // if the caret is at the top or bottom of the paragraph:
        if (down != null && ((down && nextRowPosition <= caretPosition) ||
                (!down && nextRowPosition >= caretPosition))) {
            int paragraphWithCaretIndex = items.stream()
                    .filter(p -> p.getStart() <= caretPosition &&
                            caretPosition < (p.equals(getLastParagraph()) ? p.getEnd() + 1 : p.getEnd()))
                    .mapToInt(items::indexOf)
                    .findFirst()
                    .orElse(-1);
            if (down) {
                // move to beginning of next paragraph or end
                int nextIndex = Math.min(items.size() - 1, paragraphWithCaretIndex + 1);
                Paragraph nextParagraph = items.get(nextIndex);
                return items.indexOf(nextParagraph) != paragraphWithCaretIndex ?
                        nextParagraph.getStart() : viewModel.getTextLength();
            } else {
                // move to end of previous paragraph or home
                int prevIndex = Math.max(0, paragraphWithCaretIndex - 1);
                Paragraph prevParagraph = items.get(prevIndex);
                return items.indexOf(prevParagraph) != paragraphWithCaretIndex ?
                        Math.max(0, prevParagraph.getEnd() - 1) : 0;
            }
        }
        return nextRowPosition;
    }

    private int getNextTableCellPosition(Boolean down) {
        return Math.min(viewModel.getTextLength(),
                paragraphListView.getNextTableCellPosition(down != null && down));
    }

    private static boolean isPrintableChar(char c) {
        Character.UnicodeBlock changeBlock = Character.UnicodeBlock.of(c);
        return (c == '\n' || c == '\t' || !Character.isISOControl(c)) &&
                !KeyEvent.CHAR_UNDEFINED.equals(String.valueOf(c)) &&
                changeBlock != null && changeBlock != Character.UnicodeBlock.SPECIALS;
    }

    private static boolean isCharOnly(KeyEvent e) {
        char c = e.getCharacter().isEmpty()? 0: e.getCharacter().charAt(0);
        return isPrintableChar(c) &&
               !e.isControlDown() &&
               !e.isMetaDown();
    }

    private void execute(ActionCmd action) {
        Objects.requireNonNull(action).apply(viewModel);

        if (heightDebouncer != null) {
            heightDebouncer.stop();
            heightDebouncer.playFromStart();
        }
    }

    private void keyPressedListener(KeyEvent e) {
 //       long a0 = System.nanoTime();
        //Print character assigned to key
        for (KeyCombination kc : keyPressedCharMap.keySet()) {
            if (kc.match(e)) {
                String text = getTypedCharModification(keyPressedCharMap.get(kc));
                sendKeyboardContent(text);
                e.consume();
                return;
            }
        }
        // Find an applicable action and execute it if found
        for (KeyCombination kc : INPUT_MAP.keySet()) {
            if (kc.match(e)) {
                ActionBuilder actionBuilder = INPUT_MAP.get(kc);
                ActionCmd actionCmd = actionBuilder.apply(e);
                if (actionCmd != null) {
                    execute(actionCmd);
                }
                e.consume();
                return;
            }
        }
    }

    private void keyTypedListener(KeyEvent e) {
   //     long a0 = System.nanoTime();
        if (e.isAltDown() || e.isShortcutDown()) return;
        String text;
        //Print character mapped to key
        for (Character key : keyTypedCharMap.keySet()) {
            boolean test = Character.toString(key).equals(e.getCharacter());
            if (test) {
                text = getTypedCharModification(keyTypedCharMap.get(key));
                sendKeyboardContent(text);
                e.consume();
                return;
            }
        }
        if (isCharOnly(e)) {
            if ("\t".equals(e.getCharacter())) {
                ParagraphDecoration decoration = viewModel.getDecorationAtParagraph();
                if (decoration != null &&
                    (decoration.getGraphicType() != ParagraphDecoration.GraphicType.NONE || decoration.hasTableDecoration())) {
                    // processed via keyPressedListener
                    e.consume();
                    return;
                }
            }
            text = getTypedCharModification(e.getCharacter());
  //          if (getOverlineOn()) text = "\u035e" + text;
            sendKeyboardContent(text);
            e.consume();
        }
    }


    private void sendKeyboardContent(String s) {

        paragraphListView.resetCaret();
        if (viewModel.getSelection().isDefined()) {
            execute(ACTION_CMD_FACTORY.replaceText(s));
        } else {
            execute(ACTION_CMD_FACTORY.insertText(s));
        }
    }


    /*
     * Get modified string and reset the modification flag
     * @param inString the string to be modified
     * @return the modified string
     */
    private String getTypedCharModification(String inString) {

        String outString = null;
        if (getModification() == NONE) {
            outString = inString;
        }
        else if (getModification() == OVERLINE) {
            outString = charModMaps.getOverlineMap().get(inString);
            if (!getOverlineOn()) setModification(NONE);
        }
        else if (getModification() == MAC) {
            outString = charModMaps.getMacMap().get(inString);
            setModification(NONE);
        }
        else if (getModification() == VEC) {
            outString = charModMaps.getVecMap().get(inString);
            setModification(NONE);
        }
        else if (getModification() == HAT) {
            outString = charModMaps.getHatMap().get(inString);
            setModification(NONE);
        }
        else if (getModification() == SLASH) {

            if (getKeyMapState() == KeyMapValue.BASE ) outString = charModMaps.getSlashBaseItalMap().get(inString);
            else if (getKeyMapState() == KeyMapValue.BASE_AND_SCRIPT ) outString = charModMaps.getSlashBaseScriptMap().get(inString);
            else if (getKeyMapState() == KeyMapValue.BASE_AND_SANS ) outString = charModMaps.getSlashBaseSansMap().get(inString);
            else if (getKeyMapState() == KeyMapValue.ITALIC_AND_SANS ) outString = charModMaps.getSlashItalSansMap().get(inString);
            else if (getKeyMapState() == KeyMapValue.SANS_AND_ITALIC) outString = charModMaps.getSlashSansItalMap().get(inString);
            else if (getKeyMapState() == KeyMapValue.ITALIC_AND_BLACKBOARD ) outString = charModMaps.getSlashItalBBMap().get(inString);
            else if (getKeyMapState() == KeyMapValue.SCRIPT_AND_ITALIC ) outString = charModMaps.getSlashScriptItalMap().get(inString);
            else if (getKeyMapState() == KeyMapValue.SCRIPT_AND_SANS ) outString = charModMaps.getSlashScriptSansMap().get(inString);
            else if (getKeyMapState() == KeyMapValue.GREEK_AND_FRAKTUR ) outString = charModMaps.getSlashGreekFrakMap().get(inString);

            setModification(NONE);
        }

        if (outString == null) outString = "\ud835";     //not a defined print character (shows box)
        return outString;
    }

    /*
     *Get char for keyboard diagram 
     * @param inString the string to be modified
     * @return the modified string
     */
    public String getModifiedChar(String inString) {

        String outString = null;
        if (getModification() == NONE) {
            outString = inString;
        }
        else if (getModification() == OVERLINE) {
            outString = charModMaps.getOverlineMap().get(inString);
        }
        else if (getModification() == MAC) {
            outString = charModMaps.getMacMap().get(inString);
        }
        else if (getModification() == VEC) {
            outString = charModMaps.getVecMap().get(inString);
        }
        else if (getModification() == HAT) {
            outString = charModMaps.getHatMap().get(inString);
        }
        else if (getModification() == SLASH) {
            if (getKeyMapState() == KeyMapValue.BASE ) outString = charModMaps.getSlashBaseItalMap().get(inString);
            else if (getKeyMapState() == KeyMapValue.BASE_AND_SCRIPT ) outString = charModMaps.getSlashBaseScriptMap().get(inString);
            else if (getKeyMapState() == KeyMapValue.BASE_AND_SANS ) outString = charModMaps.getSlashBaseSansMap().get(inString);
            else if (getKeyMapState() == KeyMapValue.ITALIC_AND_SANS ) outString = charModMaps.getSlashItalSansMap().get(inString);
            else if (getKeyMapState() == KeyMapValue.SANS_AND_ITALIC ) outString = charModMaps.getSlashSansItalMap().get(inString);
            else if (getKeyMapState() == KeyMapValue.ITALIC_AND_BLACKBOARD ) outString = charModMaps.getSlashItalBBMap().get(inString);
            else if (getKeyMapState() == KeyMapValue.SCRIPT_AND_ITALIC ) outString = charModMaps.getSlashScriptItalMap().get(inString);
            else if (getKeyMapState() == KeyMapValue.SCRIPT_AND_SANS ) outString = charModMaps.getSlashScriptSansMap().get(inString);
            else if (getKeyMapState() == KeyMapValue.GREEK_AND_FRAKTUR ) outString = charModMaps.getSlashGreekFrakMap().get(inString);
        }

        if (outString == null) outString = "";     //keyboard diagram has empty block
        return outString;
    }

    public double getInsets() {
        double listInsets = 0;
        if (paragraphListView.getInsets() != null) {
            listInsets = paragraphListView.getInsets().getTop() + paragraphListView.getInsets().getBottom();
        }
        return listInsets;
    }


    private final java.util.Map<Paragraph, Double> paragraphHeightCache = new java.util.WeakHashMap<>();

    private void computeFullHeight() {
        if (getSkinnable() == null || paragraphListView == null) {
            fullHeightProperty.set(0);
            return;
        }

        var items = paragraphListView.getItems();
        if (items == null || items.isEmpty()) {
            fullHeightProperty.set(0);
            return;
        }

        //new ai version
        if (measurementCell == null) {
            measurementCell = createCell();

            // CRITICAL FIX: Inject the ListView reference into the measurement cell.
            // This triggers the listViewProperty listener in RichListCell, activating
            // the "Iron Wall" width bindings so offscreen items measure perfectly.
            measurementCell.updateListView(paragraphListView);

            javafx.scene.layout.Pane dummyList = new javafx.scene.layout.Pane(measurementCell) {
                @Override public void requestLayout() {}
            };
            dummyList.getStyleClass().addAll(paragraphListView.getStyleClass());
            dummyList.setManaged(false);
            dummyList.setVisible(false);
            getChildren().add(dummyList);
        }

        /*
        if (measurementCell == null) {
            measurementCell = createCell();
            javafx.scene.layout.Pane dummyList = new javafx.scene.layout.Pane(measurementCell) {
                @Override public void requestLayout() {}
            };
            dummyList.getStyleClass().addAll(paragraphListView.getStyleClass());
            dummyList.setManaged(false);
            dummyList.setVisible(false);
            getChildren().add(dummyList);
        }

         */

        double availableWidth = textFlowPrefWidthProperty.get();
        if (availableWidth <= 0) availableWidth = getSkinnable().getContentAreaWidth();
        if (availableWidth <= 0) availableWidth = 500;

        measurementCell.getParent().applyCss();

        java.util.Map<Integer, Double> visibleHeights = new java.util.HashMap<>();

        //newer version
        for (javafx.scene.Node node : paragraphListView.lookupAll(".list-cell")) {
            if (node instanceof javafx.scene.control.ListCell) {
                @SuppressWarnings("unchecked")
                javafx.scene.control.ListCell<Paragraph> cell = (javafx.scene.control.ListCell<Paragraph>) node;
                Paragraph p = cell.getItem();

                if (p != null && cell.getHeight() > 0) {
                    // Remove prefHeight(-1). Use the exact physical layout height.
                    double h = cell.getHeight();

                    if (paragraphHeightCache.containsKey(p)) {
                        double cached = paragraphHeightCache.get(p);
                        if (Math.abs(h - cached) <= 5.0) {
                            h = cached;
                        }
                    }
                    visibleHeights.put(cell.getIndex(), h);
                }
            }
        }

        /*
        for (javafx.scene.Node node : paragraphListView.lookupAll(".list-cell")) {
            if (node instanceof javafx.scene.control.ListCell) {
                @SuppressWarnings("unchecked")
                javafx.scene.control.ListCell<Paragraph> cell = (javafx.scene.control.ListCell<Paragraph>) node;
                Paragraph p = cell.getItem();

                if (p != null && cell.getHeight() > 0) {
                    double prefH = cell.prefHeight(-1);
                    double h = prefH > 0 ? prefH : cell.getHeight();
                    if (paragraphHeightCache.containsKey(p)) {
                        double cached = paragraphHeightCache.get(p);
                        if (Math.abs(h - cached) <= 5.0) {
                            h = cached;
                        }
                    }
                    visibleHeights.put(cell.getIndex(), h);
                }
            }
        }

         */

        double fullHeight = 0;
        for (int i = 0; i < items.size(); i++) {
            var item = items.get(i);

            if (visibleHeights.containsKey(i)) {
                double h = visibleHeights.get(i);
                fullHeight += h;
                paragraphHeightCache.put(item, h);
            } else if (paragraphHeightCache.containsKey(item)) {
                fullHeight += paragraphHeightCache.get(item);

                //newer version
            } else {
                measurementCell.updateItem(null, true);
                measurementCell.updateItem(item, false);
                measurementCell.applyCss();
                measurementCell.layout();

                // Just raw calculation. No +2.0 padding.
                double prefH = measurementCell.prefHeight(availableWidth);
                double h = prefH > 0 ? prefH : measurementCell.getHeight();

                fullHeight += h;
                paragraphHeightCache.put(item, h);
                measurementCell.updateItem(null, true);
            }

            /*
            } else {
                measurementCell.updateItem(null, true);
                measurementCell.updateItem(item, false);

                // The cell width is already strictly bound by the "Iron Wall" in your RichListCell
                // constructor. We do not need to (and cannot) set it manually. Just force the layout.
                measurementCell.applyCss();
                measurementCell.layout();

                // Calculate the width to pass into the prefHeight measurement
                double cellWidth = paragraphListView.getWidth() - 25.0;
                if (cellWidth <= 0) cellWidth = availableWidth; // Fallback if listview isn't laid out yet

                // Measure it strictly against that width
                double prefH = measurementCell.prefHeight(cellWidth);
                double h = prefH > 0 ? prefH : measurementCell.getHeight();

                fullHeight += h;
                paragraphHeightCache.put(item, h);
                measurementCell.updateItem(null, true);
            }

             */
        }

        double diff = Math.abs(fullHeight - lastReportedFullHeight);
        if (lastReportedFullHeight < 0 || diff > 10.0) {
            if (lastReportedFullHeight >= 0) {
          //      System.out.println("[DIAG-DOC] Full Height Changed! Old: " + lastReportedFullHeight + " | New: " + fullHeight + " | Diff: " + diff);
            }
            lastReportedFullHeight = fullHeight;
            fullHeightProperty.set(fullHeight);
            if (caretTracker != null) {
                caretTracker.requestScroll();
            }
        }
    }


    /*
    private void computeFullHeight() {
        fullHeightProperty.set(0);
        RichListCell cell = new RichListCell(RichTextAreaSkin.this);
        double rtaWidth = getSkinnable().prefWidth(-1);
        Scene scene = new Scene(cell, rtaWidth, 1000);
        if (getSkinnable().getScene() != null) {
            scene.getStylesheets().setAll(getSkinnable().getScene().getStylesheets());
        }

        double fullHeight = paragraphListView.getItems().stream()
                .mapToDouble(item -> {
                    cell.updateItem(item, false);
                    cell.applyCss();
                    cell.layout();
                    return cell.prefHeight(rtaWidth);
                })
                .sum();

        //added by me to compensate for line feed weirdness  not right esp with many paragraphs

        int n = 0;
        if (paragraphListView.getItems().size() > 1) n = paragraphListView.getItems().size()- 1;
        fullHeight = fullHeight - n * 7.0;
        //

        fullHeightProperty.set(fullHeight);
        // TODO
    }
     */

    private void populateContextMenu(boolean isEditable) {
        if (isEditable && editableContextMenuItems == null) {
            editableContextMenuItems = FXCollections.observableArrayList(
                    createMenuItem(resources.getString("rta.context.menu.undo"), ACTION_CMD_FACTORY.undo()),
                    createMenuItem(resources.getString("rta.context.menu.redo"), ACTION_CMD_FACTORY.redo()),
                    new SeparatorMenuItem(),
                    createMenuItem(resources.getString("rta.context.menu.copy"), ACTION_CMD_FACTORY.copy()),
                    createMenuItem(resources.getString("rta.context.menu.cut"), ACTION_CMD_FACTORY.cut()),
                    createMenuItem(resources.getString("rta.context.menu.paste"), ACTION_CMD_FACTORY.paste()),
                    new SeparatorMenuItem(),
                    createMenuItem(resources.getString("rta.context.menu.selectall"), ACTION_CMD_FACTORY.selectAll()));

            if (getSkinnable().isTableAllowed()) {
                tableCellContextMenuItems = FXCollections.observableArrayList(
                        createMenuItem(resources.getString("rta.context.menu.table.cell.delete"), ACTION_CMD_FACTORY.deleteTableCell()),
                        new SeparatorMenuItem(),
                        createMenuItem(resources.getString("rta.context.menu.table.cell.alignleft"), ACTION_CMD_FACTORY.alignTableCell(TextAlignment.LEFT)),
                        createMenuItem(resources.getString("rta.context.menu.table.cell.centre"), ACTION_CMD_FACTORY.alignTableCell(TextAlignment.CENTER)),
                        createMenuItem(resources.getString("rta.context.menu.table.cell.justify"), ACTION_CMD_FACTORY.alignTableCell(TextAlignment.JUSTIFY)),
                        createMenuItem(resources.getString("rta.context.menu.table.cell.alignright"), ACTION_CMD_FACTORY.alignTableCell(TextAlignment.RIGHT))
                );
                Menu tableCellMenu = new Menu(resources.getString("rta.context.menu.table.cell"));
                tableCellMenu.setStyle("-fx-selection-bar: #0096C9; -fx-selection-bar-text: white;");

                tableCellMenu.getItems().addAll(tableCellContextMenuItems);
                MenuItem insertTableMenuItem = createMenuItem(resources.getString("rta.context.menu.table.insert"), ACTION_CMD_FACTORY.insertTable(new TableDecoration(1, 2)));
                tableCellMenu.disableProperty().bind(insertTableMenuItem.disableProperty().not());
                tableContextMenuItems = FXCollections.observableArrayList(
                        insertTableMenuItem,
                        createMenuItem(resources.getString("rta.context.menu.table.delete"), ACTION_CMD_FACTORY.deleteTable()),
                        new SeparatorMenuItem(),
                        createMenuItem(resources.getString("rta.context.menu.table.column.before"), ACTION_CMD_FACTORY.insertTableColumnBefore()),
                        createMenuItem(resources.getString("rta.context.menu.table.column.after"), ACTION_CMD_FACTORY.insertTableColumnAfter()),
                        createMenuItem(resources.getString("rta.context.menu.table.column.delete"), ACTION_CMD_FACTORY.deleteTableColumn()),
                        new SeparatorMenuItem(),
                        createMenuItem(resources.getString("rta.context.menu.table.row.above"), ACTION_CMD_FACTORY.insertTableRowAbove()),
                        createMenuItem(resources.getString("rta.context.menu.table.row.below"), ACTION_CMD_FACTORY.insertTableRowBelow()),
                        createMenuItem(resources.getString("rta.context.menu.table.row.delete"), ACTION_CMD_FACTORY.deleteTableRow()),
                        new SeparatorMenuItem(),
                        tableCellMenu
                );
                Menu tableMenu = new Menu(resources.getString("rta.context.menu.table"));
                tableMenu.getItems().addAll(tableContextMenuItems);


                Menu keyboardMenu = new Menu("Keyboards");
                keyboardMenu.setStyle("-fx-selection-bar: #0096C9; -fx-selection-bar-text: white;");

                MenuItem baseItalicItem = new MenuItem("Base/Italic");
                baseItalicItem.setOnAction(e -> setKeyMapState(KeyMapValue.BASE))
                ;
                MenuItem baseScriptItem = new MenuItem("Base/Script");
                baseScriptItem.setOnAction(e -> setKeyMapState(KeyMapValue.BASE_AND_SCRIPT));

                MenuItem baseSansItem = new MenuItem("Base/Sans");
                baseSansItem.setOnAction(e -> setKeyMapState(KeyMapValue.BASE_AND_SANS));

                MenuItem italicSansItem = new MenuItem("Italic/Sans");
                italicSansItem.setOnAction(e -> setKeyMapState(KeyMapValue.ITALIC_AND_SANS));

                MenuItem sansItalicItem = new MenuItem("Sans/Italic");
                sansItalicItem.setOnAction(e -> setKeyMapState(KeyMapValue.SANS_AND_ITALIC));

                MenuItem italicBlackboardItem = new MenuItem("Italic/Blackboard");
                italicBlackboardItem.setOnAction(e -> setKeyMapState(KeyMapValue.ITALIC_AND_BLACKBOARD));

                MenuItem scriptItalicItem = new MenuItem("Script/Italic");
                scriptItalicItem.setOnAction(e -> setKeyMapState(KeyMapValue.SCRIPT_AND_ITALIC));

                MenuItem scriptSansItem = new MenuItem("Script/Sans");
                scriptSansItem.setOnAction(e -> setKeyMapState(KeyMapValue.SCRIPT_AND_SANS));

                MenuItem greekFrakturItem = new MenuItem("Greek/Fraktur");
                greekFrakturItem.setOnAction(e -> setKeyMapState(KeyMapValue.GREEK_AND_FRAKTUR));


                keyboardContextMenuItems = FXCollections.observableArrayList(
                        baseItalicItem, baseScriptItem, baseSansItem, italicSansItem, sansItalicItem, italicBlackboardItem, scriptItalicItem, scriptSansItem, greekFrakturItem
                );
           //     keyboardMenu.getItems().clear();
                keyboardMenu.getItems().addAll(keyboardContextMenuItems);

                editableContextMenuItems.addAll(new SeparatorMenuItem(), keyboardMenu, tableMenu);



            }
        } else if (!isEditable && nonEditableContextMenuItems == null) {
            nonEditableContextMenuItems = FXCollections.observableArrayList(
                    createMenuItem(resources.getString("rta.context.menu.copy"), ACTION_CMD_FACTORY.copy()),
                    new SeparatorMenuItem(),
                    createMenuItem(resources.getString("rta.context.menu.selectall"), ACTION_CMD_FACTORY.selectAll()));
        }
        contextMenu.getItems().setAll(isEditable ? editableContextMenuItems : nonEditableContextMenuItems);
        contextMenu.setStyle("-fx-selection-bar: #0096C9; -fx-selection-bar-text: white;");
    }

    private MenuItem createMenuItem(String text, ActionCmd actionCmd) {
        MenuItem menuItem = new MenuItem(text);
        menuItem.disableProperty().bind(actionCmd.getDisabledBinding(viewModel));
        menuItem.setOnAction(e -> actionCmd.apply(viewModel));
        return menuItem;
    }

    private void dndListener(DragEvent dragEvent) {
        if (dragEvent.getEventType() == DragEvent.DRAG_ENTERED) {
            dragAndDropStart = 1;
        } else if (dragEvent.getEventType() == DragEvent.DRAG_DONE || dragEvent.getEventType() == DragEvent.DRAG_EXITED) {
            dragAndDropStart = -1;
        } else if (dragEvent.getEventType() == DragEvent.DRAG_OVER) {
            Dragboard dragboard = dragEvent.getDragboard();
            if (dragboard.hasImage() || dragboard.hasString() || dragboard.hasUrl() | dragboard.hasFiles()) {
                dragEvent.acceptTransferModes(TransferMode.ANY);
            }
        } else if (dragEvent.getEventType() == DragEvent.DRAG_DROPPED) {
            Dragboard dragboard = dragEvent.getDragboard();
            if (!dragboard.getFiles().isEmpty()) {
                dragboard.getFiles().forEach(file -> {
                    String url = file.toURI().toString();
                    // validate image before adding it
                    if (url != null && new Image(url).getException() == null) {
                        ACTION_CMD_FACTORY.decorate(new ImageDecoration(url)).apply(viewModel);
                    }
                });
            } else if (dragboard.hasUrl()) {
                String url = dragboard.getUrl();
                // validate if url is an image before adding it:
                if (url != null) {
                    if (new Image(url).getException() == null) {
                        ACTION_CMD_FACTORY.decorate(new ImageDecoration(url)).apply(viewModel);
                    } else {
                        // add text and hyperlink
                        int caret = viewModel.getCaretPosition();
                        ACTION_CMD_FACTORY.insertText(url).apply(viewModel);
                        viewModel.setSelection(new Selection(caret, caret + url.length()));
                        ACTION_CMD_FACTORY.decorate(TextDecoration.builder().url(url).build()).apply(viewModel);
                    }
                }
            } else if (dragboard.hasString()) {
                ACTION_CMD_FACTORY.insertText(dragboard.getString()).apply(viewModel);
            }
            requestLayout();
            dragAndDropStart = -1;
        }
    }

    private static final StyleablePropertyFactory<RichTextArea> FACTORY = new StyleablePropertyFactory<>(SkinBase.getClassCssMetaData());

    public static List<CssMetaData<? extends Styleable, ?>> getClassCssMetaData() {
        return FACTORY.getCssMetaData();
    }


    public List<CssMetaData<? extends Styleable, ?>> getCssMetaData() {
        return getClassCssMetaData();
    }

    private Map<Character, String> initializeBaseMap() {
        return Map.ofEntries(
                entry('a', "a"),
                entry('b', "b"),
                entry('c', "c"),
                entry('d', "d"),
                entry('e', "e"),
                entry('f', "f"),
                entry('g', "g"),
                entry('h', "h"),
                entry('i', "i"),
                entry('j', "j"),
                entry('k', "k"),
                entry('l', "l"),
                entry('m', "m"),
                entry('n', "n"),
                entry('o', "o"),
                entry('p', "p"),
                entry('q', "q"),
                entry('r', "r"),
                entry('s', "s"),
                entry('t', "t"),
                entry('u', "u"),
                entry('v', "v"),
                entry('w', "w"),
                entry('x', "x"),
                entry('y', "y"),
                entry('z', "z"),
                entry('A', "A"),
                entry('B', "B"),
                entry('C', "C"),
                entry('D', "D"),
                entry('E', "E"),
                entry('F', "F"),
                entry('G', "G"),
                entry('H', "H"),
                entry('I', "I"),
                entry('J', "J"),
                entry('K', "K"),
                entry('L', "L"),
                entry('M', "M"),
                entry('N', "N"),
                entry('O', "O"),
                entry('P', "P"),
                entry('Q', "Q"),
                entry('R', "R"),
                entry('S', "S"),
                entry('T', "T"),
                entry('U', "U"),
                entry('V', "V"),
                entry('W', "W"),
                entry('X', "X"),
                entry('Y', "Y"),
                entry('Z', "Z"),
                entry('`', "`"),
                entry('1', "1"),
                entry('2', "2"),
                entry('3', "3"),
                entry('4', "4"),
                entry('5', "5"),
                entry('6', "6"),
                entry('7', "7"),
                entry('8', "8"),
                entry('9', "9"),
                entry('0', "0"),
                entry('-', "-"),
                entry('=', "="),
                entry('[', "["),
                entry(']', "]"),
                entry('\\', "\\"),
                entry(';', ";"),
                entry('\'', "'"),
                entry(',', ","),
                entry('.', "."),
                entry('/', "/"),
                entry('~', "\u223c"),
                entry('!', "!"),
                entry('@', "@"),
                entry('#', "#"),
                entry('$', "$"),
                entry('%', "%"),
                entry('^', "^"),
                entry('&', "&"),
                entry('*', "*"),
                entry('(', "("),
                entry(')', ")"),
                entry('_', "_"),
                entry('+', "+"),
                entry('{', "{"),
                entry('}', "}"),
                entry('|', "|"),
                entry(':', ":"),
                entry('\"', "\""),
                entry('<', "<"),
                entry('>', ">"),
                entry('?', "?")
        );

    }

    private Map<KeyCodeCombination, String> initializeItalicAltMap() {
        return Map.ofEntries(
                entry(new KeyCodeCombination(KeyCode.A, KeyCombination.ALT_DOWN), "\ud835\udc4e"),
                entry(new KeyCodeCombination(KeyCode.B, KeyCombination.ALT_DOWN), "\ud835\udc4f"),
                entry(new KeyCodeCombination(KeyCode.C, KeyCombination.ALT_DOWN), "\ud835\udc50"),
                entry(new KeyCodeCombination(KeyCode.D, KeyCombination.ALT_DOWN), "\ud835\udc51"),
                entry(new KeyCodeCombination(KeyCode.E, KeyCombination.ALT_DOWN), "\ud835\udc52"),
                entry(new KeyCodeCombination(KeyCode.F, KeyCombination.ALT_DOWN), "\ud835\udc53"),
                entry(new KeyCodeCombination(KeyCode.G, KeyCombination.ALT_DOWN), "\ud835\udc54"),
                entry(new KeyCodeCombination(KeyCode.H, KeyCombination.ALT_DOWN), "\u210e"),
                entry(new KeyCodeCombination(KeyCode.I, KeyCombination.ALT_DOWN), "\ud835\udc56"),
                entry(new KeyCodeCombination(KeyCode.J, KeyCombination.ALT_DOWN), "\ud835\udc57"),
                entry(new KeyCodeCombination(KeyCode.K, KeyCombination.ALT_DOWN), "\ud835\udc58"),
                entry(new KeyCodeCombination(KeyCode.L, KeyCombination.ALT_DOWN), "\ud835\udc59"),
                entry(new KeyCodeCombination(KeyCode.M, KeyCombination.ALT_DOWN), "\ud835\udc5a"),
                entry(new KeyCodeCombination(KeyCode.N, KeyCombination.ALT_DOWN), "\ud835\udc5b"),
                entry(new KeyCodeCombination(KeyCode.O, KeyCombination.ALT_DOWN), "\ud835\udc5c"),
                entry(new KeyCodeCombination(KeyCode.P, KeyCombination.ALT_DOWN), "\ud835\udc5d"),
                entry(new KeyCodeCombination(KeyCode.Q, KeyCombination.ALT_DOWN), "\ud835\udc5e"),
                entry(new KeyCodeCombination(KeyCode.R, KeyCombination.ALT_DOWN), "\ud835\udc5f"),
                entry(new KeyCodeCombination(KeyCode.S, KeyCombination.ALT_DOWN), "\ud835\udc60"),
                entry(new KeyCodeCombination(KeyCode.T, KeyCombination.ALT_DOWN), "\ud835\udc61"),
                entry(new KeyCodeCombination(KeyCode.U, KeyCombination.ALT_DOWN), "\ud835\udc62"),
                entry(new KeyCodeCombination(KeyCode.V, KeyCombination.ALT_DOWN), "\ud835\udc63"),
                entry(new KeyCodeCombination(KeyCode.W, KeyCombination.ALT_DOWN), "\ud835\udc64"),
                entry(new KeyCodeCombination(KeyCode.X, KeyCombination.ALT_DOWN), "\ud835\udc65"),
                entry(new KeyCodeCombination(KeyCode.Y, KeyCombination.ALT_DOWN), "\ud835\udc66"),
                entry(new KeyCodeCombination(KeyCode.Z, KeyCombination.ALT_DOWN), "\ud835\udc67"),

                entry(new KeyCodeCombination(KeyCode.DIGIT0, KeyCombination.ALT_DOWN), "0"),
                entry(new KeyCodeCombination(KeyCode.DIGIT1, KeyCombination.ALT_DOWN), "1"),
                entry(new KeyCodeCombination(KeyCode.DIGIT2, KeyCombination.ALT_DOWN), "2"),
                entry(new KeyCodeCombination(KeyCode.DIGIT3, KeyCombination.ALT_DOWN), "3"),
                entry(new KeyCodeCombination(KeyCode.DIGIT4, KeyCombination.ALT_DOWN), "4"),
                entry(new KeyCodeCombination(KeyCode.DIGIT5, KeyCombination.ALT_DOWN), "5"),    //top
                entry(new KeyCodeCombination(KeyCode.DIGIT6, KeyCombination.ALT_DOWN), "6"),
                entry(new KeyCodeCombination(KeyCode.DIGIT7, KeyCombination.ALT_DOWN), "7"),
                entry(new KeyCodeCombination(KeyCode.DIGIT8, KeyCombination.ALT_DOWN), "8"),
                entry(new KeyCodeCombination(KeyCode.DIGIT9, KeyCombination.ALT_DOWN), "9"),
                entry(new KeyCodeCombination(KeyCode.A, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN), "\ud835\udc34"),
                entry(new KeyCodeCombination(KeyCode.B, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN), "\ud835\udc35"),
                entry(new KeyCodeCombination(KeyCode.C, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN), "\ud835\udc36"),
                entry(new KeyCodeCombination(KeyCode.D, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN), "\ud835\udc37"),
                entry(new KeyCodeCombination(KeyCode.E, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN), "\ud835\udc38"),
                entry(new KeyCodeCombination(KeyCode.F, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN), "\ud835\udc39"),
                entry(new KeyCodeCombination(KeyCode.G, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN), "\ud835\udc3a"),
                entry(new KeyCodeCombination(KeyCode.H, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN), "\ud835\udc3b"),
                entry(new KeyCodeCombination(KeyCode.I, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN), "\ud835\udc3c"),
                entry(new KeyCodeCombination(KeyCode.J, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN), "\ud835\udc3d"),
                entry(new KeyCodeCombination(KeyCode.K, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN), "\ud835\udc3e"),
                entry(new KeyCodeCombination(KeyCode.L, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN), "\ud835\udc3f"),
                entry(new KeyCodeCombination(KeyCode.M, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN), "\ud835\udc40"),
                entry(new KeyCodeCombination(KeyCode.N, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN), "\ud835\udc41"),
                entry(new KeyCodeCombination(KeyCode.O, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN), "\ud835\udc42"),
                entry(new KeyCodeCombination(KeyCode.P, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN), "\ud835\udc43"),
                entry(new KeyCodeCombination(KeyCode.Q, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN), "\ud835\udc44"),
                entry(new KeyCodeCombination(KeyCode.R, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN), "\ud835\udc45"),
                entry(new KeyCodeCombination(KeyCode.S, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN), "\ud835\udc46"),
                entry(new KeyCodeCombination(KeyCode.T, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN), "\ud835\udc47"),
                entry(new KeyCodeCombination(KeyCode.U, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN), "\ud835\udc48"),
                entry(new KeyCodeCombination(KeyCode.V, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN), "\ud835\udc49"),
                entry(new KeyCodeCombination(KeyCode.W, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN), "\ud835\udc4a"),
                entry(new KeyCodeCombination(KeyCode.X, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN), "\ud835\udc4b"),
                entry(new KeyCodeCombination(KeyCode.Y, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN), "\ud835\udc4c"),
                entry(new KeyCodeCombination(KeyCode.Z, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN), "\ud835\udc4d")

        );
    }

    private Map<Character, String> initializeItalicMap() {
        return Map.ofEntries(
                entry('a', "\ud835\udc4e"),
                entry('b', "\ud835\udc4f"),
                entry('c', "\ud835\udc50"),
                entry('d', "\ud835\udc51"),
                entry('e', "\ud835\udc52"),
                entry('f', "\ud835\udc53"),
                entry('g', "\ud835\udc54"),
                entry('h', "\u210e"),
                entry('i', "\ud835\udc56"),
                entry('j', "\ud835\udc57"),
                entry('k', "\ud835\udc58"),
                entry('l', "\ud835\udc59"),
                entry('m', "\ud835\udc5a"),
                entry('n', "\ud835\udc5b"),
                entry('o', "\ud835\udc5c"),
                entry('p', "\ud835\udc5d"),
                entry('q', "\ud835\udc5e"),
                entry('r', "\ud835\udc5f"),
                entry('s', "\ud835\udc60"),
                entry('t', "\ud835\udc61"),
                entry('u', "\ud835\udc62"),
                entry('v', "\ud835\udc63"),
                entry('w', "\ud835\udc64"),
                entry('x', "\ud835\udc65"),
                entry('y', "\ud835\udc66"),
                entry('z', "\ud835\udc67"),
                entry('A', "\ud835\udc34"),
                entry('B', "\ud835\udc35"),
                entry('C', "\ud835\udc36"),
                entry('D', "\ud835\udc37"),
                entry('E', "\ud835\udc38"),
                entry('F', "\ud835\udc39"),
                entry('G', "\ud835\udc3a"),
                entry('H', "\ud835\udc3b"),
                entry('I', "\ud835\udc3c"),
                entry('J', "\ud835\udc3d"),
                entry('K', "\ud835\udc3e"),
                entry('L', "\ud835\udc3f"),
                entry('M', "\ud835\udc40"),
                entry('N', "\ud835\udc41"),
                entry('O', "\ud835\udc42"),
                entry('P', "\ud835\udc43"),
                entry('Q', "\ud835\udc44"),
                entry('R', "\ud835\udc45"),
                entry('S', "\ud835\udc46"),
                entry('T', "\ud835\udc47"),
                entry('U', "\ud835\udc48"),
                entry('V', "\ud835\udc49"),
                entry('W', "\ud835\udc4a"),
                entry('X', "\ud835\udc4b"),
                entry('Y', "\ud835\udc4c"),
                entry('Z', "\ud835\udc4d"),
                entry('`', "`"),
                entry('1', "1"),
                entry('2', "2"),
                entry('3', "3"),
                entry('4', "4"),
                entry('5', "5"),
                entry('6', "6"),
                entry('7', "7"),
                entry('8', "8"),
                entry('9', "9"),
                entry('0', "0"),
                entry('-', "-"),
                entry('=', "="),
                entry('[', "["),
                entry(']', "]"),
                entry('\\', "\\"),
                entry(';', ";"),
                entry('\'', "'"),
                entry(',', ","),
                entry('.', "."),
                entry('/', "/"),
                entry('~', "\u223c"),
                entry('!', "!"),
                entry('@', "@"),
                entry('#', "#"),
                entry('$', "$"),
                entry('%', "%"),
                entry('^', "^"),
                entry('&', "&"),
                entry('*', "*"),
                entry('(', "("),
                entry(')', ")"),
                entry('_', "_"),
                entry('+', "+"),
                entry('{', "{"),
                entry('}', "}"),
                entry('|', "|"),
                entry(':', ":"),
                entry('\"', "\""),
                entry('<', "<"),
                entry('>', ">"),
                entry('?', "?")
        );
    }

    private Map<KeyCodeCombination, String> initializeScriptAltMap() {
        return Map.ofEntries(
                entry(new KeyCodeCombination(KeyCode.A, KeyCombination.ALT_DOWN), "\ud835\udcb6"),

                entry(new KeyCodeCombination(KeyCode.B, KeyCombination.ALT_DOWN), "\ud835\udcb7"),
                entry(new KeyCodeCombination(KeyCode.C, KeyCombination.ALT_DOWN), "\ud835\udcb8"),
                entry(new KeyCodeCombination(KeyCode.D, KeyCombination.ALT_DOWN), "\ud835\udcb9"),
                entry(new KeyCodeCombination(KeyCode.E, KeyCombination.ALT_DOWN),  "\u212f"),
                entry(new KeyCodeCombination(KeyCode.F, KeyCombination.ALT_DOWN), "\ud835\udcbb"),
                entry(new KeyCodeCombination(KeyCode.G, KeyCombination.ALT_DOWN), "\u210a"),
                entry(new KeyCodeCombination(KeyCode.H, KeyCombination.ALT_DOWN), "\ud835\udcbd"),
                entry(new KeyCodeCombination(KeyCode.I, KeyCombination.ALT_DOWN), "\ud835\udcbe"),
                entry(new KeyCodeCombination(KeyCode.J, KeyCombination.ALT_DOWN), "\ud835\udcbf"),
                entry(new KeyCodeCombination(KeyCode.K, KeyCombination.ALT_DOWN), "\ud835\udcc0"),
                entry(new KeyCodeCombination(KeyCode.L, KeyCombination.ALT_DOWN), "\ud835\udcc1"),
                entry(new KeyCodeCombination(KeyCode.M, KeyCombination.ALT_DOWN),  "\ud835\udcc2"),
                entry(new KeyCodeCombination(KeyCode.N, KeyCombination.ALT_DOWN), "\ud835\udcc3"),
                entry(new KeyCodeCombination(KeyCode.O, KeyCombination.ALT_DOWN), "\u2134"),
                entry(new KeyCodeCombination(KeyCode.P, KeyCombination.ALT_DOWN), "\ud835\udcc5"),
                entry(new KeyCodeCombination(KeyCode.Q, KeyCombination.ALT_DOWN), "\ud835\udcc6"),
                entry(new KeyCodeCombination(KeyCode.R, KeyCombination.ALT_DOWN),  "\ud835\udcc7"),
                entry(new KeyCodeCombination(KeyCode.S, KeyCombination.ALT_DOWN), "\ud835\udcc8"),
                entry(new KeyCodeCombination(KeyCode.T, KeyCombination.ALT_DOWN),  "\ud835\udcc9"),
                entry(new KeyCodeCombination(KeyCode.U, KeyCombination.ALT_DOWN),  "\ud835\udcca"),
                entry(new KeyCodeCombination(KeyCode.V, KeyCombination.ALT_DOWN),  "\ud835\udccb"),
                entry(new KeyCodeCombination(KeyCode.W, KeyCombination.ALT_DOWN),  "\ud835\udccc"),
                entry(new KeyCodeCombination(KeyCode.X, KeyCombination.ALT_DOWN), "\ud835\udccd"),
                entry(new KeyCodeCombination(KeyCode.Y, KeyCombination.ALT_DOWN), "\ud835\udcce"),
                entry(new KeyCodeCombination(KeyCode.Z, KeyCombination.ALT_DOWN), "\ud835\udccf"),

                entry(new KeyCodeCombination(KeyCode.DIGIT1, KeyCombination.ALT_DOWN), ""),
                entry(new KeyCodeCombination(KeyCode.DIGIT2, KeyCombination.ALT_DOWN), ""),
                entry(new KeyCodeCombination(KeyCode.DIGIT3, KeyCombination.ALT_DOWN), ""),
                entry(new KeyCodeCombination(KeyCode.DIGIT4, KeyCombination.ALT_DOWN), ""),
                entry(new KeyCodeCombination(KeyCode.DIGIT5, KeyCombination.ALT_DOWN), ""),
                entry(new KeyCodeCombination(KeyCode.DIGIT6, KeyCombination.ALT_DOWN), ""),
                entry(new KeyCodeCombination(KeyCode.DIGIT7, KeyCombination.ALT_DOWN), ""),
                entry(new KeyCodeCombination(KeyCode.DIGIT8, KeyCombination.ALT_DOWN), ""),
                entry(new KeyCodeCombination(KeyCode.DIGIT9, KeyCombination.ALT_DOWN), ""),
                entry(new KeyCodeCombination(KeyCode.DIGIT0, KeyCombination.ALT_DOWN), ""),

                entry(new KeyCodeCombination(KeyCode.A, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN), "\ud835\udc9c"),
                entry(new KeyCodeCombination(KeyCode.B, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN), "\u212c"),
                entry(new KeyCodeCombination(KeyCode.C, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN), "\ud835\udc9e"),
                entry(new KeyCodeCombination(KeyCode.D, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN), "\ud835\udc9f"),
                entry(new KeyCodeCombination(KeyCode.E, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN),  "\u2130"),
                entry(new KeyCodeCombination(KeyCode.F, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN),  "\u2131"),
                entry(new KeyCodeCombination(KeyCode.G, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN), "\ud835\udca2"),
                entry(new KeyCodeCombination(KeyCode.H, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN), "\u210b"),
                entry(new KeyCodeCombination(KeyCode.I, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN), "\u2110"),
                entry(new KeyCodeCombination(KeyCode.J, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN), "\ud835\udca5"),
                entry(new KeyCodeCombination(KeyCode.K, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN), "\ud835\udca6"),
                entry(new KeyCodeCombination(KeyCode.L, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN), "\u2112"),
                entry(new KeyCodeCombination(KeyCode.M, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN), "\u2133"),
                entry(new KeyCodeCombination(KeyCode.N, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN), "\ud835\udca9"),
                entry(new KeyCodeCombination(KeyCode.O, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN),  "\ud835\udcaa"),
                entry(new KeyCodeCombination(KeyCode.P, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN), "\ud835\udcab"),
                entry(new KeyCodeCombination(KeyCode.Q, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN), "\ud835\udcac"),
                entry(new KeyCodeCombination(KeyCode.R, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN), "\u211b"),
                entry(new KeyCodeCombination(KeyCode.S, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN), "\ud835\udcae"),
                entry(new KeyCodeCombination(KeyCode.T, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN), "\ud835\udcaf"),
                entry(new KeyCodeCombination(KeyCode.U, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN), "\ud835\udcb0"),
                entry(new KeyCodeCombination(KeyCode.V, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN), "\ud835\udcb1"),
                entry(new KeyCodeCombination(KeyCode.W, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN), "\ud835\udcb2"),
                entry(new KeyCodeCombination(KeyCode.X, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN), "\ud835\udcb3"),
                entry(new KeyCodeCombination(KeyCode.Y, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN), "\ud835\udcb4"),
                entry(new KeyCodeCombination(KeyCode.Z, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN), "\ud835\udcb5")





                );
    }

    private Map<Character, String> initializeScriptMap() {
        return Map.ofEntries(
                entry('a', "\ud835\udcb6"),
                entry('b', "\ud835\udcb7"),
                entry('c', "\ud835\udcb8"),
                entry('d', "\ud835\udcb9"),
                entry('e', "\u212f"),
                entry('f', "\ud835\udcbb"),
                entry('g', "\u210a"),
                entry('h', "\ud835\udcbd"),
                entry('i', "\ud835\udcbe"),
                entry('j', "\ud835\udcbf"),
                entry('k', "\ud835\udcc0"),
                entry('l', "\ud835\udcc1"),
                entry('m', "\ud835\udcc2"),
                entry('n', "\ud835\udcc3"),
                entry('o', "\u2134"),
                entry('p', "\ud835\udcc5"),
                entry('q', "\ud835\udcc6"),
                entry('r', "\ud835\udcc7"),
                entry('s', "\ud835\udcc8"),
                entry('t', "\ud835\udcc9"),
                entry('u', "\ud835\udcca"),
                entry('v', "\ud835\udccb"),
                entry('w', "\ud835\udccc"),
                entry('x', "\ud835\udccd"),
                entry('y', "\ud835\udcce"),
                entry('z', "\ud835\udccf"),
                entry('A', "\ud835\udc9c"),
                entry('B', "\u212c"),
                entry('C', "\ud835\udc9e"),
                entry('D', "\ud835\udc9f"),
                entry('E', "\u2130"),
                entry('F', "\u2131"),
                entry('G', "\ud835\udca2"),
                entry('H', "\u210b"),
                entry('I', "\u2110"),
                entry('J', "\ud835\udca5"),
                entry('K', "\ud835\udca6"),
                entry('L', "\u2112"),
                entry('M', "\u2133"),
                entry('N', "\ud835\udca9"),
                entry('O', "\ud835\udcaa"),
                entry('P', "\ud835\udcab"),
                entry('Q', "\ud835\udcac"),
                entry('R', "\u211b"),
                entry('S', "\ud835\udcae"),
                entry('T', "\ud835\udcaf"),
                entry('U', "\ud835\udcb0"),
                entry('V', "\ud835\udcb1"),
                entry('W', "\ud835\udcb2"),
                entry('X', "\ud835\udcb3"),
                entry('Y', "\ud835\udcb4"),
                entry('Z', "\ud835\udcb5"),
                entry('`', "`"),
                entry('1', "1"),
                entry('2', "2"),
                entry('3', "3"),
                entry('4', "4"),
                entry('5', "5"),
                entry('6', "6"),
                entry('7', "7"),
                entry('8', "8"),
                entry('9', "9"),
                entry('0', "0"),
                entry('-', "-"),
                entry('=', "="),
                entry('[', "["),
                entry(']', "]"),
                entry('\\', "\\"),
                entry(';', ";"),
                entry('\'', "'"),
                entry(',', ","),
                entry('.', "."),
                entry('/', "/"),
                entry('~', "\u223c"),
                entry('!', "!"),
                entry('@', "@"),
                entry('#', "#"),
                entry('$', "$"),
                entry('%', "%"),
                entry('^', "^"),
                entry('&', "&"),
                entry('*', "*"),
                entry('(', "("),
                entry(')', ")"),
                entry('_', "_"),
                entry('+', "+"),
                entry('{', "{"),
                entry('}', "}"),
                entry('|', "|"),
                entry(':', ":"),
                entry('\"', "\""),
                entry('<', "<"),
                entry('>', ">"),
                entry('?', "?")
        );
    }



    private Map<Character, String> initializeGreekMap() {
        return Map.ofEntries(
                entry('a', "\u03b1"),
                entry('A', "\u0391"),
                entry('b', "\u03b2"),
                entry('B', "\u0392"),
                entry('c', "\u03c8"),
                entry('C', "\u03a8"),
                entry('d', "\u03b4"),
                entry('D', "\u0394"),
                entry('e', "\u03b5"),
                entry('E', "\u0395"),
                entry('f', "\u03c6"),
                entry('F', "\u03a6"),
                entry('g', "\u03b3"),
                entry('G', "\u0393"),
                entry('h', "\u03b7"),
                entry('H', "\u0397"),
                entry('i', "\u03b9"),
                entry('I', "\u0399"),
                entry('j', "\u03be"),
                entry('J', "\u039e"),
                entry('k', "\u03ba"),
                entry('K', "\u039a"),
                entry('l', "\u03bb"),
                entry('L', "\u039B"),
                entry('m', "\u03bc"),
                entry('M', "\u039c"),
                entry('n', "\u03bd"),
                entry('N', "\u039d"),
                entry('o', "\u03bf"),
                entry('O', "\u039f"),
                entry('p', "\u03c0"),
                entry('P', "\u03a0"),
                entry('q', "\uf8b4"),
                entry('Q', "\uf8d5"),
                entry('r', "\u03c1"),
                entry('R', "\u03a1"),
                entry('s', "\u03c3"),
                entry('S', "\u03a3"),
                entry('t', "\u03c4"),
                entry('T', "\u03a4"),
                entry('u', "\u03b8"),
                entry('U', "\u0398"),
                entry('v', "\u03c9"),
                entry('V', "\u03a9"),
                entry('w', "\uf8b5"),
                entry('W', ""),
                entry('x', "\u03c7"),
                entry('X', "\u03a7"),
                entry('y', "\u03c5"),
                entry('Y', "\u03a5"),
                entry('z', "\u03b6"),
                entry('Z', "\u0396"),
                entry('`', "`"),
                entry('1', "1"),
                entry('2', "2"),
                entry('3', "3"),
                entry('4', "4"),
                entry('5', "5"),
                entry('6', "6"),
                entry('7', "7"),
                entry('8', "8"),
                entry('9', "9"),
                entry('0', "0"),
                entry('-', "-"),
                entry('=', "="),
                entry('[', "["),
                entry(']', "]"),
                entry('\\', "\\"),
                entry(';', ";"),
                entry('\'', "'"),
                entry(',', ","),
                entry('.', "."),
                entry('/', "/"),
                entry('~', "\u223c"),
                entry('!', "!"),
                entry('@', "@"),
                entry('#', "#"),
                entry('$', "$"),
                entry('%', "%"),
                entry('^', "^"),
                entry('&', "&"),
                entry('*', "*"),
                entry('(', "("),
                entry(')', ")"),
                entry('_', "_"),
                entry('+', "+"),
                entry('{', "{"),
                entry('}', "}"),
                entry('|', "|"),
                entry(':', ":"),
                entry('\"', "\""),
                entry('<', "<"),
                entry('>', ">"),
                entry('?', "?")
        );
    }

    private Map<Character, String> initializeSansMap() {
        return Map.ofEntries(
                entry('a', "\ud835\uddba"),
                entry('b', "\ud835\uddbb"),
                entry('c', "\ud835\uddbc"),
                entry('d', "\ud835\uddbd"),
                entry('e', "\ud835\uddbe"),
                entry('f', "\ud835\uddbf"),
                entry('g', "\ud835\uddc0"),
                entry('h', "\ud835\uddc1"),
                entry('i', "\ud835\uddc2"),
                entry('j', "\ud835\uddc3"),
                entry('k', "\ud835\uddc4"),
                entry('l', "\ud835\uddc5"),
                entry('m', "\ud835\uddc6"),
                entry('n', "\ud835\uddc7"),
                entry('o', "\ud835\uddc8"),
                entry('p', "\ud835\uddc9"),
                entry('q', "\ud835\uddca"),
                entry('r', "\ud835\uddcb"),
                entry('s', "\ud835\uddcc"),
                entry('t', "\ud835\uddcd"),
                entry('u', "\ud835\uddce"),
                entry('v', "\ud835\uddcf"),
                entry('w', "\ud835\uddd0"),
                entry('x', "\ud835\uddd1"),
                entry('y', "\ud835\uddd2"),
                entry('z', "\ud835\uddd3"),
                entry('0', "\ud835\udfe2"),
                entry('1', "\ud835\udfe3"),
                entry('2', "\ud835\udfe4"),
                entry('3', "\ud835\udfe5"),
                entry('4', "\ud835\udfe6"),
                entry('5', "\ud835\udfe7"),
                entry('6', "\ud835\udfe8"),
                entry('7', "\ud835\udfe9"),
                entry('8', "\ud835\udfea"),
                entry('9', "\ud835\udfeb"),
                entry('A', "\ud835\udda0"),
                entry('B', "\ud835\udda1"),
                entry('C', "\ud835\udda2"),
                entry('D', "\ud835\udda3"),
                entry('E', "\ud835\udda4"),
                entry('F', "\ud835\udda5"),
                entry('G', "\ud835\udda6"),
                entry('H', "\ud835\udda7"),
                entry('I', "\ud835\udda8"),
                entry('J', "\ud835\udda9"),
                entry('K', "\ud835\uddaa"),
                entry('L', "\ud835\uddab"),
                entry('M', "\ud835\uddac"),
                entry('N', "\ud835\uddad"),
                entry('O', "\ud835\uddae"),
                entry('P', "\ud835\uddaf"),
                entry('Q', "\ud835\uddb0"),
                entry('R', "\ud835\uddb1"),
                entry('S', "\ud835\uddb2"),
                entry('T', "\ud835\uddb3"),
                entry('U', "\ud835\uddb4"),
                entry('V', "\ud835\uddb5"),
                entry('W', "\ud835\uddb6"),
                entry('X', "\ud835\uddb7"),
                entry('Y', "\ud835\uddb8"),
                entry('Z', "\ud835\uddb9"),
                entry('`', "`"),
                entry('-', "-"),
                entry('=', "="),
                entry('[', "["),
                entry(']', "]"),
                entry('\\', "\\"),
                entry(';', ";"),
                entry('\'', "'"),
                entry(',', ","),
                entry('.', "."),
                entry('/', "/"),
                entry('~', "\u223c"),
                entry('!', "!"),
                entry('@', "@"),
                entry('#', "#"),
                entry('$', "$"),
                entry('%', "%"),
                entry('^', "^"),
                entry('&', "&"),
                entry('*', "*"),
                entry('(', "("),
                entry(')', ")"),
                entry('_', "_"),
                entry('+', "+"),
                entry('{', "{"),
                entry('}', "}"),
                entry('|', "|"),
                entry(':', ":"),
                entry('\"', "\""),
                entry('<', "<"),
                entry('>', ">"),
                entry('?', "?")
        );

    }

    private Map<KeyCodeCombination, String> initializeSansItalicAltMap() {
        return Map.ofEntries(
                entry(new KeyCodeCombination(KeyCode.A, KeyCombination.ALT_DOWN), "\ud835\ude22"),
                entry(new KeyCodeCombination(KeyCode.B, KeyCombination.ALT_DOWN), "\ud835\ude23"),
                entry(new KeyCodeCombination(KeyCode.C, KeyCombination.ALT_DOWN), "\ud835\ude24"),
                entry(new KeyCodeCombination(KeyCode.D, KeyCombination.ALT_DOWN), "\ud835\ude25"),
                entry(new KeyCodeCombination(KeyCode.E, KeyCombination.ALT_DOWN), "\ud835\ude26"),
                entry(new KeyCodeCombination(KeyCode.F, KeyCombination.ALT_DOWN), "\ud835\ude27"),
                entry(new KeyCodeCombination(KeyCode.G, KeyCombination.ALT_DOWN), "\ud835\ude28"),
                entry(new KeyCodeCombination(KeyCode.H, KeyCombination.ALT_DOWN), "\ud835\ude29"),
                entry(new KeyCodeCombination(KeyCode.I, KeyCombination.ALT_DOWN), "\ud835\ude2a"),
                entry(new KeyCodeCombination(KeyCode.J, KeyCombination.ALT_DOWN), "\ud835\ude2b"),
                entry(new KeyCodeCombination(KeyCode.K, KeyCombination.ALT_DOWN), "\ud835\ude2c"),
                entry(new KeyCodeCombination(KeyCode.L, KeyCombination.ALT_DOWN), "\ud835\ude2d"),
                entry(new KeyCodeCombination(KeyCode.M, KeyCombination.ALT_DOWN), "\ud835\ude2e"),
                entry(new KeyCodeCombination(KeyCode.N, KeyCombination.ALT_DOWN), "\ud835\ude2f"),
                entry(new KeyCodeCombination(KeyCode.O, KeyCombination.ALT_DOWN), "\ud835\ude30"),
                entry(new KeyCodeCombination(KeyCode.P, KeyCombination.ALT_DOWN), "\ud835\ude31"),
                entry(new KeyCodeCombination(KeyCode.Q, KeyCombination.ALT_DOWN), "\ud835\ude32"),
                entry(new KeyCodeCombination(KeyCode.R, KeyCombination.ALT_DOWN), "\ud835\ude33"),
                entry(new KeyCodeCombination(KeyCode.S, KeyCombination.ALT_DOWN), "\ud835\ude34"),
                entry(new KeyCodeCombination(KeyCode.T, KeyCombination.ALT_DOWN), "\ud835\ude35"),
                entry(new KeyCodeCombination(KeyCode.U, KeyCombination.ALT_DOWN), "\ud835\ude36"),
                entry(new KeyCodeCombination(KeyCode.V, KeyCombination.ALT_DOWN), "\ud835\ude37"),
                entry(new KeyCodeCombination(KeyCode.W, KeyCombination.ALT_DOWN), "\ud835\ude38"),
                entry(new KeyCodeCombination(KeyCode.X, KeyCombination.ALT_DOWN), "\ud835\ude39"),
                entry(new KeyCodeCombination(KeyCode.Y, KeyCombination.ALT_DOWN), "\ud835\ude3a"),
                entry(new KeyCodeCombination(KeyCode.Z, KeyCombination.ALT_DOWN), "\ud835\ude3b"),
                entry(new KeyCodeCombination(KeyCode.DIGIT0, KeyCombination.ALT_DOWN), "\ud835\udfe2"),
                entry(new KeyCodeCombination(KeyCode.DIGIT1, KeyCombination.ALT_DOWN), "\ud835\udfe3"),
                entry(new KeyCodeCombination(KeyCode.DIGIT2, KeyCombination.ALT_DOWN), "\ud835\udfe4"),
                entry(new KeyCodeCombination(KeyCode.DIGIT3, KeyCombination.ALT_DOWN), "\ud835\udfe5"),
                entry(new KeyCodeCombination(KeyCode.DIGIT4, KeyCombination.ALT_DOWN), "\ud835\udfe6"),
                entry(new KeyCodeCombination(KeyCode.DIGIT5, KeyCombination.ALT_DOWN), "\ud835\udfe7"),
                entry(new KeyCodeCombination(KeyCode.DIGIT6, KeyCombination.ALT_DOWN), "\ud835\udfe8"),
                entry(new KeyCodeCombination(KeyCode.DIGIT7, KeyCombination.ALT_DOWN), "\ud835\udfe9"),
                entry(new KeyCodeCombination(KeyCode.DIGIT8, KeyCombination.ALT_DOWN), "\ud835\udfea"),
                entry(new KeyCodeCombination(KeyCode.DIGIT9, KeyCombination.ALT_DOWN), "\ud835\udfeb"),
                entry(new KeyCodeCombination(KeyCode.A, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN), "\ud835\ude08"),
                entry(new KeyCodeCombination(KeyCode.B, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN), "\ud835\ude09"),
                entry(new KeyCodeCombination(KeyCode.C, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN), "\ud835\ude0a"),
                entry(new KeyCodeCombination(KeyCode.D, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN), "\ud835\ude0b"),
                entry(new KeyCodeCombination(KeyCode.E, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN), "\ud835\ude0c"),
                entry(new KeyCodeCombination(KeyCode.F, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN), "\ud835\ude0d"),
                entry(new KeyCodeCombination(KeyCode.G, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN), "\ud835\ude0e"),
                entry(new KeyCodeCombination(KeyCode.H, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN), "\ud835\ude0f"),
                entry(new KeyCodeCombination(KeyCode.I, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN), "\ud835\ude10"),
                entry(new KeyCodeCombination(KeyCode.J, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN), "\ud835\ude11"),
                entry(new KeyCodeCombination(KeyCode.K, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN), "\ud835\ude12"),
                entry(new KeyCodeCombination(KeyCode.L, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN), "\ud835\ude13"),
                entry(new KeyCodeCombination(KeyCode.M, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN), "\ud835\ude14"),
                entry(new KeyCodeCombination(KeyCode.N, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN), "\ud835\ude15"),
                entry(new KeyCodeCombination(KeyCode.O, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN), "\ud835\ude16"),
                entry(new KeyCodeCombination(KeyCode.P, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN), "\ud835\ude17"),
                entry(new KeyCodeCombination(KeyCode.Q, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN), "\ud835\ude18"),
                entry(new KeyCodeCombination(KeyCode.R, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN), "\ud835\ude19"),
                entry(new KeyCodeCombination(KeyCode.S, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN), "\ud835\ude1a"),
                entry(new KeyCodeCombination(KeyCode.T, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN), "\ud835\ude1b"),
                entry(new KeyCodeCombination(KeyCode.U, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN), "\ud835\ude1c"),
                entry(new KeyCodeCombination(KeyCode.V, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN), "\ud835\ude1d"),
                entry(new KeyCodeCombination(KeyCode.W, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN), "\ud835\ude1e"),
                entry(new KeyCodeCombination(KeyCode.X, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN), "\ud835\ude1f"),
                entry(new KeyCodeCombination(KeyCode.Y, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN), "\ud835\ude20"),
                entry(new KeyCodeCombination(KeyCode.Z, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN), "\ud835\ude21")
        );

    }


    private Map<KeyCodeCombination, String> initializeSansAltMap() {
        return Map.ofEntries(
                entry(new KeyCodeCombination(KeyCode.A, KeyCombination.ALT_DOWN), "\ud835\uddba"),
                entry(new KeyCodeCombination(KeyCode.B, KeyCombination.ALT_DOWN), "\ud835\uddbb"),
                entry(new KeyCodeCombination(KeyCode.C, KeyCombination.ALT_DOWN), "\ud835\uddbc"),
                entry(new KeyCodeCombination(KeyCode.D, KeyCombination.ALT_DOWN), "\ud835\uddbd"),
                entry(new KeyCodeCombination(KeyCode.E, KeyCombination.ALT_DOWN), "\ud835\uddbe"),
                entry(new KeyCodeCombination(KeyCode.F, KeyCombination.ALT_DOWN), "\ud835\uddbf"),
                entry(new KeyCodeCombination(KeyCode.G, KeyCombination.ALT_DOWN), "\ud835\uddc0"),
                entry(new KeyCodeCombination(KeyCode.H, KeyCombination.ALT_DOWN), "\ud835\uddc1"),
                entry(new KeyCodeCombination(KeyCode.I, KeyCombination.ALT_DOWN), "\ud835\uddc2"),
                entry(new KeyCodeCombination(KeyCode.J, KeyCombination.ALT_DOWN), "\ud835\uddc3"),
                entry(new KeyCodeCombination(KeyCode.K, KeyCombination.ALT_DOWN), "\ud835\uddc4"),
                entry(new KeyCodeCombination(KeyCode.L, KeyCombination.ALT_DOWN), "\ud835\uddc5"),
                entry(new KeyCodeCombination(KeyCode.M, KeyCombination.ALT_DOWN), "\ud835\uddc6"),
                entry(new KeyCodeCombination(KeyCode.N, KeyCombination.ALT_DOWN), "\ud835\uddc7"),
                entry(new KeyCodeCombination(KeyCode.O, KeyCombination.ALT_DOWN), "\ud835\uddc8"),
                entry(new KeyCodeCombination(KeyCode.P, KeyCombination.ALT_DOWN), "\ud835\uddc9"),
                entry(new KeyCodeCombination(KeyCode.Q, KeyCombination.ALT_DOWN), "\ud835\uddca"),
                entry(new KeyCodeCombination(KeyCode.R, KeyCombination.ALT_DOWN), "\ud835\uddcb"),
                entry(new KeyCodeCombination(KeyCode.S, KeyCombination.ALT_DOWN), "\ud835\uddcc"),
                entry(new KeyCodeCombination(KeyCode.T, KeyCombination.ALT_DOWN), "\ud835\uddcd"),
                entry(new KeyCodeCombination(KeyCode.U, KeyCombination.ALT_DOWN), "\ud835\uddce"),
                entry(new KeyCodeCombination(KeyCode.V, KeyCombination.ALT_DOWN), "\ud835\uddcf"),
                entry(new KeyCodeCombination(KeyCode.W, KeyCombination.ALT_DOWN), "\ud835\uddd0"),
                entry(new KeyCodeCombination(KeyCode.X, KeyCombination.ALT_DOWN), "\ud835\uddd1"),
                entry(new KeyCodeCombination(KeyCode.Y, KeyCombination.ALT_DOWN), "\ud835\uddd2"),
                entry(new KeyCodeCombination(KeyCode.Z, KeyCombination.ALT_DOWN), "\ud835\uddd3"),
                entry(new KeyCodeCombination(KeyCode.DIGIT0, KeyCombination.ALT_DOWN), "\ud835\udfe2"),
                entry(new KeyCodeCombination(KeyCode.DIGIT1, KeyCombination.ALT_DOWN), "\ud835\udfe3"),
                entry(new KeyCodeCombination(KeyCode.DIGIT2, KeyCombination.ALT_DOWN), "\ud835\udfe4"),
                entry(new KeyCodeCombination(KeyCode.DIGIT3, KeyCombination.ALT_DOWN), "\ud835\udfe5"),
                entry(new KeyCodeCombination(KeyCode.DIGIT4, KeyCombination.ALT_DOWN), "\ud835\udfe6"),
                entry(new KeyCodeCombination(KeyCode.DIGIT5, KeyCombination.ALT_DOWN), "\ud835\udfe7"),
                entry(new KeyCodeCombination(KeyCode.DIGIT6, KeyCombination.ALT_DOWN), "\ud835\udfe8"),
                entry(new KeyCodeCombination(KeyCode.DIGIT7, KeyCombination.ALT_DOWN), "\ud835\udfe9"),
                entry(new KeyCodeCombination(KeyCode.DIGIT8, KeyCombination.ALT_DOWN), "\ud835\udfea"),
                entry(new KeyCodeCombination(KeyCode.DIGIT9, KeyCombination.ALT_DOWN), "\ud835\udfeb"),
                entry(new KeyCodeCombination(KeyCode.A, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN), "\ud835\udda0"),
                entry(new KeyCodeCombination(KeyCode.B, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN), "\ud835\udda1"),
                entry(new KeyCodeCombination(KeyCode.C, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN), "\ud835\udda2"),
                entry(new KeyCodeCombination(KeyCode.D, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN), "\ud835\udda3"),
                entry(new KeyCodeCombination(KeyCode.E, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN), "\ud835\udda4"),
                entry(new KeyCodeCombination(KeyCode.F, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN), "\ud835\udda5"),
                entry(new KeyCodeCombination(KeyCode.G, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN), "\ud835\udda6"),
                entry(new KeyCodeCombination(KeyCode.H, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN), "\ud835\udda7"),
                entry(new KeyCodeCombination(KeyCode.I, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN), "\ud835\udda8"),
                entry(new KeyCodeCombination(KeyCode.J, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN), "\ud835\udda9"),
                entry(new KeyCodeCombination(KeyCode.K, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN), "\ud835\uddaa"),
                entry(new KeyCodeCombination(KeyCode.L, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN), "\ud835\uddab"),
                entry(new KeyCodeCombination(KeyCode.M, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN), "\ud835\uddac"),
                entry(new KeyCodeCombination(KeyCode.N, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN), "\ud835\uddad"),
                entry(new KeyCodeCombination(KeyCode.O, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN), "\ud835\uddae"),
                entry(new KeyCodeCombination(KeyCode.P, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN), "\ud835\uddaf"),
                entry(new KeyCodeCombination(KeyCode.Q, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN), "\ud835\uddb0"),
                entry(new KeyCodeCombination(KeyCode.R, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN), "\ud835\uddb1"),
                entry(new KeyCodeCombination(KeyCode.S, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN), "\ud835\uddb2"),
                entry(new KeyCodeCombination(KeyCode.T, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN), "\ud835\uddb3"),
                entry(new KeyCodeCombination(KeyCode.U, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN), "\ud835\uddb4"),
                entry(new KeyCodeCombination(KeyCode.V, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN), "\ud835\uddb5"),
                entry(new KeyCodeCombination(KeyCode.W, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN), "\ud835\uddb6"),
                entry(new KeyCodeCombination(KeyCode.X, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN), "\ud835\uddb7"),
                entry(new KeyCodeCombination(KeyCode.Y, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN), "\ud835\uddb8"),
                entry(new KeyCodeCombination(KeyCode.Z, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN), "\ud835\uddb9")
        );

    }

    private Map<KeyCodeCombination, String> initializeFrakturMap() {
        return Map.ofEntries(
                entry(new KeyCodeCombination(KeyCode.A, KeyCombination.ALT_DOWN), "\ud835\udd1e"),
                entry(new KeyCodeCombination(KeyCode.B, KeyCombination.ALT_DOWN), "\ud835\udd1f"),
                entry(new KeyCodeCombination(KeyCode.C, KeyCombination.ALT_DOWN), "\ud835\udd20"),
                entry(new KeyCodeCombination(KeyCode.D, KeyCombination.ALT_DOWN), "\ud835\udd21"),
                entry(new KeyCodeCombination(KeyCode.E, KeyCombination.ALT_DOWN), "\ud835\udd22"),
                entry(new KeyCodeCombination(KeyCode.F, KeyCombination.ALT_DOWN), "\ud835\udd23"),
                entry(new KeyCodeCombination(KeyCode.G, KeyCombination.ALT_DOWN), "\ud835\udd24"),
                entry(new KeyCodeCombination(KeyCode.H, KeyCombination.ALT_DOWN), "\ud835\udd25"),
                entry(new KeyCodeCombination(KeyCode.I, KeyCombination.ALT_DOWN), "\ud835\udd26"),
                entry(new KeyCodeCombination(KeyCode.J, KeyCombination.ALT_DOWN), "\ud835\udd27"),
                entry(new KeyCodeCombination(KeyCode.K, KeyCombination.ALT_DOWN), "\ud835\udd28"),
                entry(new KeyCodeCombination(KeyCode.L, KeyCombination.ALT_DOWN), "\ud835\udd29"),
                entry(new KeyCodeCombination(KeyCode.M, KeyCombination.ALT_DOWN), "\ud835\udd2a"),
                entry(new KeyCodeCombination(KeyCode.N, KeyCombination.ALT_DOWN), "\ud835\udd2b"),
                entry(new KeyCodeCombination(KeyCode.O, KeyCombination.ALT_DOWN), "\ud835\udd2c"),
                entry(new KeyCodeCombination(KeyCode.P, KeyCombination.ALT_DOWN), "\ud835\udd2d"),
                entry(new KeyCodeCombination(KeyCode.Q, KeyCombination.ALT_DOWN), "\ud835\udd2e"),
                entry(new KeyCodeCombination(KeyCode.R, KeyCombination.ALT_DOWN), "\ud835\udd2f"),
                entry(new KeyCodeCombination(KeyCode.S, KeyCombination.ALT_DOWN), "\ud835\udd30"),
                entry(new KeyCodeCombination(KeyCode.T, KeyCombination.ALT_DOWN), "\ud835\udd31"),
                entry(new KeyCodeCombination(KeyCode.U, KeyCombination.ALT_DOWN), "\ud835\udd32"),
                entry(new KeyCodeCombination(KeyCode.V, KeyCombination.ALT_DOWN), "\ud835\udd33"),
                entry(new KeyCodeCombination(KeyCode.W, KeyCombination.ALT_DOWN), "\ud835\udd34"),
                entry(new KeyCodeCombination(KeyCode.X, KeyCombination.ALT_DOWN), "\ud835\udd35"),
                entry(new KeyCodeCombination(KeyCode.Y, KeyCombination.ALT_DOWN), "\ud835\udd36"),
                entry(new KeyCodeCombination(KeyCode.Z, KeyCombination.ALT_DOWN), "\ud835\udd37"),
                entry(new KeyCodeCombination(KeyCode.DIGIT1, KeyCombination.ALT_DOWN), ""),
                entry(new KeyCodeCombination(KeyCode.DIGIT2, KeyCombination.ALT_DOWN), ""),
                entry(new KeyCodeCombination(KeyCode.DIGIT3, KeyCombination.ALT_DOWN), ""),
                entry(new KeyCodeCombination(KeyCode.DIGIT4, KeyCombination.ALT_DOWN), ""),
                entry(new KeyCodeCombination(KeyCode.DIGIT5, KeyCombination.ALT_DOWN), ""),
                entry(new KeyCodeCombination(KeyCode.DIGIT6, KeyCombination.ALT_DOWN), ""),
                entry(new KeyCodeCombination(KeyCode.DIGIT7, KeyCombination.ALT_DOWN), ""),
                entry(new KeyCodeCombination(KeyCode.DIGIT8, KeyCombination.ALT_DOWN), ""),
                entry(new KeyCodeCombination(KeyCode.DIGIT9, KeyCombination.ALT_DOWN), ""),
                entry(new KeyCodeCombination(KeyCode.DIGIT0, KeyCombination.ALT_DOWN), ""),

                entry(new KeyCodeCombination(KeyCode.A, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN), "\ud835\udd04"),
                entry(new KeyCodeCombination(KeyCode.B, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN), "\ud835\udd05"),
                entry(new KeyCodeCombination(KeyCode.C, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN), "\u212D"),
                entry(new KeyCodeCombination(KeyCode.D, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN), "\ud835\udd07"),
                entry(new KeyCodeCombination(KeyCode.E, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN), "\ud835\udd08"),
                entry(new KeyCodeCombination(KeyCode.F, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN), "\ud835\udd09"),
                entry(new KeyCodeCombination(KeyCode.G, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN), "\ud835\udd0a"),
                entry(new KeyCodeCombination(KeyCode.H, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN), "\u210c"),
                entry(new KeyCodeCombination(KeyCode.I, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN), "\u2111"),
                entry(new KeyCodeCombination(KeyCode.J, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN), "\ud835\udd0d"),
                entry(new KeyCodeCombination(KeyCode.K, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN), "\ud835\udd0e"),
                entry(new KeyCodeCombination(KeyCode.L, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN), "\ud835\udd0f"),
                entry(new KeyCodeCombination(KeyCode.M, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN), "\ud835\udd10"),
                entry(new KeyCodeCombination(KeyCode.N, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN), "\ud835\udd11"),
                entry(new KeyCodeCombination(KeyCode.O, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN), "\ud835\udd12"),
                entry(new KeyCodeCombination(KeyCode.P, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN), "\ud835\udd13"),
                entry(new KeyCodeCombination(KeyCode.Q, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN), "\ud835\udd14"),
                entry(new KeyCodeCombination(KeyCode.R, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN), "\u211c"),
                entry(new KeyCodeCombination(KeyCode.S, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN), "\ud835\udd16"),
                entry(new KeyCodeCombination(KeyCode.T, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN), "\ud835\udd17"),
                entry(new KeyCodeCombination(KeyCode.U, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN), "\ud835\udd18"),
                entry(new KeyCodeCombination(KeyCode.V, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN), "\ud835\udd19"),
                entry(new KeyCodeCombination(KeyCode.W, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN), "\ud835\udd1a"),
                entry(new KeyCodeCombination(KeyCode.X, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN), "\ud835\udd1b"),
                entry(new KeyCodeCombination(KeyCode.Y, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN), "\ud835\udd1c"),
                entry(new KeyCodeCombination(KeyCode.Z, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN), "\u2128")
        );
    }

    private Map<KeyCodeCombination, String> initializeBlackboardMap() {
        return Map.ofEntries(
                entry(new KeyCodeCombination(KeyCode.A, KeyCombination.ALT_DOWN), "\uf893"),
                entry(new KeyCodeCombination(KeyCode.B, KeyCombination.ALT_DOWN), "\uf894"),
                entry(new KeyCodeCombination(KeyCode.C, KeyCombination.ALT_DOWN), "\uf895"),
                entry(new KeyCodeCombination(KeyCode.D, KeyCombination.ALT_DOWN), "\uf896"),
                entry(new KeyCodeCombination(KeyCode.E, KeyCombination.ALT_DOWN), "\uf897"),
                entry(new KeyCodeCombination(KeyCode.F, KeyCombination.ALT_DOWN), "\uf898"),
                entry(new KeyCodeCombination(KeyCode.G, KeyCombination.ALT_DOWN), "\uf899"),
                entry(new KeyCodeCombination(KeyCode.H, KeyCombination.ALT_DOWN), "\uf89a"),
                entry(new KeyCodeCombination(KeyCode.I, KeyCombination.ALT_DOWN), "\uf89b"),
                entry(new KeyCodeCombination(KeyCode.J, KeyCombination.ALT_DOWN), "\uf89c"),
                entry(new KeyCodeCombination(KeyCode.K, KeyCombination.ALT_DOWN), "\uf89d"),
                entry(new KeyCodeCombination(KeyCode.L, KeyCombination.ALT_DOWN), "\uf89e"),
                entry(new KeyCodeCombination(KeyCode.M, KeyCombination.ALT_DOWN), "\uf89f"),
                entry(new KeyCodeCombination(KeyCode.N, KeyCombination.ALT_DOWN), "\uf8a0"),
                entry(new KeyCodeCombination(KeyCode.O, KeyCombination.ALT_DOWN), "\uf8a1"),
                entry(new KeyCodeCombination(KeyCode.P, KeyCombination.ALT_DOWN), "\uf8a2"),
                entry(new KeyCodeCombination(KeyCode.Q, KeyCombination.ALT_DOWN), "\uf8a3"),
                entry(new KeyCodeCombination(KeyCode.R, KeyCombination.ALT_DOWN), "\uf8a4"),
                entry(new KeyCodeCombination(KeyCode.S, KeyCombination.ALT_DOWN), "\uf8a5"),
                entry(new KeyCodeCombination(KeyCode.T, KeyCombination.ALT_DOWN), "\uf8a6"),
                entry(new KeyCodeCombination(KeyCode.U, KeyCombination.ALT_DOWN), "\uf8a7"),
                entry(new KeyCodeCombination(KeyCode.V, KeyCombination.ALT_DOWN), "\uf8a8"),
                entry(new KeyCodeCombination(KeyCode.W, KeyCombination.ALT_DOWN), "\uf8a9"),
                entry(new KeyCodeCombination(KeyCode.X, KeyCombination.ALT_DOWN), "\uf8aa"),
                entry(new KeyCodeCombination(KeyCode.Y, KeyCombination.ALT_DOWN), "\uf8ab"),
                entry(new KeyCodeCombination(KeyCode.Z, KeyCombination.ALT_DOWN), "\uf8ac"),
                entry(new KeyCodeCombination(KeyCode.DIGIT0, KeyCombination.ALT_DOWN), "\ud835\udfd8"),
                entry(new KeyCodeCombination(KeyCode.DIGIT1, KeyCombination.ALT_DOWN), "\ud835\udfd9"),
                entry(new KeyCodeCombination(KeyCode.DIGIT2, KeyCombination.ALT_DOWN), "\ud835\udfda"),
                entry(new KeyCodeCombination(KeyCode.DIGIT3, KeyCombination.ALT_DOWN), "\ud835\udfdb"),
                entry(new KeyCodeCombination(KeyCode.DIGIT4, KeyCombination.ALT_DOWN), "\ud835\udfdc"),
                entry(new KeyCodeCombination(KeyCode.DIGIT5, KeyCombination.ALT_DOWN), "\ud835\udfdd"),
                entry(new KeyCodeCombination(KeyCode.DIGIT6, KeyCombination.ALT_DOWN), "\ud835\udfde"),
                entry(new KeyCodeCombination(KeyCode.DIGIT7, KeyCombination.ALT_DOWN), "\ud835\udfdf"),
                entry(new KeyCodeCombination(KeyCode.DIGIT8, KeyCombination.ALT_DOWN), "\ud835\udfe0"),
                entry(new KeyCodeCombination(KeyCode.DIGIT9, KeyCombination.ALT_DOWN), "\ud835\udfe1"),
                entry(new KeyCodeCombination(KeyCode.A, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN), "\uf880"),
                entry(new KeyCodeCombination(KeyCode.B, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN), "\uf881"),
                entry(new KeyCodeCombination(KeyCode.C, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN), "\uf8ad"),
                entry(new KeyCodeCombination(KeyCode.D, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN), "\uf882"),
                entry(new KeyCodeCombination(KeyCode.E, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN), "\uf883"),
                entry(new KeyCodeCombination(KeyCode.F, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN), "\uf884"),
                entry(new KeyCodeCombination(KeyCode.G, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN), "\uf885"),
                entry(new KeyCodeCombination(KeyCode.H, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN), "\uf8ae"),
                entry(new KeyCodeCombination(KeyCode.I, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN), "\uf886"),
                entry(new KeyCodeCombination(KeyCode.J, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN), "\uf887"),
                entry(new KeyCodeCombination(KeyCode.K, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN), "\uf888"),
                entry(new KeyCodeCombination(KeyCode.L, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN), "\uf889"),
                entry(new KeyCodeCombination(KeyCode.M, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN), "\uf88a"),
                entry(new KeyCodeCombination(KeyCode.N, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN), "\uf8af"),
                entry(new KeyCodeCombination(KeyCode.O, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN), "\uf88b"),
                entry(new KeyCodeCombination(KeyCode.P, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN), "\uf8b0"),
                entry(new KeyCodeCombination(KeyCode.Q, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN), "\uf8b1"),
                entry(new KeyCodeCombination(KeyCode.R, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN), "\uf8b2"),
                entry(new KeyCodeCombination(KeyCode.S, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN), "\uf88c"),
                entry(new KeyCodeCombination(KeyCode.T, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN), "\uf88d"),
                entry(new KeyCodeCombination(KeyCode.U, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN), "\uf88e"),
                entry(new KeyCodeCombination(KeyCode.V, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN), "\uf88f"),
                entry(new KeyCodeCombination(KeyCode.W, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN), "\uf890"),
                entry(new KeyCodeCombination(KeyCode.X, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN), "\uf891"),
                entry(new KeyCodeCombination(KeyCode.Y, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN), "\uf892"),
                entry(new KeyCodeCombination(KeyCode.Z, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN), "\uf8b3")
        );
    }

    private Map<KeyCodeCombination, String> initializeSpecialCharacterMap() {
        return Map.ofEntries(
                entry(new KeyCodeCombination(KeyCode.BACK_QUOTE, KeyCombination.ALT_DOWN), "\u00ac"),                                                             //hammer
                entry(new KeyCodeCombination(KeyCode.MINUS, KeyCombination.ALT_DOWN), "\u25b3"),                                                                  //meta caret
                entry(new KeyCodeCombination(KeyCode.EQUALS, KeyCombination.ALT_DOWN), "\u25bd"),                                                                 //meta vee
                entry(new KeyCodeCombination(KeyCode.OPEN_BRACKET, KeyCombination.ALT_DOWN), "\u21d2"),                                                           //meta arrow
                entry(new KeyCodeCombination(KeyCode.CLOSE_BRACKET, KeyCombination.ALT_DOWN), "\u21d4"),                                                          //meta double arrow
                entry(new KeyCodeCombination(KeyCode.BACK_SLASH, KeyCombination.ALT_DOWN), "\u234a"),                                                             //meta bottom
                entry(new KeyCodeCombination(KeyCode.SEMICOLON, KeyCombination.ALT_DOWN), "\u22a2"),                                                              //proves
                entry(new KeyCodeCombination(KeyCode.QUOTE, KeyCombination.ALT_DOWN), "\u22ac"),                                                                  //not proves
                entry(new KeyCodeCombination(KeyCode.COMMA, KeyCombination.ALT_DOWN), "\u22a8"),                                                                  //entails
                entry(new KeyCodeCombination(KeyCode.PERIOD, KeyCombination.ALT_DOWN), "\u22ad"),                                                                 //not entails
                entry(new KeyCodeCombination(KeyCode.SLASH, KeyCombination.ALT_DOWN), "\ue8bc"),                                                                  //prime


                entry(new KeyCodeCombination(KeyCode.BACK_QUOTE, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN), "\u223c"),                                  //tilde
                entry(new KeyCodeCombination(KeyCode.DIGIT1, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN), "\u2227"),                                      //caret
                entry(new KeyCodeCombination(KeyCode.DIGIT2, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN), "\u2228"),                                      //wedge
                entry(new KeyCodeCombination(KeyCode.DIGIT3, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN), "\u2192"),                                      //arrow
                entry(new KeyCodeCombination(KeyCode.DIGIT4, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN), "\u2194"),                                      //double arrow
                entry(new KeyCodeCombination(KeyCode.DIGIT5, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN), "\u22a5"),                                      //bottom
                entry(new KeyCodeCombination(KeyCode.DIGIT6, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN), "\u2200"),                                      //universal quantifier
                entry(new KeyCodeCombination(KeyCode.DIGIT7, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN), "\u2203"),                                      //existential quantifier
                entry(new KeyCodeCombination(KeyCode.DIGIT8, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN), "\ue8a5"),                                      //big not less than
                entry(new KeyCodeCombination(KeyCode.DIGIT9, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN), "\ue8a7"),                                      //big not less than or equal
                entry(new KeyCodeCombination(KeyCode.DIGIT0, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN), "\ue886"),                                      //zero with slash
                entry(new KeyCodeCombination(KeyCode.MINUS, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN), "\ue8ad"),                                       //big not equals
                entry(new KeyCodeCombination(KeyCode.EQUALS, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN), "\ue8ac"),                                      //big equals
                entry(new KeyCodeCombination(KeyCode.OPEN_BRACKET, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN), "\u00d7"),                                //times
                entry(new KeyCodeCombination(KeyCode.CLOSE_BRACKET, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN), "\ue8b8"),                               //big plus
                entry(new KeyCodeCombination(KeyCode.BACK_SLASH, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN), "\ue8ba"),                                  //big times
                entry(new KeyCodeCombination(KeyCode.SEMICOLON, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN), "\u2264"),                                   //less than or equal
                entry(new KeyCodeCombination(KeyCode.QUOTE, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN), "\u2265"),                                       //greater than or equal
                entry(new KeyCodeCombination(KeyCode.COMMA, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN), "\ue8a4"),                                       //big less than
                entry(new KeyCodeCombination(KeyCode.PERIOD, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN), "\ue8a6"),                                      //big less than or equal
                entry(new KeyCodeCombination(KeyCode.SLASH, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN), "\u2260"),                                       //not equal

                entry(new KeyCodeCombination(KeyCode.BACK_QUOTE, KeyCombination.SHORTCUT_DOWN, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN), "\u25fb"),    //box
                entry(new KeyCodeCombination(KeyCode.DIGIT1, KeyCombination.SHORTCUT_DOWN, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN), "\u25ca"),        //diamond
                entry(new KeyCodeCombination(KeyCode.DIGIT2, KeyCombination.SHORTCUT_DOWN, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN), "\u2261"),        //triple bar
                entry(new KeyCodeCombination(KeyCode.DIGIT3, KeyCombination.SHORTCUT_DOWN, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN), "\u2283"),        //horseshoe
                entry(new KeyCodeCombination(KeyCode.DIGIT4, KeyCombination.SHORTCUT_DOWN, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN), "\u22c0"),        //big caret
                entry(new KeyCodeCombination(KeyCode.DIGIT5, KeyCombination.SHORTCUT_DOWN, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN), "\u22c1"),        //big wedge
                entry(new KeyCodeCombination(KeyCode.DIGIT6, KeyCombination.SHORTCUT_DOWN, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN), "\u2191"),        //up arrow
                entry(new KeyCodeCombination(KeyCode.DIGIT7, KeyCombination.SHORTCUT_DOWN, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN), "\u2193"),        //down arrow
                entry(new KeyCodeCombination(KeyCode.DIGIT8, KeyCombination.SHORTCUT_DOWN, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN), "\u2217"),        //asterisk
                entry(new KeyCodeCombination(KeyCode.DIGIT9, KeyCombination.SHORTCUT_DOWN, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN), "\u22c6"),         //star
                entry(new KeyCodeCombination(KeyCode.DIGIT0, KeyCombination.SHORTCUT_DOWN, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN), "\u2205"),        //empty set
                entry(new KeyCodeCombination(KeyCode.MINUS, KeyCombination.SHORTCUT_DOWN, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN), "\u00f7"),         //divides
                entry(new KeyCodeCombination(KeyCode.EQUALS, KeyCombination.SHORTCUT_DOWN, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN), "\u2238"),        //dot minus

                    entry(new KeyCodeCombination(KeyCode.Q, KeyCombination.SHORTCUT_DOWN, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN), ""),             //can't use because of Mac shortcut

                entry(new KeyCodeCombination(KeyCode.W, KeyCombination.SHORTCUT_DOWN, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN), "\u21be"),             //harpoon

                entry(new KeyCodeCombination(KeyCode.E, KeyCombination.SHORTCUT_DOWN, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN), "\u2208"),             //element
                entry(new KeyCodeCombination(KeyCode.R, KeyCombination.SHORTCUT_DOWN, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN), "\u2209"),             //not element
                entry(new KeyCodeCombination(KeyCode.T, KeyCombination.SHORTCUT_DOWN, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN), "\u2282"),             //proper subset
                entry(new KeyCodeCombination(KeyCode.Y, KeyCombination.SHORTCUT_DOWN, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN), "\u2286"),             //subset


                entry(new KeyCodeCombination(KeyCode.U, KeyCombination.SHORTCUT_DOWN, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN), "\u222a"),             //union
                entry(new KeyCodeCombination(KeyCode.I, KeyCombination.SHORTCUT_DOWN, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN), "\u22c3"),             //big union
                entry(new KeyCodeCombination(KeyCode.O, KeyCombination.SHORTCUT_DOWN, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN), "\u2229"),             //intersection
                entry(new KeyCodeCombination(KeyCode.P, KeyCombination.SHORTCUT_DOWN, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN), "\u22c2"),             //big intersection
                entry(new KeyCodeCombination(KeyCode.OPEN_BRACKET, KeyCombination.SHORTCUT_DOWN, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN), "\u27e6"),   //left double bracket
                entry(new KeyCodeCombination(KeyCode.CLOSE_BRACKET, KeyCombination.SHORTCUT_DOWN, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN), "\u27e7"),  //right double bracket
                entry(new KeyCodeCombination(KeyCode.BACK_SLASH, KeyCombination.SHORTCUT_DOWN, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN), "\u00a0"),    //unbreakable space


                entry(new KeyCodeCombination(KeyCode.A, KeyCombination.SHORTCUT_DOWN, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN), "\u2248"),            //double wave
                entry(new KeyCodeCombination(KeyCode.S, KeyCombination.SHORTCUT_DOWN, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN), "\u2243"),             //wave above line
                entry(new KeyCodeCombination(KeyCode.D, KeyCombination.SHORTCUT_DOWN, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN), "\u2245"),             //wave above equals
                entry(new KeyCodeCombination(KeyCode.F, KeyCombination.SHORTCUT_DOWN, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN), "\u2291"),             //submodel
                entry(new KeyCodeCombination(KeyCode.G, KeyCombination.SHORTCUT_DOWN, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN), "\uf8d1"),             //embedding
                entry(new KeyCodeCombination(KeyCode.H, KeyCombination.SHORTCUT_DOWN, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN), "\u227a"),             //set smaller than
                entry(new KeyCodeCombination(KeyCode.J, KeyCombination.SHORTCUT_DOWN, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN), "\uf8d2"),             //elementary submodel
                entry(new KeyCodeCombination(KeyCode.K, KeyCombination.SHORTCUT_DOWN, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN), "\u227e"),             //elementary embedding
                entry(new KeyCodeCombination(KeyCode.L, KeyCombination.SHORTCUT_DOWN, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN), "\u227c"),             //set less than or equal

                entry(new KeyCodeCombination(KeyCode.SEMICOLON, KeyCombination.SHORTCUT_DOWN, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN), "\u231c"),        //left corner
                entry(new KeyCodeCombination(KeyCode.QUOTE, KeyCombination.SHORTCUT_DOWN, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN), "\u231d"),            //right corner



                entry(new KeyCodeCombination(KeyCode.N, KeyCombination.SHORTCUT_DOWN, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN), "\ue89b"),             //x with dot
                entry(new KeyCodeCombination(KeyCode.M, KeyCombination.SHORTCUT_DOWN, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN), "\ue89c"),             //y with dot


                entry(new KeyCodeCombination(KeyCode.COMMA, KeyCombination.SHORTCUT_DOWN, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN), "\ue89d"),     //z with dot
                entry(new KeyCodeCombination(KeyCode.PERIOD, KeyCombination.SHORTCUT_DOWN, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN), "\u27e9"),         //right angle bracket
                entry(new KeyCodeCombination(KeyCode.SLASH, KeyCombination.SHORTCUT_DOWN, KeyCombination.ALT_DOWN, KeyCombination.SHIFT_DOWN), "\u27e8")             //left angle bracket
        );
    }

    public enum KeyMapValue {
        BASE("Base/Italic"),
        BASE_AND_SCRIPT("Base/Script"),
        BASE_AND_SANS("Base/Sans"),
        BASE_AND_SANS_ITALIC ("Base/Sans-Ital"),
        ITALIC_AND_SANS("Italic/Sans"),
        SANS_AND_ITALIC("Sans/Italic"),
        ITALIC_AND_BLACKBOARD("Italic/Blackboard"),
        SCRIPT_AND_ITALIC("Script/Italic"),
        SCRIPT_AND_SANS("Script/Sans"),
        GREEK_AND_FRAKTUR("Greek/Fraktur");

        private String name;

        KeyMapValue(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }


    public Map<Character, String> getKeyTypedCharMap() {
        return keyTypedCharMap;
    }

    public Map<KeyCodeCombination, String> getKeyPressedCharMap() {
        return keyPressedCharMap;
    }

    public final Boolean getOverlineOn() {
        return overlineOn.get();
    }

    public final void setOverlineOn(Boolean state) {
        overlineOn.set(state);
    }

    public final BooleanProperty overlineOnProperty() {
        return overlineOn;
    }

    private final void toggleOverlineOn() {
        if (getOverlineOn()) {
            setOverlineOn(false);
            setModification(NONE);
        }
        else {
            setOverlineOn(true);
            setModification(OVERLINE);
        }
    }

    public final KeyMapValue getKeyMapState() {
        return keyMapState.get();
    }

    public final void setKeyMapState(KeyMapValue mapValue) {
        lastKeyMapState.set(keyMapState.get());
        keyMapState.set(mapValue);
    }

    public final ObjectProperty<KeyMapValue> keyMapStateProperty() {
        return keyMapState;
    }

    public CharModification getModification() {
        return modification.get();
    }

    public ObjectProperty<CharModification> modificationProperty() {
        return modification;
    }

    public void setModification(CharModification modification) {
        this.modification.set(modification);
    }

    //added by me as part of effort to get height

    public void setCaretPosition(int length) {
        viewModel.setCaretPosition(length);
    }


 //   public double getComputedHeight() {
 //       return paragraphListView.computeTotalHeight();
 //   }


    private Bounds bounds;
    public Bounds getCaretPosition() {
        caret().ifPresentOrElse(caret ->
        {
        //   bounds = caret.sceneToLocal(caret.getBoundsInLocal());
            bounds = caret.localToScene(caret.getBoundsInLocal());
        }, () ->
        {
        //    System.out.println("caret not present");
        });
        return bounds;
    }


    public DoubleProperty nodesWidthProperty() {
        return nodesWidth;
    }



}

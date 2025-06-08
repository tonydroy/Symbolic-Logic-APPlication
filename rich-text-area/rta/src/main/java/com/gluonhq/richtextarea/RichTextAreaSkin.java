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
import com.gluonhq.richtextarea.viewmodel.RichTextAreaViewModel;
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
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.Skin;
import javafx.scene.control.SkinBase;
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
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.input.KeyCode;

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

    private static final ActionCmdFactory ACTION_CMD_FACTORY = new ActionCmdFactory();


    


    private Map<KeyCodeCombination, String> keyPressedCharMap;
    private Map<Character, String> keyTypedCharMap;
    private final Map<Character, String> BASE_MAP = initializeBaseMap();
    private final Map<KeyCodeCombination, String> ITALIC_ALT_MAP = initializeItalicAltMap();
    private final Map<KeyCodeCombination, String> SCRIPT_ALT_MAP = initializeScriptAltMap();
    private final Map<Character, String> ITALIC_MAP = initializeItalicMap();
    private final Map<Character, String> SCRIPT_MAP = initializeScriptMap();
    private final Map<Character, String> GREEK_MAP = initializeGreekMap();
    private final Map<KeyCodeCombination, String> SANS_MAP = initializeSansMap();
    private final Map<KeyCodeCombination, String> BLACKBOARD_MAP = initializeBlackboardMap();
    private final Map<KeyCodeCombination, String> FRAKTUR_MAP = initializeFrakturMap();
    private final Map<KeyCodeCombination, String> SPECIAL_CHARACTER_MAP = initializeSpecialCharacterMap();
    private final Map<KeyCodeCombination, String> SPECIAL_WITH_ITALIC_MAP;
    private final Map<KeyCodeCombination, String> SPECIAL_WITH_SANS_MAP;
    private final Map<KeyCodeCombination, String> SPECIAL_WITH_SCRIPT_MAP;
    private final Map<KeyCodeCombination, String> SPECIAL_WITH_BLACKBOARD_MAP;
    private final Map<KeyCodeCombination, String> SPECIAL_WITH_FRAKTUR_MAP;

    private DoubleProperty nodesWidth = new SimpleDoubleProperty();

    private BooleanProperty overlineOn = new SimpleBooleanProperty(false);
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

            case ITALIC_AND_SANS: {
                keyPressedCharMap = SPECIAL_WITH_SANS_MAP;
                keyTypedCharMap = ITALIC_MAP;
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
            entry(new KeyCodeCombination(F4), e -> {
                setKeyMapState(KeyMapValue.ITALIC_AND_SANS);
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
                else setModification(NONE);
                return null;
            }),

            entry(new KeyCodeCombination(X, SHORTCUT_DOWN, ALT_DOWN, SHIFT_DOWN), e -> {
                if (getModification() != MAC) setModification(MAC);
                else setModification(NONE);
                return null;
            }),

            entry(new KeyCodeCombination(C, SHORTCUT_DOWN, ALT_DOWN, SHIFT_DOWN), e -> {
                if (getModification() != VEC) setModification(VEC);
                else setModification(NONE);
                return null;
            }),

            entry(new KeyCodeCombination(V, SHORTCUT_DOWN, ALT_DOWN, SHIFT_DOWN), e -> {
                if (getModification() != HAT) setModification(HAT);
                else setModification(NONE);
                return null;
            }),

            entry(new KeyCodeCombination(KeyCode.B, SHORTCUT_DOWN, ALT_DOWN, SHIFT_DOWN), e -> {
                if (getModification() != SLASH) setModification(SLASH);
                else setModification(NONE);
                return null;
            }),

            entry(new KeyCodeCombination(KeyCode.ESCAPE), e -> {
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

    private final ParagraphListView paragraphListView;
    private final SortedList<Paragraph> paragraphSortedList = new SortedList<>(viewModel.getParagraphList(), Comparator.comparing(Paragraph::getStart));

    final ContextMenu contextMenu = new ContextMenu();
    private ObservableList<MenuItem> tableCellContextMenuItems;
    private ObservableList<MenuItem> tableContextMenuItems;
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
            getSkinnable().caretPosition.set(externalCaret);
            viewModel.getParagraphWithCaret()
                    .ifPresent(paragraph -> Platform.runLater(paragraphListView::scrollIfNeeded));
        }
    };

    private final InvalidationListener focusListener;
    private final EventHandler<DragEvent> dndHandler = this::dndListener;

    private final ChangeListener<Boolean> tableAllowedListener;
    private final ChangeListener<EmojiSkinTone> skinToneChangeListener;

    private final ResourceBundle resources;


    private class RichVirtualFlow extends VirtualFlow<ListCell<Paragraph>> {

        RichVirtualFlow(RichTextArea control) {
            ReadOnlyObjectProperty<Bounds> clippedBounds = lookup(".clipped-container").layoutBoundsProperty();
            textFlowPrefWidthProperty.bind(Bindings.createDoubleBinding(() -> control.getContentAreaWidth() > 0 ?
                    control.getContentAreaWidth() :
                            clippedBounds.get().getWidth() > 0 ? clippedBounds.get().getWidth() - 10 : -1,
                    control.contentAreaWidthProperty(), clippedBounds));
        }

        @Override
        protected void rebuildCells() {
            super.rebuildCells();
        }
    }

    private class ParagraphListView extends ListView<Paragraph> {

        private final RichVirtualFlow virtualFlow;
        private Group sheet;
        private Region container;

        public ParagraphListView(RichTextArea control) {
            virtualFlow = new RichVirtualFlow(control);
            getStyleClass().setAll("paragraph-list-view");

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
            // force updateItem call to recalculate backgroundPath positions
            virtualFlow.rebuildCells();
        }

        void scrollIfNeeded() {
            final Bounds vfBounds = virtualFlow.localToScene(virtualFlow.getBoundsInLocal());
            double viewportMinY = vfBounds.getMinY();
            double viewportMaxY = vfBounds.getMaxY();
            caret().ifPresentOrElse(caret -> {
                final Bounds bounds = caret.localToScene(caret.getBoundsInLocal());
                double minY = bounds.getMinY();
                double maxY = bounds.getMaxY();
                if (!(maxY <= viewportMaxY && minY >= viewportMinY)) {
                    // If caret is not fully visible, scroll line by line as needed
                    virtualFlow.scrollPixels(maxY > viewportMaxY ?
                            maxY - viewportMaxY + 1 : minY - viewportMinY - 1);
                }
            }, () -> {
                // In case no caret was found (paragraph is not in a listCell yet),
                // scroll directly to the paragraph
                viewModel.getParagraphWithCaret().ifPresent(this::scrollTo);
            });
        }

        private double computeTotalHeight() {
            double totalHeight = 0.0;

            for (int i = 0; i < this.getItems().size(); i++) {
                Paragraph paragraph = this.getItems().get(i);
                totalHeight += (paragraph.getParaHeight() + 1.5);

                }
            return totalHeight;
        }
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

    protected RichTextAreaSkin(final RichTextArea control) {
        super(control);

        this.control = control;

        nodesWidth.bind(viewModel.nodesWidthProperty());

        overlineOnProperty().addListener((ob, ov, nv) -> {
            if (nv) setModification(OVERLINE);
            else setModification(NONE);

        });


        Map<KeyCodeCombination, String> tempMap = new HashMap();
        tempMap.putAll(SPECIAL_CHARACTER_MAP);
        tempMap.putAll(SANS_MAP);
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


        resources = ResourceBundle.getBundle("com.gluonhq.richtextarea.rich-text-area");

        paragraphListView = new ParagraphListView(control);
        paragraphListView.setItems(paragraphSortedList);
        paragraphListView.setFocusTraversable(false);
        getChildren().add(paragraphListView);
        paragraphListView.setCellFactory(p -> new RichListCell(this));
        objectsCacheEvictionTimer = new SmartTimer(paragraphListView::evictUnusedObjects, 1000, 60000);
        controlPrefWidthListener = (obs, ov, nv) -> {
            refreshTextFlow();
            paragraphListView.updateLayout();
        };

        tableAllowedListener = (obs, ov, nv) -> viewModel.setTableAllowed(nv);
        skinToneChangeListener = (obs, ov, nv) -> refreshTextFlow();


        focusListener = o -> paragraphListView.updateLayout();

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
        editableContextMenuItems = null;
        nonEditableContextMenuItems = null;
        attachedProperty.set(false);
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

    private void setup(Document document) {
        if (document == null) {
            return;
        }
        attachedProperty.set(false);
        viewModel.setTextBuffer(new PieceTable(document));
        lastValidCaretPosition = viewModel.getTextBuffer().getInternalPosition(document.getCaretPosition());
        viewModel.setCaretPosition(lastValidCaretPosition);
        viewModel.setDecorationAtParagraph(viewModel.getTextBuffer().getParagraphDecorationAtCaret(lastValidCaretPosition));
        viewModel.addChangeListener(textChangeListener);
        viewModel.setDocument(document);
        viewModel.documentProperty().addListener(documentChangeListener);
        viewModel.autoSaveProperty().bind(getSkinnable().autoSaveProperty());
        promptNode.textProperty().bind(getSkinnable().promptTextProperty());
        promptNode.fillProperty().bind(promptTextFillProperty());

//        getSkinnable().setOnContextMenuRequested(contextMenuEventEventHandler);
        getSkinnable().editableProperty().addListener(this::editableChangeListener);
        getSkinnable().tableAllowedProperty().addListener(tableAllowedListener);
        viewModel.setTableAllowed(getSkinnable().isTableAllowed());

        getSkinnable().setOnKeyPressed(this::keyPressedListener);
        getSkinnable().setOnKeyTyped(this::keyTypedListener);
        getSkinnable().widthProperty().addListener(controlPrefWidthListener);
        getSkinnable().focusedProperty().addListener(focusListener);
        getSkinnable().addEventHandler(DragEvent.ANY, dndHandler);
        getSkinnable().skinToneProperty().addListener(skinToneChangeListener);
        refreshTextFlow();
        requestLayout();
        editableChangeListener(null); // sets up all related listeners
        attachedProperty.set(true);
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
            return Font.font(
                    textDecoration.getFontFamily(),
                    textDecoration.getFontWeight(),
                    textDecoration.getFontPosture(),
                    textDecoration.getFontSize()
            );
        }
        return Font.font(14);
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
            // this ensures changes in decoration are applied:
            paragraphListView.updateLayout();



            if (nonTextNodesCount != nonTextNodes.get()) {
                // when number of images changes, caret
                requestLayout();
                nonTextNodesCount = nonTextNodes.get();
            }
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
        Platform.runLater(paragraphListView::scrollIfNeeded);
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
    }

    private void keyPressedListener(KeyEvent e) {
        long a0 = System.nanoTime();
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
        if (LOG.isLoggable(Level.FINEST)) {
            long a1 = System.nanoTime();
            LOG.finest("KeyPressed processed in " + (a1 - a0) + "ns");
        }

    }

    private void keyTypedListener(KeyEvent e) {
        long a0 = System.nanoTime();
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
        if (LOG.isLoggable(Level.FINEST)) {
            long a1 = System.nanoTime();
            LOG.finest("KeyTyped processed in "+ (a1-a0) + "ns");
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
            else if (getKeyMapState() == KeyMapValue.ITALIC_AND_BLACKBOARD ) outString = charModMaps.getSlashItalBBMap().get(inString);
            else if (getKeyMapState() == KeyMapValue.SCRIPT_AND_ITALIC ) outString = charModMaps.getSlashScriptItalMap().get(inString);
            else if (getKeyMapState() == KeyMapValue.SCRIPT_AND_SANS ) outString = charModMaps.getSlashScriptSansMap().get(inString);
            else if (getKeyMapState() == KeyMapValue.GREEK_AND_FRAKTUR ) outString = charModMaps.getSlashGreekFrakMap().get(inString);
        }

        if (outString == null) outString = "";     //keyboard diagram has empty block
        return outString;
    }

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
                editableContextMenuItems.addAll(new SeparatorMenuItem(), tableMenu);
            }
        } else if (!isEditable && nonEditableContextMenuItems == null) {
            nonEditableContextMenuItems = FXCollections.observableArrayList(
                    createMenuItem(resources.getString("rta.context.menu.copy"), ACTION_CMD_FACTORY.copy()),
                    new SeparatorMenuItem(),
                    createMenuItem(resources.getString("rta.context.menu.selectall"), ACTION_CMD_FACTORY.selectAll()));
        }
        contextMenu.getItems().setAll(isEditable ? editableContextMenuItems : nonEditableContextMenuItems);
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

                entry(new KeyCodeCombination(KeyCode.DIGIT0, KeyCombination.ALT_DOWN), ""),
                entry(new KeyCodeCombination(KeyCode.DIGIT1, KeyCombination.ALT_DOWN), ""),
                entry(new KeyCodeCombination(KeyCode.DIGIT2, KeyCombination.ALT_DOWN), ""),
                entry(new KeyCodeCombination(KeyCode.DIGIT3, KeyCombination.ALT_DOWN), ""),
                entry(new KeyCodeCombination(KeyCode.DIGIT4, KeyCombination.ALT_DOWN), ""),
                entry(new KeyCodeCombination(KeyCode.DIGIT5, KeyCombination.ALT_DOWN), ""),    //top
                entry(new KeyCodeCombination(KeyCode.DIGIT6, KeyCombination.ALT_DOWN), ""),
                entry(new KeyCodeCombination(KeyCode.DIGIT7, KeyCombination.ALT_DOWN), ""),
                entry(new KeyCodeCombination(KeyCode.DIGIT8, KeyCombination.ALT_DOWN), ""),
                entry(new KeyCodeCombination(KeyCode.DIGIT9, KeyCombination.ALT_DOWN), ""),
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

    private Map<KeyCodeCombination, String> initializeSansMap() {
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
                entry(new KeyCodeCombination(KeyCode.SLASH, KeyCombination.ALT_DOWN), "\u2032"),                                                                  //prime


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
        ITALIC_AND_SANS("Italic/Sans"),
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


    public double getComputedHeight() {
        return paragraphListView.computeTotalHeight();
    }


    private Bounds bounds;
    public Bounds getCaretPosition() {
        caret().ifPresentOrElse(caret ->
        {
        //   bounds = caret.sceneToLocal(caret.getBoundsInLocal());
            bounds = caret.localToScene(caret.getBoundsInLocal());
        }, () ->
        {
            System.out.println("caret not present");
        });
        return bounds;
    }


    public DoubleProperty nodesWidthProperty() {
        return nodesWidth;
    }
}

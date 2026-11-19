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

import com.gluonhq.emoji.Emoji;
import com.gluonhq.emoji.EmojiData;
import com.gluonhq.emoji.EmojiSkinTone;
import com.gluonhq.emoji.util.TextUtils;
import com.gluonhq.richtextarea.model.Block;
import com.gluonhq.richtextarea.model.BlockUnit;
import com.gluonhq.richtextarea.model.EmojiUnit;
import com.gluonhq.richtextarea.model.ImageDecoration;
import com.gluonhq.richtextarea.model.Paragraph;
import com.gluonhq.richtextarea.model.TextBuffer;
import com.gluonhq.richtextarea.model.TextDecoration;
import com.gluonhq.richtextarea.model.TextUnit;
import com.gluonhq.richtextarea.model.Unit;
import javafx.application.Platform;
import javafx.geometry.VPos;
import javafx.scene.Cursor;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.ListCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.DragEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseDragEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.IntStream;

import static com.gluonhq.richtextarea.RichTextAreaSkin.DEFAULT_FONT_SIZE;
import static com.gluonhq.richtextarea.model.TableDecoration.TABLE_SEPARATOR;


class RichListCell extends ListCell<Paragraph> {

    private static final Font MIN_LF_FONT = Font.font(DEFAULT_FONT_SIZE);

    private final static Map<String, Color> COLOR_MAP = new HashMap<>();

    private final RichTextAreaSkin richTextAreaSkin;
    private final ParagraphTile paragraphTile;

    RichListCell(RichTextAreaSkin richTextAreaSkin) {
        this.richTextAreaSkin = richTextAreaSkin;
        setFont(MIN_LF_FONT);

        paragraphTile = new ParagraphTile(richTextAreaSkin);
        setText(null);

        // =========================================================
        // THE IRON WALL: Strict Width Constraints
        // =========================================================
        listViewProperty().addListener((obs, oldLv, newLv) -> {
            if (newLv != null) {
                // Force the Cell to never exceed the ListView's inner width
                prefWidthProperty().bind(newLv.widthProperty().subtract(25.0));
                maxWidthProperty().bind(newLv.widthProperty().subtract(25.0));

                // Force the internal ParagraphTile to respect the Cell's bounds
                paragraphTile.prefWidthProperty().bind(prefWidthProperty());
                paragraphTile.maxWidthProperty().bind(maxWidthProperty());
            }
        });

        addEventHandler(MouseEvent.DRAG_DETECTED, event -> {
            event.consume();
            startFullDrag();
            richTextAreaSkin.anchorIndex = getIndex();
        });
        addEventHandler(MouseDragEvent.MOUSE_DRAG_OVER, event -> {
            event.consume();
            if (richTextAreaSkin.anchorIndex != -1) {
                getParagraphTile().ifPresent(p -> p.mouseDraggedListener(event));
            }
        });
        addEventHandler(MouseEvent.MOUSE_PRESSED, event -> {
            getParagraphTile().ifPresentOrElse(
                    p -> p.mousePressedListener(event),
                    () -> {
                        if (!richTextAreaSkin.getSkinnable().isFocused()) {
                            richTextAreaSkin.getSkinnable().requestFocus();
                        }
                        int textLength = richTextAreaSkin.getViewModel().getTextLength();
                        if (richTextAreaSkin.getViewModel().getSelection().isDefined()) {
                            if (!event.isShiftDown()) {
                                richTextAreaSkin.getViewModel().clearSelection();
                            }
                        } else {
                            richTextAreaSkin.getViewModel().setCaretPosition(textLength);
                        }
                        if (richTextAreaSkin.anchorIndex == -1) {
                            richTextAreaSkin.mouseDragStart = textLength;
                        }
                        if (richTextAreaSkin.contextMenu.isShowing()) {
                            richTextAreaSkin.contextMenu.hide();
                        }
                    });
        });
        addEventHandler(MouseEvent.MOUSE_RELEASED, event -> {
            if (richTextAreaSkin.anchorIndex != -1) {
                event.consume();
                richTextAreaSkin.mouseDragStart = -1;
                richTextAreaSkin.anchorIndex = -1;
            }
        });
        addEventHandler(DragEvent.DRAG_OVER, de -> {
            if (richTextAreaSkin.dragAndDropStart != -1) {
                getParagraphTile().ifPresent(p -> {
                    if (!richTextAreaSkin.getSkinnable().isFocused()) {
                        richTextAreaSkin.getSkinnable().requestFocus();
                    }
                    p.mousePressedListener(new MouseEvent(de.getSource(), de.getTarget(), MouseEvent.MOUSE_PRESSED,
                            de.getX(), de.getY(), de.getScreenX(), de.getScreenY(),
                            MouseButton.PRIMARY, 1,
                            false, false, false, false,
                            true, false, false,
                            false, false, false, null));
                });
            }
        });
    }

    @Override
    protected void updateItem(Paragraph item, boolean empty) {
        super.updateItem(item, empty);
        if (item != null && !empty) {
            var fragments = new ArrayList<Node>();
            var backgroundIndexRanges = new ArrayList<IndexRangeColor>();
            var length = new AtomicInteger();
            var positions = new ArrayList<Integer>();
            positions.add(item.getStart());
            AtomicInteger tp = new AtomicInteger(item.getStart());
            richTextAreaSkin.getViewModel().walkFragments((unit, decoration) -> {
                if (decoration instanceof TextDecoration && !unit.isEmpty()) {
                    if (item.getDecoration().hasTableDecoration()) {
                        if (unit instanceof TextUnit) {
                            String text = unit.getText();
                            AtomicInteger s = new AtomicInteger();
                            IntStream.iterate(text.indexOf(TextBuffer.ZERO_WIDTH_TABLE_SEPARATOR),
                                            index -> index >= 0,
                                            index -> text.indexOf(TextBuffer.ZERO_WIDTH_TABLE_SEPARATOR, index + 1))
                                    .boxed()
                                    .forEach(i -> {
                                        String tableText = text.substring(s.getAndSet(i + 1), i + 1);
                                        final Text textNode = buildText(tableText, (TextDecoration) decoration);
                                        textNode.getProperties().put(TABLE_SEPARATOR, tp.get());
                                        fragments.add(textNode);
                                        positions.add(tp.addAndGet(tableText.length()));
                                    });
                            if (s.get() < text.length()) {
                                String tableText = text.substring(s.get()).replace("\n", TextBuffer.ZERO_WIDTH_TEXT);
                                final Text textNode = buildText(tableText, (TextDecoration) decoration);
                                textNode.getProperties().put(TABLE_SEPARATOR, tp.getAndAdd(tableText.length()));
                                fragments.add(textNode);
                                if (text.substring(s.get()).contains("\n")) {
                                    positions.add(tp.get());
                                }
                            }
                        } else {
                            final Node node = buildNode(unit, (TextDecoration) decoration);
                            node.getProperties().put(TABLE_SEPARATOR, tp.getAndIncrement());
                            fragments.add(node);
                            length.addAndGet(unit.length());
                            if (unit instanceof EmojiUnit) {
                                richTextAreaSkin.nonTextNodes.incrementAndGet();
                            }
                        }
                    } else {
                        final Node node = buildNode(unit, (TextDecoration) decoration);
                        fragments.add(node);
                        String background = ((TextDecoration) decoration).getBackground();
                        Color backgroundColor = COLOR_MAP.computeIfAbsent(background, s -> parseColorOrDefault(background, Color.TRANSPARENT));
                        if (!Color.TRANSPARENT.equals(backgroundColor)) {
                            backgroundIndexRanges.add(new IndexRangeColor(
                                    length.get(), length.get() + unit.length(), backgroundColor));
                        }
                    }
                    length.addAndGet(unit.length());
                    if (unit instanceof EmojiUnit) {
                        richTextAreaSkin.nonTextNodes.incrementAndGet();
                    }
                } else if (decoration instanceof ImageDecoration) {
                    fragments.add(buildImage((ImageDecoration) decoration));
                    length.incrementAndGet();
                    richTextAreaSkin.nonTextNodes.incrementAndGet();
                }
            }, item.getStart(), item.getEnd());

            paragraphTile.setParagraph(item, fragments, positions, backgroundIndexRanges);
            setGraphic(paragraphTile);

            // =========================================================
            // PIPELINE SEQUENCE ENFORCER
            // =========================================================

            // 1. Force layout so the TextFlow physically hits the wall and wraps
            paragraphTile.applyCss();
            paragraphTile.layout();

            // 2. NOW calculate the caret bounds (it will correctly report the line drop)
            paragraphTile.updateLayout();

            // 3. Command the Skin to paint the Caret Path at the newly calculated coordinates
            Platform.runLater(() -> {
                if (richTextAreaSkin.getSkinnable() != null) {
                    richTextAreaSkin.getSkinnable().requestLayout();
                }
            });

        } else {
            paragraphTile.setParagraph(null, null, null, null);
            setGraphic(null);

            // Tell JavaFX to naturally recalculate the empty space,
            // avoiding the divide-by-zero Infinity Panic!
            setPrefHeight(javafx.scene.layout.Region.USE_COMPUTED_SIZE);
            setMinHeight(javafx.scene.layout.Region.USE_COMPUTED_SIZE);
            paragraphTile.setPrefHeight(javafx.scene.layout.Region.USE_COMPUTED_SIZE);
            paragraphTile.setMinHeight(javafx.scene.layout.Region.USE_COMPUTED_SIZE);
            // -------------------------------------------------------
        }
    }

    private Node buildNode(Unit unit, TextDecoration decoration) {
        if (unit instanceof TextUnit) {
            return buildText(unit.getText(), decoration);
        } else if (unit instanceof BlockUnit) {
            Block block = ((BlockUnit) unit).getBlock();
            Text text = buildText(block.getContent(), decoration);
            text.setTextOrigin(VPos.TOP);
            return new Group(text);
        } else if (unit instanceof EmojiUnit) {
            Emoji emoji = ((EmojiUnit) unit).getEmoji();
            EmojiSkinTone tone = richTextAreaSkin.getSkinnable().getSkinTone();
            double emojiSize = Math.ceil(decoration.getFontSize() * TextUtils.EMOJI_SIZE_FONT_FACTOR);
            return TextUtils.convertUnifiedToImageNode(tone != null ?
                    EmojiData.emojiWithTone(emoji, tone).getUnified() :
                    emoji.getUnified(), emojiSize);
        } else {
            throw new RuntimeException("Error: Unit " + unit + " not supported yet");
        }
    }

    //new ai version

    private Text buildText(String content, TextDecoration decoration) {
        boolean isNewline = "\n".equals(content);
        Objects.requireNonNull(decoration);

        // CRITICAL: Use a zero-width space for newlines so it can hold the font height
        Text text = new Text(isNewline ? "\u200B" : content.replace("\n", ""));

        String foreground = decoration.getForeground();
        text.setFill(COLOR_MAP.computeIfAbsent(foreground, s -> parseColorOrDefault(foreground, Color.BLACK)));

        if (!isNewline) {
            text.setStrikethrough(decoration.isStrikethrough());
            text.setUnderline(decoration.isUnderline());

            if (decoration.isSubscript() || decoration.isTransSubscript()) text.setTranslateY(decoration.getFontSize() * .17);
            else if (decoration.isSuperscript() || decoration.isTransSuperscript()) text.setTranslateY(decoration.getFontSize() * -.4);
            else text.setTranslateY(0.0);

            if (decoration.isTransSubscript() || decoration.isTransSuperscript()) text.setTranslateX(decoration.getFontSize() * -.3);
            else text.setTranslateX(0.0);
        }

        double actualFontSize = (decoration.isSuperscript() || decoration.isSubscript() || decoration.isTransSuperscript() || decoration.isTransSubscript() ) ? decoration.getFontSize() * .72 : decoration.getFontSize();

        int hash = Objects.hash(decoration.getFontFamily(), decoration.getFontWeight(), decoration.getFontPosture(), actualFontSize);

        Font font = richTextAreaSkin.getFontCache().computeIfAbsent(hash, h -> Font.font(
                decoration.getFontFamily(), decoration.getFontWeight(), decoration.getFontPosture(), actualFontSize));

        // By applying the font to the zero-width space, TextFlow sizes the empty line perfectly
        text.setFont(font);

        if (!isNewline) {
            String url = decoration.getURL();
            if (url != null) {
                text.setUnderline(true);
                text.setFill(Color.BLUE);
                text.setCursor(Cursor.HAND);
                text.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> {
                    if (e.getButton() == MouseButton.PRIMARY) {
                        Function<Node, Consumer<String>> linkCallbackFactory = richTextAreaSkin.getSkinnable().getLinkCallbackFactory();
                        if (linkCallbackFactory != null) {
                            Consumer<String> consumer = linkCallbackFactory.apply(text);
                            if (consumer != null) {
                                consumer.accept(url);
                            }
                        }
                    }
                });
            }
        }
        return text;
    }

    /*
    private Text buildText(String content, TextDecoration decoration) {
        if ("\n".equals(content)) {
            return Tools.noWidthText();
        }
        Objects.requireNonNull(decoration);
        Text text = new Text(Objects.requireNonNull(content).replace("\n", ""));
        String foreground = decoration.getForeground();
        text.setFill(COLOR_MAP.computeIfAbsent(foreground, s -> parseColorOrDefault(foreground, Color.BLACK)));
        text.setStrikethrough(decoration.isStrikethrough());
        text.setUnderline(decoration.isUnderline());

        if (decoration.isSubscript() || decoration.isTransSubscript()) text.setTranslateY(decoration.getFontSize() * .17);
        else if (decoration.isSuperscript() || decoration.isTransSuperscript()) text.setTranslateY(decoration.getFontSize() * -.4);
        else text.setTranslateY(0.0);

        if (decoration.isTransSubscript() || decoration.isTransSuperscript()) text.setTranslateX(decoration.getFontSize() * -.3);
        else text.setTranslateX(0.0);

        double actualFontSize = (decoration.isSuperscript() || decoration.isSubscript() || decoration.isTransSuperscript() || decoration.isTransSubscript() ) ? decoration.getFontSize() * .72 : decoration.getFontSize();

        int hash = Objects.hash(
                decoration.getFontFamily(),
                decoration.getFontWeight(),
                decoration.getFontPosture(),
                actualFontSize);

        Font font = richTextAreaSkin.getFontCache().computeIfAbsent(hash, h -> Font.font(
                decoration.getFontFamily(),
                decoration.getFontWeight(),
                decoration.getFontPosture(),
                actualFontSize));

        text.setFont(font);
        String url = decoration.getURL();
        if (url != null) {
            text.setUnderline(true);
            text.setFill(Color.BLUE);
            text.setCursor(Cursor.HAND);
            text.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> {
                if (e.getButton() == MouseButton.PRIMARY) {
                    Function<Node, Consumer<String>> linkCallbackFactory = richTextAreaSkin.getSkinnable().getLinkCallbackFactory();
                    if (linkCallbackFactory != null) {
                        Consumer<String> consumer = linkCallbackFactory.apply(text);
                        if (consumer != null) {
                            consumer.accept(url);
                        }
                    }
                }
            });
        }
        return text;
    }

     */

    private ImageView buildImage(ImageDecoration imageDecoration) {
        Image image = richTextAreaSkin.getImageCache().computeIfAbsent(imageDecoration.getUrl(), Image::new);
        final ImageView imageView = new ImageView(image);
        if (imageDecoration.getWidth() > -1 && imageDecoration.getHeight() > -1) {

            //******** ai fit picture
            double maxAllowedWidth = richTextAreaSkin.textFlowPrefWidthProperty.get() - 10;
            double w = imageDecoration.getWidth();
            double h = imageDecoration.getHeight();
            if (maxAllowedWidth > 20 && w > maxAllowedWidth) {
                h = h * (maxAllowedWidth / w);
                w = maxAllowedWidth;
            }
            imageView.setFitWidth(w);
            imageView.setFitHeight(h);

            //**********
       //     imageView.setFitWidth(imageDecoration.getWidth());
        //    imageView.setFitHeight(imageDecoration.getHeight());
        } else {
            double width = Math.min(image.getWidth(), richTextAreaSkin.textFlowPrefWidthProperty.get() - 10);
            imageView.setFitWidth(width);
            imageView.setPreserveRatio(true);
        }
        if (imageDecoration.getLink() != null) {
            imageView.setCursor(Cursor.HAND);
            imageView.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> {
                Function<Node, Consumer<String>> linkCallbackFactory = richTextAreaSkin.getSkinnable().getLinkCallbackFactory();
                if (linkCallbackFactory != null) {
                    Consumer<String> consumer = linkCallbackFactory.apply(imageView);
                    if (consumer != null) {
                        consumer.accept(imageDecoration.getLink());
                    }
                }
            });
        }
        return imageView;
    }

    void evictUnusedObjects(Set<Font> usedFonts, Set<Image> usedImages) {
        getParagraphTile().ifPresent(tile -> tile.evictUnusedObjects(usedFonts, usedImages));
    }

    public void forwardDragEvent(MouseEvent e) {
        getParagraphTile().ifPresent(tile -> tile.mouseDraggedListener(e));
    }

    public boolean hasCaret() {
        return getParagraphTile()
                .map(ParagraphTile::hasCaret)
                .orElse(false);
    }

    public void resetCaret() {
        getParagraphTile().ifPresent(ParagraphTile::resetCaret);
    }

    public int getNextRowPosition(double x, boolean down) {
        return getParagraphTile()
                .map(tile -> tile.getNextRowPosition(x, down))
                .orElse(-1);
    }

    public int getNextTableCellPosition(boolean down) {
        return getParagraphTile()
                .map(tile -> tile.getNextTableCellPosition(down))
                .orElse(-1);
    }

    private Optional<ParagraphTile> getParagraphTile() {
        if (getGraphic() instanceof ParagraphTile) {
            return Optional.of((ParagraphTile) getGraphic());
        }
        return Optional.empty();
    }

    private static Color parseColorOrDefault(String color, Color defaultColor) {
        try {
            return Color.web(color);
        } catch (Exception e) {
            return defaultColor;
        }
    }
}
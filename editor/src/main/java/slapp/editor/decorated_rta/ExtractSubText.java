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

import com.gluonhq.richtextarea.model.DecorationModel;
import com.gluonhq.richtextarea.model.Document;
import com.gluonhq.richtextarea.model.TextBuffer;
import com.gluonhq.richtextarea.model.TextDecoration;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import java.util.List;
import java.util.Objects;


public class ExtractSubText {

    /**
     * Extract TextFlow (like a decorated substring) from Document
     * @param start the start position in the document
     * @param length the length in the document
     * @param doc the Document
     * @return the text flow
     */
    public static TextFlow getTextFromDoc(int start, int length, Document doc) {
        TextFlow flow = new TextFlow();
        String docString = doc.getText();
        List<DecorationModel> decorations = doc.getDecorations();
        StringBuilder sb = new StringBuilder();
        DecorationModel currentDec = getDecorationAtIndex(start, decorations);

        //to stop wrapping on base column
        double width = 0;

        for (int i = start; i < start + length; i++ ) {
            DecorationModel decorationAtIndex = getDecorationAtIndex(i, decorations);
            if (decorationAtIndex != currentDec) {
                Text text = buildText(sb.toString(), (TextDecoration) currentDec.getDecoration());

                //
                width = width + text.getLayoutBounds().getWidth();

                flow.getChildren().add(text);
                sb.delete(0, sb.length());
                currentDec = decorationAtIndex;
                sb.append(docString.charAt(i));
            } else {
                if (!Character.isWhitespace(docString.charAt(i))) sb.append(docString.charAt(i));
            }
        }
        if (sb.length() != 0) {
            Text text = buildText(sb.toString(), (TextDecoration) currentDec.getDecoration());

            width = width + text.getLayoutBounds().getWidth();

            flow.getChildren().add(text);
        }

        //
        flow.setMinWidth(width );

        return flow;
    }

    /*
     * Get the decoration with application to the index position in Document
     * @param index position in document
     * @param decorations the document decorations
     * @return the applicable DecorationModel
     */
    private static DecorationModel getDecorationAtIndex(int index, List<DecorationModel> decorations) {
        DecorationModel decorationAtIndex = null;
        for (DecorationModel decoration : decorations) {
            if (decoration.getStart() <= index && index < decoration.getStart() + decoration.getLength()) {
                decorationAtIndex = decoration;
                break;
            }
        }
        return decorationAtIndex;
    }

    /*
     * Build a Text object from string content and decoration
     * @param content the string content
     * @param decoration the decorations
     * @return the text
     */
    private static Text buildText(String content, TextDecoration decoration) {

        if ("\n".equals(content)) {
            Text lfText = new Text(TextBuffer.ZERO_WIDTH_TEXT);
            return lfText;
        }
        Objects.requireNonNull(decoration);
        Text text = new Text(Objects.requireNonNull(content));
//        text.setFill(decoration.getForeground());
        Color.web(decoration.getForeground());
        text.setStrikethrough(decoration.isStrikethrough());
        text.setUnderline(decoration.isUnderline());

        if (decoration.isSubscript() || decoration.isTransSubscript()) text.setTranslateY(decoration.getFontSize() * .17);
        else if (decoration.isSuperscript() || decoration.isTransSuperscript()) text.setTranslateY(decoration.getFontSize() * -.4);
        else text.setTranslateY(0.0);

        if (decoration.isTransSubscript() || decoration.isTransSuperscript()) text.setTranslateX(decoration.getFontSize() * -.3);
        else text.setTranslateX(0.0);

        double actualFontSize = (decoration.isSuperscript() || decoration.isSubscript() || decoration.isTransSuperscript() || decoration.isTransSubscript() ) ? decoration.getFontSize() * .72 : decoration.getFontSize();

        Font font = Font.font(
                decoration.getFontFamily(),
                decoration.getFontWeight(),
                decoration.getFontPosture(),
                actualFontSize);
        text.setFont(font);

        return text;
    }

}

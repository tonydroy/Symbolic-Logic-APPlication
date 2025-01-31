package slapp.editor.parser;

import com.gluonhq.richtextarea.model.DecorationModel;
import com.gluonhq.richtextarea.model.Document;
import com.gluonhq.richtextarea.model.TextDecoration;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

import java.util.ArrayList;
import java.util.List;

public class ParseUtilities {


    //strip space characters and return list of remaining elements
    public static List<OriginalElement> getElements(Document doc) {
        List<OriginalElement> elements = new ArrayList<OriginalElement>();
        String text = doc.getText();
        for (int i = 0; i < text.length(); ) {
            int codePoint = text.codePointAt(i);
            TextDecoration dec = (TextDecoration) (getDecorationAtIndex(i, doc.getDecorations()).getDecoration());
            OriginalElement element = new OriginalElement(codePoint, dec);
            elements.add(element);
            i += Character.charCount(codePoint);
        }
        int i = 0;
        while (i < elements.size()) {
            if (Character.isWhitespace((char) (elements.get(i).code))) {
                elements.remove(i);
                continue;
            }
            i = i + 1;
        }

        return elements;
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

    public static Text newRegularText(String text) {
        Text t = new Text(text);
        t.setFont(new Font("Noto Serif Combo", 11));
        return t;
    }

    public static Text newSuperscriptText(String text) {
        Text t = new Text(text);
        t.setFont(new Font("Noto Serif Combo", 11 * .72));
        t.setTranslateY(11 * -.4);
        return t;
    }

    public static Text newSubscriptText(String text) {
        Text t = new Text(text);
        t.setFont(new Font("Noto Serif Combo", 11 * .72));
        t.setTranslateY(11 * .17);
        return t;
    }

    public static Text newShiftedSubscriptText(String text) {
        Text t = new Text(text);
        t.setFont(new Font("Noto Serif Combo", 11 * .72));
        t.setTranslateY(11 * .17);
        t.setTranslateX(11 * -.3);
        return t;
    }

    //delete once have create window
    private Language testLanguage() {
        Language lang = new Language("Lq");

        lang.setOpenBracket1(40); lang.setCloseBracket1(41);
        lang.setNegation(8764);
        lang.setConditional(8594);
        lang.setUniversal(8704);
        lang.setVariables(Alphabets.getCharacterRange(11984, 119911));
        lang.setIntVariableSubs(true);
        lang.setConstants(Alphabets.getCharacterRange(119886, 8462));
        lang.setIntConstantSubs(true);
        lang.setSentenceLetters(Alphabets.getCharacterRange(119860, 119885));
        lang.setIntSentenceLetterSubs(true);
        lang.setXrelationSymbols(Alphabets.getCharacterRange(119860, 119885));
        lang.setXrelationSymbolSubs(true);
        lang.setXrelationSymbolsRequireSuper(true);
        lang.setXfunctionSymbols(Alphabets.getCharacterRange(119886, 1219911));
        lang.setXfunctionSymbolSubs(true);
        return lang;
    }

}

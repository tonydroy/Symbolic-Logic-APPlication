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
        String s;
        int i = 0;
        while (i < text.length()) {
            int len = 1;
            if (i + 2 <= text.length() && text.codePointCount(i, i + 2) == 1) {
                len = 2;
            }
            s = text.substring(i, i + len);
            TextDecoration dec = (TextDecoration) (getDecorationAtIndex(i, doc.getDecorations()).getDecoration());
            OriginalElement element = new OriginalElement(s, dec);
            elements.add(element);
            i = i + len;
        }
        i = 0;
        while (i < elements.size()) {
            String elementStr = elements.get(i).getElementStr();
            char ch = elementStr.charAt(0);
            boolean bad = elementStr.length() == 1 && 0xD800 <= (int) ch && (int) ch <= 0xDFFF;
            if (Character.isWhitespace(ch) || bad) {
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

    public static Text[] newSupSubText(String supText, String subText) {
        Text t1 = new Text(supText);
        Text t2 = new Text(subText);
        t1.setFont(new Font("Noto Serif Combo", 11 * .72));
        t2.setFont(new Font("Noto Serif Combo", 11 * .72));
        t1.setTranslateY(11 * -.4);
        t2.setTranslateY(11 * .17);
        t1.applyCss();
        double offset = t1.getLayoutBounds().getWidth();
        t2.setTranslateX(-offset);
        return new Text[]{t1, t2};
    }

    //delete once have create window
    private Language testLanguage() {
        Language lang = new Language("Lq");

        lang.setOpenBracket1("("); lang.setCloseBracket1(")");
        lang.setNegation("\u223c");
        lang.setConditional("\u2192");
        lang.setUniversalQuant("\u2200");
        lang.setVariables(Alphabets.getCharacterRange("\ud835\udc56", "\ud835\udc67"));
        lang.setVariableSubs(true);
        lang.setConstants(Alphabets.getCharacterRange("\ud835\udc4e", "\u210e"));
        lang.setConstantSubs(true);
        lang.setSentenceLetters(Alphabets.getCharacterRange("\ud835\udc34", "\ud835\udc4d"));
        lang.setSentenceLetterSubs(true);
        lang.setXrelationSymbols(Alphabets.getCharacterRange("\ud835\udc34", "\ud835\udc4d"));
        lang.setXrelationSymbolSubs(true);
        lang.setXrelationSymbolsRequireSuper(true);
        lang.setXfunctionSymbols(Alphabets.getCharacterRange("\ud835\udc4e", "\ud835\udc67"));
        lang.setXfunctionSymbolSubs(true);
        return lang;
    }

}

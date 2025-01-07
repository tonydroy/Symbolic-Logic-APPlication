package slapp.editor.parser;

import com.gluonhq.richtextarea.model.DecorationModel;
import com.gluonhq.richtextarea.model.Document;
import com.gluonhq.richtextarea.model.TextDecoration;

import java.util.ArrayList;
import java.util.List;

public class ParseUtilities {

    public static List<Element> getElements(Document doc) {
        List<Element> elements = new ArrayList<Element>();
        String text = doc.getText();
        for (int i = 0; i < text.length(); ) {
            int codePoint = text.codePointAt(i);
            TextDecoration dec = (TextDecoration) (getDecorationAtIndex(i, doc.getDecorations()).getDecoration());
            Element element = new Element(codePoint, dec);
            elements.add(element);
            i += Character.charCount(codePoint);
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

}

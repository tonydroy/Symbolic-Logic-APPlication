package slapp.editor.parser;

import com.gluonhq.richtextarea.model.TextDecoration;
import javafx.scene.text.TextFlow;

import static slapp.editor.parser.ExpressionType.ORIGINAL_ELEMENT;

public class OriginalElement implements Expression {

    ExpressionType type = ORIGINAL_ELEMENT;
    TextDecoration decoration;
    String elementStr;


    OriginalElement(String elementStr, TextDecoration decoration) {
        this.elementStr = elementStr;
        this.decoration = decoration;
    }

    public String getElementStr() {     return elementStr;  }

    @Override
    public ExpressionType getType() { return type; }

    @Override
    public String toString() {
        return "element string: " + elementStr + " Dec: " + decoration.toString();

        /*
        String s = "";
        for (char c : elementStr.toCharArray()) {
            s = s + String.format("\\u%04x", (int) c);
        }
        return "char: " + elementStr + " code: " + s;

         */
    }

    @Override
    public TextFlow toTextFlow() {return null;}
}

package slapp.editor.parser;

import com.gluonhq.richtextarea.model.TextDecoration;

import static slapp.editor.parser.ExpressionType.ELEMENT;

public class Element implements Expression {

    ExpressionType type = ELEMENT;
    TextDecoration decoration;
    int code;


    Element(int code, TextDecoration decoration) {
        this.code = code;
        this.decoration = decoration;
    }


    @Override
    public ExpressionType getType() { return type; }

    @Override
    public String toString() {
        return "code: " + code + " Dec: " + decoration.toString();
    }
}

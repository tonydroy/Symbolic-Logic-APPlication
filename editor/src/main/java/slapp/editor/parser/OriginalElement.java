package slapp.editor.parser;

import com.gluonhq.richtextarea.model.TextDecoration;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import static slapp.editor.parser.ExpressionType.ORIGINAL_ELEMENT;

public class OriginalElement implements Expression {

    ExpressionType type = ORIGINAL_ELEMENT;
    TextDecoration decoration;
    int code;


    OriginalElement(int code, TextDecoration decoration) {
        this.code = code;
        this.decoration = decoration;
    }


    @Override
    public ExpressionType getType() { return type; }

    @Override
    public String toString() {
        return "code: " + code + " Dec: " + decoration.toString();
    }

    @Override
    public TextFlow toTextFlow() {return null;}
}

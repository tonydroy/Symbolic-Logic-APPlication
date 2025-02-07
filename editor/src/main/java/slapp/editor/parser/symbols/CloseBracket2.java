package slapp.editor.parser.symbols;

import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import slapp.editor.parser.Expression;
import slapp.editor.parser.ExpressionType;
import slapp.editor.parser.ParseUtilities;

import static slapp.editor.parser.ExpressionType.CLOSE_BRACKET1;
import static slapp.editor.parser.ExpressionType.CLOSE_BRACKET2;


public class CloseBracket2 extends CloseBracket implements Expression {

    public CloseBracket2(String textString) {
        super(textString);
        setType(CLOSE_BRACKET2);
    }



}

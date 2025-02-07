package slapp.editor.parser.symbols;

import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import slapp.editor.parser.Expression;
import slapp.editor.parser.ExpressionType;
import slapp.editor.parser.ParseUtilities;

import static slapp.editor.parser.ExpressionType.CLOSE_BRACKET1;
import static slapp.editor.parser.ExpressionType.OPEN_BRACKET1;



public class OpenBracket1 extends OpenBracket implements Expression {

    public OpenBracket1(String textString) {

        super(textString);
        setType(OPEN_BRACKET1);
    }





}

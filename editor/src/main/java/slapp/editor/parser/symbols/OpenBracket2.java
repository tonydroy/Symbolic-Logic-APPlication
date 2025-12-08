package slapp.editor.parser.symbols;

import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import slapp.editor.parser.Expression;
import slapp.editor.parser.ExpressionType;
import slapp.editor.parser.ParseUtilities;

import static slapp.editor.parser.ExpressionType.OPEN_BRACKET1;
import static slapp.editor.parser.ExpressionType.OPEN_BRACKET2;


public class OpenBracket2  extends OpenBracket implements Expression{

    public OpenBracket2(String textString) {

        super(textString);
        setType(OPEN_BRACKET2);
    }



}

package slapp.editor.parser.symbols;

import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import slapp.editor.parser.Expression;
import slapp.editor.parser.ExpressionType;
import slapp.editor.parser.ParseUtilities;

import static slapp.editor.parser.ExpressionType.CLOSE_BRACKET1;
import static slapp.editor.parser.ExpressionType.CLOSE_BRACKET3;


public class CloseBracket3 implements Expression {

    private ExpressionType type = CLOSE_BRACKET3;
    private String textString;


    CloseBracket3(String textString) {
        this.textString = textString;
    }

    @Override
    public String toString() {
        return textString;
    }

    @Override
    public TextFlow toTextFlow() {
        Text text = ParseUtilities.newRegularText(textString);
        return new TextFlow(text);
    }

    @Override
    public ExpressionType getType() {return type;}
}

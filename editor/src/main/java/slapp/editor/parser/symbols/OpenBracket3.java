package slapp.editor.parser.symbols;

import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import slapp.editor.parser.Expression;
import slapp.editor.parser.ExpressionType;
import slapp.editor.parser.ParseUtilities;

import static slapp.editor.parser.ExpressionType.OPEN_BRACKET1;
import static slapp.editor.parser.ExpressionType.OPEN_BRACKET3;


public class OpenBracket3 implements Expression {

    private ExpressionType type = OPEN_BRACKET3;
    private String textString;


    OpenBracket3(String textString) {
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

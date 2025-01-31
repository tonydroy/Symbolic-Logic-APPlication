package slapp.editor.parser.symbols;

import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import slapp.editor.parser.Expression;
import slapp.editor.parser.ExpressionType;
import slapp.editor.parser.ParseUtilities;

import static slapp.editor.parser.ExpressionType.CONDITIONAL;
import static slapp.editor.parser.ExpressionType.NOR;


public class Nor implements Expression {

    private ExpressionType type = NOR;
    private String textString;

    Nor(String textString) {
        this.textString = textString;
    }

    @Override
    public String toString() {
        return textString;
    }

    @Override
    public TextFlow toTextFlow() {
        Text text = ParseUtilities.newRegularText(" " + textString + " ");
        return new TextFlow(text);
    }

    @Override
    public ExpressionType getType() {return type;}

}

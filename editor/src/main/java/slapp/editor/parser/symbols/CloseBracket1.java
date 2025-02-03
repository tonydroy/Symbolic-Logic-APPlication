package slapp.editor.parser.symbols;

import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import slapp.editor.parser.Expression;
import slapp.editor.parser.ExpressionType;
import slapp.editor.parser.ParseUtilities;

import static slapp.editor.parser.ExpressionType.CLOSE_BRACKET1;
import static slapp.editor.parser.ExpressionType.OPEN_BRACKET1;


public class CloseBracket1 implements Expression {

    private ExpressionType type = CLOSE_BRACKET1;
    private String textString;


    public CloseBracket1(String textString) {
        this.textString = textString;
    }

    @Override
    public ExpressionType getType() {return type;}

    @Override
    public TextFlow toTextFlow() {
        Text text = ParseUtilities.newRegularText(textString);
        return new TextFlow(text);
    }

    @Override
    public String toString() {
        return textString;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o instanceof Biconditional) {
            CloseBracket1 other = (CloseBracket1) o;
            return textString.equals(other.textString);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return textString.hashCode();
    }


}

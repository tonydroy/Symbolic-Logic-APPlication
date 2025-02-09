package slapp.editor.parser.symbols;

import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import slapp.editor.parser.Expression;
import slapp.editor.parser.ExpressionType;
import slapp.editor.parser.ParseUtilities;
import slapp.editor.parser.grammatical_parts.Operator;

import static slapp.editor.parser.ExpressionType.UNIVERSAL_SYM;


public class UniversalQuantifierSym extends OperatorSym implements Expression {

    private ExpressionType type = UNIVERSAL_SYM;
    private String textString;

    public UniversalQuantifierSym(String textString) {
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
        if (o instanceof UniversalQuantifierSym) {
            UniversalQuantifierSym other = (UniversalQuantifierSym) o;
            return textString.equals(other.textString);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return textString.hashCode();
    }






}

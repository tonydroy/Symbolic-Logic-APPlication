package slapp.editor.parser.symbols;

import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import slapp.editor.parser.Expression;
import slapp.editor.parser.ExpressionType;
import slapp.editor.parser.ParseUtilities;


public class Variable implements Expression {

    private ExpressionType type = ExpressionType.VARIABLE;
    private String baseStr;
    private String subscriptStr = "";


    public Variable(String baseSymbol, String subscriptStr) {
        this.baseStr = baseSymbol;
        this.subscriptStr = subscriptStr;
    }

    @Override
    public ExpressionType getType() { return type; }

    @Override
    public TextFlow toTextFlow() {
        Text baseText = ParseUtilities.newRegularText(baseStr);
        Text subText = null;
        if (!subscriptStr.isEmpty()) subText = ParseUtilities.newSubscriptText(subscriptStr);
        return new TextFlow(baseText, subText);
    }

    @Override
    public String toString() {
        String endString = "";
        if (!subscriptStr.isEmpty()) {endString = "_" + subscriptStr;}
        return baseStr + endString;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if ((o instanceof Variable)) {
            Variable other = (Variable) o;
            return baseStr.equals(other.baseStr) && subscriptStr.equals(other.subscriptStr);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return baseStr.hashCode() + subscriptStr.hashCode();
    }

}

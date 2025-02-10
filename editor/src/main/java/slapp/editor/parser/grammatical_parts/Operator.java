package slapp.editor.parser.grammatical_parts;

import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import slapp.editor.parser.Expression;
import slapp.editor.parser.ExpressionType;
import slapp.editor.parser.symbols.OperatorSym;

import java.util.List;

public class Operator implements Expression {

    private ExpressionType type;
    private OperatorSym mainSymbol;
    private boolean permitInfix = false;
    private boolean unary;


    public Operator(ExpressionType type, OperatorSym mainSymbol, boolean unary) {
        this.type = type;
        this.unary = unary;
        this.mainSymbol = mainSymbol;
    }


    public boolean isUnary() {
        return unary;
    }

    public void setType(ExpressionType type) {
        this.type = type;
    }

    public OperatorSym getMainSymbol() {
        return mainSymbol;
    }

    public void setMainSymbol(OperatorSym mainSymbol) {
        this.mainSymbol = mainSymbol;
    }

    public boolean isPermitInfix() {
        return permitInfix;
    }

    public void setPermitInfix(boolean permitInfix) {
        this.permitInfix = permitInfix;
    }

    @Override
    public ExpressionType getType() {
        return type;
    }

    @Override
    public List<Text> toTextList() {
        return mainSymbol.toTextList();
    }


    @Override
    public String toString() {
        return mainSymbol.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if ((o instanceof Operator)) {
            Operator other = (Operator) o;
            return mainSymbol.equals(other.mainSymbol);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return mainSymbol.hashCode();
    }

}

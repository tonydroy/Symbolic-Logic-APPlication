package slapp.editor.parser.grammatical_parts;

import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import slapp.editor.parser.Expression;
import slapp.editor.parser.ExpressionType;
import slapp.editor.parser.symbols.ExistentialQuantifierSym;

import java.util.ArrayList;
import java.util.List;

public class ExistentialOp extends Operator implements Expression {
    Term variableTerm;

    public ExistentialOp(ExistentialQuantifierSym sym, Term variableTerm) {

        super(ExpressionType.EXISTENTIAL_OP, sym, true);
        this.variableTerm = variableTerm;
    }

    @Override
    public Term getVariableTerm() {
        return variableTerm;
    }

    @Override
    public List<Text> toTextList() {
        List<Text> texts = new ArrayList();
        texts.addAll(getMainSymbol().toTextList());
        texts.addAll(variableTerm.toTextList());
        return texts;
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getMainSymbol().toString());
        sb.append(variableTerm.toString());
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if ((o instanceof ExistentialOp)) {
            ExistentialOp other = (ExistentialOp) o;
            return getMainSymbol().equals(other.getMainSymbol()) && variableTerm.equals(other.variableTerm);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return getMainSymbol().hashCode() + variableTerm.hashCode();
    }
}

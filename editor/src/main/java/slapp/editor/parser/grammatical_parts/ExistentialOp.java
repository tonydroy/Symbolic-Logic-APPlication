package slapp.editor.parser.grammatical_parts;

import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import slapp.editor.parser.Expression;
import slapp.editor.parser.ExpressionType;
import slapp.editor.parser.symbols.ExistentialQuantifierSym;
import slapp.editor.parser.symbols.UniversalQuantifierSym;
import slapp.editor.parser.symbols.Variable;

import java.util.ArrayList;
import java.util.List;

public class ExistentialOp extends Operator implements Expression {
    Term variableTerm;

    public ExistentialOp(ExistentialQuantifierSym sym, Term variableTerm) {

        super(ExpressionType.EXISTENTIAL_OP, sym, true);
        this.variableTerm = variableTerm;
    }

    public Term getVariableTerm() {
        return variableTerm;
    }

    @Override
    public TextFlow toTextFlow() {
        List texts = new ArrayList();
        texts.addAll(getMainSymbol().toTextFlow().getChildren());
        texts.add(variableTerm.toTextFlow().getChildren());
        Text[] txtArray = new Text[texts.size()];
        TextFlow textFlow = new TextFlow(txtArray);
        return textFlow;
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

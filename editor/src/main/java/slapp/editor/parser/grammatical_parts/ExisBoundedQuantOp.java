package slapp.editor.parser.grammatical_parts;

import javafx.scene.text.Text;
import slapp.editor.parser.Expression;
import slapp.editor.parser.ExpressionType;
import slapp.editor.parser.symbols.CloseBracket;
import slapp.editor.parser.symbols.OpenBracket;
import slapp.editor.parser.symbols.RelationSymbol;
import slapp.editor.parser.symbols.UniversalQuantifierSym;

import java.util.ArrayList;
import java.util.List;

public class ExisBoundedQuantOp extends Operator implements Expression {

    private OpenBracket openBracket;
    private CloseBracket closeBracket;
    private ExistentialOp existentialOp;
    private RelationSymbol infixRelation;
    private Term boundingTerm;

    public ExisBoundedQuantOp(OpenBracket openBracket, CloseBracket closeBracket, ExistentialOp existentialOp, RelationSymbol infixRelation, Term boundingTerm) {
        super(ExpressionType.EXIS_BOUNDED_OP, new UniversalQuantifierSym(""), true);
        this.openBracket = openBracket;
        this.closeBracket = closeBracket;
        this.existentialOp = existentialOp;
        this.infixRelation = infixRelation;
        this.boundingTerm = boundingTerm;
    }

    public ExistentialOp getExistentialOp() {
        return existentialOp;
    }

    public Term getBoundingTerm() {
        return boundingTerm;
    }

    @Override
    public Term getVariableTerm() {return getExistentialOp().getVariableTerm();}

    public RelationSymbol getInfixRelation() {
        return infixRelation;
    }

    @Override
    public List<Text> toTextList() {
        List<Text> texts = new ArrayList();
        texts.addAll(openBracket.toTextList());
        texts.addAll(existentialOp.toTextList());
        texts.add(new Text(" "));
        texts.addAll(infixRelation.toTextList());
        texts.add(new Text(" "));
        texts.addAll(boundingTerm.toTextList());
        texts.addAll(closeBracket.toTextList());
        return texts;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(openBracket.toString());
        sb.append(existentialOp.toString());
        sb.append(" ");
        sb.append(infixRelation.toString());
        sb.append(" ");
        sb.append(boundingTerm.toString());
        sb.append(closeBracket.toString());
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if ((o instanceof ExisBoundedQuantOp)) {
            ExisBoundedQuantOp other = (ExisBoundedQuantOp) o;
            return existentialOp.equals(other.existentialOp) && infixRelation.equals(other.infixRelation) && boundingTerm.equals(other.boundingTerm);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return existentialOp.hashCode() + infixRelation.hashCode() + boundingTerm.hashCode();
    }


}

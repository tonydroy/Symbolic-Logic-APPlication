package slapp.editor.parser.grammatical_parts;

import javafx.scene.text.Text;
import slapp.editor.parser.Expression;
import slapp.editor.parser.ExpressionType;
import slapp.editor.parser.symbols.*;

import java.util.ArrayList;
import java.util.List;

public class UnivBoundedQuantOp extends Operator implements Expression {

    private OpenBracket openBracket;
    private CloseBracket closeBracket;
    private UniversalOp universalOp;
    private RelationSymbol infixRelation;
    private Term boundingTerm;

    public UnivBoundedQuantOp(OpenBracket openBracket, CloseBracket closeBracket, UniversalOp universalOp, RelationSymbol infixRelation, Term boundingTerm) {
        super(ExpressionType.UNIV_BOUNDED_OP, new UniversalQuantifierSym(""), true);
        this.openBracket = openBracket;
        this.closeBracket = closeBracket;
        this.universalOp = universalOp;
        this.infixRelation = infixRelation;
        this.boundingTerm = boundingTerm;
    }

    public UniversalOp getUniversalOp() {
        return universalOp;
    }

    public Term getBoundingTerm() {
        return boundingTerm;
    }

    public OpenBracket getOpenBracket() {
        return openBracket;
    }

    public CloseBracket getCloseBracket() {
        return closeBracket;
    }

    @Override
    public Term getVariableTerm() {
        return getUniversalOp().getVariableTerm();
    }

    public RelationSymbol getInfixRelation() {
        return infixRelation;
    }

    @Override
    public List<Text> toTextList() {
        List<Text> texts = new ArrayList();
        texts.addAll(openBracket.toTextList());
        texts.addAll(universalOp.toTextList());
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
        sb.append(universalOp.toString());
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
        if ((o instanceof UnivBoundedQuantOp)) {
            UnivBoundedQuantOp other = (UnivBoundedQuantOp) o;
            return universalOp.equals(other.universalOp) && infixRelation.equals(other.infixRelation) && boundingTerm.equals(other.boundingTerm);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return universalOp.hashCode() + infixRelation.hashCode() + boundingTerm.hashCode();
    }


}

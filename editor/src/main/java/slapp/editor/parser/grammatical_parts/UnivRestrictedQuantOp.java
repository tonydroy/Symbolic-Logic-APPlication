package slapp.editor.parser.grammatical_parts;

import javafx.scene.text.Text;
import slapp.editor.parser.Expression;
import slapp.editor.parser.ExpressionType;
import slapp.editor.parser.symbols.CloseBracket;
import slapp.editor.parser.symbols.DividerSym;
import slapp.editor.parser.symbols.OpenBracket;
import slapp.editor.parser.symbols.UniversalQuantifierSym;

import java.util.ArrayList;
import java.util.List;

public class UnivRestrictedQuantOp extends Operator implements Expression {

    private OpenBracket openBracket;
    private CloseBracket closeBracket;
    private DividerSym divider;
    private UniversalOp universalOp;
    private Formula restrictingFormula;

    public UnivRestrictedQuantOp(OpenBracket openBracket, CloseBracket closeBracket, UniversalOp universalOp, DividerSym divider, Formula restrictingFormula) {
        super(ExpressionType.UNIV_RESTRICTED_OP, new UniversalQuantifierSym(""), true);
        this.openBracket = openBracket;
        this.closeBracket = closeBracket;
        this.divider = divider;
        this.universalOp = universalOp;
        this.restrictingFormula = restrictingFormula;
    }

    public UniversalOp getUniversalOp() {
        return universalOp;
    }

    public Formula getRestrictingFormula() {
        return restrictingFormula;
    }

    @Override
    public Term getVariableTerm() {
        return getUniversalOp().getVariableTerm();
    }

    @Override
    public List<Text> toTextList() {
        List<Text> texts = new ArrayList();
        texts.addAll(openBracket.toTextList());
        texts.addAll(universalOp.toTextList());
        texts.add(new Text(" "));
        texts.addAll(divider.toTextList());
        texts.add(new Text(" "));
        texts.addAll(restrictingFormula.toTextList());
        texts.addAll(closeBracket.toTextList());
        return texts;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(openBracket.toString());
        sb.append(universalOp.toString());
        sb.append(" ");
        sb.append(divider.toString());
        sb.append(" ");
        sb.append(restrictingFormula.toString());
        sb.append(closeBracket.toString());
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if ((o instanceof UnivRestrictedQuantOp)) {
            UnivRestrictedQuantOp other = (UnivRestrictedQuantOp) o;
            return universalOp == other.universalOp && restrictingFormula == other.restrictingFormula;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return universalOp.hashCode() + restrictingFormula.hashCode();
    }


}

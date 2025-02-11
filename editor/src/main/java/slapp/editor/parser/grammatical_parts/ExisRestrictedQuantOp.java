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

public class ExisRestrictedQuantOp extends Operator implements Expression {

    private OpenBracket openBracket;
    private CloseBracket closeBracket;
    private DividerSym divider;
    private ExistentialOp existentialOp;
    private Formula restrictingFormula;

    public ExisRestrictedQuantOp(OpenBracket openBracket, CloseBracket closeBracket, ExistentialOp existentialOp, DividerSym divider, Formula restrictingFormula) {
        super(ExpressionType.UNIV_RESTRICTED_OP, new UniversalQuantifierSym(""), true);
        this.openBracket = openBracket;
        this.closeBracket = closeBracket;
        this.divider = divider;
        this.existentialOp = existentialOp;
        this.restrictingFormula = restrictingFormula;
    }

    public ExistentialOp getExistentialOp() {
        return existentialOp;
    }

    public Formula getRestrictingFormula() {
        return restrictingFormula;
    }

    @Override
    public List<Text> toTextList() {
        List<Text> texts = new ArrayList();
        texts.addAll(openBracket.toTextList());
        texts.addAll(existentialOp.toTextList());
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
        sb.append(existentialOp.toString());
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
        if ((o instanceof ExisRestrictedQuantOp)) {
            ExisRestrictedQuantOp other = (ExisRestrictedQuantOp) o;
            return existentialOp.equals(other.existentialOp) && restrictingFormula.equals(other.restrictingFormula);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return existentialOp.hashCode() + restrictingFormula.hashCode();
    }


}

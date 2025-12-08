package slapp.editor.parser.grammatical_parts;

import slapp.editor.parser.Expression;
import slapp.editor.parser.ExpressionType;
import slapp.editor.parser.symbols.BiconditionalSym;
import slapp.editor.parser.symbols.DisjunctionSym;

public class DisjunctionOp extends Operator implements Expression {

    public DisjunctionOp(DisjunctionSym sym) {
        super(ExpressionType.DISJ_OP, sym, false);
    }

}

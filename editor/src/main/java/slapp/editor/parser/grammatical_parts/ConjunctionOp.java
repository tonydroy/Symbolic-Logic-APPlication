package slapp.editor.parser.grammatical_parts;

import slapp.editor.parser.Expression;
import slapp.editor.parser.ExpressionType;
import slapp.editor.parser.symbols.BiconditionalSym;
import slapp.editor.parser.symbols.ConjunctionSym;

public class ConjunctionOp extends Operator implements Expression {

    public ConjunctionOp(ConjunctionSym sym) {

        super(ExpressionType.CONJ_OP, sym, false);
    }

}

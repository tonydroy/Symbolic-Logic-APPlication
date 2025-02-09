package slapp.editor.parser.grammatical_parts;

import slapp.editor.parser.Expression;
import slapp.editor.parser.ExpressionType;
import slapp.editor.parser.symbols.NegationSym;

public class NegationOp extends Operator implements Expression {

    public NegationOp(NegationSym sym) {
        super(ExpressionType.NEG_OP, sym, true);
    }






}

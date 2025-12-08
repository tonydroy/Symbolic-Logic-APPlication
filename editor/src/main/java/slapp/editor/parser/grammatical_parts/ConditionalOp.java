package slapp.editor.parser.grammatical_parts;

import slapp.editor.parser.Expression;
import slapp.editor.parser.ExpressionType;
import slapp.editor.parser.symbols.BiconditionalSym;
import slapp.editor.parser.symbols.ConditionalSym;

public class ConditionalOp extends Operator implements Expression {

    public ConditionalOp(ConditionalSym sym) {

        super(ExpressionType.COND_OP, sym, false);
    }

}

package slapp.editor.parser.grammatical_parts;

import slapp.editor.parser.Expression;
import slapp.editor.parser.ExpressionType;
import slapp.editor.parser.symbols.BiconditionalSym;
import slapp.editor.parser.symbols.NegationSym;

public class BiconditionalOp extends Operator implements Expression {

    public BiconditionalOp(BiconditionalSym sym) {
        super(ExpressionType.BICOND_OP, sym, false);
    }

}

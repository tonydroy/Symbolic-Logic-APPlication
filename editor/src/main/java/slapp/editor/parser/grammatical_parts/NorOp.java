package slapp.editor.parser.grammatical_parts;

import slapp.editor.parser.Expression;
import slapp.editor.parser.ExpressionType;
import slapp.editor.parser.symbols.BiconditionalSym;
import slapp.editor.parser.symbols.NorSym;

public class NorOp extends Operator implements Expression {

    public NorOp(NorSym sym) {
        super(ExpressionType.NOR_OP, sym, false);
    }

}

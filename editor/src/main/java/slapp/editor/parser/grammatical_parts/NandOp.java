package slapp.editor.parser.grammatical_parts;

import slapp.editor.parser.Expression;
import slapp.editor.parser.ExpressionType;
import slapp.editor.parser.symbols.BiconditionalSym;
import slapp.editor.parser.symbols.NandSym;

public class NandOp extends Operator implements Expression {

    public NandOp(NandSym sym) {
        super(ExpressionType.NAND_OP, sym, false);
    }

}

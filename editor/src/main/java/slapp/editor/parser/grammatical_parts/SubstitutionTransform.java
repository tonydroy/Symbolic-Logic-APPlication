package slapp.editor.parser.grammatical_parts;

import javafx.scene.text.Text;
import slapp.editor.parser.Expression;
import slapp.editor.parser.ExpressionType;

import java.util.List;

public class SubstitutionTransform implements Expression {

    private ExpressionType type;
    private Expression exp1;
    private Expression exp2;
    private String dividerSym;

    public SubstitutionTransform(Expression exp1, Expression exp2, String dividerSym, ExpressionType type) {
        this.exp1 = exp1;
        this.exp2 = exp2;
        this.dividerSym = dividerSym;
        this.type = type;
    }

    public Expression getExp1() {
        return exp1;
    }

    public Expression getExp2() {
        return exp2;
    }

    @Override
    public ExpressionType getType() {return type;}

    @Override
    public SubstitutionTransform getMatch() { return this;}
}

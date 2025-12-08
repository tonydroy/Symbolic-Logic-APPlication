package slapp.editor.parser.grammatical_parts;

import javafx.scene.text.Text;
import slapp.editor.parser.Expression;
import slapp.editor.parser.ExpressionType;
import slapp.editor.parser.ParseUtilities;

import java.util.ArrayList;
import java.util.List;

public class SubstitutionTransform implements Expression {

    private ExpressionType type;
    private Expression exp1;
    private Expression exp2;
    private String dividerSymbol;


    public SubstitutionTransform(Expression exp1, Expression exp2, ExpressionType type, String dividerSymbol) {
        this.exp1 = exp1;
        this.exp2 = exp2;
        this.type = type;
        this.dividerSymbol = dividerSymbol;
    }

    public Expression getExp1() {
        return exp1;
    }

    public Expression getExp2() {
        return exp2;
    }

    public String getDividerSymbol() {
        return dividerSymbol;
    }

    @Override
    public ExpressionType getType() {return type;}

    @Override
    public SubstitutionTransform getMatch() { return this;}

    @Override
    public List<Text> toTextList() {
        List<Text> textList = new ArrayList<Text>();
        if (type == ExpressionType.ALL_TERM_SUB) {
            Text[] subSub = ParseUtilities.newSupSubText(exp1.toString(), exp2.toString());
            textList.add(subSub[0]);
            textList.add(subSub[1]);
        }
        else if (type == ExpressionType.SOME_TERM_SUB || type == ExpressionType.ONE_TERM_SUB || type == ExpressionType.ONE_FORMULA_SUB) {
            textList.add(ParseUtilities.newSuperscriptText(exp1.toString()));
            textList.add(ParseUtilities.newRegularText(dividerSymbol));
            textList.add(ParseUtilities.newSubscriptText(exp2.toString()));
        }
        return textList;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("\u2039");
        sb.append(exp1.toString());
        sb.append(dividerSymbol);
        sb.append(exp2.toString());
        sb.append("\u203a");
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o instanceof SubstitutionTransform) {
            SubstitutionTransform other = (SubstitutionTransform) o;
            boolean equals = true;
            if (!type.equals(other.type)) equals = false;
            if (!exp1.equals(other.exp1)) equals = false;
            if (!exp2.equals(other.exp2)) equals = false;
            return equals;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return type.hashCode() + exp1.hashCode() + exp2.hashCode();
    }





}

package slapp.editor.parser.grammatical_parts;

import javafx.scene.text.Text;
import slapp.editor.parser.Expression;
import slapp.editor.parser.ExpressionType;
import slapp.editor.parser.symbols.CloseBracket;
import slapp.editor.parser.symbols.OpenBracket;

import java.util.ArrayList;
import java.util.List;

public class Formula implements Expression {

    private ExpressionType type = ExpressionType.FORMULA;
    private List<Expression> children = new ArrayList<Expression>();
    boolean atomic = false;
    boolean combines = true;
    boolean negatingInfix = false;
    int level = 0;
    Operator mainOperator = null;
    OpenBracket openBracket = new OpenBracket("");
    CloseBracket closeBracket = new CloseBracket("");

    @Override
    public ExpressionType getType() {
        return type;
    }

    @Override
    public List<Text> toTextList() {
        List<Text> texts = new ArrayList();
        if (mainOperator == null) {
            return children.get(0).toTextList();
        }
        else if (mainOperator.isUnary()) {
            if (mainOperator.getType() == ExpressionType.NEG_OP && ((Formula) children.get(0)).isNegatingInfix()) {
                return ((InfixAtomic) children.get(0)).negatedTextList();
            }
            else {
                texts.addAll(mainOperator.toTextList());
                texts.addAll(children.get(0).toTextList());
            }
        }
        else {
            texts.addAll(openBracket.toTextList());
            texts.addAll(children.get(0).toTextList());
            texts.add(new Text(" "));
            texts.addAll(mainOperator.toTextList());
            texts.add(new Text(" "));
            texts.addAll(children.get(1).toTextList());
            texts.addAll(closeBracket.toTextList());
        }
        return texts;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (mainOperator == null) {
            return children.get(0).toString();
        }
        else if (mainOperator.isUnary()) {
            if (mainOperator.getType() == ExpressionType.NEG_OP && ((Formula) children.get(0)).isNegatingInfix()) {
                return ((InfixAtomic) children.get(0)).negatedString();
            }
            else {
                sb.append(mainOperator.toString());
                sb.append(children.get(0).toString());
            }
        }
        else {
            sb.append(openBracket.toString());
            sb.append(children.get(0).toString());
            sb.append(" ");
            sb.append(mainOperator.toString());
            sb.append(" ");
            sb.append(children.get(1).toString());
            sb.append(closeBracket.toString());
        }
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (o ==this) return true;
        if (o instanceof Formula) {
            Formula other = (Formula) o;
            boolean equals = true;
            if (!mainOperator.equals(other.mainOperator)) { equals = false;}
            if (children.size() != other.children.size()) { equals = false; }
            else for (int i = 0; i < children.size(); i++) {
                if (!children.get(i).equals(other.children.get(i))) { equals = false; }
            }
            return equals;
        }
        return false;
        }

    @Override public int hashCode() {
        int code = mainOperator.hashCode();
        for (Expression child : children) {code = code + child.hashCode();}
        return code;
    }

    @Override
    public int getLevel() {
        return level;
    }

    @Override
    public List<Expression> getChildren() {
        return children;
    }


    public void setOpenBracket(OpenBracket openBracket) {
        this.openBracket = openBracket;
    }

    public void setCloseBracket(CloseBracket closeBracket) {
        this.closeBracket = closeBracket;
    }



    public void setChildren(List<Expression> children) {
        this.children = children;
    }

    public boolean isNegatingInfix() {
        return negatingInfix;
    }

    public void setNegatingInfix(boolean negatingInfix) {
        this.negatingInfix = negatingInfix;
    }

    public boolean isAtomic() {
        return atomic;
    }

    public void setAtomic(boolean atomic) {
        this.atomic = atomic;
    }

    public boolean isCombines() {
        return combines;
    }

    public void setCombines(boolean combines) {
        this.combines = combines;
    }



    public void setLevel(int level) {
        this.level = level;
    }

    public Operator getMainOperator() {
        return mainOperator;
    }

    public void setMainOperator(Operator mainOperator) {
        this.mainOperator = mainOperator;
    }





}

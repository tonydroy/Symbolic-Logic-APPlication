package slapp.editor.parser.grammatical_parts;

import javafx.scene.text.Text;
import slapp.editor.parser.Expression;
import slapp.editor.parser.ExpressionType;
import slapp.editor.parser.symbols.FunctionSymbol;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Term implements Expression {

    private ExpressionType type = ExpressionType.TERM;
    private List<Expression> children = new ArrayList<>();
    private FunctionSymbol mainFnSymbol = new FunctionSymbol("","","",0, false);
    private int level = 0;
    private boolean combines = true;
    private TermType termType = TermType.COMPLEX;
    private SubstitutionTransform subTransform = null;

    @Override
    public int getLevel() {
        return level;
    }

    @Override
    public List<Expression> getChildren() {
        return children;
    }

    public TermType getTermType() {
        return termType;
    }

    public void setTermType(TermType termType) {
        this.termType = termType;
    }

    public void setChildren(List<Expression> children) {
        this.children = children;
    }

    public FunctionSymbol getMainFnSymbol() {
        return mainFnSymbol;
    }

    public SubstitutionTransform getSubTransform() {return subTransform;}

    public void setSubTransform(SubstitutionTransform subTransform) {this.subTransform = subTransform;}

    public void setMainFnSymbol(FunctionSymbol mainFnSymbol) {
        this.mainFnSymbol = mainFnSymbol;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public boolean isCombines() {
        return combines;
    }

    public void setCombines(boolean combines) {
        this.combines = combines;
    }

    @Override
    public ExpressionType getType() { return type; }

    @Override
    public Term getMatch() {
        Term matchTerm = new Term();
        matchTerm.setMainFnSymbol(mainFnSymbol.getMatch());
        if (subTransform != null) setSubTransform(subTransform.getMatch());
        matchTerm.setLevel(level);
        matchTerm.setCombines(combines);
        matchTerm.setTermType(termType);
        List<Expression> newChildren = new ArrayList<>();
        for (Expression child : children) {
            newChildren.add(child.getMatch());
        }
        matchTerm.setChildren(newChildren);
        return matchTerm;
    }


    @Override
    public List<Text> toTextList() {
        List<Text> texts = new ArrayList();
        texts.addAll(mainFnSymbol.toTextList());
        for (Expression expression : children) {
           texts.addAll(expression.toTextList());
        }
        if (subTransform != null && isCombines()) {texts.addAll(subTransform.toTextList());}
        return texts;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(mainFnSymbol.toString());
        for (Expression childExp : children) {

            sb.append(childExp.toString());
        }
        if (subTransform != null && isCombines()) { sb.append(subTransform.toString());}
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (o instanceof Term) {
            Term other = (Term) o;
            boolean equals = true;
            if (!mainFnSymbol.equals(other.mainFnSymbol)) { equals = false;}
            if (getSubTransform() == null) {
                if (other.getSubTransform() != null) equals = false;
            }
            else if (!getSubTransform().equals(other.getSubTransform())) {
                equals = false;
            }
            if (children.size() != other.children.size()) { equals = false; }
            else for (int i = 0; i < children.size(); i++) {
                if (!children.get(i).equals(other.children.get(i))) { equals = false; }
            }
            return equals;
        }
        return false;
    }

    @Override public int hashCode() {
        int code = mainFnSymbol.hashCode() + Objects.hashCode(subTransform);
        for (Expression child : children) {code = code + child.hashCode();}
        return code;
    }
}

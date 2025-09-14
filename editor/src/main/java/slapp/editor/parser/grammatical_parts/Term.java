package slapp.editor.parser.grammatical_parts;

import javafx.scene.text.Text;
import slapp.editor.parser.Expression;
import slapp.editor.parser.ExpressionType;
import slapp.editor.parser.symbols.CloseBracket;
import slapp.editor.parser.symbols.FunctionSymbol;
import slapp.editor.parser.symbols.OpenBracket;

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
    private OpenBracket openBracket = new OpenBracket("");
    private CloseBracket closeBracket = new CloseBracket("");
    private SubstitutionTransform subTransform = null;
    private Term matchTerm = null;
    private String semanticValue;


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

    public OpenBracket getOpenBracket() {
        return openBracket;
    }

    public void setOpenBracket(OpenBracket openBracket) {
        this.openBracket = openBracket;
    }

    public CloseBracket getCloseBracket() {
        return closeBracket;
    }

    public void setCloseBracket(CloseBracket closeBracket) {
        this.closeBracket = closeBracket;
    }

    public void setSubTransform(SubstitutionTransform subTransform) {
        this.subTransform = subTransform;
    }

    public SubstitutionTransform getSubTransform() {
        return subTransform;
    }

    public void setMatchTerm(Term matchTerm) {
        this.matchTerm = matchTerm;
    }

    public String getSemanticValue() {
        return semanticValue;
    }

    public void setSemanticValue(String semanticValue) {
        this.semanticValue = semanticValue;
    }

    @Override
    public ExpressionType getType() { return type; }

    @Override
    public Term getMatch() {
        if (matchTerm != null) {
            return matchTerm;
        }
        else {
            List<Expression> newChildren = new ArrayList<>();
            for (Expression child : children) {
                if (child.getMatch() == null) return null;
                else newChildren.add(child.getMatch());
            }
            Term newTerm = new Term();
            if (mainFnSymbol != null) newTerm.setMainFnSymbol(mainFnSymbol.getMatch());
            newTerm.setLevel(level);
            newTerm.setCombines(combines);
            newTerm.setTermType(termType);
            newTerm.setChildren(newChildren);
            return newTerm;
        }
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

            if (childExp != null) sb.append(childExp.toString());
        }
        if (subTransform != null && isCombines()) sb.append(subTransform.toString());
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (o instanceof Term) {
            Term other = (Term) o;
            boolean equals = true;
            if (!mainFnSymbol.equals(other.mainFnSymbol)) { equals = false;}

            if (subTransform == null) {
                if (other.subTransform != null) { equals = false;}
            }
            else if (!subTransform.equals(other.subTransform)) { equals = false;}

            if (children.size() != other.children.size()) { equals = false; }
            else if (children.size() > 0) {
                for (int i = 0; i < children.size(); i++) {
                    if (!children.get(i).equals(other.children.get(i))) { equals = false; }
                }
            }
            return equals;
        }
        return false;
    }

    @Override public int hashCode() {
        int code = mainFnSymbol.hashCode() + Objects.hashCode(subTransform) ;
        for (Expression child : children) {code = code + child.hashCode();}
        return code;
    }
}

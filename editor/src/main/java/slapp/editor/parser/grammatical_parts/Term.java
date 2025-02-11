package slapp.editor.parser.grammatical_parts;

import javafx.scene.text.Text;
import slapp.editor.parser.Expression;
import slapp.editor.parser.ExpressionType;
import slapp.editor.parser.symbols.FunctionSymbol;

import java.util.ArrayList;
import java.util.List;

public class Term implements Expression {

    private ExpressionType type = ExpressionType.TERM;
    private List<Expression> children = new ArrayList<>();
    private FunctionSymbol mainFnSymbol = new FunctionSymbol("","","",0, false);
    private int level = 0;
    private boolean combines = true;
    private TermType termType = TermType.COMPLEX;

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

    @Override
    public ExpressionType getType() { return type; }

    @Override
    public List<Text> toTextList() {
        List<Text> texts = new ArrayList();
        texts.addAll(mainFnSymbol.toTextList());
        for (Expression expression : children) {
           texts.addAll(expression.toTextList());
        }
        return texts;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(mainFnSymbol.toString());
        for (Expression childExp : children) {

            sb.append(childExp.toString());
        }
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (o instanceof Term) {
            Term other = (Term) o;
            boolean equals = true;
            if (!mainFnSymbol.equals(other.mainFnSymbol)) { equals = false;}
            if (children.size() != other.children.size()) { equals = false; }
            else for (int i = 0; i < children.size(); i++) {
                if (!children.get(i).equals(other.children.get(i))) { equals = false; }
            }
            return equals;
        }
        return false;
    }

    @Override public int hashCode() {
        int code = mainFnSymbol.hashCode();
        for (Expression child : children) {code = code + child.hashCode();}
        return code;
    }





}

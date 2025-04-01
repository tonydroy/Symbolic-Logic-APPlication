package slapp.editor.parser.grammatical_parts;

import javafx.scene.text.Text;
import slapp.editor.parser.Expression;
import slapp.editor.parser.TextMessageException;
import slapp.editor.parser.symbols.MFormulaSym;

import java.util.ArrayList;
import java.util.List;

public class MComplexFormula extends Formula{

    private MFormulaSym formulaSym;


    public MComplexFormula(MFormulaSym formulaSym) {
        super();
        this.formulaSym = formulaSym;
    }

    public MFormulaSym getFormulaSym() {
        return formulaSym;
    }


    @Override
    public Formula getMatch() {
        List<Expression> newChildren = new ArrayList<>();
        for (Expression child : getChildren()) {
            if (child.getMatch() == null) { return null; }
            else newChildren.add(child.getMatch());
        }
        MComplexFormula formula = new MComplexFormula(formulaSym.getMatch());
        formula.setChildren(newChildren);
        formula.setLevel(getLevel());
        return formula;
    }

    @Override
    public List<Text> toTextList() {
        List<Text> texts = new ArrayList<>();
        texts.addAll(formulaSym.toTextList());
        if (!getChildren().isEmpty()) {
            texts.addAll(getOpenBracket().toTextList());
            for (int i = 0; i < getChildren().size(); i++) {
                texts.addAll(getChildren().get(i).toTextList());
                if (i < getChildren().size() - 1) {texts.add(new Text(", "));}
            }
            texts.addAll(getCloseBracket().toTextList());
        }
        return texts;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(formulaSym.toString());
        if (!getChildren().isEmpty()) {
            sb.append(getOpenBracket().toString());
            for (int i = 0; i < getChildren().size(); i++) {
                sb.append(getChildren().get(i).toString());
                if (i < getChildren().size() - 1) {sb.append(", ");}
            }
            sb.append(getCloseBracket().toString());
        }

        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o instanceof MComplexFormula) {
            MComplexFormula other = (MComplexFormula) o;

            boolean equals = true;
            if (!formulaSym.equals(other.formulaSym)) { equals = false; }
            if (getChildren().size() != other.getChildren().size()) { equals = false; }
            else for (int i = 0; i < other.getChildren().size(); i++) {
                if (!getChildren().get(i).equals(other.getChildren().get(i))) { equals = false; }
            }
            return equals;
        }
        return false;
    }

    @Override
    public int hashCode() {
        int code = formulaSym.hashCode();
        for (Expression child : getChildren()) { code = code + child.hashCode(); }
        return code;
    }




}

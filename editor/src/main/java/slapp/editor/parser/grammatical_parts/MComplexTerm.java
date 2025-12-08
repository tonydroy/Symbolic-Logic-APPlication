package slapp.editor.parser.grammatical_parts;

import javafx.scene.text.Text;
import slapp.editor.parser.Expression;
import slapp.editor.parser.symbols.MFormulaSym;
import slapp.editor.parser.symbols.MTermSym;
import slapp.editor.parser.symbols.PseudoMTermSym;

import java.util.ArrayList;
import java.util.List;

public class MComplexTerm extends Term{

    private PseudoMTermSym pMTermSym;


    public MComplexTerm(PseudoMTermSym pTermSym) {
        super();
        this.pMTermSym = pTermSym;
    }

    public PseudoMTermSym getPMTermSym() {
        return pMTermSym;
    }


    @Override
    public Term getMatch() {
        List<Expression> newChildren = new ArrayList<>();
        for (Expression child : getChildren()) {
            if (child.getMatch() == null) { return null; }
            else newChildren.add(child.getMatch());
        }
        MComplexTerm term = new MComplexTerm(pMTermSym.getMatch());
        term.setChildren(newChildren);
        term.setLevel(getLevel());
        return term;
    }

    @Override
    public List<Text> toTextList() {
        List<Text> texts = new ArrayList<>();
        texts.addAll(pMTermSym.toTextList());
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
        sb.append(pMTermSym.toString());
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
        if (o instanceof MComplexTerm) {
            MComplexTerm other = (MComplexTerm) o;

            boolean equals = true;
            if (!pMTermSym.equals(other.pMTermSym)) { equals = false; }
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
        int code = pMTermSym.hashCode();
        for (Expression child : getChildren()) { code = code + child.hashCode(); }
        return code;
    }




}

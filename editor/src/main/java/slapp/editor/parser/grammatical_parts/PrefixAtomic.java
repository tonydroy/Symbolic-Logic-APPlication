package slapp.editor.parser.grammatical_parts;

import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import slapp.editor.parser.Expression;
import slapp.editor.parser.symbols.RelationSymbol;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class PrefixAtomic extends Formula implements Expression {
    RelationSymbol mainRelation;

    public PrefixAtomic(RelationSymbol mainRelation) {
        super();
        setAtomic(true);
        this.mainRelation = mainRelation;
    }


    public RelationSymbol getMainRelation() {return mainRelation;}

    @Override
    public PrefixAtomic getMatch() {
        PrefixAtomic newAtomic = new PrefixAtomic(mainRelation.getMatch());
        List<Expression> newChildren = new ArrayList<>();
        for (Expression child : getChildren()) {
            newChildren.add(child.getMatch());
        }
        newAtomic.setChildren(newChildren);
        newAtomic.setLevel(getLevel());
        return newAtomic;
    }

    @Override
    public List<Text> toTextList() {
        List<Text> texts = new ArrayList();
        texts.addAll(mainRelation.toTextList());
        for (Expression child : getChildren()) {
            texts.addAll(child.toTextList());
        }
        return texts;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (mainRelation != null) {sb.append(mainRelation.toString());}
        for (Expression child : getChildren()) {
            if (child != null) sb.append(child.toString());
        }
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (o instanceof PrefixAtomic) {
            PrefixAtomic other = (PrefixAtomic) o;
            boolean equals = true;
            if (!mainRelation.equals(other.mainRelation)) { equals = false;}
            if (getSubTransform() == null) {
                if (other.getSubTransform() != null) equals = false;
            }
            else if (!getSubTransform().equals(other.getSubTransform())) {
                equals = false;
            }
            if (getChildren().size() != other.getChildren().size()) { equals = false; }
            else for (int i = 0; i < getChildren().size(); i++) {
                if (!getChildren().get(i).equals(other.getChildren().get(i))) { equals = false; }
            }
            return equals;
        }
        return false;
    }

    @Override public int hashCode() {
        int code = mainRelation.hashCode() + Objects.hashCode(getSubTransform());
        for (Expression child : getChildren()) {code = code + child.hashCode();}
        return code;
    }
}

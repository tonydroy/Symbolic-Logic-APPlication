package slapp.editor.parser.grammatical_parts;

import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import slapp.editor.parser.Expression;
import slapp.editor.parser.symbols.RelationSymbol;

import java.util.ArrayList;
import java.util.List;

public class PrefixAtomic extends Formula implements Expression {
    RelationSymbol mainRelation;

    public PrefixAtomic(RelationSymbol mainRelation) {
        super();
        setAtomic(true);
        this.mainRelation = mainRelation;
    }

    @Override
    public TextFlow toTextFlow() {
        List texts = new ArrayList();
        texts.addAll(mainRelation.toTextFlow().getChildren());
        for (Expression child : getChildren()) {
            texts.addAll(child.toTextFlow().getChildren());
        }
        Text[] txtArray = new Text[texts.size()];
        TextFlow textFlow = new TextFlow(txtArray);
        return textFlow;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(mainRelation.toString());
        for (Expression child : getChildren()) {
            sb.append(child.toString());
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
            if (getChildren().size() != other.getChildren().size()) { equals = false; }
            for (int i = 0; i < getChildren().size(); i++) {
                if (!getChildren().get(i).equals(other.getChildren().get(i))) { equals = false; }
            }
            return equals;
        }
        return false;
    }

    @Override public int hashCode() {
        int code = mainRelation.hashCode();
        for (Expression child : getChildren()) {code = code + child.hashCode();}
        return code;
    }
}

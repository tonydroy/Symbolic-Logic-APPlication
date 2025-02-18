package slapp.editor.parser.grammatical_parts;

import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import slapp.editor.parser.Expression;
import slapp.editor.parser.symbols.CloseBracket;
import slapp.editor.parser.symbols.OpenBracket;
import slapp.editor.parser.symbols.RelationSymbol;

import java.util.ArrayList;
import java.util.List;

public class InfixAtomic extends Formula implements Expression {
    private OpenBracket openBracket = new OpenBracket("");
    private CloseBracket closeBracket = new CloseBracket("");
    private RelationSymbol mainRelation;
    private RelationSymbol complementRelation = null;

    public InfixAtomic(RelationSymbol mainRelation, RelationSymbol complementRelation, boolean negating) {
        super();
        setAtomic(true);
        setNegatingInfix(negating);
        this.mainRelation = mainRelation;
        this.complementRelation = complementRelation;
    }

    public RelationSymbol getMainRelation() { return mainRelation;   }
    public void setCloseBracket(CloseBracket closeBracket) {
        this.closeBracket = closeBracket;
    }

    public CloseBracket getCloseBracket() {
        return closeBracket;
    }

    public void setOpenBracket(OpenBracket openBracket) {
        this.openBracket = openBracket;
    }

    public OpenBracket getOpenBracket() {
        return openBracket;
    }

    @Override
    public List<Text> toTextList() {
        List<Text> texts = new ArrayList();
        texts.addAll(openBracket.toTextList());
        texts.addAll(getChildren().get(0).toTextList());
        texts.add(new Text(" "));
        texts.addAll(mainRelation.toTextList());
        texts.add(new Text(" "));
        texts.addAll(getChildren().get(1).toTextList());
        texts.addAll(closeBracket.toTextList());
        return texts;
    }

    public List<Text> negatedTextList() {
        List<Text> texts = new ArrayList();
        texts.addAll(openBracket.toTextList());
        texts.addAll(getChildren().get(0).toTextList());
        texts.add(new Text(" "));
        texts.addAll(complementRelation.toTextList());
        texts.add(new Text(" "));
        texts.addAll(getChildren().get(1).toTextList());
        texts.addAll(closeBracket.toTextList());
        return texts;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(openBracket.toString());
        sb.append(getChildren().get(0).toString());
        sb.append(" ");
        sb.append(mainRelation.toString());
        sb.append(" ");
        sb.append(getChildren().get(1).toString());
        sb.append(closeBracket.toString());

        return sb.toString();
    }

    public String negatedString() {
        StringBuilder sb = new StringBuilder();
        sb.append(openBracket.toString());
        sb.append(getChildren().get(0).toString());
        sb.append(" ");
        sb.append(complementRelation.toString());
        sb.append(" ");
        sb.append(getChildren().get(1).toString());
        sb.append(closeBracket.toString());

        return sb.toString();
    }



    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (o instanceof InfixAtomic) {
            InfixAtomic other = (InfixAtomic) o;

            boolean equals = true;

            if (!mainRelation.equals(other.mainRelation)) { equals = false;}
            if (mainOperator == null) {
                if (other.mainOperator != null) { equals = false;}
            }
            else if (!mainOperator.equals(other.mainOperator)) { equals = false;}
            if (getChildren().size() != other.getChildren().size()) { equals = false; }
            else for (int i = 0; i < getChildren().size(); i++) {
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

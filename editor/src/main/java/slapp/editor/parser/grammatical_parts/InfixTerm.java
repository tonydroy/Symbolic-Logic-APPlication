package slapp.editor.parser.grammatical_parts;

import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import slapp.editor.parser.Expression;
import slapp.editor.parser.symbols.CloseBracket;
import slapp.editor.parser.symbols.OpenBracket;

import java.util.ArrayList;
import java.util.List;

public class InfixTerm extends Term implements Expression {

    private OpenBracket openBracket;
    private CloseBracket closeBracket;


    @Override
    public InfixTerm getMatch() {
        InfixTerm newTerm = new InfixTerm();
        newTerm.setOpenBracket(openBracket.getMatch());
        newTerm.setCloseBracket(closeBracket.getMatch());
        newTerm.setMainFnSymbol(getMainFnSymbol().getMatch());
        newTerm.setLevel(getLevel());
        newTerm.setCombines(isCombines());
        List<Expression> newChildren = new ArrayList<>();
        for (Expression child : getChildren()) {
            newChildren.add(child.getMatch());
        }
        newTerm.setChildren(newChildren);
        return newTerm;
    }



    @Override
    public List<Text> toTextList() {
        List<Text> texts = new ArrayList();
        texts.addAll(openBracket.toTextList());
        texts.addAll(getChildren().get(0).toTextList());
        texts.add(new Text(" "));
        texts.addAll(getMainFnSymbol().toTextList());
        texts.add(new Text(" "));
        texts.addAll(getChildren().get(1).toTextList());
        texts.addAll(closeBracket.toTextList());
        return texts;
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

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(openBracket.toString());
        sb.append(getChildren().get(0).toString());
        sb.append(" ");
        sb.append(getMainFnSymbol().toString());
        sb.append(" ");
        sb.append(getChildren().get(1).toString());
        sb.append(closeBracket.toString());

        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (o instanceof InfixTerm) {
            InfixTerm other = (InfixTerm) o;
            boolean equals = true;
            if (!getMainFnSymbol().equals(other.getMainFnSymbol())) { equals = false;}
            if (getChildren().size() != other.getChildren().size()) { equals = false; }
            for (int i = 0; i < getChildren().size(); i++) {
                if (!getChildren().get(i).equals(other.getChildren().get(i))) { equals = false; }
            }
            return equals;
        }
        return false;
    }

    @Override public int hashCode() {
        int code = getMainFnSymbol().hashCode();
        for (Expression child : getChildren()) {code = code + child.hashCode();}
        return code;
    }





}

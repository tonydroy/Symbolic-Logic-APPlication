package slapp.editor.parser.symbols;

import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import slapp.editor.parser.Expression;
import slapp.editor.parser.ExpressionType;
import slapp.editor.parser.ParseUtilities;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class RelationSymbol implements Expression {

    private ExpressionType type = ExpressionType.RELATION_SYMBOL;
    private String baseStr;
    private String subscriptStr;
    private String superscriptStr;
    private int places;
    private boolean permitInfix = false;
    private RelationSymbol complementSymbol = null;

public RelationSymbol(String baseStr, String subscriptStr, String superscriptStr, int places) {
        this.baseStr = baseStr;
        this.subscriptStr = subscriptStr;
        this.superscriptStr = superscriptStr;
        this.places = places;
    }

    public RelationSymbol getComplementSymbol() {
        return complementSymbol;
    }

    public void setComplementSymbol(RelationSymbol complementSymbol) {
        this.complementSymbol = complementSymbol;
    }

    public void setType(ExpressionType type) {
        this.type = type;
    }

    public int getPlaces() {
        return places;
    }

    public boolean isPermitInfix() {
        return permitInfix;
    }

    public void setPermitInfix(boolean permitInfix) {
        this.permitInfix = permitInfix;
    }

    @Override
    public ExpressionType getType() { return type; }

    @Override
    public List<Text> toTextList() {
        List<Text> textList = new ArrayList<>();
        if (!baseStr.isEmpty()) textList.add(ParseUtilities.newRegularText(baseStr));
        if (!superscriptStr.isEmpty() && subscriptStr.isEmpty()) textList.add(ParseUtilities.newSuperscriptText(superscriptStr));
        if (superscriptStr.isEmpty() && !subscriptStr.isEmpty()) textList.add(ParseUtilities.newSubscriptText(subscriptStr));
        if (!superscriptStr.isEmpty() && !subscriptStr.isEmpty()) textList.addAll(Arrays.asList(ParseUtilities.newSupSubText(superscriptStr, subscriptStr)));
        return textList;
    }

    @Override
    public String toString() {
        String endString = "";
        if (!superscriptStr.isEmpty() && subscriptStr.isEmpty()) {endString = "^" + superscriptStr;}
        if (superscriptStr.isEmpty() && !subscriptStr.isEmpty()) {endString = "_" + subscriptStr;}
        if (!superscriptStr.isEmpty() && !subscriptStr.isEmpty()) {endString = "^" + superscriptStr + "_" + subscriptStr;}
        return baseStr + endString;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (o instanceof RelationSymbol) {
            RelationSymbol other = (RelationSymbol) o;
            return baseStr.equals(other.baseStr) && places == other.places && subscriptStr.equals(other.subscriptStr) && superscriptStr.equals(other.superscriptStr);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return baseStr.hashCode() + places + subscriptStr.hashCode() + superscriptStr.hashCode();
    }

}

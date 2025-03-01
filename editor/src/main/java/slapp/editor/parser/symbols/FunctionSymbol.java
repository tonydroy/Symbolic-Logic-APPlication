package slapp.editor.parser.symbols;

import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import slapp.editor.parser.Expression;
import slapp.editor.parser.ExpressionType;
import slapp.editor.parser.ParseUtilities;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FunctionSymbol implements Expression {

    private ExpressionType type = ExpressionType.FUNCTION_SYMBOL;
    private String baseStr;
    private String subscriptStr;
    private String superscriptStr;
    private int places;
    private boolean permitInfix = false;

    public FunctionSymbol(String baseStr, String subscriptStr, String superscriptStr, int places, boolean permitInfix) {
        this.baseStr = baseStr;
        this.subscriptStr = subscriptStr;
        this.superscriptStr = superscriptStr;
        this.places = places;
        this.permitInfix = permitInfix;
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

    public String getBaseStr() {
        return baseStr;
    }

    public String getSubscriptStr() {
        return subscriptStr;
    }

    public String getSuperscriptStr() {
        return superscriptStr;
    }

    @Override
    public ExpressionType getType() { return type; }

    @Override
    public Expression getMatch() {return this;}

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
        if ((o instanceof FunctionSymbol)) {
            FunctionSymbol other = (FunctionSymbol) o;
            return baseStr.equals(other.baseStr) && places == other.places && subscriptStr.equals(other.subscriptStr) && superscriptStr.equals(other.superscriptStr);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return baseStr.hashCode() + places + subscriptStr.hashCode() + superscriptStr.hashCode();
    }


}

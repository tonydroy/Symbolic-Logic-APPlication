package slapp.editor.parser.symbols;

import javafx.scene.text.Text;
import slapp.editor.parser.Expression;
import slapp.editor.parser.ExpressionType;
import slapp.editor.parser.ParseUtilities;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class VariableSym implements Expression {

    private ExpressionType type = ExpressionType.VARIABLE_SYM;
    private String baseStr;
    private String subscriptStr = "";
    private String superscriptStr = "";


    public VariableSym(String baseSymbol, String subscriptStr) {
        this.baseStr = baseSymbol;
        this.subscriptStr = subscriptStr;
    }

    public String getBaseStr() {
        return baseStr;
    }

    public String getSuperscriptStr() {
        return superscriptStr;
    }

    public void setSuperscriptStr(String superscriptStr) {
        this.superscriptStr = superscriptStr;
    }

    public String getSubscriptStr() {
        return subscriptStr;
    }

    @Override
    public ExpressionType getType() { return type; }

    @Override 
    public Expression getMatch() { return this; }

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
        if ((o instanceof VariableSym)) {
            VariableSym other = (VariableSym) o;
            return baseStr.equals(other.baseStr) && subscriptStr.equals(other.subscriptStr) && superscriptStr.equals(other.superscriptStr);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return baseStr.hashCode() + subscriptStr.hashCode() + superscriptStr.hashCode();
    }


}

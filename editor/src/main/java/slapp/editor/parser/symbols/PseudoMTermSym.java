package slapp.editor.parser.symbols;

import javafx.scene.text.Text;
import slapp.editor.parser.Expression;
import slapp.editor.parser.ExpressionType;
import slapp.editor.parser.ParseUtilities;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PseudoMTermSym implements Expression {

    private ExpressionType type = ExpressionType.PMTERM_SYM;
    private String baseStr;
    private String subscriptStr;
    private String superscriptStr;

    public PseudoMTermSym(String baseStr, String subscriptStr, String superscriptStr) {
        this.baseStr = baseStr;
        this.subscriptStr = subscriptStr;
        this.superscriptStr = superscriptStr;
    }

    public String getSuperscriptStr() {
        return superscriptStr;
    }

    public String getSubscriptStr() {
        return subscriptStr;
    }

    public String getBaseStr() {
        return baseStr;
    }

    public void setType(ExpressionType type) {
        this.type = type;
    }



    @Override
    public ExpressionType getType() { return type; }

    @Override
    public PseudoMTermSym getMatch() {return this;}

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
        if (o instanceof PseudoMTermSym) {
            PseudoMTermSym other = (PseudoMTermSym) o;
            return baseStr.equals(other.baseStr) && subscriptStr.equals(other.subscriptStr) && superscriptStr.equals(other.superscriptStr);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return baseStr.hashCode() + subscriptStr.hashCode() + superscriptStr.hashCode();
    }

}

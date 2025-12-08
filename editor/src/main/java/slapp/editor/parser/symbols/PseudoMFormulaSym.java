package slapp.editor.parser.symbols;

import javafx.scene.text.Text;
import slapp.editor.parser.Expression;
import slapp.editor.parser.ExpressionType;
import slapp.editor.parser.ParseUtilities;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PseudoMFormulaSym implements Expression {

    private ExpressionType type = ExpressionType.PMFORMULA_SYM;
    private String baseStr;
    private String subscriptStr;


public PseudoMFormulaSym(String baseStr, String subscriptStr) {
        this.baseStr = baseStr;
        this.subscriptStr = subscriptStr;
    }

    public String getSubscriptStr() {
        return subscriptStr;
    }

    public String getBaseStr() {
        return baseStr;
    }

    @Override
    public ExpressionType getType() { return type; }

    @Override
    public PseudoMFormulaSym getMatch() {return this;}

    @Override
    public List<Text> toTextList() {
        List<Text> textList = new ArrayList<>();
        if (!baseStr.isEmpty()) textList.add(ParseUtilities.newRegularText(baseStr));
        if (!subscriptStr.isEmpty()) textList.add(ParseUtilities.newSubscriptText(subscriptStr));
        return textList;
    }

    @Override
    public String toString() {
        String endString = "";
        if (!subscriptStr.isEmpty()) {endString = "_" + subscriptStr;}
        return baseStr + endString;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (o instanceof PseudoMFormulaSym) {
            PseudoMFormulaSym other = (PseudoMFormulaSym) o;
            return baseStr.equals(other.baseStr) && subscriptStr.equals(other.subscriptStr);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return baseStr.hashCode() + subscriptStr.hashCode();
    }

}

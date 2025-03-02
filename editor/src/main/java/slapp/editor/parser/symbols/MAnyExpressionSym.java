package slapp.editor.parser.symbols;

import javafx.scene.text.Text;
import slapp.editor.parser.Expression;
import slapp.editor.parser.ExpressionType;
import slapp.editor.parser.ParseUtilities;

import java.util.ArrayList;
import java.util.List;

public class MAnyExpressionSym implements Expression {

    private ExpressionType type = ExpressionType.MEXPRESSION_SYM;
    private String baseStr;
    private String subscriptStr;

    public MAnyExpressionSym(String baseStr, String subscriptStr) {
        this.baseStr = baseStr;
        this.subscriptStr = subscriptStr;
    }

    public String getBaseStr() {
        return baseStr;
    }

    public String getSubscriptStr() {
        return subscriptStr;
    }

    @Override
    public ExpressionType getType() { return type; }

    @Override
    public MAnyExpressionSym getMatch() { return this; }

    @Override
    public List<Text> toTextList() {
        List<Text> textList = new ArrayList<>();
        if(!baseStr.isEmpty()) textList.add(ParseUtilities.newRegularText(baseStr));
        if(!subscriptStr.isEmpty()) textList.add(ParseUtilities.newSubscriptText(subscriptStr));
        return textList;
    }

    @Override
    public String toString() {
        String endstring = "";
        if (!subscriptStr.isEmpty()) { endstring = "_" + subscriptStr; }
        return baseStr + endstring;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if ((o instanceof MAnyExpressionSym)) {
            MAnyExpressionSym other = (MAnyExpressionSym) o;
            return baseStr.equals(other.baseStr) && subscriptStr.equals(other.subscriptStr);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return baseStr.hashCode() + subscriptStr.hashCode();
    }
}

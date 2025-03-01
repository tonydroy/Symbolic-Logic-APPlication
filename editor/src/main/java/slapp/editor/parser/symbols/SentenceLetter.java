package slapp.editor.parser.symbols;

import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import slapp.editor.parser.Expression;
import slapp.editor.parser.ExpressionType;
import slapp.editor.parser.ParseUtilities;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class SentenceLetter implements Expression {

    private ExpressionType type = ExpressionType.SENTENCE_LETTER;
    private String baseStr;
    private String subscriptStr = "";


    public SentenceLetter(String baseSymbol, String subscriptSymbol) {
        this.baseStr = baseSymbol;
        this.subscriptStr = subscriptSymbol;
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
    public Expression getMatch() {return this;}

    @Override
    public List<Text> toTextList() {
        List<Text> textList = new ArrayList<Text>();
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
        if ((o instanceof SentenceLetter)) {
            SentenceLetter other = (SentenceLetter) o;
            return baseStr.equals(other.baseStr) && subscriptStr.equals(other.subscriptStr);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return baseStr.hashCode() + subscriptStr.hashCode();
    }
}

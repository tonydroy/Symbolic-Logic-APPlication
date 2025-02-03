package slapp.editor.parser.symbols;

import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import slapp.editor.parser.Expression;
import slapp.editor.parser.ExpressionType;
import slapp.editor.parser.ParseUtilities;

public class SentenceLetter implements Expression {

    private ExpressionType type = ExpressionType.SENTENCE_LETTER;
    private String baseStr;
    private String subscriptStr = "";


    public SentenceLetter(String baseSymbol, String subscriptSymbol) {
        this.baseStr = baseSymbol;
        this.subscriptStr = subscriptSymbol;
    }

    @Override
    public ExpressionType getType() { return type; }

    @Override
    public TextFlow toTextFlow() {
        Text baseText = ParseUtilities.newRegularText(baseStr);
        Text subText = null;
        if (!subscriptStr.isEmpty()) subText = ParseUtilities.newSubscriptText(subscriptStr);
        return new TextFlow(baseText, subText);
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

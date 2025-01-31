package slapp.editor.parser.symbols;

import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import slapp.editor.parser.Expression;
import slapp.editor.parser.ExpressionType;
import slapp.editor.parser.ParseUtilities;

public class Variable implements Expression {

    private ExpressionType type;
    private String baseSymbol;
    private String subscriptSymbol = "";

    Variable(String baseSymbol, String subscriptSymbol) {
        this.baseSymbol = baseSymbol;

        this.subscriptSymbol = subscriptSymbol;
    }

    @Override
    public ExpressionType getType() { return type; }

    @Override
    public String toString() {
        String endString = "";
        if (!subscriptSymbol.isEmpty()) {endString = "_" + subscriptSymbol;}
        return baseSymbol + endString;
    }

    @Override
    public TextFlow toTextFlow() {
        Text baseText = ParseUtilities.newRegularText(baseSymbol);
        Text subText = ParseUtilities.newSubscriptText(subscriptSymbol);
        return new TextFlow(baseText, subText);
    }



}

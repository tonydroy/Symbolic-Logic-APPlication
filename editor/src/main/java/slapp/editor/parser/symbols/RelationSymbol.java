package slapp.editor.parser.symbols;

import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import slapp.editor.parser.Expression;
import slapp.editor.parser.ExpressionType;
import slapp.editor.parser.ParseUtilities;

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
    public TextFlow toTextFlow() {
        Text baseText = ParseUtilities.newRegularText(baseStr);
        Text supText = new Text(); Text subText = new Text();
        if (!superscriptStr.isEmpty() && subscriptStr.isEmpty()) {supText = ParseUtilities.newSuperscriptText(superscriptStr);}
        if (superscriptStr.isEmpty() && !subscriptStr.isEmpty()) {subText = ParseUtilities.newSubscriptText(subscriptStr);}
        if (!superscriptStr.isEmpty() && !subscriptStr.isEmpty()) {
            Text[] supSub = ParseUtilities.newSupSubText(superscriptStr, subscriptStr);
            supText = supSub[0];
            subText = supSub[1];
        }
        return new TextFlow(baseText, supText, subText);
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

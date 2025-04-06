package slapp.editor.parser.symbols;

import javafx.scene.text.Text;
import slapp.editor.parser.Expression;
import slapp.editor.parser.ExpressionType;
import slapp.editor.parser.ParseUtilities;

import java.util.ArrayList;
import java.util.List;

public class ContradictionSymbol implements Expression {

    private ExpressionType type = ExpressionType.CONTRADICTION_SYM;
    private String baseStr;



    public ContradictionSymbol(String baseSymbol) {
        this.baseStr = baseSymbol;
    }

    public String getBaseStr() {
        return baseStr;
    }



    @Override
    public ExpressionType getType() { return type; }

    @Override
    public ContradictionSymbol getMatch() {return this;}

    @Override
    public List<Text> toTextList() {
        List<Text> textList = new ArrayList<Text>();
        if (!baseStr.isEmpty()) textList.add(ParseUtilities.newRegularText(baseStr));
        return textList;
    }

    @Override
    public String toString() {
        String endString = "";

        return baseStr;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if ((o instanceof ContradictionSymbol)) {
            ContradictionSymbol other = (ContradictionSymbol) o;
            return baseStr.equals(other.baseStr);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return baseStr.hashCode();
    }
}

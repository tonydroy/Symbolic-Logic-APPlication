package slapp.editor.parser.symbols;

import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import slapp.editor.parser.Expression;
import slapp.editor.parser.ExpressionType;
import slapp.editor.parser.ParseUtilities;
import slapp.editor.parser.grammatical_parts.Operator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static slapp.editor.parser.ExpressionType.DISJ_SYM;


public class DisjunctionSym extends OperatorSym implements Expression {

    private ExpressionType type = DISJ_SYM;
    private String textString;

    public DisjunctionSym(String textString) {
        this.textString = textString;
    }

    @Override
    public ExpressionType getType() {return type;}

    @Override
    public List<Text> toTextList() {
        List<Text> textList = new ArrayList<Text>();
        if (!textString.isEmpty()) textList.add(ParseUtilities.newRegularText(" " + textString + " "));
        return textList;
    }

    @Override
    public String toString() {
        return textString;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o instanceof DisjunctionSym) {
            DisjunctionSym other = (DisjunctionSym) o;
            return textString.equals(other.textString);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return textString.hashCode();
    }


}

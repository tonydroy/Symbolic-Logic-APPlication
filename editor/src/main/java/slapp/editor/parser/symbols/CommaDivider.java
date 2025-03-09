package slapp.editor.parser.symbols;

import javafx.scene.text.Text;
import slapp.editor.parser.Expression;
import slapp.editor.parser.ExpressionType;
import slapp.editor.parser.ParseUtilities;

import java.util.ArrayList;
import java.util.List;


public class CommaDivider implements Expression {
    private ExpressionType type = ExpressionType.COMMA_DIVIDER;
    private String textString;


    public CommaDivider(String textString) {
        this.textString = textString;
    }


    @Override
    public ExpressionType getType() {return type;}

    @Override
    public CommaDivider getMatch() {return this;}

    @Override
    public List<Text> toTextList() {
        List<Text> textList = new ArrayList<Text>();
        if (!textString.isEmpty()) textList.add(ParseUtilities.newRegularText(textString));
        return textList;
    }

    @Override
    public String toString() {
        return textString;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o instanceof CommaDivider) {
            CommaDivider other = (CommaDivider) o;
            return textString.equals(other.textString);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return textString.hashCode();
    }



}

package slapp.editor.parser.symbols;

import javafx.scene.text.Text;
import slapp.editor.parser.Expression;
import slapp.editor.parser.ExpressionType;
import slapp.editor.parser.ParseUtilities;

import java.util.ArrayList;
import java.util.List;


public class DoubleSlashDivider implements Expression {
    private ExpressionType type = ExpressionType.DOUBLE_SLASH_DIVIDER;
    private String textString;


    public DoubleSlashDivider(String textString) {
        this.textString = textString;
    }


    @Override
    public ExpressionType getType() {return type;}

    @Override
    public DoubleSlashDivider getMatch() {return this;}

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
        if (o instanceof DoubleSlashDivider) {
            DoubleSlashDivider other = (DoubleSlashDivider) o;
            return textString.equals(other.textString);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return textString.hashCode();
    }



}

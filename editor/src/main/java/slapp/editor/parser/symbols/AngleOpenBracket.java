package slapp.editor.parser.symbols;

import javafx.scene.text.Text;
import slapp.editor.parser.Expression;
import slapp.editor.parser.ExpressionType;
import slapp.editor.parser.ParseUtilities;

import java.util.ArrayList;
import java.util.List;


public class AngleOpenBracket implements Expression {
    private ExpressionType type = ExpressionType.ANGLE_OPEN_BRACKET;
    private String textString;


    public AngleOpenBracket(String textString) {this.textString = textString;
    }


    @Override
    public ExpressionType getType() {return type;}

    @Override
    public AngleOpenBracket getMatch() {return this;}

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
        if (o instanceof AngleOpenBracket) {
            AngleOpenBracket other = (AngleOpenBracket) o;
            return textString.equals(other.textString);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return textString.hashCode();
    }



}

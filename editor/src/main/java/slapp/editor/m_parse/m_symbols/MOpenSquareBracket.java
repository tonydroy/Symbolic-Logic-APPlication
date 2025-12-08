package slapp.editor.m_parse.m_symbols;

import javafx.scene.text.Text;
import slapp.editor.parser.Expression;
import slapp.editor.parser.ExpressionType;
import slapp.editor.parser.ParseUtilities;

import java.util.ArrayList;
import java.util.List;

import static slapp.editor.parser.ExpressionType.M_OPEN_SQUARE_BRACKET;
import static slapp.editor.parser.ExpressionType.M_VBAR_DIVIDER_SYM;


public class MOpenSquareBracket implements Expression {

    private ExpressionType type = M_OPEN_SQUARE_BRACKET;
    private String textString;

    public MOpenSquareBracket(String textString) {
        this.textString = textString;
    }

    @Override
    public ExpressionType getType() {return type;}

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
        if (o instanceof MOpenSquareBracket) {
            MOpenSquareBracket other = (MOpenSquareBracket) o;
            return textString.equals(other.textString);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return textString.hashCode();
    }



}

package slapp.editor.parser.symbols;

import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import slapp.editor.parser.Expression;
import slapp.editor.parser.ExpressionType;
import slapp.editor.parser.ParseUtilities;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static slapp.editor.parser.ExpressionType.CLOSE_BRACKET1;

public class CloseBracket implements Expression {

    private ExpressionType type;
    private String textString;


    public CloseBracket(String textString) {
        this.textString = textString;
    }

    public void setType(ExpressionType type) {
        this.type = type;
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
        if (o instanceof CloseBracket) {
            CloseBracket other = (CloseBracket) o;
            return textString.equals(other.textString);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return textString.hashCode();
    }

}

package slapp.editor.m_parse.m_symbols;

import javafx.scene.text.Text;
import slapp.editor.parser.Expression;
import slapp.editor.parser.ExpressionType;
import slapp.editor.parser.ParseUtilities;

import java.util.ArrayList;
import java.util.List;

public abstract class MBasicObjTerm implements Expression {
    private ExpressionType type;
    private String textString;


    @Override
    public ExpressionType getType() {
        return type;
    }

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
    public int hashCode() {
        return textString.hashCode();
    }

    public void setType(ExpressionType type) {
        this.type = type;
    }

    public void setTextString(String textString) {
        this.textString = textString;
    }
}

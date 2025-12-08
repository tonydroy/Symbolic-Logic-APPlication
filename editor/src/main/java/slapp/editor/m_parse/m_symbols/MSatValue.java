package slapp.editor.m_parse.m_symbols;

import javafx.scene.text.Text;
import slapp.editor.parser.Expression;
import slapp.editor.parser.ParseUtilities;

import java.util.ArrayList;
import java.util.List;


public abstract class MSatValue implements Expression {

    private String textString;




    public MSatValue(String textString) {
        this.textString = textString;
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



    public String getTextString() {
        return textString;
    }
}

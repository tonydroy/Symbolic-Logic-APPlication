package slapp.editor.m_parse.m_symbols;

import javafx.scene.text.Text;
import slapp.editor.parser.Expression;
import slapp.editor.parser.ExpressionType;
import slapp.editor.parser.ParseUtilities;

import java.util.ArrayList;
import java.util.List;

import static slapp.editor.parser.ExpressionType.M_EMPTY_SET;
import static slapp.editor.parser.ExpressionType.M_UNIVERSE_SYM;


public class MEmptySet implements Expression {

    private ExpressionType type = M_EMPTY_SET;
    private String openBrace;
    private String closeBrace;

    public MEmptySet(String openBrace, String closeBrace)
    {
        this.openBrace = openBrace;
        this.closeBrace = closeBrace;
    }

    @Override
    public ExpressionType getType() {return type;}

    @Override
    public List<Text> toTextList() {
        List<Text> textList = new ArrayList<Text>();
        if (!openBrace.isEmpty()) textList.add(ParseUtilities.newRegularText(openBrace));
        if (!closeBrace.isEmpty()) textList.add(ParseUtilities.newRegularText(closeBrace));
        return textList;
    }

    @Override
    public String toString() {
        return openBrace + closeBrace;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o instanceof MEmptySet) {
            MEmptySet other = (MEmptySet) o;
            return openBrace.equals(other.openBrace) && closeBrace.equals(other.closeBrace);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return openBrace.hashCode() + closeBrace.hashCode();
    }



}

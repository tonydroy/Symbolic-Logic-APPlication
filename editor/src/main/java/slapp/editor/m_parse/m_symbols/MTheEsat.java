package slapp.editor.m_parse.m_symbols;

import slapp.editor.parser.Expression;
import slapp.editor.parser.ExpressionType;

import static slapp.editor.parser.ExpressionType.M_THE_NSAT;


public class MTheEsat extends MSatValue implements Expression {

    private ExpressionType type = M_THE_NSAT;
    private String textString;

    public MTheEsat(String textString) {
        super(textString);
    }

    @Override
    public ExpressionType getType() {return type;}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o instanceof MTheEsat) {
            MTheEsat other = (MTheEsat) o;
            return getTextString().equals(other.getTextString());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return getTextString().hashCode();
    }


}

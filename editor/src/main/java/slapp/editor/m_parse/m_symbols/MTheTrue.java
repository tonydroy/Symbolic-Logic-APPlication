package slapp.editor.m_parse.m_symbols;

import javafx.scene.text.Text;
import slapp.editor.parser.Expression;
import slapp.editor.parser.ExpressionType;
import slapp.editor.parser.ParseUtilities;

import java.util.ArrayList;
import java.util.List;

import static slapp.editor.parser.ExpressionType.M_INTERP_UNIVERSE;
import static slapp.editor.parser.ExpressionType.M_THE_TRUE;


public class MTheTrue extends MTruthValue implements Expression {

    private ExpressionType type = M_THE_TRUE;


    public MTheTrue(String textString) {
        super(textString);

    }

    @Override
    public ExpressionType getType() {
        return type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o instanceof MTheTrue) {
            MTheTrue other = (MTheTrue) o;
            return getTextString().equals(other.getTextString());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return getTextString().hashCode();
    }


}
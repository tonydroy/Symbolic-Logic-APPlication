package slapp.editor.m_parse.m_symbols;

import javafx.scene.text.Text;
import slapp.editor.parser.Expression;
import slapp.editor.parser.ExpressionType;
import slapp.editor.parser.ParseUtilities;

import java.util.ArrayList;
import java.util.List;

public class MProperName extends MBasicObjTerm implements Expression {
    String nameString;


    public MProperName(String nameString) {
        this.nameString = nameString;
        setTextString(nameString);
        setType(ExpressionType.M_PROPER_NAME);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o instanceof MProperName) {
            MProperName other = (MProperName) o;
            return nameString.equals(other.nameString);
        }
        return false;
    }

}

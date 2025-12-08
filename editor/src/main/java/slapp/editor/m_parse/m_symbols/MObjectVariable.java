package slapp.editor.m_parse.m_symbols;

import slapp.editor.parser.Expression;
import slapp.editor.parser.ExpressionType;

public class MObjectVariable extends MBasicObjTerm implements Expression {
    String varString;


    public MObjectVariable(String varString) {
        this.varString = varString;
        setTextString(varString);
        setType(ExpressionType.M_BASIC_OBJ_VAR);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o instanceof MObjectVariable) {
            MObjectVariable other = (MObjectVariable) o;
            return varString.equals(other.varString);
        }
        return false;
    }

}

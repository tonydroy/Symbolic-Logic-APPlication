package slapp.editor.parser.symbols;

import slapp.editor.parser.ExpressionType;

public class ObjObjVar extends ObjVar{



public ObjObjVar(String baseSymbol) {
    super(baseSymbol);
    type = ExpressionType.OBJ_OBJ_VAR;
}

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if ((o instanceof ObjObjVar)) {
            ObjObjVar other = (ObjObjVar) o;
            return baseStr.equals(other.baseStr);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return baseStr.hashCode();
    }
}

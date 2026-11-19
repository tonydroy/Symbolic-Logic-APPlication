package slapp.editor.parser.symbols;

import slapp.editor.parser.ExpressionType;

public class ObjUnivSym extends UnivSym{



public ObjUnivSym(String baseSymbol) {
    super(baseSymbol);
    type = ExpressionType.OBJ_UNIV_SYM;
}

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if ((o instanceof ObjUnivSym)) {
            ObjUnivSym other = (ObjUnivSym) o;
            return baseStr.equals(other.baseStr);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return baseStr.hashCode();
    }
}

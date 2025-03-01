package slapp.editor.parser.symbols;

import java.util.ArrayList;
import java.util.List;

public class MConstant extends Constant {

    private static List<MConstant> constantSyms = new ArrayList<MConstant>();
    private Constant matchConstantSymbol;

    private MConstant(String baseSymbol, String subscriptSymbol) {
        super(baseSymbol, subscriptSymbol);
        constantSyms.add(this);
    }

    public static MConstant getInstance(String baseSymbol, String subscriptSymbol) {
        MConstant constSym = null;
        for (MConstant sym : constantSyms) {
            if (sym.getBaseStr().equals(baseSymbol) && sym.getSubscriptStr().equals(subscriptSymbol)) {
                constSym = sym;
                break;
            }
        }
        if (constSym == null) {constSym = new MConstant(baseSymbol, subscriptSymbol);}
        return constSym;
    }

    @Override
    public Constant getMatch() {return matchConstantSymbol;}

    public void setMatch(Constant match) {this.matchConstantSymbol = match;}

}

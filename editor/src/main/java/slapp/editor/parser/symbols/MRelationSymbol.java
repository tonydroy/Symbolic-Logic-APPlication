package slapp.editor.parser.symbols;

import java.util.ArrayList;
import java.util.List;

public class MRelationSymbol extends RelationSymbol {

    private static List<MRelationSymbol> relSymbols = new ArrayList<MRelationSymbol>();
    private RelationSymbol matchRelSymbol;

    private MRelationSymbol(String baseStr, String subscriptStr, String superscriptStr, int places) {
        super(baseStr, subscriptStr, superscriptStr, places);
        relSymbols.add(this);
    }

    public static MRelationSymbol getInstance(String baseStr, String subscriptStr, String superscriptStr, int places) {
        MRelationSymbol relSym = null;
        for (MRelationSymbol sym : relSymbols) {
            if (sym.getBaseStr().equals(baseStr) && sym.getSubscriptStr().equals(subscriptStr) && sym.getSuperscriptStr().equals(superscriptStr) && sym.getPlaces() == places) {
                relSym = sym;
                break;
            }
        }
        if (relSym == null) {relSym = new MRelationSymbol(baseStr, subscriptStr, superscriptStr, places);}
        return relSym;
    }

    @Override
    public RelationSymbol getMatch() {return matchRelSymbol;}

    public void setMatch(RelationSymbol matchRelSymbol) {this.matchRelSymbol = matchRelSymbol;}

}

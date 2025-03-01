package slapp.editor.parser.symbols;

import java.util.ArrayList;
import java.util.List;

public class MFunctionSymbol extends FunctionSymbol {

    private static List<MFunctionSymbol> fnSymbols = new ArrayList<MFunctionSymbol>();
    private FunctionSymbol matchFnSymbol;

    private MFunctionSymbol(String baseStr, String subscriptStr, String superscriptStr, int places, boolean permitInfix) {
        super(baseStr, subscriptStr, superscriptStr, places, permitInfix);
        fnSymbols.add(this);
    }

    public static MFunctionSymbol getInstance(String baseStr, String subscriptStr, String superscriptStr, int places, boolean permitInfix) {
        MFunctionSymbol fnSymbol = null;
        for (MFunctionSymbol symbol : fnSymbols) {
            if (symbol.getBaseStr().equals(baseStr) && symbol.getSubscriptStr().equals(subscriptStr) && symbol.getSuperscriptStr().equals(superscriptStr) && symbol.getPlaces() == places) {
                fnSymbol = symbol;
                break;
            }
        }
        if (fnSymbol == null) { fnSymbol = new MFunctionSymbol(baseStr, subscriptStr, superscriptStr, places, permitInfix); }
        return fnSymbol;
    }

    @Override
    public FunctionSymbol getMatch() {return matchFnSymbol;}

    public void setMatch(FunctionSymbol matchFnSymbol) {this.matchFnSymbol = matchFnSymbol;}



}

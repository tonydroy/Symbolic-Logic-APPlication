package slapp.editor.parser.symbols;

import javafx.scene.text.Text;
import slapp.editor.parser.TextMessageException;

import java.util.ArrayList;
import java.util.List;

public class MFunctionSymbol extends FunctionSymbol {

    private static List<MFunctionSymbol> fnSymbols = new ArrayList<MFunctionSymbol>();
    private FunctionSymbol matchFnSymbol = null;

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

    public static List<MFunctionSymbol> getFnSymbols() {
        return fnSymbols;
    }

    public FunctionSymbol getMatchFnSymbol() {
        return matchFnSymbol;
    }

    @Override
    public FunctionSymbol getMatch() {return matchFnSymbol;}

    public void setMatch(FunctionSymbol match) throws TextMessageException {
        if (matchFnSymbol == null) { matchFnSymbol = match; }
        else if (!matchFnSymbol.equals(match)) {
            List<Text> messageTxts = new ArrayList<>();
            messageTxts.add(new Text("Variable "));
            messageTxts.addAll(this.toTextList());
            messageTxts.add(new Text(" cannot match to both "));
            messageTxts.addAll(matchFnSymbol.toTextList());
            messageTxts.add(new Text(" and "));
            messageTxts.addAll(match.toTextList());
            messageTxts.add(new Text("."));
            throw new TextMessageException(messageTxts);
        }
    }

    public static void clear() {
        for (MFunctionSymbol fnSymbol : fnSymbols) {
            fnSymbol.clearMatch();
        }
    }

    private void clearMatch() {
        matchFnSymbol = null;
    }


}

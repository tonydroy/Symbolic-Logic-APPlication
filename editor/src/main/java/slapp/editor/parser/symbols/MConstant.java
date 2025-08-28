package slapp.editor.parser.symbols;

import javafx.scene.text.Text;
import slapp.editor.parser.TextMessageException;

import java.util.ArrayList;
import java.util.List;

public class MConstant extends Constant {

    private static List<MConstant> constantSyms = new ArrayList<MConstant>();
    private Constant matchConstant = null;

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

    public static List<MConstant> getConstantSyms() {
        return constantSyms;
    }

    public Constant getMatchConstant() {
        return matchConstant;
    }

    @Override
    public Constant getMatch() {return matchConstant;}

    public void setMatch(Constant match) throws TextMessageException {
        if (matchConstant == null) { matchConstant = match; }
        else if (!matchConstant.equals(match)) {
            List<Text> messageTxts = new ArrayList<>();
            messageTxts.add(new Text("Meta constant symbol "));
            messageTxts.addAll(this.toTextList());
            messageTxts.add(new Text(" cannot match to both "));
            messageTxts.addAll(matchConstant.toTextList());
            messageTxts.add(new Text(" and "));
            messageTxts.addAll(match.toTextList());
            messageTxts.add(new Text("."));
            throw new TextMessageException(messageTxts);
        }
    }

    public static void clear() {
        for (MConstant sym : constantSyms) {
            sym.clearMatch();
        }
    }

    private void clearMatch() {
        matchConstant = null;
    }

}

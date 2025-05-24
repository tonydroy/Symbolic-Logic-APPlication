package slapp.editor.parser.symbols;

import javafx.scene.text.Text;
import slapp.editor.parser.TextMessageException;
import java.util.ArrayList;
import java.util.List;


//just one instance for each base/subscript combination

public class MVariable extends VariableSym {

    private static List<MVariable> variables = new ArrayList<MVariable>();
    private VariableSym matchVar;
    private boolean matches;

    private MVariable(String baseSymbol, String subscriptStr, boolean matches) {
        super(baseSymbol, subscriptStr);
        variables.add(this);
        this.matches = matches;
        if (matches) matchVar = null;
        else matchVar = this;
    }

    public static MVariable getInstance(String baseSymbol, String subscriptStr, boolean matches) {
        MVariable mVar = null;
        for (MVariable var : variables) {
            if (var.getBaseStr().equals(baseSymbol) && var.getSubscriptStr().equals(subscriptStr)) {
                mVar = var;
                break;
            }
        }
        if (mVar == null) { mVar = new MVariable(baseSymbol, subscriptStr, matches); }
        return mVar;
    }

    @Override
    public VariableSym getMatch() {
       return matchVar;
    }

    public void setMatch (VariableSym match) throws TextMessageException {
        if (matchVar == null) {
            matchVar = match; }
        else if (!matchVar.equals(match)) {
            List<Text> messageTxts = new ArrayList<>();
            messageTxts.add(new Text("A variable"));
        //    messageTxts.addAll(this.toTextList());
            messageTxts.add(new Text(" cannot match to both "));
            messageTxts.addAll(matchVar.toTextList());
            messageTxts.add(new Text(" and "));
            messageTxts.addAll(match.toTextList());
            messageTxts.add(new Text("."));
            throw new TextMessageException(messageTxts);
        }
    }

    public static void clear() {
        for (MVariable var : variables) {
            var.clearMatch();
        }
    }

    private void clearMatch() {
        if (matches) matchVar = null;
    }

    public static List<MVariable> getVariables() {
        return variables;
    }

    public VariableSym getMatchVar() {
        return matchVar;
    }
}

package slapp.editor.parser.grammatical_parts;

import javafx.scene.text.Text;
import slapp.editor.parser.Expression;
import slapp.editor.parser.TextMessageException;
import slapp.editor.parser.symbols.MFormulaSym;
import slapp.editor.parser.symbols.MVariable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MFormula extends Formula{

    private static List<MFormula> mFormulas = new ArrayList<MFormula>();
    private Formula matchFormula;

    private MFormula(MFormulaSym formulaSym) {
        super();
        setChildren(Collections.singletonList((Expression) formulaSym));
        mFormulas.add(this);
    }



    public static MFormula getInstance(MFormulaSym formulaSym) {
        MFormula newFormula = null;
        for (MFormula formula : mFormulas) {
            if (formula.getChildren().get(0).equals(formulaSym)) {
                newFormula = formula;
                break;
            }
        }
        if (newFormula == null) { newFormula = new MFormula(formulaSym); }
        return newFormula;
    }



    @Override
    public Formula getMatch() { return matchFormula; }

    public void setMatch(Formula match) throws TextMessageException {
        if (matchFormula == null) matchFormula = match;
        else if (!matchFormula.equals(match)) {
            List<Text> messageTxts = new ArrayList<>();
            messageTxts.add(new Text("Variable "));
            messageTxts.addAll(this.getChildren().get(0).toTextList());
            messageTxts.add(new Text(" cannot match to both "));
            messageTxts.addAll(match.toTextList());
            messageTxts.add(new Text(" and "));
            messageTxts.addAll(matchFormula.toTextList());
            messageTxts.add(new Text("."));
            throw new TextMessageException(messageTxts);
        }
    }

    public static void clear() { mFormulas.clear(); }


}

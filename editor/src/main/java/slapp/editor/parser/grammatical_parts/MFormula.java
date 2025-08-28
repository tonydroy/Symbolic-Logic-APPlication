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

    public static List<MFormula> mFormulas = new ArrayList<MFormula>();
    private MFormulaSym formulaSym;
    private Formula matchFormula = null;


    private MFormula(MFormulaSym formulaSym) {
        super();
        this.formulaSym = formulaSym;
        mFormulas.add(this);
    }

    public static MFormula getInstance(MFormulaSym formulaSym) {
        MFormula f = null;
        for (MFormula form : mFormulas) {
            if (form.getFormulaSym().equals(formulaSym)) {
                f = form;
                break;
            }
        }
        if (f == null) { f = new MFormula(formulaSym); }
        return f;
    }

    public static void clear() {
        for (MFormula formula : mFormulas) {
            formula.clearMatch();
        }
    }



    private void clearMatch() {
        matchFormula = null;
    }

    public MFormulaSym getFormulaSym() {
        return formulaSym;
    }

    public static List<MFormula> getmFormulas() {
        return mFormulas;
    }

    @Override
    public Formula getMatch() { return matchFormula; }

    public void setMatch(Formula match) throws TextMessageException {
        if (matchFormula == null) {
            matchFormula = match;    }
        else if (!matchFormula.equals(match)) {
            List<Text> messageTxts = new ArrayList<>();
            messageTxts.add(new Text("A formula symbol"));
      //      messageTxts.addAll(this.toTextList());

            messageTxts.add(new Text(" cannot match to both "));
            messageTxts.addAll(matchFormula.toTextList());

            messageTxts.add(new Text(" and "));
            messageTxts.addAll(match.toTextList());

            messageTxts.add(new Text("."));
            throw new TextMessageException(messageTxts);
        }
    }

    @Override
    public List<Text> toTextList() {
        return formulaSym.toTextList();
    }

    @Override
    public String toString() {
        return formulaSym.toString();
    }

    @Override
    public boolean equals(Object o) {
        boolean regular = super.equals(o);
        return regular && formulaSym.equals(((MFormula) o).getFormulaSym());
    }

    @Override
    public int hashCode() {
        int regular = super.hashCode();
        return regular + formulaSym.hashCode();
    }




}

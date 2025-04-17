package slapp.editor.parser.grammatical_parts;

import javafx.scene.text.Text;
import slapp.editor.EditorAlerts;
import slapp.editor.ReplacementTxtMsgException;
import slapp.editor.parser.TextMessageException;
import slapp.editor.parser.symbols.MTermSym;
import slapp.editor.parser.symbols.MVariable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MTerm extends Term {
    private static List<MTerm> mTerms = new ArrayList<MTerm>();
    private MTermSym mTermSym;
    private Term matchTerm = null;


    private MTerm(MTermSym termSym) {
        super();
        this.mTermSym = termSym;
        setTermType(TermType.MTERM);
        mTerms.add(this);
    }



    public static MTerm getInstance(MTermSym termSym) {
        MTerm newTerm = null;
        for (MTerm term : mTerms) {
            if (term.getmTermSym().equals(termSym)) {
                newTerm = term;
                break;
            }
        }
        if (newTerm == null) {  newTerm = new MTerm(termSym);    }
        return newTerm;
    }

    public static void clear() {
        for (MTerm term : mTerms) {
            term.clearMatch();
        }
    }

    private void clearMatch() {
        matchTerm = null;
    }

    public MTermSym getmTermSym() {
        return mTermSym;
    }

    public static List<MTerm> getmTerms() {
        return mTerms;
    }

    public Term getMatchTerm() {
        return matchTerm;
    }

    @Override
    public Term getMatch() { return matchTerm;  }

    public void setMatch(Term match) throws TextMessageException {

        if (matchTerm == null)  {
            matchTerm = match;      }
        else if (!matchTerm.equals(match)) {
            List<Text> messageTxts = new ArrayList<>();
            messageTxts.add(new Text("A term"));
     //       messageTxts.addAll(mTermSym.toTextList());
            messageTxts.add(new Text(" cannot match to both "));
            messageTxts.addAll(matchTerm.toTextList());
            messageTxts.add(new Text(" and "));
            messageTxts.addAll(match.toTextList());
            messageTxts.add(new Text("."));

    //        EditorAlerts.showSimpleTxtListAlert("from mterm", messageTxts);

            throw new ReplacementTxtMsgException(messageTxts);
        }
    }

    @Override
    public List<Text> toTextList() {
        return mTermSym.toTextList();
    }

    @Override
    public String toString() {
        return mTermSym.toString();
    }

    @Override
    public boolean equals(Object o) {
        boolean regular = super.equals(o);
        return regular && mTermSym.equals(((MTerm) o).getmTermSym());
    }

    @Override
    public int hashCode() {
        int regular = super.hashCode();
        return regular + mTermSym.hashCode();
    }




}

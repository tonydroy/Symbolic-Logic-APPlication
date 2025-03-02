package slapp.editor.parser.grammatical_parts;

import javafx.scene.text.Text;
import slapp.editor.parser.TextMessageException;
import slapp.editor.parser.symbols.MTermSym;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MTerm extends Term {
    private static List<MTerm> mTerms = new ArrayList<MTerm>();
    private Term matchTerm;


    private MTerm(MTermSym termSym) {
        super();
        setChildren(Collections.singletonList(termSym));
        setTermType(TermType.MTERM);
        mTerms.add(this);
    }



    public static MTerm getInstance(MTermSym termSym) {
        MTerm newTerm = null;
        for (MTerm term : mTerms) {
            if (term.getChildren().get(0).equals(termSym)) {
                newTerm = term;
                break;
            }
        }
        if (newTerm == null) {  newTerm = new MTerm(termSym);    }
        return newTerm;
    }

    @Override
    public Term getMatch() { return matchTerm;  }

    public void setMatch(Term match) throws TextMessageException {
        if (matchTerm == null) { matchTerm = match; }
        else if (!matchTerm.equals(match)) {
            List<Text> messageTxts = new ArrayList<>();
            messageTxts.add(new Text("Variable "));
            messageTxts.addAll(this.toTextList());
            messageTxts.add(new Text(" cannot match to both "));
            messageTxts.addAll(match.toTextList());
            messageTxts.add(new Text(" and "));
            messageTxts.addAll(matchTerm.toTextList());
            messageTxts.add(new Text("."));
            throw new TextMessageException(messageTxts);
        }


    }



}

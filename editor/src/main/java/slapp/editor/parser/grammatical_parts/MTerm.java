package slapp.editor.parser.grammatical_parts;

import javafx.scene.text.Text;
import slapp.editor.parser.TextMessageException;
import slapp.editor.parser.symbols.MTermSym;
import slapp.editor.parser.symbols.MVariable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MTerm extends Term {
    private static List<MTerm> mTerms = new ArrayList<MTerm>();
    private MTermSym mTermSym;
    private Term matchTerm;


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

    public static void clear() { mTerms.clear(); }


    public MTermSym getmTermSym() {
        return mTermSym;
    }

    @Override
    public Term getMatch() { return matchTerm;  }

    public void setMatch(Term match) throws TextMessageException {
        if (matchTerm == null) {
            if (!getChildren().isEmpty()) {
                String vars = "variables";
                if (getChildren().size() == 1) {
                    vars = "variable";
                }
                List<Text> messageTxts = new ArrayList<>();
                messageTxts.add(new Text("A metalinguistic expression with " + vars + " ("));
                for (int i = 0; i < getChildren().size(); i++) {
                    messageTxts.addAll(getChildren().get(i).toTextList());
                    if (i < getChildren().size() - 1) {
                        messageTxts.add(new Text(","));
                    }
                }
                messageTxts.add(new Text(") does not map."));
                throw new TextMessageException(messageTxts);
            } else {
                matchTerm = match;
            }
        }
        else if (!matchTerm.equals(match)) {
            List<Text> messageTxts = new ArrayList<>();
            messageTxts.add(new Text("Variable "));
            messageTxts.addAll(this.getChildren().get(0).toTextList());
            messageTxts.add(new Text(" cannot match to both "));
            messageTxts.addAll(match.toTextList());
            messageTxts.add(new Text(" and "));
            messageTxts.addAll(matchTerm.toTextList());
            messageTxts.add(new Text("."));
            throw new TextMessageException(messageTxts);
        }
    }

    @Override
    public List<Text> toTextList() {
        List<Text> texts = new ArrayList<>();
        texts.addAll(mTermSym.toTextList());
        if (!getChildren().isEmpty()) {
            texts.add(new Text("("));
            for (int i = 0; i < getChildren().size(); i++) {
                texts.addAll(getChildren().get(i).toTextList());
                if (i < getChildren().size() - 1) {texts.add(new Text(","));}
            }
            texts.add(new Text(")"));
        }
        if (getSubTransform() != null) {texts.addAll(getSubTransform().toTextList());}
        return texts;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(mTermSym.toString());
        if (!getChildren().isEmpty()) {
            sb.append("(");
            for (int i = 0; i < getChildren().size(); i++) {
                sb.append(getChildren().get(i).toString());
                if (i < getChildren().size() - 1) {sb.append(",");}
            }
            sb.append(")");
        }
        if (getSubTransform() != null) {sb.append(getSubTransform().toString());}
        return sb.toString();
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

package slapp.editor.parser.symbols;

import javafx.scene.text.Text;
import slapp.editor.parser.ExpressionType;
import slapp.editor.parser.TextMessageException;

import java.util.ArrayList;
import java.util.List;

public class MetUnivSym extends UnivSym {
    private static List<MetUnivSym> metUnivSyms = new ArrayList<>();
    private ObjUnivSym matchObjUnivSym;


    private MetUnivSym(String baseStr) {
        super(baseStr);
        type = ExpressionType.META_UNIV_SYM;
        metUnivSyms.add(this);
        matchObjUnivSym = null;
    }

    public static MetUnivSym getInstance(String baseStr) {
        MetUnivSym mSym = null;
        for (MetUnivSym sym : metUnivSyms) {
            if (sym.getBaseStr().equals(baseStr)) {
                mSym = sym;
                break;
            }
        }
        if (mSym == null) { mSym = new MetUnivSym(baseStr); }

        return mSym;
    }

    public ObjUnivSym getMatch() {return matchObjUnivSym;}

    public void setMatch (ObjUnivSym match) throws TextMessageException {
        if (matchObjUnivSym == null) {
            matchObjUnivSym = match; }
        else if (!matchObjUnivSym.equals(match)) {
            List<Text> messageTxts = new ArrayList<>();
            messageTxts.add(new Text("Universe meta symbol "));
            messageTxts.addAll(this.toTextList());
            messageTxts.add(new Text(" cannot match to both "));
            messageTxts.addAll(matchObjUnivSym.toTextList());
            messageTxts.add(new Text(" and "));
            messageTxts.addAll(match.toTextList());
            messageTxts.add(new Text("."));
            throw new TextMessageException(messageTxts);
        }
    }

    public static void clear() {
        for (MetUnivSym mSym : metUnivSyms) {
            mSym.clearMatch();
        }
    }

    private void clearMatch() { matchObjUnivSym = null;}

    public static List<MetUnivSym> getMetUnivSyms() { return metUnivSyms; }

    public ObjUnivSym getMatchObjUnivSym() { return matchObjUnivSym; }


    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if ((o instanceof MetUnivSym)) {
            MetUnivSym other = (MetUnivSym) o;
            return baseStr.equals(other.baseStr);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return baseStr.hashCode();
    }
}

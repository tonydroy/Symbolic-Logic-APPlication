package slapp.editor.parser.symbols;

import javafx.scene.text.Text;
import slapp.editor.parser.ExpressionType;
import slapp.editor.parser.TextMessageException;

import java.util.ArrayList;
import java.util.List;

public class MetObjVar extends ObjVar {
    private static List<MetObjVar> metObjVars = new ArrayList<>();
    private ObjObjVar matchObjVar;


    private MetObjVar(String baseStr) {
        super(baseStr);
        type = ExpressionType.MET_OBJ_VAR;
        metObjVars.add(this);
        matchObjVar = null;
    }

    public static MetObjVar getInstance(String baseStr) {
        MetObjVar mVar = null;
        for (MetObjVar sym : metObjVars) {
            if (sym.getBaseStr().equals(baseStr)) {
                mVar = sym;
                break;
            }
        }
        if (mVar == null) { mVar = new MetObjVar(baseStr); }

        return mVar;
    }

    public ObjObjVar getMatch() {return matchObjVar;}

    public void setMatch (ObjObjVar match) throws TextMessageException {
        if (matchObjVar == null) {
            matchObjVar = match; }
        else if (!matchObjVar.equals(match)) {
            List<Text> messageTxts = new ArrayList<>();
            messageTxts.add(new Text("Object meta symbol "));
            messageTxts.addAll(this.toTextList());
            messageTxts.add(new Text(" cannot match to both "));
            messageTxts.addAll(matchObjVar.toTextList());
            messageTxts.add(new Text(" and "));
            messageTxts.addAll(match.toTextList());
            messageTxts.add(new Text("."));
            throw new TextMessageException(messageTxts);
        }
    }

    public static void clear() {
        for (MetObjVar mVar : metObjVars) {
            mVar.clearMatch();
        }
    }

    private void clearMatch() { matchObjVar = null;}

    public static List<MetObjVar> getMetObjVars() { return metObjVars; }




    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if ((o instanceof MetObjVar)) {
            MetObjVar other = (MetObjVar) o;
            return baseStr.equals(other.baseStr);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return baseStr.hashCode();
    }
}

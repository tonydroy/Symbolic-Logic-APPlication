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
    private Formula matchFormula;


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

    public static void clear() { mFormulas.clear(); }

    public MFormulaSym getFormulaSym() {
        return formulaSym;
    }

    

    @Override
    public Formula getMatch() { return matchFormula; }

    public void setMatch(Formula match) throws TextMessageException {
        if (matchFormula == null) {
            if (!getChildren().isEmpty()) {
                String vars = "variables";
                if (getChildren().size() == 1) {vars = "variable";}
                List<Text> messageTxts = new ArrayList<>();
                messageTxts.add(new Text("A metalinguistic expression with " + vars + " ("));
                for (int i = 0; i < getChildren().size(); i++) {
                    messageTxts.addAll(getChildren().get(i).toTextList());
                    if (i < getChildren().size() - 1) { messageTxts.add(new Text(","));}
                }
                messageTxts.add(new Text(") does not map."));
                throw new TextMessageException(messageTxts);
            }
            else {
                matchFormula = match;
            }
        }
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

    @Override
    public List<Text> toTextList() {
        List<Text> texts = new ArrayList<>();
        texts.addAll(formulaSym.toTextList());
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
        sb.append(formulaSym.toString());
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
        return regular && formulaSym.equals(((MFormula) o).getFormulaSym());
    }

    @Override
    public int hashCode() {
        int regular = super.hashCode();
        return regular + formulaSym.hashCode();
    }




}

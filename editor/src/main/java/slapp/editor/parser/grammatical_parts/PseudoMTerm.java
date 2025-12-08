package slapp.editor.parser.grammatical_parts;

import javafx.scene.text.Text;
import slapp.editor.parser.Expression;
import slapp.editor.parser.symbols.PseudoMFormulaSym;
import slapp.editor.parser.symbols.PseudoMTermSym;

import java.util.ArrayList;
import java.util.List;

public class PseudoMTerm extends Term {
    PseudoMTermSym pMTermSym;

    public PseudoMTerm(PseudoMTermSym pMTermSym) {
        super();
        this.pMTermSym = pMTermSym;
        setTermType(TermType.PMTERM);
    }


    public PseudoMTermSym getMainTermSym() {
        return pMTermSym;
    }


    @Override
    public PseudoMTerm getMatch() {
        PseudoMTerm newTerm = new PseudoMTerm(pMTermSym.getMatch());
        newTerm.setLevel(getLevel());
        return newTerm;
    }

    @Override
    public List<Text> toTextList() {

        List<Text> list = new ArrayList<Text>();
        list.addAll(pMTermSym.toTextList());
        return list;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(pMTermSym.toString());
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (o instanceof PseudoMTerm) {
            PseudoMTerm other = (PseudoMTerm) o;


            return pMTermSym.equals(other.pMTermSym);


        }
        return false;
    }

        @Override
        public int hashCode() {
            return pMTermSym.hashCode();
        }
}

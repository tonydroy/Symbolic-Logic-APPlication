package slapp.editor.parser.grammatical_parts;

import javafx.scene.text.Text;
import slapp.editor.parser.Expression;
import slapp.editor.parser.symbols.PseudoMFormulaSym;
import slapp.editor.parser.symbols.PseudoMTermSym;
import slapp.editor.parser.symbols.SentenceLetter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class PseudoMFormula extends Formula implements Expression {
    PseudoMFormulaSym mainSym;

    public PseudoMFormula(PseudoMFormulaSym mainSym) {
        super();
        setAtomic(true);
        this.mainSym = mainSym;
    }


    public PseudoMFormulaSym getMainFormulaSym() {
        return mainSym;
    }

    @Override
    public PseudoMFormula getMatch() {
        PseudoMFormula newFormula = new PseudoMFormula(mainSym.getMatch());
        newFormula.setLevel(getLevel());
        return newFormula;
    }

    @Override
    public List<Text> toTextList() {

        List<Text> list = new ArrayList<Text>();
        list.addAll(mainSym.toTextList());
        return list;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(mainSym.toString());
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (o instanceof PseudoMFormula) {
            PseudoMFormula other = (PseudoMFormula) o;


            return mainSym.equals(other.mainSym);


        }
        return false;
    }

        @Override
        public int hashCode() {
            return mainSym.hashCode();
        }
}

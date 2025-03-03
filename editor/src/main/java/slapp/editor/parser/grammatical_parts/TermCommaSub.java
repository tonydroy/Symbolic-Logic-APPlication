package slapp.editor.parser.grammatical_parts;

import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import slapp.editor.parser.ExpressionType;
import slapp.editor.parser.ParseUtilities;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TermCommaSub extends SubstitutionTransform {

    public TermCommaSub(Term term1, Term term2) {
        super (term1, term2, ",", ExpressionType.TERM_COMMA_SUB);
    }

    @Override
    public TermCommaSub getMatch() {
        return new TermCommaSub(((Term) getExp1()).getMatch(), ((Term) getExp2()).getMatch());
    }

    @Override
    public List<Text> toTextList() {
        List<Text> texts = new ArrayList<Text>();
        texts.addAll(Arrays.asList(ParseUtilities.newSupSubText(getExp1().toString(), getExp2().toString())));
        return texts;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("\u2039");
        sb.append(getExp1().toString());
        sb.append(", ");
        sb.append(getExp2().toString());
        sb.append("\u203a");
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o instanceof TermCommaSub) {
            TermCommaSub other = (TermCommaSub) o;
            return getExp1().equals(other.getExp1()) && getExp2().equals(other.getExp2());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return getExp1().hashCode() + getExp2().hashCode();
    }





}

package slapp.editor.parser.grammatical_parts;

import javafx.scene.text.Text;
import slapp.editor.parser.ExpressionType;
import slapp.editor.parser.ParseUtilities;

import java.util.ArrayList;
import java.util.List;

public class FormulaDSlashSub extends SubstitutionTransform {

    public FormulaDSlashSub(Formula formula1, Formula formula2, String dividerSymbol) {
        super (formula1, formula2, ExpressionType.ONE_FORMULA_SUB, dividerSymbol);
    }

    @Override
    public FormulaDSlashSub getMatch() {
        return new FormulaDSlashSub(((Formula) getExp1()).getMatch(), ((Formula) getExp2()).getMatch(), getDividerSymbol());
    }

    @Override
    public List<Text> toTextList() {
        List<Text> texts = new ArrayList<Text>();
        texts.add(ParseUtilities.newSuperscriptText(getExp1().toString()));
        texts.add(new Text("\u2afd"));
        texts.add(ParseUtilities.newSubscriptText((getExp2().toString())));
        return texts;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("\u2039");
        sb.append(getExp1().toString());
        sb.append("\u2afd");
        sb.append(getExp2().toString());
        sb.append("\u203a");
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o instanceof FormulaDSlashSub) {
            FormulaDSlashSub other = (FormulaDSlashSub) o;
            return getExp1().equals(other.getExp1()) && getExp2().equals(other.getExp2());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return getExp1().hashCode() + getExp2().hashCode();
    }


}

package slapp.editor.m_parse.m_symbols;

import javafx.scene.text.Text;
import slapp.editor.parser.Expression;
import slapp.editor.parser.ExpressionType;
import slapp.editor.parser.grammatical_parts.Formula;

import java.util.ArrayList;
import java.util.List;

public class MInterpFormula implements Expression {
    private ExpressionType type = ExpressionType.M_INTERP_FORMULA;
    private MInterpretationVariable interpVar;
    private Formula formula;
    private MOpenSquareBracket openBracket;
    private MCloseSquareBracket closeBracket;


    public MInterpFormula(MInterpretationVariable interpVar, Formula formula, MOpenSquareBracket openBracket, MCloseSquareBracket closeBracket) {
        this.interpVar = interpVar;
        this.formula = formula;
        this.openBracket = openBracket;
        this.closeBracket = closeBracket;
    }

    @Override
    public ExpressionType getType() {return type;}

    @Override
    public List<Text> toTextList() {
        List<Text> textList = new ArrayList<>();
        textList.addAll(interpVar.toTextList());
        textList.addAll(openBracket.toTextList());
        textList.addAll(formula.toTextList());
        textList.addAll(closeBracket.toTextList());
        return textList;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(interpVar.toString());
        sb.append(openBracket.toString());
        sb.append(formula.toString());
        sb.append(closeBracket.toString());
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o instanceof MInterpFormula) {
            MInterpFormula other = (MInterpFormula) o;
            if (!interpVar.equals(other.getInterpVar()) || !formula.equals(other.getFormula())) return false;
            return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return interpVar.hashCode() + formula.hashCode();
    }


    public MInterpretationVariable getInterpVar() {
        return interpVar;
    }

    public Formula getFormula() {
        return formula;
    }
}

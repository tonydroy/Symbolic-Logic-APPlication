package slapp.editor.m_parse.m_symbols;

import javafx.scene.text.Text;
import slapp.editor.parser.Expression;
import slapp.editor.parser.ExpressionType;
import slapp.editor.parser.grammatical_parts.Formula;
import slapp.editor.parser.grammatical_parts.Term;

import java.util.ArrayList;
import java.util.List;

public class MInterpAssignFormula implements Expression {
    private ExpressionType type = ExpressionType.M_INTERP_ASSIGN_FORMULA;
    private MInterpretationAssignment interpAssign;
    private Formula formula;
    private MOpenSquareBracket openBracket;
    private MCloseSquareBracket closeBracket;


    public MInterpAssignFormula(MInterpretationAssignment interpAssign, Formula formula, MOpenSquareBracket openBracket, MCloseSquareBracket closeBracket) {
        this.interpAssign = interpAssign;
        this.formula = formula;
        this.openBracket = openBracket;
        this.closeBracket = closeBracket;
    }

    @Override
    public ExpressionType getType() {return type;}

    @Override
    public List<Text> toTextList() {
        List<Text> textList = new ArrayList<>();
        textList.addAll(interpAssign.toTextList());
        textList.addAll(openBracket.toTextList());
        textList.addAll(formula.toTextList());
        textList.addAll(closeBracket.toTextList());
        return textList;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(interpAssign.toString());
        sb.append(openBracket.toString());
        sb.append(formula.toString());
        sb.append(closeBracket.toString());
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o instanceof MInterpAssignFormula) {
            MInterpAssignFormula other = (MInterpAssignFormula) o;
            if (!interpAssign.equals(other.getInterpAssign()) || !formula.equals(other.getFormula())) return false;
            return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return interpAssign.hashCode() + formula.hashCode();
    }


    public Formula getFormula() {
        return formula;
    }

    public MInterpretationVariable getInterpretationVariable() {
        return interpAssign.getInterpVariable();
    }

    public MInterpretationAssignment getInterpAssign() {
        return interpAssign;
    }

    public MAssignment getAssignment() {
        return interpAssign.getAssignment();
    }

}

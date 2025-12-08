package slapp.editor.m_parse.m_symbols;

import javafx.scene.text.Text;
import slapp.editor.parser.Expression;
import slapp.editor.parser.ExpressionType;
import slapp.editor.parser.grammatical_parts.Term;

import java.util.ArrayList;
import java.util.List;

public class MInterpAssignTerm implements Expression {
    private ExpressionType type = ExpressionType.M_INTERP_ASSIGN_TERM;
    private MInterpretationAssignment interpAssign;
    private Term term;
    private MOpenSquareBracket openBracket;
    private MCloseSquareBracket closeBracket;


    public MInterpAssignTerm(MInterpretationAssignment interpAssign, Term term, MOpenSquareBracket openBracket, MCloseSquareBracket closeBracket) {
        this.interpAssign = interpAssign;
        this.term = term;
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
        textList.addAll(term.toTextList());
        textList.addAll(closeBracket.toTextList());
        return textList;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(interpAssign.toString());
        sb.append(openBracket.toString());
        sb.append(term.toString());
        sb.append(closeBracket.toString());
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o instanceof MInterpAssignTerm) {
            MInterpAssignTerm other = (MInterpAssignTerm) o;
            if (!interpAssign.equals(other.getInterpAssign()) || !term.equals(other.getTerm())) return false;
            return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return interpAssign.hashCode() + term.hashCode();
    }


    public Term getTerm() {
        return term;
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

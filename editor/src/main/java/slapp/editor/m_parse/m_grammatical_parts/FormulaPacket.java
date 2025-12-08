package slapp.editor.m_parse.m_grammatical_parts;

import javafx.util.Pair;
import slapp.editor.m_parse.m_symbols.*;
import slapp.editor.parser.grammatical_parts.Formula;
import slapp.editor.parser.grammatical_parts.Operator;
import slapp.editor.parser.grammatical_parts.Term;

import java.util.LinkedHashMap;

public class FormulaPacket extends SemanticPacket {

    private MInterpretation interpretation;
    private MInterpretationVariable interpretationVariable;
    private LinkedHashMap<Term, MBasicObjTerm> assignment = new LinkedHashMap<>();
    private MAssignmentVariable assignmentVariable;
    private Pair<Term, MBasicObjTerm> lastAssignment;
    private Formula formula;
    private MSatValue satisfactionVal;


    public FormulaPacket(MInterpretation interpretation, Formula formula) {
        assignment = new LinkedHashMap<>();
        satisfactionVal = null;
        this.interpretation = interpretation;
        this.formula = formula;
    }

    public void putAssignment(Term term, MBasicObjTerm assignment) {
        this.assignment.put(term, assignment);
        lastAssignment = new Pair<>(term, assignment);
    }

    public MInterpretation getInterpretation() {
        return interpretation;
    }

    public LinkedHashMap<Term, MBasicObjTerm> getAssignment() {
        return assignment;
    }

    public Formula getFormula() {
        return formula;
    }

    public MSatValue getSatisfactionVal() {
        return satisfactionVal;
    }

    public void setSatisfactionVal(MSatValue satisfactionVal) {
        this.satisfactionVal = satisfactionVal;
    }

    public Operator getMainOperator() {
        return formula.getMainOperator();
    }

    public Pair<Term, MBasicObjTerm> getLastAssignment() {
        return lastAssignment;
    }

    public MInterpretationVariable getInterpretationVariable() {
        return interpretationVariable;
    }

    public void setInterpretationVariable(MInterpretationVariable interpretationVariable) {
        this.interpretationVariable = interpretationVariable;
    }

    public MAssignmentVariable getAssignmentVariable() {
        return assignmentVariable;
    }

    public void setAssignmentVariable(MAssignmentVariable assignmentVariable) {
        this.assignmentVariable = assignmentVariable;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(interpretation.toString());
        sb.append("\n");
        sb.append(formula.toString());
        sb.append("\n");
        sb.append(assignment.toString());
        sb.append("\n");
        return sb.toString();
    }
}

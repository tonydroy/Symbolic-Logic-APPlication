package slapp.editor.m_parse.m_grammatical_parts;

import javafx.util.Pair;
import slapp.editor.m_parse.m_symbols.MBasicObjTerm;
import slapp.editor.parser.grammatical_parts.Formula;
import slapp.editor.parser.grammatical_parts.Operator;
import slapp.editor.parser.grammatical_parts.Term;

import java.util.LinkedHashMap;

public class TermPacket extends SemanticPacket {

    private MInterpretation interpretation;
    private LinkedHashMap<Term, MBasicObjTerm> assignment = new LinkedHashMap<>();
    private Term term;
    private MBasicObjTerm basicObject;


    public TermPacket(Term term, MInterpretation interpretation, LinkedHashMap<Term, MBasicObjTerm> assignment) {
        this.term = term;
        this.interpretation = interpretation;
        this.assignment = assignment;
    }



    public MInterpretation getInterpretation() {
        return interpretation;
    }

    public LinkedHashMap<Term, MBasicObjTerm> getAssignment() {
        return assignment;
    }

    public Term getTerm() {
        return term;
    }

    public MBasicObjTerm getBasicObject() {
        return basicObject;
    }

    public void setBasicObject(MBasicObjTerm basicObject) {
        this.basicObject = basicObject;
    }



    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(interpretation.toString());
        sb.append("\n");
        sb.append(term.toString());
        sb.append("\n");
        sb.append(assignment.toString());
        sb.append("\n");
        return sb.toString();
    }
}

package slapp.editor.m_parse.m_grammatical_parts;

import slapp.editor.m_parse.m_symbols.MAssignmentVariable;
import slapp.editor.m_parse.m_symbols.MBasicObjTerm;
import slapp.editor.parser.grammatical_parts.Term;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MVarAssign {

    private Map<MAssignmentVariable, LinkedHashMap<Term, MBasicObjTerm>> assignments = new LinkedHashMap<MAssignmentVariable, LinkedHashMap<Term, MBasicObjTerm>>();

    public Map<Term, MBasicObjTerm> getAssignment(MAssignmentVariable var) {
        if (assignments.containsKey(var)) return assignments.get(var);
        else {
            LinkedHashMap<Term, MBasicObjTerm> assign = new LinkedHashMap<>();
            assignments.put(var, assign);
            return assign;
        }
    }

    public void addVariable(MAssignmentVariable assVar, Term variable, MBasicObjTerm obj) {
        getAssignment(assVar).put(variable, obj);
    }

    public MBasicObjTerm getVariableVal(MAssignmentVariable assVar, Term variable) {
        return getAssignment(assVar).get(variable);
    }

    public  Map<MAssignmentVariable, LinkedHashMap<Term, MBasicObjTerm>> get() {
        return assignments;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (MAssignmentVariable var : assignments.keySet()) {
            sb.append("[" + assignments.get(var).toString() + "]");
        }
        return sb.toString();
    }

}

package slapp.editor.m_parse.m_symbols;

import javafx.scene.text.Text;
import slapp.editor.parser.Expression;
import slapp.editor.parser.ExpressionType;
import slapp.editor.parser.grammatical_parts.Term;

import java.util.ArrayList;
import java.util.List;

public class MAssignVariable implements Expression {
    private ExpressionType type = ExpressionType.M_ASSIGN_VARIABLE;
    private MAssignment variableAssignment;
    private Term variableTerm;
    private MOpenSquareBracket openBracket;
    private MCloseSquareBracket closeBracket;


    public MAssignVariable(MAssignment variableAssignment, Term variableTerm, MOpenSquareBracket openBracket, MCloseSquareBracket closeBracket) {
        this.variableAssignment = variableAssignment;
        this.variableTerm = variableTerm;
        this.openBracket = openBracket;
        this.closeBracket = closeBracket;
    }

    @Override
    public ExpressionType getType() {return type;}

    @Override
    public List<Text> toTextList() {
        List<Text> textList = new ArrayList<>();
        textList.addAll(variableAssignment.toTextList());
        textList.addAll(openBracket.toTextList());
        textList.addAll(variableTerm.toTextList());
        textList.addAll(closeBracket.toTextList());
        return textList;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(variableAssignment.toString());
        sb.append(openBracket.toString());
        sb.append(variableTerm.toString());
        sb.append(closeBracket.toString());
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o instanceof MAssignVariable) {
            MAssignVariable other = (MAssignVariable) o;
            if (!variableAssignment.equals(other.getVariableAssignment()) || !variableTerm.equals(other.getVariableTerm())) return false;
            return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return variableAssignment.hashCode() + variableTerm.hashCode();
    }


    public MAssignment getVariableAssignment() {
        return variableAssignment;
    }

    public Term getVariableTerm() {
        return variableTerm;
    }
}

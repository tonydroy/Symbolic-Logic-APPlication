package slapp.editor.m_parse.m_grammatical_parts;

import javafx.scene.text.Text;
import slapp.editor.m_parse.m_symbols.*;
import slapp.editor.parser.Expression;
import slapp.editor.parser.ExpressionType;

import java.util.ArrayList;
import java.util.List;

public class MVAssignVariableAssign implements Expression {
    private ExpressionType type = ExpressionType.M_VASSIGN_VARIABLE_ASSIGN;

    private MAssignVariable assignVariable;
    private MBasicObjTerm basicObj;
    private MEquals mEquals;

    public MVAssignVariableAssign(MAssignVariable assignVariable, MBasicObjTerm basicObj, MEquals mEquals) {
        this.assignVariable = assignVariable;
        this.basicObj = basicObj;
        this.mEquals = mEquals;
    }

    @Override
    public ExpressionType getType() {return type;}

    @Override
    public List<Text> toTextList() {
        List<Text> textList = new ArrayList<Text>();
        textList.addAll(assignVariable.toTextList());
        textList.add(new Text(" "));
        textList.addAll(mEquals.toTextList());
        textList.add(new Text(" "));
        textList.addAll(basicObj.toTextList());
        return textList;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(assignVariable.toString());
        sb.append(" ");
        sb.append(mEquals.toString());
        sb.append(" ");
        sb.append(basicObj.toString());
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o instanceof MVAssignVariableAssign) {
            MVAssignVariableAssign other = (MVAssignVariableAssign) o;
            return assignVariable.equals(other.getAssignVariable()) && basicObj.equals(other.getBasicObj());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return assignVariable.hashCode() + basicObj.hashCode();
    }


    public MAssignVariable getAssignVariable() {
        return assignVariable;
    }

    public MBasicObjTerm getBasicObj() {
        return basicObj;
    }
}

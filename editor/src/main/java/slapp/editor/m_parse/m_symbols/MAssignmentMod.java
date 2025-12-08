package slapp.editor.m_parse.m_symbols;

import javafx.scene.text.Text;
import slapp.editor.parser.Expression;
import slapp.editor.parser.ExpressionType;
import slapp.editor.parser.ParseUtilities;
import slapp.editor.parser.grammatical_parts.Term;

import java.util.ArrayList;
import java.util.List;

import static slapp.editor.parser.ExpressionType.M_ASSIGNMENT_MOD;
import static slapp.editor.parser.ExpressionType.M_VBAR_DIVIDER_SYM;


public class MAssignmentMod implements Expression {

    private ExpressionType type = M_ASSIGNMENT_MOD;
    private Term variable;
    private MBasicObjTerm object;
    private MVerticalDividerSym divider;

    public MAssignmentMod(Term variable, MVerticalDividerSym divider, MBasicObjTerm object) {
        this.variable = variable;
        this.divider = divider;
        this.object = object;
    }

    @Override
    public ExpressionType getType() {return type;}

    @Override
    public List<Text> toTextList() {
        List<Text> textList = new ArrayList<Text>();
        if (variable != null) textList.addAll(variable.toTextList());
        if (divider != null) textList.addAll(divider.toTextList());
        if (object != null) textList.addAll(object.toTextList());
        return textList;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(variable.toString());
        sb.append(divider.toString());
        sb.append(object.toString());
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o instanceof MAssignmentMod) {
            MAssignmentMod other = (MAssignmentMod) o;
            return variable.equals(other.getVariable()) && divider.equals(other.getDivider()) && object.equals(other.getObject());
        }
        return false;
    }

    @Override
    public int hashCode() {

        return variable.hashCode() + divider.hashCode() + object.hashCode();
    }

    public Term getVariable() {
        return variable;
    }

    public MBasicObjTerm getObject() {
        return object;
    }

    public MVerticalDividerSym getDivider() {
        return divider;
    }
}

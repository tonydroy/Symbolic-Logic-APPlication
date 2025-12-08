package slapp.editor.m_parse.m_symbols;

import javafx.scene.text.Text;
import slapp.editor.parser.Expression;
import slapp.editor.parser.ExpressionType;
import slapp.editor.parser.symbols.CommaDivider;

import java.util.*;

public class MAssignment implements Expression {

    private ExpressionType type = ExpressionType.M_ASSIGNMENT;
    private MAssignmentVariable assignmentVariable;
    private Set<MAssignmentMod> assignmentMods = new LinkedHashSet<>();
    private MOpenParenthesis openParenthesis;
    private MCloseParenthesis closeParenthesis;
    private CommaDivider commaDivider;


    public MAssignment(MAssignmentVariable assignmentVariable) {
        this.assignmentVariable = assignmentVariable;
        this.openParenthesis = openParenthesis;
        this.closeParenthesis = closeParenthesis;
        this.commaDivider = commaDivider;
    }

    @Override
    public ExpressionType getType() {return type;}

    //assumes well-formed assignment
    @Override
    public List<Text> toTextList() {
        List<Text> textList = new ArrayList<Text>();
        textList.addAll(assignmentVariable.toTextList());
        if (assignmentMods.size() > 0) {
            textList.addAll(openParenthesis.toTextList());
            Iterator<MAssignmentMod> iterator = assignmentMods.iterator();
            if (iterator.hasNext()) textList.addAll(iterator.next().toTextList());
            while (iterator.hasNext()) {
                textList.addAll(commaDivider.toTextList());
                textList.add(new Text(" "));
                textList.addAll(iterator.next().toTextList());
            }
            textList.addAll(closeParenthesis.toTextList());
        }
        return textList;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(assignmentVariable.toString());
        if (assignmentMods.size() > 0) {
            sb.append(openParenthesis.toString());
            Iterator<MAssignmentMod> iterator = assignmentMods.iterator();
            if (iterator.hasNext()) sb.append(iterator.next().toString());
            while (iterator.hasNext()) {
                sb.append(commaDivider.toString());
                sb.append(" ");
                sb.append(iterator.next().toString());
            }
            sb.append(closeParenthesis.toString());
        }
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o instanceof MAssignment) {
            MAssignment other = (MAssignment) o;
            if (assignmentMods.size() != other.assignmentMods.size()) return false;
            for (MAssignmentMod mod : assignmentMods) {
                if (!other.assignmentMods.contains(mod)) return false;
            }
            return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        int code = assignmentVariable.hashCode();
        for (MAssignmentMod mod : assignmentMods) {
            code = code + mod.hashCode();
        }
        return code;
    }


    public Set<MAssignmentMod> getAssignmentMods() {
        return assignmentMods;
    }

    public void setAssignmentMods(Set<MAssignmentMod> assignmentMods) {
        this.assignmentMods = assignmentMods;
    }

    public void setOpenParenthesis(MOpenParenthesis openParenthesis) {
        this.openParenthesis = openParenthesis;
    }

    public void setCloseParenthesis(MCloseParenthesis closeParenthesis) {
        this.closeParenthesis = closeParenthesis;
    }

    public void setCommaDivider(CommaDivider commaDivider) {
        this.commaDivider = commaDivider;
    }

    public MAssignmentVariable getAssignmentVariable() {
        return assignmentVariable;
    }
}

package slapp.editor.m_parse.m_symbols;

import javafx.scene.text.Font;
import javafx.scene.text.Text;
import slapp.editor.parser.Expression;
import slapp.editor.parser.ExpressionType;

import java.util.ArrayList;
import java.util.List;

public class MInterpretationAssignment implements Expression {
    private ExpressionType type = ExpressionType.M_INTERP_ASSIGNMENT;
    private MInterpretationVariable interpVariable;
    private MAssignment assignment;
    private double baseFontSize = 12.0;

    public MInterpretationAssignment(MInterpretationVariable interpVariable, MAssignment assignment) {
        this.interpVariable = interpVariable;
        this.assignment = assignment;
    }

    @Override
    public ExpressionType getType() {return type;}

    @Override
    public List<Text> toTextList() {
        List<Text> textList = new ArrayList<Text>();
        textList.addAll(interpVariable.toTextList());
        int start = textList.size();
        textList.addAll(assignment.toTextList());
        for (int i = start; i < textList.size(); i++) {
            Text subText = textList.get(i);
            subText.setFont(new Font("Noto Serif Combo", baseFontSize * .72));
            subText.setTranslateY(baseFontSize * .17);
        }
        return textList;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(interpVariable.toString());
        sb.append("_");
        sb.append(assignment.toString());
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o instanceof MInterpretationAssignment) {
            MInterpretationAssignment other = (MInterpretationAssignment) o;
            if (!interpVariable.equals(other.getInterpVariable()) || !assignment.equals(other.getAssignment())) {return false;}
            return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return interpVariable.hashCode() + assignment.hashCode();
    }

    public MInterpretationVariable getInterpVariable() {
        return interpVariable;
    }

    public MAssignment getAssignment() {
        return assignment;
    }
}

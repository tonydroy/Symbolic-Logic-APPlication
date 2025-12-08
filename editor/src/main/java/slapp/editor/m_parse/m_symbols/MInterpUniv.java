package slapp.editor.m_parse.m_symbols;

import javafx.scene.text.Text;
import slapp.editor.parser.Expression;
import slapp.editor.parser.ExpressionType;
import slapp.editor.parser.ParseUtilities;

import java.util.ArrayList;
import java.util.List;

import static slapp.editor.parser.ExpressionType.M_INTERP_UNIVERSE;


public class MInterpUniv implements Expression {

    private ExpressionType type = M_INTERP_UNIVERSE;
    private MInterpretationVariable interpVar;
    private String textString;

    public MInterpUniv(String textString, MInterpretationVariable interpVar) {
        this.textString = textString;
        this.interpVar = interpVar;
    }

    public void setInterpVar(MInterpretationVariable interpVar) {
        this.interpVar = interpVar;
    }

    public String nameString() {return textString;}

    @Override
    public ExpressionType getType() {return type;}

    @Override
    public List<Text> toTextList() {
        List<Text> textList = new ArrayList<Text>();
        if (!textString.isEmpty()) textList.add(ParseUtilities.newRegularText(textString));
        textList.addAll(interpVar.toTextList());
        return textList;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(textString);
        sb.append(interpVar.toString());
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o instanceof MInterpUniv) {
            MInterpUniv other = (MInterpUniv) o;
            return textString.equals(other.getTextString()) && interpVar.equals(other.getInterpVar());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return textString.hashCode() + interpVar.hashCode();
    }

    public MInterpretationVariable getInterpVar() {
        return interpVar;
    }

    public String getTextString() {
        return textString;
    }
}

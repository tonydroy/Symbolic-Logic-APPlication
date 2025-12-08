package slapp.editor.m_parse.m_grammatical_parts;

import javafx.scene.text.Text;
import slapp.editor.m_parse.m_symbols.*;
import slapp.editor.parser.Expression;
import slapp.editor.parser.ExpressionType;

import java.util.ArrayList;
import java.util.List;

public class MInterpSentenceAssign implements Expression {
    private ExpressionType type = ExpressionType.M_INTERP_SENTENCE_ASSIGN;

    private MInterpFormula interpFormula;
    private MTruthValue truthValue;
    private MEquals mEquals;

    public MInterpSentenceAssign(MInterpFormula interpFormula, MTruthValue truthValue, MEquals mEquals) {
        this.interpFormula = interpFormula;
        this.truthValue = truthValue;
        this.mEquals = mEquals;
    }

    @Override
    public ExpressionType getType() {return type;}

    @Override
    public List<Text> toTextList() {
        List<Text> textList = new ArrayList<Text>();
        textList.addAll(interpFormula.toTextList());
        textList.add(new Text(" "));
        textList.addAll(mEquals.toTextList());
        textList.add(new Text(" "));
        textList.addAll(truthValue.toTextList());
        return textList;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(interpFormula.toString());
        sb.append(" ");
        sb.append(mEquals.toString());
        sb.append(" ");
        sb.append(truthValue.toString());
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o instanceof MInterpSentenceAssign) {
            MInterpSentenceAssign other = (MInterpSentenceAssign) o;
            return interpFormula.equals(other.getInterpFormula()) && truthValue.equals(other.getTruthValue());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return interpFormula.hashCode() + truthValue.hashCode();
    }


    public MInterpFormula getInterpFormula() {
        return interpFormula;
    }

    public MTruthValue getTruthValue() {
        return truthValue;
    }
}

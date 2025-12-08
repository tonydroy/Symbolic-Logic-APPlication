package slapp.editor.m_parse.m_grammatical_parts;

import javafx.scene.text.Text;
import slapp.editor.m_parse.m_symbols.MEquals;
import slapp.editor.m_parse.m_symbols.MInterpUniv;
import slapp.editor.m_parse.m_symbols.MUniverse;
import slapp.editor.parser.Expression;
import slapp.editor.parser.ExpressionType;

import java.util.ArrayList;
import java.util.List;

public class MUniverseAssign implements Expression {
    private ExpressionType type = ExpressionType.M_UNIVERSE_ASSIGN;
    private MInterpUniv interpUniv;
    private MUniverse universeSet;
    private MEquals mEquals;

    public MUniverseAssign(MInterpUniv interpUniv, MUniverse universeSet, MEquals mEquals) {
        this.interpUniv = interpUniv;
        this.universeSet = universeSet;
        this.mEquals = mEquals;
    }

    @Override
    public ExpressionType getType() {return type;}

    @Override
    public List<Text> toTextList() {
        List<Text> textList = new ArrayList<Text>();
        textList.addAll(interpUniv.toTextList());
        textList.add(new Text(" "));
        textList.addAll(mEquals.toTextList());
        textList.add(new Text(" "));
        textList.addAll(universeSet.toTextList());
        return textList;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(interpUniv.toString());
        sb.append(" ");
        sb.append(mEquals.toString());
        sb.append(" ");
        sb.append(universeSet.toString());
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o instanceof MUniverseAssign) {
            MUniverseAssign other = (MUniverseAssign) o;
            return interpUniv.equals(other.getInterpUniv()) && universeSet.equals(other.getUniverseSet());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return interpUniv.hashCode() + universeSet.hashCode();
    }






    public MInterpUniv getInterpUniv() {
        return interpUniv;
    }

    public MUniverse getUniverseSet() {
        return universeSet;
    }
}

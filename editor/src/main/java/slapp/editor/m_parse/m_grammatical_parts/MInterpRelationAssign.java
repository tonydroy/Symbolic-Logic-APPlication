package slapp.editor.m_parse.m_grammatical_parts;

import javafx.scene.text.Text;
import slapp.editor.m_parse.m_symbols.*;
import slapp.editor.parser.Expression;
import slapp.editor.parser.ExpressionType;

import java.util.ArrayList;
import java.util.List;

public class MInterpRelationAssign implements Expression {
    private ExpressionType type = ExpressionType.M_INTERP_RELATION_ASSIGN;

    private MInterpRelation interpRelation;
    private MRelation relation;
    private MEquals mEquals;

    public MInterpRelationAssign(MInterpRelation interpRelation, MRelation relation, MEquals mEquals) {
        this.interpRelation = interpRelation;
        this.relation = relation;
        this.mEquals = mEquals;
    }

    @Override
    public ExpressionType getType() {return type;}

    @Override
    public List<Text> toTextList() {
        List<Text> textList = new ArrayList<Text>();
        textList.addAll(interpRelation.toTextList());
        textList.add(new Text(" "));
        textList.addAll(mEquals.toTextList());
        textList.add(new Text(" "));
        textList.addAll(relation.toTextList());
        return textList;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(interpRelation.toString());
        sb.append(" ");
        sb.append(mEquals.toString());
        sb.append(" ");
        sb.append(relation.toString());
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o instanceof MInterpRelationAssign) {
            MInterpRelationAssign other = (MInterpRelationAssign) o;
            return interpRelation.equals(other.getInterpRelation()) && relation.equals(other.getRelation());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return interpRelation.hashCode() + relation.hashCode();
    }


    public MInterpRelation getInterpRelation() {
        return interpRelation;
    }

    public MRelation getRelation() {
        return relation;
    }
}

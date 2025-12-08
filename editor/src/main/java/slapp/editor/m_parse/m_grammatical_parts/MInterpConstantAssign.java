package slapp.editor.m_parse.m_grammatical_parts;

import javafx.scene.text.Text;
import slapp.editor.m_parse.m_symbols.*;
import slapp.editor.parser.Expression;
import slapp.editor.parser.ExpressionType;

import java.util.ArrayList;
import java.util.List;

public class MInterpConstantAssign implements Expression {
    private ExpressionType type = ExpressionType.M_INTERP_CONSTANT_ASSIGN;

    private MInterpConstant interpConstant;
    private MBasicObjTerm basicObject;
    private MEquals mEquals;

    public MInterpConstantAssign(MInterpConstant interpConstant, MBasicObjTerm basicObject, MEquals mEquals) {
        this.interpConstant = interpConstant;
        this.basicObject = basicObject;
        this.mEquals = mEquals;
    }

    @Override
    public ExpressionType getType() {return type;}

    @Override
    public List<Text> toTextList() {
        List<Text> textList = new ArrayList<Text>();
        textList.addAll(interpConstant.toTextList());
        textList.add(new Text(" "));
        textList.addAll(mEquals.toTextList());
        textList.add(new Text(" "));
        textList.addAll(basicObject.toTextList());
        return textList;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(interpConstant.toString());
        sb.append(" ");
        sb.append(mEquals.toString());
        sb.append(" ");
        sb.append(basicObject.toString());
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o instanceof MInterpConstantAssign) {
            MInterpConstantAssign other = (MInterpConstantAssign) o;
            return interpConstant.equals(other.getInterpConstant()) && basicObject.equals(other.getBasicObject());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return interpConstant.hashCode() + basicObject.hashCode();
    }


    public MInterpConstant getInterpConstant() {
        return interpConstant;
    }

    public MBasicObjTerm getBasicObject() {
        return basicObject;
    }
}

package slapp.editor.m_parse.m_grammatical_parts;

import javafx.scene.text.Text;
import slapp.editor.m_parse.m_symbols.*;
import slapp.editor.parser.Expression;
import slapp.editor.parser.ExpressionType;

import java.util.ArrayList;
import java.util.List;

public class MInterpFunctionAssign implements Expression {
    private ExpressionType type = ExpressionType.M_INTERP_FUNCTION_ASSIGN;

    private MInterpFunction interpFunction;
    private MFunctionCandidate function;
    private MEquals mEquals;

    public MInterpFunctionAssign(MInterpFunction interpFunction, MFunctionCandidate function, MEquals mEquals) {
        this.interpFunction = interpFunction;
        this.function = function;
        this.mEquals = mEquals;
    }

    @Override
    public ExpressionType getType() {return type;}

    @Override
    public List<Text> toTextList() {
        List<Text> textList = new ArrayList<Text>();
        textList.addAll(interpFunction.toTextList());
        textList.add(new Text(" "));
        textList.addAll(mEquals.toTextList());
        textList.add(new Text(" "));
        textList.addAll(function.toTextList());
        return textList;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(interpFunction.toString());
        sb.append(" ");
        sb.append(mEquals.toString());
        sb.append(" ");
        sb.append(function.toString());
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o instanceof MInterpFunctionAssign) {
            MInterpFunctionAssign other = (MInterpFunctionAssign) o;
            return interpFunction.equals(other.getInterpFunction()) && function.equals(other.getFunction());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return interpFunction.hashCode() + function.hashCode();
    }


    public MInterpFunction getInterpFunction() {
        return interpFunction;
    }

    public MFunctionCandidate getFunction() {
        return function;
    }
}

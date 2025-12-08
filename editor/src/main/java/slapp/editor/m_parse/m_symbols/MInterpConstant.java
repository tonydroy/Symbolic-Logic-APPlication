package slapp.editor.m_parse.m_symbols;

import javafx.scene.text.Text;
import slapp.editor.parser.Expression;
import slapp.editor.parser.ExpressionType;
import slapp.editor.parser.grammatical_parts.Term;

import java.util.ArrayList;
import java.util.List;

public class MInterpConstant implements Expression {
    private ExpressionType type = ExpressionType.M_INTERP_CONSTANT;
    private MInterpretationVariable interpVar;
    private Term constantTerm;
    private MOpenSquareBracket openBracket;
    private MCloseSquareBracket closeBracket;


    public MInterpConstant(MInterpretationVariable interpVar, Term constantTerm, MOpenSquareBracket openBracket, MCloseSquareBracket closeBracket) {
        this.interpVar = interpVar;
        this.constantTerm = constantTerm;
        this.openBracket = openBracket;
        this.closeBracket = closeBracket;
    }

    @Override
    public ExpressionType getType() {return type;}

    @Override
    public List<Text> toTextList() {
        List<Text> textList = new ArrayList<>();
        textList.addAll(interpVar.toTextList());
        textList.addAll(openBracket.toTextList());
        textList.addAll(constantTerm.toTextList());
        textList.addAll(closeBracket.toTextList());
        return textList;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(interpVar.toString());
        sb.append(openBracket.toString());
        sb.append(constantTerm.toString());
        sb.append(closeBracket.toString());
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o instanceof MInterpConstant) {
            MInterpConstant other = (MInterpConstant) o;
            if (!interpVar.equals(other.getInterpVar()) || !constantTerm.equals(other.getConstantTerm())) return false;
            return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return interpVar.hashCode() + constantTerm.hashCode();
    }


    public MInterpretationVariable getInterpVar() {
        return interpVar;
    }

    public Term getConstantTerm() {
        return constantTerm;
    }
}

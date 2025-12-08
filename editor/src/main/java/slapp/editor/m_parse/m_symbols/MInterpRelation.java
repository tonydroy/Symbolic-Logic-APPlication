package slapp.editor.m_parse.m_symbols;

import javafx.scene.text.Text;
import slapp.editor.parser.Expression;
import slapp.editor.parser.ExpressionType;
import slapp.editor.parser.symbols.RelationSymbol;

import java.util.ArrayList;
import java.util.List;

public class MInterpRelation implements Expression {
    private ExpressionType type = ExpressionType.M_INTERP_RELATION;
    private MInterpretationVariable interpVar;
    private RelationSymbol relationSymbol;
    private MOpenSquareBracket openBracket;
    private MCloseSquareBracket closeBracket;


    public MInterpRelation(MInterpretationVariable interpVar, RelationSymbol relationSymbol, MOpenSquareBracket openBracket, MCloseSquareBracket closeBracket) {
        this.interpVar = interpVar;
        this.relationSymbol = relationSymbol;
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
        textList.addAll(relationSymbol.toTextList());
        textList.addAll(closeBracket.toTextList());
        return textList;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(interpVar.toString());
        sb.append(openBracket.toString());
        sb.append(relationSymbol.toString());
        sb.append(closeBracket.toString());
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o instanceof MInterpRelation) {
            MInterpRelation other = (MInterpRelation) o;
            if (!interpVar.equals(other.getInterpVar()) || !relationSymbol.equals(other.getRelationSymbol())) return false;
            return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return interpVar.hashCode() + relationSymbol.hashCode();
    }


    public MInterpretationVariable getInterpVar() {
        return interpVar;
    }

    public RelationSymbol getRelationSymbol() {
        return relationSymbol;
    }
}

package slapp.editor.m_parse.m_symbols;

import javafx.scene.text.Text;
import slapp.editor.parser.Expression;
import slapp.editor.parser.ExpressionType;
import slapp.editor.parser.symbols.FunctionSymbol;

import java.util.ArrayList;
import java.util.List;

public class MInterpFunction implements Expression {
    private ExpressionType type = ExpressionType.M_INTERP_FUNCTION;
    private MInterpretationVariable interpVar;
    private FunctionSymbol functionSymbol;
    private MOpenSquareBracket openBracket;
    private MCloseSquareBracket closeBracket;


    public MInterpFunction(MInterpretationVariable interpVar, FunctionSymbol functionSymbol, MOpenSquareBracket openBracket, MCloseSquareBracket closeBracket) {
        this.interpVar = interpVar;
        this.functionSymbol = functionSymbol;
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
        textList.addAll(functionSymbol.toTextList());
        textList.addAll(closeBracket.toTextList());
        return textList;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(interpVar.toString());
        sb.append(openBracket.toString());
        sb.append(functionSymbol.toString());
        sb.append(closeBracket.toString());
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o instanceof MInterpFunction) {
            MInterpFunction other = (MInterpFunction) o;
            if (!interpVar.equals(other.getInterpVar()) || !functionSymbol.equals(other.getFunctionSymbol())) return false;
            return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return interpVar.hashCode() + functionSymbol.hashCode();
    }


    public MInterpretationVariable getInterpVar() {
        return interpVar;
    }

    public FunctionSymbol getFunctionSymbol() {
        return functionSymbol;
    }
}

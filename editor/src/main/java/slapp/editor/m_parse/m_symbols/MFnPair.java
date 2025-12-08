package slapp.editor.m_parse.m_symbols;

import javafx.scene.text.Text;
import slapp.editor.parser.Expression;
import slapp.editor.parser.ExpressionType;
import slapp.editor.parser.symbols.CommaDivider;

import java.util.ArrayList;
import java.util.List;

public class MFnPair implements Expression {

    private ExpressionType type = ExpressionType.M_FUNCTION_PAIR;
    private MRelTuple input;
    private MBasicObjTerm output;
    private MOpenAngleBracket openBracket;
    private MCloseAngleBracket closeBracket;
    private CommaDivider commaDivider;



    public MFnPair(MRelTuple input, MBasicObjTerm output, MOpenAngleBracket openBracket, MCloseAngleBracket closeBracket, CommaDivider divider) {
        this.input = input;
        this.output = output;
        this.openBracket = openBracket;
        this.closeBracket = closeBracket;
        this.commaDivider = divider;
    }

    @Override
    public ExpressionType getType() {return type;}

    @Override
    public List<Text> toTextList() {
        List<Text> textList = new ArrayList<>();
        textList.addAll(openBracket.toTextList());
        textList.addAll(input.toTextList());
        textList.addAll(commaDivider.toTextList());
        textList.add(new Text(" "));
        textList.addAll(output.toTextList());
        textList.addAll(closeBracket.toTextList());
        return textList;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(openBracket.toString());
        sb.append(input.toString());
        sb.append(commaDivider.toString());
        sb.append(" ");
        sb.append(output.toString());
        sb.append(closeBracket.toString());
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o instanceof MFnPair) {
            MFnPair other = (MFnPair) o;
            if (!input.equals(other.input)) return false;
            if (!output.equals(other.output)) return false;
            return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return input.hashCode() + output.hashCode();
    }


    public MRelTuple getInput() {
        return input;
    }

    public MBasicObjTerm getOutput() {
        return output;
    }

    public int getPlaces() {
        return input.getPlaces();
    }

}

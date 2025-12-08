package slapp.editor.m_parse.m_symbols;

import javafx.scene.text.Text;
import slapp.editor.parser.Expression;
import slapp.editor.parser.ExpressionType;
import slapp.editor.parser.symbols.CommaDivider;

import java.util.*;

public class MFunctionCandidate implements Expression {

    private ExpressionType type = ExpressionType.M_FUNCTION_CANDIDATE;
    private Set<MFnPair> pairs = new LinkedHashSet<>();
    private MOpenBrace openBrace;
    private MCloseBrace closeBrace;
    private CommaDivider commaDivider;
    private int places;


    public MFunctionCandidate(Set<MFnPair> pairs, int places, MOpenBrace openBrace, MCloseBrace closeBrace, CommaDivider divider) {
        this.pairs = pairs;
        this.places = places;
        this.openBrace = openBrace;
        this.closeBrace = closeBrace;
        this.commaDivider = divider;
    }

    public boolean isFunction() {
        boolean isFunction = true;
        outerLoop:
        for (MFnPair pair : pairs) {
            for (MFnPair p : pairs ) {
                boolean sameInput = pair.getInput().equals(p.getInput());
                boolean sameOutput = pair.getOutput().equals(p.getOutput());
                if (pair.getInput().equals(p.getInput()) && !pair.getOutput().equals(p.getOutput())) {
                    isFunction = false;
                    break outerLoop;
                }
            }
        }
        return isFunction;
    }

    @Override
    public ExpressionType getType() {return type;}

    @Override
    public List<Text> toTextList() {
        List<Text> textList = new ArrayList<>();
        textList.addAll(openBrace.toTextList());
        Iterator<MFnPair> iterator = pairs.iterator();
        if (iterator.hasNext())  textList.addAll(iterator.next().toTextList());
        while (iterator.hasNext()) {
            textList.addAll(commaDivider.toTextList());
            textList.add(new Text(" "));
            textList.addAll(iterator.next().toTextList());
        }
        textList.addAll(closeBrace.toTextList());
        return textList;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(openBrace.toString());
        Iterator<MFnPair> iterator = pairs.iterator();
        if (iterator.hasNext())  sb.append(iterator.next().toString());
        while (iterator.hasNext()) {
            sb.append(commaDivider.toString());
            sb.append(" ");
            sb.append(iterator.next().toString());
        }
        sb.append(closeBrace.toString());
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o instanceof MFunctionCandidate) {
            MFunctionCandidate other = (MFunctionCandidate) o;
            if (pairs.size() != other.getPairs().size()) return false;
            boolean ok = true;
            for (MFnPair pair : pairs) {
                if (!other.getPairs().contains(pair)) ok = false;
            }
            return ok;
        }
        return false;
    }

    @Override
    public int hashCode() {
        int code = 0;
        for (MFnPair pair : pairs) {
            code += pair.hashCode();
        }
        return code;
    }


    public Set<MFnPair> getPairs() {
        return pairs;
    }

    public int getPlaces() {
        return places;
    }
}

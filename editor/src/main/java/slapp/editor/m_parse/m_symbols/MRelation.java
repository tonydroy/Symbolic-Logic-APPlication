package slapp.editor.m_parse.m_symbols;

import javafx.scene.text.Text;
import slapp.editor.parser.Expression;
import slapp.editor.parser.ExpressionType;
import slapp.editor.parser.symbols.CommaDivider;

import java.util.*;

public class MRelation implements Expression {

    private ExpressionType type = ExpressionType.M_RELATION;
    private Set<MRelTuple> tuples = new LinkedHashSet<>();
    private MOpenBrace openBrace;
    private MCloseBrace closeBrace;
    private CommaDivider commaDivider;
    private int places;


    public MRelation(Set<MRelTuple> tuples, int places, MOpenBrace openBrace, MCloseBrace closeBrace, CommaDivider divider) {
        this.tuples = tuples;
        this.places = places;
        this.openBrace = openBrace;
        this.closeBrace = closeBrace;
        this.commaDivider = divider;
    }

    @Override
    public ExpressionType getType() {return type;}

    @Override
    public List<Text> toTextList() {
        List<Text> textList = new ArrayList<>();
        textList.addAll(openBrace.toTextList());
        Iterator<MRelTuple> iterator = tuples.iterator();
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
        Iterator<MRelTuple> iterator = tuples.iterator();
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
        if (o instanceof MRelation) {
            MRelation other = (MRelation) o;
            if (tuples.size() != other.getTuples().size()) return false;
            boolean ok = true;
            for (MRelTuple term : tuples) {
                if (!other.getTuples().contains(term)) ok = false;
            }
            return ok;
        }
        return false;
    }

    @Override
    public int hashCode() {
        int code = 0;
        for (MRelTuple term : tuples) {
            code += term.hashCode();
        }
        return code;
    }


    public Set<MRelTuple> getTuples() {
        return tuples;
    }

    public int getPlaces() {
        return places;
    }
}

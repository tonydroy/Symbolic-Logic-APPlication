package slapp.editor.m_parse.m_symbols;

import javafx.scene.text.Text;
import slapp.editor.parser.Expression;
import slapp.editor.parser.ExpressionType;
import slapp.editor.parser.symbols.CommaDivider;

import java.util.ArrayList;
import java.util.List;

public class MRelTuple implements Expression {

    private ExpressionType type = ExpressionType.M_RELATION_TUPLE;
    private List<MBasicObjTerm> members;
    private MOpenAngleBracket openBracket;
    private MCloseAngleBracket closeBracket;
    private CommaDivider commaDivider;



    public MRelTuple(List<MBasicObjTerm> members, MOpenAngleBracket openBracket, MCloseAngleBracket closeBracket, CommaDivider divider) {
        this.members = members;
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
        if (members.size() > 0) textList.addAll(members.get(0).toTextList());
        for (int i = 1; i < members.size(); i++) {
            textList.addAll(commaDivider.toTextList());
            textList.add(new Text(" "));
            textList.addAll(members.get(i).toTextList());
        }
        textList.addAll(closeBracket.toTextList());
        return textList;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(openBracket.toString());
        if (members.size() > 0) sb.append(members.get(0).toString());
        for (int i = 1; i < members.size(); i++) {
            sb.append(commaDivider.toString());
            sb.append(" ");
            sb.append(members.get(i).toString());
        }
        sb.append(closeBracket.toString());
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o instanceof MRelTuple) {
            MRelTuple other = (MRelTuple) o;
            if (members.size() != other.getMembers().size()) return false;
            for (int i = 0; i < members.size(); i++) {
                if (!members.get(i).equals(other.getMembers().get(i))) return false;
            }
            return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        int code = 0;
        for (MBasicObjTerm term : members) {
            code += term.hashCode();
        }
        return code;
    }


    public List<MBasicObjTerm> getMembers() {
        return members;
    }

    public int getPlaces() {
        return members.size();
    }


}

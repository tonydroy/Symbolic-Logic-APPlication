package slapp.editor.parser.grammatical_parts;

import javafx.scene.text.Text;
import slapp.editor.parser.Expression;
import slapp.editor.parser.ExpressionType;
import slapp.editor.parser.OriginalElement;

import java.util.ArrayList;
import java.util.List;

public class AnyExpression implements Expression {

    private ExpressionType type = ExpressionType.ANY_EXPRESSION;
    List<Expression> children = new ArrayList<Expression>();

    @Override
    public ExpressionType getType() {
        return type;
    }

    @Override
    public List<Expression> getChildren() {
        return children;
    }

    @Override
    public List<Text> toTextList() {
        List<Text> texts = new ArrayList<>();
        for (Expression child : children) {
            OriginalElement e = (OriginalElement) child;
            texts.addAll(e.toTextList());
        }
        return texts;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (Expression child : children) {
            OriginalElement e = (OriginalElement) child;
            sb.append(e.toString());
        }
        return sb.toString();
    }

    @Override
    public AnyExpression getMatch() {return this;}

    public boolean equals(Object o) {
        if (o == this) return true;
        if (o instanceof AnyExpression) {
            AnyExpression other = (AnyExpression) o;
            boolean equal = true;
            if (children.size() != ((AnyExpression) o).children.size()) { equal = false; }
            else for (int i = 0; i < children.size(); i++) {
                if (!children.get(i).equals(other.children.get(i))) { equal = false; }
            }
            return equal;
        }
        return false;
    }

    @Override
    public int hashCode() {
        int code = 0;
        for (Expression child : children) { code += child.hashCode(); }
        return code;
    }

}

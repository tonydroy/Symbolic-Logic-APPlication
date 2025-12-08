package slapp.editor.parser.symbols;

import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import slapp.editor.parser.Expression;
import slapp.editor.parser.ExpressionType;

import java.util.List;

public class OperatorSym implements Expression {

    private int position;

    public OperatorSym(int position) {
        this.position = position;
    }

    public int getPosition() {
        return position;
    }

    @Override
    public ExpressionType getType() {
        return null;
    }

    @Override
    public List<Text> toTextList() {
        return null;
    }

    @Override
    public Expression getMatch() {return null;}
}

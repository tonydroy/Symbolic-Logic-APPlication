package slapp.editor.parser.symbols;

import javafx.scene.text.Text;
import slapp.editor.parser.Expression;
import slapp.editor.parser.ExpressionType;
import slapp.editor.parser.ParseUtilities;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class UnivSym extends LanguageElement implements Expression {
    private static final long serialVersionUID = 100L;

    ExpressionType type;
    String baseStr;



    public UnivSym(String baseStr) {
        this.baseStr = baseStr;
    }

    public String getBaseStr() {
        return baseStr;
    }

    @Override
    public ExpressionType getType() { return type; }

    @Override
    public Expression getMatch() { return this; }

    @Override
    public List<Text> toTextList() {
        List<Text> textList = new ArrayList<>();
        if (!baseStr.isEmpty()) textList.add(ParseUtilities.newRegularText(baseStr));
        return textList;
    }

    @Override
    public String toString() {
        return baseStr;
    }

    @Override
    public boolean equals(Object o) { return false;}


    @Override
    public int hashCode() {
        return 0;
    }

}

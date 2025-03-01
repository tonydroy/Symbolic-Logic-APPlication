package slapp.editor.parser;

import com.gluonhq.richtextarea.model.TextDecoration;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static slapp.editor.parser.ExpressionType.ORIGINAL_ELEMENT;

public class OriginalElement implements Expression {

    ExpressionType type = ORIGINAL_ELEMENT;
    TextDecoration decoration;
    String elementStr;


    OriginalElement(String elementStr, TextDecoration decoration) {
        this.elementStr = elementStr;
        this.decoration = decoration;
    }

    public String getElementStr() {     return elementStr;  }

    public boolean isSuperscript() {
        return decoration.isSuperscript() || decoration.isTransSuperscript();
    }

    public boolean isSubscript() {
        return decoration.isSubscript() || decoration.isTransSubscript();
    }

    public boolean isNormal() {
        return !isSubscript() && !isSuperscript();
    }

    @Override
    public Expression getMatch() {return this;}

    @Override
    public ExpressionType getType() { return type; }

    @Override
    public String toString() {
        return "element string: " + elementStr + "(" + elementStr.codePointAt(0) +")" + " Dec: " + decoration.toString();

    }

    @Override
    public List<Text> toTextList() {
        List<Text> textList = new ArrayList<>();
        if (isSubscript()) textList = Collections.singletonList(ParseUtilities.newSubscriptText(elementStr));
        if (isSuperscript()) textList = Collections.singletonList(ParseUtilities.newSuperscriptText(elementStr));
        if (isNormal()) textList = Collections.singletonList(ParseUtilities.newRegularText(elementStr));
        return textList;
    }

}

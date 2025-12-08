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
    int position;


    OriginalElement(String elementStr, TextDecoration decoration, int position) {
        this.elementStr = elementStr;
        this.decoration = decoration;
        this.position = position;
    }

    public String getElementStr() {     return elementStr;  }

    public boolean isSuperscript() {
        return decoration.isSuperscript() || decoration.isTransSuperscript();
    }

    public boolean isShiftedSuperscript() {
        return decoration.isTransSuperscript();
    }

    public boolean isSubscript() {
        return decoration.isSubscript() || decoration.isTransSubscript();
    }

    public boolean isShiftedSubscript() {
        return decoration.isTransSubscript();
    }

    public boolean isNormal() {
        return !isSubscript() && !isSuperscript();
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    @Override
    public Expression getMatch() {return this;}

    @Override
    public ExpressionType getType() { return type; }

    @Override
    public String toString() {
        return elementStr;
    }

    @Override
    public List<Text> toTextList() {
        List<Text> textList = new ArrayList<>();

        if (isSubscript()) {
            if (isShiftedSubscript()) textList = Collections.singletonList(ParseUtilities.newShiftedSubscriptText(elementStr));
            else  textList = Collections.singletonList(ParseUtilities.newSubscriptText(elementStr));
        }
        else if (isSuperscript()) {
            if (isShiftedSuperscript())textList = Collections.singletonList(ParseUtilities.newShiftedSubscriptText(elementStr));
            else textList = Collections.singletonList(ParseUtilities.newSuperscriptText(elementStr));
        }
        else if (isNormal()) textList = Collections.singletonList(ParseUtilities.newRegularText(elementStr));

        return textList;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o instanceof OriginalElement) {
            OriginalElement other = (OriginalElement) o;
            return elementStr.equals(other.elementStr) && decoration.equals(other.decoration);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return elementStr.hashCode() + decoration.hashCode();
    }


}

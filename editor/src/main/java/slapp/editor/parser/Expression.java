package slapp.editor.parser;

import javafx.scene.text.Text;

import java.util.List;

public interface Expression {


    ExpressionType getType();

    List<Text> toTextList();

}

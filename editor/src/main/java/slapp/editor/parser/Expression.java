package slapp.editor.parser;

import javafx.scene.text.Text;

import java.util.List;

public interface Expression {


    ExpressionType getType();

    default List<Expression> getChildren() {return null;}

    default int getLevel() {return -1;}

    List<Text> toTextList();



}

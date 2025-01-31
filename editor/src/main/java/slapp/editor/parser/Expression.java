package slapp.editor.parser;

import javafx.scene.text.TextFlow;

public interface Expression {


    ExpressionType getType();

    TextFlow toTextFlow();

}

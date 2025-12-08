package slapp.editor.simpleTests;

import com.gluonhq.richtextarea.RichTextArea;
import com.gluonhq.richtextarea.model.Document;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Spinner;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import slapp.editor.decorated_rta.BoxedDRTA;
import slapp.editor.decorated_rta.DecoratedRTA;

import java.util.List;

public class SimpleEditor {
    Spinner statementHeightSpinner;

    public SimpleEditor(Stage stage) {
        BoxedDRTA bdrta = new BoxedDRTA();
        DecoratedRTA drta = bdrta.getDRTA();
        RichTextArea rta = drta.getEditor();
        rta.setPrefHeight(300);
        rta.setPrefWidth(500);

        Document doc = new Document("");
        rta.getActionFactory().open(doc).execute(new ActionEvent());
        rta.setPadding(new Insets(10));


        Button button = new Button("Dump Txt");
        button.setPadding(new Insets(10));
        button.setOnAction(e -> {
                    rta.getActionFactory().saveNow().execute(new ActionEvent());
                    Document document = rta.getDocument();
                    System.out.println(document.getText());
                    System.out.println(document);
                });





        VBox box = new VBox(10, button, rta);





        Scene scene = new Scene(box);
        stage.setScene(scene);
        stage.show();
    }
}

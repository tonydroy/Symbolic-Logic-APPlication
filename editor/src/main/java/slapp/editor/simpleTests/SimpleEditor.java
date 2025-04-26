package slapp.editor.simpleTests;

import com.gluonhq.richtextarea.RichTextArea;
import com.gluonhq.richtextarea.model.Document;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import slapp.editor.decorated_rta.BoxedDRTA;
import slapp.editor.decorated_rta.DecoratedRTA;

import java.util.List;

public class SimpleEditor {

    public SimpleEditor(Stage stage) {
        BoxedDRTA bdrta = new BoxedDRTA();
        DecoratedRTA drta = bdrta.getDRTA();
        RichTextArea rta = drta.getEditor();
        rta.setPrefHeight(100);
        rta.setPrefWidth(400);
        Document doc = new Document("");
        rta.getActionFactory().open(doc).execute(new ActionEvent());
        rta.setPadding(new Insets(10));

        Button button = new Button("Dump Txt");
        button.setPadding(new Insets(10));
        button.setOnAction(e -> {
            rta.getActionFactory().saveNow().execute(new ActionEvent());
            Document document = rta.getDocument();
            System.out.println(document.getText());

            /*
            for (char c : document.getText().toCharArray()) {
                System.out.print(String.format("%04x", (int) c));
                System.out.print(' ');
            }
            System.out.println();
             */

        });


        VBox box = new VBox(10, button, bdrta.getBoxedRTA());
        Scene scene = new Scene(box);
        stage.setScene(scene);
        stage.show();
    }
}

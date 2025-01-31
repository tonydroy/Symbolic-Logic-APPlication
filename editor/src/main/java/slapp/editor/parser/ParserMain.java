package slapp.editor.parser;

import com.gluonhq.richtextarea.RichTextArea;
import com.gluonhq.richtextarea.model.Document;
import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import slapp.editor.decorated_rta.DecoratedRTA;

import java.util.List;

import static javafx.application.Application.launch;

public class ParserMain {

    public ParserMain(Stage stage) {

        DecoratedRTA drta = new DecoratedRTA();

        RichTextArea rta = drta.getEditor();
        rta.setPrefHeight(400);
        rta.setPrefWidth(400);

        Document doc = new Document("");
        rta.getActionFactory().open(doc).execute(new ActionEvent());
        rta.setPadding(new Insets(20));

        Button button = new Button("Document");
        button.setPadding(new Insets(20));
        button.setOnAction(e -> {
            rta.getActionFactory().saveNow().execute(new ActionEvent());
            Document doc1 = rta.getDocument();


            for (int i = 0; i < ParseUtilities.getElements(doc1).size(); i ++) {
                System.out.println(i + ": " + ParseUtilities.getElements(doc1).get(i));
            }



  //          System.out.println(doc1);
        });

        VBox box = new VBox(10, button, rta);
        Scene scene = new Scene(box);
        stage.setScene(scene);
        stage.show();

    }

    void runTest() {    }


}

package slapp.editor.simpleTests;

import com.gluonhq.richtextarea.RichTextArea;
import com.gluonhq.richtextarea.model.Document;
import javafx.event.ActionEvent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.geometry.Insets;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import slapp.editor.decorated_rta.BoxedDRTA;
import slapp.editor.simpleTests.RegexTest;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexTest {

    public RegexTest(Stage primaryStage) {

        BoxedDRTA bdrta1 = new BoxedDRTA();
        BoxedDRTA bdrta2 = new BoxedDRTA();
        RichTextArea rta1 = bdrta1.getRTA();
        RichTextArea rta2 = bdrta2.getRTA();
        rta1.setPrefHeight(100);
        rta1.setPrefWidth(400);
        rta2.setPrefHeight(100);
        rta2.setPrefWidth(400);

        Button button = new Button("try");
        button.setOnAction(e -> {

            rta1.getActionFactory().saveNow().execute(new ActionEvent());
            Document doc1 = rta1.getDocument();
            rta2.getActionFactory().saveNow().execute(new ActionEvent());
            Document doc2 = rta2.getDocument();

            String regex = doc1.getText();
            String string = doc2.getText();

            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(string);
            System.out.println("match: " + matcher.matches());
        });

        VBox vBox = new VBox(20,button, rta1, rta2);
        vBox.setPadding(new Insets(20,20,20,20));

        Scene scene = new Scene(vBox);
        primaryStage.setScene(scene);
        primaryStage.setTitle("RegexTest");
        primaryStage.show();






    }


}

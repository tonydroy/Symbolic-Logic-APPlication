package slapp.editor.parser;

import com.gluonhq.richtextarea.RichTextArea;
import com.gluonhq.richtextarea.model.Document;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import slapp.editor.EditorAlerts;
import slapp.editor.decorated_rta.DecoratedRTA;

import java.util.ArrayList;
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

            ParseUtilities parse = new ParseUtilities();
            List<Expression> symbols = ParseUtilities.parseDoc(doc1);


            List<Text> texts = new ArrayList<>();
            for (Expression expr : symbols) {
                System.out.println(expr.getType() + ": " + expr);
                texts.addAll(expr.toTextList());
            }
            Text[] txt = new Text[texts.size()];
            texts.toArray(txt);
            TextFlow textFlow = new TextFlow(txt);
            EditorAlerts.showSimpleTxtFlowAlert("content", textFlow);

        });

        VBox box = new VBox(10, button, rta);
        Scene scene = new Scene(box);
        stage.setScene(scene);
        stage.show();

    }




}

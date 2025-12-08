package slapp.editor.m_parse;

import com.gluonhq.richtextarea.RichTextArea;
import com.gluonhq.richtextarea.model.Document;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import slapp.editor.decorated_rta.BoxedDRTA;
import slapp.editor.decorated_rta.DecoratedRTA;

import java.util.ArrayList;
import java.util.List;

public class MParserMain {
    String objectLangName = "\u2112\ud835\udcc6 (w/abv)";
    MSyntacticalFns mSyntacticalFns;

    public MParserMain(Stage stage) {

        BoxedDRTA bdrta = new BoxedDRTA();
        DecoratedRTA drta = bdrta.getDRTA();
        RichTextArea rta = drta.getEditor();
        rta.setPrefHeight(100);
        rta.setPrefWidth(400);
        Document doc = new Document("");
        rta.getActionFactory().open(doc).execute(new ActionEvent());
        rta.setPadding(new Insets(10));

        //test
        Button button1 = new Button("Parse Test");  //some meta sexprssion
        button1.setPadding(new Insets(20));
        button1.setOnAction(e -> {
            rta.getActionFactory().saveNow().execute(new ActionEvent());
            Document doc0 = rta.getDocument();

            List<Text> texts = new ArrayList<>();

            //to show parse contents
 //           mSyntacticalFns = new MSyntacticalFns();
            boolean success = mSyntacticalFns.setInterpBaseVarAssign(doc0, objectLangName);

            /*
            MParseUtilities mParseUtilities = new MParseUtilities();
            List<Expression> parseExpressions = mParseUtilities.parseDoc(doc0, objectLangName);
            if (parseExpressions.size() == 0) {
                texts.add(new Text("Empty"));
                texts.add(new Text("\n"));
            } else {
                for (Expression expr : parseExpressions) {
                    texts.add(new Text(expr.getType() + ": "));
                    texts.addAll(expr.toTextList());
                    texts.add(new Text("\n"));
                }
            }

            //to text flow and show
            Text[] txt = new Text[texts.size()];
            texts.toArray(txt);
            TextFlow textFlow = new TextFlow(txt);
            EditorAlerts.showSimpleTxtFlowAlert("Parser Content", textFlow);

             */


        });

        HBox buttonBox = new HBox(20, button1);
        VBox box = new VBox(10, buttonBox, bdrta.getBoxedRTA());
        Scene scene = new Scene(box);
        stage.setScene(scene);
        stage.show();

        stage2();
    }

    public void stage2() {
        BoxedDRTA bdrta = new BoxedDRTA();
        DecoratedRTA drta = bdrta.getDRTA();
        RichTextArea rta = drta.getEditor();
        rta.setPrefHeight(50);
        rta.setPrefWidth(400);
        Document doc = new Document("");
        rta.getActionFactory().open(doc).execute(new ActionEvent());
        rta.setPadding(new Insets(10));

        //test
        Button button1 = new Button("Formula Test");  //some meta sexprssion
        button1.setPadding(new Insets(20));
        button1.setOnAction(e -> {



            //to show parse contents

            boolean success = mSyntacticalFns.setFormulaPacket(bdrta);
        });

        HBox buttonBox = new HBox(20, button1);
        VBox box = new VBox(10, buttonBox, bdrta.getBoxedRTA());
        Scene scene = new Scene(box);
        Stage stage = new Stage();
        stage.setScene(scene);
        stage.show();
    }

}

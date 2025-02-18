package slapp.editor.parser;

import com.gluonhq.richtextarea.RichTextArea;
import com.gluonhq.richtextarea.model.Document;
import javafx.event.ActionEvent;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import slapp.editor.EditorAlerts;
import slapp.editor.decorated_rta.BoxedDRTA;
import slapp.editor.decorated_rta.DecoratedRTA;

import java.util.ArrayList;
import java.util.List;

import static javafx.application.Application.launch;

public class ParserMain {

    //for now
    String langName = "Lq_abv";

    public ParserMain(Stage stage) {

        BoxedDRTA bdrta = new BoxedDRTA();
        DecoratedRTA drta = bdrta.getDRTA();

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

            List<Text> texts = new ArrayList<>();


            //to show parse contents
            List<Expression> parseExpressions = ParseUtilities.parseDoc(doc1, langName);
            if (parseExpressions.size() == 0) {
                texts.add(new Text("Empty"));
                texts.add(new Text("\n"));
            }
            else {
                for (Expression expr : parseExpressions) {
                    texts.add(new Text(expr.getType() + ": "));
                    texts.addAll(expr.toTextList());
                    texts.add(new Text("\n"));
                }
            }



            //to show subformulas
            List<Expression> subs = SyntacticalFns.subFormulas(doc1, langName);
            texts.add(new Text("Subformulas: "));
            if (subs != null && subs.size() > 0) {
                texts.addAll(subs.get(0).toTextList());
                for (int i = 1; i < subs.size(); i++) {
                    texts.add(new Text(", "));
                    texts.addAll(subs.get(i).toTextList());
                }
            }
            texts.add(new Text("\n"));

            //to show atomic subformulas
            List<Expression> atomics = SyntacticalFns.atomicSubformulas(doc1, langName);
            texts.add(new Text("Atomic Subs: "));
            if (atomics != null && atomics.size() > 0) {
                texts.addAll(atomics.get(0).toTextList());
                for (int i = 1; i < atomics.size(); i++) {
                    texts.add(new Text(", "));
                    texts.addAll(atomics.get(i).toTextList());
                }
            }
            texts.add(new Text("\n"));

            //to show immediate subformulas
            List<Expression> immediateSubs = SyntacticalFns.immediateSubformulas(doc1, langName);
            texts.add(new Text("Immediate Subs: "));
            if (immediateSubs != null && immediateSubs.size() > 0) {
                texts.addAll(immediateSubs.get(0).toTextList());
                for (int i = 1; i < immediateSubs.size(); i++) {
                    texts.add(new Text(", "));
                    texts.addAll(immediateSubs.get(i).toTextList());
                }
            }
            texts.add(new Text("\n"));

            //to show main operator
            Expression mainOp = SyntacticalFns.mainOperator(doc1, langName);
            texts.add(new Text("Main Operator: "));
            if (mainOp != null) {
                texts.addAll(mainOp.toTextList());
            }
            texts.add(new Text("\n"));


            //to show unabbreviation
            Expression unabb = SyntacticalFns.unabbreviate(doc1, langName);
            texts.add(new Text("Unabb: "));
            if (unabb != null) {
                texts.addAll(unabb.toTextList());
            }
            texts.add(new Text("\n"));


            //to text flow and show
            Text[] txt = new Text[texts.size()];
            texts.toArray(txt);
            TextFlow textFlow = new TextFlow(txt);
            EditorAlerts.showSimpleTxtFlowAlert("Parser Content", textFlow);
        });

            VBox box = new VBox(10, button, bdrta.getBoxedRTA());
            Scene scene = new Scene(box);
            stage.setScene(scene);
            stage.show();





    }


}

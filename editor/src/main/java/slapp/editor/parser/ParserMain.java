package slapp.editor.parser;

import com.gluonhq.richtextarea.RichTextArea;
import com.gluonhq.richtextarea.model.Document;
import javafx.event.ActionEvent;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
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
    String langName = "Lnt";

    public ParserMain(Stage stage) {

        BoxedDRTA bdrta = new BoxedDRTA();
        DecoratedRTA drta = bdrta.getDRTA();
        RichTextArea rta = drta.getEditor();
        rta.setPrefHeight(100);
        rta.setPrefWidth(400);
        Document doc = new Document("");
        rta.getActionFactory().open(doc).execute(new ActionEvent());
        rta.setPadding(new Insets(10));

        BoxedDRTA bdrta2 = new BoxedDRTA();
        DecoratedRTA drta2 = bdrta2.getDRTA();
        RichTextArea rta2 = drta2.getEditor();
        rta2.setPrefHeight(100);
        rta2.setPrefWidth(400);
        Document doc2 = new Document("");
        rta2.getActionFactory().open(doc2).execute(new ActionEvent());
        rta2.setPadding(new Insets(10));

        BoxedDRTA bdrta3 = new BoxedDRTA();
        DecoratedRTA drta3 = bdrta3.getDRTA();
        RichTextArea rta3 = drta3.getEditor();
        rta3.setPrefHeight(100);
        rta3.setPrefWidth(400);
        Document doc3 = new Document("");
        rta3.getActionFactory().open(doc3).execute(new ActionEvent());
        rta3.setPadding(new Insets(10));


        //test1
        Button button1 = new Button("Parse Test\n P");  //formula or term P
        button1.setPadding(new Insets(20));
        button1.setOnAction(e -> {
            rta.getActionFactory().saveNow().execute(new ActionEvent());
            Document doc0 = rta.getDocument();

            List<Text> texts = new ArrayList<>();


            //to show parse contents
            List<Expression> parseExpressions = ParseUtilities.parseDoc(doc0, langName);
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
            List<Expression> subs = SyntacticalFns.subFormulasDoc(doc0, langName);
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
            List<Expression> atomics = SyntacticalFns.atomicSubformulas(doc0, langName);
            texts.add(new Text("Atomic Subs: "));
            if (atomics != null && atomics.size() > 0) {
                texts.addAll(atomics.get(0).toTextList());
                for (int i = 1; i < atomics.size(); i++) {
                    texts.add(new Text(", "));
                    texts.addAll(atomics.get(i).toTextList());
                }
            }
            texts.add(new Text("\n"));

            //to show atomic level
            int level = SyntacticalFns.atomicLevel(doc0, langName);
            texts.add(new Text("Atomic Level: "));
            if (level >= 0) {
                texts.add(new Text(String.valueOf(level)));
            }
            texts.add(new Text("\n"));

            //to show immediate subformulas
            List<Expression> immediateSubs = SyntacticalFns.immediateSubformulas(doc0, langName);
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
            Expression mainOp = SyntacticalFns.mainOperator(doc0, langName);
            texts.add(new Text("Main Operator: "));
            if (mainOp != null) {
                texts.addAll(mainOp.toTextList());
            }
            texts.add(new Text("\n"));


            //to show unabbreviation
            Expression unabb = SyntacticalFns.unabbreviate(doc0, langName);
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


        //test2

        Button button2 = new Button("Sub Test\nP, t, s");  //formula (term) P with term t replaced by term s
        button2.setPadding(new Insets(20));
        button2.setOnAction(e -> {
            rta.getActionFactory().saveNow().execute(new ActionEvent());
            Document newDoc = rta.getDocument();

            rta2.getActionFactory().saveNow().execute(new ActionEvent());
            Document newDoc2 = rta2.getDocument();

            rta3.getActionFactory().saveNow().execute(new ActionEvent());
            Document newDoc3 = rta3.getDocument();

           Expression result = SyntacticalFns.substituteDocTerms(newDoc, newDoc2, newDoc3, langName);


           //to text flow and show
            List<Text> resultTxts = result.toTextList();
            Text[] resultArray = new Text[resultTxts.size()];
            resultTxts.toArray(resultArray);
            TextFlow textFlow = new TextFlow(resultArray);
            EditorAlerts.showSimpleTxtFlowAlert("Test 2", textFlow);
        });


        //test 3
        Button button3 = new Button("Free In\nP, t");  //formula (term) has term t free
        button3.setPadding(new Insets(20));
        button3.setOnAction(e -> {
            rta.getActionFactory().saveNow().execute(new ActionEvent());
            Document newDoc = rta.getDocument();
            rta2.getActionFactory().saveNow().execute(new ActionEvent());
            Document newDoc2 = rta2.getDocument();

            System.out.println("term " + newDoc2.getText() + " is free in " + newDoc.getText() + ": " + SyntacticalFns.docTermFreeInFormula(newDoc, newDoc2, langName));
        });

        //test 4
        Button button4 = new Button("Free For\nP, t, s");  //formula (term) has term s free for term t
        button4.setPadding(new Insets(20));
        button4.setOnAction(e -> {
            rta.getActionFactory().saveNow().execute(new ActionEvent());
            Document newDoc = rta.getDocument();
            rta2.getActionFactory().saveNow().execute(new ActionEvent());
            Document newDoc2 = rta2.getDocument();
            rta3.getActionFactory().saveNow().execute(new ActionEvent());
            Document newDoc3 = rta3.getDocument();

           System.out.println("freefor: " + SyntacticalFns.freeForDoc(newDoc, newDoc2, newDoc3, langName));


        });


        HBox buttonBox = new HBox(20, button1, button2, button3, button4);
            VBox box = new VBox(10, buttonBox, bdrta.getBoxedRTA(), bdrta2.getBoxedRTA(), bdrta3.getBoxedRTA());
            Scene scene = new Scene(box);
            stage.setScene(scene);
            stage.show();





    }


}

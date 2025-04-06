package slapp.editor.derivation.DerSystems;

import com.gluonhq.richtextarea.RichTextArea;
import com.gluonhq.richtextarea.model.Document;
import javafx.event.ActionEvent;
import javafx.scene.text.Text;
import javafx.util.Pair;
import slapp.editor.decorated_rta.BoxedDRTA;
import slapp.editor.derivation.DerivationCheck;
import slapp.editor.derivation.ViewLine;
import slapp.editor.parser.*;

import java.util.Collections;
import java.util.List;

public class Commutation extends DerivationRule {

    public Commutation(String name, String rgexTemplate) {
        super(name, rgexTemplate);
        this.premAssp = false;
    }

    public Pair<Boolean, List<Text>> applies(DerivationCheck checker, ViewLine line, String... inputs) {

        Language objectLanguage = checker.getDerivationRuleset().getObjectLanguage();
        Language metaLanguage = checker.getDerivationRuleset().getMetaLanguage();

        BoxedDRTA lineBDRTA = line.getLineContentBoxedDRTA();
        RichTextArea lineRTA = lineBDRTA.getRTA();
        lineRTA.getActionFactory().saveNow().execute(new ActionEvent());
        Document lineDoc = lineRTA.getDocument();
        List<Expression> lineExpressions = ParseUtilities.parseDoc(lineDoc, objectLanguage.getNameString());
        Expression lineExpression = lineExpressions.get(0);

        ViewLine inputLine1;
        Pair<ViewLine, List<Text>> line1Pair = checker.getLineFromLabel(inputs[0]);
        if (line1Pair.getKey() != null) inputLine1 = line1Pair.getKey();
        else return new Pair(false, line1Pair.getValue());

        BoxedDRTA inputBDRTA1 = inputLine1.getLineContentBoxedDRTA();
        RichTextArea inputRTA1 = inputBDRTA1.getRTA();
        inputRTA1.getActionFactory().saveNow().execute(new ActionEvent());
        Document inputDoc1 = inputRTA1.getDocument();
        if (inputDoc1.getText().equals("")) return new Pair(false, Collections.singletonList(ParseUtilities.newRegularText("Justification cannot appeal to the empty line (" + inputs[0] + ").")));
        List<Expression> inputExpressions = ParseUtilities.parseDoc(inputDoc1, objectLanguage.getNameString());
        Expression inputExpression = inputExpressions.get(0);

        Pair<Boolean, List<Text>> accessibilityPair1 = checker.lineIsAccessibleTo(inputLine1, line);
        if (!accessibilityPair1.getKey()) {
            return accessibilityPair1;
        }

        String openBracketString = metaLanguage.getOpenBracket1();
        String closeBracketString = metaLanguage.getCloseBracket1();

        Document leftForm1 = new Document(openBracketString + "\ud835\udcab \u2227 \ud835\udcac" + closeBracketString);
        Document rightForm1 = new Document(openBracketString + "\ud835\udcac \u2227 \ud835\udcab" + closeBracketString);
        Document leftForm2 = new Document(openBracketString + "\ud835\udcab \u2228 \ud835\udcac" + closeBracketString);
        Document rightForm2 = new Document( openBracketString + "\ud835\udcac \u2228 \ud835\udcab" + closeBracketString);
        Document leftForm3 = new Document(openBracketString + "\ud835\udcab \u2194 \ud835\udcac"+ closeBracketString);
        Document rightForm3 = new Document(openBracketString + "\ud835\udcac \u2194 \ud835\udcab"+ closeBracketString);


        Expression leftForm1Exp = null;
        Expression rightForm1Exp = null;
        Expression leftForm2Exp = null;
        Expression rightForm2Exp = null;
        Expression leftForm3Exp = null;
        Expression rightForm3Exp = null;

        List<Expression> leftExpressions1 = ParseUtilities.parseDoc(leftForm1, metaLanguage.getNameString());
        if (leftExpressions1 == null || leftExpressions1.size() != 1) {
            System.out.println("problem parsing left meta form");
            return new Pair(false, "problem parsing left meta form");
        }
        else leftForm1Exp = leftExpressions1.get(0);

        List<Expression> rightExpressions1 = ParseUtilities.parseDoc(rightForm1, metaLanguage.getNameString());
        if (rightExpressions1 == null || rightExpressions1.size() != 1) {
            System.out.println("problem parsing right meta form");
            return new Pair(false, "problem parsing right meta form");
        }
        else rightForm1Exp = rightExpressions1.get(0);

        List<Expression> leftExpressions2 = ParseUtilities.parseDoc(leftForm2, metaLanguage.getNameString());
        if (leftExpressions2 == null || leftExpressions2.size() != 1) {
            System.out.println("problem parsing left meta form");
            return new Pair(false, "problem parsing left meta form");
        }
        else leftForm2Exp = leftExpressions2.get(0);

        List<Expression> rightExpressions2 = ParseUtilities.parseDoc(rightForm2, metaLanguage.getNameString());
        if (rightExpressions2 == null || rightExpressions2.size() != 1) {
            System.out.println("problem parsing right meta form");
            return new Pair(false, "problem parsing right meta form");
        }
        else rightForm2Exp = rightExpressions2.get(0);

        List<Expression> leftExpressions3 = ParseUtilities.parseDoc(leftForm3, metaLanguage.getNameString());
        if (leftExpressions3 == null || leftExpressions3.size() != 1) {
            System.out.println("problem parsing left meta form");
            return new Pair(false, "problem parsing left meta form");
        }
        else leftForm3Exp = leftExpressions3.get(0);

        List<Expression> rightExpressions3 = ParseUtilities.parseDoc(rightForm3, metaLanguage.getNameString());
        if (rightExpressions3 == null || rightExpressions3.size() != 1) {
            System.out.println("problem parsing right meta form");
            return new Pair(false, "problem parsing right meta form");
        }
        else rightForm3Exp = rightExpressions3.get(0);

        //symmetrical so only need try one direction

        //first version
        MatchUtilities.clearFormMatch();
        try {
            boolean resultGood1 = MatchUtilities.replacementCheck(leftForm1Exp, rightForm1Exp, inputExpression, lineExpression);
            if (resultGood1) return new Pair(true, null);
        }
        catch (TextMessageException e) { }


        //second version
        MatchUtilities.clearFormMatch();
        try {
            boolean resultGood3 = MatchUtilities.replacementCheck(leftForm2Exp, rightForm2Exp, inputExpression, lineExpression);
            if (resultGood3) return new Pair(true, null);
        }
        catch (TextMessageException e) {  }


        //third version
        MatchUtilities.clearFormMatch();
        try {
            boolean resultGood5 = MatchUtilities.replacementCheck(leftForm3Exp, rightForm3Exp, inputExpression, lineExpression);
            if (resultGood5) return new Pair(true, null);
        }
        catch (TextMessageException e) {  }



        return new Pair(false, Collections.singletonList(ParseUtilities.newRegularText(("Lines (" + inputLine1.getLineNumberLabel().getText() + ") and (" + line.getLineNumberLabel().getText() + ") are not of the right form for application of " + getName() + "."))));

    }


}

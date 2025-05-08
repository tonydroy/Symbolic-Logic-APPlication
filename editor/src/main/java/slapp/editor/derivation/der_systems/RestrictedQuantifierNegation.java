package slapp.editor.derivation.der_systems;

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

public class RestrictedQuantifierNegation extends DerivationRule {

    public RestrictedQuantifierNegation(String name, String rgexTemplate) {
        super(name, rgexTemplate);
        this.premAssp = false;
    }

    public Pair<Boolean, List<Text>> applies(DerivationCheck checker, ViewLine line, String... inputs) {

        Language objectLanguage = checker.getDerivationRuleset().getObjectLanguage();
        Language metaLanguage = checker.getDerivationRuleset().getMetaLanguage();

        String openBracketString = metaLanguage.getOpenBracket1();
        String closeBracketString = metaLanguage.getCloseBracket1();
        String dividerString = metaLanguage.getDividerSymbol();

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



        Document leftForm = new Document("\u223c" + openBracketString + "\u2200\ud835\udccd" + dividerString + "\u212c" + closeBracketString + "\ud835\udcab");
        Document rightForm = new Document( openBracketString + "\u2203\ud835\udccd" + dividerString + "\u212c" + closeBracketString + "\u223c\ud835\udcab");

        Document leftForm2 = new Document("\u223c" + openBracketString + "\u2203\ud835\udccd" + dividerString + "\u212c" + closeBracketString + "\ud835\udcab" );
        Document rightForm2 = new Document(openBracketString + "\u2200\ud835\udccd" + dividerString + "\u212c" + closeBracketString + "\u223c\ud835\udcab");

        Document leftFormA = new Document("∼(∀\uD835\uDCCD \uE8A4 \uD835\uDCC9)\uD835\uDCAB");
        Document rightFormA = new Document("(∃\uD835\uDCCD \uE8A4 \uD835\uDCC9)∼\uD835\uDCAB");

        Document leftFormA2 = new Document("∼(∃\uD835\uDCCD \uE8A4 \uD835\uDCC9)\uD835\uDCAB");
        Document rightFormA2 = new Document("(∀\uD835\uDCCD \uE8A4 \uD835\uDCC9)∼\uD835\uDCAB");

        Document leftFormB = new Document("∼(∀\uD835\uDCCD \uE8A6 \uD835\uDCC9)\uD835\uDCAB");
        Document rightFormB = new Document("(∃\uD835\uDCCD \uE8A6 \uD835\uDCC9)∼\uD835\uDCAB");

        Document leftFormB2 = new Document("∼(∃\uD835\uDCCD \uE8A6 \uD835\uDCC9)\uD835\uDCAB");
        Document rightFormB2 = new Document("(∀\uD835\uDCCD \uE8A6 \uD835\uDCC9)∼\uD835\uDCAB");

        //

        List<Expression> leftExpressions = ParseUtilities.parseDoc(leftForm, metaLanguage.getNameString());
        Expression leftFormExp = leftExpressions.get(0);

        List<Expression> rightExpressions = ParseUtilities.parseDoc(rightForm, metaLanguage.getNameString());
        Expression rightFormExp = rightExpressions.get(0);

        List<Expression> leftExpressions2 = ParseUtilities.parseDoc(leftForm2, metaLanguage.getNameString());
        Expression leftFormExp2 = leftExpressions2.get(0);

        List<Expression> rightExpressions2 = ParseUtilities.parseDoc(rightForm2, metaLanguage.getNameString());
        Expression rightFormExp2 = rightExpressions2.get(0);

        //
        List<Expression> leftExpressionsA = ParseUtilities.parseDoc(leftFormA, metaLanguage.getNameString());
        Expression leftFormExpA = leftExpressionsA.get(0);

        List<Expression> rightExpressionsA = ParseUtilities.parseDoc(rightFormA, metaLanguage.getNameString());
        Expression rightFormExpA = rightExpressionsA.get(0);

        List<Expression> leftExpressionsA2 = ParseUtilities.parseDoc(leftFormA2, metaLanguage.getNameString());
        Expression leftFormExpA2 = leftExpressionsA2.get(0);

        List<Expression> rightExpressionsA2 = ParseUtilities.parseDoc(rightFormA2, metaLanguage.getNameString());
        Expression rightFormExpA2 = rightExpressionsA2.get(0);

        //

        List<Expression> leftExpressionsB = ParseUtilities.parseDoc(leftFormB, metaLanguage.getNameString());
        Expression leftFormExpB = leftExpressionsB.get(0);

        List<Expression> rightExpressionsB = ParseUtilities.parseDoc(rightFormB, metaLanguage.getNameString());
        Expression rightFormExpB = rightExpressionsB.get(0);

        List<Expression> leftExpressionsB2 = ParseUtilities.parseDoc(leftFormB2, metaLanguage.getNameString());
        Expression leftFormExpB2 = leftExpressionsB2.get(0);

        List<Expression> rightExpressionsB2 = ParseUtilities.parseDoc(rightFormB2, metaLanguage.getNameString());
        Expression rightFormExpB2 = rightExpressionsB2.get(0);




        //first form
        MatchUtilities.clearFormMatch();
        try {
            boolean resultGood1 = MatchUtilities.replacementCheck(leftFormExp, rightFormExp, inputExpression, lineExpression);
            if (resultGood1) return new Pair(true, null);
        }
        catch (TextMessageException e) {  }

        MatchUtilities.clearFormMatch();
        try {
            boolean resultGood1 = MatchUtilities.replacementCheck(rightFormExp, leftFormExp, inputExpression, lineExpression);
            if (resultGood1) return new Pair(true, null);
        }
        catch (TextMessageException e) {  }

        //second form
        MatchUtilities.clearFormMatch();
        try {
            boolean resultGood2 = MatchUtilities.replacementCheck(leftFormExp2, rightFormExp2, inputExpression, lineExpression);
            if (resultGood2) return new Pair(true, null);
        }
        catch (TextMessageException e) {  }

        MatchUtilities.clearFormMatch();
        try {
            boolean resultGood2 = MatchUtilities.replacementCheck(rightFormExp2, leftFormExp2, inputExpression, lineExpression);
            if (resultGood2) return new Pair(true, null);
        }
        catch (TextMessageException e) {  }

        //A forms
        //first form
        MatchUtilities.clearFormMatch();
        try {
            boolean resultGood1 = MatchUtilities.replacementCheck(leftFormExpA, rightFormExpA, inputExpression, lineExpression);
            if (resultGood1) return new Pair(true, null);
        }
        catch (TextMessageException e) {  }

        MatchUtilities.clearFormMatch();
        try {
            boolean resultGood1 = MatchUtilities.replacementCheck(rightFormExpA, leftFormExpA, inputExpression, lineExpression);
            if (resultGood1) return new Pair(true, null);
        }
        catch (TextMessageException e) {  }

        //second form
        MatchUtilities.clearFormMatch();
        try {
            boolean resultGood2 = MatchUtilities.replacementCheck(leftFormExpA2, rightFormExpA2, inputExpression, lineExpression);
            if (resultGood2) return new Pair(true, null);
        }
        catch (TextMessageException e) {  }

        MatchUtilities.clearFormMatch();
        try {
            boolean resultGood2 = MatchUtilities.replacementCheck(rightFormExpA2, leftFormExpA2, inputExpression, lineExpression);
            if (resultGood2) return new Pair(true, null);
        }
        catch (TextMessageException e) {  }

        //B forms
        //first form
        MatchUtilities.clearFormMatch();
        try {
            boolean resultGood1 = MatchUtilities.replacementCheck(leftFormExpB, rightFormExpB, inputExpression, lineExpression);
            if (resultGood1) return new Pair(true, null);
        }
        catch (TextMessageException e) {  }

        MatchUtilities.clearFormMatch();
        try {
            boolean resultGood1 = MatchUtilities.replacementCheck(rightFormExpB, leftFormExpB, inputExpression, lineExpression);
            if (resultGood1) return new Pair(true, null);
        }
        catch (TextMessageException e) {  }

        //second form
        MatchUtilities.clearFormMatch();
        try {
            boolean resultGood2 = MatchUtilities.replacementCheck(leftFormExpB2, rightFormExpB2, inputExpression, lineExpression);
            if (resultGood2) return new Pair(true, null);
        }
        catch (TextMessageException e) {  }

        MatchUtilities.clearFormMatch();
        try {
            boolean resultGood2 = MatchUtilities.replacementCheck(rightFormExpB2, leftFormExpB2, inputExpression, lineExpression);
            if (resultGood2) return new Pair(true, null);
        }
        catch (TextMessageException e) {  }




        return new Pair(false, Collections.singletonList(ParseUtilities.newRegularText(("Lines (" + inputLine1.getLineNumberLabel().getText() + ") and (" + line.getLineNumberLabel().getText() + ") are not of the right form for application of " + getName() + "."))));


    }


}

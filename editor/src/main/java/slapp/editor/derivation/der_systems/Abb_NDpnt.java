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

public class Abb_NDpnt extends DerivationRule {

    public Abb_NDpnt(String name, String rgexTemplate) {
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



        //bounded univ need 1:2, 1:3, 2:3 (each with both directions)
        Document boundedLeqUniv1 = new Document("(∀\uD835\uDCCD \uE8A6 \uD835\uDCC9)\uD835\uDCAB");
        Document boundedLeqUniv2 = new Document("(∀\uD835\uDCCD : \uD835\uDCCD \uE8A6 \uD835\uDCC9)\uD835\uDCAB");
        Document boundedLeqUniv3 = new Document("∀\uD835\uDCCD(\uD835\uDCCD \uE8A6 \uD835\uDCC9 → \uD835\uDCAB)");

        Document boundedLssUniv1 = new Document("(∀\uD835\uDCCD \uE8A4 \uD835\uDCC9)\uD835\uDCAB");
        Document boundedLssUniv2 = new Document("(∀\uD835\uDCCD : \uD835\uDCCD \uE8A4 \uD835\uDCC9)\uD835\uDCAB");
        Document boundedLssUniv3 = new Document("∀\uD835\uDCCD(\uD835\uDCCD \uE8A4 \uD835\uDCC9 → \uD835\uDCAB)");

        //bounded exis
        Document boundedLeqExis1 = new Document("(∃\uD835\uDCCD \uE8A6 \uD835\uDCC9)\uD835\uDCAB");
        Document boundedLeqExis2 = new Document("(∃\uD835\uDCCD : \uD835\uDCCD \uE8A6 \uD835\uDCC9)\uD835\uDCAB");
        Document boundedLeqExis3 = new Document("∃\uD835\uDCCD(\uD835\uDCCD \uE8A6 \uD835\uDCC9 ∧ \uD835\uDCAB)");

        Document boundedLssExis1 = new Document("(∃\uD835\uDCCD \uE8A4 \uD835\uDCC9)\uD835\uDCAB");
        Document boundedLssExis2 = new Document("(∃\uD835\uDCCD : \uD835\uDCCD \uE8A4 \uD835\uDCC9)\uD835\uDCAB");
        Document boundedLssExis3 = new Document("∃\uD835\uDCCD(\uD835\uDCCD \uE8A4 \uD835\uDCC9 ∧ \uD835\uDCAB)");

        List<Expression> boundedLeqUniv1Expressions = ParseUtilities.parseDoc(boundedLeqUniv1, metaLanguage.getNameString());
        Expression boundedLeqUniv1Exp = boundedLeqUniv1Expressions.get(0);
        List<Expression> boundedLeqUniv2Expressions = ParseUtilities.parseDoc(boundedLeqUniv2, metaLanguage.getNameString());
        Expression boundedLeqUniv2Exp = boundedLeqUniv2Expressions.get(0);
        List<Expression> boundedLeqUniv3Expressions = ParseUtilities.parseDoc(boundedLeqUniv3, metaLanguage.getNameString());
        Expression boundedLeqUniv3Exp = boundedLeqUniv3Expressions.get(0);

        List<Expression> boundedLssUniv1Expressions = ParseUtilities.parseDoc(boundedLssUniv1, metaLanguage.getNameString());
        Expression boundedLssUniv1Exp = boundedLssUniv1Expressions.get(0);
        List<Expression> boundedLssUniv2Expressions = ParseUtilities.parseDoc(boundedLssUniv2, metaLanguage.getNameString());
        Expression boundedLssUniv2Exp = boundedLssUniv2Expressions.get(0);
        List<Expression> boundedLssUniv3Expressions = ParseUtilities.parseDoc(boundedLssUniv3, metaLanguage.getNameString());
        Expression boundedLssUniv3Exp = boundedLssUniv3Expressions.get(0);

        List<Expression> boundedLeqExis1Expressions = ParseUtilities.parseDoc(boundedLeqExis1, metaLanguage.getNameString());
        Expression boundedLeqExis1Exp = boundedLeqExis1Expressions.get(0);
        List<Expression> boundedLeqExis2Expressions = ParseUtilities.parseDoc(boundedLeqExis2, metaLanguage.getNameString());
        Expression boundedLeqExis2Exp = boundedLeqExis2Expressions.get(0);
        List<Expression> boundedLeqExis3Expressions = ParseUtilities.parseDoc(boundedLeqExis3, metaLanguage.getNameString());
        Expression boundedLeqExis3Exp = boundedLeqExis3Expressions.get(0);

        List<Expression> boundedLssExis1Expressions = ParseUtilities.parseDoc(boundedLssExis1, metaLanguage.getNameString());
        Expression boundedLssExis1Exp = boundedLssExis1Expressions.get(0);
        List<Expression> boundedLssExis2Expressions = ParseUtilities.parseDoc(boundedLssExis2, metaLanguage.getNameString());
        Expression boundedLssExis2Exp = boundedLssExis2Expressions.get(0);
        List<Expression> boundedLssExis3Expressions = ParseUtilities.parseDoc(boundedLssExis3, metaLanguage.getNameString());
        Expression boundedLssExis3Exp = boundedLssExis3Expressions.get(0);


        //for each group of three need 1:2, 1:3, 2:3 (each with both directions)

        // bounded leq unv
        MatchUtilities.clearFormMatch();
        try {
            boolean resultGood = MatchUtilities.replacementCheck(boundedLeqUniv1Exp, boundedLeqUniv2Exp, inputExpression, lineExpression);
            if (resultGood) return new Pair(true, null);
        } catch (TextMessageException e) {}
        try {
            boolean resultGood = MatchUtilities.replacementCheck(boundedLeqUniv2Exp, boundedLeqUniv1Exp, inputExpression, lineExpression);
            if (resultGood) return new Pair(true, null);
        } catch (TextMessageException e) {}

        MatchUtilities.clearFormMatch();
        try {
            boolean resultGood = MatchUtilities.replacementCheck(boundedLeqUniv1Exp, boundedLeqUniv3Exp, inputExpression, lineExpression);
            if (resultGood) return new Pair(true, null);
        } catch (TextMessageException e) {}
        try {
            boolean resultGood = MatchUtilities.replacementCheck(boundedLeqUniv3Exp, boundedLeqUniv1Exp, inputExpression, lineExpression);
            if (resultGood) return new Pair(true, null);
        } catch (TextMessageException e) {}

        MatchUtilities.clearFormMatch();
        try {
            boolean resultGood = MatchUtilities.replacementCheck(boundedLeqUniv2Exp, boundedLeqUniv3Exp, inputExpression, lineExpression);
            if (resultGood) return new Pair(true, null);
        } catch (TextMessageException e) {}
        try {
            boolean resultGood = MatchUtilities.replacementCheck(boundedLeqUniv3Exp, boundedLeqUniv2Exp, inputExpression, lineExpression);
            if (resultGood) return new Pair(true, null);
        } catch (TextMessageException e) {}

        // bounded less univ
        MatchUtilities.clearFormMatch();
        try {
            boolean resultGood = MatchUtilities.replacementCheck(boundedLssUniv1Exp, boundedLssUniv2Exp, inputExpression, lineExpression);
            if (resultGood) return new Pair(true, null);
        } catch (TextMessageException e) {}
        try {
            boolean resultGood = MatchUtilities.replacementCheck(boundedLssUniv2Exp, boundedLssUniv1Exp, inputExpression, lineExpression);
            if (resultGood) return new Pair(true, null);
        } catch (TextMessageException e) {}

        MatchUtilities.clearFormMatch();
        try {
            boolean resultGood = MatchUtilities.replacementCheck(boundedLssUniv1Exp, boundedLssUniv3Exp, inputExpression, lineExpression);
            if (resultGood) return new Pair(true, null);
        } catch (TextMessageException e) {}
        try {
            boolean resultGood = MatchUtilities.replacementCheck(boundedLssUniv3Exp, boundedLssUniv1Exp, inputExpression, lineExpression);
            if (resultGood) return new Pair(true, null);
        } catch (TextMessageException e) {}

        MatchUtilities.clearFormMatch();
        try {
            boolean resultGood = MatchUtilities.replacementCheck(boundedLssUniv2Exp, boundedLssUniv3Exp, inputExpression, lineExpression);
            if (resultGood) return new Pair(true, null);
        } catch (TextMessageException e) {}
        try {
            boolean resultGood = MatchUtilities.replacementCheck(boundedLssUniv3Exp, boundedLssUniv2Exp, inputExpression, lineExpression);
            if (resultGood) return new Pair(true, null);
        } catch (TextMessageException e) {}

        //bounded leq exis
        MatchUtilities.clearFormMatch();
        try {
            boolean resultGood = MatchUtilities.replacementCheck(boundedLeqExis1Exp, boundedLeqExis2Exp, inputExpression, lineExpression);
            if (resultGood) return new Pair(true, null);
        } catch (TextMessageException e) {}
        try {
            boolean resultGood = MatchUtilities.replacementCheck(boundedLeqExis2Exp, boundedLeqExis1Exp, inputExpression, lineExpression);
            if (resultGood) return new Pair(true, null);
        } catch (TextMessageException e) {}

        MatchUtilities.clearFormMatch();
        try {
            boolean resultGood = MatchUtilities.replacementCheck(boundedLeqExis1Exp, boundedLeqExis3Exp, inputExpression, lineExpression);
            if (resultGood) return new Pair(true, null);
        } catch (TextMessageException e) {}
        try {
            boolean resultGood = MatchUtilities.replacementCheck(boundedLeqExis3Exp, boundedLeqExis1Exp, inputExpression, lineExpression);
            if (resultGood) return new Pair(true, null);
        } catch (TextMessageException e) {}

        MatchUtilities.clearFormMatch();
        try {
            boolean resultGood = MatchUtilities.replacementCheck(boundedLeqExis2Exp, boundedLeqExis3Exp, inputExpression, lineExpression);
            if (resultGood) return new Pair(true, null);
        } catch (TextMessageException e) {}
        try {
            boolean resultGood = MatchUtilities.replacementCheck(boundedLeqExis3Exp, boundedLeqExis2Exp, inputExpression, lineExpression);
            if (resultGood) return new Pair(true, null);
        } catch (TextMessageException e) {}

        //bounded less exis
        MatchUtilities.clearFormMatch();
        try {
            boolean resultGood = MatchUtilities.replacementCheck(boundedLssExis1Exp, boundedLssExis2Exp, inputExpression, lineExpression);
            if (resultGood) return new Pair(true, null);
        } catch (TextMessageException e) {}
        try {
            boolean resultGood = MatchUtilities.replacementCheck(boundedLssExis2Exp, boundedLssExis1Exp, inputExpression, lineExpression);
            if (resultGood) return new Pair(true, null);
        } catch (TextMessageException e) {}

        MatchUtilities.clearFormMatch();
        try {
            boolean resultGood = MatchUtilities.replacementCheck(boundedLssExis1Exp, boundedLssExis3Exp, inputExpression, lineExpression);
            if (resultGood) return new Pair(true, null);
        } catch (TextMessageException e) {}
        try {
            boolean resultGood = MatchUtilities.replacementCheck(boundedLssExis3Exp, boundedLssExis1Exp, inputExpression, lineExpression);
            if (resultGood) return new Pair(true, null);
        } catch (TextMessageException e) {}

        MatchUtilities.clearFormMatch();
        try {
            boolean resultGood = MatchUtilities.replacementCheck(boundedLssExis2Exp, boundedLssExis3Exp, inputExpression, lineExpression);
            if (resultGood) return new Pair(true, null);
        } catch (TextMessageException e) {}
        try {
            boolean resultGood = MatchUtilities.replacementCheck(boundedLssExis3Exp, boundedLssExis2Exp, inputExpression, lineExpression);
            if (resultGood) return new Pair(true, null);
        } catch (TextMessageException e) {}



        return new Pair(false, Collections.singletonList(ParseUtilities.newRegularText(("Lines (" + inputLine1.getLineNumberLabel().getText() + ") and (" + line.getLineNumberLabel().getText() + ") are not of the right form for application of " + getName() + "."))));

    }


}

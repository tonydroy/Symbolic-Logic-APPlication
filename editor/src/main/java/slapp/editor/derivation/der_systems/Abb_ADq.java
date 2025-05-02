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

public class Abb_ADq extends DerivationRule {

    public Abb_ADq(String name, String rgexTemplate) {
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



        //vee
        Document vee = new Document( openBracketString + "\uD835\uDCAB ∨ \uD835\uDCAC" + closeBracketString);
        Document veeAbb = new Document(openBracketString + "∼\uD835\uDCAB → \uD835\uDCAC" + closeBracketString);
        //wedge
        Document wedge = new Document(openBracketString + "\uD835\uDCAB ∧ \uD835\uDCAC" + closeBracketString);
        Document wedgeAbb = new Document(openBracketString + "∼(\uD835\uDCAB → ∼\uD835\uDCAC)" + closeBracketString);
        //double arrow1
        Document doubleArrow1 = new Document(openBracketString + "(\uD835\uDCAB ↔ \uD835\uDCAC)" + closeBracketString);
        Document doubleArrow1Abb = new Document(openBracketString + "∼((\uD835\uDCAB → \uD835\uDCAC) → ∼(\uD835\uDCAC → \uD835\uDCAB))" + closeBracketString);
        //double arrow2
        Document doubleArrow2 = new Document(openBracketString + "\uD835\uDCAB ↔ \uD835\uDCAC" + closeBracketString);
        Document doubleArrow2Abb = new Document(openBracketString + "((\uD835\uDCAB → \uD835\uDCAC) ∧ (\uD835\uDCAC → \uD835\uDCAB))" + closeBracketString);
        //existential
        Document existential = new Document("∃\uD835\uDCCD\uD835\uDCAB");
        Document existentialAbb = new Document("∼∀\uD835\uDCCD∼\uD835\uDCAB");


        List<Expression> veeExpressions = ParseUtilities.parseDoc(vee, metaLanguage.getNameString());
        Expression veeExp = veeExpressions.get(0);
        List<Expression> veeAbbExpressions = ParseUtilities.parseDoc(veeAbb, metaLanguage.getNameString());
        Expression veeAbbExp = veeAbbExpressions.get(0);

        List<Expression> wedgeExpressions = ParseUtilities.parseDoc(wedge, metaLanguage.getNameString());
        Expression wedgeExp = wedgeExpressions.get(0);
        List<Expression> wedgeAbbExpressions = ParseUtilities.parseDoc(wedgeAbb, metaLanguage.getNameString());
        Expression wedgeAbbExp = wedgeAbbExpressions.get(0);

        List<Expression> doubleArrow1Expressions = ParseUtilities.parseDoc(doubleArrow1, metaLanguage.getNameString());
        Expression doubleArrow1Exp = doubleArrow1Expressions.get(0);
        List<Expression> doubleArrow1AbbExpressions = ParseUtilities.parseDoc(doubleArrow1Abb, metaLanguage.getNameString());
        Expression doubleArrow1AbbExp = doubleArrow1AbbExpressions.get(0);

        List<Expression> doubleArrow2Expressions = ParseUtilities.parseDoc(doubleArrow2, metaLanguage.getNameString());
        Expression doubleArrow2Exp = doubleArrow2Expressions.get(0);
        List<Expression> doubleArrow2AbbExpressions = ParseUtilities.parseDoc(doubleArrow2Abb, metaLanguage.getNameString());
        Expression doubleArrow2AbbExp = doubleArrow2AbbExpressions.get(0);

        List<Expression> existentialExpressions = ParseUtilities.parseDoc(existential, metaLanguage.getNameString());
        Expression existentialExp = existentialExpressions.get(0);
        List<Expression> existentialAbbExpressions = ParseUtilities.parseDoc(existentialAbb, metaLanguage.getNameString());
        Expression existentialAbbExp = existentialAbbExpressions.get(0);


        //vee
        MatchUtilities.clearFormMatch();
        try {
            boolean resultGood1 = MatchUtilities.replacementCheck(veeExp, veeAbbExp, inputExpression, lineExpression);
            if (resultGood1) return new Pair(true, null);
        }
        catch (TextMessageException e) { }

        try {
            boolean resultGood2 = MatchUtilities.replacementCheck(veeAbbExp, veeExp, inputExpression, lineExpression);
            if (resultGood2) return new Pair(true, null);
        }
        catch (TextMessageException e) { }

        //wedge
        MatchUtilities.clearFormMatch();
        try {
            boolean resultGood1 = MatchUtilities.replacementCheck(wedgeExp, wedgeAbbExp, inputExpression, lineExpression);
            if (resultGood1) return new Pair(true, null);
        }
        catch (TextMessageException e) { }

        try {
            boolean resultGood2 = MatchUtilities.replacementCheck(wedgeAbbExp, wedgeExp, inputExpression, lineExpression);
            if (resultGood2) return new Pair(true, null);
        }
        catch (TextMessageException e) { }

        //double arrow 1
        MatchUtilities.clearFormMatch();
        try {
            boolean resultGood1 = MatchUtilities.replacementCheck(doubleArrow1Exp, doubleArrow1AbbExp, inputExpression, lineExpression);
            if (resultGood1) return new Pair(true, null);
        }
        catch (TextMessageException e) { }

        try {
            boolean resultGood2 = MatchUtilities.replacementCheck(doubleArrow1AbbExp, doubleArrow1Exp, inputExpression, lineExpression);
            if (resultGood2) return new Pair(true, null);
        }
        catch (TextMessageException e) { }

        //double arrow 2
        MatchUtilities.clearFormMatch();
        try {
            boolean resultGood1 = MatchUtilities.replacementCheck(doubleArrow2Exp, doubleArrow2AbbExp, inputExpression, lineExpression);
            if (resultGood1) return new Pair(true, null);
        }
        catch (TextMessageException e) { }

        try {
            boolean resultGood2 = MatchUtilities.replacementCheck(doubleArrow2AbbExp, doubleArrow2Exp, inputExpression, lineExpression);
            if (resultGood2) return new Pair(true, null);
        }
        catch (TextMessageException e) { }

        //existential
        MatchUtilities.clearFormMatch();
        try {
            boolean resultGood1 = MatchUtilities.replacementCheck(existentialExp, existentialAbbExp, inputExpression, lineExpression);
            if (resultGood1) return new Pair(true, null);
        }
        catch (TextMessageException e) { }

        try {
            boolean resultGood2 = MatchUtilities.replacementCheck(existentialAbbExp, existentialExp, inputExpression, lineExpression);
            if (resultGood2) return new Pair(true, null);
        }
        catch (TextMessageException e) { }


        return new Pair(false, Collections.singletonList(ParseUtilities.newRegularText(("Lines (" + inputLine1.getLineNumberLabel().getText() + ") and (" + line.getLineNumberLabel().getText() + ") are not of the right form for application of " + getName() + "."))));

    }


}

package slapp.editor.derivation.der_systems;

import com.gluonhq.richtextarea.RichTextArea;
import com.gluonhq.richtextarea.model.Document;
import javafx.event.ActionEvent;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.util.Pair;
import slapp.editor.decorated_rta.BoxedDRTA;
import slapp.editor.derivation.DerivationCheck;
import slapp.editor.derivation.ViewLine;
import slapp.editor.parser.*;
import slapp.editor.parser.grammatical_parts.Formula;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class RestrictedUnivIntro extends DerivationRule {

    public RestrictedUnivIntro(String name, String rgexTemplate) {
        super(name, rgexTemplate);
        this.premAssp = false;
    }

    public Pair<Boolean, List<Text>> applies(DerivationCheck checker, ViewLine line, String... inputs) {

        BoxedDRTA lineBDRTA = line.getLineContentBoxedDRTA();
        RichTextArea lineRTA = lineBDRTA.getRTA();
        lineRTA.getActionFactory().saveNow().execute(new ActionEvent());
        Document lineDoc = lineRTA.getDocument();

        ViewLine topLine;
        Pair<ViewLine, List<Text>> topLinePair = checker.getLineFromLabel(inputs[0]);
        if (topLinePair.getKey() != null) topLine = topLinePair.getKey();
        else return new Pair(false, topLinePair.getValue());

        BoxedDRTA topLineBDRTA = topLine.getLineContentBoxedDRTA();
        RichTextArea topLineRTA = topLineBDRTA.getRTA();
        topLineRTA.getActionFactory().saveNow().execute(new ActionEvent());
        Document topLineDoc = topLineRTA.getDocument();
        if (topLineDoc.getText().equals("")) return new Pair(false, Collections.singletonList(ParseUtilities.newRegularText("Justification cannot appeal to the empty line (" + inputs[0] + ").")));


        ViewLine bottomLine;
        Pair<ViewLine, List<Text>> bottomLinePair = checker.getLineFromLabel(inputs[1]);
        if (bottomLinePair.getKey() != null) bottomLine = bottomLinePair.getKey();
        else return new Pair(false, bottomLinePair.getValue());

        BoxedDRTA bottomLineBDRTA = bottomLine.getLineContentBoxedDRTA();
        RichTextArea bottomLineRTA = bottomLineBDRTA.getRTA();
        bottomLineRTA.getActionFactory().saveNow().execute(new ActionEvent());
        Document bottomLineDoc = bottomLineRTA.getDocument();
        if (bottomLineDoc.getText().equals("")) return new Pair(false, Collections.singletonList(ParseUtilities.newRegularText("Justification cannot appeal to the empty line (" + inputs[1] + ").")));


        Pair<Boolean, List<Text>> subderivationPair = checker.isSubderivation(inputs[0], inputs[1]);
        if (!subderivationPair.getKey()) {
            return subderivationPair;
        }

        Pair<Boolean, List<Text>> accessibilityPair = checker.isAccessibleSubderivationFor(line, inputs[0], inputs[1]);
        if (!accessibilityPair.getKey()) {
            return accessibilityPair;
        }

        boolean asspOK = true;
        TextFlow justificationFlow = topLine.getJustificationFlow();
        String justificationString = checker.getDerivationExercise().getStringFromJustificationFlow(justificationFlow);
        if (!checker.getDerivationRuleset().getAsspRestrictedUnivIntroRule().matches(justificationString)) {
            asspOK = false;
        }

        Language objectLanguage = checker.getDerivationRuleset().getObjectLanguage();
        Language metaLanguage = checker.getDerivationRuleset().getMetaLanguage();

        String openBracketString = metaLanguage.getOpenBracket1();
        String closeBracketString = metaLanguage.getCloseBracket1();
        String dividerSymbol = metaLanguage.getDividerSymbol();
        String commaDividerString = metaLanguage.getCommaDivider();

        Document outputForm = new Document(openBracketString + "\u2200\ud835\udccd" + dividerSymbol + "\u212c" + closeBracketString + "\ud835\udcab");
        Document topLineForm = new Document("\u212c \u2039 \ud835\udccd" + commaDividerString + "\ud835\udccb \u203a"  );
        Document bottomLineForm = new Document("\ud835\udcab \u2039 \ud835\udccd" + commaDividerString + "\ud835\udccb \u203a" );

        Document outputForm1 = new Document( "(∀\uD835\uDCCD \uE8A4 \uD835\uDCC9)\uD835\uDCAB");
        Document topLineForm1 = new Document( "(\uD835\uDCCB \uE8A4 \uD835\uDCC9)");
        Document bottomLineForm1 = new Document("\ud835\udcab \u2039 \ud835\udccd" + commaDividerString + "\ud835\udccb \u203a");

        Document outputForm2 = new Document("(∀\uD835\uDCCD \uE8A6 \uD835\uDCC9)\uD835\uDCAB");
        Document topLineForm2 = new Document("\uD835\uDCCB \uE8A6 \uD835\uDCC9");
        Document bottomLineForm2 = new Document("\ud835\udcab \u2039 \ud835\udccd" + commaDividerString + "\ud835\udccb \u203a");


        boolean resultGoodA = false;
        boolean resultGoodB = false;
        boolean resultGoodC = false;
        boolean resultGoodA1 = false;
        boolean resultGoodB1 = false;
        boolean resultGoodC1 = false;
        boolean resultGoodA2 = false;
        boolean resultGoodB2 = false;
        boolean resultGoodC2 = false;


        //try unabb
        MatchUtilities.clearFormMatch();
        try {
            Pair<Boolean, Boolean> outputMatch = MatchUtilities.simpleFormMatch(outputForm, lineDoc, objectLanguage.getNameString(), metaLanguage.getNameString());
            if (outputMatch.getKey() && outputMatch.getValue()) resultGoodA = true;
        }
        catch (TextMessageException e) {
  //          return new Pair(false, Collections.singletonList(ParseUtilities.newRegularText("Line (" + line.getLineNumberLabel().getText() + ") is not of the right form to result by " + getName() + ".")));
        }
        if (resultGoodA) {
            try {
                Pair<Boolean, Boolean> topLineMatch = MatchUtilities.simpleFormMatch(topLineForm, topLineDoc, objectLanguage.getNameString(), metaLanguage.getNameString());
                if (topLineMatch.getKey() && topLineMatch.getValue()) resultGoodB = true;
            } catch (TextMessageException e) {
                return new Pair(false, e.getMessageList());
            }
            try {
                Pair<Boolean, Boolean> bottomLineMatch = MatchUtilities.simpleFormMatch(bottomLineForm, bottomLineDoc, objectLanguage.getNameString(), metaLanguage.getNameString());
                if (bottomLineMatch.getKey() && bottomLineMatch.getValue()) resultGoodC = true;
            } catch (TextMessageException e) {
                return new Pair(false, e.getMessageList());
            }
     //       if (resultGoodB && resultGoodC) return new Pair(true, null);
        }

        //try bounded 1
        if (!resultGoodA || !resultGoodB || !resultGoodC) {
            MatchUtilities.clearFormMatch();
            try {
                Pair<Boolean, Boolean> outputMatch = MatchUtilities.simpleFormMatch(outputForm1, lineDoc, objectLanguage.getNameString(), metaLanguage.getNameString());
                if (outputMatch.getKey() && outputMatch.getValue()) resultGoodA1 = true;
            }
            catch (TextMessageException e) { }
            if (resultGoodA1) {
                try {
                    Pair<Boolean, Boolean> topLineMatch = MatchUtilities.simpleFormMatch(topLineForm1, topLineDoc, objectLanguage.getNameString(), metaLanguage.getNameString());
                    if (topLineMatch.getKey() && topLineMatch.getValue()) resultGoodB1 = true;
                } catch (TextMessageException e) {
                    return new Pair(false, e.getMessageList());
                }
                try {
                    Pair<Boolean, Boolean> bottomLineMatch = MatchUtilities.simpleFormMatch(bottomLineForm1, bottomLineDoc, objectLanguage.getNameString(), metaLanguage.getNameString());
                    if (bottomLineMatch.getKey() && bottomLineMatch.getValue()) resultGoodC1 = true;
                } catch (TextMessageException e) {
                    return new Pair(false, e.getMessageList());
                }
            }

            //try bounded 2
            if (!resultGoodA1 || !resultGoodB1 || !resultGoodC1) {
                MatchUtilities.clearFormMatch();
                try {
                    Pair<Boolean, Boolean> outputMatch = MatchUtilities.simpleFormMatch(outputForm2, lineDoc, objectLanguage.getNameString(), metaLanguage.getNameString());
                    if (outputMatch.getKey() && outputMatch.getValue()) resultGoodA2 = true;
                }
                catch (TextMessageException e) { }
                if (resultGoodA2) {
                    try {
                        Pair<Boolean, Boolean> topLineMatch = MatchUtilities.simpleFormMatch(topLineForm2, topLineDoc, objectLanguage.getNameString(), metaLanguage.getNameString());
                        if (topLineMatch.getKey() && topLineMatch.getValue()) resultGoodB2 = true;
                    } catch (TextMessageException e) {
                        return new Pair(false, e.getMessageList());
                    }
                    try {
                        Pair<Boolean, Boolean> bottomLineMatch = MatchUtilities.simpleFormMatch(bottomLineForm2, bottomLineDoc, objectLanguage.getNameString(), metaLanguage.getNameString());
                        if (bottomLineMatch.getKey() && bottomLineMatch.getValue()) resultGoodC2 = true;
                    } catch (TextMessageException e) {
                        return new Pair(false, e.getMessageList());
                    }
                }
            }
        }

        if (!resultGoodA && !resultGoodA1 && !resultGoodA2) {
            return new Pair(false, Collections.singletonList(ParseUtilities.newRegularText("Line (" + line.getLineNumberLabel().getText() + ") is not of the right form to result by " + getName() + ".")));
        }



        Expression variableExp = ((Formula) MatchUtilities.getTransformList().get(0).getKey()).getSubTransform().getExp2().getMatch();

        //check variable free in undischarged assumption
        List<String> asspList = line.getAssumptionList();
        for (String asspLabel : asspList) {
            ViewLine asspLine;
            Pair<ViewLine, List<Text>> asspLinePair = checker.getLineFromLabel(asspLabel);
            if (asspLinePair.getKey() != null) asspLine = asspLinePair.getKey();
            else return new Pair(false, asspLinePair.getValue());

            TextFlow asspJustificationFlow = asspLine.getJustificationFlow();
            String asspJustificationString = checker.getDerivationExercise().getStringFromJustificationFlow(asspJustificationFlow);
            if (!checker.getDerivationRuleset().getPremiseRule().matches(asspJustificationString)) {

                BoxedDRTA asspLineBDRTA = asspLine.getLineContentBoxedDRTA();
                RichTextArea asspLineRTA = asspLineBDRTA.getRTA();
                asspLineRTA.getActionFactory().saveNow().execute(new ActionEvent());
                Document asspLineDoc = asspLineRTA.getDocument();
                Expression asspLineExp = ParseUtilities.parseDoc(asspLineDoc, objectLanguage.getNameString()).get(0);

                if (SyntacticalFns.expTermFreeInFormula(asspLineExp, variableExp, objectLanguage.getNameString())) {
                    List<Text> list = new ArrayList<>();
                    list.add(new Text("Variable "));
                    list.addAll(variableExp.toTextList());
                    list.add(new Text(" is free in an undischarged assumption."));
                    return new Pair(false, list);
                }
            }
        }

        //check variable free in goal
        Expression lineExp = ParseUtilities.parseDoc(lineDoc, objectLanguage.getNameString()).get(0);
        if (SyntacticalFns.expTermFreeInFormula(lineExp, variableExp, objectLanguage.getNameString())) {
            List<Text> list = new ArrayList<>();
            list.add(new Text("Variable "));
            list.addAll(variableExp.toTextList());
            list.add(new Text(" is free in "));
            list.addAll(lineExp.toTextList());
            return new Pair(false, list);
        }


        if (((resultGoodA && resultGoodB && resultGoodC) || (resultGoodA1 && resultGoodB1 && resultGoodC1) || (resultGoodA2 && resultGoodB2 && resultGoodC2))  && asspOK) {
            return new Pair(true, null);
        }

        if (asspOK) {
            return new Pair(false, Collections.singletonList(ParseUtilities.newRegularText("Subderivation (" + inputs[0] + ")-(" + inputs[1] + ") is not of the right form to justify (" + line.getLineNumberLabel().getText() + ") by " + getName() + ".")));
        }
        else {
            return new Pair(false, Collections.singletonList(ParseUtilities.newRegularText("To use this subderivation by " + getName() + " the exit strategy should be (\ud835\udc54, " + getName() + ").")));
        }

    }


}

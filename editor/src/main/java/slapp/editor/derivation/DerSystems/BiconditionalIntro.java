package slapp.editor.derivation.DerSystems;

import com.gluonhq.richtextarea.RichTextArea;
import com.gluonhq.richtextarea.model.Document;
import javafx.event.ActionEvent;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.util.Pair;
import slapp.editor.decorated_rta.BoxedDRTA;
import slapp.editor.derivation.DerivationCheck;
import slapp.editor.derivation.ViewLine;
import slapp.editor.parser.Language;
import slapp.editor.parser.MatchUtilities;
import slapp.editor.parser.ParseUtilities;
import slapp.editor.parser.TextMessageException;

import java.util.Collections;
import java.util.List;

public class BiconditionalIntro extends DerivationRule {

    public BiconditionalIntro(String name, String rgexTemplate) {
        super(name, rgexTemplate);
        this.premAssp = false;
    }

    public Pair<Boolean, List<Text>> applies(DerivationCheck checker, ViewLine line, String... inputs) {

        BoxedDRTA lineBDRTA = line.getLineContentBoxedDRTA();
        RichTextArea lineRTA = lineBDRTA.getRTA();
        lineRTA.getActionFactory().saveNow().execute(new ActionEvent());
        Document lineDoc = lineRTA.getDocument();

        ViewLine topLine1;
        Pair<ViewLine, List<Text>> topLine1Pair = checker.getLineFromLabel(inputs[0]);
        if (topLine1Pair.getKey() != null) topLine1 = topLine1Pair.getKey();
        else return new Pair(false, topLine1Pair.getValue());

        BoxedDRTA topLine1BDRTA = topLine1.getLineContentBoxedDRTA();
        RichTextArea topLine1RTA = topLine1BDRTA.getRTA();
        topLine1RTA.getActionFactory().saveNow().execute(new ActionEvent());
        Document topLine1Doc = topLine1RTA.getDocument();
        if (topLine1Doc.getText().equals("")) return new Pair(false, Collections.singletonList(ParseUtilities.newRegularText("Justification cannot appeal to the empty line (" + inputs[0] + ").")));


        ViewLine bottomLine1;
        Pair<ViewLine, List<Text>> bottomLine1Pair = checker.getLineFromLabel(inputs[1]);
        if (bottomLine1Pair.getKey() != null) bottomLine1 = bottomLine1Pair.getKey();
        else return new Pair(false, bottomLine1Pair.getValue());

        BoxedDRTA bottomLine1BDRTA = bottomLine1.getLineContentBoxedDRTA();
        RichTextArea bottomLine1RTA = bottomLine1BDRTA.getRTA();
        bottomLine1RTA.getActionFactory().saveNow().execute(new ActionEvent());
        Document bottomLine1Doc = bottomLine1RTA.getDocument();
        if (bottomLine1Doc.getText().equals("")) return new Pair(false, Collections.singletonList(ParseUtilities.newRegularText("Justification cannot appeal to the empty line (" + inputs[1] + ").")));

        ViewLine topLine2;
        Pair<ViewLine, List<Text>> topLine2Pair = checker.getLineFromLabel(inputs[2]);
        if (topLine2Pair.getKey() != null) topLine2 = topLine2Pair.getKey();
        else return new Pair(false, topLine2Pair.getValue());

        BoxedDRTA topLine2BDRTA = topLine2.getLineContentBoxedDRTA();
        RichTextArea topLine2RTA = topLine2BDRTA.getRTA();
        topLine2RTA.getActionFactory().saveNow().execute(new ActionEvent());
        Document topLine2Doc = topLine2RTA.getDocument();
        if (topLine2Doc.getText().equals("")) return new Pair(false, Collections.singletonList(ParseUtilities.newRegularText("Justification cannot appeal to the empty line (" + inputs[2] + ").")));


        ViewLine bottomLine2;
        Pair<ViewLine, List<Text>> bottomLine2Pair = checker.getLineFromLabel(inputs[3]);
        if (bottomLine2Pair.getKey() != null) bottomLine2 = bottomLine2Pair.getKey();
        else return new Pair(false, bottomLine2Pair.getValue());

        BoxedDRTA bottomLine2BDRTA = bottomLine2.getLineContentBoxedDRTA();
        RichTextArea bottomLine2RTA = bottomLine2BDRTA.getRTA();
        bottomLine2RTA.getActionFactory().saveNow().execute(new ActionEvent());
        Document bottomLine2Doc = bottomLine2RTA.getDocument();
        if (bottomLine2Doc.getText().equals("")) return new Pair(false, Collections.singletonList(ParseUtilities.newRegularText("Justification cannot appeal to the empty line (" + inputs[3] + ").")));

        Pair<Boolean, List<Text>> subderivation1Pair = checker.isSubderivation(inputs[0], inputs[1]);
        if (!subderivation1Pair.getKey()) {
            return subderivation1Pair;
        }

        Pair<Boolean, List<Text>> subderivation2Pair = checker.isSubderivation(inputs[2], inputs[3]);
        if (!subderivation2Pair.getKey()) {
            return subderivation2Pair;
        }

        Pair<Boolean, List<Text>> accessibility1Pair = checker.isAccessibleSubderivationFor(line, inputs[0], inputs[1]);
        if (!accessibility1Pair.getKey()) {
            return accessibility1Pair;
        }

        Pair<Boolean, List<Text>> accessibility2Pair = checker.isAccessibleSubderivationFor(line, inputs[2], inputs[3]);
        if (!accessibility2Pair.getKey()) {
            return accessibility2Pair;
        }

        boolean asspOK = true;
        TextFlow topJustificationFlow1 = topLine1.getJustificationFlow();
        String justificationString1 = checker.getDerivationExercise().getStringFromJustificationFlow(topJustificationFlow1);
        TextFlow topJustificationFlow2 = topLine2.getJustificationFlow();
        String juustificationString2 = checker.getDerivationExercise().getStringFromJustificationFlow(topJustificationFlow2);
        DerivationRule rule = checker.getDerivationRuleset().getAsspBicondIntroRule();
        if (!rule.matches(justificationString1) || !rule.matches(juustificationString2)) {
            asspOK = false;
        }

        Language objectLanguage = checker.getDerivationRuleset().getObjectLanguage();
        Language metaLanguage = checker.getDerivationRuleset().getMetaLanguage();

        String openBracketString = metaLanguage.getOpenBracket1();
        String closeBracketString = metaLanguage.getCloseBracket1();

        Document outputForm = new Document(openBracketString + "\ud835\udcab \u2194 \ud835\udcac" + closeBracketString);
        Document inputFormA = new Document("\ud835\udcab" );
        Document inputFormB = new Document("\ud835\udcac" );

        //try one way
        MatchUtilities.clearFormMatch();
        boolean resultGood1 = false;
        boolean resultGood2 = false;
        boolean resultGood3 = false;
        boolean resultGood4 = false;
        try {
            Pair<Boolean, Boolean> outputMatch = MatchUtilities.simpleFormMatch(outputForm, lineDoc, objectLanguage.getNameString(), metaLanguage.getNameString());
        }
        catch (TextMessageException e) {
            return new Pair(false, Collections.singletonList(ParseUtilities.newRegularText("Line (" + line.getLineNumberLabel().getText() + ") is not of the right form to result by " + getName() + ".")));
        }
        try {
            Pair<Boolean, Boolean> topLine1Match = MatchUtilities.simpleFormMatch(inputFormA, topLine1Doc, objectLanguage.getNameString(), metaLanguage.getNameString());
            resultGood1 = true;
        }
        catch (TextMessageException e) { }

        try {
            Pair<Boolean, Boolean> bottomLine1Match = MatchUtilities.simpleFormMatch(inputFormB, bottomLine1Doc, objectLanguage.getNameString(), metaLanguage.getNameString());
            resultGood2 = true;
        } catch (TextMessageException e) { }

        try {
            Pair<Boolean, Boolean> topLine2Match = MatchUtilities.simpleFormMatch(inputFormB, topLine2Doc, objectLanguage.getNameString(), metaLanguage.getNameString());
            resultGood3 = true;
        }
        catch (TextMessageException e) { }

        try {
            Pair<Boolean, Boolean> bottomLine2Match = MatchUtilities.simpleFormMatch(inputFormA, bottomLine2Doc, objectLanguage.getNameString(), metaLanguage.getNameString());
            resultGood4= true;
        } catch (TextMessageException e) { }

        if (resultGood1 && resultGood2 && resultGood3 && resultGood4 && asspOK) {
            return new Pair(true, null);
        }

        //try the other
        MatchUtilities.clearFormMatch();
        resultGood1 = false;
        resultGood2 = false;
        resultGood3 = false;
        resultGood4 = false;
        try {
            Pair<Boolean, Boolean> outputMatch = MatchUtilities.simpleFormMatch(outputForm, lineDoc, objectLanguage.getNameString(), metaLanguage.getNameString());
        }
        catch (TextMessageException e) {  }
        try {
            Pair<Boolean, Boolean> topLine1Match = MatchUtilities.simpleFormMatch(inputFormB, topLine1Doc, objectLanguage.getNameString(), metaLanguage.getNameString());
            resultGood1 = true;
        }
        catch (TextMessageException e) { }

        try {
            Pair<Boolean, Boolean> bottomLine1Match = MatchUtilities.simpleFormMatch(inputFormA, bottomLine1Doc, objectLanguage.getNameString(), metaLanguage.getNameString());
            resultGood2 = true;
        } catch (TextMessageException e) { }

        try {
            Pair<Boolean, Boolean> topLine2Match = MatchUtilities.simpleFormMatch(inputFormA, topLine2Doc, objectLanguage.getNameString(), metaLanguage.getNameString());
            resultGood3 = true;
        }
        catch (TextMessageException e) { }

        try {
            Pair<Boolean, Boolean> bottomLine2Match = MatchUtilities.simpleFormMatch(inputFormB, bottomLine2Doc, objectLanguage.getNameString(), metaLanguage.getNameString());
            resultGood4= true;
        } catch (TextMessageException e) { }

        if (resultGood1 && resultGood2 && resultGood3 && resultGood4 && asspOK) {
            return new Pair(true, null);
        }

        if (asspOK) {
            return new Pair(false, Collections.singletonList(ParseUtilities.newRegularText("Line (" + line.getLineNumberLabel().getText() + ") requires a pair of subderivations, one starting with the left side and going to the right, and another starting with the right and going to the left.")));
        }
        else {
            return new Pair(false, Collections.singletonList(ParseUtilities.newRegularText("To use these subderivations by " + getName() + " the exit strategies should both be (\ud835\udc54, " + getName() + ").")));

        }

    }


}

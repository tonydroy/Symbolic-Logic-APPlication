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

public class ConditionalIntro extends DerivationRule {

    public ConditionalIntro(String name, String rgexTemplate) {
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
        if (!checker.getDerivationRuleset().getAsspCondIntroRule().matches(justificationString)) {
            asspOK = false;
        }

        Language objectLanguage = checker.getDerivationRuleset().getObjectLanguage();
        Language metaLanguage = checker.getDerivationRuleset().getMetaLanguage();

        String openBracketString = metaLanguage.getOpenBracket1();
        String closeBracketString = metaLanguage.getCloseBracket1();

        Document outputForm = new Document(openBracketString + "\ud835\udcab \u2192 \ud835\udcac" + closeBracketString);
        Document inputFormA = new Document("\ud835\udcab" );
        Document inputFormB = new Document("\ud835\udcac" );


        //try correct
        MatchUtilities.clearFormMatch();
        boolean resultGood1 = false;
        boolean resultGood2 = false;
        try {
            Pair<Boolean, Boolean> outputMatch = MatchUtilities.simpleFormMatch(outputForm, lineDoc, objectLanguage.getNameString(), metaLanguage.getNameString());
        }
        catch (TextMessageException e) {
            return new Pair(false, Collections.singletonList(ParseUtilities.newRegularText("Line (" + line.getLineNumberLabel().getText() + ") is not of the right form to result by " + getName() + ".")));
        }
        try {
            Pair<Boolean, Boolean> inputMatch1 = MatchUtilities.simpleFormMatch(inputFormA, topLineDoc, objectLanguage.getNameString(), metaLanguage.getNameString());
            resultGood1 = true;
        }
        catch (TextMessageException e) { }

        try {
            Pair<Boolean, Boolean> inputMatch2 = MatchUtilities.simpleFormMatch(inputFormB, bottomLineDoc, objectLanguage.getNameString(), metaLanguage.getNameString());
            resultGood2 = true;
        } catch (TextMessageException e) { }

        if (resultGood1 && resultGood2 && asspOK) {
            return new Pair(true, null);
        }

        if (asspOK) {
            return new Pair(false, Collections.singletonList(ParseUtilities.newRegularText("Line (" + line.getLineNumberLabel().getText() + ") requires a subderivation starting with the antecedent as its assumption, and ending with the consequent (at the same scope) as its last line.")));
        }
        else {
            return new Pair(false, Collections.singletonList(ParseUtilities.newRegularText("To use this subderivation by " + getName() + " the exit strategy should be (\ud835\udc54, " + getName() + ").")));
        }

    }


}

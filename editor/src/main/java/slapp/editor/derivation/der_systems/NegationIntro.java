package slapp.editor.derivation.der_systems;

import com.gluonhq.richtextarea.RichTextArea;
import com.gluonhq.richtextarea.model.Document;
import javafx.event.ActionEvent;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.util.Pair;
import slapp.editor.decorated_rta.BoxedDRTA;
import slapp.editor.derivation.DCheck;
import slapp.editor.derivation.DerivationCheck;
import slapp.editor.derivation.DerivationExercise;
import slapp.editor.derivation.ViewLine;
import slapp.editor.derivation_explain.DrvtnExpExercise;
import slapp.editor.parser.Language;
import slapp.editor.parser.MatchUtilities;
import slapp.editor.parser.ParseUtilities;
import slapp.editor.parser.TextMessageException;

import java.util.Collections;
import java.util.List;

public class NegationIntro extends DerivationRule {

    public NegationIntro(String name, String rgexTemplate) {
        super(name, rgexTemplate);
        this.premAssp = false;
    }

    public Pair<Boolean, List<Text>> applies(DCheck checker, ViewLine line, String... inputs) {

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

        String justificationString = (checker instanceof DerivationCheck) ? ((DerivationExercise) checker.getExercise()).getStringFromJustificationFlow(justificationFlow) :
                ((DrvtnExpExercise) checker.getExercise()).getStringFromJustificationFlow(justificationFlow);


 //       String justificationString = checker.getDerivationExercise().getStringFromJustificationFlow(justificationFlow);
        if (!checker.getDerivationRuleset().getAsspNegIntroRule().matches(justificationString)) {
            asspOK = false;
        }

        Language objectLanguage = checker.getDerivationRuleset().getObjectLanguage();
        Language metaLanguage = checker.getDerivationRuleset().getMetaLanguage();

        String openBracketString = metaLanguage.getOpenBracket1();
        String closeBracketString = metaLanguage.getCloseBracket1();

        Document topForm = new Document("\ud835\udcab" );
        Document bottomForm = new Document("\u22a5" );
        Document bottomForm2 = new Document("\ud835\udcac \u2227 \u223c\ud835\udcac");
        Document outputForm = new Document("\u223c\ud835\udcab");




        //try with bottom
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
            Pair<Boolean, Boolean> topMatch = MatchUtilities.simpleFormMatch(topForm, topLineDoc, objectLanguage.getNameString(), metaLanguage.getNameString());
            resultGood1 = true;
        }
        catch (TextMessageException e) { }

        try {
            Pair<Boolean, Boolean> bottomMatch = MatchUtilities.simpleFormMatch(bottomForm, bottomLineDoc, objectLanguage.getNameString(), metaLanguage.getNameString());
            resultGood2 = true;
        } catch (TextMessageException e) { }

        if (resultGood1 && resultGood2 && asspOK) {
            return new Pair(true, null);
        }

        //try with contradiction
        MatchUtilities.clearFormMatch();
        resultGood1 = false;
        resultGood2 = false;
        try {
            Pair<Boolean, Boolean> outputMatch = MatchUtilities.simpleFormMatch(outputForm, lineDoc, objectLanguage.getNameString(), metaLanguage.getNameString());
        }
        catch (TextMessageException e) { }
        try {
            Pair<Boolean, Boolean> topMatch = MatchUtilities.simpleFormMatch(topForm, topLineDoc, objectLanguage.getNameString(), metaLanguage.getNameString());
            resultGood1 = true;
        }
        catch (TextMessageException e) { }

        try {
            Pair<Boolean, Boolean> bottomMatch = MatchUtilities.simpleFormMatch(bottomForm2, bottomLineDoc, objectLanguage.getNameString(), metaLanguage.getNameString());
            resultGood2 = true;
        } catch (TextMessageException e) { }

        if (resultGood1 && resultGood2 && asspOK) {
            return new Pair(true, null);
        }

        if (asspOK) {
            return new Pair(false, Collections.singletonList(ParseUtilities.newRegularText("Line (" + line.getLineNumberLabel().getText() + ") requires a subderivation starting with an assumption that drops the (main) negation, and ends with a contradiction (at the same scope).")));
        }
        else {
            return new Pair(false, Collections.singletonList(ParseUtilities.newRegularText("To use this subderivation by " + getName() + " its exit strategy should be (\ud835\udc50, " + getName() + ").")));

        }

    }


}

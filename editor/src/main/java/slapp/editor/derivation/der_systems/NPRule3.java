package slapp.editor.derivation.der_systems;

import com.gluonhq.richtextarea.RichTextArea;
import com.gluonhq.richtextarea.model.Document;
import javafx.event.ActionEvent;
import javafx.scene.text.Text;
import javafx.util.Pair;
import slapp.editor.decorated_rta.BoxedDRTA;
import slapp.editor.derivation.DCheck;
import slapp.editor.derivation.ViewLine;
import slapp.editor.parser.Language;
import slapp.editor.parser.MatchUtilities;
import slapp.editor.parser.ParseUtilities;
import slapp.editor.parser.TextMessageException;

import java.util.Collections;
import java.util.List;

public class NPRule3 extends DerivationRule {

    public NPRule3(String name, String rgexTemplate) {
        super(name, rgexTemplate);
        this.premAssp = false;
    }

    public Pair<Boolean, List<Text>> applies(DCheck checker, ViewLine line, String... inputs) {

        BoxedDRTA lineBDRTA = line.getLineContentBoxedDRTA();
        RichTextArea lineRTA = lineBDRTA.getRTA();
        lineRTA.getActionFactory().saveNow().execute(new ActionEvent());
        Document lineDoc = lineRTA.getDocument();

        ViewLine inputLine;
        Pair<ViewLine, List<Text>> inputLine1Pair = checker.getLineFromLabel(inputs[0]);
        if (inputLine1Pair.getKey() != null) inputLine = inputLine1Pair.getKey();
        else return new Pair(false, inputLine1Pair.getValue());

        BoxedDRTA inputBDRTA = inputLine.getLineContentBoxedDRTA();
        RichTextArea inputRTA = inputBDRTA.getRTA();
        inputRTA.getActionFactory().saveNow().execute(new ActionEvent());
        Document inputDoc = inputRTA.getDocument();
        if (inputDoc.getText().equals("")) return new Pair(false, Collections.singletonList(ParseUtilities.newRegularText("Justification cannot appeal to the empty line (" + inputs[0] + ").")));


        Pair<Boolean, List<Text>> accessibilityPair = checker.lineIsAccessibleTo(inputLine, line);
        if (!accessibilityPair.getKey()) {
            return accessibilityPair;
        }

        Language objectLanguage = checker.getDerivationRuleset().getObjectLanguage();
        Language metaLanguage = checker.getDerivationRuleset().getMetaLanguage();

        String openBracketString = metaLanguage.getOpenBracket1();
        String closeBracketString = metaLanguage.getCloseBracket1();

        Document inputForm = new Document(openBracketString + "\ud835\udcab \u2227 \ud835\udcac" + closeBracketString);
        Document outputFormA = new Document("\ud835\udcab" );
        Document outputFormB = new Document("\ud835\udcac" );
        boolean resultGood = false;

        //try A
        MatchUtilities.clearFormMatch();
        try {
            Pair<Boolean, Boolean> inputMatch = MatchUtilities.simpleFormMatch(inputForm, inputDoc, objectLanguage.getNameString(), metaLanguage.getNameString());
        }
        catch (TextMessageException e) {
            return new Pair(false, Collections.singletonList(ParseUtilities.newRegularText("Line (" + inputs[0] + ") is not of the right form to result in (" + line.getLineNumberLabel().getText() + ") by " + getName() + ".")));
        }

        try {
            Pair<Boolean, Boolean> outputMatchA = MatchUtilities.simpleFormMatch(outputFormA, lineDoc, objectLanguage.getNameString(), metaLanguage.getNameString());
            if (outputMatchA.getKey()) return new Pair(true, null);
        }
        catch (TextMessageException e) { }


        //try B
        MatchUtilities.clearFormMatch();
        try {
            Pair<Boolean, Boolean> inputMatch = MatchUtilities.simpleFormMatch(inputForm, inputDoc, objectLanguage.getNameString(), metaLanguage.getNameString());
        } catch (TextMessageException e) { }

        try {
            Pair<Boolean, Boolean> outputMatchB = MatchUtilities.simpleFormMatch(outputFormB, lineDoc, objectLanguage.getNameString(), metaLanguage.getNameString());
            if (outputMatchB.getKey()) return new Pair(false, Collections.singletonList(ParseUtilities.newRegularText("The \uD835\uDC41\uD835\uDC43 R3 rule applies just to a left conjunct.")));

        } catch (TextMessageException e) { }




        return new Pair(false, Collections.singletonList(ParseUtilities.newRegularText("Line (" + line.getLineNumberLabel().getText() + ") does not result from (" + inputs[0] + ") by " + getName() + ".")));


    }


}

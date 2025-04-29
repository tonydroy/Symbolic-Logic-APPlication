package slapp.editor.derivation.der_systems;

import com.gluonhq.richtextarea.RichTextArea;
import com.gluonhq.richtextarea.model.Document;
import javafx.event.ActionEvent;
import javafx.scene.text.Text;
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

public class DisjunctionIntro extends DerivationRule {

    public DisjunctionIntro(String name, String rgexTemplate) {
        super(name, rgexTemplate);
        this.premAssp = false;
    }

    public Pair<Boolean, List<Text>> applies(DerivationCheck checker, ViewLine line, String... inputs) {

        BoxedDRTA lineBDRTA = line.getLineContentBoxedDRTA();
        RichTextArea lineRTA = lineBDRTA.getRTA();
        lineRTA.getActionFactory().saveNow().execute(new ActionEvent());
        Document lineDoc = lineRTA.getDocument();

        ViewLine inputLine;
        Pair<ViewLine, List<Text>> inputLinePair = checker.getLineFromLabel(inputs[0]);
        if (inputLinePair.getKey() != null) inputLine = inputLinePair.getKey();
        else return new Pair(false, inputLinePair.getValue());

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

        Document outputForm = new Document(openBracketString + "\ud835\udcab \u2228 \ud835\udcac" + closeBracketString);
        Document inputFormA = new Document("\ud835\udcab" );
        Document inputFormB = new Document("\ud835\udcac" );
        boolean resultGood = false;

        //try A
        MatchUtilities.clearFormMatch();
        try {
            Pair<Boolean, Boolean> outputMatch = MatchUtilities.simpleFormMatch(outputForm, lineDoc, objectLanguage.getNameString(), metaLanguage.getNameString());
        }
        catch (TextMessageException e) {
            return new Pair(false, Collections.singletonList(ParseUtilities.newRegularText("Line (" + line.getLineNumberLabel().getText() + ") is not of the right form to be justified by " + getName() + ".")));
        }

        try {
            Pair<Boolean, Boolean> inputMatchA = MatchUtilities.simpleFormMatch(inputFormA, inputDoc, objectLanguage.getNameString(), metaLanguage.getNameString());
            if (inputMatchA.getKey()) resultGood = true;
        }
        catch (TextMessageException e) { }

        if (!resultGood) {
            //try B
            MatchUtilities.clearFormMatch();
            try {
                Pair<Boolean, Boolean> inputMatch = MatchUtilities.simpleFormMatch(outputForm, lineDoc, objectLanguage.getNameString(), metaLanguage.getNameString());
            } catch (TextMessageException e) { }
            try {
                Pair<Boolean, Boolean> inputMatchB = MatchUtilities.simpleFormMatch(inputFormB, inputDoc, objectLanguage.getNameString(), metaLanguage.getNameString());
                if (inputMatchB.getKey()) resultGood = true;
            } catch (TextMessageException e) { }
        }

        if (resultGood) return new Pair(true, null);
        else {
            return new Pair(false, Collections.singletonList(ParseUtilities.newRegularText("Line (" + line.getLineNumberLabel().getText() + ") does not result from (" + inputs[0] + ") by " + getName() + ".")));
        }

    }


}

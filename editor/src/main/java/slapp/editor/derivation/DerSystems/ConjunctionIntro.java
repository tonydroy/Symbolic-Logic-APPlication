package slapp.editor.derivation.DerSystems;

import com.gluonhq.richtextarea.RichTextArea;
import com.gluonhq.richtextarea.model.Document;
import javafx.event.ActionEvent;
import javafx.scene.text.Text;
import slapp.editor.decorated_rta.BoxedDRTA;
import slapp.editor.derivation.DerivationCheck;
import slapp.editor.derivation.ViewLine;
import slapp.editor.parser.Language;
import javafx.util.Pair;
import slapp.editor.parser.MatchUtilities;
import slapp.editor.parser.ParseUtilities;
import slapp.editor.parser.TextMessageException;

import javax.swing.text.View;
import java.util.Collections;
import java.util.List;

public class ConjunctionIntro extends DerivationRule {

    public ConjunctionIntro(String name, String rgexTemplate) {
        super(name, rgexTemplate);
        this.premAssp = false;
    }

    public Pair<Boolean, List<Text>> applies(DerivationCheck checker, ViewLine line, String... inputs) {

        BoxedDRTA lineBDRTA = line.getLineContentBoxedDRTA();
        RichTextArea lineRTA = lineBDRTA.getRTA();
        lineRTA.getActionFactory().saveNow().execute(new ActionEvent());
        Document lineDoc = lineRTA.getDocument();

        ViewLine inputLine1 = checker.getLineFromLabel(inputs[0]);
        BoxedDRTA inputBDRTA1 = inputLine1.getLineContentBoxedDRTA();
        RichTextArea inputRTA1 = inputBDRTA1.getRTA();
        inputRTA1.getActionFactory().saveNow().execute(new ActionEvent());
        Document inputDoc1 = inputRTA1.getDocument();

        ViewLine inputLine2 = checker.getLineFromLabel(inputs[1]);
        BoxedDRTA inputBDRTA2 = inputLine2.getLineContentBoxedDRTA();
        RichTextArea inputRTA2 = inputBDRTA2.getRTA();
        inputRTA2.getActionFactory().saveNow().execute(new ActionEvent());
        Document inputDoc2 = inputRTA2.getDocument();

        Pair<Boolean, List<Text>> accessibilityPair1 = checker.lineIsAccessibleTo(inputLine1, line);
        if (!accessibilityPair1.getKey()) {
            return accessibilityPair1;
        }

        Pair<Boolean, List<Text>> accessibilityPair2 = checker.lineIsAccessibleTo(inputLine2, line);
        if (!accessibilityPair2.getKey()) {
            return accessibilityPair2;
        }

        Language objectLanguage = checker.getDerivationRuleset().getObjectLanguage();
        Language metaLanguage = checker.getDerivationRuleset().getMetaLanguage();

        String openBracketString = metaLanguage.getOpenBracket1();
        String closeBracketString = metaLanguage.getCloseBracket1();

        Document outputForm = new Document(openBracketString + "\ud835\udcab \u2227 \ud835\udcac" + closeBracketString);
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
            return new Pair(false, Collections.singletonList((ParseUtilities.newRegularText("Line (" + line.getLineNumberLabel().getText() + ") is not of the right form to result by " + getName() + "."))));
        }
        try {
            Pair<Boolean, Boolean> inputMatch1 = MatchUtilities.simpleFormMatch(inputFormA, inputDoc1, objectLanguage.getNameString(), metaLanguage.getNameString());
            resultGood1 = true;
        }
        catch (TextMessageException e) {}
        if (resultGood1) {
            try {
                Pair<Boolean, Boolean> inputMatch2 = MatchUtilities.simpleFormMatch(inputFormB, inputDoc2, objectLanguage.getNameString(), metaLanguage.getNameString());
                resultGood2 = true;
            } catch (TextMessageException e) { }
        }
        if (resultGood1 && resultGood2) {
            return new Pair(true, null);
        }

        //try reversed
        MatchUtilities.clearFormMatch();
        resultGood1 = false;
        resultGood2 = false;
        try {
            Pair<Boolean, Boolean> outputMatch = MatchUtilities.simpleFormMatch(outputForm, lineDoc, objectLanguage.getNameString(), metaLanguage.getNameString());
        }
        catch (TextMessageException e) { }
        try {
            Pair<Boolean, Boolean> inputMatch1 = MatchUtilities.simpleFormMatch(inputFormB, inputDoc1, objectLanguage.getNameString(), metaLanguage.getNameString());
            resultGood1 = true;
        }
        catch (TextMessageException e) {}
        if (resultGood1) {
            try {
                Pair<Boolean, Boolean> inputMatch2 = MatchUtilities.simpleFormMatch(inputFormA, inputDoc2, objectLanguage.getNameString(), metaLanguage.getNameString());
                resultGood2 = true;
            } catch (TextMessageException e) { }
        }
        if (resultGood1 && resultGood2) {
            return new Pair(false, Collections.singletonList(ParseUtilities.newRegularText(("Lines (" + inputLine1.getLineNumberLabel().getText() + ") and (" + inputLine2.getLineNumberLabel().getText() +
                    ") are not of the right form to result in (" + line.getLineNumberLabel().getText() + ") by " + getName() + " (check citation order)."))));
        }
        else {
            return new Pair(false, Collections.singletonList(ParseUtilities.newRegularText(("Lines (" + inputLine1.getLineNumberLabel().getText() + ") and (" + inputLine2.getLineNumberLabel().getText() +
                    ") are not of the right form to result in (" + line.getLineNumberLabel().getText() + ") by " + getName() + " (check citation order)."))));
        }

    }


}

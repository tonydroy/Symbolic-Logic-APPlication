package slapp.editor.derivation.DerSystems;

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

public class ModusTollens extends DerivationRule {

    public ModusTollens(String name, String rgexTemplate) {
        super(name, rgexTemplate);
        this.premAssp = false;
    }

    public Pair<Boolean, List<Text>> applies(DerivationCheck checker, ViewLine line, String... inputs) {

        BoxedDRTA lineBDRTA = line.getLineContentBoxedDRTA();
        RichTextArea lineRTA = lineBDRTA.getRTA();
        lineRTA.getActionFactory().saveNow().execute(new ActionEvent());
        Document lineDoc = lineRTA.getDocument();

        ViewLine inputLine1;
        Pair<ViewLine, List<Text>> line1Pair = checker.getLineFromLabel(inputs[0]);
        if (line1Pair.getKey() != null) inputLine1 = line1Pair.getKey();
        else return new Pair(false, line1Pair.getValue());

        BoxedDRTA inputBDRTA1 = inputLine1.getLineContentBoxedDRTA();
        RichTextArea inputRTA1 = inputBDRTA1.getRTA();
        inputRTA1.getActionFactory().saveNow().execute(new ActionEvent());
        Document inputDoc1 = inputRTA1.getDocument();
        if (inputDoc1.getText().equals("")) return new Pair(false, Collections.singletonList(ParseUtilities.newRegularText("Justification cannot appeal to the empty line (" + inputs[0] + ").")));


        ViewLine inputLine2;
        Pair<ViewLine, List<Text>> line2Pair = checker.getLineFromLabel(inputs[1]);
        if (line2Pair.getKey() != null) inputLine2 = line2Pair.getKey();
        else return new Pair(false, line2Pair.getValue());

        BoxedDRTA inputBDRTA2 = inputLine2.getLineContentBoxedDRTA();
        RichTextArea inputRTA2 = inputBDRTA2.getRTA();
        inputRTA2.getActionFactory().saveNow().execute(new ActionEvent());
        Document inputDoc2 = inputRTA2.getDocument();
        if (inputDoc2.getText().equals("")) return new Pair(false, Collections.singletonList(ParseUtilities.newRegularText("Justification cannot appeal to the empty line (" + inputs[1] + ").")));


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

        Document inputFormA = new Document(openBracketString + "\ud835\udcab \u2192 \ud835\udcac" + closeBracketString);
        Document inputFormB = new Document("\u223c\ud835\udcac");
        Document outputForm = new Document("\u223c\ud835\udcab");


        //try correct
        MatchUtilities.clearFormMatch();
        boolean resultGood1 = false;
        boolean resultGood2 = false;
        boolean resultGood3 = false;
        try {
            Pair<Boolean, Boolean> inputMatchA = MatchUtilities.simpleFormMatch(inputFormA, inputDoc1, objectLanguage.getNameString(), metaLanguage.getNameString());
            resultGood1 = true;
        }
        catch (TextMessageException e) {
       //     return new Pair(false, Collections.singletonList((ParseUtilities.newRegularText("Line (" + inputLine1.getLineNumberLabel().getText() + ") is not of the right form be an input to " + getName() + "."))));
        }
        try {
            Pair<Boolean, Boolean> inputMatchB = MatchUtilities.simpleFormMatch(inputFormB, inputDoc2, objectLanguage.getNameString(), metaLanguage.getNameString());
            resultGood2 = true;
        }
        catch (TextMessageException e) {}
        try {
            Pair<Boolean, Boolean> outputMatch = MatchUtilities.simpleFormMatch(outputForm, lineDoc, objectLanguage.getNameString(), metaLanguage.getNameString());
            resultGood3 = true;
        } catch (TextMessageException e) { }

        if (resultGood1 && resultGood2 && resultGood3) {
            return new Pair(true, null);
        }

        //try reversed
        MatchUtilities.clearFormMatch();
        resultGood1 = false;
        resultGood2 = false;
        resultGood3 = false;
        try {
            Pair<Boolean, Boolean> inputMatchA = MatchUtilities.simpleFormMatch(inputFormA, inputDoc2, objectLanguage.getNameString(), metaLanguage.getNameString());
            resultGood1 = true;
        }
        catch (TextMessageException e) { }
        try {
            Pair<Boolean, Boolean> inputMatch1 = MatchUtilities.simpleFormMatch(inputFormB, inputDoc1, objectLanguage.getNameString(), metaLanguage.getNameString());
            resultGood2 = true;
        }
        catch (TextMessageException e) {}
            try {
                Pair<Boolean, Boolean> outputMatch = MatchUtilities.simpleFormMatch(outputForm, lineDoc, objectLanguage.getNameString(), metaLanguage.getNameString());
                resultGood3 = true;
            } catch (TextMessageException e) { }

        if (resultGood1 && resultGood2 && resultGood3) {
            return new Pair(false, Collections.singletonList(ParseUtilities.newRegularText(("Lines (" + inputLine1.getLineNumberLabel().getText() + ") and (" + inputLine2.getLineNumberLabel().getText() +
                    ") are not of the right form to result in (" + line.getLineNumberLabel().getText() + ") by " + getName() + " (check citation order)."))));
        }
        else {
            return new Pair(false, Collections.singletonList(ParseUtilities.newRegularText(("Lines (" + inputLine1.getLineNumberLabel().getText() + ") and (" + inputLine2.getLineNumberLabel().getText() +
                    ") are not of the right form to result in (" + line.getLineNumberLabel().getText() + ") by " + getName() + "."))));
        }

    }


}

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

public class RestrictedExisIntro extends DerivationRule {

    public RestrictedExisIntro(String name, String rgexTemplate) {
        super(name, rgexTemplate);
        this.premAssp = false;
    }

    public Pair<Boolean, List<Text>> applies(DerivationCheck checker, ViewLine line, String... inputs) {

        BoxedDRTA lineBDRTA = line.getLineContentBoxedDRTA();
        RichTextArea lineRTA = lineBDRTA.getRTA();
        lineRTA.getActionFactory().saveNow().execute(new ActionEvent());
        Document lineDoc = lineRTA.getDocument();

        ViewLine inputLine1;
        Pair<ViewLine, List<Text>> inputLine1Pair = checker.getLineFromLabel(inputs[0]);
        if (inputLine1Pair.getKey() != null) inputLine1 = inputLine1Pair.getKey();
        else return new Pair(false, inputLine1Pair.getValue());

        BoxedDRTA inputBDRTA1 = inputLine1.getLineContentBoxedDRTA();
        RichTextArea inputRTA1 = inputBDRTA1.getRTA();
        inputRTA1.getActionFactory().saveNow().execute(new ActionEvent());
        Document inputDoc1 = inputRTA1.getDocument();
        if (inputDoc1.getText().equals("")) return new Pair(false, Collections.singletonList(ParseUtilities.newRegularText("Justification cannot appeal to the empty line (" + inputs[0] + ").")));

        ViewLine inputLine2;
        Pair<ViewLine, List<Text>> inputLine2Pair = checker.getLineFromLabel(inputs[1]);
        if (inputLine2Pair.getKey() != null) inputLine2 = inputLine2Pair.getKey();
        else return new Pair(false, inputLine2Pair.getValue());

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
        String commaDividerString = metaLanguage.getCommaDivider();
        String dividerSymbol = metaLanguage.getDividerSymbol();
        String openBracketString = metaLanguage.getOpenBracket1();
        String closeBracketString = metaLanguage.getCloseBracket1();


        Document inputForm1 = new Document("\ud835\udcab \u2039 \ud835\udccd" + commaDividerString + "\ud835\udcc9 \u203a" );
        Document inputForm2 = new Document("\u212c \u2039 \ud835\udccd" + commaDividerString + "\ud835\udcc9 \u203a" );
        Document outputForm = new Document(openBracketString + "\u2203\ud835\udccd" + dividerSymbol + "\u212c" + closeBracketString + "\ud835\udcab");


        boolean resultGood2 = false;
        boolean resultGood3 = false;


        MatchUtilities.clearFormMatch();
        try {
            Pair<Boolean, Boolean> outputMatch = MatchUtilities.simpleFormMatch(outputForm, lineDoc, objectLanguage.getNameString(), metaLanguage.getNameString());
        }
        catch (TextMessageException e) {
            return new Pair(false, Collections.singletonList(ParseUtilities.newRegularText("Line (" + line.getLineNumberLabel().getText() + ") is not of the right form to result by " + getName() + ".")));
        }

        try {
            Pair<Boolean, Boolean> inputMatch1 = MatchUtilities.simpleFormMatch(inputForm1, inputDoc1, objectLanguage.getNameString(), metaLanguage.getNameString());
            if (inputMatch1.getKey() == true && inputMatch1.getValue() == true ) resultGood2 = true;
        }
        catch (TextMessageException e) {
            return new Pair(false, e.getMessageList());
        }

        try {
            Pair<Boolean, Boolean> inputMatch2 = MatchUtilities.simpleFormMatch(inputForm2, inputDoc2, objectLanguage.getNameString(), metaLanguage.getNameString());
            if (inputMatch2.getKey() == true && inputMatch2.getValue() == true ) resultGood3 = true;
        }
        catch (TextMessageException e) {
            return new Pair(false, e.getMessageList());
        }

        if (resultGood2 && resultGood3) return new Pair(true, null);


        else {
            return new Pair(false, Collections.singletonList(ParseUtilities.newRegularText("Line (" + line.getLineNumberLabel().getText() + ") does not result from (" + inputs[0] + ") and (" + inputs[1] + ") by " + getName() + ".")));
        }



    }


}

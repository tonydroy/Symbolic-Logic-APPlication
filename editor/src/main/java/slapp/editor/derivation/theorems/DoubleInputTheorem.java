package slapp.editor.derivation.theorems;

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
import java.util.regex.Pattern;

public class DoubleInputTheorem extends Theorem {

    public DoubleInputTheorem(String name, String... forms) {
        super(name, forms);
        String regName = name.replace(".", "\\.");
        regName = regName.replace("(", "\\(").replace(")", "\\)");
        String regexString = "^\\s*\\d+\\s*,\\s*\\d+\\s*" + regName + "\\s*$";
        Pattern pattern = Pattern.compile(regexString);
        setMatcher(pattern.matcher(""));
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
        if (inputDoc1.getText().equals("")) return new Pair(false, Collections.singletonList(ParseUtilities.newRegularText("Justification cannot appeal to empty line (" + inputs[0] + ").")));


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

        Document inputFormA = new Document(getForms()[0]);
        Document inputFormB = new Document(getForms()[1]);
        Document outputForm = new Document(getForms()[2]);

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
                    ") are not of the right form to result in (" + line.getLineNumberLabel().getText() + ") by " + getName() + "."))));
        }

    }


}

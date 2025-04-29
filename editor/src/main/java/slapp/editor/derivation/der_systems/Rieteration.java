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

public class Rieteration extends DerivationRule {

    public Rieteration(String name, String rgexTemplate) {
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

        Document outputForm = new Document("\ud835\udcab");
        Document inputForm = new Document("\ud835\udcab");
        boolean resultGood = false;

        MatchUtilities.clearFormMatch();
        try {
            Pair<Boolean, Boolean> outputMatch = MatchUtilities.simpleFormMatch(outputForm, lineDoc, objectLanguage.getNameString(), metaLanguage.getNameString());
        }
        catch (TextMessageException e) { }

        try {
            Pair<Boolean, Boolean> inputMatch = MatchUtilities.simpleFormMatch(inputForm, inputDoc, objectLanguage.getNameString(), metaLanguage.getNameString());
            resultGood = true;
        }
        catch (TextMessageException e) { }

        if (!resultGood) {
            return new Pair(false, Collections.singletonList(ParseUtilities.newRegularText("Line (" + line.getLineNumberLabel().getText() + ") cannot be justified by " + inputs[0] + " " + getName() + ".")));
        }
        return new Pair(true, null);
    }

}

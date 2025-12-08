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

public class EqualityIntro extends DerivationRule {

    public EqualityIntro(String name, String rgexTemplate) {
        super(name, rgexTemplate);
        this.premAssp = false;
    }

    public Pair<Boolean, List<Text>> applies(DCheck checker, ViewLine line, String... inputs) {

        BoxedDRTA lineBDRTA = line.getLineContentBoxedDRTA();
        RichTextArea lineRTA = lineBDRTA.getRTA();
        lineRTA.getActionFactory().saveNow().execute(new ActionEvent());
        Document lineDoc = lineRTA.getDocument();



        Language objectLanguage = checker.getDerivationRuleset().getObjectLanguage();
        Language metaLanguage = checker.getDerivationRuleset().getMetaLanguage();

        String openBracketString = metaLanguage.getOpenBracket1();
        String closeBracketString = metaLanguage.getCloseBracket1();


        Document outputForm1 = new Document(openBracketString + "\ud835\udcc9 \ue8ac \ud835\udcc9" + closeBracketString );
        Document outputForm2 = new Document("\ue8ac\ud835\udcc9\ud835\udcc9");

        boolean resultGood = false;

        MatchUtilities.clearFormMatch();
        try {
            Pair<Boolean, Boolean> outputMatch1 = MatchUtilities.simpleFormMatch(outputForm1, lineDoc, objectLanguage.getNameString(), metaLanguage.getNameString());
            if (outputMatch1.getKey()) resultGood = true;
        }
        catch (TextMessageException e) { }


        try {
            Pair<Boolean, Boolean> outputMatch2 = MatchUtilities.simpleFormMatch(outputForm2, lineDoc, objectLanguage.getNameString(), metaLanguage.getNameString());
            if (outputMatch2.getKey()) resultGood = true;
        }
        catch (TextMessageException e) { }


        if (resultGood) return new Pair(true, null);
        else {
            return new Pair(false, Collections.singletonList(ParseUtilities.newRegularText("Line (" + line.getLineNumberLabel().getText() + ") is not of the right form to result by " + getName() + ".")));
        }

    }


}

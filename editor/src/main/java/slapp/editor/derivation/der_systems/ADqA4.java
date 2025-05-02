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

public class ADqA4 extends DerivationRule {

    public ADqA4(String name, String rgexTemplate) {
        super(name, rgexTemplate);
        this.premAssp = false;
    }

    public Pair<Boolean, List<Text>> applies(DerivationCheck checker, ViewLine line, String... inputs) {

        BoxedDRTA lineBDRTA = line.getLineContentBoxedDRTA();
        RichTextArea lineRTA = lineBDRTA.getRTA();
        lineRTA.getActionFactory().saveNow().execute(new ActionEvent());
        Document lineDoc = lineRTA.getDocument();


        Language objectLanguage = checker.getDerivationRuleset().getObjectLanguage();
        Language metaLanguage = checker.getDerivationRuleset().getMetaLanguage();
        String commaDividerString = metaLanguage.getCommaDivider();


        Document form = new Document("(\u2200\ud835\udccd\ud835\udcab \u2192  \ud835\udcab\u2039\ud835\udccd\" + commaDividerString + \ud835\udcc9\u203a)" );

        boolean resultGood = false;

        //try A
        MatchUtilities.clearFormMatch();
        try {
            Pair<Boolean, Boolean> match = MatchUtilities.simpleFormMatch(form, lineDoc, objectLanguage.getNameString(), metaLanguage.getNameString());
            if (match.getKey()) resultGood = true;
        }
        catch (TextMessageException e) {      }


        if (resultGood) return new Pair(true, null);

        else {
            return new Pair(false, Collections.singletonList(ParseUtilities.newRegularText("Line (" + line.getLineNumberLabel().getText() + ") is not an instance of " + getName() + ".")));
        }

    }


}

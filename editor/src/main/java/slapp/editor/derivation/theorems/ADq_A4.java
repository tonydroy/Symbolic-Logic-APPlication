package slapp.editor.derivation.theorems;

import com.gluonhq.richtextarea.RichTextArea;
import com.gluonhq.richtextarea.model.Document;
import javafx.event.ActionEvent;
import javafx.scene.text.Text;
import javafx.util.Pair;
import slapp.editor.decorated_rta.BoxedDRTA;
import slapp.editor.derivation.DerivationCheck;
import slapp.editor.derivation.ViewLine;
import slapp.editor.parser.*;
import slapp.editor.parser.grammatical_parts.MFormula;
import slapp.editor.parser.grammatical_parts.Term;
import slapp.editor.parser.grammatical_parts.TermType;
import slapp.editor.parser.symbols.MVariable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;


//need this (instead of zero input) to get free for message.

public class ADq_A4 extends Theorem {
    List<Text> freeVariableFailure = new ArrayList<>();
    Language objectLanguage;

    public ADq_A4(String name, String... forms) {
        super(name, forms);
        String regName = name.replace(".", "\\.");
        regName = regName.replace("(", "\\(").replace(")", "\\)");
        String regexString = "^\\s*" + regName + "\\s*$";
        Pattern pattern = Pattern.compile(regexString);
        setMatcher(pattern.matcher(""));
    }

    public Pair<Boolean, List<Text>> applies(DerivationCheck checker, ViewLine line, String... inputs) {

        BoxedDRTA lineBDRTA = line.getLineContentBoxedDRTA();
        RichTextArea lineRTA = lineBDRTA.getRTA();
        lineRTA.getActionFactory().saveNow().execute(new ActionEvent());
        Document lineDoc = lineRTA.getDocument();

        objectLanguage = checker.getDerivationRuleset().getObjectLanguage();
        Language metaLanguage = checker.getDerivationRuleset().getMetaLanguage();

        String form = "(∀\uD835\uDCCD\uD835\uDCAB → \ud835\udcab\u2039\ud835\udccd , \ud835\udcc9\u203a)";


        MatchUtilities.clearFormMatch();
        try {
            Pair<Boolean, Boolean> resultGood = MatchUtilities.simpleFormMatch(new Document(form), lineDoc, objectLanguage.getNameString(), metaLanguage.getNameString());
            if (resultGood.getKey() == true ) return new Pair(true, null);
        }
        catch (TextMessageException e) {
            return new Pair(false, e.getMessageList());
        }

        return new Pair(false, Collections.singletonList(ParseUtilities.newRegularText("Line (" + line.getLineNumberLabel().getText() + ") is not an instance of " + getName() + ".")));

    }


}

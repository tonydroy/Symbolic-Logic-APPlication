package slapp.editor.derivation.theorems;

import com.gluonhq.richtextarea.RichTextArea;
import com.gluonhq.richtextarea.model.DecorationModel;
import com.gluonhq.richtextarea.model.Document;
import com.gluonhq.richtextarea.model.ParagraphDecoration;
import com.gluonhq.richtextarea.model.TextDecoration;
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
import java.util.regex.Pattern;

public class AD_A7 extends Theorem {

    public AD_A7(String name, String... forms) {
        super(name, forms);
        String regName = name.replace(".", "\\.");
        regName = regName.replace("(", "\\(").replace(")", "\\)");
        String regexString = "^\\s*" + regName + "\\s*$";
        Pattern pattern = Pattern.compile(regexString);
        setMatcher(pattern.matcher(""));
    }

    public Pair<Boolean, List<Text>> applies(DCheck checker, ViewLine line, String... inputs) {

        BoxedDRTA lineBDRTA = line.getLineContentBoxedDRTA();
        RichTextArea lineRTA = lineBDRTA.getRTA();
        lineRTA.getActionFactory().saveNow().execute(new ActionEvent());
        Document lineDoc = lineRTA.getDocument();

        Language objectLanguage = checker.getDerivationRuleset().getObjectLanguage();
        Language metaLanguage = checker.getDerivationRuleset().getMetaLanguage();

        TextDecoration base = TextDecoration.builder().presets().superscript(false).build();
        TextDecoration sup = TextDecoration.builder().presets().superscript(true).build();
        ParagraphDecoration par = ParagraphDecoration.builder().presets().build();

        // one place
        List<DecorationModel> dec1List = List.of(
                new DecorationModel(0, 12, base, par),
                new DecorationModel(12, 1, sup, par),
                new DecorationModel(13, 4, base, par),
                new DecorationModel(17, 1, sup, par),
                new DecorationModel(18, 3, base, par)
        );
        Document form1Doc = new Document("(\uE8AC\uD835\uDCCD\uD835\uDCCC → \uE8AC\uD835\uDCBD1\uD835\uDCCD\uD835\uDCBD1\uD835\uDCCC)", dec1List, 21);

        //two place (metalanguage uses unabbreviated prefix form)
        List<DecorationModel> dec2List = List.of(
                new DecorationModel(0, 12, base, par),
                new DecorationModel(12, 1, sup, par),
                new DecorationModel(13, 6, base, par),
                new DecorationModel(19, 1, sup, par),
                new DecorationModel(20, 5, base, par)
        );
        Document form2aDoc = new Document("(\uE8AC\uD835\uDCCD\uD835\uDCCB → \uE8AC\uD835\uDCBB2\uD835\uDCCD\uD835\uDCCE\uD835\uDCBB2\uD835\uDCCB\uD835\uDCCE)", dec2List, 25);
        Document form2bDoc = new Document("(\uE8AC\uD835\uDCCE\uD835\uDCCB → \uE8AC\uD835\uDCBB2\uD835\uDCCD\uD835\uDCCE\uD835\uDCBB2\uD835\uDCCD\uD835\uDCCB)", dec2List, 25);

        //three place
        List<DecorationModel> dec3List = List.of(
                new DecorationModel(0, 12, base, par),
                new DecorationModel(12, 1, sup, par),
                new DecorationModel(13, 8, base, par),
                new DecorationModel(21, 1, sup, par),
                new DecorationModel(22, 7, base, par)
        );
        Document form3aDoc = new Document("(\uE8AC\uD835\uDCCD\uD835\uDCCC → \uE8AC\uD835\uDCBD3\uD835\uDCCD\uD835\uDCCE\uD835\uDCCF\uD835\uDCBD3\uD835\uDCCC\uD835\uDCCE\uD835\uDCCF)", dec3List, 29);
        Document form3bDoc = new Document("(\uE8AC\uD835\uDCCE\uD835\uDCCC → \uE8AC\uD835\uDCBD3\uD835\uDCCD\uD835\uDCCE\uD835\uDCCF\uD835\uDCBD3\uD835\uDCCD\uD835\uDCCC\uD835\uDCCF)", dec3List, 29);
        Document form3cDoc = new Document("(\uE8AC\uD835\uDCCF\uD835\uDCCC → \uE8AC\uD835\uDCBD3\uD835\uDCCD\uD835\uDCCE\uD835\uDCCF\uD835\uDCBD3\uD835\uDCCD\uD835\uDCCE\uD835\uDCCC)", dec3List, 29);

        boolean resultGood = false;

        MatchUtilities.clearFormMatch();
        try {
            Pair<Boolean, Boolean> match = MatchUtilities.simpleFormMatch(form1Doc, lineDoc, objectLanguage.getNameString(), metaLanguage.getNameString());
            if (match.getKey()) return new Pair(true, null);
        }
        catch (TextMessageException e) {}

        MatchUtilities.clearFormMatch();
        try {
            Pair<Boolean, Boolean> match = MatchUtilities.simpleFormMatch(form2aDoc, lineDoc, objectLanguage.getNameString(), metaLanguage.getNameString());
            if (match.getKey()) return new Pair(true, null);
        }
        catch (TextMessageException e) {}

        MatchUtilities.clearFormMatch();
        try {
            Pair<Boolean, Boolean> match = MatchUtilities.simpleFormMatch(form2bDoc, lineDoc, objectLanguage.getNameString(), metaLanguage.getNameString());
            if (match.getKey()) return new Pair(true, null);
        }
        catch (TextMessageException e) {}

        MatchUtilities.clearFormMatch();
        try {
            Pair<Boolean, Boolean> match = MatchUtilities.simpleFormMatch(form3aDoc, lineDoc, objectLanguage.getNameString(), metaLanguage.getNameString());
            if (match.getKey()) return new Pair(true, null);
        }
        catch (TextMessageException e) {}

        MatchUtilities.clearFormMatch();
        try {
            Pair<Boolean, Boolean> match = MatchUtilities.simpleFormMatch(form3bDoc, lineDoc, objectLanguage.getNameString(), metaLanguage.getNameString());
            if (match.getKey()) return new Pair(true, null);
        }
        catch (TextMessageException e) {}

        MatchUtilities.clearFormMatch();
        try {
            Pair<Boolean, Boolean> match = MatchUtilities.simpleFormMatch(form3cDoc, lineDoc, objectLanguage.getNameString(), metaLanguage.getNameString());
            if (match.getKey()) return new Pair(true, null);
        }
        catch (TextMessageException e) {}

        return new Pair(false, Collections.singletonList(ParseUtilities.newRegularText("Line (" + line.getLineNumberLabel().getText() + ") is not an instance of " + getName() + ".")));


    }

}

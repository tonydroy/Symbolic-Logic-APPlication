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
import slapp.editor.derivation.DerivationCheck;
import slapp.editor.derivation.ViewLine;
import slapp.editor.parser.Language;
import slapp.editor.parser.MatchUtilities;
import slapp.editor.parser.ParseUtilities;
import slapp.editor.parser.TextMessageException;

import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

public class AD_A8 extends Theorem {

    public AD_A8(String name, String... forms) {
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

        Language objectLanguage = checker.getDerivationRuleset().getObjectLanguage();
        Language metaLanguage = checker.getDerivationRuleset().getMetaLanguage();

        TextDecoration base = TextDecoration.builder().presets().superscript(false).build();
        TextDecoration sup = TextDecoration.builder().presets().superscript(true).build();
        ParagraphDecoration par = ParagraphDecoration.builder().presets().build();

        List<DecorationModel> dec1List = List.of(
                new DecorationModel(0, 15, base, par),
                new DecorationModel(15, 1, sup, par),
                new DecorationModel(16, 6, base, par),
                new DecorationModel(22, 1, sup, par),
                new DecorationModel(23, 4, base, par)
        );
        Document form1Doc = new Document("((\uD835\uDCCD \uE8AC \uD835\udccb) → (ℛ1\uD835\uDCCD → ℛ1\uD835\uDCCE))", dec1List, 27);

        List<DecorationModel> dec2List = List.of(
                new DecorationModel(0, 15, base, par),
                new DecorationModel(15, 1, sup, par),
                new DecorationModel(16, 8, base, par),
                new DecorationModel(24, 1, sup, par),
                new DecorationModel(25, 6, base, par)
        );
        Document form2aDoc = new Document("((\uD835\uDCCD \uE8AC \uD835\udccb) → (ℛ2\uD835\uDCCD\uD835\uDCCE → ℛ2\uD835\udccb\uD835\uDCCE))", dec2List, 31);
        Document form2bDoc = new Document("((\uD835\uDCCE \uE8AC \uD835\udccb) → (ℛ2\uD835\uDCCD\uD835\uDCCE → ℛ2\uD835\uDCCD\uD835\udccb))", dec2List, 31);

        List<DecorationModel> dec3List = List.of(
                new DecorationModel(0, 15, base, par),
                new DecorationModel(15, 1, sup, par),
                new DecorationModel(16, 10, base, par),
                new DecorationModel(26, 1, sup, par),
                new DecorationModel(27, 8, base, par)
        );
        Document form3aDoc = new Document("((\uD835\uDCCD \uE8AC \uD835\udccb) → (ℛ3\uD835\uDCCD\uD835\uDCCE\uD835\uDCCF → ℛ3\uD835\udccb\uD835\uDCCE\uD835\uDCCF))", dec3List, 35);
        Document form3bDoc = new Document("((\uD835\uDCCE \uE8AC \uD835\udccb) → (ℛ3\uD835\uDCCD\uD835\uDCCE\uD835\uDCCF → ℛ3\uD835\uDCCD\uD835\udccb\uD835\uDCCF))", dec3List, 35);
        Document form3cDoc = new Document("((\uD835\uDCCF \uE8AC \uD835\udccb) → (ℛ3\uD835\uDCCD\uD835\uDCCE\uD835\uDCCF → ℛ3\uD835\uDCCD\uD835\uDCCE\uD835\udccb))", dec3List, 35);



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

        return new Pair(false, Collections.singletonList(ParseUtilities.newRegularText("Line (" + line.getLineNumberLabel().getText() + ") is not an instance of " + getName() + ".\n (SLAPP recognizes " + getName() + " only for 1- to 3-place relation symbols).")));


    }

}

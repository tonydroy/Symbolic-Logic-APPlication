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

public class ADT3_38 extends Theorem {

    public ADT3_38(String name, String... forms) {
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

        //one place
        List<DecorationModel> dec1List = List.of(
                new DecorationModel(0, 11, base, par),
                new DecorationModel(11, 1, sup, par),
                new DecorationModel(12, 6, base, par),
                new DecorationModel(18, 1, sup, par),
                new DecorationModel(19, 4, base, par)
        );
        Document form1Doc = new Document("(\uE8AC\uD835\uDCC2\uD835\uDCC8 → (ℛ1\uD835\uDCC2 → ℛ1\uD835\uDCC8))", dec1List, 23);

        //two place (metalanguage uses unabbreviated prefix form)
        List<DecorationModel> dec2List = List.of(
                new DecorationModel(0, 11, base, par),
                new DecorationModel(11, 1, sup, par),
                new DecorationModel(12, 8, base, par),
                new DecorationModel(20, 1, sup, par),
                new DecorationModel(21, 6, base, par)
        );
        Document form2aDoc = new Document("(\uE8AC\uD835\uDCC2\uD835\uDCC8 → (ℛ2\uD835\uDCC2\uD835\uDCC3 → ℛ2\uD835\uDCC8\uD835\uDCC3))", dec2List, 27);
        Document form2bDoc = new Document("(\uE8AC\uD835\uDCC3\uD835\uDCC8 → (ℛ2\uD835\uDCC2\uD835\uDCC3 → ℛ2\uD835\uDCC2\uD835\uDCC8))", dec2List, 27);

        //three place
        List<DecorationModel> dec3List = List.of(
                new DecorationModel(0, 11, base, par),
                new DecorationModel(11, 1, sup, par),
                new DecorationModel(12, 9, base, par),
                new DecorationModel(21, 1, sup, par),
                new DecorationModel(22, 7, base, par)
        );
        List<DecorationModel> dec3aList = List.of(
                new DecorationModel(0, 10, base, par),
                new DecorationModel(10, 1, sup, par),
                new DecorationModel(11, 9, base, par),
                new DecorationModel(20, 1, sup, par),
                new DecorationModel(21, 8, base, par)
        );
        Document form3aDoc = new Document("(\uE8AC\uD835\uDCC2\uD835\uDCC8 → (ℛ3\uD835\uDCC2\uD835\uDCC3ℴ → ℛ3\uD835\uDCC8\uD835\uDCC3ℴ))", dec3List, 29);
        Document form3bDoc = new Document("(\uE8AC\uD835\uDCC3\uD835\uDCC8 → (ℛ3\uD835\uDCC2\uD835\uDCC3ℴ → ℛ3\uD835\uDCC2\uD835\uDCC8ℴ))", dec3List, 29);
        Document form3cDoc = new Document("(\uE8ACℴ\uD835\uDCC8 → (ℛ3\uD835\uDCC2\uD835\uDCC3ℴ → ℛ3\uD835\uDCC2\uD835\uDCC3\uD835\uDCC8))", dec3aList, 27);



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

        return new Pair(false, Collections.singletonList(ParseUtilities.newRegularText("Line (" + line.getLineNumberLabel().getText() + ") is not an instance of " + getName() + ".\n (SLAPP recognizes " + getName() + " only for 1-, 2- and 3-place relation symbols).")));


    }

}

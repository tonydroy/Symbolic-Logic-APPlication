package slapp.editor.derivation.der_systems;

import com.gluonhq.richtextarea.RichTextArea;
import com.gluonhq.richtextarea.model.Document;
import javafx.event.ActionEvent;
import javafx.scene.text.Text;
import javafx.util.Pair;
import slapp.editor.decorated_rta.BoxedDRTA;
import slapp.editor.derivation.DerivationCheck;
import slapp.editor.derivation.ViewLine;
import slapp.editor.parser.ParseUtilities;

import java.util.Collections;
import java.util.List;

public class AsspBicondIntro extends DerivationRule {

    public AsspBicondIntro(String name, String rgexTemplate) {
        super(name, rgexTemplate);
        this.premAssp = true;

    }

    public Pair<Boolean, List<Text>> applies(DerivationCheck checker, ViewLine line, String... inputs) {

        ViewLine bottomLine = checker.lastLineAtScope(line);

        BoxedDRTA bottomDRTA = bottomLine.getLineContentBoxedDRTA();
        RichTextArea bottomRTA = bottomDRTA.getRTA();
        bottomRTA.getActionFactory().saveNow().execute(new ActionEvent());
        Document bottomLineDoc = bottomRTA.getDocument();

        if (bottomLineDoc.getText().equals("")) {
            String bottomLineLabel = bottomLine.getLineNumberLabel().getText();
            return new Pair<>(false, Collections.singletonList(ParseUtilities.newRegularText("Exit strategy is not complete without statement of the goal formula at the bottom of the scope line (" + bottomLineLabel + ").")));
        }

        return new Pair(true, null);




    }
}

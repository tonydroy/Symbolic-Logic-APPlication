package slapp.editor.derivation.DerSystems;

import javafx.scene.text.Text;
import slapp.editor.derivation.DerivationCheck;
import slapp.editor.derivation.ViewLine;
import slapp.editor.parser.Language;
import javafx.util.Pair;

import javax.swing.text.View;
import java.util.List;

public class ConjunctionIntro extends DerivationRule {

    public ConjunctionIntro(String name, String rgexTemplate) {
        super(name, rgexTemplate);
        this.premAssp = false;
    }

    public Pair<Boolean, List<Text>> applies(DerivationCheck checker, ViewLine line, String... inputs) {

        return new Pair(true, null);
    }


}

package slapp.editor.derivation.DerSystems;

import javafx.scene.text.Text;
import javafx.util.Pair;
import slapp.editor.derivation.ViewLine;
import slapp.editor.parser.Language;

import java.util.List;

public class Premise extends DerivationRule {

    public Premise(String rgexTemplate) {
        super(rgexTemplate);
        this.premAssp = true;
    }

    public Pair<Boolean, List<Text>> applies(List<ViewLine> viewLines, String... inputs) {

        return new Pair(true, null);
    }


}

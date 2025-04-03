package slapp.editor.derivation.DerSystems;

import javafx.scene.text.Text;
import javafx.util.Pair;
import slapp.editor.derivation.DerivationCheck;
import slapp.editor.derivation.ViewLine;

import java.util.List;

public class Rieteration extends DerivationRule {

    public Rieteration(String name, String rgexTemplate) {
        super(name, rgexTemplate);
        this.premAssp = false;

    }

    public Pair<Boolean, List<Text>> applies(DerivationCheck checker, ViewLine line, String... inputs) {

        return new Pair(true, null);
    }


}

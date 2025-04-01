package slapp.editor.derivation.DerSystems;

import javafx.scene.text.Text;
import javafx.util.Pair;
import slapp.editor.derivation.ViewLine;
import slapp.editor.parser.Language;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class DerivationRule {

    Matcher matcher;
    boolean premAssp;

    public DerivationRule(String rgexTemplate) {
        Pattern pattern = Pattern.compile(rgexTemplate);
        matcher = pattern.matcher("");
    }

    public boolean isPremAssp() {
        return premAssp;
    }

    public boolean matches(String input) {
        matcher.reset(input);
        return matcher.matches();
    }

    abstract Pair<Boolean, List<Text>> applies(List<ViewLine> viewLines, String... inputs);


}

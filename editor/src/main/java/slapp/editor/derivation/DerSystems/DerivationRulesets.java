package slapp.editor.derivation.DerSystems;

import javafx.scene.text.Text;
import slapp.editor.parser.Languages;
import slapp.editor.parser.ParseUtilities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DerivationRulesets {
    private static List<DerivationRuleset> rulesets = new ArrayList<>();

    static {
        loadRulesets();
    }

    public DerivationRulesets() {}

    private static void loadRulesets() {
        rulesets.add(getNDs());
    }

    public static DerivationRuleset getRuleset(String name) {
        for (DerivationRuleset ruleset : rulesets) {
            if (ruleset.getName().equals(name)) return ruleset;
        }
        return null;
    }




    private static DerivationRuleset getNDs() {
        DerivationRuleset NDs = new DerivationRuleset("NDs", Collections.singletonList(ParseUtilities.newItalicText("NDs")), Languages.getLanguage("Ls"));

        List<DerivationRule> rules = new ArrayList<>();

        rules.add(new Premise("\\s*P\\s*"));
        rules.add(new AsspCondIntro("\\s*A\\s*\\((g|\\ud835\\udc54),?\\s*\\u2192I\\)\\s*"));
        rules.add(new AsspBicondIntro("\\s*A\\s*\\((g|\\ud835\\udc54),?\\s*\\u2194I\\)\\s*"));
        rules.add(new AsspNegIntro("\\s*A\\s*\\((c|\\ud835\\udc50),?\\s*\\u223cI\\)\\s*"));
        rules.add(new AsspNegExploit("\\s*A\\s*\\((c|\\ud835\\udc50),?\\s*\\u223cE\\)\\s*"));
        rules.add(new AsspDisjExploit("\\s*A\\s*\\((g|\\ud835\\udc54|c|\\ud835\\udc50),?\\s*\\u2228E\\)\\s*"));

        rules.add(new ConjunctionIntro("\\s*\\d+\\s+,\\s*\\d+\\s*\\u2227I\\s*"));
        rules.add(new ConjunctionExploit("\\s*\\d+\\s*\\u2227E\\s*"));

        NDs.setRules(rules);
        return NDs;
    }

}

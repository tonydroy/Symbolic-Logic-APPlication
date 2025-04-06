package slapp.editor.derivation.DerSystems;

import javafx.util.Pair;
import slapp.editor.parser.Languages;
import slapp.editor.parser.ParseUtilities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

public class DerivationRulesets implements Serializable {
    private static List<DerivationRuleset> rulesets = new ArrayList<>();

    static {
        loadRulesets();
    }

    public DerivationRulesets() {}

    private static void loadRulesets() {
        rulesets.add(getNDs());
        rulesets.add(getNDs_p());
    }

    public static DerivationRuleset getRuleset(String name) {
        for (DerivationRuleset ruleset : rulesets) {
            if (ruleset.getName().equals(name)) return ruleset;
        }
        return null;
    }




    private static DerivationRuleset getNDs() {
        DerivationRuleset NDs = new DerivationRuleset("NDs", Collections.singletonList(ParseUtilities.newItalicText("NDs")), Languages.getLanguage("Ls_abv"), Languages.getLanguage("Meta"));

        DerivationRule premiseRule = new Premise("P","^\\s*P\\s*$");
        NDs.setPremiseRule(premiseRule);

        List<DerivationRule> rules = new ArrayList<>();
        rules.add(premiseRule);
        rules.add(new AsspCondIntro("A","^\\s*A\\s*\\((g|\\ud835\\udc54),?\\s*\\u2192I\\)\\s*$"));   //assp arrow I
        rules.add(new AsspBicondIntro("A","^\\s*A\\s*\\((g|\\ud835\\udc54),?\\s*\\u2194I\\)\\s*$")); //assp double arrow I
        rules.add(new AsspNegIntro("A","^\\s*A\\s*\\((c|\\ud835\\udc50),?\\s*\\u223cI\\)\\s*$"));  //assp tilde I
        rules.add(new AsspNegExploit("A","^\\s*A\\s*\\((c|\\ud835\\udc50),?\\s*\\u223cE\\)\\s*$"));  //assp tilde E
        rules.add(new AsspDisjExploitG("A","^\\s*A\\s*\\((g|\\ud835\\udc54),?\\s*\\d+\\s*\\u2228E\\)\\s*$"));  //assp disj E for goal
        rules.add(new AsspDisjExploitC("A","^\\s*A\\s*\\((c|\\ud835\\udc50),?\\s*\\d*\\s*\\u2228E\\)\\s*$"));  //assp disj E for contradiction

        rules.add(new Rieteration("R", "^\\s*\\d+\\s*R\\s*$"));  //reiteration
        rules.add(new ConjunctionIntro("\u2227I","^\\s*\\d+\\s*,\\s*\\d+\\s*\\u2227I\\s*$"));  // caret I
        rules.add(new ConjunctionExploit("\u2227E","^\\s*\\d+\\s*\\u2227E\\s*$"));  //caret E
        rules.add(new ConditionalExploit("\u2192E","^\\s*\\d+\\s*,\\s*\\d+\\s*\\u2192E\\s*$"));  // arrow E
        rules.add(new DisjunctionIntro("\u2228I", "^\\s*\\d+\\s*\\u2228I\\s*$"));  //wedgeI))
        rules.add(new BiconditionalExploit("\u2194E","^\\s*\\d+\\s*,\\s*\\d+\\s*\\u2194E\\s*$"));  // double arrow E
        rules.add(new ContradictionIntro("\u22a5I", "^\\s*\\d+\\s*,\\s*\\d+\\s*\\u22a5I\\s*$"));

        rules.add(new ConditionalIntro("\u2192I", "^\\s*\\d+\\s*-\\s*\\d+\\s*\\u2192I\\s*$"));  // arrow I
        rules.add(new BiconditionalIntro("\u2194I", "^\\s*\\d+\\s*-\\s*\\d+,\\s*\\d+\\s*-\\s*\\d+\\s*\\u2194I\\s*$")); // double arrow I
        rules.add(new NegationIntro("\u223cI", "^\\s*\\d+\\s*-\\s*\\d+\\s*\\u223cI\\s*$"));  // tilde I
        rules.add(new NegationExploit("\u223cE", "^\\s*\\d+\\s*-\\s*\\d+\\s*\\u223cE\\s*$"));  // tilde E
        rules.add(new DisjunctionExploit("\u2228E", "^\\s*\\d+\\s*,\\s*\\d+\\s*-\\s*\\d+,\\s*\\d+\\s*-\\s*\\d+\\s*\\u2228E\\s*$")); // wedge E

        NDs.setRules(rules);

        List<Pair<Pattern, String>> dummyRules = new ArrayList<>();
        dummyRules.add(new Pair(Pattern.compile("^\\s*A\\s*$"), "Assumption must by followed by (parenthetical) exit strategy."));
        dummyRules.add(new Pair(Pattern.compile("^.*\\((g|\\ud835\\udc54|c|\\ud835\\udc50),?\\s*\\u2227I\\)\\s*$"), "\u2227I does not appeal to a subderivation and cannot appear as part of an exit strategy." ));
        dummyRules.add(new Pair(Pattern.compile("^.*\\((g|\\ud835\\udc54|c|\\ud835\\udc50),?\\s*\\u2227E\\)\\s*$"), "\u2227E does not appeal to a subderivation and cannot appear as part of an exit strategy." ));
        dummyRules.add(new Pair(Pattern.compile("^.*\\((g|\\ud835\\udc54|c|\\ud835\\udc50),?\\s*\\u2192E\\)\\s*$"), "\u2192E does not appeal to a subderivation and cannot appear as part of an exit strategy." ));
        dummyRules.add(new Pair(Pattern.compile("^.*\\((g|\\ud835\\udc54|c|\\ud835\\udc50),?\\s*\\u2228I\\)\\s*$"), "\u2228I does not appeal to a subderivation and cannot appear as part of an exit strategy." ));
        dummyRules.add(new Pair(Pattern.compile("^.*\\((g|\\ud835\\udc54|c|\\ud835\\udc50),?\\s*\\u2194E\\)\\s*$"), "\u2194E does not appeal to a subderivation and cannot appear as part of an exit strategy." ));
        dummyRules.add(new Pair(Pattern.compile("^\\s*\\d+\\s*\\u2192E\\s*$"), "\u2192E requires two lines: both the conditional and the antecedent."));
        dummyRules.add(new Pair(Pattern.compile("^\\s*\\d+\\s*,\\s*\\d+\\s*\\u2192I\\s*$"), "\u2192I requires a subderivation, not two individual lines."));
        dummyRules.add(new Pair(Pattern.compile("^\\s*A\\s*\\((g|\\ud835\\udc54),?\\s*\\u2228E\\)\\s*$"), "Exit strategy for \u2228E should cite the line number of the disjunction to which it applies."));
        dummyRules.add(new Pair(Pattern.compile("^\\s*A\\s*\\((c|\\ud835\\udc50),?\\s*\\u2228E\\)\\s*$"), "Exit strategy for \u2228E should cite the line number of the disjunction to which it applies."));

        NDs.setDummyRules(dummyRules);

        NDs.setGenericAssumption(Pattern.compile("^\\s*A.*$"));
        NDs.setRequirePremisesAtTop(true);

        return NDs;
    }

    private static DerivationRuleset getNDs_p() {
        DerivationRuleset NDs_p = new DerivationRuleset("NDs+", Collections.singletonList(ParseUtilities.newItalicText("NDs+")), Languages.getLanguage("Ls_abv"), Languages.getLanguage("Meta"));

        DerivationRule premiseRule = new Premise("P","^\\s*P\\s*$");
        NDs_p.setPremiseRule(premiseRule);

        List<DerivationRule> rules = new ArrayList<>();
        rules.add(premiseRule);
        rules.add(new AsspCondIntro("A","^\\s*A\\s*\\((g|\\ud835\\udc54),?\\s*\\u2192I\\)\\s*$"));   //assp arrow I
        rules.add(new AsspBicondIntro("A","^\\s*A\\s*\\((g|\\ud835\\udc54),?\\s*\\u2194I\\)\\s*$")); //assp double arrow I
        rules.add(new AsspNegIntro("A","^\\s*A\\s*\\((c|\\ud835\\udc50),?\\s*\\u223cI\\)\\s*$"));  //assp tilde I
        rules.add(new AsspNegExploit("A","^\\s*A\\s*\\((c|\\ud835\\udc50),?\\s*\\u223cE\\)\\s*$"));  //assp tilde E
        rules.add(new AsspDisjExploitG("A","^\\s*A\\s*\\((g|\\ud835\\udc54),?\\s*\\d+\\s*\\u2228E\\)\\s*$"));  //assp disj E for goal
        rules.add(new AsspDisjExploitC("A","^\\s*A\\s*\\((c|\\ud835\\udc50),?\\s*\\d*\\s*\\u2228E\\)\\s*$"));  //assp disj E for contradiction

        rules.add(new Rieteration("R", "^\\s*\\d+\\s*R\\s*$"));  //reiteration
        rules.add(new ConjunctionIntro("\u2227I","^\\s*\\d+\\s*,\\s*\\d+\\s*\\u2227I\\s*$"));  // caret I
        rules.add(new ConjunctionExploit("\u2227E","^\\s*\\d+\\s*\\u2227E\\s*$"));  //caret E
        rules.add(new ConditionalExploit("\u2192E","^\\s*\\d+\\s*,\\s*\\d+\\s*\\u2192E\\s*$"));  // arrow E
        rules.add(new DisjunctionIntro("\u2228I", "^\\s*\\d+\\s*\\u2228I\\s*$"));  //wedgeI))
        rules.add(new BiconditionalExploit("\u2194E","^\\s*\\d+\\s*,\\s*\\d+\\s*\\u2194E\\s*$"));  // double arrow E
        rules.add(new ContradictionIntro("\u22a5I", "^\\s*\\d+\\s*,\\s*\\d+\\s*\\u22a5I\\s*$"));

        rules.add(new ConditionalIntro("\u2192I", "^\\s*\\d+\\s*-\\s*\\d+\\s*\\u2192I\\s*$"));  // arrow I
        rules.add(new BiconditionalIntro("\u2194I", "^\\s*\\d+\\s*-\\s*\\d+,\\s*\\d+\\s*-\\s*\\d+\\s*\\u2194I\\s*$")); // double arrow I
        rules.add(new NegationIntro("\u223cI", "^\\s*\\d+\\s*-\\s*\\d+\\s*\\u223cI\\s*$"));  // tilde I
        rules.add(new NegationExploit("\u223cE", "^\\s*\\d+\\s*-\\s*\\d+\\s*\\u223cE\\s*$"));  // tilde E
        rules.add(new DisjunctionExploit("\u2228E", "^\\s*\\d+\\s*,\\s*\\d+\\s*-\\s*\\d+,\\s*\\d+\\s*-\\s*\\d+\\s*\\u2228E\\s*$")); // wedge E

        //plus rules
        rules.add(new ContradictionExploit("\u22a5E", "^\\s*\\d+\\s*\\u22a5E\\s*$"));  //bottomE
        rules.add(new ModusTollens("MT","^\\s*\\d+\\s*,\\s*\\d+\\s*MT\\s*$"));  // MT
        rules.add(new NegatedBiconditional("NB","^\\s*\\d+\\s*,\\s*\\d+\\s*NB\\s*$"));  //NB
        rules.add(new DisjunctiveSyllogism("DS","^\\s*\\d+\\s*,\\s*\\d+\\s*DS\\s*$"));  //DS
        rules.add(new HyptheticalSyllogism("HS","^\\s*\\d+\\s*,\\s*\\d+\\s*HS\\s*$"));  // HS

        rules.add(new DoubleNegation("DN", "^\\s*\\d+\\s*DN\\s*$"));  //DN
        rules.add(new Idempotence("Idem", "^\\s*\\d+\\s*Idem\\s*$"));  //Idem
        rules.add(new Association("Assoc", "^\\s*\\d+\\s*Assoc\\s*$"));  //Assoc
        rules.add(new Commutation("Com", "^\\s*\\d+\\s*Com\\s*$"));  //Com
        rules.add(new Exportation("Exp", "^\\s*\\d+\\s*Exp\\s*$"));  //Exp
        rules.add(new Transposition("Trans", "^\\s*\\d+\\s*Trans\\s*$"));  //Trans
        rules.add(new DeMorgan("DeM", "^\\s*\\d+\\s*DeM\\s*$"));  //DeM
        rules.add(new Implication("Impl", "^\\s*\\d+\\s*Impl\\s*$"));  //Impl
        rules.add(new Distribution("Dist", "^\\s*\\d+\\s*Dist\\s*$"));  //Dist
        rules.add(new Equivalence("Equiv", "^\\s*\\d+\\s*Equiv\\s*$"));  //Equiv







        NDs_p.setRules(rules);

        List<Pair<Pattern, String>> dummyRules = new ArrayList<>();
        dummyRules.add(new Pair(Pattern.compile("^\\s*A\\s*$"), "Assumption must by followed by (parenthetical) exit strategy."));
        dummyRules.add(new Pair(Pattern.compile("^.*\\((g|\\ud835\\udc54|c|\\ud835\\udc50),?\\s*\\u2227I\\)\\s*$"), "\u2227I does not appeal to a subderivation and cannot appear as part of an exit strategy." ));
        dummyRules.add(new Pair(Pattern.compile("^.*\\((g|\\ud835\\udc54|c|\\ud835\\udc50),?\\s*\\u2227E\\)\\s*$"), "\u2227E does not appeal to a subderivation and cannot appear as part of an exit strategy." ));
        dummyRules.add(new Pair(Pattern.compile("^.*\\((g|\\ud835\\udc54|c|\\ud835\\udc50),?\\s*\\u2192E\\)\\s*$"), "\u2192E does not appeal to a subderivation and cannot appear as part of an exit strategy." ));
        dummyRules.add(new Pair(Pattern.compile("^.*\\((g|\\ud835\\udc54|c|\\ud835\\udc50),?\\s*\\u2228I\\)\\s*$"), "\u2228I does not appeal to a subderivation and cannot appear as part of an exit strategy." ));
        dummyRules.add(new Pair(Pattern.compile("^.*\\((g|\\ud835\\udc54|c|\\ud835\\udc50),?\\s*\\u2194E\\)\\s*$"), "\u2194E does not appeal to a subderivation and cannot appear as part of an exit strategy." ));
        dummyRules.add(new Pair(Pattern.compile("^\\s*\\d+\\s*\\u2192E\\s*$"), "\u2192E requires two lines: both the conditional and the antecedent."));
        dummyRules.add(new Pair(Pattern.compile("^\\s*\\d+\\s*,\\s*\\d+\\s*\\u2192I\\s*$"), "\u2192I requires a subderivation, not two individual lines."));
        dummyRules.add(new Pair(Pattern.compile("^\\s*A\\s*\\((g|\\ud835\\udc54),?\\s*\\u2228E\\)\\s*$"), "Exit strategy for \u2228E should cite the line number of the disjunction to which it applies."));
        dummyRules.add(new Pair(Pattern.compile("^\\s*A\\s*\\((c|\\ud835\\udc50),?\\s*\\u2228E\\)\\s*$"), "Exit strategy for \u2228E should cite the line number of the disjunction to which it applies."));

        NDs_p.setDummyRules(dummyRules);

        NDs_p.setGenericAssumption(Pattern.compile("^\\s*A.*$"));
        NDs_p.setRequirePremisesAtTop(true);

        return NDs_p;
    }


}

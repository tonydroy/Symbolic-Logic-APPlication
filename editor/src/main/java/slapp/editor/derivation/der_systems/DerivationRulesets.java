package slapp.editor.derivation.der_systems;

import javafx.util.Pair;
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
        rulesets.add(getND());
        rulesets.add(getND_p());
        rulesets.add(getADs());
        rulesets.add(getADq());
        rulesets.add(getAD());
    }

    public static List<DerivationRuleset> getRulesets() {
        return rulesets;
    }

    public static DerivationRuleset getRuleset(String rulesetName) {
        for (DerivationRuleset ruleset : rulesets) {
            if (ruleset.getName().equals(rulesetName))  return ruleset;
        }
        return null;
    }

    private static DerivationRuleset getADs() {
        DerivationRuleset ADs = new DerivationRuleset("\ud835\udc34\ud835\udc37\ud835\udc60", Collections.singletonList(ParseUtilities.newItalicText("ADs")));

        DerivationRule premiseRule = new Premise("prem","^\\s*prem\\s*$");
        ADs.setPremiseRule(premiseRule);

        List<DerivationRule> rules = new ArrayList<>();
        rules.add(premiseRule);
        rules.add(new ConditionalExploit("MP","^\\s*\\d+\\s*,\\s*\\d+\\s*MP\\s*$"));
        rules.add(new Abb_ADs("abv", "^\\s*\\d+\\s*abv\\s*$" ));
        ADs.setRules(rules);
        ADs.setRequirePremisesAtTop(false);

        return ADs;
    }

    private static DerivationRuleset getADq() {
        DerivationRuleset ADq = new DerivationRuleset("\ud835\udc34\ud835\udc37\ud835\udc5e", Collections.singletonList(ParseUtilities.newItalicText("ADq")));
        return ADq;
    }

    private static DerivationRuleset getAD() {
        DerivationRuleset AD = new DerivationRuleset("\ud835\udc34\ud835\udc37", Collections.singletonList(ParseUtilities.newItalicText("AD")));
        return AD;
    }



    private static DerivationRuleset getND_p() {
        DerivationRuleset ND_p = new DerivationRuleset("\ud835\udc41\ud835\udc37+", Collections.singletonList(ParseUtilities.newItalicText("ND+")));

        DerivationRule premiseRule = new Premise("P","^\\s*P\\s*$");
        ND_p.setPremiseRule(premiseRule);
        DerivationRule asspCondIntroRule = new AsspCondIntro("A","^\\s*A\\s*\\((g|\\ud835\\udc54),?\\s*\\u2192I\\)\\s*$");
        ND_p.setAsspCondIntroRule(asspCondIntroRule);
        DerivationRule asspBicondIntroRule = new AsspBicondIntro("A","^\\s*A\\s*\\((g|\\ud835\\udc54),?\\s*\\u2194I\\)\\s*$");
        ND_p.setAsspBicondIntroRule(asspBicondIntroRule);
        DerivationRule asspNegIntro = new AsspNegIntro("A","^\\s*A\\s*\\((c|\\ud835\\udc50),?\\s*\\u223cI\\)\\s*$");
        ND_p.setAsspNegIntroRule(asspNegIntro);
        DerivationRule asspNegExploit = new AsspNegExploit("A","^\\s*A\\s*\\((c|\\ud835\\udc50),?\\s*\\u223cE\\)\\s*$");
        ND_p.setAsspNegExploitRule(asspNegExploit);
        DerivationRule asspDisjExploitGRule = new AsspDisjExploitG("A","^\\s*A\\s*\\((g|\\ud835\\udc54),?\\s*\\d+\\s*\\u2228E\\)\\s*$");
        ND_p.setAsspDisjExploitGRule(asspDisjExploitGRule);
        DerivationRule asspDisjExploitCRule = new AsspDisjExploitC("A","^\\s*A\\s*\\((c|\\ud835\\udc50),?\\s*\\d+\\s*\\u2228E\\)\\s*$");
        ND_p.setAsspDisjExploitCRule(asspDisjExploitCRule);
        DerivationRule asspExisExploitCRule = new AsspExisExploitC("A","^\\s*A\\s*\\((c|\\ud835\\udc50),?\\s*\\d+\\s*\\u2203E\\)\\s*$");
        ND_p.setAsspExisExploitCRule(asspExisExploitCRule);
        DerivationRule asspExisExploitGRule = new AsspExisExploitG("A","^\\s*A\\s*\\((g|\\ud835\\udc54),?\\s*\\d+\\s*\\u2203E\\)\\s*$");
        ND_p.setAsspExisExploitGRule(asspExisExploitGRule);
        DerivationRule asspRestrictedUnivIntroRule = new AsspRestrictedUnivIntroRule("A","^\\s*A\\s*\\((g|\\ud835\\udc54),?\\s*\\(\\u2200I\\)\\)\\s*$");
        ND_p.setAsspRestrictedUnivIntroRule(asspRestrictedUnivIntroRule);

        DerivationRule asspRestrictedExisExploitCRule = new AsspRestrictedExisExploitC("A","^\\s*A\\s*\\((c|\\ud835\\udc50),?\\s*\\d+\\s*\\(\\u2203E\\)\\)\\s*$");
        ND_p.setAsspRestrictedExisExploitCRule(asspRestrictedExisExploitCRule);
        DerivationRule asspRestrictedExisExploitGRule = new AsspRestrictedExisExploitG("A","^\\s*A\\s*\\((g|\\ud835\\udc54),?\\s*\\d+\\s*\\(\\u2203E\\)\\)\\s*$");
        ND_p.setAsspRestrictedExisExploitGRule(asspRestrictedExisExploitGRule);


        List<DerivationRule> rules = new ArrayList<>();
        rules.add(premiseRule);
        rules.add(asspCondIntroRule);
        rules.add(asspBicondIntroRule);
        rules.add(asspNegIntro);
        rules.add(asspNegExploit);
        rules.add(asspDisjExploitGRule);
        rules.add(asspDisjExploitCRule);
        rules.add(asspExisExploitCRule);
        rules.add(asspExisExploitGRule);
        rules.add(asspRestrictedUnivIntroRule);
        rules.add(asspRestrictedExisExploitCRule);
        rules.add(asspRestrictedExisExploitGRule);


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

        rules.add(new UniversalExploit("\u2200E", "^\\s*\\d+\\s*\\u2200E\\s*$"));
        rules.add(new ExistentialIntro("\u2203I", "^\\s*\\d+\\s*\\u2203I\\s*$"));
        rules.add(new UniversalIntro("\u2200I", "^\\s*\\d+\\s*\\u2200I\\s*$"));
        rules.add(new ExistentialExploit("\u2203E", "^\\s*\\d+\\s*,\\s*\\d+\\s*-\\s*\\d+\\s*\\u2203E\\s*$"));
        rules.add(new EqualityIntro("\ue8acI", "^\\s*\\ue8acI\\s*$"));
        rules.add(new EqualityExploit("\ue8acE" ,"^\\s*\\d+\\s*,\\s*\\d+\\s*\\ue8acE\\s*$"));

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

        rules.add(new QuantifierSwitch("QS", "^\\s*\\d+\\s*QS\\s*$"));  //QS
        rules.add(new QuantifierDistribution("QD", "^\\s*\\d+\\s*QD\\s*$"));  //QD
        rules.add(new QuantifierNegation("QN", "^\\s*\\d+\\s*QN\\s*$"));
        rules.add(new RestrictedQuantifierNegation("RQN", "^\\s*\\d+\\s*RQN\\s*$"));
        rules.add(new QuantifierPlacement("QP", "^\\s*\\d+\\s*QP\\s*$"));
        rules.add(new RestrictedUnivExploit("(\u2200E)","^\\s*\\d+\\s*,\\s*\\d+\\s*\\(\\u2200E\\)\\s*$"));
        rules.add(new RestrictedExisIntro("(\u2203I)","^\\s*\\d+\\s*,\\s*\\d+\\s*\\(\\u2203I\\)\\s*$"));
        rules.add(new RestrictedUnivIntro("(\u2200I)", "^\\s*\\d+\\s*-\\s*\\d+\\s*\\(\\u2200I\\)\\s*$"));
        rules.add(new RestrictedExisExploit("(\u2203E)", "^\\s*\\d+\\s*,\\s*\\d+\\s*-\\s*\\d+\\s*\\(\\u2203E\\)\\s*$"));

        ND_p.setRules(rules);

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

        ND_p.setDummyRules(dummyRules);

        ND_p.setGenericAssumption(Pattern.compile("^\\s*A.*$"));
        ND_p.setRequirePremisesAtTop(true);

        return ND_p;
    }


    private static DerivationRuleset getND() {
        DerivationRuleset ND = new DerivationRuleset("\ud835\udc41\ud835\udc37", Collections.singletonList(ParseUtilities.newItalicText("ND")));

        DerivationRule premiseRule = new Premise("P","^\\s*P\\s*$");
        ND.setPremiseRule(premiseRule);
        DerivationRule asspCondIntroRule = new AsspCondIntro("A","^\\s*A\\s*\\((g|\\ud835\\udc54),?\\s*\\u2192I\\)\\s*$");
        ND.setAsspCondIntroRule(asspCondIntroRule);
        DerivationRule asspBicondIntroRule = new AsspBicondIntro("A","^\\s*A\\s*\\((g|\\ud835\\udc54),?\\s*\\u2194I\\)\\s*$");
        ND.setAsspBicondIntroRule(asspBicondIntroRule);
        DerivationRule asspNegIntro = new AsspNegIntro("A","^\\s*A\\s*\\((c|\\ud835\\udc50),?\\s*\\u223cI\\)\\s*$");
        ND.setAsspNegIntroRule(asspNegIntro);
        DerivationRule asspNegExploit = new AsspNegExploit("A","^\\s*A\\s*\\((c|\\ud835\\udc50),?\\s*\\u223cE\\)\\s*$");
        ND.setAsspNegExploitRule(asspNegExploit);
        DerivationRule asspDisjExploitGRule = new AsspDisjExploitG("A","^\\s*A\\s*\\((g|\\ud835\\udc54),?\\s*\\d+\\s*\\u2228E\\)\\s*$");
        ND.setAsspDisjExploitGRule(asspDisjExploitGRule);
        DerivationRule asspDisjExploitCRule = new AsspDisjExploitC("A","^\\s*A\\s*\\((c|\\ud835\\udc50),?\\s*\\d*\\s*\\u2228E\\)\\s*$");
        ND.setAsspDisjExploitCRule(asspDisjExploitCRule);
        DerivationRule asspExisExploitCRule = new AsspExisExploitC("A","^\\s*A\\s*\\((c|\\ud835\\udc50),?\\s*\\d*\\s*\\u2203E\\)\\s*$");
        ND.setAsspExisExploitCRule(asspExisExploitCRule);
        DerivationRule asspExisExploitGRule = new AsspExisExploitG("A","^\\s*A\\s*\\((g|\\ud835\\udc54),?\\s*\\d+\\s*\\u2203E\\)\\s*$");
        ND.setAsspExisExploitGRule(asspExisExploitGRule);


        List<DerivationRule> rules = new ArrayList<>();
        rules.add(premiseRule);
        rules.add(asspCondIntroRule);
        rules.add(asspBicondIntroRule);
        rules.add(asspNegIntro);
        rules.add(asspNegExploit);
        rules.add(asspDisjExploitGRule);
        rules.add(asspDisjExploitCRule);
        rules.add(asspExisExploitCRule);
        rules.add(asspExisExploitGRule);


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

        rules.add(new UniversalExploit("\u2200E", "^\\s*\\d+\\s*\\u2200E\\s*$"));
        rules.add(new ExistentialIntro("\u2203I", "^\\s*\\d+\\s*\\u2203I\\s*$"));
        rules.add(new UniversalIntro("\u2200I", "^\\s*\\d+\\s*\\u2200I\\s*$"));
        rules.add(new ExistentialExploit("\u2203E", "^\\s*\\d+\\s*,\\s*\\d+\\s*-\\s*\\d+\\s*\\u2203E\\s*$"));
        rules.add(new EqualityIntro("\ue8acI", "^\\s*\\ue8acI\\s*$"));
        rules.add(new EqualityExploit("\ue8acE" ,"^\\s*\\d+\\s*,\\s*\\d+\\s*\\ue8acE\\s*$"));


        ND.setRules(rules);

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

        ND.setDummyRules(dummyRules);

        ND.setGenericAssumption(Pattern.compile("^\\s*A.*$"));
        ND.setRequirePremisesAtTop(true);

        return ND;
    }


    private static DerivationRuleset getNDs() {
        DerivationRuleset NDs = new DerivationRuleset("\ud835\udc41\ud835\udc37\ud835\udc60", Collections.singletonList(ParseUtilities.newItalicText("NDs")));

        DerivationRule premiseRule = new Premise("P","^\\s*P\\s*$");
        NDs.setPremiseRule(premiseRule);
        DerivationRule asspCondIntroRule = new AsspCondIntro("A","^\\s*A\\s*\\((g|\\ud835\\udc54),?\\s*\\u2192I\\)\\s*$");
        NDs.setAsspCondIntroRule(asspCondIntroRule);
        DerivationRule asspBicondIntroRule = new AsspBicondIntro("A","^\\s*A\\s*\\((g|\\ud835\\udc54),?\\s*\\u2194I\\)\\s*$");
        NDs.setAsspBicondIntroRule(asspBicondIntroRule);
        DerivationRule asspNegIntro = new AsspNegIntro("A","^\\s*A\\s*\\((c|\\ud835\\udc50),?\\s*\\u223cI\\)\\s*$");
        NDs.setAsspNegIntroRule(asspNegIntro);
        DerivationRule asspNegExploit = new AsspNegExploit("A","^\\s*A\\s*\\((c|\\ud835\\udc50),?\\s*\\u223cE\\)\\s*$");
        NDs.setAsspNegExploitRule(asspNegExploit);
        DerivationRule asspDisjExploitGRule = new AsspDisjExploitG("A","^\\s*A\\s*\\((g|\\ud835\\udc54),?\\s*\\d+\\s*\\u2228E\\)\\s*$");
        NDs.setAsspDisjExploitGRule(asspDisjExploitGRule);
        DerivationRule asspDisjExploitCRule = new AsspDisjExploitC("A","^\\s*A\\s*\\((c|\\ud835\\udc50),?\\s*\\d*\\s*\\u2228E\\)\\s*$");
        NDs.setAsspDisjExploitCRule(asspDisjExploitCRule);

        List<DerivationRule> rules = new ArrayList<>();
        rules.add(premiseRule);
        rules.add(asspCondIntroRule);
        rules.add(asspBicondIntroRule);
        rules.add(asspNegIntro);
        rules.add(asspNegExploit);
        rules.add(asspDisjExploitGRule);
        rules.add(asspDisjExploitCRule);

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
        DerivationRuleset NDs_p = new DerivationRuleset("\ud835\udc41\ud835\udc37\ud835\udc60+", Collections.singletonList(ParseUtilities.newItalicText("NDs+")));

        DerivationRule premiseRule = new Premise("P","^\\s*P\\s*$");
        NDs_p.setPremiseRule(premiseRule);
        DerivationRule asspCondIntroRule = new AsspCondIntro("A","^\\s*A\\s*\\((g|\\ud835\\udc54),?\\s*\\u2192I\\)\\s*$");
        NDs_p.setAsspCondIntroRule(asspCondIntroRule);
        DerivationRule asspBicondIntroRule = new AsspBicondIntro("A","^\\s*A\\s*\\((g|\\ud835\\udc54),?\\s*\\u2194I\\)\\s*$");
        NDs_p.setAsspBicondIntroRule(asspBicondIntroRule);
        DerivationRule asspNegIntro = new AsspNegIntro("A","^\\s*A\\s*\\((c|\\ud835\\udc50),?\\s*\\u223cI\\)\\s*$");
        NDs_p.setAsspNegIntroRule(asspNegIntro);
        DerivationRule asspNegExploit = new AsspNegExploit("A","^\\s*A\\s*\\((c|\\ud835\\udc50),?\\s*\\u223cE\\)\\s*$");
        NDs_p.setAsspNegExploitRule(asspNegExploit);
        DerivationRule asspDisjExploitGRule = new AsspDisjExploitG("A","^\\s*A\\s*\\((g|\\ud835\\udc54),?\\s*\\d+\\s*\\u2228E\\)\\s*$");
        NDs_p.setAsspDisjExploitGRule(asspDisjExploitGRule);
        DerivationRule asspDisjExploitCRule = new AsspDisjExploitC("A","^\\s*A\\s*\\((c|\\ud835\\udc50),?\\s*\\d*\\s*\\u2228E\\)\\s*$");
        NDs_p.setAsspDisjExploitCRule(asspDisjExploitCRule);

        List<DerivationRule> rules = new ArrayList<>();
        rules.add(premiseRule);
        rules.add(asspCondIntroRule);
        rules.add(asspBicondIntroRule);
        rules.add(asspNegIntro);
        rules.add(asspNegExploit);
        rules.add(asspDisjExploitGRule);
        rules.add(asspDisjExploitCRule);
    //    rules.add(new AsspCondIntro("A","^\\s*A\\s*\\((g|\\ud835\\udc54),?\\s*\\u2192I\\)\\s*$"));   //assp arrow I
   //     rules.add(new AsspBicondIntro("A","^\\s*A\\s*\\((g|\\ud835\\udc54),?\\s*\\u2194I\\)\\s*$")); //assp double arrow I
   //     rules.add(new AsspNegIntro("A","^\\s*A\\s*\\((c|\\ud835\\udc50),?\\s*\\u223cI\\)\\s*$"));  //assp tilde I
   //     rules.add(new AsspNegExploit("A","^\\s*A\\s*\\((c|\\ud835\\udc50),?\\s*\\u223cE\\)\\s*$"));  //assp tilde E
   //     rules.add(new AsspDisjExploitG("A","^\\s*A\\s*\\((g|\\ud835\\udc54),?\\s*\\d+\\s*\\u2228E\\)\\s*$"));  //assp disj E for goal
   //     rules.add(new AsspDisjExploitC("A","^\\s*A\\s*\\((c|\\ud835\\udc50),?\\s*\\d*\\s*\\u2228E\\)\\s*$"));  //assp disj E for contradiction

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

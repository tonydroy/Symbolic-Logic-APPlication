package slapp.editor.derivation.der_systems;

import javafx.scene.text.Text;
import javafx.util.Pair;
import slapp.editor.parser.ParseUtilities;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

public class DerivationRulesets implements Serializable {
    private static final long serialVersionUID = 100L;
    private static List<DerivationRuleset> rulesets = new ArrayList<>();

    static {
        loadRulesets();
    }

    public DerivationRulesets() {}

    private static void loadRulesets() {
        rulesets.add(getNP134());
        rulesets.add(getNP1234());
        rulesets.add(getNDs());
        rulesets.add(getNDs_p());
        rulesets.add(getND());
        rulesets.add(getND_p());
        rulesets.add(getND_pnt());
        rulesets.add(getAP());
        rulesets.add(getADs());
        rulesets.add(getADq());
        rulesets.add(getAD());
        rulesets.add(getAstar());
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

    public static DerivationRuleset getNP134() {
        DerivationRuleset NP = new DerivationRuleset("\uD835\uDC41\uD835\uDC43 (134)", Collections.singletonList(ParseUtilities.newItalicText("NP 134")));
        DerivationRule premiseRule = new Premise("P","^\\s*P\\s*$");
        NP.setPremiseRule(premiseRule);
        NP.setGenericAssumption(Pattern.compile("^\\s*A.*$"));

        List<Pair<Pattern, String>> dummyRules = new ArrayList<>();
        dummyRules.add(new Pair(Pattern.compile("^.*R\\d+\\s*$"), "Insert dot prior to rule number (as 'R.1')."));
        NP.setDummyRules(dummyRules);

        List<DerivationRule> rules = new ArrayList<>();
        rules.add(premiseRule);
        rules.add(new NPRule1("R.1", "^\\s*\\d+\\s*,\\s*\\d+\\s*R\\.1\\s*$"));
        rules.add(new NPRule3("R.3", "\\s*\\d+\\s*R\\.3\\s*$"));
        rules.add(new NPRule4("R.4", "\\s*\\d+\\s*R\\.4\\s*$"));
        NP.setRules(rules);

        NP.setRequirePremisesAtTop(true);
        NP.setPermitSubderivations(false);


        return NP;
    }

    public static DerivationRuleset getNP1234() {
        DerivationRuleset NP = new DerivationRuleset("\uD835\uDC41\uD835\uDC43 (1234)", Collections.singletonList(ParseUtilities.newItalicText("NP 1234")));
        DerivationRule premiseRule = new Premise("P","^\\s*P\\s*$");
        NP.setPremiseRule(premiseRule);
        NP.setGenericAssumption(Pattern.compile("^\\s*A.*$"));

        List<Pair<Pattern, String>> dummyRules = new ArrayList<>();
        dummyRules.add(new Pair(Pattern.compile(".*R\\d+\\s*$"), "Insert dot prior to rule number (as 'R.1')."));
        NP.setDummyRules(dummyRules);

        List<DerivationRule> rules = new ArrayList<>();
        rules.add(premiseRule);
        rules.add(new NPRule1("R.1", "^\\s*\\d+\\s*,\\s*\\d+\\s*R\\.1\\s*$"));
        rules.add(new NPRule2("R.2", "\\s*\\d+\\s*R\\.2\\s*$"));
        rules.add(new NPRule3("R.3", "\\s*\\d+\\s*R\\.3\\s*$"));
        rules.add(new NPRule4("R.4", "\\s*\\d+\\s*R\\.4\\s*$"));
        NP.setRules(rules);

        NP.setRequirePremisesAtTop(true);
        NP.setPermitSubderivations(false);


        return NP;
    }

    public static DerivationRuleset getAP() {
        DerivationRuleset AP = new DerivationRuleset("\uD835\uDC34\uD835\uDC43", Collections.singletonList(ParseUtilities.newItalicText("AP")));
        DerivationRule premiseRule = new Premise("prem","^\\s*prem\\s*$");
        AP.setPremiseRule(premiseRule);
        AP.setGenericAssumption(Pattern.compile("^\\s*assp.*$"));

        List<Pair<Pattern, String>> dummyRules = new ArrayList<>();
        dummyRules.add(new Pair(Pattern.compile("^\\s*∧\\d+\\s*$"), "Insert dot prior to axiom number (as '∧.1')."));
        AP.setDummyRules(dummyRules);

        List<DerivationRule> rules = new ArrayList<>();
        rules.add(premiseRule);
        rules.add(new ConditionalExploit("MP","^\\s*\\d+\\s*,\\s*\\d+\\s*MP\\s*$"));
        AP.setRules(rules);

        AP.setRequirePremisesAtTop(false);
        AP.setPermitSubderivations(false);
        return AP;
    }


    public static DerivationRuleset getAstar() {
        DerivationRuleset Astar = new DerivationRuleset("\uD835\uDC34*", Collections.singletonList(ParseUtilities.newItalicText("A*")));
        DerivationRule premiseRule = new Premise("prem","^\\s*prem\\s*$");
        Astar.setPremiseRule(premiseRule);
        Astar.setGenericAssumption(Pattern.compile("^\\s*assp.*$"));

        List<Pair<Pattern, String>> dummyRules = new ArrayList<>();
        dummyRules.add(new Pair(Pattern.compile("^[^\\(]*[a-z][^\\)]*$"), "Appeal to \uD835\uDC34* theorem by letter in parentheses (as '(a)')."));
        dummyRules.add(new Pair(Pattern.compile("^\\s*A\\d+\\s*$"), "Insert dot prior to axiom number (as 'A.1')."));
        Astar.setDummyRules(dummyRules);

        List<DerivationRule> rules = new ArrayList<>();
        rules.add(premiseRule);
        rules.add(new AstarMP("MP", "^\\s*\\d+\\s*,\\s*\\d+\\s*MP\\s*$"));
        rules.add(new Abb_Astar("abv", "^\\s*\\d+\\s*abv\\s*$" ));
        Astar.setRules(rules);

        Astar.setRequirePremisesAtTop(false);
        Astar.setPermitSubderivations(false);

        return Astar;
    }

    private static DerivationRuleset getADs() {
        DerivationRuleset ADs = new DerivationRuleset("\ud835\udc34\ud835\udc37\ud835\udc60", Collections.singletonList(ParseUtilities.newItalicText("ADs")));

        DerivationRule premiseRule = new Premise("prem","^\\s*prem\\s*$");
        ADs.setPremiseRule(premiseRule);
        ADs.setGenericAssumption(Pattern.compile("^\\s*assp.*$"));

        List<Pair<Pattern, String>> dummyRules = new ArrayList<>();
        dummyRules.add(new Pair(Pattern.compile("^\\s*A\\d+\\s*$"), "Insert dot prior to axiom number (as 'A.1')."));
        dummyRules.add(new Pair(Pattern.compile("^\\s*P\\s*$"), "Justification for \ud835\udc34\ud835\udc37\ud835\udc60 premise is 'prem'."));
        ADs.setDummyRules(dummyRules);

        List<DerivationRule> rules = new ArrayList<>();
        rules.add(premiseRule);
        rules.add(new ConditionalExploit("MP","^\\s*\\d+\\s*,\\s*\\d+\\s*MP\\s*$"));
        rules.add(new Abb_ADs("abv", "^\\s*\\d+\\s*abv\\s*$" ));
        ADs.setRules(rules);

        ADs.setRequirePremisesAtTop(false);
        ADs.setPermitSubderivations(false);

        return ADs;
    }

    private static DerivationRuleset getADq() {
        DerivationRuleset ADq = new DerivationRuleset("\ud835\udc34\ud835\udc37\ud835\udc5e", Collections.singletonList(ParseUtilities.newItalicText("ADq")));
        DerivationRule premiseRule = new Premise("prem","^\\s*prem\\s*$");
        ADq.setPremiseRule(premiseRule);
        ADq.setGenericAssumption(Pattern.compile("^\\s*assp.*$"));

        List<Pair<Pattern, String>> dummyRules = new ArrayList<>();
        dummyRules.add(new Pair(Pattern.compile("^\\s*A\\d+\\s*$"), "Insert dot prior to axiom number (as 'A.1')."));
        dummyRules.add(new Pair(Pattern.compile("^\\s*P\\s*$"), "Justification for \ud835\udc34\ud835\udc37\ud835\udc5e premise is 'prem'."));
        ADq.setDummyRules(dummyRules);


        List<DerivationRule> rules = new ArrayList<>();
        rules.add(new ADqGen("Gen", "^\\s*\\d+\\s*Gen\\s*$"));
        rules.add(premiseRule);
        rules.add(new ConditionalExploit("MP","^\\s*\\d+\\s*,\\s*\\d+\\s*MP\\s*$"));

        rules.add(new Abb_ADq("abv", "^\\s*\\d+\\s*abv\\s*$" ));
        ADq.setRules(rules);

        ADq.setRequirePremisesAtTop(false);
        ADq.setPermitSubderivations(false);


        ADq.setRules(rules);

        return ADq;
    }



    private static DerivationRuleset getAD() {
        DerivationRuleset AD = new DerivationRuleset("\ud835\udc34\ud835\udc37", Collections.singletonList(ParseUtilities.newItalicText("AD")));

        DerivationRule premiseRule = new Premise("prem","^\\s*prem\\s*$");
        AD.setPremiseRule(premiseRule);
        AD.setGenericAssumption(Pattern.compile("^\\s*assp.*$"));

        List<Pair<Pattern, String>> dummyRules = new ArrayList<>();
        dummyRules.add(new Pair(Pattern.compile("^\\s*A\\d+\\s*$"), "Insert dot prior to axiom number (as 'A.1')."));
        dummyRules.add(new Pair(Pattern.compile("^\\s*P\\s*$"), "Justification for \ud835\udc34\ud835\udc37\ud835\udc5e premise is 'prem'."));
        AD.setDummyRules(dummyRules);


        List<DerivationRule> rules = new ArrayList<>();
        rules.add(new ADqGen("Gen", "^\\s*\\d+\\s*Gen\\s*$"));
        rules.add(premiseRule);
        rules.add(new ConditionalExploit("MP","^\\s*\\d+\\s*,\\s*\\d+\\s*MP\\s*$"));

        rules.add(new Abb_ADq("abv", "^\\s*\\d+\\s*abv\\s*$" ));
        AD.setRules(rules);

        AD.setRequirePremisesAtTop(false);
        AD.setPermitSubderivations(false);

        AD.setRules(rules);
        return AD;
    }


    private static DerivationRuleset getND_pnt() {
        List<Text> nameTxts = new ArrayList<>();
        nameTxts.add(ParseUtilities.newItalicText("ND+"));
        nameTxts.add(ParseUtilities.newRegularText(" (nt)"));
        DerivationRuleset ND_pnt = new DerivationRuleset("\ud835\udc41\ud835\udc37+ (nt)",nameTxts);

        DerivationRule premiseRule = new Premise("P","^\\s*P\\s*$");
        ND_pnt.setPremiseRule(premiseRule);
        DerivationRule asspCondIntroRule = new AsspCondIntro("A","^\\s*A\\s*\\((g|\\ud835\\udc54),?\\s*\\u2192I\\)\\s*$");
        ND_pnt.setAsspCondIntroRule(asspCondIntroRule);
        DerivationRule asspBicondIntroRule = new AsspBicondIntro("A","^\\s*A\\s*\\((g|\\ud835\\udc54),?\\s*\\u2194I\\)\\s*$");
        ND_pnt.setAsspBicondIntroRule(asspBicondIntroRule);
        DerivationRule asspNegIntro = new AsspNegIntro("A","^\\s*A\\s*\\((c|\\ud835\\udc50),?\\s*\\u223cI\\)\\s*$");
        ND_pnt.setAsspNegIntroRule(asspNegIntro);
        DerivationRule asspNegExploit = new AsspNegExploit("A","^\\s*A\\s*\\((c|\\ud835\\udc50),?\\s*\\u223cE\\)\\s*$");
        ND_pnt.setAsspNegExploitRule(asspNegExploit);
        DerivationRule asspDisjExploitGRule = new AsspDisjExploitG("A","^\\s*A\\s*\\((g|\\ud835\\udc54),?\\s*\\d+\\s*\\u2228E\\)\\s*$");
        ND_pnt.setAsspDisjExploitGRule(asspDisjExploitGRule);
        DerivationRule asspDisjExploitCRule = new AsspDisjExploitC("A","^\\s*A\\s*\\((c|\\ud835\\udc50),?\\s*\\d+\\s*\\u2228E\\)\\s*$");
        ND_pnt.setAsspDisjExploitCRule(asspDisjExploitCRule);
        DerivationRule asspExisExploitCRule = new AsspExisExploitC("A","^\\s*A\\s*\\((c|\\ud835\\udc50),?\\s*\\d+\\s*\\u2203E\\)\\s*$");
        ND_pnt.setAsspExisExploitCRule(asspExisExploitCRule);
        DerivationRule asspExisExploitGRule = new AsspExisExploitG("A","^\\s*A\\s*\\((g|\\ud835\\udc54),?\\s*\\d+\\s*\\u2203E\\)\\s*$");
        ND_pnt.setAsspExisExploitGRule(asspExisExploitGRule);
        DerivationRule asspRestrictedUnivIntroRule = new AsspRestrictedUnivIntroRule("A","^\\s*A\\s*\\((g|\\ud835\\udc54),?\\s*\\(\\u2200I\\)\\)\\s*$");
        ND_pnt.setAsspRestrictedUnivIntroRule(asspRestrictedUnivIntroRule);

        DerivationRule asspRestrictedExisExploitCRule = new AsspRestrictedExisExploitC("A","^\\s*A\\s*\\((c|\\ud835\\udc50),?\\s*\\d+\\s*\\(\\u2203E\\)\\)\\s*$");
        ND_pnt.setAsspRestrictedExisExploitCRule(asspRestrictedExisExploitCRule);
        DerivationRule asspRestrictedExisExploitGRule = new AsspRestrictedExisExploitG("A","^\\s*A\\s*\\((g|\\ud835\\udc54),?\\s*\\d+\\s*\\(\\u2203E\\)\\)\\s*$");
        ND_pnt.setAsspRestrictedExisExploitGRule(asspRestrictedExisExploitGRule);

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

        rules.add(new SymmetryRepRule("Sym", "^\\s*\\d+\\s*Sym\\s*$"));
        rules.add(new QuantifierSwitch("QS", "^\\s*\\d+\\s*QS\\s*$"));  //QS
        rules.add(new QuantifierDistribution("QD", "^\\s*\\d+\\s*QD\\s*$"));  //QD
        rules.add(new QuantifierNegation("QN", "^\\s*\\d+\\s*QN\\s*$"));
        rules.add(new RestrictedQuantifierNegation("RQN", "^\\s*\\d+\\s*RQN\\s*$"));
        rules.add(new QuantifierPlacement("QP", "^\\s*\\d+\\s*QP\\s*$"));
        rules.add(new RestrictedUnivExploit("(\u2200E)","^\\s*\\d+\\s*,\\s*\\d+\\s*\\(\\u2200E\\)\\s*$"));
        rules.add(new RestrictedExisIntro("(\u2203I)","^\\s*\\d+\\s*,\\s*\\d+\\s*\\(\\u2203I\\)\\s*$"));
        rules.add(new RestrictedUnivIntro("(\u2200I)", "^\\s*\\d+\\s*-\\s*\\d+\\s*\\(\\u2200I\\)\\s*$"));
        rules.add(new RestrictedExisExploit("(\u2203E)", "^\\s*\\d+\\s*,\\s*\\d+\\s*-\\s*\\d+\\s*\\(\\u2203E\\)\\s*$"));

        rules.add(new Abb_NDpnt("abv", "^\\s*\\d+\\s*abv\\s*$" ));

        ND_pnt.setRules(rules);

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

        ND_pnt.setDummyRules(dummyRules);

        ND_pnt.setGenericAssumption(Pattern.compile("^\\s*A.*$"));
        ND_pnt.setRequirePremisesAtTop(true);

        return ND_pnt;
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

        rules.add(new SymmetryRepRule("Sym", "^\\s*\\d+\\s*Sym\\s*$"));
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

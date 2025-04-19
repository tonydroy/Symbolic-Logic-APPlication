package slapp.editor.derivation.DerSystems;

import javafx.scene.text.Text;
import javafx.util.Pair;
import slapp.editor.parser.Language;

import java.util.List;
import java.util.regex.Pattern;

public class DerivationRuleset {
    String name;
    List<Text> textName;
    Language objectLanguage;
    Language metaLanguage;
    List<DerivationRule> rules;
    DerivationRule premiseRule;
    DerivationRule asspBicondIntroRule;
    DerivationRule asspCondIntroRule;
    DerivationRule asspDisjExploitCRule;
    DerivationRule asspDisjExploitGRule;
    DerivationRule asspNegExploitRule;
    DerivationRule asspNegIntroRule;
    DerivationRule asspExisExploitCRule;
    DerivationRule asspExisExploitGRule;
    DerivationRule asspRestrictedUnivIntroRule;
    DerivationRule asspRestrictedExisExploitCRule;
    DerivationRule asspRestrictedExisExploitGRule;



    Pattern genericAssumption;
    boolean requirePremisesAtTop = true;

    List<Pair<Pattern, String>> dummyRules;

    DerivationRuleset(String name, List<Text> textName) {
        this.name = name;
        this.textName = textName;
        this.objectLanguage = objectLanguage;
        this.metaLanguage = metaLanguage;
    }

    public List<DerivationRule> getRules() {
        return rules;
    }

    public void setRules(List<DerivationRule> rules) {
        this.rules = rules;
    }

    public Language getMetaLanguage() {
        return metaLanguage;
    }

    public String getName() {
        return name;
    }

    public List<Text> getTextName() {
        return textName;
    }

    public Language getObjectLanguage() {
        return objectLanguage;
    }

    public DerivationRule getPremiseRule() {
        return premiseRule;
    }

    public void setPremiseRule(DerivationRule premiseRule) {
        this.premiseRule = premiseRule;
    }

    public DerivationRule getAsspCondIntroRule() {
        return asspCondIntroRule;
    }

    public void setAsspCondIntroRule(DerivationRule asspCondIntroRule) {
        this.asspCondIntroRule = asspCondIntroRule;
    }

    public DerivationRule getAsspBicondIntroRule() {
        return asspBicondIntroRule;
    }

    public void setAsspBicondIntroRule(DerivationRule asspBicondIntroRule) {
        this.asspBicondIntroRule = asspBicondIntroRule;
    }

    public DerivationRule getAsspDisjExploitCRule() {
        return asspDisjExploitCRule;
    }

    public void setAsspDisjExploitCRule(DerivationRule asspDisjExploitCRule) {
        this.asspDisjExploitCRule = asspDisjExploitCRule;
    }

    public DerivationRule getAsspDisjExploitGRule() {
        return asspDisjExploitGRule;
    }

    public void setAsspDisjExploitGRule(DerivationRule asspDisjExploitGRule) {
        this.asspDisjExploitGRule = asspDisjExploitGRule;
    }

    public DerivationRule getAsspNegExploitRule() {
        return asspNegExploitRule;
    }

    public void setAsspNegExploitRule(DerivationRule asspNegExploitRule) {
        this.asspNegExploitRule = asspNegExploitRule;
    }

    public DerivationRule getAsspExisExploitCRule() {
        return asspExisExploitCRule;
    }

    public void setAsspExisExploitCRule(DerivationRule asspExisExploitCRule) {
        this.asspExisExploitCRule = asspExisExploitCRule;
    }

    public DerivationRule getAsspExisExploitGRule() {
        return asspExisExploitGRule;
    }

    public void setAsspExisExploitGRule(DerivationRule asspExisExploitGRule) {
        this.asspExisExploitGRule = asspExisExploitGRule;
    }

    public DerivationRule getAsspNegIntroRule() {
        return asspNegIntroRule;
    }

    public void setAsspNegIntroRule(DerivationRule asspNegIntroRule) {
        this.asspNegIntroRule = asspNegIntroRule;
    }

    public DerivationRule getAsspRestrictedUnivIntroRule() {
        return asspRestrictedUnivIntroRule;
    }

    public void setAsspRestrictedUnivIntroRule(DerivationRule asspRestrictedUnivIntroRule) {
        this.asspRestrictedUnivIntroRule = asspRestrictedUnivIntroRule;
    }

    public DerivationRule getAsspRestrictedExisExploitCRule() {
        return asspRestrictedExisExploitCRule;
    }

    public void setAsspRestrictedExisExploitCRule(DerivationRule asspRestrictedExisExploitCRule) {
        this.asspRestrictedExisExploitCRule = asspRestrictedExisExploitCRule;
    }

    public DerivationRule getAsspRestrictedExisExploitGRule() {
        return asspRestrictedExisExploitGRule;
    }

    public void setAsspRestrictedExisExploitGRule(DerivationRule asspRestrictedExisExploitGRule) {
        this.asspRestrictedExisExploitGRule = asspRestrictedExisExploitGRule;
    }

    public List<Pair<Pattern, String>> getDummyRules() {
        return dummyRules;
    }

    public void setDummyRules(List<Pair<Pattern, String>> dummyRules) {
        this.dummyRules = dummyRules;
    }

    public Pattern getGenericAssumption() {
        return genericAssumption;
    }

    public void setGenericAssumption(Pattern genericAssumption) {
        this.genericAssumption = genericAssumption;
    }

    public boolean isRequirePremisesAtTop() {
        return requirePremisesAtTop;
    }

    public void setRequirePremisesAtTop(boolean requirePremisesAtTop) {
        this.requirePremisesAtTop = requirePremisesAtTop;
    }

    public void setObjectLanguage(Language objectLanguage) {
        this.objectLanguage = objectLanguage;
    }

    public void setMetaLanguage(Language metaLanguage) {
        this.metaLanguage = metaLanguage;
    }
}



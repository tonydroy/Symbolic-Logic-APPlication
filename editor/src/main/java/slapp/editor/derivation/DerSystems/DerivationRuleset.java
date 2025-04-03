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
    Pattern genericAssumption;
    boolean requirePremisesAtTop = true;

    List<Pair<Pattern, String>> dummyRules;

    DerivationRuleset(String name, List<Text> textName, Language objectLanguage, Language metaLanguage) {
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
}



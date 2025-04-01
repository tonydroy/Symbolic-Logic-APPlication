package slapp.editor.derivation.DerSystems;

import javafx.scene.text.Text;
import slapp.editor.parser.Language;

import java.util.List;

public class DerivationRuleset {
    String name;
    List<Text> textName;
    Language language;
    List<DerivationRule> rules;

    DerivationRuleset(String name, List<Text> textName, Language language) {
        this.name = name;
        this.textName = textName;
        this.language = language;
    }

    public List<DerivationRule> getRules() {
        return rules;
    }

    public void setRules(List<DerivationRule> rules) {
        this.rules = rules;
    }

    public String getName() {
        return name;
    }

    public List<Text> getTextName() {
        return textName;
    }

    public Language getLanguage() {
        return language;
    }
}



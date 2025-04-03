package slapp.editor.derivation.DerSystems;

import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.util.Pair;
import slapp.editor.derivation.DerivationCheck;
import slapp.editor.derivation.LineType;
import slapp.editor.derivation.ViewLine;
import slapp.editor.parser.Language;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;

public class Premise extends DerivationRule {

    public Premise(String name, String rgexTemplate) {
        super(name, rgexTemplate);
        this.premAssp = true;
    }

    public Pair<Boolean, List<Text>> applies(DerivationCheck checker, ViewLine line, String... inputs) {
        if (checker.getDerivationRuleset().isRequirePremisesAtTop()) {
            List<ViewLine> viewLines = checker.getViewLines();
            int index = viewLines.indexOf(line);
            for (int i = 0; i < index; i++) {
                ViewLine vline = viewLines.get(i);
                if (LineType.isContentLine(vline.getLineType())) {
                    TextFlow justificationFlow = vline.getJustificationFlow();
                    String justificationString = checker.getDerivationExercise().getStringFromJustificationFlow(justificationFlow);
                    if (!checker.getDerivationRuleset().getPremiseRule().matches(justificationString)) {
                        List<Text> texts = new ArrayList<>();
                        texts.add(new Text("In "));
                        texts.addAll(checker.getDerivationRuleset().getTextName());
                        texts.add(new Text(" premises must all appear at the top."));
                        return new Pair(false, texts);
                    }
                }
            }
        }
        return new Pair(true, null);
    }


}

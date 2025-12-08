package slapp.editor.derivation.der_systems;

import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.util.Pair;
import slapp.editor.derivation.*;
import slapp.editor.derivation_explain.DrvtnExpExercise;

import java.util.ArrayList;
import java.util.List;

public class Premise extends DerivationRule {

    public Premise(String name, String rgexTemplate) {
        super(name, rgexTemplate);
        this.premAssp = true;
    }

    public Pair<Boolean, List<Text>> applies(DCheck checker, ViewLine line, String... inputs) {
        if (checker.getDerivationRuleset().isRequirePremisesAtTop()) {
            List<ViewLine> viewLines = checker.getViewLines();
            int index = viewLines.indexOf(line);
            for (int i = 0; i < index; i++) {
                ViewLine vline = viewLines.get(i);
                if (LineType.isContentLine(vline.getLineType())) {
                    TextFlow justificationFlow = vline.getJustificationFlow();

                    String justificationString = (checker instanceof DerivationCheck) ? ((DerivationExercise) checker.getExercise()).getStringFromJustificationFlow(justificationFlow) :
                            ((DrvtnExpExercise) checker.getExercise()).getStringFromJustificationFlow(justificationFlow);

          //          String justificationString = checker.getDerivationExercise().getStringFromJustificationFlow(justificationFlow);
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

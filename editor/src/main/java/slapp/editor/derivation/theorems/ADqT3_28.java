package slapp.editor.derivation.theorems;

import com.gluonhq.richtextarea.RichTextArea;
import com.gluonhq.richtextarea.model.Document;
import javafx.event.ActionEvent;
import javafx.scene.text.Text;
import javafx.util.Pair;
import slapp.editor.decorated_rta.BoxedDRTA;
import slapp.editor.derivation.DerivationCheck;
import slapp.editor.derivation.ViewLine;
import slapp.editor.parser.*;
import slapp.editor.parser.grammatical_parts.Formula;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;


//need this (instead of zero input) to get free for message.

public class ADqT3_28 extends Theorem {
    List<Text> freeVariableFailure = new ArrayList<>();
    Language objectLanguage;

    public ADqT3_28(String name, String... forms) {
        super(name, forms);
        String regName = name.replace(".", "\\.");
        regName = regName.replace("(", "\\(").replace(")", "\\)");
        String regexString = "^\\s*" + regName + "\\s*$";
        Pattern pattern = Pattern.compile(regexString);
        setMatcher(pattern.matcher(""));
    }

    public Pair<Boolean, List<Text>> applies(DerivationCheck checker, ViewLine line, String... inputs) {

        BoxedDRTA lineBDRTA = line.getLineContentBoxedDRTA();
        RichTextArea lineRTA = lineBDRTA.getRTA();
        lineRTA.getActionFactory().saveNow().execute(new ActionEvent());
        Document lineDoc = lineRTA.getDocument();

        objectLanguage = checker.getDerivationRuleset().getObjectLanguage();
        Language metaLanguage = checker.getDerivationRuleset().getMetaLanguage();

        String form = "(∀\uD835\uDCCD\uD835\uDCAB → ∀\uD835\uDCCB\uD835\uDCAB‹\uD835\uDCCD, \uD835\uDCCB›)";


        boolean resultGood = false;
        MatchUtilities.clearFormMatch();
        try {
            Pair<Boolean, Boolean> match = MatchUtilities.simpleFormMatch(new Document(form), lineDoc, objectLanguage.getNameString(), metaLanguage.getNameString());
            if (match.getKey()) resultGood = true;
        }
        catch (TextMessageException e) {
            return new Pair(false, e.getMessageList());
        }




        //check variable free in universal
        Expression variableExp = ((Formula) MatchUtilities.getTransformList().get(0).getKey()).getSubTransform().getExp2().getMatch();
  //      System.out.println(variableExp);

        List<Expression> immediateSubs = SyntacticalFns.immediateSubformulas(lineDoc, objectLanguage.getNameString());

        Expression antecedent = immediateSubs.get(0);
        if (SyntacticalFns.expTermFreeInFormula(antecedent, variableExp, objectLanguage.getNameString())) {
            List<Text> list = new ArrayList<>();
            list.add(new Text("Variable "));
            list.addAll(variableExp.toTextList());
            list.add(new Text(" is free in "));
            list.addAll(antecedent.toTextList());
            return new Pair(false, list);
        }

        if (resultGood) return new Pair(true, null);
        else {
            return new Pair(false, Collections.singletonList(ParseUtilities.newRegularText("Line (" + line.getLineNumberLabel().getText() + ") is not an instance of " + getName() + ".")));
        }

    }


}

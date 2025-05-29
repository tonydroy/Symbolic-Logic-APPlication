package slapp.editor.derivation.theorems;

import com.gluonhq.richtextarea.RichTextArea;
import com.gluonhq.richtextarea.model.Document;
import javafx.event.ActionEvent;
import javafx.scene.text.Text;
import javafx.util.Pair;
import slapp.editor.decorated_rta.BoxedDRTA;
import slapp.editor.derivation.DCheck;
import slapp.editor.derivation.ViewLine;
import slapp.editor.parser.*;
import slapp.editor.parser.grammatical_parts.MFormula;
import slapp.editor.parser.grammatical_parts.Term;
import slapp.editor.parser.grammatical_parts.TermType;
import slapp.editor.parser.symbols.MVariable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

public class ADqT3_29 extends Theorem {
    Language objectLanguage;
    List<Text> freeVariableFailure;

    public ADqT3_29(String name, String... forms) {
        super(name, forms);
        String regName = name.replace(".", "\\.");
        regName = regName.replace("(", "\\(").replace(")", "\\)");
        String regexString = "^\\s*\\d+\\s*" + regName + "\\s*$";
        Pattern pattern = Pattern.compile(regexString);
        setMatcher(pattern.matcher(""));
    }

    public Pair<Boolean, List<Text>> applies(DCheck checker, ViewLine line, String... inputs) {
        BoxedDRTA lineBDRTA = line.getLineContentBoxedDRTA();
        RichTextArea lineRTA = lineBDRTA.getRTA();
        lineRTA.getActionFactory().saveNow().execute(new ActionEvent());
        Document lineDoc = lineRTA.getDocument();

        ViewLine inputLine;
        Pair<ViewLine, List<Text>> inputLine1Pair = checker.getLineFromLabel(inputs[0]);
        if (inputLine1Pair.getKey() != null) inputLine = inputLine1Pair.getKey();
        else return new Pair(false, inputLine1Pair.getValue());

        BoxedDRTA inputBDRTA = inputLine.getLineContentBoxedDRTA();
        RichTextArea inputRTA = inputBDRTA.getRTA();
        inputRTA.getActionFactory().saveNow().execute(new ActionEvent());
        Document inputDoc = inputRTA.getDocument();
        if (inputDoc.getText().equals("")) return new Pair(false, Collections.singletonList(ParseUtilities.newRegularText("Justification cannot appeal to the empty line (" + inputs[0] + ").")));

        Pair<Boolean, List<Text>> accessibilityPair = checker.lineIsAccessibleTo(inputLine, line);
        if (!accessibilityPair.getKey()) {
            return accessibilityPair;
        }

        objectLanguage = checker.getDerivationRuleset().getObjectLanguage();
        Language metaLanguage = checker.getDerivationRuleset().getMetaLanguage();

        String inputForm = "(\uD835\uDCAB → \uD835\uDCAC)";
        String outputForm = "(\uD835\uDCAB → ∀\uD835\uDCCD\uD835\uDCAC)";

        boolean resultGood = false;
        MatchUtilities.clearFormMatch();
        try {
            Pair<Boolean, Boolean> inputMatch = MatchUtilities.simpleFormMatch(new Document(inputForm), inputDoc, objectLanguage.getNameString(), metaLanguage.getNameString());
        }
        catch (TextMessageException e) {
            return new Pair(false, Collections.singletonList(ParseUtilities.newRegularText("Line (" + inputs[0] + ") is not of the right form to result in (" + line.getLineNumberLabel().getText() + ") by " + getName() + ".")));
        }

        try {
            Pair<Boolean, Boolean> outputMatchA= MatchUtilities.simpleFormMatch(new Document(outputForm), lineDoc, objectLanguage.getNameString(), metaLanguage.getNameString());
            if (outputMatchA.getKey()) resultGood = true;
        }
        catch (TextMessageException e) {   }

        boolean xFree = xFreeInP();
        if (xFree) return new Pair(false, freeVariableFailure);
        if (resultGood) return new Pair(true, null);

        else {
            return new Pair(false, Collections.singletonList(ParseUtilities.newRegularText("Line (" + line.getLineNumberLabel().getText() + ") does not result from (" + inputs[0] + ") by " + getName() + ".")));
        }
    }

    private boolean xFreeInP() {
        Term variableMatch = null;
        Expression formulaMatch = null;


        List<MVariable> mVariables = MVariable.getVariables();
        for (MVariable mVariable : mVariables) {
            if (mVariable.getBaseStr().equals("\ud835\udccd")) {
                Term term = new Term();
                term.setLevel(0);
                ArrayList children = new ArrayList();
                children.add(mVariable.getMatch());
                term.setChildren(children);
                term.setCombines(true);
                term.setTermType(TermType.VARIABLE);
                variableMatch = term;
            }
        }
        //       System.out.println("var: " + variableMatch);

        List<MFormula> mFormulas = MFormula.getmFormulas();
        for (MFormula mFormula : mFormulas) {
            if (mFormula.getFormulaSym().getBaseStr().equals("\ud835\udcab")) {
                formulaMatch = mFormula.getMatch();
            }
        }

        //       System.out.println("formula: " + formulaMatch);

        if (variableMatch != null && formulaMatch != null) {

            boolean freeInFormula = SyntacticalFns.expTermFreeInFormula(formulaMatch, variableMatch, objectLanguage.getNameString());

            if (freeInFormula) {
                List<Text> texts = new ArrayList<>();
                texts.add(ParseUtilities.newRegularText("Variable "));
                texts.addAll(variableMatch.toTextList());
                texts.add(ParseUtilities.newRegularText(" is free in formula "));
                texts.addAll(formulaMatch.toTextList());
                texts.add(ParseUtilities.newRegularText(". Cannot apply " + getName() + "."));
                freeVariableFailure = texts;
            }

            return freeInFormula;
        } else {
            System.out.println("problem with 'free in formula' check");
            return false;
        }

    }

}

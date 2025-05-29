package slapp.editor.derivation.der_systems;

import com.gluonhq.richtextarea.RichTextArea;
import com.gluonhq.richtextarea.model.Document;
import javafx.event.ActionEvent;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.util.Pair;
import slapp.editor.decorated_rta.BoxedDRTA;
import slapp.editor.derivation.DCheck;
import slapp.editor.derivation.DerivationCheck;
import slapp.editor.derivation.DerivationExercise;
import slapp.editor.derivation.ViewLine;
import slapp.editor.derivation_explain.DrvtnExpExercise;
import slapp.editor.parser.*;
import slapp.editor.parser.grammatical_parts.Formula;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class UniversalIntro extends DerivationRule {

    public UniversalIntro(String name, String rgexTemplate) {
        super(name, rgexTemplate);
        this.premAssp = false;
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

        Language objectLanguage = checker.getDerivationRuleset().getObjectLanguage();
        Language metaLanguage = checker.getDerivationRuleset().getMetaLanguage();
        String commaDividerString = metaLanguage.getCommaDivider();



        Document inputForm = new Document("\ud835\udcab\u2039\ud835\udccd" + commaDividerString + "\ud835\udccb\u203a" );
        Document outputForm = new Document("\u2200\ud835\udccd\ud835\udcab");

        boolean resultGood = false;

        //try A
        MatchUtilities.clearFormMatch();
        try {
            Pair<Boolean, Boolean> outputMatch = MatchUtilities.simpleFormMatch(outputForm, lineDoc, objectLanguage.getNameString(), metaLanguage.getNameString());
        }
        catch (TextMessageException e) {
            return new Pair(false, Collections.singletonList(ParseUtilities.newRegularText("Line (" + line.getLineNumberLabel().getText() + ") is not of the right form to result by " + getName() + ".")));
        }

        try {
            Pair<Boolean, Boolean> outputMatch = MatchUtilities.simpleFormMatch(inputForm, inputDoc, objectLanguage.getNameString(), metaLanguage.getNameString());
            if (outputMatch.getKey() == true && outputMatch.getValue() == true ) resultGood = true;
        }
        catch (TextMessageException e) {
            return new Pair(false, e.getMessageList());
        }


        Expression variableExp = ((Formula) MatchUtilities.getTransformList().get(0).getKey()).getSubTransform().getExp2().getMatch();

        //check variable free in undischarged assumption
        List<String> asspList = line.getAssumptionList();
        for (String asspLabel : asspList) {
            ViewLine asspLine;
            Pair<ViewLine, List<Text>> asspLinePair = checker.getLineFromLabel(asspLabel);
            if (asspLinePair.getKey() != null) asspLine = asspLinePair.getKey();
            else return new Pair(false, asspLinePair.getValue());

            TextFlow asspJustificationFlow = asspLine.getJustificationFlow();

            String asspJustificationString = (checker instanceof DerivationCheck) ? ((DerivationExercise) checker.getExercise()).getStringFromJustificationFlow(asspJustificationFlow) :
                    ((DrvtnExpExercise) checker.getExercise()).getStringFromJustificationFlow(asspJustificationFlow);


   //         String asspJustificationString = checker.getDerivationExercise().getStringFromJustificationFlow(asspJustificationFlow);
            if (!checker.getDerivationRuleset().getPremiseRule().matches(asspJustificationString)) {

                BoxedDRTA asspLineBDRTA = asspLine.getLineContentBoxedDRTA();
                RichTextArea asspLineRTA = asspLineBDRTA.getRTA();
                asspLineRTA.getActionFactory().saveNow().execute(new ActionEvent());
                Document asspLineDoc = asspLineRTA.getDocument();
                Expression asspLineExp = ParseUtilities.parseDoc(asspLineDoc, objectLanguage.getNameString()).get(0);

                if (SyntacticalFns.expTermFreeInFormula(asspLineExp, variableExp, objectLanguage.getNameString())) {
                    List<Text> list = new ArrayList<>();
                    list.add(new Text("Variable "));
                    list.addAll(variableExp.toTextList());
                    list.add(new Text(" is free in an undischarged assumption."));
                    return new Pair(false, list);
                }
            }
        }



        //check variable free in universal

        Expression lineExp = ParseUtilities.parseDoc(lineDoc, objectLanguage.getNameString()).get(0);
        if (SyntacticalFns.expTermFreeInFormula(lineExp, variableExp, objectLanguage.getNameString())) {
            List<Text> list = new ArrayList<>();
            list.add(new Text("Variable "));
            list.addAll(variableExp.toTextList());
            list.add(new Text(" is free in "));
            list.addAll(lineExp.toTextList());
            return new Pair(false, list);
        }


        if (resultGood) return new Pair(true, null);

        else {
            return new Pair(false, Collections.singletonList(ParseUtilities.newRegularText("Line (" + line.getLineNumberLabel().getText() + ") does not result from (" + inputs[0] + ") by " + getName() + ".")));
        }

    }


}

package slapp.editor.derivation.DerSystems;

import com.gluonhq.richtextarea.RichTextArea;
import com.gluonhq.richtextarea.model.Document;
import javafx.event.ActionEvent;
import javafx.scene.text.Text;
import javafx.util.Pair;
import slapp.editor.decorated_rta.BoxedDRTA;
import slapp.editor.derivation.DerivationCheck;
import slapp.editor.derivation.ViewLine;
import slapp.editor.parser.*;
import slapp.editor.parser.grammatical_parts.MFormula;
import slapp.editor.parser.grammatical_parts.Term;
import slapp.editor.parser.grammatical_parts.TermType;
import slapp.editor.parser.symbols.MFormulaSym;
import slapp.editor.parser.symbols.MVariable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class QuantifierPlacement extends DerivationRule {
    Language objectLanguage;
    List<Text> freeVariableFailure = new ArrayList<>();

    public QuantifierPlacement(String name, String rgexTemplate) {
        super(name, rgexTemplate);
        this.premAssp = false;
    }

    public Pair<Boolean, List<Text>> applies(DerivationCheck checker, ViewLine line, String... inputs) {

        objectLanguage = checker.getDerivationRuleset().getObjectLanguage();
        Language metaLanguage = checker.getDerivationRuleset().getMetaLanguage();

        String openBracketString = metaLanguage.getOpenBracket1();
        String closeBracketString = metaLanguage.getCloseBracket1();

        BoxedDRTA lineBDRTA = line.getLineContentBoxedDRTA();
        RichTextArea lineRTA = lineBDRTA.getRTA();
        lineRTA.getActionFactory().saveNow().execute(new ActionEvent());
        Document lineDoc = lineRTA.getDocument();


        List<Expression> lineExpressions = ParseUtilities.parseDoc(lineDoc, objectLanguage.getNameString());
        Expression lineExpression = lineExpressions.get(0);

        ViewLine inputLine1;
        Pair<ViewLine, List<Text>> line1Pair = checker.getLineFromLabel(inputs[0]);
        if (line1Pair.getKey() != null) inputLine1 = line1Pair.getKey();
        else return new Pair(false, line1Pair.getValue());

        BoxedDRTA inputBDRTA1 = inputLine1.getLineContentBoxedDRTA();
        RichTextArea inputRTA1 = inputBDRTA1.getRTA();
        inputRTA1.getActionFactory().saveNow().execute(new ActionEvent());
        Document inputDoc1 = inputRTA1.getDocument();
        if (inputDoc1.getText().equals("")) return new Pair(false, Collections.singletonList(ParseUtilities.newRegularText("Justification cannot appeal to the empty line (" + inputs[0] + ").")));

        List<Expression> inputExpressions = ParseUtilities.parseDoc(inputDoc1, objectLanguage.getNameString());
        Expression inputExpression = inputExpressions.get(0);

        Pair<Boolean, List<Text>> accessibilityPair1 = checker.lineIsAccessibleTo(inputLine1, line);
        if (!accessibilityPair1.getKey()) {
            return accessibilityPair1;
        }



        Document leftForm = new Document("\u2200\ud835\udccd" + openBracketString + "\ud835\udcab \u2227 \ud835\udcac" + closeBracketString );
        Expression leftFormExp = ParseUtilities.parseDoc(leftForm, metaLanguage.getNameString()).get(0);
        Document rightForm = new Document(openBracketString + "\ud835\udcab \u2227 \u2200\ud835\udccd\uud835\udcac" + closeBracketString );
        Expression rightFormExp = ParseUtilities.parseDoc(rightForm, metaLanguage.getNameString()).get(0);

        Document leftForm2 = new Document("\u2200\ud835\udccd" + openBracketString + "\ud835\udcac \u2227 \ud835\udcab" + closeBracketString );
        Expression leftFormExp2 = ParseUtilities.parseDoc(leftForm2, metaLanguage.getNameString()).get(0);
        Document rightForm2 = new Document(openBracketString + "\u2200\ud835\udccd\ud835\udcac \u2227 \ud835\udcab" + closeBracketString );
        Expression rightFormExp2 = ParseUtilities.parseDoc(rightForm2, metaLanguage.getNameString()).get(0);

        Document leftForm3 = new Document("\u2200\ud835\udccd" + openBracketString + "\ud835\udcab \u2228 \ud835\udcac" + closeBracketString );
        Expression leftFormExp3 = ParseUtilities.parseDoc(leftForm3, metaLanguage.getNameString()).get(0);
        Document rightForm3 = new Document(openBracketString + "\ud835\udcab \u2228 \u2200\ud835\udccd\uud835\udcac" + closeBracketString );
        Expression rightFormExp3 = ParseUtilities.parseDoc(rightForm3, metaLanguage.getNameString()).get(0);

        Document leftForm4 = new Document("\u2200\ud835\udccd" + openBracketString + "\ud835\udcac \u2228 \ud835\udcab" + closeBracketString );
        Expression leftFormExp4 = ParseUtilities.parseDoc(leftForm4, metaLanguage.getNameString()).get(0);
        Document rightForm4 = new Document(openBracketString + "\u2200\ud835\udccd\ud835\udcac \u2228 \ud835\udcab" + closeBracketString );
        Expression rightFormExp4 = ParseUtilities.parseDoc(rightForm4, metaLanguage.getNameString()).get(0);

        Document leftForm5 = new Document("\u2200\ud835\udccd" + openBracketString + "\ud835\udcab \u2192 \ud835\udcac" + closeBracketString );
        Expression leftFormExp5 = ParseUtilities.parseDoc(leftForm5, metaLanguage.getNameString()).get(0);
        Document rightForm5 = new Document(openBracketString + "\ud835\udcab \u2192 \u2200\ud835\udccd\uud835\udcac" + closeBracketString );
        Expression rightFormExp5 = ParseUtilities.parseDoc(rightForm5, metaLanguage.getNameString()).get(0);

        Document leftForm6 = new Document("\u2200\ud835\udccd" + openBracketString + "\ud835\udcac \u2192 \ud835\udcab" + closeBracketString );
        Expression leftFormExp6 = ParseUtilities.parseDoc(leftForm6, metaLanguage.getNameString()).get(0);
        Document rightForm6 = new Document(openBracketString + "\u2203\ud835\udccd\ud835\udcac \u2192 \ud835\udcab" + closeBracketString );
        Expression rightFormExp6 = ParseUtilities.parseDoc(rightForm6, metaLanguage.getNameString()).get(0);
        //-------

        Document leftForm7 = new Document("\u2203\ud835\udccd" + openBracketString + "\ud835\udcab \u2227 \ud835\udcac" + closeBracketString );
        Expression leftFormExp7 = ParseUtilities.parseDoc(leftForm7, metaLanguage.getNameString()).get(0);
        Document rightForm7 = new Document(openBracketString + "\ud835\udcab \u2227 \u2203\ud835\udccd\uud835\udcac" + closeBracketString );
        Expression rightFormExp7 = ParseUtilities.parseDoc(rightForm7, metaLanguage.getNameString()).get(0);

        Document leftForm8 = new Document("\u2203\ud835\udccd" + openBracketString + "\ud835\udcac \u2227 \ud835\udcab" + closeBracketString );
        Expression leftFormExp8 = ParseUtilities.parseDoc(leftForm8, metaLanguage.getNameString()).get(0);
        Document rightForm8 = new Document(openBracketString + "\u2203\ud835\udccd\ud835\udcac \u2227 \ud835\udcab" + closeBracketString );
        Expression rightFormExp8 = ParseUtilities.parseDoc(rightForm8, metaLanguage.getNameString()).get(0);

        Document leftForm9 = new Document("\u2203\ud835\udccd" + openBracketString + "\ud835\udcab \u2228 \ud835\udcac" + closeBracketString );
        Expression leftFormExp9 = ParseUtilities.parseDoc(leftForm9, metaLanguage.getNameString()).get(0);
        Document rightForm9 = new Document(openBracketString + "\ud835\udcab \u2228 \u2203\ud835\udccd\uud835\udcac" + closeBracketString );
        Expression rightFormExp9 = ParseUtilities.parseDoc(rightForm9, metaLanguage.getNameString()).get(0);

        Document leftForm10 = new Document("\u2203\ud835\udccd" + openBracketString + "\ud835\udcac \u2228 \ud835\udcab" + closeBracketString );
        Expression leftFormExp10 = ParseUtilities.parseDoc(leftForm10, metaLanguage.getNameString()).get(0);
        Document rightForm10 = new Document(openBracketString + "\u2203\ud835\udccd\ud835\udcac \u2228 \ud835\udcab" + closeBracketString );
        Expression rightFormExp10 = ParseUtilities.parseDoc(rightForm10, metaLanguage.getNameString()).get(0);

        Document leftForm11 = new Document("\u2203\ud835\udccd" + openBracketString + "\ud835\udcab \u2192 \ud835\udcac" + closeBracketString );
        Expression leftFormExp11 = ParseUtilities.parseDoc(leftForm11, metaLanguage.getNameString()).get(0);
        Document rightForm11 = new Document(openBracketString + "\ud835\udcab \u2192 \u2203\ud835\udccd\uud835\udcac" + closeBracketString );
        Expression rightFormExp11 = ParseUtilities.parseDoc(rightForm11, metaLanguage.getNameString()).get(0);


        Document leftForm12 = new Document("\u2203\ud835\udccd" + openBracketString + "\ud835\udcac \u2192 \ud835\udcab" + closeBracketString );
        Expression leftFormExp12 = ParseUtilities.parseDoc(leftForm12, metaLanguage.getNameString()).get(0);
        Document rightForm12 = new Document(openBracketString + "\u2200\ud835\udccd\ud835\udcac \u2192 \ud835\udcab" + closeBracketString );
        Expression rightFormExp12 = ParseUtilities.parseDoc(rightForm12, metaLanguage.getNameString()).get(0);






        //form 1
        MatchUtilities.clearFormMatch();
        try {
            boolean resultGood1 = MatchUtilities.replacementCheck(leftFormExp, rightFormExp, inputExpression, lineExpression);
            boolean xFree = xFreeInP();
            if (xFree) return new Pair(false, freeVariableFailure);
            if (resultGood1) return new Pair(true, null);
        }
        catch (TextMessageException e) {  }

        MatchUtilities.clearFormMatch();
        try {
            boolean resultGood1 = MatchUtilities.replacementCheck(rightFormExp, leftFormExp, inputExpression, lineExpression);
            boolean xFree = xFreeInP();
            if (xFree) return new Pair(false, freeVariableFailure);
            if (resultGood1) return new Pair(true, null);
        }
        catch (TextMessageException e) {  }

        //form 2
        MatchUtilities.clearFormMatch();
        try {
            boolean resultGood2 = MatchUtilities.replacementCheck(leftFormExp2, rightFormExp2, inputExpression, lineExpression);
            boolean xFree = xFreeInP();
            if (xFree)  return new Pair(false, freeVariableFailure);
            if (resultGood2) return new Pair(true, null);
        }
        catch (TextMessageException e) {  }

        MatchUtilities.clearFormMatch();
        try {
            boolean resultGood2 = MatchUtilities.replacementCheck(rightFormExp2, leftFormExp2, inputExpression, lineExpression);
            boolean xFree = xFreeInP();
            if (xFree) return new Pair(false, freeVariableFailure);
            if (resultGood2) return new Pair(true, null);
        }
        catch (TextMessageException e) {  }

        //form 3
        MatchUtilities.clearFormMatch();
        try {
            boolean resultGood3 = MatchUtilities.replacementCheck(leftFormExp3, rightFormExp3, inputExpression, lineExpression);
            boolean xFree = xFreeInP();
            if (xFree)  return new Pair(false, freeVariableFailure);
            if (resultGood3) return new Pair(true, null);
        }
        catch (TextMessageException e) {  }

        MatchUtilities.clearFormMatch();
        try {
            boolean resultGood3 = MatchUtilities.replacementCheck(rightFormExp3, leftFormExp3, inputExpression, lineExpression);
            boolean xFree = xFreeInP();
            if (xFree) return new Pair(false, freeVariableFailure);
            if (resultGood3) return new Pair(true, null);
        }
        catch (TextMessageException e) {  }

        //form 4
        MatchUtilities.clearFormMatch();
        try {
            boolean resultGood4 = MatchUtilities.replacementCheck(leftFormExp4, rightFormExp4, inputExpression, lineExpression);
            boolean xFree = xFreeInP();
            if (xFree)  return new Pair(false, freeVariableFailure);
            if (resultGood4) return new Pair(true, null);
        }
        catch (TextMessageException e) {  }

        MatchUtilities.clearFormMatch();
        try {
            boolean resultGood4 = MatchUtilities.replacementCheck(rightFormExp4, leftFormExp4, inputExpression, lineExpression);
            boolean xFree = xFreeInP();
            if (xFree) return new Pair(false, freeVariableFailure);
            if (resultGood4) return new Pair(true, null);
        }
        catch (TextMessageException e) {  }

        //form 5
        MatchUtilities.clearFormMatch();
        try {
            boolean resultGood5 = MatchUtilities.replacementCheck(leftFormExp5, rightFormExp5, inputExpression, lineExpression);
            boolean xFree = xFreeInP();
            if (xFree)  return new Pair(false, freeVariableFailure);
            if (resultGood5) return new Pair(true, null);
        }
        catch (TextMessageException e) {  }

        MatchUtilities.clearFormMatch();
        try {
            boolean resultGood5 = MatchUtilities.replacementCheck(rightFormExp5, leftFormExp5, inputExpression, lineExpression);
            boolean xFree = xFreeInP();
            if (xFree) return new Pair(false, freeVariableFailure);
            if (resultGood5) return new Pair(true, null);
        }
        catch (TextMessageException e) {  }


        //form 6
        MatchUtilities.clearFormMatch();
        try {
            boolean resultGood6 = MatchUtilities.replacementCheck(leftFormExp6, rightFormExp6, inputExpression, lineExpression);
            boolean xFree = xFreeInP();
            if (xFree)  return new Pair(false, freeVariableFailure);
            if (resultGood6) return new Pair(true, null);
        }
        catch (TextMessageException e) {  }

        MatchUtilities.clearFormMatch();
        try {
            boolean resultGood6 = MatchUtilities.replacementCheck(rightFormExp6, leftFormExp6, inputExpression, lineExpression);
            boolean xFree = xFreeInP();
            if (xFree) return new Pair(false, freeVariableFailure);
            if (resultGood6) return new Pair(true, null);
        }
        catch (TextMessageException e) {  }



        //form 7
        MatchUtilities.clearFormMatch();
        try {
            boolean resultGood7 = MatchUtilities.replacementCheck(leftFormExp7, rightFormExp7, inputExpression, lineExpression);
            boolean xFree = xFreeInP();
            if (xFree)  return new Pair(false, freeVariableFailure);
            if (resultGood7) return new Pair(true, null);
        }
        catch (TextMessageException e) {  }

        MatchUtilities.clearFormMatch();
        try {
            boolean resultGood7 = MatchUtilities.replacementCheck(rightFormExp7, leftFormExp7, inputExpression, lineExpression);
            boolean xFree = xFreeInP();
            if (xFree) return new Pair(false, freeVariableFailure);
            if (resultGood7) return new Pair(true, null);
        }
        catch (TextMessageException e) {  }

        //form 8
        MatchUtilities.clearFormMatch();
        try {
            boolean resultGood8 = MatchUtilities.replacementCheck(leftFormExp8, rightFormExp8, inputExpression, lineExpression);
            boolean xFree = xFreeInP();
            if (xFree)  return new Pair(false, freeVariableFailure);
            if (resultGood8) return new Pair(true, null);
        }
        catch (TextMessageException e) {  }

        MatchUtilities.clearFormMatch();
        try {
            boolean resultGood8 = MatchUtilities.replacementCheck(rightFormExp8, leftFormExp8, inputExpression, lineExpression);
            boolean xFree = xFreeInP();
            if (xFree) return new Pair(false, freeVariableFailure);
            if (resultGood8) return new Pair(true, null);
        }
        catch (TextMessageException e) {  }

        //form 9
        MatchUtilities.clearFormMatch();
        try {
            boolean resultGood9 = MatchUtilities.replacementCheck(leftFormExp9, rightFormExp9, inputExpression, lineExpression);
            boolean xFree = xFreeInP();
            if (xFree)  return new Pair(false, freeVariableFailure);
            if (resultGood9) return new Pair(true, null);
        }
        catch (TextMessageException e) {  }

        MatchUtilities.clearFormMatch();
        try {
            boolean resultGood9 = MatchUtilities.replacementCheck(rightFormExp9, leftFormExp9, inputExpression, lineExpression);
            boolean xFree = xFreeInP();
            if (xFree) return new Pair(false, freeVariableFailure);
            if (resultGood9) return new Pair(true, null);
        }
        catch (TextMessageException e) {  }

        //form 10
        MatchUtilities.clearFormMatch();
        try {
            boolean resultGood10 = MatchUtilities.replacementCheck(leftFormExp10, rightFormExp10, inputExpression, lineExpression);
            boolean xFree = xFreeInP();
            if (xFree)  return new Pair(false, freeVariableFailure);
            if (resultGood10) return new Pair(true, null);
        }
        catch (TextMessageException e) {  }

        MatchUtilities.clearFormMatch();
        try {
            boolean resultGood10 = MatchUtilities.replacementCheck(rightFormExp10, leftFormExp10, inputExpression, lineExpression);
            boolean xFree = xFreeInP();
            if (xFree) return new Pair(false, freeVariableFailure);
            if (resultGood10) return new Pair(true, null);
        }
        catch (TextMessageException e) {  }

        //form 11
        MatchUtilities.clearFormMatch();
        try {
            boolean resultGood11 = MatchUtilities.replacementCheck(leftFormExp11, rightFormExp11, inputExpression, lineExpression);
            boolean xFree = xFreeInP();
            if (xFree)  return new Pair(false, freeVariableFailure);
            if (resultGood11) return new Pair(true, null);
        }
        catch (TextMessageException e) {  }

        MatchUtilities.clearFormMatch();
        try {
            boolean resultGood11 = MatchUtilities.replacementCheck(rightFormExp11, leftFormExp11, inputExpression, lineExpression);
            boolean xFree = xFreeInP();
            if (xFree) return new Pair(false, freeVariableFailure);
            if (resultGood11) return new Pair(true, null);
        }
        catch (TextMessageException e) {  }


        //form 12
        MatchUtilities.clearFormMatch();
        try {
            boolean resultGood12 = MatchUtilities.replacementCheck(leftFormExp12, rightFormExp12, inputExpression, lineExpression);
            boolean xFree = xFreeInP();
            if (xFree)  return new Pair(false, freeVariableFailure);
            if (resultGood12) return new Pair(true, null);
        }
        catch (TextMessageException e) {  }

        MatchUtilities.clearFormMatch();
        try {
            boolean resultGood12 = MatchUtilities.replacementCheck(rightFormExp12, leftFormExp12, inputExpression, lineExpression);
            boolean xFree = xFreeInP();
            if (xFree) return new Pair(false, freeVariableFailure);
            if (resultGood12) return new Pair(true, null);
        }
        catch (TextMessageException e) {  }




        return new Pair(false, Collections.singletonList(ParseUtilities.newRegularText(("Lines (" + inputLine1.getLineNumberLabel().getText() + ") and (" + line.getLineNumberLabel().getText() + ") are not of the right form for application of " + getName() + "."))));


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
        }
        else {
            System.out.println("problem with 'free in formula' check");
            return false;
        }

    }


}

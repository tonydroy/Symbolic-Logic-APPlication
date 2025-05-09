package slapp.editor.derivation.der_systems;

import com.gluonhq.richtextarea.RichTextArea;
import com.gluonhq.richtextarea.model.Document;
import javafx.event.ActionEvent;
import javafx.scene.text.Text;
import javafx.util.Pair;
import slapp.editor.decorated_rta.BoxedDRTA;
import slapp.editor.derivation.DerivationCheck;
import slapp.editor.derivation.ViewLine;
import slapp.editor.parser.*;
import slapp.editor.parser.grammatical_parts.MTerm;
import slapp.editor.parser.grammatical_parts.Term;
import slapp.editor.parser.grammatical_parts.TermType;
import slapp.editor.parser.symbols.MVariable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Abb_NDpnt extends DerivationRule {
    Language objectLanguage;
    List<Text> freeVariableFailure;

    public Abb_NDpnt(String name, String rgexTemplate) {
        super(name, rgexTemplate);
        this.premAssp = false;

    }

    public Pair<Boolean, List<Text>> applies(DerivationCheck checker, ViewLine line, String... inputs) {

        objectLanguage = checker.getDerivationRuleset().getObjectLanguage();
        Language metaLanguage = checker.getDerivationRuleset().getMetaLanguage();

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

        String openBracketString = metaLanguage.getOpenBracket1();
        String closeBracketString = metaLanguage.getCloseBracket1();



        //bounded univ need 1:2, 1:3, 2:3 (each with both directions)
        Document boundedLeqUniv1 = new Document("(∀\uD835\uDCCD \uE8A6 \uD835\uDCC9)\uD835\uDCAB");
        Document boundedLeqUniv2 = new Document("(∀\uD835\uDCCD : \uD835\uDCCD \uE8A6 \uD835\uDCC9)\uD835\uDCAB");
        Document boundedLeqUniv3 = new Document("∀\uD835\uDCCD(\uD835\uDCCD \uE8A6 \uD835\uDCC9 → \uD835\uDCAB)");

        Document boundedLssUniv1 = new Document("(∀\uD835\uDCCD \uE8A4 \uD835\uDCC9)\uD835\uDCAB");
        Document boundedLssUniv2 = new Document("(∀\uD835\uDCCD : \uD835\uDCCD \uE8A4 \uD835\uDCC9)\uD835\uDCAB");
        Document boundedLssUniv3 = new Document("∀\uD835\uDCCD(\uD835\uDCCD \uE8A4 \uD835\uDCC9 → \uD835\uDCAB)");

        //bounded exis
        Document boundedLeqExis1 = new Document("(∃\uD835\uDCCD \uE8A6 \uD835\uDCC9)\uD835\uDCAB");
        Document boundedLeqExis2 = new Document("(∃\uD835\uDCCD : \uD835\uDCCD \uE8A6 \uD835\uDCC9)\uD835\uDCAB");
        Document boundedLeqExis3 = new Document("∃\uD835\uDCCD(\uD835\uDCCD \uE8A6 \uD835\uDCC9 ∧ \uD835\uDCAB)");

        Document boundedLssExis1 = new Document("(∃\uD835\uDCCD \uE8A4 \uD835\uDCC9)\uD835\uDCAB");
        Document boundedLssExis2 = new Document("(∃\uD835\uDCCD : \uD835\uDCCD \uE8A4 \uD835\uDCC9)\uD835\uDCAB");
        Document boundedLssExis3 = new Document("∃\uD835\uDCCD(\uD835\uDCCD \uE8A4 \uD835\uDCC9 ∧ \uD835\uDCAB)");

        //leq
        Document leq = new Document("(\uD835\uDCC8 \uE8A6 \uD835\uDCC9)");
        Document leqAbb = new Document("∃\uD835\uDC62(\uD835\uDC62 \uE8B8 \uD835\uDCC8 \uE8AC \uD835\uDCC9)");

        List<Expression> boundedLeqUniv1Expressions = ParseUtilities.parseDoc(boundedLeqUniv1, metaLanguage.getNameString());
        Expression boundedLeqUniv1Exp = boundedLeqUniv1Expressions.get(0);
        List<Expression> boundedLeqUniv2Expressions = ParseUtilities.parseDoc(boundedLeqUniv2, metaLanguage.getNameString());
        Expression boundedLeqUniv2Exp = boundedLeqUniv2Expressions.get(0);
        List<Expression> boundedLeqUniv3Expressions = ParseUtilities.parseDoc(boundedLeqUniv3, metaLanguage.getNameString());
        Expression boundedLeqUniv3Exp = boundedLeqUniv3Expressions.get(0);

        List<Expression> boundedLssUniv1Expressions = ParseUtilities.parseDoc(boundedLssUniv1, metaLanguage.getNameString());
        Expression boundedLssUniv1Exp = boundedLssUniv1Expressions.get(0);
        List<Expression> boundedLssUniv2Expressions = ParseUtilities.parseDoc(boundedLssUniv2, metaLanguage.getNameString());
        Expression boundedLssUniv2Exp = boundedLssUniv2Expressions.get(0);
        List<Expression> boundedLssUniv3Expressions = ParseUtilities.parseDoc(boundedLssUniv3, metaLanguage.getNameString());
        Expression boundedLssUniv3Exp = boundedLssUniv3Expressions.get(0);

        List<Expression> boundedLeqExis1Expressions = ParseUtilities.parseDoc(boundedLeqExis1, metaLanguage.getNameString());
        Expression boundedLeqExis1Exp = boundedLeqExis1Expressions.get(0);
        List<Expression> boundedLeqExis2Expressions = ParseUtilities.parseDoc(boundedLeqExis2, metaLanguage.getNameString());
        Expression boundedLeqExis2Exp = boundedLeqExis2Expressions.get(0);
        List<Expression> boundedLeqExis3Expressions = ParseUtilities.parseDoc(boundedLeqExis3, metaLanguage.getNameString());
        Expression boundedLeqExis3Exp = boundedLeqExis3Expressions.get(0);

        List<Expression> boundedLssExis1Expressions = ParseUtilities.parseDoc(boundedLssExis1, metaLanguage.getNameString());
        Expression boundedLssExis1Exp = boundedLssExis1Expressions.get(0);
        List<Expression> boundedLssExis2Expressions = ParseUtilities.parseDoc(boundedLssExis2, metaLanguage.getNameString());
        Expression boundedLssExis2Exp = boundedLssExis2Expressions.get(0);
        List<Expression> boundedLssExis3Expressions = ParseUtilities.parseDoc(boundedLssExis3, metaLanguage.getNameString());
        Expression boundedLssExis3Exp = boundedLssExis3Expressions.get(0);

        List<Expression> leqExpressions = ParseUtilities.parseDoc(leq, metaLanguage.getNameString());
        Expression leqExp = leqExpressions.get(0);
        List<Expression> leqAbbExpressions = ParseUtilities.parseDoc(leqAbb, metaLanguage.getNameString());
        Expression leqAbbExp = leqAbbExpressions.get(0);


        //for each group of three need 1:2, 1:3, 2:3 (each with both directions)

        // bounded leq unv
        MatchUtilities.clearFormMatch();
        try {
            boolean resultGood = MatchUtilities.replacementCheck(boundedLeqUniv1Exp, boundedLeqUniv2Exp, inputExpression, lineExpression);
            if (resultGood) return new Pair(true, null);
        } catch (TextMessageException e) {}
        try {
            boolean resultGood = MatchUtilities.replacementCheck(boundedLeqUniv2Exp, boundedLeqUniv1Exp, inputExpression, lineExpression);
            if (resultGood) return new Pair(true, null);
        } catch (TextMessageException e) {}

        MatchUtilities.clearFormMatch();
        try {
            boolean resultGood = MatchUtilities.replacementCheck(boundedLeqUniv1Exp, boundedLeqUniv3Exp, inputExpression, lineExpression);
            if (resultGood) return new Pair(true, null);
        } catch (TextMessageException e) {}
        try {
            boolean resultGood = MatchUtilities.replacementCheck(boundedLeqUniv3Exp, boundedLeqUniv1Exp, inputExpression, lineExpression);
            if (resultGood) return new Pair(true, null);
        } catch (TextMessageException e) {}

        MatchUtilities.clearFormMatch();
        try {
            boolean resultGood = MatchUtilities.replacementCheck(boundedLeqUniv2Exp, boundedLeqUniv3Exp, inputExpression, lineExpression);
            if (resultGood) return new Pair(true, null);
        } catch (TextMessageException e) {}
        try {
            boolean resultGood = MatchUtilities.replacementCheck(boundedLeqUniv3Exp, boundedLeqUniv2Exp, inputExpression, lineExpression);
            if (resultGood) return new Pair(true, null);
        } catch (TextMessageException e) {}

        // bounded less univ
        MatchUtilities.clearFormMatch();
        try {
            boolean resultGood = MatchUtilities.replacementCheck(boundedLssUniv1Exp, boundedLssUniv2Exp, inputExpression, lineExpression);
            if (resultGood) return new Pair(true, null);
        } catch (TextMessageException e) {}
        try {
            boolean resultGood = MatchUtilities.replacementCheck(boundedLssUniv2Exp, boundedLssUniv1Exp, inputExpression, lineExpression);
            if (resultGood) return new Pair(true, null);
        } catch (TextMessageException e) {}

        MatchUtilities.clearFormMatch();
        try {
            boolean resultGood = MatchUtilities.replacementCheck(boundedLssUniv1Exp, boundedLssUniv3Exp, inputExpression, lineExpression);
            if (resultGood) return new Pair(true, null);
        } catch (TextMessageException e) {}
        try {
            boolean resultGood = MatchUtilities.replacementCheck(boundedLssUniv3Exp, boundedLssUniv1Exp, inputExpression, lineExpression);
            if (resultGood) return new Pair(true, null);
        } catch (TextMessageException e) {}

        MatchUtilities.clearFormMatch();
        try {
            boolean resultGood = MatchUtilities.replacementCheck(boundedLssUniv2Exp, boundedLssUniv3Exp, inputExpression, lineExpression);
            if (resultGood) return new Pair(true, null);
        } catch (TextMessageException e) {}
        try {
            boolean resultGood = MatchUtilities.replacementCheck(boundedLssUniv3Exp, boundedLssUniv2Exp, inputExpression, lineExpression);
            if (resultGood) return new Pair(true, null);
        } catch (TextMessageException e) {}

        //bounded leq exis
        MatchUtilities.clearFormMatch();
        try {
            boolean resultGood = MatchUtilities.replacementCheck(boundedLeqExis1Exp, boundedLeqExis2Exp, inputExpression, lineExpression);
            if (resultGood) return new Pair(true, null);
        } catch (TextMessageException e) {}
        try {
            boolean resultGood = MatchUtilities.replacementCheck(boundedLeqExis2Exp, boundedLeqExis1Exp, inputExpression, lineExpression);
            if (resultGood) return new Pair(true, null);
        } catch (TextMessageException e) {}

        MatchUtilities.clearFormMatch();
        try {
            boolean resultGood = MatchUtilities.replacementCheck(boundedLeqExis1Exp, boundedLeqExis3Exp, inputExpression, lineExpression);
            if (resultGood) return new Pair(true, null);
        } catch (TextMessageException e) {}
        try {
            boolean resultGood = MatchUtilities.replacementCheck(boundedLeqExis3Exp, boundedLeqExis1Exp, inputExpression, lineExpression);
            if (resultGood) return new Pair(true, null);
        } catch (TextMessageException e) {}

        MatchUtilities.clearFormMatch();
        try {
            boolean resultGood = MatchUtilities.replacementCheck(boundedLeqExis2Exp, boundedLeqExis3Exp, inputExpression, lineExpression);
            if (resultGood) return new Pair(true, null);
        } catch (TextMessageException e) {}
        try {
            boolean resultGood = MatchUtilities.replacementCheck(boundedLeqExis3Exp, boundedLeqExis2Exp, inputExpression, lineExpression);
            if (resultGood) return new Pair(true, null);
        } catch (TextMessageException e) {}

        //bounded less exis
        MatchUtilities.clearFormMatch();
        try {
            boolean resultGood = MatchUtilities.replacementCheck(boundedLssExis1Exp, boundedLssExis2Exp, inputExpression, lineExpression);
            if (resultGood) return new Pair(true, null);
        } catch (TextMessageException e) {}
        try {
            boolean resultGood = MatchUtilities.replacementCheck(boundedLssExis2Exp, boundedLssExis1Exp, inputExpression, lineExpression);
            if (resultGood) return new Pair(true, null);
        } catch (TextMessageException e) {}

        MatchUtilities.clearFormMatch();
        try {
            boolean resultGood = MatchUtilities.replacementCheck(boundedLssExis1Exp, boundedLssExis3Exp, inputExpression, lineExpression);
            if (resultGood) return new Pair(true, null);
        } catch (TextMessageException e) {}
        try {
            boolean resultGood = MatchUtilities.replacementCheck(boundedLssExis3Exp, boundedLssExis1Exp, inputExpression, lineExpression);
            if (resultGood) return new Pair(true, null);
        } catch (TextMessageException e) {}

        MatchUtilities.clearFormMatch();
        try {
            boolean resultGood = MatchUtilities.replacementCheck(boundedLssExis2Exp, boundedLssExis3Exp, inputExpression, lineExpression);
            if (resultGood) return new Pair(true, null);
        } catch (TextMessageException e) {}
        try {
            boolean resultGood = MatchUtilities.replacementCheck(boundedLssExis3Exp, boundedLssExis2Exp, inputExpression, lineExpression);
            if (resultGood) return new Pair(true, null);
        } catch (TextMessageException e) {}

        //inequalities
        MatchUtilities.clearFormMatch();
        try{
            boolean resultGood = MatchUtilities.replacementCheck(leqExp, leqAbbExp, inputExpression, lineExpression);
            if (resultGood && (varFreeInTerm("\ud835\udc62", "\ud835\udcc8") || varFreeInTerm("\ud835\udc62", "\ud835\udcc9"))) return new Pair(false, freeVariableFailure);
            if (resultGood) return new Pair(true, null);
        }
        catch (TextMessageException e){}

        try {
            boolean resultGood = MatchUtilities.replacementCheck(leqAbbExp, leqExp, inputExpression, lineExpression);
            if (resultGood && (varFreeInTerm("\ud835\udc62", "\ud835\udcc8") || varFreeInTerm("\ud835\udc62", "\ud835\udcc9"))) return new Pair(false, freeVariableFailure);
            if (resultGood) return new Pair(true, null);
        }
        catch (TextMessageException e){}



        return new Pair(false, Collections.singletonList(ParseUtilities.newRegularText(("Lines (" + inputLine1.getLineNumberLabel().getText() + ") and (" + line.getLineNumberLabel().getText() + ") are not of the right form for application of " + getName() + "."))));

    }

    private boolean varFreeInTerm(String varString, String termString) {
        Term variableMatch = null;
        Expression matchTerm = null;


        List<MVariable> mVariables = MVariable.getVariables();
        for (MVariable mVariable : mVariables) {
            if (mVariable.getBaseStr().equals(varString)) {
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

        List<MTerm> mTerms = MTerm.getmTerms();
        for (MTerm mTerm : mTerms) {
            if (mTerm.getmTermSym().getBaseStr().equals(termString)) {
                matchTerm = mTerm.getMatch();
            }
        }

        //        System.out.println("term: " + matchTerm);

        if (variableMatch != null && matchTerm != null) {

       //     System.out.println("varMatch: " + variableMatch + " matchTerm: " + matchTerm);

            boolean freeInTerm = SyntacticalFns.expTermFreeInFormula(matchTerm, variableMatch, objectLanguage.getNameString());

            if (freeInTerm) {
                List<Text> texts = new ArrayList<>();
                texts.add(ParseUtilities.newRegularText("Variable "));
                texts.addAll(variableMatch.toTextList());
                if (!objectLanguage.getNameString().equals("LM Obj")) texts.add(ParseUtilities.newRegularText(" is free in term "));
                else texts.add(ParseUtilities.newRegularText(" (possibly) free in term "));
                texts.addAll(matchTerm.toTextList());
                texts.add(ParseUtilities.newRegularText(". Cannot apply " + getName() + "."));
                freeVariableFailure = texts;
            }

            return freeInTerm;
        } else {
            System.out.println("problem with 'free in term' check");
            return false;
        }
    }



}

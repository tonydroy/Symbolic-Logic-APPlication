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
import slapp.editor.parser.grammatical_parts.MFormula;
import slapp.editor.parser.grammatical_parts.MTerm;
import slapp.editor.parser.grammatical_parts.Term;
import slapp.editor.parser.grammatical_parts.TermType;
import slapp.editor.parser.symbols.MVariable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

public class QND_T51 extends Theorem {
    Language objectLanguage;
    List<Text> freeVariableFailure;

    public QND_T51(String name, String... forms) {
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

        String form = "(∼\uE8AC\uD835\uDCC9\uE886 → ∃\uD835\uDCCD\uE8AC\uD835\uDCC9\uD835\uDC46\uD835\uDCCD)";

        boolean resultGood = false;

        MatchUtilities.clearFormMatch();
        try {
            Pair<Boolean, Boolean> match = MatchUtilities.simpleFormMatch(new Document(form), lineDoc, objectLanguage.getNameString(), metaLanguage.getNameString());
            if (match.getKey()) resultGood = true;
        } catch (TextMessageException e) {
        }

        boolean xFree = xFreeInT();
        if (xFree) return new Pair(false, freeVariableFailure);
        if (resultGood) return new Pair(true, null);
        else {
            return new Pair(false, Collections.singletonList(ParseUtilities.newRegularText("Line (" + line.getLineNumberLabel().getText() + ") is not an instance of " + getName() + ".")));
        }
    }



    private boolean xFreeInT() {
        Term variableMatch = null;
        Expression matchTerm = null;


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

        List<MTerm> mTerms = MTerm.getmTerms();
        for (MTerm mTerm : mTerms) {
            if (mTerm.getmTermSym().getBaseStr().equals("\ud835\udcc9")) {
                matchTerm = mTerm.getMatch();
            }
        }

       //        System.out.println("term: " + matchTerm);

        if (variableMatch != null && matchTerm != null) {

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

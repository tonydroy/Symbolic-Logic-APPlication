package slapp.editor.derivation;

import com.gluonhq.richtextarea.RichTextArea;
import com.gluonhq.richtextarea.model.Document;
import javafx.event.ActionEvent;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Pair;
import slapp.editor.EditorAlerts;
import slapp.editor.ExerciseHelpPopup;
import slapp.editor.parser.*;
import slapp.editor.parser.grammatical_parts.Formula;
import slapp.editor.parser.grammatical_parts.MFormula;
import slapp.editor.parser.grammatical_parts.Operator;
import slapp.editor.parser.symbols.MFormulaSym;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DerivationHelp {
    private DerivationExercise derivationExercise;
    private DerivationView derivationView;
    private DerivationModel derivationModel;
    private DerivationCheck derivationCheck;
    private List<ViewLine> viewLines;
    Stage helpStage;

    private ViewLine targetLine;
    private int targetRow;
    private Formula targetFormula;
    private Language objectLanguage;
    private Language metaLanguage;

    private List<Formula> accessibleFormulas;
    private List<Formula> eObtainableFormulas;
    private List<Formula> ePotentialFormulas;

    public DerivationHelp(DerivationExercise derivationExercise) {
        this.derivationExercise = derivationExercise;
        this.derivationView = derivationExercise.getExerciseView();
        this.derivationModel= derivationExercise.getExerciseModel();
        this.derivationCheck = derivationExercise.getDerivationCheck();


        derivationView.getHelpButton().setOnAction(e -> {
            closeHelpWindow();
            runHelp();
        });


    }

    public void closeHelpWindow() {
        if (helpStage != null) helpStage.close();
    }

    private void runHelp() {
        derivationCheck.setHelpTries(derivationCheck.getHelpTries() + 1);
        derivationCheck.setHelpCounter();

        objectLanguage = derivationCheck.getDerivationRuleset().getObjectLanguage();
        metaLanguage = derivationCheck.getDerivationRuleset().getMetaLanguage();

        //progress ok
        EditorAlerts.setShowPreliminary(true, "Contextual help begins with a background progress check.");
        boolean progressCheck = derivationCheck.backgroundProgressCheck();
        EditorAlerts.setShowPreliminary(false, "");
        if (!progressCheck) return;

        //non-empty selected row
        viewLines = derivationView.getViewLines();
        targetRow = derivationExercise.currentRow();
        if (targetRow < 0) {
            EditorAlerts.fleetingRedPopup("Select goal formula for contextual help");
            return;
        }

        targetLine = viewLines.get(targetRow);
        RichTextArea targetRTA = targetLine.getLineContentBoxedDRTA().getRTA();
        targetRTA.getActionFactory().saveNow().execute(new ActionEvent());
        Document targetDoc = targetRTA.getDocument();

        if (targetDoc.getText().equals("")) {
            EditorAlerts.fleetingRedPopup("Select goal formula for contextual help");
            return;
        }

        List<Expression> targetExpressions = ParseUtilities.parseDoc(targetDoc, objectLanguage.getNameString());
        targetFormula = (Formula) targetExpressions.get(0);

        //goal or contradiction strategies
        Document contradictionForm1 = new Document("\u22a5" );
        Document contradictionForm2 = new Document("(\ud835\udcac \u2227 \u223c\ud835\udcac)");

        MatchUtilities.clearFormMatch();
        boolean goalIsContradiction = false;
        try {
            Pair<Boolean, Boolean> match1 = MatchUtilities.simpleExpFormMatch(contradictionForm1, targetFormula, objectLanguage.getNameString(), metaLanguage.getNameString());
            goalIsContradiction = true;
        } catch (TextMessageException e) {}

        MatchUtilities.clearFormMatch();
        try {
            Pair<Boolean, Boolean> match2 = MatchUtilities.simpleExpFormMatch(contradictionForm2, targetFormula, objectLanguage.getNameString(), metaLanguage.getNameString());
            goalIsContradiction = true;
        } catch (TextMessageException e) {}

        if (goalIsContradiction) runStrategiesForContradiction();
        else runStrategiesForGoal();
    }

    private void runStrategiesForGoal() {
        List<Text> footnotes = new ArrayList<>();
        footnotes.add(new Text("\n-----"));

        setAccessibleFormulas();

        //target in accesible
        if (formulaInList(targetFormula, accessibleFormulas)) {
            List texts = new ArrayList();
            texts.addAll(targetFormula.toTextList());
            texts.add(ParseUtilities.newRegularText(" exists on an accessible line.  Reiterate."));
            ExerciseHelpPopup popup = new ExerciseHelpPopup(texts);
            helpStage = popup.getHelpStage();
            helpStage.show();
            return;
        }

        setEObtainableFormulas();

        //target in EObtainable
        if (formulaInList(targetFormula, eObtainableFormulas)) {
            List texts = new ArrayList();
            texts.addAll(targetFormula.toTextList());
            texts.add(ParseUtilities.newRegularText(" can be obtained without subderivations by simple exploitation rules.  Use E-rules to reach goal."));
            ExerciseHelpPopup popup = new ExerciseHelpPopup(texts);
            helpStage = popup.getHelpStage();
            helpStage.show();
            return;
        }

        //contradiction
        if (contradictionInList(accessibleFormulas)) {
            List<Text> texts = Collections.singletonList(ParseUtilities.newRegularText("There is a contradiction on accessible lines.  Set up to obtain goal by \u223cE and use the contradiction."));
            ExerciseHelpPopup popup = new ExerciseHelpPopup(texts);
            helpStage = popup.getHelpStage();
            helpStage.show();
            return;
        }

        if (contradictionInList(eObtainableFormulas)) {
            List<Text> texts = Collections.singletonList(ParseUtilities.newRegularText("A contradiction can be obtained from accessible lines by simple exploitation rules.  Set up to reach goal by \u223cE and use the contradiction."));
            ExerciseHelpPopup popup = new ExerciseHelpPopup(texts);
            helpStage = popup.getHelpStage();
            helpStage.show();
            return;
        }

        footnotes.add(ParseUtilities.newRegularText("\n\u2022 No contradiction from accessible lines."));

        //disjunction
        if (disjunctionInList(accessibleFormulas)) {
            List texts = new ArrayList();
            texts.add(ParseUtilities.newRegularText("There is a disjunction on an accessible line.  Set up with subderivations to obtain goal by \u2228E."));
            texts.addAll(footnotes);
            ExerciseHelpPopup popup = new ExerciseHelpPopup(texts);
            helpStage = popup.getHelpStage();
            helpStage.show();
            return;
        }

        if (disjunctionInList(eObtainableFormulas)) {
            List texts = new ArrayList();
            texts.add(ParseUtilities.newRegularText("A disjunction can be obtained from accessible lines by simple exploitation rules.  Obtain the disjunction, and set up subderivations to obtain goal by \u2228E."));
            texts.addAll(footnotes);
            ExerciseHelpPopup popup = new ExerciseHelpPopup(texts);
            helpStage = popup.getHelpStage();
            helpStage.show();
            return;
        }

        footnotes.add(ParseUtilities.newRegularText("\n\u2022 No disjunction from accessible lines."));

        setEPotentialFormulas();

        if (formulaInList(targetFormula, ePotentialFormulas)) {
            List texts = new ArrayList();
            texts.addAll(targetFormula.toTextList());
            texts.add(ParseUtilities.newRegularText(" appears \"in\" accessible lines.  Set new goal(s) as necessary, and attempt to exploit it out."));
            texts.addAll(footnotes);
            ExerciseHelpPopup popup = new ExerciseHelpPopup(texts);
            helpStage = popup.getHelpStage();
            helpStage.show();
            return;
        }

        footnotes.add(ParseUtilities.newRegularText("\n\u2022 Goal not \"in\" accessible lines."));

        //main operator intro
        Operator mainOperator = targetFormula.getMainOperator();
        if (mainOperator != null) {
            List<Text> opTexts = mainOperator.toTextList();
            List<Text> opTexts2 = mainOperator.toTextList();
            List<Text> opTexts3 = mainOperator.toTextList();
            if (mainOperator.getType() == ExpressionType.NEG_OP || mainOperator.getType() == ExpressionType.COND_OP) {
                List texts = new ArrayList();
                texts.add(ParseUtilities.newRegularText("Goal has has main operator "));
                texts.addAll(opTexts);
                texts.add(ParseUtilities.newRegularText(".  Set up subderivation to obtain goal by "));
                texts.addAll(opTexts2);
                texts.add(ParseUtilities.newRegularText("I."));
                texts.addAll(footnotes);
                ExerciseHelpPopup popup = new ExerciseHelpPopup(texts);
                helpStage = popup.getHelpStage();
                helpStage.show();
                return;
            }

            if (mainOperator.getType() == ExpressionType.BICOND_OP) {
                List texts = new ArrayList();
                texts.add(ParseUtilities.newRegularText("Goal has has main operator "));
                texts.addAll(opTexts);
                texts.add(ParseUtilities.newRegularText(".  Set up subderivations to obtain goal by "));
                texts.addAll(opTexts2);
                texts.add(ParseUtilities.newRegularText("I."));
                texts.addAll(footnotes);
                ExerciseHelpPopup popup = new ExerciseHelpPopup(texts);
                helpStage = popup.getHelpStage();
                helpStage.show();
                return;
            }

            if (mainOperator.getType() == ExpressionType.CONJ_OP) {
                List texts = new ArrayList();
                texts.add(ParseUtilities.newRegularText("Goal has has main operator "));
                texts.addAll(opTexts);
                texts.add(ParseUtilities.newRegularText(".  If you do not already have them, set the sides as new goals, for application of "));
                texts.addAll(opTexts2);
                texts.add(ParseUtilities.newRegularText("I."));
                texts.addAll(footnotes);
                ExerciseHelpPopup popup = new ExerciseHelpPopup(texts);
                helpStage = popup.getHelpStage();
                helpStage.show();
                return;
            }

            if (mainOperator.getType() == ExpressionType.DISJ_OP) {
                List texts = new ArrayList();
                texts.add(ParseUtilities.newRegularText("Goal has has main operator "));
                texts.addAll(opTexts);
                texts.add(ParseUtilities.newRegularText(".  Set one side or the other as new goal to use "));
                texts.addAll(opTexts2);
                texts.add(ParseUtilities.newRegularText("I.  Note: If your are not able to obtain either side for "));
                texts.addAll(opTexts3);
                texts.add(ParseUtilities.newRegularText("I, set subderivation to obtain goal by negation exploit."));
                texts.addAll(footnotes);
                ExerciseHelpPopup popup = new ExerciseHelpPopup(texts);
                helpStage = popup.getHelpStage();
                helpStage.show();
                return;
            }
        }

        footnotes.add(ParseUtilities.newRegularText("\n\u2022 No main operator."));

        List texts = new ArrayList();
        texts.add(ParseUtilities.newRegularText(" Since there is no main operator, you cannot use an I-rule.  Set up to obtain goal by negation exploit."));
        texts.addAll(footnotes);
        ExerciseHelpPopup popup = new ExerciseHelpPopup(texts);
        helpStage = popup.getHelpStage();
        helpStage.show();
    }

    private void runStrategiesForContradiction() {
        List<Text> footnotes = new ArrayList<>();
        footnotes.add(new Text("\n-----"));

        setAccessibleFormulas();

        if (contradictionInList(accessibleFormulas)) {
            List<Text> texts = Collections.singletonList(ParseUtilities.newRegularText("There is a contradiction on accessible lines.  Use this contradiction."));
            ExerciseHelpPopup popup = new ExerciseHelpPopup(texts);
            helpStage = popup.getHelpStage();
            helpStage.show();
            return;
        }

        setEObtainableFormulas();

        if (contradictionInList(eObtainableFormulas)) {
            List<Text> texts = Collections.singletonList(ParseUtilities.newRegularText("A contradiction can be obtained from accessible lines by simple exploitation rules.  Use this contradiction."));
            ExerciseHelpPopup popup = new ExerciseHelpPopup(texts);
            helpStage = popup.getHelpStage();
            helpStage.show();
            return;
        }

        footnotes.add(ParseUtilities.newRegularText("\n\u2022 No contradiction from accessible lines."));

        //disjunction
        if (disjunctionInList(accessibleFormulas)) {
            List texts = new ArrayList();
            texts.add(ParseUtilities.newRegularText("There is a disjunction on an accessible line.  Set up with subderivations to obtain \u22a5 by \u2228E."));
            texts.addAll(footnotes);
            ExerciseHelpPopup popup = new ExerciseHelpPopup(texts);
            helpStage = popup.getHelpStage();
            helpStage.show();
            return;
        }

        if (disjunctionInList(eObtainableFormulas)) {
            List texts = new ArrayList();
            texts.add(ParseUtilities.newRegularText("A disjunction can be obtained from accessible lines by simple exploitation rules.  Obtain the disjunction, and set up subderivations to obtain \u22a5 by \u2228E."));
            texts.addAll(footnotes);
            ExerciseHelpPopup popup = new ExerciseHelpPopup(texts);
            helpStage = popup.getHelpStage();
            helpStage.show();
            return;
        }

        footnotes.add(ParseUtilities.newRegularText("\n\u2022 No disjunction from accessible lines."));

        //complex negation
        if (complexNegationInList(accessibleFormulas)) {
            List texts = new ArrayList();
            texts.add(ParseUtilities.newRegularText("There is a complex negation \u223c\ud835\udcab on accessible lines.  Set its opposite \ud835\udcab as goal, and use the pair of them for contradiction."));
            texts.addAll(footnotes);
            ExerciseHelpPopup popup = new ExerciseHelpPopup(texts);
            helpStage = popup.getHelpStage();
            helpStage.show();
            return;
        }

        if (contradictionInList(eObtainableFormulas)) {
            List texts = new ArrayList();
            texts.add(ParseUtilities.newRegularText("A complex negation \u223c\ud835\udcab can be obtained from accessible lines by simple exploitation rules.  Obtain this negation; then set its opposite \ud835\udcab as a new goal, and then use the pair of them for contradiction."));
            texts.addAll(footnotes);
            ExerciseHelpPopup popup = new ExerciseHelpPopup(texts);
            helpStage = popup.getHelpStage();
            helpStage.show();
            return;
        }

        footnotes.add(ParseUtilities.newRegularText("\n\u2022 No complex negation."));

        List texts = new ArrayList();
        texts.add(ParseUtilities.newRegularText("For some \ud835\udcab such that both \ud835\udcab and \u223c\ud835\udcab lead to contradiction: " +
                "Assume \ud835\udcab (\u223c\ud835\udcab), and obtain the first contradiction to conclude \u223c\ud835\udcab (\ud835\udcab); then from that obtain the second contradiction.  " +
                "Note: This is the hardest strategy to apply because it can be difficult to identify a \ud835\udcab to do the job.  The best you can do is look for it!"));
        texts.addAll(footnotes);
        ExerciseHelpPopup popup = new ExerciseHelpPopup(texts);
        helpStage = popup.getHelpStage();
        helpStage.show();
        return;


    }


    private void setEPotentialFormulas() {
        ePotentialFormulas = new ArrayList<>(eObtainableFormulas);

        boolean changes = true;
        whileLoop:
        while (changes) {
            changes = false;
            for (Formula formula : ePotentialFormulas) {

                //process conjunctions
                Document PandQform = new Document("(\ud835\udcab \u2227 \ud835\udcac)");
                try {
                    MatchUtilities.clearFormMatch();
                    MatchUtilities.simpleExpFormMatch(PandQform, formula, objectLanguage.getNameString(), metaLanguage.getNameString() );
                    for (MFormula mForm : MFormula.getmFormulas()) {
                        if (mForm.getFormulaSym().equals(new MFormulaSym("\ud835\udcab", "")) ) {
                            Formula matchFormula = mForm.getMatch();
                            if (!formulaInList(matchFormula, ePotentialFormulas)) {
                                ePotentialFormulas.add(matchFormula);
                                changes = true;
                                continue whileLoop;
                            }
                        }
                    }
                } catch (TextMessageException e) {}

                try {
                    MatchUtilities.clearFormMatch();
                    MatchUtilities.simpleExpFormMatch(PandQform, formula, objectLanguage.getNameString(), metaLanguage.getNameString() );
                    for (MFormula mForm : MFormula.getmFormulas()) {
                        if (mForm.getFormulaSym().equals(new MFormulaSym("\ud835\udcac", ""))) {
                            Formula matchFormula = mForm.getMatch();
                            if (!formulaInList(matchFormula, ePotentialFormulas)) {
                                ePotentialFormulas.add(matchFormula);
                                changes = true;
                                continue whileLoop;
                            }
                        }
                    }
                } catch (TextMessageException e) {}

                //process conditionals
                Document PimpQform = new Document("(\ud835\udcab \u2192 \ud835\udcac)");
                try {
                    MatchUtilities.clearFormMatch();
                    MatchUtilities.simpleExpFormMatch(PimpQform, formula, objectLanguage.getNameString(), metaLanguage.getNameString() );
                    for (MFormula mForm : MFormula.getmFormulas()) {
                        if (mForm.getFormulaSym().equals(new MFormulaSym("\ud835\udcac", "")) ) {
                            Formula matchFormula = mForm.getMatch();
                            if (!formulaInList(matchFormula, ePotentialFormulas)) {
                                ePotentialFormulas.add(matchFormula);
                                changes = true;
                                continue whileLoop;
                            }
                        }
                    }
                } catch (TextMessageException e) {}


                //process biconditionals
                Document PbimpQform = new Document("(\ud835\udcab \u2194 \ud835\udcac)");
                try {
                    MatchUtilities.clearFormMatch();
                    MatchUtilities.simpleExpFormMatch(PbimpQform, formula, objectLanguage.getNameString(), metaLanguage.getNameString() );
                    for (MFormula mForm : MFormula.getmFormulas()) {
                        if (mForm.getFormulaSym().equals(new MFormulaSym("\ud835\udcab", "")) ) {
                            Formula matchFormula = mForm.getMatch();
                            if (!formulaInList(matchFormula, ePotentialFormulas)) {
                                ePotentialFormulas.add(matchFormula);
                                changes = true;
                                continue whileLoop;
                            }
                        }
                    }
                } catch (TextMessageException e) {}

                try {
                    MatchUtilities.clearFormMatch();
                    MatchUtilities.simpleExpFormMatch(PbimpQform, formula, objectLanguage.getNameString(), metaLanguage.getNameString() );
                    for (MFormula mForm : MFormula.getmFormulas()) {
                        if (mForm.getFormulaSym().equals(new MFormulaSym("\ud835\udcac", ""))) {
                            Formula matchFormula = mForm.getMatch();
                            if (!formulaInList(matchFormula, ePotentialFormulas)) {
                                ePotentialFormulas.add(matchFormula);
                                changes = true;
                                continue whileLoop;
                            }
                        }
                    }
                } catch (TextMessageException e) {}
            }
        }
    }


    private void setEObtainableFormulas() {
        eObtainableFormulas = new ArrayList<>(accessibleFormulas);

        boolean changes = true;
        whileLoop:
        while (changes) {
            changes = false;
            for (Formula formula : eObtainableFormulas) {

                //process conjunctions
                Document PandQform = new Document("(\ud835\udcab \u2227 \ud835\udcac)");
                try {
                    MatchUtilities.clearFormMatch();
                    MatchUtilities.simpleExpFormMatch(PandQform, formula, objectLanguage.getNameString(), metaLanguage.getNameString() );
                    for (MFormula mForm : MFormula.getmFormulas()) {
                        if (mForm.getFormulaSym().equals(new MFormulaSym("\ud835\udcab", "")) ) {
                            Formula matchFormula = mForm.getMatch();
                            if (!formulaInList(matchFormula, eObtainableFormulas)) {
                                eObtainableFormulas.add(matchFormula);
                                changes = true;
                                continue whileLoop;
                            }
                        }
                    }
                } catch (TextMessageException e) {}

                try {
                    MatchUtilities.clearFormMatch();
                    MatchUtilities.simpleExpFormMatch(PandQform, formula, objectLanguage.getNameString(), metaLanguage.getNameString() );
                    for (MFormula mForm : MFormula.getmFormulas()) {
                        if (mForm.getFormulaSym().equals(new MFormulaSym("\ud835\udcac", ""))) {
                            Formula matchFormula = mForm.getMatch();
                            if (!formulaInList(matchFormula, eObtainableFormulas)) {
                                eObtainableFormulas.add(matchFormula);
                                changes = true;
                                continue whileLoop;
                            }
                        }
                    }
                } catch (TextMessageException e) {}

                //process conditionals
                Document PimpQform = new Document("(\ud835\udcab \u2192 \ud835\udcac)");
                try {
                    MatchUtilities.clearFormMatch();
                    MatchUtilities.simpleExpFormMatch(PimpQform, formula, objectLanguage.getNameString(), metaLanguage.getNameString() );
                    Formula antecedent = null;
                    Formula consequent = null;
                    for (MFormula mForm : MFormula.getmFormulas()) {
                        if (mForm.getFormulaSym().equals(new MFormulaSym("\ud835\udcab", "")))  antecedent = mForm.getMatch();
                        if (mForm.getFormulaSym().equals(new MFormulaSym("\ud835\udcac", ""))) consequent = mForm.getMatch();
                    }
                    if (antecedent != null && consequent != null && formulaInList(antecedent, eObtainableFormulas)) {
                        if (!formulaInList(consequent, eObtainableFormulas)) {
                            eObtainableFormulas.add(consequent);
                            changes = true;
                            continue whileLoop;
                        }
                    }
                } catch (TextMessageException e) {}

                //process biconditionals
                Document PbimpQform = new Document("(\ud835\udcab \u2194 \ud835\udcac)");
                try {
                    MatchUtilities.clearFormMatch();
                    MatchUtilities.simpleExpFormMatch(PbimpQform, formula, objectLanguage.getNameString(), metaLanguage.getNameString() );
                    Formula leftSide = null;
                    Formula rightSide = null;
                    for (MFormula mForm : MFormula.getmFormulas()) {
                        if (mForm.getFormulaSym().equals(new MFormulaSym("\ud835\udcab", ""))) leftSide = mForm.getMatch();
                        if (mForm.getFormulaSym().equals(new MFormulaSym("\ud835\udcac", ""))) rightSide = mForm.getMatch();
                    }
                    if (leftSide != null && rightSide != null && formulaInList(leftSide, eObtainableFormulas)) {
                        if (!formulaInList(rightSide, eObtainableFormulas)) {
                            eObtainableFormulas.add(rightSide);
                            changes = true;
                            continue whileLoop;
                        }
                    }
                    if (leftSide != null && rightSide != null && formulaInList(rightSide, eObtainableFormulas)) {
                        if (!formulaInList(leftSide, eObtainableFormulas)) {
                            eObtainableFormulas.add(leftSide);
                            changes = true;
                            continue whileLoop;
                        }
                    }
                } catch (TextMessageException e) {}
            }
        }



    }


    private void setAccessibleFormulas() {
        accessibleFormulas = new ArrayList<>();
        for (ViewLine line : viewLines) {
            if (LineType.isContentLine(line.getLineType())) {
                if (derivationCheck.lineIsAccessibleTo(line, targetLine).getKey()) {
                    Formula lineForm = getFormulaFromViewLine(line);
                    if (lineForm != null && !formulaInList(lineForm, accessibleFormulas)) accessibleFormulas.add(lineForm);
                }
            }
        }
    }

    private boolean complexNegationInList(List<Formula> formulaList) {
        Document negationForm = new Document("\u223c\ud835\udcac");
        for (Formula formula : formulaList) {
            MatchUtilities.clearFormMatch();
            try {
                MatchUtilities.simpleExpFormMatch(negationForm, formula, objectLanguage.getNameString(), metaLanguage.getNameString() );

                for (MFormula mForm : MFormula.getmFormulas()) {
                    if (mForm.getFormulaSym().equals(new MFormulaSym("\ud835\udcac", ""))) {
                        Formula matchFormula = mForm.getMatch();
                        if (!matchFormula.isAtomic()) return true;
                    }
                }
            } catch (TextMessageException e) {}
        }
        return false;
    }

    private boolean disjunctionInList(List<Formula> formulaList) {
        Document disjunctionForm = new Document("(\ud835\udcab \u2228 \ud835\udcac)");
        for (Formula formula : formulaList) {
            MatchUtilities.clearFormMatch();
            try {
                MatchUtilities.simpleExpFormMatch(disjunctionForm, formula, objectLanguage.getNameString(), metaLanguage.getNameString());
                return true;
            } catch (TextMessageException e) {}
        }
        return false;
    }

    private boolean contradictionInList(List<Formula> formulaList) {
        Document contradictionForm1 = new Document("\u22a5" );
        Document contradictionForm2 = new Document("(\ud835\udcac \u2227 \u223c\ud835\udcac)");

        for (Formula formula : formulaList) {
            MatchUtilities.clearFormMatch();
            try {
                MatchUtilities.simpleExpFormMatch(contradictionForm1, formula, objectLanguage.getNameString(), metaLanguage.getNameString());
                return true;
            } catch (TextMessageException e) {}

            MatchUtilities.clearFormMatch();
            try {
                MatchUtilities.simpleExpFormMatch(contradictionForm2, targetFormula, objectLanguage.getNameString(), metaLanguage.getNameString());
                return true;
            } catch (TextMessageException e) {   }
        }

        for (Formula formula : formulaList) {
            Document Pdoc = new Document("\ud835\udcac");
            Document negPdoc = new Document("\u223c\ud835\udcac");

            MatchUtilities.clearFormMatch();
            try {
                MatchUtilities.simpleExpFormMatch(Pdoc, formula, objectLanguage.getNameString(), metaLanguage.getNameString());
            } catch (TextMessageException e) { }

            for (Formula candidate : formulaList) {
                try {
                    MatchUtilities.simpleExpFormMatch(negPdoc, candidate, objectLanguage.getNameString(), metaLanguage.getNameString());
                    return true;
                } catch (TextMessageException e) {  }
            }
        }

        return false;
    }


    private boolean formulaInList(Formula formula, List<Formula> formulaList) {

        Document formDoc = new Document("\ud835\udcab");

        MatchUtilities.clearFormMatch();
        try {
            MatchUtilities.simpleExpFormMatch(formDoc, formula, objectLanguage.getNameString(), metaLanguage.getNameString());
        } catch (TextMessageException e) { }

        for (Formula candidate : formulaList) {
            try {
                MatchUtilities.simpleExpFormMatch(formDoc, candidate, objectLanguage.getNameString(), metaLanguage.getNameString());
                return true;
            } catch (TextMessageException e) {  }
        }
        return false;
    }


    private Formula getFormulaFromViewLine(ViewLine viewLine) {

        Formula lineFormula = null;
        RichTextArea lineRTA = viewLine.getLineContentBoxedDRTA().getRTA();
        lineRTA.getActionFactory().saveNow().execute(new ActionEvent());
        Document lineDoc = lineRTA.getDocument();


        if (!lineDoc.getText().equals("")) {
            List<Expression> lineExpressions = ParseUtilities.parseDoc(lineDoc, objectLanguage.getNameString());
            lineFormula = (Formula) lineExpressions.get(0);
        }
        return lineFormula;
    }




    public Stage getHelpStage() {
        return helpStage;
    }







}

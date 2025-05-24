package slapp.editor.derivation;

import com.gluonhq.richtextarea.RichTextArea;
import com.gluonhq.richtextarea.model.Document;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Pair;
import slapp.editor.EditorAlerts;
import slapp.editor.ExerciseHelpPopup;
import slapp.editor.derivation.der_systems.DerivationRule;
import slapp.editor.derivation.der_systems.DisjunctionExploit;
import slapp.editor.parser.*;
import slapp.editor.parser.grammatical_parts.*;
import slapp.editor.parser.symbols.MFormulaSym;
import slapp.editor.parser.symbols.MTermSym;
import slapp.editor.parser.symbols.MVariable;
import slapp.editor.parser.symbols.VariableSym;

import java.util.*;
import java.util.logging.Handler;

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
    private double windowOffset;
    private double offsetIncrement = 25.0;
    public List<Stage> helpStages = new ArrayList<>();
    private List<Text> footnotes = new ArrayList<>();
    private String disjunctionStatus;

    private List<Term> termList = new ArrayList<>();
    private List<Term> formulaVars = new ArrayList<>();




    public DerivationHelp(DerivationExercise derivationExercise) {
        this.derivationExercise = derivationExercise;
        this.derivationView = derivationExercise.getExerciseView();
        this.derivationModel= derivationExercise.getExerciseModel();
        this.derivationCheck = derivationExercise.getDerivationCheck();


        derivationView.getHelpButton().setOnAction(e -> {
            boolean helpShowing = false;
            for (Stage stage : helpStages) { if (stage != null && stage.isShowing()) helpShowing = true; }
            closeHelpWindows();
            if (helpShowing) {
                try {Thread.sleep(250);}
                catch (InterruptedException intexception) { }
            }
            runHelp();
        });


    }

    public void closeHelpWindows() {
        for (Stage stage : helpStages) {
            if (stage != null) stage.close();
        }

    }

    private void runHelp() {


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
        if (!derivationExercise.getStringFromJustificationFlow(targetLine.getJustificationFlow()).equals("")) {
            EditorAlerts.fleetingRedPopup("Help applies lines that are not (yet) justified.");
            return;
        }

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



        if (goalIsContradiction) {
            footnotes.clear();
            footnotes.add(new Text("\n-----"));


            helpStages.clear();
            windowOffset = 0.0;

            setAccessibleFormulas();
            findTermsInAccessibleFormulas();
            setEObtainableFormulas();
            setEPotentialFormulas();

            //start check chain
            SC1a();



            for (int i = helpStages.size() - 1; i >= 0; i--) {
                Stage stage = helpStages.get(i);
                stage.show();
            }
        }
        else {
            footnotes.clear();
            footnotes.add(new Text("\n-----"));

            helpStages.clear();
            windowOffset = 0.0;

            setAccessibleFormulas();
            findTermsInAccessibleFormulas();
            setEObtainableFormulas();
            setEPotentialFormulas();

            System.out.println(eObtainableFormulas);

            //start check chain
            SG0a();



            for (int i = helpStages.size() - 1; i >= 0; i--) {
                Stage stage = helpStages.get(i);
                stage.show();
            }
        }

        derivationCheck.setHelpTries(derivationCheck.getHelpTries() + 1);
        derivationCheck.setHelpCounter();
        derivationExercise.getMainWindow().getMainView().getBorderPane().requestFocus();
    }




    private void SG0a() {
        //target in accesible
        if (formulaInList(targetFormula, accessibleFormulas)) {
            List texts = new ArrayList();
            texts.addAll(targetFormula.toTextList());
            texts.add(ParseUtilities.newRegularText(" exists on an accessible line.  Reiterate."));
            ExerciseHelpPopup popup = new ExerciseHelpPopup(texts, windowOffset);
            helpStage = popup.getHelpStage();
            helpStages.add(helpStage);
            return;
        }
        else SG0b();
    }

    private void SG0b() {
        //target in EObtainable
        if (formulaInList(targetFormula, eObtainableFormulas)) {
            List texts = new ArrayList();
            texts.addAll(targetFormula.toTextList());
            texts.add(ParseUtilities.newRegularText(" can be obtained (without subderivations) by exploitation rules.  Use E-rules to reach goal."));
            ExerciseHelpPopup popup = new ExerciseHelpPopup(texts, windowOffset);
            helpStage = popup.getHelpStage();
            helpStages.add(helpStage);
            return;
        }
        else SG1a();
    }

    private void SG1a() {
        //contradiction
        if (contradictionInList(accessibleFormulas)) {
            List<Text> texts = Collections.singletonList(ParseUtilities.newRegularText("There is a contradiction on accessible lines.  Set up to obtain goal by negation exploit and then use this contradiction."));
            ExerciseHelpPopup popup = new ExerciseHelpPopup(texts, windowOffset);
            helpStage = popup.getHelpStage();
            helpStages.add(helpStage);
            return;
        }
        else {
            SG1b();
        }
    }

    private void SG1b() {
        if (contradictionInList(eObtainableFormulas)) {
            List<Text> texts = Collections.singletonList(ParseUtilities.newRegularText("A contradiction can be obtained (without subderivations) from accessible lines by exploitation rules.  Set up a subderivation to reach goal by negation exploit, and use this contradiction."));
            ExerciseHelpPopup popup = new ExerciseHelpPopup(texts, windowOffset);
            helpStage = popup.getHelpStage();
            helpStages.add(helpStage);
            return;
        }
        else {
            if (helpStages.isEmpty()) {
                footnotes.add(ParseUtilities.newRegularText("\n\u2022 No contradiction from accessible lines."));
            }
            SG2a();
        }
    }

    private void SG2a() {
        //disjunction
        String lineLabel = availableDisjunction();
        if (!lineLabel.equals("0")) {
            List texts = new ArrayList();
            texts.add(ParseUtilities.newRegularText("There is an available disjunction on line "));
            texts.add(ParseUtilities.newRegularText(lineLabel));
            texts.add(ParseUtilities.newRegularText(". Set up with subderivations to obtain goal by disjunction exploit."));
            if (helpStages.isEmpty()) texts.addAll(footnotes);
            else windowOffset = windowOffset + offsetIncrement;
            ExerciseHelpPopup popup = new ExerciseHelpPopup(texts, windowOffset);
            helpStage = popup.getHelpStage();
            helpStages.add(helpStage);
            Operator mainOp = targetFormula.getMainOperator();
   //         if (mainOp != null && mainOp.getType() != ExpressionType.DISJ_OP && mainOp.getType() != ExpressionType.CONJ_OP)
            SG4();
            return;
        }
        else {
            SG2b();
        }
    }

    private void SG2b() {
        if (disjunctionInList(eObtainableFormulas, accessibleFormulas)) {
            List texts = new ArrayList();
            texts.add(ParseUtilities.newRegularText("A disjunction can be obtained (without subderivations) from accessible lines by exploitation rules.  Obtain the disjunction, and set up subderivations to obtain goal by disjunction exploit."));
            if (helpStages.isEmpty()) texts.addAll(footnotes);
            else windowOffset = windowOffset + offsetIncrement;
            ExerciseHelpPopup popup = new ExerciseHelpPopup(texts, windowOffset);
            helpStage = popup.getHelpStage();
            helpStages.add(helpStage);
            Operator mainOp = targetFormula.getMainOperator();
            SG4();
            return;
        }
        else {
            footnotes.add(ParseUtilities.newRegularText("\n\u2022 No disjunction from accessible line."));
       //         SG2c();
            SG3();
        }
    }

    private void SG2c() {
        if (disjunctionInList(ePotentialFormulas, accessibleFormulas)) {
            List texts = new ArrayList();
            texts.add(ParseUtilities.newRegularText("A disjunction appears \"in\" accessible lines.  Setting new goal(s) as necessary; consider an attempt to obtain the disjunction; and set up subderivations to obtain goal by disjunction exploit.\n\n" +
                    "*Weird but possible: if you get some \ud835\udc9c by goal \u212c, do not by this strategy get into a loop where you get \u212c by goal \ud835\udc9c, and then . . ."));
            if (helpStages.isEmpty()) texts.addAll(footnotes);
            else windowOffset = windowOffset + offsetIncrement;
            ExerciseHelpPopup popup = new ExerciseHelpPopup(texts, windowOffset);
            helpStage = popup.getHelpStage();
            helpStages.add(helpStage);
            Operator mainOp = targetFormula.getMainOperator();
            //          if (mainOp != null && mainOp.getType() != ExpressionType.DISJ_OP && mainOp.getType() != ExpressionType.CONJ_OP)
            SG5();
            return;
        }
        else {
      //      footnotes.add(ParseUtilities.newRegularText("\n\u2022 No disjunction from accessible lines."));
            SG5();
        }
    }

    private void SG3() {
        if (formulaInList(targetFormula, ePotentialFormulas)) {
            List texts = new ArrayList();
            texts.addAll(targetFormula.toTextList());
            texts.add(ParseUtilities.newRegularText(" appears \"in\" accessible lines.  Setting new goal(s) as necessary consider an attempt to exploit it out.\n\n" +
                    "*Weird but possible: if you get some \ud835\udc9c by goal \u212c, do not by this strategy get into a loop where you get \u212c by goal \ud835\udc9c, and then . . ."));
            if (helpStages.isEmpty()) texts.addAll(footnotes);
            else windowOffset = windowOffset + offsetIncrement;
            ExerciseHelpPopup popup = new ExerciseHelpPopup(texts, windowOffset);
            helpStage = popup.getHelpStage();
            helpStages.add(helpStage);
     //       SG4();
            SG4();
            return;
        }
        else {
            footnotes.add(ParseUtilities.newRegularText("\n\u2022 Goal not \"in\" accessible lines."));
            SG4();
    //        SG4();
        }
    }

    private void SG4() {
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
                if (helpStages.isEmpty()) texts.addAll(footnotes);
                else windowOffset = windowOffset + offsetIncrement;
                ExerciseHelpPopup popup = new ExerciseHelpPopup(texts, windowOffset);
                helpStage = popup.getHelpStage();
                helpStages.add(helpStage);
                return;
            }
            else if (mainOperator.getType() == ExpressionType.BICOND_OP) {
                List texts = new ArrayList();
                texts.add(ParseUtilities.newRegularText("Goal has has main operator "));
                texts.addAll(opTexts);
                texts.add(ParseUtilities.newRegularText(".  Set up subderivations to obtain goal by "));
                texts.addAll(opTexts2);
                texts.add(ParseUtilities.newRegularText("I."));
                if (helpStages.isEmpty()) texts.addAll(footnotes);
                else windowOffset = windowOffset + offsetIncrement;
                ExerciseHelpPopup popup = new ExerciseHelpPopup(texts, windowOffset);
                helpStage = popup.getHelpStage();
                helpStages.add(helpStage);
                return;
            }
            else if (mainOperator.getType() == ExpressionType.CONJ_OP) {
                List texts = new ArrayList();
                texts.add(ParseUtilities.newRegularText("Goal has has main operator "));
                texts.addAll(opTexts);
                texts.add(ParseUtilities.newRegularText(".  If you do not already have them, set the sides as new goals, for application of "));
                texts.addAll(opTexts2);
                texts.add(ParseUtilities.newRegularText("I."));
                if (helpStages.isEmpty()) texts.addAll(footnotes);
                else windowOffset = windowOffset + offsetIncrement;
                ExerciseHelpPopup popup = new ExerciseHelpPopup(texts, windowOffset);
                helpStage = popup.getHelpStage();
                helpStages.add(helpStage);
                return;
            }
            else if (mainOperator.getType() == ExpressionType.UNIVERSAL_OP) {
                List opSymTxts = ((UniversalOp) mainOperator).getMainSymbol().toTextList();
                List texts = new ArrayList();
                texts.add(ParseUtilities.newRegularText("Goal has has main operator "));
                texts.addAll(opTexts);
                texts.add(ParseUtilities.newRegularText(".  Set new goal for application of "));
                texts.addAll(opSymTxts);
                texts.add(ParseUtilities.newRegularText("I - usually with a new variable."));
                if (helpStages.isEmpty()) texts.addAll(footnotes);
                else windowOffset = windowOffset + offsetIncrement;
                ExerciseHelpPopup popup = new ExerciseHelpPopup(texts, windowOffset);
                helpStage = popup.getHelpStage();
                helpStages.add(helpStage);
                return;
            }
            else if (mainOperator.getType() == ExpressionType.UNIV_RESTRICTED_OP) {
                List opSymTxts2 = ((UnivRestrictedQuantOp) mainOperator).getUniversalOp().getMainSymbol().toTextList();
                List texts = new ArrayList();
                texts.add(ParseUtilities.newRegularText("Goal has has main operator "));
                texts.addAll(opTexts);
                texts.add(ParseUtilities.newRegularText(".  Set up subderivation for application of ("));
                texts.addAll(opSymTxts2);
                texts.add(ParseUtilities.newRegularText("I) - usually with a new variable."));
                if (helpStages.isEmpty()) texts.addAll(footnotes);
                else windowOffset = windowOffset + offsetIncrement;
                ExerciseHelpPopup popup = new ExerciseHelpPopup(texts, windowOffset);
                helpStage = popup.getHelpStage();
                helpStages.add(helpStage);
                return;
            }

            else if (mainOperator.getType() == ExpressionType.DISJ_OP) {
                List texts = new ArrayList();
                texts.add(ParseUtilities.newRegularText("Goal has has main operator "));
                texts.addAll(opTexts);
                texts.add(ParseUtilities.newRegularText(".  If you do not already have it, set one side or the other as new goal to use "));
                texts.addAll(opTexts2);
                texts.add(ParseUtilities.newRegularText("I."));
                if (helpStages.isEmpty()) texts.addAll(footnotes);
                else windowOffset = windowOffset + offsetIncrement;
                ExerciseHelpPopup popup = new ExerciseHelpPopup(texts, windowOffset);
                helpStage = popup.getHelpStage();
                helpStages.add(helpStage);
                SG5();
                return;
            }

        }
        else {
            footnotes.add(ParseUtilities.newRegularText("\n\u2022 No main operator."));
        //    SG5();
            SG2c();
        }
    }

    private void SG5() {
        List texts = new ArrayList();
        texts.add(ParseUtilities.newRegularText("Especially for an atomic or disjunction, set up to obtain goal by negation exploit."));
        if (helpStages.isEmpty()) texts.addAll(footnotes);
        else windowOffset = windowOffset + offsetIncrement;
        ExerciseHelpPopup popup = new ExerciseHelpPopup(texts, windowOffset);
        helpStage = popup.getHelpStage();
        helpStages.add(helpStage);
    }

    //strategies for contradiction

    private void SC1a() {
        if (contradictionInList(accessibleFormulas)) {
            List<Text> texts = Collections.singletonList(ParseUtilities.newRegularText("There is a contradiction on accessible lines.  Use this contradiction."));
            ExerciseHelpPopup popup = new ExerciseHelpPopup(texts, windowOffset);
            helpStage = popup.getHelpStage();
            helpStages.add(helpStage);
            return;
        }
        else SC1b();
    }

    private void SC1b() {
        if (contradictionInList(eObtainableFormulas)) {
            List<Text> texts = Collections.singletonList(ParseUtilities.newRegularText("A contradiction can be obtained (without subderivations) from accessible lines by exploitation rules.  Use this contradiction."));
            ExerciseHelpPopup popup = new ExerciseHelpPopup(texts, windowOffset);
            helpStage = popup.getHelpStage();
            helpStages.add(helpStage);
            return;
        }
        else {
       //     SC2a();
            SC1c();
        }
    }

    private void SC1c() {
        if (atomicContradictionInList(ePotentialFormulas)) {
            List<Text> texts = Collections.singletonList(ParseUtilities.newRegularText("There is a contradiction \"in\" accessible lines.  Setting new goal(s) as necessary, consider an attempt to exploit it out.\n\n" +
                    "*Weird but possible: if you get some \ud835\udc9c by goal \u212c, do not by this strategy get into a loop where you get \u212c by goal \ud835\udc9c, and then . . ."));
            ExerciseHelpPopup popup = new ExerciseHelpPopup(texts, windowOffset);
            helpStage = popup.getHelpStage();
            helpStages.add(helpStage);
            SC2a();
        }
        else {
            footnotes.add(ParseUtilities.newRegularText("\n\u2022 No contradiction from accessible lines."));
            SC2a();
        }
    }

    private void SC2a() {
        String lineLabel = availableDisjunction();
        if (!lineLabel.equals("0")) {
            List texts = new ArrayList();
            texts.add(ParseUtilities.newRegularText("There is an available disjunction on line "));
            texts.add(ParseUtilities.newRegularText(lineLabel));
            texts.add(ParseUtilities.newRegularText(". Set up with subderivations to obtain \u22a5 by disjunction exploit."));
            if (helpStages.isEmpty()) texts.addAll(footnotes);
            else windowOffset = windowOffset + offsetIncrement;
            ExerciseHelpPopup popup = new ExerciseHelpPopup(texts, windowOffset);
            helpStage = popup.getHelpStage();
            helpStages.add(helpStage);
            return;
        }
        else SC2b();
    }

    private void SC2b() {

        if (disjunctionInList(eObtainableFormulas, accessibleFormulas)) {
            List texts = new ArrayList();
            texts.add(ParseUtilities.newRegularText("A disjunction can be obtained (without subderivations) from accessible lines by exploitation rules.  Obtain the disjunction, and set up subderivations to obtain \u22a5 by disjunction exploit."));
            if (helpStages.isEmpty()) texts.addAll(footnotes);
            else windowOffset = windowOffset + offsetIncrement;
            ExerciseHelpPopup popup = new ExerciseHelpPopup(texts, windowOffset);
            helpStage = popup.getHelpStage();
            helpStages.add(helpStage);
            return;
        }
        else {
            footnotes.add(ParseUtilities.newRegularText("\n\u2022 No disjunction from accessible lines."));
        //    SC2c();
            SC3a();
        }
    }

    private void SC2c() {

        if (disjunctionInList(ePotentialFormulas, accessibleFormulas)) {
            List texts = new ArrayList();
            texts.add(ParseUtilities.newRegularText("A disjunction appears \"in\" accessible lines.  Setting new goal(s) as necessary consider an attempt to obtain the disjunction; then set up subderivations to obtain \u22a5 by disjunction exploit.\n\n" +
                    "*Weird but possible: if you get some \ud835\udc9c by goal \u212c, do not by this strategy get into a loop where you get \u212c by goal \ud835\udc9c, and then . . ."));
            if (helpStages.isEmpty()) texts.addAll(footnotes);
            else windowOffset = windowOffset + offsetIncrement;
            ExerciseHelpPopup popup = new ExerciseHelpPopup(texts, windowOffset);
            helpStage = popup.getHelpStage();
            helpStages.add(helpStage);
            SC3d();
        }
        else {
            footnotes.add(ParseUtilities.newRegularText("\n\u2022 No disjunction from accessible lines."));
            SC3d();
        }
    }

    private void SC3a() {
        String lineLabel = availableOrdinaryComplexNegation();
        if (!lineLabel.equals("0")) {
            List texts = new ArrayList();
            texts.add(ParseUtilities.newRegularText("There is an available complex negation \u223c\ud835\udcab on line "));
            texts.add(ParseUtilities.newRegularText(lineLabel));
            texts.add(ParseUtilities.newRegularText(".   Set its opposite \ud835\udcab as goal, and use the pair of them for contradiction."));
            if (helpStages.isEmpty()) texts.addAll(footnotes);
            else windowOffset = windowOffset + offsetIncrement;
            ExerciseHelpPopup popup = new ExerciseHelpPopup(texts, windowOffset);
            helpStage = popup.getHelpStage();
            helpStages.add(helpStage);
            return;
        }
        else SC3b();
    }

    private void SC3b() {
        String lineLabel = availableNegatedDisjunction();
        if (!lineLabel.equals("0")) {
            List texts = new ArrayList();
            texts.add(ParseUtilities.newRegularText("There is " + disjunctionStatus + " negated disjunction ∼(\uD835\uDCAB ∨ \uD835\uDCAC) on line "));
            texts.add(ParseUtilities.newRegularText(lineLabel));
            texts.add(ParseUtilities.newRegularText(".   Set its opposite \uD835\uDCAB ∨ \uD835\uDCAC as goal, and use the pair of them for contradiction."));
            if (helpStages.isEmpty()) texts.addAll(footnotes);
            else windowOffset = windowOffset + offsetIncrement;
            ExerciseHelpPopup popup = new ExerciseHelpPopup(texts, windowOffset);
            helpStage = popup.getHelpStage();
            helpStages.add(helpStage);
            return;
        }
        else SC3c();
    }

    private void SC3c() {
        if (complexNegationInList(eObtainableFormulas, accessibleFormulas)) {
            List texts = new ArrayList();
            texts.add(ParseUtilities.newRegularText("A complex negation \u223c\ud835\udcab can be obtained (without subderivations) from accessible lines by exploitation rules.  Obtain this negation; then set its opposite \ud835\udcab as a new goal -- and use them for contradiction."));
            if (helpStages.isEmpty()) texts.addAll(footnotes);
            else windowOffset = windowOffset + offsetIncrement;
            ExerciseHelpPopup popup = new ExerciseHelpPopup(texts, windowOffset);
            helpStage = popup.getHelpStage();
            helpStages.add(helpStage);
            return;
        }
        else {
        //   SC3d();
            SC2c();
        }
    }

    private void SC3d() {
        if (complexNegationInList(ePotentialFormulas, accessibleFormulas)) {
            List texts = new ArrayList();
            texts.add(ParseUtilities.newRegularText("A complex negation \u223c\ud835\udcab appears \"in\" accessible lines.   Setting new goal(s) as necessary consider an attempt to exploit it out.  Then set its opposite \uD835\uDCAB as a new goal, and then use the pair of them for contradiction.\n\n" +
                    "*But if you get some \ud835\udc9c by goal \u212c, do not by this strategy get into a loop where you get \u212c by goal \ud835\udc9c, and then . . . "));
            if (helpStages.isEmpty()) texts.addAll(footnotes);
            else windowOffset = windowOffset + offsetIncrement;
            ExerciseHelpPopup popup = new ExerciseHelpPopup(texts, windowOffset);
            helpStage = popup.getHelpStage();
            helpStages.add(helpStage);
            SC4();
        }
        else {
            footnotes.add(ParseUtilities.newRegularText("\n\u2022 No available complex negation."));
            SC4();
        }
    }

    private void SC4() {
        List texts = new ArrayList();
        texts.add(ParseUtilities.newRegularText("For some \ud835\udcab such that both \ud835\udcab and \u223c\ud835\udcab lead to contradiction: " +
                "Assume \ud835\udcab (\u223c\ud835\udcab), and obtain the first contradiction to conclude \u223c\ud835\udcab (\ud835\udcab); then from that obtain the second contradiction.\n\n  " +
                "Note: This is the hardest strategy to apply because it can be difficult to identify a \ud835\udcab to do the job.  The best you can do is look for it!"));
        if (helpStages.isEmpty()) texts.addAll(footnotes);
        else windowOffset = windowOffset + offsetIncrement;
        ExerciseHelpPopup popup = new ExerciseHelpPopup(texts, windowOffset);
        helpStage = popup.getHelpStage();
        helpStages.add(helpStage);
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

                //process universals
                Document univForm = new Document("\u2200\ud835\udccd\ud835\udcab");
                try {
                    MatchUtilities.clearFormMatch();
                    MVariable.clear();
                    MatchUtilities.simpleExpFormMatch(univForm, formula, objectLanguage.getNameString(), metaLanguage.getNameString());
                    Formula matchFormula = null;
                    Term quantifiedVariable = null;
                    for (MFormula mForm : MFormula.getmFormulas()) {
                        if (mForm.getFormulaSym().equals(new MFormulaSym("\ud835\udcab", ""))) {
                            matchFormula = mForm.getMatch();
                        }
                    }
                    for (MVariable mVariable : MVariable.getVariables()) {
                        if (mVariable.getBaseStr().equals("\ud835\udccd")) {
                            VariableSym variableSym = mVariable.getMatch();
                            Term term = new Term();
                            term.setLevel(0);
                            ArrayList children = new ArrayList();
                            children.add(variableSym);
                            term.setChildren(children);
                            term.setCombines(true);
                            term.setTermType(TermType.VARIABLE);
                            quantifiedVariable = term;
                        }
                    }
                    for (Term term : termList) {
                        Formula subForm = substituteForTermInFormula(matchFormula, quantifiedVariable, term);

                        if (!eObtainableFormulas.contains(subForm)) {
                            eObtainableFormulas.add(subForm);
                            changes = true;
                            continue whileLoop;
                        }
                    }
                }
                catch (TextMessageException e) {}
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

                //process universals
                Document univForm = new Document("\u2200\ud835\udccd\ud835\udcab");
                try {
                    MatchUtilities.clearFormMatch();
                    MVariable.clear();
                    MatchUtilities.simpleExpFormMatch(univForm, formula, objectLanguage.getNameString(), metaLanguage.getNameString());
                    Formula matchFormula = null;
                    Term quantifiedVariable = null;
                    for (MFormula mForm : MFormula.getmFormulas()) {
                        if (mForm.getFormulaSym().equals(new MFormulaSym("\ud835\udcab", ""))) {
                            matchFormula = mForm.getMatch();
                        }
                    }
                    for (MVariable mVariable : MVariable.getVariables()) {
                        if (mVariable.getBaseStr().equals("\ud835\udccd")) {
                            VariableSym variableSym = mVariable.getMatch();
                            Term term = new Term();
                            term.setLevel(0);
                            ArrayList children = new ArrayList();
                            children.add(variableSym);
                            term.setChildren(children);
                            term.setCombines(true);
                            term.setTermType(TermType.VARIABLE);
                            quantifiedVariable = term;
                        }
                    }
                    for (Term term : termList) {
                        Formula subForm = substituteForTermInFormula(matchFormula, quantifiedVariable, term);

                        if (!eObtainableFormulas.contains(subForm)) {
                            eObtainableFormulas.add(subForm);
                            changes = true;
                            continue whileLoop;
                        }
                    }
                }
                catch (TextMessageException e) {}
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

    //complex negation in formula list but not exclude list
    private boolean complexNegationInList(List<Formula> formulaList, List<Formula> excludeList) {
        Document negationForm = new Document("\u223c\ud835\udcac");
        for (Formula formula : formulaList) {
            MatchUtilities.clearFormMatch();
            try {
                MatchUtilities.simpleExpFormMatch(negationForm, formula, objectLanguage.getNameString(), metaLanguage.getNameString() );
                Formula matchForm = (Formula) ParseUtilities.parseDoc(negationForm, metaLanguage.getNameString()).get(0).getMatch();
                for (MFormula mForm : MFormula.getmFormulas()) {
                    if (mForm.getFormulaSym().equals(new MFormulaSym("\ud835\udcac", ""))) {
                        Formula immediateSub = mForm.getMatch();
                        if (!immediateSub.isAtomic() ) {
                            if (!excludeList.contains((Formula) matchForm)) return true;
                        }
                    }
                }
            } catch (TextMessageException e) {}
        }
        return false;
    }

    //complex negation (not disjunction) in formula list but not exclude list
    private boolean ordinaryComplexNegationInList(List<Formula> formulaList, List<Formula> excludeList) {
        Document negationForm = new Document("\u223c\ud835\udcac");
        for (Formula formula : formulaList) {
            MatchUtilities.clearFormMatch();
            try {
                MatchUtilities.simpleExpFormMatch(negationForm, formula, objectLanguage.getNameString(), metaLanguage.getNameString() );
                Formula matchForm = (Formula) ParseUtilities.parseDoc(negationForm, metaLanguage.getNameString()).get(0).getMatch();
                for (MFormula mForm : MFormula.getmFormulas()) {
                    if (mForm.getFormulaSym().equals(new MFormulaSym("\ud835\udcac", ""))) {
                        Formula immediateSub = mForm.getMatch();
                        if (!immediateSub.isAtomic() && immediateSub.getMainOperator().getType() != ExpressionType.DISJ_OP) {
                            if (!excludeList.contains((Formula) matchForm)) return true;
                        }
                    }
                }
            } catch (TextMessageException e) {}
        }
        return false;
    }

    private String availableOrdinaryComplexNegation() {
        String negLabelString = "0";
        for (ViewLine line : viewLines) {
            if (LineType.isContentLine(line.getLineType())) {
                if (derivationCheck.lineIsAccessibleTo(line, targetLine).getKey()) {
                    Formula lineFormula = getFormulaFromViewLine(line);
                    if (lineFormula != null) {
                        Document negationForm = new Document("\u223c\ud835\udcac");
                        MatchUtilities.clearFormMatch();
                        try {
                            MatchUtilities.simpleExpFormMatch(negationForm, lineFormula, objectLanguage.getNameString(), metaLanguage.getNameString());
                            Formula matchForm = (Formula) ParseUtilities.parseDoc(negationForm, metaLanguage.getNameString()).get(0).getMatch();
                            Formula immediateSub = null;
                            for (MFormula mForm : MFormula.getmFormulas()) {
                                if (mForm.getFormulaSym().equals(new MFormulaSym("\ud835\udcac", ""))) {
                                    immediateSub = mForm.getMatch();
                                    break;
                                }
                            }
                            if (!immediateSub.isAtomic() && immediateSub.getMainOperator().getType() != ExpressionType.DISJ_OP) {
                                String lineLabel = line.getLineNumberLabel().getText();
                                DerivationRule contradictionIntroRule = derivationCheck.getDerivationRuleset().getContradictionIntroRule();
                                boolean used = false;
                                for (ViewLine checkLine : viewLines) {
                                    if (LineType.isContentLine(checkLine.getLineType()) ) {
                                        String checkJustString = derivationExercise.getStringFromJustificationFlow(checkLine.getJustificationFlow());
                                        if (contradictionIntroRule.matches(checkJustString)) {
                                            List<String> checkLabels = derivationExercise.getLineLabelsFromJustificationFlow(checkLine.getJustificationFlow());
                                            String checkLabelString = checkLabels.get(0);
                                            if (checkLabels.get(0).equals(lineLabel) || checkLabels.get(1).equals(lineLabel) ) {
                                                if (targetLine.getAssumptionList().containsAll(checkLine.getAssumptionList()) || checkLine.getAssumptionList().containsAll(targetLine.getAssumptionList()) )
                                                used = true;
                                            }
                                        }
                                    }
                                }
                                if (!used) {
                                    negLabelString = lineLabel;
                                    break;
                                }
                            }
                        } catch (TextMessageException e) {}
                    }
                }
            }
        }
        return negLabelString;

    }

    private String availableNegatedDisjunction() {
        String negLabelString = "0";
        for (ViewLine line : viewLines) {
            if (LineType.isContentLine(line.getLineType())) {
                if (derivationCheck.lineIsAccessibleTo(line, targetLine).getKey()) {
                    Formula lineFormula = getFormulaFromViewLine(line);
                    if (lineFormula != null) {
                        Document negationForm = new Document("∼(\uD835\uDCAB ∨ \uD835\uDCAC)");
                        MatchUtilities.clearFormMatch();
                        try {
                            MatchUtilities.simpleExpFormMatch(negationForm, lineFormula, objectLanguage.getNameString(), metaLanguage.getNameString());
                            Formula matchForm = (Formula) ParseUtilities.parseDoc(negationForm, metaLanguage.getNameString()).get(0).getMatch();

                            String lineLabel = line.getLineNumberLabel().getText();
                            DerivationRule contradictionIntroRule = derivationCheck.getDerivationRuleset().getContradictionIntroRule();
                            int uses = 0;
                            for (ViewLine checkLine : viewLines) {
                                if (LineType.isContentLine(checkLine.getLineType())) {
                                    String checkJustString = derivationExercise.getStringFromJustificationFlow(checkLine.getJustificationFlow());
                                    if (contradictionIntroRule.matches(checkJustString)) {
                                        List<String> checkLabels = derivationExercise.getLineLabelsFromJustificationFlow(checkLine.getJustificationFlow());
                                        String checkLabelString = checkLabels.get(0);
                                        if (checkLabels.get(0).equals(lineLabel) || checkLabels.get(1).equals(lineLabel)) {
                                            if (targetLine.getAssumptionList().containsAll(checkLine.getAssumptionList()) || checkLine.getAssumptionList().containsAll(targetLine.getAssumptionList()) )
                                                uses += 1;
                                        }
                                    }
                                }
                            }
                            if (uses <= 1) {
                                if (uses == 0) disjunctionStatus = "an available";
                                if (uses == 1) disjunctionStatus = "a (still) available";
                                negLabelString = lineLabel;
                                break;
                            }
                        } catch (TextMessageException e) {}
                    }
                }
            }
        }
        return negLabelString;

    }

    private boolean negatedDisjunctionInList(List<Formula> formulaList, List<Formula> excludeList) {
        Document negatedDisjunctionForm = new Document("∼(\uD835\uDCAB ∨ \uD835\uDCAC)");
        for (Formula formula : formulaList) {
            MatchUtilities.clearFormMatch();
            try {
                MatchUtilities.simpleExpFormMatch(negatedDisjunctionForm, formula, objectLanguage.getNameString(), metaLanguage.getNameString());
                Expression matchExp = ParseUtilities.parseDoc(negatedDisjunctionForm, metaLanguage.getNameString()).get(0).getMatch();
                if (!excludeList.contains((Formula) matchExp)) return true;
                //    return true;
            } catch (TextMessageException e) {}
        }
        return false;
    }


    private boolean disjunctionInList(List<Formula> formulaList, List<Formula> excludeList) {
        Document disjunctionForm = new Document("(\ud835\udcab \u2228 \ud835\udcac)");
        for (Formula formula : formulaList) {
            MatchUtilities.clearFormMatch();
            try {
                MatchUtilities.simpleExpFormMatch(disjunctionForm, formula, objectLanguage.getNameString(), metaLanguage.getNameString());
                Expression matchExp = ParseUtilities.parseDoc(disjunctionForm, metaLanguage.getNameString()).get(0).getMatch();
                if (!excludeList.contains((Formula) matchExp)) return true;

            //    return true;
            } catch (TextMessageException e) {}
        }
        return false;
    }

    //list assumed to be accessible formulas
    private String availableDisjunction() {
        String disjLabelString = "0";
        for (ViewLine line : viewLines) {
            if (LineType.isContentLine(line.getLineType())) {
                if (derivationCheck.lineIsAccessibleTo(line, targetLine).getKey()) {
                    Formula lineFormula = getFormulaFromViewLine(line);
                    if (lineFormula != null) {
                        Document disjunctionForm = new Document("(\ud835\udcab \u2228 \ud835\udcac)");
                        MatchUtilities.clearFormMatch();
                        try {
                            MatchUtilities.simpleExpFormMatch(disjunctionForm, lineFormula, objectLanguage.getNameString(), metaLanguage.getNameString());
                            String justificationString = derivationExercise.getStringFromJustificationFlow(line.getJustificationFlow());
                            DerivationRule disjIntroRule = derivationCheck.getDerivationRuleset().getDisjunctionIntroRule();
                            if (!disjIntroRule.matches(justificationString)) {

                                String lineLabel = line.getLineNumberLabel().getText();
                                DerivationRule disjExpRule = derivationCheck.getDerivationRuleset().getDisjunctionExploitRule();
                                boolean used = false;
                                for (ViewLine checkLine : viewLines) {
                                    if (LineType.isContentLine(checkLine.getLineType())) {
                                        String checkJustString = derivationExercise.getStringFromJustificationFlow(checkLine.getJustificationFlow());
                                        if (disjExpRule.matches(checkJustString)) {
                                            List<String> checkLabels = derivationExercise.getLineLabelsFromJustificationFlow(checkLine.getJustificationFlow());
                                            String checkLabelString = checkLabels.get(0);
                                            if (checkLabelString.equals(lineLabel)) {
                                                if (targetLine.getAssumptionList().containsAll(checkLine.getAssumptionList()) || checkLine.getAssumptionList().containsAll(targetLine.getAssumptionList()))
                                                    used = true;
                                            }
                                        }
                                    }
                                }
                                if (!used) {
                                    disjLabelString = lineLabel;
                                    break;
                                }
                            }

                        } catch (TextMessageException e) {}
                    }
                }
            }
        }
        return disjLabelString;
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

    private boolean atomicContradictionInList(List<Formula> formulaList) {
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
                    Formula contForm = null;
                    for (MFormula mForm : MFormula.getmFormulas()) {
                        if (mForm.getFormulaSym().equals(new MFormulaSym("\ud835\udcac", ""))) {
                            contForm = mForm.getMatch();
                            break;
                        }
                    }
                    if (contForm.isAtomic()) return true;
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


    private void findTermsInAccessibleFormulas() {
        termList.clear();
        for (Formula formula : accessibleFormulas) {
            addTermsToList(formula);
        }
    }

    private void addTermsToList(Expression exp) {
        if (exp instanceof Term && !termList.contains(exp)) termList.add((Term) exp);

        if (exp.getChildren() != null && exp.getLevel() > 0) {
            for (int i = 0; i < exp.getChildren().size(); i++) {
                addTermsToList(exp.getChildren().get(i));
            }
        }
    }

    private void addVarsToList(Expression exp, Term filterVar) {

        if (exp instanceof Term && ((Term) exp).getTermType() == TermType.VARIABLE && ((Term) exp).equals(filterVar)) formulaVars.add((Term) exp);

        if (exp.getChildren() != null && exp.getLevel() > 0) {
            for (int i = 0; i < exp.getChildren().size(); i++) {
                addVarsToList(exp.getChildren().get(i), filterVar);
            }
        }
    }

    private Formula substituteForTermInFormula(Formula formula, Term replacedTerm, Term replacingTerm) {
        Formula resultFormula = formula;
        formulaVars.clear();
        addVarsToList(resultFormula, replacedTerm);

        List<Expression> goodVars = new ArrayList<>();
        for (Term var : formulaVars) {
            if (SyntacticalFns.freeForExp(resultFormula, var, replacingTerm, objectLanguage.getNameString()))
                goodVars.add(var);
        }
        if (!goodVars.isEmpty()) resultFormula = (Formula) SyntacticalFns.substituteParticularTerms(resultFormula, replacingTerm, goodVars);

        return resultFormula;
    }










}

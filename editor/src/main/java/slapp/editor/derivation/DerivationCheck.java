package slapp.editor.derivation;

import com.gluonhq.richtextarea.RichTextArea;
import com.gluonhq.richtextarea.model.Document;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.text.*;
import javafx.stage.Stage;
import javafx.util.Pair;
import slapp.editor.EditorAlerts;
import slapp.editor.decorated_rta.BoxedDRTA;
import slapp.editor.derivation.der_systems.DerivationRule;
import slapp.editor.derivation.der_systems.DerivationRuleset;
import slapp.editor.derivation.der_systems.DerivationRulesets;
import slapp.editor.derivation.theorems.Theorem;
import slapp.editor.main_window.Exercise;
import slapp.editor.parser.Expression;
import slapp.editor.parser.Languages;
import slapp.editor.parser.MatchUtilities;
import slapp.editor.parser.ParseUtilities;
import slapp.editor.parser.grammatical_parts.Formula;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javafx.event.EventHandler;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;


public class DerivationCheck implements DCheck {

    private DerivationExercise derivationExercise;
    private DerivationView derivationView;
    private DerivationModel derivationModel;
    private DerivationRuleset derivationRuleset;
    private ViewLine markedLine;
    private List<ViewLine> viewLines;

    private int checkMax;
    private int checkTries;
    private int helpMax;
    private int helpTries;
    private boolean checkFinal;
    private boolean checkSuccess;

    // SCORCHED EARTH RULE 2: Kill Switch
    private boolean isTornDown = false;

    // SCORCHED EARTH RULE 3: Eradicate Anonymous Lambdas
    private EventHandler<ActionEvent> checkButtonHandler;
    private EventHandler<ActionEvent> checkProgButtonHandler;
    private EventHandler<ActionEvent> staticHelpButtonHandler;

    DerivationCheck(DerivationExercise derivationExercise) {
        this.derivationExercise = derivationExercise;
        this.derivationView = derivationExercise.getExerciseView();
        this.derivationModel = derivationExercise.getExerciseModel();

        setRightControlBox();

        CheckSetup checkSetup = derivationExercise.getExerciseModel().getCheckSetup();
        if (checkSetup == null) {checkSetup = new CheckSetup();}

        checkSuccess = checkSetup.isCheckSuccess();
        checkFinal = checkSetup.isCheckFinal();
        if (checkFinal) {
            derivationView.setCheckColor(Color.LAWNGREEN);
            derivationView.setCheckElementsColor(Color.GREEN);
            derivationView.setCheckMessage("Derivation");
        }
        else {
            derivationView.setCheckColor(Color.ORCHID);
            derivationView.setCheckElementsColor(Color.PURPLE);
            derivationView.setCheckMessage("Justified Lines");
        }
        if (checkSuccess) derivationView.activateBigCheck();
        else derivationView.deactivateBigCheck();

        derivationRuleset = DerivationRulesets.getRuleset(checkSetup.getRulesetName());
        derivationRuleset.setMetaLanguage(Languages.getLanguage(checkSetup.getMetLangName()));
        derivationRuleset.setObjectLanguage(Languages.getLanguage(checkSetup.getObjLangName()));

        checkMax = checkSetup.getCheckMax();
        checkTries = checkSetup.getCheckTries();
        helpMax = checkSetup.getHelpMax();
        helpTries = checkSetup.getHelpTries();
        updateCounters();
    }

    public String getContradictionSymbolString() {
        if (isTornDown || derivationRuleset == null || derivationRuleset.getObjectLanguage() == null) return "";
        return derivationRuleset.getObjectLanguage().getContradictionSymbol().toString();
    }

    public void updateCounters() {
        if (isTornDown) return;
        setChecksCounter();
        setHelpCounter();
    }

    private void setChecksCounter() {
        if (isTornDown || derivationExercise == null || derivationExercise.getMainWindow() == null || derivationView == null) return;
        if (checkMax != -1 && checkTries >= checkMax && !derivationExercise.getMainWindow().isInstructorFunctions()) {
            if (derivationView.getCheckButton() != null) derivationView.getCheckButton().setDisable(true);
            if (derivationView.getCheckProgButton() != null) derivationView.getCheckProgButton().setDisable(true);
        }
        String checkString;
        if (checkMax == -1) checkString = "(unlimited)";
        else if (checkMax == 0) checkString = "(none)";
        else checkString = "(" + String.valueOf(checkTries) + "/" + String.valueOf(checkMax) + ")";

        if (derivationView.getCheckTriesLabel() != null) derivationView.getCheckTriesLabel().setText(checkString);
    }

    public void setHelpCounter() {
        if (isTornDown || derivationExercise == null || derivationExercise.getMainWindow() == null || derivationRuleset == null || derivationModel == null || derivationView == null) return;
        if ((helpMax != -1 && helpTries >= helpMax && !derivationExercise.getMainWindow().isInstructorFunctions()) || !derivationRuleset.isContextualHelpCompatible() ||
                !derivationModel.getCheckSetup().getTheoremSets().isEmpty() ) {
            if (derivationView.getHelpButton() != null) derivationView.getHelpButton().setDisable(true);
        }

        String helpString;
        if (helpMax == -1) helpString = "(unlimited)";
        else if (helpMax == 0) helpString = "(none)";
        else helpString = "(" + String.valueOf(helpTries) + "/" + String.valueOf(helpMax) + ")";

        if (derivationView.getHelpTriesLabel() != null) derivationView.getHelpTriesLabel().setText(helpString);
    }

    private void setRightControlBox() {
        if (derivationView == null) return;

        checkButtonHandler = e -> {
            if (isTornDown || derivationView == null) return;
            checkFinal = true;
            derivationView.setCheckColor(Color.LAWNGREEN);
            derivationView.setCheckMessage("Derivation");
            derivationView.setCheckElementsColor(Color.GREEN);
            runCheck();
        };
        if (derivationView.getCheckButton() != null) derivationView.getCheckButton().setOnAction(checkButtonHandler);

        checkProgButtonHandler = e -> {
            if (isTornDown || derivationView == null) return;
            checkFinal = false;
            derivationView.setCheckColor(Color.ORCHID);
            derivationView.setCheckMessage("Justified Lines");
            derivationView.setCheckElementsColor(Color.PURPLE);
            runCheck();
        };
        if (derivationView.getCheckProgButton() != null) derivationView.getCheckProgButton().setOnAction(checkProgButtonHandler);

        if (derivationView.getStaticHelpButton() != null && derivationModel != null && derivationModel.getCheckSetup() != null) {
            derivationView.getStaticHelpButton().setDisable(!derivationModel.getCheckSetup().isStaticHelpButton());

            staticHelpButtonHandler = e -> {
                if (isTornDown || derivationView == null || derivationModel == null || derivationModel.getCheckSetup() == null) return;
                Stage helpStage = derivationView.getStaticHelpStage();
                if (helpStage != null && helpStage.isShowing()) helpStage.close();
                else derivationView.showStaticHelp(derivationModel.getCheckSetup().getStaticHelpDoc());
            };
            derivationView.getStaticHelpButton().setOnAction(staticHelpButtonHandler);
        }
    }

    private boolean runCheck() {
        if (isTornDown || derivationExercise == null || derivationExercise.getMainWindow() == null || derivationView == null) return false;
        checkSuccess = false;
        derivationView.deactivateBigCheck();
        if (!derivationExercise.getMainWindow().isInstructorFunctions()) {
            checkTries++;
            setChecksCounter();
        }
        if (derivationExercise.getDerivationHelp() != null) {
            derivationExercise.getDerivationHelp().closeHelpWindows();
        }

        viewLines = derivationView.getViewLines();
        if (viewLines == null) return false;

        if (!checkFormulas()) return false;
        if (!checkScopeStructure()) return false;
        if (!checkAssumptionScopes()) return false;
        if (!checkJustificationForms()) return false;
        setScopeLists();
        if (!checkJustifications()) return false;

        checkSuccess = true;
        derivationView.activateBigCheck();

        return true;
    }

    public boolean backgroundProgressCheck() {
        if (isTornDown || derivationView == null) return false;
        checkFinal = false;

        viewLines = derivationView.getViewLines();
        if (viewLines == null) return false;

        if (!checkFormulas()) return false;
        if (!checkScopeStructure()) return false;
        if (!checkAssumptionScopes()) return false;
        if (!checkJustificationForms()) return false;
        setScopeLists();
        if (!checkJustifications()) return false;

        return true;
    }

    private boolean checkJustifications() {
        if (isTornDown || viewLines == null || derivationRuleset == null || derivationExercise == null) return false;

        for (int i = 0; i < viewLines.size(); i++) {
            ViewLine viewLine = viewLines.get(i);
            if (LineType.isContentLine(viewLine.getLineType())) {
                TextFlow justificationFlow = viewLine.getJustificationFlow();
                String justificationString = derivationExercise.getStringFromJustificationFlow(justificationFlow);

                if (derivationRuleset.getRules() != null) {
                    for (DerivationRule rule : derivationRuleset.getRules()) {
                        if (rule.matches(justificationString)) {
                            List<String> lineLabels = derivationExercise.getLineLabelsFromJustificationFlow(justificationFlow);
                            String[] labelArray = new String[lineLabels.size()];
                            lineLabels.toArray(labelArray);
                            Pair<Boolean, List<Text>> resultPair = rule.applies(this, viewLines.get(i), labelArray);
                            if (!resultPair.getKey()) {
                                highlightLine(i);
                                EditorAlerts.showSimpleTxtListAlert("Justification Issue:", resultPair.getValue());
                                resetHighlights();
                                return false;
                            }
                            break;
                        }
                    }
                }

                if (derivationExercise.getTheorems() != null) {
                    for (Theorem theorem : derivationExercise.getTheorems()) {
                        if (theorem.matches(justificationString)) {
                            List<String> lineLabels = derivationExercise.getLineLabelsFromJustificationFlow(justificationFlow);
                            String[] labelArray = new String[lineLabels.size()];
                            lineLabels.toArray(labelArray);
                            Pair<Boolean, List<Text>> resultPair = theorem.applies(this, viewLines.get(i), labelArray);
                            if (!resultPair.getKey()) {
                                highlightLine(i);
                                EditorAlerts.showSimpleTxtListAlert("Justification Issue:", resultPair.getValue());
                                resetHighlights();
                                return false;
                            }
                            break;
                        }
                    }
                }
            }
        }

        return true;
    }

    public Pair<ViewLine, List<Text>> getLineFromLabel(String label) {
        ViewLine line = null;
        if (viewLines != null) {
            for (ViewLine viewLine : viewLines) {
                if (LineType.isContentLine(viewLine.getLineType()) && viewLine.getLineNumberLabel() != null && viewLine.getLineNumberLabel().getText().equals(label)) {
                    line = viewLine;
                    break;
                }
            }
        }
        List<Text> texts = new ArrayList<>();
        if (line == null) texts.add(ParseUtilities.newRegularText("Line (" + label + ") not found in this derivation."));
        return new Pair<>(line, texts);
    }

    //requires justifier and candidate are content lines
    public Pair<Boolean, List<Text>> lineIsAccessibleTo(ViewLine justifier, ViewLine candidate) {
        if (isTornDown || justifier == null || candidate == null || justifier.getLineNumberLabel() == null || candidate.getLineNumberLabel() == null || viewLines == null) return new Pair<>(false, null);

        String justifierLabel = justifier.getLineNumberLabel().getText();
        String candidateLabel = candidate.getLineNumberLabel().getText();
        if (viewLines.indexOf(candidate) <= viewLines.indexOf(justifier)) {
            return new Pair<>(false, Collections.singletonList(ParseUtilities.newRegularText("Line " + justifierLabel + " (does not come before and) is not available for justification of line " + candidateLabel + ".")));
        }
        List<String> justifierAssps = justifier.getAssumptionList();
        List<String> candidateAssps = candidate.getAssumptionList();
        if (justifierAssps != null && candidateAssps != null) {
            for (String labelStr : justifierAssps) {
                if (!candidateAssps.contains(labelStr)) {
                    return new Pair<>(false, Collections.singletonList(ParseUtilities.newRegularText("Line " + justifierLabel + " is not accessible for justification of line " + candidateLabel + ".")));
                }
            }
        }
        return new Pair<>(true, null);
    }

    //requires start line is content line
    public ViewLine lastLineAtScope(ViewLine startLine) {
        if (isTornDown || viewLines == null || startLine == null) return startLine;

        int index = viewLines.indexOf(startLine);
        int depth = startLine.realDepth();
        ViewLine lastCandidate = startLine;
        if (index >= 0) {
            for (int i = index; i < viewLines.size(); i++) {
                ViewLine viewLine = viewLines.get(i);
                if (viewLine != null && viewLine.realDepth() >= depth) {
                    if (LineType.isContentLine(viewLine.getLineType())) lastCandidate = viewLine;
                }
                else break;
            }
        }
        return lastCandidate;
    }

    public Pair<Boolean, List<Text>> isSubderivation(String label1, String label2) {
        if (isTornDown || derivationRuleset == null || derivationExercise == null) return new Pair<>(false, null);

        ViewLine startLine;
        Pair<ViewLine, List<Text>> startLinePair = getLineFromLabel(label1);
        if (startLinePair.getKey() != null) startLine = startLinePair.getKey();
        else return new Pair<>(false, startLinePair.getValue());

        TextFlow startJustificationFlow = startLine.getJustificationFlow();
        String startJustificationString = derivationExercise.getStringFromJustificationFlow(startJustificationFlow);

        if (derivationRuleset.getGenericAssumption() != null) {
            Matcher matcher = derivationRuleset.getGenericAssumption().matcher(startJustificationString);
            if (!matcher.matches()) {
                return new Pair<>(false, Collections.singletonList(ParseUtilities.newRegularText("A subderivation " + label1 + "-" + label2 + " must start with an assumption.")));
            }
        }

        ViewLine lastLine = lastLineAtScope(startLine);
        if (lastLine == null || lastLine.getLineNumberLabel() == null || !lastLine.getLineNumberLabel().getText().equals(label2)) {
            return new Pair<>(false, Collections.singletonList(ParseUtilities.newRegularText(label2 + " does not identify the last line of the subderivation beginning at " + label1 + ".")));
        }
        return new Pair<>(true, null);
    }

    public Pair<Boolean, List<Text>> isAccessibleSubderivationFor(ViewLine line, String label1, String label2) {
        if (isTornDown || line == null || line.getLineNumberLabel() == null || viewLines == null) return new Pair<>(false, null);

        Pair<Boolean, List<Text>> subderResult = isSubderivation(label1, label2);
        if (!subderResult.getKey()) {
            return subderResult;
        }

        ViewLine firstLine;
        Pair<ViewLine, List<Text>> firstLinePair = getLineFromLabel(label1);
        if (firstLinePair.getKey() != null) firstLine = firstLinePair.getKey();
        else return new Pair<>(false, firstLinePair.getValue());

        ViewLine lastLine;
        Pair<ViewLine, List<Text>> lastLinePair = getLineFromLabel(label2);
        if (lastLinePair.getKey() != null) lastLine = lastLinePair.getKey();
        else return new Pair<>(false, lastLinePair.getValue());

        String candidateLabel = line.getLineNumberLabel().getText();
        if (viewLines.indexOf(line) <= viewLines.indexOf(firstLine)) {
            return new Pair<>(false, Collections.singletonList(ParseUtilities.newRegularText("Subderivation must be complete (and assumption discharged) before any appeal to it.")));
        }

        if (viewLines.indexOf(line) <= viewLines.indexOf(lastLine)) {
            return new Pair<>(false, Collections.singletonList(ParseUtilities.newRegularText("Assumption (" + label1 + ") is not discharged at (" + candidateLabel + ") and so (" + label1 + ")-(" + label2 +") is not accessible to (" + candidateLabel + ").")));
        }

        List<String> lastLineAssps = lastLine.getAssumptionList();
        List<String> candidateAssps = line.getAssumptionList();
        if (lastLineAssps != null && candidateAssps != null) {
            for (int i = 0; i < lastLineAssps.size() - 1; i++) {
                String labelStr = lastLineAssps.get(i);
                if (!candidateAssps.contains(labelStr)) {
                    return new Pair<>(false, Collections.singletonList(ParseUtilities.newRegularText("Subderivation " + label1 + "-" + label2 + " is not accessible for the justification of (" + line.getLineNumberLabel().getText() + ").")));
                }
            }
        }
        return new Pair<>(true, null);
    }

    private void setScopeLists() {
        if (isTornDown || viewLines == null || derivationRuleset == null || derivationExercise == null) return;

        ArrayList<String> premiseList = new ArrayList<>();
        ArrayList<String> assumptionList = new ArrayList<>();
        int currentDepth = 1;

        for (int i = 0; i < viewLines.size(); i++) {
            ViewLine viewLine = viewLines.get(i);
            if (viewLine.realDepth() == currentDepth) {
                if (LineType.isContentLine(viewLine.getLineType())) {
                    TextFlow justificationFlow = viewLine.getJustificationFlow();
                    String justificationString = derivationExercise.getStringFromJustificationFlow(justificationFlow);
                    if (derivationRuleset.getPremiseRule() != null && derivationRuleset.getPremiseRule().matches(justificationString)) {
                        if (viewLine.getLineNumberLabel() != null) premiseList.add(viewLine.getLineNumberLabel().getText());
                    }
                }
            }
            else {
                if (LineType.isContentLine(viewLine.getLineType()) && viewLine.realDepth() == currentDepth + 1) {
                    if (viewLine.getLineNumberLabel() != null) assumptionList.add(viewLine.getLineNumberLabel().getText());
                    currentDepth++;
                }
                else if (viewLine.realDepth() == currentDepth - 1) {
                    if (!assumptionList.isEmpty()) assumptionList.remove(assumptionList.size() - 1);
                    currentDepth--;
                }
            }
            ArrayList<String> newList = new ArrayList<>(premiseList);
            newList.addAll(assumptionList);
            viewLine.setAssumptionList(newList);
        }
    }

    private boolean checkAssumptionScopes() {
        if (isTornDown || viewLines == null || derivationRuleset == null || derivationExercise == null) return false;

        int currentDepth = 1;
        for (int i = 0; i < viewLines.size(); i++) {
            ViewLine viewLine = viewLines.get(i);
            String justificationString = "";
            if (LineType.isContentLine(viewLine.getLineType())) {
                TextFlow justificationFlow = viewLine.getJustificationFlow();
                justificationString = derivationExercise.getStringFromJustificationFlow(justificationFlow);
            }

            if (derivationRuleset.getGenericAssumption() != null) {
                Matcher matcher = derivationRuleset.getGenericAssumption().matcher(justificationString);
                if (viewLine.realDepth() == currentDepth + 1 && !matcher.matches()) {
                    highlightLine(i);
                    EditorAlerts.showSimpleTxtListAlert("Scope Increase:", Collections.singletonList(ParseUtilities.newRegularText("An increase in scope should be associated with a justification by assumption.")));
                    resetHighlights();
                    return false;
                }
                else if (viewLine.realDepth() != currentDepth + 1 && matcher.matches()) {
                    highlightLine(i);
                    EditorAlerts.showSimpleTxtListAlert("Assumption Scope:", Collections.singletonList(ParseUtilities.newRegularText("A justification by assumption should be associated with a scope increase.")));
                    resetHighlights();
                    return false;
                }
                else{
                    currentDepth = viewLine.realDepth();
                }
            } else {
                currentDepth = viewLine.realDepth();
            }
        }
        return true;
    }

    private boolean checkJustificationForms() {
        if (isTornDown || viewLines == null || derivationRuleset == null || derivationExercise == null) return false;

        for (int i = 0; i < viewLines.size(); i++) {
            ViewLine viewLine = viewLines.get(i);
            if (LineType.isContentLine(viewLine.getLineType())) {
                BoxedDRTA bdrta = viewLine.getLineContentBoxedDRTA();
                if (bdrta == null || bdrta.getRTA() == null) continue;
                RichTextArea rta = bdrta.getRTA();
                rta.getActionFactory().saveNow().execute(new ActionEvent());
                Document lineDoc = rta.getDocument();
                TextFlow justificationFlow = viewLine.getJustificationFlow();
                String justificationString = derivationExercise.getStringFromJustificationFlow(justificationFlow);

                if (lineDoc != null && lineDoc.getText() != null && lineDoc.getText().equals("") && !justificationString.equals("")) {
                    highlightJustification(i);
                    EditorAlerts.showSimpleTxtListAlert("Missing Formula", Collections.singletonList(ParseUtilities.newRegularText("No formula to which justification applies.")));
                    resetHighlights();
                    return false;
                }

                if (lineDoc != null && lineDoc.getText() != null && !lineDoc.getText().equals("")) {
                    boolean blankOK = false;

                    ViewLine priorLine = derivationExercise.getContentLineAbove(i);
                    if (priorLine != null && !priorLine.equals(viewLine)) {
                        TextFlow priorJustificationFlow = priorLine.getJustificationFlow();
                        String priorJustificationString = derivationExercise.getStringFromJustificationFlow(priorJustificationFlow);
                        if ((derivationRuleset.getAsspRestrictedExisExploitCRule() != null && derivationRuleset.getAsspRestrictedExisExploitCRule().matches(priorJustificationString))
                                || (derivationRuleset.getAsspRestrictedExisExploitGRule() != null && derivationRuleset.getAsspRestrictedExisExploitGRule().matches(priorJustificationString))) {
                            blankOK = true;
                        }
                    }

                    if (justificationString.equals("")) {
                        if (blankOK) break;
                        if (checkFinal) {
                            highlightJustification(i);
                            EditorAlerts.showSimpleTxtListAlert("Missing Justification:", Collections.singletonList(ParseUtilities.newRegularText("Line requires justification.")));
                            resetHighlights();
                            return false;
                        }
                    } else {
                        boolean ok = false;
                        if (derivationRuleset.getRules() != null) {
                            for (DerivationRule rule : derivationRuleset.getRules()) {
                                if (rule.matches(justificationString)) {
                                    ok = true;
                                    break;
                                }
                            }
                        }

                        if (derivationExercise.getTheorems() != null) {
                            for (Theorem theorem : derivationExercise.getTheorems()) {
                                if (theorem.matches(justificationString)) {
                                    ok = true;
                                    break;
                                }
                            }
                        }

                        if (!ok) {
                            if (derivationRuleset.getDummyRules() != null) {
                                for (Pair<Pattern, String> dummyPair : derivationRuleset.getDummyRules()) {
                                    if (dummyPair.getKey() != null) {
                                        Matcher matcher = dummyPair.getKey().matcher(justificationString);
                                        if (matcher.matches()) {
                                            highlightJustification(i);
                                            EditorAlerts.showSimpleTxtListAlert("Justification Error:", Collections.singletonList(ParseUtilities.newRegularText(dummyPair.getValue())));
                                            resetHighlights();
                                            return false;
                                        }
                                    }
                                }
                            }

                            String message = "I do not recognize this as a(n) ";
                            if (derivationExercise.getTheorems() != null && !derivationExercise.getTheorems().isEmpty()) message = "I do not recognize this as an (available) ";
                            highlightJustification(i);
                            List<Text> texts = new ArrayList<>();

                            texts.add(new Text(message));
                            if (derivationRuleset.getTextName() != null) texts.addAll(derivationRuleset.getTextName());
                            texts.add(new Text(" justification."));
                            EditorAlerts.showSimpleTxtListAlert("Justification Issue:", texts);
                            resetHighlights();
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }

    private boolean checkFormulas() {
        if (isTornDown || viewLines == null || derivationRuleset == null || derivationRuleset.getObjectLanguage() == null || derivationRuleset.getObjectLanguage().getNameString() == null) return false;

        for (int i = 0; i < viewLines.size(); i++) {
            ViewLine viewLine = viewLines.get(i);
            if (LineType.isContentLine(viewLine.getLineType())) {
                BoxedDRTA bdrta = viewLine.getLineContentBoxedDRTA();
                if (bdrta == null || bdrta.getRTA() == null) continue;
                RichTextArea rta = bdrta.getRTA();
                rta.getActionFactory().saveNow().execute(new ActionEvent());
                Document lineDoc = rta.getDocument();

                if (lineDoc != null && lineDoc.getText() != null && !lineDoc.getText().equals("")) {
                    List<Expression> parseExpressions = ParseUtilities.parseDoc(lineDoc, derivationRuleset.getObjectLanguage().getNameString());
                    if (parseExpressions != null && parseExpressions.size() == 1 && parseExpressions.get(0) instanceof Formula) {
                        if (ParseUtilities.getAlternateMessage() != null && !ParseUtilities.getAlternateMessage().isEmpty()) {
                            highlightFormula(i);
                            EditorAlerts.showSimpleTxtListAlert("Problem in bounded quantifier:", ParseUtilities.getAlternateMessage());
                            resetHighlights();
                            return false;
                        }
                        continue;
                    }
                    else {
                        List<Text> texts = new ArrayList<>();
                        Text leadText = new Text("Parse results:\n");
                        leadText.setFont(Font.font("Noto Serif Combo", FontPosture.ITALIC, 11));
                        texts.add(leadText);

                        if (parseExpressions != null) {
                            for (Expression expr : parseExpressions) {
                                texts.add(new Text("    "));
                                texts.add(new Text(expr.getType() + ": "));
                                texts.addAll(expr.toTextList());
                                texts.add(new Text("\n"));
                            }
                        }
                        highlightFormula(i);
                        EditorAlerts.showSimpleTxtListAlert("Not a formula of " + derivationRuleset.getObjectLanguage().getNameString() + ":", texts);
                        resetHighlights();
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private boolean checkScopeStructure() {
        if (isTornDown || viewLines == null || derivationRuleset == null) return false;

        if (viewLines.size() > 0) {
            ViewLine firstLine = viewLines.get(0);
            if (firstLine != null) {
                if (!LineType.isContentLine(firstLine.getLineType())) {
                    EditorAlerts.showSimpleTxtListAlert("Derivation Start:", Collections.singletonList(ParseUtilities.newRegularText("Top line of derivation cannot be a 'shelf' or 'gap'.")));
                    return false;
                }
                if (firstLine.realDepth() > 2) {
                    highlightFormula(0);
                    EditorAlerts.showSimpleTxtListAlert("Derivation Start:", Collections.singletonList(ParseUtilities.newRegularText("Starting scope depth cannot be greater than 2.")));
                    resetHighlights();
                    return false;
                }
            }
        }
        if (viewLines.size() > 1) {
            ViewLine lastLine = viewLines.get(viewLines.size() - 1);
            if (lastLine != null) {
                if (!LineType.isContentLine(lastLine.getLineType())) {
                    EditorAlerts.showSimpleTxtListAlert("Derivation End", Collections.singletonList(ParseUtilities.newRegularText("Last line of derivation cannot be a 'shelf' or 'gap'.")));
                    return false;
                }
                if (lastLine.realDepth() != 1) {
                    highlightFormula(viewLines.size() - 1);
                    EditorAlerts.showSimpleTxtListAlert("Derivation End", Collections.singletonList(ParseUtilities.newRegularText("Last line of derivation cannot have scope depth greater than 1.")));
                    resetHighlights();
                    return false;
                }

                BoxedDRTA bottomDRTA = lastLine.getLineContentBoxedDRTA();
                if (bottomDRTA != null && bottomDRTA.getRTA() != null) {
                    RichTextArea bottomRTA = bottomDRTA.getRTA();
                    bottomRTA.getActionFactory().saveNow().execute(new ActionEvent());
                    Document bottomLineDoc = bottomRTA.getDocument();

                    if (bottomLineDoc != null && bottomLineDoc.getText() != null && bottomLineDoc.getText().equals("")) {
                        highlightFormula(viewLines.size() - 1);
                        EditorAlerts.showSimpleTxtListAlert("Derivation End", Collections.singletonList(ParseUtilities.newRegularText("Last line of derivation should be populated with a formula.")));
                        resetHighlights();
                        return false;

                    }
                }
            }
        }
        if (viewLines.isEmpty()) return true;

        int depth = viewLines.get(0).getDepth();
        for (int i = 0; i < viewLines.size(); i++) {
            ViewLine currentLine = viewLines.get(i);
            if (currentLine == null) continue;
            int currentDepth = currentLine.realDepth();

            if (currentDepth != 1 && !derivationRuleset.isPermitSubderivations()) {
                highlightLine(i);
                EditorAlerts.showSimpleTxtListAlert("Scope Increase:", Collections.singletonList(ParseUtilities.newRegularText("Basic " + derivationRuleset.getName() + " does not permit subderivations.")));
                resetHighlights();
                return false;
            }

            if (LineType.isGapLine(currentLine.getLineType())) {
                if (i > 0 && viewLines.get(i - 1) != null && currentDepth + 1 != viewLines.get(i - 1).realDepth() ) {
                    highlightFormula(i - 1);
                    EditorAlerts.showSimpleTxtListAlert("Gap Scope:", Collections.singletonList(ParseUtilities.newRegularText("Scope depth of line before gap should be one more than that of gap.")));
                    resetHighlights();
                    return false;
                }
                if (i < viewLines.size() - 1 && viewLines.get(i + 1) != null && currentDepth + 1 != viewLines.get(i + 1).realDepth()) {
                    highlightFormula(i + 1);
                    EditorAlerts.showSimpleTxtListAlert("Gap Scope:", Collections.singletonList(ParseUtilities.newRegularText("Scope depth of line after gap should be one more than that of gap.")));
                    resetHighlights();
                    return false;
                }
            }
            else if (LineType.isContentLine(currentLine.getLineType())) {
                if (currentLine.realDepth() > depth + 1 || currentLine.realDepth() < depth - 1) {
                    highlightFormula(i);
                    EditorAlerts.showSimpleTxtListAlert("Scope Jump:", Collections.singletonList(ParseUtilities.newRegularText("From one line to the next, scope depth cannot change by more than one.")));
                    resetHighlights();
                    return false;
                }
            }
            depth = currentDepth;
        }
        return true;
    }

    // SCORCHED EARTH RULE 7: Safe Global State wrapper
    private void safeScrollToNode(Node node) {
        try {
            if (derivationExercise != null && derivationExercise.getMainWindow() != null && derivationExercise.getMainWindow().getMainView() != null) {
                derivationExercise.getMainWindow().getMainView().scrollToNode(node);
            }
        } catch (Exception e) {}
    }

    private void highlightLine(int index) {
        if (isTornDown) return;
        highlightFormula(index);
        highlightJustification(index);
    }

    private void highlightJustification(int index) {
        if (isTornDown || viewLines == null || index < 0 || index >= viewLines.size()) return;
        try {
            ViewLine line = viewLines.get(index);
            if (line != null && line.getJustificationFlow() != null) {
                TextFlow justificationFlow = line.getJustificationFlow();
                justificationFlow.setStyle("-fx-background-color: mistyrose");
                line.setLineHighlight(true);
                markedLine = line;
            }
        } catch (Exception e) {}
    }

    private void highlightFormula(int index) {
        if (isTornDown || viewLines == null || index < 0 || index >= viewLines.size()) return;
        try {
            ViewLine line = viewLines.get(index);
            if (line != null && line.getLineContentBoxedDRTA() != null) {
                BoxedDRTA bdrta = line.getLineContentBoxedDRTA();
                bdrta.setDerHighlight();
                safeScrollToNode(bdrta.getRTA());
                markedLine = line;
            }
        } catch (Exception e) {}
    }

    private void resetHighlights() {
        if (isTornDown || markedLine == null) return;
        try {
            ViewLine line = markedLine;
            if (line.getLineContentBoxedDRTA() != null) {
                BoxedDRTA bdrta = line.getLineContentBoxedDRTA();
                bdrta.resetDerHighlight();
            }
            if (line.getJustificationFlow() != null) line.getJustificationFlow().setStyle("-fx-background-color: transparent");
            line.setLineHighlight(false);

            // CRITICAL LEAK FIX: We must clear the reference to the ViewLine so it doesn't anchor memory forever!
            markedLine = null;
        } catch (Exception e) {}
    }

    // SCORCHED EARTH RULE 1: Cascading Cleanup & Isolation
    public void cleanup() {

        // Reset state UI safely before nullifying
        try { resetHighlights(); } catch (Exception e) {}

        // KILL SWITCH: Engaged
        isTornDown = true;

        // SCORCHED EARTH RULE 5: Rule of Isolation
        try {
            if (derivationView != null) {
                if (derivationView.getCheckButton() != null && checkButtonHandler != null) {
                    derivationView.getCheckButton().removeEventHandler(ActionEvent.ACTION, checkButtonHandler);
                    derivationView.getCheckButton().setOnAction(null);
                }
            }
        } catch (Exception e) {}

        try {
            if (derivationView != null) {
                if (derivationView.getCheckProgButton() != null && checkProgButtonHandler != null) {
                    derivationView.getCheckProgButton().removeEventHandler(ActionEvent.ACTION, checkProgButtonHandler);
                    derivationView.getCheckProgButton().setOnAction(null);
                }
            }
        } catch (Exception e) {}

        try {
            if (derivationView != null) {
                if (derivationView.getStaticHelpButton() != null && staticHelpButtonHandler != null) {
                    derivationView.getStaticHelpButton().removeEventHandler(ActionEvent.ACTION, staticHelpButtonHandler);
                    derivationView.getStaticHelpButton().setOnAction(null);
                }
            }
        } catch (Exception e) {}



        // SCORCHED EARTH RULE 10: Non-destructive data nullifications
        try { this.viewLines = null; } catch (Exception e) {}

        try { MatchUtilities.clearFormMatch(); } catch (Exception e) {}

        // Final Explicit Class Nullifications
        this.derivationExercise = null;
        this.derivationView = null;
        this.derivationModel = null;
        this.derivationRuleset = null;
        this.markedLine = null;
        this.checkButtonHandler = null;
        this.checkProgButtonHandler = null;
        this.staticHelpButtonHandler = null;
    }

    public void setCheckMax(int checkMax) {
        this.checkMax = checkMax;
    }

    public void setCheckTries(int checkTries) {
        this.checkTries = checkTries;
    }

    public void setHelpMax(int helpMax) {
        this.helpMax = helpMax;
    }

    public void setHelpTries(int helpTries) {
        this.helpTries = helpTries;
    }

    public List<ViewLine> getViewLines() {
        return viewLines;
    }

    public DerivationRuleset getDerivationRuleset() {
        return derivationRuleset;
    }

    public Exercise getExercise() {
        return derivationExercise;
    }

    public int getCheckMax() {
        return checkMax;
    }

    public int getCheckTries() {
        return checkTries;
    }

    public int getHelpMax() {
        return helpMax;
    }

    public int getHelpTries() {
        return helpTries; }

    public boolean isCheckFinal() { return checkFinal; }

    public boolean isCheckSuccess() { return checkSuccess; }
}
